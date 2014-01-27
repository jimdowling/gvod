package se.sics.gvod.web.port;

import se.sics.kompics.Event;
import se.sics.gvod.net.VodAddress;

/**
 * The <code>DownloadCompleted</code> class.
 *
 * @author Cosmin Arad <cosmin@sics.se>
 */
public final class DownloadCompletedSim extends Event {

    private final VodAddress peer;
    private final long downloadTime;
    private final boolean free;
    private final boolean jumped;

    public DownloadCompletedSim(VodAddress peer, long downloadTime,
            boolean free,
            boolean jumped) {
        this.peer = peer;
        this.downloadTime = downloadTime;
        
        this.free = free;
        this.jumped = jumped;
    }

    public boolean isFree() {
        return free;
    }

    public long getDownloadTime() {
        return downloadTime;
    }

    public VodAddress getPeer() {
        return peer;
    }

    public boolean isJumped() {
        return jumped;
    }

}

