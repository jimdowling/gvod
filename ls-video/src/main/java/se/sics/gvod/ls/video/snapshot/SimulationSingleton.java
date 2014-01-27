package se.sics.gvod.ls.video.snapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * Singleton for storing information shared across nodes.
 *
 * @author Niklas Wahl&#233;n <nwahlen@kth.se>
 */
public class SimulationSingleton {

    private static SimulationSingleton instance;
    // For monitoring stream lag
    private Map<Integer, Long> pieceDisseminationStartTime; // Piece ID, time

    private SimulationSingleton() {
        pieceDisseminationStartTime = new ConcurrentHashMap<Integer, Long>();
    }

    public static SimulationSingleton getInstance() {
        if (instance == null) {
            instance = new SimulationSingleton();
        }
        return instance;
    }

    public boolean register(int pieceId) {
        if (!pieceDisseminationStartTime.containsKey(pieceId)) {
            pieceDisseminationStartTime.put(pieceId, System.currentTimeMillis());
            return true;
        }
        return false;
    }

    public Long get(int pieceId) {
        return pieceDisseminationStartTime.get(pieceId);
    }
}
