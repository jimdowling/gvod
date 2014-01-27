/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.system.main;

import java.io.OutputStream;
import se.sics.kompics.Component;

/**
 *
 * @author jdowling
 */
public interface GMain {

    public boolean changeUtility(String filename, int seekPos,
            int adjustedTime,
            OutputStream responseBody);

    public void stopPeer(Component peer);
}
