/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.system.vod;

import se.sics.kompics.ChannelFilter;
import se.sics.kompics.web.WebRequest;

/**
 *
 * @author Jim Dowling<jdowling@sics.se>
 */
public final class WebRequestFilterOverlayId extends ChannelFilter<WebRequest, Integer> {

    public WebRequestFilterOverlayId(int id) {
        super(WebRequest.class, id, false);
    }

    @Override
    public Integer getValue(WebRequest event) {
        return event.getDestination();
    }
}
