package se.sics.gvod.ls.ws;

import se.sics.gvod.ls.video.snapshot.Experiment;
import se.sics.gvod.ls.video.snapshot.Stats;
import se.sics.gvod.ls.ws.persistent.ExperimentEntity;
import se.sics.gvod.ls.ws.persistent.StatsEntity;
import se.sics.gvod.net.VodAddress.NatType;

/**
 *
 * @author Niklas Wahl&#233;n <nwahlen@kth.se>
 */
public class WS {

    public static Stats getStats(StatsEntity se) {
        if (se == null) {
            throw new NullPointerException();
        }
        Stats s = new Stats();
        // Node identification
        s.setOverlayId(se.getOverlayId());
        s.setNodeId(se.getNodeId());
        // Experiment identification
        s.setExperimentId(se.getExperimentId());
        s.setExperimentIteration(se.getExperimentIteration());
        s.setStep(se.getStep());
        // Node properties
        s.setSource(se.getSource());
        if(se.getNatType() == 0) {
            s.setNatType(NatType.OPEN);
        } else if(se.getNatType() == 1) {
            s.setNatType(NatType.NAT);
        }
        s.setAsn(se.getAsn());
        // Neighbour connections messages
        s.setConnectionRequestsSentClose(se.getConnectionRequestsSentClose());
        s.setConnectionRequestsSentRandom(se.getConnectionRequestsSentRandom());
        s.setConnectionRequestTimeoutsClose(se.getConnectionRequestTimeoutsClose());
        s.setConnectionRequestTimeoutsRandom(se.getConnectionRequestTimeoutsRandom());
        s.setConnectionResponsesReceivedClose(se.getConnectionResponsesReceivedClose());
        s.setConnectionResponsesReceivedRandom(se.getConnectionResponsesReceivedRandom());
        // Neighbour disconnections messages
        s.setDisconnectsSentClose(se.getDisconnectsSentClose());
        s.setDisconnectsSentRandom(se.getDisconnectsSentRandom());
        s.setDisconnectsReceivedClose(se.getDisconnectsReceivedClose());
        s.setDisconnectsReceivedRandom(se.getDisconnectsReceivedRandom());
        // Neighbour connections
        s.setIngoingConnectionsClose((short) se.getIngoingConnectionsClose());
        s.setIngoingConnectionsRandom((short) se.getIngoingConnectionsRandom());
        s.setOutgoingConnectionsClose((short) se.getOutgoingConnectionsClose());
        s.setOutgoingConnectionsRandom((short) se.getOutgoingConnectionsRandom());
        s.setAvgAsHopsToNeighbours(se.getAvgAsHopsToNeighbours());
        // Video content
        s.setSeenSubPieces(se.getSeenSubPieces());
        s.setSubPieceRequestTimeouts(se.getSubPieceRequestTimeouts());
        s.setCompletePieces(se.getCompletePieces());
        s.setHighestCompletePiece(se.getHighestCompletePiece());
        s.setDownloadedSubPiecesIntraAs(se.getDownloadedSubPiecesIntraAs());
        s.setDownloadedSubPiecesNeighbourAs(se.getDownloadedSubPiecesNeighbourAs());
        s.setDownloadedSubPiecesOtherAs(se.getDownloadedSubPiecesOtherAs());
        s.setSentSubPiecesIntraAs(se.getSentSubPiecesIntraAs());
        s.setSentSubPiecesNeighbourAs(se.getSentSubPiecesNeighbourAs());
        s.setSentSubPiecesOtherAs(se.getSentSubPiecesOtherAs());
        s.setFanout(se.getFanout());
        s.setDlBwBytes(se.getDlBwBytes());
        s.setUlBwBytes(se.getUlBwBytes());
        // Playback
        s.setStreamRate(se.getStreamRate());
        s.setStreamLag(se.getStreamLag());
        s.setBufferLength(se.getBufferLength());
        s.setMissedPieces(se.getMissedPieces());
        return s;
    }

    public static StatsEntity getStatsEntity(Stats s) {
        if (s == null) {
            throw new NullPointerException();
        }
        StatsEntity se = new StatsEntity();
        // Node identification
        se.setOverlayId(s.getOverlayId());
        se.setNodeId(s.getNodeId());
        // Experiment identification
        se.setExperimentId(s.getExperimentId());
        se.setExperimentIteration(s.getExperimentIteration());
        se.setStep(s.getStep());
        // Node properties
        se.setSource(s.isSource());
        if(s.getNatType() == NatType.OPEN) {
            se.setNatType((short) 0);
        } else if(s.getNatType() == NatType.NAT) {
            se.setNatType((short) 1);
        }
        se.setAsn(s.getAsn());
        // Neighbour connections messages
        se.setConnectionRequestsSentClose(s.getConnectionRequestsSentClose());
        se.setConnectionRequestsSentRandom(s.getConnectionRequestsSentRandom());
        se.setConnectionRequestTimeoutsClose(s.getConnectionRequestTimeoutsClose());
        se.setConnectionRequestTimeoutsRandom(s.getConnectionRequestTimeoutsRandom());
        se.setConnectionResponsesReceivedClose(s.getConnectionResponsesReceivedClose());
        se.setConnectionResponsesReceivedRandom(s.getConnectionResponsesReceivedRandom());
        // Neighbour disconnections messages
        se.setDisconnectsSentClose(s.getDisconnectsSentClose());
        se.setDisconnectsSentRandom(s.getDisconnectsSentRandom());
        se.setDisconnectsReceivedClose(s.getDisconnectsReceivedClose());
        se.setDisconnectsReceivedRandom(s.getDisconnectsReceivedRandom());
        // Neighbour connections
        se.setIngoingConnectionsClose((short) s.getIngoingConnectionsClose());
        se.setIngoingConnectionsRandom((short) s.getIngoingConnectionsRandom());
        se.setOutgoingConnectionsClose((short) s.getOutgoingConnectionsClose());
        se.setOutgoingConnectionsRandom((short) s.getOutgoingConnectionsRandom());
        se.setAvgAsHopsToNeighbours(s.getAvgAsHopsToNeighbours());
        // Video content
        se.setSeenSubPieces(s.getSeenSubPieces());
        se.setSubPieceRequestTimeouts(s.getSubPieceRequestTimeouts());
        se.setCompletePieces(s.getCompletePieces());
        se.setHighestCompletePiece(s.getHighestCompletePiece());
        se.setDownloadedSubPiecesIntraAs(s.getDownloadedSubPiecesIntraAs());
        se.setDownloadedSubPiecesNeighbourAs(s.getDownloadedSubPiecesNeighbourAs());
        se.setDownloadedSubPiecesOtherAs(s.getDownloadedSubPiecesOtherAs());
        se.setSentSubPiecesIntraAs(s.getSentSubPiecesIntraAs());
        se.setSentSubPiecesNeighbourAs(s.getSentSubPiecesNeighbourAs());
        se.setSentSubPiecesOtherAs(s.getSentSubPiecesOtherAs());
        se.setFanout(s.getFanout());
        se.setDlBwBytes(s.getDlBwBytes());
        se.setUlBwBytes(s.getUlBwBytes());
        // Playback
        se.setStreamRate(s.getStreamRate());
        se.setStreamLag(s.getStreamLag());
        se.setBufferLength(s.getBufferLength());
        se.setMissedPieces(s.getMissedPieces());
        return se;
    }

    /*
     * EXPERIMENT
     */
    public static Experiment getExperiment(ExperimentEntity ee) {
        if (ee == null) {
            return null;
        }
        Experiment e = new Experiment(ee.getId());
        // Video Configuration
        e.setMaxOutClose(ee.getMaxOutClose());
        e.setMaxOutRandom(ee.getMaxOutRandom());
        e.setSpPerPiece(ee.getSpPerPiece());
        e.setRedundantSps(ee.getRedundantSps());
        e.setArguments(ee.getArguments());
        e.setScenario(ee.getScenario());
        // Other Configuration
        e.setExpLength(ee.getExpLength());
        e.setLocalHistory(ee.getLocalHistory());
        e.setNeighbourHistory(ee.getNeighbourHistory());
        e.setNodeSelection(ee.getNodeSelection());
        // Info
        e.setIterations(ee.getIterations());
        e.setStartTs(ee.getStartTs());
        e.setEndTs(ee.getEndTs());
        if (ee.getStatus() != null) {
            if (ee.getStatus().equals("opened")) {
                e.setStatus(Experiment.Status.opened);
            } else if (ee.getStatus().equals("running")) {
                e.setStatus(Experiment.Status.running);
            } else if (ee.getStatus().equals("paused")) {
                e.setStatus(Experiment.Status.paused);
            } else if (ee.getStatus().equals("finished")) {
                e.setStatus(Experiment.Status.finished);
            } else if (ee.getStatus().equals("failed")) {
                e.setStatus(Experiment.Status.failed);
            }
        }
        return e;
    }

    public static ExperimentEntity getExperimentEntity(Experiment e) {
        if (e == null) {
            return null;
        }
        ExperimentEntity ee = new ExperimentEntity(e.getId());
        // Video Configuration
        ee.setMaxOutClose(e.getMaxOutClose());
        ee.setMaxOutRandom(e.getMaxOutRandom());
        ee.setSpPerPiece(e.getSpPerPiece());
        ee.setRedundantSps(e.getRedundantSps());
        ee.setArguments(e.getArguments());
        ee.setScenario(e.getScenario());
        // Other Configuration
        ee.setExpLength(e.getExpLength());
        ee.setLocalHistory(e.getLocalHistory());
        ee.setNeighbourHistory(e.getNeighbourHistory());
        ee.setNodeSelection(e.getNodeSelection());
        // Info
        ee.setIterations(e.getIterations());
        ee.setStartTs(e.getStartTs());
        ee.setEndTs(e.getEndTs());
        if (e.getStatus() != null) {
            switch (e.getStatus()) {
                case opened:
                    ee.setStatus("opened");
                    break;
                case running:
                    ee.setStatus("running");
                    break;
                case paused:
                    ee.setStatus("paused");
                    break;
                case finished:
                    ee.setStatus("finished");
                    break;
                case failed:
                    ee.setStatus("failed");
                    break;
                default:
                    ee.setStatus("opened");
                    break;
            }
        }
        return ee;
    }
}
