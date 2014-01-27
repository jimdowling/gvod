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


import se.sics.gvod.common.Self;
import se.sics.gvod.system.main.GMain;
import se.sics.gvod.system.storage.Storage;
import se.sics.kompics.Init;
import se.sics.gvod.system.vod.snapshot.Snapshot;
import se.sics.gvod.web.server.VodMonitorConfiguration;
/**
 * 
 * @author gautier
 */
public final class VodInit extends Init {
   
    private final Self self;
    private final VodConfiguration config;
    private final int asn;
    private final int numPieces;
    private final boolean simulation;
    private final boolean simuBW;
    private final VodMonitorConfiguration vodMonitorConfiguration;

    private final int utility;
    private final boolean seed;
    private final long downloadBw; // in bytes per second
    private final long uploadBw; // in bytes per second
    private final boolean freeRider;
    private final Snapshot snapshot;
    private final String torrentFileAddress;
    private final GMain main;
    private final boolean play;
    private final Storage storage;
    
    
    public VodInit(Self self, VodConfiguration configuration,
            VodMonitorConfiguration mConfig, int numPieces, int asn,
            boolean simulation,
            boolean simuBW, 
            GMain main, int utility,            
            boolean seed, long downloadBw, long uploadBw, boolean freeRider,
            Snapshot snapshot, String torrentFileAddress, boolean play,
            Storage storage
            ){
        this.self = self;
        this.config=configuration;
        this.numPieces = numPieces;
        this.asn = asn;
        this.simulation = simulation;
        this.simuBW = simuBW;
        this.vodMonitorConfiguration = mConfig;
        this.main = main;
        this.utility = utility;
        this.seed = seed;
        this.downloadBw = downloadBw;
        this.uploadBw = uploadBw;
        this.freeRider = freeRider;
        this.snapshot = snapshot;
        this.torrentFileAddress = torrentFileAddress;
        this.play = play;
        this.storage = storage;
    }

    public Storage getStorage() {
        return storage;
    }

    
    public Self getSelf() {
        return self;
    }

    public int getNumPieces() {
        return numPieces;
    }
    
    public int getAsn() {
        return asn;
    }
    
    public boolean isSimuBW() {
        return simuBW;
    }

    public boolean isSimulation() {
        return simulation;
    }
    
    public VodConfiguration getConfig() {
        return config;
    }

    public VodMonitorConfiguration getVodMonitorConfiguration() {
        return vodMonitorConfiguration;
    }

    
    public boolean isPlay() {
        return play;
    }

    public GMain getMain() {
        return main;
    }

    public String getTorrentFileAddress() {
        return torrentFileAddress;
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