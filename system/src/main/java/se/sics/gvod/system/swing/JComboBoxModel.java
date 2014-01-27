/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package se.sics.gvod.system.swing;

import javax.swing.JComboBox;
import javax.swing.JFrame;

/**
 *
 * @author jdowling
 */
public class JComboBoxModel {

  public static void main(String[] a){
    JFrame frame = new JFrame();
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    JComboBox cbox = new JComboBox(new MyComboBoxModel());
    cbox.setMaximumRowCount(5);
    frame.add(cbox);

    frame.setSize(300, 200);
    frame.setVisible(true);
  }


}
