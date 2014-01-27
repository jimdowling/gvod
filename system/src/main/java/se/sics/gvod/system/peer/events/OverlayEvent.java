/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.system.peer.events;

import se.sics.gvod.timer.OverlayId;
import se.sics.kompics.Event;


/**
 * This class is used to filter events to Vod components.
 * Without it, every Vod component would receive N * #events
 * for N Vod components.
 * @author jim
 */
public abstract class OverlayEvent extends Event implements OverlayId{

    private final int overlayId;
    
    public OverlayEvent(int overlayId) {
        this.overlayId = overlayId;
    }

    @Override
    public int getOverlayId() {
        return overlayId;
    }
}
