package se.sics.gvod.simulator.interas;

import se.sics.gvod.config.InterAsConfiguration;
import se.sics.gvod.address.Address;
import se.sics.gvod.config.CroupierConfiguration;
import se.sics.gvod.config.ParentMakerConfiguration;
import se.sics.kompics.Init;

public final class LsSimulatorInit extends Init {

    private final CroupierConfiguration croupierConfiguration;
    private final ParentMakerConfiguration parentMakerConfiguration;
    private final InterAsConfiguration interAsConfiguration;

//-------------------------------------------------------------------	
    public LsSimulatorInit(
            CroupierConfiguration croupierConfiguration,
            ParentMakerConfiguration parentMakerConfiguration,
            InterAsConfiguration interAsConfiguration) {
        super();
        this.croupierConfiguration = croupierConfiguration;
        this.parentMakerConfiguration = parentMakerConfiguration;
        this.interAsConfiguration = interAsConfiguration;
    }

    public InterAsConfiguration getInterAsConfiguration() {
        return interAsConfiguration;
    }

    public CroupierConfiguration getCroupierConfiguration() {
        return croupierConfiguration;
    }

//-------------------------------------------------------------------	
    public ParentMakerConfiguration getParentMakerConfiguration() {
        return parentMakerConfiguration;
    }
}
