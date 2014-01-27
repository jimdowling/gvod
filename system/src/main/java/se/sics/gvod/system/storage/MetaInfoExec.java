package se.sics.gvod.system.storage;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.HashMap;
import se.sics.gvod.common.util.bencode.BEncoder;
import se.sics.gvod.common.util.bencode.BDecoder;
import se.sics.gvod.common.util.bencode.InvalidBEncodingException;
import se.sics.gvod.common.util.bencode.BEValue;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.gvod.common.BitField;
import se.sics.gvod.system.util.FileUtils;
import se.sics.gvod.address.Address;

public class MetaInfoExec implements MetaInfo {

    /**
     * The hashes for the first number of chunks are stored in the meta info
     * file to enable the viewer to start playing promptly. Each hash is 20
     * bytes long. There are 128 pieces per chunk, that is, 2560 bytes. TODO -
     * need to break this up into 2/3 UDP packets depending on MTU
     */
    private static final int FIRST_CHUNKS = 1 * BitField.NUM_PIECES_PER_CHUNK * 20;
    private static final Logger logger = LoggerFactory.getLogger(MetaInfoExec.class);
    /**
     * Name of meta info file.
     */
    private final String name;
//    private final int id;
    private final int width;
    private final int height;
    private final Long length;
    private final int pieceSize;
    /**
     * The piece hashes for the first few chunks included in the torrent file.
     */
    private final byte[] pieceHashes;
    private final int pieceHashesLength;
    private final Address bootstrapServerAddress;
    private final Address monitorAddress;
    private final long readingPeriod;
    private final boolean[] initializedPieceHashes;
    private final byte[] chunkHashes;
    private final String metaInfoAddress;
    private final boolean mp4;

    public MetaInfoExec(String name, int width, int height,
            int pieceSize, byte[] chunkHashes, int pieceHashesLength, long length,
            Address bootstrapServerAddress, long readingPeriod,
            boolean[] initializedPieceHashes,
            byte[] pieceHashes, String metaInfoAddress, Address monitorAddress) {

        if (name == null || chunkHashes == null || pieceHashes == null || metaInfoAddress == null
                || bootstrapServerAddress == null) {
            throw new NullPointerException();
        }
        this.name = name;
        this.width = width;
        this.height = height;
        this.pieceSize = pieceSize;
        this.pieceHashes = pieceHashes;
        this.pieceHashesLength = pieceHashesLength;
        this.length = length;
        this.bootstrapServerAddress = bootstrapServerAddress;
        this.readingPeriod = readingPeriod;
        this.initializedPieceHashes = initializedPieceHashes;
        this.chunkHashes = chunkHashes;
        this.metaInfoAddress = metaInfoAddress;
        this.monitorAddress = monitorAddress;

        if (testMp4() == true) {
            mp4 = true;
        } else {
            mp4 = false;
        }
//        id = ActiveTorrents.calculateVideoId(name);
    }

    /**
     * Creates a new MetaInfo from the given InputStream. The InputStream must
     * start with a correctly bencoded dictionary describing the torrent.
     * 
     * @param in InputStream
     * @param metaInfoAddress the String value for the torrent filename
     * @throws IOException 
     */
    public MetaInfoExec(InputStream in, String metaInfoAddress) throws IOException {
        this(new BDecoder(in), metaInfoAddress);
    }

    /**
     * Creates a new MetaInfo from the given BDecoder. The BDecoder must have a
     * complete dictionary describing the torrent.
     */
    public MetaInfoExec(BDecoder be, String metaInfoAddress) throws IOException {
        // Note that evaluation order matters here...
        this(be.bdecodeMap().getMap(), metaInfoAddress);
    }

    /**
     * Creates a new MetaInfo from a Map of BEValues and the SHA1 over the
     * original bencoded info dictionary (this is a hack, we could reconstruct
     * the bencoded stream and recalculate the hash). Will throw a
     * InvalidBEncodingException if the given map does not contain a valid
     * announce string or info dictionary.
     */
    public MetaInfoExec(Map m, String metaInfoAddress) throws InvalidBEncodingException {
        this.metaInfoAddress = metaInfoAddress;
        BEValue val = (BEValue) m.get("info");
        if (val == null) {
            throw new InvalidBEncodingException("Missing info map");
        }
        Map info = val.getMap();

        val = (BEValue) info.get("name");
        if (val == null) {
            throw new InvalidBEncodingException("Missing name string");
        }
        this.name = val.getString();

        val = (BEValue) info.get("piece length");
        if (val == null) {
            throw new InvalidBEncodingException("Missing piece length number");
        }
        this.pieceSize = val.getInt();

        val = (BEValue) info.get("width");
        if (val == null) {
            throw new InvalidBEncodingException("Missing video width number");
        }
        this.width = val.getInt();

        val = (BEValue) info.get("height");
        if (val == null) {
            throw new InvalidBEncodingException("Missing video height number");
        }
        this.height = val.getInt();

        val = (BEValue) info.get("readingPeriod");
        if (val == null) {
            throw new InvalidBEncodingException("Missing readingPeriod");
        }
        this.readingPeriod = val.getInt();
        val = (BEValue) info.get("bootstrapServerAddress");
        if (val == null) {
            throw new InvalidBEncodingException("Missing bootstrapServerAddress");
        }
        this.bootstrapServerAddress = val.getAddress();

        val = (BEValue) info.get("monitorAddress");
        if (val == null) {
            throw new InvalidBEncodingException("Missing monitorAddress");
        }
        this.monitorAddress = val.getAddress();

        val = (BEValue) info.get("piece_hashesLength");
        if (val == null) {
            throw new InvalidBEncodingException("Missing piece bytes");
        }
        this.pieceHashesLength = val.getInt();

        val = (BEValue) info.get("length");
        if (val != null) {
            this.length = val.getLong();
        } else {
            val = (BEValue) info.get("file");
            Map<String, Object> file = val.getMap();

            val = (BEValue) file.get("length");
            if (val == null) {
                throw new InvalidBEncodingException("Missing length");
            }
            this.length = val.getLong();
        }
        val = (BEValue) info.get("chunks");
        if (val == null) {
            throw new InvalidBEncodingException("Missing chunks bytes");
        }
        this.chunkHashes = val.getBytes();
        this.pieceHashes = new byte[chunkHashes.length * BitField.NUM_PIECES_PER_CHUNK];
        this.initializedPieceHashes = new boolean[chunkHashes.length / 20];

        val = (BEValue) info.get("firstChunks");
        if (val == null) {
            throw new InvalidBEncodingException("Missing first chunks bytes");
        }
        byte[] firstChunks = val.getBytes();
        System.arraycopy(firstChunks, 0, pieceHashes, 0, firstChunks.length);
        int j = 0;
        for (j = 0; j < firstChunks.length / BitField.NUM_PIECES_PER_CHUNK / 20; j++) {
            this.initializedPieceHashes[j] = true;
        }
        for (int i = j; i < chunkHashes.length / 20; i++) {
            this.initializedPieceHashes[i] = false;
        }
        File f = new File(metaInfoAddress.concat(".pieces"));
        if (f.exists()) {
            FileInputStream in = null;
            try {
                in = new FileInputStream(f);
                setPiecesHash(in);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException ex) {
                        java.util.logging.Logger.getLogger(MetaInfoExec.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }

        if (testMp4() == true) {
            mp4 = true;
        } else {
            mp4 = false;
        }
    }

    private Map<String, Object> createInfoMap() {
        Map<String, Object> info = new HashMap<String, Object>();
        info.put("name", name);
//        info.put("id", id);
        info.put("width", width);
        info.put("height", height);
        info.put("piece length", pieceSize);
        info.put("chunks", chunkHashes);
        byte[] firstChunks = new byte[FIRST_CHUNKS];
        System.arraycopy(pieceHashes, 0, firstChunks, 0, firstChunks.length);
        info.put("firstChunks", firstChunks);
        info.put("piece_hashesLength", pieceHashesLength);
        Map<String, Object> file = new HashMap<String, Object>();
        file.put("length", length);
        info.put("file", file);
        info.put("readingPeriod", readingPeriod);
        info.put("bootstrapServerAddress", bootstrapServerAddress);
        info.put("monitorAddress", monitorAddress);
        return info;
    }

    private boolean testMp4() {
        String prefix = FileUtils.getPostFix(name);
        return prefix.compareToIgnoreCase(".mp4") == 0;
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public int getHeight() {
        return this.height;
    }

    public int getnbSubpieces() {
        return pieceHashesLength / 20;
    }

    public Address getBootstrapServerAddress() {
        return bootstrapServerAddress;
    }

    public Address getMonitorAddress() {
        return monitorAddress;
    }

    public long getReadingPeriod() {
        return readingPeriod;
    }

    /**
     * Given a piece number return the number of subpieces in that piece. The
     * last piece may have a number of subpieces less than 16.
     *
     * @param piece number
     * @return number of subpieces in that piece
     */
    public int getPieceSize(int piece) {
        int lastSubpiece = piece * BitField.NUM_SUBPIECES_PER_PIECE
                + (BitField.NUM_SUBPIECES_PER_PIECE - 1);
        int numSubpieces = getnbSubpieces();
        if (lastSubpiece >= 0 && lastSubpiece < numSubpieces) { // - 1
            return pieceSize * BitField.NUM_SUBPIECES_PER_PIECE;
        } else if (lastSubpiece >= numSubpieces) { // - 1
//            return (int) (length - ((piece-1) * 16 * pieceSize));
            return (int) (((length / BitField.NUM_PIECES_PER_CHUNK) / pieceSize) / BitField.NUM_SUBPIECES_PER_PIECE);
        } else {
            throw new IndexOutOfBoundsException("no piece: " + piece);
        }
    }

    @Override
    public int getPieceNbSubPieces(int piece) {
        int lastSubpiece = piece * BitField.NUM_SUBPIECES_PER_PIECE + (BitField.NUM_SUBPIECES_PER_PIECE - 1);
        int nbSubpieces = getnbSubpieces();
        if (lastSubpiece >= 0 && lastSubpiece < nbSubpieces) { //  - 1
            return BitField.NUM_SUBPIECES_PER_PIECE;
        } else if (lastSubpiece >= nbSubpieces) { // - 1
            return (int) (nbSubpieces - (piece * BitField.NUM_SUBPIECES_PER_PIECE));
        } else {
            throw new IndexOutOfBoundsException("no piece: " + piece);
        }
    }

    byte[] getPieceHashes() {
        return pieceHashes;
    }

    public boolean[] getInitializedPieceHashes() {
        return initializedPieceHashes;
    }

    public byte[] getChunksHashes() {
        return chunkHashes;
    }

    /**
     * returns the number of subpieces found in the given piece.
     * For example, the last piece may not have a full complement of
     * subpieces.
     * @param piece
     * @return 
     */
    public int getSubpieceSize(int piece) {
        int numPieces = getnbSubpieces();
        if (piece >= 0 && piece < numPieces - 1) {
            return pieceSize;
        } else if (piece == numPieces - 1) {
            return (int) (length - piece * pieceSize);
        } else {
            throw new IndexOutOfBoundsException("no piece: " + piece);
        }
    }

    public MetaInfoExec reannounce() {
        return new MetaInfoExec(name, width, height, pieceSize,
                chunkHashes, pieceHashesLength, length, bootstrapServerAddress,
                readingPeriod, initializedPieceHashes, pieceHashes, metaInfoAddress,
                monitorAddress);
    }

    public Long getLength() {
        return length;
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * Checks that the given piece has the same SHA1 hash as the given byte
     * array. Returns random results or IndexOutOfBoundsExceptions when the
     * piece number is unknown.
     */
    public boolean checkPiece(int piece, byte[] bs, int off, int length) {
        // Check digest
        MessageDigest sha1;
        try {
            sha1 = MessageDigest.getInstance("SHA");
        } catch (NoSuchAlgorithmException nsae) {
            throw new InternalError("No SHA digest available: " + nsae);
        }

        sha1.update(bs, off, length);
        byte[] hash = sha1.digest();
        for (int i = 0; i < 20; i++) {
            if (hash[i] != pieceHashes[20 * piece + i]) {
                logger.trace("check fail for piece : " + piece);
                return false;
            }
        }
        return true;
    }

    /**
     * Encode a byte array as a hex encoded string.
     */
    private static String hexencode(byte[] bs) {
        StringBuilder sb = new StringBuilder(bs.length * 2);
        for (byte element : bs) {
            int c = element & 0xFF;
            if (c < 16) {
                sb.append('0');
            }
            sb.append(Integer.toHexString(c));
        }

        return sb.toString();
    }

    @Override
    public byte[] getData() {
        Map<String, Object> m = new HashMap<String, Object>();
        Map info = createInfoMap();
        m.put("info", info);
        return BEncoder.bencode(m);
    }

    private byte[] getPiecesHashes() {
        Map<String, Object> m = new HashMap<String, Object>();
        m.put("piecesHashes", pieceHashes);
        m.put("initializedPieceHashes", initializedPieceHashes);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("info", m);
        byte[] result = BEncoder.bencode(map);
        return result;
    }

    @Override
    public boolean haveHashes(int chunk) {
        return initializedPieceHashes[chunk];
    }

    @Override
    public boolean setPieceHashes(byte[] pieceHashes, int chunk) {
        if (initializedPieceHashes[chunk]) {
            return true;
        }
        MessageDigest sha1;
        try {
            sha1 = MessageDigest.getInstance("SHA");
        } catch (NoSuchAlgorithmException nsae) {
            throw new InternalError("No SHA digest available: " + nsae);
        }

        sha1.update(pieceHashes, 0, pieceHashes.length);
        byte[] hash = sha1.digest();
        for (int i = 0; i < 20; i++) {
            if (hash[i] != chunkHashes[20 * chunk + i]) {
                return false;
            }
        }
        for (int i = 0; i < pieceHashes.length; i++) {
            this.pieceHashes[BitField.NUM_PIECES_PER_CHUNK * chunk * 20 + i] = pieceHashes[i];
            int val = BitField.NUM_PIECES_PER_CHUNK * chunk * 20 + i;
        }
        initializedPieceHashes[chunk] = true;
        return true;
    }
    
    public void writePieceHashesToFile() throws FileNotFoundException, IOException {
            FileOutputStream fos = new FileOutputStream(metaInfoAddress.concat(".pieces"));
            fos.write(getPiecesHashes());
            fos.close();
    }
    
    public void readPieceHashesFromFile() throws FileNotFoundException, IOException {
            FileInputStream fis = new FileInputStream(metaInfoAddress.concat(".pieces"));
//            fos.write(getPiecesHashes());
            fis.close();
    }    

    @Override
    public byte[] getChunkHashes(int chunk) {
        // TODO malicious attack to ask for out-of-range chunk

        if (chunk < 1 || chunk > initializedPieceHashes.length) {
            throw new ArrayIndexOutOfBoundsException("Non-existant chunk : " + chunk);
//            return null;
        }
        if (initializedPieceHashes[chunk]) {
            byte[] result = new byte[BitField.NUM_PIECES_PER_CHUNK * 20];
            for (int i = 0; i < BitField.NUM_PIECES_PER_CHUNK; i++) {
                for (int j = 0; j < 20; j++) {
                    result[i * 20 + j] = pieceHashes[chunk * BitField.NUM_PIECES_PER_CHUNK * 20 + 20 * i + j];
                }
            }
            return result;
        } else {
            return null;
        }
    }

    @Override
    public int getNbChunks() {
        return initializedPieceHashes.length;
    }

    public int getChunkNbPieces(int chunk) {
        int lastPiece = chunk * BitField.NUM_PIECES_PER_CHUNK + BitField.NUM_PIECES_PER_CHUNK - 1;
        int nbPieces = getNbPieces();
        if (lastPiece >= 0 && lastPiece < nbPieces - 1) {
            return BitField.NUM_PIECES_PER_CHUNK;
        } else if (lastPiece >= nbPieces - 1) {
            return (int) (nbPieces - chunk * BitField.NUM_PIECES_PER_CHUNK);
        } else {
            throw new IndexOutOfBoundsException("no chunk: " + chunk);
        }
    }

    public int getNbPieces() {
        return getnbSubpieces() / BitField.NUM_SUBPIECES_PER_PIECE + 1;
    }

    public boolean checkChunk(int chunk, byte[] bs, int off, int length) {
        // Check digest
        MessageDigest sha1;
        try {
            sha1 = MessageDigest.getInstance("SHA");
        } catch (NoSuchAlgorithmException nsae) {
            throw new InternalError("No SHA digest available: " + nsae);
        }

        sha1.update(bs, off, length);
        byte[] hash = sha1.digest();

        // too expensive to copy and use Array.equals here, just loop and compare.
        for (int i = 0; i < 20; i++) {
            if (hash[i] != chunkHashes[20 * chunk + i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * TODO: Should I throw execeptions everytime a BDecoding error occurs?
     *
     * @param in
     */
    private void setPiecesHash(FileInputStream in) {
        try {
            Map m = new BDecoder(in).bdecode().getMap();

            BEValue val = (BEValue) m.get("info");
            if (val == null) {
                throw new InvalidBEncodingException("Missing info map piecesHash");
            }
            Map info = val.getMap();

            val = (BEValue) info.get("piecesHashes");
            if (val == null) {
                throw new InvalidBEncodingException("Missing piecesHashes map");
            }
            byte[] temp = val.getBytes();
            System.arraycopy(temp, 0, pieceHashes, 0, temp.length);

            val = (BEValue) info.get("initializedPieceHashes");
            if (val == null) {
                throw new InvalidBEncodingException("Missing initializedPieceHashes map");
            }
            boolean[] t = val.getBoolean();
            System.arraycopy(t, 0, initializedPieceHashes, 0, t.length);

            //verify if it correspond to the chunk hashs in the .data
            for (int i = 0; i < getNbChunks(); i++) {
                if (haveHashes(i)) {
                    byte[] chunkHashes = new byte[BitField.NUM_PIECES_PER_CHUNK * 20];
                    for (int j = 0; j < getChunkNbPieces(i); j++) {

                        System.arraycopy(pieceHashes, i * BitField.NUM_PIECES_PER_CHUNK * 20 + j * 20, chunkHashes, j * 20, 20);
                    }
                    boolean correctHash = checkChunk(i, chunkHashes, 0, 20 * BitField.NUM_PIECES_PER_CHUNK);
                    if (!correctHash) {
                        initializedPieceHashes[i] = false;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isMp4() {
        return mp4;
    }
}
