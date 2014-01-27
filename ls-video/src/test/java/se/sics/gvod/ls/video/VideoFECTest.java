package se.sics.gvod.ls.video;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.TestCase;
import org.junit.Ignore;
import org.junit.Test;
import se.sics.gvod.ls.http.HTTPStreamingClient;
import se.sics.gvod.ls.system.LSConfig;
import se.sics.gvod.ls.system.PieceHandler;
import se.sics.gvod.video.msgs.EncodedSubPiece;
import se.sics.gvod.video.msgs.Piece;
import se.sics.gvod.video.msgs.SubPiece;

/**
 *
 * @author Niklas Wahl&#233;n <nwahlen@kth.se>
 */
public class VideoFECTest extends TestCase {
    
    Map<Integer, VideoFEC> fecs;
    Map<Integer, Piece> decodedPieces;
    int seed = 17;
    Random rnd;
    
    public VideoFECTest(String testName) {
        super(testName);
        fecs = new HashMap<Integer, VideoFEC>();
        decodedPieces = new HashMap<Integer, Piece>();
        rnd = new Random(seed);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    // TODO add test methods here. The name must begin with 'test'. For example:
    // public void testHello() {}

    public void testEncodingDecoding() {
        if (LSConfig.FEC_ENCODED_PIECES == LSConfig.FEC_SUB_PIECES) {
            return;
        }
        // create piece
        Piece piece = new Piece(0);
        SubPiece[] sps = new SubPiece[100];
        for (int i = 0; i < sps.length; i++) {
            byte[] data = new byte[SubPiece.SUBPIECE_DATA_SIZE];
            Arrays.fill(data, (byte) i);
            sps[i] = new SubPiece(100 * piece.getId() + i, data, piece);
        }
        piece.setSubPieces(sps);
        System.out.println("Encoding Piece " + piece.getId() + " with " + sps.length + " sub-pieces.");
        System.out.println("Using 5 extra pieces for encoding");
        VideoFEC fecEncode = new VideoFEC(piece);
        
        VideoFEC fecDecode = new VideoFEC(piece.getId());
        assertFalse(fecDecode.isReady());
        System.out.print("Transfering sub-pieces ... skipping sub-pieces: ");
        int piecesToSkip = LSConfig.FEC_ENCODED_PIECES - LSConfig.FEC_SUB_PIECES;
        for (int i = 0; i < fecEncode.getEncodedSubPieces().length; i++) {
            if (piecesToSkip > 0) {
                if (rnd.nextInt(10) < 3) {
                    System.out.print(i + " ");
                }
                piecesToSkip--;
                continue;
            }
            fecDecode.addEncodedSubPiece(fecEncode.getEncodedSubPieces()[i]);
        }
        System.out.println();
        System.out.println(fecDecode.getNumberOfReceivedPieces() + " pieces received");
        assertTrue(fecDecode.isReady());
        System.out.println("Decoding transfered sub-pieces");
        Piece decodedPiece = fecDecode.decode();
        assertEquals(piece, decodedPiece);
        assertEquals(piece.getId(), decodedPiece.getId());
        assertEquals(piece.getSubPieces().length, piece.getSubPieces().length);
        for (int i = 0; i < piece.getSubPieces().length || i < decodedPiece.getSubPieces().length; i++) {
            SubPiece sp = piece.getSubPieces()[i];
            SubPiece decodedSp = decodedPiece.getSubPieces()[i];
            for (int j = 0; j < sp.getData().length || j < decodedSp.getData().length; j++) {
                assertEquals(sp.getData()[j], decodedSp.getData()[j]);
            }
        }
        System.out.println("Decoded Piece " + decodedPiece.getId()
                + " with " + decodedPiece.getSubPieces().length + " sub-pieces, "
                + SubPiece.SUBPIECE_DATA_SIZE + " bytes in each, "
                + "is equal to original Piece");
    }
    
    public void testUsageScenario() {
        if (LSConfig.FEC_ENCODED_PIECES == LSConfig.FEC_SUB_PIECES) {
            return;
        }
        System.out.println("### testUsageScenario ###");
        int nPieces = 10;
        System.out.println("Creating " + nPieces + " Pieces");
        System.out.println("Each Piece contains " + LSConfig.FEC_SUB_PIECES + " SubPieces");
        System.out.println("Each Piece will be transfered as " + LSConfig.FEC_ENCODED_PIECES + " EncodedSubPieces");
        Piece[] pieces = new Piece[nPieces];
        List<EncodedSubPiece> encodedSubPieces = new ArrayList<EncodedSubPiece>();
        for (int i = 0; i < pieces.length; i++) {
            SubPiece[] subPieces = new SubPiece[LSConfig.FEC_SUB_PIECES];
            pieces[i] = new Piece(i, subPieces);
            for (int j = 0; j < subPieces.length; j++) {
                byte[] data = new byte[SubPiece.SUBPIECE_DATA_SIZE];
                Arrays.fill(data, (byte) i);
                subPieces[j] = new SubPiece(j, data, pieces[i]);
            }
            VideoFEC fec = new VideoFEC(pieces[i]);
            for (int j = 0; j < fec.getEncodedSubPieces().length; j++) {
                encodedSubPieces.add(fec.getEncodedSubPiece(j));
            }
        }
        // randomly transfer encoded sub-pieces and let the handler decode them when possible
        while (!encodedSubPieces.isEmpty()) {
            int ni = rnd.nextInt(encodedSubPieces.size());
            deliverEncodedSubPiece(encodedSubPieces.get(ni));
            encodedSubPieces.remove(ni);
        }
        assertEquals(nPieces, decodedPieces.size());
        assertEquals(0, fecs.size());
        // test consistency
        for (int i = 0; i < pieces.length; i++) {
            // uncomment for printing more information
//            NewPiece decodedPiece = decodedPieces.get(pieces[i].getId());
//            System.out.println("Piece " + pieces[i].getId()
//                    + " == decoded Piece " + decodedPiece.getId()
//                    + ": " + pieces[i].equals(decodedPiece));
//            
//            SubPiece[] sps = pieces[i].getSubPieces();
//            SubPiece[] decodedSps = decodedPiece.getSubPieces();
//            for(int ii = 0; ii < sps.length || ii < decodedSps.length; ii++) {
//                System.out.println("\tSubPiece " + sps[ii].getId()
//                        + " == decodedSubPiece " + decodedSps[ii].getId()
//                        + ": " + sps[ii].equals(decodedSps[ii]));
//            }
            assertEquals(pieces[i], decodedPieces.get(pieces[i].getId()));
        }
        System.out.println("Success. Decoded equals original.");
        // Clean up
        fecs.clear();
        decodedPieces.clear();
    }
    int n = 15;
    int k = 10;
    int toBeRemoved = n - k;
    
    public void resetRemove() {
        toBeRemoved = n - k;
    }
    
    public boolean remove() {
        if (toBeRemoved > 0) {
            if (Math.random() > 0.6) {
                return true;
            }
        }
        return false;
    }
    
    public void deliverEncodedSubPiece(EncodedSubPiece p) {
        if (decodedPieces.containsKey(p.getParentId())) {
            return;
        }
        VideoFEC fec = fecs.get(p.getParentId());
        if (fec == null) {
            fec = new VideoFEC(p.getParentId());
            fecs.put(p.getParentId(), fec);
            System.out.println("deliverEncodedSubPiece(): created VideoFEC instance for Piece " + p.getParentId());
            assertEquals(0, fec.getNumberOfReceivedPieces());
        }
        if (!fec.contains(p)) {
            fec.addEncodedSubPiece(p);
            if (fec.isReady()) {
                Piece piece = fec.decode();
                // TODO: make sure no more sub-pieces of this piece is requested
                // this is easiest achieved by decoding this piece, encoding it
                // again and then adding the sub-pieces to the ThreePhaseGossip.
                //  - A problem will be finding out the global IDs.
                //  --- not a problem: just take (for any sub-piece) its global ID
                // minus its encoded ID (which is its index in this Piece) to get
                // the lowest global ID, and corresponding for the highest
                int lowestGlobalID = p.getGlobalId() - p.getEncodedIndex();
                //
                // Signal to concerned classes that the piece is complete
                decodedPieces.put(piece.getId(), piece);
                System.out.println("Piece " + piece.getId() + " complete"
                        + " (after decoding " + fec.getNumberOfReceivedPieces() + " pieces)");
                //
                // TODO: remove this FEC from the Map
                fecs.remove(piece.getId());
            }
        }
    }
    

    @Ignore
    @Test
    public void tWithRealFile() {
        try {
            URL myTestURL = ClassLoader.getSystemClassLoader().getResource("source.mp4");
            File sourceFile = new File(myTestURL.toURI());
            
            String destinationFileName = "VideoFECTest.mp4";
            System.out.println("Reading and encoding " + sourceFile.getName());
            boolean printed = false;
            Map<Integer, Piece> sourcePieceBuffer = new HashMap<Integer, Piece>();
            List<VideoFEC> sourceFecs = new ArrayList<VideoFEC>();
            // source 
            System.out.println("-- Source encode --");
            HTTPStreamingClient stream = new HTTPStreamingClient(sourceFile);
            stream.run();
            while (stream.hasNextPiece()) {
                Piece p = stream.getNextPiece();
                VideoFEC fec = new VideoFEC(p);
                sourceFecs.add(fec);
                if (!printed) {
                    printed = true;
                }
            }
            System.out.println("Read complete, " + sourceFecs.size() + " pieces read.");
            // receiver
            System.out.println("-- Transfer, Receiver decode --");
            Map<Integer, Piece> pieces = new HashMap<Integer, Piece>();
            Map<Integer, VideoFEC> fecs = new HashMap<Integer, VideoFEC>();
            Collections.shuffle(sourceFecs);
            for (VideoFEC sourceFec : sourceFecs) {
                List<EncodedSubPiece> sps = Arrays.asList(sourceFec.getEncodedSubPieces());
                Collections.shuffle(sps);
                for (EncodedSubPiece sp : sps) {
                    VideoFEC fec = fecs.get(sp.getParentId());
                    if (fec == null) {
                        fec = new VideoFEC(sp.getParentId());
                        fecs.put(sp.getParentId(), fec);
                    }
                    
                    if (!fec.isReady()) {
                        fec.addEncodedSubPiece(sp);
                    }
                    
                    if (fec.isReady() && !pieces.containsKey(fec.getId())) {
                        pieces.put(fec.getId(), fec.decode());
                        System.out.println("Piece " + fec.getId() + " decoded");
                    }
                }
            }
            System.out.println("FEC instances created: " + fecs.size());
            PieceHandler.writePieceData(destinationFileName, new ArrayList<Piece>(pieces.values()));
            File destinationFile = new File(destinationFileName);
            destinationFile.deleteOnExit();
            for (int pieceId : sourcePieceBuffer.keySet()) {
                Piece sourcePiece = sourcePieceBuffer.get(pieceId);
                Piece piece = pieces.get(pieceId);
                assert (sourcePiece.equals(piece));
            }
            assert (destinationFile.length() == sourceFile.length());
            // receiver encode
            System.out.println("-- Receiver encode --");
            printed = false;
            List<VideoFEC> receiverFecs = new ArrayList<VideoFEC>();
            for (Piece p : pieces.values()) {
                VideoFEC fec = new VideoFEC(p);
                receiverFecs.add(fec);
                if (!printed) {
                    printed = true;
                }
            }
            // second receiver decode
            System.out.println("-- Transfer 2, Receiver 2 decode --");
            Map<Integer, Piece> pieces2 = new HashMap<Integer, Piece>();
            Map<Integer, VideoFEC> fecs2 = new HashMap<Integer, VideoFEC>();
            Collections.shuffle(receiverFecs);
            for (VideoFEC receiverFec : receiverFecs) {
                List<EncodedSubPiece> sps = Arrays.asList(receiverFec.getEncodedSubPieces());
                Collections.shuffle(sps);
                for (EncodedSubPiece sp : sps) {
                    VideoFEC fec = fecs2.get(sp.getParentId());
                    if (fec == null) {
                        fec = new VideoFEC(sp.getParentId());
                        fecs2.put(sp.getParentId(), fec);
                    }
                    
                    if (!fec.isReady()) {
                        fec.addEncodedSubPiece(sp);
                    }
                    
                    if (fec.isReady() && !pieces2.containsKey(fec.getId())) {
                        pieces2.put(fec.getId(), fec.decode());
                        System.out.println("Piece " + fec.getId() + " decoded");
                    }
                }
            }
            System.out.println("Fecs created: " + fecs.size());
            PieceHandler.writePieceData("2" + destinationFileName, new ArrayList<Piece>(pieces2.values()));
            File destinationFile2 = new File("2" + destinationFileName);
            destinationFile2.deleteOnExit();
            for (int pieceId : sourcePieceBuffer.keySet()) {
                Piece sourcePiece = sourcePieceBuffer.get(pieceId);
                Piece piece = pieces2.get(pieceId);
                assert (sourcePiece.equals(piece));
            }
            assert (destinationFile2.length() == destinationFile.length());
        } catch (URISyntaxException ex) {
            Logger.getLogger(VideoFECTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(VideoFECTest.class.getName()).log(Level.SEVERE, null, ex);
            assert (false);
        }
    }
}
