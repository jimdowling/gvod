/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.system.vod;

import java.io.IOException;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import se.sics.gvod.common.SelfImpl;
import se.sics.gvod.common.VodRetryComponentTestCase;
import se.sics.gvod.config.VodConfig;
import se.sics.gvod.web.server.VodMonitorConfiguration;
import se.sics.kompics.Event;

/**
 *
 * @author Owner
 */
public class VodUnitTest extends VodRetryComponentTestCase {

    Vod vod = null;
    LinkedList<Event> events;

    public VodUnitTest() {
        super();
        vod = new Vod(this);
        try {
            VodConfig.init(new String[0]);
        } catch (IOException ex) {
            Logger.getLogger(VodUnitTest.class.getName()).log(Level.SEVERE, null, ex);
        }
//        setNat(natFactory.getEiPpPd());
        vod.handleInit.handle(new VodInit(
                new SelfImpl(selfDesc.getVodAddress()), 
                new VodConfiguration("topgear.mp4", 200), 
                new VodMonitorConfiguration(pubAddrs.get(0).getPeerAddress()), 
                10000, 1212, false, true, 
                 null, 1, false, 5000, 5000,
                false, null, "src/test/resources/topgear.mp4.data", true, null            
                ));
//        vod.handleJoin.handle(new VodJoin(null, 1, false, 5000, 5000,
//                false, null, "movie", true));
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
    }

    @After
    @Override
    public void tearDown() {
        super.tearDown();
    }

    @Test
    public void testDataMsg() {
    }
    
    @Test
    public void testStop() {
//        vod.stop(new Stop());        
    }
    

}
