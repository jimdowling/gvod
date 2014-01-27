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
package se.sics.gvod.simulator.vod;

import se.sics.gvod.system.vod.VodConfiguration;
import se.sics.gvod.config.BootstrapConfiguration;
import java.io.IOException;
import org.apache.log4j.PropertyConfigurator;

import se.sics.kompics.Component;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Kompics;
import se.sics.gvod.bootstrap.server.BootstrapServerInit;
import se.sics.gvod.bootstrap.server.BootstrapServerMysql;
import se.sics.gvod.config.VodConfig;
import se.sics.gvod.config.CroupierConfiguration;
import se.sics.gvod.filters.MsgDestFilterOverlayId;
import se.sics.gvod.config.GradientConfiguration;
import se.sics.gvod.net.VodNetwork;
import se.sics.gvod.network.model.king.KingLatencyMap;
import se.sics.gvod.web.server.VodMonitorServer;
import se.sics.gvod.web.server.VodMonitorConfiguration;
import se.sics.gvod.web.server.VodMonitorServerInit;
import se.sics.kompics.p2p.experiment.dsl.SimulationScenario;
import se.sics.kompics.simulation.SimulatorScheduler;
import se.sics.gvod.timer.Timer;
import se.sics.gvod.p2p.simulator.P2pSimulator;
import se.sics.gvod.p2p.simulator.P2pSimulatorInit;

/**
 * The <code>GVodSimulationMain</code> class.
 * 
 * @author Cosmin Arad <cosmin@sics.se>
 */
public final class VodSimulationMain extends ComponentDefinition {

    static int seed;
    static {
        PropertyConfigurator.configureAndWatch("log4j.properties");
    }
    private static SimulatorScheduler simulatorScheduler = new SimulatorScheduler();
    private static SimulationScenario scenario = SimulationScenario.load(System.getProperty("scenario"));
    
    public static void main(String[] args) {
        seed = Integer.parseInt(System.getProperty("seed"));
        Kompics.setScheduler(simulatorScheduler);
        Kompics.createAndStart(VodSimulationMain.class, 0);
    }

    public VodSimulationMain() throws InterruptedException, IOException {
        P2pSimulator.setSimulationPortType(VodExperiment.class);

        // create
        Component p2pSimulator = create(P2pSimulator.class);
        Component bootstrapServer = create(BootstrapServerMysql.class);
        Component monitorServer = create(VodMonitorServer.class);
        Component gvodSimulator = create(VodSimulator.class);

        // loading component configurations
        final BootstrapConfiguration bootConfiguration = 
                (BootstrapConfiguration)
                BootstrapConfiguration.load(BootstrapConfiguration.class);
        final VodMonitorConfiguration monitorConfiguration = VodMonitorConfiguration.load(System.getProperty("gvod.monitor.configuration"));
        final CroupierConfiguration croupierConfiguration = 
                (CroupierConfiguration)
                CroupierConfiguration.load(CroupierConfiguration.class);
        final GradientConfiguration gradientConfiguration = 
                (GradientConfiguration)
                GradientConfiguration.load(GradientConfiguration.class);
        final VodConfiguration gvodConfiguration = (VodConfiguration)
                VodConfiguration.load(VodConfiguration.class);
        
        trigger(new P2pSimulatorInit(simulatorScheduler, scenario,
                new KingLatencyMap(seed)/*new Latency(50,20,seed)*/), p2pSimulator.getControl());
        trigger(new BootstrapServerInit(bootConfiguration,false), bootstrapServer.getControl());
        trigger(new VodMonitorServerInit(monitorConfiguration), monitorServer.getControl());
        trigger(
                new VodSimulatorInit(bootConfiguration,
                monitorConfiguration, croupierConfiguration, gradientConfiguration, 
                        gvodConfiguration
                ), gvodSimulator.getControl());


        // connect
        connect(bootstrapServer.getNegative(VodNetwork.class), p2pSimulator.getPositive(VodNetwork.class), 
                new MsgDestFilterOverlayId(VodConfig.SYSTEM_OVERLAY_ID));
        connect(bootstrapServer.getNegative(Timer.class), p2pSimulator.getPositive(Timer.class));

        connect(monitorServer.getNegative(VodNetwork.class), p2pSimulator.getPositive(VodNetwork.class), 
                new MsgDestFilterOverlayId(VodConfig.SYSTEM_OVERLAY_ID));
//                new MsgDestFilterOverlayId(monitorConfiguration.getMonitorServerAddress()));
        connect(monitorServer.getNegative(Timer.class), p2pSimulator.getPositive(Timer.class));

        connect(gvodSimulator.getNegative(VodNetwork.class), p2pSimulator.getPositive(VodNetwork.class));
        connect(gvodSimulator.getNegative(Timer.class), p2pSimulator.getPositive(Timer.class));
        connect(gvodSimulator.getNegative(VodExperiment.class),
                p2pSimulator.getPositive(VodExperiment.class));
    }
}
