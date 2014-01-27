package se.sics.gvod.bootstrap.port;

import java.util.Map;
import java.util.Set;
import se.sics.gvod.net.VodAddress;
import se.sics.kompics.Request;

public class BootstrapHeartbeat extends Request {

    private final VodAddress src;
    private final short mtu;
    private final Set<Integer> seedingOverlays;
    private final Map<Integer,Integer> downloadingUtilities;

    /**
     * 
     * @param mtu acquired at local network interface
     * @param seedingOverlays list of overlay IDs
     * @param downloadingUtilities map of overlayId,utility value pairs
     */
    public BootstrapHeartbeat(short mtu,
            Set<Integer> seedingOverlays,
            Map<Integer,Integer> downloadingUtilities) {
        this(mtu, seedingOverlays, downloadingUtilities, null);
    }
    
    /**
     * 
     * @param src address of client including NAT info
     * @param mtu acquired at local network interface
     * @param seedingOverlays list of overlay IDs
     * @param overlayUtilities map of overlayId,utility value pairs
     */
    public BootstrapHeartbeat(short mtu,
            Set<Integer> seedingOverlays,
            Map<Integer,Integer> overlayUtilities, VodAddress src) {
        this.src = src;
        this.mtu = mtu;
        this.seedingOverlays = seedingOverlays;
        this.downloadingUtilities = overlayUtilities;
    }

    public short getMtu() {
        return mtu;
    }

    public Set<Integer> getSeedingOverlays() {
        return seedingOverlays;
    }

    public Map<Integer, Integer> getDownloadingUtilities() {
        return downloadingUtilities;
    }


    /**
     * 
     * @return null or the Address from which the Heartbeat originated
     */
    public VodAddress getDownloadingAddress() {
        return src;
    }

}
