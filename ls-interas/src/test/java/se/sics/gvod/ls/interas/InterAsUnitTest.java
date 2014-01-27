/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.ls.interas;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import se.sics.gvod.common.UtilityVod;
import se.sics.gvod.common.VodDescriptor;
import se.sics.gvod.common.VodRetryComponentTestCase;
import se.sics.gvod.config.VodConfig;
import se.sics.gvod.net.VodAddress;
import se.sics.kompics.Event;
import se.sics.gvod.ls.interas.events.InterAsSetsExchangeCycle;

/**
 *
 * @author Owner
 */
public class InterAsUnitTest extends VodRetryComponentTestCase {

    InterAs interAs = null;
    LinkedList<Event> events;
    List<VodDescriptor> nodes;
    List<VodDescriptor> updates;
    VodDescriptor n1, n2, n3, n4;
        
    public InterAsUnitTest() {
        super();
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    @Override
    public void setUp() {
        super.setUp();
        try {
            VodConfig.init(new String[]{""});
        } catch (IOException ex) {
            Logger.getLogger(InterAsUnitTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        interAs = new InterAs(this);
        
        interAs.handleInit.handle(new InterAsInit(this, 1000, 1000));
        events = pollEvent(1);
        assertSequence(events, InterAsSetsExchangeCycle.class);

        VodAddress v1 = new VodAddress(pubAddrs.get(0).getPeerAddress(), 
                121);
        VodAddress v2 = new VodAddress(pubAddrs.get(1).getPeerAddress(), 
                121);
        VodAddress v3 = new VodAddress(pubAddrs.get(2).getPeerAddress(), 
                121);
        VodAddress v4 = new VodAddress(pubAddrs.get(3).getPeerAddress(), 
                121);
        
        n1 = new VodDescriptor(v1, new UtilityVod(10), 0, 1500);
        n2 = new VodDescriptor(v2, new UtilityVod(12), 0, 1500);
        n3 = new VodDescriptor(v3, new UtilityVod(20), 5, 1500);
        n4 = new VodDescriptor(v4, new UtilityVod(40), 0, 1500);
        
        nodes = new ArrayList<VodDescriptor>();
        updates = new ArrayList<VodDescriptor>();
        nodes.add(n1);
        nodes.add(n2);
        nodes.add(n3);
        updates.add(n4);
        n3 = new VodDescriptor(v3, new UtilityVod(50), 2, 1500);
        updates.add(n3);
    }

    @After
    @Override
    public void tearDown() {
        super.tearDown();
    }

}
