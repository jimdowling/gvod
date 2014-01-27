/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package se.sics.gvod.system.main;

import se.sics.kompics.Event;

/**
 *
 * @author jdowling
 */
public class LoadTorrentResult extends Event {

    private final String metadataInfo;

    public LoadTorrentResult(String metadataInfo) {
        this.metadataInfo = metadataInfo;
    }

    public String getMetadataInfo() {
        return metadataInfo;
    }



}
