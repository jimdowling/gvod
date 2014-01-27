package se.sics.gvod.ls.interas;

import java.util.*;
import se.sics.gvod.common.Self;
import se.sics.gvod.common.VodDescriptor;
import se.sics.gvod.net.VodAddress;

/**
 *
 * @author Niklas Wahl&#233;n <nwahlen@kth.se>
 */
public class InterAsNeighbours {

    private Self self;
    private int maxSize;
    private TreeSet<VodDescriptor> neighboursTreeSet;
    private ArrayList<VodDescriptor> neighboursArrayList;
    private int type;
    private InterAsComparator comparator;
    public static final int TREE_SET = 1;
    public static final int ARRAY_LIST = 2;

    public InterAsNeighbours(Self self, int maxSize, int type) {
        this.self = self;
        this.maxSize = maxSize;
        this.type = type;
        this.comparator = new InterAsComparator(self.getDescriptor());
        switch (type) {
            case TREE_SET:
                neighboursTreeSet = new TreeSet<VodDescriptor>(comparator);
                break;
            case ARRAY_LIST:
                neighboursArrayList = new ArrayList<VodDescriptor>();
                break;
            default:
                // ...
                break;
        }
    }

    public void add(VodDescriptor d) {
        if (d.getVodAddress().getId() != self.getId()) {
            switch (type) {
                case TREE_SET:
                    addToTreeSet(d);
                    break;
                case ARRAY_LIST:
                    addToArrayList(d);
                    break;
                default:
                    // ...
                    break;
            }
        }
    }

    public void addToTreeSet(VodDescriptor d) {
        if (((neighboursTreeSet.size() < maxSize)
                || (neighboursTreeSet.comparator().compare(d, neighboursTreeSet.last()) > 0))) {
            if (neighboursTreeSet.add(d)) {
                if (neighboursTreeSet.size() > maxSize) {
                    // Since the set is sorted (TreeSet) we remove the
                    // "worst" neighbour after adding a new node
                    neighboursTreeSet.remove(neighboursTreeSet.last());
                    // It would also be possible to compare the node to be
                    // added with the worst node to see which should be kept
                }
            } else {
                // TODO: compare ages? (if we are here the entry already exists)
            }
        }
    }

    public void addToArrayList(VodDescriptor d) {
        boolean add = true;
        for (VodDescriptor d1 : neighboursArrayList) {
            if (d.getVodAddress().getId() == d1.getVodAddress().getId()) {
                add = false;
            }
        }
        if (add) {
            if (neighboursArrayList.size() < maxSize) {
                neighboursArrayList.add(d);
            } else {
                Collections.sort(neighboursArrayList, comparator);
                if (comparator.compare(d, neighboursArrayList.get(0)) > 0) {
                    neighboursArrayList.remove(0);
                    neighboursArrayList.add(d);
                }
            }
        }
    }

    public void addAll(Collection<VodDescriptor> ds) {
        for (VodDescriptor d : ds) {
            if (d.getVodAddress().getId() != self.getId()) {
                this.add(d);
            }
        }
        return;
//        switch (type) {
//            case TREE_SET:
//                neighboursTreeSet.addAll(ds);
//                break;
//            case ARRAY_LIST:
//                neighboursArrayList.addAll(ds);
//                break;
//            default:
//                break;
//        }
    }

    public void remove(VodDescriptor d) {
        switch (type) {
            case TREE_SET:
                neighboursTreeSet.remove(d);
            case ARRAY_LIST:
                neighboursArrayList.remove(d);
            default:
                throw new RuntimeException("Unknown storage type specified");
        }
    }

    public boolean isEmpty() {
        switch (type) {
            case TREE_SET:
                return neighboursTreeSet.isEmpty();
            case ARRAY_LIST:
                return neighboursArrayList.isEmpty();
            default:
                throw new RuntimeException("Unknown storage type specified");
        }

    }

    public int size() {
        switch (type) {
            case TREE_SET:
                return neighboursTreeSet.size();
            case ARRAY_LIST:
                return neighboursArrayList.size();
            default:
                throw new RuntimeException("Unknown storage type specified");
        }

    }

    public VodDescriptor getBestNeighbour() {
        switch (type) {
            case TREE_SET:
                return neighboursTreeSet.first();
            case ARRAY_LIST:
                Collections.sort(neighboursArrayList, comparator);
                return neighboursArrayList.get(neighboursArrayList.size() - 1);
            default:
                throw new RuntimeException("Unknown storage type specified");
        }

    }

    public VodDescriptor getDescriptorFromAddress(VodAddress a) {
        for (VodDescriptor d1 : this.getAsCollection()) {
            if (d1.getVodAddress().getId() == a.getId()) {
                return d1;
            }
        }
        return null;
    }

    public Collection<VodDescriptor> getAsCollection() {
        switch (type) {
            case TREE_SET:
                return neighboursTreeSet;
            case ARRAY_LIST:
                return neighboursArrayList;
            default:
                throw new RuntimeException("Unknown storage type specified");
        }

    }

    public void punish(int id) {
        comparator.punish(id);
    }

    public void decreasePenalites() {
        comparator.decreasePenalties();
    }
    public int getType() {
        return type;
    }
}
