/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.system.util;

import java.io.IOException;
import java.util.logging.Level;
import java.io.OutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.gvod.common.BitField;
import se.sics.gvod.system.vod.Vod;

/**
 * TODO - use an Executor and ThreadPool here
 * @author gautier
 */
public class Sender extends Thread implements Thread.UncaughtExceptionHandler {

    private Logger logger = LoggerFactory.getLogger(Sender.class);
    private Vod gvod;
    private OutputStream responseBody;
    private final int pieceToReadFrom;
    private int offsetWithinPiece;
    private int bytesWritten = 0;

    public Sender(Vod gvod, OutputStream responseBody,
            int pieceToReadFrom, int offsetWithinPiece) {
        this.gvod = gvod;
        this.responseBody = responseBody;
        this.pieceToReadFrom = pieceToReadFrom;
        this.offsetWithinPiece = offsetWithinPiece;
    }

    public void run() {
        // TODO gvod.getStorage().getBitField().pieceFieldSize() is final 
        // - apart from storage, but that should be ok, as storage
        // is set once on handleJoin in Gvod.
        // write flv or mp4 header (MOOV objs for mp4)
        byte[] header = gvod.getStorage().getHttpPseudoStreamingHeader(0);

        boolean writeHeader = false;
        if (pieceToReadFrom != 0 && header != null) {
            writeHeader = true;
        }

        logger.info("SENDER THREAD (http server) STARTING");

        while (gvod.getNextPieceToSend() == 0
                || gvod.getNextPieceToSend() < gvod.getStorage().getBitField().pieceFieldSize()) {
            try {
                if (!gvod.isBuffering()) {

                    // TODO - sychronize getPiece() method in StorageExec.
                    int pieceNum = gvod.getNextPieceToSend();
                    byte[] piece = gvod.getStorage().getPiece(pieceNum);

                    if (piece != null) {
                        if (writeHeader) {
                            logger.info("STOPPED BUFFERING IN SENDER THREAD. Piece {}/{}",
                                    pieceNum,
                                    (gvod.getStorage().getLength() / 
                                    (BitField.SUBPIECE_SIZE * BitField.NUM_SUBPIECES_PER_PIECE)));
                            responseBody.write(header);
                            responseBody.flush();
                            writeHeader = false;
                        }
                        if (offsetWithinPiece == 0) {

                            // TODO - mp4 only writes after the adjustedTime is reached
                            logger.trace("Piece {}/{} size was " + piece.length,
                                    pieceNum,
                                    (gvod.getStorage().getLength() / 
                                    (BitField.SUBPIECE_SIZE * BitField.NUM_SUBPIECES_PER_PIECE)));
                            try {
                                responseBody.write(piece);
                            } catch (java.io.IOException ex) {
                                logger.warn("Exception at: Piece {}/{} size was " + piece.length,
                                        pieceNum,
                                        (gvod.getStorage().getLength() / (1024 * BitField.NUM_SUBPIECES_PER_PIECE)));
                                logger.error(ex.getLocalizedMessage());
                                throw ex;
                            }
                            responseBody.flush();
//                            rf.write(piece);
                            bytesWritten += piece.length;

                        } else {
                            int idx = offsetWithinPiece;
                            logger.debug("idx = " + idx
                                    + " pieceNum = " + pieceNum);
                            byte[] response = new byte[piece.length - idx];
                            for (int i = idx; i < piece.length; i++) {
                                response[i - idx] = piece[i];
                            }
                            responseBody.write(response);
                            responseBody.flush();
                            offsetWithinPiece = 0;
//                            rf.write(response);
                            bytesWritten += response.length;
                        }
                        gvod.setNextPieceToSend(gvod.getNextPieceToSend() + 1);
                    } else {
                        // TODO - Jim, should use event to wakeup thread here
                        sleep(5);
                    }
                } else {
                    sleep(1000);
                    logger.info("Web server buffering: {}", gvod.isBuffering());
                }
            } catch (Exception e) {
//                System.out.println("############################################");
                e.printStackTrace();
//                try {
//                    responseBody.close();
//                } catch (IOException ex) {
//                    Logger.getLogger(Sender.class.getName()).log(Level.SEVERE, null, ex);
//                }
//                if (!(e instanceof SocketTimeoutException)) {
                break;
//                }
//                else {
//                      gvod.setNextPieceToSend(gvod.getNextPieceToSend() + 1);

//                    try {
//                        sleep(100);
//                    } catch (Exception ex) {
//                        ex.printStackTrace();
//                    }
//                }
            }


        }

        logger.info("Sender Thread Exited. Bytes written : " + bytesWritten);
        try {
//            gvod.writeHandlerBody();
            responseBody.close();
//            rf.close();
        } catch (IOException ex) {
            logger.error(ex.getMessage());
        }

    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        logger.warn("Thread exited with unexpected exception: " + e.getMessage());
        if (responseBody != null) {
            try {
                responseBody.close();
            } catch (IOException ex) {
                java.util.logging.Logger.getLogger(Sender.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
