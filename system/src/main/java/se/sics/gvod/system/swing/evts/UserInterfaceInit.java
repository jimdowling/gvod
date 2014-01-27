/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package se.sics.gvod.system.swing.evts;

import se.sics.gvod.system.main.SwingMain;
import se.sics.kompics.Init;

/**
 *
 * @author gautier
 */
public class UserInterfaceInit extends Init {

    private SwingMain main;
    public UserInterfaceInit(SwingMain main) {
        this.main = main;
    }
    
//    private final Set<String> activeStreams;
//    private final HashMap<Integer, Component> gvodPeers;
//    private final HashMap<String, Integer> namePeerIds;
//    private final HashMap<String, String> videoTorrent;
//
//    public UserInterfaceInit(Set<String> activeStreams,
//            HashMap<Integer,Component> gvodPeersBack,
//            HashMap<String,Integer> gvodPeersBackName,
//            HashMap<String, String> videoTorrent
//            ) {
////        this.gvodId = gvodId;
//        this.activeStreams = activeStreams;
//        this.gvodPeers = gvodPeersBack;
//        this.namePeerIds = gvodPeersBackName;
//        this.videoTorrent = videoTorrent;
//    }
//
//
////    public Integer getgvodId() {
////        return gvodId;
////    }
//
//
//    public Set<String> getActiveStreams() {
//        return activeStreams;
//    }
//
//    public HashMap<Integer, Component> getMapIdPeers() {
//        return gvodPeers;
//    }
//
//    public HashMap<String, Integer> getMapNamePeerIds() {
//        return namePeerIds;
//    }
//
//    public HashMap<String, String> getMapVideoTorrent() {
//        return videoTorrent;
//    }

    public SwingMain getMain() {
        return main;
    }
    
    
}
