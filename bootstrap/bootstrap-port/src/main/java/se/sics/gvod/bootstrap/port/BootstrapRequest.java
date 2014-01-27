package se.sics.gvod.bootstrap.port;

import se.sics.gvod.common.Utility;
import se.sics.kompics.Request;

public class BootstrapRequest extends Request {

    // -1 and 0 are reserved for stunServerId and nodeId respectively
    private final int overlayId;
    private final int utility;

    /**
     * This constructor returns a list of recent open peers.
     */
    public BootstrapRequest(int overlayId) {
        this.overlayId = overlayId;
        this.utility = 0;
    }
    
    /**
     * Returns a list of peers active in overlay, preferring nodes
     * with a utility value above (but closer) to utility.
     * @param overlayId
     * @param utility 
     */
    public BootstrapRequest(int overlayId, Utility utility) {
        this.overlayId = overlayId;
        this.utility = (utility == null) ? 0 : utility.getValue();
    }

    public int getOverlay() {
        return overlayId;
    }

    public int getUtility() {
        return utility;
    }
}