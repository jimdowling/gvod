/**
 * This file is part of the Kompics P2P Framework.
 *
 * Copyright (C) 2009 Swedish Institute of Computer Science (SICS) Copyright (C)
 * 2009 Royal Institute of Technology (KTH)
 *
 * Kompics is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package se.sics.gvod.system.vod;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.gvod.common.BitField;
import se.sics.gvod.common.Self;
import se.sics.gvod.common.UtilityVod;
import se.sics.gvod.common.VodDescriptor;
import se.sics.gvod.common.msgs.DataMsg;
import se.sics.gvod.config.VodConfig;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.system.peer.sets.DownloadStats;
import se.sics.gvod.system.storage.Storage;
import se.sics.gvod.system.storage.StorageMemMapWholeFile;
import se.sics.gvod.system.util.ActiveTorrents;
import se.sics.gvod.system.vod.Vod.DownloadDelegator;
import static se.sics.gvod.system.vod.Vod.durationToString;
import se.sics.gvod.timer.TimeoutId;
import se.sics.gvod.web.port.DownloadCompletedSim;

/**
 * @author Alex Ormenisan <aaor@sics.se>
 */
public class DownloadManager {

    private final Logger logger = LoggerFactory.getLogger(Vod.class);
    private final Random randGen;
    private final String compName;
    private final DownloadDelegator delegator;
    private final Self self;
    private final boolean simulation;

    private final ConnectionManager connectionMngr;
    private final HistoryManager historyMngr;
    private final PlayerManager playerMngr;

    private final long dataRequestTimeout;
    private final int infUtilFrec;

    private int numRoundsNoPiecesDownloaded = 0;
    private int totalNumPiecesDownloaded = 0;
    private float piecesFromUtilitySet = 0, piecesFromUpperSet = 0;
    private int count;

    //TODO Alex turn private
    public final Storage storage;
    public final Map<Integer, PieceInTransit> partialPieces;
    public AtomicInteger pieceToRead;
    public DownloadStats downloadStats = new DownloadStats();
    public String videoName;
    public long readingPeriod;
    public int overhead;
    public boolean seeder;
    public boolean freeRider;
    public boolean jumped;
    public final String torrentFileAddress;
    public Map<Integer, List<VodAddress>> hashRequests;

    public final int pipeSize;

    public DownloadManager(VodInit init, Self self, DownloadDelegator delegator, Storage storage,
            ConnectionManager connectionMngr, HistoryManager historyMngr, PlayerManager playerMngr, String compName) {
        this.self = self;
        this.delegator = delegator;
        this.storage = storage;
        this.connectionMngr = connectionMngr;
        this.historyMngr = historyMngr;
        this.playerMngr = playerMngr;
        this.compName = compName;

        this.partialPieces = new HashMap<Integer, PieceInTransit>();
        this.hashRequests = new HashMap<Integer, List<VodAddress>>();
        this.pieceToRead = new AtomicInteger(0);
        this.jumped = false;
        this.count = 0;

        this.simulation = init.isSimulation();
        this.seeder = init.isSeed();
        this.torrentFileAddress = init.getTorrentFileAddress();
        this.pieceToRead.set(init.getUtility() * BitField.NUM_PIECES_PER_CHUNK);
        this.randGen = new Random(init.getConfig().getSeed());

        this.overhead = 20;
        this.infUtilFrec = init.getConfig().getInfUtilFrec();
        this.readingPeriod = init.getConfig().getReadingPeriod();
        this.dataRequestTimeout = init.getConfig().getDataRequestTimeout();
        this.pipeSize = init.getConfig().getPipeSize();
        this.videoName = init.getConfig().getName();
        if (seeder) {
            this.freeRider = false;
        } else {
            this.freeRider = init.isFreeRider();
        }

        logger.debug(compName + "PIPELINE_SIZE = " + pipeSize);
        logger.info("{} freerider: {}", compName, freeRider);
    }

    public void download(int time) {
        if (storage.getBitField().getNextUncompletedPiece()
                >= storage.getBitField().numberPieces() && !seeder) {
            finishedDownloading(time);
        }
        startDownload(time);
    }

    /**
     * start to download pieces from any peer in the neighborhood where we have
     * space in our pipeline for it
     */
    public void startDownload(int time) {
        logger.debug("{} start download", compName);

        // First check if all of the pieces have been downloaded
        if (storage.complete()) {
            logger.info("{} storage finished. Not downloading.", compName);
            return;
        }

        // Check if the last piece has been downloaded (i may have skipped over some of the pieces).
        if (storage.getBitField().getNextUncompletedPiece() >= storage.getBitField().numberPieces()) {
            logger.debug("{} storage finished or first uncompleted piece > lastPiece {}/{}",
                    new Object[]{compName, storage.getBitField().getNextUncompletedPiece(), storage.getBitField().numberPieces()});
            return;
        }
        boolean noDownloading = true;

        List<VodAddress> upperSet = connectionMngr.getUpperSet();
        for (VodAddress add : upperSet) {

            // TODO - Not DRY code - same code repeated in the next while-loop
            VodDescriptor peer = connectionMngr.getDescriptor(add);
            logger.info(compName + "Downloading: Pipeline size {} . Max size {}",
                    peer.getRequestPipeline().size(), peer.getPipeSize());

            peer.cleanupPipeline();
            cleanupPartialPieces();
            if (peer.getRequestPipeline().size() < peer.getPipeSize()) {
                startDownloadingPieceFrom(add,
                        peer.getPipeSize() - peer.getRequestPipeline().size(),
                        null, 0, time);
                noDownloading = false;
            } else {
                peer.cleanupPipeline();
            }
            if (numRoundsNoPiecesDownloaded > 5) {
                peer.clearPipeline();
            }
        }
        List<VodAddress> bitTorrentSet = connectionMngr.getBitTorrentSet();
        while (bitTorrentSet.size() > 0) {
            int i = randGen.nextInt(bitTorrentSet.size());
            VodAddress add = bitTorrentSet.get(i);
            VodDescriptor peer = connectionMngr.getDescriptor(add);
            logger.info(compName + "Pipeline size {} . Max size {}",
                    peer.getRequestPipeline().size(), peer.getPipeSize());

            peer.cleanupPipeline();
            if (peer.getRequestPipeline().size() < peer.getPipeSize()) {
                startDownloadingPieceFrom(add,
                        peer.getPipeSize() - peer.getRequestPipeline().size(),
                        null, 0, time);
                noDownloading = false;
            }
            bitTorrentSet.remove(add);
            if (numRoundsNoPiecesDownloaded > 3) {
                peer.clearPipeline();
            }
        }
        if (noDownloading) {
            logger.warn("{} No downloading. BittorrentSet size {}. UpperSet size {}",
                    new Object[]{compName, bitTorrentSet.size(), upperSet.size()});
            if (numRoundsNoPiecesDownloaded > 3) {
                numRoundsNoPiecesDownloaded = 0;
            } else {
                numRoundsNoPiecesDownloaded++;
            }
        }

    }

    private void cleanupPartialPieces() {
        Collection<PieceInTransit> piecesInTransit = partialPieces.values();
        List<PieceInTransit> stalePieces = new ArrayList<PieceInTransit>();

        for (PieceInTransit p : piecesInTransit) {
            if (p.isStalePiece(VodConfig.DATA_REQUEST_TIMEOUT)) {
                stalePieces.add(p);
            }
        }
        List<Integer> staleKeys = new ArrayList<Integer>();

        for (Map.Entry<Integer, PieceInTransit> entry : partialPieces.entrySet()) {
            for (PieceInTransit p : stalePieces) {
                if (p.equals(entry.getValue())) {
                    staleKeys.add(entry.getKey());
                }
            }
        }

        for (Integer k : staleKeys) {
            partialPieces.remove(k);
            logger.debug("Cleaning up stale partial piece: {}", k);
        }

    }

    /**
     * start downloading a piece from peer
     *
     * @param peer
     * @param numRequestBlocks
     * @param ackId
     * @param rtt
     */
    public void startDownloadingPieceFrom(VodAddress peer,
            int numRequestBlocks, TimeoutId ackId, long rtt, int time) {
        logger.trace(compName + "Starting to download {} blocks from : {}", numRequestBlocks,
                peer.getId());
        if (numRequestBlocks <= 0) {
            if (ackId != null) {
                delegator.ack(new DataMsg.Ack(self.getAddress(), peer, ackId, rtt));
            }
            logger.debug(compName + "NumBlocks < 0. AckId was not null. Not requesting piece");
            return;
        }
        int piece;
        List<Integer> missing = null;
        do {
            piece = selectPiece(peer);
            logger.trace(compName + "piece to download : {}", piece);
            if (piece == -1) {
                // no piece is eligible for download from this peer
                if (connectionMngr.inUpperSet(peer)) {
                    piece = storage.getBitField().getNextUncompletedPiece();
                    while (piece < storage.getBitField().numberPieces()
                            && (partialPieces.containsKey(piece)
                            || storage.getBitField().getPiece(piece))) {
                        piece++;
                    }
                    if (piece >= storage.getBitField().numberPieces()) {
                        if (ackId != null) {
                            delegator.ack(new DataMsg.Ack(self.getAddress(), peer, ackId, rtt));
                        }
                        logger.debug(compName + "Piece > current. AckId was not null. Not requesting piece");
                        return;
                    }
                } else {
                    if (ackId != null) {
                        delegator.ack(new DataMsg.Ack(self.getAddress(), peer, ackId, rtt));
                    }
                    logger.debug(compName + "Utility set. AckId was not null. Not requesting piece");
                    return;
                }
            }
            missing = storage.missingSubpieces(piece);
            if (missing.isEmpty()) {
                pieceCompleted(piece, null, time);
                piece = -1;
            }
        } while (piece == -1);
        int chunk = piece / BitField.NUM_PIECES_PER_CHUNK;
        if (!storage.getMetaInfo().haveHashes(chunk)) {
            if ((!hashRequests.containsKey(chunk)
                    || !hashRequests.get(chunk).contains(peer))) {
                delegator.hashReq(peer, chunk, 0);
                return;
            } else {
                logger.debug(compName + "Hash request outstanding.");
            }
            if (ackId != null) {
                delegator.ack(new DataMsg.Ack(self.getAddress(), peer, ackId, rtt));
            }
        }

        UtilityVod utility = (UtilityVod) self.getUtility();
        VodDescriptor peerInfo = connectionMngr.getDescriptor(peer);
        UtilityVod peerUtility = (UtilityVod) peerInfo.getUtility();
        if (piece > pieceToRead.get() + playerMngr.bufferingWindow
                && (peerUtility.getChunk() > utility.getChunk() || peerUtility.isSeeder())) {
            chunk = utility.getChunk() + 3;
            if (chunk < storage.getMetaInfo().getNbChunks()
                    && !storage.getMetaInfo().haveHashes(chunk)) {
                delegator.hashReq(peer, chunk, 0);
            }
        }
        int blockCount = BitField.NUM_SUBPIECES_PER_PIECE;
        PieceInTransit transit = new PieceInTransit(piece, blockCount, peer,
                missing);
        partialPieces.put(piece, transit);

        if (peerInfo == null) {
            if (ackId != null) {
                delegator.ack(new DataMsg.Ack(self.getAddress(), peer, ackId, rtt));
            }
            logger.debug(compName + "PeerInfo null. AckId was not null. Not requesting piece");
            return;
        }
        int lim = numRequestBlocks;
        if (missing.size() < lim) {
            lim = missing.size();
            logger.debug(compName + "Changed numRequestBlocks to {}", lim);
        }
        for (int i = 0; i < lim; i++) {
            int nextBlock = transit.getNextSubpieceToRequest();

            delegator.downloadReq(peerInfo, ackId, piece, nextBlock, rtt);
        }
    }

    /**
     * select next piece to download from peer
     *
     * @param peer
     * @return
     */
    private int selectPiece(VodAddress peer) {
        VodDescriptor info = connectionMngr.getDescriptor(peer);
        if (info == null) {
            logger.warn("{} No descriptor for {} - when selecting piece", new Object[]{compName, peer.getId()});
            return -1;
        }

        if (connectionMngr.inUpperSet(peer)) {
            int piece = connectionMngr.getBitTorrentStats().getUpperPiece(partialPieces,
                    pieceToRead.get(), playerMngr.bufferingWindow);
            if (piece != -1) {
                return piece;
            }
        }
        // first we try to complete a stale piece before selecting a new piece
        // [strict piece selection policy]
        int stalePiece = selectStalePiece(peer);
        if (stalePiece != -1) {
            logger.warn(compName + "Selecting stale piece {} from peer {}", stalePiece, peer.getId());
            return stalePiece;
        }

        // compute the set of eligible pieces
        if (connectionMngr.inUpperSet(peer)) {
            int piece = connectionMngr.getBitTorrentStats().pieceToDownloadFromUpper(
                    info, partialPieces, pieceToRead.get(), playerMngr.bufferingWindow);
            if (piece == -1) {
                piece = storage.getBitField().getNextUncompletedPiece();
                while (piece < storage.getBitField().numberPieces()
                        && (partialPieces.containsKey(piece)
                        || storage.getBitField().getPiece(piece))) {
                    piece++;
                }
                if (piece < storage.getBitField().numberPieces()) {
                    logger.trace(compName + "Upper set: Piece num {} found", piece);
                    return piece;
                } else {
                    logger.trace(compName + "Upper set: Piece num {} is greater than "
                            + "maxPieceSize {}", piece,
                            storage.getBitField().numberPieces());
                    return -1;
                }
            }
            return piece;
        } else {
            int piece = connectionMngr.getBitTorrentStats().pieceToDownload(
                    info, partialPieces, pieceToRead.get(), playerMngr.bufferingWindow);
            logger.trace(compName + "Piece num {} found from bittorrent set", piece);
            return piece;
        }

    }

    /**
     * select a stale piece to download from peer
     *
     * @param peer
     * @return
     */
    private int selectStalePiece(VodAddress peer) {
        VodDescriptor info = connectionMngr.getDescriptor(peer);
        if (info == null) {
            return -1;
        }
        List<Integer> eligible;
        if (connectionMngr.inUpperSet(peer)) {
            eligible = new ArrayList<Integer>(partialPieces.keySet());
        } else {
            eligible = connectionMngr.getBitTorrentStats().getEligible(new ArrayList<Integer>(partialPieces.keySet()), info);
        }
        // eligible contains the pieces in transit that the peer has
        // we look for stale ones

        for (int piece : eligible) {
            PieceInTransit transit = partialPieces.get(piece);
            if (transit.isStalePiece(dataRequestTimeout)) {
                // discard old stale piece and select it again
                partialPieces.remove(piece);
                for (VodDescriptor inf : connectionMngr.getAllDescriptor()) {
                    inf.discardPiece(piece);
                }
                return piece;
            }
        }
        return -1;
    }

    /**
     * check if a downloaded piece correspond to its hash if yes update the
     * utility if not unmark the subpiece as downloaded
     *
     * @param piece
     * @param peer
     */
    public void pieceCompleted(int piece, VodAddress peer, int time) {
        UtilityVod utility = (UtilityVod) self.getUtility();
        // downloaded a complete piece
        try {
            if (!simulation) {
                if (!storage.checkPiece(piece)) {
                    for (int i = 0; i < storage.getMetaInfo().getPieceNbSubPieces(piece); i++) {
                        storage.removeSubpiece(piece * BitField.NUM_SUBPIECES_PER_PIECE + i);
                        downloadStats.removeDownloaded(peer, 1);
                    }
                    return;
                }
                ActiveTorrents.updatePercentage(videoName, storage.percent());
            }
            storage.getBitField().setPiece(piece);
            logger.debug(compName + "completed piece : {} . Total pieces downloaded {}", piece,
                    totalNumPiecesDownloaded++);
            partialPieces.remove(piece);
            connectionMngr.getBitTorrentStats().removePieceFromStats(piece);

            // TODO: JIM - is this correct? I want the next uncomplete piece - not the first uncompleted piece!!
            utility.setPiece(storage.getBitField().getNextUncompletedPiece());

            if (playerMngr.buffering.get() && utility.getPiece() > pieceToRead.get() + playerMngr.bufferingWindow) {
                restartToRead(time);
            }
            if (storage.getBitField().hasChunk(piece / BitField.NUM_PIECES_PER_CHUNK)) {
                int holdUtility = utility.getChunk();
                utility.setChunkOnly(storage.getBitField().getNextUncompletedChunk());
                if (utility.getPiece() < utility.getChunk() * BitField.NUM_PIECES_PER_CHUNK) {
                    logger.error(compName + "##### bad piece Utility value : ({};{}) "
                            + storage.getBitField().getTotal(),
                            utility.getChunk(), utility.getPiece());
                    logger.error(compName + "##### " + storage.getBitField().getChunkHumanReadable());
                }

//                delegator.doTrigger(new ChangeBootstrapUtility(utility,
//                        "GVod", self), vod);
                connectionMngr.getBitTorrentStats().changeUtility(holdUtility,
                        utility, storage.getBitField().numberPieces(),
                        storage.getBitField());
                connectionMngr.informUtilityChange(seeder);
            }
            if (count >= infUtilFrec) {
                logger.info(compName + storage.percent() + "%");
                connectionMngr.informUtilityChange(seeder);
                count = 0;
            } else {
                count++;
            }
        } catch (Exception e) {
            logger.error(compName + "problem accessing storage");
        }

        self.updateUtility(utility);
    }

    /**
     * tell vodPeer that the downloading is finished, put the stream in the
     * background streams and become a seed
     */
    private void finishedDownloading(int time) {
        UtilityVod utility = (UtilityVod) self.getUtility();
        seeder = true;
        playerMngr.buffering.set(false);
        long duration = System.currentTimeMillis() - playerMngr.startedAtTime;
        logger.info("{} finished buffering after {} ratio : {} ({}; {}) freerider:{}",
                new Object[]{compName, durationToString(duration), piecesFromUpperSet / piecesFromUtilitySet, utility.getChunk(), utility.getPiece(), freeRider});
        logger.info("{} download time: {}, buffering time: {}",
                new Object[]{compName, durationToString(duration), playerMngr.totalBufferTime});

        // write hash pieces to file when finished downloading.
        if (storage instanceof StorageMemMapWholeFile) {
            StorageMemMapWholeFile se = (StorageMemMapWholeFile) storage;
            try {
                se.writePieceHashesToFile();
            } catch (FileNotFoundException ex) {
                java.util.logging.Logger.getLogger(Vod.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                java.util.logging.Logger.getLogger(Vod.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        DownloadCompletedSim down = new DownloadCompletedSim(self.getAddress(), duration, freeRider, jumped);
        delegator.downloadComplete(down);
        restartToRead(time);
        logger.info("{} become seeder", compName);
        // TODO JIM - remove this and fix problem
        playerMngr.buffering.set(true);
        connectionMngr.changeUtility(VodConfig.SEEDER_UTILITY_VALUE, seeder, storage.getBitField());

        try {
            ActiveTorrents.makeSeeder(torrentFileAddress);
        } catch (Exception e) {
            logger.error(compName + "impossible to add this movie to the background movies");
        }
    }

    /**
     * check if the conditions have been fulfilled to restart reading after
     * having buffered .
     */
    public void restartToRead(int time) {
        UtilityVod utility = (UtilityVod) self.getUtility();
        if (playerMngr.isBuffering()) { // && read) {
            if (storage.getBitField().getNextUncompletedPiece() >= storage.getBitField().numberPieces()) { // at the end of movie
                playerMngr.bufferComplete();
            } else {
                if (time - 1 - ((time - 1) % 10) >= 0) { // true the whole time, time > 0
                    int timeOffset = time - 1 - ((time - 1) % 10);
                    int left = historyMngr.getRest(timeOffset);
                    float utilityDelta = (left - storage.needed()) / BitField.NUM_SUBPIECES_PER_PIECE;
                    float lecRest = (storage.getBitField().numberPieces() - pieceToRead.get()) * readingPeriod;
                    float downRest = (storage.getBitField().numberPieces() - utility.getPiece()) * (10 + ((time - 10) % 10)) / utilityDelta * 1000;
                    if (lecRest > downRest + (overhead * downRest / 100)) {
                        playerMngr.bufferComplete();
                    }
                } else {
                    logger.info("{} Buffering: {} < 0", compName, time - 1 - ((time - 1) % 1));
                }
            }
        }
    }

    /**
     * @return next uncompleted chunk (it might be that we already downloaded
     * chunks after the chunkPos)
     */
    public boolean changeUtility(int time, int chunkPos, int piecePos) {
        int oldUtility = ((UtilityVod) self.getUtility()).getChunk();
        int newUtility = storage.getBitField().setNextUncompletedChunk(chunkPos);
        if (newUtility != oldUtility) {
            connectionMngr.changeUtility(newUtility, seeder, storage.getBitField());
            pieceToRead.set(piecePos);
            playerMngr.buffer();
            restartToRead(time);
            return true;
        }
        return false;
    }
    
    public byte[] getSubpiece(int subpiece) throws IOException {
        return storage.getSubpiece(subpiece);
    }
}
