package se.sics.gvod.ls.video.snapshot;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.net.VodAddress.NatType;

/**
 * Bean for storing and sending statistics about a Video instance (a node).
 *
 * @author Niklas Wahl&#233;n <nwahlen@kth.se>
 */
@XmlRootElement
public class Stats implements StatsIntf {

    // Experiment identification
    private int experimentId;
    private int experimentIteration;
    private volatile int step;
    // Node identification
    private int overlayId;
    private int nodeId;
    // Node properties
    private boolean source;
    private VodAddress.NatType natType;
    private int asn;
    // Neighbour connections messages
    private volatile int connectionRequestsSentClose;
    private volatile int connectionRequestsSentRandom;
    private volatile int connectionRequestTimeoutsClose;
    private volatile int connectionRequestTimeoutsRandom;
    private volatile int connectionResponsesReceivedClose;
    private volatile int connectionResponsesReceivedRandom;
    // Neighbour disconncetion messages
    private volatile int disconnectsSentClose;
    private volatile int disconnectsSentRandom;
    private volatile int disconnectsReceivedClose;
    private volatile int disconnectsReceivedRandom;
    // Neighbour connections
    private volatile int ingoingConnectionsClose;
    private volatile int ingoingConnectionsRandom;
    private volatile int outgoingConnectionsClose;
    private volatile int outgoingConnectionsRandom;
    private volatile float avgAsHopsToNeighbours;
    private List<VodAddress> neighbours = new ArrayList<VodAddress>();
    // Video content
    private volatile int seenSubPieces;
    private int subPieceRequestTimeouts;
    private volatile int completePieces;
    private volatile int highestCompletePiece = 0;
    private volatile int downloadedSubPiecesIntraAs;
    private volatile int downloadedSubPiecesNeighbourAs;
    private volatile int downloadedSubPiecesOtherAs;
    private volatile int sentSubPiecesIntraAs;
    private volatile int sentSubPiecesNeighbourAs;
    private volatile int sentSubPiecesOtherAs;
    private short fanout;
    private List<Integer> pieceStats = new ArrayList<Integer>();
    // Playback
    private int dlBwBytes;
    private int ulBwBytes;
    private int streamRate;
    private int streamLag;
    private volatile int bufferLength;
    private volatile int missedPieces;

//-------------------------------------------------------------------
    public Stats() {
    }

    public Stats(int nodeId, int overlayId, boolean source) {
        this.nodeId = nodeId;
        this.overlayId = overlayId;
        this.source = source;
    }

    /*
     * Experiment identification
     */
    @Override
    public int getExperimentId() {
        return experimentId;
    }

    public void setExperimentId(int experimentId) {
        this.experimentId = experimentId;
    }

    @Override
    public int getExperimentIteration() {
        return experimentIteration;
    }

    public void setExperimentIteration(int experimentIteration) {
        this.experimentIteration = experimentIteration;
    }

    @Override
    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    /*
     * Node identification
     */
    public void setNodeId(int nodeId) {
        this.nodeId = nodeId;
    }

    @Override
    public int getNodeId() {
        return nodeId;
    }

    public void setOverlayId(int overlayId) {
        this.overlayId = overlayId;
    }

    @Override
    public int getOverlayId() {
        return overlayId;
    }

    /*
     * Node properties
     */
    public void setSource(boolean source) {
        this.source = source;
    }

    @Override
    public boolean isSource() {
        return source;
    }

    public void setNatType(NatType natType) {
        this.natType = natType;
    }

    @Override
    public VodAddress.NatType getNatType() {
        return natType;
    }

    /*
     * NEIGHBOUR CONNECTION MESSAGES
     */
    @Override
    public int getConnectionRequestTimeoutsClose() {
        return connectionRequestTimeoutsClose;
    }

    public void incConnectionRequestTimeoutsClose() {
        connectionRequestTimeoutsClose++;
    }

    public void setConnectionRequestTimeoutsClose(int connectionRequestTimeoutsClose) {
        this.connectionRequestTimeoutsClose = connectionRequestTimeoutsClose;
    }

    @Override
    public int getConnectionRequestTimeoutsRandom() {
        return connectionRequestTimeoutsRandom;
    }

    public void incConnectionRequestTimeoutsRandom() {
        connectionRequestTimeoutsRandom++;
    }

    public void setConnectionRequestTimeoutsRandom(int connectionRequestTimeoutsRandom) {
        this.connectionRequestTimeoutsRandom = connectionRequestTimeoutsRandom;
    }

    @Override
    public int getConnectionRequestsSentClose() {
        return connectionRequestsSentClose;
    }

    public void incConnectionRequestsSentClose() {
        connectionRequestsSentClose++;
    }

    public void setConnectionRequestsSentClose(int connectionRequestsSentClose) {
        this.connectionRequestsSentClose = connectionRequestsSentClose;
    }

    @Override
    public int getConnectionRequestsSentRandom() {
        return connectionRequestsSentRandom;
    }

    public void incConnectionRequestsSentRandom() {
        connectionRequestsSentRandom++;
    }

    public void setConnectionRequestsSentRandom(int connectionRequestsSentRandom) {
        this.connectionRequestsSentRandom = connectionRequestsSentRandom;
    }

    @Override
    public int getConnectionResponsesReceivedClose() {
        return connectionResponsesReceivedClose;
    }

    public void incConnectionResponsesReceivedClose() {
        connectionResponsesReceivedClose++;
    }

    public void setConnectionResponsesReceivedClose(int connectionResponsesReceivedClose) {
        this.connectionResponsesReceivedClose = connectionResponsesReceivedClose;
    }

    @Override
    public int getConnectionResponsesReceivedRandom() {
        return connectionResponsesReceivedRandom;
    }

    public void incConnectionResponsesReceivedRandom() {
        connectionResponsesReceivedRandom++;
    }

    public void setConnectionResponsesReceivedRandom(int connectionResponsesReceivedRandom) {
        this.connectionResponsesReceivedRandom = connectionResponsesReceivedRandom;
    }

    /*
     * NEIGHBOUR DISCONNECTION MESSAGES
     */
    @Override
    public int getDisconnectsReceivedClose() {
        return disconnectsReceivedClose;
    }

    public void incDisconnectsReceivedClose() {
        disconnectsReceivedClose++;
    }

    public void setDisconnectsReceivedClose(int disconnectsReceivedClose) {
        this.disconnectsReceivedClose = disconnectsReceivedClose;
    }

    @Override
    public int getDisconnectsReceivedRandom() {
        return disconnectsReceivedRandom;
    }

    public void incDisconnectsReceivedRandom() {
        disconnectsReceivedRandom++;
    }

    public void setDisconnectsReceivedRandom(int disconnectsReceivedRandom) {
        this.disconnectsReceivedRandom = disconnectsReceivedRandom;
    }

    @Override
    public int getDisconnectsSentClose() {
        return disconnectsSentClose;
    }

    public void incDisconnectsSentClose() {
        disconnectsSentClose++;
    }

    public void setDisconnectsSentClose(int disconnectsSentClose) {
        this.disconnectsSentClose = disconnectsSentClose;
    }

    @Override
    public int getDisconnectsSentRandom() {
        return disconnectsSentRandom;
    }

    public void incDisconnectsSentRandom() {
        disconnectsSentRandom++;
    }

    public void setDisconnectsSentRandom(int disconnectsSentRandom) {
        this.disconnectsSentRandom = disconnectsSentRandom;
    }

    /*
     * NEIGHBOUR CONNECTIONS
     */
    @Override
    public int getIngoingConnectionsClose() {
        return ingoingConnectionsClose;
    }

    public void setIngoingConnectionsClose(int ingoingConnectionsClose) {
        this.ingoingConnectionsClose = ingoingConnectionsClose;
    }

    @Override
    public int getIngoingConnectionsRandom() {
        return ingoingConnectionsRandom;
    }

    public void setIngoingConnectionsRandom(int ingoingConnectionsRandom) {
        this.ingoingConnectionsRandom = ingoingConnectionsRandom;
    }

    @Override
    public int getOutgoingConnectionsClose() {
        return outgoingConnectionsClose;
    }

    public void setOutgoingConnectionsClose(int outgoingConnectionsClose) {
        this.outgoingConnectionsClose = outgoingConnectionsClose;
    }

    @Override
    public int getOutgoingConnectionsRandom() {
        return outgoingConnectionsRandom;
    }

    public void setOutgoingConnectionsRandom(int outgoingConnectionsRandom) {
        this.outgoingConnectionsRandom = outgoingConnectionsRandom;
    }

    @Override
    public float getAvgAsHopsToNeighbours() {
        return avgAsHopsToNeighbours;
    }

    public void setAvgAsHopsToNeighbours(float avgAsHopsToNeighbours) {
        this.avgAsHopsToNeighbours = avgAsHopsToNeighbours;
    }

    @Override
    public List<VodAddress> getNeighbours() {
        return neighbours;
    }

    public void setNeighbours(List<VodAddress> neighbours) {
        this.neighbours = neighbours;
    }
    /*
     * VIDEO CONTENT
     */

    @Override
    public int getCompletePieces() {
        return completePieces;
    }

    public void incCompletePieces() {
        completePieces++;
    }

    public void setCompletePieces(int completePieces) {
        this.completePieces = completePieces;
    }

    @Override
    public int getHighestCompletePiece() {
        return highestCompletePiece;
    }

    public void setHighestCompletePiece(int highestCompletePiece) {
        this.highestCompletePiece = highestCompletePiece;
    }

    @Override
    public int getSeenSubPieces() {
        return seenSubPieces;
    }

    public void incSeenSubPieces() {
        seenSubPieces++;
    }

    public void setSeenSubPieces(int seenSubPieces) {
        this.seenSubPieces = seenSubPieces;
    }

    @Override
    public int getDownloadedSubPiecesIntraAs() {
        return downloadedSubPiecesIntraAs;
    }

    public void incDownloadedSubPiecesIntraAs() {
        downloadedSubPiecesIntraAs++;
    }

    public void setDownloadedSubPiecesIntraAs(int downloadedSubPiecesIntraAs) {
        this.downloadedSubPiecesIntraAs = downloadedSubPiecesIntraAs;
    }

    @Override
    public int getDownloadedSubPiecesNeighbourAs() {
        return downloadedSubPiecesNeighbourAs;
    }

    public void incDownloadedSubPiecesNeighbourAs() {
        downloadedSubPiecesNeighbourAs++;
    }

    public void setDownloadedSubPiecesNeighbourAs(int downloadedSubPiecesNeighbourAs) {
        this.downloadedSubPiecesNeighbourAs = downloadedSubPiecesNeighbourAs;
    }

    @Override
    public int getDownloadedSubPiecesOtherAs() {
        return downloadedSubPiecesOtherAs;
    }

    public void incDownloadedSubPiecesOtherAs() {
        downloadedSubPiecesOtherAs++;
    }

    public void setDownloadedSubPiecesOtherAs(int downloadedSubPiecesOtherAs) {
        this.downloadedSubPiecesOtherAs = downloadedSubPiecesOtherAs;
    }

    @Override
    public int getSentSubPiecesIntraAs() {
        return sentSubPiecesIntraAs;
    }

    public void incSentSubPiecesIntraAs() {
        sentSubPiecesIntraAs++;
    }

    public void setSentSubPiecesIntraAs(int sentSubPiecesIntraAs) {
        this.sentSubPiecesIntraAs = sentSubPiecesIntraAs;
    }

    @Override
    public int getSentSubPiecesNeighbourAs() {
        return sentSubPiecesNeighbourAs;
    }

    public void incSentSubPiecesNeighbourAs() {
        sentSubPiecesNeighbourAs++;
    }

    public void setSentSubPiecesNeighbourAs(int sentSubPiecesNeighbourAs) {
        this.sentSubPiecesNeighbourAs = sentSubPiecesNeighbourAs;
    }

    @Override
    public int getSentSubPiecesOtherAs() {
        return sentSubPiecesOtherAs;
    }

    public void incSentSubPiecesOtherAs() {
        sentSubPiecesOtherAs++;
    }

    public void setSentSubPiecesOtherAs(int sentSubPiecesOtherAs) {
        this.sentSubPiecesOtherAs = sentSubPiecesOtherAs;
    }

    @Override
    public List<Integer> getPieceStats() {
        return pieceStats;
    }

    public void setPieceStats(List<Integer> pieceStats) {
        this.pieceStats = pieceStats;
    }

    public void setCompletedPiece(int pieceId) {
        if (!pieceStats.contains(pieceId)) {
            pieceStats.add(pieceId);
            incCompletePieces();
            if (highestCompletePiece < pieceId) {
                highestCompletePiece = pieceId;
            }
        }
    }

    /*
     * PLAYBACK
     */
    @Override
    public int getBufferLength() {
        return bufferLength;
    }

    public void setBufferLength(int bufferLength) {
        this.bufferLength = bufferLength;
    }

    @Override
    public int getMissedPieces() {
        return missedPieces;
    }
    
    public void incMissedPieces() {
        missedPieces++;
    }

    public void setMissedPieces(int missedPieces) {
        this.missedPieces = missedPieces;
    }

    @Override
    public int getAsn() {
        return asn;
    }

    public void setAsn(int asn) {
        this.asn = asn;
    }

    @Override
    public int getDlBwBytes() {
        return dlBwBytes;
    }

    public void setDlBwBytes(int dlBwBytes) {
        this.dlBwBytes = dlBwBytes;
    }

    @Override
    public short getFanout() {
        return fanout;
    }

    public void setFanout(short fanout) {
        this.fanout = fanout;
    }

    @Override
    public int getStreamRate() {
        return streamRate;
    }

    public void setStreamRate(int streamRate) {
        this.streamRate = streamRate;
    }

    public int getStreamLag() {
        return streamLag;
    }

    public void setStreamLag(int streamLag) {
        this.streamLag = streamLag;
    }

    @Override
    public int getSubPieceRequestTimeouts() {
        return subPieceRequestTimeouts;
    }
    
    public void incSubPieceRequestTimeouts() {
        subPieceRequestTimeouts++;
    }

    public void setSubPieceRequestTimeouts(int subPieceRequestTimeouts) {
        this.subPieceRequestTimeouts = subPieceRequestTimeouts;
    }

    @Override
    public int getUlBwBytes() {
        return ulBwBytes;
    }

    public void setUlBwBytes(int ulBwBytes) {
        this.ulBwBytes = ulBwBytes;
    }

    public String toString() {
        return "Peer : nat(" + this.natType + ")";
    }
}
