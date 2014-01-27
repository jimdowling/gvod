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

import se.sics.gvod.system.vod.PieceInTransit;
import se.sics.gvod.common.BitField;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.gvod.common.UtilityVod;
import se.sics.gvod.common.VodDescriptor;

/**
 *
 * @author gautier
 */
public class Stats {

    private Logger logger = LoggerFactory.getLogger(Stats.class);
    private List<PieceStats> stats;
    private Map<Integer, PieceStats> statsMap;
    protected Random random;
    private int seed;
    private Map<VodDescriptor, Integer> interestingNode = new HashMap<VodDescriptor, Integer>();

    public Stats(UtilityVod utility, int seed) {
        stats = new ArrayList<PieceStats>();
        this.seed = seed;
        random = new Random(seed);
        statsMap = new HashMap<Integer, PieceStats>();

        for (int i = utility.getChunk(); i < utility.getChunk() + utility.getOffset(); i++) {
            for (int j = i * BitField.NUM_PIECES_PER_CHUNK;
            j < i * BitField.NUM_PIECES_PER_CHUNK + BitField.NUM_PIECES_PER_CHUNK;
            j++) {
                initiateStats(j);
            }
        }
    }

    public int getUpperPiece(Map<Integer, PieceInTransit> outstandingPieces, int readingPos, int readingWindow) {
        List<PieceStats> piecesList;
        piecesList = stats;
        if (piecesList.isEmpty()) {
//            System.out.println("BUUUUUUUUUUUUUG stats");
            return -1;
        }
        Collections.sort(piecesList, PieceStats.statsOrder);
        int i = 0;
        int temp;
        List<PieceStats> list = new ArrayList<PieceStats>();
        List<PieceStats> priorityList = new ArrayList<PieceStats>();
        do {
            temp = piecesList.get(i).getNumNodesWithPiece();
            if (temp == 0 && !outstandingPieces.containsKey(piecesList.get(i).getPiece())) {
                if (piecesList.get(i).getPiece() < readingPos + readingWindow) {
                    priorityList.add(piecesList.get(i));
                } else {
                    list.add(piecesList.get(i));
                }

            } else if (temp > 0) {
                break;
            }
            i++;
        } while (i < piecesList.size() && temp == 0);
        if (priorityList.size() > 0) {
            return priorityList.get(random.nextInt(priorityList.size())).getPiece();
        }
        if (list.size() > 0) {
            return list.get(random.nextInt(list.size())).getPiece();
        } else {
            return -1;
        }
    }

    public int pieceToDownload(VodDescriptor node,
            Map<Integer, PieceInTransit> outstandingPieces,
            int readingPos, int readingWindow) {
        List<PieceStats> piecesList;
        List<PieceStats> priorityList = new ArrayList<PieceStats>();
        piecesList = stats;
        for (Integer i : statsMap.keySet()) {
            if (i <= readingPos + readingWindow
                    && statsMap.get(i).getPeers().contains(node)
                    && !outstandingPieces.containsKey(statsMap.get(i).getPiece())) {
                priorityList.add(statsMap.get(i));
            }
        }
        if (piecesList.isEmpty() || !interestingNode.containsKey(node)) {
            if (piecesList.isEmpty()) {
                logger.warn("Pieces list is empty");
            } else {
                logger.warn("Node not interesting: " + node.getVodAddress().getId());
            }
            return -1;
        }


        if (priorityList.size() > 0) {
            return priorityList.get(random.nextInt(priorityList.size())).getPiece();
        }
        Collections.sort(piecesList, PieceStats.statsOrder);
        int i = 0;
        int temp;
        do {
            temp = piecesList.get(i).getNumNodesWithPiece();
            i++;
        } while (i < piecesList.size() && temp == 0);
        i--;
        if (i >= piecesList.size()) {
            return -1;
        }
        do {
            if (!outstandingPieces.containsKey(piecesList.get(i).getPiece())
                    && piecesList.get(i).getPeers().contains(node)) {
                return piecesList.get(i).getPiece();
            }
            i++;
        } while (i < piecesList.size());

        logger.warn("Stats didn't find an interesting piece.");
        return -1;
    }

    public int pieceToDownloadFromUpper(VodDescriptor node,
            Map<Integer, PieceInTransit> outstandingPieces, int readingPos, int readingWindow) {
        List<PieceStats> piecesList;
        List<PieceStats> priorityList = new ArrayList<PieceStats>();
        piecesList = stats;
        for (Integer i : statsMap.keySet()) {
            if (i <= readingPos + readingWindow
                    && statsMap.get(i).getPeers().contains(node)
                    && !outstandingPieces.containsKey(statsMap.get(i).getPiece())) {
                priorityList.add(statsMap.get(i));
            }
        }
        if (piecesList.isEmpty()) {
            return -1;
        }
        if (priorityList.size() > 0) {
            return priorityList.get(random.nextInt(priorityList.size())).getPiece();
        }
        Collections.sort(piecesList, PieceStats.statsOrder);
        int i = 0;
        do {
            if (!outstandingPieces.containsKey(piecesList.get(i).getPiece())
                    && piecesList.get(i).getPeers().contains(node)) {
                return piecesList.get(i).getPiece();
            }
            i++;
        } while (i < piecesList.size());
        return -1;
    }

    public List<Integer> getEligible(List<Integer> outstandingPieces, VodDescriptor node) {
        List<Integer> result = new ArrayList<Integer>();
        for (Integer piece : outstandingPieces) {
            if ((statsMap.containsKey(piece) && statsMap.get(piece).getPeers().contains(node)) /*|| (idxMap.containsKey(piece) && idxMap.get(piece).getPeers().contains(node))*/) {
                result.add(piece);
            }
        }
        return result;
    }

    private void addStats(int k, VodDescriptor peer) {
        if (statsMap.containsKey(k)) {
            statsMap.get(k).add(peer);
            if (interestingNode.containsKey(peer)) {
                interestingNode.put(peer, interestingNode.get(peer) + 1);
            } else {
                interestingNode.put(peer, 1);
            }
            return;
        }
    }

    private void initiateStats(int k) {
        PieceStats pieceStats = new PieceStats(k, seed);
        statsMap.put(k, pieceStats);
        stats.add(pieceStats);
    }

    public void changeUtility(int oldUtility, UtilityVod newUtility, int max, BitField bitField) {
        if (newUtility.isSeeder()) {
            return;
        }
        if (newUtility.getChunk() - oldUtility >= newUtility.getOffset()
                || newUtility.getChunk() < oldUtility) {
            stats = new ArrayList<PieceStats>();
            statsMap = new HashMap<Integer, PieceStats>();
            int lim = newUtility.getChunk() + newUtility.getOffset();
            if (lim > bitField.getChunkFieldSize()) {
                lim = bitField.getChunkFieldSize();
            }
            for (int i = newUtility.getChunk();
                    i < lim; i++) {
                if (!bitField.getChunk(i)) {
                    for (int j = i * BitField.NUM_PIECES_PER_CHUNK;
                        j < i * BitField.NUM_PIECES_PER_CHUNK + BitField.NUM_PIECES_PER_CHUNK;
                        j++) {
                        if (j >= max) {
                            break;
                        }
                        if (!bitField.getPiece(j)) {
                            initiateStats(j);
                        }
                    }
                }
            }
        } else {
            for (int i = oldUtility; i < newUtility.getChunk(); i++) {
                for (int j = i * BitField.NUM_PIECES_PER_CHUNK;
                j < i * BitField.NUM_PIECES_PER_CHUNK+ BitField.NUM_PIECES_PER_CHUNK;
                j++) {
                    PieceStats pieceStats = statsMap.get(j);
                    stats.remove(pieceStats);
                    statsMap.remove(j);
                }
            }
            int lim = newUtility.getChunk() + newUtility.getOffset();
            if (lim > bitField.getChunkFieldSize()) {
                lim = bitField.getChunkFieldSize();
            }
            for (int i = oldUtility + newUtility.getOffset();
                    i < lim; i++) {
                if (!bitField.getChunk(i)) {
                    for (int j = i * BitField.NUM_PIECES_PER_CHUNK;
                        j < i * BitField.NUM_PIECES_PER_CHUNK + BitField.NUM_PIECES_PER_CHUNK;
                        j++) {
                        if (j >= max) {
                            break;
                        }
                        if (!bitField.getPiece(j)) {
                            initiateStats(j);
                        }
                    }
                }
            }
        }

    }

    public boolean removePieceFromStats(int piece) {
        if (statsMap.containsKey(piece)) {
            for (VodDescriptor node : statsMap.get(piece).getPeers()) {
                interestingNode.put(node, interestingNode.get(node) - 1);
                if (interestingNode.get(node) == 0) {
                    interestingNode.remove(node);
                }
            }
            boolean result = stats.remove(statsMap.get(piece));
            statsMap.remove(piece);
            return result;
        } else {
            return false;
        }
    }

    public void updateStats(VodDescriptor peer, byte[][] availablePieces,
            byte[] availableChunk, UtilityVod peerUtility, UtilityVod utility) {
        if (utility.isSeeder()) {
            return;
        }
        if (peerUtility.isSeeder()) {
            for (int j = 0; j < utility.getChunk() + utility.getOffset(); j++) {
                for (int k = (utility.getChunk() + j) * BitField.NUM_PIECES_PER_CHUNK;
                k < (utility.getChunk() + j) * BitField.NUM_PIECES_PER_CHUNK +
                        BitField.NUM_PIECES_PER_CHUNK;
                k++) {
                    addStats(k, peer);
                }
            }
            return;
        }
        if (availablePieces == null) {
            return;
        }
        int i = 0;
        if (peerUtility.getChunk() >= utility.getChunk()) {
            for (int j = 0; j < peerUtility.getChunk() - utility.getChunk(); j++) {
                int mask = BitField.NUM_PIECES_PER_CHUNK >> (j % 8);
                if ((availableChunk[j / 8] & mask) != 0) {
                    for (int k = (utility.getChunk() + j) * BitField.NUM_PIECES_PER_CHUNK;
                            k < (utility.getChunk() + j) * BitField.NUM_PIECES_PER_CHUNK + BitField.NUM_PIECES_PER_CHUNK; k++) {
                        addStats(k, peer);
                    }
                }
                i++;
            }
            for (int j = 0; j < utility.getOffset() - i; j++) {
                for (int k = 0; k < BitField.NUM_PIECES_PER_CHUNK; k++) {
                    int mask = BitField.NUM_PIECES_PER_CHUNK >> (k % 8);
                    if ((availablePieces[j][k / 8] & mask) != 0) {
                        addStats((peerUtility.getChunk() + j) * BitField.NUM_PIECES_PER_CHUNK + k, peer);
                    }
                }
            }
        } else {
            for (int j = 0; j < utility.getChunk() - peerUtility.getChunk(); j++) {
                i++;
            }
            for (int j = i; j < utility.getOffset(); j++) {
                for (int k = 0; k < BitField.NUM_PIECES_PER_CHUNK; k++) {
                    int mask = BitField.NUM_PIECES_PER_CHUNK >> (k % 8);
                    if ((availablePieces[j][k / 8] & mask) != 0) {
                        addStats((peerUtility.getChunk() + j) * BitField.NUM_PIECES_PER_CHUNK + k, peer);
                    }
                }
            }
        }
    }

    public VodDescriptor getRandomNodeWithPiece(int piece,
            Collection<VodDescriptor> usedNodes) {
        List<VodDescriptor> list = new ArrayList<VodDescriptor>();
        if (statsMap.get(piece) == null) {
            return null;
        }
        list.addAll(statsMap.get(piece).getPeers());
        if (usedNodes != null) {
            list.removeAll(usedNodes);
        }
        if (list.isEmpty()) {
            return null;
        } else {
            return list.get(random.nextInt(list.size()));
        }
    }

    public void removeNode(VodDescriptor node) {
        for (PieceStats st : stats) {
            st.removeNode(node);
        }
        interestingNode.remove(node);
    }
}
