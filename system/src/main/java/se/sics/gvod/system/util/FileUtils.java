/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.system.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import se.sics.gvod.config.VodConfig;
import se.sics.gvod.common.util.bencode.BDecoder;
import se.sics.gvod.common.util.bencode.BEValue;

/**
 *
 * @author jdowling
 */
public class FileUtils {

    List<BEValue> getListActiveTorrents() {
        List<BEValue> active = new ArrayList<BEValue>();
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
                    ActiveTorrents.updateTorrentLibrary();
                } else {
                    Map info = val.getMap();
                    val = (BEValue) info.get("dataFiles");
                    if (val == null) {
                        in.close();
                        in = null;
                        activeStreamsFile.delete();
                        ActiveTorrents.updateTorrentLibrary();
                    } else {
                        active = val.getList();
                    }
                }
            } catch (IOException ex) {
                java.util.logging.Logger.getLogger(ActiveTorrents.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                } catch (IOException ex) {
                    java.util.logging.Logger.getLogger(ActiveTorrents.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return active;
    }

    public void overwriteActiveStreams(List<BEValue> torrents) {

    }

    public static String getPostFix(String filename) {
        int idx = filename.lastIndexOf('.');
        if (idx == -1) {
            return "";
        }
        return filename.substring(idx);
    }


    public static String getValidFileName(String fileName) {
        // replace spaces with empty characters in the filename
        String newFileName = fileName.replaceAll("[^A-Za-z0-9_.\\\\s-]*", "");

        // if the filename still contains invalids chars, raise an error.
        if (newFileName.length() == 0 
                ||  newFileName.contains("*")
                ||  newFileName.contains("\\")
                ||  newFileName.contains(":")
                ||  newFileName.contains("?")
                ||  newFileName.contains("|")
                ||  newFileName.contains("\"")
                ) {
            throw new IllegalStateException(
                    "File Name " + fileName + " is invalid. Change to a filename that can be used in a URL.");
        }
        return newFileName;
    }

}
