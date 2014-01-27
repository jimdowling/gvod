package se.sics.gvod.ls.interas;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import se.sics.gvod.ls.interas.events.InterAsSetsExchangeCycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.asdistances.ASDistances;
import se.sics.gvod.common.RetryComponentDelegator;
import se.sics.gvod.common.Self;
import se.sics.gvod.common.VodDescriptor;
import se.sics.gvod.croupier.PeerSamplePort;
import se.sics.gvod.croupier.events.CroupierSample;
import se.sics.gvod.interas.msgs.InterAsGossipMsg;
import se.sics.gvod.ls.interas.events.InterAsSample;
import se.sics.gvod.ls.interas.snapshot.InterAsStats;
import se.sics.gvod.ls.system.LSConfig;
import se.sics.gvod.nat.common.MsgRetryComponent;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.net.VodAddress.NatType;
import se.sics.gvod.net.msgs.ScheduleRetryTimeout;
import se.sics.kompics.Handler;
import se.sics.kompics.Negative;
import se.sics.kompics.Stop;
import se.sics.gvod.timer.SchedulePeriodicTimeout;
import se.sics.gvod.timer.TimeoutId;
import se.sics.kompics.Positive;

/**
 *
 * @author jim, hooman, niklas
 */
public class InterAs extends MsgRetryComponent {
    public static final int SYSTEM_INTER_AS_OVERLAY_ID = 1000;

    private final Logger logger = LoggerFactory.getLogger(InterAs.class);
    private Negative<InterAsPort> interAs = negative(InterAsPort.class);
    private Positive<PeerSamplePort> croupier = positive(PeerSamplePort.class);
    private TimeoutId setsGossipingTimeoutId;
    private Self self;
    private long setsExchangeRto;
    private long setsExchangePeriod;
    private InterAsNeighbours neighbours;
    private Set<TimeoutId> receivedTimeoutIds;

    public InterAs() {
        this(null);
    }

    public InterAs(RetryComponentDelegator delegator) {
        super(delegator);
        this.delegator.doAutoSubscribe();
    }
    Handler<InterAsInit> handleInit = new Handler<InterAsInit>() {

        @Override
        public void handle(InterAsInit init) {
            self = init.getSelf();
            setsExchangeRto = init.getSetsExchangeRto();
            setsExchangePeriod = init.getSetsExchangePeriod();

            SchedulePeriodicTimeout periodicTimeout =
                    new SchedulePeriodicTimeout(0, setsExchangePeriod);
            periodicTimeout.setTimeoutEvent(new InterAsSetsExchangeCycle(periodicTimeout));
            setsGossipingTimeoutId = periodicTimeout.getTimeoutEvent().getTimeoutId();
            delegator.doTrigger(periodicTimeout, timer);

            neighbours = new InterAsNeighbours(self, LSConfig.INTER_AS_MAX_NEIGHBOURS, InterAsNeighbours.ARRAY_LIST);
            InterAsStats.addNode(self.getAddress());

            receivedTimeoutIds = new HashSet<TimeoutId>();
        }
    };
    Handler<InterAsSetsExchangeCycle> handleCycle = new Handler<InterAsSetsExchangeCycle>() {

        @Override
        public void handle(InterAsSetsExchangeCycle e) {

            if (!neighbours.isEmpty()) {
                // Find ~highest utility peer
                VodAddress dest = neighbours.getBestNeighbour().getVodAddress();
                if (dest.getId() == self.getId()) {
                    throw new IllegalArgumentException("Can't send Request to myself");
                } else {
                    // Initiate gossiping request
                    InterAsStats.instance(self).incSentRequests();

                    InterAsGossipMsg.Request request = new InterAsGossipMsg.Request(self.getAddress(), dest);
                    ScheduleRetryTimeout schedule =
                            new ScheduleRetryTimeout(setsExchangeRto, 1);
                    InterAsGossipMsg.RequestRetryTimeout requestTimeout =
                            new InterAsGossipMsg.RequestRetryTimeout(schedule, request);

                    delegator.doRetry(request, self.getOverlayId());

                    if (self.getAddress().getNatType().equals(NatType.NAT)) {
                        logger.trace("{}: Sending Request from "
                                + self.getAddress()
                                + " to "
                                + request.getDestination()
                                + " TimeoutId="
                                + request.getTimeoutId(), self.getId());
                    }
                }

                neighbours.decreasePenalites();

                // Send neighbour set to parent
                InterAsSample sample = new InterAsSample(new ArrayList(neighbours.getAsCollection()));
                delegator.doTrigger(sample, interAs);
            }
            InterAsStats.instance(self).incSelectedTimes();
        }
    };
    Handler<CroupierSample> handleCroupierSample = new Handler<CroupierSample>() {

        @Override
        public void handle(CroupierSample event) {
            neighbours.addAll(event.getNodes());
            calculateAvgAsHops();
        }
    };
    Handler<InterAsGossipMsg.Request> handleInterAsGossipMsgRequest = new Handler<InterAsGossipMsg.Request>() {

        @Override
        public void handle(InterAsGossipMsg.Request request) {
            if (request.getSource().getId() == self.getId()) {
                logger.warn("{} Received Request from myself", self.getId());
            }
            if (request.getVodSource().getNatType().equals(NatType.NAT)) {
                logger.trace("{}: Received Request from "
                        + request.getSource()
                        + " to "
                        + request.getDestination()
                        + " TimeoutId="
                        + request.getTimeoutId(), self.getId());
            }

            InterAsStats.instance(self).incReceivedRequests();


            InterAsGossipMsg.Response resp = new InterAsGossipMsg.Response(self.getAddress(),
                    request, new ArrayList<VodDescriptor>(neighbours.getAsCollection()));
            InterAsGossipMsg.Response response = new InterAsGossipMsg.Response(self.getAddress(), request.getVodSource(), request.getVodSource(), request.getTimeoutId(), new ArrayList(neighbours.getAsCollection()));

            if (response.getVodDestination().getNatType().equals(NatType.NAT)) {
                logger.trace("{}: Sending Response from "
                        + response.getSource()
                        + " to "
                        + response.getDestination()
                        + " nextDest "
                        + response.getNextDest()
                        + " TimeoutId="
                        + response.getTimeoutId(), self.getId());
            }

            delegator.doTrigger(response, network);
//            InterAsStats.instance(self).incSentResponses();
        }
    };
    Handler<InterAsGossipMsg.Response> handleInterAsGossipMsgResponse = new Handler<InterAsGossipMsg.Response>() {

        @Override
        public void handle(InterAsGossipMsg.Response response) {
            //if (cancelRetry(msg.getTimeoutId())) {
//            CancelTimeout ct = new CancelTimeout(response.getTimeoutId());
//            delegator.doTrigger(ct, timer);
            if (response.getVodSource().getId() == self.getId()) {
                logger.warn("{}: Received Response from myself. TimeoutId={}", self.getId(), response.getTimeoutId());
            }
            if (self.getAddress().getNatType().equals(NatType.NAT)) {
                logger.trace("{}: Received Response from "
                        + response.getSource()
                        + " to "
                        + response.getDestination()
                        + " TimeoutId="
                        + response.getTimeoutId(), self.getId());
            }
            delegator.doCancelRetry(response.getTimeoutId());

            neighbours.addAll(response.getInterAsNeighbours());
            InterAsStats.instance(self).incReceivedResponses();

            if (!receivedTimeoutIds.add(response.getTimeoutId())) {
                logger.warn("InterAsGossipMsg.Response: Received already received TimeoutId");
            }
            //}
        }
    };
    Handler<InterAsGossipMsg.RequestRetryTimeout> handleInterAsGossipMsgRequestRetryTimeout = new Handler<InterAsGossipMsg.RequestRetryTimeout>() {

        @Override
        public void handle(InterAsGossipMsg.RequestRetryTimeout rrt) {
            if (cancelRetry(rrt.getTimeoutId())) {
                InterAsStats.instance(self).incShuffleTimeout();
                neighbours.punish(rrt.getRequestMsg().getVodDestination().getId());
            }
        }
    };
    Handler<InterAsGossipMsg.RequestTimeout> handleInterAsGossipMsgRequestTimeout = new Handler<InterAsGossipMsg.RequestTimeout>() {

        @Override
        public void handle(InterAsGossipMsg.RequestTimeout rt) {
            if (receivedTimeoutIds.add(rt.getTimeoutId())) {
                logger.warn("{}: RequestTimeout (TimeoutId " + rt.getTimeoutId() + ")", self.getId());
                InterAsStats.instance(self).incShuffleTimeout();
            } else {
                logger.warn("InterAsGossipMsg.RequestTimeout: Received already received TimeoutId");
            }

        }
    };

    @Override
    public void stop(Stop stop) {
    }

    private void calculateAvgAsHops() {
        int sumAsHops = 0;
        for (VodDescriptor d : neighbours.getAsCollection()) {
            ASDistances distances = ASDistances.getInstance();
            sumAsHops += distances.getDistance(self.getAddress().getIp().getHostAddress(), d.getVodAddress().getIp().getHostAddress());
        }
        InterAsStats.instance(self).setAvgAsHops((double) ((double) sumAsHops) / ((double) neighbours.size()));
    }
}
