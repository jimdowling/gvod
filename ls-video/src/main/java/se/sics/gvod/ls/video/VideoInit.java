package se.sics.gvod.ls.video;

import se.sics.gvod.config.VideoConfiguration;
import se.sics.gvod.common.Self;
import se.sics.kompics.Init;

/**
 *
 * @author Jim
 */
public class VideoInit extends Init {
    
    private final Self self;
    private final VideoConfiguration config;
    private final boolean source;

    public VideoInit(Self self, boolean source, VideoConfiguration config) {
        this.self = self;
        this.source = source;
        this.config = config;
    }

    public Self getSelf() {
        return self;
    }

    public boolean isSource() {
        return source;
    }

    public VideoConfiguration getConfig() {
        return config;
    }
    
}
