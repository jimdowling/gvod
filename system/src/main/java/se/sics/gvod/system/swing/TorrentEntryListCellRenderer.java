/*
 * Copyright (C) 2006 Sun Microsystems, Inc. All rights reserved. Use is
 * subject to license terms.
 */
package se.sics.gvod.system.swing;

import java.awt.Color;
import java.awt.Component;
import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.border.LineBorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.gvod.system.util.ActiveTorrents;

/**
 * ListCellRenderer for the PasswordEntrys. 
 *
 * @author sky
 */
final class TorrentEntryListCellRenderer extends JPanel implements ListCellRenderer {

    private static final Logger logger = LoggerFactory.getLogger(TorrentEntryListCellRenderer.class);
    private static final int LIST_CELL_ICON_SIZE = 16;
    private JLabel videoLabel;
    private JLabel percentLabel;
    private VerticalSessionConnectedBar connectedBar;

    TorrentEntryListCellRenderer() {
        videoLabel = new JLabel(" ");
        percentLabel = new JLabel(" ");

        connectedBar = new VerticalSessionConnectedBar();
        connectedBar.setOpaque(true);
        connectedBar.setBackground(Color.WHITE);
        connectedBar.setBorder(new LineBorder(Color.BLACK));

        GroupLayout layout = new GroupLayout(this);
        setLayout(layout);
        layout.setAutoCreateContainerGaps(true);
        layout.setAutoCreateGaps(true);
        GroupLayout.SequentialGroup hg = layout.createSequentialGroup();
        layout.setHorizontalGroup(hg);
        hg.addGroup(layout.createParallelGroup().
                addComponent(videoLabel, 5, 10, 400));
        hg.addGroup(layout.createParallelGroup().
                addComponent(percentLabel, 5, 10, 20)); 
        hg.addGroup(layout.createParallelGroup().
                addComponent(connectedBar, 5, 10, 20));

        GroupLayout.ParallelGroup vg = layout.createParallelGroup();
        layout.setVerticalGroup(vg);
        vg.addGroup(layout.createSequentialGroup().
                addComponent(videoLabel));
        vg.addGroup(layout.createSequentialGroup().
                addComponent(percentLabel));
        vg.addGroup(layout.createSequentialGroup().
                addComponent(connectedBar));

        setOpaque(true);
    }

    public Component getListCellRendererComponent(JList list, Object value,
            int index, boolean isSelected, boolean cellHasFocus) {
        if (value == null) {
            return this;
        }
        ActiveTorrents.TorrentEntry entry = (ActiveTorrents.TorrentEntry) value;
        String torrentFilename = entry.getTorrentFilename();
        if (torrentFilename == null) {
            torrentFilename = " ";
        }
        String video = entry.getVideoName();
        String percent = entry.getPercent();

        videoLabel.setText(video);
        percentLabel.setText(percent);

        float strength = 0.5f;
        strength = 0.3f;
        strength = 0.4f;
        strength = 1.0f;
//        int strength = (sessionId == 0) ? 0 : 100;
        if (entry != null) {
            connectedBar.setStrength(strength);
        }
        if (isSelected) {
            adjustColors(list.getSelectionBackground(),
                    list.getSelectionForeground(), this, videoLabel, percentLabel, connectedBar);
        } else {
            adjustColors(list.getBackground(),
                    list.getForeground(), this, videoLabel, percentLabel, connectedBar);
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
