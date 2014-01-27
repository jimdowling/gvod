/**
 * This file is part of the Kompics P2P Framework.
 *
 * Copyright (C) 2009 Swedish Institute of Computer Science (SICS)
 * Copyright (C) 2009 Royal Institute of Technology (KTH)
 *
 * Kompics is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package se.sics.gvod.system.vod.snapshot;

import se.sics.gvod.net.VodAddress;
import java.util.HashMap;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import se.sics.gvod.system.vod.PeerInfo;
/**
 * 
 * @author gautier
 */
public class Snapshot {

    private static String DOTFILENAME = "g.dot";
    private static HashMap<Integer, PeerInfo> peers = new HashMap<Integer, PeerInfo>();
    private static List<Integer> removedPeers = new ArrayList<Integer>();
    private final int seed;

    public Snapshot(int seed) {
        this.seed = seed;
        DOTFILENAME = "g.dot";
        try {
            FileWriter writer = new FileWriter(DOTFILENAME, false);
            writer.write("");
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void addPeer(Integer address, long bw) {
        peers.put(address, new PeerInfo(bw, address));
    }

    public static void removePeer(Integer address) {
        removedPeers.add(address);
    }

    public static void addToUtilitySet(VodAddress node, VodAddress add) {
        peers.get(node.getId()).addToUtilitySet(add);
    }

    public static void addToUpperSet(VodAddress node, VodAddress add) {
        peers.get(node.getId()).addToUpperSet(add);
    }

    public static void quit(Integer nodeId){
        peers.get(nodeId).quit();
        peers.remove(nodeId);
    }
    public static void addToBelowSet(VodAddress node, VodAddress add) {
        peers.get(node.getId()).addToBelowSet(add);
    }

    public static void removeFromUtilitySet(VodAddress node, VodAddress add) {
        peers.get(node.getId()).removeFromUtilitySet(add);
    }

    public static void removeFromUpperSet(VodAddress node, VodAddress add) {
        peers.get(node.getId()).removeFromUpperSet(add);
    }

    public static void removeFromBelowSet(VodAddress node, VodAddress add) {
        peers.get(node.getId()).removeFromBelowSet(add);
    }

    public static void setUtility(VodAddress node, int utility) {
        peers.get(node.getId()).setUtility(utility);
    }

    public static void generateGraphVizReport() {
        String str = "digraph g {\n" +
                "\"" + durationToString(System.currentTimeMillis()) + "\";\n";
        String[] edgeColor = {"gray", "green", "purple"};
        String[] nodeColor = {"gray75", "gray50", "gray25" , "gray25", "gray10"};
        String srcLabel = new String();
        boolean flag = false;

        ArrayList<PeerInfo> test = new ArrayList<PeerInfo>();
        for (Integer peer : peers.keySet()) {
            test.add(peers.get(peer));
        }
        Collections.sort(test);
        ArrayList<String> temp = new ArrayList<String>();
        int utility = test.get(0).getUtility();
        temp.add("test" + utility);
        str += "subgraph " + utility + " {\n" +
                "rank = source;\n" +
                "test" + utility + "[style=invisible];\n";
        boolean f = true;
        for (PeerInfo peer : test) {
            if (utility != peer.getUtility() && peer.getUtility() >= 0) {
                utility = peer.getUtility();
                str += "}\n" +
                        "subgraph " + utility + " {\n" +
                        "rank = same;\n" +
                        "test" + utility + "[style=invisible];\n";
                temp.add("test" + utility);
            } else if(utility != peer.getUtility() && peer.getUtility()<=0){
                str += "}\n";
                f = false;
                utility=peer.getUtility();
            }
            srcLabel = peer.getId() + "(" + peers.get(peer.getId()).getUtility() + ")";
            str += peer.getId() + " [ color = " + nodeColor[peers.get(peer.getId()).getBwN()] + ", style = filled, label = \"" + srcLabel + "\" ];\n";
        }
        if (f) {
            str += "}\n";
        }
        str += temp.get(0);
        for (int i = 1; i < temp.size(); i++) {
            str += "->" + temp.get(i);
        }
        str += "[style=invisible, arrowhead=none];\n";
        for (Integer peer : peers.keySet()) {
            HashMap<Integer, List<VodAddress>> children = peers.get(peer).getNeighbours();
            for (Integer stripe : children.keySet()) {
                for (VodAddress child : children.get(stripe)) {

                    if (stripe == 1) {
                        str += peer + "->" + child.getId() + "[ color = " + edgeColor[stripe] + ", ordering = out ];\n";
                    } else if (stripe == 2) {
                        str += peer + "->" + child.getId() + "[ color = " + edgeColor[stripe] + ", ordering = in ];\n";
                    } else {
                        str += peer + "->" + child.getId() + "[ color = " + edgeColor[stripe] + " ];\n";
                    }
                    flag = true;
                }
            }
        }

        str += "}\n\n";

        if (flag) {
            try {
                FileWriter writer = new FileWriter(DOTFILENAME, true);
                writer.write(str);
                writer.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    public static final String durationToString(long duration) {
        StringBuilder sb = new StringBuilder();
        int ms = 0, s = 0, m = 0, h = 0, d = 0, y = 0;
        ms = (int) (duration % 1000);
        // get duration in seconds
        duration /= 1000;
        s = (int) (duration % 60);
        // get duration in minutes
        duration /= 60;
        if (duration > 0) {
            m = (int) (duration % 60);
            // get duration in hours
            duration /= 60;
            if (duration > 0) {
                h = (int) (duration % 24);
                // get duration in days
                duration /= 24;
                if (duration > 0) {
                    d = (int) (duration % 365);
                    // get duration in years
                    y = (int) (duration / 365);
                }
            }
        }
        boolean printed = false;
        if (y > 0) {
            sb.append(y).append("y_");
            printed = true;
        }
        if (d > 0) {
            sb.append(d).append("d_");
            printed = true;
        }
        if (h > 0) {
            sb.append(h).append("h_");
            printed = true;
        }
        if (m > 0) {
            sb.append(m).append("m_");
            printed = true;
        }
        if (s > 0 || !printed) {
            sb.append(s);
            if (ms > 0) {
                sb.append(".").append(String.format("%03d", ms));
                sb.append("ms");
            }else {
                sb.append("s");
            }
        }
        return sb.toString();
    }
}
