package se.sics.gvod.web;

import se.sics.kompics.Init;
import se.sics.gvod.address.Address;
import se.sics.gvod.net.VodAddress;

public final class VodWebApplicationInit extends Init {

	private final Address monitorWebAddress, bootstrapWebAddress;

	private final VodAddress self;

	private final int webPort;

    private final int utility;

	public VodWebApplicationInit(VodAddress self,
			Address monitorWebAddress, Address bootstrapWebAddress, int webPort, int utility) {
		super();
		this.self = self;
		this.monitorWebAddress = monitorWebAddress;
		this.bootstrapWebAddress = bootstrapWebAddress;
		this.webPort = webPort;
        this.utility=utility;
	}

	public final VodAddress getSelf() {
		return self;
	}

	public final Address getBootstrapWebAddress() {
		return bootstrapWebAddress;
	}

	public final Address getMonitorWebAddress() {
		return monitorWebAddress;
	}

	public int getWebPort() {
		return webPort;
	}

    public int getUtility() {
        return utility;
    }
    
}
