/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.system.swing;

import se.sics.gvod.system.swing.evts.UserInterfaceInit;
import se.sics.gvod.system.swing.evts.CheckTorrentsTimeout;
import java.awt.Image;
import java.awt.SystemTray;
import java.io.IOException;
import se.sics.gvod.system.peer.VodPeerPort;
import se.sics.gvod.system.peer.events.Quit;
import se.sics.gvod.common.util.bencode.BDecoder;
import se.sics.gvod.common.util.bencode.BEValue;
import se.sics.gvod.common.util.bencode.BEncoder;
import se.sics.gvod.common.util.bencode.InvalidBEncodingException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.ImageIcon;
import javax.swing.JProgressBar;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.gvod.config.VodConfig;
import se.sics.kompics.Component;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Positive;
import se.sics.gvod.address.Address;
import se.sics.gvod.bootstrap.port.BootstrapPort;
import se.sics.gvod.timer.Timer;
import se.sics.gvod.system.main.AppMainPort;
import se.sics.gvod.system.main.SwingMain;
import se.sics.gvod.system.util.ActiveTorrents;
import se.sics.gvod.timer.SchedulePeriodicTimeout;

/**
 *
 * @author jim
 */
public final class SwingComponent extends ComponentDefinition {

    private Logger logger = LoggerFactory.getLogger(SwingComponent.class.getName());
    Positive<AppMainPort> wrapperPort = positive(AppMainPort.class);
    Positive<BootstrapPort> bootstrap = positive(BootstrapPort.class);
    Positive<Timer> timer = positive(Timer.class);
    TrayUI trayUi;
    private boolean updatePercent = true;
    private String torrentDir;
    private final SwingComponent component;
    private SwingMain main;

    public SwingComponent() {
        System.out.println("interface");
        subscribe(handleInit, control);
        subscribe(handleCheckTorrentsTimeout, timer);
        component = this;
    }
    Handler<UserInterfaceInit> handleInit = new Handler<UserInterfaceInit>() {

        @Override
        public void handle(UserInterfaceInit event) {
            main = event.getMain();
            SchedulePeriodicTimeout spt = new SchedulePeriodicTimeout(
                    500, 500);
            spt.setTimeoutEvent(new CheckTorrentsTimeout(spt));
            trigger(spt, timer);

            try {
                String lookAndFeelClass = UIManager.getSystemLookAndFeelClassName();
//                        "com.sun.java.swing.plaf.gtk.GTKLookAndFeel"
//                        "com.seaglasslookandfeel.SeaGlassLookAndFeel"
                if (lookAndFeelClass != null) {
                    UIManager.setLookAndFeel(lookAndFeelClass);
                }

            } catch (UnsupportedLookAndFeelException ex) {
                ex.printStackTrace();
            } catch (IllegalAccessException ex) {
                ex.printStackTrace();
            } catch (InstantiationException ex) {
                ex.printStackTrace();
            } catch (ClassNotFoundException ex) {
                ex.printStackTrace();
            }

            //Check the SystemTray support
            if (!SystemTray.isSupported()) {
                throw new IllegalStateException("SystemTray is not supported");
            }

            trayUi = new TrayUI(createImage("if.png", "tray icon"),
                    component);
        }
    };
    Handler<CheckTorrentsTimeout> handleCheckTorrentsTimeout = new Handler<CheckTorrentsTimeout>() {

        @Override
        public void handle(CheckTorrentsTimeout event) {
        }
    };

    protected static Image createImage(String path, String description) {
        URL imageURL = SwingComponent.class.getResource(path);

        if (imageURL == null) {
            System.err.println("Resource not found: " + path);
            return null;
        } else {
            return (new ImageIcon(imageURL, description)).getImage();
        }
    }

    public void setFile(String path) {
//        trigger(new DataFileInfos(path), gvodPeer);
//        fileSet = true;
    }

    public void displayPercent(final int percent) {
        if (updatePercent) {
        }
    }

    public void setPercent(int percent) {
        System.out.println("set percent_pos : " + percent);
        updatePercent = true;
    }

    public void blockUpdatePercent() {
        updatePercent = false;
    }

    public boolean deactivate(String videoName) {
        int overlayId = ActiveTorrents.calculateVideoId(videoName);
        Component peer = ActiveTorrents.getPeer(overlayId);
        if (peer == null) {
            logger.warn("Couldn't find peer to deactivate");
            return false;
        } else {
            trigger(new Quit(overlayId), peer.getPositive(VodPeerPort.class));
            ActiveTorrents.removeTorrent(ActiveTorrents.getTorrentFilename(videoName));
        }

        String activeStreamsFilename = VodConfig.getTorrentIndexFile();
        try {

            ArrayList<String> activeStreams = new ArrayList<String>();
            File activeStreamsFile = new File(activeStreamsFilename);
            if (activeStreamsFile.exists()) {
                FileInputStream in = new FileInputStream(activeStreamsFile);
                BEValue val = (BEValue) new BDecoder(in).bdecodeMap().getMap().get("info");

                if (val == null) {
                    throw new InvalidBEncodingException("Missing info in "
                            + VodConfig.getTorrentIndexFile());
                }
                Map info = val.getMap();

                val = (BEValue) info.get("dataFiles");
                if (val == null) {
                    throw new InvalidBEncodingException("Missing name string");
                }
                List<BEValue> list = val.getList();

                for (BEValue value : list) {
                    activeStreams.add(value.getString());
                }

                activeStreams.remove(videoName);
                Map<String, Object> m = new HashMap<String, Object>();
                Map newinfo = new HashMap<String, Object>();
                newinfo.put("dataFiles", activeStreams);
                m.put("info", newinfo);
                FileOutputStream fos = new FileOutputStream(activeStreamsFilename);
                fos.write(BEncoder.bencode(m));
                fos.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Called by Swing components to create new Torrents.
     *
     * @param bootstrapIPAddress
     * @param bootPort
     * @param monitorIPAddress
     * @param monitorPort
     * @param videoFilename
     * @param videoDescription
     * @param imageUrl
     * @param width
     * @param height
     * @param error
     * @param progressBar
     * @return
     * @throws java.io.IOException
     */
    public boolean createStream(String bootstrapIPAddress, int bootPort,
            String monitorIPAddress, int monitorPort, String videoFilename,
            String videoDescription, String imageUrl,
            int width, int height,
            String error, JProgressBar progressBar) throws IOException {

        Address bootstrapServerAddress = null;
            bootstrapServerAddress = new Address(InetAddress.getByName(bootstrapIPAddress),
                    bootPort, Integer.MAX_VALUE);
        main.createStream(bootstrapServerAddress, videoFilename,
                width, height, videoDescription, imageUrl, progressBar);
        return true;

    }

    public String getBaseDir() {
        return torrentDir;
    }

    public Set<String> getTorrents() {
        return new HashSet<String>(ActiveTorrents.getListVideos());
    }

    public boolean removeTorrent(String torrentName) {

        // TODO -make this thread-safe
        return deactivate(torrentName);
    }

    public SwingMain getMain() {
        return main;
    }
}
