package se.sics.gvod.ls.interas.snapshot;

import java.util.ArrayList;
import java.util.List;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.net.VodAddress.NatType;

public class Stats {

    private final int nodeId, overlayId;
    private volatile int selectedTimes = 0;
    private volatile VodAddress.NatType natType;
    private volatile int shuffleTimeout, numReceivedRequests, numSentRequests, numReceivedResponses, numSentResponses;
    private volatile double avgAsHops;
    private List<VodAddress> neighbours = new ArrayList<VodAddress>();

//-------------------------------------------------------------------
    public Stats(int nodeId, int overlayId) {
        this.nodeId = nodeId;
        this.overlayId = overlayId;
    }

    public int getNodeId() {
        return nodeId;
    }

    public int getOverlayId() {
        return overlayId;
    }

    public void setNatType(NatType peerType) {
        this.natType = peerType;
    }

    public void updatePartners(List<VodAddress> n) {
        this.neighbours.clear();
        this.neighbours.addAll(n);
    }

//-------------------------------------------------------------------
    public List<VodAddress> getAllPartners() {
        List<VodAddress> allPartners = new ArrayList<VodAddress>();
        allPartners.addAll(neighbours);
        return allPartners;
    }

//-------------------------------------------------------------------
    public List<VodAddress> getPartners() {
        return this.neighbours;
    }

//-------------------------------------------------------------------
    public void incSelectedTimes() {
        this.selectedTimes++;
    }

//-------------------------------------------------------------------
    public int getSelectedTimes() {
        return this.selectedTimes;
    }

    public void incReceivedResponses() {
        this.numReceivedResponses++;
    }

    public void resetReceivedResponses() {
        this.numReceivedResponses = 0;
    }

    public int getReceivedResponses() {
        return this.numReceivedResponses;
    }

    public void incReceivedRequests() {
        this.numReceivedRequests++;
    }

    public void resetReceivedRequests() {
        this.numReceivedRequests = 0;
    }

    public int getReceivedRequests() {
        return this.numReceivedRequests;
    }
    
    public void incSentResponses() {
        numSentResponses++;
    }
    
    public void resetSentResponses() {
        numSentResponses = 0;
    }
    
    public int getSentResponses() {
        return numSentResponses;
    }

    public void incShuffleTimeout() {
        this.shuffleTimeout++;
    }

    public void resetShuffleTimeout() {
        this.shuffleTimeout = 0;
    }

    public int getShuffleTimeout() {
        return this.shuffleTimeout;
    }

    public int getSentRequests() {
        return numSentRequests;
    }

    public void incSentRequests() {
        this.numSentRequests++;
    }

    public void resetSentRequests() {
        this.numSentRequests = 0;
    }

    public void setAvgAsHops(double avgAsHops) {
        this.avgAsHops = avgAsHops;
    }

    public void resetAvgAsHops() {
        this.avgAsHops = 0;
    }

    public double getAvgAsHops() {
        return avgAsHops;
    }

    public VodAddress.NatType getNatType() {
        return this.natType;
    }

//-------------------------------------------------------------------
    public boolean isPartner(VodAddress peer) {
        for (VodAddress desc : getAllPartners()) {
            if (desc.equals(peer)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public String toString() {
        return "Peer : nat(" + this.natType + ")";
    }
}
