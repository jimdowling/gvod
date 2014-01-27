/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package se.sics.gvod.system.peer.events;

/**
 *
 * @author jim
 */
public class ReportDownloadSpeed extends OverlayEvent {

    private final String torrent;
    private final int numBytesSec;

    public ReportDownloadSpeed(int overlayId, int numBytesSec, String torrent) {
        super(overlayId);
        this.torrent = torrent;
        this.numBytesSec = numBytesSec;
    }

    public int getNumBytesSec() {
        return numBytesSec;
    }

    public String getTorrent() {
        return torrent;
    }




}
