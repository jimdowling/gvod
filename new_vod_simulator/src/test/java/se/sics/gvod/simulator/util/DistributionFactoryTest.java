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
package se.sics.gvod.simulator.util;

import com.google.common.collect.Range;
import java.util.Random;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.ranges.RangeException;
import se.sics.kompics.p2p.experiment.dsl.distribution.Distribution;

/**
 * @author Alex Ormenisan
 */
public class DistributionFactoryTest {

    @Test
    public void testUInt1() throws DistributionFactory.RangeException {
        Random rand = new Random(1234);

        Range<Integer> r = Range.closed(1, 1);
        Distribution<Integer> dist = DistributionFactory.uniformInteger(rand, r);
        for (int i = 1; i < 10; i++) {
            int d = dist.draw();
            Assert.assertEquals(1, d);
        }
    }
    
    @Test
    public void testUIntRand() throws DistributionFactory.RangeException {
        Random rand = new Random(1234);

        Range<Integer> r = Range.closed(100, 1000);
        Distribution<Integer> dist = DistributionFactory.uniformInteger(rand, r);
        for (int i = 1; i < 10000; i++) {
            int d = dist.draw();
            Assert.assertTrue(r.contains(d));
        }
    }

    @Test(expected = DistributionFactory.RangeException.class)
    public void testUIntEx1() throws DistributionFactory.RangeException {
        Random rand = new Random(1234);

        Range<Integer> r = Range.openClosed(1, 1);
        Distribution<Integer> dist = DistributionFactory.uniformInteger(rand, r);
    }

    @Test(expected = DistributionFactory.RangeException.class)
    public void testUIntEx2() throws DistributionFactory.RangeException {
        Random rand = new Random(1234);

        Range<Integer> r = Range.closedOpen(1, 1);
        Distribution<Integer> dist = DistributionFactory.uniformInteger(rand, r);
    }

    
}
