/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package se.sics.gvod.system.swing;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFrame;

class MyComboBoxModel extends AbstractListModel implements ComboBoxModel {
  String[] ComputerComps = { "Monitor", "Key Board", "Mouse", "Joy Stick", "Modem", "CD ROM",
      "RAM Chip", "Diskette" };

  String selection = null;

  public Object getElementAt(int index) {
    return ComputerComps[index];
  }

  public int getSize() {
    return ComputerComps.length;
  }

  public void setSelectedItem(Object anItem) {
    selection = (String) anItem; // to select and register an
  } // item from the pull-down list

  // Methods implemented from the interface ComboBoxModel
  public Object getSelectedItem() {
    return selection; // to add the selection to the combo box
  }
}

