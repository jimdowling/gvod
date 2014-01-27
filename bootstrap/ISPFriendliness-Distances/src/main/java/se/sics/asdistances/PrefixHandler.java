package se.sics.asdistances;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Reads prefix to AS mapping from file on instantiation (only used by
 * <code>DistanceCalculator</code> when creating
 * <code>ASDistances</code>) and provides static methods for manipulating and
 * analyzing IP addresses.
 *
 * @author Niklas Wahl&#233;n <nwahlen@kth.se>
 * @see DistanceCalculator
 * @see ASDistances
 */
public class PrefixHandler {

    private static PrefixHandler instance = null;
    private Map<InternalPrefix, Integer> prefixToAS;
    private final String DEFAULT_DATA_PATH = ASDistances.DEFAULT_DATA_PATH;
    private final String DEFAULT_PREFIX_FILE = "routes.txt";

    private PrefixHandler() {
        InputStream is = null;
        try {
            is = new FileInputStream(DEFAULT_DATA_PATH + DEFAULT_PREFIX_FILE);
            Scanner s = new Scanner(is);
            prefixToAS = new HashMap<InternalPrefix, Integer>();
            // Statistics
            int privateASes = 0;
            int reservedASes = 0;
            int doubles = 0;
            int multiple = 0;
            int counter = 0;
            System.out.println("Reading prefixes:");
            while (s.hasNext()) {
                // Read the first column, a routing prefix (formatted as a.b.c.d/x)
                String prefix = s.next();
                // Read the next column, an AS number
                int asn = s.nextInt();
                // Retrieve the prefix length from the routing prefix
                byte prefixLength = Byte.valueOf(prefix.split("[/]")[1]);
                int intPrefix = PrefixHandler.prefixToInteger(prefix);


                Integer testASN = prefixToAS.get(new InternalPrefix(intPrefix, prefixLength));
                // Check if the AS number is private or reserved (possible due to misconfigurations)
                if (asn >= 64512 && asn <= 65534) {
                    privateASes++;
                } else if (asn == 0 || (asn >= 59392 && asn <= 64511) || (asn >= 65535 && asn <= 131071)) {
                    reservedASes++;
                } // Check if this exact prefix is already mapped to an AS number
                // TODO: handle the case of multiple ASes mapped to the same prefix correctly
                else if (testASN != null) {
                    if (testASN == asn) {
                        doubles++;
                    } else {
                        multiple++;
                    }
                } // If the above cases check out ok it is safe to add this prefix-AS mapping 
                else {
                    prefixToAS.put(new InternalPrefix(intPrefix, prefixLength), asn);
                }
                counter++;
                if (counter % 50000 == 0) {
                    System.out.println(counter);
                }
            }
            System.out.println(counter);
            System.out.println("- - - - - - - - - - - - - ");
            System.out.println("Total prefixes saved: " + prefixToAS.size() + " (should be " + (counter - privateASes - reservedASes - doubles - multiple) + ")");
            System.out.println("Doubles found: " + doubles);
            System.out.println("Prefixes with multiple ASNs: " + multiple);
            System.out.println("Prefixes to Private ASNs: " + privateASes);
            System.out.println("Prefixes to Reserved ASNs: " + reservedASes);
            s.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(PrefixHandler.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                is.close();
            } catch (IOException ex) {
                Logger.getLogger(PrefixHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
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
            Integer asn = prefixToAS.get(new InternalPrefix(ip, prefixLength));
            if (asn != null) {
                return asn;
            }
        }
        return -1;
    }

    /**
     * Returns the mapping of routing prefixes and their corresponding AS
     * number.
     *
     * @return prefix to AS mapping
     */
    public Map<InternalPrefix, Integer> getPrefixToAS() {
        return prefixToAS;
    }

    /**
     * Finds the length of the longest shared prefix of two IP addresses.
     *
     * @param ip1
     * @param ip2
     * @return The shared prefix length in bits.
     * @see #sharedPrefix(int, int)
     */
    public static int sharedPrefix(String ip1, String ip2) {
        return sharedPrefix(prefixToInteger(ip1), prefixToInteger(ip2));
    }

    /**
     * Finds the length of the longest shared prefix of two IP addresses by
     * calling
     * <code>Integer.numberOfLeadingZeros(ip1 ^ ip2)</code>.
     *
     * @param ip1
     * @param ip2
     * @return The shared prefix length in bits.
     */
    public static int sharedPrefix(int ip1, int ip2) {
        return Integer.numberOfLeadingZeros(ip1 ^ ip2);
    }

    /**
     * Converts an IP address (a.b.c.d) or a network prefix (a.b.c.d/x) to its
     * integer value.
     *
     * @param prefix An IP address or a network prefix (if the prefix length is
     * omitted the address will be treated as a prefix of length 32 bits).
     * @return The integer value of the prefix.
     */
    public static Integer prefixToInteger(String prefix) {
        int intPrefix = 0;
        String[] prefixPart = prefix.split("[/]");
        if (prefixPart.length > 1) {
            intPrefix = prefixToInteger(prefixPart[0], Integer.valueOf(prefixPart[1]));
        } else {
            intPrefix = prefixToInteger(prefixPart[0], 32);
        }
        return intPrefix;
    }

    /**
     * Converts a network prefix to its integer value.
     *
     * @param prefix
     * @param prefixLength
     * @return The integer value of the prefix.
     */
    public static Integer prefixToInteger(String prefix, int prefixLength) {
        String[] prefixPart = prefix.split("[.]");
        if (prefixPart.length < 4) {
            throw new IllegalArgumentException("Not a valid IP prefix");
        }
        int intPrefix = ((Integer.parseInt(prefixPart[0]) << 24
                | Integer.parseInt(prefixPart[1]) << 16
                | Integer.parseInt(prefixPart[2]) << 8
                | Integer.parseInt(prefixPart[3])) >>> (32 - prefixLength)) << (32 - prefixLength);
        return intPrefix;
    }

    /**
     * Converts an integer representation of an IP address to a String.
     *
     * @param ip
     * @return The String representation of the IP address ("a.b.c.d").
     */
    public static String prefixToString(int ip) {
        return ((ip >>> 24) & 0xFF) + "."
                + ((ip >>> 16) & 0xFF) + "."
                + ((ip >>> 8) & 0xFF) + "."
                + (ip & 0xFF);
    }

    public static PrefixHandler getInstance() {
        if (instance == null) {
            instance = new PrefixHandler();
        }
        return instance;
    }
}
