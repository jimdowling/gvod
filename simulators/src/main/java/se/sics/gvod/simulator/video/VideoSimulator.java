package se.sics.gvod.simulator.video;

import se.sics.gvod.simulator.common.PeerFail;
import se.sics.gvod.simulator.common.PeerChurn;
import se.sics.gvod.simulator.common.StartCollectData;
import se.sics.gvod.simulator.common.PeerJoin;
import se.sics.gvod.simulator.common.StopCollectData;
import se.sics.gvod.simulator.common.GenerateReport;
import java.net.InetAddress;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.sics.ipasdistances.AsIpGenerator;
import se.sics.gvod.address.Address;
import se.sics.gvod.bootstrap.port.Rebootstrap;
import se.sics.gvod.bootstrap.port.RebootstrapResponse;
import se.sics.gvod.common.*;
import se.sics.gvod.config.VodConfig;
import se.sics.gvod.croupier.Croupier;
import se.sics.gvod.config.CroupierConfiguration;
import se.sics.gvod.croupier.CroupierPort;
import se.sics.gvod.croupier.events.CroupierInit;
import se.sics.gvod.common.evts.JoinCompleted;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.net.VodNetwork;
import se.sics.gvod.net.Nat;
import se.sics.gvod.config.ParentMakerConfiguration;
import se.sics.gvod.simulator.common.ConsistentHashtable;
import se.sics.gvod.config.CroupierCompositeConfiguration;
import se.sics.gvod.croupier.snapshot.CroupierStats;
import se.sics.gvod.filters.MsgDestFilterNodeId;
import se.sics.gvod.ls.interas.InterAs;
import se.sics.gvod.ls.interas.InterAsInit;
import se.sics.gvod.ls.interas.InterAsPort;
import se.sics.gvod.ls.interas.snapshot.InterAsStats;
import se.sics.gvod.ls.system.LSConfig;
import se.sics.gvod.ls.video.Video;
import se.sics.gvod.config.VideoConfiguration;
import se.sics.gvod.ls.video.VideoInit;
import se.sics.gvod.ls.video.snapshot.Experiment;
import se.sics.gvod.ls.video.snapshot.StatsRestClient;
import se.sics.gvod.ls.video.snapshot.VideoStats;
import se.sics.gvod.config.InterAsConfiguration;
import se.sics.gvod.croupier.PeerSamplePort;
import se.sics.gvod.croupier.events.CroupierJoin;
import se.sics.gvod.timer.SchedulePeriodicTimeout;
import se.sics.gvod.timer.Timer;
import se.sics.kompics.Component;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Kompics;
import se.sics.kompics.Positive;
import se.sics.kompics.Stop;

public final class VideoSimulator extends ComponentDefinition {

    private final static int EXPERIMENT_TIME = 1 * 5000 * 1000;
    Positive<VideoSimulatorPort> simulator = positive(VideoSimulatorPort.class);
    Positive<VodNetwork> network = positive(VodNetwork.class);
    Positive<Timer> timer = positive(Timer.class);
    Positive<CroupierPort> croupierPort = positive(CroupierPort.class);
    private static final Logger logger = LoggerFactory.getLogger(VideoSimulator.class);
    private final HashMap<Integer, Component> publicCroupierPeers;
    private final HashMap<Integer, Component> privateCroupierPeers;
    private final HashMap<Integer, VodAddress.NatType> peerTypes;
    private final HashMap<Integer, VodAddress> privateAddress;
    private final HashMap<Integer, VodAddress> publicAddress;
    // peer initialization state
    private CroupierConfiguration croupierConfiguration;
    private ParentMakerConfiguration parentMakerConfiguration;
    private InterAsConfiguration interAsConfiguration;
    private VideoConfiguration videoConfiguration;
    private int peerIdSequence;
    private ConsistentHashtable<Integer> view;
    private Set<Integer> usedIds;
    private long seed;
    private int sourceId;
    private Random rnd;
    private AsIpGenerator ipGenerator;
    private String compName = "VideoSimulator ";
    private StatsRestClient monitoringClient;
//    private PrefixMatcher pm = PrefixMatcher.getInstance();
//-------------------------------------------------------------------	
    private final HashMap<Integer, Component> publicInterAsPeers;
    private final HashMap<Integer, Component> privateVideoPeers;
    private final HashMap<Integer, Component> privateInterAsPeers;
    private final HashMap<Integer, Component> publicVideoPeers;

    public VideoSimulator() {
        publicCroupierPeers = new HashMap<Integer, Component>();
        privateCroupierPeers = new HashMap<Integer, Component>();
        publicInterAsPeers = new HashMap<Integer, Component>();
        privateInterAsPeers = new HashMap<Integer, Component>();
        publicVideoPeers = new HashMap<Integer, Component>();
        privateVideoPeers = new HashMap<Integer, Component>();
        privateAddress = new HashMap<Integer, VodAddress>();
        publicAddress = new HashMap<Integer, VodAddress>();
        peerTypes = new HashMap<Integer, VodAddress.NatType>();
        view = new ConsistentHashtable<Integer>();
        // usedIds used to prevent a joining node to have the same ID as an old one
        // (desirable when simulating churn)
        usedIds = new HashSet<Integer>();

        subscribe(handleInit, control);

        subscribe(handleGenerateReport, timer);

        subscribe(handleVideoSourceJoin, simulator);

        subscribe(handleVideoPeerJoin, simulator);
        subscribe(handleVideoPeerFail, simulator);
        subscribe(handleVideoPeerChurn, simulator);

        subscribe(handleStartCollectData, simulator);
        subscribe(handleStopCollectData, simulator);

        subscribe(handleCroupierJoinCompleted, croupierPort);
    }
//-------------------------------------------------------------------	
    Handler<VideoSimulatorInit> handleInit = new Handler<VideoSimulatorInit>() {

        @Override
        public void handle(VideoSimulatorInit init) {
            seed = init.getVideoConfiguration().getSeed();
            rnd = new Random(seed);
            publicCroupierPeers.clear();
            privateCroupierPeers.clear();
            peerIdSequence = 100;
            ipGenerator = AsIpGenerator.getInstance(seed);
            croupierConfiguration = init.getCroupierConfiguration();
            parentMakerConfiguration = init.getParentMakerConfiguration();
            interAsConfiguration = init.getInterAsConfiguration();
            videoConfiguration = init.getVideoConfiguration();

            // generate periodic report
            SchedulePeriodicTimeout spt = new SchedulePeriodicTimeout(CroupierCompositeConfiguration.SNAPSHOT_PERIOD, CroupierCompositeConfiguration.SNAPSHOT_PERIOD);
            spt.setTimeoutEvent(new GenerateReport(spt));
            trigger(spt, timer);
            System.out.println();

            if (LSConfig.hasMonitorUrlSet()) {
                monitoringClient = new StatsRestClient(LSConfig.getMonitorServerUrl());
                Experiment e = monitoringClient.findExperiment("" + LSConfig.getExperimentId());
                e.setIterations((short) LSConfig.getExperimentIteration());
                e.setStatus(Experiment.Status.running);
                monitoringClient.editExperiment(e);
            }
        }
    };
//-------------------------------------------------------------------	
    Handler<SourceJoin> handleVideoSourceJoin = new Handler<SourceJoin>() {

        @Override
        public void handle(SourceJoin sourceJoinEvent) {
            logger.debug(compName + "handleVideoSourceJoin");
            Integer id = sourceJoinEvent.getPeerId();
            VodAddress.NatType peerType = sourceJoinEvent.getPeerType();
            Integer option = sourceJoinEvent.getOption();
            source(id, peerType, option);
        }
    };
//-------------------------------------------------------------------	
    Handler<PeerJoin> handleVideoPeerJoin = new Handler<PeerJoin>() {

        @Override
        public void handle(PeerJoin event) {
            logger.debug(compName + "handleCropupierPeerJoin");
            Integer id = event.getPeerId();
            VodAddress.NatType peerType = event.getPeerType();
            join(id, peerType);
        }
    };
//-------------------------------------------------------------------	
    Handler<PeerFail> handleVideoPeerFail = new Handler<PeerFail>() {

        @Override
        public void handle(PeerFail event) {
            Integer id = event.getPeerId();
            VodAddress.NatType peerType = event.getPeerType();

            fail(id, peerType);
        }
    };
//-------------------------------------------------------------------	
    Handler<PeerChurn> handleVideoPeerChurn = new Handler<PeerChurn>() {

        @Override
        public void handle(PeerChurn event) {
            VodAddress.NatType peerType;
            Integer id = event.getPeerId();
            int operation = event.getOperation();
            double privateNodesRatio = event.getPrivateNodesRatio();
            int operationCount = Math.abs(operation);

            if (operation > 0) { // join
                for (int i = 0; i < operationCount; i++) {
                    if (rnd.nextDouble() < privateNodesRatio) {
                        peerType = VodAddress.NatType.NAT;
                    } else {
                        peerType = VodAddress.NatType.OPEN;
                    }

                    join(id, peerType);
                }
            } else { // failure				
                for (int i = 0; i < operationCount; i++) {
                    if (rnd.nextDouble() < privateNodesRatio) {
                        peerType = VodAddress.NatType.NAT;
                    } else {
                        peerType = VodAddress.NatType.OPEN;
                    }

                    fail(id, peerType);
                }
            }
        }
    };

//-------------------------------------------------------------------	
    private Component createAndStartNewPeer(Integer id, VodAddress.NatType natType, boolean videoSource) {
        Component video = create(Video.class);
        Component interAs = create(InterAs.class);
        Component croupier = create(Croupier.class);
//        Component parentMaker = create(ParentMaker.class);
//        Component natTraverser = create(NatTraverser.class);
//        Component natGateway = create(DistributedNatGatewayEmulator.class);
//        Component portReservoir = create(PortReservoirComp.class);

        InetAddress ip = ipGenerator.generateIP();
        Address peerAddress = new Address(ip, 8081, id);

        Nat nat;
        InetAddress natIp;
        if (natType == VodAddress.NatType.OPEN) {
            natIp = ip;
            nat = new Nat(Nat.Type.OPEN);
            publicCroupierPeers.put(id, croupier);
            publicInterAsPeers.put(id, interAs);
            publicVideoPeers.put(id, video);
//            trigger(new DistributedNatGatewayEmulatorInit(new Nat(Nat.Type.OPEN),
//                    natIp, 10000, 65000), natGateway.control());
        } else {
            natIp = ipGenerator.generateIP();
            nat = new NatFactory(seed).getProbabilisticNat();
//            trigger(new DistributedNatGatewayEmulatorInit(nat, natIp, 50000, 65000), natGateway.control());
//            trigger(new PortInit(seed), portReservoir.control());
            privateCroupierPeers.put(id, croupier);
            privateInterAsPeers.put(id, interAs);
            privateVideoPeers.put(id, video);
        }
        VodAddress videoPeerAddress = new VodAddress(peerAddress,
                VodConfig.SYSTEM_OVERLAY_ID, nat);



        // Enable networking
        // Only NATs
//        connect(natGateway.getPositive(VodNetwork.class),
//                video.getNegative(VodNetwork.class));
//        connect(natGateway.getPositive(VodNetwork.class),
//                interAs.getNegative(VodNetwork.class));
//        connect(natGateway.getPositive(VodNetwork.class),
//                croupier.getNegative(VodNetwork.class), new MsgDestFilterOverlayId(VodConfig.SYSTEM_OVERLAY_ID));
//        connect(network, natGateway.getNegative(VodNetwork.class),
//                new MsgDestFilterNodeId(peerAddress.getId()));
                
        // NATs and Hole punching
//        connect(natTraverser.getPositive(VodNetwork.class),
//                video.getNegative(VodNetwork.class));
//        connect(natTraverser.getPositive(VodNetwork.class),
//                interAs.getNegative(VodNetwork.class));
//        connect(natTraverser.getPositive(VodNetwork.class),
//                croupier.getNegative(VodNetwork.class), new MsgDestFilterOverlayId(VodConfig.SYSTEM_OVERLAY_ID));
//        connect(natGateway.getPositive(VodNetwork.class),
//                natTraverser.getNegative(VodNetwork.class));
//        connect(natGateway.getPositive(VodNetwork.class),
//                parentMaker.getNegative(VodNetwork.class), new MsgDestFilterOverlayId(VodConfig.HP_OVERLAY_ID));
//        connect(network, natGateway.getNegative(VodNetwork.class),
//                new MsgDestFilterNodeId(peerAddress.getId()));

        // No NATs
        connect(network,
                interAs.getNegative(VodNetwork.class), new MsgDestFilterNodeId(peerAddress.getId()));
        connect(network,
                video.getNegative(VodNetwork.class), new MsgDestFilterNodeId(peerAddress.getId()));
        connect(network,
                croupier.getNegative(VodNetwork.class), new MsgDestFilterNodeId(peerAddress.getId()));

        // Enable timed events
        connect(timer, video.getNegative(Timer.class));
        connect(timer, interAs.getNegative(Timer.class));
        connect(timer, croupier.getNegative(Timer.class));
//        connect(timer, natTraverser.getNegative(Timer.class));
//        connect(timer, parentMaker.getNegative(Timer.class));
//        connect(timer, natGateway.getNegative(Timer.class));


//        connect(natGateway.getPositive(NatNetworkControl.class),
//                parentMaker.getNegative(NatNetworkControl.class));
//        connect(natGateway.getPositive(NatNetworkControl.class),
//                natTraverser.getNegative(NatNetworkControl.class));
//        connect(natGateway.getNegative(NatNetworkControl.class),
//                portReservoir.getPositive(NatNetworkControl.class));

        connect(croupier.getPositive(PeerSamplePort.class),
                interAs.getNegative(PeerSamplePort.class));

        connect(croupier.getPositive(PeerSamplePort.class),
                video.getNegative(PeerSamplePort.class));
        connect(interAs.getPositive(InterAsPort.class),
                video.getNegative(InterAsPort.class));

        subscribe(handleRebootstrap, croupier.getPositive(CroupierPort.class));


        Self self = new SelfImpl(videoPeerAddress);

//        trigger(new NatTraverserInit(self.clone(VodConfig.SYSTEM_OVERLAY_ID),
//                new NatTraverserConfiguration(),
//                new HpClientConfiguration(),
//                new RendezvousServerConfiguration(30 * 1000),
//                new StunClientConfiguration(),
//                new BootstrapConfiguration(peerAddress),
//                seed), natTraverser.control());

        if (natType == VodAddress.NatType.OPEN) {
            publicAddress.put(id, videoPeerAddress);
        } else {
            privateAddress.put(id, videoPeerAddress);
//            trigger(new ParentMakerInit(self.clone(VodConfig.HP_OVERLAY_ID),
//                    parentMakerConfiguration), parentMaker.control());
        }


        trigger(new CroupierInit(self.clone(VodConfig.SYSTEM_OVERLAY_ID),
                croupierConfiguration), croupier.getControl());

        trigger(new InterAsInit(self.clone(InterAs.SYSTEM_INTER_AS_OVERLAY_ID),
                interAsConfiguration.getSetsExchangePeriod(),
                interAsConfiguration.getSetsExchangeRto()), interAs.getControl());

        trigger(new VideoInit(self.clone(Video.SYSTEM_VIDEO_OVERLAY_ID),
                videoSource, videoConfiguration), video.getControl());
        if (videoSource) {
            logger.debug(compName + "creating source " + id);
        }


        trigger(new CroupierJoin(getNodes()), croupier.getPositive(CroupierPort.class));

        return video;
    }

//-------------------------------------------------------------------	
    private void stopAndDestroyPeer(Integer id) {
        Component croupier = privateCroupierPeers.get(id);
        Component interAs = privateInterAsPeers.get(id);
        Component video = privateVideoPeers.get(id);
        if (croupier == null) {
            croupier = publicCroupierPeers.get(id);
        }
        if (interAs == null) {
            interAs = publicInterAsPeers.get(id);
        }
        if (video == null) {
            video = publicVideoPeers.get(id);
        }

        trigger(new Stop(), croupier.getControl());
        trigger(new Stop(), interAs.getControl());
        trigger(new Stop(), video.getControl());

        disconnect(network, croupier.getNegative(VodNetwork.class));
        disconnect(timer, croupier.getNegative(Timer.class));
        disconnect(network, interAs.getNegative(VodNetwork.class));
        disconnect(timer, interAs.getNegative(Timer.class));
        disconnect(network, video.getNegative(VodNetwork.class));
        disconnect(timer, video.getNegative(Timer.class));

        CroupierStats.removeNode(id, VodConfig.SYSTEM_OVERLAY_ID);
        InterAsStats.removeNode(id, InterAs.SYSTEM_INTER_AS_OVERLAY_ID);
        VideoStats.removeNode(id, Video.SYSTEM_VIDEO_OVERLAY_ID);

        privateCroupierPeers.remove(id);
        publicCroupierPeers.remove(id);
        privateInterAsPeers.remove(id);
        publicInterAsPeers.remove(id);
        privateVideoPeers.remove(id);
        publicVideoPeers.remove(id);
        privateAddress.remove(id);
        publicAddress.remove(id);
        peerTypes.remove(id);

        destroy(croupier);
        destroy(interAs);
        destroy(video);
    }
    Handler<GenerateReport> handleGenerateReport = new Handler<GenerateReport>() {

        @Override
        public void handle(GenerateReport event) {
            CroupierStats.report(VodConfig.SYSTEM_OVERLAY_ID);
            InterAsStats.report(InterAs.SYSTEM_INTER_AS_OVERLAY_ID);
            VideoStats.report(Video.SYSTEM_VIDEO_OVERLAY_ID);
            if (System.currentTimeMillis() > EXPERIMENT_TIME) {
                Kompics.shutdown();
                System.exit(0);
            }
        }
    };
//-------------------------------------------------------------------	
    Handler<StartCollectData> handleStartCollectData = new Handler<StartCollectData>() {

        @Override
        public void handle(StartCollectData event) {
            CroupierStats.startCollectData();
            InterAsStats.startCollectData();
            VideoStats.startCollectData();
        }
    };
//-------------------------------------------------------------------	
    Handler<StopCollectData> handleStopCollectData = new Handler<StopCollectData>() {

        @Override
        public void handle(StopCollectData event) {
            CroupierStats.stopCollectData();
            CroupierStats.report(VodConfig.SYSTEM_OVERLAY_ID);

            InterAsStats.stopCollectData();
            InterAsStats.report(InterAs.SYSTEM_INTER_AS_OVERLAY_ID);

            VideoStats.stopCollectData();
            VideoStats.report(Video.SYSTEM_VIDEO_OVERLAY_ID);

            // experiment finished
            if (LSConfig.hasMonitorUrlSet()) {
                Experiment e = monitoringClient.findExperiment(String.valueOf(LSConfig.getExperimentId()));
                e.setStatus(Experiment.Status.finished);
                monitoringClient.editExperiment(e);
            }

            Kompics.shutdown();
            System.exit(0);
        }
    };
    Handler<Rebootstrap> handleRebootstrap = new Handler<Rebootstrap>() {

        @Override
        public void handle(Rebootstrap event) {
            logger.warn("Rebootstrapping...." + event.getId());
            Component peer = publicCroupierPeers.get(event.getId());
            if (peer == null) {
                peer = privateCroupierPeers.get(event.getId());
            }
            if (peer != null) {
                trigger(new RebootstrapResponse(event.getId(), getNodes()),
                        peer.getPositive(CroupierPort.class));
            } else {
                logger.warn("Couldn't Reboot null peer with id: " + event.getId());
            }
        }
    };
    Handler<JoinCompleted> handleCroupierJoinCompleted = new Handler<JoinCompleted>() {

        @Override
        public void handle(JoinCompleted event) {
            // TODO
        }
    };
//-------------------------------------------------------------------	

    private void source(Integer id, VodAddress.NatType peerType, Integer option) {
        // join with the next id if this id is taken
        Integer successor = view.getNode(id);
        while (usedIds.contains(id) || (successor != null && successor.equals(id))) {
            id = (id == Integer.MAX_VALUE) ? 0 : ++peerIdSequence;
            successor = view.getNode(id);
        }
        usedIds.add(id);
        sourceId = id;
        logger.debug("SOURCE@{}", id);

        Component newPeer = createAndStartNewPeer(id, peerType, true);
        view.addNode(id);
        peerTypes.put(id, peerType);
    }

//-------------------------------------------------------------------	
    private void join(Integer id, VodAddress.NatType peerType) {
        // join with the next id if this id is taken
        Integer successor = view.getNode(id);
        while (usedIds.contains(id)
                || (successor != null && successor.equals(id))) {
            id = (id == Integer.MAX_VALUE) ? 0 : ++peerIdSequence;
            successor = view.getNode(id);
        }
        usedIds.add(id);
        logger.debug("JOIN@{}", id);

        Component newPeer = createAndStartNewPeer(id, peerType, false);
        view.addNode(id);
        peerTypes.put(id, peerType);

    }

    private List<VodDescriptor> getNodes() {
        List<VodDescriptor> nodes = new ArrayList<VodDescriptor>();
        int i = 10;
        List<VodAddress> candidates = new ArrayList<VodAddress>();
        candidates.addAll(publicAddress.values());
        Collections.shuffle(candidates);
        for (VodAddress a : candidates) {
//            int asn = pm.matchIPtoAS(a.getIp().getHostAddress());
            VodDescriptor gnd = new VodDescriptor(a, new UtilityVod(0), 0, 1500);
            nodes.add(gnd);
            if (--i == 0) {
                break;
            }
        }
        return nodes;
    }

//-------------------------------------------------------------------	
    private void fail(Integer id, VodAddress.NatType peerType) {
        id = view.getNode(id);
        // don't want to kill the source
        if (id != sourceId) {
            logger.debug("FAIL@" + id);

            if (view.size() == 0) {
                System.err.println("Empty network");
                return;
            }
            view.removeNode(id);
            stopAndDestroyPeer(id);
        }
    }
}
