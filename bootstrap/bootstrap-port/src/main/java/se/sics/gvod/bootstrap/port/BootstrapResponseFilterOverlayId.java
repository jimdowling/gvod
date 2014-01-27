/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.bootstrap.port;

import se.sics.kompics.ChannelFilter;

/**
 *
 * @author Jim Dowling<jdowling@sics.se>
 */
public final class BootstrapResponseFilterOverlayId extends ChannelFilter<BootstrapResponse, Integer> {

    public BootstrapResponseFilterOverlayId(int id) {
        super(BootstrapResponse.class, id, true);
    }

    @Override
    public Integer getValue(BootstrapResponse event) {
        return event.getOverlayId();
    }
}
