/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.ls.video.snapshot;

import java.util.List;
import se.sics.gvod.net.VodAddress;

/**
 *
 * @author Niklas Wahl&#233;n <nwahlen@kth.se>
 */
public interface StatsIntf {

    int getAsn();

    float getAvgAsHopsToNeighbours();

    int getBufferLength();

    int getCompletePieces();

    int getConnectionRequestTimeoutsClose();

    int getConnectionRequestTimeoutsRandom();

    int getConnectionRequestsSentClose();

    int getConnectionRequestsSentRandom();

    int getConnectionResponsesReceivedClose();

    int getConnectionResponsesReceivedRandom();

    int getDisconnectsReceivedClose();

    int getDisconnectsReceivedRandom();

    int getDisconnectsSentClose();

    int getDisconnectsSentRandom();

    int getDlBwBytes();

    int getDownloadedSubPiecesIntraAs();

    int getDownloadedSubPiecesNeighbourAs();

    int getDownloadedSubPiecesOtherAs();

    int getExperimentId();

    int getExperimentIteration();

    short getFanout();

    int getHighestCompletePiece();

    int getIngoingConnectionsClose();

    int getIngoingConnectionsRandom();

    int getMissedPieces();

    VodAddress.NatType getNatType();

    List<VodAddress> getNeighbours();

    int getNodeId();

    int getOutgoingConnectionsClose();

    int getOutgoingConnectionsRandom();

    int getOverlayId();

    List<Integer> getPieceStats();

    int getSeenSubPieces();

    int getSentSubPiecesIntraAs();

    int getSentSubPiecesNeighbourAs();

    int getSentSubPiecesOtherAs();

    int getStep();

    int getStreamRate();
    
    int getStreamLag();

    int getSubPieceRequestTimeouts();

    int getUlBwBytes();

    boolean isSource();
    
}
