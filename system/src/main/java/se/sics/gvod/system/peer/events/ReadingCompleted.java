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
package se.sics.gvod.system.peer.events;

import se.sics.kompics.Event;
import se.sics.gvod.net.VodAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * The <code>ReadingCompleted</code> class.
 *
 * @author Gautier Berthou
 */
public final class ReadingCompleted extends Event {

    private final VodAddress peer;
    private final int nbBuffering;
    private final int waiting;
    private final int misConnect;
    private final long waitingTime;
    private final Map<Integer, Long> UtilitySetSizeEvolution;
    private final Map<Integer, Long> utilityAfterTime;
    private final boolean freeRider;
    private final long jumpForwardTime;

    /**
     * carry information used to make statistics for the simulations
     * @param peer
     * @param nbBuffering
     * @param waiting
     * @param misConnect
     * @param waitingTime
     * @param UtilitySetSizeEvolution
     * @param utilityAfterTime
     * @param freeRider
     * @param jumpForwardTime
     */
    public ReadingCompleted(VodAddress peer, int nbBuffering, int waiting,
            int misConnect, long waitingTime,
            Map<Integer, Long> UtilitySetSizeEvolution,
            Map<Integer, Long> utilityAfterTime, boolean freeRider,
            long jumpForwardTime) {
        this.peer = peer;
        this.nbBuffering = nbBuffering;
        this.waiting = waiting;
        this.misConnect = misConnect;
        this.waitingTime = waitingTime;
        if (UtilitySetSizeEvolution!= null){
        this.UtilitySetSizeEvolution = new HashMap<Integer, Long>(UtilitySetSizeEvolution);
        } else {
            this.UtilitySetSizeEvolution = null;
        }
        this.utilityAfterTime = utilityAfterTime;
        this.freeRider = freeRider;
        this.jumpForwardTime = jumpForwardTime;
    }

    public int getNbBuffering() {
        return nbBuffering;
    }

    public VodAddress getPeer() {
        return peer;
    }

    public int getWaiting() {
        return waiting;
    }

    public int getMisConnect() {
        return misConnect;
    }

    public long getWaitingTime() {
        return waitingTime;
    }

    public Map<Integer, Long> getUtilitySetSizeEvolution() {
        return UtilitySetSizeEvolution;
    }

    public Map<Integer, Long> getUtilityAfterTime() {
        return utilityAfterTime;
    }

    public boolean isFreeRider() {
        return freeRider;
    }

    public long getJumpForwardTime() {
        return jumpForwardTime;
    }
}
