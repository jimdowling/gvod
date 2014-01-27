package se.sics.gvod.ls.ws;

import java.util.ArrayList;
import java.util.List;
import org.junit.*;
import se.sics.gvod.ls.video.snapshot.Stats;
import se.sics.gvod.ls.ws.persistent.StatsEntity;
import se.sics.gvod.net.VodAddress;

/**
 *
 * @author Niklas Wahl&#233;n <nwahlen@kth.se>
 */
public class WSTest {

    // Experiment identification
    private int experimentId = 1;
    private int experimentIteration = 2;
    private volatile int step = 4;
    // Node identification
    private int overlayId = 5;
    private int nodeId = 6;
    // Node properties
    private boolean source = false;
    private VodAddress.NatType natType = VodAddress.NatType.NAT;
    // Neighbour connections messages
    private volatile int connectionRequestsSentClose = 7;
    private volatile int connectionRequestsSentRandom = 8;
    private volatile int connectionRequestTimeoutsClose = 9;
    private volatile int connectionRequestTimeoutsRandom = 10;
    private volatile int connectionResponsesReceivedClose = 11;
    private volatile int connectionResponsesReceivedRandom = 12;
    // Neighbour disconncetion messages
    private volatile int disconnectsSentClose = 13;
    private volatile int disconnectsSentRandom = 14;
    private volatile int disconnectsReceivedClose = 15;
    private volatile int disconnectsReceivedRandom = 16;
    // Neighbour connections
    private volatile int ingoingConnectionsClose = 17;
    private volatile int ingoingConnectionsRandom = 18;
    private volatile int outgoingConnectionsClose = 19;
    private volatile int outgoingConnectionsRandom = 20;
    private volatile int avgAsHopsToNeighbours = 21;
    private List<VodAddress> neighbours = new ArrayList<VodAddress>();
    // Video content
    private volatile int seenSubPieces = 22;
    private volatile int completedPieces = 23;
    private volatile int highestCompletedPiece = 24;
    private volatile int downloadedSubPiecesClose = 25;
    private volatile int downloadedSubPiecesRandom = 26;
    private volatile int sentSubPiecesClose = 27;
    private volatile int sentSubPiecesRandom = 28;
    private List<Integer> pieceStats = new ArrayList<Integer>();
    private volatile int dlBwBytes = 122;
    private volatile int ulBwBytes = 211;
    // Playback
    private volatile int bufferLength = 29;
    private volatile int missedPieces = 30;
    //
    Stats stats;
    StatsEntity statsEntity;

    public WSTest() {
        pieceStats.add(134353464);
        //
        stats = new Stats();
        stats.setExperimentId(experimentId);
        stats.setExperimentIteration(experimentIteration);
        stats.setStep(step);
        //
        stats.setOverlayId(overlayId);
        stats.setNodeId(nodeId);
        //
        stats.setSource(source);
        stats.setNatType(natType);
        //
        stats.setConnectionRequestsSentClose(connectionRequestsSentClose);
        stats.setConnectionRequestsSentRandom(connectionRequestsSentRandom);
        stats.setConnectionRequestTimeoutsClose(connectionRequestTimeoutsClose);
        stats.setConnectionRequestTimeoutsRandom(connectionRequestTimeoutsRandom);
        stats.setConnectionResponsesReceivedClose(connectionResponsesReceivedClose);
        stats.setConnectionResponsesReceivedRandom(connectionResponsesReceivedRandom);
        //
        stats.setDisconnectsSentClose(disconnectsSentClose);
        stats.setDisconnectsSentRandom(disconnectsSentRandom);
        stats.setDisconnectsReceivedClose(disconnectsReceivedClose);
        stats.setDisconnectsReceivedRandom(disconnectsReceivedRandom);
        //
        stats.setIngoingConnectionsClose(ingoingConnectionsClose);
        stats.setIngoingConnectionsRandom(ingoingConnectionsRandom);
        stats.setOutgoingConnectionsClose(outgoingConnectionsClose);
        stats.setOutgoingConnectionsRandom(outgoingConnectionsRandom);
        stats.setAvgAsHopsToNeighbours(avgAsHopsToNeighbours);
        stats.setNeighbours(neighbours);
        //
        stats.setSeenSubPieces(seenSubPieces);
        stats.setCompletePieces(completedPieces);
        stats.setHighestCompletePiece(highestCompletedPiece);
        stats.setDownloadedSubPiecesIntraAs(downloadedSubPiecesClose);
        stats.setDownloadedSubPiecesNeighbourAs(downloadedSubPiecesRandom);
        stats.setSentSubPiecesIntraAs(sentSubPiecesClose);
        stats.setSentSubPiecesNeighbourAs(sentSubPiecesRandom);
        stats.setDlBwBytes(dlBwBytes);
        stats.setUlBwBytes(ulBwBytes);
        stats.setPieceStats(pieceStats);
        //
        stats.setBufferLength(bufferLength);
        stats.setMissedPieces(missedPieces);
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

    public void testStatsToStatsEntity() {
        StatsEntity testSE = WS.getStatsEntity(stats);
        assert(experimentId==testSE.getExperimentId().intValue());
        assert(experimentIteration==testSE.getExperimentIteration().intValue());
        //
        assert(dlBwBytes==testSE.getDlBwBytes());
        assert(ulBwBytes==testSE.getUlBwBytes());
    }
}
