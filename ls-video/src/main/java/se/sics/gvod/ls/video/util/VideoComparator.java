package se.sics.gvod.ls.video.util;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import se.sics.asdistances.ASDistances;
import se.sics.asdistances.PrefixHandler;
import se.sics.gvod.common.Self;
import se.sics.gvod.net.VodAddress;

/**
 *
 * @author Niklas Wahl&#233;n <nwahlen@kth.se>
 */
public class VideoComparator implements Comparator<VodAddress> {

    private ASDistances distances;
    private Self self;
    private Map<VodAddress, PeerStatistics> statistics;

    /**
     * Internal class for keeping statistics about peers
     */
    private class PeerStatistics {

        public double health;
        public int availablePieces;
        public int timeouts;
        public long firstSeen;
        public long lastSeen;
    }

    public VideoComparator(Self self) {
        this.self = self;
        distances = ASDistances.getInstance();
        statistics = new HashMap<VodAddress, PeerStatistics>();
    }

    @Override
    public int compare(VodAddress a1, VodAddress a2) {
        if (a1 == null && a2 == null) {
            return 0;
        } else if (a1 == null) {
            return 1;
        } else if (a2 == null) {
            return -1;
        }
        return compareDistances(a1, a2);
    }

    public int compareDistances(VodAddress a1, VodAddress a2) {
        String ip1 = a1.getIp().getHostAddress();
        String ip2 = a2.getIp().getHostAddress();
        String selfIp = self.getIp().getHostAddress();
        byte d1 = distances.getDistance(selfIp, ip1);
        byte d2 = distances.getDistance(selfIp, ip2);
        if (d1 < d2) {
            return -1;
        } else if (d1 > d2) {
            return 1;
        } else {
            int sp1 = PrefixHandler.sharedPrefix(selfIp, ip1);
            int sp2 = PrefixHandler.sharedPrefix(selfIp, ip2);
            if (sp1 < sp2) {
                return -1;
            } else if (sp1 > sp2) {
                return 1;
            } else {
                return 0;
            }
        }
    }

    public int compareStatistics(VodAddress a1, VodAddress a2) {
        int value1 = 0;
        int value2 = 0;
        int retval = 0;
        PeerStatistics stats1 = statistics.get(a1);
        PeerStatistics stats2 = statistics.get(a2);
        if (stats1.availablePieces > stats2.availablePieces) {
            value1++;
        } else if (stats1.availablePieces < stats2.availablePieces) {
            value2++;
        }
        if (stats1.lastSeen > stats2.lastSeen) {
            value1++;
        } else if (stats1.lastSeen < stats2.lastSeen) {
            value2++;
        }
        if (value1 > value2) {
            retval = -1;
        } else if (value1 < value2) {
            retval = 1;
        }
        return retval;
    }

    public void addPeer(VodAddress a) {
        if (!statistics.containsKey(a)) {
            PeerStatistics aStats = new PeerStatistics();
            aStats.firstSeen = System.currentTimeMillis();
            statistics.put(a, aStats);
        }
    }

    public void incAvailablePieces(VodAddress a) {
        if(statistics.containsKey(a)) {
            PeerStatistics s = statistics.get(a);
            s.availablePieces++;
            statistics.put(a, s);
        }
    }

    public void updateLastSeen(VodAddress a) {
        if(statistics.containsKey(a)) {
            PeerStatistics s = statistics.get(a);
            s.lastSeen = System.currentTimeMillis();
            statistics.put(a, s);
        }
    }
    
    public Long getLastSeen(VodAddress a) {
        if(statistics.get(a) == null) {
            return null;
        }
        return statistics.get(a).lastSeen;
    }

    public void incTimeouts(VodAddress a) {
        if(statistics.containsKey(a)) {
            PeerStatistics s = statistics.get(a);
            s.timeouts++;
            statistics.put(a, s);
        }
    }
    
    public void removePeer(VodAddress a) {
        statistics.remove(a);
    }
    
    public short getDistanceTo(VodAddress a) {
        return distances.getDistance(self.getIp().getHostAddress(), a.getIp().getHostAddress());
    }
}
