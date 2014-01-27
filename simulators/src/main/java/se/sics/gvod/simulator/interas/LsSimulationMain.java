package se.sics.gvod.simulator.interas;

import se.sics.gvod.config.InterAsConfiguration;
import se.sics.gvod.simulator.common.SimulatorPort;
import java.io.IOException;

import org.apache.log4j.PropertyConfigurator;
import se.sics.gvod.config.AbstractConfiguration;

import se.sics.gvod.config.CroupierConfiguration;
import se.sics.gvod.ls.system.LSConfig;
import se.sics.gvod.net.VodNetwork;
import se.sics.gvod.network.model.king.KingLatencyMap;
import se.sics.gvod.p2p.simulator.P2pSimulator;
import se.sics.gvod.p2p.simulator.P2pSimulatorInit;
import se.sics.gvod.config.ParentMakerConfiguration;
import se.sics.gvod.timer.Timer;
import se.sics.kompics.Component;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Kompics;
import se.sics.kompics.p2p.experiment.dsl.SimulationScenario;
import se.sics.kompics.simulation.SimulatorScheduler;

public final class LsSimulationMain extends ComponentDefinition {

    static {
        PropertyConfigurator.configureAndWatch("log4j.properties");
    }
    private static SimulatorScheduler simulatorScheduler = new SimulatorScheduler();
    private static SimulationScenario scenario = SimulationScenario.load(System.getProperty("scenario"));

    public static void main(String[] args) {
        Kompics.setScheduler(simulatorScheduler);
        Kompics.createAndStart(LsSimulationMain.class, 1);
    }

    public LsSimulationMain() throws IOException {
        P2pSimulator.setSimulationPortType(SimulatorPort.class);



        // loading component configurations
        final CroupierConfiguration croupierConfiguration =
                (CroupierConfiguration)
                CroupierConfiguration.load(CroupierConfiguration.class);
        final ParentMakerConfiguration parentMakerConfiguration =
                (ParentMakerConfiguration)
                AbstractConfiguration.load(ParentMakerConfiguration.class);
        final InterAsConfiguration interAsConfiguration =
                (InterAsConfiguration)
                InterAsConfiguration.load(InterAsConfiguration.class);

        LSConfig.init(new String[]{"-seed", "" + interAsConfiguration.getSeed()});

        // create
        Component p2pSimulator = create(P2pSimulator.class);
        Component LsSimulator = create(LsSimulator.class);

        trigger(new P2pSimulatorInit(simulatorScheduler, scenario,
                new KingLatencyMap(croupierConfiguration.getSeed())), p2pSimulator.getControl());
        trigger(new LsSimulatorInit(croupierConfiguration,
                parentMakerConfiguration, interAsConfiguration), LsSimulator.getControl());

        connect(LsSimulator.getNegative(VodNetwork.class), p2pSimulator.getPositive(VodNetwork.class));
        connect(LsSimulator.getNegative(Timer.class), p2pSimulator.getPositive(Timer.class));
        connect(LsSimulator.getNegative(SimulatorPort.class), p2pSimulator.getPositive(SimulatorPort.class));
    }
}
