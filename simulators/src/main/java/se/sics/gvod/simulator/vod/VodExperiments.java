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



import se.sics.gvod.config.VodCompositeConfiguration;
import se.sics.gvod.simulator.common.StartDataCollection;
import se.sics.gvod.simulator.common.StopDataCollection;
import se.sics.kompics.p2p.experiment.dsl.SimulationScenario;
import se.sics.kompics.p2p.experiment.dsl.adaptor.Operation1;
import se.sics.kompics.p2p.experiment.dsl.adaptor.Operation3;
import java.util.Random;
import se.sics.gvod.net.Nat;
import se.sics.kompics.p2p.experiment.dsl.adaptor.Operation;
import se.sics.kompics.p2p.experiment.dsl.distribution.Distribution;
import se.sics.kompics.p2p.experiment.dsl.adaptor.Operation5;

/**
 * The <code>CyclonExperiments</code> class.
 *
 * @author Cosmin Arad <cosmin@sics.se>
 */
@SuppressWarnings("serial")
public class VodExperiments {

    static Random random = new Random(0);
    private static int seed;
    static long KBps = 1024;

    public static void main(String[] args) throws Throwable {
        
        if (args.length != 2) {
            System.out.println("usage: <prog> seedd numNodes");
        }
        
        seed = Integer.parseInt(args[0]);
        random = new Random(seed);
        final int nbNodes = Integer.parseInt(args[1]);
        SimulationScenario gvodScenario1 = new SimulationScenario() {

            {

                StochasticProcess creatSeed = new StochasticProcess() {

                    {
                        eventInterArrivalTime(constant(1000));
                        raise(1, gvodSeedJoin, 
                                uniformInt(65535),
//                                uniform(13),
                                constant(5 * 128 * KBps) /* download */,
                                constant(320 * KBps) /* upload */,
                                uniformInt(0),
                                uniformInt(1)
                                );
                    }
                };
                StochasticProcess process2 = new StochasticProcess() {

                    {
                        eventInterArrivalTime(new PoissonDistribution(new Long(1000), random));
                        raise(nbNodes, gvodJoin1, uniformInt(65535), myDistrib(), 
                                uniformInt(0));
                    }
                };
                StochasticProcess process3 = new StochasticProcess() {

                    {
                        eventInterArrivalTime(exponential(10));
                        raise(100, gvodJoin1, uniformInt(65535),
                                myDistrib(), uniformInt(0));
                    }
                };

                StochasticProcess process4 = new StochasticProcess() {

                    {
                        eventInterArrivalTime(constant(1) );
                        raise(nbNodes * 50 / 100, gvodFail, uniformInt(65535));
                    }
                };

                StochasticProcess process5 = new StochasticProcess() {

                    {
                        eventInterArrivalTime(exponential(50));
                        raise(nbNodes * 10 / 100 + 1, gvodJoin1, uniformInt(65535),
                                myDistrib(), uniformInt(0));
                        raise(10, gvodQuit, uniformInt(65535));
                    }
                };

                StochasticProcess process6 = new StochasticProcess() {

                    {
                        eventInterArrivalTime(exponential(20000));
                        raise(nbNodes * 10 / 100 + 1, gvodJumpForward, uniformInt(65535));
                    }
                };

                StochasticProcess process7 = new StochasticProcess() {

                    {
                        eventInterArrivalTime(constant(0));
                        raise(1, startMesurs);
                    }
                };
                StochasticProcess process8 = new StochasticProcess() {

                    {
                        eventInterArrivalTime(constant(0));
                        raise(1, stopMesurs);
                    }
                };
                creatSeed.start();

//                process2.startAfterTerminationOf(10000, process1);
                process7.startAfterTerminationOf(1, creatSeed);
                process2.startAfterTerminationOf(1000, creatSeed);
//                process7.startAfterStartOf(250 * 1000, process2);
//                 process8.startAfterStartOf(60*60*1000, process2);
                process8.startAfterStartOf(750 * 1000 + 10, process2);
//                  process3.startAfterTerminationOf(1000, process2);
//                process4.startAfterTerminationOf(8*60*1000, process2);
                //  process5.startAtSameTimeWith(process4);
//                process6.startAfterTerminationOf(300000, creatSeed);
                //terminateAfterTerminationOf(1000, process2);
            }
        };
        VodCompositeConfiguration configuration = new VodCompositeConfiguration();
//        configuration.set(args[1], args[2], args[4]);
        configuration.store();

        gvodScenario1.setSeed(seed);
//        gvodScenario1.execute(GVodExecutionMain.class);
        gvodScenario1.simulate(VodSimulationMain.class);
    }
    // operations
    static Operation3<VodPeerJoin, Integer, BW, Integer> gvodJoin1 = 
            new Operation3<VodPeerJoin, Integer, BW, Integer>() {

        @Override
        public VodPeerJoin generate(Integer id, BW bw, Integer overlayId) {
            
            // TODO - remove this hack
            overlayId += 10;
            // TODO - generate these distributions
            Integer natType = 0;
            int mp=1, fp=1, ap=1;
            Nat nat;
            if (natType == 0) {
                nat = new Nat(Nat.Type.OPEN);
            } else {
                nat = new Nat(Nat.Type.NAT, 
                        Nat.MappingPolicy.values()[mp],
                        Nat.AllocationPolicy.values()[ap],
                        Nat.FilteringPolicy.values()[fp],
                        1, 
                        (long) Nat.DEFAULT_RULE_EXPIRATION_TIME
                        );
            }
            
            return new VodPeerJoin(id, 0, false, bw.getDownloadBw(), bw.getUploadBw(),
                    nat, overlayId);
        }
    };
    static Operation5<VodPeerJoin, Integer, Long, Long, Integer, Integer> gvodSeedJoin = 
            new Operation5<VodPeerJoin, Integer, Long, Long, Integer, Integer>() {

        @Override
        public VodPeerJoin generate(Integer id, Long downloadBw,
                Long uploadBw, Integer overlayId, Integer fp) {
            
            // TODO - remove this hack
            overlayId += 10;
            // TODO - generate these distributions
            Integer natType = 0;
            int mp=1, ap=1;
            Nat nat;
            if (natType == 0) {
                nat = new Nat(Nat.Type.OPEN);
            } else {
                nat = new Nat(Nat.Type.NAT, 
                        Nat.MappingPolicy.values()[mp],
                        Nat.AllocationPolicy.values()[ap],
                        Nat.FilteringPolicy.values()[fp],
                        1, 
                        (long) Nat.DEFAULT_RULE_EXPIRATION_TIME
                        );
            }
            
            return new VodPeerJoin(id, 100, false, downloadBw, uploadBw,
                    nat, overlayId);            
        }
    };
    static Operation1<VodPeerQuit, Integer> gvodQuit = new Operation1<VodPeerQuit, Integer>() {

        @Override
        public VodPeerQuit generate(Integer id) {
            return new VodPeerQuit(id);
        }
    };
    static Operation1<VodPeerFail, Integer> gvodFail = new Operation1<VodPeerFail, Integer>() {

        @Override
        public VodPeerFail generate(Integer id) {
            return new VodPeerFail(id);
        }
    };
    static Operation1<VodChangeUtility, Integer> gvodChangeUtility = new Operation1<VodChangeUtility, Integer>() {

        @Override
        public VodChangeUtility generate(Integer id) {
            return new VodChangeUtility(id, random.nextInt(30));
        }
    };
    static Operation1<VodJumpForward, Integer> gvodJumpForward = new Operation1<VodJumpForward, Integer>() {

        @Override
        public VodJumpForward generate(Integer id) {
            return new VodJumpForward(id, 5);
        }
    };
    static Operation1<VodJumpBackward, Integer> gvodJumpBackward = new Operation1<VodJumpBackward, Integer>() {

        @Override
        public VodJumpBackward generate(Integer id) {
            return new VodJumpBackward(id, 10);
        }
    };
    static Operation<StartDataCollection> startMesurs = new Operation<StartDataCollection>() {

        @Override
        public StartDataCollection generate() {
            return new StartDataCollection();
        }
    };
    static Operation<StopDataCollection> stopMesurs = new Operation<StopDataCollection>() {

        @Override
        public StopDataCollection generate() {
            return new StopDataCollection();
        }
    };

    static Distribution<BW> myDistrib() {
        return new MyDistribution(seed);
    }

    static Distribution<Integer> uniformInt(int max) {
        return new IntegerUniformDistribution(0, max, new Random(seed));
    }
}
