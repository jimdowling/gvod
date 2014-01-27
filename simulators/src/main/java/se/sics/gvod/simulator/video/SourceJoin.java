package se.sics.gvod.simulator.video;

import se.sics.gvod.net.VodAddress;
import se.sics.gvod.net.VodAddress.NatType;
import se.sics.kompics.Event;

/**
 *
 * @author Niklas Wahl&#233;n <nwahlen@kth.se>
 */
public class SourceJoin extends Event {
    
    public static final int NO_OPTION = 0;

    private final Integer peerId;
    private final VodAddress.NatType peerType;
    private final Integer option;
    public SourceJoin(Integer peerId, NatType peerType, Integer option) {
        this.peerId = peerId;
        this.peerType = peerType;
        this.option = option;
    }

    public Integer getOption() {
        return option;
    }

    public Integer getPeerId() {
        return peerId;
    }

    public NatType getPeerType() {
        return peerType;
    }
}
