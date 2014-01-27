package se.sics.gvod.ls.system;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import se.sics.gvod.video.msgs.Piece;
import se.sics.gvod.video.msgs.SubPiece;

/**
 * TODO: to be removed
 * @author Niklas Wahl&#233;n <nwahlen@kth.se>
 */
public class PieceHandler {

    public static void writePieceData(String filePath, List<Piece> pieces) throws IOException {
        // TODO: never assume that the pieces are ordered at method invocation
        Collections.sort(pieces, pieceComparator);
        boolean append = false;
        System.out.println(PieceHandler.class.getSimpleName() + ": About to write " + pieces.size() + " pieces.");
        System.out.println(new String(Arrays.copyOf(pieces.get(0).getSubPieces()[0].getData(), 30), Charset.forName("US-ASCII")));
        FileOutputStream out = getFileOutputStream(filePath, append);
        for (int i = 0; i < pieces.size(); i++) {
            Piece p = pieces.get(i);
            if (p.getId() != i) {
                System.out.println("Piece at index " + i + " has Id " + p.getId());
            }
            if (i < pieces.size() - 1) {
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
}