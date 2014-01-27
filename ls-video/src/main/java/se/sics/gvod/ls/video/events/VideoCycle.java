package se.sics.gvod.ls.video.events;

import se.sics.gvod.timer.SchedulePeriodicTimeout;
import se.sics.gvod.timer.ScheduleTimeout;
import se.sics.gvod.timer.Timeout;

/**
 *
 * @author jim
 */
public class VideoCycle extends Timeout {

    public VideoCycle(SchedulePeriodicTimeout periodicTimeout) {
        super(periodicTimeout);
    }

    public VideoCycle(ScheduleTimeout timeout) {
        super(timeout);
    }
}
