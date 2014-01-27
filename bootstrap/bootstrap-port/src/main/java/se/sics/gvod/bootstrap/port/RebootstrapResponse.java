/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.bootstrap.port;

import java.util.List;
import se.sics.gvod.common.VodDescriptor;
import se.sics.kompics.Event;
//import se.sics.kompics.p2p.overlay.OverlayAddress;

/**
 *
 * @author jdowling
 */
public class RebootstrapResponse extends Event {

    private final int id;
    private final List<VodDescriptor> gvodInsiders;

    public RebootstrapResponse(int id, List<VodDescriptor> gvodInsiders) {
        this.id = id;
        this.gvodInsiders = gvodInsiders;
    }

    public int getId() {
        return id;
    }
     
    public List<VodDescriptor> getVodInsiders() {
        return gvodInsiders;
    }
}
