/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.system.vod;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static se.sics.gvod.system.vod.Vod.durationToString;

/**
 *
 * @author Alex Ormenisan <aaor@sics.se>
 */
public class PlayerManager {

    private final Logger logger = LoggerFactory.getLogger(Vod.class);

    public AtomicBoolean buffering;
    public AtomicInteger nextPieceToSend = new AtomicInteger(0);
    public int bufferingWindow;
    public long totalBufferTime;
    public long startedAtTime;
    public long stoppedReadingAtTime;
    public long startJumpForward;
    public long totalJumpForward;

    private final String compName;

    public PlayerManager(VodInit init, String compName) {
        this.compName = compName;
        this.bufferingWindow = init.getConfig().getBufferingWindow();
        if (init.isSeed()) {
            this.buffering.set(false);
        } else {
            this.buffering.set(true);
        }

        this.totalBufferTime = 0;
        this.startedAtTime = System.currentTimeMillis();
        this.stoppedReadingAtTime = startedAtTime;
        this.startJumpForward = 0;
        this.totalJumpForward = 0;
    }

    public boolean isBuffering() {
        return buffering.get();
    }

    public void bufferComplete() {
        if (buffering.get() == true) {
            buffering.set(false);
            long bufferTime = System.currentTimeMillis() - stoppedReadingAtTime;
            logger.info("{} starting reading after {}", compName, durationToString(bufferTime));

            totalBufferTime += bufferTime;
            if (startJumpForward != 0) {
                totalJumpForward += (System.currentTimeMillis() - startJumpForward);
                startJumpForward = 0;
            }
        }
    }

    public void buffer() {
        if (buffering.get() == false) {
            buffering.set(true);
            stoppedReadingAtTime = System.currentTimeMillis();
        }
    }
}
