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
package se.sics.gvod.system.vod;

import java.util.BitSet;
import java.util.List;
import se.sics.gvod.common.BitField;
import se.sics.gvod.net.VodAddress;

/**
 * The <code>PieceInTransit</code> class.
 *
 * @author Cosmin Arad <cosmin@sics.se>
 * @version $Id: PieceInTransit.java 1789 2010-03-05 13:37:21Z jdowling $
 */
public final class PieceInTransit {

    int pieceIndex;
    BitSet receivedSubpieces;
    BitSet requestedSubpieces;
    int totalSubpieces;
    long lastSubpieceReceivedAt, lastSubpieceRequestedAt;
    VodAddress fromPeer;

    public PieceInTransit(int piece, int totalBlocks, VodAddress peer) {
        this.pieceIndex = piece;
        this.totalSubpieces = totalBlocks;
        this.requestedSubpieces = new BitSet(totalBlocks);
        this.receivedSubpieces = new BitSet(totalBlocks);
        this.fromPeer = peer;
        lastSubpieceReceivedAt = System.currentTimeMillis();
        lastSubpieceRequestedAt = lastSubpieceReceivedAt;
    }

    public PieceInTransit(int piece, int totalSubpieces, VodAddress peer,
            List<Integer> missing) {
        this.pieceIndex = piece;
        this.totalSubpieces = totalSubpieces;
        this.requestedSubpieces = new BitSet(totalSubpieces);
        this.receivedSubpieces = new BitSet(totalSubpieces);
        for (int i = 0; i<BitField.NUM_SUBPIECES_PER_PIECE; i++){
            if(!missing.contains(i+BitField.NUM_SUBPIECES_PER_PIECE*pieceIndex)){
                receivedSubpieces.set(i);
                requestedSubpieces.set(i);
            }
        }
        this.fromPeer = peer;
        lastSubpieceReceivedAt = System.currentTimeMillis();
        lastSubpieceRequestedAt = lastSubpieceReceivedAt;
    }

    /**
     * @param subpieceIndex
     * @return <code>true</code> if all blocks have been received and
     *         <code>false</code> otherwise.
     */
    public boolean subpieceReceived(int subpieceIndex) {
        lastSubpieceReceivedAt = System.currentTimeMillis();
        receivedSubpieces.set(subpieceIndex-BitField.NUM_SUBPIECES_PER_PIECE*pieceIndex);
        return receivedSubpieces.cardinality() == totalSubpieces;
    }

    public void subpieceTimedOut(int subpieceIndex) {
        requestedSubpieces.set(subpieceIndex-BitField.NUM_SUBPIECES_PER_PIECE*pieceIndex);
    }

    public int getNextSubpieceToRequest() {
        lastSubpieceRequestedAt = System.currentTimeMillis();
        int nextBlock = requestedSubpieces.nextClearBit(0);
        if (nextBlock < totalSubpieces) {
            requestedSubpieces.set(nextBlock);
        } else {
            return -1;
        }
        return pieceIndex*BitField.NUM_SUBPIECES_PER_PIECE+nextBlock;
    }

    public boolean isStalePiece(long threshold) {
        long now = System.currentTimeMillis();
        return now - lastSubpieceRequestedAt > threshold;
    }

    public VodAddress getFromPeer() {
        return fromPeer;
    }

    public void resetRequestedBlocks() {
        requestedSubpieces = new BitSet(totalSubpieces);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < totalSubpieces; i++) {
            sb.append(requestedSubpieces.get(i) ? 1 : 0);
        }
        System.err.println("RST " + sb.toString());
        // requestedBlocks = (BitSet) receivedBlocks.clone();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + pieceIndex;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        PieceInTransit other = (PieceInTransit) obj;
        if (pieceIndex != other.pieceIndex) {
            return false;
        }
        return true;
    }

    public int getPieceIndex() {
        return pieceIndex;
    }
}