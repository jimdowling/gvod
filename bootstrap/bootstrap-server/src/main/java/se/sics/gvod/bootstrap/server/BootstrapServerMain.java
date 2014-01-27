package se.sics.gvod.bootstrap.server;

import se.sics.gvod.bootstrap.config.BootstrapConfig;
import java.util.logging.Level;
import java.io.IOException;
import java.net.InetAddress;
import java.util.EnumSet;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.gvod.net.VodNetwork;
import se.sics.kompics.Component;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Fault;
import se.sics.kompics.Handler;
import se.sics.kompics.Kompics;
import se.sics.gvod.config.BootstrapConfiguration;
import se.sics.gvod.config.VodConfig;
import se.sics.gvod.net.NatNetworkControl;
import se.sics.gvod.timer.Timer;
import se.sics.gvod.timer.java.JavaTimer;
import se.sics.kompics.nat.utils.getip.ResolveIp;
import se.sics.kompics.nat.utils.getip.ResolveIpPort;
import se.sics.kompics.nat.utils.getip.events.GetIpRequest;
import se.sics.kompics.nat.utils.getip.events.GetIpRequest.NetworkInterfacesMask;
import se.sics.kompics.nat.utils.getip.events.GetIpResponse;
import se.sics.gvod.net.NettyInit;
import se.sics.gvod.net.NettyNetwork;
import se.sics.gvod.net.Transport;
import se.sics.gvod.net.VodMsgFrameDecoder;
import se.sics.gvod.net.events.PortBindRequest;
import se.sics.gvod.net.events.PortBindResponse;
import se.sics.kompics.web.Web;
import se.sics.kompics.web.jetty.JettyWebServer;
import se.sics.kompics.web.jetty.JettyWebServerConfiguration;
import se.sics.kompics.web.jetty.JettyWebServerInit;

/**
 * The
 * <code>BootstrapServerMain</code> class.
 *
 * @author Cosmin Arad <cosmin@sics.se>
 * @version $Id: BootstrapServerMain.java 1139 2009-09-01 22:53:58Z Cosmin $
 */
public class BootstrapServerMain extends ComponentDefinition {

    Component resolveIp;
    Component bootstrapServer;
    Component network;
    Component web;
    Component timer;
    private BootstrapConfiguration bootConfiguration;

    static {
        PropertyConfigurator.configureAndWatch("log4j.properties");
    }
    private static final Logger logger = LoggerFactory.getLogger(BootstrapServerMain.class);

    public static void main(String[] args) {
        System.setProperty("java.net.preferIPv4Stack", "true");
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                logger.info("Shutting down BootstrapServer...");
                //Get the jvm heap size.
                long heapSize = Runtime.getRuntime().totalMemory();
                //Print the jvm heap size.
                logger.debug("Heap Size = " + heapSize);
                try {
                    if (Kompics.getScheduler() != null) {
                        Kompics.shutdown();
                    }
                } catch (Exception e) {
                    logger.warn("Error shutting down: " + e.getMessage());
                }
            }
        });

        try {
            BootstrapConfig.init(args);
            Kompics.createAndStart(BootstrapServerMain.class, BootstrapConfig.getNumWorkers());
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(BootstrapServerMain.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static class WebPortBindResponse extends PortBindResponse {

        public WebPortBindResponse(PortBindRequest request) {
            super(request);
        }
    }

    public static class BootPortBindResponse extends PortBindResponse {

        public BootPortBindResponse(PortBindRequest request) {
            super(request);
        }
    }

    public BootstrapServerMain() throws IOException {
        // creating components
        bootstrapServer = create(BootstrapServerMysql.class);
        timer = create(JavaTimer.class);
        network = create(NettyNetwork.class);
        resolveIp = create(ResolveIp.class);
        web = create(JettyWebServer.class);

        // connecting components
        connect(bootstrapServer.getNegative(Timer.class), timer.getPositive(Timer.class));
        connect(bootstrapServer.getNegative(VodNetwork.class), network.getPositive(VodNetwork.class));
        connect(resolveIp.getNegative(Timer.class), timer.getPositive(Timer.class));
        connect(bootstrapServer.getPositive(Web.class), web.getNegative(Web.class));

        subscribe(handleGetIpResponse, resolveIp.getPositive(ResolveIpPort.class));
        subscribe(handleBootPortBindResponse, network.getPositive(NatNetworkControl.class));

        subscribe(handleFault, resolveIp.getControl());
        subscribe(handleFault, bootstrapServer.getControl());
        subscribe(handleFault, network.getControl());
        subscribe(handleFault, timer.getControl());
        subscribe(handleFault, web.getControl());


        trigger(new GetIpRequest(false, EnumSet.of(
                NetworkInterfacesMask.IGNORE_LOCAL_ADDRESSES)), resolveIp.getPositive(ResolveIpPort.class));

    }
    public Handler<GetIpResponse> handleGetIpResponse = new Handler<GetIpResponse>() {
        @Override
        public void handle(GetIpResponse event) {
            try {

                InetAddress ip = event.getIpAddress();
                VodConfig.setIp(ip);
                VodConfig.setBoostrapServerIp(ip);
                logger.info("Bootstrap IP: " + ip.getHostAddress());

                // loading component configurations
                bootConfiguration =
                        new BootstrapConfiguration(ip.getHostAddress(),
                        VodConfig.getBootstrapServerPort(),
                        VodConfig.SYSTEM_OVERLAY_ID,
                        4000, 2, 30 * 1000, 8764);

                trigger(new BootstrapServerInit(bootConfiguration,
                        BootstrapConfig.isAsnMatcher()), bootstrapServer.getControl());
                /**
                 * Bind on all network interfaces...
                 */
                trigger(new NettyInit(BootstrapConfig.getSeed(), true,
                        VodMsgFrameDecoder.class), network.getControl());
                logger.info("Started. Network={}", bootConfiguration.getBootstrapServerAddress());

                PortBindRequest pb1 = new PortBindRequest(bootConfiguration.getBootstrapServerAddress(),
                        Transport.UDP, false);
                BootPortBindResponse pbr1 = new BootPortBindResponse(pb1);
                trigger(pb1, network.getPositive(NatNetworkControl.class));


            } catch (Exception e) {
                logger.error(e.getMessage());
                Kompics.shutdown();
                System.exit(-1);
            }
        }
    };
    public Handler<BootPortBindResponse> handleBootPortBindResponse = new Handler<BootPortBindResponse>() {
        @Override
        public void handle(BootPortBindResponse event) {
            if (event.getStatus() == BootPortBindResponse.Status.SUCCESS) {
                String webServerAddr = "http:/" + VodConfig.getIp().toString()
                        + ":" + BootstrapConfig.getWebSearchPort();
                final JettyWebServerConfiguration webConfiguration =
                        new JettyWebServerConfiguration(VodConfig.getIp(),
                        BootstrapConfig.getWebSearchPort(),
                        BootstrapConfig.DEFAULT_WEB_REQUEST_TIMEOUT_MS,
                        BootstrapConfig.DEFAULT_WEB_THREADS,
                        webServerAddr);
                trigger(new JettyWebServerInit(webConfiguration), web.getControl());
                logger.info("Webserver Started. Address={}", webServerAddr + "/1/1/search");
            } else {
                logger.error(event.getStatus()
                        + " - problem binding to port for BootstrapServer: " + event.getPort());
                Kompics.shutdown();
                System.exit(-2);
            }
        }
    };
    public Handler<Fault> handleFault = new Handler<Fault>() {
        @Override
        public void handle(Fault event) {
    
            for (StackTraceElement ste : event.getFault().getStackTrace()) {
                logger.error(ste.toString());
            }
            logger.error(event.getFault().toString());
            
            String errorMsg = event.getFault().getMessage();
            if (errorMsg == null) {
                logger.error("Exception thrown but msg is null");
            } else {
                logger.error(errorMsg);
            }

            Kompics.shutdown();
            System.exit(-1);
        }
    };
}
