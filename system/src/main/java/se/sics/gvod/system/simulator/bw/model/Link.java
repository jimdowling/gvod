/**
 * This file is part of the ID2210 course assignments kit.
 * 
 * Copyright (C) 2009 Royal Institute of Technology (KTH)
 *
 * This program is free software; you can redistribute it and/or
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
package se.sics.gvod.system.simulator.bw.model;

import se.sics.gvod.net.msgs.DirectMsg;


/**
 * The <code>Link</code> class.
 * 
 * @author Cosmin Arad <cosmin@sics.se>
 */
public final class Link {

    // link capacities are given in bytes per second
    private final long capacity;
    private long lastExitTime;
    private long totalThrough;
    private long firstExitTime;
    private boolean first=true;

    public Link(long capacity) {
        this.capacity = capacity;
        this.lastExitTime = System.currentTimeMillis();
    }

    /**
     * @param message
     * @return the delay in ms for this message
     */
    public long addMessage(DirectMsg message) {
        double size = message.getSize();
        double capacityPerMs = ((double) capacity) / 1000;
        long bwDelayMs = (long) (size / capacityPerMs);
        if (bwDelayMs < 1){
            bwDelayMs = 1;
        }
        long now = System.currentTimeMillis();
        totalThrough+=size;
        if(first){
            firstExitTime = now + bwDelayMs;
            first=false;
        }
        if (now >= lastExitTime) {
            // the pipe is empty
            lastExitTime = now + bwDelayMs;
        } else {
            // the pipe has some messages and the last message's exit time is
            // stored in lastExitTime
            lastExitTime = lastExitTime + bwDelayMs;
            //
            bwDelayMs = lastExitTime - now;
        }
        return bwDelayMs;
    }

    public long getCapacity() {
        return capacity;
    }

    public long getBwUsePs() {
        return (totalThrough*1000)/(lastExitTime-firstExitTime);
    }

    public long getBwUse() {
        return totalThrough;
    }
}
