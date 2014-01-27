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

import se.sics.gvod.system.vod.VodConfiguration;
import se.sics.gvod.config.BootstrapConfiguration;
import se.sics.kompics.Init;
import se.sics.gvod.config.CroupierConfiguration;
import se.sics.gvod.config.GradientConfiguration;
import se.sics.gvod.web.server.VodMonitorConfiguration;

/**
 * 
 * @author gautier
 */
public final class VodSimulatorInit extends Init {

    private final BootstrapConfiguration bootstrapConfiguration;
    private final VodMonitorConfiguration monitorConfiguration;
    private final CroupierConfiguration croupierConfiguration;
    private final GradientConfiguration gradientConfiguration;
    private final VodConfiguration gvodConfiguration;
//    private final Address peer0Address;

    public VodSimulatorInit(BootstrapConfiguration bootstrapConfiguration,
            VodMonitorConfiguration monitorConfiguration,
            CroupierConfiguration croupierConfiguration,
            GradientConfiguration gradientConfiguration,
            VodConfiguration gvodConfiguration) {
        super();
        this.bootstrapConfiguration = bootstrapConfiguration;
        this.monitorConfiguration = monitorConfiguration;
        this.croupierConfiguration = croupierConfiguration;
        this.gradientConfiguration = gradientConfiguration;
        this.gvodConfiguration = gvodConfiguration;
    }

    public BootstrapConfiguration getBootstrapConfiguration() {
        return bootstrapConfiguration;
    }

    public VodMonitorConfiguration getMonitorConfiguration() {
        return monitorConfiguration;
    }

    public VodConfiguration getGVodConfiguration() {
        return gvodConfiguration;
    }

    public CroupierConfiguration getCroupierConfiguration() {
        return croupierConfiguration;
    }

    public GradientConfiguration getGradientConfiguration() {
        return gradientConfiguration;
    }

}