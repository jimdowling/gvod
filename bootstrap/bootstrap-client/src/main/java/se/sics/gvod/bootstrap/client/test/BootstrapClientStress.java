/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.bootstrap.client.test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import se.sics.gvod.bootstrap.client.BootstrapClient;
import se.sics.gvod.bootstrap.client.BootstrapClientInit;
import se.sics.gvod.bootstrap.port.BootstrapPort;
import se.sics.gvod.bootstrap.port.BootstrapRequest;
import se.sics.gvod.bootstrap.port.BootstrapResponse;
import se.sics.gvod.common.Utility;
import se.sics.gvod.net.VodNetwork;
import se.sics.kompics.Component;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Fault;
import se.sics.kompics.Handler;
import se.sics.kompics.Kompics;
import se.sics.gvod.address.Address;
import se.sics.gvod.bootstrap.port.AddOverlayRequest;
import se.sics.gvod.bootstrap.port.AddOverlayResponse;
import se.sics.gvod.bootstrap.port.BootstrapHeartbeat;
import se.sics.gvod.common.SelfNoParents;
import se.sics.gvod.common.UtilityVod;
import se.sics.gvod.common.util.ToVodAddr;
import se.sics.gvod.config.BootstrapConfiguration;
import se.sics.gvod.config.VodConfig;
import se.sics.gvod.net.Nat;
import se.sics.kompics.nat.utils.getip.ResolveIp;
import se.sics.kompics.nat.utils.getip.ResolveIpPort;
import se.sics.kompics.nat.utils.getip.events.GetIpRequest;
import se.sics.kompics.nat.utils.getip.events.GetIpResponse;
import se.sics.gvod.net.NettyInit;
import se.sics.gvod.net.NettyNetwork;
import se.sics.gvod.net.VodMsgFrameDecoder;
import se.sics.gvod.timer.ScheduleTimeout;
import se.sics.gvod.timer.Timeout;
import se.sics.gvod.timer.Timer;
import se.sics.gvod.timer.java.JavaTimer;

/**
 *
 * @author jdowling
 */
public class BootstrapClientStress extends ComponentDefinition {

    public static enum ExperimentStep {

        GET_OPEN_NODES,
        //        INSERT_TORRENT, 
        GET_TORRENT_NODES
    };
    private static int delayBetweenBootstraps;
    private static int numClients;
    private Component timer;
    private Component resolveIp;
    private List<Component> networkComponents = new ArrayList<Component>();
    private List<Component> bootstrapClients = new ArrayList<Component>();
    private LinkedList<Long> durations = new LinkedList<Long>();
    private int overlay;
    private Utility utility = new UtilityVod(1, 2, 3);
    private int openResponses = 0;
    private int torrentPeersResponses = 0;
    private int addOverlayResponses = 0;
    private static String server;
    private static Address serverAddr;
    private static InetAddress ip;
    private long baseTime = System.currentTimeMillis();
    private ExperimentStep step = ExperimentStep.GET_OPEN_NODES;
    private int succeededOpenBootstraps = 0;
    private int failedOpenBootstraps = 0;
    private int succeededOverlayBootstraps = 0;
    private int failedOverlayBootstraps = 0;

    public static class StressTimeout extends Timeout {

        private final int clientNum;

        public StressTimeout(ScheduleTimeout st, int clientNum) {
            super(st);
            this.clientNum = clientNum;
        }

        public StressTimeout(ScheduleTimeout st, int clientNum, ExperimentStep state) {
            super(st);
            this.clientNum = clientNum;
        }

        public int getClientNum() {
            return clientNum;
        }
    }

    public static void main(String[] args) {
//        if (args.length != 4) {
//            System.out.println("Usage: host numWorkerThreads numClients delay");
//            return;
//        }
//        server = args[0];
//        int numWorkers = Integer.parseInt(args[1]);
//        numClients = Integer.parseInt(args[2]);
//        delayBetweenBootstraps = Integer.parseInt(args[3]);
        server = "b00t.info";
        int numWorkers = 4;
        numClients = 4;
        delayBetweenBootstraps = 2000;
        System.setProperty("java.net.preferIPv4Stack", "true");
        Kompics.createAndStart(BootstrapClientStress.class, numWorkers);

    }

    public BootstrapClientStress() {
        timer = create(JavaTimer.class);
        resolveIp = create(ResolveIp.class);

        subscribe(handleStressTimeout, timer.getPositive(Timer.class));
        subscribe(handleGetIpResponse, resolveIp.getPositive(ResolveIpPort.class));

        // EnumSet.of(NetworkInterfacesMask.IGNORE_LOOPBACK)
        trigger(new GetIpRequest(false), resolveIp.getPositive(ResolveIpPort.class));

    }
    public Handler<GetIpResponse> handleGetIpResponse = new Handler<GetIpResponse>() {
        @Override
        public void handle(GetIpResponse event) {
            int seed = 100;
            ip = null;
            InetAddress serverIp = null;
            short mtu;
            Random r = new Random(System.currentTimeMillis());
            overlay = r.nextInt();
            try {
                ip = event.getBoundIp();
                //                mtu = (short) event.getFirstAddress().getMtu();
                mtu = (short) 1500;
                java.util.logging.Logger.getLogger(BootstrapClientStress.class.getName()).log(
                        Level.INFO, "Resolving server ip " + server + ": " + (System.currentTimeMillis() - baseTime));
                serverIp = InetAddress.getByName(server);
                java.util.logging.Logger.getLogger(BootstrapClientStress.class.getName()).log(
                        Level.INFO, "MTU " + mtu);
            } catch (UnknownHostException ex) {
                Logger.getLogger(BootstrapClientStress.class.getName()).log(Level.SEVERE, ex.getMessage());
            }

            serverAddr = new Address(serverIp, VodConfig.DEFAULT_BOOTSTRAP_PORT,
                    VodConfig.SYSTEM_OVERLAY_ID);
            int evictAfter = 30 * 1000, retryPeriod = 20 * 1000, retryCount = 1,
                    clientKeepAlive = 30 * 1000;
            BootstrapConfiguration config = BootstrapConfiguration.build()
                    .setBootstrapServerAddress(serverAddr);
            for (int i = 50000; i < 50000 + numClients; i++) {
                Address self = new Address(ip, i, i);
                Component network = create(NettyNetwork.class);
                networkComponents.add(network);

                Component bc = create(BootstrapClient.class);
                bootstrapClients.add(bc);
                subscribe(handleBootstrapResponse, bc.getPositive(BootstrapPort.class));
                subscribe(handleAddOverlayResp, bc.getPositive(BootstrapPort.class));
                subscribe(handleFault, network.getControl());

                connect(bc.getNegative(Timer.class), timer.getPositive(Timer.class));
                connect(bc.getNegative(VodNetwork.class), network.getPositive(VodNetwork.class));

                trigger(new NettyInit(VodConfig.getSeed(), true,
                        VodMsgFrameDecoder.class), network.getControl());

                trigger(new BootstrapClientInit(
                        new SelfNoParents(ToVodAddr.systemAddr(self)),
                        config), bc.control());

            }


            // send a bootstrap request for open peers to the first bootstrap client.
            Address b = VodConfig.getBootstrapServer();
            if (b != null) {
                AddOverlayRequest req = new AddOverlayRequest(b,
                        overlay, "test", "Some desc here", new byte[]{'a', 'b'}, null);
                trigger(req, bootstrapClients.get(0).getPositive(BootstrapPort.class));
            }
        }
    };
    public Handler<AddOverlayResponse> handleAddOverlayResp = new Handler<AddOverlayResponse>() {
        @Override
        public void handle(AddOverlayResponse event) {

            java.util.logging.Logger.getLogger(BootstrapClientStress.class.getName()).log(Level.INFO,
                    "AddOverlayResponse: " + event.isSucceeded());


//            if (++addOverlayResponses >= numClients) {
            ScheduleTimeout st = new ScheduleTimeout(delayBetweenBootstraps);
            st.setTimeoutEvent(new StressTimeout(st, addOverlayResponses++));
            trigger(st, timer.getPositive(Timer.class));
//            }

        }
    };
    public Handler<Fault> handleFault = new Handler<Fault>() {
        @Override
        public void handle(Fault event) {
            System.out.print(event.getFault().getMessage());
            Kompics.shutdown();
            System.exit(-1);
        }
    };
    public Handler<StressTimeout> handleStressTimeout = new Handler<StressTimeout>() {
        @Override
        public void handle(StressTimeout event) {
            if (step == ExperimentStep.GET_OPEN_NODES) {
                for (int i = 0; i < numClients; i++) {
                    Component bc = bootstrapClients.get(i);
                    trigger(new BootstrapRequest(VodConfig.SYSTEM_OVERLAY_ID),
                            bc.getPositive(BootstrapPort.class));
                    durations.addFirst(System.currentTimeMillis());
                }
            } else if (step == ExperimentStep.GET_TORRENT_NODES) {
                for (int i = 0; i < numClients; i++) {
                    Component bc = bootstrapClients.get(torrentPeersResponses++);
                    trigger(new BootstrapRequest(overlay, utility),
                            bc.getPositive(BootstrapPort.class));
                    durations.addFirst(System.currentTimeMillis());
                }
            }
        }
    };
    public Handler<BootstrapResponse> handleBootstrapResponse = new Handler<BootstrapResponse>() {
        @Override
        public void handle(BootstrapResponse event) {

            long duration = System.currentTimeMillis() - durations.getLast();

            Logger.getLogger(BootstrapClientStress.class.getName()).log(Level.INFO,
                    "Response " + openResponses + " with " + event.getPeers().size()
                    + " peers. Time taken: " + duration);
            durations.removeLast();

            if (step == ExperimentStep.GET_OPEN_NODES) {
                if (event.bootstrapSucceeded()) {
                    succeededOpenBootstraps++;
                } else {
                    failedOpenBootstraps++;
                }

                Set<Integer> seeds = new HashSet<Integer>();
                seeds.add(overlay);
                Map<Integer, Integer> downloaders = new HashMap<Integer, Integer>();
                trigger(new BootstrapHeartbeat(false,
                        (short) 1500, seeds, downloaders,
                        ToVodAddr.systemAddr(new Address(ip, 50000, 50000))),
                        bootstrapClients.get(openResponses++).getPositive(BootstrapPort.class));
                if (openResponses >= numClients) {
                    step = ExperimentStep.GET_TORRENT_NODES;
                    ScheduleTimeout st = new ScheduleTimeout(1000);
                    st.setTimeoutEvent(new StressTimeout(st, -1));
                    trigger(st, timer.getPositive(Timer.class));
                }
            } else if (step == ExperimentStep.GET_TORRENT_NODES) {
                if (event.bootstrapSucceeded()) {
                    succeededOverlayBootstraps++;
                } else {
                    failedOverlayBootstraps++;
                }

                if (++torrentPeersResponses >= numClients) {
                    Logger.getLogger(BootstrapClientStress.class.getName()).log(Level.INFO,
                            "Finished experiment");
                    Logger.getLogger(BootstrapClientStress.class.getName()).log(Level.INFO,
                            "Succeeded Open Bootstraps :" + succeededOpenBootstraps);
                    Logger.getLogger(BootstrapClientStress.class.getName()).log(Level.INFO,
                            "Failed Open Bootstraps :" + failedOpenBootstraps);
                    Logger.getLogger(BootstrapClientStress.class.getName()).log(Level.INFO,
                            "Succeeded Overlay Bootstraps :" + succeededOverlayBootstraps);
                    Logger.getLogger(BootstrapClientStress.class.getName()).log(Level.INFO,
                            "Failed Overlay Bootstraps :" + failedOverlayBootstraps);
                    Kompics.shutdown();
                    System.exit(0);
                }
            }
        }
    };
}
