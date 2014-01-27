package se.sics.gvod.simulator.video.scenarios;

import se.sics.gvod.net.VodAddress;
import se.sics.kompics.p2p.experiment.dsl.SimulationScenario;

/**
 *
 * @author Niklas Wahl&#233;n <nwahlen@kth.se>
 */
public class VideoCrashScenario extends VideoScenario {
    
    public static int NODES_TO_CRASH = 100;
    public static int CRASH_ROUND = 50;
    private static SimulationScenario scenario = new SimulationScenario() {

        {

            SimulationScenario.StochasticProcess firstNodesJoin = new SimulationScenario.StochasticProcess() {

                {
                    eventInterArrivalTime(constant(10));
                    raise(PUBLIC, VideoOperations.Operations.videoPeerJoin(VodAddress.NatType.OPEN),
                            uniform(0, 10000));
                }
            };

            SimulationScenario.StochasticProcess sourceNodeJoin = new SimulationScenario.StochasticProcess() {

                {
                    eventInterArrivalTime(constant(1));
                    raise(SOURCE_NODES, VideoOperations.Operations.sourceJoin(VodAddress.NatType.OPEN),
                            uniform(0, 10000));
                }
            };

            SimulationScenario.StochasticProcess startCollectData = new SimulationScenario.StochasticProcess() {

                {
                    eventInterArrivalTime(constant(1000));
                    raise(COLLECT_VIDEO_RESULTS, VideoOperations.Operations.startCollectData());
                }
            };

            SimulationScenario.StochasticProcess nodesCrash = new SimulationScenario.StochasticProcess() {

                {
                    eventInterArrivalTime(constant(1));
                    raise(NODES_TO_CRASH, VideoOperations.Operations.videoPeerFail(VodAddress.NatType.OPEN),
                            uniform(0, 10000));
                }
            };

            SimulationScenario.StochasticProcess stopCollectData = new SimulationScenario.StochasticProcess() {

                {
                    eventInterArrivalTime(exponential(10));
                    raise(1, VideoOperations.Operations.stopCollectData());
                }
            };

            // Some nodes has to start initially (for Croupier)
            firstNodesJoin.start();
            sourceNodeJoin.startAfterTerminationOf(5000, firstNodesJoin);
            startCollectData.startAfterTerminationOf(1000, sourceNodeJoin);
            nodesCrash.startAfterTerminationOf(CRASH_ROUND * 5000, sourceNodeJoin);
            stopCollectData.startAfterTerminationOf(10 * 1000, startCollectData);
        }
    };

//-------------------------------------------------------------------
    public VideoCrashScenario() {
        super(scenario);
    }
    
}
