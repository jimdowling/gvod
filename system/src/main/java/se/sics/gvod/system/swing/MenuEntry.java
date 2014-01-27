/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package se.sics.gvod.system.swing;

/**
 *
 * @author jdowling
 */
public class MenuEntry {
    private final String name;
    private boolean hidden;

    public MenuEntry(String name, boolean hidden) {
        this.name = name;
        this.hidden = hidden;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public String getName() {
        return name;
    }
}
