package se.sics.gvod.config;

import se.sics.gvod.ls.system.LSConfig;

/**
 *
 * @author Jim
 */
public class VideoConfiguration 
        extends AbstractConfiguration<VideoConfiguration>
{

    /**
     * Fields cannot be private. Package protected, ok.
     */
    // simulation, experiment
    boolean simulationSet;
    int experimentId, experimentIteration;
    // http streaming
    boolean sourceUrlSet;
    String sourceUrl;
    boolean destUrlSet;
    String destIp;
    int destPort;
    // file
    boolean inputFilenameSet;
    String inputFilename;
    boolean outputFilenameSet;
    String outputFilename;
    // monitor
    boolean monitorSet;
    String monitorServerUrl;
    // other 
    long connectionRequestRto;
    int connectionRequestRetries;
    int maxPeerConnections;

    /**
     * Default constructor comes first.
     */
    public VideoConfiguration() {
        this(BaseCommandLineConfig.getSeed(), false, 0, 0,
                LSConfig.hasSourceUrlSet(), LSConfig.getSourceUrl(),
                LSConfig.hasDestUrlSet(), LSConfig.getDestIp(), LSConfig.getDestPort(),
                LSConfig.hasInputFileSet(), LSConfig.getInputFilename(),
                LSConfig.hasOutputFileSet(), LSConfig.getOutputFilename(),
                LSConfig.hasMonitorUrlSet(), LSConfig.getMonitorServerUrl(),
                5000, 1, 50
                );
    }
    
    /**
     * Full argument constructor comes second.
     */
    public VideoConfiguration(
            int seed,
            boolean simulation, int experimentId, int experimentIteration,
            boolean sourceUrlSet, String sourceUrl,
            boolean destUrlSet, String destIp, int destPort,
            boolean inputFilenameSet, String inputFilename,
            boolean outputFilenameSet, String outputFilename,
            boolean monitorSet, String monitorServerUrl,
            long connectionRequestRto, int connectionRequestRetries, int maxPeerConnections) {
        this.seed = seed;
        // simulation, experiment
        this.simulationSet = simulation;
        this.experimentId = experimentId;
        this.experimentIteration = experimentIteration;
        // http streaming
        this.sourceUrlSet = sourceUrlSet;
        this.sourceUrl = sourceUrl;
        this.destUrlSet = destUrlSet;
        this.destIp = destIp;
        this.destPort = destPort;
        // file
        this.inputFilenameSet = inputFilenameSet;
        this.inputFilename = inputFilename;
        this.outputFilenameSet = outputFilenameSet;
        this.outputFilename = outputFilename;
        // monitor
        this.monitorSet = monitorSet;
        this.monitorServerUrl = monitorServerUrl;
        // other
        this.connectionRequestRto = connectionRequestRto;
        this.connectionRequestRetries = connectionRequestRetries;
        this.maxPeerConnections = maxPeerConnections;
    }
    
    public static VideoConfiguration build() {
        return new VideoConfiguration();
    }

//    public void store(String file) throws IOException {
//        Properties p = new Properties();
//        p.setProperty("seed", "" + seed);
//        // simulation, experiment
//        p.setProperty(LSConfig.PROPERTY_SIMULATION_SET, ""+simulationSet);
//        p.setProperty(LSConfig.PROPERTY_EXPERIMENT_ID, ""+experimentId);
//        p.setProperty(LSConfig.PROPERTY_EXPERIMENT_ITERATION, ""+experimentIteration);
//        // http streaming
//        p.setProperty(LSConfig.PROPERTY_SOURCE_URL_SET, ""+sourceUrlSet);
//        p.setProperty(LSConfig.PROPERTY_SOURCE_URL, sourceUrl);
//        p.setProperty(LSConfig.PROPERTY_DEST_URL_SET, ""+destUrlSet);
//        p.setProperty(LSConfig.PROPERTY_DEST_IP, destIp);
//        p.setProperty(LSConfig.PROPERTY_DEST_PORT, ""+destPort);
//        // file
//        p.setProperty(LSConfig.PROPERTY_INPUT_FILENAME_SET, ""+inputFilenameSet);
//        p.setProperty(LSConfig.PROPERTY_INPUT_FILENAME, inputFilename);
//        p.setProperty(LSConfig.PROPERTY_OUTPUT_FILENAME_SET, ""+outputFilenameSet);
//        p.setProperty(LSConfig.PROPERTY_OUTPUT_FILENAME, outputFilename);
//        // monitor
//        p.setProperty(LSConfig.PROPERTY_MONITOR_SET, ""+monitorSet);
//        p.setProperty(LSConfig.PROPERTY_MONITOR_SERVER_URL, monitorServerUrl);
//        // other
//        p.setProperty("connectionRequestRto", "" + connectionRequestRto);
//        p.setProperty("connectionRequestRetries", "" + connectionRequestRetries);
//        p.setProperty("maxPeerConnections", "" + maxPeerConnections);
//        Writer writer = new FileWriter(file);
//        p.store(writer, "se.sics.gvod.ls.video");
//    }
//
//    public static VideoConfiguration load(String file) throws IOException {
//        Properties p = new Properties();
//        Reader reader = new FileReader(file);
//        p.load(reader);
//
//        int seed = Integer.parseInt(p.getProperty("seed"));
//        // simulation, experiment
//        boolean simulationSet = Boolean.parseBoolean(p.getProperty(LSConfig.PROPERTY_SIMULATION_SET));
//        int experimentId = Integer.parseInt(p.getProperty(LSConfig.PROPERTY_EXPERIMENT_ID));
//        int experimentIteration = Integer.parseInt(p.getProperty(LSConfig.PROPERTY_EXPERIMENT_ITERATION));
//        // http streaming
//        boolean sourceUrlSet = Boolean.parseBoolean(p.getProperty(LSConfig.PROPERTY_SOURCE_URL_SET));
//        String sourceUrl = p.getProperty(LSConfig.PROPERTY_SOURCE_URL);
//        boolean destUrlSet = Boolean.parseBoolean(p.getProperty(LSConfig.PROPERTY_DEST_URL_SET));
//        String destIp = p.getProperty(LSConfig.PROPERTY_DEST_IP);
//        int destPort = Integer.valueOf(p.getProperty(LSConfig.PROPERTY_DEST_PORT));
//        // file
//        boolean inputFilenameSet = Boolean.parseBoolean(p.getProperty(LSConfig.PROPERTY_INPUT_FILENAME_SET));
//        String inputFilename = p.getProperty(LSConfig.PROPERTY_INPUT_FILENAME);
//        boolean outputFilenameSet = Boolean.parseBoolean(p.getProperty(LSConfig.PROPERTY_OUTPUT_FILENAME_SET));
//        String outputFilename = p.getProperty(LSConfig.PROPERTY_OUTPUT_FILENAME);
//        // monitor
//        boolean monitorSet = Boolean.parseBoolean(p.getProperty(LSConfig.PROPERTY_MONITOR_SET));
//        String monitorServerUri = p.getProperty(LSConfig.PROPERTY_MONITOR_SERVER_URL);
//        // other
//        long connectionRequestRto = Long.parseLong(p.getProperty("connectionRequestRto"));
//        int connectionRequestRetries = Integer.parseInt(p.getProperty("connectionRequestRetries"));
//        int maxPeerConnections = Integer.parseInt(p.getProperty("maxPeerConnections"));
//        return new VideoConfiguration(seed,simulationSet, experimentId, experimentIteration,
//                sourceUrlSet, sourceUrl, destUrlSet, destIp, destPort,
//                inputFilenameSet, inputFilename, outputFilenameSet, outputFilename,
//                monitorSet, monitorServerUri,
//                connectionRequestRto,connectionRequestRetries,maxPeerConnections);
//    }
//
//    public int getSeed() {
//        return seed;
//    }

    public int getConnectionRequestRetries() {
        return connectionRequestRetries;
    }

    public long getConnectionRequestRto() {
        return connectionRequestRto;
    }

    public int getMaxPeerConnections() {
        return maxPeerConnections;
    }

    public String getDestIp() {
        return destIp;
    }

    public int getDestPort() {
        return destPort;
    }

    public boolean isDestUrlSet() {
        return destUrlSet;
    }

    public int getExperimentId() {
        return experimentId;
    }

    public int getExperimentIteration() {
        return experimentIteration;
    }

    public String getInputFilename() {
        return inputFilename;
    }

    public boolean isInputFilenameSet() {
        return inputFilenameSet;
    }

    public String getMonitorServerUrl() {
        return monitorServerUrl;
    }

    public boolean isMonitorSet() {
        return monitorSet;
    }

    public String getOutputFilename() {
        return outputFilename;
    }

    public boolean isOutputFilenameSet() {
        return outputFilenameSet;
    }

    public boolean isSimulationSet() {
        return simulationSet;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public boolean isSourceUrlSet() {
        return sourceUrlSet;
    }

    public VideoConfiguration setSimulationSet(boolean simulationSet) {
        this.simulationSet = simulationSet;
        return this;
    }

    public VideoConfiguration setExperimentId(int experimentId) {
        this.experimentId = experimentId;
        return this;
    }

    public VideoConfiguration setExperimentIteration(int experimentIteration) {
        this.experimentIteration = experimentIteration;
        return this;
    }

    public VideoConfiguration setSourceUrlSet(boolean sourceUrlSet) {
        this.sourceUrlSet = sourceUrlSet;
        return this;
    }

    public VideoConfiguration setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
        return this;
    }

    public VideoConfiguration setDestUrlSet(boolean destUrlSet) {
        this.destUrlSet = destUrlSet;
        return this;
    }

    public VideoConfiguration setDestIp(String destIp) {
        this.destIp = destIp;
        return this;
    }

    public VideoConfiguration setDestPort(int destPort) {
        this.destPort = destPort;
        return this;
    }

    public VideoConfiguration setInputFilenameSet(boolean inputFilenameSet) {
        this.inputFilenameSet = inputFilenameSet;
        return this;
    }

    public VideoConfiguration setInputFilename(String inputFilename) {
        this.inputFilename = inputFilename;
        return this;
    }

    public VideoConfiguration setOutputFilenameSet(boolean outputFilenameSet) {
        this.outputFilenameSet = outputFilenameSet;
        return this;
    }

    public VideoConfiguration setOutputFilename(String outputFilename) {
        this.outputFilename = outputFilename;
        return this;
    }

    public VideoConfiguration setMonitorSet(boolean monitorSet) {
        this.monitorSet = monitorSet;
        return this;
    }

    public VideoConfiguration setMonitorServerUrl(String monitorServerUrl) {
        this.monitorServerUrl = monitorServerUrl;
        return this;
    }

    public VideoConfiguration setConnectionRequestRto(long connectionRequestRto) {
        this.connectionRequestRto = connectionRequestRto;
        return this;
    }

    public VideoConfiguration setConnectionRequestRetries(int connectionRequestRetries) {
        this.connectionRequestRetries = connectionRequestRetries;
        return this;
    }

    public VideoConfiguration setMaxPeerConnections(int maxPeerConnections) {
        this.maxPeerConnections = maxPeerConnections;
        return this;
    }
}
