/**
 * This file is part of the Kompics P2P Framework.
 *
 * Copyright (C) 2009 Swedish Institute of Computer Science (SICS)
 * Copyright (C) 2009 Royal Institute of Technology (KTH)
 *
 * Kompics is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package se.sics.gvod.system.storage;

import se.sics.gvod.common.BitField;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;
import java.util.Iterator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.io.IOException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel.MapMode;
import java.util.Map;
import javax.swing.JProgressBar;
import se.sics.gvod.config.VodConfig;
import se.sics.gvod.system.util.FlvHandler;
import se.sics.gvod.address.Address;

/**
 * 
 */
public class StorageMemMapWholeFile implements Storage {

    private static final int SUBPIECE_SIZE = 1024;
    private MetaInfoExec metainfo;
    private int needed;
    private String name;
    private final String baseDir;
    private long length;
    private MappedByteBuffer mbb;
    private RandomAccessFile raf;
    int subpieceSize;
    int nbSubpieces;
    private final BitField bitfield;

    /* create storage from infos about the path
     */
    public StorageMemMapWholeFile(MetaInfoExec metainfo, String baseDir) throws IOException {
        this.metainfo = metainfo;
        this.baseDir = baseDir;
        needed = metainfo.getnbSubpieces();
        nbSubpieces = metainfo.getnbSubpieces();
        bitfield = new BitField(needed);
    }

    /* create storage from the path itself
     */
    public StorageMemMapWholeFile(File videoFile, int width, int height, 
            Address bootstrapServerAddress,
            long readingPeriod, String metainfoAddress, Address monitorAddress)
            throws IOException {
        // Create names, rafs and lengths arrays.
        setFile(videoFile);
        baseDir = videoFile.getParent();
        subpieceSize = SUBPIECE_SIZE;
        nbSubpieces = (int) ((length - 1) / subpieceSize) + 1;
        byte[] chunkHashes = new byte[VodConfig.NUM_HASHES_IN_TORRENT_FILE *
                ((((nbSubpieces / BitField.NUM_SUBPIECES_PER_PIECE) + 1) / BitField.NUM_PIECES_PER_CHUNK) + 1)];
        int pieceHashesLength = VodConfig.NUM_HASHES_IN_TORRENT_FILE * nbSubpieces;
        bitfield = new BitField(nbSubpieces);
        needed = 0;
        
        // Note that the piece_hashes are not correctly setup yet.
        metainfo = new MetaInfoExec(videoFile.getName(), width, height,
                subpieceSize, chunkHashes, pieceHashesLength, length,
                bootstrapServerAddress,
                readingPeriod, new boolean[chunkHashes.length / VodConfig.NUM_HASHES_IN_TORRENT_FILE],
                new byte[chunkHashes.length * BitField.NUM_PIECES_PER_CHUNK], metainfoAddress, monitorAddress);

    }

    private void setFile(File videoFile) throws IOException {
        name = videoFile.getPath();
        length = videoFile.length();
        raf = new RandomAccessFile(videoFile, "r");
        mbb = raf.getChannel().map(MapMode.READ_ONLY, 0, length);
        // try to load the whole file into memory
        mbb.load();
        raf.close();
    }

    @Override
    public void create(JProgressBar progressBar) throws IOException {
        // Calculate piece_hashes
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA");
        } catch (NoSuchAlgorithmException nsa) {
            throw new InternalError(nsa.toString());
        }

        byte[] pieceHashes = metainfo.getPieceHashes();

        byte[] piece = new byte[subpieceSize * BitField.NUM_SUBPIECES_PER_PIECE];
        for (int i = 0; i < (nbSubpieces / BitField.NUM_SUBPIECES_PER_PIECE) + 1; i++) {
            if (progressBar != null && (i % 100 == 0)) {
                int percentComplete = i / ((nbSubpieces / BitField.NUM_SUBPIECES_PER_PIECE) + 1);
                progressBar.setValue(percentComplete);
            }

            int pieceLength = getUncheckedPiece(i, piece, 0);
            if (pieceLength > 0) {
                digest.update(piece, 0, pieceLength);
                byte[] hash = digest.digest();
                System.arraycopy(hash, 0, pieceHashes, VodConfig.NUM_HASHES_IN_TORRENT_FILE * i, VodConfig.NUM_HASHES_IN_TORRENT_FILE);
                for (int j = 0; j < metainfo.getPieceNbSubPieces(i); j++) {
                    bitfield.set(i * BitField.NUM_SUBPIECES_PER_PIECE + j, true);
                }
            }
        }

        boolean[] initializedPieceHash = metainfo.getInitializedPieceHashes();
        byte[] chunkHash = metainfo.getChunksHashes();

        byte[] chunk = new byte[BitField.NUM_PIECES_PER_CHUNK * VodConfig.NUM_HASHES_IN_TORRENT_FILE];
        for (int i = 0; i < pieceHashes.length / BitField.NUM_PIECES_PER_CHUNK / VodConfig.NUM_HASHES_IN_TORRENT_FILE; i++) {
            for (int j = 0; j < BitField.NUM_PIECES_PER_CHUNK * VodConfig.NUM_HASHES_IN_TORRENT_FILE; j++) {
                chunk[j] = pieceHashes[i * BitField.NUM_PIECES_PER_CHUNK * VodConfig.NUM_HASHES_IN_TORRENT_FILE + j];
            }
            digest.update(chunk, 0, chunk.length);
            byte[] hash = digest.digest();
            System.arraycopy(hash, 0, chunkHash, VodConfig.NUM_HASHES_IN_TORRENT_FILE * i, VodConfig.NUM_HASHES_IN_TORRENT_FILE);
            initializedPieceHash[i] = true;
        }
        // Reannounce to force recalculating the info_hash.
        metainfo = metainfo.reannounce();
    }

    @Override
    public void writePieceHashesToFile() throws FileNotFoundException, IOException {
        metainfo.writePieceHashesToFile();
    }
    
    private int getUncheckedSubPiece(int piece, byte[] bs, int off)
            throws IOException {
        int start = piece * metainfo.getSubpieceSize(0);
        int subPieceSize = metainfo.getSubpieceSize(piece);
        long raflen = this.length;
        assert(raflen != 0);
        while (start > raflen) {
            start -= raflen;
            raflen = this.length;
        }

        int read = 0;
        while (read < subPieceSize) {
            int need = subPieceSize - read;
//            int len = (start + need < raflen) ? need : (int) (raflen - start);
            int len = Math.min(need, (int) (raflen - start));
            synchronized (mbb) {
                mbb.position(start);
                mbb.get(bs, off + read, len);
            }
            read += len;
            if (need - len > 0) {
                raflen = this.length;
                start = 0;
            }
        }

        return subPieceSize;
    }

    @Override
    public byte[] getSubpiece(int subpiece) throws IOException {
        byte[] bs = new byte[metainfo.getSubpieceSize(subpiece)];
        getUncheckedSubPiece(subpiece, bs, 0);
        return bs;
    }

    private int getUncheckedPiece(int piece, byte[] bs, int off)
            throws IOException {
        // XXX - copy/paste code from putSubpiece().
        int start = piece * 1024 * BitField.NUM_SUBPIECES_PER_PIECE;
        int pieceSize = metainfo.getPieceSize(piece);
        long raflen = this.length;
        int i = 0;
        while (start > raflen && i < 10) {
            start -= raflen;
            raflen = this.length;
            i++;
        }

        int read = 0;
        while (read < pieceSize) {
            int need = pieceSize - read;
            int len = (start + need < raflen) ? need : (int) (raflen - start);
            synchronized (mbb) {
                mbb.position(start);
                try {
                // TODO - sometimes we get a jvm crash here...
                    mbb.get(bs, off + read, len);
                } catch (java.lang.IndexOutOfBoundsException e) {
                    e.printStackTrace();
                }
            }
            read += len;
            if (need - len > 0) {
                i++;
                raflen = this.length;
                start = 0;
            }
        }

        return pieceSize;
    }

    /**
     * Returns the MetaInfo associated with this Storage.
     */
    @Override
    public MetaInfoExec getMetaInfo() {
        return metainfo;
    }

    /**
     * How many pieces are still missing from this storage.
     */
    @Override
    public int needed() {
        return needed;
    }

    /**
     * Whether or not this storage contains all pieces if the MetaInfo.
     */
    @Override
    public boolean complete() {
        return needed == 0;
    }

    /**
     * The BitField that tells which pieces this storage contains. Do not change
     * this since this is the current state of the storage.
     */
    @Override
    public BitField getBitField() {
        return bitfield;
    }

    /**
     * Creates and/or validates the path from metainfo.
     * @param resumeDownloading true if the file already exists, and we want to check it. False if it is a new file.
     * @throws IOException 
     */
    @Override
    public void check(boolean resumeDownloading) throws IOException {
        String fileName = metainfo.getName();
        File f = createFileFromNames(baseDir + File.separator + fileName);
        length = metainfo.getLength();
        raf = new RandomAccessFile(f, "rw");
        mbb = raf.getChannel().map(MapMode.READ_WRITE, 0, length);
        raf.close();
        name = f.getName();
        if (resumeDownloading) {
            checkCreateFiles();
        }
    }

    /**
     * Removes 'suspicious' characters from the give path name.
     */
    private String filterName(String name) {
        // XXX - Is this enough?
        return name.replace(File.separatorChar, '_');
    }

    private File createFileFromNames(File base, List names) throws IOException {
        File f = null;
        Iterator it = names.iterator();
        while (it.hasNext()) {
            String fileName = filterName((String) it.next());
            if (it.hasNext()) {
                // Another dir in the hierarchy.
                f = new File(base, fileName);
                if (!f.mkdir() && !f.isDirectory()) {
                    throw new IOException("Could not create directory " + f);
                }
                base = f;
            } else {
                // The final element (path) in the hierarchy.
                f = new File(base, fileName);
                if (!f.createNewFile() && !f.exists()) {
                    throw new IOException("Could not create file " + f);
                }
            }
        }
        return f;
    }

    private File createFileFromNames(String fileName) throws IOException {
        File f = new File(fileName);

        if (!f.createNewFile() && !f.exists()) {
            throw new IOException("Could not create file " + f);
        }
        return f;
    }

    private void checkCreateFiles() throws IOException {
        // Whether we are resuming or not,
        // if any of the files already exists we assume we are resuming.
        boolean resume = false;

        // Make sure all files are available and of correct length
        int mbbLength = mbb.limit();
        if (mbbLength == this.length) {
            resume = true; // XXX Could dynamically check
        } else if (mbbLength == 0) {
            allocateFile();
        } else {
            // no need to synchronize access to mbb here, as this is called
            // directly after creating the mbb
            mbb.limit((int) this.length);
            allocateFile();
        }

        // Check which pieces match and which don't
        if (resume) {
            nbSubpieces = metainfo.getnbSubpieces();
            byte[] piece = new byte[metainfo.getSubpieceSize(0) * BitField.NUM_SUBPIECES_PER_PIECE];
            for (int i = 0; i < metainfo.getNbChunks(); i++) {
                if (metainfo.haveHashes(i)) {
                    for (int j = 0; j < metainfo.getChunkNbPieces(i); j++) {
                        if (metainfo.checkPiece(i * BitField.NUM_PIECES_PER_CHUNK + j, getUncheckedPiece(i * BitField.NUM_PIECES_PER_CHUNK + j), 0, metainfo.getPieceSize(i * BitField.NUM_PIECES_PER_CHUNK + j))) {
                            for (int k = 0; k < metainfo.getPieceNbSubPieces(i * BitField.NUM_PIECES_PER_CHUNK + j); k++) {
                                bitfield.set(i * BitField.NUM_PIECES_PER_CHUNK * BitField.NUM_SUBPIECES_PER_PIECE + j * BitField.NUM_SUBPIECES_PER_PIECE + k, true);
                                needed--;
                            }
                        }
                    }
                } else {
                    byte[] chunkHashes = new byte[BitField.NUM_PIECES_PER_CHUNK * VodConfig.NUM_HASHES_IN_TORRENT_FILE];
                    for (int j = 0; j < metainfo.getChunkNbPieces(i); j++) {
                        int pieceLength = getUncheckedPiece(i * BitField.NUM_PIECES_PER_CHUNK + j, piece, 0);

                        MessageDigest sha1;
                        try {
                            sha1 = MessageDigest.getInstance("SHA");
                        } catch (NoSuchAlgorithmException nsae) {
                            throw new InternalError("No SHA digest available: " + nsae);
                        }

                        sha1.update(piece, 0, pieceLength);
                        byte[] hash = sha1.digest();

                        System.arraycopy(hash, 0, chunkHashes, j * VodConfig.NUM_HASHES_IN_TORRENT_FILE, VodConfig.NUM_HASHES_IN_TORRENT_FILE);
                    }
                    boolean correctHash = metainfo.checkChunk(i, chunkHashes, 0, VodConfig.NUM_HASHES_IN_TORRENT_FILE * BitField.NUM_PIECES_PER_CHUNK);
                    if (correctHash) {
                        for (int j = 0; j < metainfo.getChunkNbPieces(i); j++) {
                            for (int k = 0; k < metainfo.getPieceNbSubPieces(i * BitField.NUM_PIECES_PER_CHUNK + j); k++) {
                                bitfield.set(i * BitField.NUM_PIECES_PER_CHUNK * BitField.NUM_SUBPIECES_PER_PIECE + j * BitField.NUM_SUBPIECES_PER_PIECE + k, true);
                                needed--;
                            }
                        }
                        metainfo.setPieceHashes(chunkHashes, i);
                    }
                }
            }
        }
    }

    private void allocateFile() throws IOException {
        // XXX - Is this the best way to make sure we have enough space for
        // the whole path?
        // TODO - what about the size of the last subpiece? Here, we assume the
        // last subpiece has ZEROBLOCKSIZE

        final int ZEROBLOCKSIZE = metainfo.getSubpieceSize(0);
        byte[] zeros = new byte[ZEROBLOCKSIZE];
        int i;
        for (i = 0; i < length / ZEROBLOCKSIZE; i++) {
            // no need to synchronize access to mbb here, as this is called
            // directly after creating the mbb
            mbb.put(zeros);
        }
        int size = (int) (length - i * ZEROBLOCKSIZE);
        mbb.put(zeros, 0, size);
    }

    @Override
    public boolean checkPiece(int piece) throws IOException {
        byte[] bs = new byte[metainfo.getSubpieceSize(0) * BitField.NUM_SUBPIECES_PER_PIECE];
        int pieceLength = getUncheckedPiece(piece, bs, 0);
        return metainfo.checkPiece(piece, bs, 0, pieceLength);
    }

    /**
     * Put the piece in the Storage if it is correct.
     *
     * @return true if the piece was correct (sha metainfo hash matches),
     *         otherwise false.
     * @exception IOException
     *                when some storage related error occurs.
     */
    @Override
    public byte[] getUncheckedPiece(int piece) throws IOException {
        byte[] result = new byte[metainfo.getSubpieceSize(0) * BitField.NUM_SUBPIECES_PER_PIECE];
        getUncheckedPiece(piece, result, 0);
        return result;
    }

    @Override
    public boolean putSubpiece(int piece, byte[] bs) throws IOException {
        // First check if the piece is correct.
        // If we were paranoid we could copy the array first.
        int bsLength = bs.length;

        synchronized (bitfield) {
            if (bitfield.get(piece)) {
                return true; // No need to store twice.
            } else {
                bitfield.set(piece, false);
                needed--;
            }
        }

        int start = piece * metainfo.getSubpieceSize(0);
        int i = 0;
        long raflen = this.length;
        while (start > raflen) {
            i++;
            start -= raflen;
            raflen = this.length;
        }

        int written = 0;
        int off = 0;
        while (written < bsLength) {
            int need = bsLength - written;
            int len = (start + need < raflen) ? need : (int) (raflen - start);
            synchronized (mbb) {
                mbb.position(start);
                mbb.put(bs, off + written, len);
            }
            written += len;
            if (need - len > 0) {
                i++;
                raflen = bsLength;
                start = 0;
            }
        }

        return true;
    }

    @Override
    public void removeSubpiece(int subpiece) throws IOException {
        synchronized (bitfield) {
            bitfield.remove(subpiece);
            needed++;
        }
    }

    /**
     * Returns a byte array containing the requested piece or null if the
     * storage doesn't contain the piece yet.
     */
    @Override
    public Map<Integer, byte[]> getSubpieces(int piece) throws IOException {
        if (!bitfield.getPiece(piece)) {
            return null;
        }

        Map<Integer, byte[]> result = new HashMap<Integer, byte[]>();

        int subpiece;
        for (int i = 0; i < BitField.NUM_SUBPIECES_PER_PIECE; i++) {
            subpiece = piece * BitField.NUM_SUBPIECES_PER_PIECE + i;
            byte[] bs = new byte[metainfo.getSubpieceSize(subpiece)];
            getUncheckedSubPiece(subpiece, bs, 0);
            result.put(subpiece, bs);
            if (subpiece == metainfo.getnbSubpieces() - 1) {
                return result;
            }
        }
        return result;
    }

    @Override
    public byte[] getPiece(int piece) throws IOException {
        if (!bitfield.getPiece(piece)) {
            return null;
        }
        byte[] bs = new byte[metainfo.getPieceSize(piece)];
        getUncheckedPiece(piece, bs, 0);
        return bs;

    }

    @Override
    public List<Integer> missingSubpieces(int piece) {
        List<Integer> result = new ArrayList<Integer>();
        int subpiece;
        if (bitfield.getPiece(piece)) {
            System.out.println("bug");
        }
        for (int i = 0; i < BitField.NUM_SUBPIECES_PER_PIECE; i++) {
            subpiece = piece * BitField.NUM_SUBPIECES_PER_PIECE + i;
            if (!bitfield.get(subpiece)) {
                result.add(subpiece);
            }
            if (subpiece == metainfo.getnbSubpieces() - 1) {
                return result;
            }
        }
        return result;
    }

    @Override
    public String percent() {
        StringBuilder sb = new StringBuilder();
        float p = ((float) (nbSubpieces - needed) / nbSubpieces) * 100;
        sb.append(p).append("% ").append(bitfield.getChunkHumanReadable());
        return sb.toString();
    }
    
    
    /** The Java logger used to process our log events. */
    protected static final Logger log = Logger.getLogger("se.scis.kompics.Storage");

    @Override
    public long getLength() {
        return length;
    }

    @Override
    public byte[] getHttpPseudoStreamingHeader(int seekMs) {
        if (!metainfo.isMp4()) {
            // flv file
            return FlvHandler.FLVX_HEADER;
        } else {
//            return Mp4Handler.MP4_HEADER;
            return null;
        }
    }

    protected String getName() {
        return name;
    }
}
