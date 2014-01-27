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

import java.util.Collections;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import se.sics.gvod.common.Self;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.common.VodDescriptor;
import se.sics.gvod.common.Utility;
import se.sics.gvod.common.UtilityVod;

public class UpperSet extends GSet {

    private int seed;

    public UpperSet(Self self, DescriptorStore store, int maxSize, int seed) {
        super(self, store, maxSize, seed);
        this.seed = seed;
    }

    public VodAddress updateUtility(VodAddress addr, UtilityVod newUtility) {
        UtilityVod myUtility = (UtilityVod) self.getUtility();
        if (contains(addr)) {
            if (!newUtility.isSeeder() && myUtility.validInUpperSet(newUtility)) {
                remove(addr);
            }
            if (getDescriptor(addr) != null) {
                getDescriptor(addr).setUtility(newUtility);
            }
        }
        return null;
    }

    public VodDescriptor getNodeBoltzman(UtilityVod utility) {
        int rank = 0;
        int nextBound = 0;
        if (!nodes.isEmpty()) {
            List<VodDescriptor> listTemp = new ArrayList<VodDescriptor>();
            listTemp.addAll(getAll());
            nextBound = 0;
            List<Long> utilitiesValues = new ArrayList<Long>();
            HashMap<Long, MapComp> map = new HashMap<Long, MapComp>();
            for (VodDescriptor node : listTemp) {
                UtilityVod u = (UtilityVod) node.getUtility();
                if (u.getChunk() > utility.getChunk()) {
                    long temp = u.getChunk() - utility.getChunk();
                    if (!utilitiesValues.contains(temp)) {
                        MapComp comp = new MapComp(nextBound,
                                nextBound + (int) ((1 / (float) temp) * 10000),
                                node, seed);
                        nextBound += (int) ((1 / (float) temp) * 10000);
                        utilitiesValues.add(temp);
                        map.put(temp, comp);
                    } else {
                        map.get(temp).adNode(node);
                    }
                } else {
                    // TODO: Put a logger here if necessary
//                    System.out.println(self + " #### ton code marche pas bien upperSet "
//
//                            + node.getVodAddress().getId() + " my utility " + utility.getChunk()
//                            + " its utility " + node.getUtility().getChunk());
                }
            }
            if (nextBound == 0) {
                return null;
            }
            rank = random.nextInt(nextBound);

            //FIXME is there a beter solution
            for (MapComp comp : map.values()) {
                if (rank >= comp.getLowerBound() && rank < comp.getUpperBound()) {
                    return comp.getRandomNode();
                }
            }
        }
        return null;
    }

    public List<VodDescriptor> changeUtility(UtilityVod utility) {
        self.updateUtility(utility);
        List<VodDescriptor> toDisconnect = new ArrayList<VodDescriptor>();
        if (utility.isSeeder()) {
            List<VodDescriptor> tempList = new ArrayList<VodDescriptor>();
            VodDescriptor temp;
            tempList.addAll(getAll());
            for (VodDescriptor node : tempList) {
                temp = remove(node.getVodAddress());
                if (temp != null) {
                    toDisconnect.add(temp);
                }
            }
            return toDisconnect;
        }
        List<VodDescriptor> tempList = new ArrayList<VodDescriptor>();
        VodDescriptor temp;
        tempList.addAll(getAll());
        for (VodDescriptor node : tempList) {
            UtilityVod u = (UtilityVod) node.getUtility();
            if (u.getChunk() > 0 && u.getChunk() < utility.getChunk() + utility.getOffset()) {
                temp = remove(node.getVodAddress());
                if (temp != null) {
                    toDisconnect.add(temp);
                }
            }
        }
        return toDisconnect;
    }

    public List<VodDescriptor> updateAll(List<VodDescriptor> list, UtilityVod utility) {
        List<VodDescriptor> sendConnect = new ArrayList<VodDescriptor>();

        self.updateUtility(utility);
        if (utility.isSeeder()) {
            return new ArrayList<VodDescriptor>();
        }
        /* first loop to suppress the entries that are already in the set
         * doing that we avoid the case where we suppress and then add the same node.
         * I think that we should solve the problem in one loop but it's more complicated
         * and as the loop are small I'm not sur if it's very interesting.
         */
        List<VodDescriptor> removeFromList = new ArrayList<VodDescriptor>();

        for (VodDescriptor listEntry : list) {
            if (contains(listEntry.getVodAddress())) {
                VodDescriptor d = getDescriptor(listEntry.getVodAddress());
                // TODO - keep reference with most recent age, here just
                // replacing with new entry
                VodDescriptor entry =
                        new VodDescriptor(
                        listEntry.getVodAddress(),
                        listEntry.getUtility(),
                        d.getUploadRate(),
                        d.getRequestPipeline(),
                        d.getWindow(),
                        d.getPipeSize(),
                        d.getMtu());
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
            //    //System.out.println(self + " updateAll " + list);
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

    public List<VodAddress> add(VodAddress address, Utility utility, Utility myUtility,
            int comWinSize, int pipeSize, int maxWindowSize, int mtu) {
        self.updateUtility(myUtility);
        VodDescriptor node = new VodDescriptor(address, utility, 
                comWinSize, pipeSize, maxWindowSize, mtu);
        if (!contains(address)) {
            store.add(node, true);
            nodes.add(address);
        } else {
            VodDescriptor d = getDescriptor(address);
            VodDescriptor entry = new VodDescriptor(
                    address, utility,
                    d.getUploadRate(),
                    d.getRequestPipeline(),
                    d.getWindow(),
                    d.getPipeSize(),
                    d.getMtu());
            nodes.add(address);
            store.update(entry);
        }
        List<VodAddress> toBeRemove = new ArrayList<VodAddress>();
        while (nodes.size() > maxSize) {
            toBeRemove.add(remove(getWorstUtilityNode().getVodAddress()).getVodAddress());
        }
        numChanged += toBeRemove.size();
        return toBeRemove;
    }

    protected VodDescriptor getBestNode(List<VodDescriptor> list) {
        Collections.sort(list, comparatorDistance);
        return list.get(0);
    }
}
