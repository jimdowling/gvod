package se.sics.asdistances;

import java.io.*;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.*;

/**
 * A serializable class for storage of distances between transit ASes, with
 * methods for calculating the distance in hops between any two ASes.
 *
 * @author Niklas Wahl&#233;n <nwahlen@kth.se>
 * @see DistanceCalculator
 */
public class ASDistances implements Serializable {

    private static final long serialVersionUID = 7656264260256297456L;
    private Map<Integer, int[]> stubASProviders;
    private byte[] transitASDistances;
    private Map<Integer, Integer> transitASToInternalIndex;
    private Map<InternalPrefix, Integer> ipPrefixToAS;
    private static ASDistances instance;
    public static final String DEFAULT_DATA_PATH = "data/";
    public static final String DEFAULT_AS_DISTANCES_FILE = "ASDistances.gz";

    public ASDistances() {
    }

    public void serialize() throws IOException {
        serialize(DEFAULT_DATA_PATH + DEFAULT_AS_DISTANCES_FILE);
    }

    public void serialize(String distancesFilePath) throws IOException {
        File distancesFile = new File(distancesFilePath);
        if (!distancesFile.exists()) {
            distancesFile.createNewFile();
        }
        GZIPOutputStream out = new GZIPOutputStream(new FileOutputStream(distancesFile));
        ObjectOutputStream oos = new ObjectOutputStream(out);
        oos.writeObject(this);
        oos.flush();
        oos.close();
        out.close();
    }

    private static ASDistances load() throws IOException {
        return load(DEFAULT_DATA_PATH + DEFAULT_AS_DISTANCES_FILE);
    }

    private static ASDistances load(String distancesFile) throws IOException {
        GZIPInputStream in = new GZIPInputStream(Thread.currentThread().getContextClassLoader().getResourceAsStream(distancesFile));
        ObjectInputStream ois = new ObjectInputStream(in);
        ASDistances asd = null;
        try {
            asd = (ASDistances) ois.readObject();
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ASDistances.class.getName()).log(Level.SEVERE, null, ex);
        }
        ois.close();
        in.close();
        return asd;
    }

    public static ASDistances getInstance() {
        if (instance == null) {
            try {
                instance = load();
            } catch (IOException ex) {
                Logger.getLogger(ASDistances.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return instance;
    }

    /**
     * Calculates the AS hop count between two IP addresses. The order of the IP
     * addresses does not matter.
     *
     * @param ip1
     * @param ip2
     * @return The AS hop count between the IP addresses. This method returns
     * <code>Byte.MAX_VALUE</code> if: at least on of the IP addresses misses an
     * AS mapping, or there is no connection between the ASes holding the IP
     * addresses.
     */
    public byte getDistance(String ip1, String ip2) {
        return getDistance(PrefixHandler.prefixToInteger(ip1), PrefixHandler.prefixToInteger(ip2));
    }

    public byte getDistance(int ip1, int ip2) {
        int as1 = getASFromIP(ip1);
        int as2 = getASFromIP(ip2);
        // If there is not matching prefix/AS for this IP (indicated by
        // getASFromIP returning -1), return Byte.MAX_VALUE
        if (as1 < 0 || as2 < 0) {
            return Byte.MAX_VALUE;
        }
        // If the IP addresses belong to the same AS return 0
        if (as1 == as2) {
            return 0;
        }
        // Case: both ASes are transit ASes
        if (transitASToInternalIndex.containsKey(as1) && transitASToInternalIndex.containsKey(as2)) {
            return getDistanceBetweenTransitASes(as1, as2);
        } // Case: as1 is a stub AS
        else if (!transitASToInternalIndex.containsKey(as1) && transitASToInternalIndex.containsKey(as2)) {
            return getDistanceFromStubASToTransitAS(as1, as2);
        } // Case: as2 is a stub AS
        else if (transitASToInternalIndex.containsKey(as1) && !transitASToInternalIndex.containsKey(as2)) {
            return getDistanceFromStubASToTransitAS(as2, as1);
        } // Case: both ASes are stub ASes
        else if (!transitASToInternalIndex.containsKey(as1) && !transitASToInternalIndex.containsKey(as2)) {
            return getDistanceBetweenStubASes(as1, as2);
        }
        // This segment should never be reached
        return Byte.MAX_VALUE;
    }

    private byte getDistanceBetweenTransitASes(int transitAS1, int transitAS2) {
        int i1 = transitASToInternalIndex.get(transitAS1);
        int i2 = transitASToInternalIndex.get(transitAS2);
        if (i1 > i2) {
            return transitASDistances[(i1 * (i1 + 1) / 2) + i2];
        } else {
            return transitASDistances[(i2 * (i2 + 1) / 2) + i1];
        }
    }

    /*
     * Note that the order is important! (The method could be modified so that
     * it isn't by checking in which Map each AS is present, but since this is
     * an internal method it doesn't seem necessary).
     */
    private byte getDistanceFromStubASToTransitAS(int stubAS, int transitAS) {
        int[] fromStubASProviders = stubASProviders.get(stubAS);
        if (fromStubASProviders == null) {
            return Byte.MAX_VALUE;
        }
        byte transitDistance = Byte.MAX_VALUE;
        for (int i = 0; i < fromStubASProviders.length; i++) {
            byte d = getDistanceBetweenTransitASes(fromStubASProviders[i], transitAS);
            if (d < transitDistance) {
                transitDistance = d;
            }
        }
        return (byte) (transitDistance + 1);
    }

    private byte getDistanceBetweenStubASes(int fromStubAS, int toStubAS) {
        int[] fromStubASProviders = stubASProviders.get(fromStubAS);
        int[] toStubASProviders = stubASProviders.get(toStubAS);
        if (fromStubASProviders == null || toStubASProviders == null) {
            return Byte.MAX_VALUE;
        }
        byte transitDistance = Byte.MAX_VALUE;
        // Find the transit AS pair closest to eachother among the stub ASes providers
        for (int i = 0; i < fromStubASProviders.length; i++) {
            for (int j = 0; j < toStubASProviders.length; j++) {
                byte d = getDistanceBetweenTransitASes(fromStubASProviders[i], toStubASProviders[j]);
                if (d < transitDistance) {
                    transitDistance = d;
                }
            }
        }
        return (byte) (transitDistance + 2);
    }

    /**
     * Finds the AS which owns an IP address.
     *
     * @param ip
     * @return The AS which owns the specified IP or -1 if no owner is found.
     */
    public int getASFromIP(String ip) {
        return getASFromIP(PrefixHandler.prefixToInteger(ip));
    }

    public int getASFromIP(int ip) {
        for (byte prefixLength = 32; prefixLength > 0; prefixLength--) {
            Integer asn = ipPrefixToAS.get(new InternalPrefix(ip, prefixLength));
            if (asn != null) {
                return asn;
            }
        }
        return -1;
    }

    /*
     * -- Setters and getters --
     *
     */
    public byte[] getTransitASDistances() {
        return transitASDistances;
    }

    /**
     * This method reads a full
     * <code>NxN</code> distance matrix and stores it as an array of only half
     * the size (since the path between AS A and AS B is bidirectional, it is
     * possible to remove half of the cells without losing information if done
     * right).
     *
     * @param transitASDistancesFull
     */
    public void setTransitASDistances(byte[][] transitASDistancesFull) {
        int transits = transitASDistancesFull.length;
        System.out.println("ASDistances#setTransitASDistances() - Minimizing the distance structure");
        transitASDistances = new byte[transits * (transits + 1) / 2];
        for (Integer i1 : transitASToInternalIndex.values()) {
            for (Integer i2 : transitASToInternalIndex.values()) {
                if (i1 > i2) {
                    transitASDistances[(i1 * (i1 + 1) / 2) + i2] = transitASDistancesFull[i1][i2];
                } else {
                    transitASDistances[(i2 * (i2 + 1) / 2) + i1] = transitASDistancesFull[i1][i2];
                }
            }
        }
        System.out.println("ASDistances#setTransitASDistances() - Verifying the minimizied distance structure");
        if (transitASDistancesVerification(transitASDistancesFull, transitASDistances)) {
            System.out.println("ASDistances#setTransitASDistances() - Verification OK");
        } else {
            System.out.println("ASDistances#setTransitASDistances() - Failed verification");
        }
    }

    public void setTransitASDistances(byte[] transitASDistances) {
        this.transitASDistances = transitASDistances;
    }

    public Map<Integer, Integer> getTransitASToInternalIndex() {
        return transitASToInternalIndex;
    }

    public void setTransitASToInternalIndex(Map<Integer, Integer> transitASToInternalIndex) {
        this.transitASToInternalIndex = transitASToInternalIndex;
    }

    public Map<Integer, int[]> getStubASProviders() {
        return stubASProviders;
    }

    public void setStubAProviders(Map<Integer, int[]> stubASProviders) {
        this.stubASProviders = stubASProviders;
    }

    public Map<InternalPrefix, Integer> getIpPrefixToAS() {
        return ipPrefixToAS;
    }

    public void setIpPrefixToAS(Map<InternalPrefix, Integer> ipPrefixToAS) {
        this.ipPrefixToAS = ipPrefixToAS;
    }

    /*
     * -- Other --
     *
     */
    private boolean transitASDistancesVerification(byte[][] distancesFull, byte[] distances) {
        boolean verified = true;
        for (Integer i1 : transitASToInternalIndex.values()) {
            for (Integer i2 : transitASToInternalIndex.values()) {
                int distanceHalf;
                if (i1 > i2) {
                    distanceHalf = distances[(i1 * (i1 + 1) / 2) + i2];
                } else {
                    distanceHalf = distances[(i2 * (i2 + 1) / 2) + i1];
                }
                if (distancesFull[i1][i2] != distanceHalf) {
                    System.out.println("ASDistances::transitASDistancesVerification() - ! (" + i1 + "," + i2 + "): " + distancesFull[i1][i2] + " != " + distanceHalf);
                    verified = false;
                }
            }
        }
        return verified;
    }
}
