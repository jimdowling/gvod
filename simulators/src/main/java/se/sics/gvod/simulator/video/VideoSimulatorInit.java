package se.sics.gvod.simulator.video;

import se.sics.gvod.config.CroupierConfiguration;
import se.sics.gvod.config.VideoConfiguration;
import se.sics.gvod.config.ParentMakerConfiguration;
import se.sics.gvod.config.InterAsConfiguration;
import se.sics.kompics.Init;

public final class VideoSimulatorInit extends Init {

    private final CroupierConfiguration croupierConfiguration;
    private final ParentMakerConfiguration parentMakerConfiguration;
    private final InterAsConfiguration interAsConfiguration;
    private final VideoConfiguration videoConfiguration;

//-------------------------------------------------------------------	
    public VideoSimulatorInit(
            CroupierConfiguration croupierConfiguration,
            ParentMakerConfiguration parentMakerConfiguration,
            InterAsConfiguration interAsConfiguration,
            VideoConfiguration videoConfiguration) {
        super();
        this.croupierConfiguration = croupierConfiguration;
        this.parentMakerConfiguration = parentMakerConfiguration;
        this.interAsConfiguration = interAsConfiguration;
        this.videoConfiguration = videoConfiguration;
    }
    
    public VideoConfiguration getVideoConfiguration() {
        return videoConfiguration;
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
