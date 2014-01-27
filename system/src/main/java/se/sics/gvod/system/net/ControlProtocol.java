/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.system.net;

import java.io.IOException;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.gvod.system.main.SwingMain;

/**
 *
 * @author jdowling
 */
public class ControlProtocol {

    private static final String START = "START";
    private static final String PLAY = "PLAY";
    private static final String PAUSE = "PAUSE";
    private static final String STOP = "STOP";
    private static final String SHUTDOWN = "SHUTDOWN";
    private static final String RESUME = "RESUME";
    private Logger logger = LoggerFactory.getLogger(ControlProtocol.class);
    private static final int WAITING = 0;
    private static final int PLAYING = 1;
    private static final int ERROR = 2;
    private static final int PAUSED = 3;
    private static final int STOPPED = 5;
    private int state = WAITING;
    private String activeTorrentUrl = "";
    private String activeGuiUrl = "";
    private final SwingMain swingMain;

    public ControlProtocol(SwingMain swingMain) {
        this.swingMain = swingMain;
    }

    public String processInput(String theInput) {
        String theOutput = null;

        logger.debug(theInput);
        if (theInput == null || theInput.length() < PLAY.length()) {
            return theOutput;
        }

        String input = theInput.length() < START.length() ? theInput
                : theInput.substring(0, START.length());

        if (input.equalsIgnoreCase(START)) {
            state = PLAYING;
            String torrentUrl = theInput.substring(START.length() + 1, theInput.length());
            if (activeTorrentUrl.compareTo(torrentUrl) != 0) {
                try {
                    
                    long maxSleep = 60 * 1000;
                    long totalSleep = 0;
                    long sleepTime = 100;
                    while (!swingMain.isInitialized() && totalSleep < maxSleep) {
                        try {
                            totalSleep += sleepTime;
                            Thread.sleep(sleepTime);
                        } catch (InterruptedException ex) {
                            java.util.logging.Logger.getLogger(SwingMain.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    if (swingMain.isInitialized()) {
                        theOutput = swingMain.downloadTorrentAndCreatePeer(torrentUrl);
                    }
                    if (theOutput == null) {
                        theOutput = "ERROR - peer already running for " + torrentUrl;
                    } else {
                        activeGuiUrl = theOutput;
                    }
                } catch (IOException ex) {
                    theOutput = "ERROR " + ex.getMessage();
                }
            } else {
                logger.warn("Tried to start {} when already playing", activeTorrentUrl);
                // return the same URL to the torrent for viewing - http://127.0.0.1:58026/...
                theOutput = activeGuiUrl; 
            }
        } else if (theInput.substring(0, PAUSE.length()).equalsIgnoreCase(PAUSE)) {
            if (state == PLAYING) {
                state = PAUSED;
            }

        } else if (theInput.substring(0, SHUTDOWN.length()).equalsIgnoreCase(SHUTDOWN)) {
        } else if (theInput.substring(0, RESUME.length()).equalsIgnoreCase(RESUME)) {
        }

        logger.debug(theOutput);

        return theOutput;
    }
}