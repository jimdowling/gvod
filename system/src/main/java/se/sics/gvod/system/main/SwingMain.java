package se.sics.gvod.system.main;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import org.mortbay.util.URIUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.gvod.address.Address;
import se.sics.gvod.bootstrap.client.BootstrapClient;
import se.sics.gvod.bootstrap.client.BootstrapClientInit;
import se.sics.gvod.bootstrap.port.AddOverlayRequest;
import se.sics.gvod.bootstrap.port.AddOverlayResponse;
import se.sics.gvod.bootstrap.port.BootstrapHeartbeat;
import se.sics.gvod.bootstrap.port.BootstrapPort;
import se.sics.gvod.bootstrap.port.BootstrapRequest;
import se.sics.gvod.bootstrap.port.BootstrapResponse;
import se.sics.gvod.bootstrap.port.BootstrapResponseFilterOverlayId;
import se.sics.gvod.common.Self;
import se.sics.gvod.common.SelfImpl;
import se.sics.gvod.common.VodDescriptor;
import se.sics.gvod.config.BootstrapConfiguration;
import se.sics.gvod.config.VodConfig;
import se.sics.gvod.common.util.ToVodAddr;
import se.sics.gvod.config.CroupierConfiguration;
import se.sics.gvod.common.util.CachedNatType;
import se.sics.gvod.config.HpClientConfiguration;
import se.sics.gvod.config.RendezvousServerConfiguration;
import se.sics.gvod.nat.traversal.NatTraverser;
import se.sics.gvod.config.NatTraverserConfiguration;
import se.sics.gvod.nat.traversal.events.NatTraverserInit;
import se.sics.gvod.net.Nat;
import se.sics.gvod.net.NettyInit;
import se.sics.gvod.net.NettyNetwork;
import se.sics.gvod.net.VodNetwork;
import se.sics.gvod.filters.MsgDestFilterOverlayId;
import se.sics.gvod.filters.TimeoutOverlayIdFilter;
import se.sics.gvod.net.NatNetworkControl;
import se.sics.gvod.config.ParentMakerConfiguration;
import se.sics.gvod.config.StunClientConfiguration;
import se.sics.gvod.config.StunServerConfiguration;
import se.sics.gvod.croupier.Croupier;
import se.sics.gvod.croupier.CroupierPort;
import se.sics.gvod.croupier.PeerSamplePort;
import se.sics.gvod.croupier.events.CroupierInit;
import se.sics.gvod.croupier.events.CroupierJoin;
import se.sics.gvod.filters.MsgDestFilterNodeId;
import se.sics.gvod.nat.traversal.NatTraverserPort;
import se.sics.gvod.net.Transport;
import se.sics.gvod.net.VodMsgFrameDecoder;
import se.sics.gvod.net.events.BandwidthStats;
import se.sics.gvod.net.events.PortBindRequest;
import se.sics.gvod.net.events.PortBindResponse;
import se.sics.gvod.stun.client.events.GetNatTypeResponse;
import se.sics.gvod.system.storage.StorageMemMapWholeFile;
import se.sics.gvod.system.net.ControlServer;
import se.sics.gvod.system.peer.VodPeer;
import se.sics.gvod.system.peer.VodPeerInit;
import se.sics.gvod.system.peer.VodPeerPort;
import se.sics.gvod.system.peer.events.ChangeUtility;
import se.sics.gvod.system.peer.events.QuitCompleted;
import se.sics.gvod.system.peer.events.ReportDownloadSpeed;
import se.sics.gvod.system.peer.events.SlowBackground;
import se.sics.gvod.system.peer.events.SpeedBackground;
import se.sics.gvod.system.storage.Storage;
import se.sics.gvod.system.storage.StorageFcByteBuf;
import se.sics.gvod.system.swing.SwingComponent;
import se.sics.gvod.system.swing.evts.UserInterfaceInit;
import se.sics.gvod.system.util.ActiveTorrents;
import se.sics.gvod.system.util.ActiveTorrentsException;
import se.sics.gvod.system.util.BrowserGuiHandler;
import se.sics.gvod.system.util.FileUtils;
import se.sics.gvod.system.util.JQTFaststart;
import se.sics.gvod.system.util.JwHttpServer;
import se.sics.gvod.system.vod.Vod;
import se.sics.gvod.system.vod.VodConfiguration;
import se.sics.gvod.system.vod.WebRequestFilterOverlayId;
import se.sics.gvod.timer.SchedulePeriodicTimeout;
import se.sics.gvod.timer.ScheduleTimeout;
import se.sics.gvod.timer.Timeout;
import se.sics.gvod.timer.Timer;
import se.sics.gvod.timer.java.JavaTimer;
import se.sics.gvod.web.server.VodMonitorConfiguration;
import se.sics.ipasdistances.PrefixMatcher;
import se.sics.kompics.Component;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Fault;
import se.sics.kompics.Handler;
import se.sics.kompics.Kompics;
import se.sics.kompics.nat.utils.getip.ResolveIp;
import se.sics.kompics.nat.utils.getip.ResolveIpPort;
import se.sics.kompics.nat.utils.getip.events.GetIpRequest;
import se.sics.kompics.nat.utils.getip.events.GetIpRequest.NetworkInterfacesMask;
import se.sics.kompics.nat.utils.getip.events.GetIpResponse;
import se.sics.kompics.web.Web;
import se.sics.kompics.web.jetty.JettyWebServer;

/**
 *
 * @author jim
 */
public class SwingMain extends ComponentDefinition implements GMain {

    static boolean isOpenServer = false;
    static boolean offline = false;
    static String openServerIp;
    static int openServerId;
    static int seed;
    static int myId = -1;
    static InetAddress monitorIp;
    static SwingMain main;
    private static long appStartTime = System.currentTimeMillis();

    public static int PLAYER = 0;
    private static final Logger logger = LoggerFactory.getLogger(SwingMain.class);

    private Component timer;
    private Component network;
    private Component globalCroupier;
    private Component natTraverser;
    private Component web;
    private Component resolveIp;
    private Component userInterface;
    private Component bootstrap;
    private static Random random;
    private int numBootstrapAttempts = 0;
    private int natTypeRequestTimeout = 10 * 1000;
    private InetAddress upnpIpAddress = null;
    private int upnpPort = 0;
    private List<VodDescriptor> servers = new ArrayList<VodDescriptor>();
    private PrefixMatcher pm = PrefixMatcher.getInstance();
    AtomicInteger downloadSpeed = new AtomicInteger(0);
    AtomicInteger uploadSpeed = new AtomicInteger(0);
    AtomicInteger totalDownload = new AtomicInteger(0);
    AtomicBoolean initialized = new AtomicBoolean(false);
    ControlServer cs;
    Self self;

    public static final class BootServerKeepAlive extends Timeout {

        public BootServerKeepAlive(SchedulePeriodicTimeout request) {
            super(request);
        }
    }

    public static final class RebootstrapMain extends Timeout {

        public RebootstrapMain(ScheduleTimeout request) {
            super(request);
        }
    }

    public static class MainPortBindResponse extends PortBindResponse {

        public MainPortBindResponse(PortBindRequest request) {
            super(request);
        }
    }

    public static void main(String[] args) {
        try {
            StringBuilder sb = new StringBuilder();

            for (String s : args) {
                sb.append(s).append(" ");
            }
            if (args.length > 0 && args[0].compareToIgnoreCase("true") == 0) {
                int numArgs = 3;
                isOpenServer = true;
                myId = Integer.parseInt(args[1]);
                String serverStr = args[2];
                int idx = serverStr.lastIndexOf("@");
                if (idx == -1) {
                    logger.info("id@ip is the correct format. Your address format was incorrect.");
                    System.exit(-1);
                }
                openServerIp = serverStr.substring(idx + 1);
                openServerId = Integer.parseInt(serverStr.substring(0, idx));
                String[] updatedArgs = new String[args.length - numArgs];
                System.arraycopy(args, numArgs, updatedArgs, 0, updatedArgs.length);
                args = updatedArgs;
            }

            logger.info("java -jar gvod.jar {}", sb.toString());

            System.setProperty("java.net.preferIPv4Stack", "true");

            if (args.length > 0) {
                String firstArg = args[0];
                // If the 1st arg is a torrent, it won't start with '-'
                // It will start with "http://" or "file://"
                if (firstArg.startsWith("http") || firstArg.startsWith("file")
                        || firstArg.startsWith("ftp")) {
                    String[] updatedArgs = new String[args.length + 1];
                    updatedArgs[0] = "-torrent";
                    System.arraycopy(args, 0, updatedArgs, 1, args.length);
                    args = updatedArgs;
                }
            }

            VodConfig.init(args);
            logger.info((System.currentTimeMillis() - appStartTime) +
                    ": Config params initialized ");

        } catch (IOException ex) {
            logger.warn(ex.getMessage());
            Kompics.shutdown();
            System.exit(-1);
        }

        System.setProperty("java.net.preferIPv4Stack", "true");

        seed = VodConfig.getSeed();
        random = new Random(seed);
        PLAYER = VodConfig.getMediaPlayer();

        if (PLAYER != 0 && PLAYER != 1) {
            throw new IllegalArgumentException("Invalid player number: " + PLAYER);
        }

        Kompics.createAndStart(SwingMain.class, VodConfig.getNumWorkers());

        logger.info((System.currentTimeMillis() - appStartTime) +
                ": Kompics Started");

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {

                logger.debug("Shutdown hook was invoked. Shutting down GVod.");

                if (main != null) {
                    // TODO - this throws an exception sometimes, investigate.
                    main.stopAndDestroyAllTorrents();
                }

                try {
                    // TODO - how long should we sleep here before cleanup complete?
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                    java.util.logging.Logger.getLogger(SwingMain.class.getName()).log(Level.SEVERE, null, ex);
                }

                try {
                    Kompics.shutdown();
                } catch (Exception e) {
                    logger.warn("Error shutting down: " + e.getMessage());
                }
            }
        });
    }

    private void printTime(String msg) {
        logger.info((System.currentTimeMillis() - appStartTime) + " : " + msg);
    }

    public SwingMain() throws IOException {
        main = this;

        printTime("Constructor");
        // TODO - small period of vulnerability here, where requests arrive at the ControlServer,
        // but we haven't bootstraped the main node yet.
        try {
            cs = new ControlServer(main);
            cs.start();
        } catch (IOException ex) {
            logger.warn("Problem binding to control server port :" + VodConfig.DEFAULT_CONTROL_PORT);
            logger.error(ex.getMessage());
            Kompics.shutdown();
            System.exit(-1);
        }
        printTime("ControlServer");

        timer = create(JavaTimer.class);
        natTraverser = create(NatTraverser.class);
        network = create(NettyNetwork.class);
        web = create(JettyWebServer.class);
        resolveIp = create(ResolveIp.class);
        bootstrap = create(BootstrapClient.class);
        globalCroupier = create(Croupier.class);

        connect(network.getNegative(Timer.class), timer.getPositive(Timer.class));
        connect(resolveIp.getNegative(Timer.class), timer.getPositive(Timer.class));

        connect(bootstrap.getNegative(Timer.class), timer.getPositive(Timer.class));
        connect(bootstrap.getNegative(VodNetwork.class), network.getPositive(VodNetwork.class));

        subscribe(handleGetIpResponse, resolveIp.getPositive(ResolveIpPort.class));
        subscribe(handleMainPortBindResponse, network.getPositive(NatNetworkControl.class));

        subscribe(handleBandwidthStats, network.getPositive(NatNetworkControl.class));

        subscribe(handleFault, network.getControl());
        subscribe(handleFault, timer.getControl());
        subscribe(handleFault, web.getControl());
        subscribe(handleFault, bootstrap.getControl());
        subscribe(handleFault, resolveIp.getControl());
        subscribe(handleFault, natTraverser.getControl());
        subscribe(handleRebootstrapMain, timer.getPositive(Timer.class));
        subscribe(handleGetNatTypeResponse, natTraverser.getPositive(NatTraverserPort.class));

        getIp();

    }

    private void errorDialog(String msg) {
        logger.error(msg);

        if (VodConfig.GUI) {
            JFrame frame = new JFrame("GVod Error");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            JOptionPane.showMessageDialog(frame,
                    msg,
                    "Gvod error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exitErrorDialog(String msg) {
        errorDialog(msg);
        Kompics.shutdown();
        System.exit(-1);

    }
//-------------------------------------------------------------------    
    public Handler<GetIpResponse> handleGetIpResponse = new Handler<GetIpResponse>() {
        @Override
        public void handle(GetIpResponse event) {
            printTime("GetIpResponse");
            InetAddress ip = event.getIpAddress();

            if (ip == null || ip.isAnyLocalAddress()) {
                errorDialog("Unable to connect to the internet. GVod will run in offline mode.");
                offline = true;
                if (ip == null) {
                    try {
                        ip = InetAddress.getLocalHost();
                    } catch (UnknownHostException ex) {
                        java.util.logging.Logger.getLogger(SwingMain.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }

//            int mtu = event.getFirstAddress().getMtu();
//            if (mtu > 700 && mtu <= 1500) {
//                VodConfig.LB_MTU_MEASURED = mtu;
//            } else {
//                logger.warn("Measured mtu was out-of-range: " + mtu);
//            }
            int nodeId = (myId == -1) ? getNodeId(ip) : myId;
            VodConfig.setNodeId(nodeId);
            logger.info("nodeId={}", nodeId);

            // can only connect up nattraverser, when I know my ID.
            connect(natTraverser.getNegative(Timer.class), timer.getPositive(Timer.class));
            connect(natTraverser.getNegative(VodNetwork.class), network.getPositive(VodNetwork.class),
                    new MsgDestFilterNodeId(nodeId));
            connect(natTraverser.getNegative(NatNetworkControl.class),
                    network.getPositive(NatNetworkControl.class));
            connect(natTraverser.getNegative(PeerSamplePort.class),
                    globalCroupier.getPositive(PeerSamplePort.class));
            connect(globalCroupier.getNegative(Timer.class), timer.getPositive(Timer.class));
            connect(globalCroupier.getNegative(VodNetwork.class), natTraverser.getPositive(VodNetwork.class),
                    new MsgDestFilterOverlayId(VodConfig.SYSTEM_OVERLAY_ID));

            subscribe(handleBootstrapResponse, bootstrap.getPositive(BootstrapPort.class));
            subscribe(handleAddOverlayResponse, bootstrap.getPositive(BootstrapPort.class));
            subscribe(handleBootServerKeepAlive, timer.getPositive(Timer.class));

            Address selfAddress = new Address(ip, VodConfig.getPort(), nodeId);
            logger.info("Network component listening on={}", selfAddress);

            // Hack to make all nodes OPEN
            self = new SelfImpl(new Nat(Nat.Type.OPEN), ip, VodConfig.getPort(), nodeId, VodConfig.SYSTEM_OVERLAY_ID);

            // Pass a reference to this parent object, so that ActiveTorrents can call 'stopPeer' from it.
            ActiveTorrents.init(main);

            BootstrapConfiguration bootConfig = BootstrapConfiguration.build()
                    .setClientRetryPeriod(1000).setClientRetryCount(10);
            if (VodConfig.getBootstrapServer() != null) {
                bootConfig.setBootstrapServerAddress(VodConfig.getBootstrapServer());
            }
            trigger(new NettyInit(VodConfig.getSeed(), true, VodMsgFrameDecoder.class),
                    network.getControl());
            trigger(new BootstrapClientInit(self.clone(VodConfig.SYSTEM_OVERLAY_ID),
                    bootConfig), bootstrap.getControl());

            PortBindRequest pb1 = new PortBindRequest(selfAddress, Transport.UDP);
            MainPortBindResponse pbr1 = new MainPortBindResponse(pb1);
            trigger(pb1, network.getPositive(NatNetworkControl.class));

        }
    };
    private Handler<MainPortBindResponse> handleMainPortBindResponse
            = new Handler<MainPortBindResponse>() {
                @Override
                public void handle(MainPortBindResponse event) {
                    printTime("MainPortBind");

                    if (event.getStatus() == MainPortBindResponse.Status.SUCCESS) {
                        sendBootstrapRequest();
                    } else {
                        exitErrorDialog("Exiting. GVod or some other program is already running on port 58022. "
                                + "Cannot bind to port: " + event.getStatus());
                    }

                }
            };
//-------------------------------------------------------------------    
    /**
     * This should return the IP addresses of the CLOMMUNITY nodes.
     */
    public Handler<BootstrapResponse> handleBootstrapResponse = new Handler<BootstrapResponse>() {
        @Override
        public void handle(BootstrapResponse e) {

            printTime("Bootstrapping");

            if (e.getOverlayId() != VodConfig.SYSTEM_OVERLAY_ID) {
                logger.debug("Ignoring bootstrap response in SwingMain - destined for VodPeer.");
                // response is not destined for a VodPeer
                return;
            }

            if (e.bootstrapSucceeded() != true) {
                ScheduleTimeout st = new ScheduleTimeout(60 * 1000);
                st.setTimeoutEvent(new RebootstrapMain(st));
                trigger(st, timer.getPositive(Timer.class));
            }
            if (numBootstrapAttempts > 1) {
                return;
            }

            if (e.bootstrapSucceeded() != true && offline == false) {
                errorDialog("Could not Bootstrap. GVod will now run in offline mode, but keep trying to connect to "
                        + "the Bootstrap server to get online.");
            }
            servers = e.getPeers();
            Set<Address> openNodes = new HashSet<Address>();
            for (VodDescriptor vd : servers) {
                openNodes.add(vd.getVodAddress().getPeerAddress());
            }
            NatTraverserConfiguration ntc = NatTraverserConfiguration.build();
            HpClientConfiguration hpcc = HpClientConfiguration.build();
            StunClientConfiguration scc = StunClientConfiguration.build();
            StunServerConfiguration ssc = StunServerConfiguration.build();
            RendezvousServerConfiguration rsc = RendezvousServerConfiguration.build();
            ParentMakerConfiguration pmc = ParentMakerConfiguration.build();

            if (isOpenServer) {
                try {
                    openNodes.clear();
                    InetAddress sIp = InetAddress.getByName(openServerIp);
                    Address s = new Address(sIp, VodConfig.getPort(), openServerId);
                    openNodes.add(s);
                    servers.add(new VodDescriptor(ToVodAddr.systemAddr(s)));
                } catch (UnknownHostException ex) {
                    java.util.logging.Logger.getLogger(SwingMain.class.getName()).log(Level.SEVERE, null, ex);
                    exitErrorDialog("Couldn't resolve ip of open server: " + openServerIp);
                }
            }

            trigger(new NatTraverserInit(self, openNodes, seed,
                    ntc, hpcc, rsc, ssc, scc, pmc, VodConfig.isOpenIp()), 
                    natTraverser.getControl());

            trigger(new CroupierInit(self, CroupierConfiguration.build()),
                    globalCroupier.getControl());
            trigger(new CroupierJoin(servers), globalCroupier.getPositive(CroupierPort.class));

            startServices();
        }
    };

    Handler<BootServerKeepAlive> handleBootServerKeepAlive = new Handler<BootServerKeepAlive>() {
        @Override
        public void handle(BootServerKeepAlive event) {
            sendKeepalive();
        }
    };

    Handler<RebootstrapMain> handleRebootstrapMain = new Handler<RebootstrapMain>() {
        @Override
        public void handle(RebootstrapMain event) {
            sendBootstrapRequest();
        }
    };

    Handler<GetNatTypeResponse> handleGetNatTypeResponse = new Handler<GetNatTypeResponse>() {
        @Override
        public void handle(GetNatTypeResponse event) {
            logger.info("GetNatTypeResponse is received: " + event.getNat());

            if (event.getStatus() == GetNatTypeResponse.Status.ALL_HOSTS_TIMED_OUT) {
                logger.warn("Not able to perform NAT type identification: " + event.getStatus());
            } else if (event.getStatus() == GetNatTypeResponse.Status.FAIL) {
                logger.warn("Not able to perform NAT type identification: " + event.getStatus());
            } else if (event.getStatus() == GetNatTypeResponse.Status.SUCCEED) {
                if (event.getNat().getType() == Nat.Type.UPNP) {
                    upnpIpAddress = event.getExternalUpnpIp();
                    upnpPort = event.getMappedUpnpPort();
                }
                self.setNat(event.getNat());
                cacheNatType();
            }
//            if (!isOpenServer && !isInitialized()) {
//                startServices();
//            }
        }
    };
//-------------------------------------------------------------------    
    Handler<QuitCompleted> handleQuitCompleted = new Handler<QuitCompleted>() {
        @Override
        public void handle(QuitCompleted event) {
            logger.info("QUITCOMPLETED@" + event.getOverlayId());

//            int id = event.getGVodId();
//            stopPeer(id);
//            ActiveTorrents.removePeer(id);
        }
    };
//-------------------------------------------------------------------    
    Handler<ReportDownloadSpeed> handleDownloadSpeed = new Handler<ReportDownloadSpeed>() {
        @Override
        public void handle(ReportDownloadSpeed event) {
        }
    };
    Handler<BandwidthStats> handleBandwidthStats = new Handler<BandwidthStats>() {
        @Override
        public void handle(BandwidthStats event) {
            downloadSpeed.set(event.getLastSecBytesRead());
            uploadSpeed.set(event.getLastSecBytesWritten());
            totalDownload.set(event.getTotalBytesDownloaded());
        }
    };
//-------------------------------------------------------------------    
    Handler<StartInBackground> handleStartInBackground = new Handler<StartInBackground>() {
        @Override
        public void handle(StartInBackground event) {
            try {
                createPeer(event.getMetadataInfo(), true, false, null);
            } catch (IOException ex) {
                java.util.logging.Logger.getLogger(SwingMain.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    };
//-------------------------------------------------------------------    
    Handler<SlowBackground> handleSlowBackground = new Handler<SlowBackground>() {
        @Override
        public void handle(SlowBackground event) {
            for (Component comp : ActiveTorrents.getPeers()) {
                trigger(event, comp.getPositive(VodPeerPort.class));
            }
        }
    };
//-------------------------------------------------------------------    
    Handler<AddOverlayResponse> handleAddOverlayResponse = new Handler<AddOverlayResponse>() {
        @Override
        public void handle(AddOverlayResponse event) {
            int videoId = event.getOverlayId();
            logger.info("Video successfully added to bootstrap server: " + videoId);
            sendKeepalive();
        }
    };
//-------------------------------------------------------------------    
    Handler<Fault> handleFault = new Handler<Fault>() {
        @Override
        public void handle(Fault event) {
            for (StackTraceElement ste : event.getFault().getStackTrace()) {
                logger.error(ste.toString());
            }
            Throwable t = event.getFault();
            if (t != null) {
                if (t.getCause() != null) {
                    logger.error(t.getCause().toString());
                    exitErrorDialog("Network fault: " + t.getCause().toString());
                }
            }
            // for whatever reason, didn't call exitErrorDialog
            Kompics.shutdown();
            System.exit(-1);
        }
    };
    Handler<ReportDownloadSpeed> handleReportDownloadSpeed = new Handler<ReportDownloadSpeed>() {
        @Override
        public void handle(ReportDownloadSpeed event) {
            downloadSpeed.set(event.getNumBytesSec());
            totalDownload.addAndGet(downloadSpeed.get());
            // NEED TO TELL BROWSER THE DOWNLOAD SPEED
        }
    };
    Handler<SpeedBackground> handleSpeedBackground = new Handler<SpeedBackground>() {
        @Override
        public void handle(SpeedBackground event) {
            // TODO - need a mainPeer here.
            // Speedbackground is sent from the mainPeer to the other peers.
//            for (Component comp : mapIdPeers.values()) {
//                trigger(event, comp.getPositive(GVodPeerSampling.class));
//            }
        }
    };

    /**
     * Called by ControlThread via javascript to download a torrent, and It
     * should block until the video is ready to be viewed...
     *
     * @return true when the Vod peers have been created
     */
    public boolean isInitialized() {
        return initialized.get();
    }

    public String downloadTorrentAndCreatePeer(String torrentUrl) throws IOException {

        String videoUrl = "PLAY http://127.0.0.1:" + VodConfig.getMediaPort() + "/?view=";
        String videoName = null;
        logger.info(videoUrl);
        logger.info("Torrent URL: {}", torrentUrl);

        // 1. download the torrent, then create and launch the peer
        int lastSlash = torrentUrl.lastIndexOf("/") + 1;
        String torrentFilename = VodConfig.getTorrentDir() + File.separator + torrentUrl.substring(lastSlash, torrentUrl.length());

        if (!ActiveTorrents.existsAndValidTorrentFile(torrentFilename)) {
            logger.info("Torrent file downloaded to: " + torrentFilename);
            URL url = null;
            try {
                url = new java.net.URL(URIUtil.encodePath(torrentUrl));
                BufferedInputStream in = new BufferedInputStream(url.openStream());
                FileOutputStream fos = new java.io.FileOutputStream(torrentFilename);

                BufferedOutputStream bout = new BufferedOutputStream(fos, 1024);
                byte[] data = new byte[1024];
                int x = 0;
                while ((x = in.read(data, 0, 1024)) >= 0) {
                    bout.write(data, 0, x);
                }
                bout.close();
                in.close();
                ActiveTorrents.addTorrent(torrentFilename);
            } catch (MalformedURLException ex) {
                logger.warn(ex.getMessage());
                // in case this torrent is in our library, remove it.
                ActiveTorrents.removeTorrent(torrentFilename);
                return videoUrl + "error-url.html";
            } catch (java.net.SocketException ex) {
                logger.warn(ex.getMessage());
                // in case this torrent is in our library, remove it.
                ActiveTorrents.removeTorrent(torrentFilename);
                return videoUrl + "error-url.html";
            }
        } else {
            logger.info("Torrent file already existed and was valid: " + torrentFilename);
        }

        loadTorrentFromFile(torrentFilename);

        videoName = ActiveTorrents.getVideoName(torrentFilename);

        return videoUrl + videoName;
    }

    /**
     *
     * @param torrentFilename name of the .data video file
     * @return true if the video is loaded from the file, false if it is already
     * running.
     * @throws ActiveTorrentsException
     */
    public boolean loadTorrentFromFile(String torrentFilename) throws ActiveTorrentsException {
        if (ActiveTorrents.peerAlreadyRunning(torrentFilename)) {
            logger.info("Peer already running for this torrent: {}", torrentFilename);
            return false;
        } else {
            boolean videoFileExists = false;
            File vf = ActiveTorrents.getVideoFile(torrentFilename);
            if (vf != null) {
                videoFileExists = vf.exists();
            }
            createPeer(torrentFilename, false, videoFileExists, null);
            ActiveTorrents.addTorrent(torrentFilename);

        }
        return true;
    }

//-------------------------------------------------------------------    
    /**
     *
     * @param filname: full pathname of torrent file
     * @param seed : true if seed, false otherwise
     * @return name of video file
     * @throws IOException
     */
    private String createPeer(String torrentFilename, boolean seed,
            boolean videoFileAlreadyExists, Storage storage)
            throws ActiveTorrentsException {
        if (ActiveTorrents.peerAlreadyRunning(torrentFilename)) {
            logger.warn("Tried to create a new torrent, but the torrent was already " + "in your library: {}", torrentFilename);
            return ActiveTorrents.getVideoFilename(torrentFilename);
        }

        logger.info("Starting: " + torrentFilename);
        // TODO - should get peerId from bootstrap server.
        Component vodPeer = create(VodPeer.class);
        String videoName = ActiveTorrents.addTorrent(torrentFilename, vodPeer, seed);

        if (seed) {
            ActiveTorrents.makeSeeder(torrentFilename);
        }

        int overlayId = ActiveTorrents.calculateVideoId(videoName);

        Address peerAddress = new Address(self.getIp(), self.getPort(), VodConfig.getNodeId());

        connect(vodPeer.getNegative(Timer.class), timer.getPositive(Timer.class),
                new TimeoutOverlayIdFilter(overlayId));
        connect(vodPeer.getNegative(VodNetwork.class), natTraverser.getPositive(VodNetwork.class), new MsgDestFilterOverlayId(overlayId)
        );
        connect(web.getNegative(Web.class), vodPeer.getPositive(Web.class),
                new WebRequestFilterOverlayId(overlayId));
        connect(vodPeer.getNegative(BootstrapPort.class),
                bootstrap.getPositive(BootstrapPort.class),
                new BootstrapResponseFilterOverlayId(overlayId));
        connect(vodPeer.getNegative(NatTraverserPort.class),
                natTraverser.getPositive(NatTraverserPort.class)
        );

        subscribe(handleQuitCompleted, vodPeer.getPositive(VodPeerPort.class));
        subscribe(handleSlowBackground, vodPeer.getPositive(VodPeerPort.class));
        subscribe(handleSpeedBackground, vodPeer.getPositive(VodPeerPort.class));
        subscribe(handleReportDownloadSpeed, vodPeer.getPositive(VodPeerPort.class));

        BootstrapConfiguration bConfig = BootstrapConfiguration.build();
        VodMonitorConfiguration mConfig = new VodMonitorConfiguration(VodConfig.getMonitorServer());

        long len = new File(torrentFilename).length();
        VodConfiguration vodConfig = new VodConfiguration(videoName, len);
        CroupierConfiguration croupierConfig = CroupierConfiguration.build();

        short mtu = 1500;
        int asn = pm.matchIPtoAS(peerAddress.getIp().getHostAddress());

        trigger(new VodPeerInit(self.clone(overlayId), main,
                bConfig, croupierConfig, vodConfig, mConfig,
                false, seed, false, torrentFilename, videoFileAlreadyExists,
                mtu, asn, seed ? VodConfig.SEEDER_UTILITY_VALUE : 0,
                false, 0, 0, false, null, storage),
                vodPeer.getControl());

        logger.info("Started: " + torrentFilename);
        return videoName;
    }

//-------------------------------------------------------------------    
    /**
     * Generates a peerId as a 4-byte Int using 3 bytes from the node's MAC
     * address, and 1 byte using a random number.
     */
    private int getNodeId(InetAddress myIp) {
        int nodeId = random.nextInt();
        if (myIp == null) {
            return nodeId;
        }
        try {
            NetworkInterface netIf = NetworkInterface.getByInetAddress(myIp);
            if (netIf == null) {
                return random.nextInt();
            }
            byte[] mac = netIf.getHardwareAddress();
            if (mac == null) {
                return nodeId;
            }

            // MAC address has 6 bytes. Copy bytes 2-6 into an INT.
            byte[] macInt = Arrays.copyOfRange(mac, 2, 6);
            nodeId = ActiveTorrents.byteArrayToInt(macInt);
        } catch (SocketException e) {
            logger.error("Problem getting the MAC address to generate peer id");
        }

        return nodeId;
    }

//-------------------------------------------------------------------    
    public static int byteArrayToInt(byte[] b, int offset) {
        int value = 0;
        for (int i = 0; i < 4; i++) {
            int shift = (4 - 1 - i) * 8;
            value += (b[i + offset] & 0x000000FF) << shift;
        }

        return value;
    }

//-------------------------------------------------------------------    
    @Override
    public boolean changeUtility(String videoFilename, int seekPos, int readPos, OutputStream responseBody) {
        // TODO - Broken
        Component peer = ActiveTorrents.getPeerFromVideo(videoFilename);
        if (peer == null) {
            return false;
        }
        int overlayId = ActiveTorrents.calculateVideoId(videoFilename);
        trigger(new ChangeUtility(overlayId, seekPos, readPos, responseBody),
                peer.getPositive(VodPeerPort.class));
        return true;
    }

//-------------------------------------------------------------------    
    /**
     *
     * @param bootstrapServerAddr
     * @param videoFilename - name for the torrent to be stored as a file.
     * @param width width of video
     * @param height height of video
     * @param description
     * @param imageUrl
     * @param progressBar
     * @return true if file didn't exist before, false otherwise
     * @throws java.io.IOException
     */
    public synchronized boolean createStream(Address bootstrapServerAddr,
            String videoFilename, int width, int height, String description,
            String imageUrl, JProgressBar progressBar) throws IOException {

        Address monitorAddress = VodConfig.getMonitorServer();
        File videoFile = new File(videoFilename);
        if (!videoFile.exists()) {
            return false;
        }
        String videoName = videoFile.getName();

        String escapedFilename = FileUtils.getValidFileName(videoName);

        File newFile = new File(VodConfig.getVideoDir() + File.separator + escapedFilename);
        // if the source video is not in our default VideoDirectory, and the new file already
        // exists in our default VideoDirectory, replace the existing video file.
        if (newFile.exists() && videoFile.getAbsolutePath().compareTo(newFile.getAbsolutePath()) != 0) {
            newFile.delete();
        }

        String postfix = FileUtils.getPostFix(videoName);
        if (postfix.compareToIgnoreCase(".mp4") == 0) {
            RandomAccessFile orig = new RandomAccessFile(videoFilename, "r");
            RandomAccessFile fixed = new RandomAccessFile(newFile, "rw");
            JQTFaststart.startFast(orig, fixed);
            orig.close();
            fixed.close();
        } else if (postfix.compareToIgnoreCase(".flv") == 0) {
            if (videoFile.getAbsolutePath().compareTo(newFile.getAbsolutePath()) != 0) {
                org.apache.commons.io.FileUtils.copyFile(videoFile, newFile, true);
            }
        } else {
            throw new IllegalArgumentException("Only .flv and .mp4 file types supported.");
        }
//        JQTFaststart.copyOverwriteFile(newFile.getAbsolutePath(), videoFilename);

        String torrentFileAddress = VodConfig.getTorrentDir() + File.separator
                + escapedFilename.concat(VodConfig.TORRENT_FILE_POSTFIX);
        File torrentFile = new File(torrentFileAddress);
        Storage storage;

        if (Vod.MEM_MAP_VOD_FILES) {
            storage = new StorageMemMapWholeFile(newFile, width, height,
                    bootstrapServerAddr, 100, torrentFileAddress, monitorAddress);
        } else {
            storage = new StorageFcByteBuf(newFile, width, height,
                    bootstrapServerAddr, 100, torrentFileAddress, monitorAddress);
        }

        // TODO - this can take a long time. Provide some
        // feedback to the user...
        logger.info("Creating hashes for file pieces...");
        storage.create(progressBar);

        torrentFile.createNewFile();
        FileOutputStream fos = new FileOutputStream(torrentFile);
        fos.write(storage.getMetaInfo().getData());
        fos.close();

        storage.writePieceHashesToFile();

        AddOverlayRequest req = new AddOverlayRequest(
                bootstrapServerAddr,
                ActiveTorrents.calculateVideoId(videoName),
                videoName,
                description,
                storage.getMetaInfo().getData(),
                imageUrl);
        trigger(req, bootstrap.getPositive(BootstrapPort.class));

        createPeer(torrentFileAddress, true, true, storage);

        return true;
    }

//-------------------------------------------------------------------    
    public synchronized void stopAndDestroyAllTorrents() {
        Set<Integer> ids = ActiveTorrents.getAllIds();
        for (Integer id : ids) {
            try {
                stopPeer(id);
            } catch (RuntimeException e) {
                // swallow it
            }
        }
    }

//-------------------------------------------------------------------    
    private void sendBootstrapRequest() {
        trigger(new BootstrapRequest(VodConfig.SYSTEM_OVERLAY_ID),
                bootstrap.getPositive(BootstrapPort.class));
        natTypeRequestTimeout = (numBootstrapAttempts == 0) ? 1 : natTypeRequestTimeout * 2;
        numBootstrapAttempts++;
    }

//-------------------------------------------------------------------    
    public int getSpeed() {
        return downloadSpeed.get();
    }

//-------------------------------------------------------------------    
    public int getTotalDownloaded() {
        return totalDownload.get();
    }

//-------------------------------------------------------------------    
    public void stopPeer(Integer peerId) {
        synchronized (this) {
            Component peer = ActiveTorrents.getPeer(peerId);
            if (peer != null) {
                stopPeer(peer);
            } else {
                logger.warn("Problem deleting peer {}", peerId);
            }
        }
    }

//-------------------------------------------------------------------    
    @Override
    public void stopPeer(Component peer) {
        synchronized (this) {
            if (peer != null) {
                logger.warn("Stopping peer");
                try {
                    unsubscribe(handleQuitCompleted, peer.getPositive(VodPeerPort.class));
                    disconnect(timer.getPositive(Timer.class), peer.getNegative(Timer.class));

                    // Disconnecting from netty fails for some reason
//                disconnect(network.getPositive(VodNetwork.class), peer.getNegative(VodNetwork.class));
                    disconnect(web.getNegative(Web.class), peer.getPositive(Web.class));
                    destroy(peer);
                } catch (RuntimeException e) {
                    // swallow execeptions...
                    logger.warn("Problem deleting peer: " + e.getMessage());
                }
            } else {
                logger.warn("Problem deleting peer, null pointer to peer.   ");
            }
        }
    }

    private void startServices() {

        if (initialized.get() == false) {
            printTime("Starting services. Network=" + self.getAddress());
            startGui();
            printTime("Started  gui");
            startVideos();
            printTime("Started  torrents");
            // Allow the ControlProtocol thread to start downloading a new torrent
            initialized.set(true);
        }
    }

    private void startGui() {

        try {
            JwHttpServer.startOrUpdate(
                    new InetSocketAddress(VodConfig.getMediaPort()),
                    "/", new BrowserGuiHandler(main));
            logger.info("Media server listening at: "
                    + VodConfig.LOCALHOST + ":"
                    + VodConfig.getMediaPort()
                    + "/");
        } catch (IOException ex) {
            logger.warn("Couldn't bind to BrowserGuiHandler port");
            logger.error(ex.getMessage());
            Kompics.shutdown();
            System.exit(-1);
        }

        if (VodConfig.GUI) {
            logger.info("Starting Swing GUI..");
            userInterface = create(SwingComponent.class);
            connect(userInterface.getNegative(BootstrapPort.class), bootstrap.getPositive(BootstrapPort.class));
            trigger(new UserInterfaceInit(this), userInterface.getControl());

            subscribe(handleStartInBackground, userInterface.getNegative(AppMainPort.class));
            subscribe(handleDownloadSpeed, userInterface.getNegative(AppMainPort.class));
        }

    }

    private void cacheNatType() {
        Address upnpAddress = null;
        if (upnpIpAddress != null && upnpPort != 0) {
            upnpAddress = new Address(upnpIpAddress, upnpPort, self.getId());
        }

//            VodAddress selfVodAddress;
        if (upnpAddress != null) {
//                selfAddress = upnpAddress;
//                selfVodAddress = new VodAddress(selfAddress, VodConfig.getNodeId(), nat);
//                self
//                VodConifg.saveNatType(self, 
//                        cachedNatConfig.getNatBean().getNumTimesSinceStunLastRun() == 0,
//                );
//                VodConfig.saveConfiguration(new VodAddressBean(selfVodAddress.getPeerAddress(),
//                        selfVodAddress.getParents(), selfVodAddress.getNatPolicy()), true,
//                        cachedNatConfig.getNumTimesUnchanged(),
//                        cachedNatConfig.getNumTimesSinceStunLastRun());
        } else {
//                selfVodAddress = new VodAddress(selfAddress, VodConfig.getNodeId(), nat, new HashSet<Address>());
            VodConfig.saveNatType(self, true);
        }
    }

    private void startVideos() {
        if (VodConfig.getMovie().compareTo("") != 0 && VodConfig.getTorrentUrl().compareTo("") == 0) {
            logger.error("You must set the torrent filename, if you" + "specify a movie you want to encode.");
            Kompics.shutdown();
            System.exit(-1);
        }

        // load torrents in my local library called 'activeStreams'
        for (String stream : ActiveTorrents.getTorrentsUsingIndexFile()) {
            try {
                createPeer(stream, true, true, null);
            } catch (ActiveTorrentsException ex) {
                ex.printStackTrace();
                logger.warn("Couldn't start: " + stream);
            }
        }

        // Now register myself and my streams with the bootstrap server
        SchedulePeriodicTimeout spt = new SchedulePeriodicTimeout(10 * 1000,
                VodConfig.getBootstrapRefreshPeriod());
        spt.setTimeoutEvent(new BootServerKeepAlive(spt));
        trigger(spt, timer.getPositive(Timer.class));

        // Create the torrent from command-line args
        // To create a torrent:
        // -movie path 
        // #-torrent path
        if (VodConfig.getMovie().compareTo("") != 0) {
            try {
                Address b = VodConfig.getBootstrapServer();
                if (b != null) {
                    createStream(b,
                            VodConfig.getMovie(),
                            VodConfig.getWidth(),
                            VodConfig.getHeight(),
                            "Video description to come",
                            "", null);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } else if (VodConfig.getTorrentUrl().compareTo("") != 0) {
            try {
                downloadTorrentAndCreatePeer(VodConfig.getTorrentUrl());
            } catch (java.net.ConnectException ex) {
                ex.printStackTrace();
                logger.error("Could not connect to: " + VodConfig.getTorrentUrl());
            } catch (IOException ex) {
                ex.printStackTrace();
                logger.warn("Could not download torrent URL: " + VodConfig.getTorrentUrl());
            }
        }

    }

//-------------------------------------------------------------------    
    private void sendKeepalive() {
        Set<Integer> seeds = ActiveTorrents.getSeederIds();
        Map<Integer, Integer> downloaders = ActiveTorrents.getLeecherIdsUtilities();
        trigger(new BootstrapHeartbeat((short) VodConfig.LB_MTU_MEASURED, seeds, downloaders),
                bootstrap.getPositive(BootstrapPort.class));
    }

//-------------------------------------------------------------------    
    private void getIp() {
        if (VodConfig.isTenDot()) {
            trigger(new GetIpRequest(false, EnumSet.of(
                    NetworkInterfacesMask.IGNORE_LOCAL_ADDRESSES,
                    NetworkInterfacesMask.IGNORE_PRIVATE,
                    NetworkInterfacesMask.IGNORE_PUBLIC
            )
            ), resolveIp.getPositive(ResolveIpPort.class));
        } else {
            trigger(new GetIpRequest(false), resolveIp.getPositive(ResolveIpPort.class));
        }
    }
}
