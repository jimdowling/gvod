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
package se.sics.gvod.system.vod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import se.sics.gvod.net.VodAddress;
/**
 * 
 * @author gautier
 */
public class PeerInfo implements Comparable<PeerInfo> {

    private long bw;
    private int bwN;
    private final HashMap<Integer, List<VodAddress>> neighbours;
    private int utility;
    private Integer id;

    public PeerInfo(long bw, Integer id) {
        this.bw = bw;
        this.id = id;
        neighbours = new HashMap<Integer, List<VodAddress>>();
        neighbours.put(0, new ArrayList<VodAddress>());
        neighbours.put(1, new ArrayList<VodAddress>());
        neighbours.put(2, new ArrayList<VodAddress>());
        if (bw <= 64 * 1024) {
            bwN = 0;
        } else if (bw <= 128 * 1024) {
            bwN = 1;
        } else {
            bwN = 2;
        }
        utility = 0;
    }

    public Integer getId() {
        return id;
    }

    public long getBw() {
        return bw;
    }

    public int getBwN() {
        return bwN;
    }

    public int getUtility() {
        return utility;
    }

    public HashMap<Integer, List<VodAddress>> getNeighbours() {

        return neighbours;
    }

    public void setUtility(int utility) {
        this.utility = utility;
    }

    public void addToUtilitySet(VodAddress add) {
        neighbours.get(0).add(add);
    }

    public void addToUpperSet(VodAddress add) {
        neighbours.get(1).add(add);
    }

    public void quit() {
        neighbours.get(0).clear();
        neighbours.get(1).clear();
        neighbours.get(2).clear();
    }

    public void addToBelowSet(VodAddress add) {
        neighbours.get(2).add(add);
    }

    public void removeFromUtilitySet(VodAddress add) {
        neighbours.get(0).remove(add);
    }

    public void removeFromUpperSet(VodAddress add) {
        neighbours.get(1).remove(add);
    }

    public void removeFromBelowSet(VodAddress add) {
        neighbours.get(2).remove(add);
    }

    @Override
    public int compareTo(PeerInfo otherPeer) {
        if (otherPeer.getUtility() == utility) {
            return 0;
        } else if (otherPeer.getUtility() < utility) {
            return -1;
        } else {
            return 1;
        }
    }
}
