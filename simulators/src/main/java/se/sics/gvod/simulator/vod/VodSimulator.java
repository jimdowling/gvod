/**
 * This file is part of the Kompics P2P Framework.
 *
 * Copyright (C) 2009 Swedish Institute of Computer Science (SICS)
 * Copyright (C) 2009 Royal Institute of Technology (KTH)
 * 
 * Kompics is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package se.sics.gvod.simulator.vod;

import se.sics.gvod.simulator.common.StartDataCollection;
import se.sics.gvod.simulator.common.StopDataCollection;
import se.sics.gvod.simulator.common.ConsistentHashtable;
import se.sics.gvod.system.vod.VodConfiguration;
import se.sics.gvod.config.BootstrapConfiguration;
import se.sics.gvod.web.port.DownloadCompletedSim;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.io.*;
import java.net.InetAddress;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.sics.gvod.timer.SchedulePeriodicTimeout;
import se.sics.kompics.ChannelFilter;
import se.sics.kompics.Component;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.web.Web;
import se.sics.gvod.timer.Timer;
import se.sics.kompics.Positive;
import se.sics.kompics.Negative;
import se.sics.gvod.address.Address;
import se.sics.kompics.p2p.experiment.dsl.events.TerminateExperiment;
import se.sics.gvod.system.simulator.bw.model.Link;
import se.sics.gvod.timer.ScheduleTimeout;
import se.sics.gvod.system.peer.JoinGVod;
import se.sics.gvod.system.peer.VodPeer;
import se.sics.kompics.web.WebRequest;
import se.sics.gvod.system.peer.VodPeerInit;
import se.sics.kompics.Start;
import se.sics.kompics.Handler;
import se.sics.gvod.web.server.VodMonitorConfiguration;
import se.sics.kompics.Stop;
import se.sics.gvod.system.peer.events.QuitCompleted;
import se.sics.gvod.system.peer.events.Quit;
import se.sics.gvod.system.peer.events.JumpForward;
import se.sics.gvod.system.peer.events.JumpBackward;
import se.sics.gvod.system.simulator.bw.model.BwDelayedMessage;
import se.sics.gvod.system.peer.events.ReadingCompleted;
import org.apache.commons.math.stat.descriptive.SummaryStatistics;
import java.util.Collections;
import java.util.List;
import se.sics.ipasdistances.PrefixMatcher;
import se.sics.gvod.common.Self;
import se.sics.gvod.config.VodConfig;
import se.sics.gvod.config.CroupierConfiguration;
import se.sics.gvod.config.GradientConfiguration;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.net.Nat;
import se.sics.gvod.net.VodNetwork;
import se.sics.gvod.net.msgs.RewriteableMsg;
import se.sics.gvod.common.SelfImpl;
import se.sics.gvod.net.msgs.DirectMsg;
import se.sics.gvod.system.peer.VodPeerPort;
import se.sics.gvod.system.main.GMain;
import se.sics.ipasdistances.AsIpGenerator;

/**
 * 
 * @author gautier
 */
public final class VodSimulator extends ComponentDefinition implements GMain {

    Positive<VodExperiment> simulator = positive(VodExperiment.class);
    Positive<VodNetwork> network = positive(VodNetwork.class);
    Positive<Timer> timer = positive(Timer.class);
    Negative<Web> web = negative(Web.class);
    private static final Logger logger = LoggerFactory.getLogger(VodSimulator.class);
    private final Map<Integer, Component> peers;
    private ConsistentHashtable<Integer> gvodView;
    private ConsistentHashtable<Integer> gvodView2;
    private int peerIdSequence;
    private BootstrapConfiguration bootstrapConfiguration;
    private CroupierConfiguration croupierConfiguration;
    private GradientConfiguration gradientConfiguration;
    private VodConfiguration gvodConfiguration;
    private VodMonitorConfiguration monitorConfiguration;
    private Integer gvodIdentifierSpaceSize;
    private final Map<Integer, Link> uploadLink;
    private final Map<Integer, Link> downloadLink;
    private Map<Class<? extends RewriteableMsg>, ReceivedMessage> messageHistogram;
    private int expected = 0;
    private List<Integer> nodes;
    private Map<Integer, Long> nodeDownloadTimeNotFree, nodeDownloadTimeFree;
    private Map<Integer, Long> nodeLivingTime;
    private Map<Integer, Integer> nodeNbBufferingNotFree;
    private Map<Integer, Integer> nodeNbBufferingFree;
    private Map<Integer, Integer> nodeNbWaiting;
    private Map<Integer, Integer> nodeNbMisConnect;
    private Map<Integer, Long> nodeWaitingTimeNotFree;
    private Map<Integer, Long> nodeWaitingTimeFree;
    private long finishingTime;
    private Map<Long, SummaryStatistics> downloadUse = new HashMap<Long, SummaryStatistics>();
    private Map<Long, SummaryStatistics> uploadUse = new HashMap<Long, SummaryStatistics>();
    private Map<Long, Map<Integer, SummaryStatistics>> utilitySetSize =
            new HashMap<Long, Map<Integer, SummaryStatistics>>();
    private Integer seedId;
    private int seed;
    private Random random;
    private SummaryStatistics jumpForwardStats = new SummaryStatistics();
    private List<Integer> toMeasure;
    private boolean measure = false;
    private VodSimulator main;
    private PrefixMatcher pm = PrefixMatcher.getInstance();
    
    private AsIpGenerator ipGenerator;

    
    public VodSimulator() {
        main = this;
        uploadLink = new HashMap<Integer, Link>();
        downloadLink = new HashMap<Integer, Link>();
        peers = new HashMap<Integer, Component>();
        gvodView = new ConsistentHashtable<Integer>();
        gvodView2 = new ConsistentHashtable<Integer>();
        messageHistogram = new HashMap<Class<? extends RewriteableMsg>, ReceivedMessage>();
        nodeDownloadTimeNotFree = new HashMap<Integer, Long>();
        nodeDownloadTimeFree = new HashMap<Integer, Long>();
        nodeLivingTime = new HashMap<Integer, Long>();
        nodes = new ArrayList<Integer>();
        nodeNbBufferingNotFree = new HashMap<Integer, Integer>();
        nodeNbBufferingFree = new HashMap<Integer, Integer>();
        nodeNbWaiting = new HashMap<Integer, Integer>();
        nodeNbMisConnect = new HashMap<Integer, Integer>();
        nodeWaitingTimeNotFree = new HashMap<Integer, Long>();
        nodeWaitingTimeFree = new HashMap<Integer, Long>();
        toMeasure = new ArrayList<Integer>();
        subscribe(handleInit, control);
        subscribe(handleJoin, simulator);
        subscribe(handleQuit, simulator);
        subscribe(handleFail, simulator);
        subscribe(handleStartDataCollection, simulator);
        subscribe(handleStopDataCollection, simulator);
        subscribe(handleJumpForward, simulator);
        subscribe(handleJumpBackward, simulator);
//        subscribe(handleChangeUtility, simulator);
        subscribe(handleMessageReceived, network);
        subscribe(handleDelayedMessage, timer);
        subscribe(handleMakeItQuit, timer);
    }
    Handler<VodSimulatorInit> handleInit = new Handler<VodSimulatorInit>() {

        @Override
        public void handle(VodSimulatorInit init) {
            peers.clear();
            peerIdSequence = 0;
            bootstrapConfiguration = init.getBootstrapConfiguration();
            monitorConfiguration = init.getMonitorConfiguration();
            croupierConfiguration = init.getCroupierConfiguration();
            gradientConfiguration = init.getGradientConfiguration();
            gvodConfiguration = init.getGVodConfiguration();
            gvodIdentifierSpaceSize = Integer.MAX_VALUE;
            seed = gvodConfiguration.getSeed();
//            snapshot = new Snapshot(seed);
            random = new Random(gvodConfiguration.getSeed());
//            arg = Integer.parseInt(gvodConfiguration.getArg());
            SchedulePeriodicTimeout spt = new SchedulePeriodicTimeout(
                    30000, 30000);
            spt.setTimeoutEvent(new TakeSnapshot(spt));
            trigger(spt, timer);
            
            ipGenerator = AsIpGenerator.getInstance(init.getCroupierConfiguration().getSeed());            
        }
    };
    int joined = 0;
    Handler<VodPeerJoin> handleJoin = new Handler<VodPeerJoin>() {

        @Override
        public void handle(VodPeerJoin event) {
            Integer id = event.getGVodId();
            int utility = event.getUtility();

            // join with the next id if this id is taken
            Integer successor = gvodView.getNode(id);
            while (successor != null && successor.equals(id)) {
                id = (id + 1) % gvodIdentifierSpaceSize;
                successor = gvodView.getNode(id);
            }
            joined++;
            logger.info("JOIN@{} ; {}", id, joined);

            Component newPeer = createAndStartNewPeer(id, utility,
                    event.getDownloadBw(), event.getUploadBw(), event.getNat(),
                    event.getOverlayId());
            gvodView.addNode(id);
            gvodView2.addNode(id);

            boolean freeRider = false;
            if (random.nextInt(100) < VodConfig.PERCENTAGE_FREERIDERS) { //arg
                freeRider = true;
            }
//            snapshot.addPeer(id, event.getUploadBw());
            trigger(new JoinGVod(utility, event.isSeed(), event.getDownloadBw(),
                    event.getUploadBw(), freeRider, null, true),
                    newPeer.getPositive(VodPeerPort.class));
            if (!event.isSeed()) {
                long startedDownloadAt = System.currentTimeMillis();
                nodeLivingTime.put(id, startedDownloadAt);
                nodes.add(id);
                if (measure) {
                    toMeasure.add(id);
                    logger.info("add to toMesure {}", toMeasure.size());
                }
                expected++;
            } else {
                gvodView2.removeNode(id);
                seedId = id;
            }
        }
    };
    Handler<VodPeerFail> handleFail = new Handler<VodPeerFail>() {

        @Override
        public void handle(VodPeerFail event) {
            Integer id = gvodView2.getNode(event.getGVodId());

            logger.info("FAIL@" + id);

            if (gvodView2.size() == 0) {
                System.err.println("Empty network");
                return;
            }
            gvodView2.removeNode(id);
            stopAndDestroyPeer(id);
            expected--;
            nodes.remove(id);
            nodeDownloadTimeNotFree.remove(id);
        }
    };
    Handler<VodPeerQuit> handleQuit = new Handler<VodPeerQuit>() {

        @Override
        public void handle(VodPeerQuit event) {
            Integer id = gvodView2.getNode(event.getGVodId());

            logger.debug("QUIT@" + id);

            if (gvodView2.size() == 0) {
                System.err.println("Empty network");
                return;
            }
            trigger(new Quit(id), peers.get(id).getPositive(VodPeerPort.class));
        }
    };
    Handler<QuitCompleted> handleQuitCompleted = new Handler<QuitCompleted>() {

        @Override
        public void handle(QuitCompleted event) {
            logger.info("QUITCOMPLETED@" + event.getOverlayId());
            gvodView2.removeNode(event.getOverlayId());
            stopAndDestroyPeer(event.getOverlayId());
//            snapshot.quit(event.getGVodId());
        }
    };
//    Handler<GVodChangeUtility> handleChangeUtility = new Handler<GVodChangeUtility>() {
//
//        @Override
//        public void handle(GVodChangeUtility event) {
//            Integer id = gvodView2.getNode(event.getGVodId());
//            if (gvodView2.size() == 0) {
//                System.err.println("Empty network");
//                return;
//            }
//            trigger(new ChangeUtility(event.getUtility()), peers.get(id).getPositive(GVodPeerPort.class));
//        }
//    };
    Handler<VodJumpForward> handleJumpForward = new Handler<VodJumpForward>() {

        @Override
        public void handle(VodJumpForward event) {
            Integer id = gvodView2.getNode(event.getOverlayId());
            if (gvodView2.size() == 0) {
                System.err.println("Empty network");
                return;
            }
            trigger(new JumpForward(event.getOverlayId(),
                    event.getGap()), peers.get(id).getPositive(VodPeerPort.class));
        }
    };
    Handler<VodJumpBackward> handleJumpBackward = new Handler<VodJumpBackward>() {

        @Override
        public void handle(VodJumpBackward event) {
            Integer id = gvodView2.getNode(event.getOverlayId());
            if (gvodView2.size() == 0) {
                System.err.println("Empty network");
                return;
            }
            trigger(new JumpBackward(event.getOverlayId(),
                    event.getGap()), peers.get(id).getPositive(VodPeerPort.class));
        }
    };

    @Override
    public boolean changeUtility(String filename, int seekPos, int adjustedTime, OutputStream responseBody) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private final static class WebRequestDestinationFilter extends ChannelFilter<WebRequest, Integer> {

        public WebRequestDestinationFilter(Integer destination) {
            super(WebRequest.class, destination, false);
        }

        @Override
        public Integer getValue(WebRequest event) {
            return event.getDestination();
        }
    }

    private Component createAndStartNewPeer(Integer id, int utility,
            long downloadBw, long uploadBw, Nat nat, int overlayId) {
        Component peer = create(VodPeer.class);
//        int peerId = ++peerIdSequence;
        ++peerIdSequence;
        
        InetAddress ip = ipGenerator.generateIP();
        int port = 4444;
        final Address peerAddress = new Address(ip, port, id);
        final VodAddress addr = new VodAddress(peerAddress, overlayId, nat);

        subscribe(handleReadingCompleted, peer.getPositive(VodPeerPort.class));
        subscribe(handleDownloadCompleted, peer.getPositive(VodPeerPort.class));
        subscribe(handleMessageSent, peer.getNegative(VodNetwork.class));

        connect(timer, peer.getNegative(Timer.class));
        connect(web, peer.getPositive(Web.class),
                new WebRequestDestinationFilter(id));

        subscribe(handleQuitCompleted, peer.getPositive(VodPeerPort.class));


        Self selfContainer = new SelfImpl(addr);

        short mtu = 1500;
        int asn = pm.matchIPtoAS(peerAddress.getIp().getHostAddress());
        trigger(new VodPeerInit(selfContainer, main, bootstrapConfiguration,
                croupierConfiguration, gvodConfiguration, 
                monitorConfiguration, true, false, true,
                null, true, mtu, asn,
                0, true, 10000l, 10000l, false, null, null
                ), 
                peer.getControl());
        
        trigger(new Start(), peer.getControl());
        peers.put(id, peer);
        uploadLink.put(id, new Link(uploadBw));
        downloadLink.put(id, new Link(downloadBw));
        return peer;
    }

    private void stopAndDestroyPeer(Integer id) {
        Component peer = peers.get(id);

        trigger(new Stop(), peer.getControl());

        unsubscribe(handleQuitCompleted, peer.getPositive(VodPeerPort.class));

        disconnect(timer, peer.getNegative(Timer.class));
        disconnect(web, peer.getPositive(Web.class));

        peers.remove(id);
        destroy(peer);
//        snapshot.removePeer(id);
    }
    Handler<DirectMsg> handleMessageSent = new Handler<DirectMsg>() {

        @Override
        public void handle(DirectMsg msg) {
            if (!(msg instanceof DirectMsg)) {
                trigger(msg, network);
            } else {
                DirectMsg message = (DirectMsg) msg;
                // message just sent by some peer goes into peer's up pipe
                Link link = uploadLink.get(message.getVodSource().getId());
                long delay = link.addMessage(message);
                if (delay == 0) {
                    // immediately send to cloud
                    trigger(message, network);
                    return;
                }
                ScheduleTimeout st = new ScheduleTimeout(delay);
                st.setTimeoutEvent(new BwDelayedMessage(st, message, true));
                trigger(st, timer);
            }
        }
    };
    Handler<DirectMsg> handleMessageReceived = new Handler<DirectMsg>() {

        @Override
        public void handle(DirectMsg msg) {
            if (!(msg instanceof DirectMsg)) {
                for (Component peer : peers.values()) {
                    trigger(msg, peer.getNegative(VodNetwork.class));
                }
            } else {
                DirectMsg message = (DirectMsg) msg;
                // traffic stats
                ReceivedMessage rm = messageHistogram.get(message.getClass());
                if (rm == null) {
                    rm = new ReceivedMessage(message.getClass(), 0, 0);
                    messageHistogram.put(message.getClass(), rm);
                }
                rm.incrementCount();
                rm.incrementSize(message.getSize());

                // message to be received by some peer goes into peer's down pipe
                Link link = downloadLink.get(message.getVodDestination().getId());
                if (link == null) {
                    return;
                }
                long delay = link.addMessage(message);
                if (delay == 0) {
                    // immediately deliver to peer
                    Component peer = peers.get(message.getVodDestination().getId());
                    if (peer != null) {
                        trigger(message, peer.getNegative(VodNetwork.class));
                    }
                    return;
                }
                ScheduleTimeout st = new ScheduleTimeout(delay);
                st.setTimeoutEvent(new BwDelayedMessage(st, message, false));
                trigger(st, timer);
            }
        }
    };
    Handler<BwDelayedMessage> handleDelayedMessage = new Handler<BwDelayedMessage>() {

        @Override
        public void handle(BwDelayedMessage delayedMessage) {
            if (delayedMessage.isBeingSent()) {
                // message comes out of upload pipe
                DirectMsg message = delayedMessage.getMessage();
                // and goes to the network cloud
                trigger(message, network);
            } else {
                // message comes out of download pipe
                DirectMsg message = delayedMessage.getMessage();
                Component peer = peers.get(message.getVodDestination().getId());
                if (peer != null) {
                    // and goes to the peer
                    trigger(message, peer.getNegative(VodNetwork.class));
                }
            }
        }
    };
    Handler<ReadingCompleted> handleReadingCompleted = new Handler<ReadingCompleted>() {

        @Override
        public void handle(ReadingCompleted event) {
            expected--;
            // a leecher just became a seed
            logger.debug("Peer {} completed watching.", event.getPeer());
            Integer peerId = event.getPeer().getId();
            if (toMeasure.contains(peerId)) {
                if (event.isFreeRider()) {
                    nodeNbBufferingFree.put(peerId, event.getNbBuffering());
                    nodeWaitingTimeFree.put(peerId, event.getWaitingTime());
                } else {
                    nodeNbBufferingNotFree.put(peerId, event.getNbBuffering());
                    nodeWaitingTimeNotFree.put(peerId, event.getWaitingTime());
                }
            }

            nodeNbWaiting.put(peerId, event.getWaiting());
            nodeNbMisConnect.put(peerId, event.getMisConnect());


            trigger(new Quit(peerId), peers.get(peerId).getPositive(VodPeerPort.class));
            if (!utilitySetSize.containsKey(downloadLink.get(event.getPeer().getId()).getCapacity())) {
                utilitySetSize.put(downloadLink.get(event.getPeer().getId()).getCapacity(),
                        new HashMap<Integer, SummaryStatistics>());
            }
            Map<Integer, SummaryStatistics> map = utilitySetSize.get(downloadLink.get(event.getPeer().getId()).getCapacity());

            Map<Integer, Long> utilityEvolution = event.getUtilitySetSizeEvolution();

            if (utilityEvolution != null) {
                for (int size : utilityEvolution.keySet()) {
                    if (!map.containsKey(size)) {
                        map.put(size, new SummaryStatistics());
                    }
                    map.get(size).addValue(event.getUtilitySetSizeEvolution().get(size));
                }
            }
            if (event.getJumpForwardTime() != 0) {
                jumpForwardStats.addValue(event.getJumpForwardTime());
            }

            if (toMeasure.contains(peerId)) {
                if (!event.isFreeRider()) {
                    for (Integer key : event.getUtilityAfterTime().keySet()) {
                        try {
                            FileWriter writer;
                            writer = new FileWriter(downloadLink.get(event.getPeer().getId()).getCapacity() + "gvodutilityAfter"
                                    + VodConfig.PERCENTAGE_FREERIDERS + "NotFree_superseed", true); // arg
                            String text = key
                                    + "\t" + event.getUtilityAfterTime().get(key)
                                    + "\n";
                            writer.write(text, 0, text.length());
                            writer.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    for (Integer key : event.getUtilityAfterTime().keySet()) {
                        try {
                            FileWriter writer;
                            writer = new FileWriter(downloadLink.get(event.getPeer().getId()).getCapacity() + "gvodutilityAfter"
                                    + VodConfig.PERCENTAGE_FREERIDERS + "Free_superseed", true); // arg
                            String text = key
                                    + "\t" + event.getUtilityAfterTime().get(key)
                                    + "\n";
                            writer.write(text, 0, text.length());
                            writer.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            if (expected == 0) {
                // all joined peers have completed the watching so we can
                // terminate the simulation
                trigger(new TerminateExperiment(), simulator);
                finishingTime = System.currentTimeMillis();
                long now = System.currentTimeMillis();
                for (Integer id : nodeLivingTime.keySet()) {
                    long started = nodeLivingTime.get(peerId);
                    nodeLivingTime.put(id, now - started);
                }
                logStatistics();
                System.exit(0);
            }
        }
    };
    Handler<MakeItQuit> handleMakeItQuit = new Handler<MakeItQuit>() {

        @Override
        public void handle(MakeItQuit event) {
            trigger(new Quit(event.getPeerId()), peers.get(event.getPeerId()).getPositive(VodPeerPort.class));
        }
    };
    Handler<DownloadCompletedSim> handleDownloadCompleted = new Handler<DownloadCompletedSim>() {

        @Override
        public void handle(DownloadCompletedSim event) {
            // a leecher just became a seed
            logger.info("Peer {} completed downloading.", event.getPeer());
            Integer peerId = event.getPeer().getId();

            if (toMeasure.contains(peerId)) {
                if (event.isFree() && !event.isJumped()) {
                    nodeDownloadTimeFree.put(peerId, event.getDownloadTime());
                } else if (!event.isJumped()) {
                    nodeDownloadTimeNotFree.put(peerId, event.getDownloadTime());
                }
            }
            if (!downloadUse.containsKey(downloadLink.get(event.getPeer().getId()).getCapacity())) {
                downloadUse.put(downloadLink.get(event.getPeer().getId()).getCapacity(),
                        new SummaryStatistics());
                uploadUse.put(downloadLink.get(event.getPeer().getId()).getCapacity(),
                        new SummaryStatistics());
            }
            downloadUse.get(downloadLink.get(event.getPeer().getId()).getCapacity()).addValue(
                    downloadLink.get(event.getPeer().getId()).getBwUsePs() / 1024);
            uploadUse.get(downloadLink.get(event.getPeer().getId()).getCapacity()).addValue(
                    uploadLink.get(event.getPeer().getId()).getBwUsePs() / 1024);
            nodes.remove(peerId);
            logger.info("peer that didn't finish downloading : ");
            int i = 1;
            for (Integer id : nodes) {
                logger.info(i + ") " + id.toString());
                i++;
            }
            logger.info("=====================================");
        }
    };
    Handler<StartDataCollection> handleStartDataCollection = new Handler<StartDataCollection>() {

        @Override
        public void handle(StartDataCollection event) {
            logger.info("start mesures");
            measure = true;
        }
    };
    Handler<StopDataCollection> handleStopDataCollection = new Handler<StopDataCollection>() {

        @Override
        public void handle(StopDataCollection event) {
            logger.info("end mesure");
            measure = false;
        }
    };

    private void logStatistics() {
//        snapshot.generateGraphVizReport();
        SummaryStatistics downloadTimeNotFree = new SummaryStatistics();
        SummaryStatistics downloadTime99NotFree = new SummaryStatistics();
        SummaryStatistics nbBufferingNotFree = new SummaryStatistics();
        SummaryStatistics nbNBufferingNotFree = new SummaryStatistics();
        SummaryStatistics statsWaitingNotFree = new SummaryStatistics();
        SummaryStatistics downloadTimeFree = new SummaryStatistics();
        SummaryStatistics downloadTime99Free = new SummaryStatistics();
        SummaryStatistics nbBufferingFree = new SummaryStatistics();
        SummaryStatistics nbNBufferingFree = new SummaryStatistics();
        SummaryStatistics statsWaitingFree = new SummaryStatistics();
        SummaryStatistics nbWaiting = new SummaryStatistics();
        SummaryStatistics nbMisConnect = new SummaryStatistics();

        Map<Long, SummaryStatistics> downloadNotFree = new HashMap<Long, SummaryStatistics>();
        Map<Long, SummaryStatistics> download99NotFree = new HashMap<Long, SummaryStatistics>();
        Map<Long, ArrayList<Long>> list99NotFree = new HashMap<Long, ArrayList<Long>>();
        Map<Long, SummaryStatistics> totalUploadUse = new HashMap<Long, SummaryStatistics>();
        Map<Long, SummaryStatistics> waitingNotFree = new HashMap<Long, SummaryStatistics>();
        Map<Long, SummaryStatistics> bufferingNotFree = new HashMap<Long, SummaryStatistics>();
        Map<Long, SummaryStatistics> notBufferingNotFree = new HashMap<Long, SummaryStatistics>();
        Map<Long, SummaryStatistics> downloadFree = new HashMap<Long, SummaryStatistics>();
        Map<Long, SummaryStatistics> download99Free = new HashMap<Long, SummaryStatistics>();
        Map<Long, ArrayList<Long>> list99Free = new HashMap<Long, ArrayList<Long>>();
        Map<Long, SummaryStatistics> waitingFree = new HashMap<Long, SummaryStatistics>();
        Map<Long, SummaryStatistics> bufferingFree = new HashMap<Long, SummaryStatistics>();
        Map<Long, SummaryStatistics> notBufferingFree = new HashMap<Long, SummaryStatistics>();

        ArrayList<Long> temp = new ArrayList<Long>();
        long totalDownload = 0;
        long totalUpload = 0;
        for (Integer node : nodeDownloadTimeNotFree.keySet()) {
            downloadTimeNotFree.addValue(nodeDownloadTimeNotFree.get(node));
            temp.add(nodeDownloadTimeNotFree.get(node));
            if (!downloadNotFree.containsKey(downloadLink.get(node).getCapacity())) {
                downloadNotFree.put(downloadLink.get(node).getCapacity(),
                        new SummaryStatistics());
                download99NotFree.put(downloadLink.get(node).getCapacity(),
                        new SummaryStatistics());
                list99NotFree.put(downloadLink.get(node).getCapacity(),
                        new ArrayList<Long>());
                totalUploadUse.put(downloadLink.get(node).getCapacity(),
                        new SummaryStatistics());
                waitingNotFree.put(downloadLink.get(node).getCapacity(), new SummaryStatistics());
                bufferingNotFree.put(downloadLink.get(node).getCapacity(), new SummaryStatistics());
                notBufferingNotFree.put(downloadLink.get(node).getCapacity(), new SummaryStatistics());
            }
            downloadNotFree.get(downloadLink.get(node).getCapacity()).addValue(nodeDownloadTimeNotFree.get(node));
            list99NotFree.get(downloadLink.get(node).getCapacity()).add(nodeDownloadTimeNotFree.get(node));
            totalUploadUse.get(downloadLink.get(node).getCapacity()).addValue(
                    uploadLink.get(node).getBwUsePs() / 1024);
            waitingNotFree.get(downloadLink.get(node).getCapacity()).addValue(nodeWaitingTimeNotFree.get(node));
            bufferingNotFree.get(downloadLink.get(node).getCapacity()).addValue(nodeNbBufferingNotFree.get(node));
            if (nodeNbBufferingNotFree.get(node) == 0) {
                notBufferingNotFree.get(downloadLink.get(node).getCapacity()).addValue(1);
            } else {
                notBufferingNotFree.get(downloadLink.get(node).getCapacity()).addValue(0);
            }
            totalDownload += downloadLink.get(node).getBwUse();
            totalUpload += uploadLink.get(node).getBwUse();
        }

        Collections.sort(temp);
        int i = temp.size() * 5 / 100;
        while (i > 0) {
            temp.remove(temp.size() - 1);
            i--;
        }
        for (Long val : temp) {
            downloadTime99NotFree.addValue(val);
        }

        for (Long key : list99NotFree.keySet()) {
            Collections.sort(list99NotFree.get(key));
            i = list99NotFree.get(key).size() / 100;
            while (i > 0) {
                long toRemove = list99NotFree.get(key).size() - 1;
                list99NotFree.get(key).remove(toRemove);
                i--;
            }
            for (Long val : list99NotFree.get(key)) {
                download99NotFree.get(key).addValue(val);
            }
        }

        if (downloadLink.get(seedId) != null) {
            totalDownload += downloadLink.get(seedId).getBwUse();
            totalUpload += uploadLink.get(seedId).getBwUse();

            if (totalUploadUse.get(downloadLink.get(seedId).getCapacity()) == null) {
                totalUploadUse.put(downloadLink.get(seedId).getCapacity(),
                        new SummaryStatistics());
            }
            totalUploadUse.get(downloadLink.get(seedId).getCapacity()).addValue(
                    uploadLink.get(seedId).getBwUsePs() / 1280);
        }
        for (int bufferingNb : nodeNbBufferingNotFree.values()) {
            nbBufferingNotFree.addValue(bufferingNb);
            if (bufferingNb == 0) {
                nbNBufferingNotFree.addValue(1);
            } else {
                nbNBufferingNotFree.addValue(0);
            }
        }
        for (int waitingTime : nodeNbWaiting.values()) {
            nbWaiting.addValue(waitingTime);
        }

        for (int misConnect : nodeNbMisConnect.values()) {
            nbMisConnect.addValue(misConnect);
        }

        for (long waitingTime : nodeWaitingTimeNotFree.values()) {
            statsWaitingNotFree.addValue(waitingTime);
        }
        int messages = 0;
        long traffic = 0;
        for (ReceivedMessage rm : messageHistogram.values()) {
            messages += rm.getTotalCount();
            traffic += rm.getTotalSize();
        }


        temp = new ArrayList<Long>();
        for (Integer node : nodeDownloadTimeFree.keySet()) {
            downloadTimeFree.addValue(nodeDownloadTimeFree.get(node));
            temp.add(nodeDownloadTimeFree.get(node));
            if (!downloadFree.containsKey(downloadLink.get(node).getCapacity())) {
                downloadFree.put(downloadLink.get(node).getCapacity(),
                        new SummaryStatistics());
                download99Free.put(downloadLink.get(node).getCapacity(),
                        new SummaryStatistics());
                list99Free.put(downloadLink.get(node).getCapacity(),
                        new ArrayList<Long>());
                totalUploadUse.put(downloadLink.get(node).getCapacity(),
                        new SummaryStatistics());
                waitingFree.put(downloadLink.get(node).getCapacity(), new SummaryStatistics());
                bufferingFree.put(downloadLink.get(node).getCapacity(), new SummaryStatistics());
                notBufferingFree.put(downloadLink.get(node).getCapacity(), new SummaryStatistics());
            }
            downloadFree.get(downloadLink.get(node).getCapacity()).addValue(nodeDownloadTimeFree.get(node));
            list99Free.get(downloadLink.get(node).getCapacity()).add(nodeDownloadTimeFree.get(node));
            totalUploadUse.get(downloadLink.get(node).getCapacity()).addValue(
                    uploadLink.get(node).getBwUsePs() / 1024);
            waitingFree.get(downloadLink.get(node).getCapacity()).addValue(nodeWaitingTimeFree.get(node));
            bufferingFree.get(downloadLink.get(node).getCapacity()).addValue(nodeNbBufferingFree.get(node));
            if (nodeNbBufferingFree.get(node) == 0) {
                notBufferingFree.get(downloadLink.get(node).getCapacity()).addValue(1);
            } else {
                notBufferingFree.get(downloadLink.get(node).getCapacity()).addValue(0);
            }
            totalDownload += downloadLink.get(node).getBwUse();
            totalUpload += uploadLink.get(node).getBwUse();
        }

        Collections.sort(temp);
        i = temp.size() * 5 / 100;
        while (i > 0) {
            temp.remove(temp.size() - 1);
            i--;
        }
        for (Long val : temp) {
            downloadTime99Free.addValue(val);
        }

        for (Long key : list99Free.keySet()) {
            Collections.sort(list99Free.get(key));
            i = list99Free.get(key).size() / 100;
            while (i > 0) {
                long toRemove = list99Free.get(key).size() - 1;
                list99Free.get(key).remove(toRemove);
                i--;
            }
            for (Long val : list99Free.get(key)) {
                download99Free.get(key).addValue(val);
            }
        }


        for (int bufferingNb : nodeNbBufferingFree.values()) {
            nbBufferingFree.addValue(bufferingNb);
            if (bufferingNb == 0) {
                nbNBufferingFree.addValue(1);
            } else {
                nbNBufferingFree.addValue(0);
            }
        }

        for (long waitingTime : nodeWaitingTimeFree.values()) {
            statsWaitingFree.addValue(waitingTime);
        }


        logger.info("=================================================");
        logger.info("Total Upload : {}", totalUpload);
        logger.info("Total Download : {}", totalDownload);
        logger.info("diff : {}", Math.abs(totalUpload - totalDownload));
        logger.info("=================================================");
        logger.info("NOT FREE");
        logger.info("Number of nodes: {}", downloadTimeNotFree.getN());
        logger.info("Min download time:  {} ms ({})", downloadTimeNotFree.getMin(),
                durationToString(Math.round(downloadTimeNotFree.getMin())));
        logger.info("Max download time:  {} ms ({})", downloadTimeNotFree.getMax(),
                durationToString(Math.round(downloadTimeNotFree.getMax())));
        logger.info("Avg download time:  {} ms ({})", downloadTimeNotFree.getMean(),
                durationToString(Math.round(downloadTimeNotFree.getMean())));
        logger.info("Std download time:  {} ms ({})",
                downloadTimeNotFree.getStandardDeviation(),
                durationToString(Math.round(downloadTimeNotFree.getStandardDeviation())));
        logger.info("=================================================");
        logger.info("FREE");
        logger.info("Number of nodes: {}", downloadTimeFree.getN());
        logger.info("Min download time:  {} ms ({})", downloadTimeFree.getMin(),
                durationToString(Math.round(downloadTimeFree.getMin())));
        logger.info("Max download time:  {} ms ({})", downloadTimeFree.getMax(),
                durationToString(Math.round(downloadTimeFree.getMax())));
        logger.info("Avg download time:  {} ms ({})", downloadTimeFree.getMean(),
                durationToString(Math.round(downloadTimeFree.getMean())));
        logger.info("Std download time:  {} ms ({})",
                downloadTimeFree.getStandardDeviation(),
                durationToString(Math.round(downloadTimeFree.getStandardDeviation())));

        int nbNodes = nodeDownloadTimeNotFree.size() + nodeDownloadTimeFree.size();
        try {
            FileWriter writer = new FileWriter("gvod" + VodConfig.PERCENTAGE_FREERIDERS // arg
                    + "NotFree_superseed.out", true);
            String text = seed
                    + "\t" + nbNodes
                    + "\t" + downloadTimeNotFree.getMean()
                    + "\t" + downloadTimeNotFree.getMax()
                    + "\t" + downloadTimeNotFree.getMin()
                    + "\t" + downloadTime99NotFree.getMax()
                    + "\t" + statsWaitingNotFree.getMean()
                    + "\t" + nbBufferingNotFree.getMean()
                    + "\t" + nbNBufferingNotFree.getMean()
                    + "\n";
            writer.write(text, 0, text.length());
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            FileWriter writer = new FileWriter("gvod" + VodConfig.PERCENTAGE_FREERIDERS + "Free_superseed.out", true);
            String text = seed
                    + "\t" + nbNodes
                    + "\t" + downloadTimeFree.getMean()
                    + "\t" + downloadTimeFree.getMax()
                    + "\t" + downloadTimeFree.getMin()
                    + "\t" + downloadTime99Free.getMax()
                    + "\t" + statsWaitingFree.getMean()
                    + "\t" + nbBufferingFree.getMean()
                    + "\t" + nbNBufferingFree.getMean()
                    + "\n";
            writer.write(text, 0, text.length());
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }


        for (long bW : downloadNotFree.keySet()) {
            logger.info("=================================================");

            SummaryStatistics stats = downloadNotFree.get(bW);
            SummaryStatistics stats99 = download99NotFree.get(bW);
            SummaryStatistics statsUpTotal = totalUploadUse.get(bW);
            SummaryStatistics statsDown = downloadUse.get(bW);
            SummaryStatistics statsUp = uploadUse.get(bW);
            SummaryStatistics statsWait = waitingNotFree.get(bW);
            SummaryStatistics statsBuf = bufferingNotFree.get(bW);
            SummaryStatistics statsNBuf = notBufferingNotFree.get(bW);

            try {
                FileWriter writer = new FileWriter(bW + "gvod" + VodConfig.PERCENTAGE_FREERIDERS + "NotFree_superseed.out", true);
                String text = nbNodes
                        + "\t" + stats.getMean()
                        + "\t" + stats.getMax()
                        + "\t" + stats.getMin()
                        + "\t" + stats99.getMax()
                        + "\t" + statsWait.getMean()
                        + "\t" + statsBuf.getMean()
                        + "\t" + statsNBuf.getMean()
                        + "\n";
                writer.write(text, 0, text.length());
                writer.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            Map<Integer, SummaryStatistics> map = utilitySetSize.get(bW);

            logger.info("BandWith down {}KB/S, nb of nodes {} ("
                    + ((float) stats.getN() / downloadTimeNotFree.getN()) * 100 + "%)", bW / 1024, stats.getN());
            logger.info("Number of nodes: {}", stats.getN());
            logger.info("Min download time:  {} ms ({})", stats.getMin(),
                    durationToString(Math.round(stats.getMin())));
            logger.info("Max download time:  {} ms ({})", stats.getMax(),
                    durationToString(Math.round(stats.getMax())));
            logger.info("Avg download time:  {} ms ({})", stats.getMean(),
                    durationToString(Math.round(stats.getMean())));
            logger.info("Std download time:  {} ms ({})",
                    stats.getStandardDeviation(),
                    durationToString(Math.round(stats.getStandardDeviation())));
            logger.info("Avg upload Use Total: {} KBytes/s", statsUpTotal.getMean());
            logger.info("Avg upload Use during download: {} KBytes/s", statsUp.getMean());
            logger.info("Max upload Use during download: {} KBytes/s", statsUp.getMax());
            logger.info("Avg download Use Total during downloag: {} KBytes/s", statsDown.getMean());
            logger.info("Min download Use Total during downloag: {} KBytes/s", statsDown.getMin());
            logger.info("-----------------------------------------------");
            logger.info("Avg buffering Time: {} ms ({})", statsWait.getMean(),
                    durationToString(Math.round(statsWait.getMean())));
            logger.info("Avg number of buffering : {}", statsBuf.getMean());
        }

        for (long bW : downloadFree.keySet()) {
            logger.info("=================================================");

            SummaryStatistics stats = downloadFree.get(bW);
            SummaryStatistics stats99 = download99Free.get(bW);
            SummaryStatistics statsUpTotal = totalUploadUse.get(bW);
            SummaryStatistics statsDown = downloadUse.get(bW);
            SummaryStatistics statsUp = uploadUse.get(bW);
            SummaryStatistics statsWait = waitingFree.get(bW);
            SummaryStatistics statsBuf = bufferingFree.get(bW);
            SummaryStatistics statsNBuf = notBufferingFree.get(bW);

            try {
                FileWriter writer = new FileWriter(bW + "gvod" //                        + arg + "Free_superseed.out"
                        , true);
                String text = nbNodes
                        //                        + "\t" + gvodConfiguration.getArg()
                        + "\t" + stats.getMean()
                        + "\t" + stats.getMax()
                        + "\t" + stats.getMin()
                        + "\t" + stats99.getMax()
                        + "\t" + statsWait.getMean()
                        + "\t" + statsBuf.getMean()
                        + "\t" + statsNBuf.getMean()
                        + "\n";
                writer.write(text, 0, text.length());
                writer.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            Map<Integer, SummaryStatistics> map = utilitySetSize.get(bW);

//            logger.info("BandWith down {}KB/S, nb of nodes {} (" +
//                    ((float) stats.getN() / downloadTimeNotFree.getN()) * 100 + "%)", bW / 1024, stats.getN());
//            logger.info("Number of nodes: {}", stats.getN());
//            logger.info("Min download time:  {} ms ({})", stats.getMin(),
//                    durationToString(Math.round(stats.getMin())));
//            logger.info("Max download time:  {} ms ({})", stats.getMax(),
//                    durationToString(Math.round(stats.getMax())));
//            logger.info("Avg download time:  {} ms ({})", stats.getMean(),
//                    durationToString(Math.round(stats.getMean())));
//            logger.info("Std download time:  {} ms ({})",
//                    stats.getStandardDeviation(),
//                    durationToString(Math.round(stats.getStandardDeviation())));
//            logger.info("Avg upload Use Total: {} KBytes/s", statsUpTotal.getMean());
//            logger.info("Avg upload Use during download: {} KBytes/s", statsUp.getMean());
//            logger.info("Max upload Use during download: {} KBytes/s", statsUp.getMax());
//            logger.info("Avg download Use Total during downloag: {} KBytes/s", statsDown.getMean());
//            logger.info("Min download Use Total during downloag: {} KBytes/s", statsDown.getMin());
//            logger.info("-----------------------------------------------");
//            logger.info("Avg buffering Time: {} ms ({})", statsWait.getMean(),
//                    durationToString(Math.round(statsWait.getMean())));
//            logger.info("Avg number of buffering : {}", statsBuf.getMean());
//            logger.info("do not buffer : {}%", donotbuffer * 100);
//            for (int size : map.keySet()) {
//                logger.info("UtilitySet of Size " + size + " during {}ms ({})", map.get(size).getMean(),
//                        durationToString(Math.round(map.get(size).getMean())));
//            }
        }

        logger.info("=================================================");
        logger.info("Min nb of buffering: {}", nbBufferingNotFree.getMin());
        logger.info("Max nb of buffering: {}", nbBufferingNotFree.getMax());
        logger.info("Avg nb of buffering:  {}", nbBufferingNotFree.getMean());
        logger.info("percent of nonbuffering:  {}", nbNBufferingNotFree.getMean());
        logger.info("Std nb of buffering:  {}",
                nbBufferingNotFree.getStandardDeviation());
        logger.info("=================================================");
        logger.info("Min waiting: {} ms ({})", statsWaitingNotFree.getMin(),
                durationToString(Math.round(statsWaitingNotFree.getMin())));
        logger.info("Max waiting: {} ms ({})", statsWaitingNotFree.getMax(),
                durationToString(Math.round(statsWaitingNotFree.getMax())));
        logger.info("Avg waiting:  {} ms ({})", statsWaitingNotFree.getMean(),
                durationToString(Math.round(statsWaitingNotFree.getMean())));
        logger.info("Avg waiting (free):  {} ms ({})", statsWaitingFree.getMean(),
                durationToString(Math.round(statsWaitingFree.getMean())));
        logger.info("Std of waiting:  {} ms ({})",
                statsWaitingNotFree.getStandardDeviation(),
                durationToString(Math.round(statsWaitingNotFree.getStandardDeviation())));
        logger.info("=================================================");
        logger.info("Min jumpTime : {} ms ({})", jumpForwardStats.getMin(),
                durationToString(Math.round(jumpForwardStats.getMin())));
        logger.info("Max jumpTime : {} ms ({})", jumpForwardStats.getMax(),
                durationToString(Math.round(jumpForwardStats.getMax())));
        logger.info("Avg jumpTime : {} ms ({})", jumpForwardStats.getMean(),
                durationToString(Math.round(jumpForwardStats.getMean())));
//        logger.info("=================================================");
//        logger.info("Min nb of waiting: {}", nbWaiting.getMin());
//        logger.info("Max nb of waiting: {}", nbWaiting.getMax());
//        logger.info("Avg nb of waiting:  {}", nbWaiting.getMean());
//        logger.info("Std nb of waiting:  {}",
//                nbWaiting.getStandardDeviation());
//        logger.info("=================================================");
//        logger.info("Min nb of MisConnect: {}", nbMisConnect.getMin());
//        logger.info("Max nb of MisConnect: {}", nbMisConnect.getMax());
//        logger.info("Avg nb of MisConnect:  {}", nbMisConnect.getMean());
//        logger.info("Std nb of MisConnect:  {}",
//                nbMisConnect.getStandardDeviation());
//        logger.info("Total nb of MisConnect:  {}",
//                nbMisConnect.getN());
        logger.info("=================================================");
        logger.info("Total number of messages: {}", messages);
        logger.info("Total amount of traffic:  {} bytes", traffic);
        for (Map.Entry<Class<? extends RewriteableMsg>, ReceivedMessage> entry : messageHistogram.entrySet()) {
            logger.info("{}: #={}  \t bytes={}", new Object[]{
                        String.format("%22s", entry.getKey().getName()),
                        entry.getValue().getTotalCount(),
                        entry.getValue().getTotalSize()});
        }
        logger.info("=================================================");

    }

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
                sb.append(".").append(String.format("%03d", ms));
            }
            sb.append("s");
        }
        return sb.toString();
    }

    @Override
    public void stopPeer(Component peer) {
        // DO NOTHING
    }
}
