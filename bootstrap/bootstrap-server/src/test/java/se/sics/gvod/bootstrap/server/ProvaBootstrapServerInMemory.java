package se.sics.gvod.bootstrap.server;

import se.sics.gvod.common.SelfNoParents;
import se.sics.gvod.common.util.ToVodAddr;
import java.util.HashMap;
import java.util.Map;
import java.io.IOException;
import java.util.logging.Level;
import se.sics.gvod.bootstrap.config.BootstrapConfig;
import se.sics.gvod.bootstrap.port.BootstrapHeartbeat;
import se.sics.gvod.bootstrap.port.BootstrapResponse;
import se.sics.gvod.bootstrap.port.BootstrapPort;
import se.sics.gvod.bootstrap.client.BootstrapClient;
import se.sics.gvod.net.VodNetwork;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Semaphore;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.gvod.bootstrap.client.BootstrapClientInit;
import se.sics.gvod.bootstrap.port.BootstrapRequest;
import se.sics.gvod.net.VodAddress;
import se.sics.kompics.Component;

import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Fault;
import se.sics.kompics.Handler;
import se.sics.kompics.Kompics;
import se.sics.kompics.Start;
import se.sics.gvod.address.Address;
import se.sics.kompics.nat.utils.getip.ResolveIp;
import se.sics.kompics.nat.utils.getip.ResolveIpPort;
import se.sics.kompics.nat.utils.getip.events.GetIpRequest;
import se.sics.kompics.nat.utils.getip.events.GetIpResponse;
import se.sics.gvod.net.NettyInit;
import se.sics.gvod.net.NettyNetwork;
import se.sics.gvod.timer.ScheduleTimeout;
import se.sics.gvod.timer.Timeout;
import se.sics.gvod.timer.Timer;
import se.sics.gvod.timer.java.JavaTimer;
import se.sics.kompics.web.Web;
import se.sics.kompics.web.jetty.JettyWebServer;

import static junit.framework.Assert.*;
import se.sics.gvod.config.BootstrapConfiguration;
import se.sics.gvod.config.VodConfig;
import se.sics.gvod.net.VodMsgFrameDecoder;

public class ProvaBootstrapServerInMemory extends TestCase {

    private static final Logger logger = LoggerFactory.getLogger(ProvaBootstrapServerInMemory.class);
    private boolean testStatus = true;
    private static final int EVENT_COUNT = 1;
    private static Semaphore semaphore = new Semaphore(0);

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public ProvaBootstrapServerInMemory(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(ProvaBootstrapServerInMemory.class);
    }

    public static void setTestObj(ProvaBootstrapServerInMemory testObj) {
        TestStClientComponent.testObj = testObj;
    }

    public static class MsgTimeout extends Timeout {

        public MsgTimeout(ScheduleTimeout request) {
            super(request);
        }
    }

    public static class TestStClientComponent extends ComponentDefinition {

        private static ProvaBootstrapServerInMemory testObj = null;
        Component bootstrapServer;
        Component bootstrapClient;
        Component network;
        Component clientNet;
        Component timer;
        Component web;
        Component resolveIp;
        Address self;
        Address self2;
        BootstrapConfiguration bootConfiguration;

        public TestStClientComponent() {

            String[] args = {};
            try {
                BootstrapConfig.init(args);
            } catch (IOException ex) {
                java.util.logging.Logger.getLogger(ProvaBootstrapServerInMemory.class.getName()).log(Level.SEVERE, null, ex);
            }
            bootstrapServer = create(BootstrapServerMemory.class);
            bootstrapClient = create(BootstrapClient.class);
            timer = create(JavaTimer.class);
            network = create(NettyNetwork.class);
            clientNet = create(NettyNetwork.class);
            web = create(JettyWebServer.class);
            resolveIp = create(ResolveIp.class);

            connect(bootstrapServer.getNegative(Timer.class), timer.getPositive(Timer.class));
            connect(bootstrapServer.getNegative(VodNetwork.class), network.getPositive(VodNetwork.class));
            connect(bootstrapServer.getPositive(Web.class), web.getNegative(Web.class));

            connect(bootstrapClient.getNegative(Timer.class), timer.getPositive(Timer.class));
            connect(bootstrapClient.getNegative(VodNetwork.class), clientNet.getPositive(VodNetwork.class));

            subscribe(handleStart, control);
            subscribe(handleFault, clientNet.getControl());
            subscribe(handleFault, network.getControl());
//            subscribe(handleGetPeersResponse, clientNet.getPositive(GVodNetwork.class));
            subscribe(handleBootstrapResponse, bootstrapClient.getPositive(BootstrapPort.class));

            subscribe(handleGetIpResponse, resolveIp.getPositive(ResolveIpPort.class));
        }
        public Handler<GetIpResponse> handleGetIpResponse = new Handler<GetIpResponse>() {
            @Override
            public void handle(GetIpResponse event) {
                try {
                    InetAddress ip = event.getBoundIp();
                    self = new Address(ip, 6666, 6);
                    self2 = new Address(ip, 7777, 7);
                    logger.debug("Bootstrap IP: " + ip.getHostAddress());

                    Address bootAddr = new Address(ip, 8010, 1);
                    // loading component configurations
                    bootConfiguration =
                            BootstrapConfiguration.build()
                            .setBootstrapServerAddress(bootAddr);

                    trigger(new BootstrapServerInit(bootConfiguration, false),
                            bootstrapServer.getControl());
                    trigger(new NettyInit(bootConfiguration.getSeed(), true, VodMsgFrameDecoder.class),
                            network.getControl());
                    trigger(new NettyInit(bootConfiguration.getSeed(), true, VodMsgFrameDecoder.class), 
                            clientNet.getControl());
                    logger.info("Started. Network={}", bootConfiguration.getBootstrapServerAddress());
                    trigger(new BootstrapClientInit(new SelfNoParents(ToVodAddr.systemAddr(self)),
                            bootConfiguration), bootstrapClient.getControl());


                    VodAddress gAddr1 = new VodAddress(self, VodConfig.SYSTEM_OVERLAY_ID);
                    VodAddress gAddr2 = new VodAddress(self2, VodConfig.SYSTEM_OVERLAY_ID);

                    Set<Integer> seeds = new HashSet<Integer>();
                    seeds.add(1);
                    Map<Integer, Integer> downloaders = new HashMap<Integer, Integer>();
                    downloaders.put(2, 0);
                    BootstrapHeartbeat hb1 = new BootstrapHeartbeat(false, (short) 1500,
                            seeds, downloaders, gAddr1);
                    trigger(hb1, bootstrapClient.getPositive(BootstrapPort.class));

                    BootstrapRequest request = new BootstrapRequest(22, null);
//                trigger(request, clientNet.getPositive(GVodNetwork.class));
                    trigger(request, bootstrapClient.getPositive(BootstrapPort.class));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
//        private Handler<BootstrapMsg.GetPeersResponse> handleGetPeersResponse = new Handler<BootstrapMsg.GetPeersResponse>() {
//
//            public void handle(BootstrapMsg.GetPeersResponse event) {
//                logger.info("Returned " + event.getPeers().size() + " peers");
//                testObj.pass();
//            }
//        };
        private Handler<BootstrapResponse> handleBootstrapResponse = new Handler<BootstrapResponse>() {
            public void handle(BootstrapResponse event) {
                int numNodes = event.getPeers().size();
                logger.info("Returned " + numNodes + " peers");

                if (numNodes == 1) {
                    testObj.pass();
                } else {
                    testObj.fail();
                }
            }
        };
        public Handler<Start> handleStart = new Handler<Start>() {
            @Override
            public void handle(Start event) {
                trigger(new GetIpRequest(false), resolveIp.getPositive(ResolveIpPort.class));

            }
        };
        public Handler<Fault> handleFault = new Handler<Fault>() {
            @Override
            public void handle(Fault event) {

                testObj.fail(true);
            }
        };
    }

    @org.junit.Ignore
    public void App() {
        setTestObj(this);

        assert (true);

        Kompics.createAndStart(TestStClientComponent.class, 1);
        try {
            ProvaBootstrapServerInMemory.semaphore.acquire(EVENT_COUNT);
            System.out.println("Finished test.");
        } catch (InterruptedException e) {
            assert (false);
        } finally {
            Kompics.shutdown();
        }
        if (testStatus == false) {
            assertTrue(false);
        }

    }

    public void pass() {
        testStatus = true;
        ProvaBootstrapServerInMemory.semaphore.release();
    }

    public void fail(boolean release) {
        testStatus = false;
        ProvaBootstrapServerInMemory.semaphore.release();
    }
}
