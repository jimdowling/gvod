    /*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.system.util;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import org.json.simple.JSONValue;
import org.mortbay.util.URIUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.gvod.config.VodConfig;
import se.sics.gvod.system.main.SwingMain;
import se.sics.gvod.system.util.ActiveTorrents.TorrentEntry;
import se.sics.kompics.Kompics;

/**
 * 
 * @author jdowling
 */
public class BrowserGuiHandler implements HttpHandler {

    private static final String VIDEO_NAME = "myvideo.flv";
    private static final String FLOWPLAYER = "flow.html";
    private static final String JWPLAYER = "gvod.html";
    private static final Logger logger = LoggerFactory.getLogger(BrowserGuiHandler.class);
    private SwingMain main;

    public BrowserGuiHandler(SwingMain main) {
        this.main = main;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        logger.trace(exchange.getRequestMethod() + "\n"
                + exchange.getRequestHeaders().entrySet() + "\n"
                + exchange.getResponseHeaders().entrySet());

        String requestMethod = exchange.getRequestMethod();
        if (requestMethod.equalsIgnoreCase("GET")) {

            URI uri = exchange.getRequestURI();
            String filename = uri.getPath().substring(1);
            StringBuffer sb = new StringBuffer();
            URIUtil.encodePath(sb, filename);

            Map<String, Object> params
                    = (Map<String, Object>) exchange.getAttribute("parameters");
            String response = null;
            byte[] byteResponse = null;

            // JSON tutorial
            // http://code.google.com/p/json-simple/
            if (params.containsKey("stats")) {
//                response = handleStatsQuery(exchange);
            } else if (params.containsKey("method")) {
                String fn = (String) params.get("method");
                if (fn.compareToIgnoreCase("get_speed_info") == 0) {
                    response = handleGetSpeedInfo(exchange);
                }
            } else if (params.containsKey("view")) {
                String video = (String) params.get("view");
                response = handleViewVideo(exchange, video);
            } else if (params.containsKey("shutdown")) {
//  Sent by the plugin when it is stopped by the browser.
//  Allows the cleanup allocated resources.
                handleShutdown(exchange);
            } else if (params.containsKey("info")) {

                //Sent to convey its current status to the plugin,
                // which should be passed on to the user.
                handleInfo(exchange);
            } else {
                // This is the fileserver part - swfobject.js, player.swf, *.htm, etc
                String postfix
                        = filename.substring(filename.lastIndexOf(".") + 1, filename.length());
                if (postfix.compareToIgnoreCase("swf") == 0
                        || postfix.compareToIgnoreCase("ico") == 0) {
                    byteResponse = handleBinaryFile(exchange, sb.toString(), postfix);
                } else {
                    response = handleTextFile(exchange, sb.toString(), postfix);
                }
            }

            OutputStream responseBody = exchange.getResponseBody();
            if (response != null) {
                responseBody.write(response.getBytes(Charset.defaultCharset()));
            } else if (byteResponse != null) {
                responseBody.write(byteResponse);
            } else {
                logger.warn("Bad request issued by client: " + requestMethod);
                throw new IllegalStateException("Something wrong.");
            }
            responseBody.flush();
            responseBody.close();
        }
    }

    /**
     *
     * @param exchange
     * @param filename
     * @param postfix
     * @return null if unsuccessful
     * @throws IOException
     */
    private byte[] handleBinaryFile(HttpExchange exchange, String filename,
            String postfix)
            throws IOException {
        String type = null;
        if (postfix.compareToIgnoreCase("swf") == 0) {
            type = "application/x-shockwave-flash";
        } else if (postfix.compareToIgnoreCase("ico") == 0) {
            type = "image/x-icon";
        }

//HTTP/1.1 200 OK
//Last-Modified: Thu, 12 Mar 2009 17:09:30 GMT
//ETag: "1036-464ef0c1c8680"
//Server: Apache/2.2.11 (Unix)
//X-Cache-TTL: 568
//X-Cached-Time: Thu, 21 Jan 2010 14:55:37 GMT
//Accept-Ranges: bytes
//Content-Length: 4150
//Content-Type: image/x-icon
//Cache-Control: max-age=463
//Expires: Sun, 12 Sep 2010 14:22:09 GMT
//Date: Sun, 12 Sep 2010 14:14:26 GMT
//Connection: keep-alive
        File f = new File(VodConfig.getTorrentDir()
                + File.separator + "www"
                + File.separator + filename);

        long length = 0;
        byte[] buffer = null;

        if (f.exists()) {
            length = f.length();
            if (length > Integer.MAX_VALUE) {
                throw new IllegalStateException("File was too large");
            }
            BufferedInputStream bis;
            FileInputStream in = null;
            try {
                in = new FileInputStream(f);
                bis = new BufferedInputStream(in);
                buffer = new byte[(int) length];
                int offset = 0;
                int numRead = 0;
                while (offset < buffer.length
                        && (numRead = bis.read(buffer, offset, buffer.length - offset)) >= 0) {
                    offset += numRead;
                }

                // Ensure all the bytes have been read in
                if (offset < length) {
                    throw new IOException("Could not completely read file " + f.getName());
                }

            } finally {
                if (in != null) {
                    in.close();
                }
            }
        }

        if (buffer != null) {

            Headers responseHeaders = exchange.getResponseHeaders();
            responseHeaders.set("Content-Type", type);
            responseHeaders.set("Connection", "Keep-Alive");
            responseHeaders.set("Keep-Alive", "timeout=15, max=100");
            responseHeaders.set("Accept-Ranges", "bytes");
            responseHeaders.set("Content-Length", "" + length);
            exchange.sendResponseHeaders(200 /* http ok code*/,
                    0);
        } else {
            // TODO - broken send correct error code
//            Headers responseHeaders = exchange.getResponseHeaders();
//            responseHeaders.set("Content-Type", "text/html");
            exchange.sendResponseHeaders(404 /* file not found http error*/,
                    0);
        }
        return buffer;
    }

    private String handleTextFile(HttpExchange exchange, String filename, String postfix)
            throws IOException {
        StringBuilder sb = new StringBuilder();
        // for .html & .js
        String type = "text/html";
        if (postfix.compareToIgnoreCase("html") == 0
                || postfix.compareToIgnoreCase("js") == 0
                || postfix.compareToIgnoreCase("css") == 0) {
            type = "text/" + postfix;
        }

        File f = new File(VodConfig.getTorrentDir()
                + File.separator + "www"
                + File.separator + filename);
        boolean fileProb = false;

        long length = 0;

        if (f.exists()) {
            length = f.length();
            if (length > Integer.MAX_VALUE) {
                throw new IllegalStateException("File was too large");
            }
            try {
                ReadTextFileWithEncoding rf
                        = new ReadTextFileWithEncoding(f, "ISO8859_1");
                String text = rf.getText();
                sb.append(text);
            } catch (IOException e) {
                fileProb = true;
            }
        } else {
            fileProb = true;
            logger.warn("HTTP SERVER: Could not find file: " + f.getAbsolutePath());
        }

        Headers responseHeaders = exchange.getResponseHeaders();
        responseHeaders.set("Content-Type", type);
        responseHeaders.set("Connection", "Keep-Alive");
        responseHeaders.set("Keep-Alive", "timeout=15, max=100");
        responseHeaders.set("Accept-Ranges", "bytes");

        responseHeaders.set("Content-Length", "" + sb.length());
        if (fileProb) {
            exchange.sendResponseHeaders(404 /* http file not found*/,
                    0);
        } else {
            exchange.sendResponseHeaders(200 /* http ok code */,
                    0);
        }
        return sb.toString();
    }

    private String handleViewVideo(HttpExchange exchange, String videoName)
            throws IOException {
        // return URL for .flv or .mp4 file to flashplayer

        StringBuffer sb = new StringBuffer();
        StringBuffer encodedVideoName = new StringBuffer();
        URIUtil.encodePath(encodedVideoName, videoName);

        String htmlFile = VodConfig.getTorrentDir()
                + File.separator + "www"
                + File.separator;

        if (SwingMain.PLAYER == 0) {
            htmlFile += JWPLAYER;
        } else if (SwingMain.PLAYER == 1) {
            htmlFile += FLOWPLAYER;
        } else {
            throw new IllegalStateException("Illegal parameter for media player.");
        }

        // Always stored the escaped (URI-encoded) video name
        TorrentEntry torrentEntry = ActiveTorrents.getTorrentEntry(encodedVideoName.toString());

        String width = Integer.toString(torrentEntry.getWidth());
        String height = Integer.toString(torrentEntry.getHeight());

        File f = new File(htmlFile);
        boolean fileProb = false;

        if (f.exists()) {
            try {
                ReadTextFileWithEncoding rf
                        = new ReadTextFileWithEncoding(f, "ISO8859_9");
                String text = rf.getText();
                text = text.replace(VIDEO_NAME, encodedVideoName.toString());
                text = text.replace("640", width);
                text = text.replace("320", height);
                sb.append(text);
            } catch (IOException e) {
                fileProb = true;
            }
        } else {
            fileProb = true;
        }

        if (fileProb) {
            exchange.sendResponseHeaders(404 /* http file not found*/, 0);
        } else {
            exchange.sendResponseHeaders(200 /* http ok code */, 0);
        }
        return sb.toString();
    }

    private void handleShutdown(HttpExchange exchange)
            throws IOException {
        Kompics.shutdown();
        System.exit(-1);
    }

    private String handleInfo(HttpExchange exchange)
            throws IOException {
        return null;
    }

    private String handleGetSpeedInfo(HttpExchange exchange) throws IOException {
        Headers responseHeaders = exchange.getResponseHeaders();
        responseHeaders.set("Server", "BaseHTTP/0.3 Java/1.6");
        responseHeaders.set("Content-Type", "application/json");
        responseHeaders.set("Connection", "Keep-Alive");
        responseHeaders.set("Keep-Alive", "timeout=15, max=1001");
        responseHeaders.set("Accept-Ranges", "bytes");

        DateFormat dateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z");
        Date date = new Date();
        String dateStr = dateFormat.format(date);
//        responseHeaders.set("Date", dateStr);

//HTTP/1.1 200 OK
//Server: BaseHTTP/0.3 Python/2.6.6
//Date: Thu, 21 Oct 2010 15:03:59 GMT
//Connection: Keep-Alive
//Keep-Alive: timeout=15, max=1001
//Content-Type: application/json
//Accept-Ranges: bytes
//Content-Length: 53
//
//{"downspeed": 0.0, "success": "true", "upspeed": 0.0}
//HTTP/1.1 200 OK
//Transfer-encoding: chunked
//Content-type: application/json
//Content-length: 53
//Connection: Keep-Alive
//Accept-ranges: bytes
//Keep-alive: timeout=15, max=100
//
//35
//{"downspeed": 1.0, "success": "true", "upspeed": 2.0}
//0
        int downSpeed = main.getSpeed();
        int totalDownloaded = main.getTotalDownloaded();

        String strResp = "{\"downspeed\":" + downSpeed
                + ", \"success\": \"true\", \"total\":" + totalDownloaded + "}";

        Map obj = new LinkedHashMap();
        obj.put("downspeed", new Integer(downSpeed));
        obj.put("success", new Boolean(true));
        obj.put("total", new Integer(totalDownloaded));
        String jsonText = JSONValue.toJSONString(obj);

//        responseHeaders.set("Content-Length", "" + strResp.length());
//        exchange.sendResponseHeaders(200 /* http error code */, strResp.length());
//        return strResp;
        responseHeaders.set("Content-Length", "" + jsonText.length());
        exchange.sendResponseHeaders(200 /* http error code */, jsonText.length());
        return jsonText;
    }
}
