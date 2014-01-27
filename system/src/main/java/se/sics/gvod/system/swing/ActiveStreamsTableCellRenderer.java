/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package se.sics.gvod.system.swing;

import java.awt.Color;
import java.awt.Component;
import java.beans.PropertyChangeListener;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author jdowling
 */
public class ActiveStreamsTableCellRenderer extends JLabel
                           implements TableCellRenderer {
//    private boolean isBordered;
//    private Border selectedBorder = new LoweredBorder();
    private Border selectedBorder = new EtchedBorder(Color.darkGray, Color.yellow);
    private Border unselectedBorder = new EtchedBorder(Color.lightGray, Color.yellow);

    public ActiveStreamsTableCellRenderer() {
//        boolean isBordered
//        this.isBordered = isBordered;
        setOpaque(true); //MUST do this for background to show up.
    }

    @Override
    public Component getTableCellRendererComponent(
                            JTable table, Object value,
                            boolean isSelected, boolean hasFocus,
                            int row, int column) {
//        Color newColor = (Color) color;
//        if (isBordered) {
            if (isSelected) {
                //selectedBorder is a solid border in the color
                //table.getSelectionBackground().
                setBorder(selectedBorder);
                setBackground(Color.ORANGE);
            } else {
                //unselectedBorder is a solid border in the color
                //table.getBackground().
                setBorder(unselectedBorder);
                    setBackground(Color.LIGHT_GRAY);
            }
//        }


        setText(value.toString());

//        setToolTipText((String)value);
        return this;
    }

    
}
