package se.sics.gvod.simulator.video;

import se.sics.gvod.config.VideoCompositeConfiguration;
import se.sics.gvod.ls.system.LSConfig;
import se.sics.gvod.ls.video.snapshot.Experiment;
import se.sics.gvod.ls.video.snapshot.StatsRestClient;
import se.sics.gvod.simulator.video.scenarios.VideoChurnScenario;
import se.sics.gvod.simulator.video.scenarios.VideoCrashScenario;
import se.sics.gvod.simulator.video.scenarios.VideoJoinScenario;
import se.sics.gvod.simulator.video.scenarios.VideoScenario;

public class Main {

    public static int SEED;
    public static int PARENT_SIZE = 4;
    public static int PARENT_UPDATE_ROUND_TIME = 2000;

    public static void main(String[] args) throws Throwable {
        if (args.length < 8) {
            System.err.println("");
            System.err.println("usage: <prog> seed numPub numPriv local-history-size neighbour-history-size expLength nodeSelection experimentId experimentIterations [scenarioOption]");
            System.err.println("");

            System.exit(1);
        }
        SEED = Integer.parseInt(args[0]);

        String experimentId = args[8];
        String experimentIteration = args[9];


        LSConfig.init(new String[]{"-seed", args[0],
                    "-sim",
                    "-i", "bunny_source.mp4",
                    //"-m", "lucan.sics.se:8080/video",
//                    "-m", "nwahlen.sics.se:8080/video",
                    "-m", "kista.wahni.se:8080/video",
                    "-eid", experimentId,
                    "-eit", experimentIteration
                });

        // Check with monitoring server
        StatsRestClient client = null;
        if (LSConfig.hasMonitorUrlSet()) {
            client = new StatsRestClient(LSConfig.getMonitorServerUrl());
            boolean exists = client.experimentIterationExists(experimentId, experimentIteration);
            if (exists) {
                Integer max = client.getMaxExperimentId();
                System.out.println(" --------- ERROR ---------");
                System.out.println("This (experiment id, experiment iteration) pair already exists.");
                System.out.println("Current maximum experiment id is: " + max);
                System.out.println(" -------------------------");
                System.exit(1);
            }
        }

        int i = 1;
        VideoScenario scenario = null;
        String scenarioName = args[i++];
        int numPub = (int) Double.parseDouble(args[i++]);
        int numPriv = (int) Double.parseDouble(args[i++]);
        int localHistorySize = (int) Double.parseDouble(args[i++]);
        int neighbourHistorySize = (int) Double.parseDouble(args[i++]);
        int expLength = (int) Integer.parseInt(args[i++]);
        String nodeSelection = args[i++];

        VideoScenario.PUBLIC = numPub;
        VideoScenario.PRIVATE = numPriv;
        VideoScenario.COLLECT_VIDEO_RESULTS = expLength;

        VideoCompositeConfiguration configuration = new VideoCompositeConfiguration(
                PARENT_SIZE,
                PARENT_UPDATE_ROUND_TIME,
                localHistorySize, neighbourHistorySize, nodeSelection);
        configuration.store();

        if (scenarioName.equalsIgnoreCase("video")) {
            scenario = new VideoStreamingScenario();
        } else if (scenarioName.equalsIgnoreCase("join")) {
            scenario = new VideoJoinScenario();
        } else if (scenarioName.equalsIgnoreCase("churn")) {
            scenario = new VideoChurnScenario();
        } else if (scenarioName.equalsIgnoreCase("crash")) {
            scenario = new VideoCrashScenario();
        }

        if (scenario == null) {
            System.err.println("");
            System.err.println("invalid scenario name "+scenarioName+".");
            System.err.println("available scenarios: video, join, churn, crash.");
            System.err.println("");

            System.exit(1);
        }

        System.out.println(" -- Launching scenario \"" + scenarioName + "\" -- "
                + "\nnumPub:\t" + scenario.PUBLIC
                + "\nnumPriv:\t" + VideoScenario.PRIVATE
                + "\nlocalHistorySize:\t" + localHistorySize
                + "\nneighbourHistorySize:\t" + neighbourHistorySize
                + "\nexpLength:\t" + expLength
                + "\nnodeSelection:\t" + nodeSelection);
        System.out.println(LSConfig.getConfigString());


        if (LSConfig.hasMonitorUrlSet()) {
            // Check existence of Experiment
            Experiment e = client.findExperiment(experimentId);
            if (e == null) {
                e = new Experiment(Integer.valueOf(experimentId));
                // Video Configuration
                e.setMaxOutClose((short) LSConfig.VIDEO_MAX_OUT_CLOSE);
                e.setMaxOutRandom((short) LSConfig.VIDEO_MAX_OUT_RANDOM);
                e.setSpPerPiece((short) LSConfig.FEC_SUB_PIECES);
                e.setRedundantSps((short) (LSConfig.FEC_ENCODED_PIECES - LSConfig.FEC_SUB_PIECES));
                e.setScenario(scenarioName);
                e.setArguments(LSConfig.getCommandLineArgs());
                // Other Configuration
                e.setLocalHistory((short) localHistorySize);
                e.setNeighbourHistory((short) neighbourHistorySize);
                e.setExpLength((short) expLength);
                e.setNodeSelection(nodeSelection);
                // Info
                e.setIterations(Short.valueOf(experimentIteration));
                e.setStatus(Experiment.Status.opened);
                client.createExperiment(e);
            }
        }

        scenario.setSeed(System.currentTimeMillis());
        try {
            scenario.simulateVideo();
        } catch (Exception ex) {
            if (LSConfig.hasMonitorUrlSet()) {
                Experiment failedE = client.findExperiment(experimentId);
                if (failedE != null) {
                    failedE.setStatus(Experiment.Status.failed);
                    failedE.setEndTs(null);
                    client.editExperiment(failedE);
                }
            }
            throw (ex);
        }
    }
}
