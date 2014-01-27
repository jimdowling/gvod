package se.sics.gvod.ls;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;
import se.sics.gvod.ls.http.HTTPStreamingClient;
import se.sics.gvod.ls.system.PieceHandler;
import se.sics.gvod.video.msgs.Piece;

/**
 *
 * @author Niklas Wahl&#233;n <nwahlen@kth.se>
 */
public class HTTPStreamingTest extends TestCase {

    public HTTPStreamingTest(String testName) {
        super(testName);
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

    public void testStreamToPiecesToStream() {
        try {
            File sourceFile = new File("source.mp4");
            HTTPStreamingClient client = new HTTPStreamingClient(sourceFile);
            PieceHandler handler = new PieceHandler();
            List<Piece> pieces = new ArrayList<Piece>();
            client.run();
            while (client.hasNextPiece()) {
                Piece p = client.getNextPiece();
                pieces.add(p);
            }
            PieceHandler.writePieceData("test.mp4", pieces);

            File destFile = new File("test.mp4");
            assert (sourceFile.length() == destFile.length());
            System.out.printf("sourceFile.length == destFile.length == %d (%.1f MB)\n", destFile.length(), ((float) destFile.length() / (1000.0 * 1000.0)));
            destFile.deleteOnExit();
        } catch (IOException ex) {
            System.out.println("Source file not found, skipping test");
        }
    }
}
