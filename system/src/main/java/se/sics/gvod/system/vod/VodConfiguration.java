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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import se.sics.gvod.config.AbstractConfiguration;
import se.sics.gvod.config.VodConfig;

/**
 *
 * @author jim
 */
public class VodConfiguration
        extends AbstractConfiguration<VodConfiguration> {

    /**
     * Fields cannot be private. Package protected, ok.
     */
    int shuffleLength;
    int randomViewSize;
    long shufflePeriod;
    long shuffleTimeout;
    long connectionTimeout;
    int bitTorrentSetSize;
    long refTimeout;
    long verifyPeriod;
    int offset;
    long dataOfferPeriod;
    long dataRequestTimeout;
    long readingPeriod;
    int pipeSize;
    long limReadingWindow;
    int upperSetSize;
    int lowerSetSize;
    int infUtilFrec;
    int percentBack;
    long checkPositionPeriod;
    int utilitySetFillingRate;
    long length;  // simulation
    String videoName;
    int comWinSize;
    int ackTimeout;
    int bufferingWindow;
    int mediaPort;
    int mtu;
    String torrentFilename;

    /**
     * Default constructor comes first.
     * Should only called by AbstractConfiguration BaseClass.
     */
    public VodConfiguration() {
        super();
    }
    /**
     * 
     * @param seed
     * @param shuffleLength
     * @param randomViewSize
     * @param shufflePeriod
     * @param shuffleTimeout
     * @param connectionTimeout
     * @param bittorrentSetSize
     * @param refTimeout
     * @param verifyPeriod
     * @param offset
     * @param dataOfferPeriod
     * @param dataRequestTimeout
     * @param readingPeriod
     * @param pipeSize
     * @param limReadingWindow
     * @param upperSetSize
     * @param belowSetSize
     * @param infUtilFrec
     * @param percentBack
     * @param checkPositionPeriod
     * @param utilitySetFillingRate
     * @param length
     * @param videoName
     * @param comWinSize
     * @param ackTimeout
     * @param bufferingWindow
     * @param mediaPort
     * @param mtu
     * @param torrentFilename 
     */
    public VodConfiguration(int seed,
            int shuffleLength, int randomViewSize,
            long shufflePeriod, long shuffleTimeout,
            long connectionTimeout, int bittorrentSetSize,
            long refTimeout, long verifyPeriod,
            int offset,
            long dataOfferPeriod, long dataRequestTimeout,
            long readingPeriod, int pipeSize,
            long limReadingWindow, int upperSetSize,
            int belowSetSize, int infUtilFrec, int percentBack,
            long checkPositionPeriod, int utilitySetFillingRate,
            long length,
            String videoName,
            int comWinSize, int ackTimeout, int bufferingWindow,
            int mediaPort, int mtu, String torrentFilename) {
        this.seed = seed;
        this.shufflePeriod = shufflePeriod;
        this.shuffleTimeout = shuffleTimeout;
        this.randomViewSize = randomViewSize;
        this.shuffleLength = shuffleLength;
        this.connectionTimeout = connectionTimeout;
        this.bitTorrentSetSize = bittorrentSetSize;
        this.refTimeout = refTimeout;
        this.verifyPeriod = verifyPeriod;
        this.offset = offset;
        this.dataOfferPeriod = dataOfferPeriod;
        this.dataRequestTimeout = dataRequestTimeout;
        this.readingPeriod = readingPeriod;
        this.pipeSize = pipeSize;
        this.limReadingWindow = limReadingWindow;
        this.upperSetSize = upperSetSize;
        this.lowerSetSize = belowSetSize;
        this.infUtilFrec = infUtilFrec;
        this.percentBack = percentBack;
        this.checkPositionPeriod = checkPositionPeriod;
        this.utilitySetFillingRate = utilitySetFillingRate;
        this.length = length;
        this.videoName = videoName;
        this.comWinSize = comWinSize;
        this.ackTimeout = ackTimeout;
        this.bufferingWindow = bufferingWindow;
        this.mediaPort = mediaPort;
        this.mtu = mtu;
        this.torrentFilename = torrentFilename;
    }

    /**
     * Other constructor comes here.
     * @param videoName
     * @param len 
     */
    public VodConfiguration(String videoName, long len) {
        this(VodConfig.getSeed(), 
                VodConfig.CROUPIER_SHUFFLE_LENGTH,
                VodConfig.CROUPIER_VIEW_SIZE,
                VodConfig.CROUPIER_SHUFFLE_PERIOD,
                VodConfig.DEFAULT_RTO,
                VodConfig.CONNECTION_TIMEOUT,
                VodConfig.BITTORRENT_SET_SIZE,
                VodConfig.REF_TIMEOUT,
                VodConfig.VERIFY_PERIOD,
                VodConfig.OFFSET,
                VodConfig.DATA_OFFER_PERIOD,
                VodConfig.DATA_REQUEST_TIMEOUT,
                VodConfig.READING_PERIOD,
                VodConfig.LB_DEFAULT_PIPELINE_SIZE,
                VodConfig.LIM_READING_WINDOW,
                VodConfig.UPPER_SET_SIZE,
                VodConfig.BELOW_SET_SIZE,
                VodConfig.INF_UTIL_FREC,
                VodConfig.PERCENT_BACK,
                VodConfig.CHECK_POSITION_PERIOD,
                VodConfig.GRADIENT_UTILITY_SET_FILLING_RATE,
                len,
                videoName,
                VodConfig.LB_MAX_SEGMENT_SIZE,
                VodConfig.ACK_TIMEOUT,
                VodConfig.BUFFERING_WINDOW_NUM_PIECES,
                VodConfig.getMediaPort(),
                VodConfig.DEFAULT_MTU,
                "no-torrent-file-defined");
    }

    public static long fileLen(String videoFileName) {
        File f = new File(videoFileName);
        long len = 0;
        FileInputStream fin = null;
        try {
            fin = new FileInputStream(f);
            len = fin.getChannel().size();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(VodConfiguration.class.getName()).log(Level.SEVERE, null, ex);
            return 0;
        } catch (IOException ex) {
            Logger.getLogger(VodConfiguration.class.getName()).log(Level.SEVERE, null, ex);
            return 0;
        }
        finally {
            if (fin != null) {
                try {
                    fin.close();
                } catch (IOException ex) {
                    Logger.getLogger(VodConfiguration.class.getName()).log(Level.SEVERE, null, ex);
                    return 0;
                }
            }
        }
        return len;
    }
    
    public static VodConfiguration build(String videoFileName) {
        long len = fileLen(videoFileName);
        return new VodConfiguration(videoFileName, len);
    }
    

    public int getMtu() {
        return mtu;
    }

    public int getMediaPort() {
        return mediaPort;
    }

    public int getBufferingWindow() {
        return bufferingWindow;
    }

    public long getShufflePeriod() {
        return shufflePeriod;
    }

    public long getShuffleTimeout() {
        return shuffleTimeout;
    }

    public int getRandomViewSize() {
        return randomViewSize;
    }

    public int getShuffleLength() {
        return shuffleLength;
    }

    public long getConnectionTimeout() {
        return connectionTimeout;
    }

    public int getBitTorrentSetSize() {
        return bitTorrentSetSize;
    }

    public long getRefTimeout() {
        return refTimeout;
    }

    public long getVerifyPeriod() {
        return verifyPeriod;
    }

    public int getOffset() {
        return offset;
    }

    public long getDataOfferPeriod() {
        return dataOfferPeriod;
    }

    public String getName() {
        return videoName;
    }

    public long getLength() {
        return length;
    }

    public String getTorrentFilename() {
        return torrentFilename;
    }

    public void setTorrentFilename(String metaInfoAddress) {
        this.torrentFilename = metaInfoAddress;
    }

    public long getDataRequestTimeout() {
        return dataRequestTimeout;
    }

    public long getReadingPeriod() {
        return readingPeriod;
    }

    public int getPipeSize() {
        return pipeSize;
    }

    public long getLimReadingWindow() {
        return limReadingWindow;
    }

    public int getInfUtilFrec() {
        return infUtilFrec;
    }

    public int getPercentBack() {
        return percentBack;
    }

    public int getUpperSetSize() {
        return upperSetSize;
    }

    public long getCheckPositionPeriod() {
        return checkPositionPeriod;
    }

    public int getLowerSetSize() {
        return lowerSetSize;
    }

    public int getUtilitySetFillingRate() {
        return utilitySetFillingRate;
    }

    public void setReadingPeriod(long readingPeriod) {
        this.readingPeriod = readingPeriod;
    }

    public int getComWinSize() {
        return comWinSize;
    }

    public int getAckTimeout() {
        return ackTimeout;
    }

    public void setVideoName(String name) {
        this.videoName = name;
    }

    public VodConfiguration setShufflePeriod(long shufflePeriod) {
        this.shufflePeriod = shufflePeriod;
        return this;
    }

    public VodConfiguration setShuffleTimeout(long shuffleTimeout) {
        this.shuffleTimeout = shuffleTimeout;
        return this;
    }

    public VodConfiguration setShuffleLength(int shuffleLength) {
        this.shuffleLength = shuffleLength;
        return this;
    }

    public VodConfiguration setRandomViewSize(int randomViewSize) {
        this.randomViewSize = randomViewSize;
        return this;
    }

    public VodConfiguration setConnectionTimeout(long connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
        return this;
    }

    public VodConfiguration setDataRequestTimeout(long dataRequestTimeout) {
        this.dataRequestTimeout = dataRequestTimeout;
        return this;
    }

    public VodConfiguration setBitTorrentSetSize(int bitTorrentSetSize) {
        this.bitTorrentSetSize = bitTorrentSetSize;
        return this;
    }

    public VodConfiguration setRefTimeout(long refTimeout) {
        this.refTimeout = refTimeout;
        return this;
    }

    public VodConfiguration setVerifyPeriod(long verifyPeriod) {
        this.verifyPeriod = verifyPeriod;
        return this;
    }

    public VodConfiguration setOffset(int offset) {
        this.offset = offset;
        return this;
    }

    public VodConfiguration setDataOfferPeriod(long dataOfferPeriod) {
        this.dataOfferPeriod = dataOfferPeriod;
        return this;
    }

    public VodConfiguration setPipeSize(int pipeSize) {
        this.pipeSize = pipeSize;
        return this;
    }

    public VodConfiguration setUpperSetSize(int upperSetSize) {
        this.upperSetSize = upperSetSize;
        return this;
    }

    public VodConfiguration setLowerSetSize(int lowerSetSize) {
        this.lowerSetSize = lowerSetSize;
        return this;
    }

    public VodConfiguration setLimReadingWindow(long limReadingWindow) {
        this.limReadingWindow = limReadingWindow;
        return this;
    }

    public VodConfiguration setInfUtilFrec(int infUtilFrec) {
        this.infUtilFrec = infUtilFrec;
        return this;
    }

    public VodConfiguration setPercentBack(int percentBack) {
        this.percentBack = percentBack;
        return this;
    }

    public VodConfiguration setCheckPositionPeriod(long checkPositionPeriod) {
        this.checkPositionPeriod = checkPositionPeriod;
        return this;
    }

    public VodConfiguration setUtilitySetFillingRate(int utilitySetFillingRate) {
        this.utilitySetFillingRate = utilitySetFillingRate;
        return this;
    }

    public VodConfiguration setLength(long length) {
        this.length = length;
        return this;
    }

    public VodConfiguration setComWinSize(int comWinSize) {
        this.comWinSize = comWinSize;
        return this;
    }

    public VodConfiguration setAckTimeout(int ackTimeout) {
        this.ackTimeout = ackTimeout;
        return this;
    }

    public VodConfiguration setBufferingWindow(int bufferingWindow) {
        this.bufferingWindow = bufferingWindow;
        return this;
    }

    public VodConfiguration setMediaPort(int mediaPort) {
        this.mediaPort = mediaPort;
        return this;
    }

    public VodConfiguration setMtu(int mtu) {
        this.mtu = mtu;
        return this;
    }

}
