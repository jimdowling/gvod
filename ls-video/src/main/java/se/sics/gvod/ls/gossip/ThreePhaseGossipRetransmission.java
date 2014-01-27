package se.sics.gvod.ls.gossip;

import se.sics.gvod.ls.video.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import se.sics.gvod.common.RetryComponentDelegator;
import se.sics.gvod.common.Self;
import se.sics.gvod.ls.system.LSConfig;
import se.sics.gvod.ls.video.snapshot.VideoStats;
import se.sics.gvod.net.VodNetwork;
import se.sics.gvod.timer.CancelTimeout;
import se.sics.gvod.timer.ScheduleTimeout;
import se.sics.gvod.timer.Timer;
import se.sics.gvod.timer.UUID;
import se.sics.gvod.video.msgs.EncodedSubPiece;
import se.sics.gvod.video.msgs.VideoPieceMsg;
import se.sics.kompics.Positive;

/**
 *
 * @author Niklas Wahl&#233;n <nwahlen@kth.se>
 */
public class ThreePhaseGossipRetransmission extends ThreePhaseGossip {

    private Map<Integer, Integer> currentRequests; // <ID, requests left>
    private final int r = 2;

    public ThreePhaseGossipRetransmission(RetryComponentDelegator delegator, Self self, Positive<VodNetwork> network, Positive<Timer> timer, VideoNeighbours neighbours, Map<Integer, EncodedSubPiece> subPieceBuffer) {
        super(delegator, self, network, timer, neighbours, subPieceBuffer);
        currentRequests = new HashMap<Integer, Integer>();
    }
    /*
     * Phase 2 - Request chunks
     */

    @Override
    public void handleAdvertisement(VideoPieceMsg.Advertisement advertisement) {
        Set<Integer> wantedChunks = new HashSet<Integer>();
        boolean retriesLeft = false;
        for (Integer id : advertisement.getAdvertisedPiecesIds()) {
            if (!requestedSubPieces.contains(id)) {
                VideoStats.instance(self).incSeenSubPieces();
            }
            if (!requestedSubPieces.contains(id) || isBeingRetransmitted(id)) {
                wantedChunks.add(id);
                if (retriesLeft(id)) {
                    retriesLeft = true;
                }
            }
        }
        if (!wantedChunks.isEmpty()) {
            requestedSubPieces.addAll(wantedChunks);
            VideoPieceMsg.Request request = new VideoPieceMsg.Request(self.getAddress(), advertisement.getVodSource(), wantedChunks);
            delegator.doTrigger(request, network);
            if (retriesLeft) {
                startRetryTimer(request);
            }
        }
    }

    /*
     * Phase 3 - Push payload
     */
    @Override
    public void handlePieces(VideoPieceMsg.Response pieces) {
        super.handlePieces(pieces);
        cancelTimer(pieces);
    }

    /*
     * Retransmission
     */
    public void handleTimeout(VideoPieceMsg.RequestTimeout timeout) {
        VideoStats.instance(self).incSubPieceRequestTimeouts();
        VideoPieceMsg.Request request = timeout.getRequestMsg();
        VideoPieceMsg.Advertisement advertisement =
                new VideoPieceMsg.Advertisement(
                request.getVodDestination(),
                self.getAddress(),
                request.getPiecesIds());
        handleAdvertisement(advertisement);
    }

    public boolean isBeingRetransmitted(Integer id) {
        return currentRequests.containsKey(id);
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

    private void startRetryTimer(VideoPieceMsg.Request request) {
        ScheduleTimeout st = new ScheduleTimeout(LSConfig.VIDEO_PIECE_REQUEST_TIMEOUT);
        st.setTimeoutEvent(new VideoPieceMsg.RequestTimeout(st, request));
        for (Integer id : request.getPiecesIds()) {
            if (!currentRequests.containsKey(id)) {
                currentRequests.put(id, r);
            }
        }
        delegator.doTrigger(st, timer);
    }

    private void cancelTimer(VideoPieceMsg.Response response) {
        CancelTimeout ct = new CancelTimeout(response.getTimeoutId());
        delegator.doTrigger(ct, timer);
        currentRequests.remove(response.getEncodedSubPiece().getGlobalId());
    }
}
