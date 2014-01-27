/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package se.sics.gvod.system.swing.evts;

import se.sics.gvod.timer.SchedulePeriodicTimeout;
import se.sics.gvod.timer.Timeout;

/**
 *
 * @author jdowling
 */
public class CheckTorrentsTimeout extends Timeout {

    public CheckTorrentsTimeout(SchedulePeriodicTimeout spt) {
        super(spt);
    }

    
}
