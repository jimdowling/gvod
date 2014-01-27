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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Random;

import se.sics.gvod.common.VodDescriptor;
/**
 *
 * @author gautier
 */
public class PieceStats {

    private final int piece;
    private int numNodesWithPiece;
    /**
     * The list of peers that have the piece
     */
    private ArrayList<VodDescriptor> peers;
    private static Random random;

    public PieceStats(int piece, int seed) {
        this.piece = piece;
        peers = new ArrayList<VodDescriptor>();
        random = new Random(seed);
    }

    public int getNumNodesWithPiece() {
        return numNodesWithPiece;
    }

    public int getPiece() {
        return piece;
    }

    public ArrayList<VodDescriptor> getPeers() {
        return peers;
    }

    public void add(VodDescriptor peer) {
        if (!peers.contains(peer)) {
            peers.add(peer);
            numNodesWithPiece++;
        }
    }
    // TODO Jim: Does this work? Will give a different order every time it runs.
    // Does it generate a random order?
    
    public static Comparator<PieceStats> statsOrder = new Comparator<PieceStats>() {

        @Override
        public int compare(PieceStats o1, PieceStats o2) {

            if (o1.getNumNodesWithPiece() == o2.getNumNodesWithPiece()) {
                //to introduce litle randomization in the order
                if (random.nextBoolean()) {
                    return -1;
                } else {
                    return 1;
                }
            } else if (o1.getNumNodesWithPiece() < o2.getNumNodesWithPiece()) {
                return -1;
            } else {
                return 1;
            }
        }
    };

    public void removeNode(VodDescriptor node) {
        peers.remove(node);
    }
}
