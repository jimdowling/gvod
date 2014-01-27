package se.sics.gvod.simulator.video.scenarios;

import java.util.Random;
import se.sics.gvod.ls.system.LSConfig;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.simulator.common.PeerFail;
import se.sics.gvod.simulator.common.PeerJoin;
import se.sics.gvod.simulator.common.StartCollectData;
import se.sics.gvod.simulator.common.StopCollectData;
import se.sics.gvod.simulator.video.SourceJoin;
import se.sics.kompics.p2p.experiment.dsl.adaptor.Operation;
import se.sics.kompics.p2p.experiment.dsl.adaptor.Operation1;

/**
 *
 * @author Niklas Wahl&#233;n <nwahlen@kth.se>
 */
public class VideoOperations {
    
    public static class Operations {

        private static int index = 0;
        private static Random rnd = new Random(LSConfig.getSeed());

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
        
        static Operation1<PeerFail, Long> videoPeerFail(final VodAddress.NatType peerType) {
        return new Operation1<PeerFail, Long>() {

            @Override
            public PeerFail generate(Long id) {
                return new PeerFail(id.intValue(), peerType);
            }
        };
    }

    static Operation1<PeerFail, Long> videoPeerFail(final double privateNodesRatio) {
        return new Operation1<PeerFail, Long>() {

            @Override
            public PeerFail generate(Long id) {
                VodAddress.NatType peerType;
                index++;

                if (rnd.nextDouble() < privateNodesRatio) {
                    peerType = VodAddress.NatType.NAT;
                } else {
                    peerType = VodAddress.NatType.OPEN;
                }

                return new PeerFail(id.intValue(), peerType);
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
