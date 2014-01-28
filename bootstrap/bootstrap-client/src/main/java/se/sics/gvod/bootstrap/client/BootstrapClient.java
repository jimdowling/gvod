package se.sics.gvod.bootstrap.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.gvod.bootstrap.msgs.BootstrapMsg;
import se.sics.gvod.bootstrap.port.AddOverlayRequest;
import se.sics.gvod.bootstrap.port.AddOverlayResponse;

import se.sics.kompics.Handler;
import se.sics.kompics.Negative;
import se.sics.kompics.Stop;
import se.sics.gvod.bootstrap.port.BootstrapPort;
import se.sics.gvod.bootstrap.port.BootstrapHeartbeat;
import se.sics.gvod.bootstrap.port.BootstrapHelperHb;
import se.sics.gvod.bootstrap.port.BootstrapRequest;
import se.sics.gvod.bootstrap.port.BootstrapResponse;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.common.VodDescriptor;
import se.sics.gvod.common.RetryComponentDelegator;
import se.sics.gvod.common.Self;
import se.sics.gvod.common.util.ToVodAddr;
import se.sics.gvod.config.VodConfig;
import se.sics.gvod.nat.common.MsgRetryComponent;
import se.sics.gvod.net.msgs.ScheduleRetryTimeout;
import se.sics.gvod.timer.TimeoutId;
import se.sics.kompics.Kompics;

/**
 * The <code>BootstrapClient</code> class.
 *
 * TODO - SwingMain must send BootstrapHeartbeat periodically
 * to refresh client at server.
 * 
 */
public class BootstrapClient extends MsgRetryComponent {

    Negative<BootstrapPort> bootstrap = negative(BootstrapPort.class);
    private Logger logger;
    private VodAddress server;
    private Self self;
    private long retryPeriod;
    private int retriesCount;
    private String compName;
    
    public BootstrapClient() {
        this(null);
    }
    public BootstrapClient(RetryComponentDelegator delegator) {
        super(delegator);
        this.delegator.doAutoSubscribe();
    }

    Handler<BootstrapClientInit> handleInit = new Handler<BootstrapClientInit>() {

        @Override
        public void handle(BootstrapClientInit init) {
            
            retryPeriod = init.getBootstrapConfiguration().getClientRetryPeriod();
            retriesCount = init.getBootstrapConfiguration().getClientRetryCount();
            server = ToVodAddr.systemAddr(
                    init.getBootstrapConfiguration().getBootstrapServerAddress());
            self = init.getSelf();
            
            logger = LoggerFactory.getLogger(BootstrapClient.class.getName() + "@" + self.getId());
            compName = BootstrapClient.class.getSimpleName() + "(" + self.getId() + ") ";
            logger.debug(compName + "BoostrapServer addr: " + server.getPeerAddress());
        }
    };
    Handler<BootstrapRequest> handleBootstrapRequest = new Handler<BootstrapRequest>() {

        @Override
        public void handle(BootstrapRequest event) {

            // set an alarm to retry the request if no response
            ScheduleRetryTimeout st =
                    new ScheduleRetryTimeout(retryPeriod, retriesCount);
            BootstrapMsg.GetPeersRequest request =
                    new BootstrapMsg.GetPeersRequest(
                    self.getAddress(),
                    server,
                    event.getOverlay(),
                    event.getUtility());
            BootstrapTimeout retryRequest = new BootstrapTimeout(st, request);
            TimeoutId id = delegator.doRetry(retryRequest, event);
            logger.debug(compName + " boostrap request to " + server.getPeerAddress() + 
                    " with timeoutId " + id);
        }
    };
    Handler<BootstrapTimeout> handleBootstrapTimeout = new Handler<BootstrapTimeout>() {

        @Override
        public void handle(BootstrapTimeout event) {
            logger.debug(compName + " boostrap timeout: " + event.getTimeoutId());

            BootstrapRequest req = (BootstrapRequest) delegator.doGetContext(event.getTimeoutId());
            if (delegator.doCancelRetry(event.getTimeoutId())) {
                if (req != null) {
                    BootstrapResponse response = new BootstrapResponse(
                            req, false,
                            req.getOverlay(),
                            new ArrayList<VodDescriptor>());
                    delegator.doTrigger(response, bootstrap);
                } else {
                    logger.error(compName + "Couldn't find BootstrapRequest object in BootstrapTimeout handler");
                    Kompics.shutdown();
                    System.exit(-1);
                }
            }
        }
    };
    Handler<BootstrapMsg.GetPeersResponse> handleGetPeersResponse =
            new Handler<BootstrapMsg.GetPeersResponse>() {

                @Override
                public void handle(BootstrapMsg.GetPeersResponse event) {
                    logger.debug(compName + "boostrapped: received {} peers. TimeoutId: " 
                            + event.getTimeoutId(), 
                            event.getPeers().size());
                    // overlay may change due to multiple client requests overwriting old state
                    BootstrapRequest req = (BootstrapRequest) delegator.doGetContext(event.getTimeoutId());
                    if (delegator.doCancelRetry(event.getTimeoutId())
                            && req != null) {
                        BootstrapResponse response = new BootstrapResponse(
                                req, true, req.getOverlay(),
                                event.getPeers());
                        delegator.doTrigger(response, bootstrap);
                    } else {
                        // request was retried, this could be a duplicate response
                        logger.warn("BootstrapMsg.GetPeersResponse: Couldn't find timoutId. Late response or duplicate response?");
                    }
                }
            };
    
    Handler<AddOverlayRequest> handleAddOverlayRequest =
            new Handler<AddOverlayRequest>() {

                @Override
                public void handle(AddOverlayRequest event) {
                    logger.debug("AddOverlayRequest from {} to {}", self.getAddress(),
                            server.getPeerAddress());

                    // byte[] data won't fit in a single UDP packet. Need to split
                    // up msg into several parts. Calculate length of data, then
                    // num of packets, then send packets one-by-one.
                    String name = event.getOverlayName();
                    int id = event.getOverlayId();
                    String desc = event.getDescription();
                    String imgUrl = event.getImageUrl();
                    byte[] data = event.getTorrentsData();
                    int firstMsgLen = name.length() + desc.length() + 100 + imgUrl.length();
                    int offsetFirst = 0;
                    int offsetSecond = VodConfig.DEFAULT_MTU - firstMsgLen;
                    byte[] firstData = Arrays.copyOfRange(data, offsetFirst, offsetSecond);
                    // Assume overhead in msgs subsequent to the first is 200 bytes, rest is data
                    int dataLen = VodConfig.DEFAULT_MTU - 200;
                    int numParts = ((data.length - firstData.length) / dataLen)
                            + 1 /* first packet contains some data */;
                    // Need to add one more to numParts if the last packet was not exactly
                    // equal to dataLen. 
                    numParts += ( (data.length - firstData.length) % dataLen ) > 0 ? 1 : 0;
                    
                    BootstrapMsg.AddOverlayReq addRequest = new BootstrapMsg.AddOverlayReq(
                            self.getAddress(), server,// event.getBootstrapServerAddr(), 
                            name, id, desc, firstData, imgUrl, 0, numParts);
                    ScheduleRetryTimeout srrt =
                            new ScheduleRetryTimeout(retryPeriod, 3, 1.5);
                    AddOverlayTimeout aot = new AddOverlayTimeout(srrt, addRequest);
                    delegator.doRetry(aot, event);
                    
                    for (int i=1; i<numParts; i++) {
                        offsetFirst = offsetSecond;
                        offsetSecond = (offsetSecond + dataLen) > data.length ? data.length :
                                (offsetSecond + dataLen);
                        byte[] nextData = Arrays.copyOfRange(data, offsetFirst, offsetSecond);
                        BootstrapMsg.AddOverlayReq msg = new BootstrapMsg.AddOverlayReq(
                            self.getAddress(), server,
                            name, id, "", nextData, "", i, numParts);
                        delegator.doRetry(msg, retryPeriod, 2, 1.5, self.getOverlayId());                        
                    }
                    
                    
                }
            };
    Handler<BootstrapMsg.AddOverlayResp> handleAddOverlayResponse =
            new Handler<BootstrapMsg.AddOverlayResp>() {

                @Override
                public void handle(BootstrapMsg.AddOverlayResp event) {
                    logger.debug("AddOverlayResponse from {} ", event.getVodSource());
                    Object obj = delegator.doGetContext(event.getTimeoutId());
                    if (obj == null) {
                        logger.trace("Couldn't find request object for AddOverlayResp");
                        // We may have already sent a response, so cancel the timeout for this request.
                        delegator.doCancelRetry(event.getTimeoutId());
                    } else {
                        if (delegator.doCancelRetry(event.getTimeoutId())) {
                            logger.warn("AddOverlayResponse received from " + event.getVodSource());                            
                            AddOverlayRequest req = (AddOverlayRequest) obj;
                            if (event.isSuccess()) {
                                AddOverlayResponse resp = new AddOverlayResponse(req, event.isSuccess(),
                                        event.getOverlayId());
                                delegator.doTrigger(resp, bootstrap);
                            }
                        } else {
                            logger.warn("AddOverlayResponse came too late. Could not remove timeoutId");
                        }
                    }
                }
            };
    Handler<AddOverlayTimeout> handleAddOverlayTimeout =
            new Handler<AddOverlayTimeout>() {

                @Override
                public void handle(AddOverlayTimeout event) {
                    Object obj = delegator.doGetContext(event.getTimeoutId());
                    if (obj == null) {
                        logger.error("Couldn't find request object for AddOverlayTimeout");
                        return;
                    }
                    AddOverlayRequest req = (AddOverlayRequest) obj;

                    if (delegator.doCancelRetry(event.getTimeoutId())) {
                        logger.info("Add overlay timed out for overlay-id: "  + event.getOverlay());
                        AddOverlayResponse response = new AddOverlayResponse(req, false, event.getOverlay());
                        delegator.doTrigger(response, bootstrap);
                    } else {
                        logger.error("Couldn't find bootstrap request or cancelTimeout");
                    }
                }
            };
    
    Handler<BootstrapHeartbeat> handleHeartbeat = new Handler<BootstrapHeartbeat>() {

        @Override
        public void handle(BootstrapHeartbeat event) {
            VodAddress src = (event.getDownloadingAddress() == null) ? self.getAddress()
                    : event.getDownloadingAddress();
            logger.debug("Sending BootstrapHeartbeat from : " + src);
            BootstrapMsg.Heartbeat request = new BootstrapMsg.Heartbeat(
                    src, server, event.isHelper(), event.getMtu(), 
                    event.getSeedingOverlays(), event.getDownloadingUtilities());
            delegator.doTrigger(request, network);

        }
    };
    
    Handler<BootstrapHelperHb> handleBootstrapHelperHb = new Handler<BootstrapHelperHb>() {

        @Override
        public void handle(BootstrapHelperHb event) {
            BootstrapMsg.HelperHeartbeat request = new BootstrapMsg.HelperHeartbeat(
                    self.getAddress(), server, event.isAvailable());
            delegator.doTrigger(request, network);

        }
    };
    
  
//    private Handler<ChangeBootstrapUtility> handleChangeBootstrapUtility = new Handler<ChangeBootstrapUtility>() {
//
//        public void handle(ChangeBootstrapUtility event) {
//            logger.debug("handle changeBootstrapUtility, new utility {}", event.getUtility());
//            PeerEntry peerEntry = new PeerEntry(event.getOverlayAddress(),
//                    event.getOverlay(), natType, event.getUtility().getChunk(), 0, 0);
//            Iterator<PeerEntry> it = overlays.iterator();
//            PeerEntry node = null;
//            boolean flag = false;
//            while (it.hasNext()) {
//                node = it.next();
//                if (node.equals(peerEntry)) {
//                    flag = true;
//                    break;
//                }
//            }
//            if (flag) {
//                overlays.remove(node);
//            }
//            overlays.add(peerEntry);
//        }
//    };

    @Override
    public void stop(Stop event) {
        // TODO 
    }
}
