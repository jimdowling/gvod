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
package se.sics.gvod.system.peer.sets;

import java.util.ArrayList;
import java.util.List;
import se.sics.gvod.common.Self;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.common.VodDescriptor;
import se.sics.gvod.system.storage.Stats;
import se.sics.gvod.common.UtilityVod;
import se.sics.gvod.config.BaseCommandLineConfig;
import se.sics.gvod.config.VodConfig;
/**
 *
 * @author gautier
 */
public class BitTorrentSet extends GSet {

    private Stats stats;

    public BitTorrentSet(Self self, DescriptorStore store, int maxSize, int seed) {
        super(self, store, maxSize, seed);
        stats = new Stats((UtilityVod) self.getUtility(), seed);
    }


    public VodDescriptor updatePeerInfo(VodAddress peer, UtilityVod peerUtility,
            byte[] availableChunks,
            byte[][] availablePieces) {
        UtilityVod myUtility = (UtilityVod) self.getUtility();
        VodDescriptor d = getDescriptor(peer);
        if (peerUtility.isSeeder()) {
            if (d != null) {
                d.setUtility(peerUtility);
                resetDescriptorAge(peer);
                stats.updateStats(d, availablePieces, availableChunks,
                        peerUtility, myUtility);
            }
            return null;
        }
        if (d != null) {
            if (myUtility.notInBittorrentSet(peerUtility)) {
                return remove(peer);
            } else {
                d.setUtility(peerUtility);
                d.resetAge();
                stats.updateStats(d, availablePieces, availableChunks,
                        peerUtility, myUtility);
                return null;
            }
        } else {
            return new VodDescriptor(peer, peerUtility, 0, 0, 0, BaseCommandLineConfig.DEFAULT_MTU);
        }
    }

    public Stats getStats() {
        return stats;
    }

    public List<VodAddress> cleanup(long dataOfferPeriod) {
        List<VodDescriptor> temp = new ArrayList<VodDescriptor>();
        List<VodAddress> toDisconnect = new ArrayList<VodAddress>();
        for (VodDescriptor node : getAll()) {
            if (node.getAge() > /*2 */ dataOfferPeriod) {
                temp.add(node);
            }
        }
        for (VodDescriptor node : temp) {
            toDisconnect.add(remove(node.getVodAddress()).getVodAddress());
        }
        return toDisconnect;
    }

    public VodAddress updateUtility(VodAddress addr, UtilityVod peerUtility) {
        UtilityVod myUtility = (UtilityVod) self.getUtility();
        VodDescriptor d = getDescriptor(addr);
        if (contains(addr)) {
            if (peerUtility.isSeeder()) {
                d.setUtility(peerUtility);
                return null;
            }
            if (myUtility.notInBittorrentSet(peerUtility)) {
                nodes.remove(addr);
                return store.remove(addr).getVodAddress();
            }
            d.setUtility(peerUtility);
        }
        return null;
    }

    public List<VodDescriptor> updateAll(List<VodDescriptor> list,
            UtilityVod utility) {
        self.updateUtility(utility);
        return updateAll(list);
    }
    public List<VodDescriptor> updateAll(List<VodDescriptor> list) {
        List<VodDescriptor> sendConnect = new ArrayList<VodDescriptor>();

//        if (self.getUtility().getValue() >= VodConfig.SEEDER_UTILITY_VALUE) {
        if (self.getUtility().getValue() <= VodConfig.SEEDER_UTILITY_VALUE) {
            return new ArrayList<VodDescriptor>();
        }

        /* first loop to suppress the entries that are already in the set
         * doing that we avoid the case where we suppress and then add the same node.
         * I think that we should solve the problem in one loop but it's more complicated
         * and as the loops are small I'm not sure if it's worthwhile.
         */
        List<VodDescriptor> removeFromList = new ArrayList<VodDescriptor>();

        for (VodDescriptor listEntry : list) {
            if (contains(listEntry.getVodAddress())) {
                VodDescriptor d = getDescriptor(listEntry.getVodAddress());
                VodDescriptor entry = new VodDescriptor(
                        listEntry.getVodAddress(), listEntry.getUtility(),
                        d.getUploadRate(),
                        d.getRequestPipeline(),
                        d.getWindow(),
                        d.getPipeSize(),
                        d.getMtu()
                        );
                nodes.add(entry.getVodAddress());
                store.update(entry);
                removeFromList.add(entry);
            }
        }
        for (VodDescriptor entry : removeFromList) {
            list.remove(entry);
        }
        //second loop to updateAll the set
        List<VodDescriptor> temp = new ArrayList<VodDescriptor>();
        temp.addAll(list);
        temp.addAll(getAll());
        if (temp.size() <= maxSize) {
            return list;
        } else {
            VodDescriptor worstNode;
            while (temp.size() > maxSize) {
                worstNode = getWorstUtilityNode(temp);
                temp.remove(worstNode);
            }

            for (VodDescriptor node : temp) {
                if (list.contains(node)) {
                    sendConnect.add(node);
                }
            }
            return sendConnect;
        }
    }

    /**
     * 
     * @param address
     * @param peerutility
     * @param comWinSize
     * @param pipeSize
     * @param maxWindowSize
     * @param mtu
     * @return 
     */
    public List<VodAddress> add(VodAddress address, UtilityVod peerutility, 
            int comWinSize, int pipeSize,int maxWindowSize, int mtu) {
        UtilityVod myUtility = (UtilityVod) self.getUtility();
        List<VodAddress> toBeRemoved = new ArrayList<VodAddress>();
        VodDescriptor node = new VodDescriptor(address, peerutility, /*uploadRate*/
                comWinSize, pipeSize, maxWindowSize, mtu);
        if (myUtility.isSeeder()) {
            if (size() >= maxSize) {
                if (contains(address)) {
                    store.add(node, false);
                    toBeRemoved.add(address);
                }
            } else {
                if (!contains(address)) {
                    store.add(node, true);
                    nodes.add(address);
                }
            }
            return toBeRemoved;
        }
        if (!contains(address)) {
            store.add(node, true);
            nodes.add(address);
        } else {

            VodDescriptor d = getDescriptor(address);
            VodDescriptor entry = new VodDescriptor(
                    address, peerutility,
                    d.getUploadRate(),
                    d.getRequestPipeline(),
                    d.getWindow(),
                    d.getPipeSize(),
                    mtu);
            nodes.add(entry.getVodAddress());
            store.update(entry);
        }

        while (size() > maxSize) {
            toBeRemoved.add(remove(getWorstUtilityNode().getVodAddress()).getVodAddress());
        }
        numChanged += toBeRemoved.size();
        return toBeRemoved;
    }

//    protected Comparator<VodDescriptor> comparatorDistance = new Comparator<VodDescriptor>() {
//
//        @Override
//        public int compare(VodDescriptor o1, VodDescriptor o2) {
//            if (o1.getUtility().getPiece() == o2.getUtility().getPiece()) {
//                if (o1.getAge() < o2.getAge()) {
//                    return -1;
//                } else if (o2.getAge() < o1.getAge()) {
//                    return 1;
//                } else {
//                    return 0;
//                }
//            } else if (o1.getUtility().getChunk() < 0) {
//                return -1;
//            } else if (o2.getUtility().getChunk() < 0) {
//                return 1;
//            } else if (o1.getUtility().getPiece() > utility.getPiece()
//                    && o2.getUtility().getPiece() < utility.getPiece()) {
//                return -1;
//            } else if (o1.getUtility().getPiece() < utility.getPiece()
//                    && o2.getUtility().getPiece() > utility.getPiece()) {
//                return 1;
//            } else if (Math.abs(o1.getUtility().getPiece() - utility.getPiece()) < Math.abs(o2.getUtility().getPiece() - utility.getPiece())) {
//                return -1;
//            } else {
//                return 1;
//            }
//        }
//    };
}
