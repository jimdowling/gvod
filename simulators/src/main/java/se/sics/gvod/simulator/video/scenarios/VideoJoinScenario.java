package se.sics.gvod.simulator.video.scenarios;

import se.sics.gvod.ls.system.LSConfig;
import se.sics.gvod.net.VodAddress;
import se.sics.kompics.p2p.experiment.dsl.SimulationScenario;

/**
 *
 * @author Niklas Wahl&#233;n <nwahlen@kth.se>
 */
public class VideoJoinScenario extends VideoScenario {

    public static final int SOURCE_NODES = 1;
    

    private static SimulationScenario scenario = new SimulationScenario() {

        {

            SimulationScenario.StochasticProcess publicNodesJoin = new SimulationScenario.StochasticProcess() {

                {
                    eventInterArrivalTime(constant(10));
                    if(PUBLIC > 0) {
                        raise(PUBLIC, VideoOperations.Operations.videoPeerJoin(VodAddress.NatType.OPEN),
                            uniform(0, 10000));
                    }
                }
            };
            
            SimulationScenario.StochasticProcess privateNodesJoin = new SimulationScenario.StochasticProcess() {

                {
                    eventInterArrivalTime(constant(10));
                    if(PRIVATE > 0) {
                        raise(PRIVATE, VideoOperations.Operations.videoPeerJoin(VodAddress.NatType.NAT),
                            uniform(0, 10000));
                    }
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

            SimulationScenario.StochasticProcess stopCollectData = new SimulationScenario.StochasticProcess() {

                {
                    eventInterArrivalTime(exponential(10));
                    raise(1, VideoOperations.Operations.stopCollectData());
                }
            };

            // Some nodes has to start initially (for Croupier)
            publicNodesJoin.start();
            if(PRIVATE > 0) {
                privateNodesJoin.startAfterTerminationOf(1000, publicNodesJoin);
                sourceNodeJoin.startAfterTerminationOf(5000, privateNodesJoin);
            } else {
                sourceNodeJoin.startAfterTerminationOf(5000, publicNodesJoin);
            }
            startCollectData.startAfterTerminationOf(1000, sourceNodeJoin);
            stopCollectData.startAfterTerminationOf(10 * 1000, startCollectData);
            
//            firstNodesJoin.start();
//            sourceNodeJoin.startAfterTerminationOf(5000, firstNodesJoin);
//            startCollectData.startAfterTerminationOf(1000, sourceNodeJoin);
//            stopCollectData.startAfterTerminationOf(10 * 1000, startCollectData);
        }
    };

//-------------------------------------------------------------------
    public VideoJoinScenario() {
        super(scenario);
    }
}
