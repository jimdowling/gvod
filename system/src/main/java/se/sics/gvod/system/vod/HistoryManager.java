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
import java.util.List;
import java.util.Map;
import se.sics.gvod.common.Self;
import se.sics.gvod.common.UtilityVod;

/**
 * @author Alex Ormenisan <aaor@sics.se>
 */
public class HistoryManager {

    private final Map<Integer, Long> utility;
    private final Map<Integer, Integer> rest;

    public HistoryManager() {
        this.utility = new HashMap<Integer, Long>();
        this.rest = new HashMap<Integer, Integer>();
    }

    public void registerUtility(int time, long piece) {
        // stats for experiment
        if (time % 30 == 0) {
            if (piece < 0) {
                utility.put(time, utility.get(time - 30));
            } else {
                utility.put(time, piece);
            }
        }
    }

    public void registerDownload(int time, int storageNeeded) {
        /*
         * history of the number of pieces still to download
         * to evaluate the speed at which the node is downloading
         */
        if (time % 10 == 0 && rest != null) {
            rest.put(time, storageNeeded);
        }
        //TODO Alex the cleanup seems too complicated for what it should be
        /*
         * cleaning the history of old values
         */
        List<Integer> toRemove = new ArrayList<Integer>();
        for (int t : rest.keySet()) {
            if (time - t > 30) {
                toRemove.add(t);
            }
        }
        for (int t : toRemove) {
            rest.remove(t);
        }
    }
    
    public int getRest(int time) {
        return rest.get(time);
    }

    public Map<Integer, Long> getUtilityAfterTime() {
        return utility;
    }
}
