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
package se.sics.gvod.system.peer;

import se.sics.gvod.common.Self;
import se.sics.gvod.config.BootstrapConfiguration;
import se.sics.gvod.config.CroupierConfiguration;
import se.sics.gvod.system.vod.VodConfiguration;
import se.sics.gvod.system.main.GMain;
import se.sics.gvod.system.storage.Storage;
import se.sics.gvod.system.vod.snapshot.Snapshot;
import se.sics.kompics.Init;
import se.sics.gvod.web.server.VodMonitorConfiguration;

public final class VodPeerInit extends Init {

    private final Self self;
    private final GMain main;
    private final BootstrapConfiguration bootstrapConfig;
    private final CroupierConfiguration croupierConfig;
    private final VodConfiguration vodConfig;
    private final VodMonitorConfiguration monitorConfig;
    private final boolean simulation;
    private final boolean seed;
    private final boolean simuBW;
    private final String torrentFileAddress;
    private final boolean withoutGUI;
    private final short mtu;
    private final int asn;

    private final int utility;
    private final boolean play;
    // in bytes per second
    private final long downloadBw;
    // in bytes per second
    private final long uploadBw;
    private final boolean freeRider;
    private final Snapshot snapshot;
    private final Storage storage;

    public VodPeerInit(Self self, GMain main,
            BootstrapConfiguration bootstrapConfig,
            CroupierConfiguration croupierConfig,
            VodConfiguration vodConfig,
            VodMonitorConfiguration monitorConfig,
            boolean simulation, boolean seed, boolean simuBW, String torrentFileAddress,
            boolean withoutGUI, short mtu, int asn,
            int utility, boolean play, long downloadBw, long uploadBw,
            boolean freeRider, Snapshot snapshot, Storage storage
    ) {
        super();
        this.self = self;
        this.main = main;
        this.bootstrapConfig = bootstrapConfig;
        this.croupierConfig = croupierConfig;
        this.vodConfig = vodConfig;
        this.monitorConfig = monitorConfig;
        this.simulation = simulation;
        this.seed = seed;
        this.simuBW = simuBW;
        this.torrentFileAddress = torrentFileAddress;
        this.withoutGUI = withoutGUI;
        this.mtu = mtu;
        this.asn = asn;
        this.utility = utility;
        this.play = play;
        this.downloadBw = downloadBw;
        this.uploadBw = uploadBw;
        this.freeRider = freeRider;
        this.snapshot = snapshot;
        this.storage = storage;
    }

    public Storage getStorage() {
        return storage;
    }

    public Self getSelf() {
        return self;
    }

    public short getMtu() {
        return mtu;
    }

    public int getAsn() {
        return asn;
    }

    public GMain getMain() {
        return main;
    }

    public boolean isWithoutGUI() {
        return withoutGUI;
    }

    public String getTorrentFileAddress() {
        return torrentFileAddress;
    }

    public boolean isSimuBW() {
        return simuBW;
    }

    public boolean isSimulation() {
        return simulation;
    }

    public CroupierConfiguration getCroupierConfig() {
        return croupierConfig;
    }

    public boolean isSeed() {
        return seed;
    }

    public BootstrapConfiguration getBootstrapConfig() {
        return bootstrapConfig;
    }

    public long getDownloadBw() {
        return downloadBw;
    }

    public VodMonitorConfiguration getMonitorConfig() {
        return monitorConfig;
    }

    public Snapshot getSnapshot() {
        return snapshot;
    }

    public long getUploadBw() {
        return uploadBw;
    }

    public int getUtility() {
        return utility;
    }

    public VodConfiguration getVodConfig() {
        return vodConfig;
    }

    public boolean isFreeRider() {
        return freeRider;
    }

    public boolean isPlay() {
        return play;
    }

}
