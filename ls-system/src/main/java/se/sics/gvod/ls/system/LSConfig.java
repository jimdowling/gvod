package se.sics.gvod.ls.system;

import java.beans.XMLDecoder;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import org.apache.commons.cli.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.gvod.config.BaseCommandLineConfig;
import se.sics.gvod.config.VodConfig;

/**
 *
 * @author Niklas Wahl&#233;n <nwahlen@kth.se>
 */
public class LSConfig extends BaseCommandLineConfig {

    private static final Logger logger = LoggerFactory.getLogger(VodConfig.class);
    public static String STARTUP_CONFIG_FILE;
//    protected static StartupConfig startupConfig;
    private static List<String> argList;
    private static Option[] optArray;
    // Options
    protected Option seedOption;
    protected Option simulationOption;
    protected Option experimentIdOption;
    protected Option experimentIterationOption;
    protected Option sourceUrlOption;
    protected Option localHttpServerOption;
    protected Option inputFilenameOption;
    protected Option outputFilenameOption;
    protected Option monitorServerOption;
    protected Option sourceOption;
    // Option properties names (why public?)
    public static final String PROPERTY_SIMULATION_SET = "simulation.set";
    public static final String PROPERTY_SOURCE = "source";
    public static final String PROPERTY_SOURCE_URL_SET = "source.url.set";
    public static final String PROPERTY_SOURCE_URL = "source.url";
    public static final String PROPERTY_DEST_URL_SET = "local.http.server.set";
    public static final String PROPERTY_DEST_IP = "local.http.server.ip";
    public static final String PROPERTY_DEST_PORT = "local.http.server.port";
    public static final String PROPERTY_INPUT_FILENAME_SET = "input.filename.set";
    public static final String PROPERTY_INPUT_FILENAME = "input.filename";
    public static final String PROPERTY_OUTPUT_FILENAME_SET = "output.filename.set";
    public static final String PROPERTY_OUTPUT_FILENAME = "output.filename";
    public static final String PROPERTY_MONITOR_SET = "monitor.set";
    public static final String PROPERTY_MONITOR_SERVER_URL = "monitor.server.url";
    public static final String PROPERTY_EXPERIMENT_ID = "experiment.id";
    public static final String PROPERTY_EXPERIMENT_ITERATION = "experiment.iteration";
    // Option properties default values
    // (if default set option is false default value doesn't matter)
    protected static final boolean DEFAULT_SIMULATION_SET = false;
    protected static final boolean DEFAULT_SOURCE = false;
    protected static final boolean DEFAULT_SOURCE_URL_SET = false;
    protected static final String DEFAULT_SOURCE_URL = "127.0.0.1:51120";
    protected static final boolean DEFAULT_DEST_URL_SET = false;
    protected static final String DEFAULT_DEST_IP = "127.0.0.1";
    protected static final int DEFAULT_DEST_PORT = 51121;
    protected static final boolean DEFAULT_INPUT_FILENAME_SET = false;
    protected static final String DEFAULT_INPUT_FILENAME = "";
    protected static final boolean DEFAULT_OUTPUT_FILENAME_SET = false;
    protected static final String DEFAULT_OUTPUT_FILENAME = "";
    protected static final boolean DEFAULT_MONITOR_SET = false;
    protected static final String DEFAULT_MONITOR_SERVER_URL = "127.0.0.1:8080/video";
    protected static final int DEFAULT_EXPERIMENT_ID = 0;
    protected static final int DEFAULT_EXPERIMENT_ITERATION = 0;
    // Base
    public static final int BASE_CYCLE = 5 * 1000;
    // InterAS
    public static final int INTER_AS_SETS_EXCHANGE_PERIOD = BASE_CYCLE;
    public static final int INTER_AS_SETS_EXCHANGE_TIMEOUT = BASE_CYCLE;
    public static final int INTER_AS_MAX_NEIGHBOURS = 20;
    // Video
    public static final int VIDEO_WARMUP = 10; // to make sure source has connections
    public static final int VIDEO_CYCLE = BASE_CYCLE;
    // Video, neighbours
    public static final int VIDEO_MAX_OUT_CLOSE = 30;//10;
    public static final int VIDEO_MAX_OUT_RANDOM = 3;
    public static final int VIDEO_SOURCE_MAX_OUT_RANDOM = 5;//1;
    public static final int VIDEO_CLOSE_NEIGHBOUR_TIMEOUT = 10 * VIDEO_CYCLE;
    public static final int VIDEO_RANDOM_NEIGHBOUR_TIMEOUT = 10 * VIDEO_CYCLE;
    // Video, gossip
    public static final int VIDEO_PIECE_REQUEST_TIMEOUT = 3 * VIDEO_CYCLE;
    public static final int VIDEO_PIECE_REQUEST_TIMEOUT_MIN = 2 * VIDEO_CYCLE;
    public static final double VIDEO_PIECE_REQUEST_TIMEOUT_SCALE = 0.9;
    public static final int VIDEO_PIECE_REQUEST_RETRIES = 5;
    public static final int VIDEO_SOURCE_UPLOAD_CAPACITY = 5 * 5*14 * 1316;
    public static final int VIDEO_SOURCE_DEFAULT_FANOUT = 5;
    public static final int VIDEO_UPLOAD_CAPACITY = 5*2*14 * 1316;
    public static final int VIDEO_DEFAULT_FANOUT = 8;
    public static final int VIDEO_MAX_FANOUT = 20;
    public static final double VIDEO_MESSAGE_DROP_RATIO = 0.01;
    // Video, io, Forward Error Correction
    public static final int FEC_SUB_PIECES = 100; // sub-pieces per piece
    public static final int FEC_ENCODED_PIECES = 105; // sub-pieces plus extra pieces 
    public static final int VIDEO_MAX_BUFFER_SIZE = VIDEO_PIECE_REQUEST_RETRIES * VIDEO_PIECE_REQUEST_TIMEOUT;
    // OVERLAY-ID RESERVATIONS
    public static final int SYSTEM_OVERLAY_ID = VodConfig.SYSTEM_OVERLAY_ID; // System-croupier
    public static final int STUN_OVERLAY_ID = 2; // stunClient, stunServer
    public static final int HP_OVERLAY_ID = 3; // hpClient, zServer, ParentMaker
    public static final int MONITOR_OVERLAY_ID = 4; // monitorClient, monitorServer
    // keep these high until resolved
    public static final int INTER_AS_OVERLAY_ID = 1005;
    public static final int VIDEO_OVERLAY_ID = 1006;

    protected LSConfig(String[] args) throws IOException {
        super(args);
    }

    public static final synchronized LSConfig init(String[] args) throws IOException {

        if (singleton != null) {
            return (LSConfig) singleton;
        }

//        XMLDecoder decoder = null;
//        try {
//            decoder = new XMLDecoder(new BufferedInputStream(new FileInputStream(
//                    STARTUP_CONFIG_FILE)) //                    )
//                    );
//            Object obj = decoder.readObject();
//            if (obj == null) {
//                System.err.println("Configuration was null. Initializing new config.");
//                startupConfig = new StartupConfig(false, new VodAddressBean(), 0, 0);
//            } else {
//                startupConfig = (StartupConfig) obj;
//            }
//        } catch (FileNotFoundException e) {
//            logger.warn("No configuration found: " + STARTUP_CONFIG_FILE);
//            startupConfig = new StartupConfig(false, new VodAddressBean(), 0, 0);
//        } catch (Throwable e) {
//            logger.warn(e.toString());
//            startupConfig = new StartupConfig(false, new VodAddressBean(), 0, 0);
//        } finally {
//            if (decoder != null) {
//                decoder.close();
//            }
//        }

        singleton = new LSConfig(args);
        return (LSConfig) singleton;
    }

    @Override
    protected void parseAdditionalOptions(String[] args) throws IOException {

        // GENERAL
        seedOption = new Option("seed", true, "Random number seed");
        seedOption.setArgName("number");
        options.addOption(seedOption);
        

        // SIMULATION, EXPERIMENT
        simulationOption = new Option("sim", false, "Indicates simulation instance");
        options.addOption(simulationOption);

        // video streaming source
        sourceOption = new Option("source", false, "This node is the streaming source for the video");
        options.addOption(sourceOption);

        experimentIdOption = new Option("eid", true, "The unique identification number for this experiment");
        experimentIdOption.setArgName("id");
        options.addOption(experimentIdOption);

        experimentIterationOption = new Option("eit", true, "The iteration number of this experiment");
        experimentIterationOption.setArgName("iteration");
        options.addOption(experimentIterationOption);

        // HTTP Streaming
        sourceUrlOption = new Option("s", true, "URL to stream source");
        sourceUrlOption.setArgName("ip[:port][path]");
        options.addOption(sourceUrlOption);

        localHttpServerOption = new Option("d", true, "URL which to publish downloaded data to");
        localHttpServerOption.setArgName("host[:port]");
        options.addOption(localHttpServerOption);

        // File
        inputFilenameOption = new Option("i", true, "Path to the source file");
        inputFilenameOption.setArgName("path");
        options.addOption(inputFilenameOption);

        outputFilenameOption = new Option("o", true, "Path to output file destination");
        outputFilenameOption.setArgName("path");
        options.addOption(outputFilenameOption);

        // Monitoring
        monitorServerOption = new Option("m", true, "Turns on monitoring, data is sent to the specified location");
        monitorServerOption.setArgName("host[:port][path]");
        options.addOption(monitorServerOption);
    }

    @Override
    protected void processAdditionalOptions() throws IOException {
        /*
         * VALIDATION
         */
        if (line.hasOption(sourceUrlOption.getOpt()) && line.hasOption(inputFilenameOption.getOpt())) {
            help(new String[]{""},
                    "It is not possible to use 2 data inputs"
                    + " (" + sourceUrlOption.getOpt()
                    + " " + line.getOptionValue(sourceUrlOption.getOpt())
                    + " " + inputFilenameOption.getOpt()
                    + " " + line.getOptionValue(inputFilenameOption.getOpt())
                    + ")", options);
        }

        if (line.hasOption(simulationOption.getOpt())) {
            compositeConfig.setProperty(PROPERTY_SIMULATION_SET, true);
        }
        
        if (line.hasOption(sourceOption.getOpt())) {
            compositeConfig.setProperty(PROPERTY_SOURCE, true);
        }
        
        if (line.hasOption(experimentIdOption.getOpt())) {
            int id = Integer.parseInt(line.getOptionValue(experimentIdOption.getOpt()));
            compositeConfig.setProperty(PROPERTY_EXPERIMENT_ID, id);
        }
        if (line.hasOption(experimentIterationOption.getOpt())) {
            int iteration = Integer.parseInt(line.getOptionValue(experimentIterationOption.getOpt()));
            compositeConfig.setProperty(PROPERTY_EXPERIMENT_ITERATION, iteration);
        }

        /*
         * SOURCE HTTP SERVER
         */
        if (line.hasOption(sourceUrlOption.getOpt())) {
            String url = line.getOptionValue(sourceUrlOption.getOpt());
            String[] strs = url.split("[:/]");
//            if (!validIPv4format(strs[0])) {
//                help(new String[]{""},
//                        sourceUrlOption.getOpt() + " " + strs[0]
//                        + " is not a valid IPv4 address", options);
//            }
            if (url.contains(":")
                    && strs.length > 1) {
//                if (!validSourcePort(strs[1])) {
//                    help(new String[]{""}, sourceUrlOption.getOpt()
//                            + " " + strs[1]
//                            + " is not a valid port number", options);
//                }
            }
//            url = "http://" + url;
            compositeConfig.setProperty(PROPERTY_SOURCE_URL, url);
            compositeConfig.setProperty(PROPERTY_SOURCE_URL_SET, true);
        }

        /*
         * LOCAL HTTP SERVER
         */
        if (line.hasOption(localHttpServerOption.getOpt())) {
            String url = line.getOptionValue(localHttpServerOption.getOpt());
            String[] strs = url.split(":");
//            if (!validIPv4format(strs[0])) {
//                help(new String[]{""}, localHttpServerOption.getOpt()
//                        + " " + strs[0]
//                        + " is not a valid IPv4 address", options);
//            }

            compositeConfig.setProperty(PROPERTY_DEST_IP, url);
            if (strs.length > 2) {
//                if (!validLocalPort(strs[1])) {
//                    help(new String[]{""}, localHttpServerOption.getOpt()
//                            + " " + strs[1]
//                            + " is not a valid port number", options);
//                }
                compositeConfig.setProperty(PROPERTY_DEST_PORT, strs[2]);
            }
            compositeConfig.setProperty(PROPERTY_DEST_URL_SET, true);
        }

        /*
         * FILE
         */
        if (line.hasOption(inputFilenameOption.getOpt())) {
            String path = line.getOptionValue(inputFilenameOption.getOpt());
            if (!validLocalPath(path)) {
                help(new String[]{""}, inputFilenameOption.getOpt()
                        + " " + path
                        + " is not a valid local path", options);
            }
            compositeConfig.setProperty(PROPERTY_INPUT_FILENAME, path);
            compositeConfig.setProperty(PROPERTY_INPUT_FILENAME_SET, true);
        }

        if (line.hasOption(outputFilenameOption.getOpt())) {
            String path = line.getOptionValue(outputFilenameOption.getOpt());
            if (!validLocalPath(path)) {
                help(new String[]{""}, outputFilenameOption.getOpt()
                        + " " + path
                        + " is not a valid local path", options);
            }
            compositeConfig.setProperty(PROPERTY_OUTPUT_FILENAME, path);
            compositeConfig.setProperty(PROPERTY_OUTPUT_FILENAME_SET, true);
        }


        /*
         * MONITORING
         */
        if (line.hasOption(monitorServerOption.getOpt())) {
            String optValue = line.getOptionValue(monitorServerOption.getOpt());
            compositeConfig.setProperty(PROPERTY_MONITOR_SERVER_URL, optValue);
            compositeConfig.setProperty(PROPERTY_MONITOR_SET, true);
        }

        argList = line.getArgList();
        optArray = line.getOptions();
    }

    public static boolean isSimulation() {
        baseInitialized();
        return getCompositeConfiguration().getBoolean(PROPERTY_SIMULATION_SET, DEFAULT_SIMULATION_SET);
    }

    public static boolean isSource() {
        baseInitialized();
        return getCompositeConfiguration().getBoolean(PROPERTY_SOURCE, DEFAULT_SOURCE);
    }

    public static int getExperimentId() {
        baseInitialized();
        return getCompositeConfiguration().getInt(PROPERTY_EXPERIMENT_ID, DEFAULT_EXPERIMENT_ID);
    }

    public static int getExperimentIteration() {
        baseInitialized();
        return getCompositeConfiguration().getInt(PROPERTY_EXPERIMENT_ITERATION, DEFAULT_EXPERIMENT_ITERATION);
    }

    public static boolean hasSourceUrlSet() {
        baseInitialized();
        return getCompositeConfiguration().getBoolean(PROPERTY_SOURCE_URL_SET, DEFAULT_SOURCE_URL_SET);
    }

    public static String getSourceUrl() {
        baseInitialized();
        return getCompositeConfiguration().getString(PROPERTY_SOURCE_URL, DEFAULT_SOURCE_URL);
    }

    public static boolean hasDestUrlSet() {
        baseInitialized();
        return getCompositeConfiguration().getBoolean(PROPERTY_DEST_URL_SET, DEFAULT_DEST_URL_SET);
    }

    public static String getDestIp() {
        baseInitialized();
        return getCompositeConfiguration().getString(PROPERTY_DEST_IP, DEFAULT_DEST_IP);
    }

    public static int getDestPort() {
        baseInitialized();
        return getCompositeConfiguration().getInt(PROPERTY_DEST_PORT, DEFAULT_DEST_PORT);
    }

    public static boolean hasInputFileSet() {
        baseInitialized();
        return getCompositeConfiguration().getBoolean(PROPERTY_INPUT_FILENAME_SET, DEFAULT_INPUT_FILENAME_SET);
    }

    public static String getInputFilename() {
        baseInitialized();
        return getCompositeConfiguration().getString(PROPERTY_INPUT_FILENAME, DEFAULT_INPUT_FILENAME);
    }

    public static boolean hasOutputFileSet() {
        baseInitialized();
        return getCompositeConfiguration().getBoolean(PROPERTY_OUTPUT_FILENAME_SET, DEFAULT_OUTPUT_FILENAME_SET);
    }

    public static String getOutputFilename() {
        baseInitialized();
        return getCompositeConfiguration().getString(PROPERTY_OUTPUT_FILENAME, DEFAULT_OUTPUT_FILENAME);
    }

    public static boolean hasMonitorUrlSet() {
        baseInitialized();
        return getCompositeConfiguration().getBoolean(PROPERTY_MONITOR_SET, DEFAULT_MONITOR_SET);
    }

    public static String getMonitorServerUrl() {
        baseInitialized();
        return getCompositeConfiguration().getString(PROPERTY_MONITOR_SERVER_URL, DEFAULT_MONITOR_SERVER_URL);
    }

    /*
     * Misc help functions
     */
    private boolean validLocalPath(String path) {
        if (path.contains(" ")) {
            return false;
        }
        return true;
    }

    private boolean validUrl(String url) {
        // TODO: check path
        String uri = url.split("/")[0];
        return validUri(uri);
    }

    private boolean validUri(String uri) {
        String[] strs = uri.split(":");
        if (strs.length > 1) {
            if (!validLocalPort(strs[1])) {
                return false;
            }
        }
        return validIPv4format(strs[0]);
    }

    private boolean validIPv4format(String ip) {
        // Validate IPv4 address format
        String[] strs = ip.split("\\.");
        if (strs.length != 4) {
            return false;
        }
        for (String str : strs) {
            int i = Integer.valueOf(str);
            if (i < 0 || i > 255) {
                return false;
            }
        }
        return true;
    }

    // Restrictions on the source port are lower than those on the local port,
    // as we are only responsible for initiating http service on the local
    private boolean validSourcePort(String port) {
        // does not currently check usage/availability
        Integer i = Integer.valueOf(port);
        if (i == 80 || (i > 1024 && i < 65535)) {
            return true;
        }
        return false;
    }

    private boolean validLocalPort(String port) {
        Integer i = Integer.valueOf(port);
        if (i > 1024 && i < 65535) {
            return true;
        }
        return false;
    }

    /*
     * Printing
     */
    public static String getConfigString() {
        StringBuilder sb = new StringBuilder();
        sb.append(" -- LSConfig --");
        sb.append("\nvideo_home: ").append(GVOD_HOME);
        sb.append("\nseed: ").append(getSeed());
        sb.append("\nisSimulation: ").append(isSimulation());
        sb.append("\nisMonitoring: ").append(hasMonitorUrlSet());
        if (hasMonitorUrlSet()) {
            sb.append("\n | monitorServerUrl: ").append(getMonitorServerUrl());
            sb.append("\n | experimentId: ").append(getExperimentId());
            sb.append("\n | experimentIteration: ").append(getExperimentIteration());
        }
        sb.append("\nsourceUrlSet: ").append(hasSourceUrlSet());
        if (hasSourceUrlSet()) {
            sb.append("\n | sourceUrl: ").append(getSourceUrl());
        }
        sb.append("\nlocalHttpServerSet: ").append(hasDestUrlSet());
        if (hasDestUrlSet()) {
            sb.append("\n | localHttpServerIp: ").append(getDestIp());
            sb.append("\n | localHttpServerPort: ").append(getDestPort());
        }
        sb.append("\ninputFileSet: ").append(hasInputFileSet());
        if (hasInputFileSet()) {
            sb.append("\n | inputFilename: ").append(getInputFilename());
        }
        sb.append("\noutputFileSet: ").append(hasOutputFileSet());
        if (hasOutputFileSet()) {
            sb.append("\n | outputFilename: ").append(getOutputFilename());
        }

        return sb.toString();
    }

    public static String getCommandLineArgs() {
        baseInitialized();
        StringBuilder sb = new StringBuilder();
        for (Option o : optArray) {
            sb.append("-").append(o.getOpt()).append(" ");
            if (o.hasArg()) {
                sb.append(o.getValue()).append(" ");
            }
        }
        return sb.toString();
    }
}
