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

import java.util.Collection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.common.VodDescriptor;

import java.util.Random;
import java.util.Set;
import se.sics.gvod.common.Self;
import se.sics.gvod.common.UtilityVod;
import se.sics.gvod.config.VodConfig;

/**
 *
 * @author gautier
 */
public class GSet {

    protected Self self;
    protected int maxSize;
    protected final DescriptorStore store;
    protected Set<VodAddress> nodes = new HashSet<VodAddress>();
    protected long numChanged;
    protected Random random;

    public GSet(Self self, DescriptorStore store, int maxSize, int seed) {
        this.self = self;
        this.store = store;
        this.maxSize = maxSize;
        numChanged = 0;
        random = new Random(seed);
    }

    public boolean isFull() {
        if (nodes.size() >= maxSize && maxSize > 0) {
            return true;
        } else {
            return false;
        }
    }

    public List<VodDescriptor> getAll() {
        return store.getVodDescriptorsFromVodAddresses(nodes);
    }

    public List<VodAddress> getAllAddress() {
        return new ArrayList<VodAddress>(nodes);
    }

    public boolean contains(VodAddress add) {
        if (add == null) {
            return false;
        }
        if (nodes.contains(add)) {
            return true;
        } else {
            return false;
        }
    }

    public VodDescriptor getRandomNode() {
        if (!nodes.isEmpty()) {
            int num = random.nextInt(nodes.size());
            Iterator<VodAddress> iter = nodes.iterator();
            VodAddress picked = null;
            for (int i = 0; i < num; i++) {
                picked = iter.next();
            }
            if (picked != null) {
                return store.getVodDescriptorFromVodAddress(picked);
            }
        }
        return null;
    }

    /**
     * TODO - What is this? Nodes that have utility greater than zero?
     * @return
     */
    public VodDescriptor getRandomNodeWithoutSuper() {
        if (nodes.isEmpty()) {
            return null;
        } else {
            List<VodDescriptor> list = new ArrayList<VodDescriptor>();
            for (VodDescriptor node : getAll()) {
//                if (node.getUtility().getValue() <= VodConfig.SEEDER_UTILITY_VALUE) {
                if (node.getUtility().getValue() > VodConfig.SEEDER_UTILITY_VALUE) {
                    list.add(node);
                }
            }
            if (list.isEmpty()) {
                return getRandomNode();
            } else {
                return list.get(random.nextInt(list.size()));
            }
        }
    }

    public VodDescriptor getRandomNode(Collection<VodAddress> usedNodes) {
        if (!nodes.isEmpty()) {
            List<VodDescriptor> temp = new ArrayList<VodDescriptor>();
            temp.addAll(store.getVodDescriptorsFromVodAddresses(nodes));
            temp.removeAll(usedNodes);
            return temp.get(random.nextInt(temp.size()));
        }
        return null;
    }

    public void incrementDescriptorAges() {
        for (VodDescriptor entry : store.getVodDescriptorsFromVodAddresses(nodes)) {
            entry.incrementAndGetAge();
        }
    }

    public void resetDescriptorAge(VodAddress addr) {
        VodDescriptor d = getDescriptor(addr);
        if (d != null) {
            d.resetAge();
        }
    }

    public void setUploadRate(VodAddress addr, int uploadRate) {
        VodDescriptor d = getDescriptor(addr);
        if (d != null) {
            d.setUploadRate(uploadRate);
        }
    }

    public void incrementUploadRate(VodAddress addr, int uploadRate) {
        VodDescriptor d = getDescriptor(addr);
        if (d != null) {
            d.setUploadRate(uploadRate + d.getUploadRate());
        }
    }

    public long getNumChanged() {
        return numChanged;
    }

    public int size() {
        return nodes.size();
    }

    public VodAddress getWorstUploader() {
        if (nodes.isEmpty()) {
            return null;
        }
        List<VodDescriptor> sortedNodes = store.getVodDescriptorsFromVodAddresses(nodes);
        Collections.sort(sortedNodes, comparatorWorstUploader);
        if (!sortedNodes.isEmpty()) {
            return sortedNodes.get(0).getVodAddress();
        }
        return null;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public VodDescriptor getDescriptor(VodAddress addr) {
        if (nodes.contains(addr)) {
            return store.getVodDescriptorFromVodAddress(addr);
        }
        return null;
    }
    
    public VodDescriptor remove(VodAddress peer) {
        VodDescriptor sendDisconnect = null;
        VodDescriptor descriptor = getDescriptor(peer);
        if (descriptor != null) {
            sendDisconnect = store.remove(peer);
        }
        return sendDisconnect;
    }
    
    protected VodDescriptor getWorstUtilityNode() {
        List<VodDescriptor> sortedNodes = store.getVodDescriptorsFromVodAddresses(nodes);
        Collections.sort(sortedNodes, comparatorDistance);
        return sortedNodes.get(sortedNodes.size() - 1);
    }
    protected VodDescriptor getWorstUtilityNode(List<VodDescriptor> nodes) {
        Collections.sort(nodes, comparatorDistance);
        return nodes.get(nodes.size() - 1);
    }
    
    protected VodDescriptor getBestUtilityNode() {
        List<VodDescriptor> sortedNodes = store.getVodDescriptorsFromVodAddresses(nodes);
        Collections.sort(sortedNodes, comparatorDistance);
        return sortedNodes.get(0);
    }
    protected VodDescriptor getBestUtilityNode(List<VodDescriptor> nodes) {
        Collections.sort(nodes, comparatorDistance);
        return nodes.get(0);
    }
    
    
    protected VodDescriptor getWorstUploaderNode() {
        List<VodDescriptor> sortedNodes = store.getVodDescriptorsFromVodAddresses(nodes);
        Collections.sort(sortedNodes, comparatorWorstUploader);
        return sortedNodes.get(0);
    }
    
    protected Comparator<VodDescriptor> comparatorWorstUploader = new Comparator<VodDescriptor>() {

        @Override
        public int compare(VodDescriptor o1, VodDescriptor o2) {
            if (o1.getUploadRate() == o2.getUploadRate()) {
                if (o1.getAge() < o2.getAge()) {
                    return 1;
                } else {
                    return -1;
                }
            } else if (o1.getUploadRate() < o2.getUploadRate()) {
                return -1;
            } else if (o2.getUploadRate() < o1.getUploadRate()) {
                return 1;
            }
            return 1;
        }
    };    

    

    protected Comparator<VodDescriptor> comparatorDistance = new Comparator<VodDescriptor>() {

        @Override
        public int compare(VodDescriptor o1, VodDescriptor o2) {
            UtilityVod u1 = (UtilityVod) o1.getUtility();
            UtilityVod u2 = (UtilityVod) o2.getUtility();
            UtilityVod s = (UtilityVod) self.getUtility();
            
            if (u1 == u2) {
                if (o1.getAge() < o2.getAge()) {
                    return -1;
                } else if (o2.getAge() < o1.getAge()) {
                    return 1;
                } else {
                    return 0;
                }
            } else if (u1.getChunk() > s.getChunk() && u2.getChunk() < s.getChunk()) {
                return -1;
            } else if (u1.getChunk() < s.getChunk() && u2.getChunk() > s.getChunk()) {
                return 1;
            } else if (Math.abs(u1.getChunk() - s.getChunk()) < Math.abs(u2.getChunk() 
                    - s.getChunk())) {
                return -1;
            } else {
                return 1;
            }
        }
    };
    
}
