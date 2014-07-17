/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.simulator.vod.newp;

import java.io.OutputStream;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.gvod.address.Address;
import se.sics.gvod.bootstrap.client.BootstrapClient;
import se.sics.gvod.bootstrap.client.BootstrapClientInit;
import se.sics.gvod.bootstrap.port.BootstrapPort;
import se.sics.gvod.bootstrap.port.BootstrapResponseFilterOverlayId;
import se.sics.gvod.common.Self;
import se.sics.gvod.common.SelfImpl;
import se.sics.gvod.config.BootstrapConfiguration;
import se.sics.gvod.config.CroupierConfiguration;
import se.sics.gvod.config.VodConfig;
import se.sics.gvod.filters.MsgDestFilterOverlayId;
import se.sics.gvod.filters.TimeoutOverlayIdFilter;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.net.VodNetwork;
import se.sics.gvod.net.msgs.DirectMsg;
import se.sics.gvod.simulator.common.ConsistentHashtable;
import se.sics.gvod.simulator.vod.VodExperiment;
import se.sics.gvod.simulator.vod.VodPeerJoin;
import se.sics.gvod.simulator.vod.VodSimulatorInit;
import se.sics.gvod.system.main.GMain;
import se.sics.gvod.system.peer.VodPeer;
import se.sics.gvod.system.peer.VodPeerInit;
import se.sics.gvod.system.peer.VodPeerPort;
import se.sics.gvod.system.peer.events.QuitCompleted;
import se.sics.gvod.system.peer.events.ReadingCompleted;
import se.sics.gvod.system.simulator.bw.model.Link;
import se.sics.gvod.system.vod.VodConfiguration;
import se.sics.gvod.system.vod.WebRequestFilterOverlayId;
import se.sics.gvod.timer.Timer;
import se.sics.gvod.web.port.DownloadCompletedSim;
import se.sics.gvod.web.server.VodMonitorConfiguration;
import se.sics.ipasdistances.AsIpGenerator;
import se.sics.ipasdistances.PrefixMatcher;
import se.sics.kompics.ChannelFilter;
import se.sics.kompics.Component;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Negative;
import se.sics.kompics.Positive;
import se.sics.kompics.Start;
import se.sics.kompics.web.Web;
import se.sics.kompics.web.WebRequest;

/**
 *
 * @author alex
 */
public class NewVodSimulator extends ComponentDefinition implements GMain {

    private static final Logger logger = LoggerFactory.getLogger(NewVodSimulator.class);

    Positive<VodExperiment> simulator = positive(VodExperiment.class);
    Positive<VodNetwork> network = positive(VodNetwork.class);
    Positive<Timer> timer = positive(Timer.class);
    Negative<Web> web = negative(Web.class);

    private Random random;

    private VodConfiguration vodConfig;
    private BootstrapConfiguration bootstrapConfig;
    private CroupierConfiguration croupierConfig;
    private VodMonitorConfiguration monitorConfig;

    private AsIpGenerator ipGenerator;
    private PrefixMatcher pm;
    private Integer gvodIdentifierSpaceSize;

    private NewVodSimulator main;

    private ConsistentHashtable<Integer> gvodView;
    private HashMap<Integer, VodAddress> peerAddrs;
    private final Map<Integer, Component> peers;
    private final Map<Integer, Link> uploadLink;
    private final Map<Integer, Link> downloadLink;
    private int joined;

    public NewVodSimulator() {
        this.gvodView = new ConsistentHashtable<Integer>();
        this.peerAddrs = new HashMap<Integer, VodAddress>();
        this.peers = new HashMap<Integer, Component>();
        this.uploadLink = new HashMap<Integer, Link>();
        this.downloadLink = new HashMap<Integer, Link>();
        this.joined = 0;
        this.pm = PrefixMatcher.getInstance();

        subscribe(handleInit, control);
        subscribe(handleJoin, simulator);

        this.main = this;
    }

    Handler<VodSimulatorInit> handleInit = new Handler<VodSimulatorInit>() {

        @Override
        public void handle(VodSimulatorInit init) {
            logger.info("VodSimulator init");
            vodConfig = init.getGVodConfiguration();
            bootstrapConfig = init.getBootstrapConfiguration();
            croupierConfig = init.getCroupierConfiguration();
            monitorConfig = init.getMonitorConfiguration();

            //TODO Alex why is the seed an integer and not a long or even better a byte[]
            random = new Random(vodConfig.getSeed());
            ipGenerator = AsIpGenerator.getInstance(init.getCroupierConfiguration().getSeed());

            gvodIdentifierSpaceSize = Integer.MAX_VALUE;

        }
    };

    @Override
    public boolean changeUtility(String filename, int seekPos, int adjustedTime, OutputStream responseBody) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void stopPeer(Component peer) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    Handler<VodPeerJoin> handleJoin = new Handler<VodPeerJoin>() {

        @Override
        public void handle(VodPeerJoin event) {
            logger.info("VodSimulator join");

            Integer id = event.getGVodId();
            int utility = event.getUtility();

            // join with the next id if this id is taken
            Integer successor = gvodView.getNode(id);
            while (successor != null && successor.equals(id)) {
                id = (id + 1) % gvodIdentifierSpaceSize;
                successor = gvodView.getNode(id);
            }
            joined++;
            gvodView.addNode(id);
            logger.info("JOIN@{} ; {}", id, joined);

            //peer state
            boolean freeRider = false;
            //TODO Alex shouldn't the Percentage of FreeRiders be a simulation arg
            if (random.nextInt(100) < VodConfig.PERCENTAGE_FREERIDERS) { //arg
                freeRider = true;
            }
            int overlayId = event.getOverlayId();
            InetAddress ip = ipGenerator.generateIP();
            int port = 4444;
            final Address peerAddress = new Address(ip, port, id);
            final VodAddress addr = new VodAddress(peerAddress, overlayId, event.getNat());
            final Self peerSelf = new SelfImpl(addr);

            uploadLink.put(id, new Link(event.getUploadBw()));
            downloadLink.put(id, new Link(event.getDownloadBw()));
            peerAddrs.put(id, addr);
            
            Component newPeer = createAndStartNewPeer(id, peerSelf, event.getOverlayId());
            peers.put(id, newPeer);
        }
    };

    private Component createAndStartNewPeer(Integer id, Self peerSelf, int overlayId) {
        Component vodPeer = create(VodPeer.class);
        Component bootstrap = create(BootstrapClient.class);

        subscribe(handleReadingCompleted, vodPeer.getPositive(VodPeerPort.class));
        subscribe(handleDownloadCompleted, vodPeer.getPositive(VodPeerPort.class));
        subscribe(handleMessageSent, vodPeer.getNegative(VodNetwork.class));
        subscribe(handleQuitCompleted, vodPeer.getPositive(VodPeerPort.class));

        //connecting BootstrapClient
        connect(bootstrap.getNegative(Timer.class), timer);
        connect(bootstrap.getNegative(VodNetwork.class), network);

        //connecting VodPeer
        //nattraverser is not connected
        connect(timer, vodPeer.getNegative(Timer.class), new TimeoutOverlayIdFilter(overlayId));
        connect(network, vodPeer.getNegative(VodNetwork.class), new MsgDestFilterOverlayId(overlayId));
        connect(web, vodPeer.getPositive(Web.class), new WebRequestFilterOverlayId(overlayId));
        connect(vodPeer.getNegative(BootstrapPort.class), bootstrap.getPositive(BootstrapPort.class),
                new BootstrapResponseFilterOverlayId(overlayId));
        
        trigger(new BootstrapClientInit(peerSelf.clone(VodConfig.SYSTEM_OVERLAY_ID),
                    bootstrapConfig), bootstrap.getControl());
        
        short mtu = 1500;
        int asn = pm.matchIPtoAS(peerSelf.getAddress().getIp().getHostAddress());
        trigger(new VodPeerInit(peerSelf, main, bootstrapConfig, croupierConfig, vodConfig,
                monitorConfig, true, false, true, vodConfig.getTorrentFilename(), true, mtu, asn, 0, true, 10000l,
                10000l, false, null, null), vodPeer.getControl());

        trigger(new Start(), vodPeer.getControl());

        return vodPeer;
    }

    Handler<ReadingCompleted> handleReadingCompleted = new Handler<ReadingCompleted>() {

        @Override
        public void handle(ReadingCompleted event) {
            throw new UnsupportedOperationException();
        }
    };

    Handler<DirectMsg> handleMessageSent = new Handler<DirectMsg>() {

        @Override
        public void handle(DirectMsg msg) {
            throw new UnsupportedOperationException();
        }
    };

    Handler<DownloadCompletedSim> handleDownloadCompleted = new Handler<DownloadCompletedSim>() {

        @Override
        public void handle(DownloadCompletedSim event) {
            throw new UnsupportedOperationException();
        }
    };

    Handler<QuitCompleted> handleQuitCompleted = new Handler<QuitCompleted>() {

        @Override
        public void handle(QuitCompleted event) {
            throw new UnsupportedOperationException();
        }
    };

    private final static class WebRequestDestinationFilter extends ChannelFilter<WebRequest, Integer> {

        public WebRequestDestinationFilter(Integer destination) {
            super(WebRequest.class, destination, false);
        }

        @Override
        public Integer getValue(WebRequest event) {
            return event.getDestination();
        }
    }
}
