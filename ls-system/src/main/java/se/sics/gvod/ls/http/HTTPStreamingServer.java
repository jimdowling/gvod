package se.sics.gvod.ls.http;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.slf4j.LoggerFactory;
import se.sics.gvod.ls.system.LSConfig;
import se.sics.gvod.video.msgs.Piece;
import se.sics.gvod.video.msgs.SubPiece;

/**
 * Reads Pieces and provides their data content through a HTTP stream.
 *
 * @author Niklas Wahl&#233;n <nwahlen@kth.se>
 */
public class HTTPStreamingServer {

    private final org.slf4j.Logger logger = LoggerFactory.getLogger(HTTPStreamingServer.class);
    private HTTPRequestHandler handler;
    private HttpServer server;

    public HTTPStreamingServer(InetSocketAddress serverAddress) throws IOException {
        handler = new HTTPRequestHandler();
        try {
        server = HttpServer.create(serverAddress, 0);
        server.createContext("/", handler);
        server.setExecutor(null);
        server.start();
        logger.info("Server started on "
                + server.getAddress().getAddress().getHostAddress()
                + ":" + server.getAddress().getPort());
        } catch (BindException be) {
            if(LSConfig.isSimulation()) {
                logger.error("Couldn't start server: " + be.getMessage() + " (OK during simulation)");
            } else {
                throw be;
            }
        } 
    }

    public void deliver(Piece piece) throws IOException {
        for(SubPiece sp : piece.getSubPieces())
        handler.deliver(sp);
    }
    
    public void deliver(SubPiece sp) throws IOException {
        handler.deliver(sp);
    }

    public void stop() {
        if (server != null) {
            try {
                handler.stop();
            } catch (IOException ex) {
                Logger.getLogger(HTTPStreamingServer.class.getName()).log(Level.SEVERE, null, ex);
            }
            server.stop(0);
            logger.debug("Server stopped");
        }
    }

    public static void main(String args[]) {
        try {
            HTTPStreamingServer streamingServer = new HTTPStreamingServer(new InetSocketAddress(8081));
            HTTPStreamingClient inStream = new HTTPStreamingClient(new URL("http://127.0.0.1:8080"));
//            HTTPStreamingClient inStream = new HTTPStreamingClient("http://sverigesradio.se/topsy/direkt/164-mp3.asx");
//            HTTPStreamingClient inStream = new HTTPStreamingClient(new File("source.mp4"));
            System.out.println("HTTP Server waiting for request");
            new Thread(inStream).start();
            while (!inStream.isReading() || inStream.hasNextPiece()) {
                if (inStream.hasNextPiece()) {
                    Piece p = inStream.getNextPiece();
                    System.out.println("HTTP Server: new Piece " + p.getId());
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(HTTPStreamingServer.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    streamingServer.deliver(p);
                }
            }
            streamingServer.stop();
        } catch (IOException ex) {
            Logger.getLogger(HTTPStreamingServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
