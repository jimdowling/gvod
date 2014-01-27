package se.sics.gvod.simulator.video;

import java.io.IOException;
import java.lang.String;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.PropertyConfigurator;
import se.sics.gvod.config.CroupierConfiguration;
import se.sics.gvod.ls.system.LSConfig;
import se.sics.gvod.config.VideoConfiguration;
import se.sics.gvod.net.VodNetwork;
import se.sics.gvod.network.model.king.KingLatencyMap;
import se.sics.gvod.p2p.simulator.P2pSimulator;
import se.sics.gvod.p2p.simulator.P2pSimulatorInit;
import se.sics.gvod.config.ParentMakerConfiguration;
import se.sics.gvod.config.InterAsConfiguration;
import se.sics.gvod.timer.Timer;
import se.sics.kompics.Component;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Kompics;
import se.sics.kompics.p2p.experiment.dsl.SimulationScenario;
import se.sics.kompics.simulation.SimulatorScheduler;

public final class VideoSimulationMain extends ComponentDefinition {

    static {
        PropertyConfigurator.configureAndWatch("log4j.properties");
    }
    private static SimulatorScheduler simulatorScheduler = new SimulatorScheduler();
    private static SimulationScenario scenario = SimulationScenario.load(System.getProperty("scenario"));

    public static void main(String[] args) {
        Kompics.setScheduler(simulatorScheduler);
        Kompics.createAndStart(VideoSimulationMain.class, 1);
    }

    public VideoSimulationMain() throws IOException {
        P2pSimulator.setSimulationPortType(VideoSimulatorPort.class);


//                VodConfig.init(new String[] {});

        // loading component configurations
        final CroupierConfiguration croupierConfiguration =
                (CroupierConfiguration)
                CroupierConfiguration.load(CroupierConfiguration.class);
        final ParentMakerConfiguration parentMakerConfiguration =
                (ParentMakerConfiguration)
                ParentMakerConfiguration.load(ParentMakerConfiguration.class);
        final InterAsConfiguration interAsConfiguration =
                (InterAsConfiguration)
                InterAsConfiguration.load(InterAsConfiguration.class);
        final VideoConfiguration videoConfiguration = 
                (VideoConfiguration)
                VideoConfiguration.load(VideoConfiguration.class);

        List<String> args = new ArrayList<String>();
        args.add("-sim");
        if (videoConfiguration.isInputFilenameSet()) {
            args.add("-i");
            args.add(videoConfiguration.getInputFilename());
        }
        if (videoConfiguration.isOutputFilenameSet()) {
            args.add("-o");
            args.add(videoConfiguration.getOutputFilename());
        }
        if (videoConfiguration.isSourceUrlSet()) {
            args.add("-s");
            args.add(videoConfiguration.getSourceUrl());
        }
        if (videoConfiguration.isDestUrlSet()) {
            args.add("-d");
            args.add(videoConfiguration.getDestIp() + ":" + videoConfiguration.getDestPort());
        }
        if (videoConfiguration.isMonitorSet()) {
            args.add("-m");
            args.add(videoConfiguration.getMonitorServerUrl());
            args.add("-eid");
            args.add("" + videoConfiguration.getExperimentId());
            args.add("-eit");
            args.add("" + videoConfiguration.getExperimentIteration());
        }
        LSConfig.init(args.toArray(new String[]{}));

        // create
        Component p2pSimulator = create(P2pSimulator.class);
        Component VideoSimulator = create(VideoSimulator.class);

        trigger(new P2pSimulatorInit(simulatorScheduler, scenario,
                new KingLatencyMap(croupierConfiguration.getSeed())), p2pSimulator.getControl());
        trigger(new VideoSimulatorInit(croupierConfiguration,
                parentMakerConfiguration, interAsConfiguration, videoConfiguration), VideoSimulator.getControl());

        connect(VideoSimulator.getNegative(VodNetwork.class), p2pSimulator.getPositive(VodNetwork.class));
        connect(VideoSimulator.getNegative(Timer.class), p2pSimulator.getPositive(Timer.class));
        connect(VideoSimulator.getNegative(VideoSimulatorPort.class), p2pSimulator.getPositive(VideoSimulatorPort.class));
    }
}
