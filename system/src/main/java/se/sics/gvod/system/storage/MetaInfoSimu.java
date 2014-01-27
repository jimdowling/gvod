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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import se.sics.gvod.common.BitField;
import se.sics.gvod.common.util.bencode.BEncoder;
import se.sics.gvod.common.util.bencode.BDecoder;
import se.sics.gvod.common.util.bencode.InvalidBEncodingException;
import se.sics.gvod.common.util.bencode.BEValue;

public class MetaInfoSimu implements MetaInfo {

    private final String name;
    private List<String> path;
    private final Long length;
    private final int piece_size;
    private final int piece_hashesLength;
    private byte[] data;

    public MetaInfoSimu(String name, List<String> file,
            int piece_size, int piece_hashesLength, long length) {
        this.name = name;
        this.path = file;
        this.piece_size = piece_size;
        this.piece_hashesLength = piece_hashesLength;
        this.length = length;
    }

    @Override
    public int getWidth() {
        return 800;
    }

    @Override
    public int getHeight() {
        return 600;
    }
    
    /**
     * Creates a new MetaInfo from the given InputStream. The InputStream must
     * start with a correctly bencoded dictonary describing the torrent.
     */
    public MetaInfoSimu(InputStream in) throws IOException {
        this(new BDecoder(in));
    }

    /**
     * Creates a new MetaInfo from the given BDecoder. The BDecoder must have a
     * complete dictionary describing the torrent.
     */
    public MetaInfoSimu(BDecoder be) throws IOException {
        // Note that evaluation order matters here...
        this(be.bdecodeMap().getMap());
    }

    /**
     * Creates a new MetaInfo from a Map of BEValues and the SHA1 over the
     * original bencoded info dictonary (this is a hack, we could reconstruct
     * the bencoded stream and recalculate the hash). Will throw a
     * InvalidBEncodingException if the given map does not contain a valid
     * announce string or info dictonary.
     */
    public MetaInfoSimu(Map m) throws InvalidBEncodingException {
        BEValue val = (BEValue) m.get("info");
        if (val == null) {
            throw new InvalidBEncodingException("Missing info map");
        }
        Map info = val.getMap();

        val = (BEValue) info.get("name");
        if (val == null) {
            throw new InvalidBEncodingException("Missing name string");
        }
        name = val.getString();

        val = (BEValue) info.get("piece length");
        if (val == null) {
            throw new InvalidBEncodingException("Missing piece length number");
        }
        piece_size = val.getInt();

        val = (BEValue) info.get("piece_hashesLength");
        if (val == null) {
            throw new InvalidBEncodingException("Missing piece bytes");
        }
        piece_hashesLength = val.getInt();

        val = (BEValue) info.get("length");
        if (val != null) {
            length = val.getLong();
            path = null;
        } else {
            val = (BEValue) info.get("file");
            Map<String, Object> file = val.getMap();

            val = (BEValue) file.get("length");
            if (val == null) {
                throw new InvalidBEncodingException("Missing length");
            }
            length = val.getLong();
            val = (BEValue) file.get("path");
            if (val == null) {
                throw new InvalidBEncodingException("Missing path list");
            }

            List<BEValue> path_list = val.getList();
            int path_length = path_list.size();
            if (path_length == 0) {
                throw new InvalidBEncodingException(
                        "zero size file path list");
            }

            this.path = new ArrayList<String>(path_length);
            for (BEValue value : path_list) {
                this.path.add(value.getString());
            }
        }
    }

    private Map<String, Object> createInfoMap() {
        Map<String, Object> info = new HashMap<String, Object>();
        info.put("name", name);
        info.put("width", 800);
        info.put("height", 600);

        info.put("piece length", piece_size);
        info.put("piece_hashesLength", piece_hashesLength);
        if (path == null) {
            info.put("length", new Long(length));
        } else {
            Map<String, Object> file = new HashMap<String, Object>();
            file.put("path", this.path);
            file.put("length", length);
            info.put("file", file);
        }
        return info;
    }

    public int getPieces() {
        return piece_hashesLength / 20;
    }

    public int getpieceSize(int piece) {
        int lastSubpiece = piece * BitField.NUM_SUBPIECES_PER_PIECE + BitField.NUM_SUBPIECES_PER_PIECE;
        int pieces = getPieces();
        if (lastSubpiece >= 0 && lastSubpiece < pieces - 1) {
            return piece_size * BitField.NUM_SUBPIECES_PER_PIECE;
        } else if (lastSubpiece >= pieces - 1) {
            return (int) (length - piece * BitField.NUM_SUBPIECES_PER_PIECE * piece_size);
        } else {
            throw new IndexOutOfBoundsException("no piece: " + piece);
        }
    }

    public int getSubpieceSize(int piece) {
        int pieces = getPieces();
        if (piece >= 0 && piece < pieces - 1) {
            return piece_size;
        } else if (piece == pieces - 1) {
            return (int) (length - piece * piece_size);
        } else {
            throw new IndexOutOfBoundsException("no piece: " + piece);
        }
    }

    public MetaInfoSimu reannounce() {
        return new MetaInfoSimu(name, path, piece_size,
                piece_hashesLength, length);
    }

    public List<String> getPath() {
        return path;
    }

    public Long getLength() {
        return length;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setPath(List<String> newPath) {
        path = newPath;
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
        if (data == null) {
            Map<String, Object> m = new HashMap<String, Object>();
            Map info = createInfoMap();
            m.put("info", info);
            data = BEncoder.bencode(m);
        }
        return data;
    }

    @Override
    public int getPieceNbSubPieces(int piece) {
        int lastSubpiece = piece * BitField.NUM_SUBPIECES_PER_PIECE + BitField.NUM_SUBPIECES_PER_PIECE-1;
        int nbSubpieces = getnbSubpieces();
        if (lastSubpiece >= 0 && lastSubpiece < nbSubpieces - 1) {
            return BitField.NUM_SUBPIECES_PER_PIECE;
        } else if (lastSubpiece >= nbSubpieces - 1) {
            return (int) (nbSubpieces - piece * BitField.NUM_SUBPIECES_PER_PIECE);
        } else {
            throw new IndexOutOfBoundsException("no piece: " + piece);
        }
    }

    public int getnbSubpieces() {
        return piece_hashesLength / 20;
    }

    @Override
    public boolean haveHashes(int chunk) {
        return true;
    }

    @Override
    public byte[] getChunkHashes(int chunk) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean setPieceHashes(byte[] pieceHashes, int chunk) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getNbChunks() {
        return (int) ((length / BitField.NUM_SUBPIECES_PER_PIECE) / BitField.NUM_PIECES_PER_CHUNK);
//        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isMp4() {
        return false;
    }

}
