package se.sics.asdistances;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Analyzes AS topology information (BGP UPDATES from
 * {@link http://archive.routeviews.org/} converted to ASCII) and calculates
 * distances between transit ASes, which is stored together with additional AS
 * topology information in an
 * <code>ASDistances</code> object.
 *
 *
 * The idea is to only calculate the distances between transit ASes, and for
 * stub ASes maintain lists of their providers (which by definition is a transit
 * AS). The distance to a stub AS is then the shortest distance to any of that
 * stub ASes providers plus 1. This saves a lot of space and memory, and the
 * time to lookup a stub AS' providers is very low.
 *
 *
 * The only purpose of this class is to generate the
 * <code>ASDistances</code> class. Otherwise it is never used.
 *
 * @author Niklas Wahl&#233;n <nwahlen@kth.se>
 * @see ASDistances
 */
public class DistanceCalculator {

    private static boolean ready = false;
    private static final String DEFAULT_DATA_PATH = ASDistances.DEFAULT_DATA_PATH;
    private static final String DEFAULT_RELATION_LOCATION = "ASrelation.txt";
    private static final String DEFAULT_CONNPAIRS_LOCATION = "ASconnpairs.txt";
    private static Map<Integer, int[]> stubASProviders;
    private static Map<Integer, Integer> transitAStoInternalIndex;
    private static Map<Integer, ArrayList<Integer>> edges;
    private static byte[][] path;
    private static DistanceCalculator instance;
    private static int counter;

    private DistanceCalculator() {
        readASRelations();
        readTransitASConnections();
        calculateDistances();
        PrefixHandler ph = PrefixHandler.getInstance();

        ASDistances asd = new ASDistances();
        asd.setIpPrefixToAS(ph.getPrefixToAS());
        asd.setTransitASToInternalIndex(transitAStoInternalIndex);
        asd.setTransitASDistances(path);
        asd.setStubAProviders(stubASProviders);
        try {
            asd.serialize();
        } catch (IOException ex) {
            Logger.getLogger(DistanceCalculator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Reads the AS relations file. All stub ASes are stored together with a
     * list of their providers. All transit ASes are stored and mapped to an
     * internal index. The internal index is necessary to minimize the size of
     * the distance matrix; many AS numbers are not used and thus not present in
     * the AS data.
     */
    private static void readASRelations() {
        InputStream is = null;
        try {
            is = new FileInputStream(DEFAULT_DATA_PATH + DEFAULT_RELATION_LOCATION);
            stubASProviders = new HashMap<Integer, int[]>();
            transitAStoInternalIndex = new HashMap<Integer, Integer>();
            Scanner s = new Scanner(is);
            int totalASes = 0;
            int stubASProvidersCount = 0;
            // Remove everything from the line except for the AS number
            Pattern asnPattern = Pattern.compile("[^0-9]");
            // Remove everything from the line except for the number of customers/siblings/peers
            Pattern p = Pattern.compile("(^.*:#)|(::.*$)");
            // Remove everything from the line except for the providers' AS numbers
            Pattern p2 = Pattern.compile("(^.*::)|(:$)");
            System.out.println("Reading AS relation information");
            while (s.hasNext()) {
                totalASes++;
                int asn = Integer.valueOf(asnPattern.matcher(s.nextLine()).replaceAll(""));
                String providerLine = s.nextLine();
                int numberOfCustomers = Integer.valueOf(p.matcher(s.nextLine()).replaceAll(""));
                int numberOfSiblings = Integer.valueOf(p.matcher(s.nextLine()).replaceAll(""));
                int numberOfPeers = Integer.valueOf(p.matcher(s.nextLine()).replaceAll(""));
                // Check if the current AS is a stub AS
                if (numberOfCustomers + numberOfSiblings + numberOfPeers == 0) {
                    providerLine = p2.matcher(providerLine).replaceAll("");
                    String[] providers = providerLine.split(":");
                    int providersInt[] = new int[providers.length];
                    for (int i = 0; i < providers.length; i++) {
                        stubASProvidersCount++;
                        providersInt[i] = Integer.valueOf(providers[i]);
                    }
                    stubASProviders.put(asn, providersInt);
                } else {
                    transitAStoInternalIndex.put(asn, transitAStoInternalIndex.size());
                }
                if (totalASes % 10000 == 0) {
                    System.out.println(totalASes);
                }
            }
            System.out.println(totalASes);
            System.out.println("- - - - - - - - - - - - - ");
            System.out.println("Total ASes: " + totalASes);
            System.out.println("Transit ASes: " + transitAStoInternalIndex.size());
            System.out.println("Stub ASes: " + stubASProviders.size());
            System.out.println("Ratio stub ASes: "
                    + ((float) stubASProviders.size()) / ((float) totalASes));
            System.out.println("Avg number of providers per stub AS: "
                    + ((float) stubASProviders.values().size()) / ((float) stubASProviders.size()));
            System.out.println();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(DistanceCalculator.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                is.close();
            } catch (IOException ex) {
                Logger.getLogger(DistanceCalculator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private int getTransitASInternalIndex(int transitAS) {
        return transitAStoInternalIndex.get(transitAS);
    }

    /**
     * Reads the AS connection pairs file and stores each AS as a key entry in a
     * map, with an
     * <code>ArrayList</code> of its neighbors as its value.
     */
    private void readTransitASConnections() {
        InputStream is = null;
        try {
            is = new FileInputStream(DEFAULT_DATA_PATH + DEFAULT_CONNPAIRS_LOCATION);
            Scanner s = new Scanner(is);
            edges = new HashMap<Integer, ArrayList<Integer>>();
            System.out.println("Importing connections between transit ASes"
                    + " (" + transitAStoInternalIndex.size() + ") .. ");
            while (s.hasNext()) {
                String strs[] = s.nextLine().split(" <-> ");
                int fromAS = Integer.valueOf(strs[0].trim());
                int toAS = Integer.valueOf(strs[1].trim());
                // Check that the conncetion is between two transit ASes
                // (otherwise it is ignored)
                if (transitAStoInternalIndex.containsKey(fromAS) && transitAStoInternalIndex.containsKey(toAS)) {
                    // From AS 
                    if (edges.containsKey(fromAS)) {
                        edges.get(fromAS).add(toAS);
                    } else {
                        ArrayList l = new ArrayList<Integer>();
                        l.add(toAS);
                        edges.put(fromAS, l);
                    }
                    // To AS
                    if (edges.containsKey(toAS)) {
                        edges.get(toAS).add(fromAS);
                    } else {
                        ArrayList l = new ArrayList<Integer>();
                        l.add(fromAS);
                        edges.put(toAS, l);
                    }
                }
            }
            System.out.print("Import done, checking that all transit ASes are connected: ");
            if (transitAStoInternalIndex.size() == edges.size()) {
                System.out.println("OK.");
            } else {
                System.out.println("Failed.");
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(DistanceCalculator.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                is.close();
            } catch (IOException ex) {
                Logger.getLogger(DistanceCalculator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Calculates distances between transit ASes according to the Floyd-Warshall
     * algorithm (an all-pairs shortest paths algorithm)
     * {@link http://en.wikipedia.org/wiki/Floyd%E2%80%93Warshall_algorithm}
     */
    private void calculateDistances() {
        System.out.println();
        System.out.println("Preparing to calculate AS distances .. ");
        int n = edges.size();
        path = new byte[n][n];
        for (Integer i : edges.keySet()) {
            int iInternal = getTransitASInternalIndex(i);
            // Set the distance between all peers to the max value initially
            Arrays.fill(path[iInternal], Byte.MAX_VALUE);
            // The distance to ourselves is 0
            path[iInternal][iInternal] = 0;
            // The distance to any directly connected neighbor AS is 1
            for (Integer j : edges.get(i)) {
                path[iInternal][getTransitASInternalIndex(j)] = 1;
            }
        }
        long starttime = System.currentTimeMillis();
        System.out.println("Calculating distances between " + n + " transit ASes:");
        // Floyd-Warshall algorithm, runs in 
        for (int k = 0; k < n; k++) {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    path[i][j] = (byte) Math.min(path[i][j], path[i][k] + path[k][j]);
                }
            }
            increaseCounter();
        }
        long endtime = System.currentTimeMillis();
        printRuntime(endtime - starttime);
    }

    private synchronized void increaseCounter() {
        counter++;
        if ((counter % 100) == 0) {
            System.out.format("%d (%.2f%%)%n", counter, (((float) counter) / ((float) transitAStoInternalIndex.size())) * 100);
        } else if (counter == transitAStoInternalIndex.size()) {
            System.out.println(transitAStoInternalIndex.size() + " (100%)");
        }
    }

    private void printRuntime(long runtime) {
        long minutes = 0, seconds = 0;
        if (runtime > 60000) {
            minutes = runtime / 60000;
            runtime -= minutes * 60000;
        }
        if (runtime > 1000) {
            seconds = runtime / 1000;
        }
        long milliseconds = runtime - (seconds * 1000);
        System.out.println("Runtime: " + minutes + ":" + seconds + "." + milliseconds);
    }

    /**
     * When this class is initialized an
     * <code>ASDistances</code> object will be created and stored to disk.
     *
     * @return the DistanceCalculator singleton
     * @see ASDistances
     */
    public static DistanceCalculator getInstance() {
        if (instance == null) {
            instance = new DistanceCalculator();
        }
        return instance;
    }

    /**
     * Calls getInstance.
     *
     * @param args
     * @see #getInstance()
     */
    public static void main(String args[]) {
        DistanceCalculator instance = getInstance();
    }
}
