/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package se.sics.gvod.simulator.vod;

import java.util.Random;
import se.sics.kompics.p2p.experiment.dsl.distribution.Distribution;
import se.sics.kompics.p2p.experiment.dsl.distribution.Distribution.Type;

/**
 *
 * @author jdowling
 */
public class IntegerUniformDistribution extends Distribution<Integer>
{
	private static final long serialVersionUID = 333453544317577L;

	private final Random random;

	private final Integer min;

	private final Integer max;

	public IntegerUniformDistribution(int min, int max, Random random) {
		super(Type.UNIFORM, Integer.class);
		if (min < 0 || max < 0) {
			throw new RuntimeException("I can only generate positive numbers");
		}
		this.random = random;
		if (min > max) {
			this.min = max;
			this.max = min;
		} else {
			this.min = min;
			this.max = max;
		}
	}

	@Override
	public final Integer draw() {
		int u = random.nextInt();
		u *= (max - min);
		u += min;
		return Math.round(u);
	}
}
