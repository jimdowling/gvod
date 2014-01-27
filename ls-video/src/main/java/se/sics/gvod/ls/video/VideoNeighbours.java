package se.sics.gvod.ls.video;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import se.sics.asdistances.ASDistances;
import se.sics.gvod.common.RetryComponentDelegator;
import se.sics.gvod.common.Self;
import se.sics.gvod.common.VodDescriptor;
import se.sics.gvod.croupier.events.CroupierSample;
import se.sics.gvod.ls.interas.events.InterAsSample;
import se.sics.gvod.ls.system.LSConfig;
import se.sics.gvod.ls.video.snapshot.VideoStats;
import se.sics.gvod.ls.video.util.VideoComparator;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.net.VodNetwork;
import se.sics.gvod.video.msgs.VideoConnectionMsg;
import se.sics.kompics.Positive;

/**
 * Holds
 *
 * @author Niklas Wahl&#233;n <nwahlen@kth.se>
 */
public class VideoNeighbours {

    // Video references
    private RetryComponentDelegator delegator;
    private Self self;
    private Positive<VodNetwork> network;
    // Collections
    private List<VodDescriptor> croupierSample;
    private List<VodDescriptor> interAsSample;
    private List<VodAddress> randomNeighbours;
    private List<VodAddress> closeNeighbours;
    // Tools
    private VideoComparator comparator;
    private Random random;
    // Inter-AS balancing -- if set to true a connection request to a
    // random peer is sent the next round
    private boolean incRandomIn;
    // Connection configuration and statistics
    private int maxOutConnectionsClose, maxOutConnectionsRandom;
    private int ingoingClose, ingoingRandom;
    private boolean source;
    // Connection timeouts, only necessary for outgoing
    private Map<VodAddress, Long> outgoingCloseTimeouts;
    private Map<VodAddress, Long> outgoingRandomTimeouts;

    public VideoNeighbours(RetryComponentDelegator delegator, Self self, Positive<VodNetwork> network, boolean source) {
        // Video references
        this.delegator = delegator;
        this.self = self;
        this.network = network;
        // Collections
        croupierSample = new ArrayList<VodDescriptor>();
        interAsSample = new ArrayList<VodDescriptor>();
        closeNeighbours = new ArrayList<VodAddress>();
        randomNeighbours = new ArrayList<VodAddress>();
        // Tools
        random = new Random(LSConfig.getSeed());
        comparator = new VideoComparator(self);
        // Inter-AS balancing -- if set to true a connection request
        // to a random peer is sent the next round
        incRandomIn = false;
        // Connection configuration and statistics
        maxOutConnectionsClose = LSConfig.VIDEO_MAX_OUT_CLOSE;
        maxOutConnectionsRandom = source ? LSConfig.VIDEO_SOURCE_MAX_OUT_RANDOM : LSConfig.VIDEO_MAX_OUT_RANDOM;
        ingoingClose = 0;
        ingoingRandom = 0;
        this.source = source;
        // Connection timeouts, only necessary for outgoing
        outgoingCloseTimeouts = new HashMap<VodAddress, Long>();
        outgoingRandomTimeouts = new HashMap<VodAddress, Long>();
    }

    public void cycle() {
        /*
         * A connection request is sent each turn. Depending on the current
         * stream quality it is either sent to a close or a random peer.
         */
        if (!source) {
            if (incRandomIn) {
                sendRandomConnectionRequests(1);
                incRandomIn = false;
            } else {
                sendInterAsConnectionRequests(1);
                // if the peer can't get enough InterAS connections, a random request should also be sent
                // (what this does is, if the peer do not send random requests by itself,
                //  even though it should, we will do so for it, however InterAS requests have to
                //  continue being sent, otherwise the peer will of course never get any)
                if (getIngoingInterAs() < 5) {
                    sendRandomConnectionRequests(1);
                }
            }

        }

        // Handle outgoing timeouts
        Long currentTime = System.currentTimeMillis();
        for (int i = 0; i < closeNeighbours.size(); i++) {
            VodAddress a = closeNeighbours.get(i);
            Long ts = outgoingCloseTimeouts.get(a);
            if ((ts != null) && ((currentTime - ts) > LSConfig.VIDEO_CLOSE_NEIGHBOUR_TIMEOUT)) {
                closeNeighbours.remove(a);
                outgoingCloseTimeouts.remove(a);
                sendDisconnect(a, false);
            }
        }
        for (int i = 0; i < randomNeighbours.size(); i++) {
            VodAddress a = randomNeighbours.get(i);
            Long ts = outgoingRandomTimeouts.get(a);
            if ((ts != null) && ((currentTime - ts) > LSConfig.VIDEO_RANDOM_NEIGHBOUR_TIMEOUT)) {
                randomNeighbours.remove(a);
                outgoingRandomTimeouts.remove(a);
                sendDisconnect(a, true);
            }
        }
        VideoStats.instance(self).setIngoingConnectionsClose(ingoingClose);
        VideoStats.instance(self).setIngoingConnectionsRandom(ingoingRandom);
        VideoStats.instance(self).setOutgoingConnectionsClose(closeNeighbours.size());
        VideoStats.instance(self).setOutgoingConnectionsRandom(randomNeighbours.size());
        reportAvgAsHops();
    }

    /*
     * Sample operations
     */
    public void handleInterAsSample(InterAsSample sample) {
        this.interAsSample = sample.getDescriptors();
    }

    public void handleCroupierSample(CroupierSample sample) {
        this.croupierSample = sample.getNodes();
    }

    /*
     * Connection operations
     */
    public void sendConnectionRequest(VodAddress destination, boolean randomRequest) {
        VideoConnectionMsg.Request request =
                new VideoConnectionMsg.Request(self.getAddress(), destination, randomRequest);
        delegator.doRetry(request, self.getOverlayId());
        if (randomRequest) {
            VideoStats.instance(self).incConnectionRequestsSentRandom();
        } else {
            VideoStats.instance(self).incConnectionRequestsSentClose();
        }
    }

    public void sendInterAsConnectionRequests(int n) {
        Collections.shuffle(interAsSample, random);
        for (int i = 0; i < n && i < interAsSample.size(); i++) {
            VodDescriptor peer = interAsSample.get(i);
            if (peer.getVodAddress().getId() != self.getId()) {
                if (!contains(peer.getVodAddress())) {
                    sendConnectionRequest(peer.getVodAddress(), false);
                }
            }
        }
    }

    public void sendRandomConnectionRequests(int n) {
        Collections.shuffle(croupierSample, random);
        for (int i = 0; i < n && i < croupierSample.size(); i++) {
            VodDescriptor peer = croupierSample.get(i);
            if (peer.getVodAddress().getId() != self.getId()) {
                if (!contains(peer.getVodAddress())) {
                    sendConnectionRequest(peer.getVodAddress(), true);
                }
            }
        }
    }

    public void handleConnectionRequestTimeout(VideoConnectionMsg.RequestTimeout timeout) {
        if (timeout.getRequestMsg().isRandomRequest()) {
            VideoStats.instance(self).incConnectionRequestTimeoutsRandom();
        } else {
            VideoStats.instance(self).incConnectionRequestTimeoutsClose();
        }
        // TODO: implement connection timeout handler
    }

    public void handleConnectionRequest(VideoConnectionMsg.Request request) {
        if (request.isRandomRequest()) {
            handleRandomConnectionRequest(request);
        } else {
            if (request.getVodSource().getId() != self.getId()) {
                if (updateClose(request.getVodSource())) {
                    VideoConnectionMsg.Response response = new VideoConnectionMsg.Response(request, true);
                    delegator.doTrigger(response, network);
                    outgoingCloseTimeouts.put(request.getVodSource(), System.currentTimeMillis());
                }
            }
        }
    }

    public void handleRandomConnectionRequest(VideoConnectionMsg.Request request) {
        if (updateRandom(request.getVodSource())) {
            VideoConnectionMsg.Response response = new VideoConnectionMsg.Response(
                    request, true);
            delegator.doTrigger(response, network);
            outgoingRandomTimeouts.put(request.getVodSource(), System.currentTimeMillis());
        }
    }

    public void handleConnectionResponse(VideoConnectionMsg.Response response) {
        if (response.getVodSource().getId() != self.getId() && response.connectionAccepted()) {
            if (response.wasRandomRequest()) {
                VideoStats.instance(self).incConnectionResponsesReceivedRandom();
                ingoingRandom++;
            } else {
                VideoStats.instance(self).incConnectionResponsesReceivedClose();
                ingoingClose++;
            }
        }
    }

    public void sendDisconnect(VodAddress destination, boolean randomConnection) {
        delegator.doTrigger(new VideoConnectionMsg.Disconnect(self.getAddress(),
                destination, randomConnection), network);
        if (randomConnection) {
            VideoStats.instance(self).incDisconnectsSentRandom();
        } else {
            VideoStats.instance(self).incDisconnectsSentClose();
        }
    }

    public void handleDisconnetion(VideoConnectionMsg.Disconnect disconnect) {
        if (disconnect.wasRandomConnection()) {
            VideoStats.instance(self).incDisconnectsReceivedRandom();
            ingoingRandom--;
        } else {
            VideoStats.instance(self).incDisconnectsReceivedClose();
            ingoingClose--;
        }
    }


    /*
     * Collection operations
     */
    public int closeNeighboursSize() {
        return closeNeighbours.size();
    }

    public int randomNeighboursSize() {
        return randomNeighbours.size();
    }

    public int totalSize() {
        return closeNeighbours.size() + randomNeighbours.size();
    }

    public boolean isEmpty() {
        return totalSize() == 0;
    }

    /**
     * Evaluates whether to add a peer as a close neighbour or not.
     *
     * @param possibleNeighbour
     * @return Returns true if the peer was added as a neighbour.
     */
    public boolean updateClose(VodAddress possibleNeighbour) {
        boolean added = false;
        if (possibleNeighbour.getId() == self.getId()) {
            return added;
        }
        if (closeNeighboursSize() >= maxOutConnectionsClose) {
            Collections.sort(closeNeighbours, comparator);
            // If the ratio of random neighbours has been changed several
            // neighbours may have to be removed
            while (closeNeighboursSize() - 1 >= maxOutConnectionsClose) {
                VodAddress removed = closeNeighbours.remove(closeNeighbours.size() - 1);
                sendDisconnect(removed, false);
            }
            int worstIndex = closeNeighbours.size() - 1;
            VodAddress worst = closeNeighbours.get(worstIndex);
            if (comparator.compare(worst, possibleNeighbour) < 1) {
                VodAddress removed = closeNeighbours.remove(worstIndex);
                outgoingCloseTimeouts.remove(removed);
                sendDisconnect(removed, false);
                added = closeNeighbours.add(possibleNeighbour);
            }
        } else {
            added = closeNeighbours.add(possibleNeighbour);
        }
        return added;
    }

    public boolean updateClose(Collection<VodDescriptor> possibleNeighbours) {
        boolean changed = false;
        for (VodDescriptor d : possibleNeighbours) {
            if (updateClose(d.getVodAddress())) {
                changed = true;
            }
        }
        return changed;
    }

    /**
     * Adds a random peer as a neighbour.
     *
     * @param newRandomNeighbour
     * @return
     */
    public boolean updateRandom(VodAddress newRandomNeighbour) {
        if (maxOutConnectionsRandom == 0) {
            return false;
        }
        while (randomNeighbours.size() >= (maxOutConnectionsRandom)
                && randomNeighbours.size() > 0) {
            VodAddress removed = randomNeighbours.remove(random.nextInt(randomNeighbours.size()));
            outgoingRandomTimeouts.remove(removed);
            sendDisconnect(removed, true);
        }
        return randomNeighbours.add(newRandomNeighbour);
    }

    public boolean contains(VodAddress a) {
        return (containsClose(a) || containsRandom(a));
    }

    public boolean containsClose(VodAddress a) {
        for (VodAddress a1 : closeNeighbours) {
            if (a1.getId() == a.getId()) {
                return true;
            }
        }
        return false;
    }

    public boolean containsRandom(VodAddress a) {
        for (VodAddress a1 : randomNeighbours) {
            if (a1.getId() == a.getId()) {
                return true;
            }
        }
        return false;
    }

    public boolean removeClose(VodAddress d) {
        return closeNeighbours.remove(d);
    }

    public boolean removeRandom(VodAddress d) {
        return randomNeighbours.remove(d);
    }

    public Collection<VodAddress> getNRandomNeighbours(int n) {
        List<VodAddress> rNeighbours = new ArrayList<VodAddress>();
        if (n >= totalSize()) {
            rNeighbours.addAll(closeNeighbours);
            rNeighbours.addAll(randomNeighbours);
            return rNeighbours;
        }
        rNeighbours.addAll(closeNeighbours);
        rNeighbours.addAll(randomNeighbours);
        Collections.shuffle(rNeighbours, random);
        rNeighbours = rNeighbours.subList(0, n);
        return rNeighbours;
    }

    public Collection<VodAddress> getNRandomCloseNeighbours(int n) {
        if (n >= closeNeighbours.size()) {
            return closeNeighbours;
        }
        Collections.shuffle(closeNeighbours, random);
        return closeNeighbours.subList(0, n);
    }

    public Collection<VodAddress> getNRandomRandomNeighbours(int n) {
        if (n >= randomNeighbours.size()) {
            return randomNeighbours;
        }
        Collections.shuffle(randomNeighbours, random);
        return randomNeighbours.subList(0, n);
    }

    public void incRandom(boolean inc) {
        incRandomIn = inc;
    }

    private double getRandomRatio() {
        return (double) ((double) randomNeighboursSize() / (randomNeighboursSize() + closeNeighboursSize()));
    }

    public int getIngoingConnections() {
        return ingoingClose + ingoingRandom;
    }

    public int getIngoingInterAs() {
        return ingoingClose;
    }

    public void mildPanic() {
        sendInterAsConnectionRequests(1);
        sendRandomConnectionRequests(1);
    }

    /*
     * Statistics operations
     */
    public void incAvailablePieces(VodAddress a) {
        comparator.incAvailablePieces(a);
    }

    public void updateLastResponse(VodAddress a) {
        comparator.updateLastSeen(a);
    }

    public void incTimeouts(VodAddress a) {
        comparator.incTimeouts(a);
    }

    public VodAddress getWorstNeighbour() {
        if (closeNeighbours.isEmpty()) {
            return null;
        }
        Collections.sort(closeNeighbours, comparator);
        return closeNeighbours.get(closeNeighbours.size() - 1);
    }

    public int compare(VodAddress a1, VodAddress a2) {
        return comparator.compare(a1, a2);
    }

    public short getDistanceTo(VodAddress a) {
        return comparator.getDistanceTo(a);
    }

    public void updateTimstamp(VodAddress a) {
        // Only updates timestamps for existing, i.e. not adding any new
        long l = System.currentTimeMillis();
        // put() should replace but doesn't seem to always
        if (outgoingCloseTimeouts.containsKey(a)) {
            outgoingCloseTimeouts.remove(a);
            outgoingCloseTimeouts.put(a, l);
        }
        // Don't update random forever, we want to disconnect them sometime to lower inter-AS traffic
        // Could set a counter for how many times they are allowed to be updated, or just 
        // set a higher total timeout and to no updates of the timeout
//        if (outgoingRandomTimeouts.containsKey(a)) {
//            outgoingRandomTimeouts.remove(a);
//            outgoingRandomTimeouts.put(a, l);
//        }
    }

    private void reportAvgAsHops() {
        Collection<VodAddress> allNeighbours = new ArrayList<VodAddress>();
        allNeighbours.addAll(closeNeighbours);
        allNeighbours.addAll(randomNeighbours);
        if (allNeighbours.isEmpty()) {
            VideoStats.instance(self).setAvgAsHopsToNeighbours(0);
        } else {
            int sumAsHops = 0;
            for (VodAddress d : allNeighbours) {
                ASDistances distances = ASDistances.getInstance();
                sumAsHops += distances.getDistance(self.getAddress().getIp().getHostAddress(), d.getIp().getHostAddress());
            }
            VideoStats.instance(self).setAvgAsHopsToNeighbours((float) ((float) sumAsHops) / ((float) allNeighbours.size()));
        }
    }
}
