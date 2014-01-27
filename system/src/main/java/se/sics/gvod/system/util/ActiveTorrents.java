/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.system.util;

import java.awt.EventQueue;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.slf4j.LoggerFactory;
import se.sics.gvod.config.VodConfig;
import se.sics.gvod.common.util.bencode.BDecoder;
import se.sics.gvod.common.util.bencode.BEValue;
import se.sics.gvod.common.util.bencode.BEncoder;
import se.sics.gvod.common.util.bencode.InvalidBEncodingException;
import se.sics.gvod.system.storage.MetaInfoExec;
import se.sics.gvod.system.main.GMain;
import se.sics.gvod.system.main.SwingMain;
import se.sics.kompics.Component;

/**
 * This class manages the set of torrent files, the video files, and a torrent
 * index file. It also stores the executing peer-ids and references to their
 * components.
 *
 * Invoke init() method before calling any other static methods on this
 * Singleton.
 *
 * @author jdowling
 */
public class ActiveTorrents {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(ActiveTorrents.class);
    
    public static final String PEER_STARTED = "peerStarted";
    public static final String PEER_STOPPED = "peerStopped";
    public static final String PEER_ALREADY_RUNNING = "peerAlreadyRunning";
    public static final String TORRENT_UPDATED = "updatedTorrent";
    public static final String TORRENT_ADDED = "addedTorrent";
    public static final String TORRENT_REMOVED = "removeTorrent";
    public static final String TORRENT_ALREADY_ADDED = "torrentAlreadyAdded";
    public static final String TORRENT_AND_DATA_REMOVED = "removeTorrentAndData";
    public static final String SEED_ADDED = "addedSeed";
    public static final String SEED_BECOME = "becomeSeed";
    private Map<String, String> mapTorrentVideo = new HashMap<String, String>();
    private Map<String, String> mapVideoTorrent = new HashMap<String, String>();
    private Map<Integer, Component> mapIdPeer = new HashMap<Integer, Component>();

    /**
     * torrentFilename, TorrentEntry
     */
    private Map<String, TorrentEntry> mapTorrentEntries
            = new HashMap<String, TorrentEntry>();
    private List<TorrentEntry> listTorrentEntries
            = new ArrayList<TorrentEntry>();
    /**
     * Singleton
     */
    private static ActiveTorrents instance = null;
    /**
     * Used to inform swing components about updates
     */
    private final PropertyChangeSupport support;
    private GMain main;

    private class TorrentVideoSize {

        private final String videoName;
        private final int width;
        private final int height;

        public TorrentVideoSize(String videoName, int width, int height) {
            this.videoName = videoName;
            this.width = width;
            this.height = height;
        }

        public String getVideoName() {
            return videoName;
        }

        public int getHeight() {
            return height;
        }

        public int getWidth() {
            return width;
        }
    }

    public static class TorrentEntry {

        private final String videoName;
        private final String torrentFilename;
        private String percent;
        private final int width;
        private final int height;
        private final long size;
        private boolean seed;
        private boolean running;

        public TorrentEntry(String videoName, String torrentFilename,
                int width, int height,
                long size,
                String percent, boolean seed, boolean running) {
            if (videoName == null || torrentFilename == null) {
                throw new NullPointerException();
            }
            this.videoName = videoName;
            this.torrentFilename = torrentFilename;
            this.width = width;
            this.height = height;
            this.size = size;
            this.percent = percent;
            this.seed = seed;
            this.running = running;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        public long getSize() {
            return size;
        }

        public boolean isRunning() {
            return running;
        }

        public void setRunning(boolean running) {
            this.running = running;
        }

        public boolean isSeed() {
            return seed;
        }

        public String getPercent() {
            return percent;
        }

        public String getTorrentFilename() {
            return torrentFilename;
        }

        public String getVideoName() {
            return videoName;
        }

        public void setPercent(String percent) {
            this.percent = percent;
        }

        public void setSeed(boolean seed) {
            this.seed = seed;
        }
    }

    private ActiveTorrents() {
        support = new PropertyChangeSupport(this);
    }

    public synchronized static void init(GMain main) {
        if (instance == null) {
            instance = new ActiveTorrents();
        }
        instance.main = main;
    }

    private static ActiveTorrents getInstance() {
        if (instance == null) {
            instance = new ActiveTorrents();
        }
        return instance;
    }

    public synchronized static void addPropertyChangeListener(PropertyChangeListener listener) {
        getInstance().addPropertyChangeListenerP(listener);
    }

    private void addPropertyChangeListenerP(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public synchronized static void removePropertyChangeListener(PropertyChangeListener listener) {
        getInstance().removePropertyChangeListenerP(listener);
    }

    private void removePropertyChangeListenerP(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

    public synchronized static void addPropertyChangeListener(String property,
            PropertyChangeListener listener) {
        getInstance().addPropertyChangeListenerP(property, listener);
    }

    private void addPropertyChangeListenerP(String property,
            PropertyChangeListener listener) {
        support.addPropertyChangeListener(property, listener);
    }

    protected void firePropertyChange(final String key, final Object oldValue,
            final Object newValue) {
        if (newValue != null && VodConfig.GUI && support != null) {
            try {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    support.firePropertyChange(key, oldValue, newValue);
                }
            });
            } catch(Throwable t) {
                logger.warn(t.getMessage());
            }
        }
    }

    public static Integer calculateVideoId(String videoName) {
        if (videoName == null) {
            return null;
        }
        // TODO - This is broken
//        Integer videoId = null;
//        int c = 1;
//        while (videoId == null) {
//            byte[] hash = getChunkHashes(c);
//            videoId = ActiveTorrents.byteArrayToInt(hash);
//            c++;
//        }
//        id = videoId;
        return videoName.hashCode();
    }

    /**
     *
     * @param id
     * @param torrentFilename
     * @param peer
     * @return videoName
     * @throws ActiveTorrentsException
     */
    public synchronized static String addTorrent(String torrentFilename,
            Component peer, boolean seeder)
            throws ActiveTorrentsException {
        return getInstance().addTorrentP(torrentFilename, peer, seeder);
    }

    private String addTorrentP(String torrentFilename,
            Component peer, boolean seeder)
            throws ActiveTorrentsException {
        if (torrentFilename == null || peer == null) {
            throw new NullPointerException("Either torrentFilename or peer was null. "
                    + "torrentFilename was "
                    + torrentFilename);
        }

        File f = new File(torrentFilename);
        if (f.exists() == false || f.isFile() == false) {
            return null;
        }
        String videoName = addTorrent(torrentFilename);
        Integer videoId = calculateVideoId(videoName);

        mapIdPeer.put(videoId, peer);
//        firePropertyChange(PEER_STARTED, );

        TorrentEntry entry = null;
        if (seeder) {
            TorrentEntry oldEntry = mapTorrentEntries.get(torrentFilename);
            entry = new TorrentEntry(oldEntry.getVideoName(),
                    oldEntry.getTorrentFilename(),
                    oldEntry.getWidth(),
                    oldEntry.getHeight(),
                    oldEntry.getSize(),
                    oldEntry.getPercent(), true,
                    oldEntry.isRunning());
            firePropertyChange(SEED_ADDED, oldEntry, entry);
            updatePercentage(oldEntry.getVideoName(), "100");
        }
        return videoName;
    }

    /**
     *
     * @param torrentFilename as a string
     * @return name of video file for torrent.
     * @throws ActiveTorrentsException
     */
    public synchronized static String addTorrent(String torrentFilename)
            throws ActiveTorrentsException {
        return getInstance().addTorrentP(torrentFilename);
    }

    private String addTorrentP(String torrentFilename)
            throws ActiveTorrentsException {
        if (torrentFilename == null) {
            throw new NullPointerException("torrentFilename was null. ");
        }
        if (mapTorrentVideo.containsKey(torrentFilename)) {
            firePropertyChange(TORRENT_ALREADY_ADDED,
                    mapTorrentEntries.get(torrentFilename),
                    mapTorrentEntries.get(torrentFilename));
            return mapTorrentVideo.get(torrentFilename);
        }
        File f = new File(torrentFilename);
        if (f.exists() == false || f.isFile() == false) {
            return null;
        }

        long sizeInBytes = f.length();
        TorrentVideoSize videoInfo = extractVideoNameSizeFromTorrentFile(torrentFilename);
        String videoName = videoInfo.getVideoName();
        mapTorrentVideo.put(torrentFilename, videoName);
        mapVideoTorrent.put(videoName, torrentFilename);

        TorrentEntry entry = new TorrentEntry(videoName, torrentFilename,
                videoInfo.getWidth(),
                videoInfo.getHeight(),
                sizeInBytes,
                "0", false, true);
        mapTorrentEntries.put(torrentFilename, entry);
        listTorrentEntries.add(entry);

        firePropertyChange(TORRENT_ADDED, null, entry);
        return videoName;
    }

    private synchronized static TorrentVideoSize extractVideoNameSizeFromTorrentFile(String torrentFilename)
            throws ActiveTorrentsException {
        return getInstance().extractVideoNameSizeFromTorrentFileP(torrentFilename);
    }

    private TorrentVideoSize extractVideoNameSizeFromTorrentFileP(String torrentFilename)
            throws ActiveTorrentsException {
        File torrentFile = new File(torrentFilename);
        if (!torrentFile.exists()) {
            throw new ActiveTorrentsException("Torrent file cannot be added, as it doesn't exist: "
                    + torrentFile.getAbsolutePath());
        }
        String videoName;
        int width, height;
        try {
            FileInputStream fis = new FileInputStream(torrentFile);
            MetaInfoExec metaInfo;
            metaInfo = new MetaInfoExec(fis, torrentFile.getAbsolutePath());
            videoName = metaInfo.getName();
            width = metaInfo.getWidth();
            height = metaInfo.getHeight();
        } catch (IOException ex) {
            throw new ActiveTorrentsException(ex.getMessage());
        }
        return new TorrentVideoSize(videoName, width, height);
    }

    /**
     *
     * @param torrentFilename
     * @return null if not found, else video file.
     */
    public synchronized static File getVideoFile(String torrentFilename) {
        return getInstance().getVideoFileP(torrentFilename);
    }

    private File getVideoFileP(String torrentFilename) {
        String videoName = mapTorrentVideo.get(torrentFilename);
        return (videoName == null) ? null : new File(videoName);
    }

    /**
     *
     * @param torrentFilename
     * @return null if not found, else video filename
     */
    public synchronized static String getVideoFilename(String torrentFilename) {
        return getInstance().getVideoFilenameP(torrentFilename);
    }

    private String getVideoFilenameP(String torrentFilename) {
        return mapTorrentVideo.get(torrentFilename);
    }

    /**
     *
     * @param video
     * @return null if not found, else the torrent file
     */
    public synchronized static File getTorrentFile(String video) {
        return getInstance().getTorrentFileP(video);
    }

    private File getTorrentFileP(String video) {
        String torrentFilename = mapVideoTorrent.get(video);
        return (torrentFilename == null) ? null : new File(torrentFilename);
    }

    /**
     *
     * @param video
     * @return null if not found, else the torrent filename.
     */
    public synchronized static String getTorrentFilename(String video) {
        return getInstance().getTorrentFilenameP(video);
    }

    private String getTorrentFilenameP(String video) {
        return mapVideoTorrent.get(video);
    }

    /**
     *
     * @return a list of all the names of videos
     */
    public synchronized static List<String> getListVideos() {
        return getInstance().getListVideosP();
    }

    private List<String> getListVideosP() {
        return new ArrayList<String>(mapVideoTorrent.keySet());
    }

    /**
     *
     * @return a list of the torrent filenames.
     */
    public synchronized static List<String> getListTorrents() {
        return getInstance().getListTorrentsP();
    }

    private List<String> getListTorrentsP() {
        return new ArrayList<String>(mapTorrentVideo.keySet());
    }

    /**
     * Scans the torrentDir for all .data torrent files. Removes all torrent
     * files currently in ActiveTorrents that are not found in the
     * torrent-directory, and adds any new torrent files found in the
     * torrent-directory.
     *
     * @return set of torrent ids in ActiveTorrents that have been removed
     * @throws IOException
     */
    public synchronized static Set<Integer> updateTorrentLibrary() throws IOException {
        return getInstance().updateTorrentLibraryP();
    }

    // TODO - i don't check to make sure the video files for the corresponding
    // torrent files exist.
    private Set<Integer> updateTorrentLibraryP() throws IOException {
        File folder = new File(VodConfig.getTorrentDir());

        File[] listOfFiles = folder.listFiles();
        if (listOfFiles == null) {
            throw new IOException("Invalid file dir for torrents");
        }

        Set<String> foundFiles = new HashSet<String>();

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                String filename = listOfFiles[i].getCanonicalPath();
                if (FileUtils.getPostFix(filename).compareToIgnoreCase(
                        VodConfig.TORRENT_FILE_POSTFIX) == 0) {
                    foundFiles.add(filename);
                }
            }
        }

        Set<String> deletedTorrents = mapTorrentVideo.keySet();
        Set<String> existingTorrents = mapTorrentVideo.keySet();

        deletedTorrents.removeAll(foundFiles);

        Set<Integer> idsToKill = new HashSet<Integer>();

        for (String deleted : deletedTorrents) {
            String video = mapTorrentVideo.remove(deleted);
            mapVideoTorrent.remove(video);
            Integer id = ActiveTorrents.calculateVideoId(video); //mapVideoId.remove(video);
            if (id != null) {
                idsToKill.add(id);
                Component peer = mapIdPeer.remove(id);
                if (peer == null) {
                    // TODO
                }
            }

            TorrentEntry entry = mapTorrentEntries.remove(deleted);
            if (entry != null) {
                listTorrentEntries.remove(entry);
            }

            firePropertyChange(TORRENT_REMOVED, entry, null);
        }

        foundFiles.removeAll(existingTorrents);
        for (String addedTorrentFilename : foundFiles) {
            TorrentVideoSize videoInfo = extractVideoNameSizeFromTorrentFile(addedTorrentFilename);
            String videoName = videoInfo.getVideoName();
            int width = videoInfo.getWidth();
            int height = videoInfo.getHeight();

            File f = new File(addedTorrentFilename);
            if (f.exists() == false || f.isFile() == false) {
                continue;
            }
            long size = f.length();
            mapTorrentVideo.put(addedTorrentFilename, videoName);
            mapVideoTorrent.put(videoName, addedTorrentFilename);
            // TODO - need to know whether the file read is a seed or not?
            TorrentEntry entry = new TorrentEntry(videoName, addedTorrentFilename,
                    width, height,
                    size, "100", false, true);
            mapTorrentEntries.put(addedTorrentFilename, entry);
            listTorrentEntries.add(entry);
            firePropertyChange(TORRENT_ADDED, null, entry);
        }

        return idsToKill;
    }

    public synchronized static void makeSeeder(String torrentFilename)
            throws ActiveTorrentsException {
        getInstance().makeSeederP(torrentFilename);
    }

    private void makeSeederP(String torrentFilename)
            throws ActiveTorrentsException {
        TorrentEntry entry = mapTorrentEntries.get(torrentFilename);
        if (entry == null) {
            throw new ActiveTorrentsException("Not found: {} when trying to make a seeder.");
        }
        entry.setSeed(true);
        writeTorrentIndexFile();
        firePropertyChange(SEED_BECOME, mapTorrentVideo.get(torrentFilename), torrentFilename);
    }

    /**
     *
     * @return @throws IOException
     */
    private boolean writeTorrentIndexFile()
            throws ActiveTorrentsException {

        File activeStreamsFile = new File(VodConfig.getTorrentIndexFile());
        if (activeStreamsFile.exists()) {
            activeStreamsFile.delete();
        }

        FileOutputStream fos = null;
        boolean created = false;
        try {
            List<String> torrents = new ArrayList<String>();
            for (TorrentEntry t : listTorrentEntries) {
                torrents.add(t.getTorrentFilename());
            }

            Map<String, Object> m = new HashMap<String, Object>();
            Map info = new HashMap<String, Object>();
            info.put("dataFiles", torrents);
            m.put("info", info);
            File indexFile = new File(VodConfig.getTorrentIndexFile());
            created = indexFile.createNewFile();
            fos = new FileOutputStream(indexFile);
            fos.write(BEncoder.bencode(m));
        } catch (IOException ex) {
            throw new ActiveTorrentsException(ex.getMessage());
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(ActiveTorrents.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return created;
    }

    /**
     * Scans the activeStreams files for the list of cached torrent files.
     *
     * @return
     */
    public synchronized static Set<String> getTorrentsUsingIndexFile() {
        return getInstance().getTorrentsUsingIndexFileP();
    }

    private Set<String> getTorrentsUsingIndexFileP() {
        Set<String> torrents = new HashSet<String>();

        File activeStreamsFile = new File(VodConfig.getTorrentIndexFile());
        if (activeStreamsFile.exists()) {
            FileInputStream in = null;
            try {
                in = new FileInputStream(activeStreamsFile);
                BEValue val = (BEValue) new BDecoder(in).bdecodeMap().getMap().get("info");
                if (val == null) {
                    // file is corrupt, rebuild by scanning directory
                    in.close();
                    in = null;
                    activeStreamsFile.delete();
                    updateTorrentLibrary();
                    torrents = mapTorrentVideo.keySet();
                } else {
                    Map info = val.getMap();
                    val = (BEValue) info.get("dataFiles");
                    if (val == null) {
                        in.close();
                        in = null;
                        activeStreamsFile.delete();
                        updateTorrentLibrary();
                        torrents = mapTorrentVideo.keySet();
                    } else {
                        List<BEValue> list = val.getList();
                        for (BEValue value : list) {
                            String torrent = value.getString();
                            torrents.add(torrent);
                        }
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(ActiveTorrents.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                } catch (IOException ex) {
                    Logger.getLogger(ActiveTorrents.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        Set<String> failedTorrents = new HashSet<String>();
        for (String t : torrents) {
            try {
                if (addTorrentP(t) == null) {
                    failedTorrents.add(t);
                }
            } catch (ActiveTorrentsException ex) {
                Logger.getLogger(ActiveTorrents.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        torrents.removeAll(failedTorrents);

        return torrents;
    }

    public synchronized static boolean peerAlreadyRunning(String torrentFilename) {
        return getInstance().peerAlreadyRunningP(torrentFilename);
    }

    private boolean peerAlreadyRunningP(String torrentFilename) {
        String video = mapTorrentVideo.get(torrentFilename);
        if (video == null) {
            return false;
        }
        Integer id = ActiveTorrents.calculateVideoId(video);
        if (id == null) {
            return false;
        }
        if (mapIdPeer.get(id) != null) {
            firePropertyChange(PEER_ALREADY_RUNNING, video, id);
            return true;
        }
        return false;
    }

    public synchronized static Component getPeer(int id) {
        return getInstance().getPeerP(id);
    }

    private Component getPeerP(int id) {
        return mapIdPeer.get(id);
    }

    public synchronized static Component getPeerFromVideo(String videoFilename) {
        return getInstance().getPeerFromVideoP(videoFilename);
    }

    private Component getPeerFromVideoP(String videoFilename) {
        Integer id = ActiveTorrents.calculateVideoId(videoFilename);
        if (id != null) {
            return mapIdPeer.get(id);
        }
        return null;
    }

    public synchronized static Component getPeerFromTorrent(String torrentFilename) {
        return getInstance().getPeerFromTorrentP(torrentFilename);
    }

    private Component getPeerFromTorrentP(String torrentFilename) {
        String videoFilename = mapTorrentVideo.get(torrentFilename);
        if (videoFilename != null) {
            Integer id = ActiveTorrents.calculateVideoId(videoFilename);
            if (id != null) {
                return mapIdPeer.get(id);
            }
        }
        return null;
    }

    public synchronized static boolean existsAndValidTorrentFile(String torrentFilename) {
        return getInstance().existsAndValidTorrentFileP(torrentFilename);
    }

    private boolean existsAndValidTorrentFileP(String torrentFilename) {
        File f = new File(torrentFilename);
        if (!f.exists()) {
            return false;
        }
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(f);
            new MetaInfoExec(fis, torrentFilename);
        } catch (InvalidBEncodingException ex) {
            return false;
        } catch (IOException ex) {
            return false;
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException ex) {
                    Logger.getLogger(ActiveTorrents.class.getName()).log(Level.SEVERE, null, ex);
                    return false;
                }
            }
        }

        return true;
    }

    public synchronized static boolean removeTorrent(String torrentFilename) {
        return getInstance().removeTorrentP(torrentFilename);
    }

    private boolean removeTorrentP(String torrentFilename) {
        boolean success = false;
        String videoName = mapTorrentVideo.remove(torrentFilename);
        if (videoName == null) {
            return false;
        }
        TorrentEntry entry = mapTorrentEntries.remove(torrentFilename);
        if (entry != null) {
            listTorrentEntries.remove(entry);
        } else {
            Logger.getLogger(ActiveTorrents.class.getName()).log(Level.SEVERE, "Could not find torrent to delete : {0}", torrentFilename);
        }
        Integer peerId = ActiveTorrents.calculateVideoId(videoName);
        if (peerId == null) {
            Logger.getLogger(ActiveTorrents.class.getName()).log(Level.SEVERE, "Could not find id for torrentfile when deleting : {0}", torrentFilename);
        } else {
            Component peer = mapIdPeer.get(peerId);
            if (peer != null) {
                main.stopPeer(peer);
                success = true;
            } else {
                Logger.getLogger(ActiveTorrents.class.getName()).log(Level.SEVERE, "Could not find peer for torrentfile when deleting : {0}", torrentFilename);
            }
        }

        try {
            // Remove torrent file (.data) and .data.pieces
            org.apache.commons.io.FileUtils.forceDelete(new File(torrentFilename));
            org.apache.commons.io.FileUtils.forceDelete(new File(torrentFilename + ".pieces"));
        } catch (IOException ex) {
            Logger.getLogger(ActiveTorrents.class.getName()).log(Level.SEVERE, null, ex);
            success = false;
        }
        try {
            // remove torrent from activeStreams library
            writeTorrentIndexFile();
        } catch (ActiveTorrentsException ex) {
            Logger.getLogger(ActiveTorrents.class.getName()).log(Level.SEVERE, null, ex);
            success = false;
        }

        firePropertyChange(TORRENT_REMOVED, entry, null);
        return success;
    }

//    public synchronized static boolean removePeer(int id) {
//        return getInstance().removePeerP(id);
//    }
//
//    private boolean removePeerP(int id) {
//        String videoToRemove = null;
//        for (String video : mapVideoTorrent.keySet()) {
//            if (mapVideoId.get(video) == id) {
//                videoToRemove = video;
//                break;
//            }
//        }
//        if (videoToRemove == null) {
//            throw new IllegalStateException("Should have been a video for this id :" + id);
//        }
//        mapVideoId.remove(videoToRemove);
//
//
//        String torrentToRemove = null;
//        for (String torrent : mapTorrentVideo.keySet()) {
//            if (mapTorrentVideo.get(torrent).compareTo(videoToRemove) == 0) {
//                torrentToRemove = torrent;
//                break;
//            }
//        }
//        TorrentEntry entry = mapTorrentEntries.get(torrentToRemove);
//
//        firePropertyChange(TORRENT_REMOVED, entry, null);
////        firePropertyChange(TORRENT_REMOVED, videoToRemove,
////                mapVideoId.get(videoToRemove));
//        Component peer = mapIdPeer.get(id);
//        if (peer != null) {
//            main.stopPeer(peer);
//        } else {
//            Logger.getLogger(ActiveTorrents.class.getName()).log(Level.SEVERE,
//                    "Could not find peer for torrentfile when deleting : {0}", torrentToRemove);
//            return false;
//        }
//
//        return true;
//    }
    public synchronized static String getVideoName(String torrentFilename) {
        return getInstance().getVideoNameP(torrentFilename);
    }

    private String getVideoNameP(String torrentFilename) {
        String videoFilename = ActiveTorrents.getVideoFilename(torrentFilename);
        if (videoFilename == null) {
            return null;
        }
        File f = new File(videoFilename);
        return f.getName();
    }

    public synchronized static Set<Component> getPeers() {
        return getInstance().getPeersP();
    }

    private Set<Component> getPeersP() {
        Set<Component> peers = new HashSet<Component>();
        peers.addAll(mapIdPeer.values());
        return peers;
    }

    public synchronized static Set<TorrentEntry> getLeecherEntries() {
        return getInstance().getTorrentEntriesP(false);
    }

    public synchronized static Map<Integer, Integer> getLeecherIdsUtilities() {
        Map<Integer, Integer> ids = new HashMap<Integer, Integer>();
        for (TorrentEntry t : getInstance().getTorrentEntriesP(false)) {
            String percent = t.getPercent();
            int p = 0;
            try {
                p = Integer.parseInt(percent);
            } catch (NumberFormatException e) {
                p = -1;
            }
            ids.put(ActiveTorrents.calculateVideoId(t.getVideoName()), p);
        }
        return ids;
    }

    public synchronized static Set<TorrentEntry> getSeederEntries() {
        return getInstance().getTorrentEntriesP(true);
    }

    public synchronized static Set<Integer> getSeederIds() {
        Set<Integer> ids = new HashSet<Integer>();
        for (TorrentEntry t : getInstance().getTorrentEntriesP(true)) {
            ids.add(ActiveTorrents.calculateVideoId(t.getVideoName()));
        }
        return ids;
    }

    /**
     *
     * @param seed return seeds
     * @return all TorrentEntries for leechers and optionally also for seeders
     */
    private Set<TorrentEntry> getTorrentEntriesP(boolean seed) {
        Set<TorrentEntry> entries = new HashSet<TorrentEntry>();
        for (TorrentEntry e : listTorrentEntries) {
            if (seed) {
                if (e.isSeed()) {
                    entries.add(e);
                }
            } else {
                if (!e.isSeed()) {
                    entries.add(e);
                }
            }
        }
        return entries;
    }

    /**
     * Get a set of ids for all active torrents.
     *
     * @return a set containing the IDs of all active torrents
     */
    public synchronized static Set<Integer> getAllIds() {
        return getInstance().getAllIdsP();
    }

    private Set<Integer> getAllIdsP() {
        Set<Integer> ids = new HashSet<Integer>();
        for (String video : mapVideoTorrent.keySet()) {
            ids.add(ActiveTorrents.calculateVideoId(video));
        }
        return ids;
    }

    public synchronized static int size() {
        return getInstance().sizeP();
    }

    private int sizeP() {
        return listTorrentEntries.size();
    }

    public synchronized static TorrentEntry getTorrentEntry(String videoName) {
        return getInstance().getTorrentEntryP(videoName);
    }

    private TorrentEntry getTorrentEntryP(String videoName) {
        String torrentFilename = mapVideoTorrent.get(videoName);
        return (torrentFilename == null) ? null : mapTorrentEntries.get(torrentFilename);
    }

    /**
     * Get a TorrentEntry using the row number from the Swing GUI (not the
     * torrent-id)
     *
     * @param rowId int value
     * @return a TorrentEntry or throws an ArrayIndexOutOfBoundsException, if
     * the rowId is out-of-range.
     */
    public synchronized static TorrentEntry getTorrentEntry(int rowId) {
        return getInstance().getTorrentEntryP(rowId);
    }

    private TorrentEntry getTorrentEntryP(int rowId) {
        if (rowId > sizeP() || rowId < 0 || listTorrentEntries.isEmpty()) {
            throw new ArrayIndexOutOfBoundsException("Row id was " + rowId
                    + ". Size was " + sizeP());
        }
        return listTorrentEntries.get(rowId);
    }

    public synchronized static void updatePercentage(String videoName,
            String percent) {
        getInstance().updatePercentageP(videoName, percent);
    }

    private void updatePercentageP(String videoName, String percent) {
        TorrentEntry entry = getTorrentEntry(videoName);
        if (entry == null) {
            Logger.getLogger(ActiveTorrents.class.getName()).log(Level.SEVERE,
                    "Torrentfilename was NULL when updating percentage for {0}", videoName);
        } else {
            entry.setPercent(percent);
            firePropertyChange(TORRENT_UPDATED, entry, entry);
        }
    }

    public synchronized static int getRowId(TorrentEntry entry) {
        return getInstance().getRowIdP(entry);
    }

    private int getRowIdP(TorrentEntry entry) {
        return listTorrentEntries.indexOf(entry);
    }

    public static int byteArrayToInt(byte[] b) {
        int value = 0;
        for (int i = 0; i < 4; i++) {
            int shift = (4 - 1 - i) * 8;
            value += (b[i] & 0x000000FF) << shift;
        }
        return value;
    }
}
