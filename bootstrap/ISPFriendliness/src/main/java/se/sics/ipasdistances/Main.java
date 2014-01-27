package se.sics.ipasdistances;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import se.sics.asdistances.ASDistances;
import se.sics.asdistances.PrefixHandler;

/**
 *
 * @author Niklas Wahlén <nwahlen@kth.se>
 */
public class Main {

    public static void main(String args[]) {
        //testDistances();
        //readPrefixes();
        readASDistances();
    }
//        System.out.println("Retrieving ASDistances object");
//        ASDistances asd = null;
//        try {
//            asd = ASDistances.load();
//        } catch (IOException ex) {
//            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        System.out.println("Done.");
//        byte[][] distances = asd.getTransitASDistances();
//        int n = 1000;
//        for (int i = 0; i < n; i++) {
//            for (int j = 0; j < n; j++) {
//                System.out.println(distances[i][j]);
//            }
//        }

    private static void readPrefixes() {
        PrefixHandler ph = PrefixHandler.getInstance();
        ASDistances asd = new ASDistances();
        asd.setIpPrefixToAS(ph.getPrefixToAS());
        AsIpGenerator ipGen = AsIpGenerator.getInstance(System.currentTimeMillis());
        String ip = "78.25.72.178";//ipGen.generateIP().getHostAddress();
        System.out.println(ip + " -> " + asd.getASFromIP(ip));
    }


    /*
     * For profiling of ASDistances size
     */
    private static void readASDistances() {
//        try {
            ASDistances asd = ASDistances.getInstance();
            byte[] distances = asd.getTransitASDistances();
            try {
                Thread.sleep(10000);
            } catch (InterruptedException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
            System.out.println("distance: " + asd.getDistance("193.10.67.148", "85.226.78.233"));
//        } catch (IOException ex) {
//            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
//        }
    }
}
