package se.sics.gvod.simulator.video.scenarios;

import java.util.Random;
import se.sics.gvod.ls.system.LSConfig;
import se.sics.gvod.simulator.interas.LsSimulationMain;
import se.sics.gvod.simulator.video.VideoSimulationMain;
import se.sics.kompics.p2p.experiment.dsl.SimulationScenario;

/**
 *
 * @author Amir Payberah <amir@sics.se>
 * @author Niklas Wahl&#233;n <nwahlen@kth.se>
 */
public class VideoScenario {
    
    public static int FIRST = 30;
    public static int PUBLIC;
    public static int PRIVATE;
    public static int COLLECT_VIDEO_RESULTS = 150;
    public static int STATS_SAMPLING_RATE = 1000;
    public static int SOURCE_NODES = 1;
    
	private static Random random;
	protected SimulationScenario scenario;

//-------------------------------------------------------------------
	public VideoScenario(SimulationScenario scenario) {
		this.scenario = scenario;
		this.scenario.setSeed(System.currentTimeMillis());
		random = scenario.getRandom();
	}

//-------------------------------------------------------------------
	public void setSeed(long seed) {
		this.scenario.setSeed(seed);
	}
        
	public void simulateLs() {
		this.scenario.simulate(LsSimulationMain.class);
	}
        
        public void simulateVideo() {
            this.scenario.simulate(VideoSimulationMain.class);
        }

	public static Random getRandom() {
		return random;
	}

	public static void setRandom(Random r) {
		random = r;
	}
    
}
