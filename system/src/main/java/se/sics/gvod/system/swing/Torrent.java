/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package se.sics.gvod.system.swing;

import java.io.File;
import se.sics.kompics.Component;

/**
 *
 * @author jdowling
 */
public class Torrent {

    private final Component peer;
    private final File torrentFile;
    private boolean seed;
    private int percentDownloaded;
    private int downloadSpeedInBytes = 0;
    private int timeUntilStartViewing = 10*1000;

    public Torrent(Component peer, File torrentFile, boolean seed) {
        this.peer = peer;
        this.torrentFile = torrentFile;
        this.seed = seed;
        if (seed == true) {
            timeUntilStartViewing = 0;
        }
    }

    public int getPercentDownloaded() {
        return percentDownloaded;
    }

    public Component getPeer() {
        return peer;
    }

    public int getTimeUntilStartViewing() {
        return timeUntilStartViewing;
    }

    public File getTorrentFile() {
        return torrentFile;
    }

    public void setPercentDownloaded(int percentDownloaded) {
        this.percentDownloaded = percentDownloaded;
    }

    public void setSeed(boolean seed) {
        this.seed = seed;
    }

    public int getDownloadSpeedInBytes() {
        return downloadSpeedInBytes;
    }

    public void setDownloadSpeedInBytes(int downloadSpeedInBytes) {
        this.downloadSpeedInBytes = downloadSpeedInBytes;
    }

}
