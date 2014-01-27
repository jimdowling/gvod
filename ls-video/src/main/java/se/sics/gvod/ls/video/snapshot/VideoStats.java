package se.sics.gvod.ls.video.snapshot;

import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.gvod.common.Self;
import se.sics.gvod.net.VodAddress;
import java.lang.management.ManagementFactory;
import java.util.logging.Level;
import javax.management.*;

/**
 *
 * @author Jim Dowling <jdowling@sics.se>
 * @author Niklas Wahl&#233;n <nwahlen@kth.se>
 */
public final class VideoStats {

    private static Logger logger = LoggerFactory.getLogger(VideoStats.class);
    // (overlayId -> (nodeId, stats))
    private static final ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, Stats>> snapshotMap =
            new ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, Stats>>();
    private static final ConcurrentHashMap<VodAddress, Stats> nodeMap =
            new ConcurrentHashMap<VodAddress, Stats>();
    private static AtomicInteger counter = new AtomicInteger(0);
    private static final ConcurrentHashMap<Integer, Integer> sentConnectionRequests =
            new ConcurrentHashMap<Integer, Integer>();
    private static final ConcurrentHashMap<Integer, Integer> receivedConnectionResponses =
            new ConcurrentHashMap<Integer, Integer>();
    private static final ConcurrentHashMap<Integer, Integer> connectionRequestTimeouts =
            new ConcurrentHashMap<Integer, Integer>();
    private static final NumPiecesComparator numPiecesComparator = new NumPiecesComparator();

    private VideoStats() {
        // hidden
    }

    public static Stats instance(Self self) {
        return addNode(self.getAddress(), false);
    }

    public static Stats addNode(VodAddress peer, boolean source) {
        int overlayId = peer.getOverlayId();
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
            stats = new Stats(nodeId, overlayId, source);
            overlayStats.put(nodeId, stats);
            nodeMap.put(peer, stats);
            registerMBean(stats);
        } else {
            stats = overlayStats.get(nodeId);
        }
        stats.setNatType(peer.getNatType());
        return stats;
    }

    public static boolean removeNode(int nodeId, int overlayId) {
        ConcurrentHashMap<Integer, Stats> overlayStats = snapshotMap.get(overlayId);
        if (overlayStats != null) {
            Stats removed = overlayStats.remove(nodeId);
            if(removed == null) {
                return false;
            } else {
                unregisterMBean(removed);
                return true;
            }
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

    private static void registerMBean(Stats stats) {
        try {
            // Register MBean in Platform MBeanServer
            ManagementFactory.getPlatformMBeanServer().
                    registerMBean(new StandardMBean(stats,
                    StatsIntf.class),
                    new ObjectName("snapshot.video.ls.gvod.sics.se:type=Stats("
                    + stats.getOverlayId() + "-" + stats.getNodeId() + ")"));
        } catch (JMException ex) {
            java.util.logging.Logger.getLogger(VideoStats.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    
    private static void unregisterMBean(Stats stats) {
        try {
            ManagementFactory.getPlatformMBeanServer().
                    unregisterMBean(
                    new ObjectName("snapshot.video.ls.gvod.sics.se:type=Stats("
                        + stats.getOverlayId() + "-" + stats.getNodeId() + ")"));
        } catch (JMException ex) {
            java.util.logging.Logger.getLogger(VideoStats.class.getName()).log(Level.SEVERE, null, ex);
        }
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
            count += (s.getConnectionRequestsSentClose() + s.getConnectionRequestsSentRandom());
        }
        incTotal(sentConnectionRequests, overlayId, count);
        return count;
    }

    private static int getReceivedConnectionRequestTimeouts(int overlayId) {
        int count = 0;
        Set<Stats> nodes = getOverlayStats(overlayId);
        for (Stats s : nodes) {
            count += (s.getConnectionRequestTimeoutsClose() + s.getConnectionRequestTimeoutsRandom());
        }
        incTotal(connectionRequestTimeouts, overlayId, count);
        return count;
    }

    private static int getNumReceivedResponses(int overlayId) {
        int count = 0;
        Set<Stats> nodes = getOverlayStats(overlayId);
        for (Stats s : nodes) {
            count += (s.getConnectionResponsesReceivedClose() + s.getConnectionResponsesReceivedRandom());
        }
        incTotal(receivedConnectionResponses, overlayId, count);
        return count;
    }

    private static double averageIngoingConnections(int overlayId) {
        double totalConnections = 0;
        Set<Stats> nodes = getOverlayStats(overlayId);
        for (Stats s : nodes) {
            totalConnections += (s.getIngoingConnectionsClose()
                    + s.getIngoingConnectionsRandom());
        }
        return (totalConnections / nodes.size());
    }

    private static int maximumIngoingConnections(int overlayId) {
        int connections = -1;
        Set<Stats> nodes = getOverlayStats(overlayId);
        for (Stats s : nodes) {
            if (connections < (s.getIngoingConnectionsClose()
                    + s.getIngoingConnectionsRandom())) {
                connections = (s.getIngoingConnectionsClose()
                        + s.getIngoingConnectionsRandom());
            }
        }
        return connections;
    }

    private static int minimumIngoingConnections(int overlayId) {
        int connections = Integer.MAX_VALUE;
        Set<Stats> nodes = getOverlayStats(overlayId);
        for (Stats s : nodes) {
            if (connections > (s.getIngoingConnectionsClose()
                    + s.getIngoingConnectionsRandom())) {
                connections = (s.getIngoingConnectionsClose()
                        + s.getIngoingConnectionsRandom());
            }
        }
        return connections;
    }

    private static int peersWithZeroConnections(int overlayId) {
        int count = 0;
        Set<Stats> nodes = getOverlayStats(overlayId);
        for (Stats s : nodes) {
            if ((s.getIngoingConnectionsClose()
                    + s.getIngoingConnectionsRandom()) == 0) {
                count++;
            }
        }
        return count;
    }

    /*
     * Pieces stats
     */
    private static int maxNumPieces(int overlayId) {
        int pieces = -1;
        Set<Stats> nodes = getOverlayStats(overlayId);
        for (Stats s : nodes) {
            if (!s.isSource() && s.getCompletePieces() > pieces) {
                pieces = s.getCompletePieces();
            }
        }
        return pieces;
    }

    private static int minNumPieces(int overlayId) {
        int pieces = Integer.MAX_VALUE;
        Set<Stats> nodes = getOverlayStats(overlayId);
        for (Stats s : nodes) {
            if (!s.isSource() && s.getCompletePieces() < pieces) {
                pieces = s.getCompletePieces();
            }
        }
        return pieces;
    }

    private static double avgNumPieces(int overlayId) {
        int piecesCount = 0;
        Set<Stats> nodes = getOverlayStats(overlayId);
        for (Stats s : nodes) {
            if (!s.isSource()) {
                piecesCount += s.getCompletePieces();
            }
        }
        return ((double) ((double) piecesCount) / ((double) nodes.size()));
    }

    private static int maxNumSubPiecesSeen(int overlayId) {
        int pieces = -1;
        Set<Stats> nodes = getOverlayStats(overlayId);
        for (Stats s : nodes) {
            if (!s.isSource() && s.getSeenSubPieces() > pieces) {
                pieces = s.getSeenSubPieces();
            }
        }
        return pieces;
    }

    private static int minNumSubPiecesSeen(int overlayId) {
        int pieces = Integer.MAX_VALUE;
        Set<Stats> nodes = getOverlayStats(overlayId);
        for (Stats s : nodes) {
            if (!s.isSource() && s.getSeenSubPieces() < pieces) {
                pieces = s.getSeenSubPieces();
            }
        }
        return pieces;
    }

    private static double avgNumSubPiecesSeen(int overlayId) {
        int piecesCount = 0;
        Set<Stats> nodes = getOverlayStats(overlayId);
        for (Stats s : nodes) {
            if (!s.isSource()) {
                piecesCount += s.getSeenSubPieces();
            }
        }
        return ((double) ((double) piecesCount) / ((double) nodes.size()));
    }

    private static int peersWithZeroSubPiecesSeen(int overlayId) {
        int count = 0;
        Set<Stats> nodes = getOverlayStats(overlayId);
        for (Stats s : nodes) {
            if (!s.isSource() && s.getSeenSubPieces() == 0) {
                count++;
            }
        }
        return count;
    }

    private static List<Stats> getWorstDownloaders(int overlayId, int size) {
        List<Stats> worst = new ArrayList<Stats>();
        Set<Stats> nodes = getOverlayStats(overlayId);
        for (Stats s : nodes) {
            if (!s.isSource()) {
                if (worst.size() == size) {
                    Collections.sort(worst, numPiecesComparator);
                    if (numPiecesComparator.compare(s, worst.get(worst.size() - 1)) == -1) {
                        worst.remove(worst.size() - 1);
                        worst.add(s);
                    }
                } else {
                    worst.add(s);
                }
            }
        }
        return worst;
    }

    private static List<Stats> getBestDownloaders(int overlayId, int size) {
        List<Stats> best = new ArrayList<Stats>();
        Set<Stats> nodes = getOverlayStats(overlayId);
        for (Stats s : nodes) {
            if (!s.isSource()) {
                if (best.size() == size) {
                    Collections.sort(best, numPiecesComparator);
                    if (numPiecesComparator.compare(s, best.get(0)) == 1) {
                        best.remove(best.get(0));
                        best.add(s);
                    }
                } else {
                    best.add(s);
                }
            }
        }
        return best;
    }

    private static int[] getPieceDownloadStats(int overlayId, int pieces) {
        Set<Stats> nodes = getOverlayStats(overlayId);
        int pieceDownloads[] = new int[pieces];
        for (Stats s : nodes) {
            List<Integer> pieceStats = s.getPieceStats();
            for (int i = 0; i < pieceDownloads.length; i++) {
                if (pieceStats.contains(i)) {
                    pieceDownloads[i]++;
                }
            }
        }
        return pieceDownloads;
    }

//-------------------------------------------------------------------
    public static void report(int overlayId) {
        StringBuilder sb = new StringBuilder("\n");
        sb.append("Video current step: ").append(counter.get()).
                append(", current time: ").append(System.currentTimeMillis()).
                append(", number of nodes: ").append(numNodes(overlayId));
        incStepCounter(overlayId);
        sb.append(reportStats(overlayId));
        logger.info(sb.toString());
    }

    private static void incStepCounter(int overlayId) {
        Set<Stats> stats = getOverlayStats(overlayId);
        for (Stats s : stats) {
            s.setStep(counter.get());
        }
        counter.incrementAndGet();
    }

    private static String reportStats(int overlayId) {
        StringBuilder sb = new StringBuilder();
        int t = getReceivedConnectionRequestTimeouts(overlayId);
        int sentRequests = getNumRequestsSent(overlayId);
        int recvd = getNumReceivedResponses(overlayId);
        int sz = numNodes(overlayId);
        DecimalFormat format = new DecimalFormat("#.##");
        String timedoutPercent = sentRequests == 0 ? "0" : format.format(100 * (double) ((double) t / (double) sentRequests));

        String avgConns = format.format(averageIngoingConnections(overlayId));
        int maxConns = maximumIngoingConnections(overlayId);
        int minConns = minimumIngoingConnections(overlayId);
        int zeroConns = peersWithZeroConnections(overlayId);

        int maxNumPieces = maxNumPieces(overlayId);
        int minNumPieces = minNumPieces(overlayId);
        String avgNumPieces = format.format(avgNumPieces(overlayId));
        int maxNumPiecesSeen = maxNumSubPiecesSeen(overlayId);
        int minNumPiecesSeen = minNumSubPiecesSeen(overlayId);

        int zeroPiecesSeen = peersWithZeroSubPiecesSeen(overlayId);
        String avgNumPiecesSeen = format.format(avgNumSubPiecesSeen(overlayId));

        List<Stats> worstDownloaders = getWorstDownloaders(overlayId, 5);
        List<Stats> bestDownloaders = getBestDownloaders(overlayId, 5);
        if (sz != 0) {
            sb.append("---\n");
            sb.append("Video msg stats: ").append("avg recvd(").append((recvd / sz)).
                    append("), sReq(").append(sentRequests).
                    append("), t(").append(t).append(" (").append(timedoutPercent).append("%)").
                    append("), rResp(").append(recvd).
                    append(")\n");
            sb.append("Video connection stats: ").append("avg(").append(avgConns).
                    append("), max(").append(maxConns).
                    append("), min(").append(minConns).
                    append("), zero(").append(zeroConns).
                    append(")\n");
            sb.append("Video pieces stats: ").append("downloaded(").append(avgNumPieces).
                    append("/").append(maxNumPieces).
                    append("/").append(minNumPieces).
                    append("), seen(").append(avgNumPiecesSeen).
                    append("/").append(maxNumPiecesSeen).
                    append("/").append(minNumPiecesSeen).
                    append("), zero(").append(zeroPiecesSeen).
                    append(")\n");
            sb.append("Worst downloaders:\n");
            Iterator<Stats> it = worstDownloaders.iterator();
            while (it.hasNext()) {
                Stats worstDownloader = it.next();
                int totalDlSubPieces = worstDownloader.getDownloadedSubPiecesIntraAs() + worstDownloader.getDownloadedSubPiecesNeighbourAs() + worstDownloader.getDownloadedSubPiecesOtherAs();
                String worstDlPercent = maxNumPieces == 0 ? "0" : format.format(100 * (double) ((double) worstDownloader.getCompletePieces() / (double) maxNumPieces));
                sb.append(worstDownloader.getNodeId()).
                        append(" (").append(worstDownloader.getNatType()).
                        append(")\t").append(worstDownloader.getCompletePieces()).append(" pieces").
                        append(" (").append(worstDlPercent).append("%)   ").
                        append("  ").append(totalDlSubPieces).append(" sub-pieces").
                        append("\tingoing").
                        append("(").append(worstDownloader.getIngoingConnectionsClose()).append(" close").
                        append(", ").append(worstDownloader.getIngoingConnectionsRandom()).append(" random").
                        append(")\toutgoing(").
                        append(worstDownloader.getOutgoingConnectionsClose()).append(" close, ").
                        append(worstDownloader.getOutgoingConnectionsRandom()).append(" random").
                        append(")\n");
            }
            sb.append("Best downloaders:\n");
            Iterator<Stats> it2 = bestDownloaders.iterator();
            while (it2.hasNext()) {
                Stats bestDownloader = it2.next();
                int totalDlSubPieces = bestDownloader.getDownloadedSubPiecesIntraAs() + bestDownloader.getDownloadedSubPiecesNeighbourAs() + bestDownloader.getDownloadedSubPiecesOtherAs();
                String bestDlPercent = maxNumPieces == 0 ? "0" : format.format(100 * (double) ((double) bestDownloader.getCompletePieces() / (double) maxNumPieces));
                sb.append(bestDownloader.getNodeId()).
                        append(" (").append(bestDownloader.getNatType()).
                        append(")\t").append(bestDownloader.getCompletePieces()).append(" pieces").
                        append(" (").append(bestDlPercent).append("%)   ").
                        append("  ").append(totalDlSubPieces).append(" sub-pieces").
                        append("\tingoing").
                        append("(").append(bestDownloader.getIngoingConnectionsClose()).append(" close").
                        append(", ").append(bestDownloader.getIngoingConnectionsRandom()).append(" random").
                        append(")\toutgoing(").
                        append(bestDownloader.getOutgoingConnectionsClose()).append(" close, ").
                        append(bestDownloader.getOutgoingConnectionsRandom()).append(" random").
                        append(")\n");
            }
            sb.append("Piece download stats:\n");
            int[] downloadStats = getPieceDownloadStats(overlayId, maxNumPieces);
            sb.append("[");
            for (int i = 0; i < downloadStats.length; i++) {
                sb.append(i).append(":").append(downloadStats[i]);
                if (i < downloadStats.length - 1) {
                    sb.append(",");
                }
            }
            sb.append("]");
        }
        return sb.toString();
    }

    private static class NumPiecesComparator implements Comparator<Stats> {

        @Override
        public int compare(Stats t, Stats t1) {
            if (t.getCompletePieces() > t1.getCompletePieces()) {
                return 1;
            } else if (t.getCompletePieces() < t1.getCompletePieces()) {
                return -1;
            } else {
                return 0;
            }
        }
    }
}
