/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.simulator.vod.newp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.gvod.address.Address;
import se.sics.gvod.bootstrap.config.BootstrapConfig;
import se.sics.gvod.bootstrap.msgs.BootstrapMsg;
import se.sics.gvod.bootstrap.server.BootstrapServerInit;
import se.sics.gvod.bootstrap.server.BootstrapServerMysql;
import se.sics.gvod.bootstrap.server.CleanupStaleTimeout;
import se.sics.gvod.common.VodDescriptor;
import se.sics.gvod.common.msgs.NatReportMsg;
import se.sics.gvod.common.util.ToVodAddr;
import se.sics.gvod.config.VodConfig;
import se.sics.gvod.net.VodNetwork;
import se.sics.gvod.timer.Timer;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Negative;
import se.sics.kompics.Positive;
import se.sics.kompics.Stop;
import se.sics.kompics.web.Web;
import se.sics.kompics.web.WebRequest;

/**
 *
 * @author alex
 */
public class BootstrapServerStub extends ComponentDefinition {

    private static final Logger logger = LoggerFactory.getLogger(BootstrapServerStub.class);

    Positive<VodNetwork> network = positive(VodNetwork.class);
    Positive<Timer> timer = positive(Timer.class);
    Negative<Web> web = negative(Web.class);

    private Address self;
    private Random rand;
    private final Map<Integer, List<VodDescriptor>> vodPeers;

    public BootstrapServerStub() {
        this.vodPeers = new HashMap<Integer, List<VodDescriptor>>();

        subscribe(handleInit, control);

        subscribe(handleAddOverlayReq, network);
        subscribe(handleHeartbeat, network);
        subscribe(handleBootstrapMsgGetPeersRequest, network);
        subscribe(handleBootstrapHelperHeartbeat, network);
        subscribe(handleCheckForNewUploadsTimeout, timer);
        subscribe(handleCleanupStaleTimeout, timer);
        subscribe(handleWebRequest, web);
        subscribe(handleNatReportMsg, network);
        subscribe(handleStop, control);
    }

    private Handler<BootstrapServerInit> handleInit = new Handler<BootstrapServerInit>() {

        @Override
        public void handle(BootstrapServerInit event) {
            logger.info("BootstrapServer init");

            self = event.getConfiguration().getBootstrapServerAddress();
            rand = new Random(BootstrapConfig.getSeed());
        }
    };

    Handler<BootstrapMsg.HelperHeartbeat> handleBootstrapHelperHeartbeat = new Handler<BootstrapMsg.HelperHeartbeat>() {
        @Override
        public void handle(BootstrapMsg.HelperHeartbeat event) {
            throw new UnsupportedOperationException();
        }
    };

    Handler<BootstrapMsg.GetPeersRequest> handleBootstrapMsgGetPeersRequest = new Handler<BootstrapMsg.GetPeersRequest>() {
        @Override
        public void handle(BootstrapMsg.GetPeersRequest event) {
            logger.info("received GetPeersRequest from {} : overlay {} : id {}",
                    new Object[]{event.getSource(), event.getOverlay(), event.getTimeoutId()});

            long baseTime = System.currentTimeMillis();

            List<VodDescriptor> peers = null;
            int overlayId = event.getOverlay();
            // specific bootstrap to an overlay,
            if (overlayId != VodConfig.SYSTEM_OVERLAY_ID) {
                peers = queryOverlay(overlayId);
            } else { // specific bootstrap to find stun servers
                //simulation does not currently use this
                throw new UnsupportedOperationException();

//                peers = queryOpenNodes(event.getSource());
            }

            BootstrapMsg.GetPeersResponse response = new BootstrapMsg.GetPeersResponse(
                    ToVodAddr.systemAddr(self), event.getVodSource(), event.getTimeoutId(), overlayId, peers);

            logger.info("sending GetPeerRespose to {} : overlay {} : id {} : peers {}",
                    new Object[]{response.getDestination(), response.getOverlayId(), response.getTimeoutId(), response.getPeers().size()});
            trigger(response, network);
        }
    };

    private List<VodDescriptor> queryOverlay(int overlayId) {
        List<VodDescriptor> returnPeers = new ArrayList<VodDescriptor>();
        List<VodDescriptor> overlayPeers = vodPeers.get(overlayId);
        if (overlayPeers == null) {
            overlayPeers = new ArrayList<VodDescriptor>();
            vodPeers.put(overlayId, overlayPeers);
        }

        Set<Integer> peerIdxs = new TreeSet<Integer>();
        if (overlayPeers.size() > BootstrapConfig.DEFAULT_NUM_NODES_RETURNED) {
            while (peerIdxs.size() < BootstrapConfig.DEFAULT_NUM_NODES_RETURNED) {
                peerIdxs.add(rand.nextInt(overlayPeers.size()));
            }

            for (Integer peerIdx : peerIdxs) {
                returnPeers.add(overlayPeers.get(peerIdx));
            }
        } else {
            returnPeers.addAll(overlayPeers);
        }
        return returnPeers;
    }

    Handler<BootstrapMsg.Heartbeat> handleHeartbeat = new Handler<BootstrapMsg.Heartbeat>() {
        @Override
        public void handle(BootstrapMsg.Heartbeat event) {
            throw new UnsupportedOperationException();
        }
    };

    Handler<BootstrapMsg.AddOverlayReq> handleAddOverlayReq = new Handler<BootstrapMsg.AddOverlayReq>() {
        @Override
        public void handle(BootstrapMsg.AddOverlayReq event) {
            throw new UnsupportedOperationException();
        }
    };

    Handler<BootstrapServerMysql.CheckForNewUploadsTimeout> handleCheckForNewUploadsTimeout = new Handler<BootstrapServerMysql.CheckForNewUploadsTimeout>() {
        @Override
        public void handle(BootstrapServerMysql.CheckForNewUploadsTimeout event) {
            throw new UnsupportedOperationException();
        }
    };

    Handler<CleanupStaleTimeout> handleCleanupStaleTimeout = new Handler<CleanupStaleTimeout>() {
        @Override
        public void handle(CleanupStaleTimeout event) {
            throw new UnsupportedOperationException();
        }
    };

    Handler<WebRequest> handleWebRequest = new Handler<WebRequest>() {
        public void handle(WebRequest event) {
            throw new UnsupportedOperationException();
        }
    };

    Handler<NatReportMsg> handleNatReportMsg = new Handler<NatReportMsg>() {
        @Override
        public void handle(NatReportMsg msg) {
            throw new UnsupportedOperationException();
        }
    };

    public Handler<Stop> handleStop = new Handler<Stop>() {
        @Override
        public void handle(Stop event) {
            throw new UnsupportedOperationException();
        }
    };
}
