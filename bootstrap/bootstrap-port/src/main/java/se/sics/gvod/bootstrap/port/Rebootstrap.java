/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package se.sics.gvod.bootstrap.port;

import se.sics.kompics.Event;
import se.sics.gvod.common.Utility;

/**
 *
 * @author jdowling
 */
public class Rebootstrap extends Event {

    private final int id;
    private final int overlay;
    private final Utility utility;

    public Rebootstrap(int id, int overlay, Utility utility) {
        this.id = id;
        this.overlay = overlay;
        this.utility = utility;
    }

    public int getId() {
        return id;
    }
    
    public int getOverlay() {
        return overlay;
    }

    public Utility getUtility() {
        return utility;
    }

}