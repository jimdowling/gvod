/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.ipasdistances;

import se.sics.ipasdistances.PrefixMatcher;
import org.junit.*;
import static org.junit.Assert.*;
import se.sics.asdistances.PrefixHandler;

/**
 *
 * @author niklas
 */
public class PrefixMatcherTest {
    
    PrefixMatcher pm;
    
    public PrefixMatcherTest() {
    }

//    @BeforeClass
//    public static void setUpClass() throws Exception {
//    }
//
//    @AfterClass
//    public static void tearDownClass() throws Exception {
//    }

    @Before
    public void setUp() {
        pm = PrefixMatcher.getInstance();
    }
//    
//    @After
//    public void tearDown() {
//    }
    
    @Test
    public void testEqualIPs() {
        int shared = PrefixHandler.sharedPrefix("0.0.0.0", "0.0.0.0");
        assertEquals(32,shared);
        shared = PrefixHandler.sharedPrefix("192.168.1.1", "192.168.1.1");
        assertEquals(32,shared);
    }
    
    @Test
    public void testCompletelyDifferentPrefixes() {
        int shared = PrefixHandler.sharedPrefix("0.0.0.0", "255.255.255.255");
        assertEquals(0,shared);
    }
    
    @Test
    public void testDifferentIPs() {
        int shared = PrefixHandler.sharedPrefix("0.0.0.0", "0.0.0.1");
        assertEquals(31,shared);
        shared = PrefixHandler.sharedPrefix("0.0.0.0", "0.0.0.2");
        assertEquals(30,shared);
        shared = PrefixHandler.sharedPrefix("0.0.0.0", "0.0.0.3");
        assertEquals(30,shared);
        shared = PrefixHandler.sharedPrefix("0.0.0.0", "0.255.0.3");
        assertEquals(8,shared);
        shared = PrefixHandler.sharedPrefix("0.0.0.0", "0.0.23.3");
        assertEquals(19,shared);
        shared = PrefixHandler.sharedPrefix("0.0.0.0", "0.2.12.3");
        assertEquals(14,shared);
    }
    
    @Test
    public void testIntegerPrefixes() {
        int prefix1 = PrefixHandler.prefixToInteger("0.0.0.0");
        int shared = PrefixHandler.sharedPrefix(prefix1, prefix1);
        assertEquals(32,shared);
        int prefix2 = PrefixHandler.prefixToInteger("255.255.255.255");
        shared = PrefixHandler.sharedPrefix(prefix1, prefix2);
        assertEquals(0,shared);
        prefix2 = PrefixHandler.prefixToInteger("0.255.0.3");
        shared = PrefixHandler.sharedPrefix(prefix1, prefix2);
        assertEquals(8,shared);
        prefix2 = PrefixHandler.prefixToInteger("0.0.23.3");
        shared = PrefixHandler.sharedPrefix(prefix1, prefix2);
        assertEquals(19,shared);
    }
    
        @Test
    public void testprefixToInteger() {
        String ip = "0.0.0.0";
        String result = PrefixHandler.prefixToString(PrefixHandler.prefixToInteger(ip));
        assertEquals(ip,result);
        ip = "255.255.255.255";
        result = PrefixHandler.prefixToString(PrefixHandler.prefixToInteger(ip));
        assertEquals(ip,result);
        ip = "0.0.0.0/20";
        result = PrefixHandler.prefixToString(PrefixHandler.prefixToInteger(ip));
        assertEquals("0.0.0.0",result);
        ip = "1.1.1.1/24";
        result = PrefixHandler.prefixToString(PrefixHandler.prefixToInteger(ip));
        assertEquals("1.1.1.0",result);
        ip = "192.168.1.12";
        result = PrefixHandler.prefixToString(PrefixHandler.prefixToInteger(ip));
        assertEquals(ip,result);
        ip = "192.168.1.12/9";
        result = PrefixHandler.prefixToString(PrefixHandler.prefixToInteger(ip));
        assertEquals("192.128.0.0",result);
    }
}
