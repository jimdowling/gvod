/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.system.main;

import se.sics.gvod.system.peer.events.ReportDownloadSpeed;
import se.sics.kompics.PortType;

/**
 *
 * @author gautier
 */
public class AppMainPort extends PortType {

    {
        negative(StartInBackground.class);
        negative(ReportDownloadSpeed.class);
    }
}
