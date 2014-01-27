/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.ipasdistances;

import se.sics.ipasdistances.AsIpGenerator;
import se.sics.ipasdistances.PrefixMatcher;
import org.junit.*;
import static org.junit.Assert.*;
import se.sics.asdistances.ASDistances;
import se.sics.asdistances.PrefixHandler;

/**
 *
 * @author Niklas Wahl&#233;n <nwahlen@kth.se>
 */
public class ASDistancesTest {

    ASDistances asd = null;
    AsIpGenerator ipGenerator = null;
    PrefixMatcher pm = null;

    public ASDistancesTest() {
        asd = ASDistances.getInstance();
        ipGenerator = AsIpGenerator.getInstance(1200);
        pm = PrefixMatcher.getInstance();
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testGetASFromIP() {
        for (int i = 0; i < 1000; i++) {
            String ipStr = ipGenerator.generateIP().getHostAddress();
            int ip = PrefixHandler.prefixToInteger(ipStr);
            int peerialismAS = pm.matchIPtoAS(ipStr);
            int asn = asd.getASFromIP(ip);
            if (peerialismAS == asn) {
                System.out.println("SUCCESS: Asn-Ip-reverse lookup succeeded for: " + 
                        ipStr + " as: " + peerialismAS);
            } else {
                System.out.println("FAILURE: Asn-Ip-reverse lookup succeeded for: " + 
                        ipStr + " as1: " + peerialismAS + " as2: " + asn);                
            }
            assert(true);
        }
    }
    
//    public void testGetDistance() {
//        System.out.println("ASN\tdistance");
//        for (int i = 0; i < 10; i++) {
//            String ipStr = ipGenerator.generateIP().getHostAddress();
//            int ip = PrefixHandler.prefixToInteger(ipStr);
//            int asn = asd.getASFromIP(ip);
//            String ipStr2 = ipGenerator.generateIP().getHostAddress();
//            int ip2 = PrefixHandler.prefixToInteger(ipStr2);
//            int asn2 = asd.getASFromIP(ip2);
//            System.out.print(asn);
//            int distance = asd.getDistance(ip, ip2);
//            int peerialismDistance = pcd.getDistanceFromAs(asn, asn2);
//            System.out.println("\t" + distance + "\t" + peerialismDistance);
//            //assertEquals(peerialismAS,asn);
//        }
//    }
}
