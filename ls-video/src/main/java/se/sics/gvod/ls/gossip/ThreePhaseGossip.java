package se.sics.gvod.ls.gossip;

import se.sics.gvod.ls.video.*;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.gvod.common.RetryComponentDelegator;
import se.sics.gvod.common.Self;
import se.sics.gvod.ls.system.LSConfig;
import se.sics.gvod.ls.video.snapshot.VideoStats;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.net.VodNetwork;
import se.sics.gvod.timer.Timer;
import se.sics.gvod.timer.UUID;
import se.sics.gvod.video.msgs.EncodedSubPiece;
import se.sics.gvod.video.msgs.Piece;
import se.sics.gvod.video.msgs.VideoPieceMsg;
import se.sics.kompics.Positive;

/**
 *
 * @author Niklas Wahl&#233;n <nwahlen@kth.se>
 */
public class ThreePhaseGossip {

    protected final Logger logger = LoggerFactory.getLogger(Video.class);
    // References
    protected RetryComponentDelegator delegator;
    protected Self self;
    protected Positive<VodNetwork> network;
    protected Positive<Timer> timer;
    protected VideoNeighbours neighbours;
    // Data
    protected Map<Integer, EncodedSubPiece> subPieces;
    // Algorithm specific
    private int f;
    protected Set<Integer> subPiecesToPropose;
    protected Set<EncodedSubPiece> subPiecesDelivered;
    protected Set<Integer> requestedSubPieces;
    // Configuration
    protected int ulBwCapacity, dlBwCapacity; // bytes
    protected int uploaded, downloaded;

    public ThreePhaseGossip(RetryComponentDelegator delegator, Self self, Positive<VodNetwork> network, Positive<Timer> timer, VideoNeighbours neighbours, Map<Integer, EncodedSubPiece> subPieces) {
        // Algorithm specific
        // fanout f = ln(system size) + constant
        f = ((int) Math.log(500)) + 2;
        subPiecesToPropose = new HashSet<Integer>();
        subPiecesDelivered = new HashSet<EncodedSubPiece>();
        requestedSubPieces = new HashSet<Integer>();
        // References
        this.delegator = delegator;
        this.self = self;
        this.network = network;
        this.timer = timer;
        this.neighbours = neighbours;
        this.subPieces = subPieces;
        // Configuration
        ulBwCapacity = LSConfig.VIDEO_UPLOAD_CAPACITY; // in bytes
        dlBwCapacity = Integer.MAX_VALUE;
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

    public void cycle() {
        if (!subPiecesToPropose.isEmpty()) {
            gossip(new HashSet<Integer>(subPiecesToPropose));
            subPiecesToPropose.clear();
        }
        VideoStats.instance(self).setUlBwBytes(uploaded);
        VideoStats.instance(self).setDlBwBytes(downloaded);
        VideoStats.instance(self).setFanout((short) f);
        uploaded = 0;
        downloaded = 0;
    }

    /*
     * Phase 2 - Request pieces
     */
    public void handleAdvertisement(VideoPieceMsg.Advertisement advertisement) {
        Set<Integer> wantedSubPieces = new HashSet<Integer>();
        for (Integer id : advertisement.getAdvertisedPiecesIds()) {
            if (!requestedSubPieces.contains(id)) {
                wantedSubPieces.add(id);
                VideoStats.instance(self).incSeenSubPieces();
            }
        }
        if (!wantedSubPieces.isEmpty()) {
            requestedSubPieces.addAll(wantedSubPieces);
            delegator.doTrigger(new VideoPieceMsg.Request(self.getAddress(), advertisement.getVodSource(), wantedSubPieces), network);
        }
    }

    /*
     * Phase 3 - Push payload
     */
    public void handleRequest(VideoPieceMsg.Request request) {
        Set<EncodedSubPiece> askedChunks = new HashSet<EncodedSubPiece>();
        for (Integer id : request.getPiecesIds()) {
            if (uploaded + EncodedSubPiece.getSize() > ulBwCapacity) {
                break;
            }
            EncodedSubPiece p = getSubPiece(id);
            if (p != null) {
                delegator.doTrigger(new VideoPieceMsg.Response(self.getAddress(), request.getVodSource(), request.getTimeoutId(), p), network);
                incSent(request.getVodSource());
                uploaded += EncodedSubPiece.getSize();
            }
        }
        if (!askedChunks.isEmpty()) {
            //delegator.doTrigger(new VideoPieceMsg.Response(self.getAddress(), request.getVodSource(), request.getTimeoutId(), askedChunks), network);
        }
    }

    public void handlePieces(VideoPieceMsg.Response pieces) {
        EncodedSubPiece p = pieces.getEncodedSubPiece();
        if (!subPiecesDelivered.contains(p)) {
            downloaded += EncodedSubPiece.getSize();
            subPiecesToPropose.add(p.getGlobalId());
            deliverSubPiece(p);
        }
    }

    // Only called after local encoding, i.e. never when receiving 
    public void handlePiece(EncodedSubPiece esp) {
        if (!subPiecesDelivered.contains(esp)) {
            subPiecesToPropose.add(esp.getGlobalId());
            deliverSubPiece(esp);
        }
    }

    /*
     * Miscellaneous
     */
    public Collection<VodAddress> selectNodes(int f) {
        return neighbours.getNRandomNeighbours(f);
    }

    public EncodedSubPiece getSubPiece(Integer id) {
        return subPieces.get(id);
    }

    public void deliverSubPiece(EncodedSubPiece p) {
        subPiecesDelivered.add(p);
        // requestedSubPieces is the only set used for managing (sending of) 
        // requests, and if a subpiece was encoded locally (not received) it
        // was never added to this set previously
        requestedSubPieces.add(p.getGlobalId());
        subPieces.put(p.getGlobalId(), p);
    }

    public int getFanout() {
        return f;
    }

    public void gossip(Set<Integer> pieceIds) {
        Collection<VodAddress> communicationPartners = selectNodes(f);
        String str = "[";
        for (Integer i : pieceIds) {
            str += i + ",";
        }
        str += "]";
        logger.debug(self.getId() + ": gossiping Advertisement"
                + str + " to "
                + communicationPartners.size() + " peers.");
        for (VodAddress p : communicationPartners) {
            delegator.doTrigger(new VideoPieceMsg.Advertisement(self.getAddress(), p, pieceIds), network);
        }
    }

    /*
     * Stats
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
