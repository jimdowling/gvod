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
import java.util.HashMap;
import se.sics.gvod.net.VodAddress;

/**
 *
 * @author gautier
 */
public class DownloadStats {

    private static final int DOWNLOAD_PERIOD = 30 * 1000;
    
    private HashMap<VodAddress, HashMap<Long, Integer>> stats;

    public DownloadStats() {
        stats = new HashMap<VodAddress, HashMap<Long, Integer>>();
    }

    public void downloadedFrom(VodAddress peer, int nbSubpieces) {
        if (!stats.containsKey(peer)) {
            stats.put(peer, new HashMap<Long, Integer>());
        }
        stats.get(peer).put(System.currentTimeMillis(), nbSubpieces);
    }

    /**
     * Removes all stats 30 seconds or older.
     */
    public void clear() {
        long now = System.currentTimeMillis();
        ArrayList<VodAddress> peerToRemove = new ArrayList<VodAddress>();
        for (VodAddress peer : stats.keySet()) {
            HashMap<Long,Integer> peerStats = stats.get(peer);
            ArrayList<Long> toRemove = new ArrayList<Long>();
            for (long t : peerStats.keySet()) {
                long minTs = now - DOWNLOAD_PERIOD;
                if (t < minTs) {
                    toRemove.add(t);
                }
            }
            for (long r : toRemove) {
                peerStats.remove(r);
            }
            if(peerStats.isEmpty()){
                peerToRemove.add(peer);
            }
        }
        for(VodAddress peer : peerToRemove){
            stats.remove(peer);
        }
    }

    public int getDownloaded(VodAddress peer){
        clear();
        HashMap<Long,Integer> peerStats = stats.get(peer);
        if(peerStats== null){
            return 0;
        }
        int result=0;
        for(long t : peerStats.keySet()){
            result+=peerStats.get(t);
        }
        return result;
    }

    public void removeDownloaded(VodAddress peer, int NbSubpieces){
        clear();
        HashMap<Long,Integer> peerStats = stats.get(peer);
        if(peerStats==null){
            return;
        }
        ArrayList<Long> keys = new ArrayList<Long>(peerStats.keySet());
        peerStats.remove(keys.get(keys.size() - 1));
    }
}
