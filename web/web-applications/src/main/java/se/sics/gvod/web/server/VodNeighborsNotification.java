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
package se.sics.gvod.web.server;

import se.sics.gvod.common.VodNeighbors;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.net.Transport;
import se.sics.gvod.net.msgs.DirectMsg;
import se.sics.gvod.net.msgs.RewriteableMsg;

/**
 * The <code>GVodNeighborsNotification</code> class.
 * 
 * @author Cosmin Arad <cosmin@sics.se>
 * @version $Id: GVodNeighborsNotification.java 1217 2009-09-06 20:51:43Z Cosmin $
 */
public final class VodNeighborsNotification extends DirectMsg {

    /**
     *
     */
    private static final long serialVersionUID = -7475140935755033727L;
    private final VodAddress peerAddress;
    private final VodNeighbors gvodNeighbors;
    private final int utility;

    public VodNeighborsNotification(VodAddress peerAddress,
            VodAddress destination, 
//            Transport protocol,
            VodNeighbors neighbors, int utility) {
        super(peerAddress, destination, Transport.UDP, null);
        this.peerAddress = peerAddress;
        this.gvodNeighbors = neighbors;
        this.utility = utility;
    }

    public VodAddress getPeerAddress() {
        return peerAddress;
    }

    public VodNeighbors getGVodNeighbors() {
        return gvodNeighbors;
    }

    public int getUtility() {
        return utility;
    }

    @Override
    public RewriteableMsg copy() {
        VodNeighborsNotification copy = new VodNeighborsNotification(peerAddress, vodDest, gvodNeighbors, utility);
        copy.setTimeoutId(timeoutId);
        return copy;
    }
}
