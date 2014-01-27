/*
 * Copyright (C) 2006 Sun Microsystems, Inc. All rights reserved. Use is
 * subject to license terms.
 */
package se.sics.gvod.system.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Image;
import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ListCellRenderer. 
 *
 * @author sky
 */
final class MenuEntryListCellRenderer extends JPanel implements ListCellRenderer {

    private static final Logger logger = LoggerFactory.getLogger(MenuEntryListCellRenderer.class);
    private JLabel menuEntryLabel;
    private JLabel closeLabel;
//    private Image closeIcon;

    MenuEntryListCellRenderer() {
        menuEntryLabel = new JLabel(" ");
        closeLabel = new JLabel(" ");
//        connectedBar.setBackground(Color.WHITE);
//        connectedBar.setBorder(new LineBorder(Color.BLACK));

        GroupLayout layout = new GroupLayout(this);
        setLayout(layout);
        layout.setAutoCreateContainerGaps(true);
        layout.setAutoCreateGaps(true);
        GroupLayout.SequentialGroup hg = layout.createSequentialGroup();
        layout.setHorizontalGroup(hg);
        hg.addGroup(layout.createParallelGroup().
                addComponent(menuEntryLabel, 5, 10, 400));
        hg.addGroup(layout.createParallelGroup().
                addComponent(closeLabel, 5, 10, 20));

        GroupLayout.ParallelGroup vg = layout.createParallelGroup();
        layout.setVerticalGroup(vg);
        vg.addGroup(layout.createSequentialGroup().
                addComponent(menuEntryLabel));
        vg.addGroup(layout.createSequentialGroup().
                addComponent(closeLabel));

        setOpaque(true);
    }

    @Override
    public Component getListCellRendererComponent(JList list, Object value,
            int index, boolean isSelected, boolean cellHasFocus) {
        if (value == null) {
            return this;
        }
        MenuEntry entry = (MenuEntry) value;
        if (entry.isHidden()) {
            return this;
        }
        String menuName = entry.getName();
        menuEntryLabel.setText(menuName);

        if (isSelected) {
            adjustColors(list.getSelectionBackground(),
                    list.getSelectionForeground(), this, menuEntryLabel, closeLabel);
        } else {
            adjustColors(list.getBackground(),
                    list.getForeground(), this, menuEntryLabel, closeLabel);
        }
        return this;
    }

    private void adjustColors(Color bg, Color fg, Component... components) {
        for (Component c : components) {
            c.setForeground(fg);
            c.setBackground(bg);
        }
    }
}
