package se.sics.gvod.ls.http;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import org.slf4j.LoggerFactory;
import se.sics.gvod.video.msgs.Piece;
import se.sics.gvod.video.msgs.SubPiece;

/**
 *
 * @author Niklas Wahl&#233;n <nwahlen@kth.se>
 */
public class HTTPRequestHandler implements HttpHandler {

    private final org.slf4j.Logger logger = LoggerFactory.getLogger(HTTPRequestHandler.class);
    private OutputStream os;

    public HTTPRequestHandler() {
    }

    @Override
    public void handle(HttpExchange he) throws IOException {
        Headers headers = he.getResponseHeaders();
        // Header according to VLC HLS response
        headers.add("Content-Type", "application/octet-stream");
        headers.add("Cache-Control", "no-cache");
        he.sendResponseHeaders(200, 0);
        // Get OutputStream to body
        os = he.getResponseBody();
        logger.info("New OutputStream connected");
    }

    public void deliver(SubPiece sp) throws IOException {
        logger.debug("Writing SubPiece " + sp.getParent() + "," + sp.getId());
        if (os != null) {
            os.write(sp.getData());
            os.flush();
        }
    }

    public void stop() throws IOException {
        if (os != null) {
            os.close();
            logger.info("OutputStream closed");
        }
    }
}
