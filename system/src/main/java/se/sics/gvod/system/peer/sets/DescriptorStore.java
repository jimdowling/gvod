package se.sics.gvod.system.peer.sets;

import java.util.Random;
import java.util.HashMap;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.common.VodDescriptor;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;
import se.sics.gvod.address.Address;
import se.sics.gvod.common.Utility;

/**
 * Normally, there's one DescriptorStore per Vod component. Or maybe one per
 * application?
 *
 */
public class DescriptorStore {

    private final ConcurrentHashMap<Integer, ConcurrentSkipListSet<VodAddress>> neighbourAddresses;
    private final ConcurrentHashMap<VodAddress, VodDescriptor> d2e;
    private final CopyOnWriteArrayList<VodDescriptor> entries;
    private final Random random;
    private final float ALPHA = 10;
    private final float BETA = 7;
    private final int seed;

    /**
     * Create a descriptor store.
     *
     * @param seed
     */
    public DescriptorStore(int seed) {
        neighbourAddresses = new ConcurrentHashMap<Integer, ConcurrentSkipListSet<VodAddress>>();
        d2e = new ConcurrentHashMap<VodAddress, VodDescriptor>();
        entries = new CopyOnWriteArrayList<VodDescriptor>();
        this.seed = seed;
        random = new Random(seed);
    }

    /**
     * Get a list of VodDescriptors for a set of VodAddresses.
     *
     * @param addrs set of VodAddresses
     * @return list of VodDescriptors for those addresses found in the
     * Descriptor Store.
     */
    public List<VodDescriptor> getVodDescriptorsFromVodAddresses(Set<VodAddress> addrs) {
        List<VodDescriptor> nodes = new ArrayList<VodDescriptor>();
        for (VodAddress addr : addrs) {
            if (d2e.containsKey(addr)) {
                nodes.add(d2e.get(addr));
            }
        }
        return nodes;
    }

    /**
     * Get the VodDescriptor from a node address.
     *
     * @param addr the VodAddress for a node
     * @return a VodDescriptor for the VodAddress
     */
    public VodDescriptor getVodDescriptorFromVodAddress(VodAddress addr) {
        return d2e.get(addr);
    }

    /**
     * Add a new VodDescriptor
     *
     * @param addr VodAddress of new node
     * @param utility its utility
     * @param inASet true if the VodDescriptor is already in another set
     * (Bittorrent/upper/lower)
     * @param comWinSize size of congestion control communications window
     * @param pipeSize size of the pipeline for downloading pieces from this
     * node
     * @param maxWindowSize maximum congestion window size
     * @param mtu maximum transfer unit (normally 1500 bytes)
     */
    public void add(VodAddress addr, Utility utility, boolean inASet, int comWinSize,
            int pipeSize, int maxWindowSize, int mtu) {
        if (!d2e.containsKey(addr)) {
            VodDescriptor desc = new VodDescriptor(addr, utility,
                    comWinSize, pipeSize, maxWindowSize, mtu);
            d2e.put(addr, desc);
            entries.add(desc);
        } else {
            VodDescriptor node = new VodDescriptor(addr, utility,
                    comWinSize, pipeSize, maxWindowSize, mtu);
            node.setRefs(d2e.get(node.getVodAddress()).getRefs());
            entries.remove(d2e.get(node.getVodAddress()));
            d2e.put(node.getVodAddress(), node);
            entries.add(node);
        }
        if (!neighbourAddresses.containsKey(addr.getId())) {
            ConcurrentSkipListSet<VodAddress> neighbours = new ConcurrentSkipListSet<VodAddress>();
            neighbourAddresses.put(addr.getId(), neighbours);
        }
        ConcurrentSkipListSet<VodAddress> neighbours = neighbourAddresses.get(addr.getId());
        neighbours.add(addr);
        // update the set of existing neighbours if the parents or nat type changed, or if there
        if (neighbours.isEmpty() || vodAddressChanged(addr, neighbours.iterator().next())) {
            ConcurrentSkipListSet<VodAddress> newNeighbours = new ConcurrentSkipListSet<VodAddress>();
            for (VodAddress va : neighbours) {
                // update set of parents, natType, etc
                newNeighbours.add(new VodAddress(addr.getPeerAddress(), va.getOverlayId(),
                        addr.getNat(), addr.getParents()));
            }
            neighbourAddresses.put(addr.getId(), newNeighbours);
        }
        if (inASet) {
            d2e.get(addr).addRef();
        }
    }

    private boolean vodAddressChanged(VodAddress oldAddr, VodAddress newAddr) {
        if (oldAddr.getNat().equals(newAddr.getNat())) {
            // O(n2) comparison, but ok as N=3/4/5
            for (Address p : oldAddr.getParents()) {
                boolean found = false;
                for (Address q : newAddr.getParents()) {
                    if (p.equals(q)) {
                        found = true; 
                        break;
                    }
                }
                if (!found) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     *
     * @param node
     * @param inASet already in another set (bittorrent, upper or lower)
     */
    public void add(VodDescriptor node, boolean inASet) {
        if (!d2e.containsKey(node.getVodAddress())) {
            d2e.put(node.getVodAddress(), node);
            entries.add(node);
        } else {
            node.setRefs(d2e.get(node.getVodAddress()).getRefs());
            entries.remove(d2e.get(node.getVodAddress()));
            d2e.put(node.getVodAddress(), node);
            entries.add(node);
        }
        if (inASet) {
            d2e.get(node.getVodAddress()).addRef();
        }
    }

    public void suppress(VodAddress peer) {
        entries.remove(d2e.get(peer));
        d2e.remove(peer);
    }

    /**
     *
     * @param peer
     * @return
     */
    VodDescriptor remove(VodAddress peer) {
        VodDescriptor sendDisconnect = null;
        if (d2e.get(peer).getRefs() > 0) {
            d2e.get(peer).supRef();
        }
        if (d2e.get(peer).getRefs() <= 0) {
            sendDisconnect = d2e.get(peer);
        }
        return sendDisconnect;
    }

    public ConcurrentHashMap<VodAddress, VodDescriptor> getNeighbours() {
        return d2e;
    }

    public int getRef(VodAddress addr) {
        if (d2e.get(addr) != null) {
            return d2e.get(addr).getRefs();
        }
        return 0;
    }

    public boolean contains(VodAddress addr) {
        return d2e.containsKey(addr);
    }

    public List<VodDescriptor> getAll() {
        return entries;
    }

    public int size() {
        return entries.size();
    }

    public CopyOnWriteArrayList<VodDescriptor> getAllAsList() {
        return entries;
    }

    public List<VodDescriptor> getNodesToVerify(int numNodes) {
        if (d2e.isEmpty()) {
            return null;
        }
        int rank = 0;
        int nextBound = 0;
        if (!entries.isEmpty()) {
            if (entries.size() <= numNodes) {
                return entries;
            }
            List<Integer> values = new ArrayList<Integer>();
            HashMap<Integer, MapComp> map = new HashMap<Integer, MapComp>();
            for (VodDescriptor node : entries) {
                int temp = (int) (ALPHA / (node.getRefs() + 1) + BETA * node.getAge());
                if (!values.contains(temp)) {
                    MapComp comp = new MapComp(nextBound, nextBound + temp, node,
                            seed);
                    nextBound += temp;
                    values.add(temp);
                    map.put(temp, comp);
                } else {
                    map.get(temp).adNode(node);
                }
            }
            int i = 0;
            List<VodDescriptor> result = new ArrayList<VodDescriptor>();
            VodDescriptor node = null;
            while (i < numNodes) {
                rank = random.nextInt(nextBound);

                //TODO - is a better solution possible here?
                for (MapComp comp : map.values()) {
                    if (rank >= comp.getLowerBound() && rank < comp.getUpperBound()) {
                        node = comp.getRandomNode();
                    }
                }
                if (!result.contains(node)) {
                    result.add(node);
                    i++;
                }
            }
            return result;
        }
        throw new IllegalStateException("Problem with minmax code for neighbour selection");
    }

    public void update(VodDescriptor entry) {
        entry.setRefs(d2e.get(entry.getVodAddress()).getRefs());
        d2e.put(entry.getVodAddress(), entry);
        entries.remove(d2e.get(entry.getVodAddress()));
        entries.add(entry);
    }

    public VodDescriptor getOldestNode() {
        if (d2e.isEmpty()) {
            return null;
        }

        VodDescriptor result = entries.get(0);
        for (VodDescriptor node : entries) {
            if (node.getAge() > result.getAge()) {
                result = node;
            }
        }
        return result;
    }

}
