package se.sics.gvod.bootstrap.port;

import se.sics.kompics.Request;

public class BootstrapHelperHb extends Request {

    private final boolean available;

    public BootstrapHelperHb(boolean available) {
        this.available = available;
    }

    public boolean isAvailable() {
        return available;
    }
    
}
