package se.sics.gvod.ls.ws.persistent;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.*;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Niklas Wahl&#233;n <nwahlen@kth.se>
 */
@Entity
@Table(name = "experiment")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "ExperimentEntity.findAll", query = "SELECT e FROM ExperimentEntity e"),
    @NamedQuery(name = "ExperimentEntity.findMaxId", query = "SELECT max(e.id) FROM ExperimentEntity e")})
public class ExperimentEntity implements Serializable {
    @Id
    @Basic(optional = false)
    @javax.validation.constraints.NotNull
    @Column(name = "id", nullable = false)
    private Integer id;
    @Column(name = "iterations")
    private Short iterations;
    @Size(max = 50)
    @Column(name = "scenario", length = 50)
    private String scenario;
    @Size(max = 8)
    @Column(name = "status", length = 8)
    private String status;
    @Size(max = 200)
    @Column(name = "arguments", length = 200)
    private String arguments;
    @Column(name = "max_out_close")
    private Short maxOutClose;
    @Column(name = "max_out_random")
    private Short maxOutRandom;
    @Column(name = "sp_per_piece")
    private Short spPerPiece;
    @Column(name = "redundant_sps")
    private Short redundantSps;
    @Column(name = "local_history")
    private Short localHistory;
    @Column(name = "neighbour_history")
    private Short neighbourHistory;
    @Column(name = "exp_length")
    private Short expLength;
    @Size(max = 10)
    @Column(name = "node_selection", length = 10)
    private String nodeSelection;
    @Basic(optional = false)
    @Column(name = "start_ts", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date startTs;
    @Basic(optional = false)
    @Column(name = "end_ts", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date endTs;

    public ExperimentEntity() {
    }

    public ExperimentEntity(Integer id) {
        this.id = id;
    }

    public ExperimentEntity(Integer id, Date startTs, Date endTs) {
        this.id = id;
        this.startTs = startTs;
        this.endTs = endTs;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Short getIterations() {
        return iterations;
    }

    public void setIterations(Short iterations) {
        this.iterations = iterations;
    }

    public String getScenario() {
        return scenario;
    }

    public void setScenario(String scenario) {
        this.scenario = scenario;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getArguments() {
        return arguments;
    }

    public void setArguments(String arguments) {
        this.arguments = arguments;
    }

    public Short getMaxOutClose() {
        return maxOutClose;
    }

    public void setMaxOutClose(Short maxOutClose) {
        this.maxOutClose = maxOutClose;
    }

    public Short getMaxOutRandom() {
        return maxOutRandom;
    }

    public void setMaxOutRandom(Short maxOutRandom) {
        this.maxOutRandom = maxOutRandom;
    }

    public Short getSpPerPiece() {
        return spPerPiece;
    }

    public void setSpPerPiece(Short spPerPiece) {
        this.spPerPiece = spPerPiece;
    }

    public Short getRedundantSps() {
        return redundantSps;
    }

    public void setRedundantSps(Short redundantSps) {
        this.redundantSps = redundantSps;
    }

    public Short getLocalHistory() {
        return localHistory;
    }

    public void setLocalHistory(Short localHistory) {
        this.localHistory = localHistory;
    }

    public Short getNeighbourHistory() {
        return neighbourHistory;
    }

    public void setNeighbourHistory(Short neighbourHistory) {
        this.neighbourHistory = neighbourHistory;
    }

    public Short getExpLength() {
        return expLength;
    }

    public void setExpLength(Short expLength) {
        this.expLength = expLength;
    }

    public String getNodeSelection() {
        return nodeSelection;
    }

    public void setNodeSelection(String nodeSelection) {
        this.nodeSelection = nodeSelection;
    }

    public Date getStartTs() {
        return startTs;
    }

    public void setStartTs(Date startTs) {
        this.startTs = startTs;
    }

    public Date getEndTs() {
        return endTs;
    }

    public void setEndTs(Date endTs) {
        this.endTs = endTs;
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
        if (!(object instanceof ExperimentEntity)) {
            return false;
        }
        ExperimentEntity other = (ExperimentEntity) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "se.sics.gvod.ls.ws.persistent.ExperimentEntity[ id=" + id + " ]";
    }
    
}
