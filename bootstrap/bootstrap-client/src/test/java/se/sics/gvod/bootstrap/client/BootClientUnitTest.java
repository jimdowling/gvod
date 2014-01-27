/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.bootstrap.client;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import se.sics.gvod.address.Address;
import se.sics.gvod.bootstrap.msgs.BootstrapMsg;
import se.sics.gvod.bootstrap.port.AddOverlayRequest;
import se.sics.gvod.common.VodRetryComponentTestCase;
import se.sics.gvod.common.hp.HPMechanism;
import se.sics.gvod.config.BootstrapConfiguration;
import se.sics.gvod.config.VodConfig;
import se.sics.gvod.net.VodAddress;
import se.sics.kompics.Event;
import se.sics.kompics.Stop;

/**
 *
 * @author Owner
 */
public class BootClientUnitTest extends VodRetryComponentTestCase {

    BootstrapClient bc = null;
    Address server;
    LinkedList<Event> events;

    public BootClientUnitTest() {
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
        bc = new BootstrapClient(this);
        InetAddress localIp=null;
        try {
            localIp = InetAddress.getLocalHost();
        } catch (UnknownHostException ex) {
            Logger.getLogger(BootClientUnitTest.class.getName()).log(Level.SEVERE, null, ex);
            assert(false);
        }
        server = new Address(localIp, 8888, 11);
        bc.handleInit.handle(new BootstrapClientInit(this, 
                new BootstrapConfiguration(localIp.getHostAddress(), 8888, 11, 2000, 2, 
                60*1000, 8903)
                ));

    }

    @After
    @Override
    public void tearDown() {
        super.tearDown();
        bc.stop(new Stop());
    }

    @Test
    public void testAddOverlayRequest() {
        int len = 6500; // muliple of 10
        byte[] data = new byte[len];
        for (int i = 0; i < len / 10; i++) {
            for (int j = 0; j < 10; j++) {
                data[(i * 10) + j] = (byte) j;
            }
        }
        AddOverlayRequest aor = new AddOverlayRequest(server, 1, "test",
                "some desc here. Make it a bit longer",
                data,
                "http://sss.cccc.com/asdfadfasdfasdfsadfsadfsdf/sdfasdfdaf.png");
        bc.handleAddOverlayRequest.handle(aor);
        LinkedList<Event> sentEvts = popEvents();
        byte[] sent = new byte[len];
        int offset = 0;
        for (Event ev : sentEvts) {
            BootstrapMsg.AddOverlayReq req = (BootstrapMsg.AddOverlayReq) ev;
            System.arraycopy(req.getTorrentData(), 0, sent, offset,
                    req.getTorrentData().length);
            offset += req.getTorrentData().length;
        }
        Assert.assertArrayEquals(sent, data);
    }

    @Override
    public HPMechanism getHpMechanism(VodAddress dest) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
