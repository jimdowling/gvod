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

import com.google.common.collect.BoundType;
import com.google.common.collect.Range;
import java.util.Random;
import se.sics.kompics.p2p.experiment.dsl.distribution.Distribution;

/**
 * @author jdowling
 * @author Alex Ormenisan <aaor@sics.se>
 */
public class DistributionFactory {

    public static class RangeException extends Exception {

        public RangeException(String msg) {
            super(msg);
        }
    }

    public static Distribution<Integer> uniformInteger(final Random generator, final Range<Integer> range) throws RangeException {
        if (!range.hasLowerBound() || !range.hasUpperBound()) {
            throw new RangeException("interval is not bound - cannot draw");
        }
        if (range.lowerEndpoint().equals(range.upperEndpoint())
                && (range.lowerBoundType() == BoundType.OPEN || range.upperBoundType() == BoundType.OPEN)) {
            throw new RangeException("interval has no elements - cannot draw");
        }

        return new Distribution(Distribution.Type.UNIFORM, Integer.class) {

            @Override
            public final Integer draw() {
                int length = range.upperEndpoint() - range.lowerEndpoint() + 1;
                if (range.lowerBoundType() == BoundType.OPEN) {
                    length--;
                }
                if (range.upperBoundType() == BoundType.OPEN) {
                    length--;
                }
                int u = generator.nextInt(length);
                u += range.lowerEndpoint();
                if(range.lowerBoundType() == BoundType.OPEN) {
                    u++;
                }
                return u;
            }
        };
    }
}