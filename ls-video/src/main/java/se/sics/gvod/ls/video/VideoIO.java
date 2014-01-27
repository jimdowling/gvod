package se.sics.gvod.ls.video;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import se.sics.gvod.ls.http.HTTPStreamingClient;
import se.sics.gvod.ls.http.HTTPStreamingServer;
import se.sics.gvod.ls.system.LSConfig;
import se.sics.gvod.ls.system.PieceHandler;
import se.sics.gvod.ls.video.snapshot.SimulationSingleton;
import se.sics.gvod.ls.video.snapshot.VideoStats;
import se.sics.gvod.video.msgs.EncodedSubPiece;
import se.sics.gvod.video.msgs.Piece;
import se.sics.gvod.video.msgs.SubPiece;

/**
 * Handles input and output of data to the Video component. In the source it
 * reads from various resources; file system, HTTP, etc. and converts to data
 * for dissemination. In peers receiving data it organizes and outputs the data
 * to an output stream.
 *
 * @author Niklas Wahl&#233;n <nwahlen@kth.se>
 */
public class VideoIO implements Runnable {

    private Map<Integer, VideoFEC> fecs; // piece id, fec
    private Map<Integer, Piece> pieceBuffer; // piece id, piece
    private Video video;
    // source
    private HTTPStreamingClient stream;
    // downloader
    private HTTPStreamingServer server;
    private Map<Integer, Long> pieceStartTimes; // piece id, start time for dl
    private Set<Integer> completePieces;
    private Set<Integer> skippedPieces;
    private int currentPieceId, maxStreamRate, bufferLength, missedPieces;
    private LinkedBlockingQueue<SubPiece> playBackQueue; // this structure is ordered, while the piece buffer may not be

    public VideoIO(Video video) {
        this.video = video;
        fecs = new HashMap<Integer, VideoFEC>();
        pieceBuffer = new HashMap<Integer, Piece>();
        pieceStartTimes = new HashMap<Integer, Long>();
        completePieces = new HashSet<Integer>();
        skippedPieces = new HashSet<Integer>();
        currentPieceId = -2;
        playBackQueue = new LinkedBlockingQueue<SubPiece>();
        if (LSConfig.isSimulation()) {
            maxStreamRate = 66 * SubPiece.SUBPIECE_DATA_SIZE;
        } else {
            maxStreamRate = Integer.MAX_VALUE;
        }
    }

    // calculates the current PieceID.
    // Next piece should have been decoded or
    public void cycle() {
        long currentTime = System.currentTimeMillis();
        // handle completed pieces and prepare for playback
        Set<Integer> toRemoveComplete = new HashSet<Integer>();
        int streamLag = 0;
        long oldestTime = Long.MAX_VALUE;
        while (pieceBuffer.containsKey(currentPieceId) || skippedPieces.contains(currentPieceId)) {
            if (pieceBuffer.containsKey(currentPieceId)) {
                playBackQueue.addAll(Arrays.asList(pieceBuffer.get(currentPieceId).getSubPieces()));
                if (LSConfig.isSimulation()) {
                    Long pieceStartTime = SimulationSingleton.getInstance().get(currentPieceId);
                    if ((pieceStartTime != null) && ((currentTime - pieceStartTime) > streamLag)) {
                        streamLag = (int) (currentTime - pieceStartTime);
                    }
                }
                toRemoveComplete.add(currentPieceId);
            }
            Long startTime = pieceStartTimes.get(currentPieceId);
            if (startTime < oldestTime) {
                oldestTime = startTime;
            }
            currentPieceId++;
        }
        if (LSConfig.isSimulation()) {
            if (streamLag > 0) {
                VideoStats.instance(video.getSelf()).setStreamLag(streamLag);
            }
        }
        int streamRate = 0;
        while (!playBackQueue.isEmpty() && (streamRate < maxStreamRate)) {
            streamRate += SubPiece.SUBPIECE_DATA_SIZE;
            SubPiece piece = playBackQueue.poll();
            // Check if the system is configured to deliver a HTTP stream
            if (LSConfig.hasDestUrlSet()) {
                // Deliver to HTTP Server
                this.deliver(piece);
            }
        }
        // look for pieces which are exceeding buffer limit
        Set<Integer> toRemoveMissing = new HashSet<Integer>();
        missedPieces = 0;
        for (Integer id : fecs.keySet()) {
            Long startTime = pieceStartTimes.get(id);
            if ((currentTime - startTime) > LSConfig.VIDEO_MAX_BUFFER_SIZE) {
                skippedPieces.add(id);
                toRemoveMissing.add(id);
                missedPieces++;
            } else if (startTime < oldestTime) {
                oldestTime = startTime;
            }
        }
        // clean
        for (Integer id : toRemoveComplete) {
            pieceBuffer.remove(id);
            // send removal signal to gossip
            video.triggerRemoval(id);
        }
        for (Integer id : toRemoveMissing) {
            fecs.remove(id);
            // send removal signal to gossip
            video.triggerRemoval(id);
        }
        // stats
        VideoStats.instance(video.getSelf()).setStreamRate(streamRate);
        VideoStats.instance(video.getSelf()).setMissedPieces(toRemoveMissing.size());
        if (oldestTime < Long.MAX_VALUE) {
            bufferLength = (int) (currentTime - oldestTime);
            VideoStats.instance(video.getSelf()).setBufferLength(bufferLength);
        }
    }
    /*
     * SOURCE -- HANDLE EXTERNAL INPUT
     */

    public void setSource(URL url) throws IOException {
        stream = new HTTPStreamingClient(url);
    }

    public void setSource(File file) throws IOException {
        stream = new HTTPStreamingClient(file);
    }

    @Override
    public void run() {
        if (LSConfig.isSimulation()) {
            stream.run();
        } else {
            new Thread(stream).start();
        }
        while (stream.isReading() || stream.hasNextPiece()) {
            while (!stream.hasNextPiece());
            Piece piece = stream.getNextPiece();
            encodeAndAdvertise(piece);
        }
    }

    /*
     * LEECHER -- HANDLE ENCODED SUB-PIECES
     */
    public void startServer(InetSocketAddress serverAddress) throws IOException {
        server = new HTTPStreamingServer(serverAddress);
    }

    public void handleEncodedSubPiece(EncodedSubPiece p) {
        // If the piece is among the completed pieces no further action required
        if (completePieces.contains(p.getParentId())
                || skippedPieces.contains(p.getParentId())) {
            return;
        }
        VideoFEC fec = fecs.get(p.getParentId());
        if (fec == null) {
            fec = new VideoFEC(p.getParentId());
            fecs.put(p.getParentId(), fec);
            pieceStartTimes.put(p.getParentId(), System.currentTimeMillis());
        }
        if (!fec.isReady() && !fec.contains(p)) {
            fec.addEncodedSubPiece(p);
            if (fec.isReady() && !fec.isDecoded()) {
                // Make sure no more (encoded) sub-pieces of this piece are
                // requested this is easiest achieved by decoding this piece,
                // encoding it again and then adding the sub-pieces to the
                // VideoGossip. (This is done through encode().)
                Piece piece = fec.decode();
                completePieces.add(piece.getId());
                pieceBuffer.put(piece.getId(), piece);
                VideoStats.instance(video.getSelf()).setCompletedPiece(piece.getId());
                this.encodeAndAdvertise(piece);
                //
                // Remove this FEC from the Map
                fecs.remove(fec.getPiece().getId());
            }
        }
        // if first received piece
        if (currentPieceId < 0) {
            currentPieceId = p.getParentId();
        }
    }

    /**
     * Method called when a new Piece is ready. The piece is encoded using FEC
     * and the encoded sub-pieces advertised to the node's neighbours.
     *
     * @param piece The Piece to be encoded and published (advertised)
     * @see VideoPieceHandler
     * @see BlockBasedFEC
     */
    private void encodeAndAdvertise(Piece piece) {
        // Encode piece
        VideoFEC fec = new VideoFEC(piece);
        for (int i = 0; i < fec.getEncodedSubPieces().length; i++) {
            video.publish(fec.getEncodedSubPiece(i));
        }
    }

    private void deliver(SubPiece sp) {
        try {
            server.deliver(sp);
        } catch (IOException ex) {
            Logger.getLogger(VideoIO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void deliver(Piece piece) {
        try {
            server.deliver(piece);
        } catch (IOException ex) {
            Logger.getLogger(VideoIO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void close() {
        if (server != null) {
            server.stop();
        }
    }

    public boolean hasPiece(int id) {
        if (id < 0) {
            return true;
        }
        return completePieces.contains(id);
    }

    /*
     * Write Piece data to file -- mostly used for testing purposes
     */
    public void writePieceData(String filePath) throws IOException {
        // TODO: never assume that the pieces are ordered at method invocation
        boolean append = false;
        System.out.println(PieceHandler.class.getSimpleName() + ": About to write " + pieceBuffer.size() + " pieces.");
        FileOutputStream out = getFileOutputStream(filePath, append);
        for (int i = 0; i < pieceBuffer.size(); i++) {
            Piece p = pieceBuffer.get(i);
            if (p.getId() != i) {
                System.out.println("Piece at index " + i + " has Id " + p.getId());
            }
            if (i < pieceBuffer.size() - 1) {
                for (SubPiece sp : p.getSubPieces()) {
                    out.write(sp.getData());
                }
            } else {
                // Find the location of padding bytes
                int found = 0;
                byte[] data = new byte[Piece.PIECE_DATA_SIZE];
                SubPiece[] sps = p.getSubPieces();
                // First gather all bytes into a single array, in case the
                // padding code was cut into two separate sub pieces
                for (int n = 0, j = 0; n < sps.length; n++, j += SubPiece.SUBPIECE_DATA_SIZE) {
                    System.arraycopy(sps[n].getData(), 0, data, j, SubPiece.SUBPIECE_DATA_SIZE);
                }
                for (int j = 0; j < data.length - Piece.PADDING_CODE.length; j++) {
                    if (data[j] == Piece.PADDING_CODE[0]
                            && data[j + 1] == Piece.PADDING_CODE[1]
                            && data[j + 2] == Piece.PADDING_CODE[2]
                            && data[j + 3] == Piece.PADDING_CODE[3]) {
                        System.out.println(PieceHandler.class.getSimpleName() + ": Detected padding code starting at " + p.getId() + "[" + j + "]");
                        break;
                    }
                    // if the loop is still running the byte is valid data
                    out.write(data[j]);
                }
            }
        }
        out.flush();
        out.close();
    }

    private static FileOutputStream getFileOutputStream(String filePath, boolean append) throws FileNotFoundException {
        return new FileOutputStream(createFile(filePath), append);
    }

    private static File createFile(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        }
        try {
            file.createNewFile();
        } catch (IOException ex) {
            Logger.getLogger(PieceHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println(PieceHandler.class.getSimpleName() + ": Created new file for writing: " + file.getName());
        return file;
    }
    public static Comparator<Piece> pieceComparator = new Comparator<Piece>() {

        @Override
        public int compare(Piece t, Piece t1) {
            if (t == null && t1 == null) {
                return 0;
            } else if (t == null) {
                return 1;
            } else if (t1 == null) {
                return -1;
            } else if (t.getId() < t1.getId()) {
                return -1;
            } else if (t.getId() > t1.getId()) {
                return 1;
            } else {
                return 0;
            }
        }
    };

    public int getCurrentBufferLength() {
        return bufferLength;
    }

    public int getMissedPieces() {
        return missedPieces;
    }
}
