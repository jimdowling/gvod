/**
 * This file is part of the ID2210 course assignments kit.
 *
 * Copyright (C) 2009 Royal Institute of Technology (KTH)
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package se.sics.gvod.simulator;

import com.google.common.collect.Range;
import java.util.Random;
import se.sics.gvod.simulator.msg.VodPeerJoin;
import se.sics.gvod.simulator.util.DistributionFactory;
import se.sics.kompics.p2p.experiment.dsl.SimulationScenario;
import se.sics.kompics.p2p.experiment.dsl.adaptor.Operation1;
import se.sics.kompics.p2p.experiment.dsl.distribution.Distribution;

@SuppressWarnings("serial")
public class Main {

    private static Random random;
    private static int seed;
    static long KBps = 1024;

    public static void main(String[] args) throws Throwable {

        if (args.length != 4) {
            throw new RuntimeException("usage: <prog> seed seeders leechers videoName");
        }

        seed = Integer.parseInt(args[0]);
        random = new Random(seed);
        final int nrSeeders = Integer.parseInt(args[1]);
        final int nrLeechers = Integer.parseInt(args[2]);
        final String videoName = args[3];

        SimulationScenario gvodScenario1 = new SimulationScenario() {

            {
                final Distribution<Integer> idDist = DistributionFactory.uniformInteger(random, Range.closed(0, 65535));

                StochasticProcess joinSeedersProc = new StochasticProcess() {

                    {
                        eventInterArrivalTime(constant(1000));
                        raise(nrSeeders, joinSeeder, idDist);
                    }
                };
                StochasticProcess joinLeechersProc = new StochasticProcess() {

                    {
                        eventInterArrivalTime(constant(1000));
                        raise(nrLeechers, joinLeecher, idDist);
                    }
                };

                joinSeedersProc.start();
                joinLeechersProc.startAfterTerminationOf(1000, joinSeedersProc);

            }
        };
//        gvodScenario1.simulate(VodSimulationMain.class);
    }
    // operations
    static Operation1<VodPeerJoin, Integer> joinSeeder
            = new Operation1<VodPeerJoin, Integer>() {

                @Override
                public VodPeerJoin generate(Integer id) {
                    return new VodPeerJoin(id);
                }
            };
    static Operation1<VodPeerJoin, Integer> joinLeecher
            = new Operation1<VodPeerJoin, Integer>() {

                @Override
                public VodPeerJoin generate(Integer id) {
                    return new VodPeerJoin(id);
                }
            };
}
