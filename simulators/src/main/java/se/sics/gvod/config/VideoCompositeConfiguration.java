package se.sics.gvod.config;

import se.sics.gvod.ls.system.LSConfig;

public class VideoCompositeConfiguration extends CompositeConfiguration {

    public static int SNAPSHOT_PERIOD = 5000;
    public static int VIEW_SIZE = 20;
    CroupierConfiguration croupierConfig;
    ParentMakerConfiguration parentMakerConfig;
    InterAsConfiguration interAsConfig;
    VideoConfiguration videoConfig;

//-------------------------------------------------------------------
    public VideoCompositeConfiguration(int parentSize, int parentUpdatePeriod,
            int localHistorySize, int neighbourHistorySize,
            String croupierNodeSelectionPolicy) {
        parentMakerConfig = ParentMakerConfiguration.build()
                .setNumParents(parentSize)
                .setParentUpdatePeriod(parentUpdatePeriod)
                .setKeepParentRttRange(100)
                .setRtoRetries(1)
                ;

        croupierConfig =
                CroupierConfiguration.build()
                .setPolicy(croupierNodeSelectionPolicy)
                .setShuffleLength(VIEW_SIZE / 2)
                .setViewSize(VIEW_SIZE);

        interAsConfig = new InterAsConfiguration();

        videoConfig = new VideoConfiguration(
                LSConfig.getSeed(),
                LSConfig.isSimulation(), LSConfig.getExperimentId(), LSConfig.getExperimentIteration(),
                LSConfig.hasSourceUrlSet(), LSConfig.getSourceUrl(),
                LSConfig.hasDestUrlSet(), LSConfig.getDestIp(), LSConfig.getDestPort(),
                LSConfig.hasInputFileSet(), LSConfig.getInputFilename(),
                LSConfig.hasOutputFileSet(), LSConfig.getOutputFilename(),
                LSConfig.hasMonitorUrlSet(), LSConfig.getMonitorServerUrl(),
                5000,
                1,
                10);
    }

    public CroupierConfiguration getCroupierConfig() {
        return croupierConfig;
    }

    public ParentMakerConfiguration getParentMakerConfig() {
        return parentMakerConfig;
    }

    public InterAsConfiguration getInterAsConfig() {
        return interAsConfig;
    }

    public VideoConfiguration getVideoConfig() {
        return videoConfig;
    }

}
