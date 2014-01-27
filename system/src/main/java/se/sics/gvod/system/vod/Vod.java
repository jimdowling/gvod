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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Random;
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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import org.apache.commons.math.stat.descriptive.SummaryStatistics;
import se.sics.gvod.common.BitField;
import se.sics.gvod.common.CommunicationWindow;
import se.sics.gvod.config.VodConfig;
import se.sics.gvod.common.msgs.ConnectMsg;
import se.sics.gvod.common.msgs.DisconnectMsg;
import se.sics.gvod.system.main.GMain;
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
import se.sics.gvod.system.peer.sets.BitTorrentSet;
import se.sics.gvod.system.peer.sets.DownloadStats;
import se.sics.gvod.system.peer.sets.InitiateDataOffer;
import se.sics.gvod.system.peer.sets.LowerSet;
import se.sics.gvod.croupier.events.CroupierSample;
import se.sics.gvod.nat.traversal.NatTraverserPort;
import se.sics.gvod.nat.traversal.events.DisconnectNeighbour;
import se.sics.gvod.net.Nat;
import se.sics.gvod.net.msgs.ScheduleRetryTimeout;
import se.sics.gvod.system.peer.VodPeerPort;
import se.sics.gvod.system.peer.sets.UpperSet;
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
import se.sics.gvod.system.peer.sets.DescriptorStore;
import se.sics.gvod.system.storage.StorageFcByteBuf;
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
 * The
 * <code>GVod</code> class.
 *
 * @author Gautier Berthou
 * @author Jim Dowling
 */
public final class Vod extends MsgRetryComponent {

    public static final boolean MEM_MAP_VOD_FILES = false;
    Positive<NatTraverserPort> natTraverserPort = positive(NatTraverserPort.class);
    Negative<Status> status = negative(Status.class);
    Negative<VodPort> vod = negative(VodPort.class);
    Positive<PeerSamplePort> croupier = positive(PeerSamplePort.class);
    Negative<VodPeerPort> peer = negative(VodPeerPort.class);
    private Logger logger = LoggerFactory.getLogger(Vod.class);
    private Self self;
    /**
     * Use croupier and gradient addresses to index into DescriptorStore
     */
    private BitTorrentSet bitTorrentSet;
    private UpperSet upperSet;
    private LowerSet lowerSet;
    private List<VodDescriptor> croupierSet = new ArrayList<VodDescriptor>();
    private List<VodDescriptor> gradientSet = new ArrayList<VodDescriptor>();
    /**
     * This contains all the most up-to-date VodDescriptors.
     */
    private DescriptorStore store;
    private long readingPeriod;
    private int bitTorrentSetSize, upperSetSize, lowerSetSize;
    private Map<VodAddress, Long> suspected;
    private Map<Integer, List<VodAddress>> forwarded;
    private Map<Integer, Long> utilityAfterTime;
    private Map<Integer, Integer> rest;
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
    private Storage storage;
    private String torrentFileAddress;
    private String videoName;
    private long length;
    private long startedAtTime, stoppedReadingAtTime;
    private float piecesFromUtilitySet = 0, piecesFromUpperSet = 0;
    private int bufferingNum = 0;
    private AtomicInteger pieceToRead = new AtomicInteger(0);
    private AtomicBoolean buffering = new AtomicBoolean(true);
    private int pipeSize;
    private int infUtilFrec;
    private int count = 0;
    private int waiting = 0;
    private int misConnect = 0;
    private int seed;
    private boolean seeder = false;
    private long bufferingTime = 0;
    private Random random;
    private boolean finished = false;
    private int bufferingWindow;
    private int commWinSize;
    private long bW;
    private int time = 0;
    private boolean freeRider;
    private List<VodAddress> choked;
    private Map<VodAddress, Long> downloadedFrom;
    private int overhead;
    private List<VodAddress> chokedUnder;
    private Map<Integer, PieceInTransit> partialPieces;
    private long startJumpForward, totalJumpForward = 0;
    private boolean jumped = false;
    private Map<Integer, VodDescriptor> fingers = new HashMap<Integer, VodDescriptor>();
    private boolean rebootstrap = false;
    private Sender sender;
    private boolean read = false;
    private boolean simulation;
    private Vod comp;
    private int ackTimeout;
    private Map<TimeoutId, Integer> outstandingAck;
    private DownloadStats downloadStats = new DownloadStats();
    private Map<Integer, List<VodAddress>> hashRequests;
    private List<TimeoutId> outstandingHashRequest;
    private Map<TimeoutId, Map<Integer, ByteBuffer>> awaitingHashResponses;
    private int maxWindowSize = 0;
    private AtomicInteger nextPieceToSend = new AtomicInteger(0);
    private GMain main;
    private int totalNumPiecesDownloaded = 0;
    private int numRoundsNoPiecesDownloaded = 0;
    private int mtu;
    private BaseHandler handler;
    private TimeoutId initiateShuffleTimeoutId,
            dataOfferPeriodTimeoutId, readTimerId;
    private Map<Integer, Long> ongoingConnectRequests = new HashMap<Integer, Long>();
    private VodConfiguration config;
    private String compName;

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
        chokedUnder = new ArrayList<VodAddress>();
        suspected = new HashMap<VodAddress, Long>();
        forwarded = new HashMap<Integer, List<VodAddress>>();
        utilityAfterTime = new HashMap<Integer, Long>();
        rest = new HashMap<Integer, Integer>();

        utilityAfterTime.put(0, new Long(0));
        choked = new ArrayList<VodAddress>();
        partialPieces = new HashMap<Integer, PieceInTransit>();
        outstandingAck = new HashMap<TimeoutId, Integer>();
        hashRequests = new HashMap<Integer, List<VodAddress>>();
        outstandingHashRequest = new ArrayList<TimeoutId>();
        awaitingHashResponses = new HashMap<TimeoutId, Map<Integer, ByteBuffer>>();
        comp = this;
    }
    /**
     * handles a request to init the component
     */
    Handler<VodInit> handleInit = new Handler<VodInit>() {
        @Override
        public void handle(VodInit init) {
            logger.trace(compName + "handle setsInit");
            self = init.getSelf();
            config = init.getConfig();
//            selfNoParents = new SelfNoParents(self.getAddress());
            simulation = init.isSimulation();
            if (simulation) {
                read = true;
            }
            compName = "(" + self.getId() + "," + self.getOverlayId() + ") ";
            overhead = 20;
            bitTorrentSetSize = config.getBitTorrentSetSize();
            upperSetSize = config.getUpperSetSize();
            lowerSetSize = config.getLowerSetSize();
//            offset = config.getOffset();
            self.updateUtility(new UtilityVod(init.getUtility()));
            videoName = config.getName();
            length = config.getLength();
            readingPeriod = config.getReadingPeriod();
            pipeSize = config.getPipeSize();
            seed = config.getSeed();
            random = new Random(seed);
            infUtilFrec = config.getInfUtilFrec();
            ackTimeout = config.getAckTimeout();
            commWinSize = config.getComWinSize();
            bufferingWindow = config.getBufferingWindow();
            mtu = config.getMtu();
            logger.trace(compName + "finish setInit");
            logger.debug(compName + "COMMS_WIN_SIZE = " + commWinSize);
            logger.debug(compName + "PIPELINE_SIZE = " + pipeSize);
            logger.info(compName + "JOIN. SELF == {}", self.getAddress());
            torrentFileAddress = init.getTorrentFileAddress();
            bW = init.getDownloadBw();
            main = init.getMain();

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
                            handler = new FlvHandler(main, comp);
                        } else if (postfix.compareToIgnoreCase(".mp4") == 0) {
                            handler = new Mp4Handler(main, comp);
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


            store = new DescriptorStore(seed);
            bitTorrentSet = new BitTorrentSet(self, store, bitTorrentSetSize, seed);
            upperSet = new UpperSet(self, store, upperSetSize, seed);
            lowerSet = new LowerSet(self, store, lowerSetSize, seed);
            freeRider = init.isFreeRider();
            buffering.set(true);

            pieceToRead.set(init.getUtility() * BitField.NUM_PIECES_PER_CHUNK);

            logger.info(compName + "freerider = {}", freeRider);

            boolean seeder = init.isSeed();

            SchedulePeriodicTimeout spt = new SchedulePeriodicTimeout(config.getShufflePeriod(),
                    config.getShufflePeriod());
            spt.setTimeoutEvent(new InitiateMembershipSearch(spt, self.getOverlayId()));
            delegator.doTrigger(spt, timer);

            spt = new SchedulePeriodicTimeout(config.getDataOfferPeriod(),
                    config.getDataOfferPeriod());
            spt.setTimeoutEvent(new InitiateDataOffer(spt, self.getOverlayId()));
            delegator.doTrigger(spt, timer);
            dataOfferPeriodTimeoutId = spt.getTimeoutEvent().getTimeoutId();

            File metaInfoFile = new File(torrentFileAddress);

            if (seeder) {
                freeRider = false;
                buffering.set(false);
                try {
                    if (simulation) {
                        logger.debug(compName + "new storage");
                        storage = new StorageSimu(videoName, length);
                        storage.create(null);
                        FileOutputStream fos = new FileOutputStream(torrentFileAddress);
                        logger.debug(compName + "write .data");
                        fos.write(storage.getMetaInfo().getData());
                        fos.close();
                    } else {
                        MetaInfoExec metaInfo = null;
                        if (storage == null) {
                            FileInputStream in = new FileInputStream(metaInfoFile);
                            metaInfo = new MetaInfoExec(in, torrentFileAddress);
                            if (MEM_MAP_VOD_FILES) {
                                storage = new StorageMemMapWholeFile(metaInfo, metaInfoFile.getParent());
                            } else {
                                storage = new StorageFcByteBuf(metaInfo, metaInfoFile.getParent());
                            }
                        } else {
                            metaInfo = (MetaInfoExec) storage.getMetaInfo();
                            // we passed in a storage object, so we must have created
                            // this storage file locally - no need to check it.
                        }
                        storage.check(false);
                        if (storage.needed() != 0) {
                            // TODO JIM - fix this behaviour, shouldn't quit.
                            //not truly a seed => quit
                            logger.warn(compName + "The movie file does not correspond to the data file the seed will quit for:"
                                    + metaInfo.getName());
//                            delegator.doTrigger(new QuitCompleted(self.getId()), vod);
//                            return;
//                            seeder = false;
                        }
                    }
                    ActiveTorrents.makeSeeder(torrentFileAddress);
                    rest.put(0, storage.needed());
                    self.updateUtility(new UtilityVod(VodConfig.SEEDER_UTILITY_VALUE));
                    UtilityVod utility = (UtilityVod) self.getUtility();
                    bitTorrentSet.getStats().changeUtility(utility.getChunk(), utility,
                            storage.getBitField().pieceFieldSize(),
                            storage.getBitField());
                    logger.info(compName + storage.getBitField().getHumanReadable2());
                } catch (IOException e) {
                    logger.error(compName + "problem while trying to initialize the seed: " + e.getMessage(), e);
                    logger.error(compName + "Metafile: " + torrentFileAddress);
                    return;
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
                            storage = new StorageMemMapWholeFile(metaInfo, metaInfoFile.getParent());
                        } else {
                            storage = new StorageFcByteBuf(metaInfo, metaInfoFile.getParent());
                        }
                    }
                    logger.info(compName + "check storage: " + storage.getMetaInfo().getName());
                    storage.check(true);
                    rest.put(0, storage.needed());
                    UtilityVod myUtility = (UtilityVod) self.getUtility();
                    myUtility.setChunk(storage.getBitField().
                            setFirstUncompletedChunk(myUtility.getChunk()));
                    bitTorrentSet.getStats().changeUtility(myUtility.getChunk() - myUtility.getOffset(),
                            myUtility, storage.getBitField().pieceFieldSize(),
                            storage.getBitField());

                    logger.trace(compName + "print infos");
                    logger.info(compName + storage.getBitField().getHumanReadable2());

                } catch (Exception e) {
                    logger.error(compName + "problem while initializing the torrent file in GVod: {} ", e);
                    //TODO: Does it need to send a message to the upper layer?
//                    delegator.doTrigger(new QuitCompleted(self.getId(), metaInfoAddress), vod);
                    throw new IllegalStateException("Could not create video file. Error: " + e.getMessage());
                }
                startedAtTime = System.currentTimeMillis();
                stoppedReadingAtTime = startedAtTime;
                if (simulation) {
                    spt = new SchedulePeriodicTimeout(readingPeriod, readingPeriod);
                    spt.setTimeoutEvent(new Read(spt));
                    delegator.doTrigger(spt, timer);
                    readTimerId = spt.getTimeoutEvent().getTimeoutId();
                    if (init.isPlay()) {
                        play();
                    }
                }
            }

            // update % in Swing GUI
            ActiveTorrents.updatePercentage(videoName, storage.percent());
        }
    };

    private void cancelPeriodicTimer(TimeoutId timerId) {
        if (timerId != null) {
            CancelPeriodicTimeout cPT = new CancelPeriodicTimeout(timerId);
            delegator.doTrigger(cPT, timer);
        }
    }
    /**
     * Handle a rebootstrap response to rejoin a GVod network using a set of
     * introducer nodes provided in the Join event.
     */
    Handler<RebootstrapResponse> handleRebootstrapResponse = new Handler<RebootstrapResponse>() {
        @Override
        public void handle(RebootstrapResponse event) {

            List<VodDescriptor> insiders = event.getVodInsiders();
            rebootstrap = false;
            logger.debug(compName + "handle rebootstrap response {}", insiders.size());
            if (insiders.size() > 0) {
                List<VodDescriptor> descriptors = event.getVodInsiders();
                if (!seeder) {
                    for (VodDescriptor entry : descriptors) {
                        if (!self.getAddress().equals(entry.getVodAddress())) {
                            triggerConnectRequest(entry, false);
                        }
                    }
                }
            }
        }
    };
    Handler<CroupierSample> handleCroupierSample = new Handler<CroupierSample>() {
        @Override
        public void handle(CroupierSample event) {

            List<VodDescriptor> descriptors = event.getNodes();
            logger.debug(compName + "handle UpdatedDescriptors response {}", descriptors.size());
            if (descriptors.size() > 0) {
                croupierSet.clear();
                StringBuilder sb = new StringBuilder();
                for (VodDescriptor entry : descriptors) {
                    if (!self.getAddress().equals(entry.getVodAddress())) {
                        croupierSet.add(entry);
                        sb.append(entry.getId()).append(", ");
                    }
                }
                logger.info(compName + "Croupier just sent us descriptors: {}", sb.toString());

                // Connect to returned nodes immediately if no neighbours and not seeding.
                UtilityVod utility = (UtilityVod) self.getUtility();
                if (bitTorrentSet.size() == 0 && upperSet.size() == 0
                        && utility.isSeeder() == false) {
                    updateSetsAndConnect();
                }
            }
        }
    };
    /**
     * handle called each round to choke, unchoke
     */
    Handler<InitiateMembershipSearch> handleInitiateMembershipSearch = new Handler<InitiateMembershipSearch>() {
        @Override
        public void handle(InitiateMembershipSearch event) {
            logger.trace(compName + "initiateshuffle");
            time++;

            UtilityVod utility = (UtilityVod) self.getUtility();

            // stats for experiment
            if (time % 30 == 0) {
                if (utility.getPiece() < 0) {
                    utilityAfterTime.put(time, utilityAfterTime.get(time - 30));
                } else {
                    utilityAfterTime.put(time, utility.getPiece());
                }
            }

            /*
             * history of the number of pieces still to download
             * to evaluate the speed at which the node is downloading
             */
            if (time % 10 == 0 && rest != null) {
                rest.put(time, storage.needed());
            }
            /*
             * cleaning the history of old values
             */
            List<Integer> toRemove = new ArrayList<Integer>();
            for (int t : rest.keySet()) {
                if (time - t > 30) {
                    toRemove.add(t);
                }
            }
            for (int t : toRemove) {
                rest.remove(t);
            }

            bitTorrentSet.incrementDescriptorAges();
            upperSet.incrementDescriptorAges();
            lowerSet.incrementDescriptorAges();

            Set<VodAddress> toChokeUnder = new HashSet<VodAddress>();
            Set<VodAddress> toChoke = new HashSet<VodAddress>();

            /*
             * every NUM_CYCLES_QUERY_GRANDCHILDREN cycles we ask the sending 
             * rate of our children to our grandchildren
             */
            int count = 0;

            /*
             * optimistic unchoke
             */
            if (choked.size() > 0 && time % (VodConfig.NUM_CYCLES_QUERY_GRANDCHILDREN * 3) == 0
                    && bitTorrentSet.size() + lowerSet.size() < bitTorrentSet.getMaxSize()) {
                // Choke similar set
                // TODO: Should I not unchoke based on download stats for nodes?
                int i = random.nextInt(choked.size());
                toChoke.remove(choked.get(i));
                choked.remove(i);
            }
            if (chokedUnder.size() > 0 && time % (VodConfig.NUM_CYCLES_QUERY_GRANDCHILDREN * 3) == 0
                    && ((!seeder
                    && lowerSet.size() + bitTorrentSet.size() < lowerSet.getMaxSize())
                    || (seeder && (lowerSet.size() < lowerSet.getMaxSize()
                    && bitTorrentSet.size() < bitTorrentSet.getMaxSize())))) {
                // Choke below set
                if (count == 0) {
                    count = 1;
                }
                for (int j = 0; j < count; j++) {
                    int i = random.nextInt(chokedUnder.size());
                    logger.debug(compName + "SEEDER OPTIMISTIC UNCHOKE: {}",
                            chokedUnder.get(i).getPeerAddress().getId());
                    toChokeUnder.remove(chokedUnder.get(i));
                    chokedUnder.remove(i);
                }

            }

            for (VodAddress add : toChokeUnder) {
                bitTorrentSet.remove(add);
                lowerSet.remove(add);
                // TODO: Should I keep the NAT table entry open to enable
                // quick connection again after disconnect?
                triggerDisconnectRequest(add, false);
            }
            for (VodAddress add : toChoke) {
                bitTorrentSet.remove(add);
                triggerDisconnectRequest(add, false);
            }

            if (storage.getBitField().getFirstUncompletedPiece()
                    >= storage.getBitField().pieceFieldSize() && !finished && !seeder) {
                finishedDownloading();
            }
            startDownload();

            updateSetsAndConnect();

            SummaryStatistics windowStats = new SummaryStatistics();
            for (VodDescriptor node : bitTorrentSet.getAll()) {
                windowStats.addValue(node.getWindow().getSize());
            }
            for (VodDescriptor node : lowerSet.getAll()) {
                windowStats.addValue(node.getWindow().getSize());
            }
            if (windowStats.getMean() / VodConfig.LB_MAX_SEGMENT_SIZE < pipeSize) {
                delegator.doTrigger(new SlowBackground(self.getOverlayId()), vod);
            } else if (windowStats.getMean() / VodConfig.LB_MAX_SEGMENT_SIZE > pipeSize * 2) {
                delegator.doTrigger(new SpeedBackground(self.getOverlayId()), vod);
            }
        }
    };

    private void cleanupPartialPieces() {
        Collection<PieceInTransit> piecesInTransit = partialPieces.values();
        List<PieceInTransit> stalePieces = new ArrayList<PieceInTransit>();

        for (PieceInTransit p : piecesInTransit) {
            if (p.isStalePiece(VodConfig.DATA_REQUEST_TIMEOUT)) {
                stalePieces.add(p);
            }
        }
        List<Integer> staleKeys = new ArrayList<Integer>();

        for (Entry<Integer, PieceInTransit> entry : partialPieces.entrySet()) {
            for (PieceInTransit p : stalePieces) {
                if (p.equals(entry.getValue())) {
                    staleKeys.add(entry.getKey());
                }
            }
        }

        for (Integer k : staleKeys) {
            partialPieces.remove(k);
        }

    }

    /**
     * start to download pieces from any peer in the neighborhood where we have
     * space in our pipeline for it
     */
    private void startDownload() {
        logger.trace(compName + "Starting download.");
        if (storage.complete()) {
            logger.info(compName + "storage finished. Not downloading.");
            return;
        }

        if (storage.getBitField().getFirstUncompletedPiece() >= storage.getBitField().pieceFieldSize()) {
            logger.trace(compName + "storage finished or first uncompleted piece > lastPiece {}/{}",
                    storage.getBitField().getFirstUncompletedPiece(),
                    storage.getBitField().pieceFieldSize());
            return;
        }
        boolean noDownloading = true;

        for (VodAddress add : upperSet.getAllAddress()) {
            VodDescriptor peer = store.getVodDescriptorFromVodAddress(add);
            logger.info(compName + "Downloading: Pipeline size {} . Max size {}",
                    peer.getRequestPipeline().size(), peer.getPipeSize());

            peer.cleanupPipeline();
//                cleanupPartialPieces();
            if (peer.getRequestPipeline().size() < peer.getPipeSize()) {
                startDownloadingPieceFrom(add,
                        peer.getPipeSize() - peer.getRequestPipeline().size(),
                        null, 0);
                noDownloading = false;
            } else {
                peer.cleanupPipeline();
            }
//                if (numRoundsNoPiecesDownloaded > 3) {
//                    peer.clearPipeline();
//                }
        }
        List<VodAddress> list = bitTorrentSet.getAllAddress();
        while (list.size() > 0) {
            int i = random.nextInt(list.size());
            VodAddress add = list.get(i);
            VodDescriptor peer = store.getVodDescriptorFromVodAddress(add);
            logger.info(compName + "Pipeline size {} . Max size {}",
                    peer.getRequestPipeline().size(), peer.getPipeSize());

            peer.cleanupPipeline();
//            if (peer.getRequestPipeline().size() < peer.getPipeSize()) {
            startDownloadingPieceFrom(add,
                    peer.getPipeSize() - peer.getRequestPipeline().size(),
                    null, 0);
            noDownloading = false;
//            }
            list.remove(add);
//                if (numRoundsNoPiecesDownloaded > 3) {
//                    peer.clearPipeline();
//                }
        }
        if (noDownloading) {
            logger.warn(compName + "No downloading. SimilarSet size {}. UpperSet size {}",
                    bitTorrentSet.size(), upperSet.size());
            if (numRoundsNoPiecesDownloaded > 3) {
                numRoundsNoPiecesDownloaded = 0;
            } else {
                numRoundsNoPiecesDownloaded++;
            }
        }

    }

    /**
     * send a rebootstrap request to the bootstrap server
     */
    private void rebootstrap() {
        if (rebootstrap) {
            return;
        }
        logger.info(compName + "rebootstrap");
        int videoId = ActiveTorrents.calculateVideoId(videoName);
        UtilityVod utility = (UtilityVod) self.getUtility();
        delegator.doTrigger(new Rebootstrap(self.getId(), videoId, utility), vod);
        rebootstrap = true;
    }
    /**
     * handle connect request containing the useful information about the node
     * wanting to connect : utility, id, children (if in below set). If the node
     * is already connected, return success.
     */
    Handler<ConnectMsg.Request> handleConnectRequest = new Handler<ConnectMsg.Request>() {
        @Override
        public void handle(ConnectMsg.Request event) {
            logger.debug(compName + "ConnectRequest from {}", event.getVodSource().getId());
            /*
             * if the node receive a request before it received a join message
             * can happen if a node disconnects and reconnects with the same id
             */
            if (self == null) {
                return;
            }
            msgReceived(event.getVodSource());

            ConnectMsg.Response response;
            /*
             * if the node asking to connect was choked we ignore the request
             */
            if (chokedUnder.contains(event.getVodSource())
                    || choked.contains(event.getVodSource())) {
                logger.info(compName + "choking {}", event.getVodSource().getId());
                return;
            }

            UtilityVod myUtility = (UtilityVod) self.getUtility();
            /*
             * check in which set the node should be in and if there is space for it
             * send the answer corresponding to the resultant set.
             * If node's utility is in the UtilitySet range, join the utilitySet.
             * If lower, join belowSet, if higher, join upperSet.
             * utility < 0 => seeder
             */
            List<VodAddress> toBeRemoved = new ArrayList<VodAddress>();
            if ((myUtility.isSeeder() && event.isToUtilitySet())
                    || !myUtility.notInBittorrentSet(event.getUtility())) {
                logger.debug(compName + "UTILITY SET -  received connectRequest from {}",
                        event.getVodSource().getId());
                // UTILITY SET
                upperSet.remove(event.getVodSource());
                lowerSet.remove(event.getVodSource());
                toBeRemoved = bitTorrentSet.add(event.getVodSource(), event.getUtility(),
                        commWinSize, pipeSize, maxWindowSize,
                        event.getMtu());
                if (!toBeRemoved.contains(event.getVodSource())) {
                    logger.debug(compName + "accepted to connect to {} with utility ",
                            event.getVodSource().getId());
                    if (myUtility.getChunk() >= 0) {
                        response = new ConnectMsg.Response(self.getAddress(), event.getVodSource(),
                                event.getTimeoutId(),
                                ConnectMsg.ResponseType.OK,
                                myUtility, storage.getBitField().getChunkfield(),
                                storage.getBitField().getAvailablePieces(myUtility),
                                true, mtu);
                        // TODO - should never reach here, as this can't be a seed,
                        // as utility.getChunk() > 0 ???
                        if (seeder) {
                            logger.warn(compName + "SHOULDNT REACH HERE!");
                            store.getVodDescriptorFromVodAddress(event.getVodSource()).setUploadRate(0);
                        }
                    } else {
                        response = new ConnectMsg.Response(self.getAddress(), event.getVodSource(),
                                event.getTimeoutId(), ConnectMsg.ResponseType.OK,
                                myUtility, null, null, true, mtu);
                        if (seeder) {
                            store.getVodDescriptorFromVodAddress(event.getVodSource()).setUploadRate(0);
                        }
                    }
                    logger.trace(compName + "send {} response to {}", ConnectMsg.ResponseType.OK,
                            event.getVodSource().getId());
                } else {
                    logger.debug(compName + "removed {} from neighbourhood (handleConnectRequest1)",
                            event.getVodSource().getId());
                    store.suppress(event.getVodSource());
                    toBeRemoved.remove(event.getVodSource());
                    response = new ConnectMsg.Response(self.getAddress(), event.getVodSource(),
                            event.getTimeoutId(), ConnectMsg.ResponseType.FULL,
                            myUtility, null, null, true, mtu);
                    logger.debug(compName + "send {} response to {}", ConnectMsg.ResponseType.FULL,
                            event.getVodSource().getId());
                }
            } else if (event.getUtility().getChunk() <= myUtility.getChunk() - myUtility.getOffset()
                    || (myUtility.isSeeder() && !event.isToUtilitySet())) {
                // BELOW SET or I am a SUPER-PEER and request is not to my utilitySet
                logger.debug(compName + "upperset {} ConnectRequest", event.getVodSource().getId());

                upperSet.remove(event.getVodSource());
                bitTorrentSet.remove(event.getVodSource());
                if (myUtility.isSeeder()) {
                    // +10 is to get the chunk piece from -10 (seeder) up to 0.
//                    UtilityVod uti = new UtilityVod(
                    //                            storage.getBitField().getChunkFieldSize() + 10
//                            storage.getBitField().getChunkFieldSize() - VodConfig.SEEDER_UTILITY_VALUE,
//                             VodConfig.SEEDER_UTILITY_VALUE,
//                            storage.getBitField().pieceFieldSize(), offset);
                    toBeRemoved = lowerSet.add(event.getVodSource(), event.getUtility(),
                            //                            uti, 
                            (UtilityVod) self.getUtility(),
                            commWinSize, pipeSize, maxWindowSize, event.getMtu());
                } else {
                    toBeRemoved = lowerSet.add(event.getVodSource(), event.getUtility(),
                            myUtility, commWinSize, pipeSize, maxWindowSize, event.getMtu());
                }
                if (!toBeRemoved.contains(event.getVodSource())) {
                    if (myUtility.getChunk() >= 0) {
                        response = new ConnectMsg.Response(self.getAddress(), event.getVodSource(),
                                event.getTimeoutId(),
                                ConnectMsg.ResponseType.OK,
                                myUtility, storage.getBitField().getChunkfield(),
                                storage.getBitField().getAvailablePieces(myUtility),
                                false, mtu);
                        VodDescriptor nodeDescriptor = store.getVodDescriptorFromVodAddress(event.getVodSource());
                        if (nodeDescriptor != null) {
                            nodeDescriptor.setUploadRate(0);
                        } else {
                            logger.warn(compName + "Node was not in Neighbourhood when trying to update uploadRate for node");
                            store.add(event.getVodSource(), event.getUtility(),
                                    false, commWinSize, pipeSize, maxWindowSize,
                                    event.getMtu());
                        }
                        logger.debug(compName + "send {} response to {}", ConnectMsg.ResponseType.OK, event.getVodSource().getId());
                    } else {
                        response = new ConnectMsg.Response(self.getAddress(), event.getVodSource(),
                                event.getTimeoutId(), ConnectMsg.ResponseType.OK,
                                myUtility, null, null, false, mtu);
                        VodDescriptor nodeDesc = store.getVodDescriptorFromVodAddress(event.getVodSource());
                        if (nodeDesc != null) {
                            nodeDesc.setUploadRate(0);
                        } else {
                            logger.warn(compName + "Node was not in Neighbourhood when trying to update uploadRate for node");
                            store.add(event.getVodSource(), event.getUtility(),
                                    false, commWinSize, pipeSize, maxWindowSize,
                                    event.getMtu());
                        }
                        logger.debug(compName + "Connect response sent to {}", event.getVodSource().getId());
                    }
                } else {
                    logger.info(compName + "ConnectResponse FULL. removed {} from neighbourhood (handleConnectRequest2)",
                            event.getVodSource().getId());
                    store.suppress(event.getVodSource());
                    toBeRemoved.remove(event.getVodSource());
                    response = new ConnectMsg.Response(self.getAddress(), event.getVodSource(),
                            event.getTimeoutId(), ConnectMsg.ResponseType.FULL,
                            myUtility, null, null, false, mtu);
                    logger.debug(compName + "send {} response to {}", ConnectMsg.ResponseType.FULL,
                            event.getVodSource().getId());
                }
            } else {
                if (lowerSet != null) {
                    lowerSet.remove(event.getVodSource());
                    upperSet.remove(event.getVodSource());
                    bitTorrentSet.remove(event.getVodSource());
                }
                logger.info(compName + "Connect BAD_UTILITY. remove {} handleConnectRequest2", event.getVodSource().getId());
                response = new ConnectMsg.Response(self.getAddress(), event.getVodSource(),
                        event.getTimeoutId(), ConnectMsg.ResponseType.BAD_UTILITY,
                        myUtility, null, null, false, mtu);
                logger.warn(compName + "send {} response to {} my utility (" + myUtility.getPiece()
                        + ";" + myUtility.getChunk() + ") its utility (" + event.getUtility().getPiece()
                        + ";" + event.getUtility().getChunk() + ")", ConnectMsg.ResponseType.BAD_UTILITY,
                        event.getVodSource().getId());
            }

            for (VodAddress add : toBeRemoved) {
                triggerDisconnectRequest(add, false);
            }
            logger.trace(compName + "trigger ConnectResponse " + response.getResponse() + " to "
                    + response.getDestination().getId());
            delegator.doTrigger(response, network);

        }
    };

    /**
     * used periodically for one of the other sets And send a connect request to
     * the interesting nodes
     */
    public void updateSetsAndConnect() {
        logger.info(compName + "Updating our upper/bittorrent sets with {} nodes from croupier.",
                croupierSet.size());

        List<VodDescriptor> sendConnect;
        UtilityVod utility = (UtilityVod) self.getUtility();
        List<VodDescriptor> toRemove = new ArrayList<VodDescriptor>();
        for (VodDescriptor des : croupierSet) {
            if (choked.contains(des.getVodAddress()) || chokedUnder.contains(des.getVodAddress())
                    || bitTorrentSet.contains(des.getVodAddress())) {
                toRemove.add(des);
            }
            if (des.getVodAddress().equals(self)) {
                logger.debug(compName + "REMOVED SELF FROM NEIGHBOURS");
                toRemove.add(des);
            }
        }
        for (VodDescriptor des : toRemove) {
            logger.info(compName + "Removing croupier descriptor: {}", des.toString());
            croupierSet.remove(des);
        }

        sendConnect = upperSet.updateAll(croupierSet, utility);
        List<VodDescriptor> sentConnectToUpper = new ArrayList<VodDescriptor>();
        if (!seeder) {
            for (VodDescriptor node : sendConnect) {
                logger.info(compName + "Connecting to neighbour {} with utility {}",
                        node.getId(), node.getUtility().getValue());
                triggerConnectRequest(node, false);
                if (node.getUtility().getValue() >= VodConfig.SEEDER_UTILITY_VALUE) {
                    sentConnectToUpper.add(node);
                }
            }
        }
        toRemove = new ArrayList<VodDescriptor>();
        toRemove.addAll(sentConnectToUpper);
        for (VodDescriptor des : gradientSet) {
            if (choked.contains(des.getVodAddress()) || chokedUnder.contains(des.getVodAddress())
                    || upperSet.contains(des.getVodAddress())) {
                toRemove.add(des);
                logger.info(compName + "choking {},removing..", des);
            }
        }
        for (VodDescriptor des : toRemove) {
            gradientSet.remove(des);
        }
        sendConnect = bitTorrentSet.updateAll(gradientSet, utility);
        if (!seeder) {
            for (VodDescriptor node : sendConnect) {
                if (!choked.contains(node.getVodAddress())) {
                    triggerConnectRequest(node, true);
                }
            }
        }

        updatefingers();
    }

    /**
     * use the random set to update the finger that we use to restart faster
     * after a jump
     */
    private void updatefingers() {
        for (VodDescriptor node : croupierSet) {
            UtilityVod u = (UtilityVod) node.getUtility();
            if (fingers.get(u.getChunk()) == null) {
                fingers.put(u.getChunk(), node);
            } else if (fingers.get(u.getChunk()).getAge() > node.getAge()) {
                fingers.put(u.getChunk(), node);
            }
        }
    }
    /**
     * handle the response to a connect request
     */
    Handler<ConnectMsg.Response> handleConnectResponse = new Handler<ConnectMsg.Response>() {
        @Override
        public void handle(ConnectMsg.Response event) {
            msgReceived(event.getVodSource());
            ongoingConnectRequests.remove(event.getVodSource().getId());

            UtilityVod utility = (UtilityVod) self.getUtility();
            TimeoutId timeoutId = event.getTimeoutId();
            List<VodAddress> toBeRemoved = new ArrayList<VodAddress>();

            logger.trace(compName + "received connectResponse {} from {} myUtility ("
                    + utility.getChunk() + ";" + utility.getPiece() + ") its ("
                    + event.getUtility().getChunk() + ";"
                    + event.getUtility().getPiece()
                    + ")", event.getResponse(), event.getVodSource().getId());
            if (delegator.doCancelRetry(timeoutId)) {
                logger.trace(compName + "Cancelled connectRequest timeout : " + timeoutId);

                if (event.getVodSource().equals(self)) {
                    logger.warn(compName + "REMOVED SELF FROM CONNECTED NEIGHBOURS");
                    return;
                }

                /*
                 * we update the utility in the random view in case we didn't have the latest 
                 * utility value
                 */
//                RandomView.updateUtility(self, event.getVodSource(), event.getUtility());
                // TODO trigger an event to Gradient containing a VodAddress and a new
                // utility value. Used to update utility value and age in gradientSet.
                switch (event.getResponse()) {
                    case OK:
                        if ((event.getUtility().isSeeder() && event.isToUtilitySet())
                                || (event.getUtility().getPiece() < utility.getPiece() + utility.getPieceOffset()
                                && event.getUtility().getPiece() > utility.getPiece() - utility.getPieceOffset())) {
                            upperSet.remove(event.getVodSource());
                            lowerSet.remove(event.getVodSource());
                            logger.trace(compName + "received connectResponse from {}", event.getVodSource().getId());
                            toBeRemoved = bitTorrentSet.add(event.getVodSource(),
                                    event.getUtility(),
                                    commWinSize, pipeSize, maxWindowSize,
                                    event.getMtu());
                            logger.trace(compName + "after connect from {} {} my utility "
                                    + utility.getChunk() + " its utility " + event.getUtility().getChunk(),
                                    event.getVodSource().getId(), event.getAvailablePieces());
                            bitTorrentSet.updatePeerInfo(event.getVodSource(),
                                    event.getUtility(),
                                    event.getAvailableChunks(),
                                    event.getAvailablePieces());
                        } else if (event.getUtility().getChunk() >= utility.getChunk() + utility.getOffset()
                                || (event.getUtility().isSeeder() && !event.isToUtilitySet())) {
                            logger.trace(compName + "remove from sets {} handleConnectResponse",
                                    event.getVodSource().getId());
                            bitTorrentSet.remove(event.getVodSource());
                            lowerSet.remove(event.getVodSource());
                            toBeRemoved = upperSet.add(event.getVodSource(), event.getUtility(), utility,
                                    commWinSize, pipeSize, maxWindowSize, event.getMtu());
                        } else {
                            logger.trace(compName + "received connectResponse2 from {}", event.getVodSource().getId());
                            store.add(event.getVodSource(), event.getUtility(), false,
                                    commWinSize, pipeSize, maxWindowSize, event.getMtu());
                            triggerDisconnectRequest(event.getVodSource(), false);
                        }
                        break;
                    case FULL:
                        // we just ignore the nodeAddr, next time we will be more luky
                        // see if there is optimisations that can be done
                        break;
                    case BAD_UTILITY:
                        // we just update the node utility in our view and ignore
                        // the nodeAddr, next time we will be more lucky
                        logger.warn(compName + "received BAD_Utility response from {} my utility (" + utility.getPiece()
                                + ";" + utility.getChunk() + ") its utility (" + event.getUtility().getPiece()
                                + ";" + event.getUtility().getChunk() + ")", event.getVodSource().getId());
                        VodDescriptor nodeSrc = store.getVodDescriptorFromVodAddress(event.getVodSource());
                        if (nodeSrc != null) {
                            nodeSrc.setUtility(event.getUtility());
                        }
                        break;
                }
            }
            for (VodAddress add : toBeRemoved) {
                triggerDisconnectRequest(add, false);
            }
            startDownload();
        }
    };

    private void msgReceived(VodAddress peer) {
        if (suspected.containsKey(peer)) {
            suspected.remove(peer);
        }
    }

    private void responseTimedOut(VodAddress peer, boolean supress, boolean force) {
        if (suspected == null) {
            logger.warn(compName + "Suspected was null.");
        }
        if (force || suspected.containsKey(peer)) {

            if (force || (suspected.get(peer) + VodConfig.SUSPECTED_DEAD_TIMEOUT_MS
                    > System.currentTimeMillis())) {
//                removeFromUploaders(peer);
                logger.warn(compName + "Removing peer due to response timed out: " + peer.getId());
                bitTorrentSet.remove(peer);
                upperSet.remove(peer);
                lowerSet.remove(peer);
                if (supress) {
                    store.suppress(peer);
                }
                if (self.getNat().getFilteringPolicy() == Nat.FilteringPolicy.PORT_DEPENDENT) {
                    // send msg to NatTraverser to delete the port
                }
            }
        } else {
            suspected.put(peer, System.currentTimeMillis());
        }
    }
    /**
     * handle a connect timeout when a node is too slow to answer to a connect
     * request
     */
    Handler<ConnectMsg.RequestTimeout> handleConnectTimeout = new Handler<ConnectMsg.RequestTimeout>() {
        @Override
        public void handle(ConnectMsg.RequestTimeout event) {
            ongoingConnectRequests.remove(event.getVodAddress().getId());
            logger.trace(compName + "connectimeout with {}", event.getVodAddress().getId());

//            TimeoutId timeoutId = event.getTimeoutId();
//
//            if (outstandingConnectionRequests.containsKey(timeoutId)) {
//                outstandingConnectionRequests.remove(timeoutId);
//                if (event.getNbTry() < 3 && !seeder) {
//                    logger.trace(compName + "connectimeout with {}", event.getVodDescriptor().getVodAddress().getId());
//                    triggerConnectRequest(event.getVodDescriptor(),
//                            (event.getNbTry() + 1) * connectionTimeout,
//                            event.getNbTry() + 1,
//                            event.isToUtilitySet());
//                }
//            }
        }
    };
    /**
     * handle a disconnect request, disconnect and answer when it's done
     */
    Handler<DisconnectMsg.Request> handleDisconnectRequest = new Handler<DisconnectMsg.Request>() {
        @Override
        public void handle(DisconnectMsg.Request event) {
            logger.debug(compName + "DisconnectMsg.Request from {}.", event.getSource().getId());
            if (self == null) {
                return;
            }
            msgReceived(event.getVodSource());

//            removeFromUploaders(event.getGVodSource());
            logger.trace(compName + "remove {} handleDisConnectRequest", event.getVodSource().getId());
            bitTorrentSet.remove(event.getVodSource());
            int ref = store.getRef(event.getVodSource());
            upperSet.remove(event.getVodSource());
            lowerSet.remove(event.getVodSource());
            logger.trace(compName + "remove {} handleDisConnectRequest2", event.getVodSource().getId());
            bitTorrentSet.remove(event.getVodSource());
            logger.trace(compName + "removed {} from neighbourhood (handleDisconnectRequest)", event.getVodSource().getId());
            store.suppress(event.getVodSource());
            ref = 0;
            logger.trace(compName + "trigger disconnectResponse");
            delegator.doTrigger(new DisconnectMsg.Response(self.getAddress(), event.getVodSource(),
                    event.getTimeoutId(), ref), network);

            trigger(new DisconnectNeighbour(event.getVodSource().getId()), natTraverserPort);
        }
    };
    /**
     * handle the response to a disconnect request and finish to disconnect
     */
    Handler<DisconnectMsg.Response> handleDisconnectResponse = new Handler<DisconnectMsg.Response>() {
        @Override
        public void handle(DisconnectMsg.Response event) {

            logger.debug(compName + "handle disconnectResponse from {}", event.getVodSource().getId());
            if (self == null) {
                return;
            }
            msgReceived(event.getVodSource());

            if (delegator.doCancelRetry(event.getTimeoutId())) {
                logger.trace(compName + "cancel timeOut {} disconnectResponse", event.getTimeoutId());

                if (event.getRef() == 0 && store.getRef(event.getVodSource()) == 0) {
//                    removeFromUploaders(event.getGVodSource());
                    upperSet.remove(event.getVodSource());
                    lowerSet.remove(event.getVodSource());
                    logger.trace(compName + "remove {} handleDisConnectResponse", event.getVodSource().getId());
                    bitTorrentSet.remove(event.getVodSource());
                    logger.trace(compName + "remove {} from neighbourhood (handleDisconnectResponse)", event.getVodSource().getId());
                    store.suppress(event.getVodSource());
                }
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
        public void handle(DisconnectMsg.RequestTimeout event) {
            logger.trace(compName + "disconnectTimeout");

            if (delegator.doCancelRetry(event.getTimeoutId())) {
                logger.trace(compName + "remove {} handleDisConnectTimeout", event.getPeer().getId());
                logger.trace(compName + "supr {} from neighbourhood (handleDisconnectTimeout)", event.getPeer().getId());

                responseTimedOut(event.getPeer(), true, true);
                bitTorrentSet.remove(event.getPeer());
                upperSet.remove(event.getPeer());
                lowerSet.remove(event.getPeer());
                store.suppress(event.getPeer());
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
            logger.trace(compName + "quit");
            if (store != null && store.getNeighbours() != null) {
                for (VodAddress destination : store.getNeighbours().keySet()) {
                    logger.debug(compName + "trigger leaveMessage");
                    delegator.doTrigger(new LeaveMsg(self.getAddress(), destination), network);
                }
            }
            delegator.doTrigger(new QuitCompleted(event.getOverlayId(), torrentFileAddress), vod);

        }
    };
    /**
     * handle a leave message, remove the living node from the different views
     */
    Handler<LeaveMsg> handleLeave = new Handler<LeaveMsg>() {
        @Override
        public void handle(LeaveMsg event) {

            logger.trace(compName + "remove {} handleleave", event.getVodSource().getId());
            bitTorrentSet.remove(event.getVodSource());
            upperSet.remove(event.getVodSource());
            lowerSet.remove(event.getVodSource());
            logger.debug(compName + "supr {} from neighbourhood (handleLeave)", event.getVodSource().getId());
            store.suppress(event.getVodSource());

        }
    };
    /**
     * handle dataoffer message containing the information on the data that the
     * sending node can share
     */
    Handler<DataOfferMsg> handleDataOffer = new Handler<DataOfferMsg>() {
        @Override
        public void handle(DataOfferMsg event) {
            UtilityVod utility = (UtilityVod) self.getUtility();
            logger.debug(compName + "Dataoffer from " + event.getVodSource().getId() + " my utility :" + utility.getChunk()
                    + " its utility : " + event.getUtility().getChunk());
            if (self == null) {
                return;
            }
            msgReceived(event.getVodSource());

            if (!bitTorrentSet.contains(event.getVodSource())) {
                logger.trace(compName + "Dataoffer from node not in my utility set");
                upperSet.remove(event.getVodSource());
                lowerSet.remove(event.getVodSource());
                triggerDisconnectRequest(event.getVodSource(), false);
                return;
            }
            VodDescriptor node = bitTorrentSet.updatePeerInfo(event.getVodSource(),
                    event.getUtility(), event.getAvailableChunks(),
                    event.getAvailablePieces());
            if (node != null) {
                triggerDisconnectRequest(node.getVodAddress(), false);
            }

        }
    };
    /**
     * handle initiatedataoffer message coming from the timer the node sends a
     * dataoffer message to all the nodes in its bittorrent set
     */
    Handler<InitiateDataOffer> handleInitiateDataOffer = new Handler<InitiateDataOffer>() {
        @Override
        public void handle(InitiateDataOffer event) {

            logger.trace(compName + "handle initiate dataoffer");
            List<VodAddress> toDisconnect = bitTorrentSet.cleanup(VodConfig.DATA_OFFER_PERIOD);
            for (VodAddress add : toDisconnect) {
                triggerDisconnectRequest(add, false);
                // TODO what happens if the node doesn't receive the DisconnectRequest?
                // Our neighbour still thinks we're a neighbour, but we dont think they are?
            }

            triggerDataOffer();
        }
    };

    private void triggerDataOffer() {
        DataOfferMsg dataOffer;
        UtilityVod utility = (UtilityVod) self.getUtility();
        for (VodDescriptor desc : bitTorrentSet.getAll()) {
            desc.incrementAndGetAge();
            if (utility.isSeeder()) { // seed
                dataOffer = new DataOfferMsg(self.getAddress(), desc.getVodAddress(), utility,
                        storage.getBitField().getChunkfield(), null);
            } else {
                dataOffer = new DataOfferMsg(self.getAddress(), desc.getVodAddress(), utility,
                        storage.getBitField().getChunkfield(),
                        storage.getBitField().getAvailablePieces(utility));
            }
            logger.trace(compName + "message utility {}, my utility {}", dataOffer.getUtility().getChunk(),
                    utility.getChunk());
            delegator.doTrigger(dataOffer, network);
        }
    }
    /**
     * Handle a request to send a subpiece send the subpiece if all the
     * conditions to do it are fulfilled
     */
    Handler<DataMsg.Request> handleDataMsgRequest = new Handler<DataMsg.Request>() {
        @Override
        public void handle(DataMsg.Request event) {
            logger.trace(compName + videoName + ": DataMsg.Request from " + event.getVodSource().getId());
            msgReceived(event.getVodSource());

            try {
                VodAddress peer = event.getVodSource();
                int piece = event.getPiece();
                TimeoutId requestId = event.getTimeoutId();
                TimeoutId ackId = event.getAckId();
                if (ackId.getId() == 0) {
                    ackId = null;
                }

                logger.debug(compName + "Got REQUEST({}, {}) from {}", new Object[]{piece,
                    event.getSubpieceOffset(), peer.getId()});
                /*
                 * if free rider just ignore the message
                 */
                if (freeRider) {
                    logger.debug(compName + "Freerider: ignoring data request");
                    return;
                }
                byte[] subpiece = storage.getSubpiece(event.getSubpieceOffset());
                /*
                 * answer only if the node is a neighbor, the pipe is not full and we have the piece
                 */
                if (!(bitTorrentSet.contains(event.getVodSource()) || lowerSet.contains(event.getVodSource()))) {
                    logger.warn(compName + "Node requesting piece is not a neighbour {}. Piece Refused.",
                            event.getVodSource().getId());
                    return;
                }
                CommunicationWindow comWin = store.getVodDescriptorFromVodAddress(peer).getWindow();
                if (updateCommsWindow(comWin, ackId, event.getDelay()) == false) {
                    if (ackId == null) {
                        logger.warn(compName + "ACK null to update comWin");
                    } else {
                        logger.info(compName + "Missing ACK {} to update comWin", ackId.toString());
                    }
                }

                if (subpiece != null) {
                    ScheduleTimeout st = new ScheduleTimeout(ackTimeout);
                    st.setTimeoutEvent(new DataMsg.AckTimeout(st, peer, self.getOverlayId()));
                    TimeoutId newAckId = st.getTimeoutEvent().getTimeoutId();
                    DataMsg.Response pieceMessage //                            = new DataMsg.Response(selfNoParents.getAddress(),
                            = new DataMsg.Response(self.getAddress(),
                            peer, requestId, newAckId,
                            subpiece,
                            event.getSubpieceOffset(), piece,
                            comWin.getSize(), System.currentTimeMillis());
                    if (comWin.addMessage(newAckId, pieceMessage.getSize())) {
                        logger.debug(compName + "DataExchangeResponse for piece " + piece + "("
                                + event.getSubpieceOffset() + ")");
                        delegator.doTrigger(pieceMessage, network);
                        outstandingAck.put(newAckId, pieceMessage.getSize());
                        delegator.doTrigger(st, timer);
                    } else {
                        logger.info(compName + "DataExchangeResponse SATURATED");
                        delegator.doTrigger(new DataMsg.Saturated(self.getAddress(), peer, piece,
                                comWin.getSize()), network);
                    }
                } else {
                    logger.info(compName + "DataExchangeResponse FORWARDED");
                    triggerForwardDataRequest(event);
                }
            } catch (Exception e) {
                logger.warn(compName + e.getMessage()
                        + ": impossible to access the requested subpiece :  {} ",
                        event.getPiece());

            }
            logger.trace(compName + "got request end");
        }
    };
    /**
     * handle the response to a piece request
     */
    Handler<DataMsg.Response> handleDataMsgResponse = new Handler<DataMsg.Response>() {
        @Override
        public void handle(DataMsg.Response event) {
            logger.trace(compName + "DataMsg.Response from " + event.getVodSource().getId()
                    + " for subpiece " + event.getSubpieceOffset());
            msgReceived(event.getVodSource());

            // TODO - am I cancelling the right timeoutId here?
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
                VodDescriptor peerInfo = store.getVodDescriptorFromVodAddress(peer);
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
                if (!partialPieces.containsKey(piece)) {
                    logger.debug(compName + "This piece wasn't requested: {}", piece);
                    return;
                }

                logger.debug(compName + "Got Piece/Subpiece({}, {}) from {}", new Object[]{piece,
                    subpieceNb, peer.getId()});

                // mark that we downloaded a block
                PieceInTransit transit = partialPieces.get(piece);

                if (transit == null) {
                    logger.warn(compName + "Piece in transit was null");
                    return;
                }

                int subpieceIndex = event.getSubpieceOffset();
                boolean completedPiece = transit.subpieceReceived(subpieceIndex);
                boolean flag = storage.putSubpiece(event.getSubpieceOffset(), event.getSubpiece());

                peerInfo.getRequestPipeline().remove(new Block(piece, subpieceIndex, 0));

                if (flag) {
                    downloadStats.downloadedFrom(peer, 1);
                }
                if (downloadedFrom.containsKey(event.getVodSource())) {
                    long val = downloadedFrom.get(event.getVodSource()) + 1;
                    downloadedFrom.put(event.getVodSource(), val);
                } else {
                    downloadedFrom.put(event.getVodSource(), (long) 1);
                }
                if (completedPiece) {
                    pieceCompleted(piece, peer);
                    partialPieces.remove(piece);
                    if (buffering.get()
                            && (storage.getBitField().getFirstUncompletedPiece() >= pieceToRead.get() + bufferingWindow
                            || storage.complete()
                            || storage.getBitField().getFirstUncompletedPiece() >= storage.getBitField().pieceFieldSize())) {
                        restartToRead();
                    } else {
                        logger.info(compName + "State: " + buffering.get() + " Buffering: {} > {}",
                                storage.getBitField().getFirstUncompletedPiece(),
                                pieceToRead.get() + bufferingWindow);
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
                        startDownloadingPieceFrom(peer,
                                peerInfo.getPipeSize() - peerInfo.getRequestPipeline().size(),
                                ackId,
                                rtt);
                    } else {
                        // continue downloading a block from the last requested piece
                        int lastRequestedPiece = lastRequestedBlock.getPieceIndex();
                        PieceInTransit latestTransit = partialPieces.get(lastRequestedPiece);

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
                            startDownloadingPieceFrom(peer,
                                    peerInfo.getPipeSize() - peerInfo.getRequestPipeline().size(),
                                    ackId, rtt);
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
                VodDescriptor peerInfo = store.getVodDescriptorFromVodAddress(peer);
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
                    PieceInTransit transit = partialPieces.get(piece);
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

    private boolean updateCommsWindow(CommunicationWindow comWin, TimeoutId ackId, long delay) {
        assert (comWin != null);

        if (ackId == null) {
            return false;
        }
        if (outstandingAck.containsKey(ackId)) {
            logger.debug(compName + "Received ACK {}. Updating CommWindow with delay {} ms.", ackId, delay);
            Integer msgSize = outstandingAck.remove(ackId);
            comWin.update(delay);
            comWin.removeMessage(ackId, msgSize);
            return true;
        }
        return false;
    }

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
        VodDescriptor node = bitTorrentSet.getStats().getRandomNodeWithPiece(event.getPiece(), null);
        if (node == null) {
            node = upperSet.getRandomNode();
        }

        if (node != null) {
            if (!forwarded.containsKey(event.getSubpieceOffset())) {
                forwarded.put(event.getSubpieceOffset(), new ArrayList<VodAddress>());
            }
            forwarded.get(event.getSubpieceOffset()).add(event.getVodSource());

            // no need to store the timeout here
            DataMsg.Request request //                    = new DataMsg.Request(selfNoParents.getAddress(),
                    = new DataMsg.Request(self.getAddress(),
                    node.getVodAddress(),
                    event.getAckId(),
                    event.getPiece(),
                    event.getSubpieceOffset(), 0);
            delegator.doRetry(request, self.getOverlayId());
        } else {
            DataMsg.PieceNotAvailable response = new DataMsg.PieceNotAvailable(self.getAddress(), event.getVodSource(),
                    storage.getBitField().getChunkfield(), utility,
                    event.getPiece(), storage.getBitField().getAvailablePieces(utility));
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
            if (store.contains(peer)) {
                CommunicationWindow comWin = store.getVodDescriptorFromVodAddress(peer).getWindow();
                ScheduleTimeout st = new ScheduleTimeout(ackTimeout);
                st.setTimeoutEvent(new DataMsg.AckTimeout(st, peer, self.getOverlayId()));
                TimeoutId ackId = st.getTimeoutEvent().getTimeoutId();
                if (comWin.addMessage(ackId, VodConfig.LB_MAX_SEGMENT_SIZE)) {
//                    DataMsg.Response response = new DataMsg.Response(selfNoParents.getAddress(), peer, event.getTimeoutId(),
                    DataMsg.Response response = new DataMsg.Response(self.getAddress(), peer, event.getTimeoutId(),
                            ackId, event.getSubpiece(),
                            event.getSubpieceOffset(), event.getPiece(), comWin.getSize(),
                            System.currentTimeMillis());
                    delegator.doTrigger(response, network);
                    outstandingAck.put(ackId, response.getSize());
                    delegator.doTrigger(st, timer);
                } else {
                    delegator.doTrigger(new DataMsg.Saturated(self.getAddress(), peer, event.getPiece(),
                            comWin.getSize()), network);
                }
            }
        }
//        if (storage != null) {
        boolean flag = storage.putSubpiece(event.getSubpieceOffset(), event.getSubpiece());
        if (flag) {
            downloadStats.downloadedFrom(event.getVodSource(), 1);
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
            if (store.contains(peer)) {
                delegator.doTrigger(new DataMsg.Saturated(self.getAddress(), peer, event.getSubpiece() / BitField.NUM_PIECES_PER_CHUNK,
                        store.getVodDescriptorFromVodAddress(peer).getWindow().getSize()), network);
            } else {
                delegator.doTrigger(new DataMsg.Saturated(self.getAddress(), peer, event.getSubpiece() / BitField.NUM_PIECES_PER_CHUNK,
                        commWinSize), network);
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

            CommunicationWindow comWin = store.getVodDescriptorFromVodAddress(event.getVodSource()).getWindow();
            updateCommsWindow(comWin, event.getAckId(), event.getDelay());

            VodDescriptor peer = store.getVodDescriptorFromVodAddress(event.getVodSource());
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
            if (outstandingAck.containsKey(event.getTimeoutId())) {
                logger.debug(compName + "Ack timed out {} to {}", event.getTimeoutId(),
                        event.getPeer().getPeerAddress().getId());
                Integer msgSize = outstandingAck.remove(event.getTimeoutId());
                if (store.contains(event.getPeer())) {
                    CommunicationWindow comWin = store.getVodDescriptorFromVodAddress(event.getPeer()).getWindow();
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
            if (!partialPieces.containsKey(event.getSubpiece() / BitField.NUM_PIECES_PER_CHUNK)) {
                return;
            }
            partialPieces.remove(event.getSubpiece() / BitField.NUM_PIECES_PER_CHUNK);
            if (store.contains(event.getVodSource())) {
                store.getVodDescriptorFromVodAddress(event.getVodSource()).setPipeSize(
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

            if (store != null && store.contains(event.getTarget())) {
                delegator.doTrigger(new UploadingRateMsg.Response(self.getAddress(), event.getVodSource(),
                        event.getTimeoutId(), event.getTarget(),
                        downloadStats.getDownloaded(event.getTarget())),
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
                bitTorrentSet.incrementUploadRate(event.getTarget(), event.getRate());
                lowerSet.incrementUploadRate(event.getTarget(), event.getRate());
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
     * check if a downloaded piece correspond to its hash if yes update the
     * utility if not unmark the subpiece as downloaded
     *
     * @param piece
     * @param peer
     */
    private void pieceCompleted(int piece, VodAddress peer) {
        UtilityVod utility = (UtilityVod) self.getUtility();
        // downloaded a complete piece
        try {
            if (!simulation) {
                if (!storage.checkPiece(piece)) {
                    for (int i = 0; i < storage.getMetaInfo().getPieceNbSubPieces(piece); i++) {
                        storage.removeSubpiece(piece * BitField.NUM_SUBPIECES_PER_PIECE + i);
                        downloadStats.removeDownloaded(peer, 1);
                    }
                    return;
                }
                ActiveTorrents.updatePercentage(videoName, storage.percent());
            }
            storage.getBitField().setPiece(piece);
            logger.debug(compName + "completed piece : {} . Total pieces downloaded {}", piece,
                    totalNumPiecesDownloaded++);
            partialPieces.remove(piece);
            bitTorrentSet.getStats().removePieceFromStats(piece);
            utility.setPiece(storage.getBitField().getFirstUncompletedPiece());

            if (buffering.get() && utility.getPiece() > pieceToRead.get() + bufferingWindow) {
                restartToRead();
            }
            if (storage.getBitField().getChunk(piece / BitField.NUM_PIECES_PER_CHUNK) && !finished) {
                int holdUtility = utility.getChunk();
                utility.setChunkOnly(storage.getBitField().getFirstUncompletedChunk());
//            snapshot.setUtility(self.getAddress(), utility.getChunck());
                if (utility.getPiece() < utility.getChunk() * BitField.NUM_PIECES_PER_CHUNK) {
                    logger.error(compName + "##### bad piece Utility value : ({};{}) "
                            + storage.getBitField().getTotal(),
                            utility.getChunk(), utility.getPiece());
                    logger.error(compName + "##### " + storage.getBitField().getChunkHumanReadable());
                }

//                delegator.doTrigger(new ChangeBootstrapUtility(utility,
//                        "GVod", self), vod);
                bitTorrentSet.getStats().changeUtility(holdUtility,
                        utility, storage.getBitField().pieceFieldSize(),
                        storage.getBitField());
                informUtilityChange();
            }
            if (count >= infUtilFrec) {
                logger.info(compName + storage.percent() + "%");
                informUtilityChange();
                count = 0;
            } else {
                count++;
            }
        } catch (Exception e) {
            logger.error(compName + "problem accessing storage");
        }

        self.updateUtility(utility);
    }

    /**
     * update the sets after a change in the utility and inform the neighbors
     * that need to be informed
     */
    private void informUtilityChange() {
        UtilityVod utility = (UtilityVod) self.getUtility();
        /* we have to do that on the upperSet because when the utility
         * increase we can have some node that have to be in the utilitySet
         * and not in the upperSet that will stay in the upperSet
         * we don't have to do it for the utilitySet because the correction
         * will be done at the next update of the sets
         */
        List<VodDescriptor> noMoreInUpperSet;
        noMoreInUpperSet = upperSet.changeUtility(utility);
        List<VodDescriptor> addToUtilitySet;
        if (utility.getChunk() >= 0) {
            List<VodDescriptor> temp = new ArrayList<VodDescriptor>();
            for (VodDescriptor node : noMoreInUpperSet) {
                UtilityVod u = (UtilityVod) node.getUtility();
                if (u.getPiece() < utility.getPiece() + utility.getPieceOffset()
                        && u.getPiece() > utility.getPiece() - utility.getPieceOffset()) {
                    temp.add(node);
                }

            }
            addToUtilitySet = bitTorrentSet.updateAll(temp, utility);
            if (!seeder) {
                for (VodDescriptor node : addToUtilitySet) {
                    triggerConnectRequest(node, true);
                }
            }

            logger.info(compName + "Utility changed, removing " + addToUtilitySet.size() + " from upper set");
            noMoreInUpperSet.removeAll(addToUtilitySet);
        }
        for (VodDescriptor node : noMoreInUpperSet) {
            triggerDisconnectRequest(node.getVodAddress(), false);
        }
        /* force the verify of the below set, else the nodes take
         * time to know that the node of their upperSet changed their
         * utility
         */

//        for (VodDescriptor node : lowerSet.getAll()) {
//            triggerRefRequest(node);
//        }
    }

    /**
     * check if the conditions have been fulfilled to restart reading after
     * having buffered .
     */
    private void restartToRead() {
        UtilityVod utility = (UtilityVod) self.getUtility();
        if (buffering.get() && read) {
            if (storage.getBitField().getFirstUncompletedPiece()
                    >= storage.getBitField().pieceFieldSize()) { // at the end of movie
                buffering.set(false);
                logger.info(compName + "2 starting reading after {}",
                        durationToString(System.currentTimeMillis() - stoppedReadingAtTime));
                bufferingTime += System.currentTimeMillis() - stoppedReadingAtTime;
                if (startJumpForward != 0) {
                    totalJumpForward += (System.currentTimeMillis() - startJumpForward);
                    startJumpForward = 0;
                }
            } else {
//                if (time - 10 - ((time - 10) % 10) >= 0) {
                if (time - 1 - ((time - 1) % 10) >= 0) {
//                    int timeOffset = time - 10 - ((time - 10) % 10);
                    int timeOffset = time - 1 - ((time - 1) % 10);
                    int left = rest.get(timeOffset);
                    float utilityDelta = (left - storage.needed())
                            / BitField.NUM_SUBPIECES_PER_PIECE;
                    float lecRest = (storage.getBitField().pieceFieldSize()
                            - pieceToRead.get()) * readingPeriod;
                    float downRest = (storage.getBitField().pieceFieldSize()
                            - utility.getPiece())
                            * (10 + ((time - 10) % 10)) / utilityDelta * 1000;
                    if (lecRest > downRest + (overhead * downRest / 100)) {
                        buffering.set(false);
                        logger.info(compName + "1 starting reading after {}",
                                durationToString(System.currentTimeMillis() - stoppedReadingAtTime));
                        bufferingTime += System.currentTimeMillis() - stoppedReadingAtTime;
                        if (startJumpForward != 0) {
                            totalJumpForward += (System.currentTimeMillis() - startJumpForward);
                            startJumpForward = 0;
                        }
                    }
                } else {
//                    logger.info(compName + "Buffering: {} < 0", time - 10 - ((time - 10) % 10));
                    logger.info(compName + "Buffering: {} < 0", time - 1 - ((time - 1) % 1));
                }
            }
        }
    }
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
            if (!partialPieces.containsKey(piece)) {
                return;
            }

            VodAddress peer = event.getVodSource();

            // mark that we downloaded a block
            VodDescriptor peerInfo = store.getVodDescriptorFromVodAddress(peer);
            partialPieces.remove(event.getPiece());
            if (peerInfo == null) {
                return;
            }
            peerInfo.discardPiece(event.getPiece());
//            removeFromUploaders(peer);
            if (bitTorrentSet.contains(peer)) {
                VodDescriptor toBeDisconnected = bitTorrentSet.updatePeerInfo(
                        event.getVodSource(), event.getUtility(),
                        event.getAvailableChunks(),
                        event.getAvailablePieces());
                if (toBeDisconnected != null) {
                    triggerDisconnectRequest(toBeDisconnected.getVodAddress(), false);
                }
            } else if (upperSet.contains(peer)) {
                VodAddress toBeDisconnected = upperSet.updateUtility(peer,
                        event.getUtility());
                if (toBeDisconnected != null) {
                    triggerDisconnectRequest(toBeDisconnected, false);
                }
            }
        }
    };
    /**
     * handle a message from gvodPeer asking to change the utility
     */
    Handler<ChangeUtility> handleChangeUtility = new Handler<ChangeUtility>() {
        @Override
        public void handle(ChangeUtility event) {
            UtilityVod utility = (UtilityVod) self.getUtility();
            logger.trace(compName + "handleChangeUtility (readpos/utility): " + event.getReadPos() + "/"
                    + event.getUtility());
            if (simulation) {
                int reqUtility = event.getUtility();
                int newUtility = storage.getBitField().setFirstUncompletedChunk(reqUtility);
                if (newUtility != utility.getChunk()) {
                    changeUtility(newUtility);
                }
                pieceToRead.set(reqUtility * BitField.NUM_PIECES_PER_CHUNK);
            } else {
                changeUtilityPosition(event.getUtility(),
                        event.getReadPos(),
                        event.getResponseBody());
            }
        }
    };
    /**
     * handle jumpforward message from gvodPeer containing a relative jump
     * forward distance
     */
    Handler<JumpForward> handleJumpForward = new Handler<JumpForward>() {
        @Override
        public void handle(JumpForward event) {
            UtilityVod utility = (UtilityVod) self.getUtility();
            logger.trace(compName + "handleJumpForward");
            int newUtility = utility.getChunk() + event.getGap();
            if (utility.isSeeder()) {
                return;
            }
            if (newUtility >= storage.getBitField().getChunkFieldSize()) {
                newUtility = storage.getBitField().getChunkFieldSize() - 1;
            }

            newUtility = storage.getBitField().setFirstUncompletedChunk(newUtility);
            if (newUtility != utility.getChunk()) {
                changeUtility(newUtility);
                startJumpForward = System.currentTimeMillis();
                jumped = true;
                pieceToRead.set(newUtility * BitField.NUM_PIECES_PER_CHUNK);
                logger.info(compName + "handle jumpForward, utility : {} piece to read : {}",
                        utility.getPiece(), pieceToRead.get());
                nextPieceToSend.set(pieceToRead.get());
            }

        }
    };
    /**
     * same as jumpforward but back
     */
    Handler<JumpBackward> handleJumpBackward = new Handler<JumpBackward>() {
        @Override
        public void handle(JumpBackward event) {
            logger.trace(compName + "handleJumpBackward");
            UtilityVod utility = (UtilityVod) self.getUtility();
            int newUtility = utility.getChunk() - event.getGap();
            if (newUtility >= storage.getBitField().getChunkFieldSize()) {
                newUtility = storage.getBitField().getChunkFieldSize() - 1;
            }
            pieceToRead.set(newUtility * BitField.NUM_PIECES_PER_CHUNK);
            newUtility = storage.getBitField().setFirstUncompletedChunk(newUtility);
            if (newUtility != utility.getChunk()) {
                changeUtility(newUtility);
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

            byte[] hashes = storage.getMetaInfo().getChunkHashes(event.getChunk());
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

            if (storage.getMetaInfo().haveHashes(chunk)) { //already have hashes
                hashRequests.remove(chunk);
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
                    if (storage.getMetaInfo().setPieceHashes(allHashes.array(), chunk)) {
                        hashRequests.remove(chunk);
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
            if (hashRequests.containsKey(event.getChunk())) {
                hashRequests.get(event.getChunk()).remove(event.getPeer());
                if (hashRequests.get(event.getChunk()).isEmpty()) {
                    hashRequests.remove(event.getChunk());
                }
                logger.warn(compName + "Hash Request timed out");
                // no need to re-send hash request, as this will happen during piece selection.
            }
        }
    };

    /**
     * change the utility value, search the first undownloaded piece from the
     * new Utility position and set it as the new utility and start to send the
     * packet corresponding to the new utility to the video player.
     *
     * @param nUtility
     * @param responseBody
     * @param positionToRead
     */
    private void changeUtilityPosition(int seekPos, int readPos,
            OutputStream responseBody) {
        logger.info(compName + "changeUtilityPosition seekPos/readPos {}/{} ", seekPos, readPos);
        UtilityVod utility = (UtilityVod) self.getUtility();
        int piece2Read;
        int offsetWithinPieceToRead;
        int newUtility;
        int chunkForByte = (seekPos / BitField.SUBPIECE_SIZE)
                / BitField.NUM_SUBPIECES_PER_PIECE / BitField.NUM_PIECES_PER_CHUNK;

        newUtility = storage.getBitField().setFirstUncompletedChunk(chunkForByte);

        if (isMp4() == false) { // FLV
            // TODO - is the chunk size is fixed here at 2MB - 128 pieces??!?
            piece2Read = seekPos / (BitField.NUM_SUBPIECES_PER_PIECE * BitField.SUBPIECE_SIZE);
            offsetWithinPieceToRead = seekPos % (BitField.NUM_SUBPIECES_PER_PIECE * BitField.SUBPIECE_SIZE);

            storage.getBitField().setFirstUncompletedPiece(piece2Read);

            utility.setPiece(piece2Read);

        } else {
            // mp4
            // 1. calculate new byte position for seek position in millisecs
            // 2. set newUtility to be first uncompleted chunk position

            // Need to start reading from beginning of chunk to get keys for pieces
            // was pieceToRead
            piece2Read = seekPos / (BitField.NUM_SUBPIECES_PER_PIECE * BitField.SUBPIECE_SIZE);
            offsetWithinPieceToRead = seekPos - (piece2Read * BitField.SUBPIECE_SIZE * BitField.NUM_SUBPIECES_PER_PIECE);

        }

        if (newUtility != utility.getChunk()) {
            changeUtility(newUtility);
        }

        nextPieceToSend.set(piece2Read);

        Sender oldSender = sender;

        sender = new Sender(this, responseBody, piece2Read, offsetWithinPieceToRead);
        sender.start();
        if (buffering.get()
                && (storage.getBitField().getFirstUncompletedPiece()
                >= piece2Read + bufferingWindow
                || storage.complete()
                || storage.getBitField().getFirstUncompletedPiece()
                >= storage.getBitField().pieceFieldSize())) {
            restartToRead();
        }
        pieceToRead.set(piece2Read);

        if (oldSender != null) {
            oldSender.interrupt();
        }

    }

    /**
     * change the utility value, search for the first piece not downloaded from
     * the nUtility position and set it as the new utility.
     *
     * @param newUtility
     */
    private void changeUtility(int newUtility) {
        UtilityVod utility = (UtilityVod) self.getUtility();
        // TODO JIM - remove this and fix problem
//        buffering.set(true);

        int oldUtility = utility.getChunk();
        utility.setChunk(newUtility);
//        snapshot.setUtility(self.getAddress(), utility.getChunck());
        bitTorrentSet.getStats().changeUtility(oldUtility, utility,
                storage.getBitField().pieceFieldSize(), storage.getBitField());

        // TODO: Check if I already have nodes at the new utility level first.
        // Ask the bootstrap server for nodes at the new utility level
        // Nodes that are behind NATs will be slower to connect to - if i already
        // have an open session to them - use it.

        /* we have to do that on the upperSet because when the utility
         * increases we can have some node that have to be in the utilitySet
         * and not in the upperSet that will stay in the upperSet
         * we don't have to do it for the utilitySet because the correction
         * will be done at the next update of the sets
         */
        List<VodDescriptor> noLongerInUpperSet = upperSet.changeUtility(utility);
        List<VodDescriptor> tocheck = new ArrayList<VodDescriptor>();
        for (VodDescriptor node : noLongerInUpperSet) {
            UtilityVod u = (UtilityVod) node.getUtility();
            if (u.getChunk() < utility.getChunk() + utility.getOffset()
                    && u.getChunk() > utility.getChunk() - utility.getOffset()) {
                tocheck.add(node);
            }
        }
        // Add those nodes that have now moved from upper-set to similar set
        List<VodDescriptor> sendConnect = bitTorrentSet.updateAll(tocheck, utility);
        logger.info(compName + "Utility changed, removing " + sendConnect.size() + " from upper set");
        noLongerInUpperSet.removeAll(sendConnect);

        for (VodDescriptor node : noLongerInUpperSet) {
            // TODO - new message to move nodes from Lower set to Utility set.
            // Here, we're using two msgs to do the same thing...
            // TODO - do we have a cleanup event to remove old connections from
            // lower sets - e.g., incase this disconnect msg is lost?
            triggerDisconnectRequest(node.getVodAddress(), false);
        }

        if (utility.getPiece() <= storage.getBitField().pieceFieldSize()) {
            for (VodDescriptor node : sendConnect) {
                triggerConnectRequest(node, true);
            }
        }

        /* force the check of the below set, else the nodes take
         * time to know that one node of their upperSet changed its
         * utility
         */
//        for (VodDescriptor node : lowerSet.getAll()) {
//            triggerRefRequest(node);
//        }
        if (seeder) {
            for (VodDescriptor node : bitTorrentSet.getAll()) {
                bitTorrentSet.remove(node.getVodAddress());
                triggerDisconnectRequest(node.getVodAddress(), false);
            }
        }

        int i = utility.getChunk();
        if (utility.getPiece() <= storage.getBitField().pieceFieldSize()) {
            while (i < storage.getBitField().getChunkFieldSize() + 11) {
                if (fingers.get(i) != null) {
                    VodDescriptor node = fingers.get(i);
                    triggerConnectRequest(node, true);
                }
                i++;
            }
        }

        self.updateUtility(utility);
        updateSetsAndConnect();
    }
    
//    Handler<ChangeUtilityMsg.Response> handleChangeUtilityMsgResponse =
//            new Handler<ChangeUtilityMsg.Response>() {
//
//                @Override
//                public void handle(ChangeUtilityMsg.Response event) {
//                    List<GVodNodeDescriptor> tocheck = event.getPeers();
//        // Add those nodes that have now moved from upper-set to similar set
//        List<GVodNodeDescriptor> sendConnect = similarSet.updateAll(tocheck);
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
    // add nodes to similarity/upper sets
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
            if (!buffering.get() && event.getTimeoutId() == readTimerId) {
                // TODO - IndexOutOfBoundsException here sometimes
                if (!storage.getBitField().getPiece(pieceToRead.get())) {
                    logger.warn(compName + "Buffering piece {}", pieceToRead.get());
                    bufferingNum++;

                    stoppedReadingAtTime = System.currentTimeMillis();
                    buffering.set(true);
                } else {
                    pieceToRead.getAndIncrement();
                    if (pieceToRead.get() >= storage.getBitField().pieceFieldSize()
                            || pieceToRead.get() >= storage.getBitField().getPieceFieldLength() * 8) {
                        logger.info(compName + "finished to read after {}, number of buffering={} "
                                + bW,
                                durationToString(System.currentTimeMillis() - startedAtTime), bufferingNum);
                        delegator.doTrigger(new ReadingCompleted(self.getAddress(), bufferingNum, waiting,
                                misConnect, bufferingTime, null,
                                utilityAfterTime, freeRider, totalJumpForward), vod);
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
        read = true;
        // TODO JIM -remove this and fix the buffering time!
//        buffering.set(false);
        if (buffering.get()
                && (storage.getBitField().getFirstUncompletedPiece()
                >= pieceToRead.get() + bufferingWindow || storage.complete()
                || storage.getBitField().getFirstUncompletedPiece()
                >= storage.getBitField().pieceFieldSize())) {
            logger.info(compName + "RESTARTING TO READ: " + self.getId() + " buffering=" + buffering + " piece-to-read:"
                    + pieceToRead.get() + " -- bufferingWindow: " + bufferingWindow
                    + " => First uncompleted piece: " + storage.getBitField().getFirstUncompletedPiece());
            restartToRead();
        } else {
            logger.info(compName + "NOT RESTARTING TO READ: " + self.getId() + " buffering=" + buffering + " piece-to-read:"
                    + pieceToRead.get() + " -- bufferingWindow: " + bufferingWindow
                    + " => First uncompleted piece: " + storage.getBitField().getFirstUncompletedPiece());
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
            read = false;
            buffering.set(true);
        }
    };
    /**
     * if the node is turning in background and use too-much
     */
    Handler<SlowBackground> handleSlowBackground = new Handler<SlowBackground>() {
        @Override
        public void handle(SlowBackground event) {
            if (maxWindowSize == 0) {
                maxWindowSize = pipeSize * VodConfig.LB_MAX_SEGMENT_SIZE;
            }
            maxWindowSize = maxWindowSize / 2;
            if (maxWindowSize < VodConfig.LB_MAX_SEGMENT_SIZE) {
                maxWindowSize = VodConfig.LB_MAX_SEGMENT_SIZE;
            }
            for (VodDescriptor node : store.getAll()) {
                node.getWindow().setMaxWindowSize(maxWindowSize);
            }
        }
    };
    /**
     * the opposite of slowbackground
     */
    Handler<SpeedBackground> handleSpeedBackground = new Handler<SpeedBackground>() {
        @Override
        public void handle(SpeedBackground event) {
            maxWindowSize = +maxWindowSize / 2;
            for (VodDescriptor node : store.getAll()) {
                node.getWindow().setMaxWindowSize(maxWindowSize);
            }
        }
    };

    /**
     * tell gvodPeer that the downloading is finished, put the stream in the
     * background streams and become a seed
     */
    private void finishedDownloading() {
        UtilityVod utility = (UtilityVod) self.getUtility();
        finished = true;
        seeder = true;
        buffering.set(false);
        long duration = System.currentTimeMillis() - startedAtTime;
        logger.info(compName + "finished buffering after {} ratio : {} ("
                + utility.getChunk() + ";" + utility.getPiece() + ") " + duration + " " + freeRider,
                durationToString(duration),
                piecesFromUpperSet / piecesFromUtilitySet);

        // write hash pieces to file when finished downloading.
        if (storage instanceof StorageMemMapWholeFile) {
            StorageMemMapWholeFile se = (StorageMemMapWholeFile) storage;
            try {
                se.writePieceHashesToFile();
            } catch (FileNotFoundException ex) {
                java.util.logging.Logger.getLogger(Vod.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                java.util.logging.Logger.getLogger(Vod.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        DownloadCompletedSim down = new DownloadCompletedSim(self.getAddress(), duration,
                freeRider, jumped);
        delegator.doTrigger(down, vod);
        delegator.doTrigger(down, status);
        restartToRead();
        logger.info(compName + "become seeder");
        changeUtility(VodConfig.SEEDER_UTILITY_VALUE);
        seeder = true;

        //become a potential background torrent.
        // TODO - move this to SwingMain?? 
        try {
            ActiveTorrents.makeSeeder(torrentFileAddress);
        } catch (Exception e) {
            logger.error(compName + "impossible to add this movie to the background movies");
        }

    }

    /**
     * trigger a disconnect request to add
     *
     * @param dest
     */
    private void triggerDisconnectRequest(VodAddress dest, boolean noDelay) {

        logger.warn(compName + "trigger DisconnectRequest to {}", dest.getId());
        if (noDelay) {
            DisconnectMsg.Request dr = new DisconnectMsg.Request(self.getAddress(), dest);
            ScheduleRetryTimeout st = new ScheduleRetryTimeout(config.getConnectionTimeout(), 1);
            DisconnectMsg.RequestTimeout drt = new DisconnectMsg.RequestTimeout(st, dr);
            delegator.doRetry(drt);
//            removeFromUploaders(addr);
            trigger(new DisconnectNeighbour(dest.getId()), natTraverserPort);
        } else {
            ScheduleTimeout st = new ScheduleTimeout(config.getConnectionTimeout());
            st.setTimeoutEvent(new DisconnectTimeout(st, dest));
            delegator.doTrigger(st, timer);
        }
    }

    /**
     * remove peer from the uploader
     *
     * @param peer
     */

    /**
     * trigger a connect request to node
     *
     * @param node
     * @param toUtilitySet
     */
    private void triggerConnectRequest(VodDescriptor node, boolean toUtilitySet) {
        UtilityVod utility = (UtilityVod) self.getUtility();
        if (node != null) {
            if (node.getVodAddress().getId() == self.getAddress().getId()) {
                logger.warn(compName + "DO NOT CONNECT TO SELF");
                return;
            }
            if (ongoingConnectRequests.containsKey(node.getVodAddress().getId())) {
                return;
            }

            UtilityVod u = (UtilityVod) node.getUtility();
            logger.info(compName + "trigger ConnectRequest to {} myUtility ("
                    + utility.getChunk() + ";" + utility.getPiece() + ") its ("
                    + u.getChunk() + ";" + u.getPiece() + ")",
                    node.getVodAddress().getId());

            ScheduleRetryTimeout st = new ScheduleRetryTimeout(2000, 3, 1.5d);
            ConnectMsg.Request request = new ConnectMsg.Request(
                    self.getAddress(), node.getVodAddress(), u, toUtilitySet, mtu);
            ConnectMsg.RequestTimeout retryRequest = new ConnectMsg.RequestTimeout(st, request,
                    toUtilitySet);
            delegator.doRetry(retryRequest);
            ongoingConnectRequests.put(node.getVodAddress().getId(), System.currentTimeMillis());
        }
    }

    /**
     * start downloading a piece from peer
     *
     * @param peer
     * @param numRequestBlocks
     * @param ackId
     * @param rtt
     */
    private void startDownloadingPieceFrom(VodAddress peer,
            int numRequestBlocks, TimeoutId ackId, long rtt) {
        logger.trace(compName + "Starting to download {} blocks from : {}", numRequestBlocks,
                peer.getId());
        if (numRequestBlocks <= 0) {
            if (ackId != null) {
                delegator.doTrigger(new DataMsg.Ack(self.getAddress(), peer, ackId, rtt), network);
            }
            logger.debug(compName + "NumBlocks < 0. AckId was not null. Not requesting piece");
            return;
        }
        int piece;
        List<Integer> missing = null;
        do {
            piece = selectPiece(peer);
            logger.trace(compName + "piece to download : {}", piece);
            if (piece == -1) {
                // no piece is eligible for download from this peer
                if (upperSet.contains(peer)) {
                    piece = storage.getBitField().getFirstUncompletedPiece();
                    while (piece < storage.getBitField().pieceFieldSize()
                            && (partialPieces.containsKey(piece)
                            || storage.getBitField().getPiece(piece))) {
                        piece++;
                    }
                    if (piece >= storage.getBitField().pieceFieldSize()) {
                        if (ackId != null) {
                            delegator.doTrigger(new DataMsg.Ack(self.getAddress(), peer, ackId, rtt), network);
                        }
                        logger.debug(compName + "Piece > current. AckId was not null. Not requesting piece");
                        return;
                    }
                } else {
                    if (ackId != null) {
                        delegator.doTrigger(new DataMsg.Ack(self.getAddress(), peer, ackId, rtt), network);
                    }
                    logger.debug(compName + "Utility set. AckId was not null. Not requesting piece");
                    return;
                }
            }
            missing = storage.missingSubpieces(piece);
            if (missing.isEmpty()) {
                pieceCompleted(piece, null);
                piece = -1;
            }
        } while (piece == -1);
        int chunk = piece / BitField.NUM_PIECES_PER_CHUNK;
        if (!storage.getMetaInfo().haveHashes(chunk)) {
            if ((!hashRequests.containsKey(chunk)
                    || !hashRequests.get(chunk).contains(peer))) {
                triggerHashRequest(peer, chunk, 0);
                return;
            } else {
                logger.debug(compName + "Hash request outstanding.");
            }
            if (ackId != null) {
                delegator.doTrigger(new DataMsg.Ack(self.getAddress(), peer, ackId, rtt), network);
            }
        }
        UtilityVod utility = (UtilityVod) self.getUtility();
        if (piece > pieceToRead.get() + bufferingWindow
                && (((UtilityVod) store.getVodDescriptorFromVodAddress(peer).getUtility()).getChunk()
                > utility.getChunk()
                || ((UtilityVod) store.getVodDescriptorFromVodAddress(peer).getUtility()).isSeeder())) {
            chunk = utility.getChunk() + 3;
            if (chunk < storage.getMetaInfo().getNbChunks()
                    && !storage.getMetaInfo().haveHashes(chunk)) {
                triggerHashRequest(peer, chunk, 0);
            }
        }
        int blockCount = BitField.NUM_SUBPIECES_PER_PIECE;
        PieceInTransit transit = new PieceInTransit(piece, blockCount, peer,
                missing);
        partialPieces.put(piece, transit);

        VodDescriptor peerInfo = store.getVodDescriptorFromVodAddress(peer);

        if (peerInfo == null) {
            if (ackId != null) {
                delegator.doTrigger(new DataMsg.Ack(self.getAddress(), peer, ackId, rtt), network);
            }
            logger.debug(compName + "PeerInfo null. AckId was not null. Not requesting piece");
            return;
        }
        int lim = numRequestBlocks;
        if (missing.size() < lim) {
            lim = missing.size();
            logger.debug(compName + "Changed numRequestBlocks to {}", lim);
        }
        for (int i = 0; i < lim; i++) {
            int nextBlock = transit.getNextSubpieceToRequest();

            triggerDataMsgRequest(peerInfo, ackId, piece, nextBlock, rtt);
        }
    }

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

        if (!hashRequests.containsKey(chunk)) {
            hashRequests.put(chunk, new ArrayList<VodAddress>());
        }
        hashRequests.get(chunk).add(peer);
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

//        DataMsg.Request request = new DataMsg.Request(selfNoParents.getAddress(), des,
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
     * select next piece to download from peer
     *
     * @param peer
     * @return
     */
    private int selectPiece(VodAddress peer) {
        VodDescriptor info = store.getVodDescriptorFromVodAddress(peer);
        if (info == null) {
            logger.warn(compName + "No descriptor for {} when selecting piece", peer.getId());
            return -1;
        }

        if (upperSet.contains(peer)) {
            int piece = bitTorrentSet.getStats().getUpperPiece(partialPieces,
                    pieceToRead.get(), bufferingWindow);
            if (piece != -1) {
                return piece;
            }
        }
        // first we try to complete a stale piece before selecting a new piece
        // [strict piece selection policy]
        int stalePiece = selectStalePiece(peer);
        if (stalePiece != -1) {
            logger.warn(compName + "Selecting stale piece {} from peer {}", stalePiece, peer.getId());
            return stalePiece;
        }

        // compute the set of eligible pieces
        if (upperSet.contains(peer)) {
            int piece = bitTorrentSet.getStats().pieceToDownloadFromUpper(
                    info, partialPieces, pieceToRead.get(), bufferingWindow);
            if (piece == -1) {
                piece = storage.getBitField().getFirstUncompletedPiece();
                while (piece < storage.getBitField().pieceFieldSize()
                        && (partialPieces.containsKey(piece)
                        || storage.getBitField().getPiece(piece))) {
                    piece++;
                }
                if (piece < storage.getBitField().pieceFieldSize()) {
                    logger.trace(compName + "Upper set: Piece num {} found", piece);
                    return piece;
                } else {
                    logger.trace(compName + "Upper set: Piece num {} is greater than "
                            + "maxPieceSize {}", piece,
                            storage.getBitField().pieceFieldSize());
                    return -1;
                }
            }
            return piece;
        } else {
            int piece = bitTorrentSet.getStats().pieceToDownload(
                    info, partialPieces, pieceToRead.get(), bufferingWindow);
            logger.trace(compName + "Piece num {} found from similar set", piece);
            return piece;
        }

    }

    /**
     * select a stale piece to download from peer
     *
     * @param peer
     * @return
     */
    private int selectStalePiece(VodAddress peer) {
        VodDescriptor info = store.getVodDescriptorFromVodAddress(peer);
        if (info == null) {
            return -1;
        }
        List<Integer> eligible;
        if (upperSet.contains(peer)) {
            eligible = new ArrayList<Integer>(partialPieces.keySet());
        } else {
            eligible = bitTorrentSet.getStats().getEligible(new ArrayList<Integer>(partialPieces.keySet()), info);
        }
        // eligible contains the pieces in transit that the peer has
        // we look for stale ones

        for (int piece : eligible) {
            PieceInTransit transit = partialPieces.get(piece);
            if (transit.isStalePiece(config.getDataRequestTimeout())) {
                // discard old stale piece and select it again
                partialPieces.remove(piece);
                for (VodDescriptor inf : store.getAll()) {
                    inf.discardPiece(piece);
                }
                return piece;
            }
        }
        return -1;
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
        return buffering.get();
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
        return nextPieceToSend.get();
    }

    /**
     * return the storage of the node
     *
     * @return
     */
    public Storage getStorage() {
        return storage;
    }

    /**
     * set the next piece to be sent to the video-player
     *
     * @param nextPieceToSend
     */
    public void setNextPieceToSend(int nextPieceToSend) {
        this.nextPieceToSend.set(nextPieceToSend);
    }

    private boolean isMp4() {
        return storage.getMetaInfo().isMp4();
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
        if (store != null) {
            for (VodAddress addr : store.getNeighbours().keySet()) {
                triggerDisconnectRequest(addr, true);
            }
        }
        cancelPeriodicTimer(initiateShuffleTimeoutId);
        cancelPeriodicTimer(dataOfferPeriodTimeoutId);
        cancelPeriodicTimer(readTimerId);
    }
}
