package se.sics.gvod.config;

public class LsCompositeConfiguration extends CompositeConfiguration {

    public static int SNAPSHOT_PERIOD = 5000;
    public static int VIEW_SIZE = 20;
    CroupierConfiguration croupierConfig;
    ParentMakerConfiguration parentMakerConfig;
    InterAsConfiguration interAsConfig;

//-------------------------------------------------------------------
    public LsCompositeConfiguration(int parentSize, int parentUpdatePeriod,
            int localHistorySize, int neighbourHistorySize,
            String croupierNodeSelectionPolicy) {
        parentMakerConfig = ParentMakerConfiguration.build()
                .setNumParents(parentSize)
                .setParentUpdatePeriod(parentUpdatePeriod)
                .setRtoRetries(0);

        croupierConfig =
                CroupierConfiguration.build()
                .setPolicy(croupierNodeSelectionPolicy)
                .setShuffleLength(VIEW_SIZE / 2)
                .setViewSize(VIEW_SIZE);

        interAsConfig = InterAsConfiguration.build();
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

}
