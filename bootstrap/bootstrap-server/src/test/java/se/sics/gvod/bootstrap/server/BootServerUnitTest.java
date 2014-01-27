/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.bootstrap.server;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import se.sics.gvod.address.Address;
import se.sics.gvod.common.VodRetryComponentTestCase;
import se.sics.gvod.common.hp.HPMechanism;
import se.sics.gvod.config.BootstrapConfiguration;
import se.sics.gvod.net.VodAddress;
import se.sics.kompics.Event;
import se.sics.kompics.Stop;

/**
 *
 * @author Owner
 */
public class BootServerUnitTest extends VodRetryComponentTestCase {

//    BootstrapServerMysql bs = null;
    Address server;
    LinkedList<Event> events;

    public BootServerUnitTest() {
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
//        bs = new BootstrapServerMysql(this);
        InetAddress localIp=null;
        try {
            localIp = InetAddress.getLocalHost();
        } catch (UnknownHostException ex) {
            Logger.getLogger(BootServerUnitTest.class.getName()).log(Level.SEVERE, null, ex);
            assert(false);
        }
        server = new Address(localIp, 8888, 11);
//        bs.handleInit.handle(new BootstrapServerInit(
//                new BootstrapConfiguration(localIp.getHostAddress(), 8888, 11, 2000, 2, 
//                60*1000, 8903),
//                false));
    }

    @After
    @Override
    public void tearDown() {
        super.tearDown();
//        bs.stop(new Stop());
    }

    @Test
    public void testAddOverlayReq() {
//        int len = 6500; // muliple of 10
//        byte[] data = new byte[len];
//        for (int i = 0; i < len / 10; i++) {
//            for (int j = 0; j < 10; j++) {
//                data[(i * 10) + j] = (byte) j;
//            }
//        }
//        byte[] firstData = new byte[1199];
//        System.arraycopy(data, 0, firstData, 0, firstData.length);
//
//        String name = "test";
//        String desc = "some desc here. Make it a bit longer";
//        String imgUrl = "http://sss.cccc.com/asdfadfasdfasdfsadfsadfsdf/sdfasdfdaf.png";
//        int firstMsgLen = name.length() + desc.length() + 100 + imgUrl.length();
//        int offsetFirst = 0;
//        int offsetSecond = VodConfig.DEFAULT_MTU - firstMsgLen;
//        int dataLen = VodConfig.DEFAULT_MTU - 200;
//        int numParts = ((data.length - firstData.length) / dataLen)
//                + 1 /* first packet contains some data */;
//        // Need to add one more to numParts if the last packet was not exactly
//        // equal to dataLen. 
//        numParts += ((data.length - firstData.length) % dataLen) > 0 ? 1 : 0;
//        BootstrapMsg.AddOverlayReq aor = new BootstrapMsg.AddOverlayReq(self,
//                pubAddrs.get(0), name, 1,
//                desc, firstData, imgUrl, 0, numParts);
//        bs.handleAddOverlayReq.handle(aor);
//
//        for (int i = 1; i < numParts; i++) {
//            offsetFirst = offsetSecond;
//            offsetSecond = (offsetSecond + dataLen) > data.length ? data.length
//                    : (offsetSecond + dataLen);
//            byte[] nextData = Arrays.copyOfRange(data, offsetFirst, offsetSecond);
//            BootstrapMsg.AddOverlayReq req = new BootstrapMsg.AddOverlayReq(self,
//                    pubAddrs.get(0), name, 1,
//                    "", nextData, "", i, numParts);
//            bs.handleAddOverlayReq.handle(req);
//        }


    }

    @Override
    public HPMechanism getHpMechanism(VodAddress dest) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
