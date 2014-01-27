/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.system.vod.snapshot;

/**
 *
 * @author Jim Dowling<jdowling@sics.se>
 */
public class Stats {
    
    private volatile int utility = 0;
    private volatile int numGradientSamples = 0;
    private volatile int numBittorrentNeighbours = 0;
    private volatile int numLowerNeighbours = 0;
    private volatile int numUpperNeighbours = 0;
    private volatile int numReqsSent = 0;
    private volatile int numReqsRecvd = 0;
    private volatile int numTimeouts = 0;
    private volatile int numResps = 0;

    public Stats() {
    }

    public int getNumResps() {
        return numResps;
    }

    public int getNumGradientSamples() {
        return numGradientSamples;
    }

    public int getNumReqsSent() {
        return numReqsSent;
    }

    public int getNumReqsRecvd() {
        return numReqsRecvd;
    }

    public int getNumBittorrentNeighbours() {
        return numBittorrentNeighbours;
    }

    public void setNumBittorrentNeighbours(int numBittorrentNeighbours) {
        this.numBittorrentNeighbours = numBittorrentNeighbours;
    }

    public int getNumLowerNeighbours() {
        return numLowerNeighbours;
    }

    public int getNumUpperNeighbours() {
        return numUpperNeighbours;
    }

    public void setNumLowerNeighbours(int numLowerNeighbours) {
        this.numLowerNeighbours = numLowerNeighbours;
    }

    public void setNumUpperNeighbours(int numUpperNeighbours) {
        this.numUpperNeighbours = numUpperNeighbours;
    }
    
    public int getUtility() {
        return utility;
    }


    public void incSentRequest() {
        numReqsSent++;
    }
    
    public void incRecvdRequest() {
        numReqsRecvd++;
    }
    
    public void incGradientSample(int size) {
        numGradientSamples += size;
    }
    
    public void incResponse() {
        numResps++;
    }
    public void incTimeouts() {
        numTimeouts++;
    }

    public void setUtility(int utility) {
        this.utility = utility;
    }

    public int getNumTimeouts() {
        return numTimeouts;
    }
    
}
