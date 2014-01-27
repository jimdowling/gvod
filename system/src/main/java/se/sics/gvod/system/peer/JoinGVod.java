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
package se.sics.gvod.system.peer;


import se.sics.gvod.system.vod.snapshot.Snapshot;
import se.sics.kompics.Event;

/**
 * The <code>JoinGVod</code> class represents an event that tells a GVodPeer
 * to initiate a bootstrap and a GVod join procedure.
 * 
 * @author Cosmin Arad <cosmin@sics.se>, Gautier Berthou
 */
public class JoinGVod extends Event {

    private final int utility;
    private final boolean seed;
    private final boolean play;
    private long downloadBw; // in bytes per second
    private long uploadBw; // in bytes per second
    private boolean freeRider;
    private Snapshot snapshot;

    public JoinGVod(int utility, boolean seed,
            long downloadBw, long uploadBw, boolean freeRider, Snapshot snapshot
            , boolean play
            ) {
        this.utility = utility;
        this.seed = seed;
        this.downloadBw = downloadBw;
        this.uploadBw = uploadBw;
        this.freeRider = freeRider;
        this.snapshot = snapshot;
        this.play = play;
    }

    public JoinGVod(int utility, boolean seed, boolean play) {
        this.utility = utility;
        this.seed = seed;
        this.play = play;
    }

    public boolean isPlay() {
        return play;
    }


    public int getUtility() {
        return utility;
    }


    public boolean isSeed() {
        return seed;
    }

    public long getDownloadBw() {
        return downloadBw;
    }

    public long getUploadBw() {
        return uploadBw;
    }

    public boolean isFreeRider() {
        return freeRider;
    }

    public Snapshot getSnapshot() {
        return snapshot;
    }
}
