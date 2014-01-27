/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package se.sics.gvod.system.swing;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;

/**
 *
 * @author jdowling
 */
public class MenuListModel 
        extends AbstractListModel implements ComboBoxModel
{

    private MenuEntry selectedEntry=null;
    private List<MenuEntry> entries = new ArrayList<MenuEntry>();

    public MenuListModel() {
    }

    @Override
    public Object getElementAt(int index) {
        return entries.get(index);
    }

    @Override
    public int getSize() {
        return entries.size();
    }


    @Override
    public void setSelectedItem(Object anItem) {
        selectedEntry = (MenuEntry) anItem;
    }

    @Override
    public Object getSelectedItem() {
        return selectedEntry;
    }

    public synchronized void addEntry(MenuEntry entry) {
        entries.add(entry);
        int idx = entries.indexOf(entry);
        fireIntervalAdded(entry, idx, idx);
    }

    public synchronized void removeEntry(MenuEntry entry) {
        if (entries.contains(entry) == false) {
            return;
        }
        int index = entries.indexOf(entry);
        entries.remove(entry);
        fireIntervalRemoved(entry, index, index);
    }
}
