/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.bootstrap.config;

import java.io.IOException;
import org.apache.commons.cli.Option;
import se.sics.gvod.config.BaseCommandLineConfig;

/**
 *
 * @author jdowling
 */
public class BootstrapConfig extends BaseCommandLineConfig {

    public static final String PROP_MYSQL_PASSWORD = "mysql.passwd";
    public static final String PROP_MYSQL_USERNAME = "mysql.username";
    public static final String PROP_JDBC_URL = "mysql.jdbc.url";
    public static final String PROP_BOOTSTRAP_EVICT_AFTER = "bootstrap.evict.after";
    public static final String PROP_ASN_MATCHER = "asn.matcher";
    public static final String PROP_WEBSERVER_TORRENT_PATH = "torrents.path";
    public static final String PROP_WEBSEARCH_PORT = "websearch.port";
    public static final String DEFAULT_JDBC_URL = "jdbc:mysql://localhost/gvod";
    public static final String DEFAULT_MYSQL_USERNAME = "root";
    public static final String DEFAULT_MYSQL_PASSWORD = "";
    public static final String DEFAULT_WEBSERVER_TORRENT_PATH = "/var/www/gvod";
    
    public static final int DEFAULT_BOOTSTRAP_EVICT_PERIOD = 20 * 60 * 1000 /* 20 minutes*/;
    public static final int DEFAULT_NUM_NODES_RETURNED = 15;

    protected Option evictAfterOption;
    protected Option pwdOption;
    protected Option userOption;
    protected Option jdbcUrlOption;
    protected Option asnOption;
    protected Option torrentsOption;
    protected Option websearchOption;
    protected static boolean asnMatcher = false;

    protected BootstrapConfig(String[] args) throws IOException {
        super(args);
    }


    public static final synchronized BootstrapConfig init(String[] args) throws IOException {

        if (singleton != null) {
            return (BootstrapConfig) singleton;
        }
        singleton = new BootstrapConfig(args);
        return (BootstrapConfig) singleton;
    }


    @Override
    protected void parseAdditionalOptions(String[] args) throws IOException {

        pwdOption = new Option("pwd", true, "mysql passwd");
        pwdOption.setArgName("password");
        options.addOption(pwdOption);

        userOption = new Option("user", true, "mysql username");
        userOption.setArgName("user");
        options.addOption(userOption);
        
        websearchOption = new Option("websearch", true, "websearch port");
        websearchOption.setArgName("port");
        options.addOption(websearchOption);

        jdbcUrlOption = new Option("jdbcurl", true, "Change DB or hostname. Default: jdbc:mysql://localhost/gvod");
        jdbcUrlOption.setArgName("jdbcurl");
        options.addOption(jdbcUrlOption);

        evictAfterOption = new Option("evict", true,
                "evict after period with no heartbeat");
        evictAfterOption.setArgName("period");
        options.addOption(evictAfterOption);
        
        asnOption = new Option("asn", "use ASN numbers when matching peers");
        options.addOption(asnOption);

        torrentsOption = new Option("torrentspath", true, "webserver torrents path");
        torrentsOption.setArgName("torrentspath");
        options.addOption(torrentsOption);        
        
    }

    @Override
    protected void processAdditionalOptions() throws IOException {
        if (line.hasOption(pwdOption.getOpt())) {
            String passwd = line.getOptionValue(pwdOption.getOpt());
            compositeConfig.setProperty(PROP_MYSQL_PASSWORD, passwd);
        }

        if (line.hasOption(userOption.getOpt())) {
            String username = line.getOptionValue(userOption.getOpt());
            compositeConfig.setProperty(PROP_MYSQL_USERNAME, username);
        }

        if (line.hasOption(jdbcUrlOption.getOpt())) {
            String jdbcUrl = line.getOptionValue(jdbcUrlOption.getOpt());
            compositeConfig.setProperty(PROP_JDBC_URL, jdbcUrl);
        }

        if (line.hasOption(evictAfterOption.getOpt())) {
            int period = new Integer(line.getOptionValue(evictAfterOption.getOpt()));
            compositeConfig.setProperty(PROP_BOOTSTRAP_EVICT_AFTER, period);
        }

        if (line.hasOption(asnOption.getOpt())) {
            asnMatcher = true;
        }
        
        if (line.hasOption(torrentsOption.getOpt())) {
            String torrentsPath = line.getOptionValue(torrentsOption.getOpt());
            compositeConfig.setProperty(PROP_WEBSERVER_TORRENT_PATH, torrentsPath);
        }
        
        if (line.hasOption(websearchOption.getOpt())) {
            int port = new Integer(line.getOptionValue(websearchOption.getOpt()));
            compositeConfig.setProperty(PROP_WEBSEARCH_PORT, port);
        }        
    }

    public static String getMySqlPassword() {
        return getCompositeConfiguration().getString(PROP_MYSQL_PASSWORD, DEFAULT_MYSQL_PASSWORD);
    }

    public static String getMySqlUsername() {
        return getCompositeConfiguration().getString(PROP_MYSQL_USERNAME, DEFAULT_MYSQL_USERNAME);
    }

    public static String getJdbcUrl() {
        return getCompositeConfiguration().getString(PROP_JDBC_URL, DEFAULT_JDBC_URL);
    }

    public static int getBootstrapEvictPeriod() {
        baseInitialized();
        return getCompositeConfiguration().getInt(PROP_BOOTSTRAP_EVICT_AFTER,
                DEFAULT_BOOTSTRAP_EVICT_PERIOD);
    }
    
    public static int getWebSearchPort() {
        baseInitialized();
        return getCompositeConfiguration().getInt(PROP_WEBSEARCH_PORT,
                DEFAULT_BS_WEB_PORT);
    }

    public static boolean isAsnMatcher() {
        return asnMatcher;
    }
    
    public static String getWebServerTorrentsPath() {
        return getCompositeConfiguration().getString(PROP_WEBSERVER_TORRENT_PATH,
                DEFAULT_WEBSERVER_TORRENT_PATH);
    }
    
    
}
