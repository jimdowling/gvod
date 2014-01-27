package se.sics.gvod.bootstrap.port;

import se.sics.kompics.PortType;

public class BootstrapPort extends PortType {
    {
        negative(BootstrapRequest.class);
        negative(BootstrapHeartbeat.class);
        positive(BootstrapResponse.class);
        negative(AddOverlayRequest.class);
        positive(AddOverlayResponse.class);
    }
}