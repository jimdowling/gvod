package se.sics.gvod.ls.video;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.gvod.common.RetryComponentDelegator;
import se.sics.gvod.common.Self;
import se.sics.gvod.ls.system.LSConfig;
import se.sics.gvod.ls.video.snapshot.VideoStats;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.net.VodNetwork;
import se.sics.gvod.timer.*;
import se.sics.gvod.timer.Timer;
import se.sics.gvod.timer.UUID;
import se.sics.gvod.video.msgs.EncodedSubPiece;
import se.sics.gvod.video.msgs.VideoPieceMsg;
import se.sics.kompics.Positive;

/**
 * Provides three-phase gossiping of EncodedSubPieces. Improved implementation
 * of Three-Phase Gossip presented in [Gossip++ p.38], similar to their Claim
 * algorithm [p.55].
 *
 * @author Niklas Wahl&#233;n <nwahlen@kth.se>
 */
public class VideoGossip {

    protected final Logger logger = LoggerFactory.getLogger(VideoGossip.class);
    // References
    protected RetryComponentDelegator delegator;
    protected Self self;
    protected Positive<VodNetwork> network;
    protected Positive<Timer> timer;
    protected VideoNeighbours neighbours;
    // Data
    protected Map<Integer, EncodedSubPiece> subPiecesDelivered;
    // Algorithm specific
    private int f;
    protected Set<Integer> subPiecesToPropose;
    protected Set<Integer> requestedSubPieces;
    // Configuration
    protected int ulBwCapacity, dlBwCapacity; // bytes
    protected int uploaded, downloaded;
    private boolean highUploadWarning;
    // congestion control
    private int lowUploadWarnings, highUploadWarnings;
    private boolean source;
    // Retransmission
    private Map<Integer, Integer> currentRequests; // <ID, requests left>
    private Map<Integer, BlockingQueue<VodAddress>> piecesProviders; // <ID, proviers>
    private Set<TimeoutId> timeoutIds;
    // Tools
    private Random random;

    
    public VideoGossip(RetryComponentDelegator delegator, Self self, Positive<VodNetwork> network, Positive<Timer> timer, VideoNeighbours neighbours, Map<Integer, EncodedSubPiece> subPieceBuffer, Set<TimeoutId> timeoutIds, boolean source) {
        // Algorithm specific
        // fanout f = ln(system size) + constant
        //f = ((int) Math.log(500)) + 2;
        subPiecesToPropose = new HashSet<Integer>();
        requestedSubPieces = new HashSet<Integer>();
        // References
        this.delegator = delegator;
        this.self = self;
        this.network = network;
        this.timer = timer;
        this.neighbours = neighbours;
        this.subPiecesDelivered = subPieceBuffer;
        // Retransmission
        currentRequests = new HashMap<Integer, Integer>();
        piecesProviders = new HashMap<Integer, BlockingQueue<VodAddress>>();
        this.timeoutIds = timeoutIds;
        // Configuration
        if (source) {
            ulBwCapacity = LSConfig.VIDEO_SOURCE_UPLOAD_CAPACITY;
            f = 5;
        } else {
            ulBwCapacity = LSConfig.VIDEO_UPLOAD_CAPACITY; // in bytes
            f = 8;
        }
        dlBwCapacity = Integer.MAX_VALUE;
        uploaded = 0;
        downloaded = 0;
        highUploadWarning = false;
        this.source = source;
        random = new Random(LSConfig.getSeed());
    }

    public void cycle() {
        if (!subPiecesToPropose.isEmpty()) {
            gossip(new HashSet<Integer>(subPiecesToPropose));
            subPiecesToPropose.clear();
        }
        if (highUploadWarning) {
            highUploadWarnings++;
        } else {
            highUploadWarnings = 0;
        }
        // it is only possible to continue uploading if source or something was downloaded
        if ((source || (downloaded > 0)) && (ulBwCapacity - uploaded) > (10 * EncodedSubPiece.getSize())) {
            lowUploadWarnings++;
        } else {
            lowUploadWarnings = 0;
        }
        updateFanout();
        VideoStats.instance(self).setFanout((short) f);
        VideoStats.instance(self).setUlBwBytes(uploaded);
        VideoStats.instance(self).setDlBwBytes(downloaded);
        // reset
        highUploadWarning = false;
        uploaded = 0;
        downloaded = 0;
    }

    /*
     * Phase 1 - Gossip chunk ids
     */
    public void publish(EncodedSubPiece p) {
        if (p == null) {
            throw new IllegalArgumentException("null Piece not allowed");
        }
        deliverSubPiece(p);
        Set<Integer> pieceIds = new HashSet<Integer>();
        pieceIds.add(p.getGlobalId());
        gossip(pieceIds);
    }

    /*
     * Phase 2 - Request chunks
     */
    public void handleAdvertisement(VideoPieceMsg.Advertisement advertisement) {
        Set<Integer> wantedChunks = new HashSet<Integer>();
        for (Integer id : advertisement.getAdvertisedPiecesIds()) {
            if (!requestedSubPieces.contains(id)) {
                VideoStats.instance(self).incSeenSubPieces();
                addPieceProvider(id, advertisement.getVodSource());
                wantedChunks.add(id);
            }
            // a request is currently outstanding for this piece.
            if (isBeingRetransmitted(id)) {
                addPieceProvider(id, advertisement.getVodSource());
            }
        }
        if (!wantedChunks.isEmpty()) {
            requestedSubPieces.addAll(wantedChunks);
            VideoPieceMsg.Request request = new VideoPieceMsg.Request(self.getAddress(), advertisement.getVodSource(), wantedChunks);
            delegator.doRetry(request, self.getOverlayId());
            startRetryTimer(request, LSConfig.VIDEO_PIECE_REQUEST_TIMEOUT);
        }
    }

    /*
     * Phase 3 - Push payload
     */
    public void handleRequest(VideoPieceMsg.Request request) {
        for (Integer id : request.getPiecesIds()) {
            if (uploaded + EncodedSubPiece.getSize() > ulBwCapacity) {
                highUploadWarning = true;
                break;
            }
            if (random.nextDouble() < LSConfig.VIDEO_MESSAGE_DROP_RATIO) {
                break;
            }
            EncodedSubPiece p = getSubPiece(id);
            if (p != null) {
                delegator.doTrigger(new VideoPieceMsg.Response(self.getAddress(), request.getVodSource(), request.getTimeoutId(), p), network);
                incSent(request.getVodSource());
                uploaded += EncodedSubPiece.getSize();
            }
        }
    }

    public void handlePieces(VideoPieceMsg.Response response) {
        EncodedSubPiece p = response.getEncodedSubPiece();
        if (!subPiecesDelivered.containsKey(p.getGlobalId())) {
            downloaded += EncodedSubPiece.getSize();
            subPiecesToPropose.add(p.getGlobalId());
            deliverSubPiece(p);
        }
        cancelTimer(response);
    }

    /*
     * Retransmission
     */
    public void handleTimeout(VideoPieceMsg.RequestTimeout timeout) {
        VideoStats.instance(self).incSubPieceRequestTimeouts();
        VideoPieceMsg.Request request = timeout.getRequestMsg();
        if (request.getVodSource().getId() != self.getId()) {
            return;
        }
        Set<Integer> wantedChunks;
        // Check for non-received sub-pieces
        for (Integer id : request.getPiecesIds()) {
            if (!subPiecesDelivered.containsKey(id)) {
                wantedChunks = new HashSet(1);
                wantedChunks.add(id);
                // Get new provider
                VodAddress newProvider = getNextPieceProvider(id);
                if (newProvider != null) {
                    VideoPieceMsg.Request newRequest = new VideoPieceMsg.Request(self.getAddress(), newProvider,  wantedChunks);
                    delegator.doRetry(newRequest, self.getOverlayId());
                    if (retriesLeft(id)) {
                        long newDelay = (long) (timeout.getDelay() * LSConfig.VIDEO_PIECE_REQUEST_TIMEOUT_SCALE);
                        if (newDelay < LSConfig.VIDEO_PIECE_REQUEST_TIMEOUT_MIN) {
                            newDelay = LSConfig.VIDEO_PIECE_REQUEST_TIMEOUT_MIN;
                        }
                        startRetryTimer(newRequest, newDelay);
                    }
                }
            }
        }
    }

    public boolean isBeingRetransmitted(Integer id) {
        return currentRequests.containsKey(id);
    }

    /*
     * Miscellaneous
     */
    public Collection<VodAddress> selectNodes(int f) {
        return neighbours.getNRandomNeighbours(f);
    }

    public EncodedSubPiece getSubPiece(Integer id) {
        return subPiecesDelivered.get(id);
    }

    public void deliverSubPiece(EncodedSubPiece p) {
        // requestedSubPieces is the only set used for managing (sending of) 
        // requests, and if a subpiece was encoded locally (not received) it
        // was never added to this set previously, so we have to make sure the
        // piece is added to this set
        requestedSubPieces.add(p.getGlobalId());
        subPiecesDelivered.put(p.getGlobalId(), p);
    }

    public int getFanout() {
        return f;
    }

    public void gossip(Set<Integer> pieceIds) {
        Collection<VodAddress> communicationPartners = selectNodes(f);
        for (VodAddress p : communicationPartners) {
            delegator.doTrigger(
                    new VideoPieceMsg.Advertisement(self.getAddress(), p, pieceIds), network);
        }
    }

    /*
     * Other
     */
    private boolean retriesLeft(Integer id) {
        Integer retries = currentRequests.get(id);
        if (retries == null) {
            return true;
        } else if (retries > 0) {
            retries--;
            currentRequests.put(id, retries);
            return true;
        }
        return false;
    }

    private void startRetryTimer(VideoPieceMsg.Request request, long timeoutDelay) {
        ScheduleTimeout st = new ScheduleTimeout(timeoutDelay);
        st.setTimeoutEvent(new VideoPieceMsg.RequestTimeout(st, request));
        timeoutIds.add(st.getTimeoutEvent().getTimeoutId());
        for (Integer id : request.getPiecesIds()) {
            if (!currentRequests.containsKey(id)) {
                currentRequests.put(id, LSConfig.VIDEO_PIECE_REQUEST_RETRIES);
            }
        }
        delegator.doTrigger(st, timer);
    }

    private void cancelTimer(VideoPieceMsg.Response response) {
        CancelTimeout ct = new CancelTimeout(response.getTimeoutId());
        delegator.doTrigger(ct, timer);
        timeoutIds.remove(response.getTimeoutId());
        currentRequests.remove(response.getEncodedSubPiece().getGlobalId());
        piecesProviders.remove(response.getEncodedSubPiece().getGlobalId());
    }

    // Only called after local encoding, i.e. never when receiving 
    public void handlePiece(EncodedSubPiece esp) {
        if (!subPiecesDelivered.containsKey(esp.getGlobalId())) {
            subPiecesToPropose.add(esp.getGlobalId());
            deliverSubPiece(esp);
        }
    }

    /*
     * Enable record of advertisers
     */
    private void addPieceProvider(Integer pieceId, VodAddress a) {
        BlockingQueue<VodAddress> pieceProviders = piecesProviders.get(pieceId);
        if (pieceProviders == null) {
            pieceProviders = new LinkedBlockingQueue<VodAddress>();
        }
        if (!pieceProviders.contains(a)) {
            try {
                pieceProviders.put(a);
            } catch (InterruptedException ex) {
            }
        }
        piecesProviders.put(pieceId, pieceProviders);
    }

    /**
     * Get known providers for a piece in a round-robin manner. The first
     * provider in the list for the
     * <code>pieceId</code> is moved to the end of the queue and returned.
     *
     * @param id
     * @return The first provider in the queue for this encoded sub-piece id
     */
    private VodAddress getNextPieceProvider(Integer id) {
        BlockingQueue<VodAddress> pieceProviders = piecesProviders.get(id);
        if (pieceProviders == null || pieceProviders.isEmpty()) {
            return null;
        }
        VodAddress provider = pieceProviders.poll();
        try {
            pieceProviders.put(provider);
        } catch (InterruptedException ex) {
            return null;
        }
        return provider;
    }

    private void updateFanout() {
        if (lowUploadWarnings >= 5) {
            f++;
            lowUploadWarnings = 0;
        } else if (highUploadWarnings >= 5) {
            f--;
            highUploadWarnings = 0;
        }
        // the source _has_ to be able to serve a certain number of peers
        if(source && f < 5) {
            f = 5;
        } else if(f < 1){
            f = 1;
        }
        // there is no need (nor good) to have too high fanout
        if(f > LSConfig.VIDEO_MAX_FANOUT) {
            // 20 is a resonable limit for most systems (Gossip++)
            f = LSConfig.VIDEO_MAX_FANOUT;
        }
    }

    public int getDownloaded() {
        return downloaded;
    }

    /*
     * Statistics
     */
    private void incSent(VodAddress a) {
        short distance = neighbours.getDistanceTo(a);
        if (distance == 0) {
            VideoStats.instance(self).incSentSubPiecesIntraAs();
        } else if (distance == 1) {
            VideoStats.instance(self).incSentSubPiecesNeighbourAs();
        } else {
            VideoStats.instance(self).incSentSubPiecesOtherAs();
        }
    }
}
