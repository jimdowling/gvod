/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package se.sics.gvod.system.main;

import se.sics.kompics.Event;

/**
 *
 * @author gautier
 */
public class StartInBackground extends Event {

    private final String metadataInfo;

    public StartInBackground(String metadataInfo) {
        this.metadataInfo = metadataInfo;
    }

    public String getMetadataInfo() {
        return metadataInfo;
    }



}
