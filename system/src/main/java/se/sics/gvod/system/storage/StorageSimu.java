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
import java.util.StringTokenizer;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;
import java.util.Iterator;
import java.io.IOException;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map;
import javax.swing.JProgressBar;

/**
 *
 * @author gautier
 */
public class StorageSimu implements Storage {

    private final String videoName;
    private final long videoLength;
    
    public StorageSimu(String videoName, long videoLength) {
        this.videoName = videoName;
        this.videoLength = videoLength;
    }
    
    @Override
    public int needed() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public BitField getBitField() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public MetaInfo getMetaInfo() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean complete() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void check(boolean fileExisted) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String percent() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Map<Integer, byte[]> getSubpieces(int piece) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public byte[] getSubpiece(int subpiece) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean putSubpiece(int piece, byte[] bs) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<Integer> missingSubpieces(int piece) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void create(JProgressBar progressBar) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public byte[] getPiece(int piece) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public byte[] getUncheckedPiece(int piece) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public long getLength() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean checkPiece(int piece) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removeSubpiece(int subpiece) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public byte[] getHttpPseudoStreamingHeader(int seekMs) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void writePieceHashesToFile() throws FileNotFoundException, IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

//    private MetaInfoSimu metainfo;
//    private int needed;
//    private String name;
//    private long length;
//    int subpiece_size;
//    int nbSubpieces;
//    private final BitField bitfield;
//    private static int SUBPIECE_SIZE = 1024;
//
//    /* create storage from infos about the path
//     */
//    public StorageSimu(MetaInfoSimu metainfo) throws IOException {
//        this.metainfo = metainfo;
//        needed = metainfo.getPieces();
//        nbSubpieces = metainfo.getPieces();
//        bitfield = new BitField(needed);
//    }
//
//    /* create storage from the path itself
//     */
//    public StorageSimu(String name, long length) throws IOException {
//        // Create names, rafs and lengths arrays.
//        getFile(name, length);
//
//        subpiece_size = SUBPIECE_SIZE;
//        nbSubpieces = (int) ((this.length - 1) / subpiece_size) + 1;
//        int piece_hashesLength = 20 * nbSubpieces;
//        bitfield = new BitField(nbSubpieces);
//        needed = 0;
//
//        List<String> file = new ArrayList<String>();
//        StringTokenizer st = new StringTokenizer(name, File.separator);
//        while (st.hasMoreTokens()) {
//            String part = st.nextToken();
//            file.add(part);
//
//            // Note that the piece_hashes are not correctly setup yet.
//            metainfo = new MetaInfoSimu(name, file,
//                    subpiece_size, /*piece_hashes*/ piece_hashesLength, length);
//        }
//    }
//
//    public StorageSimu(File baseFile) throws IOException {
//        // Create names, rafs and lengths arrays.
//        getFile(baseFile);
//
//        subpiece_size = SUBPIECE_SIZE;
//        nbSubpieces = (int) ((length - 1) / subpiece_size) + 1;
//        int piece_hashesLength = 20 * nbSubpieces;
//        bitfield = new BitField(nbSubpieces);
//        needed = 0;
//
//        List<String> file = new ArrayList<String>();
//        StringTokenizer st = new StringTokenizer(name, File.separator);
//        while (st.hasMoreTokens()) {
//            String part = st.nextToken();
//            file.add(part);
//
//            // Note that the piece_hashes are not correctly setup yet.
//            metainfo = new MetaInfoSimu(baseFile.getName(), file,
//                    subpiece_size, /*piece_hashes*/ piece_hashesLength, length);
//        }
//    }
//
//    private void getFile(String name, long length) throws IOException {
//
//        this.name = name;
//        this.length = length;
//
//    }
//
//    private void getFile(File file) throws IOException {
//
//        name = file.getPath();
//        this.length = file.length();
//    }
//
//    @Override
//    public void create(JProgressBar progressBar) throws IOException {
//        for (int i = 0; i < nbSubpieces; i++) {
//            bitfield.set(i, true);
//        }
//
//        // Reannounce to force recalculating the info_hash.
//        metainfo = metainfo.reannounce();
//    }
//
//    /**
//     * Returns the MetaInfo associated with this Storage.
//     */
//    @Override
//    public MetaInfoSimu getMetaInfo() {
//        return metainfo;
//    }
//
//    /**
//     * How many pieces are still missing from this storage.
//     */
//    @Override
//    public int needed() {
//        return needed;
//    }
//
//    /**
//     * Whether or not this storage contains all pieces if the MetaInfo.
//     */
//    @Override
//    public boolean complete() {
//        return needed == 0;
//    }
//
//    /**
//     * The BitField that tells which pieces this storage contains. Do not change
//     * this since this is the current state of the storage.
//     */
//    @Override
//    public BitField getBitField() {
//        return bitfield;
//    }
//
//    /**
//     * Creates (and/or checks) the path from metainfo.
//     */
//    @Override
//    public void check(boolean fileExisted) throws IOException {
//        //File base = new File(filterName(metainfo.getPath().get(0)));
//
////        List path = metainfo.getPath();
////        path.remove(0);
////        if (path == null) {
////
////            length = metainfo.getLength();
////            name = metainfo.getName();
////        } else {
////            length = metainfo.getLength();
////            name = metainfo.getName();
////        }
////        checkCreateFiles();
//    }
//
//    /**
//     * Removes 'suspicious' characters from the give path name.
//     */
//    private String filterName(String name) {
//        // XXX - Is this enough?
//        return name.replace(File.separatorChar, '_');
//    }
//
//    private File createFileFromNames(File base, List names) throws IOException {
//        File f = null;
//        Iterator it = names.iterator();
//        while (it.hasNext()) {
//            String fileName = filterName((String) it.next());
//            if (it.hasNext()) {
//                // Another dir in the hierarchy.
//                f = new File(base, fileName);
//                if (!f.mkdir() && !f.isDirectory()) {
//                    throw new IOException("Could not create directory " + f);
//                }
//                base = f;
//            } else {
//                // The final element (path) in the hierarchy.
//                f = new File(base, fileName);
//                if (!f.createNewFile() && !f.exists()) {
//                    throw new IOException("Could not create file " + f);
//                }
//            }
//        }
//        return f;
//    }
//
//    private void checkCreateFiles() throws IOException {
//        // Whether we are resuming or not,
//        // if any of the files already exists we assume we are resuming.
//        boolean resume = false;
//
//    }
//
//    /**
//     * Put the subPiece in the Storage if it is correct.
//     *
//     * @return true if the subPiece was correct (sha metainfo hash matches),
//     *         otherwise false.
//     * @exception IOException
//     *                when some storage related error occurs.
//     */
//    @Override
//    public boolean putSubpiece(int subPiece, byte[] bs) throws IOException {
//        synchronized (bitfield) {
//            if (bitfield.get(subPiece)) {
//                return true; // No need to store twice.
//            } else {
//                bitfield.set(subPiece, false);
//                needed--;
//            }
//        }
//        return true;
//    }
//
//    @Override
//    public List<Integer> missingSubpieces(int piece) {
//        List<Integer> result = new ArrayList<Integer>();
//        int subpiece;
//        if (bitfield.getPiece(piece)) {
//            System.out.println("bug");
//        }
//        for (int i = 0; i < 16; i++) {
//            subpiece = piece * 16 + i;
//            if (!bitfield.get(subpiece)) {
//                result.add(subpiece);
//            }
//            if (subpiece == metainfo.getPieces() - 1) {
//                return result;
//            }
//        }
//        return result;
//    }
//
//    /**
//     * Returns a byte array containing the requested subPiece or null if the
//     * storage doesn't contain the subPiece yet.
//     */
//    @Override
//    public Map<Integer, byte[]> getSubpieces(int piece) throws IOException {
//        if (!bitfield.getPiece(piece)) {
//            return null;
//        }
//
//        Map<Integer, byte[]> result = new HashMap<Integer, byte[]>();
//        int subpiece;
//        for (int i = 0; i < 16; i++) {
//            subpiece = piece * 16 + i;
//            byte[] bs = new byte[metainfo.getSubpieceSize(subpiece)];
//            result.put(subpiece, bs);
//            if (subpiece == metainfo.getPieces() - 1) {
//                return result;
//            }
//        }
//        return result;
//    }
//
//    @Override
//    public byte[] getSubpiece(int subpiece) throws IOException {
//        if (!bitfield.get(subpiece)) {
//            return null;
//        }
//        byte[] result = new byte[metainfo.getSubpieceSize(subpiece)];
//        return result;
//    }
//
//    @Override
//    public String percent() {
//        StringBuilder sb = new StringBuilder();
//        float p = ((float) (nbSubpieces - needed) / nbSubpieces) * 100;
//        sb.append(p).append("% ").append(bitfield.getChunkHumanReadable());
////        sb.append("\n" + bitfield.getPiecesHummanRedable(utility, marge));
//        return sb.toString();
//    }
////    public void writefile() throws IOException {
////        mbb.force();
////    }
//    /** The Java logger used to process our log events. */
//    protected static final Logger log = Logger.getLogger("se.scis.kompics.Storage");
//
//    @Override
//    public byte[] getPiece(int piece) throws IOException {
//        return null;
//    }
//
//    @Override
//    public byte[] getUncheckedPiece(int piece) throws IOException {
//        return null;
//    }
//
//    @Override
//    public long getLength() {
//        return length;
//    }
//
//    @Override
//    public boolean checkPiece(int piece) throws IOException {
//        return true;
//    }
//
//    @Override
//    public void removeSubpiece(int subpiece) throws IOException {
//    }
//
//    @Override
//    public byte[] getHttpPseudoStreamingHeader(int seekMs) {
//        return null;
//    }
//
//    @Override
//    public void writePieceHashesToFile() throws FileNotFoundException, IOException {
//        ; // do nothing
//    }
}
