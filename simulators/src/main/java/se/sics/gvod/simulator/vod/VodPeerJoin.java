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
package se.sics.gvod.simulator.vod;


import se.sics.gvod.net.Nat;
import se.sics.kompics.Event;
/**
 * 
 * @author gautier
 */
public final class VodPeerJoin extends Event {

    private final Integer gvodId;
    private final int utility;
    private final boolean seed;
    private final long downloadBw; // in bytes per second
    private final long uploadBw; // in bytes per second
    private final Nat nat;
    private final int overlayId;

    public VodPeerJoin(Integer gvodId, int utility, boolean seed,
            long downloadBw, long uploadBw, Nat nat, int overlayId) {
        this.gvodId = gvodId;
        this.utility = utility;
        this.seed = seed;
        this.downloadBw = downloadBw;
        this.uploadBw = uploadBw;
        this.nat = nat;
        this.overlayId = overlayId;
    }

    public Nat getNat() {
        return nat;
    }

    public int getOverlayId() {
        return overlayId;
    }
    

    public Integer getGVodId() {
        return gvodId;
    }

    public int getUtility() {
        return utility;
    }

    public boolean isSeed() {
        return seed;
    }

    @Override
    public String toString() {
        return "Join@" + gvodId;
    }

    public long getDownloadBw() {
        return downloadBw;
    }

    public long getUploadBw() {
        return uploadBw;
    }
}
