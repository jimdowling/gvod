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
package se.sics.gvod.system.peer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Positive;
import se.sics.kompics.Negative;
import se.sics.gvod.timer.Timer;
import se.sics.gvod.bootstrap.port.BootstrapPort;
import se.sics.gvod.bootstrap.port.BootstrapRequest;
import se.sics.gvod.bootstrap.port.BootstrapResponse;
import se.sics.gvod.bootstrap.port.Rebootstrap;
import se.sics.gvod.bootstrap.port.RebootstrapResponse;
import se.sics.gvod.config.BootstrapConfiguration;
import se.sics.kompics.Component;
import se.sics.gvod.address.Address;
import se.sics.kompics.web.Web;
import se.sics.gvod.system.storage.MetaInfoExec;
import se.sics.gvod.web.server.VodMonitorConfiguration;
import java.io.File;
import java.io.FileInputStream;
import se.sics.gvod.bootstrap.port.AddOverlayResponse;
import se.sics.gvod.common.Self;
import se.sics.gvod.common.UtilityVod;
import se.sics.gvod.common.msgs.DataMsg;
import se.sics.gvod.croupier.Croupier;
import se.sics.gvod.croupier.events.CroupierInit;
import se.sics.gvod.croupier.CroupierPort;
import se.sics.gvod.croupier.PeerSamplePort;
import se.sics.gvod.croupier.events.CroupierJoin;
import se.sics.gvod.croupier.events.CroupierJoinCompleted;
import se.sics.gvod.nat.traversal.NatTraverserPort;
import se.sics.gvod.net.VodNetwork;
import se.sics.gvod.system.vod.snapshot.Snapshot;
import se.sics.gvod.system.vod.Vod;
import se.sics.gvod.system.vod.VodInit;
import se.sics.gvod.system.vod.VodPort;
import se.sics.gvod.system.main.GMain;
import se.sics.gvod.system.peer.events.ChangeUtility;
import se.sics.gvod.system.storage.Storage;
import se.sics.gvod.system.vod.VodConfiguration;
import se.sics.kompics.Fault;
import se.sics.kompics.Stop;

/**
 * This component mostly just does bootstrapping and monitoring. It also
 * encapsulates the main protocols: croupier, gradient, and Vod components.
 *
 * @author gautier, jim
 */
public final class VodPeer extends ComponentDefinition {

    Positive<VodNetwork> network = positive(VodNetwork.class);
    Positive<NatTraverserPort> natTraverserPort = positive(NatTraverserPort.class);
    Positive<Timer> timer = positive(Timer.class);
    Negative<Web> web = negative(Web.class);
    Negative<VodPeerPort> vodPeer = negative(VodPeerPort.class);
    Positive<BootstrapPort> bootstrap = positive(BootstrapPort.class);
    private Component croupier, vod; // webapp, monitor
    private Self self;
    private Logger logger = LoggerFactory.getLogger(VodPeer.class);
    private BootstrapConfiguration bootstrapConfiguration;
    private VodMonitorConfiguration monitorConfiguration;
    private boolean bootstrapped = false;
    private short mtu;
    private int utility;
    private int asn;
    private boolean seed;
    private long UBW, DBW;
    private Snapshot snapshot;
    private boolean freeRider;
    private boolean simulation, simulationBW;
    private VodConfiguration vodConfiguration;
    private String torrentFileAddress;
    private GMain main;
    private boolean play;
    private int numPieces;
    private Storage storage;

    public VodPeer() {
        croupier = create(Croupier.class);
        vod = create(Vod.class);


        subscribe(handleInit, control);
        subscribe(handleStop, control);
        subscribe(handleChangeUtility, vodPeer);
        subscribe(handleBootstrapResponse, bootstrap);
        subscribe(handleAddOverlayResponse, bootstrap);
        subscribe(handleRebootstrap, vod.getPositive(VodPort.class));
        subscribe(handleCroupierJoinCompleted, croupier.getPositive(CroupierPort.class));

        subscribe(handleFault, vod.getControl());
        subscribe(handleCroupierFault, croupier.getControl());
        
        subscribe(handleDataMsgRequest, network);

        
        //        webapp = create(VodWebApplication.class);
//        monitor = create(VodMonitorClient.class);
//        connect(network, monitor.getNegative(VodNetwork.class));
//        connect(timer, monitor.getNegative(Timer.class));
//        connect(vod.getPositive(Status.class), monitor.getNegative(Status.class));
//        connect(web, webapp.getPositive(Web.class));
//        connect(vod.getPositive(Status.class), webapp.getNegative(Status.class));

//        subscribe(handleJoin, gvodPeer);
//        subscribe(handleQuit, gvodPeer);
//        subscribe(handleJumpForward, gvodPeer);
//        subscribe(handleJumpBackward, gvodPeer);
//        subscribe(handlePlay, gvodPeer);
//        subscribe(handlePause, gvodPeer);
//        subscribe(handleDataFileInfo, gvodPeer);
//        subscribe(handleQuitCompleted, vod.getPositive(VodPeerPort.class));
//        subscribe(handleChangeBootstrapUtility, gvod.getPositive(VodPeerSampling.class));
//        subscribe(handleDownloadingCompleted, vod.getPositive(VodPeerPort.class));
//        subscribe(handleReadingCompleted, vod.getPositive(VodPeerPort.class));
//        subscribe(handleReportDownloadSpeed, vod.getPositive(VodPeerPort.class));
    }
    Handler<VodPeerInit> handleInit = new Handler<VodPeerInit>() {
        @Override
        public void handle(VodPeerInit init) {
            logger.info("Initiailzing VodPeer({})", init.getSelf().getOverlayId());
            main = init.getMain();
            self = init.getSelf();
            simulation = init.isSimulation();
            simulationBW = init.isSimuBW();
            logger.trace("handle gvodPeerInit");
            bootstrapConfiguration = init.getBootstrapConfig();
            monitorConfiguration = init.getMonitorConfig();
            vodConfiguration = init.getVodConfig();
            mtu = init.getMtu();
            asn = init.getAsn();
            numPieces = 0;

        connect(network, vod.getNegative(VodNetwork.class));
        connect(timer, vod.getNegative(Timer.class));
        connect(network, croupier.getNegative(VodNetwork.class));
        connect(timer, croupier.getNegative(Timer.class));
        connect(vod.getPositive(VodPeerPort.class), vodPeer);
        connect(vod.getNegative(PeerSamplePort.class), croupier.getPositive(PeerSamplePort.class));
        connect(vod.getNegative(NatTraverserPort.class), natTraverserPort);
            
            
            
            if (init.getTorrentFileAddress() != null && !simulation) {
                torrentFileAddress = init.getTorrentFileAddress();
                MetaInfoExec metaInfo = null;
                File f = new File(torrentFileAddress);
                if (f.exists()) {
                    try {
                        FileInputStream in = new FileInputStream(f);
                        metaInfo = new MetaInfoExec(in, torrentFileAddress);
                    } catch (Exception e) {
                        throw new IllegalArgumentException("Problem reading the file: " + torrentFileAddress
                                + " - " + e.getMessage());
                    }
                    Address bootAddr = metaInfo.getBootstrapServerAddress();
                    if (bootAddr.getIp() != null) {
                        bootstrapConfiguration = new BootstrapConfiguration(
                                bootAddr.getIp().getHostAddress(),
                                bootAddr.getPort(),
                                bootAddr.getId(),
                                bootstrapConfiguration.getClientRetryPeriod(),
                                bootstrapConfiguration.getClientRetryCount(),
                                bootstrapConfiguration.getClientKeepAlivePeriod(),
                                bootstrapConfiguration.getClientWebPort());
                    }
                    Address msAddr = metaInfo.getMonitorAddress();
                    if (msAddr.getIp() != null) {
                        monitorConfiguration = new VodMonitorConfiguration(
                                metaInfo.getMonitorAddress(),
                                monitorConfiguration.getViewEvictAfter(),
                                monitorConfiguration.getClientUpdatePeriod(),
                                monitorConfiguration.getClientWebPort());
                    }
                    vodConfiguration.setReadingPeriod(metaInfo.getReadingPeriod());
                    numPieces = metaInfo.getNbPieces();
                }
            } else {
                torrentFileAddress = vodConfiguration.getTorrentFilename();
                if (torrentFileAddress != null && !simulation) {
                    MetaInfoExec metaInfo = null;
                    File f = new File(torrentFileAddress);
                    if (f.exists()) {
                        try {
                            FileInputStream in = new FileInputStream(f);
                            metaInfo = new MetaInfoExec(in, torrentFileAddress);
                        } catch (Exception e) {
                            throw new IllegalArgumentException("Problem reading the file: " + torrentFileAddress
                                    + " - " + e.getMessage());
                        }
                        Address bootAddr = metaInfo.getBootstrapServerAddress();
                        bootstrapConfiguration = new BootstrapConfiguration(
                                bootAddr.getIp().getHostAddress(),
                                bootAddr.getPort(),
                                bootAddr.getId(),
                                bootstrapConfiguration.getClientRetryPeriod(),
                                bootstrapConfiguration.getClientRetryCount(),
                                bootstrapConfiguration.getClientKeepAlivePeriod(),
                                bootstrapConfiguration.getClientWebPort());

                        monitorConfiguration = new VodMonitorConfiguration(
                                metaInfo.getMonitorAddress(),
                                monitorConfiguration.getViewEvictAfter(),
                                monitorConfiguration.getClientUpdatePeriod(),
                                monitorConfiguration.getClientWebPort());
                        vodConfiguration.setReadingPeriod(metaInfo.getReadingPeriod());
                        numPieces = metaInfo.getNbPieces();
                    } else {
                        throw new IllegalArgumentException("Could not find file: " + torrentFileAddress);
                    }
                }
            }

            logger.trace("trigger bootstrapClientInit");
            logger.trace("trigger SetsInit");
            logger.trace("trigger gvodMonitorClientInit");

            utility = init.getUtility();
            seed = init.isSeed();
            logger.trace("is seeder : {}", init.isSeed());
            DBW = init.getDownloadBw();
            UBW = init.getUploadBw();
            snapshot = init.getSnapshot();
            freeRider = init.isFreeRider();
            play = init.isPlay();
            storage = init.getStorage();
            BootstrapRequest request = new BootstrapRequest(self.getOverlayId(),
                    new UtilityVod(init.getUtility(), 0));
            logger.trace("trigger bootstraprequest");
            trigger(request, bootstrap);

            trigger(new CroupierInit(self.clone(self.getOverlayId()), init.getCroupierConfig()),
                    croupier.getControl());

            trigger(new VodInit(self.clone(self.getOverlayId()), vodConfiguration, monitorConfiguration, numPieces,
                    asn, init.isSimulation(), init.isSimuBW(), main, utility, seed,
                    DBW, UBW, freeRider, snapshot, torrentFileAddress, play, 
            storage), vod.getControl());

        }
    };
    Handler<Rebootstrap> handleRebootstrap = new Handler<Rebootstrap>() {
        @Override
        public void handle(Rebootstrap event) {
            BootstrapRequest request = new BootstrapRequest(event.getOverlay(), event.getUtility());
            trigger(request, bootstrap);
        }
    };
//    Handler<Quit> handleQuit = new Handler<Quit>() {
//
//        @Override
//        public void handle(Quit event) {
//            trigger(event, vod.getPositive(VodPeerPort.class));
//        }
//    };
    Handler<ChangeUtility> handleChangeUtility = new Handler<ChangeUtility>() {
        @Override
        public void handle(ChangeUtility event) {
            trigger(event, vod.getPositive(VodPort.class));
        }
    };

    // This handler appears to be needed because, for some strange reason, DataMsg.Requests are not getting
    // forwarded to the Vod component
    Handler<DataMsg.Request> handleDataMsgRequest = new Handler<DataMsg.Request>() {
        @Override
        public void handle(DataMsg.Request msg) {
            logger.info("DataMsg.Request: " + msg.getSubpieceOffset());
        }   
    };
    
    
//    Handler<JumpForward> handleJumpForward = new Handler<JumpForward>() {
//
//        @Override
//        public void handle(JumpForward event) {
//            trigger(event, vod.getPositive(VodPeerPort.class));
//        }
//    };
//    Handler<JumpBackward> handleJumpBackward = new Handler<JumpBackward>() {
//
//        @Override
//        public void handle(JumpBackward event) {
//            trigger(event, vod.getPositive(VodPeerPort.class));
//        }
//    };
//    Handler<QuitCompleted> handleQuitCompleted = new Handler<QuitCompleted>() {
//
//        @Override
//        public void handle(QuitCompleted event) {
//            trigger(event, gvodPeer);
//        }
//    };
//    Handler<ReadingCompleted> handleReadingCompleted = new Handler<ReadingCompleted>() {
//
//        @Override
//        public void handle(ReadingCompleted event) {
//            trigger(event, gvodPeer);
//        }
//    };
//    Handler<DownloadCompletedSim> handleDownloadingCompleted = new Handler<DownloadCompletedSim>() {
//
//        @Override
//        public void handle(DownloadCompletedSim event) {
//            trigger(event, gvodPeer);
//        }
//    };
//    Handler<Play> handlePlay = new Handler<Play>() {
//
//        @Override
//        public void handle(Play event) {
//            trigger(event, vod.getPositive(VodPort.class));
//        }
//    };
//    Handler<Pause> handlePause = new Handler<Pause>() {
//
//        @Override
//        public void handle(Pause event) {
//            trigger(event, vod.getPositive(VodPort.class));
//        }
//    };
//    Handler<ReportDownloadSpeed> handleReportDownloadSpeed = new Handler<ReportDownloadSpeed>() {
//
//        @Override
//        public void handle(ReportDownloadSpeed event) {
//
//            trigger(event, gvodPeer);
//        }
//    };
    Handler<CroupierJoinCompleted> handleCroupierJoinCompleted = new Handler<CroupierJoinCompleted>() {
        @Override
        public void handle(CroupierJoinCompleted event) {
            //TODO jim: maybe it needs to notify SwingMain to include this peer inside the Bootstrap Heartbeats
        }
    };
    Handler<BootstrapResponse> handleBootstrapResponse = new Handler<BootstrapResponse>() {
        @Override
        public void handle(BootstrapResponse event) {
            assert (event.getOverlayId() == self.getId());

            if (!bootstrapped) {
                logger.info("Got BoostrapResponse from overlay {}. Bootstrap complete with {} peers.", event.getOverlayId(), 
                        event.getPeers().size());
                logger.info("Am seeder2 : {}", seed);
                trigger(new CroupierJoin(event.getPeers()), croupier.getPositive(CroupierPort.class));
                bootstrapped = true;
            } else {
                logger.info("Got ReBoostrapResponse from {}", event.getOverlayId());
                trigger(new RebootstrapResponse(self.getId(), event.getPeers()), croupier.getPositive(CroupierPort.class));
            }
        }
    };
    Handler<AddOverlayResponse> handleAddOverlayResponse = new Handler<AddOverlayResponse>() {
        @Override
        public void handle(AddOverlayResponse event) {
            logger.info(self.getAddress() + " adding overlay "
                    + event.getOverlayId() + " result: " + event.isSucceeded());
        }
    };
//    Handler<DataFileInfos> handleDataFileInfo = new Handler<DataFileInfos>() {
//        @Override
//        public void handle(DataFileInfos event) {
//            logger.trace("DataFileInfo received");
//            torrentFileAddress = event.getPath();
//            boolean dataFileExisted = false;
//            MetaInfoExec metaInfo = null;
//            try {
//                File f = new File(event.getPath());
//                if (f.exists()) {
//                    dataFileExisted = true;
//                }
//                FileInputStream in = new FileInputStream(f);
//                metaInfo = new MetaInfoExec(in, torrentFileAddress);
//            } catch (Exception e) {
//                e.printStackTrace();
//
//                // TODO send msg back to GUI saying there was a problem opening this torrent file.
//                return;
//            }
//            bootstrapConfiguration = new BootstrapConfiguration(metaInfo.getBootstrapServerAddress(),
//                    bootstrapConfiguration.getClientRetryPeriod(),
//                    bootstrapConfiguration.getClientRetryCount(),
//                    bootstrapConfiguration.getClientKeepAlivePeriod(),
//                    bootstrapConfiguration.getClientWebPort());
//            monitorConfiguration = new VodMonitorConfiguration(
//                    metaInfo.getMonitorAddress(),
//                    monitorConfiguration.getViewEvictAfter(),
//                    monitorConfiguration.getClientUpdatePeriod(),
//                    monitorConfiguration.getClientWebPort());
//            gvodConfiguration.setReadingPeriod(metaInfo.getReadingPeriod());
//            trigger(new VodInit(self, gvodConfiguration, monitorConfiguration,
//                    metaInfo.getNbPieces(), asn, simulation, simuBW, dataFileExisted),
//                    vod.getControl());
////            trigger(new VodWebApplicationInit(self.getAddress(),
////                    monitorConfiguration.getMonitorServerAddress(),
////                    metaInfo.getBootstrapServerAddress(),
////                    monitorConfiguration.getClientWebPort(), utility), webapp.getControl());
//
//            BootstrapRequest request = new BootstrapRequest(self.getOverlayId(), new UtilityVod(0, 0));
//            trigger(request, bootstrap);
//            logger.trace("trigger bootstraprequest");
//        }
//    };
    Handler<Fault> handleFault = new Handler<Fault>() {
        @Override
        public void handle(Fault event) {
            
            logger.warn(self.getId() + "/" + self.getOverlayId() 
                    + " - Fault in Vod component: " + event.getFault().getMessage());
            if (event.getFault() != null) {
                Throwable t = event.getFault();
                t.printStackTrace();
                StringBuffer sb = new StringBuffer();
                for (StackTraceElement ste : t.getStackTrace()) {
                    sb.append(ste.getClassName()+":" + ste.getLineNumber() + " ");
                }
                logger.error(sb.toString());
            }
            unsubscribe(handleRebootstrap, vod.getPositive(VodPort.class));
            disconnect(network, vod.getNegative(VodNetwork.class));
            disconnect(timer, vod.getNegative(Timer.class));
            disconnect(vod.getPositive(VodPeerPort.class), vodPeer);
            
            destroy(vod);

            vod = create(Vod.class);
            connect(network, vod.getNegative(VodNetwork.class));
            connect(timer, vod.getNegative(Timer.class));
            subscribe(handleRebootstrap, vod.getPositive(VodPort.class));

            trigger(new VodInit(self.clone(self.getOverlayId()), 
                    vodConfiguration, monitorConfiguration, numPieces,
                    asn, simulation, simulationBW, main, utility, seed,
                    DBW, UBW, freeRider, snapshot, torrentFileAddress, play, 
            storage), vod.getControl());
        }
    };
    
    Handler<Fault> handleCroupierFault = new Handler<Fault>() {
        @Override
        public void handle(Fault event) {
            
            logger.warn(self.getId() + "/" + self.getOverlayId() 
                    + " - Fault in Croupier component: " + event.getFault().getMessage());
        }
    };
    
    Handler<Stop> handleStop = new Handler<Stop>() {
        @Override
        public void handle(Stop event) {
            // TODO - cleanup connections.
            // disconnect from Bootstrap server
            // Stop monitoring client
            // destroy bootstrap client
//                unsubscribe(handleQuitCompleted, peer.getPositive(VodPeerPort.class));
            disconnect(timer, vod.getNegative(Timer.class));
            disconnect(timer, croupier.getNegative(Timer.class));
            disconnect(network, vod.getNegative(VodNetwork.class));
            disconnect(network, croupier.getNegative(VodNetwork.class));
            disconnect(vodPeer, vod.getPositive(VodPeerPort.class));
            destroy(vod);
            destroy(croupier);
        }
    };
}
