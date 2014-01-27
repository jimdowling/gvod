package se.sics.gvod.ls.video;

import com.onionnetworks.fec.FECCode;
import com.onionnetworks.fec.FECCodeFactory;
import com.onionnetworks.util.Buffer;
import java.nio.charset.Charset;
import java.util.*;
import se.sics.gvod.ls.system.LSConfig;
import se.sics.gvod.video.msgs.EncodedSubPiece;
import se.sics.gvod.video.msgs.Piece;
import se.sics.gvod.video.msgs.SubPiece;

/**
 * Class used to encode/decode a Piece (a set of SubPieces).
 *
 * @author Niklas Wahl&#233;n <nwahlen@kth.se>
 */
public class VideoFEC {

    // Java FEC from OnionNetworks
    // TODO: move this instance into VideoIO, and pass it to the constructor?
    private FECCode fec;
    // core variables needed
    private int k, n, packetSize;
    // arrays
    private byte[][] sourceData;
    private byte[][] encodedData;
    private int[] encodedIndices;
    // the piece
    private Piece piece;
    // encoding
    private EncodedSubPiece[] encodedSubPieces;
    // decoding
    private int receivedPieces;
    private boolean decoded;
    private Map<Integer, EncodedSubPiece> receivedPiecesMap;
    // bookkeeping
    private Set<Integer> received;
    private Set<Integer> missing;

    /**
     * Constructor used when having less than k sub-pieces (k being the number
     * of source sub-pieces). #addEncodedSubPiece is then called until the
     * object contains k sub-pieces, whereafter #decode can be called.
     *
     * @param pieceId ID of the Piece to be decoded.
     */
    public VideoFEC(int pieceId) {
        /*
         * Initialize FEC
         */
        piece = new Piece(pieceId);
        // source pieces
        k = LSConfig.FEC_SUB_PIECES;
        // total encoded pieces
        n = LSConfig.FEC_ENCODED_PIECES;
        packetSize = SubPiece.SUBPIECE_DATA_SIZE;
        fec = FECCodeFactory.getDefault().createFECCode(k, n);
        receivedPieces = 0;
        encodedData = new byte[n][packetSize];
        /*
         * Prepare decoding
         */
        sourceData = new byte[k][packetSize];
        // only need k indices for decoding
        encodedIndices = new int[k];
        received = new HashSet<Integer>();
        missing = new HashSet<Integer>();
        for (int i = 0; i < n; i++) {
            missing.add(i);
        }
        decoded = false;
        receivedPiecesMap = new HashMap<Integer, EncodedSubPiece>();
    }

    /**
     * Constructor used for encoding. After calling this constructor, call
     * #getEncodedSubPieces to get the encoded sub-pieces for transmitting.
     *
     * @param piece The piece which contains the sub-pieces to be transmitted.
     * Piece.
     */
    public VideoFEC(Piece piece) {
        if (piece.getSubPieces().length != LSConfig.FEC_SUB_PIECES) {
            throw new IllegalArgumentException("A Piece has to contain "
                    + LSConfig.FEC_SUB_PIECES + " sub-pieces "
                    + "(this had " + piece.getSubPieces().length + ")");
        }
        int startGlobalID = piece.getId() * LSConfig.FEC_ENCODED_PIECES;
        /*
         * Initialize FEC
         */
        this.piece = piece;
        // source pieces
        k = LSConfig.FEC_SUB_PIECES;
        // total pieces to send (source+encoded)
        n = LSConfig.FEC_ENCODED_PIECES;
        packetSize = SubPiece.SUBPIECE_DATA_SIZE;
        fec = FECCodeFactory.getDefault().createFECCode(k, n);
        receivedPieces = k;
        encodedData = new byte[n][packetSize];
        // encoding results in n indices
        encodedIndices = new int[n];

        /*
         * Encode
         */
        byte[][] pieceData = new byte[k][packetSize];
        Buffer[] sourceBuffer = new Buffer[k];
        Buffer[] encodedBuffer = new Buffer[n];
        for (int i = 0; i < sourceBuffer.length; i++) {
            sourceBuffer[i] = new Buffer(Arrays.copyOf(piece.getSubPiece(i).getData(), SubPiece.SUBPIECE_DATA_SIZE));
            if (piece.getSubPieces()[i].getId() != i) {
                throw new RuntimeException("Encoding error. SubPiece[" + i + "] has id " + piece.getSubPieces()[i].getId());
            }
        }
        for (int i = 0; i < encodedBuffer.length; i++) {
            encodedBuffer[i] = new Buffer(encodedData[i]);
            encodedIndices[i] = i;
        }
        // TODO: How it should be done
//        for(int e = k, i=0; e < n; e++) {
//            encodedIndices[i++] = e;
//        }
        fec.encode(sourceBuffer, encodedBuffer, encodedIndices);
        // Prepare EncodedSubPieces
        encodedSubPieces = new EncodedSubPiece[n];
        for (int i = 0; i < n; i++) {
            encodedSubPieces[i] = new EncodedSubPiece(startGlobalID + i, i, encodedData[i], piece.getId());
            if (i < k) {
                if (!Arrays.equals(encodedSubPieces[i].getData(), piece.getSubPieces()[i].getData())
                        || encodedSubPieces[i].getEncodedIndex() != piece.getSubPieces()[i].getId()
                        || encodedSubPieces[i].getParentId() != piece.getId()) {
                    throw new RuntimeException("Encoding error"
                            + "\n - encoded sub-piece[id:" + encodedSubPieces[i].getEncodedIndex() + ", parent: " + encodedSubPieces[i].getParentId() + "]"
                            + "\n - original sub-piece[id: " + piece.getSubPieces()[i].getId() + ", parent: " + piece.getId() + "]");
                }
            }
        }
    }

    public Piece decode() {
        if (!isReady()) {
            // TODO: change to a more suitable exception class
            throw new RuntimeException("This piece is not available for decoding.");
        }
        if(decoded) {
            throw new RuntimeException("This piece (" + piece.getId() + ") was already decoded.");
        }
        Buffer[] sourceBuffer = new Buffer[k];
        for (int i = 0; i < sourceBuffer.length; i++) {
            sourceBuffer[i] = new Buffer(sourceData[i]);
        }
        fec.decode(sourceBuffer, encodedIndices);
        SubPiece[] subPieces = new SubPiece[k];
        for (int i = 0; i < sourceData.length; i++) {
            subPieces[i] = new SubPiece(i, sourceData[i], piece);
        }
        piece.setSubPieces(subPieces);
        decoded = true;
        return piece;
    }

    public EncodedSubPiece[] getEncodedSubPieces() {
        return encodedSubPieces;
    }

    public EncodedSubPiece getEncodedSubPiece(int i) {
        return encodedSubPieces[i];
    }

    public Piece getPiece() {
        return piece;
    }

    public void addEncodedSubPiece(EncodedSubPiece p) {
        if (p.getParentId() != piece.getId()) {
            throw new IllegalArgumentException("The added sub-piece does not belong to this piece.");
        }
        if (isReady()) {
            throw new IllegalArgumentException("There are already enough sub-pieces added to this piece"
                    + " (" + (receivedPieces) + ").");
        }
        if (received.contains(p.getGlobalId())) {
            throw new IllegalArgumentException("This sub-piece was already added to the decoder.");
        }
        encodedIndices[receivedPieces] = p.getEncodedIndex();
        // TODO  - possibly don't need to copy this array of bytes.
        sourceData[receivedPieces] = Arrays.copyOf(p.getData(),p.getData().length);
        receivedPieces++;
        // bookkeeping
        received.add(p.getGlobalId());
        missing.remove(p.getEncodedIndex());
    }

    public int getNumberOfReceivedPieces() {
        return receivedPieces;
    }

    public boolean isReady() {
        return receivedPieces == k;
    }

    public boolean contains(EncodedSubPiece p) {
        return received.contains(p.getGlobalId());
    }

    public int getId() {
        return piece.getId();
    }

    public Set<Integer> getMissing() {
        return missing;
    }

    public boolean isDecoded() {
        return decoded;
    }
}
