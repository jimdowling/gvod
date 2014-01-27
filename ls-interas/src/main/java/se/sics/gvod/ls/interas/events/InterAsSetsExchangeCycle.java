package se.sics.gvod.ls.interas.events;

import se.sics.gvod.timer.SchedulePeriodicTimeout;
import se.sics.gvod.timer.ScheduleTimeout;
import se.sics.gvod.timer.Timeout;

/**
 *
 * @author jim
 */
public class InterAsSetsExchangeCycle extends Timeout {

    public InterAsSetsExchangeCycle(SchedulePeriodicTimeout periodicTimeout) {
        super(periodicTimeout);
    }

    public InterAsSetsExchangeCycle(ScheduleTimeout timeout) {
        super(timeout);
    }
}
