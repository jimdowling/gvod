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
package se.sics.gvod.system.vod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.gvod.bootstrap.port.RebootstrapResponse;
import se.sics.gvod.common.BitField;
import se.sics.gvod.common.CommunicationWindow;
import se.sics.gvod.common.Self;
import se.sics.gvod.common.UtilityVod;
import se.sics.gvod.common.VodDescriptor;
import se.sics.gvod.common.msgs.ConnectMsg;
import se.sics.gvod.common.msgs.DataMsg;
import se.sics.gvod.common.msgs.DataOfferMsg;
import se.sics.gvod.common.msgs.DisconnectMsg;
import se.sics.gvod.common.msgs.LeaveMsg;
import se.sics.gvod.config.VodConfig;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.system.peer.events.Quit;
import se.sics.gvod.system.peer.sets.BitTorrentSet;
import se.sics.gvod.system.peer.sets.DescriptorStore;
import se.sics.gvod.system.peer.sets.InitiateDataOffer;
import se.sics.gvod.system.peer.sets.LowerSet;
import se.sics.gvod.system.peer.sets.UpperSet;
import se.sics.gvod.system.storage.Stats;
import se.sics.gvod.system.vod.Vod.ConnectionDelegator;
import se.sics.gvod.timer.ScheduleTimeout;
import se.sics.gvod.timer.TimeoutId;

/**
 * @author Alex Ormenisan <aaor@sics.se>
 */
public class ConnectionManager {

    private final Logger logger = LoggerFactory.getLogger(Vod.class);
    private final String compName;

    private final ConnectionDelegator delegator;
    private final Self self;

    public final BitTorrentSet bitTorrentSet;
    public final UpperSet upperSet;
    public final LowerSet lowerSet;
    private final List<VodAddress> choked;
    private final List<VodAddress> chokedUnder;
    private final Random randGen;

    /**
     * Use croupier and gradient addresses to index into DescriptorStore
     */
    private List<VodDescriptor> gradientSet = new ArrayList<VodDescriptor>();
    private List<VodDescriptor> croupierSet;

    //make private later
    public Map<Integer, VodDescriptor> fingers;
    public final DescriptorStore store;
    public int commWinSize;
    public int maxWindowSize;
    
    public Map<TimeoutId, Integer> outstandingAck;

    public ConnectionManager(VodConfiguration config, ConnectionDelegator delegator, Self self,
            String compName) {
        this.delegator = delegator;
        this.store = new DescriptorStore(config.getSeed());
        this.self = self;
        bitTorrentSet = new BitTorrentSet(self, store, config.getBitTorrentSetSize(), config.getSeed());
        upperSet = new UpperSet(self, store, config.getUpperSetSize(), config.getSeed());
        lowerSet = new LowerSet(self, store, config.getLowerSetSize(), config.getSeed());
        choked = new ArrayList<VodAddress>();
        chokedUnder = new ArrayList<VodAddress>();
        randGen = new Random(config.getSeed());
        this.compName = compName;

        this.fingers = new HashMap<Integer, VodDescriptor>();
        this.croupierSet = new ArrayList<VodDescriptor>();

        this.commWinSize = config.getComWinSize();

        this.maxWindowSize = 0;
        
        this.outstandingAck = new HashMap<TimeoutId, Integer>();

        logger.debug(compName + "COMMS_WIN_SIZE = " + commWinSize);
    }

    public void incrementAge() {
        bitTorrentSet.incrementDescriptorAges();
        upperSet.incrementDescriptorAges();
        lowerSet.incrementDescriptorAges();
    }

    public void chokeUnchoke(int time, boolean seeder) {
        Set<VodAddress> toChokeUnder = new HashSet<VodAddress>();
        Set<VodAddress> toChoke = new HashSet<VodAddress>();

        /*
         * every NUM_CYCLES_QUERY_GRANDCHILDREN cycles we ask the sending 
         * rate of our children to our grandchildren
         */
        int count = 0;

        /*
         * optimistic unchoke
         */
        if (choked.size() > 0 && time % (VodConfig.NUM_CYCLES_QUERY_GRANDCHILDREN * 3) == 0
                && bitTorrentSet.size() + lowerSet.size() < bitTorrentSet.getMaxSize()) {
            // Choke bittorrent set
            // TODO: Should I not unchoke based on download stats for nodes?
            int i = randGen.nextInt(choked.size());
            toChoke.remove(choked.get(i));
            choked.remove(i);
        }
        if (chokedUnder.size() > 0 && time % (VodConfig.NUM_CYCLES_QUERY_GRANDCHILDREN * 3) == 0
                && ((!seeder
                && lowerSet.size() + bitTorrentSet.size() < lowerSet.getMaxSize())
                || (seeder && (lowerSet.size() < lowerSet.getMaxSize()
                && bitTorrentSet.size() < bitTorrentSet.getMaxSize())))) {
            // Choke below set
            if (count == 0) {
                count = 1;
            }
            for (int j = 0; j < count; j++) {
                int i = randGen.nextInt(chokedUnder.size());
//                logger.debug(compName + "SEEDER OPTIMISTIC UNCHOKE: {}",
//                        chokedUnder.get(i).getPeerAddress().getId());
                toChokeUnder.remove(chokedUnder.get(i));
                chokedUnder.remove(i);
            }

        }

        for (VodAddress add : toChokeUnder) {
            bitTorrentSet.remove(add);
            lowerSet.remove(add);
            // TODO: Should I keep the NAT table entry open to enable
            // quick connection again after disconnect?
            delegator.disconnect(add, false);
        }
        for (VodAddress add : toChoke) {
            bitTorrentSet.remove(add);
            delegator.disconnect(add, false);
        }
    }

    /**
     * update the sets after a change in the utility and inform the neighbors
     * that need to be informed
     */
    public void informUtilityChange(boolean seeder) {
        UtilityVod utility = (UtilityVod) self.getUtility();
        /* we have to do that on the upperSet because when the utility
         * increase we can have some node that have to be in the utilitySet
         * and not in the upperSet that will stay in the upperSet
         * we don't have to do it for the utilitySet because the correction
         * will be done at the next update of the sets
         */
        List<VodDescriptor> noMoreInUpperSet;
        noMoreInUpperSet = upperSet.changeUtility(utility);
        List<VodDescriptor> addToUtilitySet;
        if (utility.getChunk() >= 0) {
            List<VodDescriptor> temp = new ArrayList<VodDescriptor>();
            for (VodDescriptor node : noMoreInUpperSet) {
                UtilityVod u = (UtilityVod) node.getUtility();
                if (u.getPiece() < utility.getPiece() + utility.getPieceOffset()
                        && u.getPiece() > utility.getPiece() - utility.getPieceOffset()) {
                    temp.add(node);
                }

            }
            addToUtilitySet = bitTorrentSet.updateAll(temp, utility);
            if (!seeder) {
                for (VodDescriptor node : addToUtilitySet) {
                    delegator.connect(node, true);
                }
            }

            logger.info(compName + "Utility changed, removing " + addToUtilitySet.size() + " from upper set");
            noMoreInUpperSet.removeAll(addToUtilitySet);
        }
        for (VodDescriptor node : noMoreInUpperSet) {
            delegator.disconnect(node.getVodAddress(), false);
        }
        /* force the verify of the below set, else the nodes take
         * time to know that the node of their upperSet changed their
         * utility
         */

//        for (VodDescriptor node : lowerSet.getAll()) {
//            triggerRefRequest(node);
//        }
    }

    /**
     * change the utility value, search for the first piece not downloaded from
     * the nUtility position and set it as the new utility.
     */
    public void changeUtility(int newUtility, boolean seeder,
            BitField bitField //TODO Alex do i really need this param here
    ) {
        UtilityVod utility = (UtilityVod) self.getUtility();

        int oldUtility = utility.getChunk();
        utility.setChunk(newUtility);
//        snapshot.setUtility(self.getAddress(), utility.getChunck());
        bitTorrentSet.getStats().changeUtility(oldUtility, utility, bitField.numberPieces(), bitField);

        // TODO: Check if I already have nodes at the new utility level first.
        // Ask the bootstrap server for nodes at the new utility level
        // Nodes that are behind NATs will be slower to connect to - if i already
        // have an open session to them - use it.

        /* we have to do that on the upperSet because when the utility
         * increases we can have some node that have to be in the utilitySet
         * and not in the upperSet that will stay in the upperSet
         * we don't have to do it for the utilitySet because the correction
         * will be done at the next update of the sets
         */
        List<VodDescriptor> noLongerInUpperSet = upperSet.changeUtility(utility);
        List<VodDescriptor> tocheck = new ArrayList<VodDescriptor>();
        for (VodDescriptor node : noLongerInUpperSet) {
            UtilityVod u = (UtilityVod) node.getUtility();
            if (u.getChunk() < utility.getChunk() + utility.getOffset()
                    && u.getChunk() > utility.getChunk() - utility.getOffset()) {
                tocheck.add(node);
            }
        }
        // Add those nodes that have now moved from upper-set to bittorrent set
        List<VodDescriptor> sendConnect = bitTorrentSet.updateAll(tocheck, utility);
        logger.info(compName + "Utility changed, removing " + sendConnect.size() + " from upper set");
        noLongerInUpperSet.removeAll(sendConnect);

        for (VodDescriptor node : noLongerInUpperSet) {
            // TODO - new message to move nodes from Lower set to Utility set.
            // Here, we're using two msgs to do the same thing...
            // TODO - do we have a cleanup event to remove old connections from
            // lower sets - e.g., incase this disconnect msg is lost?
            delegator.disconnect(node.getVodAddress(), false);
        }

        if (utility.getPiece() <= bitField.numberPieces()) {
            for (VodDescriptor node : sendConnect) {
                delegator.connect(node, true);
            }
        }

        /* force the check of the below set, else the nodes take
         * time to know that one node of their upperSet changed its
         * utility
         */
//        for (VodDescriptor node : lowerSet.getAll()) {
//            triggerRefRequest(node);
//        }
        if (seeder) {
            for (VodDescriptor node : bitTorrentSet.getAll()) {
                bitTorrentSet.remove(node.getVodAddress());
                delegator.disconnect(node.getVodAddress(), false);
            }
        }

        int i = utility.getChunk();
        if (utility.getPiece() <= bitField.numberPieces()) {
            while (i < bitField.getChunkFieldSize() + 11) {
                if (fingers.get(i) != null) {
                    VodDescriptor node = fingers.get(i);
                    delegator.connect(node, true);
                }
                i++;
            }
        }

        self.updateUtility(utility);
        updateSetsAndConnect(seeder);
    }

    /**
     * used periodically for one of the other sets And send a connect request to
     * the interesting nodes
     */
    public void updateSetsAndConnect(boolean seeder) {
        logger.info(compName + "Updating our upper/bittorrent sets with {} nodes from croupier.",
                croupierSet.size());

        List<VodDescriptor> sendConnect;
        UtilityVod utility = (UtilityVod) self.getUtility();
        List<VodDescriptor> toRemove = new ArrayList<VodDescriptor>();
        for (VodDescriptor des : croupierSet) {
            if (choked.contains(des.getVodAddress()) || chokedUnder.contains(des.getVodAddress())
                    || bitTorrentSet.contains(des.getVodAddress())) {
                toRemove.add(des);
            }
            if (des.getVodAddress().equals(self)) {
                logger.debug(compName + "REMOVED SELF FROM NEIGHBOURS");
                toRemove.add(des);
            }
        }
        for (VodDescriptor des : toRemove) {
            logger.info(compName + "Removing croupier descriptor: {}", des.toString());
            croupierSet.remove(des);
        }

        sendConnect = upperSet.updateAll(croupierSet, utility);
        List<VodDescriptor> sentConnectToUpper = new ArrayList<VodDescriptor>();
        if (!seeder) {
            for (VodDescriptor node : sendConnect) {
                logger.info(compName + "Connecting to neighbour {} with utility {}",
                        node.getId(), node.getUtility().getValue());
                delegator.connect(node, false);
                if (node.getUtility().getValue() >= VodConfig.SEEDER_UTILITY_VALUE) {
                    sentConnectToUpper.add(node);
                }
            }
        }
        toRemove = new ArrayList<VodDescriptor>();
        toRemove.addAll(sentConnectToUpper);
        for (VodDescriptor des : gradientSet) {
            if (choked.contains(des.getVodAddress()) || chokedUnder.contains(des.getVodAddress())
                    || upperSet.contains(des.getVodAddress())) {
                toRemove.add(des);
                logger.info(compName + "choking {},removing..", des);
            }
        }
        for (VodDescriptor des : toRemove) {
            gradientSet.remove(des);
        }
        sendConnect = bitTorrentSet.updateAll(gradientSet, utility);
        if (!seeder) {
            for (VodDescriptor node : sendConnect) {
                if (!choked.contains(node.getVodAddress())) {
                    delegator.connect(node, true);
                }
            }
        }

        updatefingers();
    }

    /**
     * use the random set to update the finger that we use to restart faster
     * after a jump
     */
    private void updatefingers() {
        for (VodDescriptor node : croupierSet) {
            UtilityVod u = (UtilityVod) node.getUtility();
            if (fingers.get(u.getChunk()) == null) {
                fingers.put(u.getChunk(), node);
            } else if (fingers.get(u.getChunk()).getAge() > node.getAge()) {
                fingers.put(u.getChunk(), node);
            }
        }
    }

    /**
     * @return Pair<ResponseType, BitTorrentSet>
     */
    public Pair<ConnectMsg.ResponseType, Boolean> processConnectReq(ConnectMsg.Request req, int pipeSize, boolean seeder) {
        UtilityVod myUtility = (UtilityVod) self.getUtility();
        /*
         * check in which set the node should be in and if there is space for it
         * send the answer corresponding to the resultant set.
         * If node's utility is in the BitTorrent range, join theBitTorrentSet.
         * If lower, join belowSet, if higher, join upperSet.
         * utility < 0 => seeder
         */
        Pair<ConnectMsg.ResponseType, Boolean> responseType;
        List<VodAddress> toBeRemoved = new ArrayList<VodAddress>();
        if ((myUtility.isSeeder() && req.isToUtilitySet())
                || !myUtility.notInBittorrentSet(req.getUtility())) {
            logger.debug("{} UTILITY SET -  received connectRequest from {}", compName, req.getVodSource().getId());
            // UTILITY SET
            upperSet.remove(req.getVodSource());
            lowerSet.remove(req.getVodSource());
            toBeRemoved = bitTorrentSet.add(req.getVodSource(), req.getUtility(), commWinSize, pipeSize, maxWindowSize, req.getMtu());
            if (!toBeRemoved.contains(req.getVodSource())) {
                logger.debug("{} accepted to connect to {} with utility ", compName, req.getVodSource().getId());
                responseType = Pair.with(ConnectMsg.ResponseType.OK, true);
                if (seeder) {
                    store.getVodDescriptorFromVodAddress(req.getVodSource()).setUploadRate(0);
                }
            } else {
                logger.debug("{} removed {} from neighbourhood (handleConnectRequest1)", compName, req.getVodSource().getId());
                store.suppress(req.getVodSource());
                toBeRemoved.remove(req.getVodSource());
                responseType = Pair.with(ConnectMsg.ResponseType.FULL, true);
            }
        } else if (req.getUtility().getChunk() <= myUtility.getChunk() - myUtility.getOffset()
                || (myUtility.isSeeder() && !req.isToUtilitySet())) {
            // BELOW SET or I am a SUPER-PEER and request is not to my utilitySet
            logger.debug("{} upperset {} ConnectRequest", compName, req.getVodSource().getId());

            upperSet.remove(req.getVodSource());
            bitTorrentSet.remove(req.getVodSource());
            toBeRemoved = lowerSet.add(req.getVodSource(), req.getUtility(),
                    myUtility, commWinSize, pipeSize, maxWindowSize, req.getMtu());

            if (!toBeRemoved.contains(req.getVodSource())) {
                responseType = Pair.with(ConnectMsg.ResponseType.OK, false);
                VodDescriptor nodeDescriptor = store.getVodDescriptorFromVodAddress(req.getVodSource());
                if (nodeDescriptor != null) {
                    nodeDescriptor.setUploadRate(0);
                } else {
                    logger.warn("{} Node was not in Neighbourhood when trying to update uploadRate for node", compName);
                    store.add(req.getVodSource(), req.getUtility(), false, commWinSize, pipeSize, maxWindowSize, req.getMtu());
                }
            } else {
                logger.info("{} ConnectResponse FULL. removed {} from neighbourhood (handleConnectRequest2)",
                        compName, req.getVodSource().getId());
                store.suppress(req.getVodSource());
                toBeRemoved.remove(req.getVodSource());
                responseType = Pair.with(ConnectMsg.ResponseType.FULL, false);
            }
        } else {
            if (lowerSet != null) {
                lowerSet.remove(req.getVodSource());
                upperSet.remove(req.getVodSource());
                bitTorrentSet.remove(req.getVodSource());
            }
            logger.info("Connect BAD_UTILITY. remove {} handleConnectRequest2", compName, req.getVodSource().getId());
            responseType = Pair.with(ConnectMsg.ResponseType.BAD_UTILITY, false);
        }

        for (VodAddress add : toBeRemoved) {
            delegator.disconnect(add, false);
        }
        return responseType;
    }

    public void processConnectResp(ConnectMsg.Response resp, int pipeSize) {
        /*
         * we update the utility in the random view in case we didn't have the latest 
         * utility value
         */
        //RandomView.updateUtility(self, event.getVodSource(), event.getUtility());
        // TODO trigger an event to Gradient containing a VodAddress and a new
        // utility value. Used to update utility value and age in gradientSet.
        UtilityVod myUtility = (UtilityVod) self.getUtility();
        List<VodAddress> toBeRemoved = new ArrayList<VodAddress>();
        switch (resp.getResponse()) {
            case OK:
                if ((resp.getUtility().isSeeder() && resp.isToUtilitySet())
                        || (resp.getUtility().getPiece() < myUtility.getPiece() + myUtility.getPieceOffset()
                        && resp.getUtility().getPiece() > myUtility.getPiece() - myUtility.getPieceOffset())) {
                    upperSet.remove(resp.getVodSource());
                    lowerSet.remove(resp.getVodSource());
                    toBeRemoved = bitTorrentSet.add(resp.getVodSource(), resp.getUtility(), commWinSize,
                            pipeSize, maxWindowSize, resp.getMtu());
                    bitTorrentSet.updatePeerInfo(resp.getVodSource(), resp.getUtility(), resp.getAvailableChunks(),
                            resp.getAvailablePieces());
                } else if (resp.getUtility().getChunk() >= myUtility.getChunk() + myUtility.getOffset()
                        || (resp.getUtility().isSeeder() && !resp.isToUtilitySet())) {
                    bitTorrentSet.remove(resp.getVodSource());
                    lowerSet.remove(resp.getVodSource());
                    //TODO Alex change utility
                    toBeRemoved = upperSet.add(resp.getVodSource(), resp.getUtility(), myUtility,
                            commWinSize, pipeSize, maxWindowSize, resp.getMtu());
                } else {
                    store.add(resp.getVodSource(), resp.getUtility(), false, commWinSize, pipeSize, maxWindowSize, resp.getMtu());
                    delegator.disconnect(resp.getVodSource(), false);
                }
                break;
            case FULL:
                // we just ignore the nodeAddr, next time we will be more luky
                // see if there is optimisations that can be done
                break;
            case BAD_UTILITY:
                // we just update the node utility in our view and ignore
                // the nodeAddr, next time we will be more lucky
                VodDescriptor nodeSrc = store.getVodDescriptorFromVodAddress(resp.getVodSource());
                if (nodeSrc != null) {
                    nodeSrc.setUtility(resp.getUtility());
                }
                break;
        }
        for (VodAddress add : toBeRemoved) {
            delegator.disconnect(add, false);
        }
    }

    public void processDisconnectReq(DisconnectMsg.Request req) {
        bitTorrentSet.remove(req.getVodSource());
        int ref = store.getRef(req.getVodSource());
        upperSet.remove(req.getVodSource());
        lowerSet.remove(req.getVodSource());
        store.suppress(req.getVodSource());
        ref = 0;
        delegator.disconnectResponse(new DisconnectMsg.Response(self.getAddress(), req.getVodSource(), req.getTimeoutId(), ref));
    }

    public void processDisconnectResp(DisconnectMsg.Response resp) {
        if (resp.getRef() == 0 && store.getRef(resp.getVodSource()) == 0) {
            upperSet.remove(resp.getVodSource());
            lowerSet.remove(resp.getVodSource());
            bitTorrentSet.remove(resp.getVodSource());
            store.suppress(resp.getVodSource());
        }
    }

    public void processRebootstrap(RebootstrapResponse resp) {
        for (VodDescriptor entry : resp.getVodInsiders()) {
            if (!self.getAddress().equals(entry.getVodAddress())) {
                delegator.connect(entry, false);
            }
        }
    }

    public void process(DisconnectMsg.RequestTimeout timeout) {
        bitTorrentSet.remove(timeout.getPeer());
        upperSet.remove(timeout.getPeer());
        lowerSet.remove(timeout.getPeer());
        store.suppress(timeout.getPeer());
    }

    public void process(Quit event) {
        if (store != null && store.getNeighbours() != null) {
            for (VodAddress destination : store.getNeighbours().keySet()) {
                delegator.leaving(new LeaveMsg(self.getAddress(), destination));
            }
        }
    }

    public void process(LeaveMsg event) {
        bitTorrentSet.remove(event.getVodSource());
        upperSet.remove(event.getVodSource());
        lowerSet.remove(event.getVodSource());
        store.suppress(event.getVodSource());
    }

    public void process(DataOfferMsg event) {
        if (!bitTorrentSet.contains(event.getVodSource())) {
            logger.trace("{} DataOffer from node not in my utility set", compName);
            //TODO Alex do I really need to remove and disconnect
            upperSet.remove(event.getVodSource());
            lowerSet.remove(event.getVodSource());
            delegator.disconnect(event.getVodSource(), false);
            return;
        }
        VodDescriptor node = bitTorrentSet.updatePeerInfo(event.getVodSource(), event.getUtility(),
                event.getAvailableChunks(), event.getAvailablePieces());
        if (node != null) {
            delegator.disconnect(node.getVodAddress(), false);
        }
    }

    public void process(InitiateDataOffer event, BitField storageBitfield) {
        List<VodAddress> toDisconnect = bitTorrentSet.cleanup(VodConfig.DATA_OFFER_PERIOD);
        for (VodAddress add : toDisconnect) {
            delegator.disconnect(add, false);
        }
        UtilityVod utility = (UtilityVod) self.getUtility();
        for (VodDescriptor desc : bitTorrentSet.getAll()) {
            //TODO do I increment age here?
            desc.incrementAndGetAge();

            if (utility.isSeeder()) { // seed
                delegator.dataOffer(new DataOfferMsg(self.getAddress(), desc.getVodAddress(), utility,
                        storageBitfield.getChunkfield(), null));
            } else {
                delegator.dataOffer(new DataOfferMsg(self.getAddress(), desc.getVodAddress(), utility,
                        storageBitfield.getChunkfield(), storageBitfield.getAvailablePieces(utility)));
            }
        }
    }

    public void process(DataMsg.Request req, int ackTimeout, byte[] subpieceVal) {
        VodAddress peer = req.getVodSource();
        /*
         * answer only if the node is a neighbor, the pipe is not full and we have the piece
         */
        if (!(bitTorrentSet.contains(peer) || lowerSet.contains(peer))) {
            logger.debug("{} Data Request from {} refused - not a neighbour",
                    new Object[]{compName, peer});
            return;
        }

        TimeoutId ackId = req.getAckId();
        if (ackId.getId() == 0) {
            ackId = null;
        }

        CommunicationWindow comWin = store.getVodDescriptorFromVodAddress(peer).getWindow();
        if (updateCommsWindow(comWin, ackId, req.getDelay()) == false) {
            if (ackId == null) {
                logger.warn("{} ACK null to update comWin", compName);
            } else {
                logger.info("{} Missing ACK {} to update comWin", compName, ackId.toString());
            }
        }

        int piece = req.getPiece();
        int subpiece = req.getSubpieceOffset();
        ScheduleTimeout st = new ScheduleTimeout(ackTimeout);
        st.setTimeoutEvent(new DataMsg.AckTimeout(st, peer, self.getOverlayId()));
        TimeoutId newAckId = st.getTimeoutEvent().getTimeoutId();
        DataMsg.Response pieceMessage = new DataMsg.Response(self.getAddress(), peer, req.getTimeoutId(), newAckId,
                subpieceVal, subpiece, piece, comWin.getSize(), System.currentTimeMillis());
        if (comWin.addMessage(newAckId, pieceMessage.getSize())) {
            logger.debug("{} Data ({},{}) Response", new Object[]{compName, piece, subpiece});
            delegator.dataResp(pieceMessage);
            outstandingAck.put(newAckId, pieceMessage.getSize());
            delegator.startTimer(st);
        } else {
            logger.info("{} peer {} connection SATURATED", compName, peer);
            delegator.saturated(new DataMsg.Saturated(self.getAddress(), peer, piece, comWin.getSize()));
        }
    }

    public boolean updateCommsWindow(CommunicationWindow comWin, TimeoutId ackId, long delay) {
        assert (comWin != null);

        if (ackId == null) {
            return false;
        }
        if (outstandingAck.containsKey(ackId)) {
            logger.debug(compName + "Received ACK {}. Updating CommWindow with delay {} ms.", ackId, delay);
            Integer msgSize = outstandingAck.remove(ackId);
            comWin.update(delay);
            comWin.removeMessage(ackId, msgSize);
            return true;
        }
        return false;
    }

    public boolean isChoked(VodAddress peer) {
        return choked.contains(peer) || chokedUnder.contains(peer);
    }

    public List<VodAddress> getUpperSet() {
        return upperSet.getAllAddress();
    }

    public boolean inUpperSet(VodAddress peer) {
        return upperSet.contains(peer);
    }

    public Stats getBitTorrentStats() {
        return bitTorrentSet.getStats();
    }

    public List<VodAddress> getBitTorrentSet() {
        return bitTorrentSet.getAllAddress();
    }

    public VodDescriptor getDescriptor(VodAddress peer) {
        return store.getVodDescriptorFromVodAddress(peer);
    }

    public List<VodDescriptor> getAllDescriptor() {
        return store.getAll();
    }

    public void newCroupierSet(List<VodDescriptor> descriptors) {
        if (descriptors.size() > 0) {
            croupierSet.clear();
            for (VodDescriptor descriptor : descriptors) {
                if (!self.getAddress().equals(descriptor.getVodAddress())) {
                    croupierSet.add(descriptor);
                }
            }
        }

        //TODO Alex is it really necessary?
        // Connect to returned nodes immediately if no neighbours and not seeding.
        UtilityVod utility = (UtilityVod) self.getUtility();
        if (bitTorrentSet.size() == 0 && upperSet.size() == 0 && utility.isSeeder() == false) {
            updateSetsAndConnect(false);
        }
    }
}
