/**
 * This file is part of the Kompics P2P Framework.
 *
 * Copyright (C) 2009 Swedish Institute of Computer Science (SICS)
 * Copyright (C) 2009 Royal Institute of Technology (KTH)
 *
 * Kompics is free software; you can redistribute it and/or
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

import se.sics.kompics.p2p.experiment.dsl.distribution.Distribution;
import java.util.Random;

public final class MyDistribution extends Distribution<BW> {

    /**
     *
     */
    private static final long serialVersionUID = 6665221714177817925L;
    private final Random random;
    private final long KBps = 1024;

    public MyDistribution(long seed) {
        super(Type.OTHER, BW.class);
        random = new Random(seed);
    }
    int i = 0;
    int tot = 0;

    @Override
    public BW draw() {
        int val = random.nextInt(100);
        if (val < 20) {
            return new BW(5 * 128 * KBps, 320 * KBps);
        } else if (val < 70) {
            return new BW(2 * 128 * KBps, 128 * KBps); //try with 256
        } else {
            return new BW(192 * KBps, 96 * KBps);
        }

    }
}
