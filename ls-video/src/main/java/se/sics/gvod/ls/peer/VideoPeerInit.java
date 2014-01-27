package se.sics.gvod.ls.peer;

import se.sics.gvod.common.Self;
import se.sics.gvod.config.BootstrapConfiguration;
import se.sics.gvod.config.CroupierConfiguration;
import se.sics.gvod.config.VideoConfiguration;
import se.sics.kompics.Init;

public final class VideoPeerInit extends Init {

    private final Self self;
    private final boolean source;
    private final BootstrapConfiguration bootstrapConfiguration;
    private final VideoConfiguration videoConfiguration;
    private final CroupierConfiguration croupierConfiguration;

//-------------------------------------------------------------------	
    public VideoPeerInit(Self self,
            boolean source,
            CroupierConfiguration croupierConfiguration,
            VideoConfiguration videoConfiguration,
            BootstrapConfiguration bootstrapConfiguration
            ) {
        super();
        this.self = self;
        this.source = source;
        this.croupierConfiguration = croupierConfiguration;
        this.videoConfiguration = videoConfiguration;
        this.bootstrapConfiguration = bootstrapConfiguration;
    }

    public CroupierConfiguration getCroupierConfiguration() {
        return croupierConfiguration;
    }

    public VideoConfiguration getVideoConfiguration() {
        return videoConfiguration;
    }

    public Self getSelf() {
        return self;
    }

    public BootstrapConfiguration getBootstrapConfiguration() {
        return bootstrapConfiguration;
    }

    public boolean isSource() {
        return source;
    }

}
