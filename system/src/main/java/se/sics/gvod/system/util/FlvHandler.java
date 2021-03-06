/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.system.util;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.gvod.system.vod.Vod;
import se.sics.gvod.system.main.GMain;

/**
 * To make sure your videos are compatible, run one of these tools:
 * flvtool2 (for flv files)
 * >flvtool2 -U <filename>
 *
 *
 * @author jdowling
 */
public class FlvHandler extends BaseHandler {
//        implements HttpHandler {

    private static final Logger logger =
            LoggerFactory.getLogger(FlvHandler.class);
    public static final String GET_FILE = "file";
    public static final String GET_POSITION = "position";
    public static final String GET_KEY = "key";
    public static final String GET_BANDWIDTH = "bandwidth";
    // JWPLAYER
//GET /crossdomain.xml HTTP/1.1
//Host: 192.168.1.123
//Connection: keep-alive
//Referer: http://192.168.1.123:9000/player.swf
//Accept: */*
//User-Agent: Mozilla/5.0 (X11; U; Linux x86_64; en-US) AppleWebKit/534.10 (KHTML, like Gecko) Chrome/7.0.544.0 Safari/534.10
//Accept-Encoding: gzip,deflate,sdch
//Accept-Language: sv-SE,sv;q=0.8,en-US;q=0.6,en;q=0.4
//Accept-Charset: ISO-8859-1,utf-8;q=0.7,*;q=0.3
//GET /lindy.flv?start=0 HTTP/1.1
//Host: 192.168.1.123
//Connection: keep-alive
//Referer: http://www.jimdowling.info/tmp/player.swf
//Accept: */*
//User-Agent: Mozilla/5.0 (X11; U; Linux x86_64; en-US) AppleWebKit/534.10 (KHTML, like Gecko) Chrome/7.0.544.0 Safari/534.10
//Accept-Encoding: gzip,deflate,sdch
//Accept-Language: sv-SE,sv;q=0.8,en-US;q=0.6,en;q=0.4
//Accept-Charset: ISO-8859-1,utf-8;q=0.7,*;q=0.3
//HTTP/1.1 200 OK
//Content-Type: video/x-flv
//Accept-Ranges: bytes
//ETag: "1314398064"
//Last-Modified: Mon, 04 Oct 2010 21:52:18 GMT
//Content-Length: 2347456
//Date: Fri, 08 Oct 2010 13:35:16 GMT
//Server: lighttpd/1.4.26
//
    /**
     * FLOWPLAYER
     */
//    GET /flowplayer.flv HTTP/1.1
//Host: 127.0.0.1
//Connection: keep-alive
//Accept: */*
//User-Agent: Mozilla/5.0 (X11; U; Linux x86_64; en-US) AppleWebKit/534.7 (KHTML, like Gecko) Chrome/7.0.517.41 Safari/534.7
//Accept-Encoding: gzip,deflate,sdch
//Accept-Language: sv-SE,sv;q=0.8,en-US;q=0.6,en;q=0.4
//Accept-Charset: ISO-8859-1,utf-8;q=0.7,*;q=0.3
//GET /gg.flv/gg.flv?start=0 HTTP/1.1
//Host: 127.0.0.1:8080
//User-Agent: Mozilla/5.0 (X11; U; Linux x86_64; en-US; rv:1.9.2.12) Gecko/20101027 Ubuntu/10.10 (maverick) Firefox/3.6.12
//Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8
//Accept-Language: en-us,en;q=0.5
//Accept-Encoding: gzip,deflate
//Accept-Charset: ISO-8859-1,utf-8;q=0.7,*;q=0.7
//Keep-Alive: 115
//Connection: keep-alive
//
//HTTP/1.1 200 OK
//Content-type: video/x-flv
//Content-length: 243034952
//Accept-ranges: bytes
//
//FLV............b..........
//onMetaData....
//..duration.@.....`B..filesize.A.........width.@t........height.@n........framerate.@7........hasAudio....hasKeyframes....metadatacreator...MEGA..metadatadate.Br.".Y......keyframes..
//filepositions
//FLV
//   	     �        
//    The FLV header
//Signature UI8 Signature byte always 'F' (0x46)
//Signature UI8 Signature byte always 'L' (0x4C)
//Signature UI8 Signature byte always 'V' (0x56)
//Version UI8 File version (for example, 0x01 for FLV version 1)
//TypeFlagsReserved UB [5] Shall be 0
//TypeFlagsAudio UB [1] 1 = Audio tags are present
//TypeFlagsReserved UB [1] Shall be 0
//TypeFlagsVideo UB [1] 1 = Video tags are present
//DataOffset UI32 The length of this header in bytes
//The DataOffset field usually has a value of 9 for FLV version 1.
    // This field is present to accommodate larger headers in future versions.

//    public static final byte[] FLVX_HEADER = {'F', 'L', 'V', 0x01,
//        0x05, 0, 0, 0, 0x09};
    // The last 4 bytes are the previousTagSize0, which is always 0.
    public static final byte[] FLVX_HEADER = {'F', 'L', 'V', 0x01,
        0x05, 0, 0, 0, 0x09, 0, 0, 0, 0};
    public static final int FLV_HEADER_LEN = FLVX_HEADER.length;
    public static final String START_STRING = "start=";
    private Vod gvod;
    private GMain main;

    public FlvHandler(GMain main, Vod gvod) {
        this.main = main;
        this.gvod = gvod;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String requestMethod = exchange.getRequestMethod();

        logger.trace(exchange.getRequestMethod() + "\n"
                + exchange.getRequestHeaders().entrySet() + "\n"
                + exchange.getResponseHeaders().entrySet());
        if (requestMethod.equalsIgnoreCase("GET")) {

            // Jwplayer request:
            // http://www.mywebsite.com/videos/bbb.flv?start=219476905


            URI uri = exchange.getRequestURI();
            if (uri.toString().compareTo("/crossdomain.xml") == 0) {
//            Headers responseHeaders = exchange.getResponseHeaders();
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
                return;
            }

            String queryPath = uri.getPath().substring(1);
            int queryPos = queryPath.indexOf("/");
            if (queryPos == -1) {
                logger.warn("Invalid Query: " + queryPath);
                error(exchange);
                return;
            }
            String filename = queryPath.substring(0, queryPos);
//            String fileQuery = queryPath.substring(queryPos + 1);
//            String offset = uri.getQuery();
//            int qMark = fileQuery.indexOf("?");
//            if (qMark < 0 || qMark > fileQuery.length()) {
//                logger.warn("Invalid Query: " + fileQuery);
//                error(exchange);
//                return;
//            }
//            String name = fileQuery.substring(0, qMark);
//            System.out.println("Trying to download file: " + filename + " at offset "
//                    + offset);

            Map<String, Object> params =
                    (Map<String, Object>) exchange.getAttribute("parameters");
            Object offsetObj = params.get("start");
            int range = 0;
            if (offsetObj != null) {
                range = Integer.parseInt((String) offsetObj);
            }
            // TODO JIM - need to modify this to point to the exact byte offset that
            // the flash player expects the meta tage to start at.

            System.out.println("new utility = " + range);

            Headers responseHeaders = exchange.getResponseHeaders();
            responseHeaders.set("Content-Type", "video/x-flv");
            DateFormat dateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z");
            Date date = new Date();
            String dateStr = dateFormat.format(date);
//            responseHeaders.set("Date", dateStr);
            responseHeaders.set("Server", "lighttpd/1.4.26"); 
            responseHeaders.set("Connection", "Keep-Alive"); 
            responseHeaders.set("Keep-Alive", "timeout=600, max=99"); 
            long contentLength = gvod.getStorage().getLength();
            if (range != 0) {
                contentLength = contentLength - range + FLV_HEADER_LEN;
            }
            responseHeaders.set("Content-Length", "" + contentLength);
            exchange.sendResponseHeaders(200 /* http error code */,
                    contentLength);

            OutputStream responseBody = exchange.getResponseBody();

            if (range != 0) {
//                responseBody.write(FLVX_HEADER);
//                responseBody.flush();
            }

//GVOD writes the video to the responseBody when the video is playable
//, i.e., we have sufficient prebuffer and the download speed is above the content's bitrate.
            gvod.play();
            main.changeUtility(filename, range, range, responseBody);
        }
    }

    private void error(HttpExchange exchange) throws IOException {

//        Headers responseHeaders = exchange.getResponseHeaders();
        exchange.sendResponseHeaders(404 /* http error code */,
                0);

        OutputStream responseBody = exchange.getResponseBody();
        responseBody.close();
    }
}
/**
 * LIGHTTPD RESULTS
 */
//GET /gg.flv?start=0 HTTP/1.1
//Host: 192.168.1.123:8080
//User-Agent: Mozilla/5.0 (X11; U; Linux x86_64; sv-SE; rv:1.9.2.12) Gecko/20101027 Ubuntu/10.04 (lucid) Firefox/3.6.12
//Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8
//Accept-Language: sv-se,sv;q=0.8,en-us;q=0.5,en;q=0.3
//Accept-Encoding: gzip,deflate
//Accept-Charset: ISO-8859-1,utf-8;q=0.7,*;q=0.7
//Keep-Alive: 115
//Connection: keep-alive
//
//HTTP/1.1 200 OK
//Content-Type: video/x-flv
//Accept-Ranges: bytes
//ETag: "458067577"
//Last-Modified: Fri, 05 Nov 2010 19:56:37 GMT
//Content-Length: 243035417
//Date: Sat, 06 Nov 2010 07:33:16 GMT
//Server: lighttpd/1.4.26
//
//FLV............d1.........
//onMetaData.......hasKeyframes....hasMetadata....duration.@....E....cuePoints
// gg.flv length = 243035417
// flv header length = 13
// seek: 219556242 + 23479188 = 243035430
//GET /gg.flv?start=219556242 HTTP/1.1
//Host: 192.168.1.123:8080
//User-Agent: Mozilla/5.0 (X11; U; Linux x86_64; sv-SE; rv:1.9.2.12) Gecko/20101027 Ubuntu/10.04 (lucid) Firefox/3.6.12
//Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8
//Accept-Language: sv-se,sv;q=0.8,en-us;q=0.5,en;q=0.3
//Accept-Encoding: gzip,deflate
//Accept-Charset: ISO-8859-1,utf-8;q=0.7,*;q=0.7
//Keep-Alive: 115
//Connection: keep-alive
//
//HTTP/1.1 200 OK
//Content-Type: video/x-flv
//Content-Length: 23479188
//Date: Sat, 06 Nov 2010 07:34:38 GMT
//Server: lighttpd/1.4.26
//
//FLV............"[".`........>...."8.......dI..9.$...F.j.edN2.^..HZ@8......VHYA...$P`.C7w.....y.\..V0.....A..x.E.z.0....DP..=.....m.@.......
//...e..o.>....A.
// GVOD RESULTS
//GET /gg.flv/gg.flv?start=219556242 HTTP/1.0
//User-Agent: Wget/1.12 (linux-gnu)
//Accept: */*
//Host: 127.0.0.1:8080
//Connection: Keep-Alive
//
//HTTP/1.1 200 OK
//Content-type: video/x-flv
//Content-length: 23479188
//Accept-ranges: bytes
//
//FLV........"[".`........>...."8.......dI..9.$...F.j.edN2.^..HZ@8......VHYA...$P`.C7w.....y.\..V0.....A..x.E.z.0....DP..=.....m.@.......
//...e..o.>....A.
// GVOD
//GET /gg.flv/gg.flv?start=0 HTTP/1.1
//Host: 127.0.0.1:8080
//User-Agent: Mozilla/5.0 (X11; U; Linux x86_64; en-US; rv:1.9.2.12) Gecko/20101027 Ubuntu/10.10 (maverick) Firefox/3.6.12
//Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8
//Accept-Language: en-us,en;q=0.5
//Accept-Encoding: gzip,deflate
//Accept-Charset: ISO-8859-1,utf-8;q=0.7,*;q=0.7
//Keep-Alive: 115
//Connection: keep-alive
//
//HTTP/1.1 200 OK
//Content-type: video/x-flv
//Content-length: 243035417
//Accept-ranges: bytes
//
//FLV............d1.........
//onMetaData.......hasKeyframes..
// GG LENGTH = 243035417
// LENGTH = 83215783 + 159819647 = 243035430
// malformed packet:
//GET /?view=gg.flv HTTP/1.1
//Host: 127.0.0.1:8080
//User-Agent: Mozilla/5.0 (X11; U; Linux x86_64; en-US; rv:1.9.2.12) Gecko/20101027 Ubuntu/10.10 (maverick) Firefox/3.6.12
//Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8
//Accept-Language: en-us,en;q=0.5
//Accept-Encoding: gzip,deflate
//Accept-Charset: ISO-8859-1,utf-8;q=0.7,*;q=0.7
//Keep-Alive: 115
//Connection: keep-alive
//GET /gg.flv/gg.flv?start=83215783 HTTP/1.1
//Host: 127.0.0.1:8080
//User-Agent: Mozilla/5.0 (X11; U; Linux x86_64; en-US; rv:1.9.2.12) Gecko/20101027 Ubuntu/10.10 (maverick) Firefox/3.6.12
//Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8
//Accept-Language: en-us,en;q=0.5
//Accept-Encoding: gzip,deflate
//Accept-Charset: ISO-8859-1,utf-8;q=0.7,*;q=0.7
//Keep-Alive: 115
//Connection: keep-alive
//
//HTTP/1.1 200 OK
//Content-type: video/x-flv
//Content-length: 159819647
//Accept-ranges: bytes
//
//FLV........<%
//.............z......@.I./....,.H.&..}X... ).......r.B.. ~>M.....1...J......)R...+..3..p........._S..|.....L.@`...H..`....I/.!( .
// EXAMPLES - LIGHTTPD
//GET /gg.flv?start=39240188 HTTP/1.1
//Host: 192.168.1.123:8080
//User-Agent: Mozilla/5.0 (X11; U; Linux x86_64; sv-SE; rv:1.9.2.12) Gecko/20101027 Ubuntu/10.04 (lucid) Firefox/3.6.12
//Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8
//Accept-Language: sv-se,sv;q=0.8,en-us;q=0.5,en;q=0.3
//Accept-Encoding: gzip,deflate
//Accept-Charset: ISO-8859-1,utf-8;q=0.7,*;q=0.7
//Keep-Alive: 115
//Connection: keep-alive
//
//HTTP/1.1 200 OK
//Content-Type: video/x-flv
//Content-Length: 203795242
//Date: Sat, 06 Nov 2010 20:04:12 GMT
//Server: lighttpd/1.4.26
//
//FLV............8..%p...........N.*.,x.D&).....!..%.@.....!.E.@<..%[...U^Y.....y....=...590.Ds...J.f.
//.<..n3.x3x..?j........O..x..u.../.....v...;D.....&.=P>....#.. .....x.. X&w....|.PI.@x.,...}.-XTw....".yfO...........0C......Z.....U.... P`^.Q..0....@a.U...J....04....6...%.....>:A..I..`c..'..}.N:4\....s0.0'.......[...a*.'s..2=/......?..~w.......s@...DI..XS+...d..X..Y.~6.....O4......B......=....+A....=...4O.CT^d.H.....0I...+......s.e../.UH........`o..>.(...
// 00000000  48 54 54 50 2f 31 2e 31  20 32 30 30 20 4f 4b 0d HTTP/1.1  200 OK.
//    00000010  0a 43 6f 6e 74 65 6e 74  2d 54 79 70 65 3a 20 76 .Content -Type: v
//    00000020  69 64 65 6f 2f 78 2d 66  6c 76 0d 0a 43 6f 6e 74 ideo/x-f lv..Cont
//    00000030  65 6e 74 2d 4c 65 6e 67  74 68 3a 20 32 30 33 37 ent-Leng th: 2037
//    00000040  39 35 32 34 32 0d 0a 44  61 74 65 3a 20 53 61 74 95242..D ate: Sat
//    00000050  2c 20 30 36 20 4e 6f 76  20 32 30 31 30 20 32 30 , 06 Nov  2010 20
//    00000060  3a 30 34 3a 31 32 20 47  4d 54 0d 0a 53 65 72 76 :04:12 G MT..Serv
//    00000070  65 72 3a 20 6c 69 67 68  74 74 70 64 2f 31 2e 34 er: ligh ttpd/1.4
//    00000080  2e 32 36 0d 0a 0d 0a 46  4c 56 01 01 00 00 00 09 .26....F LV......
//    00000090  00 00 00 09 09 00 38 84  06 25 70 00 00 00 00 12 ......8. .%p.....
//    000000A0  00 00 86 e6 91 8e 4e ab  2a af 2c 78 8d 44 26 29 ......N. *.,x.D&)
//GET /gg.flv?start=93299039 HTTP/1.1
//Host: 192.168.1.123:8080
//User-Agent: Mozilla/5.0 (X11; U; Linux x86_64; sv-SE; rv:1.9.2.12) Gecko/20101027 Ubuntu/10.04 (lucid) Firefox/3.6.12
//Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8
//Accept-Language: sv-se,sv;q=0.8,en-us;q=0.5,en;q=0.3
//Accept-Encoding: gzip,deflate
//Accept-Charset: ISO-8859-1,utf-8;q=0.7,*;q=0.7
//Keep-Alive: 115
//Connection: keep-alive
//HTTP/1.1 200 OK
//Content-Type: video/x-flv
//Content-Length: 149736391
//Date: Sat, 06 Nov 2010 20:04:14 GMT
//Server: lighttpd/1.4.26
//
//FLV............5J..Y...........>.F..m..!.b7.......)Zm.
//.|...8....D...w..T.NZ@....'X...~.C..,
//..X...~#)"Z..........
//*1..b....x..i.%..?......U....Y.+.h.e.xIQ.....J-..;...........l...Lj...`.K..$..G%;......X...~o.......+W.+.5c.u...0.d.............R.....6....C...,i....U.J.DN.\...E.\..
// gvod
// len = 118230318 + 124805112 = 243035430
//GET /gg.flv/gg.flv?start=118230318 HTTP/1.1
//Host: 127.0.0.1:8080
//User-Agent: Mozilla/5.0 (X11; U; Linux x86_64; en-US; rv:1.9.2.12) Gecko/20101027 Ubuntu/10.10 (maverick) Firefox/3.6.12
//Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8
//Accept-Language: en-us,en;q=0.5
//Accept-Encoding: gzip,deflate
//Accept-Charset: ISO-8859-1,utf-8;q=0.7,*;q=0.7
//Keep-Alive: 115
//Connection: keep-alive
//
//HTTP/1.1 200 OK
//Content-type: video/x-flv
//Content-length: 124805112
//Accept-ranges: bytes
//
//FLV...........B..Q.....E....P.~...<.3....p....O,F.%...op



// FLOWPLAYER - GVOD
//GET /gg.flv/gg.flv??start=110015710 HTTP/1.1
//Host: 127.0.0.1:8080
//User-Agent: Mozilla/5.0 (X11; U; Linux x86_64; en-US; rv:1.9.2.12) Gecko/20101027 Ubuntu/10.10 (maverick) Firefox/3.6.12
//Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8
//Accept-Language: en-us,en;q=0.5
//Accept-Encoding: gzip,deflate
//Accept-Charset: ISO-8859-1,utf-8;q=0.7,*;q=0.7
//Keep-Alive: 115
//Connection: keep-alive
//
//HTTP/1.1 200 OK
//Content-type: video/x-flv
//Content-length: 243035417
//Server: Gvod 1.0
//Accept-ranges: bytes
//Date: Mon, 8 Nov 2010 17:13:05 CET
//
//FLV............d1.........


// LIGHTTPD - FLOWPLAYER
//GET /gg.flv?start=138551073 HTTP/1.1
//Host: 127.0.0.1:8080
//User-Agent: Mozilla/5.0 (X11; U; Linux x86_64; en-US; rv:1.9.2.12) Gecko/20101027 Ubuntu/10.10 (maverick) Firefox/3.6.12
//Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8
//Accept-Language: en-us,en;q=0.5
//Accept-Encoding: gzip,deflate
//Accept-Charset: ISO-8859-1,utf-8;q=0.7,*;q=0.7
//Keep-Alive: 115
//Connection: keep-alive
//
//HTTP/1.1 200 OK
//Content-Type: video/x-flv
//Content-Length: 104484357
//Date: Mon, 08 Nov 2010 16:21:09 GMT
//Server: lighttpd/1.4.26
//
//FLV............8................$..\.x0/j....C..yR.bW=.T;..7....H\1d...).....0.......s5n.@
//y........-cU...G~Ny.Q..g.....4!.HV...



// LIVE DVR STREAMING using BitGravity soln

//GET /tatamkt/testing/ld?start=0 HTTP/1.1
//Host: bglive-a.bitgravity.com
//User-Agent: Mozilla/5.0 (X11; U; Linux x86_64; en-US; rv:1.9.2.12) Gecko/20101027 Ubuntu/10.10 (maverick) Firefox/3.6.12
//Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8
//Accept-Language: en-us,en;q=0.5
//Accept-Encoding: gzip,deflate
//Accept-Charset: ISO-8859-1,utf-8;q=0.7,*;q=0.7
//Keep-Alive: 115
//Connection: keep-alive
//
//HTTP/1.1 200 OK
//Connection: close
//Cache-Control: no-store
//Pragma: no-cache
//Content-Type: video/x-flv
//Content-Length: 1073741824
//Date: Wed, 10 Nov 2010 10:26:33 GMT
//Server: bit_asic_live/live2lh2
//
//FLV.............7..^..........B.....#gB...T....