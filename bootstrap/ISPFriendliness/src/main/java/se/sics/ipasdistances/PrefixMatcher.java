package se.sics.ipasdistances;

import java.io.*;
import java.util.HashMap;
import java.util.Scanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.asdistances.PrefixHandler;

/**
 *
 * This prefix matcher class. It is a singleton
 *
 * @author elbeltagy
 *
 */
public class PrefixMatcher {

    private static Logger log = LoggerFactory.getLogger(PrefixMatcher.class);
    private static final Integer MINUSONE = new Integer(-1);
    // private static final Logger log =
    // LoggerFactory.getLogger(PrefixMatcher.class);
    private static HashMap<Integer, Integer> prefixMap = new HashMap<Integer, Integer>();
    private static HashMap<Integer, String> asNameMap = null;
    int minPrefixLen = 50;
    int maxPrefixLen = 0;

    private PrefixMatcher() {
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("data/routes.txt");
            try {
                Scanner s = new Scanner(is);
                while (s.hasNext()) {
                    String prefix = s.next();
                    int asNum = s.nextInt();
                    String[] prefixPart = prefix.split("[./]");
                    int prefixLen = Integer.parseInt(prefixPart[4]);
                    int integerPrefix = PrefixHandler.prefixToInteger(prefix);
                    prefixMap.put(integerPrefix, asNum);
                    if (prefixLen < minPrefixLen) {
                        minPrefixLen = prefixLen;
                    }
                    if (prefixLen > maxPrefixLen) {
                        maxPrefixLen = prefixLen;
                    }
                }
                s.close();
            } catch (Exception e) {
                // log.error("Can not read prefix file data"+e.toString());
            }
    }

    /**
     * Finds the AS number that the IP adress belongs to
     *
     * @param ip
     * @return
     */
    public Integer matchIPtoAS(String ip) {
        Integer ipInteger = PrefixHandler.prefixToInteger(ip);
        return matchIPtoAS(ipInteger);
    }

    /**
     * Finds the AS number that the IP adress belongs to
     *
     * @param ip
     * @return
     */
    public Integer matchIPtoAS(Integer ipInteger) {
        Integer asNum = MINUSONE;
        for (int i = maxPrefixLen; i >= minPrefixLen; i--) {
            ipInteger = (ipInteger >>> (32 - i)) << (32 - i);
            if (prefixMap.containsKey(ipInteger)) {
                asNum = prefixMap.get(ipInteger);
                break;
            }
        }
        if (asNum == MINUSONE) {
            log.error("Failed to lookup AS for " + ipInteger);
        }
        return asNum;
    }

    private void constructASnameMap() {
        asNameMap = new HashMap<Integer, String>();
        BufferedReader br;
        try {
            InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("data/asn-ctl.txt");
            br = new BufferedReader(new InputStreamReader(is));
            br.readLine(); // Just skip the first line
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\t");
                if (parts[0].compareTo("1.0") == 0) {
                    break;
                }
                int asNum = Integer.parseInt(parts[0]);
                asNameMap.put(asNum, parts[3]);
            }
            br.close();

        } catch (Exception e) {
            // log.error("Can not read AS Name file data");
        }

    }

    public String getASName(int asNum) {
        if (asNameMap == null) {
            constructASnameMap();
        }
        return asNameMap.get(asNum);
    }
    private static PrefixMatcher instance = null;

    public static synchronized PrefixMatcher getInstance() {
        if (instance == null) {
            instance = new PrefixMatcher();
        }
        return instance;
    }

  
    /**
     *
     * @param args
     */
    public static void main(String[] args) {
        PrefixMatcher pm = PrefixMatcher.getInstance();
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        String ip = null;
        try {
            while (ip != "q") {
                System.out.print("Enter IP:");
                ip = br.readLine();
                int asNum = pm.matchIPtoAS(ip);
                String asName = pm.getASName(asNum);
                System.out.println(asNum + "\t" + asName);
            }
        } catch (IOException ioe) {
            System.out.println("IO error trying to read your name!");
            System.exit(1);
        }

    }

    public HashMap<Integer, Integer> getRawData() {
        return prefixMap;
    }
}
