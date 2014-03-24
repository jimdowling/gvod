package se.sics.gvod.bootstrap.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import se.sics.gvod.bootstrap.config.BootstrapConfig;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import se.sics.gvod.timer.TimeoutId;
import java.util.logging.Level;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.bootstrap.msgs.BootstrapMsg;

import se.sics.kompics.Handler;
import se.sics.gvod.address.Address;
import se.sics.gvod.common.VodDescriptor;
import se.sics.gvod.common.UtilityVod;
import se.sics.gvod.common.msgs.NatReportMsg;
import se.sics.gvod.common.msgs.NatReportMsg.NatReport;
import se.sics.gvod.common.util.ToVodAddr;
import se.sics.gvod.config.BaseCommandLineConfig;
import se.sics.gvod.config.VodConfig;
import se.sics.gvod.net.VodAddress.NatType;
import se.sics.gvod.net.VodNetwork;
import se.sics.kompics.Stop;
import se.sics.gvod.timer.CancelTimeout;
import se.sics.gvod.timer.SchedulePeriodicTimeout;
import se.sics.gvod.timer.Timeout;
import se.sics.gvod.timer.Timer;
import se.sics.ipasdistances.PrefixMatcher;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Negative;
import se.sics.kompics.Positive;
import se.sics.kompics.web.Web;
import se.sics.kompics.web.WebRequest;
import se.sics.kompics.web.WebResponse;

/**
 * The <code>BootstrapServer</code> class.
 * 
 * Requirements: 
 * This Bootstrap server assumes that there is a webserver running on the same
 * host that can server files from 'torrentspath' directory - default /var/www (from Apache2). 
 * The userid running this Bootstrap server should have write permissions to the 'torrentspath' directory.
 * You can set the 'torrentspath' directory as a switch.
 *
 *
 * CREATE TABLE nodes ( id INT NOT NULL, ip INT UNSIGNED NOT NULL, port SMALLINT
 * UNSIGNED NOT NULL, asn SMALLINT UNSIGNED NOT NULL DEFAULT 0, country char(2)
 * NOT NULL DEFAULT 'se', nat_type TINYINT UNSIGNED NOT NULL, open BOOLEAN NOT
 * NULL, mtu SMALLINT UNSIGNED NOT NULL DEFAULT 1500, helper BOOLEAN NOT NULL, last_ping TIMESTAMP
 * DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, joined TIMESTAMP NOT
 * NULL, PRIMARY KEY(id), KEY open_stable_idx(open, last_ping), KEY
 * country_idx(country), KEY asn_idx(asn) ) engine=innodb;
 *
 *
 * // If the parent is already in the db, we can look up it's ip, port, id
 * using its id CREATE TABLE parents ( parent_id INT UNSIGNED NOT NULL, id INT
 * NOT NULL, PRIMARY KEY(parent_id, id), KEY id_idx (id), FOREIGN KEY (id)
 * REFERENCES nodes(id) ON DELETE CASCADE ) engine=innodb;
 *
 *
 * Select seeds SELECT id, ip, port, nat_type, utility FROM nodes, overlays
 * WHERE overlay_id=1 AND utility = 10 JOIN overlays using id; SELECT id, ip,
 * port, nat_type, utility FROM nodes, overlays WHERE overlay_id=1 JOIN using
 * id; Select downloaders SELECT t2.overlay_id, t1.id, t1.ip, t1.port,
 * t1.nat_type, t2.utility FROM nodes AS t1, overlays AS t2 WHERE t1.id = t2.id;
 * WHERE overlay=VAL AND utility closestTo MYUTILITY;
 *
 *
 * CREATE TABLE overlay_details ( overlay_id INT NOT NULL, overlay_name
 * char(128) NOT NULL, overlay_description VARCHAR(512) NULL, date_added
 * TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, overlay_picture VARCHAR(255)
 * NULL, PRIMARY KEY (overlay_id), KEY name_idx(overlay_name) ) engine=innodb;
 *
 * CREATE TABLE overlays ( id INT NOT NULL, overlay_id INT NOT NULL, utility INT
 * NOT NULL, PRIMARY KEY (id, overlay_id), KEY overlay_idx (overlay_id), KEY
 * utility_idx(utility), CONSTRAINT id_fk FOREIGN KEY (id) REFERENCES nodes(id)
 * ON DELETE CASCADE, CONSTRAINT overlay_fk FOREIGN KEY (overlay_id) REFERENCES
 * overlay_details(overlay_id) ON DELETE CASCADE ) engine=innodb; *
 *
 *
 *
 *
 * insert into nodes values(12, INET_ATON('192.168.10.50'), 65535, 344, 'se',
 * 128, TRUE,1500, null,NOW()); insert into overlays values(12, 1, 10); insert
 * into overlay_details values(1, 'naked gun', 'leslie nielson', null, null);
 *
 * TO SELECT IP as bytes, rather than a String: SELECT (ipAddress >> 24) as
 * firstOctet, (ipAddress>>16) & 255 as secondOctet, (ipAddress>>8) & 255 as
 * thirdOctet, ipAddress & 255 as fourthOctet from ips; SELECT (nat_type >> 7)
 * as natted, (nat_type >> 5) & 255 as mp, (nat_type >> 3) & 255 as ap,
 * (nat_type >> 1) & 255 as fp, nat_type & 255 as aap, from ips;
 *
 *
 *
 *
 *
 *
 * Size is 4 (overlay) + 4 (ip) + 2 (port) + 4 (nat) + 4 (id) + 4 (utility) + 4
 * (ping) + 4 (joined) + INDEXES = 30 bytes + indexes
 *
 * For 1MB memory and 2 replicas, i can store ~17,000 entries For 1GB memory and
 * 2 replicas, i can store ~17 million entries
 *
 * Use NOW() to set the joined timestamp
 *
 *
 * mysql> SELECT INET_ATON('192.168.10.50'); +----------------------------+ |
 * INET_ATON('192.168.10.50') | +----------------------------+ | 3232238130 |
 * +----------------------------+ 1 row in set (0.00 sec)
 *
 * mysql> SELECT INET_NTOA(839559360); +----------------------+ |
 * INET_NTOA(839559360) | +----------------------+ | 50.10.168.192 |
 * +----------------------+ 1 row in set (0.00 sec)
 *
 *
 *
 * MONITORING - NAT REPORTS
 *
 *
 * CREATE TABLE nat_reports ( src_addr VARCHAR(64) NOT NULL, src_nat VARCHAR(64)
 * NOT NULL, target_addr VARCHAR(64) NOT NULL, target_nat VARCHAR(64) NOT NULL,
 * msg VARCHAR(255) NOT NULL, success_count INT NOT NULL DEFAULT 0, fail_count
 * INT NOT NULL DEFAULT 0, time_taken INT NOT NULL DEFAULT 0, last_modified
 * TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, PRIMARY KEY(src_addr,
 * target_addr, time_taken, msg), KEY nat_idx(src_nat, target_nat), KEY
 * src_idx(src_addr, target_addr) ) engine=innodb;
 *
 *
 *
 *
 */
public class BootstrapServerMysql extends ComponentDefinition {

    private static final Logger logger = LoggerFactory.getLogger(BootstrapServerMysql.class);
    Positive<VodNetwork> network = positive(VodNetwork.class);
    Positive<Timer> timer = positive(Timer.class);
    Negative<Web> web = negative(Web.class);
    private long evictAfter;
    private Address self;
    private String jdbcUrl;
    private String user;
    private String passwd;
    private Connection con = null;
    private int numRowsAffected = 0;
    private int numRows = 0;
    private TimeoutId cleanupId;
    private TimeoutId checkUploadsId;
    private boolean asnMatcher;
    private boolean countryMatcher = false;
    private PrefixMatcher pm = null;
    Map<Integer, SortedMap<Integer, ByteBuffer>> addingOverlaysBuffer
            = new HashMap<Integer, SortedMap<Integer, ByteBuffer>>();
    Map<Integer, String> addingDescriptions = new HashMap<Integer, String>();
    Map<Integer, String> addingImgUrls = new HashMap<Integer, String>();

    /**
     * <HelpNodeAddress, LastHeardFrom>
     */
    Map<Address, Helper> helperNodes = new HashMap<Address, Helper>();

    public final class Helper {

        private final boolean available;
        private final long lastPing;

        public Helper(boolean available, long lastPing) {
            this.available = available;
            this.lastPing = lastPing;
        }

        public long getLastPing() {
            return lastPing;
        }

        public boolean isAvailable() {
            return available;
        }
    }

    public final class CheckForNewUploadsTimeout extends Timeout {

        public CheckForNewUploadsTimeout(SchedulePeriodicTimeout request) {
            super(request);
        }

    }

    public BootstrapServerMysql() {

        subscribe(handleAddOverlayReq, network);
        subscribe(handleBootstrapMsgGetPeersRequest, network);
        subscribe(handleHeartbeat, network);
        subscribe(handleNatReportMsg, network);
        subscribe(handleCheckForNewUploadsTimeout, timer);
        subscribe(handleCleanupStaleTimeout, timer);
        subscribe(handleInit, control);
        subscribe(handleStop, control);
        subscribe(handleWebRequest, web);
    }
    Handler<BootstrapServerInit> handleInit = new Handler<BootstrapServerInit>() {
        @Override
        public void handle(BootstrapServerInit event) {
            evictAfter = BootstrapConfig.getBootstrapEvictPeriod();
            self = event.getConfiguration().getBootstrapServerAddress();

            jdbcUrl = BootstrapConfig.getJdbcUrl();
            user = BootstrapConfig.getMySqlUsername();
            passwd = BootstrapConfig.getMySqlPassword();

            logger.debug("Started");

            try {
                Class.forName("com.mysql.jdbc.Driver").newInstance();
                con = DriverManager.getConnection(jdbcUrl, user, passwd);
                if (!con.isClosed()) {
                    logger.info("Successfully connected to MySQL server using TCP/IP.");
                }
            } catch (InstantiationException ex) {
                java.util.logging.Logger.getLogger(BootstrapServerMysql.class.getName()).log(Level.SEVERE, null, ex);
                throw new IllegalStateException(ex.getMessage());
            } catch (IllegalAccessException ex) {
                java.util.logging.Logger.getLogger(BootstrapServerMysql.class.getName()).log(Level.SEVERE, null, ex);
                throw new IllegalStateException(ex.getMessage());
            } catch (ClassNotFoundException ex) {
                java.util.logging.Logger.getLogger(BootstrapServerMysql.class.getName()).log(Level.SEVERE, null, ex);
                throw new IllegalStateException(ex.getMessage());
            } catch (SQLException ex) {
                // hackish way to mock the DB
//                if (!delegator.isUnitTest()) {
                java.util.logging.Logger.getLogger(BootstrapServerMysql.class.getName()).log(Level.SEVERE, null, "SQLException: " + ex.getMessage());
                java.util.logging.Logger.getLogger(BootstrapServerMysql.class.getName()).log(Level.SEVERE, null, "SQLState: " + ex.getSQLState());
                java.util.logging.Logger.getLogger(BootstrapServerMysql.class.getName()).log(Level.SEVERE, null, "VendorError: " + ex.getErrorCode());
                throw new IllegalStateException(ex.getMessage());
//                }
            }

            if (event.isAsnMatcher()) {
                asnMatcher = true;
                pm = PrefixMatcher.getInstance();
            }

            SchedulePeriodicTimeout spt
                    = new SchedulePeriodicTimeout(evictAfter, evictAfter);
            spt.setTimeoutEvent(new CheckForNewUploadsTimeout(spt));
            cleanupId = spt.getTimeoutEvent().getTimeoutId();
            trigger(spt, timer);

            SchedulePeriodicTimeout checkUploads
                    = new SchedulePeriodicTimeout(20*1000, 20*1000);
            checkUploads.setTimeoutEvent(new CheckForNewUploadsTimeout(spt));
            checkUploadsId = spt.getTimeoutEvent().getTimeoutId();
            trigger(checkUploads, timer);
        }
    };

    Handler<BootstrapMsg.HelperHeartbeat> handleBootstrapHelperHeartbeat
            = new Handler<BootstrapMsg.HelperHeartbeat>() {
                @Override
                public void handle(BootstrapMsg.HelperHeartbeat event) {

                    Helper h = new Helper(event.isSpace(), System.currentTimeMillis());
                    helperNodes.put(event.getSource(), h);
                }
            };
    
    Handler<BootstrapMsg.GetPeersRequest> handleBootstrapMsgGetPeersRequest = new Handler<BootstrapMsg.GetPeersRequest>() {
        @Override
        public void handle(BootstrapMsg.GetPeersRequest event) {
            long baseTime = System.currentTimeMillis();
            logger.info("GetPeersRequest from {} : timeoutId {}",
                    event.getSource(), event.getTimeoutId());

            List<VodDescriptor> peers = null;
            // specific bootstrap to an overlay,
            if (event.getOverlay() != VodConfig.SYSTEM_OVERLAY_ID) {
                peers = queryOverlay(event.getOverlay(), event.getUtility());
            } else { // specific bootstrap to find stun servers
                peers = queryOpenNodes(event.getSource());
            }
            int overlayId = event.getOverlay();
            BootstrapMsg.GetPeersResponse response = new BootstrapMsg.GetPeersResponse(
                    ToVodAddr.systemAddr(self), event.getVodSource(),
                    event.getTimeoutId(), overlayId, peers);

            trigger(response, network);

            logger.info("Responded with {} peers to peer {} for overlayId "
                    + event.getOverlay() + " Time taken:  "
                    + (System.currentTimeMillis() - baseTime),
                    peers.size(), event.getSource().getId());
            logger.info(response.toString());

            StringBuilder sb = new StringBuilder();
            for (VodDescriptor v : peers) {
                sb.append(v.getVodAddress().getIp());
            }
            logger.info("Peers: " + sb.toString());
        }
    };

    private List<VodDescriptor> queryOpenNodes(Address addr) {
        List<VodDescriptor> entries = new ArrayList<VodDescriptor>();
        if (!validateDbConnection()) {
            logger.warn("Couldn't get DB connection when querying open nodes.");
            return entries;
        }

        int id = addr.getId();
        Statement stmt = null;
        ResultSet rs = null;
        boolean hadResults = false;
        try {
            stmt = con.createStatement();

            // This query should use the open_stable_idx.
            // It selects N random open nodes from gvod that have pinged the bootstrap server in the last 60 seconds
            StringBuilder selectQuery = new StringBuilder().append("SELECT id, "
                    + "INET_NTOA(ip), port, nat_type, mtu, asn, "
                    + " (NOW()-joined) "
                    + "FROM nodes WHERE ").
                    append("id != ").append(id).
                    append(" AND open=TRUE AND (NOW()-last_ping) < 300000 ORDER BY RAND() LIMIT ").
                    append(BootstrapConfig.DEFAULT_NUM_NODES_RETURNED);

            // OPEN nat =>  select * from nodes where (nat_type >> 7 & 1) = 1;
            hadResults = stmt.execute(selectQuery.toString());

            while (hadResults) {
                rs = stmt.getResultSet();

                while (rs.next()) {
                    try {
                        int nodeId = rs.getInt(1);
                        String nodeIpAsString = rs.getString(2);
                        InetAddress nodeIp = InetAddress.getByName(nodeIpAsString);
                        int nodePort = rs.getInt(3);
                        short natPolicy = rs.getShort(4);
                        short mtu = rs.getShort(5);
                        int asn = rs.getInt(6);
                        int age = rs.getInt(7);
                        // age in minutes
                        age = (age / 1000) / 60;
                        if (age > 65535) {
                            age = 65535;
                        }
                        Address nodeAddr = new Address(nodeIp, nodePort, nodeId);
                        VodDescriptor entry = new VodDescriptor(
                                new VodAddress(nodeAddr, VodConfig.SYSTEM_OVERLAY_ID,
                                        natPolicy, null), new UtilityVod(0), age, mtu);
                        entries.add(entry);
                    } catch (UnknownHostException ex) {
                        java.util.logging.Logger.getLogger(BootstrapServerMysql.class.getName()).log(
                                Level.SEVERE, null, ex);
                    }
                }
                // ADD THESE TO ENTRIES

                hadResults = stmt.getMoreResults();
            }

        } catch (SQLException ex) {
            // handle any errors
            logger.warn("SQLException: " + ex.getMessage());
            logger.warn("SQLState: " + ex.getSQLState());
            logger.warn("VendorError: " + ex.getErrorCode());
        } finally {
            // it is a good idea to release
            // resources in a finally{} block
            // in reverse-order of their creation
            // if they are no-longer needed

            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException sqlEx) {
                } // ignore

                rs = null;
            }

            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException sqlEx) {
                } // ignore

                stmt = null;
            }

        }
        return entries;
    }

    private List<VodDescriptor> queryOverlay(int overlayId, int utility) {
        List<VodDescriptor> entries = new ArrayList<VodDescriptor>();
        if (!validateDbConnection()) {
            logger.warn("Couldn't get DB connection when querying overlay.");
            return entries;
        }

        Statement stmt = null;
        ResultSet rs = null;
        boolean hadResults = false;
        try {
            stmt = con.createStatement();

            // This query should use the open_stable_idx.
            // It selects N random open nodes from gvod that have pinged the bootstrap server in the last 60 seconds
            StringBuilder selectQuery = new StringBuilder().append("SELECT nodes.id, "
                    + "INET_NTOA(ip), port, nat_type, utility, mtu, asn,  "
                    + "(NOW()-last_ping), (NOW()-joined) "
                    + "FROM nodes, overlays").
                    append(" WHERE nodes.id = overlays.id").
                    append(" AND overlay_id=").append(overlayId).
                    //                    append(" AND utility > ").append(utility).
                    append(" AND (NOW()-last_ping) < 180000 ORDER BY utility DESC LIMIT ").
                    append(BootstrapConfig.DEFAULT_NUM_NODES_RETURNED);

            logger.debug(selectQuery.toString());
            hadResults = stmt.execute(selectQuery.toString());

            while (hadResults) {
                rs = stmt.getResultSet();

                while (rs.next()) {
                    try {
                        int nodeId = rs.getInt(1);
                        String nodeIpAsString = rs.getString(2);
                        InetAddress nodeIp = InetAddress.getByName(nodeIpAsString);
                        int nodePort = rs.getInt(3);
                        short nodeNatType = rs.getShort(4);
                        int nodeUtility = rs.getInt(5);
                        int mtu = rs.getInt(6);
//                        int asn = rs.getInt(7);
                        Address nodeAddr = new Address(nodeIp, nodePort, nodeId);

                        VodAddress vodAddr = new VodAddress(nodeAddr, overlayId, nodeNatType, null);
                        if (!vodAddr.isOpen()) {
                            // Now find all the parents for this node, if it is private
                            Set<Address> parents = queryParents(nodeId);
                            vodAddr = new VodAddress(nodeAddr, overlayId, nodeNatType, parents);
                        }

                        VodDescriptor entry = new VodDescriptor(vodAddr,
                                new UtilityVod(nodeUtility), 0, mtu);
                        entries.add(entry);
                    } catch (UnknownHostException ex) {
                        java.util.logging.Logger.getLogger(BootstrapServerMysql.class.getName()).log(
                                Level.SEVERE, null, ex);
                    }
                }
                // ADD THESE TO ENTRIES

                hadResults = stmt.getMoreResults();
            }

        } catch (SQLException ex) {
            // handle any errors
            logger.warn("SQLException: " + ex.getMessage());
            logger.warn("SQLState: " + ex.getSQLState());
            logger.warn("VendorError: " + ex.getErrorCode());
        } finally {
            // it is a good idea to release
            // resources in a finally{} block
            // in reverse-order of their creation
            // if they are no-longer needed

            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException sqlEx) {
                } // ignore

                rs = null;
            }

            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException sqlEx) {
                } // ignore

                stmt = null;
            }

        }

        return entries;
    }

    Set<Address> queryParents(int nodeId) {
        Set<Address> parents = new HashSet<Address>();
        if (!validateDbConnection()) {
            logger.warn("Couldn't get DB connection when querying overlay.");
            return parents;
        }

        Statement stmt = null;
        ResultSet rs = null;
        boolean hadResults = false;
        try {
            stmt = con.createStatement();

            StringBuilder parentsQuery = new StringBuilder().append("SELECT "
                    + "INET_NTOA(nodes.ip), nodes.port, nodes.id  FROM nodes, parents WHERE "
                    + "parents.id = ").append(nodeId).append(" AND parents.parent_id=nodes.id");
            String queryStr = parentsQuery.toString();
            logger.debug(queryStr);
            hadResults = stmt.execute(queryStr);
            while (hadResults) {
                rs = stmt.getResultSet();
                while (rs.next()) {
                    try {
                        String parentIpAsString = rs.getString(1);
                        InetAddress parentIp = InetAddress.getByName(parentIpAsString);
                        int parentPort = rs.getInt(2);
                        int parentId = rs.getInt(3);
                        Address parentAddr = new Address(parentIp, parentPort, parentId);
                        parents.add(parentAddr);
                    } catch (UnknownHostException ex) {
                        java.util.logging.Logger.getLogger(BootstrapServerMysql.class.getName()).log(
                                Level.SEVERE, null, ex);
                    }
                }
                hadResults = stmt.getMoreResults();
            }

        } catch (SQLException ex) {
            // handle any errors
            logger.warn("SQLException: " + ex.getMessage());
            logger.warn("SQLState: " + ex.getSQLState());
            logger.warn("VendorError: " + ex.getErrorCode());
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException sqlEx) {
                } // ignore
            }

            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException sqlEx) {
                } // ignore
            }

        }

        return parents;
    }
    //a peer is asking to be added to the bootstraplist
    Handler<BootstrapMsg.Heartbeat> handleHeartbeat = new Handler<BootstrapMsg.Heartbeat>() {
        @Override
        public void handle(BootstrapMsg.Heartbeat event) {
            logger.debug("handleBootstrapMsg. Heartbeat recvd from {}", event.getVodSource().getId());

            insertNodes(event.getVodSource(),
                    event.getSeeders(),
                    event.getDownloaders(),
                    event.getVodSource().getNatPolicy(),
                    event.getVodSource().getNatType() == NatType.OPEN ? true : false,
                    event.getMtu(), 
                    event.isHelper());
        }
    };

    private boolean insertNodes(VodAddress nodeAddr,
            Set<Integer> seeders, Map<Integer, Integer> downloaders,
            short natPolicy, boolean isOpen, short mtu, boolean isHelper) {

        if (!validateDbConnection()) {
            logger.warn("Couldn't get DB connection when inserting nodes.");
            return false;
        }

        InetAddress ipAddr = nodeAddr.getIp();
        int id = nodeAddr.getId();
        int port = nodeAddr.getPort();

        int asn = 0;
        if (asnMatcher) {
            asn = pm.matchIPtoAS(ipAddr.getHostAddress());
        }

        int country = 0;
        if (countryMatcher) {
            // TODO -get country
        }

        StringBuilder overlays = new StringBuilder();

        boolean first = true;
        for (Integer seed : seeders) {
            if (!first) {
                overlays.append(',');
            } else {
                first = false;
            }
            overlays.append("(").append(id).append(',').append(seed).append(",")
                    .append(VodConfig.SEEDER_UTILITY_VALUE).append(")");
        }

        Set<Entry<Integer, Integer>> entries = downloaders.entrySet();
        for (Entry<Integer, Integer> entry : entries) {
            if (!first) {
                overlays.append(',');
            } else {
                first = false;
            }
            overlays.append("(").append(id).append(',').append(entry.getKey()).append(",").append(entry.getValue()).append(")");
        }

        StringBuilder insertNodeQuery = new StringBuilder();
        insertNodeQuery.append("INSERT INTO nodes"
                + "(id, ip, port, asn, country, nat_type, open, mtu, helper, last_ping, joined) VALUES (").
                append(id).append(", ").
                append("INET_ATON('").append(ipAddr.getHostAddress()).append("'),").append(port).
                append(", ").append(asn).append(", ").append("'se'").append(", ").
                append(natPolicy).append(", ").
                append(isOpen).append(", ").
                append(mtu).append(", ").
                append(isHelper).append(", ").
                append("NOW(), NOW()) ON DUPLICATE KEY UPDATE last_ping=").append("NOW()");

        StringBuilder insertOverlaysQuery = new StringBuilder();
        insertOverlaysQuery.append(" INSERT INTO overlays"
                + "(id, overlay_id, utility) VALUES ").append(overlays)
                .append(" ON DUPLICATE KEY UPDATE utility=0");
        logger.debug(insertNodeQuery.toString());

        StringBuilder insertParentsQuery = new StringBuilder();
        StringBuilder deleteParentsQuery = new StringBuilder();
        if (nodeAddr.hasParents()) {
            deleteParentsQuery.append("DELETE FROM parents where id=")
                    .append(id);
            insertParentsQuery.append("INSERT INTO parents VALUES ");
            for (Address parent : nodeAddr.getParentsAsList()) {
                insertParentsQuery.append("(").append(parent.getId()).append(", ")
                        .append(id).append("),");
            }
            // turn last comma into a semi-colon
            int pos = insertParentsQuery.lastIndexOf(",");
            insertParentsQuery.replace(pos, pos + 1, "");
            logger.debug(deleteParentsQuery.toString());
            logger.debug(insertParentsQuery.toString());
        }

        Statement stmt = null;
        try {
            stmt = con.createStatement();

            stmt.addBatch(insertNodeQuery.toString());
            if (!seeders.isEmpty()) {
                stmt.addBatch(insertOverlaysQuery.toString());
            }
            if (nodeAddr.hasParents()) {
                stmt.addBatch(deleteParentsQuery.toString());
                stmt.addBatch(insertParentsQuery.toString());
            }

            int[] results = stmt.executeBatch();
            logger.debug("Number of results {}", results.length);
            if (results.length > 0) {
                logger.debug("Number of rows inserted/updated for nodes {}", results[0]);
            }
            if (!seeders.isEmpty() && results.length > 1) {
                logger.debug("Number of rows inserted/updated for overlays {} ", results[1]);
            }
            if (nodeAddr.hasParents() && results.length > 3) {
                logger.debug("Number of rows inserted for parents {} ", results[3]);
            }
        } catch (SQLException ex) {
            // handle any errors
            logger.warn("SQLException: " + ex.getMessage());
            logger.warn("SQLState: " + ex.getSQLState());
            logger.warn("VendorError: " + ex.getErrorCode());
        } finally {
            // it is a good idea to release
            // resources in a finally{} block
            // in reverse-order of their creation
            // if they are no-longer needed

            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException sqlEx) {
                } // ignore

                stmt = null;
            }

        }
        return true;
    }
    Handler<BootstrapMsg.AddOverlayReq> handleAddOverlayReq = new Handler<BootstrapMsg.AddOverlayReq>() {
        @Override
        public void handle(BootstrapMsg.AddOverlayReq event) {

            int overlayId = event.getOverlayId();
            String overlayName = event.getOverlayName();
            String description = event.getDescription();
            String imageUrl = event.getImageUrl();
            logger.debug("BootstrapMsg.AddOverlayReq recvd from {} : {}: "
                    + overlayId + ", " + overlayName + ", " + description + ", "
                    + imageUrl + " , "
                    + event.getVodSource().getId(), event.getTimeoutId());

            String torrentFile = BootstrapConfig.getWebServerTorrentsPath()
                    + File.separator + event.getOverlayName();
            int part = event.getPart();
            int numParts = event.getNumParts();
            byte[] recvdBytes = event.getTorrentData();
            SortedMap<Integer, ByteBuffer> torrent = addingOverlaysBuffer.get(overlayId);
            if (torrent == null) {
                torrent = new TreeMap<Integer, ByteBuffer>();
                addingOverlaysBuffer.put(overlayId, torrent);
            }
            if (part == 0) {
                addingDescriptions.put(overlayId, description);
                addingImgUrls.put(overlayId, imageUrl);
            }

            boolean finished = torrent.size() == numParts ? true : false;
            boolean res = false;
            if (!torrent.containsKey(part)) {
                ByteBuffer recvdData = ByteBuffer.wrap(recvdBytes);
                torrent.put(part, recvdData);
                if (torrent.size() == numParts) {
                    int sz = 0;
                    for (ByteBuffer b : torrent.values()) {
                        sz += b.array().length;
                    }
                    ByteBuffer fullData = ByteBuffer.allocate(sz);
                    for (ByteBuffer b : torrent.values()) {
                        fullData.put(b);
                    }
                    // hackish way to mock the DB
//                    if (!delegator.isUnitTest()) {
                    res = insertOverlay(overlayId, overlayName,
                            addingDescriptions.get(overlayId),
                            addingImgUrls.get(overlayId),
                            torrentFile, fullData.array());
//                    }
                    finished = true;
                    addingOverlaysBuffer.remove(overlayId);
                    addingDescriptions.remove(overlayId);
                    addingImgUrls.remove(overlayId);
                }
            } else {
                logger.debug("Already received this part for this overlay id {} : {}",
                        overlayId, part);
            }
            trigger(new BootstrapMsg.AddOverlayResp(new VodAddress(self, overlayId),
                    event.getVodSource(), overlayId, res, finished, event.getTimeoutId()),
                    network);
            logger.debug("Sending AddOverlayResp to {}/{} to " + event.getVodSource().getPeerAddress()
                    + " Part: " + part,
                    torrent.size(), numParts);
        }
    };

    private boolean insertOverlay(int overlayId, String overlayName, String description,
            String imageUrl, String torrentFile, byte[] torrentData) {
        if (!validateDbConnection()) {
            logger.warn("Couldn't get DB connection when inserting overlay entry.");
            return false;
        }

        description = description == null ? "" : description;
        imageUrl = imageUrl == null ? "" : imageUrl;

        StringBuilder insertOverlay = new StringBuilder();
        insertOverlay.append("INSERT INTO overlay_details"
                + "(overlay_id, overlay_name, overlay_description, overlay_picture) VALUES (")
                .append(overlayId).append(", '").
                append(overlayName).append("','").
                append(description).append("', '").
                append(imageUrl).append("'").
                append(")").append(" ON DUPLICATE KEY UPDATE overlay_description='")
                .append(description).append("'");

        logger.debug(insertOverlay.toString());

        Statement stmt = null;
        try {
            stmt = con.createStatement();
            numRowsAffected = stmt.executeUpdate(insertOverlay.toString());
            logger.debug("Number of rows inserted/updated for overlay_details: " + numRowsAffected);
            logger.debug("Inserted overlay_details: " + overlayId + ", " + overlayName + ", "
                    + description + ", " + imageUrl);
        } catch (SQLException ex) {
            // handle any errors
            logger.warn("SQLException: " + ex.getMessage());
            logger.warn("SQLState: " + ex.getSQLState());
            logger.warn("VendorError: " + ex.getErrorCode());
            return false;
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException sqlEx) {
                } // ignore

                stmt = null;
            }
        }

        FileOutputStream fos = null;
        try {
            File f = new File(torrentFile + ".data");
            if (f.exists()) {
                f.delete();
            }
            fos = new FileOutputStream(f);
            fos.write(torrentData);
        } catch (FileNotFoundException ex) {
            java.util.logging.Logger.getLogger(BootstrapServerMysql.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(BootstrapServerMysql.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException ex) {
                    java.util.logging.Logger.getLogger(BootstrapServerMysql.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        return true;
    }

    /**
     * If a new file has been uploaded, and we have some cloud seeders available
     * ask an available cloud seeder to download the file and re-seed it.
     */
    Handler<CheckForNewUploadsTimeout> handleCheckForNewUploadsTimeout
            = new Handler<CheckForNewUploadsTimeout>() {
                @Override
                public void handle(CheckForNewUploadsTimeout event) {

                    Statement stmt = null;
                    ResultSet rs = null;
                    boolean hadResults = false;
                    List<String> videosNeedingHelp = new ArrayList<String>();
                    try {
                        if (!validateDbConnection()) {
                            logger.warn("Couldn't get DB connection when checking for new uploads");
                            return;
                        }
                        stmt = con.createStatement();

                        StringBuilder query = new StringBuilder();
                        // find any movies that have only 1 node as an uploader and that node is not
                        // a cloud-helper node. 
                        // Note: This doesn't include where nodes need a helper because the b/w is too low.
                        query.append("SELECT distinct(overlay_name) FROM overlay_details,overlays,nodes "
                                + "WHERE (SELECT COUNT(*) from overlays WHERE "
                                + "overlay_details.overlay_id = overlays.overlay_id) = 1 "
                                + "AND overlays.id=nodes.id AND nodes.helper=0"
                        );

                        hadResults = stmt.execute(query.toString());

                        while (hadResults) {
                            rs = stmt.getResultSet();
                            while (rs.next()) {
                                String name = rs.getString(1);
                                StringBuilder sb = new StringBuilder();
                                sb.append("gvod://http:/").append(self.getIp())
                                .append("/gvod/").append(name).append(".data");
                                videosNeedingHelp.add(sb.toString());
                            }
                            hadResults = stmt.getMoreResults();
                        }
                    } catch (SQLException ex) {
                        // handle any errors
                        logger.warn("SQLException: " + ex.getMessage());
                        logger.warn("SQLState: " + ex.getSQLState());
                        logger.warn("VendorError: " + ex.getErrorCode());
                    } finally {
                        if (stmt != null) {
                            try {
                                stmt.close();
                            } catch (SQLException sqlEx) {
                            } // ignore
                            stmt = null;
                        }
                    }

                    // Each available helper downloads one new video per 'round',
                    // up to numVideos
                    int numVideos = videosNeedingHelp.size();
                    for (Address dest : helperNodes.keySet()) {
                        if (numVideos == 0) {
                            break;
                        }
                        Helper h = helperNodes.get(dest);
                        if (h.isAvailable()) {
                            trigger(new BootstrapMsg.HelperDownload(ToVodAddr.systemAddr(self), 
                               ToVodAddr.systemAddr(dest), 
                            videosNeedingHelp.get(numVideos-1)), network);
                            numVideos--;
                        }
                    }
                    
                }
            };

    Handler<CleanupStaleTimeout> handleCleanupStaleTimeout
            = new Handler<CleanupStaleTimeout>() {
                @Override
                public void handle(CleanupStaleTimeout event) {

                    // select all primary keys of all entries that have not sent a heartbeat
                    // for 3 times the HEARTBEAT period
                    Statement stmt = null;
                    try {
                        if (!validateDbConnection()) {
                            logger.warn("Couldn't get DB connection when cleaning up");
                            return;
                        }
                        stmt = con.createStatement();

                        // if you don't know ahead of time that
                        // the query will be a SELECT...
                        StringBuilder deleteQuery = new StringBuilder();
                        // This query should use the last_ping_idx.
                        deleteQuery.append("DELETE FROM nodes WHERE NOW()- last_ping > ").append((BaseCommandLineConfig.BOOTSTRAP_HEARTBEAT_MS / 1000) * 10);
                        int numRows = stmt.executeUpdate(deleteQuery.toString());
                        logger.debug("Cleaner removed " + numRows + " rows."
                        );

                    } catch (SQLException ex) {
                        // handle any errors
                        logger.warn("SQLException: " + ex.getMessage());
                        logger.warn("SQLState: " + ex.getSQLState());
                        logger.warn("VendorError: " + ex.getErrorCode());
                    } finally {
                        if (stmt != null) {
                            try {
                                stmt.close();
                            } catch (SQLException sqlEx) {
                            } // ignore
                            stmt = null;
                        }
                    }

                }
            };

    // TODO - this is broken. Should work with overlayId, not overlayname
    private Set<VodDescriptor> getAllNodes(String overlay) {

        Set<VodDescriptor> entries = new HashSet<VodDescriptor>();
        if (!validateDbConnection()) {
            logger.warn("No connection to DB available");
            return entries;
        }

        Statement stmt = null;
        ResultSet rs = null;
        boolean hadResults = false;
        try {
            stmt = con.createStatement();

            // This query should use the open_stable_idx.
            // It selects 15 random open nodes from gvod that have pinged the bootstrap server in the last 60 seconds
            StringBuilder selectQuery = new StringBuilder().append("SELECT id, "
                    + "INET_NTOA(ip), port, nat_type, utility, mtu, asn"
                    //                    + "(NOW()-last_ping), "
                    + "(NOW()-joined) "
                    + "FROM nodes WHERE overlay_name like '").
                    append(overlay).
                    append("'");

            hadResults = stmt.execute(selectQuery.toString());

            while (hadResults) {
                rs = stmt.getResultSet();

                while (rs.next()) {
                    try {
                        int nodeId = rs.getInt(1);
                        String nodeIpAsString = rs.getString(2);
                        InetAddress nodeIp = InetAddress.getByName(nodeIpAsString);
                        int nodePort = rs.getInt(3);
                        short natPolicy = rs.getShort(4);
                        int nodeUtility = rs.getInt(5);
                        short mtu = rs.getShort(6);
                        int asn = rs.getInt(7);
                        int age = rs.getInt(8);
                        age = age / 1000;
                        if (age > 65535) {
                            age = 65535;
                        }

                        Address nodeAddr = new Address(nodeIp, nodePort, nodeId);
                        VodDescriptor entry
                                = new VodDescriptor(
                                        new VodAddress(nodeAddr, VodConfig.SYSTEM_OVERLAY_ID, natPolicy, null),
                                        new UtilityVod(nodeUtility),
                                        age,
                                        mtu
                                );
                        entries.add(entry);
                    } catch (UnknownHostException ex) {
                        java.util.logging.Logger.getLogger(BootstrapServerMysql.class.getName()).log(
                                Level.SEVERE, null, ex);
                    }
                }
                // ADD THESE TO ENTRIES

                hadResults = stmt.getMoreResults();
            }

        } catch (SQLException ex) {
            // handle any errors
            logger.warn("SQLException: " + ex.getMessage());
            logger.warn("SQLState: " + ex.getSQLState());
            logger.warn("VendorError: " + ex.getErrorCode());
        } finally {
            // it is a good idea to release
            // resources in a finally{} block
            // in reverse-order of their creation
            // if they are no-longer needed

            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException sqlEx) {
                } // ignore

                rs = null;
            }

            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException sqlEx) {
                } // ignore

                stmt = null;
            }

        }

        return entries;
    }
    Handler<WebRequest> handleWebRequest = new Handler<WebRequest>() {
        public void handle(WebRequest event) {
            logger.debug("Handling Webpage Request");
            WebResponse response = new WebResponse(searchPageHtml(event
                    .getTarget()), event, 1, 1);
            trigger(response, web);
        }
    };

    private String searchPageHtml(String operation) {
        StringBuilder sb = new StringBuilder(
                "<!DOCTYPE html PUBLIC \"-//W3C");
        sb
                .append("//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR");
        sb
                .append("/xhtml1/DTD/xhtml1-transitional.dtd\"><html xmlns=\"http:");
        sb
                .append("//www.w3.org/1999/xhtml\"><head><meta http-equiv=\"Conten");
        sb.append("t-Type\" content=\"text/html; charset=utf-8\" />");
        sb.append("<title>Clommunity VoD Bootstrap Server</title>");
        sb.append("<style type=\"text/css\"><!--.style2 {font-family: ");
        sb
                .append("Arial, Helvetica, sans-serif; color: #0099FF;}--></style>");
        sb.append("</head><body><h2 align=\"center\" class=\"style2\">");
        sb.append("Clommunity Video-on-Demand</h2><br>");
        sb.append("<para align=\"center\"><a href=\"http://snurran.sics.se/gvod/gvod.xpi\">"
                + "Click here to download and install our firefox addon.</a><br/>");
        sb.append("<br/>Installation requirements: jdk 1.6+, firefox.<br/>"
                + "Tested on ubuntu and windows 7.<para/>");
        sb.append(searchResults());
        sb.append("</body></html>");
        return sb.toString();
    }

    private String searchResults() {
        StringBuilder results = new StringBuilder();
        results.append("<hr style=\"border: 1px dashed #666;\" /><br/>");
        results.append("<table>");
        if (!validateDbConnection()) {
            logger.warn("No connection to DB available for search results.");
            results.append("<tr>DB connection not working.</tr>");
            results.append("</table>");
            results.append("<hr style=\"border: 1px dashed #666;\" /><br/>");
            return results.toString();
        }
        Statement stmt = null;
        ResultSet rs = null;
        boolean hadResults = false;
        try {
            stmt = con.createStatement();
            StringBuilder selectQuery = new StringBuilder().append("SELECT *"
                    + "FROM overlay_details ORDER BY date_added");
            hadResults = stmt.execute(selectQuery.toString());
            while (hadResults) {
                rs = stmt.getResultSet();
                while (rs.next()) {
                    String name = rs.getString(2);
                    String desc = rs.getString(3);
                    String imgUrl = rs.getString(5);
//                    results.append("<tr>");
                    // player=0/
//                    results.append("</tr>");
                    results.append("<tr style=\"border-bottom: 1px dashed #000;\">");
                    if (imgUrl != null) {
                        results.append("<td><img height=\"50\" width=\"50\" src=\"")
                                .append(imgUrl).append("\"/></td>");
                    }
                    results.append("<td><a href=\"gvod://http:/").append(self.getIp())
                            .append("/gvod/").append(name).append(".data\">")
                            .append(name).append("</a></td>");
                    if (desc != null) {
                        results.append("<td>").append(desc).append("</td>");
                    }
                    results.append("</tr>");
                }
                hadResults = stmt.getMoreResults();
            }

        } catch (SQLException ex) {
            // handle any errors
            logger.warn("SQLException: " + ex.getMessage());
            logger.warn("SQLState: " + ex.getSQLState());
            logger.warn("VendorError: " + ex.getErrorCode());
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException sqlEx) {
                }
                rs = null;
            }

            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException sqlEx) {
                } // ignore

                stmt = null;
            }

        }
        results.append("</table>");
        results.append("<hr style=\"border: 1px dashed #666;\" /><br/>");
        return results.toString();
    }
    Handler<NatReportMsg> handleNatReportMsg = new Handler<NatReportMsg>() {
        @Override
        public void handle(NatReportMsg msg) {
            logger.debug("Received NatReport from " + msg.getVodSource());
            for (NatReport r : msg.getNatReports()) {
                insertNatReport(msg.getVodSource(), r.getTarget(), r.isSuccess(),
                        (int) r.getTimeTaken(), r.getMsg());
            }
        }
    };

    private boolean validateDbConnection() {
        try {
            if (con.isClosed()) {
                con = DriverManager.getConnection(jdbcUrl, user, passwd);
                logger.info("Re-connected to MySQL server using TCP/IP.");
            }
        } catch (SQLException ex) {
            // hackish way to mock the DB
//            if (!delegator.isUnitTest()) {
            java.util.logging.Logger.getLogger(BootstrapServerMysql.class.getName()).log(Level.SEVERE, null, "SQLException: " + ex.getMessage());
            java.util.logging.Logger.getLogger(BootstrapServerMysql.class.getName()).log(Level.SEVERE, null, "SQLState: " + ex.getSQLState());
            java.util.logging.Logger.getLogger(BootstrapServerMysql.class.getName()).log(Level.SEVERE, null, "VendorError: " + ex.getErrorCode());
//            }
            return false;
        }
        return true;

    }

    private void insertNatReport(VodAddress src, VodAddress target, boolean success,
            int timeTaken, String msg) {

        if (!validateDbConnection()) {
            logger.warn("No connection to DB available, not saving NatReport");
            return;
        }
        if (msg == null) {
            msg = "";
        }

        StringBuilder natReportStr = new StringBuilder();
        int success_int = success == true ? 1 : 0;
        int fail_int = success == true ? 0 : 1;

        natReportStr.append("INSERT INTO nat_reports"
                + "(src_addr, src_nat, target_addr, target_nat, msg, success_count, "
                + "fail_count, time_taken, last_modified) VALUES ('")
                .append(src.getId()).append("@").append(src.getIp())
                .append(":").append(src.getPort()).append("', '")
                .append(src.getNatAsString()).append("', '")
                .append(target.getId()).append("@").append(target.getIp())
                .append(":").append(target.getPort()).append("', '")
                .append(target.getNatAsString()).append("', '")
                .append(msg).append("', ")
                .append(success_int).append(", ")
                .append(fail_int).append(", ")
                .append(timeTaken).append(", ")
                .append("NOW()) ON DUPLICATE KEY UPDATE ");
        if (success) {
            natReportStr.append("success_count=success_count+1, ");
        } else {
            natReportStr.append("fail_count=fail_count+1, ");
        }
        natReportStr.append("last_modified=NOW()");
        logger.debug(natReportStr.toString());
        Statement stmt = null;
        try {
            stmt = con.createStatement();

            stmt.addBatch(natReportStr.toString());
            int[] results = stmt.executeBatch();
            logger.debug("Number of rows inserted/updated for nodes {}",
                    results[0]);
        } catch (SQLException ex) {
            // handle any errors
            logger.warn("SQLException: " + ex.getMessage());
            logger.warn("SQLState: " + ex.getSQLState());
            logger.warn("VendorError: " + ex.getErrorCode());
        } finally {
            // it is a good idea to release
            // resources in a finally{} block
            // in reverse-order of their creation
            // if they are no-longer needed

            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException sqlEx) {
                } // ignore
                stmt = null;
            }
        }
    }
    public Handler<Stop> handleStop = new Handler<Stop>() {
        @Override
        public void handle(Stop event) {
            trigger(new CancelTimeout(cleanupId), timer);
            try {
                if (con != null) {
                    con.close();
                }
            } catch (SQLException e) {
            }
        }
    };
}
