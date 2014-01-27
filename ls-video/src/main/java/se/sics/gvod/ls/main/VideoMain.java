/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.ls.main;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Random;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.gvod.address.Address;
import se.sics.gvod.bootstrap.msgs.BootstrapMsg;
import se.sics.gvod.common.Self;
import se.sics.gvod.common.SelfImpl;
import se.sics.gvod.config.BootstrapConfiguration;
import se.sics.gvod.config.VodConfig;
import se.sics.gvod.common.util.ToVodAddr;
import se.sics.gvod.config.CroupierConfiguration;
import se.sics.gvod.ls.peer.JoinPeer;
import se.sics.gvod.ls.peer.VideoPeer;
import se.sics.gvod.ls.peer.VideoPeerInit;
import se.sics.gvod.ls.peer.VideoPeerPort;
import se.sics.gvod.ls.system.LSConfig;
import se.sics.gvod.config.VideoConfiguration;
import se.sics.gvod.net.NettyInit;
import se.sics.gvod.net.NettyNetwork;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.net.VodMsgFrameDecoder;
import se.sics.gvod.net.VodNetwork;
import se.sics.gvod.system.util.ActiveTorrents;
import se.sics.gvod.timer.Timer;
import se.sics.gvod.timer.java.JavaTimer;
import se.sics.kompics.Component;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Kompics;
import se.sics.kompics.nat.utils.getip.ResolveIp;
import se.sics.kompics.nat.utils.getip.ResolveIpPort;
import se.sics.kompics.nat.utils.getip.events.GetIpRequest;
import se.sics.kompics.nat.utils.getip.events.GetIpResponse;

/**
 *
 * @author Jim Dowling<jdowling@sics.se>
 */
public class VideoMain extends ComponentDefinition {

    private static final Logger logger = LoggerFactory.getLogger(VideoMain.class);
    private Component timer;
    private Component network;
    private Component resolveIp;
    private Component peer;
    private VodAddress selfVideoAddress;
    private Address selfAddress;
    private Self selfNode;
    private static Random random;
    private BootstrapConfiguration bConfig;

    public VideoMain() {
        timer = create(JavaTimer.class);
        network = create(NettyNetwork.class);
        resolveIp = create(ResolveIp.class);
        peer = create(VideoPeer.class);

        connect(peer.getNegative(Timer.class), timer.getPositive(Timer.class));
        connect(peer.getNegative(VodNetwork.class), network.getPositive(VodNetwork.class));

        connect(network.getNegative(Timer.class), timer.getPositive(Timer.class));

        // connecting components
        connect(resolveIp.getNegative(Timer.class), timer.getPositive(Timer.class));

        subscribe(handleGetIpResponse, resolveIp.getPositive(ResolveIpPort.class));
//        subscribe(handleNetworkFault, network.getControl());
        random = new Random(LSConfig.getSeed());
        getIp();
    }

    private void getIp() {
        trigger(new GetIpRequest(false, EnumSet.of(
                GetIpRequest.NetworkInterfacesMask.IGNORE_LOCAL_ADDRESSES)),
                resolveIp.getPositive(ResolveIpPort.class));
    }
    public Handler<GetIpResponse> handleGetIpResponse = new Handler<GetIpResponse>() {
        @Override
        public void handle(GetIpResponse event) {
            InetAddress ip = event.getIpAddress();
            int mtu = event.getFirstAddress().getMtu();
            int nodeId = getNodeId(ip);
            VodConfig.setNodeId(nodeId);
            selfAddress = new Address(ip, LSConfig.getPort(), nodeId);
            logger.info("Listening on: {}", selfAddress);
            selfVideoAddress = ToVodAddr.systemAddr(selfAddress);
            selfNode = new SelfImpl(selfVideoAddress);
            trigger(new NettyInit(LSConfig.getSeed(), true, 
                    VodMsgFrameDecoder.class), network.getControl());

//                InetAddress bootstrapServer = InetAddress.getByName();
            int retryPeriod = 20 * 1000, retryCount = 1,
                    clientKeepAlive = 30 * 1000;
            Address bootAddr = LSConfig.getBootstrapServer();
            bConfig =  BootstrapConfiguration.build()
                    .setBootstrapServerAddress(bootAddr)
                    .setClientRetryPeriod(retryPeriod)
                    .setClientKeepAlivePeriod(clientKeepAlive);

            CroupierConfiguration croupierConfig = CroupierConfiguration.build();
            VideoConfiguration videoConfig = new VideoConfiguration();
            trigger(new VideoPeerInit(selfNode, LSConfig.isSource(),
                    croupierConfig, videoConfig, bConfig), peer.getControl());

            trigger(new JoinPeer(), peer.getPositive(VideoPeerPort.class));
        }
    };

    /**
     * Generates a peerId as a 4-byte Int using 3 bytes from the node's MAC
     * address, and 1 byte using a random number.
     */
    private int getNodeId(InetAddress myIp) {
        int nodeId = random.nextInt();
        try {
            NetworkInterface netIf = NetworkInterface.getByInetAddress(myIp);
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

    public static void main(String[] args) {
        try {
            LSConfig.init(args);
        } catch (IOException ex) {
            System.err.println("Exiting after parsing command line args");
            java.util.logging.Logger.getLogger(VideoMain.class.getName()).log(Level.SEVERE, null, ex);
            Kompics.shutdown();
            System.exit(-1);
        }
        System.setProperty("java.net.preferIPv4Stack", "true");
        Kompics.createAndStart(VideoMain.class, 2);

    }
}
