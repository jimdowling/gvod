package se.sics.gvod.ls.ws.persistent;

import java.io.Serializable;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Niklas Wahl&#233;n <nwahlen@kth.se>
 */
@Entity
@Table(name = "stats")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "StatsEntity.findAll", query = "SELECT s FROM StatsEntity s"),
    @NamedQuery(name = "StatsEntity.findByExperimentIteration", query = "SELECT s FROM StatsEntity s WHERE s.experimentId = :id AND s.experimentIteration = :iteration"),
    @NamedQuery(name = "StatsEntity.maxExperimentId", query = "SELECT max(s.experimentId) FROM StatsEntity s")})
public class StatsEntity implements Serializable {
    @Basic(optional = false)
    @NotNull
    @Column(name = "asn", nullable = false)
    private int asn;
    @Basic(optional = false)
    @NotNull
    @Column(name = "sub_piece_request_timeouts", nullable = false)
    private int subPieceRequestTimeouts;
    @Basic(optional = false)
    @NotNull
    @Column(name = "fanout", nullable = false)
    private short fanout;
    @Basic(optional = false)
    @NotNull
    @Column(name = "dl_bw_bytes", nullable = false)
    private int dlBwBytes;
    @Basic(optional = false)
    @NotNull
    @Column(name = "ul_bw_bytes", nullable = false)
    private int ulBwBytes;
    @Basic(optional = false)
    @NotNull
    @Column(name = "stream_rate", nullable = false)
    private int streamRate;
    @Basic(optional = false)
    @NotNull
    @Column(name = "stream_lag", nullable = false)
    private int streamLag;
    @Basic(optional = false)
    @NotNull
    @Column(name = "downloaded_sub_pieces_intra_as")
    private int downloadedSubPiecesIntraAs;
    @Basic(optional = false)
    @NotNull
    @Column(name = "downloaded_sub_pieces_neighbour_as")
    private int downloadedSubPiecesNeighbourAs;
    @Basic(optional = false)
    @NotNull
    @Column(name = "downloaded_sub_pieces_other_as")
    private int downloadedSubPiecesOtherAs;
    @Basic(optional = false)
    @NotNull
    @Column(name = "sent_sub_pieces_intra_as")
    private int sentSubPiecesIntraAs;
    @Basic(optional = false)
    @NotNull
    @Column(name = "sent_sub_pieces_neighbour_as")
    private int sentSubPiecesNeighbourAs;
    @Basic(optional = false)
    @NotNull
    @Column(name = "sent_sub_pieces_other_as")
    private int sentSubPiecesOtherAs;
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
//    @NotNull
// http://netbeans.org/bugzilla/show_bug.cgi?id=197845
    @Column(name = "id", nullable = false)
    private Integer id;
    @Basic(optional = false)
    @NotNull
    @Column(name = "overlay_id", nullable = false)
    private int overlayId;
    @Basic(optional = false)
    @NotNull
    @Column(name = "node_id", nullable = false)
    private int nodeId;
    @Column(name = "experiment_id")
    private Integer experimentId;
    @Column(name = "experiment_iteration")
    private Integer experimentIteration;
    @Basic(optional = false)
    @NotNull
    @Column(name = "step", nullable = false)
    private int step;
    @Basic(optional = false)
    @NotNull
    @Column(name = "source", nullable = false)
    private boolean source;
    @Basic(optional = false)
    @NotNull
    @Column(name = "nat_type", nullable = false)
    private short natType;
    @Basic(optional = false)
    @NotNull
    @Column(name = "connection_requests_sent_close", nullable = false)
    private int connectionRequestsSentClose;
    @Basic(optional = false)
    @NotNull
    @Column(name = "connection_requests_sent_random", nullable = false)
    private int connectionRequestsSentRandom;
    @Basic(optional = false)
    @NotNull
    @Column(name = "connection_request_timeouts_close", nullable = false)
    private int connectionRequestTimeoutsClose;
    @Basic(optional = false)
    @NotNull
    @Column(name = "connection_request_timeouts_random", nullable = false)
    private int connectionRequestTimeoutsRandom;
    @Basic(optional = false)
    @NotNull
    @Column(name = "connection_responses_received_close", nullable = false)
    private int connectionResponsesReceivedClose;
    @Basic(optional = false)
    @NotNull
    @Column(name = "connection_responses_received_random", nullable = false)
    private int connectionResponsesReceivedRandom;
    @Basic(optional = false)
    @NotNull
    @Column(name = "disconnects_sent_close", nullable = false)
    private int disconnectsSentClose;
    @Basic(optional = false)
    @NotNull
    @Column(name = "disconnects_sent_random", nullable = false)
    private int disconnectsSentRandom;
    @Basic(optional = false)
    @NotNull
    @Column(name = "disconnects_received_close", nullable = false)
    private int disconnectsReceivedClose;
    @Basic(optional = false)
    @NotNull
    @Column(name = "disconnects_received_random", nullable = false)
    private int disconnectsReceivedRandom;
    @Basic(optional = false)
    @NotNull
    @Column(name = "ingoing_connections_close", nullable = false)
    private short ingoingConnectionsClose;
    @Basic(optional = false)
    @NotNull
    @Column(name = "ingoing_connections_random", nullable = false)
    private short ingoingConnectionsRandom;
    @Basic(optional = false)
    @NotNull
    @Column(name = "outgoing_connections_close", nullable = false)
    private short outgoingConnectionsClose;
    @Basic(optional = false)
    @NotNull
    @Column(name = "outgoing_connections_random", nullable = false)
    private short outgoingConnectionsRandom;
    @Basic(optional = false)
    @NotNull
    @Column(name = "avg_as_hops_to_neighbours", nullable = false)
    private float avgAsHopsToNeighbours;
    @Basic(optional = false)
    @NotNull
    @Column(name = "seen_sub_pieces", nullable = false)
    private int seenSubPieces;
    @Basic(optional = false)
    @NotNull
    @Column(name = "complete_pieces", nullable = false)
    private int completePieces;
    @Basic(optional = false)
    @NotNull
    @Column(name = "highest_complete_piece", nullable = false)
    private int highestCompletePiece;
    @Basic(optional = false)
    @NotNull
    @Column(name = "buffer_length", nullable = false)
    private int bufferLength;
    @Basic(optional = false)
    @NotNull
    @Column(name = "missed_pieces", nullable = false)
    private int missedPieces;

    public StatsEntity() {
    }

    public StatsEntity(Integer id) {
        this.id = id;
    }

    public StatsEntity(Integer id, int overlayId, int nodeId, int step, boolean source, short natType, int connectionRequestsSentClose, int connectionRequestsSentRandom, int connectionRequestTimeoutsClose, int connectionRequestTimeoutsRandom, int connectionResponsesReceivedClose, int connectionResponsesReceivedRandom, int disconnectsSentClose, int disconnectsSentRandom, int disconnectsReceivedClose, int disconnectsReceivedRandom, short ingoingConnectionsClose, short ingoingConnectionsRandom, short outgoingConnectionsClose, short outgoingConnectionsRandom, float avgAsHopsToNeighbours, int seenSubPieces, int completePieces, int highestCompletePiece, int downloadedSubPiecesClose, int downloadedSubPiecesRandom, int sentSubPiecesClose, int sentSubPiecesRandom, int bufferLength, int missedPieces) {
        this.id = id;
        this.overlayId = overlayId;
        this.nodeId = nodeId;
        this.step = step;
        this.source = source;
        this.natType = natType;
        this.connectionRequestsSentClose = connectionRequestsSentClose;
        this.connectionRequestsSentRandom = connectionRequestsSentRandom;
        this.connectionRequestTimeoutsClose = connectionRequestTimeoutsClose;
        this.connectionRequestTimeoutsRandom = connectionRequestTimeoutsRandom;
        this.connectionResponsesReceivedClose = connectionResponsesReceivedClose;
        this.connectionResponsesReceivedRandom = connectionResponsesReceivedRandom;
        this.disconnectsSentClose = disconnectsSentClose;
        this.disconnectsSentRandom = disconnectsSentRandom;
        this.disconnectsReceivedClose = disconnectsReceivedClose;
        this.disconnectsReceivedRandom = disconnectsReceivedRandom;
        this.ingoingConnectionsClose = ingoingConnectionsClose;
        this.ingoingConnectionsRandom = ingoingConnectionsRandom;
        this.outgoingConnectionsClose = outgoingConnectionsClose;
        this.outgoingConnectionsRandom = outgoingConnectionsRandom;
        this.avgAsHopsToNeighbours = avgAsHopsToNeighbours;
        this.seenSubPieces = seenSubPieces;
        this.completePieces = completePieces;
        this.highestCompletePiece = highestCompletePiece;
        this.bufferLength = bufferLength;
        this.missedPieces = missedPieces;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getOverlayId() {
        return overlayId;
    }

    public void setOverlayId(int overlayId) {
        this.overlayId = overlayId;
    }

    public int getNodeId() {
        return nodeId;
    }

    public void setNodeId(int nodeId) {
        this.nodeId = nodeId;
    }

    public Integer getExperimentId() {
        return experimentId;
    }

    public void setExperimentId(Integer experimentId) {
        this.experimentId = experimentId;
    }

    public Integer getExperimentIteration() {
        return experimentIteration;
    }

    public void setExperimentIteration(Integer experimentIteration) {
        this.experimentIteration = experimentIteration;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public boolean getSource() {
        return source;
    }

    public void setSource(boolean source) {
        this.source = source;
    }

    public short getNatType() {
        return natType;
    }

    public void setNatType(short natType) {
        this.natType = natType;
    }

    public int getConnectionRequestsSentClose() {
        return connectionRequestsSentClose;
    }

    public void setConnectionRequestsSentClose(int connectionRequestsSentClose) {
        this.connectionRequestsSentClose = connectionRequestsSentClose;
    }

    public int getConnectionRequestsSentRandom() {
        return connectionRequestsSentRandom;
    }

    public void setConnectionRequestsSentRandom(int connectionRequestsSentRandom) {
        this.connectionRequestsSentRandom = connectionRequestsSentRandom;
    }

    public int getConnectionRequestTimeoutsClose() {
        return connectionRequestTimeoutsClose;
    }

    public void setConnectionRequestTimeoutsClose(int connectionRequestTimeoutsClose) {
        this.connectionRequestTimeoutsClose = connectionRequestTimeoutsClose;
    }

    public int getConnectionRequestTimeoutsRandom() {
        return connectionRequestTimeoutsRandom;
    }

    public void setConnectionRequestTimeoutsRandom(int connectionRequestTimeoutsRandom) {
        this.connectionRequestTimeoutsRandom = connectionRequestTimeoutsRandom;
    }

    public int getConnectionResponsesReceivedClose() {
        return connectionResponsesReceivedClose;
    }

    public void setConnectionResponsesReceivedClose(int connectionResponsesReceivedClose) {
        this.connectionResponsesReceivedClose = connectionResponsesReceivedClose;
    }

    public int getConnectionResponsesReceivedRandom() {
        return connectionResponsesReceivedRandom;
    }

    public void setConnectionResponsesReceivedRandom(int connectionResponsesReceivedRandom) {
        this.connectionResponsesReceivedRandom = connectionResponsesReceivedRandom;
    }

    public int getDisconnectsSentClose() {
        return disconnectsSentClose;
    }

    public void setDisconnectsSentClose(int disconnectsSentClose) {
        this.disconnectsSentClose = disconnectsSentClose;
    }

    public int getDisconnectsSentRandom() {
        return disconnectsSentRandom;
    }

    public void setDisconnectsSentRandom(int disconnectsSentRandom) {
        this.disconnectsSentRandom = disconnectsSentRandom;
    }

    public int getDisconnectsReceivedClose() {
        return disconnectsReceivedClose;
    }

    public void setDisconnectsReceivedClose(int disconnectsReceivedClose) {
        this.disconnectsReceivedClose = disconnectsReceivedClose;
    }

    public int getDisconnectsReceivedRandom() {
        return disconnectsReceivedRandom;
    }

    public void setDisconnectsReceivedRandom(int disconnectsReceivedRandom) {
        this.disconnectsReceivedRandom = disconnectsReceivedRandom;
    }

    public short getIngoingConnectionsClose() {
        return ingoingConnectionsClose;
    }

    public void setIngoingConnectionsClose(short ingoingConnectionsClose) {
        this.ingoingConnectionsClose = ingoingConnectionsClose;
    }

    public short getIngoingConnectionsRandom() {
        return ingoingConnectionsRandom;
    }

    public void setIngoingConnectionsRandom(short ingoingConnectionsRandom) {
        this.ingoingConnectionsRandom = ingoingConnectionsRandom;
    }

    public short getOutgoingConnectionsClose() {
        return outgoingConnectionsClose;
    }

    public void setOutgoingConnectionsClose(short outgoingConnectionsClose) {
        this.outgoingConnectionsClose = outgoingConnectionsClose;
    }

    public short getOutgoingConnectionsRandom() {
        return outgoingConnectionsRandom;
    }

    public void setOutgoingConnectionsRandom(short outgoingConnectionsRandom) {
        this.outgoingConnectionsRandom = outgoingConnectionsRandom;
    }

    public float getAvgAsHopsToNeighbours() {
        return avgAsHopsToNeighbours;
    }

    public void setAvgAsHopsToNeighbours(float avgAsHopsToNeighbours) {
        this.avgAsHopsToNeighbours = avgAsHopsToNeighbours;
    }

    public int getSeenSubPieces() {
        return seenSubPieces;
    }

    public void setSeenSubPieces(int seenSubPieces) {
        this.seenSubPieces = seenSubPieces;
    }

    public int getCompletePieces() {
        return completePieces;
    }

    public void setCompletePieces(int completePieces) {
        this.completePieces = completePieces;
    }

    public int getHighestCompletePiece() {
        return highestCompletePiece;
    }

    public void setHighestCompletePiece(int highestCompletePiece) {
        this.highestCompletePiece = highestCompletePiece;
    }

    public int getBufferLength() {
        return bufferLength;
    }

    public void setBufferLength(int bufferLength) {
        this.bufferLength = bufferLength;
    }

    public int getMissedPieces() {
        return missedPieces;
    }

    public void setMissedPieces(int missedPieces) {
        this.missedPieces = missedPieces;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof StatsEntity)) {
            return false;
        }
        StatsEntity other = (StatsEntity) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "se.sics.gvod.ls.ws.persistent.StatsEntity[ id=" + id + " ]";
    }

    public int getDownloadedSubPiecesIntraAs() {
        return downloadedSubPiecesIntraAs;
    }

    public void setDownloadedSubPiecesIntraAs(int downloadedSubPiecesIntraAs) {
        this.downloadedSubPiecesIntraAs = downloadedSubPiecesIntraAs;
    }

    public int getDownloadedSubPiecesNeighbourAs() {
        return downloadedSubPiecesNeighbourAs;
    }

    public void setDownloadedSubPiecesNeighbourAs(int downloadedSubPiecesNeighbourAs) {
        this.downloadedSubPiecesNeighbourAs = downloadedSubPiecesNeighbourAs;
    }

    public int getDownloadedSubPiecesOtherAs() {
        return downloadedSubPiecesOtherAs;
    }

    public void setDownloadedSubPiecesOtherAs(int downloadedSubPiecesOtherAs) {
        this.downloadedSubPiecesOtherAs = downloadedSubPiecesOtherAs;
    }

    public int getSentSubPiecesIntraAs() {
        return sentSubPiecesIntraAs;
    }

    public void setSentSubPiecesIntraAs(int sentSubPiecesIntraAs) {
        this.sentSubPiecesIntraAs = sentSubPiecesIntraAs;
    }

    public int getSentSubPiecesNeighbourAs() {
        return sentSubPiecesNeighbourAs;
    }

    public void setSentSubPiecesNeighbourAs(int sentSubPiecesNeighbourAs) {
        this.sentSubPiecesNeighbourAs = sentSubPiecesNeighbourAs;
    }

    public int getSentSubPiecesOtherAs() {
        return sentSubPiecesOtherAs;
    }

    public void setSentSubPiecesOtherAs(int sentSubPiecesOtherAs) {
        this.sentSubPiecesOtherAs = sentSubPiecesOtherAs;
    }

    public int getAsn() {
        return asn;
    }

    public void setAsn(int asn) {
        this.asn = asn;
    }

    public int getSubPieceRequestTimeouts() {
        return subPieceRequestTimeouts;
    }

    public void setSubPieceRequestTimeouts(int subPieceRequestTimeouts) {
        this.subPieceRequestTimeouts = subPieceRequestTimeouts;
    }

    public short getFanout() {
        return fanout;
    }

    public void setFanout(short fanout) {
        this.fanout = fanout;
    }

    public int getDlBwBytes() {
        return dlBwBytes;
    }

    public void setDlBwBytes(int dlBwBytes) {
        this.dlBwBytes = dlBwBytes;
    }

    public int getUlBwBytes() {
        return ulBwBytes;
    }

    public void setUlBwBytes(int ulBwBytes) {
        this.ulBwBytes = ulBwBytes;
    }

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
    
}
