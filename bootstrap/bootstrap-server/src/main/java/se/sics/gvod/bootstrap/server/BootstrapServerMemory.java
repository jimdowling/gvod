package se.sics.gvod.bootstrap.server;



import com.sun.corba.se.internal.CosNaming.BootstrapServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.kompics.ComponentDefinition;

/**
 * The <code>BootstrapServer</code> class.
 *
 * @author Cosmin Arad <cosmin@sics.se>
 * @version $Id: BootstrapServer.java 1067 2009-08-28 08:51:56Z Cosmin $
 */
public class BootstrapServerMemory extends ComponentDefinition {
    private static final Logger logger = LoggerFactory.getLogger(BootstrapServer.class);

//    Positive<VodNetwork> network = positive(VodNetwork.class);
//    Positive<Timer> timer = positive(Timer.class);
//    Negative<Web> web = negative(Web.class);
//    private final HashMap<String, HashSet<UUID>> outstandingTimeouts;
//    private final HashMap<String, HashMap<Address, PeerEntry>> cache;
//    private final HashMap<String, Long> cacheEpoch;
//    private long evictAfter;
//    private Address self;
//    private String webAddress;
//    private int webPort;
//
//    public BootstrapServer() {
//        this.cache = new HashMap<String, HashMap<Address, PeerEntry>>();
//        this.cacheEpoch = new HashMap<String, Long>();
//
//        outstandingTimeouts = new HashMap<String, HashSet<UUID>>();
//
//        subscribe(handleInit, control);
//
//        subscribe(handleWebRequest, web);
////        subscribe(handleCacheResetRequest, network);
//        subscribe(handleBootstrapMsgAddPeerRequest, network);
//        subscribe(handleBootstrapMsgGetPeersRequest, network);
//        subscribe(handleCacheEvictPeer, timer);
//    }
//    //initialization
//    private Handler<BootstrapServerInit> handleInit = new Handler<BootstrapServerInit>() {
//
//        @Override
//        public void handle(BootstrapServerInit event) {
//            evictAfter = BootstrapConfig.getBootstrapEvictPeriod();
//            self = event.getConfiguration().getBootstrapServerAddress();
//
//            webPort = event.getConfiguration().getClientWebPort();
//            webAddress = "http://" + self.getIp().getHostAddress() + ":" + webPort + "/" + self.getId() + "/";
//
//            logger.debug("Started");
//            logger.debug("{}", webAddress);
//            dumpCacheToLog();
//        }
//    };
//    //a peer is asking to be added to the bootstraplist
//    private Handler<BootstrapMsg.Heartbeat> handleBootstrapMsgAddPeerRequest = new Handler<BootstrapMsg.Heartbeat>() {
//
//        @Override
//        public void handle(BootstrapMsg.Heartbeat event) {
////                       System.out.println("test" + event.getPeerOverlays().toString());
//            logger.info("handleBootstrapMsg.AddPeerRequest from {}", event.getSource());
//            PeerEntry peer = new PeerEntry(event.getGVodSource(), event.getOverlay(),
//                    event.getGVodSource().getNatType().toString(), event.getUtility(), 0, 0);
//            Set<PeerEntry> entry = new HashSet<PeerEntry>();
//            entry.add(peer);
//            addPeerToCache(event.getSource(),
//                    entry
////                    event.getPeerOverlays()
//                    );
//            dumpCacheToLog();
//        }
//    };
//
//    private Handler<CacheEvictPeer> handleCacheEvictPeer = new Handler<CacheEvictPeer>() {
//        @Override
//        public void handle(CacheEvictPeer event) {
//            // only evict if it was not refreshed in the meantime
//            // which means the timer is not anymore outstanding
//            HashSet<UUID> overlayEvictionTimoutIds = outstandingTimeouts.get(event.getOverlay());
//            if (overlayEvictionTimoutIds != null) {
//                if (overlayEvictionTimoutIds.contains(event.getTimeoutId())) {
//                    removePeerFromCache(event.getPeerAddress(), event.getOverlay(), event.getEpoch());
//                    overlayEvictionTimoutIds.remove(event.getTimeoutId());
//                }
//            }
//            dumpCacheToLog();
//        }
//    };
//    //a node is asking for peers to bootstrap
//    /* can be improved by adding the marge an doing a beter solution
//     * fonctions of this marge
//     */
//    private Handler<BootstrapMsg.GetPeersRequest> handleBootstrapMsgGetPeersRequest = new Handler<BootstrapMsg.GetPeersRequest>() {
//        @Override
//        public void handle(BootstrapMsg.GetPeersRequest event) {
//            int peersMax = 10;
////                    event.getPeersMax();
//            logger.debug("handleGCacheGetPeerRequest from {}", event.getSource());
////            dumpCacheToLog();
//            //  System.out.println("peersMax=" + peersMax);
//            HashSet<PeerEntry> peers = new HashSet<PeerEntry>();
//            long now = System.currentTimeMillis();
//            String overlay = event.getOverlay();
//
//            HashMap<Address, PeerEntry> overlayCache = cache.get(overlay);
//
//            if (overlayCache != null) {
//                Collection<PeerEntry> entries = overlayCache.values();
//                ArrayList<PeerEntry> sorted = new ArrayList<PeerEntry>(
//                        entries);
//                if (entries.size() <= peersMax) {
//                    for (PeerEntry cacheEntry : sorted) {
//                        if (!cacheEntry.getAddress().equals(event.getSource())) {
//                            PeerEntry peerEntry = new PeerEntry(
//                                    cacheEntry.getOverlayAddress(),
//                                    overlay,
//                                    Nat.Type.NAT.toString(),
//                                    cacheEntry.getUtility(),
//                                    (int) (now - cacheEntry.getAge()),
//                                    (int) (now - cacheEntry.getFreshness())
//                                    );
//                            peers.add(peerEntry);
//                            peersMax--;
//                        }
//                    }
//                } else {
//                    // get the most recent up to peersMax entries
//                    Collections.sort(sorted);
//                    int i = 0;
//                    for (PeerEntry cacheEntry : sorted) {
//                        if (cacheEntry.getUtility() >= event.getUtility()
//                                && !cacheEntry.getAddress().equals(event.getSource())) {
//                            PeerEntry peerEntry = new PeerEntry(cacheEntry.getOverlayAddress()
//                                    , overlay
//                                    , event.getGVodSource().getNatType().toString()
//                                    , cacheEntry.getUtility()
//                                    , (int) (now - cacheEntry.getAge())
//                                    , (int) (now - cacheEntry.getFreshness())
//                                    );
//                            peers.add(peerEntry);
//                            peersMax--;
//                            if (peersMax == 0) {
//                                break;
//                            }
//                        }
//                        i++;
//                    }
//                    while (peersMax != 0 && sorted.size() > peers.size()) {
//                        if (i == sorted.size()) {
//                            i = 0;
//                        }
//                        if (!sorted.get(i).getAddress().equals(event.getSource())) {
//                            PeerEntry peerEntry = new PeerEntry(
//                                    sorted.get(i).getOverlayAddress(),
//                                    overlay,
//                                    event.getGVodSource().getNatType().toString(),
//                                    sorted.get(i).getUtility(),
//                                    (int) (now - sorted.get(i).getAge()),
//                                    (int) (now - sorted.get(i).getFreshness())
//                                    );
//                            peers.add(peerEntry);
//                        }
//                        peersMax--;
//                        i++;
//                    }
//                }
//
//            }
//            int j = 0;
//            BootstrapMsg.GetPeersResponse response = new BootstrapMsg.GetPeersResponse(
//                    self, event.getSource(), event.getTimeoutId(), peers );
//
//            trigger(response, network);
//
//            logger.debug("Responded with {} peers to peer {}",
//                    peers.size(),
//                    event.getSource());
//        }
//    };
//    private Handler<WebRequest> handleWebRequest = new Handler<WebRequest>() {
//        @Override
//        public void handle(WebRequest event) {
//            logger.debug("Handling WebRequest");
//
//            WebResponse response = new WebResponse(dumpCacheToHtml(event.getTarget()), event, 1, 1);
//            trigger(response, web);
//        }
//    };
//
////    private final void resetCache(String overlay) {
////        // cancel all eviction timers for this overlay
////        HashSet<UUID> overlayEvictionTimoutIds = outstandingTimeouts.get(overlay);
////        if (overlayEvictionTimoutIds != null) {
////            for (UUID timoutId : overlayEvictionTimoutIds) {
////                CancelTimeout ct = new CancelTimeout(timoutId);
////                trigger(ct, timer);
////            }
////
////            overlayEvictionTimoutIds.clear();
////        }
////
////// reset cache
////        HashMap<Address, PeerEntry> overlayCache = cache.get(overlay);
////        if (overlayCache != null) {
////            overlayCache.clear();
////            Long epoch = cacheEpoch.get(overlay);
////            cacheEpoch.put(overlay, 1 + epoch);
////        } else {
////            cache.put(overlay, new HashMap<Address, PeerEntry>());
////            cacheEpoch.put(overlay, 1L);
////            outstandingTimeouts.put(overlay, new HashSet<UUID>());
////        }
////
////        logger.debug("Cleared cache for " + overlay);
////        dumpCacheToLog();
////
////    }
//    private void addPeerToCache(Address address, Set<PeerEntry> overlays) {
//        if (address != null) {
//            long now = System.currentTimeMillis();
//
//            for (PeerEntry peerEntry : overlays) {
//                String overlay = peerEntry.getOverlay();
//
//                HashMap<Address, PeerEntry> overlayCache = cache.get(overlay);
//                if (overlayCache == null) {
//                    overlayCache = new HashMap<Address, PeerEntry>();
//                    cache.put(overlay, overlayCache);
//                    cacheEpoch.put(overlay, 1L);
//                    outstandingTimeouts.put(overlay, new HashSet<UUID>());
//                }
//
//                PeerEntry entry = overlayCache.get(address);
//                if (entry == null) {
//                    // add a new entry
//                    entry = new PeerEntry(
//                            peerEntry.getOverlayAddress(),
//                            overlay,
//                            Nat.Type.NAT.toString(),
//                           peerEntry.getUtility(),
//                            (int) now,
//                            (int) now);
//                    overlayCache.put(address, entry);
//
//                    // set a new eviction timeout
//                    ScheduleTimeout st = new ScheduleTimeout(evictAfter);
//                    st.setTimeoutEvent(new CacheEvictPeer(st, address, overlay,
//                            cacheEpoch.get(overlay)));
//
//                    UUID evictionTimerId = st.getTimeoutEvent().getTimeoutId();
////                    entry.setEvictionTimerId(evictionTimerId);
//                    outstandingTimeouts.get(overlay).add(evictionTimerId);
//                    trigger(st, timer);
//
//                    logger.debug("Added peer {}", address);
//                } else {
//                    // update an existing entry
//                    entry.setFreshness((int) now);
//                    entry.setUtility(peerEntry.getUtility());
//                    // cancel an old eviction timeout, if it exists
////                    UUID oldTimeoutId = entry.getEvictionTimerId();
////                    if (oldTimeoutId != null) {
////                        trigger(new CancelTimeout(oldTimeoutId), timer);
////                        outstandingTimeouts.get(overlay).remove(oldTimeoutId);
////                    }
//                    // set a new eviction timeout
//
//                    ScheduleTimeout st = new ScheduleTimeout(evictAfter);
//                    st.setTimeoutEvent(new CacheEvictPeer(st, address, overlay,
//                            cacheEpoch.get(overlay)));
//
////                    UUID evictionTimerId = st.getTimeoutEvent().getTimeoutId();
////                    entry.setEvictionTimerId(evictionTimerId);
////                    outstandingTimeouts.get(overlay).add(evictionTimerId);
////                    trigger(st, timer);
//
//                    logger.debug("Refreshed peer {}", address);
//                }
//
//            }
//        }
//    }
//
//    private void removePeerFromCache(Address address, String overlay,
//            long epoch) {
//        long thisEpoch = cacheEpoch.get(overlay);
//        if (address != null && epoch == thisEpoch) {
//            cache.get(overlay).remove(address);
//
//            logger.debug("Removed peer {}", address);
//        }
//
//    }
//
//    private void dumpCacheToLog() {
//        if (cache.isEmpty()) {
//            logger.info("cache is empty");
//        }
//        for (String overlay : cache.keySet()) {
//            dumpCacheToLog(overlay);
//        }
//
//    }
//
//    private void dumpCacheToLog(String overlay) {
//        logger.info("Overlay {} now contains:", overlay);
//        logger.info("Age=====Freshness==Utility==Peer address=========================");
//        /*        System.out.println("Overlay {} now contains:");
//        System.out.println("Age=====Freshness==Utility==Peer address=========================");
//         */
//        long now = System.currentTimeMillis();
//
//        Collection<PeerEntry> entries = cache.get(overlay).values();
//        ArrayList<PeerEntry> sorted = new ArrayList<PeerEntry>(entries);
//
//        // get all peers in most recently added order
//        Collections.sort(sorted);
//        for (PeerEntry cacheEntry : sorted) {
//            logger.info("{}\t{}\t{}\t  {}", new Object[]{
//                        durationToString(now - cacheEntry.getAge()),
//                        durationToString(now - cacheEntry.getFreshness()),
//                        cacheEntry.getUtility(),
//                        cacheEntry.getAddress().getId()});
//            /*          System.out.println(durationToString(now - cacheEntry.getAge()) + "\t" +
//            durationToString(now - cacheEntry.getFreshness()) + "\t" +
//            cacheEntry.getUtility() +
//            "\t" + cacheEntry.getPeerAddress());
//             */
//        }
//
//        logger.info("========================================================");
//    }
//
//    private String dumpCacheToHtml(String overlay) {
//        if (!cache.containsKey(overlay)) {
//            StringBuilder sb = new StringBuilder(
//                    "<!DOCTYPE html PUBLIC \"-//W3C");
//            sb.append("//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR");
//            sb.append("/xhtml1/DTD/xhtml1-transitional.dtd\"><html xmlns=\"http:");
//            sb.append("//www.w3.org/1999/xhtml\"><head><meta http-equiv=\"Conten");
//            sb.append("t-Type\" content=\"text/html; charset=utf-8\" />");
//            sb.append("<title>Kompics P2P Bootstrap Server</title>");
//            sb.append("<style type=\"text/css\"><!--.style2 {font-family: ");
//            sb.append("Arial, Helvetica, sans-serif; color: #0099FF;}--></style>");
//            sb.append("</head><body><h2 align=\"center\" class=\"style2\">");
//            sb.append("Kompics P2P Bootstrap Overlays:</h2><br>");
//            for (String o : cache.keySet()) {
//                sb.append("<a href=\"" + webAddress + o + "\">" + o + "</a>").append("<br>");
//            }
//
//            sb.append("</body></html>");
//            return sb.toString();
//        }
//
//        StringBuilder sb = new StringBuilder("<!DOCTYPE html PUBLIC \"-//W3C");
//        sb.append("//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR");
//        sb.append("/xhtml1/DTD/xhtml1-transitional.dtd\"><html xmlns=\"http:");
//        sb.append("//www.w3.org/1999/xhtml\"><head><meta http-equiv=\"Conten");
//        sb.append("t-Type\" content=\"text/html; charset=utf-8\" />");
//        sb.append("<title>Kompics P2P Bootstrap Server</title>");
//        sb.append("<style type=\"text/css\"><!--.style2 {font-family: ");
//        sb.append("Arial, Helvetica, sans-serif; color: #0099FF;}--></style>");
//        sb.append("</head><body><h2 align=\"center\" class=\"style2\">");
//        sb.append("Kompics P2P Bootstrap Cache for " + overlay + "</h2>");
//        sb.append("<table width=\"600\" border=\"0\" align=\"center\"><tr>");
//        sb.append("<th class=\"style2\" width=\"100\" scope=\"col\">Count</th>");
//        sb.append("<th class=\"style2\" width=\"80\" scope=\"col\">Age</th>");
//        sb.append("<th class=\"style2\" width=\"120\" scope=\"col\">Freshness</th>");
//        sb.append("<th class=\"style2\" width=\"80\" scope=\"col\">utility</th>");
//        sb.append("<th class=\"style2\" width=\"300\" scope=\"col\">" + overlay + " id</th>");
//        sb.append("<th class=\"style2\" width=\"300\" scope=\"col\">Peer address</th></tr>");
//        long now = System.currentTimeMillis();
//
//        Collection<PeerEntry> entries = cache.get(overlay).values();
//        ArrayList<PeerEntry> sorted = new ArrayList<PeerEntry>(entries);
//
//        // get all peers in most recently added order
//        Collections.sort(sorted);
//
//        int count = 1;
//
//        for (PeerEntry cacheEntry : sorted) {
//            sb.append("<tr>");
//            sb.append("<td><div align=\"center\">").append(count++);
//            sb.append("</div></td>");
//            sb.append("<td><div align=\"right\">");
//            sb.append(durationToString(now - cacheEntry.getAge()));
//            sb.append("</div></td><td><div align=\"right\">");
//            sb.append(durationToString(now - cacheEntry.getFreshness()));
//            sb.append("</div></td><td><div align=\"right\">");
//            sb.append(cacheEntry.getUtility());
//            sb.append("</div></td><td><div align=\"center\">");
//            String wAddress = "http://" + cacheEntry.getAddress().getIp().getHostAddress() + ":"
//                    + webPort + "/" + cacheEntry.getAddress().getId() + "/";
//            /*            sb.append("<a href=\"").append(webAddress).append("\">");
//            sb.append(cacheEntry.getOverlayAddress().toString()).append("</a>");*/
//            sb.append("</div></td><td><div align=\"left\">");
//            sb.append(cacheEntry.getAddress());
//            sb.append("</div></td>");
//            sb.append("</tr>");
//        }
//
//        sb.append("</table></body></html>");
//        return sb.toString();
//    }
//
//    private String durationToString(long duration) {
//        StringBuilder sb = new StringBuilder();
//
//        // get duration in seconds
//        duration /=
//                1000;
//
//        int s = 0, m = 0, h = 0, d = 0, y = 0;
//        s =
//                (int) (duration % 60);
//        // get duration in minutes
//        duration /=
//                60;
//        if (duration > 0) {
//            m = (int) (duration % 60);
//            // get duration in hours
//            duration /=
//                    60;
//            if (duration > 0) {
//                h = (int) (duration % 24);
//                // get duration in days
//                duration /=
//                        24;
//                if (duration > 0) {
//                    d = (int) (duration % 365);
//                    // get duration in years
//                    y =
//                            (int) (duration / 365);
//                }
//
//            }
//        }
//
//        boolean printed = false;
//
//        if (y > 0) {
//            sb.append(y).append("y");
//            printed =
//                    true;
//        }
//
//        if (d > 0) {
//            sb.append(d).append("d");
//            printed =
//                    true;
//        }
//
//        if (h > 0) {
//            sb.append(h).append("h");
//            printed =
//                    true;
//        }
//
//        if (m > 0) {
//            sb.append(m).append("m");
//            printed =
//                    true;
//        }
//
//        if (s > 0 || printed == false) {
//            sb.append(s).append("s");
//        }
//
//        return sb.toString();
//    }
}
