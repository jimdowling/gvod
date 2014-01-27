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
import se.sics.gvod.common.Utility;
import se.sics.gvod.common.UtilityVod;

public class LowerSet extends GSet {

    public LowerSet(Self self, DescriptorStore store, int maxSize, int seed) {
        super(self, store, maxSize, seed);
    }

    public void setSize(int size) {
        this.maxSize = size;
    }

    public List<VodAddress> add(VodAddress address, Utility utility, UtilityVod myUtility, /*int uploadRate*/
            int comWinSize, int pipeSize, int maxWindowSize, int mtu) {
        self.updateUtility(myUtility);
        VodDescriptor node = new VodDescriptor(address, utility, 
                comWinSize, pipeSize, maxWindowSize, mtu);
        if (!contains(address)) {
            store.add(node, false);
            nodes.add(address);
        } else {
            VodDescriptor d = getDescriptor(address);
            VodDescriptor entry = new VodDescriptor(
                    address,
                    utility,
                    d.getUploadRate(),
                    d.getRequestPipeline(),
                    d.getWindow(),
                    d.getPipeSize(),
                    mtu);
            nodes.add(entry.getVodAddress());
            store.update(entry);
        }
        List<VodAddress> toBeRemove = new ArrayList<VodAddress>();
        while (nodes.size() > maxSize) {
            toBeRemove.add(remove(getWorstUtilityNode().getVodAddress()).getVodAddress());
        }
        numChanged += toBeRemove.size();
        return toBeRemove;
    }
    


}
