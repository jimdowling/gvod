/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.bootstrap.client;

import se.sics.gvod.bootstrap.msgs.BootstrapMsg.AddOverlayReq;
import se.sics.gvod.net.msgs.RewriteableRetryTimeout;
import se.sics.gvod.net.msgs.ScheduleRetryTimeout;

/**
 *
 * @author jdowling
 */
public class AddOverlayTimeout extends RewriteableRetryTimeout {

    private int overlay;

    public AddOverlayTimeout(ScheduleRetryTimeout scheduleTimeout,
            AddOverlayReq requestMsg) {
        super(scheduleTimeout, requestMsg, requestMsg.getVodSource().getOverlayId());
        this.overlay = requestMsg.getOverlayId();
    }

    public int getOverlay() {
        return overlay;
    }
}
