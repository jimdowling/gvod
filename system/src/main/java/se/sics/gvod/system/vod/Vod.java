/**
 * This file is part of the Kompics P2P Framework.
 *
 * Copyright (C) 2009 Swedish Institute of Computer Science (SICS) Copyright (C)
 * 2009 Royal Institute of Technology (KTH)
 *
 * Kompics is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package se.sics.gvod.system.vod;

import se.sics.gvod.common.msgs.DataMsg.Ack;
import se.sics.gvod.common.msgs.DataMsg.AckTimeout;
import se.sics.gvod.common.msgs.DataMsg.Saturated;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.kompics.Handler;
import se.sics.gvod.net.VodAddress;
import se.sics.kompics.Negative;
import se.sics.gvod.common.VodDescriptor;
import se.sics.gvod.web.port.Status;
import se.sics.gvod.system.storage.StorageMemMapWholeFile;
import se.sics.gvod.common.msgs.DataMsg;
import se.sics.gvod.system.storage.Read;
import se.sics.gvod.web.port.DownloadCompletedSim;
import se.sics.gvod.bootstrap.port.Rebootstrap;
import se.sics.gvod.common.Block;
import se.sics.gvod.common.msgs.DataOfferMsg;
import se.sics.gvod.common.msgs.LeaveMsg;
import se.sics.gvod.common.msgs.UploadingRateMsg;
import se.sics.gvod.system.storage.MetaInfoExec;
import se.sics.gvod.system.storage.MetaInfoSimu;
import se.sics.gvod.system.storage.Storage;
import se.sics.gvod.system.storage.StorageSimu;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import org.apache.commons.math.stat.descriptive.SummaryStatistics;
import org.javatuples.Pair;
import se.sics.gvod.common.BitField;
import se.sics.gvod.common.CommunicationWindow;
import se.sics.gvod.config.VodConfig;
import se.sics.gvod.common.msgs.ConnectMsg;
import se.sics.gvod.common.msgs.DisconnectMsg;
import se.sics.gvod.system.util.ActiveTorrents;
import se.sics.gvod.system.util.BaseHandler;
import se.sics.gvod.system.util.FileUtils;
import se.sics.gvod.system.util.FlvHandler;
import se.sics.gvod.system.util.JwHttpServer;
import se.sics.gvod.system.util.Mp4Handler;
import se.sics.gvod.bootstrap.port.RebootstrapResponse;
import se.sics.gvod.common.*;
import se.sics.gvod.croupier.PeerSamplePort;
import se.sics.gvod.nat.common.MsgRetryComponent;
import se.sics.gvod.system.peer.events.InitiateMembershipSearch;
import se.sics.gvod.system.peer.sets.InitiateDataOffer;
import se.sics.gvod.croupier.events.CroupierSample;
import se.sics.gvod.nat.traversal.NatTraverserPort;
import se.sics.gvod.nat.traversal.events.DisconnectNeighbour;
import se.sics.gvod.net.Nat;
import se.sics.gvod.net.msgs.ScheduleRetryTimeout;
import se.sics.gvod.system.peer.VodPeerPort;
import se.sics.gvod.system.peer.events.ChangeUtility;
import se.sics.gvod.system.peer.events.JumpBackward;
import se.sics.gvod.system.peer.events.JumpForward;
import se.sics.gvod.system.peer.events.Pause;
import se.sics.gvod.system.peer.events.Play;
import se.sics.gvod.system.peer.events.Quit;
import se.sics.gvod.system.peer.events.QuitCompleted;
import se.sics.gvod.system.peer.events.ReadingCompleted;
import se.sics.gvod.system.peer.events.SlowBackground;
import se.sics.gvod.system.peer.events.SpeedBackground;
import se.sics.gvod.system.storage.StorageFcByteBuf;
import se.sics.gvod.system.util.ActiveTorrentsException;
import se.sics.gvod.system.util.Sender;
import se.sics.gvod.timer.CancelPeriodicTimeout;
import se.sics.gvod.timer.CancelTimeout;
import se.sics.gvod.timer.SchedulePeriodicTimeout;
import se.sics.gvod.timer.ScheduleTimeout;
import se.sics.gvod.timer.Timeout;
import se.sics.gvod.timer.TimeoutId;
import se.sics.kompics.Positive;
import se.sics.kompics.Stop;

/**
 * The <code>GVod</code> class.
 *
 * @author Gautier Berthou
 * @author Jim Dowling
 */
public final class Vod extends MsgRetryComponent {

    public static final boolean MEM_MAP_VOD_FILES = true;
    private Logger logger = LoggerFactory.getLogger(Vod.class);

    private VodConfiguration config;

    private BaseHandler handler;
    private Vod comp;

    private HistoryManager historyMngr;
    private ConnectionManager connectionMngr;
    private DownloadManager downloadMngr;
    private PlayerManager playerMngr;
    private Self self;

    private String compName;
    private int time = 1;
    private boolean simulation;

    private int ackTimeout;
    private int mtu;

    private Sender sender; //is this used for sending data?

    private int bufferingNum = 0; //What is this one used for...it is used only in the read
    private int waiting = 0; //What is this? It is never changed
    private int misConnect = 0; //What is this? It is never changed
    private long bW; //What is this?

    private Map<VodAddress, Long> downloadedFrom; //is this leftover code? doesn't seem to be code that works 

    private List<TimeoutId> outstandingHashRequest;
    private Map<TimeoutId, Map<Integer, ByteBuffer>> awaitingHashResponses;
    private Map<Integer, Long> ongoingConnectRequests;
    private TimeoutId initiateShuffleTimeoutId, dataOfferPeriodTimeoutId, readTimerId;

    private Map<VodAddress, Long> suspected; //do I actually do anything with this? or do I just put the peers in here?
    private Map<Integer, List<VodAddress>> forwarded;

    private boolean rebootstrap = false;
    /**
     * *****Alex*****
     */
    Positive<NatTraverserPort> natTraverserPort = positive(NatTraverserPort.class);
    Negative<Status> status = negative(Status.class);
    Negative<VodPort> vod = negative(VodPort.class);
    Positive<PeerSamplePort> croupier = positive(PeerSamplePort.class);
    Negative<VodPeerPort> peer = negative(VodPeerPort.class);

    private Map<Integer, Long> srtts = new HashMap<Integer, Long>();
    private Map<Integer, Double> rtos = new HashMap<Integer, Double>();
    private Map<TimeoutId, Long> rttMsgs = new HashMap<TimeoutId, Long>();
    //  α is a constant between 0 and 1 that control how rapidly the
    // smoothed round-trip time (SRTT) adapts to changes.
    // SRTT(i+1) = α * SRTT(i) + ( 1-α ) * S(i)
    // a nonlinear filter where α is smaller when SRTT(i) < S(i), allowing the
    // SRTT to adapt more swiftly to sudden increase in network delay.
    private double tcpAlpha = 0.75;
    // TODO: vary β based on the observed variance in measured round-trip times.
    // RTO(i) = β * SRTT(i)
    private double tcpBeta = 2.0d;

    class DisconnectTimeout extends Timeout {

        private VodAddress dest;

        public DisconnectTimeout(ScheduleTimeout st, VodAddress dest) {
            super(st);
            this.dest = dest;
        }

        public VodAddress getDest() {
            return dest;
        }
    }

    /**
     * constructor
     */
    public Vod() {
        this(null);
    }

    public Vod(RetryComponentDelegator delegator) {
        super(delegator);
        this.delegator.doAutoSubscribe();

        downloadedFrom = new HashMap<VodAddress, Long>();
        suspected = new HashMap<VodAddress, Long>();
        forwarded = new HashMap<Integer, List<VodAddress>>();
        outstandingHashRequest = new ArrayList<TimeoutId>();
        awaitingHashResponses = new HashMap<TimeoutId, Map<Integer, ByteBuffer>>();
        ongoingConnectRequests = new HashMap<Integer, Long>();
        comp = this;
    }

    Handler<VodInit> handleInit = new Handler<VodInit>() {
        @Override
        public void handle(VodInit init) {
            logger.debug("{} - init", compName);
            self = init.getSelf();
            self.updateUtility(new UtilityVod(init.getUtility()));
            config = init.getConfig();
            simulation = init.isSimulation();
            compName = "(" + self.getId() + "," + self.getOverlayId() + ") ";

            bW = init.getDownloadBw();

            String videoName = config.getName();
            long videoLength = config.getLength();
            boolean seeder = init.isSeed();
            String torrentFileAddress = init.getTorrentFileAddress();

            if (!simulation) {

                // start the handler that receive request from the video player
                boolean flag = true;
                int i = 0;
                while (flag && i < 10) {
                    try {
                        String httpPath = "/" + videoName + "/";
                        String postfix = FileUtils.getPostFix(videoName);
                        handler = null;
                        if (postfix.compareToIgnoreCase(".flv") == 0) {
                            handler = new FlvHandler(init.getMain(), comp);
                        } else if (postfix.compareToIgnoreCase(".mp4") == 0) {
                            handler = new Mp4Handler(init.getMain(), comp);
                        } else {
                            throw new IllegalArgumentException("Invalid file type: " + postfix);
                        }
                        logger.info(compName + postfix + " handler at " + "http://"
                                + VodConfig.LOCALHOST + ":"
                                + VodConfig.getMediaPort()
                                + httpPath);
                        JwHttpServer.startOrUpdate(
                                new InetSocketAddress(
                                        VodConfig.getMediaPort()),
                                httpPath,
                                handler);
                        flag = false;
                    } catch (Exception e) {
                        logger.warn(compName + "Problem binding to Media port for .flv/.mp4 handler");
                        logger.error(compName + e.getMessage());
                    }
                    i++;
                }
            }

            long readingPeriod = config.getReadingPeriod();
            ackTimeout = config.getAckTimeout();
            mtu = config.getMtu();
            logger.info(compName + "JOIN. SELF == {}", self.getAddress());

            connectionMngr = new ConnectionManager(config, new ConnectionDelegator(), self, compName);
            Storage storage;
            try {
                storage = buildStorage(seeder, simulation, torrentFileAddress, videoName, videoLength);
            } catch (IOException ex) {
                //TODO Alex what to do
                throw new RuntimeException(ex);
            }
            if (seeder) {
                self.updateUtility(new UtilityVod(VodConfig.SEEDER_UTILITY_VALUE));
                UtilityVod myUtility = (UtilityVod) self.getUtility();
                connectionMngr.getBitTorrentStats().changeUtility(myUtility.getChunk(), myUtility,
                        storage.getBitField().numberPieces(), storage.getBitField());
            } else {
                UtilityVod myUtility = (UtilityVod) self.getUtility();
                myUtility.setChunk(storage.getBitField().setNextUncompletedChunk(myUtility.getChunk()));
                connectionMngr.getBitTorrentStats().changeUtility(myUtility.getChunk() - myUtility.getOffset(),
                        myUtility, storage.getBitField().numberPieces(), storage.getBitField());
            }
            historyMngr = new HistoryManager();
            historyMngr.registerDownload(0, storage.needed());
            historyMngr.registerUtility(0, 0);
            playerMngr = new PlayerManager(init, compName);
            downloadMngr = new DownloadManager(init, self, new DownloadDelegator(), storage, connectionMngr, historyMngr, playerMngr, compName);

            SchedulePeriodicTimeout spt = new SchedulePeriodicTimeout(config.getShufflePeriod(),
                    config.getShufflePeriod());
            spt.setTimeoutEvent(new InitiateMembershipSearch(spt, self.getOverlayId()));
            delegator.doTrigger(spt, timer);

            spt = new SchedulePeriodicTimeout(config.getDataOfferPeriod(),
                    config.getDataOfferPeriod());
            spt.setTimeoutEvent(new InitiateDataOffer(spt, self.getOverlayId()));
            delegator.doTrigger(spt, timer);
            dataOfferPeriodTimeoutId = spt.getTimeoutEvent().getTimeoutId();

            if (!seeder && simulation) {
                spt = new SchedulePeriodicTimeout(readingPeriod, readingPeriod);
                spt.setTimeoutEvent(new Read(spt));
                delegator.doTrigger(spt, timer);
                readTimerId = spt.getTimeoutEvent().getTimeoutId();
                if (init.isPlay()) {
                    play();
                }
            }
            // update % in Swing GUI
            if (seeder) {
                try {
                    ActiveTorrents.makeSeeder(torrentFileAddress);
                } catch (ActiveTorrentsException ex) {
                    //TODO Alex what to do
                    throw new RuntimeException(ex);
                }
            } else {
                ActiveTorrents.updatePercentage(videoName, storage.percent());
            }
        }
    };

    private Storage buildStorage(boolean seeder, boolean simulation, String torrentFileAddress, String videoName, long videoLength) throws IOException {
        File metaInfoFile = new File(torrentFileAddress);
        Storage storage = null;

        if (seeder) {
            try {
                if (simulation) {
                    logger.debug("{} new storage", compName);
                    storage = new StorageSimu(videoName, videoLength);
                    storage.create(null);
                    FileOutputStream fos = new FileOutputStream(torrentFileAddress);
                    logger.debug("{} write .data", compName);
                    fos.write(downloadMngr.storage.getMetaInfo().getData());
                    fos.close();
                } else {
                    FileInputStream in = new FileInputStream(metaInfoFile);
                    MetaInfoExec metaInfo = new MetaInfoExec(in, torrentFileAddress);
                    if (MEM_MAP_VOD_FILES) {
                        storage = new StorageMemMapWholeFile(metaInfo, metaInfoFile.getParent(), true);
                    } else {
                        storage = new StorageFcByteBuf(metaInfo, metaInfoFile.getParent(), true);
                    }
                    storage.check(false);
                    if (storage.needed() != 0) {
                        // TODO JIM - fix this behaviour, shouldn't quit.
                        //not truly a seed => quit
                        logger.warn("{} The movie file does not correspond to the data file the seed will quit for:",
                                new Object[]{compName, metaInfo.getName()});
//                            delegator.doTrigger(new QuitCompleted(self.getId()), vod);
//                            seeder = false;
                    }
                }
                logger.info("{} - {} ", compName, storage.getBitField().getHumanReadable2());
            } catch (IOException e) {
                logger.error(compName + "problem while trying to initialize the seed: " + e.getMessage(), e);
                logger.error(compName + "Metafile: " + torrentFileAddress);
                throw new IllegalStateException("Could not create video file. Error: " + e.getMessage());
            }
        } else { // not a seeder
            try {
                FileInputStream in = new FileInputStream(metaInfoFile);
                if (simulation) {
                    MetaInfoSimu metaInfo = new MetaInfoSimu(in);
                    storage = new StorageSimu(metaInfo);
                } else {
                    MetaInfoExec metaInfo = new MetaInfoExec(in, torrentFileAddress);
                    if (Vod.MEM_MAP_VOD_FILES) {
                        storage = new StorageMemMapWholeFile(metaInfo,
                                metaInfoFile.getParent(), false);
                    } else {
                        storage = new StorageFcByteBuf(metaInfo,
                                metaInfoFile.getParent(), false);
                    }
                }
                logger.info("{} check storage: {}", compName, storage.getMetaInfo().getName());
                storage.check(true);
                logger.info("{} - {}", compName, storage.getBitField().getHumanReadable2());
            } catch (IOException e) {
                logger.error("{} problem while initializing the torrent file in GVod: {} ", compName, e);
                //TODO: Does it need to send a message to the upper layer?
//                    delegator.doTrigger(new QuitCompleted(self.getId(), metaInfoAddress), vod);
                throw new IllegalStateException("Could not create video file. Error: " + e.getMessage());
            }
        }

        return storage;
    }

    Handler<InitiateMembershipSearch> handleInitiateMembershipSearch = new Handler<InitiateMembershipSearch>() {
        @Override
        public void handle(InitiateMembershipSearch event) {
            logger.trace(compName + "initiateshuffle");
            time++;

            historyMngr.registerUtility(time, ((UtilityVod) self.getUtility()).getPiece());
            historyMngr.registerDownload(time, downloadMngr.storage.needed());
            connectionMngr.incrementAge();
            connectionMngr.chokeUnchoke(time, downloadMngr.seeder);
            downloadMngr.download(time);

            connectionMngr.updateSetsAndConnect(downloadMngr.seeder);

            SummaryStatistics windowStats = new SummaryStatistics();
            for (VodDescriptor node : connectionMngr.bitTorrentSet.getAll()) {
                windowStats.addValue(node.getWindow().getSize());
            }
            for (VodDescriptor node : connectionMngr.lowerSet.getAll()) {
                windowStats.addValue(node.getWindow().getSize());
            }
            if (windowStats.getMean() / VodConfig.LB_MAX_SEGMENT_SIZE < downloadMngr.pipeSize) {
                delegator.doTrigger(new SlowBackground(self.getOverlayId()), vod);
            } else if (windowStats.getMean() / VodConfig.LB_MAX_SEGMENT_SIZE > downloadMngr.pipeSize * 2) {
                delegator.doTrigger(new SpeedBackground(self.getOverlayId()), vod);
            }
        }
    };

    Handler<CroupierSample> handleCroupierSample = new Handler<CroupierSample>() {
        @Override
        public void handle(CroupierSample event) {
            List<VodDescriptor> descriptors = event.getNodes();

            logger.debug("{} Croupier sample size {}", compName, descriptors.size());
            connectionMngr.newCroupierSet(descriptors);

            if (descriptors.size() > 0) {
                StringBuilder sb = new StringBuilder();
                for (VodDescriptor descriptor : event.getNodes()) {
                    if (!self.getAddress().equals(descriptor.getVodAddress())) {
                        sb.append(descriptor.getId()).append(", ");
                    }
                }

                logger.debug("{} Croupier just sent us descriptors: {}", compName, sb.toString());
            }
        }
    };

    /**
     * handle connect request containing the useful information about the node
     * wanting to connect : utility, id, children (if in below set). If the node
     * is already connected, return success.
     */
    Handler<ConnectMsg.Request> handleConnectRequest = new Handler<ConnectMsg.Request>() {
        @Override
        public void handle(ConnectMsg.Request req) {
            logger.debug("{} ConnectRequest from {}", compName, req.getVodSource().getId());
            /*
             * if the node receive a request before it received a join message
             * can happen if a node disconnects and reconnects with the same id
             */
            if (self == null) {
                return;
            }

            msgReceived(req.getVodSource());

            //TODO Alex is the behaviour below the wanted one?
            /*
             * if the node asking to connect was choked we ignore the request
             */
            if (connectionMngr.isChoked(req.getVodSource())) {
                logger.info("{} - chocked {}", compName, req.getVodSource().getId());
                return;
            }

            UtilityVod myUtility = (UtilityVod) self.getUtility();
            ConnectMsg.Response response;
            Pair<ConnectMsg.ResponseType, Boolean> responseType = connectionMngr.processConnectReq(req, downloadMngr.pipeSize, downloadMngr.seeder);
            switch (responseType.getValue0()) {
                case OK:
                    if (myUtility.getChunk() >= 0) {
                        response = new ConnectMsg.Response(self.getAddress(), req.getVodSource(), req.getTimeoutId(),
                                ConnectMsg.ResponseType.OK, myUtility, downloadMngr.storage.getBitField().getChunkfield(),
                                downloadMngr.storage.getBitField().getAvailablePieces(myUtility), responseType.getValue1(), mtu);
                    } else {
                        response = new ConnectMsg.Response(self.getAddress(), req.getVodSource(), req.getTimeoutId(),
                                ConnectMsg.ResponseType.OK, myUtility, null, null, responseType.getValue1(), mtu);
                    }
                    break;
                case FULL:
                    response = new ConnectMsg.Response(self.getAddress(), req.getVodSource(), req.getTimeoutId(),
                            ConnectMsg.ResponseType.FULL, myUtility, null, null, responseType.getValue1(), mtu);
                    break;
                case BAD_UTILITY:
                    response = new ConnectMsg.Response(self.getAddress(), req.getVodSource(), req.getTimeoutId(),
                            ConnectMsg.ResponseType.BAD_UTILITY, myUtility, null, null, responseType.getValue1(), mtu);
                    logger.warn("{} send {} response to {} my utility ({},{}), its utility ({},{})",
                            new Object[]{compName, ConnectMsg.ResponseType.BAD_UTILITY, req.getVodSource().getId(), myUtility.getPiece(),
                                myUtility.getChunk(), req.getUtility().getPiece(), req.getUtility().getChunk()});
                    break;
                default:
                    return;
            }
            logger.debug("{} send {} response to {}", new Object[]{compName, responseType, req.getVodSource().getId()});
            delegator.doTrigger(response, network);
        }
    };

    Handler<ConnectMsg.Response> handleConnectResponse = new Handler<ConnectMsg.Response>() {
        @Override
        public void handle(ConnectMsg.Response resp) {
            UtilityVod myUtility = (UtilityVod) self.getUtility();
            logger.debug("{} ConnectResponse from {}", compName, resp.getVodSource().getId());
            logger.trace("{} ConnectResponse {} - myUtility ({},{}) - its utility ({},{})",
                    new Object[]{compName, resp.getResponse(), myUtility.getChunk(), myUtility.getPiece(),
                        resp.getUtility().getChunk(), resp.getUtility().getPiece()});
            msgReceived(resp.getVodSource());
            ongoingConnectRequests.remove(resp.getVodSource().getId());

            if (delegator.doCancelRetry(resp.getTimeoutId())) {
                logger.trace("{} Cancelled connectRequest timeout : {}", compName, resp.getTimeoutId());

                if (resp.getVodSource().equals(self)) {
                    logger.warn("{} REMOVED SELF FROM CONNECTED NEIGHBOURS", compName);
                    return;
                }

                connectionMngr.processConnectResp(resp, downloadMngr.pipeSize);
                downloadMngr.startDownload(time);
            }
        }
    };

    Handler<ConnectMsg.RequestTimeout> handleConnectTimeout = new Handler<ConnectMsg.RequestTimeout>() {
        @Override
        public void handle(ConnectMsg.RequestTimeout event) {
            ongoingConnectRequests.remove(event.getVodAddress().getId());
            logger.trace("{} connect timeout with {}", compName, event.getVodAddress().getId());
        }
    };

    Handler<DisconnectMsg.Request> handleDisconnectRequest = new Handler<DisconnectMsg.Request>() {
        @Override
        public void handle(DisconnectMsg.Request req) {
            logger.debug("{} DisconnectMsg.Request from {}.", compName, req.getSource().getId());
            if (self == null) {
                return;
            }
            msgReceived(req.getVodSource());

            connectionMngr.processDisconnectReq(req);

            trigger(new DisconnectNeighbour(req.getVodSource().getId()), natTraverserPort);
        }
    };

    Handler<DisconnectMsg.Response> handleDisconnectResponse = new Handler<DisconnectMsg.Response>() {
        @Override
        public void handle(DisconnectMsg.Response resp) {

            logger.debug("{} handle disconnectResponse from {}", compName, resp.getVodSource().getId());
            if (self == null) {
                return;
            }
            msgReceived(resp.getVodSource());

            if (delegator.doCancelRetry(resp.getTimeoutId())) {
                logger.trace("{} Cancelled connectRequest timeout : {}", compName, resp.getTimeoutId());
                connectionMngr.processDisconnectResp(resp);
            }
        }
    };

    Handler<DisconnectTimeout> handleDisconnectTimeout = new Handler<DisconnectTimeout>() {
        @Override
        public void handle(DisconnectTimeout event) {
            triggerDisconnectRequest(event.getDest(), true);
        }
    };

    /**
     * handle timeout when a node is to slow to answer to a disconnect request
     * finish to disconnect
     */
    Handler<DisconnectMsg.RequestTimeout> handleDisconnectReqTimeout = new Handler<DisconnectMsg.RequestTimeout>() {
        @Override
        public void handle(DisconnectMsg.RequestTimeout timeout) {
            VodAddress peer = timeout.getPeer();
            logger.debug("{} DisconnectReq Timeout peer: {}", compName, peer.getId());

            if (delegator.doCancelRetry(timeout.getTimeoutId())) {
                if (suspected.containsKey(peer)) {
                    if (suspected.get(peer) + VodConfig.SUSPECTED_DEAD_TIMEOUT_MS > System.currentTimeMillis()) {
                        logger.warn("{} Removing peer {} due to response timed out.", compName, peer.getId());
                        connectionMngr.process(timeout);
                        if (self.getNat().getFilteringPolicy() == Nat.FilteringPolicy.PORT_DEPENDENT) {
                            // send msg to NatTraverser to delete the port
                            //TODO Alex -> Jim
                        }
                    }
                } else {
                    suspected.put(timeout.getPeer(), System.currentTimeMillis());
                }
            }
        }
    };

    private void rebootstrap() {
        if (rebootstrap) {
            return;
        }
        logger.info("{} rebootstrap", compName);
        int videoId = ActiveTorrents.calculateVideoId(downloadMngr.videoName);
        UtilityVod utility = (UtilityVod) self.getUtility();
        delegator.doTrigger(new Rebootstrap(self.getId(), videoId, utility), vod);
        rebootstrap = true;
    }

    /**
     * Handle a rebootstrap response to rejoin a GVod network using a set of
     * introducer nodes provided in the Join event.
     */
    Handler<RebootstrapResponse> handleRebootstrapResponse = new Handler<RebootstrapResponse>() {
        @Override
        public void handle(RebootstrapResponse resp) {
            List<VodDescriptor> insiders = resp.getVodInsiders();
            rebootstrap = false;
            logger.debug("{} handle rebootstrap response {}", compName, insiders.size());
            if (!downloadMngr.seeder) {
                connectionMngr.processRebootstrap(resp);
            }
        }
    };

    /**
     * handle a quit request, send a leave message to the neighbor to inform
     * them that the node quit the network
     */
    Handler<Quit> handleQuit = new Handler<Quit>() {
        @Override
        public void handle(Quit event) {
            logger.info("{} quit", compName);
            connectionMngr.process(event);
            delegator.doTrigger(new QuitCompleted(event.getOverlayId(), downloadMngr.torrentFileAddress), vod);
        }
    };
    /**
     * handle a leave message, remove the living node from the different views
     */
    Handler<LeaveMsg> handleLeave = new Handler<LeaveMsg>() {
        @Override
        public void handle(LeaveMsg event) {
            logger.debug("{} remove {} is leaving", compName, event.getVodSource().getId());
            connectionMngr.process(event);
        }
    };

    /**
     * handle a message from gvodPeer asking to change the utility
     */
    Handler<ChangeUtility> handleChangeUtility = new Handler<ChangeUtility>() {
        @Override
        public void handle(ChangeUtility event) {
            logger.info("{} changeUtilityPosition seekPos/readPos {}/{} ",
                    new Object[]{compName, event.getUtility(), event.getReadPos()});

            int seekPos = event.getReadPos();
            int piecePos = seekPos / (BitField.NUM_SUBPIECES_PER_PIECE * BitField.SUBPIECE_SIZE);
            int offsetWithinPiece = seekPos % (BitField.NUM_SUBPIECES_PER_PIECE * BitField.SUBPIECE_SIZE);
            int chunkPos = piecePos / BitField.NUM_PIECES_PER_CHUNK;
            playerMngr.nextPieceToSend.set(piecePos);
            if (downloadMngr.changeUtility(time, chunkPos, piecePos)) {
                Sender oldSender = sender;
                sender = new Sender(comp, event.getResponseBody(), piecePos, offsetWithinPiece);
                sender.start();
                if (oldSender != null) {
                    oldSender.interrupt();
                }
            }
        }
    };

    private void triggerConnectRequest(VodDescriptor node, boolean toUtilitySet) {
        UtilityVod utility = (UtilityVod) self.getUtility();
        if (node != null) {
            if (node.getVodAddress().getId() == self.getAddress().getId()) {
                logger.warn("{} DO NOT CONNECT TO SELF", compName);
                return;
            }
            if (ongoingConnectRequests.containsKey(node.getVodAddress().getId())) {
                return;
            }

            UtilityVod u = (UtilityVod) node.getUtility();
            logger.info("{} trigger ConnectRequest to {}", compName, node.getVodAddress().getId());

            ScheduleRetryTimeout st = new ScheduleRetryTimeout(2000, 3, 1.5d);
            ConnectMsg.Request request = new ConnectMsg.Request(
                    self.getAddress(), node.getVodAddress(), u, toUtilitySet, mtu);
            ConnectMsg.RequestTimeout retryRequest = new ConnectMsg.RequestTimeout(st, request,
                    toUtilitySet);
            delegator.doRetry(retryRequest);
            ongoingConnectRequests.put(node.getVodAddress().getId(), System.currentTimeMillis());
        }
    }

    private void triggerDisconnectRequest(VodAddress dest, boolean noDelay) {

        logger.info("{} trigger DisconnectRequest to {}", compName, dest.getId());
        if (noDelay) {
            DisconnectMsg.Request dr = new DisconnectMsg.Request(self.getAddress(), dest);
            ScheduleRetryTimeout st = new ScheduleRetryTimeout(config.getConnectionTimeout(), 1);
            DisconnectMsg.RequestTimeout drt = new DisconnectMsg.RequestTimeout(st, dr);
            delegator.doRetry(drt);
            trigger(new DisconnectNeighbour(dest.getId()), natTraverserPort);
        } else {
            ScheduleTimeout st = new ScheduleTimeout(config.getConnectionTimeout());
            st.setTimeoutEvent(new DisconnectTimeout(st, dest));
            delegator.doTrigger(st, timer);
        }
    }

    private void cancelPeriodicTimer(TimeoutId timerId) {
        if (timerId != null) {
            CancelPeriodicTimeout cPT = new CancelPeriodicTimeout(timerId);
            delegator.doTrigger(cPT, timer);
        }
    }

    private void msgReceived(VodAddress peer) {
        if (suspected.containsKey(peer)) {
            suspected.remove(peer);
        }
    }

    /**
     * handle dataoffer message containing the information on the data that the
     * sending node can share
     */
    Handler<DataOfferMsg> handleDataOffer = new Handler<DataOfferMsg>() {
        @Override
        public void handle(DataOfferMsg event) {
            UtilityVod utility = (UtilityVod) self.getUtility();
            logger.debug("{} DataOffer received from {}", compName, event.getVodSource().getId());
            if (self == null) {
                return;
            }
            msgReceived(event.getVodSource());
            connectionMngr.process(event);
        }
    };
    /**
     * handle initiatedataoffer message coming from the timer the node sends a
     * dataoffer message to all the nodes in its bittorrent set
     */
    Handler<InitiateDataOffer> handleInitiateDataOffer = new Handler<InitiateDataOffer>() {
        @Override
        public void handle(InitiateDataOffer event) {
            logger.debug("{} initiating DataOffer at {}", compName, self.getId());
            List<VodAddress> toDisconnect = connectionMngr.bitTorrentSet.cleanup(VodConfig.DATA_OFFER_PERIOD);
            connectionMngr.process(event, downloadMngr.storage.getBitField());
        }
    };

    /**
     * Handle a request to send a subpiece send the subpiece if all the
     * conditions to do it are fulfilled
     */
    Handler<DataMsg.Request> handleDataMsgRequest = new Handler<DataMsg.Request>() {
        @Override
        public void handle(DataMsg.Request req) {
            VodAddress peer = req.getVodSource();
            int piece = req.getPiece();
            int subpiece = req.getSubpieceOffset();
            logger.debug("{} Request from {}, Video {}, Data ({},{})",
                    new Object[]{compName, peer.getId(), downloadMngr.videoName, piece, subpiece});
            msgReceived(req.getVodSource());

            /*
             * if free rider just ignore the message
             */
            if (downloadMngr.freeRider) {
                logger.debug("{} Freerider: ignoring data request", compName);
                return;
            }
            byte[] subpieceVal = null;
            try {
                subpieceVal = downloadMngr.getSubpiece(subpiece);
            } catch (IOException ex) {
                logger.warn("{} impossible to access the requested Data ({},{})",
                        new Object[]{compName, piece, subpiece});
                return;
            }
            if (subpieceVal == null) {
                logger.info("{} DataExchangeResponse FORWARDED", compName);
                triggerForwardDataRequest(req);
            }
            connectionMngr.process(req, ackTimeout, subpieceVal);
        }
    };

    //re-refactor Alex here
    Handler<DataMsg.Response> handleDataMsgResponse = new Handler<DataMsg.Response>() {
        @Override
        public void handle(DataMsg.Response event) {
            logger.trace(compName + "DataMsg.Response from " + event.getVodSource().getId()
                    + " for subpiece " + event.getSubpieceOffset());

            msgReceived(event.getVodSource());

            try {
                // TODO - make this a 2-way delay estimate. event.getTime() is now the time
                // of the sender's clock - not receivers clock.
                long now = System.currentTimeMillis();
                long rtt = now - event.getTime();
                int piece = event.getPiece();
                int subpieceNb = event.getSubpieceOffset();
                TimeoutId requestId = event.getTimeoutId();
                TimeoutId ackId = event.getAckId();
                Long respTime = rttMsgs.remove(requestId);
                // if dataRequest timedout, there won't be rttMsg entry, so 
                // assume that respTime is the default DataRequest timeout
                long rtt2 = now - ((respTime != null) ? respTime
                        : (now - VodConfig.DATA_REQUEST_TIMEOUT)); // DEFAULT_LEDBAT_TARGET_DELAY*2
                int srcId = event.getSource().getId();
                updateRtts(srcId, rtt2);

                VodAddress peer = event.getVodSource();
                VodDescriptor peerInfo = connectionMngr.store.getVodDescriptorFromVodAddress(peer);
                if (delegator.doCancelRetry(event.getTimeoutId())) {
                    logger.debug(compName + "Timer cancelled for({}, {}) from {}", new Object[]{piece,
                        subpieceNb, peer.getId()});
                } else {
                    logger.debug(compName + "Duplicate? No timer cancelled for({}, {}) from {}", new Object[]{piece,
                        subpieceNb, peer.getId()});
                    return;
                }

                if (peerInfo == null) {
                    logger.warn(compName + "Could not find VodDescriptor for a piece!");
                    return;
                }


                /*
                 * if the request was sent due to a forward send the piece to
                 * the original seeker
                 */
                if (forwarded.containsKey(subpieceNb)) {
                    forwardResponse(event);
                    // don't download the piece myself?
//                    return;
                }
                /*
                 * if the piece was not requested we ignore the message
                 */
                if (!downloadMngr.partialPieces.containsKey(piece)) {
                    logger.debug(compName + "This piece wasn't requested: {}", piece);
                    return;
                }

                logger.debug(compName + "Got Piece/Subpiece({}, {}) from {}", new Object[]{piece,
                    subpieceNb, peer.getId()});

                // mark that we downloaded a block
                PieceInTransit transit = downloadMngr.partialPieces.get(piece);

                if (transit == null) {
                    logger.warn(compName + "Piece in transit was null");
                    return;
                }

                int subpieceIndex = event.getSubpieceOffset();
                boolean completedPiece = transit.subpieceReceived(subpieceIndex);
                boolean flag = downloadMngr.storage.putSubpiece(event.getSubpieceOffset(), event.getSubpiece());

                peerInfo.getRequestPipeline().remove(new Block(piece, subpieceIndex, 0));

                if (flag) {
                    downloadMngr.downloadStats.downloadedFrom(peer, 1);
                }
                if (downloadedFrom.containsKey(event.getVodSource())) {
                    long val = downloadedFrom.get(event.getVodSource()) + 1;
                    downloadedFrom.put(event.getVodSource(), val);
                } else {
                    downloadedFrom.put(event.getVodSource(), (long) 1);
                }
                if (completedPiece) {
                    downloadMngr.pieceCompleted(piece, peer, time);
                    downloadMngr.partialPieces.remove(piece);
                    if (playerMngr.buffering.get()
                            && (downloadMngr.storage.getBitField().getNextUncompletedPiece() >= downloadMngr.pieceToRead.get() + playerMngr.bufferingWindow
                            || downloadMngr.storage.complete()
                            || downloadMngr.storage.getBitField().getNextUncompletedPiece() >= downloadMngr.storage.getBitField().numberPieces())) {
                        downloadMngr.restartToRead(time);
                    } else {
                        logger.info(compName + "State: " + playerMngr.buffering.get() + " Buffering: {} > {}",
                                downloadMngr.storage.getBitField().getNextUncompletedPiece(),
                                downloadMngr.pieceToRead.get() + playerMngr.bufferingWindow);
                    }
                }

                // if peer not choking us
                peerInfo.setPipeSize(event.getComWinSize() / VodConfig.LB_MAX_SEGMENT_SIZE);
                // try to continue requesting further blocks of this piece
                int newBlockIndex = transit.getNextSubpieceToRequest();

                if (newBlockIndex == -1) {
                    // requested all blocks of this piece. We can now try to
                    // select a new piece to request from this peer.
                    Block lastRequestedBlock = peerInfo.getRequestPipeline().peekLast();

                    if (lastRequestedBlock == null
                            || lastRequestedBlock.getPieceIndex() == piece) {
                        logger.trace(compName + "Requesting new piece");
                        // start downloading a new piece
                        downloadMngr.startDownloadingPieceFrom(peer,
                                peerInfo.getPipeSize() - peerInfo.getRequestPipeline().size(),
                                ackId, rtt, time);
                    } else {
                        // continue downloading a block from the last requested piece
                        int lastRequestedPiece = lastRequestedBlock.getPieceIndex();
                        PieceInTransit latestTransit = downloadMngr.partialPieces.get(lastRequestedPiece);

                        int blockToRequest = -1;
                        if (latestTransit != null) {
                            blockToRequest = latestTransit.getNextSubpieceToRequest();
                        }
                        if (blockToRequest != -1) {
                            triggerDataMsgRequest(peerInfo, ackId, lastRequestedPiece,
                                    blockToRequest, rtt);
                        } else {
                            // all blocks were requested from the last requested
                            // piece. we request a new piece
                            downloadMngr.startDownloadingPieceFrom(peer,
                                    peerInfo.getPipeSize() - peerInfo.getRequestPipeline().size(),
                                    ackId, rtt, time);
                        }
                    }
                } else {
                    // we just request the next block of this piece
                    triggerDataMsgRequest(peerInfo, ackId, piece, newBlockIndex, rtt);
                }

            } catch (IOException e) {
                e.printStackTrace();
                logger.error(compName + e.getMessage());
                logger.error(compName + "Exception in handleDataMsgResponse. Could not write the subpiece to disk : ",
                        event.getSubpieceOffset());
            }
        }
    };
    Handler<DataMsg.DataRequestTimeout> handleDataRequestTimeout = new Handler<DataMsg.DataRequestTimeout>() {
        @Override
        public void handle(DataMsg.DataRequestTimeout event) {

            TimeoutId timeoutId = event.getTimeoutId();
//            if (outstandingDataRequests.contains(timeoutId)) {
            if (delegator.doCancelRetry(timeoutId)) {
                // remove that piece from the pipeline and outstanding
                int piece = event.getPiece();
                int subpiece = event.getSubpiece();
                VodAddress peer = event.getDest();
                logger.warn(compName + "DataRequestTimeout for SUBPIECE({}, {}) from {}",
                        new Object[]{piece, subpiece, peer.getId()});
                // remove the subpiece request from the pipeline
                VodDescriptor peerInfo = connectionMngr.store.getVodDescriptorFromVodAddress(peer);
                if (peerInfo == null) {
                    logger.warn(compName + "VodNodeDescriptor was null for {}. Probably removed.", peer.getId());
                } else {
                    LinkedList<Block> pipe = peerInfo.getRequestPipeline();
                    if (pipe != null) {
                        pipe.remove(new Block(piece, subpiece, 0));
                    } else {
                        logger.warn(compName + "Pipeline was null after the DataRequest.Timeout.");
                    }

                    // re-request the subpiece
                    PieceInTransit transit = downloadMngr.partialPieces.get(piece);
                    if (transit != null) {
                        transit.subpieceTimedOut(subpiece);
                    } else {
                        logger.warn(compName + "DataRequestTimeout: couldn't timeout block in PieceInTransit");
                    }
                }
                rttMsgs.remove(timeoutId);
            }
        }
    };

    private void updateRtts(int id, long rtt) {
        // SRTT(i+1) = α * SRTT(i) + ( 1-α ) * S(i)
        // RTO(i) = β * SRTT(i)
        Long srtt = srtts.get(id);
        if (srtt == null) {
            srtt = rtt;
            srtts.put(id, srtt);
        } else {
            srtt = (long) ((tcpAlpha * srtt) + ((1 - tcpAlpha) * rtt));
        }

        Double rto = rtos.get(id);
        if (rto == null) {
            rto = (double) VodConfig.DATA_REQUEST_TIMEOUT;
            rtos.put(id, rto);
        } else {
            rto = (tcpBeta * srtt);
        }

//                System.out.println("SRTT to " + id + ": " + srtt + "  rto:" + rto);
    }

    /**
     * forward a request message to a node that have the requested piece
     *
     * @param event
     */
    private void triggerForwardDataRequest(DataMsg.Request event) {
        UtilityVod utility = (UtilityVod) self.getUtility();
        VodDescriptor node = connectionMngr.bitTorrentSet.getStats().getRandomNodeWithPiece(event.getPiece(), null);
        if (node == null) {
            node = connectionMngr.upperSet.getRandomNode();
        }

        if (node != null) {
            if (!forwarded.containsKey(event.getSubpieceOffset())) {
                forwarded.put(event.getSubpieceOffset(), new ArrayList<VodAddress>());
            }
            forwarded.get(event.getSubpieceOffset()).add(event.getVodSource());

            // no need to store the timeout here
            DataMsg.Request request
                    = new DataMsg.Request(self.getAddress(),
                            node.getVodAddress(),
                            event.getAckId(),
                            event.getPiece(),
                            event.getSubpieceOffset(), 0);
            delegator.doRetry(request, self.getOverlayId());
        } else {
            DataMsg.PieceNotAvailable response = new DataMsg.PieceNotAvailable(self.getAddress(), event.getVodSource(),
                    downloadMngr.storage.getBitField().getChunkfield(), utility,
                    event.getPiece(), downloadMngr.storage.getBitField().getAvailablePieces(utility));
            delegator.doTrigger(response, network);
        }
    }

    /**
     * send the subpiece contained in event to the original seeker
     *
     * @param event
     * @throws IOException
     */
    private void forwardResponse(DataMsg.Response event) throws IOException {

        for (VodAddress peer : forwarded.get(event.getSubpieceOffset())) {
            if (connectionMngr.store.contains(peer)) {
                CommunicationWindow comWin = connectionMngr.store.getVodDescriptorFromVodAddress(peer).getWindow();
                ScheduleTimeout st = new ScheduleTimeout(ackTimeout);
                st.setTimeoutEvent(new DataMsg.AckTimeout(st, peer, self.getOverlayId()));
                TimeoutId ackId = st.getTimeoutEvent().getTimeoutId();
                if (comWin.addMessage(ackId, VodConfig.LB_MAX_SEGMENT_SIZE)) {
                    DataMsg.Response response = new DataMsg.Response(self.getAddress(), peer, event.getTimeoutId(),
                            ackId, event.getSubpiece(),
                            event.getSubpieceOffset(), event.getPiece(), comWin.getSize(),
                            System.currentTimeMillis());
                    delegator.doTrigger(response, network);
                    connectionMngr.outstandingAck.put(ackId, response.getSize());
                    delegator.doTrigger(st, timer);
                } else {
                    delegator.doTrigger(new DataMsg.Saturated(self.getAddress(), peer, event.getPiece(),
                            comWin.getSize()), network);
                }
            }
        }
//        if (storage != null) {
        boolean flag = downloadMngr.storage.putSubpiece(event.getSubpieceOffset(), event.getSubpiece());
        if (flag) {
            downloadMngr.downloadStats.downloadedFrom(event.getVodSource(), 1);
        }
//        }
        forwarded.remove(event.getSubpieceOffset());
    }

    /**
     * forward the fact that they we don't have the possibility to obtain the
     * piece
     *
     * @param event
     */
    private void forwardResponse(DataMsg.Saturated event) {
        for (VodAddress peer : forwarded.get(event.getSubpiece())) {
            if (connectionMngr.store.contains(peer)) {
                delegator.doTrigger(new DataMsg.Saturated(self.getAddress(), peer, event.getSubpiece() / BitField.NUM_PIECES_PER_CHUNK,
                        connectionMngr.store.getVodDescriptorFromVodAddress(peer).getWindow().getSize()), network);
            } else {
                delegator.doTrigger(new DataMsg.Saturated(self.getAddress(), peer, event.getSubpiece() / BitField.NUM_PIECES_PER_CHUNK,
                        connectionMngr.commWinSize), network);
            }
        }
        forwarded.remove(event.getSubpiece());
    }
    /**
     * handle the acknowledgment of the reception of a piece recalculate the
     * size of the pipe
     */
    Handler<DataMsg.Ack> handleAck = new Handler<DataMsg.Ack>() {
        @Override
        public void handle(Ack event) {
            logger.debug(compName + "DataMsg.Ack from " + event.getVodSource().getId());
            if (self == null) {
                return;
            }
            msgReceived(event.getVodSource());

            CommunicationWindow comWin = connectionMngr.store.getVodDescriptorFromVodAddress(event.getVodSource()).getWindow();
            connectionMngr.updateCommsWindow(comWin, event.getAckId(), event.getDelay());

            VodDescriptor peer = connectionMngr.store.getVodDescriptorFromVodAddress(event.getVodSource());
            if (peer == null) {
                logger.warn(compName + "Got Ack from node who isn't a neighbour: " + event.getVodSource().getId());
            } else {
                logger.debug(compName + "Node: " + event.getVodSource().getId()
                        + " CommsWindow size {} . Base/Current Delay :{}/ " + peer.getCurrentDelay(),
                        peer.getWindowSize(), peer.getBaseDelay());
            }
//            if (outstandingAck.contains(event.getAckId())) {
//                logger.trace(compName + "Received ACK {}. Updating CommWindow.", event.getAckId());
//                outstandingAck.remove(event.getAckId());
//                if (store.contains(event.getGVodSource())) {
//                    CommunicationWindow comWin = store.getNodeDescriptor(event.getGVodSource()).getWindow();
//                    comWin.update(event.getDelay());
//                    comWin.removeMessage(event.getAckId(), 1024);
//                }
//            }
        }
    };
    /**
     * handle the fact that a node is too slow to handle a dataResponse reduce
     * the size of the pipe
     */
    Handler<DataMsg.AckTimeout> handleAckTimeout = new Handler<DataMsg.AckTimeout>() {
        @Override
        public void handle(AckTimeout event) {
            if (connectionMngr.outstandingAck.containsKey(event.getTimeoutId())) {
                logger.debug(compName + "Ack timed out {} to {}", event.getTimeoutId(),
                        event.getPeer().getPeerAddress().getId());
                Integer msgSize = connectionMngr.outstandingAck.remove(event.getTimeoutId());
                if (connectionMngr.store.contains(event.getPeer())) {
                    CommunicationWindow comWin = connectionMngr.store.getVodDescriptorFromVodAddress(event.getPeer()).getWindow();
                    comWin.timedout(event.getTimeoutId(), msgSize);
                }
            }
        }
    };
    /**
     * handle the fact that the the pipe is full adjust the size of its own pipe
     */
    Handler<DataMsg.Saturated> handleSaturated = new Handler<DataMsg.Saturated>() {
        @Override
        public void handle(Saturated event) {
            logger.warn(compName + "DataMsg.Saturated from {} for subpiece {}. Comm Win Size = " + event.getComWinSize(),
                    event.getVodSource().getId(),
                    event.getSubpiece());
            if (self == null) {
                return;
            }
            msgReceived(event.getVodSource());

            if (forwarded.containsKey(event.getSubpiece())) {
                forwardResponse(event);
            }
            if (!downloadMngr.partialPieces.containsKey(event.getSubpiece() / BitField.NUM_PIECES_PER_CHUNK)) {
                return;
            }
            downloadMngr.partialPieces.remove(event.getSubpiece() / BitField.NUM_PIECES_PER_CHUNK);
            if (connectionMngr.store.contains(event.getVodSource())) {
                connectionMngr.store.getVodDescriptorFromVodAddress(event.getVodSource()).setPipeSize(
                        event.getComWinSize() / VodConfig.LB_MAX_SEGMENT_SIZE);
            }
        }
    };
    /**
     * handle a uploading rate request, inform the grandparent of the parent
     * behavior
     */
    Handler<UploadingRateMsg.Request> handleUploadingRateRequest = new Handler<UploadingRateMsg.Request>() {
        @Override
        public void handle(UploadingRateMsg.Request event) {
            logger.debug(compName + "UploadingRateMsg.Request from {}", event.getVodSource().getId());
            if (self == null) {
                return;
            }
            msgReceived(event.getVodSource());

            if (connectionMngr.store != null && connectionMngr.store.contains(event.getTarget())) {
                delegator.doTrigger(new UploadingRateMsg.Response(self.getAddress(), event.getVodSource(),
                        event.getTimeoutId(), event.getTarget(),
                        downloadMngr.downloadStats.getDownloaded(event.getTarget())),
                        network);
            } else {
                delegator.doTrigger(new UploadingRateMsg.Response(self.getAddress(), event.getVodSource(),
                        event.getTimeoutId(), event.getTarget(),
                        0), network);
            }
        }
    };
    /**
     * handle uploading rate response from the grandchildren
     */
    Handler<UploadingRateMsg.Response> handleUploadingRateResponse = new Handler<UploadingRateMsg.Response>() {
        @Override
        public void handle(UploadingRateMsg.Response event) {
            logger.debug(compName + "UploadingRateMsg.Response from {}", event.getVodSource().getId());
            if (self == null) {
                return;
            }
            msgReceived(event.getVodSource());

            if (delegator.doCancelRetry(event.getTimeoutId())) {
                delegator.doTrigger(new CancelTimeout(event.getTimeoutId()), timer);
                connectionMngr.bitTorrentSet.incrementUploadRate(event.getTarget(), event.getRate());
                connectionMngr.lowerSet.incrementUploadRate(event.getTarget(), event.getRate());
            }
        }
    };
    /**
     * time out if a grandchildren is to long to send an uploading rate response
     */
    Handler<UploadingRateMsg.UploadingRateTimeout> handleUploadingRateTimeout = new Handler<UploadingRateMsg.UploadingRateTimeout>() {
        @Override
        public void handle(UploadingRateMsg.UploadingRateTimeout event) {
            logger.warn(compName + "handleUploadingRateTimeout");
        }
    };

    /**
     * handle a pieceNotAvailable informing that we asked a piece to a node that
     * didn't have it
     */
    Handler<DataMsg.PieceNotAvailable> handlePieceNotAvailable = new Handler<DataMsg.PieceNotAvailable>() {
        @Override
        public void handle(DataMsg.PieceNotAvailable event) {
            UtilityVod utility = (UtilityVod) self.getUtility();
            logger.warn(compName + "handlePieceNotAvailable " + event.getPiece()
                    + " at " + event.getSource().getId());
            int piece = event.getPiece();
            if (!downloadMngr.partialPieces.containsKey(piece)) {
                return;
            }

            VodAddress peer = event.getVodSource();

            // mark that we downloaded a block
            VodDescriptor peerInfo = connectionMngr.store.getVodDescriptorFromVodAddress(peer);
            downloadMngr.partialPieces.remove(event.getPiece());
            if (peerInfo == null) {
                return;
            }
            peerInfo.discardPiece(event.getPiece());
//            removeFromUploaders(peer);
            if (connectionMngr.bitTorrentSet.contains(peer)) {
                VodDescriptor toBeDisconnected = connectionMngr.bitTorrentSet.updatePeerInfo(
                        event.getVodSource(), event.getUtility(),
                        event.getAvailableChunks(),
                        event.getAvailablePieces());
                if (toBeDisconnected != null) {
                    triggerDisconnectRequest(toBeDisconnected.getVodAddress(), false);
                }
            } else if (connectionMngr.upperSet.contains(peer)) {
                VodAddress toBeDisconnected = connectionMngr.upperSet.updateUtility(peer,
                        event.getUtility());
                if (toBeDisconnected != null) {
                    triggerDisconnectRequest(toBeDisconnected, false);
                }
            }
        }
    };

    /**
     * handle a request to share the hashes of a chunk's pieces
     */
    Handler<DataMsg.HashRequest> handleHashRequest = new Handler<DataMsg.HashRequest>() {
        @Override
        public void handle(DataMsg.HashRequest event) {
            logger.debug(compName + "handle hash request {}", event.getChunk());
            if (self == null) {
                return;
            }
            msgReceived(event.getVodSource());

            byte[] hashes = downloadMngr.storage.getMetaInfo().getChunkHashes(event.getChunk());
            if (hashes != null) {
                int numParts = 0;
                int lastHashSize = hashes.length % mtu;
                if (hashes.length > mtu) {
                    numParts = (hashes.length / mtu);
                }
                if (lastHashSize > 0) {
                    numParts++;
                }
                logger.debug(compName + "hash responses. num parts = " + numParts);
                int start = 0, end = 0;
                for (int i = 0; i < numParts; i++) {
                    if (i + 1 == numParts) {
                        end += lastHashSize;
                    } else {
                        end += mtu;
                    }
                    byte[] hash = Arrays.copyOfRange(hashes, start, end);
                    delegator.doTrigger(new DataMsg.HashResponse(self.getAddress(), event.getVodSource(),
                            event.getTimeoutId(), event.getChunk(), hash, i,
                            numParts), network);
                    start += mtu;
                    logger.debug(compName + "Sending HashResponse of size {} for chunk {} part {} to {}",
                            new Object[]{hash.length, event.getChunk(), i,
                                event.getVodSource().getPeerAddress().getId()});
                }
            }
        }
    };
    /**
     * handle the response to a hash request, check if they are good and store
     * them
     */
    Handler<DataMsg.HashResponse> handleHashResponse = new Handler<DataMsg.HashResponse>() {
        @Override
        public void handle(DataMsg.HashResponse event) {
            logger.debug(compName + "handle hash response {}. Part {}", event.getChunk(), event.getPart());
            if (self == null) {
                return;
            }
            msgReceived(event.getVodSource());

            TimeoutId hashReqId = event.getTimeoutId();
            int chunk = event.getChunk();
            int part = event.getPart();
            int numParts = event.getNumParts();

            if (!outstandingHashRequest.contains(hashReqId)) {
                logger.info(compName + "Stale HashResponse received for chunk:part {}:{}",
                        chunk, part);
                return;
            }

            if (downloadMngr.storage.getMetaInfo().haveHashes(chunk)) { //already have hashes
                downloadMngr.hashRequests.remove(chunk);
                awaitingHashResponses.remove(hashReqId);
                outstandingHashRequest.remove(hashReqId);
                CancelTimeout ct = new CancelTimeout(hashReqId);
                delegator.doTrigger(ct, timer);
            } else {
                // TODO checksum of the hashes recvd to see if correct
                Map<Integer, ByteBuffer> waitingResponses = awaitingHashResponses.get(hashReqId);
                ByteBuffer bb = ByteBuffer.allocate(event.getHashes().length);
                bb.put(event.getHashes());
                waitingResponses.put(part, bb);

                int n = numHashPartsReturned(waitingResponses);
                if (n == numParts) {
                    int totalSize = 0;
                    for (int i = 0; i < numParts; i++) {
                        totalSize += waitingResponses.get(i).capacity();
                    }
                    ByteBuffer allHashes = ByteBuffer.allocate(totalSize);
                    for (int i = 0; i < numParts; i++) {
                        allHashes.put(waitingResponses.get(i).array());
                    }

                    // check if already received the hashes, if yes - remove req & responses
                    if (downloadMngr.storage.getMetaInfo().setPieceHashes(allHashes.array(), chunk)) {
                        downloadMngr.hashRequests.remove(chunk);
                        awaitingHashResponses.remove(hashReqId);
                        outstandingHashRequest.remove(hashReqId);
                        CancelTimeout ct = new CancelTimeout(hashReqId);
                        delegator.doTrigger(ct, timer);
                    }
                }
            }

        }
    };

    private int numHashPartsReturned(Map<Integer, ByteBuffer> waitingResponses) {
        Set<Integer> keys = waitingResponses.keySet();
        int n = 0;
        for (Integer i : keys) {
            if (waitingResponses.get(i) != null) {
                n++;
            }
        }
        return n;
    }
    /**
     * handle a timeout when a node is to slow to answer to a hash request
     */
    Handler<DataMsg.HashTimeout> handleHashTimeout = new Handler<DataMsg.HashTimeout>() {
        @Override
        public void handle(DataMsg.HashTimeout event) {
            outstandingHashRequest.remove(event.getTimeoutId());
            if (downloadMngr.hashRequests.containsKey(event.getChunk())) {
                downloadMngr.hashRequests.get(event.getChunk()).remove(event.getPeer());
                if (downloadMngr.hashRequests.get(event.getChunk()).isEmpty()) {
                    downloadMngr.hashRequests.remove(event.getChunk());
                }
                logger.warn(compName + "Hash Request timed out");
                // no need to re-send hash request, as this will happen during piece selection.
            }
        }
    };

//    Handler<ChangeUtilityMsg.Response> handleChangeUtilityMsgResponse =
//            new Handler<ChangeUtilityMsg.Response>() {
//
//                @Override
//                public void handle(ChangeUtilityMsg.Response event) {
//                    List<GVodNodeDescriptor> tocheck = event.getPeers();
//        // Add those nodes that have now moved from upper-set to bittorrent set
//        List<GVodNodeDescriptor> sendConnect = bittorrentSet.updateAll(tocheck);
//
//        noLongerInUpperSet.removeAll(sendConnect);
//
//        for (GVodNodeDescriptor node : noLongerInUpperSet) {
//            // TODO - do we have a cleanup event to remove old connections from
//            // lower sets - e.g., incase this disconnect msg is lost?
//            triggerDisconnectRequest(node.getGVodAddress());
//        }
//
//        if (utility.getPiece() <= storage.getBitField().pieceFieldSize()) {
//            for (GVodNodeDescriptor node : sendConnect) {
//                triggerConnectRequest(node, true);
//            }
//        }
    // add nodes to bittorrentity/upper sets
//                }
//            };
//    Handler<ChangeUtilityMsg.RequestTimeout> handleChangeUtilityMsgRequestTimeout =
//            new Handler<ChangeUtilityMsg.RequestTimeout>() {
//
//                @Override
//                public void handle(ChangeUtilityMsg.RequestTimeout event) {
//                    // resend
//                }
//            };
    /**
     * Simulator for viewing the video. Read the next piece to be read.
     * readingPeriod in GVodConfiguration determines the frequency with which
     * this handler is invoked.
     */
    Handler<Read> handleRead = new Handler<Read>() {
        @Override
        public void handle(Read event) {
//            logger.trace(compName + "handleRead:");
            if (!playerMngr.buffering.get() && event.getTimeoutId() == readTimerId) {
                // TODO - IndexOutOfBoundsException here sometimes
                if (!downloadMngr.storage.getBitField().getPiece(downloadMngr.pieceToRead.get())) {
                    logger.warn(compName + "Buffering piece {}", downloadMngr.pieceToRead.get());
                    bufferingNum++;

                    playerMngr.stoppedReadingAtTime = System.currentTimeMillis();
                    playerMngr.buffering.set(true);
                } else {
                    downloadMngr.pieceToRead.getAndIncrement();
                    if (downloadMngr.pieceToRead.get() >= downloadMngr.storage.getBitField().numberPieces()
                            || downloadMngr.pieceToRead.get() >= downloadMngr.storage.getBitField().getPieceFieldLength() * 8) {
                        logger.info(compName + "finished to read after {}, number of buffering={} "
                                + bW,
                                durationToString(System.currentTimeMillis() - playerMngr.startedAtTime), bufferingNum);
                        delegator.doTrigger(new ReadingCompleted(self.getAddress(), bufferingNum, waiting,
                                misConnect, playerMngr.totalBufferTime, null,
                                historyMngr.getUtilityAfterTime(), downloadMngr.freeRider, playerMngr.totalJumpForward), vod);
                        CancelPeriodicTimeout cPT = new CancelPeriodicTimeout(readTimerId);
                        delegator.doTrigger(cPT, timer);
                        readTimerId = null;
                    }

                }
            }

        }
    };
    /**
     * handle a message from the user interface asking to restart to read
     */
    Handler<Play> handlePlayEvent = new Handler<Play>() {
        @Override
        public void handle(Play event) {
            logger.debug(compName + "handlePlayEvent");
            play();
        }
    };

    public void play() {
//        read = true;
        if (playerMngr.buffering.get()
                && (downloadMngr.storage.getBitField().getNextUncompletedPiece()
                >= downloadMngr.pieceToRead.get() + playerMngr.bufferingWindow || downloadMngr.storage.complete()
                || downloadMngr.storage.getBitField().getNextUncompletedPiece()
                >= downloadMngr.storage.getBitField().numberPieces())) {
            logger.info(compName + "RESTARTING TO READ: " + self.getId() + " buffering=" + playerMngr.buffering + " piece-to-read:"
                    + downloadMngr.pieceToRead.get() + " -- bufferingWindow: " + playerMngr.bufferingWindow
                    + " => First uncompleted piece: " + downloadMngr.storage.getBitField().getNextUncompletedPiece());
            downloadMngr.restartToRead(time);
        } else {
            logger.info(compName + "NOT RESTARTING TO READ: " + self.getId() + " buffering=" + playerMngr.buffering + " piece-to-read:"
                    + downloadMngr.pieceToRead.get() + " -- bufferingWindow: " + playerMngr.bufferingWindow
                    + " => First uncompleted piece: " + downloadMngr.storage.getBitField().getNextUncompletedPiece());
        }
    }
    /**
     * handle a message from the user interface asking tu pause the reading of
     * the video
     */
    Handler<Pause> handlePause = new Handler<Pause>() {
        @Override
        public void handle(Pause event) {
            logger.trace(compName + "handlePause");
//            read = false;
            playerMngr.buffering.set(true);
        }
    };
    /**
     * if the node is turning in background and use too-much
     */
    Handler<SlowBackground> handleSlowBackground = new Handler<SlowBackground>() {
        @Override
        public void handle(SlowBackground event) {
            if (connectionMngr.maxWindowSize == 0) {
                connectionMngr.maxWindowSize = downloadMngr.pipeSize * VodConfig.LB_MAX_SEGMENT_SIZE;
            }
            connectionMngr.maxWindowSize = connectionMngr.maxWindowSize / 2;
            if (connectionMngr.maxWindowSize < VodConfig.LB_MAX_SEGMENT_SIZE) {
                connectionMngr.maxWindowSize = VodConfig.LB_MAX_SEGMENT_SIZE;
            }
            for (VodDescriptor node : connectionMngr.store.getAll()) {
                node.getWindow().setMaxWindowSize(connectionMngr.maxWindowSize);
            }
        }
    };
    /**
     * the opposite of slowbackground
     */
    Handler<SpeedBackground> handleSpeedBackground = new Handler<SpeedBackground>() {
        @Override
        public void handle(SpeedBackground event) {
            connectionMngr.maxWindowSize = +connectionMngr.maxWindowSize / 2;
            for (VodDescriptor node : connectionMngr.store.getAll()) {
                node.getWindow().setMaxWindowSize(connectionMngr.maxWindowSize);
            }
        }
    };

    private void triggerHashRequest(VodAddress peer, int chunk, int part) {

        DataMsg.HashRequest msg = new DataMsg.HashRequest(self.getAddress(), peer, chunk);

        ScheduleRetryTimeout st = new ScheduleRetryTimeout(VodConfig.HASH_REQUEST_TIMEOUT,
                VodConfig.DEFAULT_RTO_RETRIES);
        DataMsg.HashTimeout dht = new DataMsg.HashTimeout(st, msg, chunk, peer, part);
        logger.debug(compName + "trigger hash request {} to {}", chunk, peer.getId());
        TimeoutId tid = delegator.doRetry(dht);
        outstandingHashRequest.add(tid);
        Map<Integer, ByteBuffer> awaitingResponses = new HashMap<Integer, ByteBuffer>();

        // ASSUME MAX NUMBER OF UDP PACKETS IN HASH RESPONSE
        for (int i = 0; i < VodConfig.MAX_NUM_HASH_RESPONSE_PACKETS; i++) {
            awaitingResponses.put(i, null);
        }
        awaitingHashResponses.put(tid, awaitingResponses);

        if (!downloadMngr.hashRequests.containsKey(chunk)) {
            downloadMngr.hashRequests.put(chunk, new ArrayList<VodAddress>());
        }
        downloadMngr.hashRequests.get(chunk).add(peer);
    }

    private void triggerDataMsgRequest(VodDescriptor peerInfo, TimeoutId ackId, int piece,
            int subpiece, long delay) {
        if (ackId == null) {
            logger.debug(compName + "ACKID was null when requesting new piece");
        }
        VodAddress des = peerInfo.getVodAddress();
        Double rto = rtos.get(des.getId());
        if (rto == null) {
            rto = (double) config.getDataRequestTimeout();
        }

        DataMsg.Request request = new DataMsg.Request(self.getAddress(), des,
                ackId, piece, subpiece, delay);
        ScheduleRetryTimeout st = new ScheduleRetryTimeout(rto.longValue(), 1);
        DataMsg.DataRequestTimeout t = new DataMsg.DataRequestTimeout(st, request);
        delegator.doRetry(t);

        peerInfo.getRequestPipeline().add(new Block(piece, subpiece,
                System.currentTimeMillis()));
        rttMsgs.put(t.getTimeoutId(), System.currentTimeMillis());

        logger.debug(compName + "Requesting subpiece {}:{} on {}",
                new Object[]{piece, subpiece, des.getId()});
    }

    /**
     * take a time in a long formate and transform in in a human-readable
     * string.
     *
     * @param duration
     * @return
     */
    public static String durationToString(long duration) {
        StringBuilder sb = new StringBuilder();
        int ms = 0, s = 0, m = 0, h = 0, d = 0, y = 0;
        ms = (int) (duration % 1000);
        // get duration in seconds
        duration /= 1000;
        s = (int) (duration % 60);
        // get duration in minutes
        duration /= 60;
        if (duration > 0) {
            m = (int) (duration % 60);
            // get duration in hours
            duration /= 60;
            if (duration > 0) {
                h = (int) (duration % 24);
                // get duration in days
                duration /= 24;
                if (duration > 0) {
                    d = (int) (duration % 365);
                    // get duration in years
                    y = (int) (duration / 365);
                }
            }
        }
        boolean printed = false;
        if (y > 0) {
            sb.append(y).append("y ");
            printed = true;
        }
        if (d > 0) {
            sb.append(d).append("d ");
            printed = true;
        }
        if (h > 0) {
            sb.append(h).append("h ");
            printed = true;
        }
        if (m > 0) {
            sb.append(m).append("m ");
            printed = true;
        }
        if (s > 0 || !printed) {
            sb.append(s);
            if (ms > 0) {
                sb.append("").append(String.format("%03d", ms));
            }
            sb.append("ms");
        }
        return sb.toString();
    }

    /**
     * return true if the node is buffering
     *
     * @return
     */
    public boolean isBuffering() {
        return playerMngr.buffering.get();
    }

    /**
     * return the logger of the node
     *
     * @return
     */
    public Logger getLogger() {
        return logger;
    }

    /**
     * return the next piece to send to the video reader
     *
     * @return
     */
    public int getNextPieceToSend() {
        return playerMngr.nextPieceToSend.get();
    }

    /**
     * return the storage of the node
     *
     * @return
     */
    public Storage getStorage() {
        return downloadMngr.storage;
    }

    /**
     * set the next piece to be sent to the video-player
     *
     * @param nextPieceToSend
     */
    public void setNextPieceToSend(int nextPieceToSend) {
        playerMngr.nextPieceToSend.set(nextPieceToSend);
    }

    private boolean isMp4() {
        return downloadMngr.storage.getMetaInfo().isMp4();
    }

    public void interruptSender() throws SecurityException {
        logger.info(compName + "Starting Interrupting Sender");
        if (sender != null) {
            sender.interrupt();
            sender = null;
        }
        logger.info(compName + "Finished Interrupting Sender");
    }

    public void writeHandlerBody() {
        handler.writeBody();
    }

    @Override
    public void stop(Stop event) {
        if (connectionMngr.store != null) {
            for (VodAddress addr : connectionMngr.store.getNeighbours().keySet()) {
                triggerDisconnectRequest(addr, true);
            }
        }
        cancelPeriodicTimer(initiateShuffleTimeoutId);
        cancelPeriodicTimer(dataOfferPeriodTimeoutId);
        cancelPeriodicTimer(readTimerId);
    }

    public class ConnectionDelegator {

        public void connect(VodDescriptor peer, boolean noDelay) {
            triggerConnectRequest(peer, noDelay);
        }

        public void disconnect(VodAddress peer, boolean noDelay) {
            triggerDisconnectRequest(peer, noDelay);
        }

        public void disconnectResponse(DisconnectMsg.Response resp) {
            delegator.doTrigger(resp, network);
        }

        public void leaving(LeaveMsg event) {
            delegator.doTrigger(event, network);
        }

        public void dataOffer(DataOfferMsg event) {
            delegator.doTrigger(event, network);
        }

        public void dataResp(DataMsg.Response resp) {
            delegator.doTrigger(resp, network);
        }

        public void startTimer(ScheduleTimeout st) {
            delegator.doTrigger(st, timer);
        }

        public void saturated(DataMsg.Saturated event) {
            delegator.doTrigger(event, network);
        }
    }

    public class DownloadDelegator {

        public void ack(DataMsg.Ack msg) {
            delegator.doTrigger(msg, network);
        }

        public void downloadComplete(DownloadCompletedSim msg) {
            delegator.doTrigger(msg, vod);
            delegator.doTrigger(msg, status);
        }

        public void downloadReq(VodDescriptor peerInfo, TimeoutId ackId, int piece, int nextBlock, long rtt) {
            triggerDataMsgRequest(peerInfo, ackId, piece, nextBlock, rtt);
        }

        public void hashReq(VodAddress peer, int chunk, int part) {
            triggerHashRequest(peer, chunk, part);
        }
    }
}
