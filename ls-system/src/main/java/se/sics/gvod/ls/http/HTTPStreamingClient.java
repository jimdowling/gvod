package se.sics.gvod.ls.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.slf4j.LoggerFactory;
import se.sics.gvod.ls.system.PieceHandler;
import se.sics.gvod.video.msgs.Piece;

/**
 * Provides various functionalities for reading HTTP (Live) Streaming data and
 * converting it to Pieces.
 *
 * @author Niklas Wahl&#233;n <nwahlen@kth.se>
 * @see Piece
 * @see <a href="http://en.wikipedia.org/wiki/HTTP_Live_Streaming">Wikipedia:
 * HTTP Live Streaming</a>
 * @see <a href="https://developer.apple.com/resources/http-streaming/">HTTP
 * Live Streaming Resources - Apple Developer</a>
 */
public class HTTPStreamingClient implements Runnable {

    private final org.slf4j.Logger logger = LoggerFactory.getLogger(HTTPStreamingClient.class);
    private List<Piece> pieces;
    private int piecesRead;
    private ByteBuffer buffer;
    private InputStream in;
    private AtomicBoolean reading;

    public HTTPStreamingClient(URL url) throws IOException {
        in = getHTTPStream(url);
        init();
    }

    public HTTPStreamingClient(File file) throws IOException {
        in = new FileInputStream(file);
        init();
    }

    private void init() {
        pieces = Collections.synchronizedList(new ArrayList<Piece>());
        piecesRead = 0;
        buffer = ByteBuffer.allocate(2 * Piece.PIECE_DATA_SIZE);
        reading = new AtomicBoolean(false);
    }

    /**
     * Starts downloading the stream and saving it as Pieces.
     *
     * @throws IOException if the connection to the streaming server fails.
     */
    @Override
    public void run() {
        try {
            reading.set(true);
            byte[] data = new byte[Piece.PIECE_DATA_SIZE];
            int nRead;
            while ((nRead = in.read(data, 0, data.length)) != -1) {
                logger.debug("Read " + nRead + " bytes.");
                buffer.put(data, 0, nRead);
                if (buffer.position() >= Piece.PIECE_DATA_SIZE) {
                    buffer.flip();
                    buffer.get(data, 0, Piece.PIECE_DATA_SIZE);
                    buffer.compact();
                    pieces.add(new Piece(piecesRead, data));
                    piecesRead++;
                }
            }
            if (buffer.position() > 0 || buffer.limit() < buffer.capacity()) {
                // TODO: currently only handling position() < 0
                // (only a matter of flipping/not flipping)
                int position = buffer.position();
                // Create padding for last piece
                buffer.flip();
                buffer.get(data, 0, position);
                buffer.compact();
                logger.debug(this.getClass().getSimpleName() + ": Writing padding code starting at " + piecesRead + "[" + position + "]");
                System.arraycopy(Piece.PADDING_CODE, 0, data, position, Piece.PADDING_CODE.length);
                Arrays.fill(data, position + Piece.PADDING_CODE.length, data.length, (byte) 1);
                pieces.add(new Piece(piecesRead, data));
                piecesRead++;
            }
            in.close();
        } catch (IOException ex) {
            Logger.getLogger(HTTPStreamingClient.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            reading.set(false);
        }
    }

    /**
     * Returns the downloaded
     * <code>Piece</code> with the lowest ID and removes it from the streaming
     * client instance.
     *
     * @return The
     * <code>Piece</code> with the lowest ID of the remaining pieces. Returns
     * <code>null</code> if there are currently no Pieces available.
     */
    public Piece getNextPiece() {
        if (hasNextPiece()) {
            return pieces.remove(0);
        } else {
            return null;
        }
    }

    /**
     * Indicates if there are any new Pieces downloaded.
     *
     * @return
     * <code>true</code> if there are any new downloaded Pieces ready.
     */
    public boolean hasNextPiece() {
        return !pieces.isEmpty();
    }

    private InputStream getHTTPStream(URL url) throws IOException {
        URLConnection streamConnection = url.openConnection();

        logger.info(this.getClass().getSimpleName() + ": Waiting for stream connection ...");
        while (true) {
            boolean connected = true;
            try {
                streamConnection.connect();
            } catch (IOException ex) {
                connected = false;
            }
            if (connected) {
                logger.info(this.getClass().getSimpleName() + ": Connected.");
                break;
            }
        }
        return streamConnection.getInputStream();
    }

    public boolean isReading() {
        return reading.get();
    }
    
        public static void main(String args[]) {
        List<Piece> pieces = new ArrayList<Piece>();
        try {
            //HTTPStreamingClient client = new HTTPStreamingClient("http://127.0.0.1/~niklas/source.mp4");
            HTTPStreamingClient client = new HTTPStreamingClient(new URL("http://127.0.0.1:8080"));
            client.run();
            while (client.hasNextPiece()) {
                Piece p = client.getNextPiece();
                pieces.add(p);
            }
            System.out.println(HTTPStreamingClient.class.getSimpleName() + ": Read " + pieces.size() + " pieces.");
            PieceHandler.writePieceData("stream.mp4", pieces);
        } catch (IOException ex) {
            Logger.getLogger(HTTPStreamingClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
