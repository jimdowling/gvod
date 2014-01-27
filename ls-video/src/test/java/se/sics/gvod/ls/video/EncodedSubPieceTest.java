package se.sics.gvod.ls.video;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Ignore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.gvod.address.Address;
import se.sics.gvod.common.UtilityVod;
import se.sics.gvod.config.VodConfig;
import se.sics.gvod.ls.http.HTTPStreamingClient;
import se.sics.gvod.net.NatNetworkControl;
import se.sics.gvod.net.NettyInit;
import se.sics.gvod.net.NettyNetwork;
import se.sics.gvod.net.Transport;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.net.VodMsgFrameDecoder;
import se.sics.gvod.net.VodNetwork;
import se.sics.gvod.net.events.PortBindRequest;
import se.sics.gvod.net.events.PortBindResponse;
import se.sics.gvod.timer.Timer;
import se.sics.gvod.timer.UUID;
import se.sics.gvod.timer.java.JavaTimer;
import se.sics.gvod.video.msgs.EncodedSubPiece;
import se.sics.gvod.video.msgs.Piece;
import se.sics.gvod.video.msgs.SubPiece;
import se.sics.gvod.video.msgs.VideoPieceMsg;
import se.sics.kompics.*;
import se.sics.kompics.nat.utils.getip.ResolveIp;
import se.sics.kompics.nat.utils.getip.ResolveIpPort;
import se.sics.kompics.nat.utils.getip.events.GetIpRequest;
import se.sics.kompics.nat.utils.getip.events.GetIpResponse;

/**
 * Unit test for simple App.
 */
public class EncodedSubPieceTest
        extends TestCase {

    private static final Logger logger = LoggerFactory.getLogger(EncodedSubPieceTest.class);
    private boolean testStatus = true;

    /**
     * Create the test case
     *
     */
    public EncodedSubPieceTest(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(EncodedSubPieceTest.class);
    }

    public static void setTestObj(EncodedSubPieceTest testObj) {
        EncodedSubPieceTest.TestStClientComponent.testObj = testObj;
    }

    public static class TestStClientComponent extends ComponentDefinition {

        private Component client2;
        private Component client;
        private Component server;
        private Component timer;
        private Component resolveIp;
        private static EncodedSubPieceTest testObj = null;
        private VodAddress client2Addr;
        private VodAddress clientAddr;
        private VodAddress serverAddr;
        private UtilityVod utility = new UtilityVod(10, 200, 15);
        // EncodedSubPieceTest
        Map<Integer, VideoFEC> fecs, decodeFecs, decodeFecs2;
        int numberOfTestSubPieces, numberOfTestSubPiecesLeft, numberOfTestSubPiecesLeft2;
        Random random;

        public TestStClientComponent() {
            timer = create(JavaTimer.class);
            client2 = create(NettyNetwork.class);
            client = create(NettyNetwork.class);
            server = create(NettyNetwork.class);
            resolveIp = create(ResolveIp.class);

            connect(client.getNegative(Timer.class), timer.getPositive(Timer.class));
            connect(client2.getNegative(Timer.class), timer.getPositive(Timer.class));
            connect(server.getNegative(Timer.class), timer.getPositive(Timer.class));

            subscribe(handleStart, control);
            subscribe(handleMsgTimeout, timer.getPositive(Timer.class));
            subscribe(handleConnectRequest, server.getPositive(VodNetwork.class));
            subscribe(handleDataResponse, client.getPositive(VodNetwork.class));
            subscribe(handleDataResponse, client2.getPositive(VodNetwork.class));
            subscribe(handleGetIpResponse, resolveIp.getPositive(ResolveIpPort.class));            
        }
        public Handler<Start> handleStart = new Handler<Start>() {

            @Override
            public void handle(Start event) {
                System.out.println("Starting");
                trigger(new GetIpRequest(false), 
                        resolveIp.getPositive(ResolveIpPort.class));

            }
        };
        
        public Handler<GetIpResponse> handleGetIpResponse = new Handler<GetIpResponse>() {
            @Override
            public void handle(GetIpResponse event) {

            InetAddress ip = event.getBoundIp();
            int client2Port = 59878;
            int clientPort = 59877;
            int serverPort = 33221;
            try {
                ip = InetAddress.getLocalHost();

            } catch (UnknownHostException ex) {
                logger.error("UnknownHostException");
                testObj.fail();
            }
            Address cAddr = new Address(ip, clientPort, 0);
            Address c2Addr = new Address(ip, client2Port, 2);
            Address sAddr = new Address(ip, serverPort, 1);

            clientAddr = new VodAddress(cAddr, VodConfig.SYSTEM_OVERLAY_ID);
            client2Addr = new VodAddress(c2Addr, VodConfig.SYSTEM_OVERLAY_ID);
            serverAddr = new VodAddress(sAddr, VodConfig.SYSTEM_OVERLAY_ID);

            trigger(new NettyInit(222, false, VodMsgFrameDecoder.class),
                    client.getControl());
            PortBindRequest pb1 = new PortBindRequest(clientAddr.getPeerAddress(), 
                    Transport.UDP);
            PortBindResponse pbr1 = new PortBindResponse(pb1) { };
            trigger(pb1, client.getPositive(NatNetworkControl.class));
                
            trigger(new NettyInit(99, false, VodMsgFrameDecoder.class),
                    client2.getControl());
            pb1 = new PortBindRequest(client2Addr.getPeerAddress(), Transport.UDP);
            pbr1 = new PortBindResponse(pb1) { };
            trigger(pb1, client2.getPositive(NatNetworkControl.class));
            
            trigger(new NettyInit(77, false, VodMsgFrameDecoder.class),
                    server.getControl());
            pb1 = new PortBindRequest(serverAddr.getPeerAddress(), Transport.UDP);
            pbr1 = new PortBindResponse(pb1) { };
            trigger(pb1, server.getPositive(NatNetworkControl.class));

            // EncodedSubPieceTest
            HTTPStreamingClient stream = null;
            fecs = new HashMap<Integer, VideoFEC>();
            decodeFecs = new HashMap<Integer, VideoFEC>();
            decodeFecs2 = new HashMap<Integer, VideoFEC>();
            random = new Random();
            try {

                URL srcUrl = ClassLoader.getSystemClassLoader().getResource("source.mp4");
                File sourceFile = new File(srcUrl.toURI());
                stream = new HTTPStreamingClient(sourceFile);
                stream.run();
            } catch (URISyntaxException ex) {
                java.util.logging.Logger.getLogger(EncodedSubPieceTest.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                java.util.logging.Logger.getLogger(EncodedSubPieceTest.class.getName()).log(Level.SEVERE, null, ex);
            }
            while (stream.hasNextPiece()) {
                VideoFEC fec = new VideoFEC(stream.getNextPiece());
                fecs.put(fec.getId(), fec);
            }

            numberOfTestSubPieces = fecs.size() * Piece.SUBPIECES;
            numberOfTestSubPiecesLeft = numberOfTestSubPieces;
            numberOfTestSubPiecesLeft2 = numberOfTestSubPieces;

                Set<EncodedSubPiece> pieceSet;
                System.out.println("Sending " + numberOfTestSubPieces + " sub-pieces");
                for (VideoFEC fec : fecs.values()) {
                    // Send in random order and drop some packets
                    List<Integer> ids = new ArrayList<Integer>();
                    for (int i = 0; i < fec.getEncodedSubPieces().length; i++) {
                        ids.add(i);
                    }
                    for (int i = fec.getEncodedSubPieces().length - 1; i >= 0; i--) {
//                        if (i % 30 == 0) {
//                            continue;
//                        }
                        EncodedSubPiece esp = fec.getEncodedSubPiece(ids.remove(random.nextInt(ids.size())));
                        VideoPieceMsg.Response piecesMsg = new VideoPieceMsg.Response(serverAddr, clientAddr, UUID.nextUUID(), esp);
                        trigger(piecesMsg, server.getPositive(VodNetwork.class));
                        try {
                            // Do not overload the receiver's buffer
                            Thread.sleep(5);
                        } catch (InterruptedException ex) {
                            java.util.logging.Logger.getLogger(EncodedSubPieceTest.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
                System.out.println("Source: done sending.");
            
            
            }
        };
        
        public Handler<VideoPieceMsg.Request> handleConnectRequest = new Handler<VideoPieceMsg.Request>() {

            @Override
            public void handle(VideoPieceMsg.Request r) {
                System.out.println("Piece Request");
            }
        };
        public Handler<VideoPieceMsg.Response> handleDataResponse = new Handler<VideoPieceMsg.Response>() {

            @Override
            public void handle(VideoPieceMsg.Response r) {
                int myId = r.getDestination().getId();
                EncodedSubPiece esp = r.getEncodedSubPiece();
                handleEncodedSubPiece(esp, myId);
                if (myId == clientAddr.getId()) {
                    if (--numberOfTestSubPiecesLeft == 0) {
                        System.out.println(myId + ": All sub-pieces received, writing to file.");
                        List<Piece> pieces = new ArrayList<Piece>();
                        for (VideoFEC fec : fecs.values()) {
                            pieces.add(fec.getPiece());
                        }
//                        try {
//                            File destFile = new File("EncodedSubPieceTest" + myId + ".mp4");
//                            destFile.deleteOnExit();
//                            PieceHandler.writePieceData(destFile.getName(), pieces);
//                            assertEquals(new File("source.mp4").length(), destFile.length());
//                        } catch (IOException ex) {
//                            java.util.logging.Logger.getLogger(EncodedSubPieceTest.class.getName()).log(Level.SEVERE, null, ex);
//                        }
                        System.out.println(myId + ": Done, waiting for second client.");
                    }
                } else if (myId == client2Addr.getId()) {
                    if (--numberOfTestSubPiecesLeft2 == 0) {
                        System.out.println(myId + ": All sub-pieces received, writing to file.");
                        List<Piece> pieces = new ArrayList<Piece>();
                        for (VideoFEC fec : fecs.values()) {
                            pieces.add(fec.getPiece());
                        }
//                        try {
//                            File destFile = new File("EncodedSubPieceTest" + myId + ".mp4");
//                            destFile.deleteOnExit();
//                            PieceHandler.writePieceData(destFile.getName(), pieces);
//                            assertEquals(new File("source.mp4").length(), destFile.length());
//                        } catch (IOException ex) {
//                            java.util.logging.Logger.getLogger(EncodedSubPieceTest.class.getName()).log(Level.SEVERE, null, ex);
//                        }
                        System.out.println(myId + ": Done.");
                        // Clean up
                        fecs.clear();
                        decodeFecs.clear();
                        decodeFecs2.clear();
                        trigger(new Stop(), client.getControl());
                        trigger(new Stop(), client2.getControl());
                        trigger(new Stop(), server.getControl());
                        testObj.pass();
                    }
                }
            }
        };
        public Handler<VideoPieceMsg.RequestTimeout> handleMsgTimeout = new Handler<VideoPieceMsg.RequestTimeout>() {

            @Override
            public void handle(VideoPieceMsg.RequestTimeout rt) {
                trigger(new Stop(), client.getControl());
                trigger(new Stop(), server.getControl());
                System.out.println("Msg timeout");
                testObj.testStatus = false;
                testObj.fail(true);
            }
        };

        public void handleEncodedSubPiece(EncodedSubPiece esp, int nodeId) {
            if (nodeId == clientAddr.getId()) {
                VideoFEC decodeFec = decodeFecs.get(esp.getParentId());
                if (decodeFec == null) {
                    decodeFec = new VideoFEC(esp.getParentId());
                    decodeFecs.put(decodeFec.getId(), decodeFec);
                }
                if (!decodeFec.isReady()) {
                    decodeFec.addEncodedSubPiece(esp);
                    if (decodeFec.isReady()) {
                        Piece decodedPiece = decodeFec.decode();

                        // compare to original
                        VideoFEC encodeFec = fecs.get(decodeFec.getId());
                        Piece piece = encodeFec.getPiece();
                        for (SubPiece sp : piece.getSubPieces()) {
                            SubPiece dsp = decodedPiece.getSubPieces()[sp.getId()];
                            if (!sp.equals(dsp)) {
                                System.out.println(nodeId + ": decoded SubPiece " + dsp.getId() + " in Piece " + dsp.getParent().getId() + " does not equal original ("
                                        + sp.getId() + ")"
                                        + " - " + new String(Arrays.copyOf(dsp.getData(), 32), Charset.forName("US-ASCII"))
                                        + " != " + new String(Arrays.copyOf(sp.getData(), 32), Charset.forName("US-ASCII")));
                            }
                        }
                        assertEquals(piece, decodedPiece);

                        VideoFEC reencodeFec = new VideoFEC(decodedPiece);
                        assertTrue(Arrays.deepEquals(encodeFec.getEncodedSubPieces(), reencodeFec.getEncodedSubPieces()));
                        // Send to second client
                        Set<EncodedSubPiece> pieceSet;
                        // Send in reverse order and drop some packets
                        for (int i = encodeFec.getEncodedSubPieces().length - 1; i >= 0; i--) {
                            if (i % 25 == 0) {
                                continue;
                            }
                            esp = encodeFec.getEncodedSubPiece(i);
                            VideoPieceMsg.Response piecesMsg = new VideoPieceMsg.Response(clientAddr, client2Addr, UUID.nextUUID(), esp);
                            trigger(piecesMsg, client.getPositive(VodNetwork.class));
                            try {
                                // Do not overload the receiver's buffer
                                Thread.sleep(5);
                            } catch (InterruptedException ex) {
                                java.util.logging.Logger.getLogger(EncodedSubPieceTest.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    }
                }

            } else if (nodeId == client2Addr.getId()) {
                VideoFEC decodeFec = decodeFecs2.get(esp.getParentId());
                if (decodeFec == null) {
                    decodeFec = new VideoFEC(esp.getParentId());
                    decodeFecs2.put(decodeFec.getId(), decodeFec);
                }
                if (!decodeFec.isReady()) {
                    decodeFec.addEncodedSubPiece(esp);
                    if (decodeFec.isReady()) {
                        Piece decodedPiece = decodeFec.decode();

                        // compare to original
                        VideoFEC encodeFec = fecs.get(decodeFec.getId());
                        Piece piece = encodeFec.getPiece();
                        assertEquals(piece, decodedPiece);

                        VideoFEC reencodeFec = new VideoFEC(decodedPiece);
                        assertTrue(Arrays.deepEquals(encodeFec.getEncodedSubPieces(), reencodeFec.getEncodedSubPieces()));
                    }
                }
            }

        }
    }
    private static final int EVENT_COUNT = 1;
    private static Semaphore semaphore = new Semaphore(0);

    private void allTests() {
        int i = 0;

        runInstance();
        if (testStatus == true) {
            assertTrue(true);
        }
    }

    private void runInstance() {
        Kompics.createAndStart(EncodedSubPieceTest.TestStClientComponent.class, 1);
        try {
            EncodedSubPieceTest.semaphore.acquire(EVENT_COUNT);
            System.out.println("Finished test.");
        } catch (InterruptedException e) {
            assert (false);
        } finally {
            Kompics.shutdown();
        }
        if (testStatus == false) {
            assertTrue(false);
        }

    }

    @Ignore
    public void testApp() {
        setTestObj(this);

//        allTests();
    }

    public void pass() {
        EncodedSubPieceTest.semaphore.release();
    }

    public void fail(boolean release) {
        testStatus = false;
        EncodedSubPieceTest.semaphore.release();
    }
}
