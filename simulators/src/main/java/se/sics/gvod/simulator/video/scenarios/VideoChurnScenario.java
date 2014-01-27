package se.sics.gvod.simulator.video.scenarios;

import se.sics.gvod.ls.system.LSConfig;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.net.VodAddress.NatType;
import se.sics.kompics.p2p.experiment.dsl.SimulationScenario;

/**
 *
 * @author Niklas Wahl&#233;n <nwahlen@kth.se>
 */
public class VideoChurnScenario extends VideoScenario {

    public static int CHURN_ROUNDS = 10;
    public static int CHURN_PER_ROUND = 2;
    public static int CHURN_START_ROUND = 50;
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

            SimulationScenario.StochasticProcess startChurn = new SimulationScenario.StochasticProcess() {

                private int CHURN_RATE = 5000 / CHURN_PER_ROUND;
                private int TOTAL_CHURN_EVENTS = CHURN_ROUNDS * CHURN_PER_ROUND;

                {
                    eventInterArrivalTime(constant(CHURN_RATE));
                    raise(TOTAL_CHURN_EVENTS, VideoOperations.Operations.videoPeerFail(NatType.OPEN),
                            uniform(0, 10000));
                    raise(TOTAL_CHURN_EVENTS, VideoOperations.Operations.videoPeerJoin(NatType.OPEN),
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
            startChurn.startAfterTerminationOf(CHURN_START_ROUND * 5000, sourceNodeJoin);
            stopCollectData.startAfterTerminationOf(10 * 1000, startCollectData);
        }
    };

//-------------------------------------------------------------------
    public VideoChurnScenario() {
        super(scenario);
    }
}
