package se.sics.gvod.ls.interas;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import se.sics.asdistances.ASDistances;
import se.sics.asdistances.PrefixHandler;
import se.sics.gvod.common.VodDescriptor;

/**
 *
 * @author Niklas Wahl&#233;n <nwahlen@kth.se>
 */
public class InterAsComparator implements Comparator<VodDescriptor> {

    private VodDescriptor referenceNode;
    private ASDistances distances;
    private Map<Integer, Integer> penalties;

    public InterAsComparator(VodDescriptor referenceNode) {
        this.referenceNode = referenceNode;
        this.distances = ASDistances.getInstance();
        penalties = new HashMap<Integer, Integer>();
    }

    @Override
    public int compare(VodDescriptor d1, VodDescriptor d2) {
        String ipr = referenceNode.getVodAddress().getIp().getHostAddress();
        String ip1 = d1.getVodAddress().getIp().getHostAddress();
        String ip2 = d2.getVodAddress().getIp().getHostAddress();
        byte distanceTo1 = distances.getDistance(ipr, ip1);
        byte distanceTo2 = distances.getDistance(ipr, ip2);
        int penalty1 = this.getPenalty(d1.getVodAddress().getId());
        int penalty2 = this.getPenalty(d2.getVodAddress().getId());

        if (penalty1 < penalty2) {
            return 1;
        } else if (penalty1 > penalty2) {
            return -1;
        } else if (distanceTo1 < distanceTo2) {
            return 1;
        } else if (distanceTo1 > distanceTo2) {
            return -1;
        } else {
            int sharedPrefixLength1 = PrefixHandler.sharedPrefix(ipr, ip1);
            int sharedPrefixLength2 = PrefixHandler.sharedPrefix(ipr, ip2);
            if (sharedPrefixLength1 > sharedPrefixLength2) {
                return 1;
            } else if (sharedPrefixLength1 < sharedPrefixLength2) {
                return -1;
            } else {
                return 0;
            }
        }
    }

    public void punish(int id) {
        Integer currentPenalty = penalties.get(id);
        if (currentPenalty == null || currentPenalty < 51) {
            penalties.put(id, 100);
        } else { /*
             * If the map previously contained a mapping for the key, the old
             * value is replaced by the specified value.
             */
            penalties.put(id, currentPenalty * 2);
        }
    }

    public int getPenalty(int id) {
        Integer penalty = penalties.get(id);
        return penalty == null ? 0 : penalty;
    }

    public void decreasePenalties() {
        Set<Integer> ids = penalties.keySet();
        for (Integer id : ids) {
            Integer currentPenalty = penalties.get(id);
            if (currentPenalty != null) {
                penalties.put(id, currentPenalty - 1);
            }
        }
    }
}
