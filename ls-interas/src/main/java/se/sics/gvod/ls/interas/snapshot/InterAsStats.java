/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.ls.interas.snapshot;

import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.gvod.common.Self;
import se.sics.gvod.ls.interas.InterAs;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.net.VodAddress.NatType;

/**
 *
 * @author Jim Dowling<jdowling@sics.se>
 */
public final class InterAsStats {

    private static Logger logger = LoggerFactory.getLogger(InterAsStats.class);
    // (overlayId -> (nodeId, stats))
    private static final ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, Stats>> snapshotMap =
            new ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, Stats>>();
    private static final ConcurrentHashMap<VodAddress, Stats> nodeMap =
            new ConcurrentHashMap<VodAddress, Stats>();
    private static AtomicInteger counter = new AtomicInteger(0);
    private static final ConcurrentHashMap<Integer, Integer> sentRequests =
            new ConcurrentHashMap<Integer, Integer>();
    private static final ConcurrentHashMap<Integer, Integer> receivedResponses =
            new ConcurrentHashMap<Integer, Integer>();
    private static final ConcurrentHashMap<Integer, Integer> gossipTimedout =
            new ConcurrentHashMap<Integer, Integer>();
    private static final ConcurrentHashMap<Integer, Integer> sentResponses =
            new ConcurrentHashMap<Integer, Integer>();

    private InterAsStats() {
        // hidden
    }

    public static Stats instance(Self self) {
        return addNode(self.getAddress());
    }

    public static Stats addNode(VodAddress peer) {
//        int overlayId = peer.getOverlayId();
        int overlayId = InterAs.SYSTEM_INTER_AS_OVERLAY_ID;
        int nodeId = peer.getId();

        ConcurrentHashMap<Integer, Stats> overlayStats;
        if (!snapshotMap.containsKey(overlayId)) {
            overlayStats = new ConcurrentHashMap<Integer, Stats>();
            snapshotMap.put(overlayId, overlayStats);
        } else {
            overlayStats = snapshotMap.get(overlayId);
        }
        Stats stats;
        if (!overlayStats.containsKey(nodeId)) {
            stats = new Stats(nodeId, overlayId);
            overlayStats.put(nodeId, stats);
            nodeMap.put(peer, stats);

        } else {
            stats = overlayStats.get(nodeId);
        }
        stats.setNatType(peer.getNatType());
        return stats;
    }

    public static boolean removeNode(int nodeId, int overlayId) {
        ConcurrentHashMap<Integer, Stats> overlayStats = snapshotMap.get(overlayId);
        if (overlayStats != null) {
            return (overlayStats.remove(nodeId) == null) ? false : true;
        } else {
            return false;
        }
    }

    private static Set<Stats> getNodes(int overlayId) {
        Set<Stats> nodes = new HashSet<Stats>();
        nodes.addAll(snapshotMap.get(overlayId).values());
        return nodes;
    }

    private static int numNodes(int overlayId) {
        return getNodes(overlayId).size();
    }

//-------------------------------------------------------------------
    public static void startCollectData() {
        logger.debug("\nStart collecting data ...\n");
    }

//-------------------------------------------------------------------
    public static void stopCollectData() {
        logger.debug("\nStop collecting data ...\n");
        for (int overlayId : snapshotMap.keySet()) {
            report(overlayId);
        }
    }

    /**
     * Returns all nodes with the same overlayId
     *
     * @param overlayId
     * @return
     */
    private static Set<Stats> getOverlayStats(int overlayId) {
        Set<Stats> nodes = new HashSet<Stats>();
        nodes.addAll(snapshotMap.get(overlayId).values());
        return nodes;
    }

    private static void incTotal(Map<Integer, Integer> map, int overlayId, int count) {
        Integer total = map.get(overlayId);
        if (total == null) {
            total = 0;
            map.put(overlayId, total);
        }
        total += count;
    }

    private static int getNumRequestsSent(int overlayId) {
        int count = 0;
        Set<Stats> nodes = getOverlayStats(overlayId);
        for (Stats s : nodes) {
            count += s.getSentRequests();
        }
        incTotal(sentRequests, overlayId, count);
        return count;
    }

    private static int getNumShufflesTimeouts(int overlayId) {
        int count = 0;
        Set<Stats> nodes = getOverlayStats(overlayId);
        for (Stats s : nodes) {
            count += s.getShuffleTimeout();
        }
        incTotal(gossipTimedout, overlayId, count);
        return count;
    }

    private static int getNumResponsesSent(int overlayId) {
        int count = 0;
        Set<Stats> nodes = getOverlayStats(overlayId);
        for (Stats s : nodes) {
            count += s.getSentResponses();
        }
        incTotal(sentResponses, overlayId, count);
        return count;
    }

    private static int getNumReceivedResponses(int overlayId) {
        int count = 0;
        Set<Stats> nodes = getOverlayStats(overlayId);
        for (Stats s : nodes) {
            count += s.getReceivedResponses();
        }
        incTotal(receivedResponses, overlayId, count);
        return count;
    }

    private static int stdReceivedRequests(int overlayId, int avg) {
        int std = 0;
        Set<Stats> nodes = getOverlayStats(overlayId);
        for (Stats s : nodes) {
            if (s.getNatType() == NatType.OPEN) {
                std += Math.abs(avg - s.getReceivedRequests());
            }
        }
        return std;
    }

    private static int maxShufflesRecvd(int overlayId) {
        int count = -1;
        Set<Stats> nodes = getOverlayStats(overlayId);
        for (Stats s : nodes) {
            count = (s.getReceivedRequests() > count)
                    ? s.getReceivedRequests() : count;
        }
        return count;
    }

    private static int minShufflesRecvd(int overlayId) {
        int count = Integer.MAX_VALUE;
        Set<Stats> nodes = getOverlayStats(overlayId);
        for (Stats s : nodes) {
            if (s.getNatType() == NatType.OPEN) {
                count = (s.getReceivedRequests() < count)
                        ? s.getReceivedRequests() : count;
            }
        }
        return count;
    }

    private static double avgAsHops(int overlayId) {
        double sumOfPeersAvgAsHops = 0;
        Set<Stats> nodes = getOverlayStats(overlayId);
        for (Stats s : nodes) {
            sumOfPeersAvgAsHops += s.getAvgAsHops();
        }
        return (sumOfPeersAvgAsHops / nodes.size());
    }
    
    private static int sentRequestsFromNat(int overlayId) {
        int count = 0;
        Set<Stats> nodes = getOverlayStats(overlayId);
        for(Stats s : nodes) {
            if(s.getNatType().equals(NatType.NAT)) {
                count += s.getSentRequests();
            }
        }
        return count;
    }
    
    private static int sentRequestsFromOpen(int overlayId) {
        int count = 0;
        Set<Stats> nodes = getOverlayStats(overlayId);
        for(Stats s : nodes) {
            if(s.getNatType().equals(NatType.OPEN)) {
                count += s.getSentRequests();
            }
        }
        return count;
    }
    
    private static int successfulExchangesNat(int overlayId) {
        int count = 0;
        Set<Stats> nodes = getOverlayStats(overlayId);
        for(Stats s : nodes) {
            if(s.getNatType().equals(NatType.NAT)) {
                count += s.getReceivedResponses();
            }
        }
        return count;
    }
    
    private static int successfulExchangesOpen(int overlayId) {
        int count = 0;
        Set<Stats> nodes = getOverlayStats(overlayId);
        for(Stats s : nodes) {
            if(s.getNatType().equals(NatType.OPEN)) {
                count += s.getReceivedResponses();
            }
        }
        return count;
    }

//-------------------------------------------------------------------
    public static void report(int overlayId) {
        StringBuilder sb = new StringBuilder("\n");
        sb.append("InterAs current step: ").append(counter.getAndIncrement()).
                append(", current time: ").append(System.currentTimeMillis()).
                append(", number of nodes: ").append(numNodes(overlayId));
        sb.append(reportShuffles(overlayId));
        logger.info(sb.toString());
    }

    private static String reportShuffles(int overlayId) {
        StringBuilder sb = new StringBuilder();
        int t = getNumShufflesTimeouts(overlayId);
        int sentRequests = getNumRequestsSent(overlayId);
        int sentResponses = getNumResponsesSent(overlayId);
        int recvd = getNumReceivedResponses(overlayId);
        int sz = numNodes(overlayId);
        int max = maxShufflesRecvd(overlayId);
        int min = minShufflesRecvd(overlayId);
        double avgAsHops = avgAsHops(overlayId);
        DecimalFormat format = new DecimalFormat("#.##");
        String timedoutPercent = sentRequests == 0 ? "0" : format.format(100 * (double) ((double) t / (double) sentRequests));
        String receivedPercent = sentRequests == 0 ? "0" : format.format(100 * (double) ((double) recvd / (double) sentRequests));
        String averageAsHops = format.format(avgAsHops);
        
        int sentRequestsFromNat = sentRequestsFromNat(overlayId);
        int sentRequestsFromOpen = sentRequestsFromOpen(overlayId);
        int receivedResponsesNat = successfulExchangesNat(overlayId);
        int receivedResponsesOpen = successfulExchangesOpen(overlayId);
        if (sz != 0) {
            sb.append("---\n");
            sb.append("Msg Stats: ");
            sb.append("avg recvd(").append((recvd / sz)).
                    //                    append("), max(").append(max).
                    //                    append("), min(").append(min).
                    append("), sReq(").append(sentRequests).
                    append("), t(").append(t).append(":").append(timedoutPercent).append("%").
                    append("), rResp(").append(recvd).append(":").append(receivedPercent).append("%").
                    append(")\n");
            sb.append("NAT Stats (recvd/sent): ");
            sb.append("NAT(").append(receivedResponsesNat).append("/").append(sentRequestsFromNat).
                    append("), Open(").append(receivedResponsesOpen).append("/").append(sentRequestsFromOpen).
                    append(")\n");
            sb.append("Topology Stats: ");
            sb.append("avgAsHops(").append(averageAsHops).
                    append(")\n");
        }
        return sb.toString();
    }
}
