/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.system.util;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import mp4.util.RandomAccessFileInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.gvod.config.VodConfig;
import se.sics.gvod.system.vod.Vod;
import se.sics.gvod.system.main.GMain;

/**
 * To make sure your videos are compatible, run one of these tools:
 * mp4creator (for mp4 files)
 * >mp4creator -optimize <filename>
 *
 * MP4Box moves metadata to start of file, and interleaves media in 0.5 second
 * chunks - useful for HTTP streaming
 * http://gpac.sourceforge.net/doc_mp4box.php
 * >MP4Box -inter 0.5 <filename>
 *
 * For MP4 videos, the offset is provided in seconds:
 * http://www.mywebsite.com/videos/bbb.mp4?starttime=30.4
 *
 * TODO: port code from : http://h264.code-shop.com/trac to Java
 * and http://codeblog.palos.ro/2008/11/13/pseudo-streaming-mp4h264-video-from-php/
 * See also Streambaby source code.
 *
 * The mp4 header consists of 2 parts that need to be sent:
 * (1) MOOV: the metadata of the MP4 file, and
 * (2) an array with keyframepositions in that metadata.
 * The moov needs to be recomputed with every seek.
 * @author jdowling
 */
public class Mp4Handler extends BaseHandler {
//        extends Mp4Split implements HttpHandler {

    private static final Logger logger =
            LoggerFactory.getLogger(Mp4Handler.class);
    public static final String GET_FILE = "file";
    public static final String GET_POSITION = "position";
    public static final String GET_KEY = "key";
    public static final String GET_BANDWIDTH = "bandwidth";
    public static final String START_STRING = "start=";
    private Vod gvod;
    private GMain main;
    private AtomicBoolean processing = new AtomicBoolean(false);
//GET /topgear.mp4?start=3525.96 HTTP/1.1
//Host: localhost
//User-Agent: Mozilla/5.0 (X11; U; Linux x86_64; en-US; rv:1.9.2.13) Gecko/20101206 Ubuntu/10.10 (maverick) Firefox/3.6.13
//Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8
//Accept-Language: en-us,en;q=0.5
//Accept-Encoding: gzip,deflate
//Accept-Charset: ISO-8859-1,utf-8;q=0.7,*;q=0.7
//Keep-Alive: 115
//Connection: keep-alive
//
//HTTP/1.1 200 OK
//Date: Tue, 04 Jan 2011 14:44:15 GMT
//Server: Apache/2.2.16 (Ubuntu)
//X-Mod-H264-Streaming: version=2.2.7
//Content-Length: 52945578
//Last-Modified: Tue, 04 Jan 2011 13:54:47 GMT
//ETag: "300172-181cb220-499059c8a4c6f;52945578"
//Accept-Ranges: bytes
//Keep-Alive: timeout=15, max=100
//Connection: Keep-Alive
//Content-Type: video/mp4
//
//... ftypisom....isomiso2avc1mp41...*freevideo served by mod_h264_streaming..GFmoov...lmvhd....|%..|%........&.................................................@..................................`udta...Xmeta.......!hdlr........mdirappl............+ilst...#.too....data........Lavf52.64.2..s.trak...\tkhd....|%...G............&.................................................@........h....sCmdia... mdhd....|%..|%........&.U......,hdlr........vide............VideoHandler..r.minf....vmhd...............$dinf....dref............url ......r.stbl....stsd............avc1...........................h.H...H...............................................MavcC.M.....5gM@..R.@_....@.I.............
//.@.W.......W .+.?.p.......h..<.... stts..........4'...(..........{.ctts....../|.......P...............P...............(...............P..............
//GET /topgear.mp4?start=1301.08 HTTP/1.1
//Host: localhost
//User-Agent: Mozilla/5.0 (X11; U; Linux x86_64; en-US; rv:1.9.2.13) Gecko/20101206 Ubuntu/10.10 (maverick) Firefox/3.6.13
//Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8
//Accept-Language: en-us,en;q=0.5
//Accept-Encoding: gzip,deflate
//Accept-Charset: ISO-8859-1,utf-8;q=0.7,*;q=0.7
//Keep-Alive: 115
//Connection: keep-alive
//
//HTTP/1.1 200 OK
//Date: Tue, 04 Jan 2011 14:44:17 GMT
//Server: Apache/2.2.16 (Ubuntu)
//X-Mod-H264-Streaming: version=2.2.7
//Content-Length: 274937719
//Last-Modified: Tue, 04 Jan 2011 13:54:47 GMT
//ETag: "300172-181cb220-499059c8a4c6f;274937719"
//Accept-Ranges: bytes
//Keep-Alive: timeout=15, max=99
//Connection: Keep-Alive
//Content-Type: video/mp4
//
//... ftypisom....isomiso2avc1mp41...*freevideo served by mod_h264_streaming..(.moov...lmvhd....|%..|%.......*..................................................@..................................`udta...Xmeta.......!hdlr........mdirappl............+ilst...#.too....data........Lavf52.64.2....trak...\tkhd....|%...G...........*..................................................@........h.....3mdia... mdhd....|%..|%.......*..U......,hdlr........vide............VideoHandler....minf....vmhd...............$dinf....dref............url ........stbl....stsd............avc1...........................h.H...H...............................................MavcC.M.....5gM@..R.@_....@.I.............
//.@.W.......W .+.?.p.......h..<.... stts..........
//m...(............ctts...............P...............P...............(...............P...............
//    public static final byte[] MP4_HEADER = {0, 0, 0, 0x14,
//        0x66, 0x74, 0x79, 0x70, 0x69, 0x73, 0x6f, 0x6D, 0, 0, 0x02, 0};
//    public static final byte[] MDAT_TAG = {0x0 /*size 1*/, 0x0 /*size 2*/,
//        0x0 /*size 3*/, 0x0 /*size 4*/,
//        0X6d, 0X64, 0X61, 0x74};
//Looking at the mp4 file format and the ffmpeg source code, I would think a simple ftyp tag, followed by mdat and the movie data should do the trick:
//So first an ftyp id tag to identify this as an mp4 file:
//0000 0014 6674 7970 6973 6f6d 0000 0200 ....ftypisom.
//6d70 3431 ....
//0x14 (==20 bytes) is the length of the tag, 0x66747970 (=="ftyp") is the name of the tag,
//      and the rest seems to be specific for the mp4 format (to distinguish it from .mov etc).
//Then start the movie data mdat tag:
//xxxx xxxx 6d64 6174
//where xxxx xxxx should be replaced by the size (as a bigendian 64 bit number) of the remainder of the data plus these 8 bytes, and 0x6d646174 ("mdat") is the tag name to signal the start of the movie data.
//After that, just stream the remainder of the movie data.

// The relevant Metadata in mp4 files can be exctracted with the onMetaData events.
//They look like :
//seekpoints[i]; ["time"]["offset"]
//0 - 0;77262
//1 - 0.5;144183
//2 - 1;222965
//3 - 1.5;293303
//4 - 2;362199
//5 - 2.5;431178
    public Mp4Handler(GMain main, Vod gvod) {
        this.main = main;
        this.gvod = gvod;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String requestMethod = exchange.getRequestMethod();

        processing.set(true);

        DataOutputStream dataOut = null;
        try {

            logger.trace(exchange.getRequestMethod() + "\n"
                    + exchange.getRequestHeaders().entrySet() + "\n"
                    + exchange.getResponseHeaders().entrySet());
            if (requestMethod.equalsIgnoreCase("GET")) {

                // start == seconds. Need to convert it to bytes.
                // http://www.mywebsite.com/videos/bbb.mp4?start=219


                URI uri = exchange.getRequestURI();
                if (uri.toString().compareTo("/crossdomain.xml") == 0) {
                    String allowAccess = "<?xml version=\"1.0\"?>\n"
                            + "<!DOCTYPE cross-domain-policy SYSTEM "
                            + "\"http://www.macromedia.com/xml/dtds/cross-domain-policy.dtd\">\n"
                            + "<cross-domain-policy> \n  <allow-access-from domain=\"*\" />\n"
                            + "</cross-domain-policy>\n";
                    byte[] aa = new byte[1];
                    aa[0] = 0x0;
                    OutputStream responseBody = exchange.getResponseBody();
                    responseBody.write(allowAccess.getBytes());
                    responseBody.flush();
                    responseBody.close();
                    processing.set(false);
                    return;
                }

                String queryPath = uri.getPath().substring(1);
                int queryPos = queryPath.indexOf("/");
                if (queryPos == -1) {
                    logger.warn("Invalid Query: " + queryPath);
                    error(exchange);
                    processing.set(false);
                    return;
                }
                String videoFilename = queryPath.substring(0, queryPos);

                Map<String, Object> params =
                        (Map<String, Object>) exchange.getAttribute("parameters");
                Object offsetObj = params.get("start");
                float range = 0;
                if (offsetObj != null) {
                    range = Float.parseFloat((String) offsetObj);
                }


                logger.debug("new utility = " + range);

                Headers responseHeaders = exchange.getResponseHeaders();
                responseHeaders.set("Content-Type", "video/mp4");
                DateFormat dateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z");
                Date date = new Date();
                String dateStr = dateFormat.format(date);
                responseHeaders.set("Date", dateStr);
                responseHeaders.set("Server", "lighttpd/1.4.26"); // Gvod 1.0
                responseHeaders.set("Connection", "Keep-Alive");
                responseHeaders.set("Keep-Alive", "timeout=600, max=99");
                responseHeaders.set("X-Mod-H264-Streaming", "version=2.2.7");

                long contentLength = gvod.getStorage().getLength();

                OutputStream responseBody = exchange.getResponseBody();
                long mDatOffset = 0;
                long mDatLen = 0;
                
                if (range != 0.0f) {
                    logger.debug("Interrupting sender thread");
                    try {
                        gvod.interruptSender();
                    } catch (SecurityException ex) {
                       logger.warn("Exception thrown interrupting sender: " +
                               ex.getMessage());
                    }
                    String cwd = System.getProperty("user.dir");
                    logger.debug("New input stream using file: "
                            + gvod.getStorage().getMetaInfo().getName()
                            + " in cwd " + cwd);
                    InputStream is =
                            new RandomAccessFileInputStream(
                            VodConfig.getTorrentDir()
                            + File.separator + 
                            gvod.getStorage().getMetaInfo().getName());
                    mp4file = new DataInputStream(is);

                    time = range;
                    this.writeMdat = false;
                    logger.info("FF to " + range);
                    mDatOffset = progSplitMp4(false);
                    mDatLen = contentLength - mDatOffset;
                    dataOut = new DataOutputStream(responseBody);
                    long lenMoov = lenHeaders();
                    long altLen = lenMoov + mDatLen;
                    contentLength = lenSplitMp4();
                    logger.info("Content-Length: " + contentLength);
                    logger.info("Alt Content-Length: " + altLen);
                    responseHeaders.set("Content-Length", "" + altLen);
                    exchange.sendResponseHeaders(200 /* http error code */,
                            altLen);
                    writeProgressiveMp4(dataOut, mDatLen);

                } else {
                    responseHeaders.set("Content-Length", "" + contentLength);
                    exchange.sendResponseHeaders(200 /* http error code */,
                            contentLength);
                }


//GVOD writes the video to the responseBody when the video is playable
//, i.e., we have sufficient prebuffer and the download speed is above the content's bitrate.

                gvod.play();
                boolean foundPeer = main.changeUtility(videoFilename, (int) mDatOffset,
                        (int) mDatOffset, responseBody);
                if (!foundPeer) {
                    // TODO - print to screen that I couldn't find the video
                    logger.warn("Couldn't find a peer for the video: " + videoFilename);
                }

            }
        } catch (IOException ex) {
            throw ex;
        } finally {
            processing.set(false);
//            dataOut = null; // allow for garbage collection
//            if (dataOut != null) {
//                dataOut.close();
//            }
//            if (mp4file != null) {
//                mp4file.close();
//            }
        }

    }

    private void error(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(404 /* http error code */, 0);
        OutputStream responseBody = exchange.getResponseBody();
        responseBody.close();


    }
}
