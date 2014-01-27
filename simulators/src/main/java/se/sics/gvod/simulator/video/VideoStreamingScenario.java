package se.sics.gvod.simulator.video;

import java.util.Random;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.simulator.common.PeerJoin;
import se.sics.gvod.simulator.common.StartCollectData;
import se.sics.gvod.simulator.common.StopCollectData;
import se.sics.gvod.simulator.video.scenarios.VideoScenario;
import se.sics.kompics.p2p.experiment.dsl.SimulationScenario;
import se.sics.kompics.p2p.experiment.dsl.adaptor.Operation;
import se.sics.kompics.p2p.experiment.dsl.adaptor.Operation1;

/**
 *
 * @author Niklas Wahl&#233;n <nwahlen@kth.se>
 */
@SuppressWarnings("serial")
public class VideoStreamingScenario extends VideoScenario {

    public static final int SOURCE_NODES = 1;
    private static SimulationScenario scenario = new SimulationScenario() {

        {

            SimulationScenario.StochasticProcess firstNodesJoin = new SimulationScenario.StochasticProcess() {

                {
                    eventInterArrivalTime(constant(10));
                    raise(FIRST, Operations.videoPeerJoin(VodAddress.NatType.OPEN),
                            uniform(0, 10000));
                }
            };

            SimulationScenario.StochasticProcess sourceNodeJoin = new SimulationScenario.StochasticProcess() {

                {
                    eventInterArrivalTime(constant(1));
                    raise(SOURCE_NODES, Operations.sourceJoin(VodAddress.NatType.OPEN),
                            uniform(0, 10000));
                }
            };

            SimulationScenario.StochasticProcess nodesJoin1 = new SimulationScenario.StochasticProcess() {

                {
                    eventInterArrivalTime(exponential(10));
                    raise(VideoScenario.PUBLIC, Operations.videoPeerJoin(VodAddress.NatType.OPEN),
                            uniform(0, 10000));
                    if (VideoScenario.PRIVATE > 0) {
                        raise(VideoScenario.PRIVATE, Operations.videoPeerJoin(VodAddress.NatType.NAT),
                                uniform(0, 10000));
                    }
                }
            };

            SimulationScenario.StochasticProcess startCollectData = new SimulationScenario.StochasticProcess() {

                {
                    eventInterArrivalTime(constant(1000));
                    raise(VideoScenario.COLLECT_VIDEO_RESULTS, Operations.startCollectData());
                }
            };

            SimulationScenario.StochasticProcess stopCollectData = new SimulationScenario.StochasticProcess() {

                {
                    eventInterArrivalTime(exponential(10));
                    raise(1, Operations.stopCollectData());
                }
            };

            // Some nodes has to start initially (for Croupier)
            firstNodesJoin.start();
            sourceNodeJoin.startAfterTerminationOf(5000, firstNodesJoin);
            nodesJoin1.startAfterTerminationOf(15000, firstNodesJoin);
            startCollectData.startAfterTerminationOf(1000, nodesJoin1);
            stopCollectData.startAfterTerminationOf(15 * 1000, startCollectData);
        }
    };

//-------------------------------------------------------------------
    public VideoStreamingScenario() {
        super(scenario);
    }

    public static class Operations {

        private static int index = 0;
        private static Random rnd = new Random();

        static Operation1<SourceJoin, Long> sourceJoin(final VodAddress.NatType peerType) {
            return new Operation1<SourceJoin, Long>() {

                @Override
                public SourceJoin generate(Long id) {
                    return new SourceJoin(id.intValue(), peerType, SourceJoin.NO_OPTION);
                }
            };
        }

        static Operation1<PeerJoin, Long> videoPeerJoin(final VodAddress.NatType peerType) {
            return new Operation1<PeerJoin, Long>() {

                @Override
                public PeerJoin generate(Long id) {
                    return new PeerJoin(id.intValue(), peerType);
                }
            };
        }

        static Operation1<PeerJoin, Long> videoPeerJoin(final double privateNodesRatio) {
            return new Operation1<PeerJoin, Long>() {

                @Override
                public PeerJoin generate(Long id) {
                    VodAddress.NatType peerType;
                    index++;

                    if (rnd.nextDouble() < privateNodesRatio) {
                        peerType = VodAddress.NatType.NAT;
                    } else {
                        peerType = VodAddress.NatType.OPEN;
                    }

                    return new PeerJoin(id.intValue(), peerType);
                }
            };
        }

        static Operation<StartCollectData> startCollectData() {
            return new Operation<StartCollectData>() {

                @Override
                public StartCollectData generate() {
                    return new StartCollectData();
                }
            };
        }

        static Operation<StopCollectData> stopCollectData() {

            return new Operation<StopCollectData>() {

                @Override
                public StopCollectData generate() {
                    return new StopCollectData();
                }
            };
        }
    }
}
