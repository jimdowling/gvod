/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.system.simulator.bw.model;

import se.sics.gvod.timer.SchedulePeriodicTimeout;
import se.sics.gvod.timer.Timeout;

/**
 *
 * @author gautier
 */
public class ReadingPosition extends Timeout {

    public ReadingPosition(SchedulePeriodicTimeout request) {
        super(request);
    }
}
