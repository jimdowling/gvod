package se.sics.gvod.ls.video.snapshot;

import java.util.Date;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Niklas Wahl&#233;n <nwahlen@kth.se>
 */
@XmlRootElement
public class Experiment {
    
    public enum Status {
        opened, running, paused, finished, failed
    }

    private Date startTs;
    private Date endTs;
    private static final long serialVersionUID = 1L;
    private Integer id;
    private Short iterations;
    private String arguments;
    private String scenario;
    private Status status;
    private Short maxOutClose;
    private Short maxOutRandom;
    private Short spPerPiece;
    private Short redundantSps;
    private Short localHistory;
    private Short neighbourHistory;
    private Short expLength;
    private String nodeSelection;
    
    public Experiment() {
        
    }
    
    public Experiment(int id) {
        this.id = id;
    }

    public String getArguments() {
        return arguments;
    }

    public void setArguments(String arguments) {
        this.arguments = arguments;
    }

    public Date getEndTs() {
        return endTs;
    }

    public void setEndTs(Date endTs) {
        this.endTs = endTs;
    }

    public Short getExpLength() {
        return expLength;
    }

    public void setExpLength(Short expLength) {
        this.expLength = expLength;
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

    public Short getLocalHistory() {
        return localHistory;
    }

    public void setLocalHistory(Short localHistory) {
        this.localHistory = localHistory;
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

    public Short getNeighbourHistory() {
        return neighbourHistory;
    }

    public void setNeighbourHistory(Short neighbourHistory) {
        this.neighbourHistory = neighbourHistory;
    }

    public String getNodeSelection() {
        return nodeSelection;
    }

    public void setNodeSelection(String nodeSelection) {
        this.nodeSelection = nodeSelection;
    }

    public Short getRedundantSps() {
        return redundantSps;
    }

    public void setRedundantSps(Short redundantSps) {
        this.redundantSps = redundantSps;
    }

    public String getScenario() {
        return scenario;
    }

    public void setScenario(String scenario) {
        this.scenario = scenario;
    }

    public Short getSpPerPiece() {
        return spPerPiece;
    }

    public void setSpPerPiece(Short spPerPiece) {
        this.spPerPiece = spPerPiece;
    }

    public Date getStartTs() {
        return startTs;
    }

    public void setStartTs(Date startTs) {
        this.startTs = startTs;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
