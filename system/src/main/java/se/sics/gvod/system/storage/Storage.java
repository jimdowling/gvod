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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.swing.JProgressBar;
import se.sics.gvod.common.BitField;

/**
 *
 */
public interface Storage {

    public int needed();

    public BitField getBitField();

    /**
     * 
     * @return MetaInfo object representing this file
     */
    public MetaInfo getMetaInfo();

    public boolean complete();

    /**
     * 
     * @param fileExisted true if the file already exists, false if creating a new file
     * @throws IOException 
     */
    public void check(boolean fileExisted) throws IOException;

    public String percent();

    public Map<Integer, byte[]> getSubpieces(int piece) throws IOException;

    public byte[] getSubpiece(int subpiece) throws IOException;

    public boolean putSubpiece(int piece, byte[] bs) throws IOException;

    public List<Integer> missingSubpieces(int piece);

    public void create(JProgressBar progressBar) throws IOException;

    public byte[] getPiece(int piece) throws IOException;

    public byte[] getUncheckedPiece(int piece) throws IOException;

    public long getLength();

    public boolean checkPiece(int piece) throws IOException;

    public void removeSubpiece(int subpiece) throws IOException;

    public byte[] getHttpPseudoStreamingHeader(int seekMs);

    public void writePieceHashesToFile() throws FileNotFoundException, IOException;
}
