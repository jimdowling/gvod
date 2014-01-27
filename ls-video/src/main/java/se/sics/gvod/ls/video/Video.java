package se.sics.gvod.ls.video;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.asdistances.ASDistances;
import se.sics.gvod.common.RetryComponentDelegator;
import se.sics.gvod.common.Self;
import se.sics.gvod.croupier.PeerSamplePort;
import se.sics.gvod.croupier.events.CroupierSample;
import se.sics.gvod.ls.interas.InterAsPort;
import se.sics.gvod.ls.interas.events.InterAsSample;
import se.sics.gvod.ls.system.LSConfig;
import se.sics.gvod.ls.video.events.VideoCycle;
import se.sics.gvod.ls.video.snapshot.SimulationSingleton;
import se.sics.gvod.ls.video.snapshot.StatsRestClient;
import se.sics.gvod.ls.video.snapshot.VideoStats;
import se.sics.gvod.nat.common.MsgRetryComponent;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.timer.CancelPeriodicTimeout;
import se.sics.gvod.timer.SchedulePeriodicTimeout;
import se.sics.gvod.timer.ScheduleTimeout;
import se.sics.gvod.timer.Timeout;
import se.sics.gvod.timer.TimeoutId;
import se.sics.gvod.video.msgs.EncodedSubPiece;
import se.sics.gvod.video.msgs.VideoConnectionMsg;
import se.sics.gvod.video.msgs.VideoConnectionMsg.Disconnect;
import se.sics.gvod.video.msgs.VideoPieceMsg;
import se.sics.gvod.video.msgs.VideoPieceMsg.Advertisement;
import se.sics.gvod.video.msgs.VideoPieceMsg.Request;
import se.sics.gvod.video.msgs.VideoPieceMsg.RequestTimeout;
import se.sics.gvod.video.msgs.VideoPieceMsg.Response;
import se.sics.kompics.Handler;
import se.sics.kompics.Positive;
import se.sics.kompics.Stop;

/**
 *
 * @author Niklas Wahl&#233;n <nwahlen@kth.se>
 */
public class Video extends MsgRetryComponent {

    // Framework
    private final Logger logger = LoggerFactory.getLogger(Video.class);
    private String compName;
    private Self self;
    private Positive<InterAsPort> interAs = positive(InterAsPort.class);
    private Positive<PeerSamplePort> croupier = positive(PeerSamplePort.class);
    // Child classes
    private VideoNeighbours neighbours;
    private VideoGossip gossiping;
    private VideoIO io;
    private Map<Integer, EncodedSubPiece> subPieceBuffer;
    // Video component configuration
    private long videoCyclePeriod = LSConfig.VIDEO_CYCLE;
    private TimeoutId videoCycleTimeoutId;
    private Set<TimeoutId> gossipTimeoutIds;
    private boolean source;
    // Video simulation
    private int warmup;
    private int currentEncodedSubPiece = 0;
    private boolean fileWritten = false;
    // Monitoring and diagnostics
    private StatsRestClient statsClient;
    public static int SYSTEM_VIDEO_OVERLAY_ID = 1431;

    private int roundCount = 0;

    
    public Video() {
        this(null);
    }

    public Video(RetryComponentDelegator delegator) {
        super(delegator);
        this.delegator.doAutoSubscribe();
        io = new VideoIO(this);
        subPieceBuffer = new HashMap<Integer, EncodedSubPiece>();
        gossipTimeoutIds = new HashSet<TimeoutId>();
        if (LSConfig.hasMonitorUrlSet()) {
            statsClient = new StatsRestClient(LSConfig.getMonitorServerUrl());
        }
    }
    Handler<VideoInit> handleInit = new Handler<VideoInit>() {

        @Override
        public void handle(VideoInit init) {
            self = init.getSelf();

            // Configuration
            warmup = LSConfig.VIDEO_WARMUP;
            source = init.isSource();
            neighbours = new VideoNeighbours(delegator, self, network, source);
            gossiping = new VideoGossip(delegator, self, network, timer, neighbours, subPieceBuffer, gossipTimeoutIds, source);

            SchedulePeriodicTimeout periodicTimeout =
                    new SchedulePeriodicTimeout(0, videoCyclePeriod);
            periodicTimeout.setTimeoutEvent(new VideoCycle(periodicTimeout));
            videoCycleTimeoutId = periodicTimeout.getTimeoutEvent().getTimeoutId();
            delegator.doTrigger(periodicTimeout, timer);


            VideoStats.addNode(self.getAddress(), source);
            if (LSConfig.hasMonitorUrlSet()) {
                VideoStats.instance(self).setExperimentId(LSConfig.getExperimentId());
                VideoStats.instance(self).setExperimentIteration(LSConfig.getExperimentIteration());
            }
            int asn = ASDistances.getInstance().getASFromIP(self.getIp().getHostAddress());
            VideoStats.instance(self).setAsn(asn);

            compName = "Video(" + self.getId() + ") ";
            if (source) {
                logger.info(self.getId() + ": Source created");
                compName = "Video(" + self.getId() + " SOURCE) ";
                try {
                    if (LSConfig.hasInputFileSet()) {
                        io.setSource(new File(LSConfig.getInputFilename()));
                    } else if (LSConfig.hasSourceUrlSet()) {
                        io.setSource(new URL(LSConfig.getSourceUrl()));
                    } else {
                        logger.error("Neither input file nor source URL specified for source");
                        System.exit(1);
                    }
                    if (LSConfig.isSimulation()) {
                        logger.info("Run video io");
                        io.run();
                        logger.info("Run video io: done");
                    } else {
                        new Thread(io).start();
                    }

                } catch (IOException ex) {
                    java.util.logging.Logger.getLogger(Video.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                // Not source
                try {
                    if (LSConfig.hasDestUrlSet()) {
                        String ip = LSConfig.getDestIp();
                        InetAddress addr = InetAddress.getByName(LSConfig.getDestIp());
                        int port = LSConfig.getDestPort();
                        io.startServer(new InetSocketAddress(port));
                    }
                } catch (IOException ex) {
                    java.util.logging.Logger.getLogger(Video.class.getName()).log(Level.SEVERE, null, ex);
                }
                neighbours.incRandom(true);
            }
        }
    };
    /*
     * Node samples
     */
    Handler<InterAsSample> handleInterAsSample = new Handler<InterAsSample>() {

        @Override
        public void handle(InterAsSample sample) {
            neighbours.handleInterAsSample(sample);
        }
    };
    Handler<CroupierSample> handleCroupierSample = new Handler<CroupierSample>() {

        @Override
        public void handle(CroupierSample sample) {
            neighbours.handleCroupierSample(sample);
        }
    };
    /*
     * Cycle
     */
    Handler<VideoCycle> handleCycle = new Handler<VideoCycle>() {

        @Override
        public void handle(VideoCycle c) {
            // Warmup for simulation. Croupier takes time to deliver samples,
            // TODO - should remove this for production.
            if (warmup > 0) {
                // send additional connection requests during warmup
                neighbours.sendInterAsConnectionRequests(1);
                warmup--;
            } else {
                if (!source && (neighbours.getIngoingConnections() == 0)) {
                    neighbours.mildPanic();
                }
                if (!source && LSConfig.hasMonitorUrlSet()) {
                    statsClient.createStats(VideoStats.instance(self));
                }
            }
            updateRandomNeighbours();
            neighbours.cycle();
            io.cycle();
            if (!neighbours.isEmpty()) {
                gossiping.cycle();
                if (LSConfig.isSimulation()) {
                    if (source) {
                        Set<Integer> newPieces = new HashSet<Integer>();
                        int interval = 5 * 14; // encoded sub-pieces
                        // rate = interval * 1316 B
                        int from = currentEncodedSubPiece;
                        int to = currentEncodedSubPiece + interval;
                        for (int i = from; i < to; i++) {
                            // if we run out of subpieces, restart from the beginning
                            // (however publish with increasing piece and sub-piece ids)
                            EncodedSubPiece esp = subPieceBuffer.get(i % subPieceBuffer.size());
                            int ppieceId = (i - esp.getEncodedIndex()) / LSConfig.FEC_ENCODED_PIECES;
                            EncodedSubPiece pesp = new EncodedSubPiece(i, esp.getEncodedIndex(), esp.getData(), ppieceId);
                            gossiping.publish(pesp);
                            if (!newPieces.contains(pesp.getParentId())) {
                                newPieces.add(pesp.getParentId());
                            }
                        }
                        for (Integer newPieceId : newPieces) {
                            SimulationSingleton.getInstance().register(newPieceId);
                        }
                        currentEncodedSubPiece += interval;
                    }
                    if (LSConfig.hasOutputFileSet() && !fileWritten && (subPieceBuffer.size() == 33 * LSConfig.FEC_ENCODED_PIECES)) {
                        // dissemination ended
                        try {
                            String fileName = source ? "streamSource.mp4" : LSConfig.getOutputFilename() + self.getId() + ".mp4";
                            io.writePieceData(fileName);
                        } catch (IOException ex) {
                            java.util.logging.Logger.getLogger(Video.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        fileWritten = true;
                    }
                }
            } // endif !neighbours.isEmpty()
            
            roundCount++;
        }
    };


    
    /*
     * Neighbour connections
     */
    Handler<VideoConnectionMsg.Request> handleConnectionRequest = new Handler<VideoConnectionMsg.Request>() {

        @Override
        public void handle(VideoConnectionMsg.Request request) {
            neighbours.handleConnectionRequest(request);
            neighbours.updateTimstamp(request.getVodSource());
        }
    };
    Handler<VideoConnectionMsg.RequestTimeout> handleConnectionRequestTimeout = new Handler<VideoConnectionMsg.RequestTimeout>() {

        @Override
        public void handle(VideoConnectionMsg.RequestTimeout timeout) {
            neighbours.handleConnectionRequestTimeout(timeout);
        }
    };
    Handler<VideoConnectionMsg.Response> handleConnectionResponse = new Handler<VideoConnectionMsg.Response>() {

        @Override
        public void handle(VideoConnectionMsg.Response response) {
            neighbours.handleConnectionResponse(response);
            neighbours.updateTimstamp(response.getVodSource());
        }
    };
    Handler<VideoConnectionMsg.Disconnect> handleDisconnection = new Handler<VideoConnectionMsg.Disconnect>() {

        @Override
        public void handle(Disconnect disconnect) {
            logger.debug(compName + " received disconnection msg from " + disconnect.getVodSource());
            neighbours.handleDisconnetion(disconnect);
            neighbours.updateTimstamp(disconnect.getVodSource());
        }
    };
    /*
     * Three-phase gossip
     */
    Handler<VideoPieceMsg.Advertisement> handleAdvertisement = new Handler<VideoPieceMsg.Advertisement>() {

        @Override
        public void handle(Advertisement advertisement) {
            if (!source) {
                gossiping.handleAdvertisement(advertisement);
                neighbours.updateTimstamp(advertisement.getVodSource());
            }
        }
    };
    Handler<VideoPieceMsg.Request> handleRequest = new Handler<VideoPieceMsg.Request>() {

        @Override
        public void handle(Request request) {
            gossiping.handleRequest(request);
            neighbours.updateTimstamp(request.getVodSource());
        }
    };
    Handler<VideoPieceMsg.RequestTimeout> handleRequestTimeout = new Handler<VideoPieceMsg.RequestTimeout>() {

        @Override
        public void handle(RequestTimeout rt) {
            gossiping.handleTimeout(rt);
        }
    };
    Handler<VideoPieceMsg.Response> handlePieces = new Handler<VideoPieceMsg.Response>() {

        @Override
        public void handle(Response response) {
            if (!source) {
                incDownloaded(response.getVodSource());
                EncodedSubPiece p = response.getEncodedSubPiece();
                io.handleEncodedSubPiece(p);
//                updateRandomNeighbours(p);
                gossiping.handlePieces(response);
                neighbours.updateTimstamp(response.getVodSource());
            }
        }
    };
    Handler<RemovalTimer> handleRemoval = new Handler<RemovalTimer>() {

        @Override
        public void handle(RemovalTimer removal) {
            if (!source) {
                int startGlobalId = removal.getPieceId() * LSConfig.FEC_ENCODED_PIECES;
                int endGlobalId = startGlobalId + LSConfig.FEC_ENCODED_PIECES - 1;
                for (int i = startGlobalId; i < endGlobalId; i++) {
                    subPieceBuffer.remove(i);
                }
            }
        }
    };

    public void publish(EncodedSubPiece esp) {
        if (LSConfig.isSimulation() && source) {
            // Put in buffer for use at cycle event (as in simulation, it is reading the data too quickly).
            subPieceBuffer.put(esp.getGlobalId(), esp);
        } else {
            gossiping.handlePiece(esp);
        }
    }

    /**
     * Updates the ratio of random neighbours depending on the current stream
     * quality.
     *
     * @param p
     */
    private void updateRandomNeighbours() {
        float minimumBufferLengthInSeconds = 4.0f;
        // allow buffer to fill upp during first rounds
//        if ((p.getParentId() - 5) < 0) {
//            return;
//        }
        float currentBufferLength = io.getCurrentBufferLength() / LSConfig.VIDEO_CYCLE;
        if ((gossiping.getDownloaded() == 0) || (io.getMissedPieces() > 0) || (currentBufferLength < minimumBufferLengthInSeconds)) {
            neighbours.incRandom(true);
        } else {
            neighbours.incRandom(false);
        }

    }

    private void incDownloaded(VodAddress a) {
        short distance = neighbours.getDistanceTo(a);
        if (distance == 0) {
            VideoStats.instance(self).incDownloadedSubPiecesIntraAs();
        } else if (distance == 1) {
            VideoStats.instance(self).incDownloadedSubPiecesNeighbourAs();
        } else {
            VideoStats.instance(self).incDownloadedSubPiecesOtherAs();
        }
    }

    @Override
    public void stop(Stop event) {
        if (videoCycleTimeoutId != null) {
            CancelPeriodicTimeout cancelTimeout = new CancelPeriodicTimeout(videoCycleTimeoutId);
            delegator.doTrigger(cancelTimeout, timer);
        }
        for (TimeoutId tid : gossipTimeoutIds) {
            CancelPeriodicTimeout cancelTimeout = new CancelPeriodicTimeout(tid);
            delegator.doTrigger(cancelTimeout, timer);
        }
        io.close();
    }

    public Self getSelf() {
        return self;
    }

    /**
     * Called when a piece is ready or skipped -- schedules the removal of its
     * subpieces. (they may still be needed for gossiping for a while)
     *
     * @param id
     */
    public void triggerRemoval(Integer id) {
        // maximum time until piece is not needed
        long delay = LSConfig.VIDEO_PIECE_REQUEST_RETRIES * LSConfig.VIDEO_PIECE_REQUEST_TIMEOUT;
        ScheduleTimeout st = new ScheduleTimeout(delay);
        st.setTimeoutEvent(new RemovalTimer(st, id));
        delegator.doTrigger(st, timer);
    }
    
    public int getRoundCount() {
        return roundCount;
    }    

    public static final class RemovalTimer extends Timeout {

        private final int pieceId;

        public RemovalTimer(ScheduleTimeout st, int pieceId) {
            super(st);
            this.pieceId = pieceId;
        }

        public int getPieceId() {
            return pieceId;
        }
    }
}
