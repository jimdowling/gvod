/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.system.peer.events;

import se.sics.gvod.timer.OverlayTimeout;
import se.sics.gvod.timer.SchedulePeriodicTimeout;


/**
 *
 * @author jim
 */
public class InitiateMembershipSearch extends OverlayTimeout {
    
    public InitiateMembershipSearch(SchedulePeriodicTimeout schedule, int overlayId) {
        super(schedule, overlayId);
    }

}
