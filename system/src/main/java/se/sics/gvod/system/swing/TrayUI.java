/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.system.swing;

/**
 *
 * @author jdowling
 */
/*
 * Copyright 2008 Sun Microsystems, Inc., 4150 Network Circle,
 * Santa Clara, California 95054, U.S.A. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
import se.sics.gvod.system.swing.dialogs.WarningDialog;
import javax.swing.*;
import javax.swing.event.PopupMenuListener;
import javax.swing.event.PopupMenuEvent;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.gvod.system.util.ActiveTorrents;
import se.sics.gvod.system.util.ActiveTorrents.TorrentEntry;
import se.sics.kompics.Kompics;

public class TrayUI extends TrayIcon implements PropertyChangeListener {

    private static final Logger logger = LoggerFactory.getLogger(TrayUI.class);
    private JPopupMenu menu;
    private VodMdi mdi;
    private static final JDialog dialog;
    private static Image image;
    int x;
    int y;
    private static boolean showPopup = false;

    static {
        dialog = new JDialog((Frame) null);
        dialog.setUndecorated(true);
        dialog.setAlwaysOnTop(true);
    }
    final SwingComponent swingInterface;
    private static PopupMenuListener popupListener = new PopupMenuListener() {

        @Override
        public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
            showPopup = true;
        }

        @Override
        public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            dialog.setVisible(false);
            showPopup = false;
        }

        @Override
        public void popupMenuCanceled(PopupMenuEvent e) {
            dialog.setVisible(false);
            showPopup = false;
        }
    };

    public TrayUI(Image image, SwingComponent swingInterfaceComponent) {
        super(image);
        TrayUI.image = image;

        this.swingInterface = swingInterfaceComponent;

        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                createGui();
            }
        });

        addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
//                    mdi.pack();
//                    mdi.setVisible(true);

                if (SwingUtilities.isRightMouseButton(e)) {
                    if (showPopup == false) {
                        showJPopupMenu(e);
                    } else {
                         dialog.setVisible(false);
                    }
                } else //if (e.getClickCount() == 2)
                {
                    mdi.pack();
                    mdi.setVisible(true);
                }

            }

            @Override
            public void mousePressed(MouseEvent e) {
//                showJPopupMenu(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
//                showJPopupMenu(e);
            }

            @Override
            public void mouseExited(MouseEvent e) {
//                showJPopupMenu(e);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
//                super.mouseEntered(e);
            }
        });

        ActiveTorrents.addPropertyChangeListener(this);

    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {

        if (evt.getSource() instanceof TorrentEntry) {
            entryChanged((TorrentEntry) evt.getSource(), evt.getPropertyName(),
                    (TorrentEntry) evt.getOldValue());
        }
    }

    private void entryChanged(TorrentEntry torrentEntry,
            String propertyChanged, TorrentEntry lastValue) {

        if (propertyChanged.compareTo(ActiveTorrents.PEER_ALREADY_RUNNING) == 0) {
            WarningDialog d = new WarningDialog((Frame) null, true,
                    "Peer already running for torrent: " + torrentEntry.getVideoName()
                    + " at " + torrentEntry.getTorrentFilename());
            d.setLocation(x + 200, y + 200);
            d.setIconImage(new ImageIcon(image).getImage());
            d.pack();
            d.setVisible(true);
        } else if (propertyChanged.compareTo(ActiveTorrents.PEER_STARTED) == 0) {
            // do nothing
        } else if (propertyChanged.compareTo(ActiveTorrents.PEER_STOPPED) == 0) {
            // do nothing
        } else if (propertyChanged.compareTo(ActiveTorrents.SEED_ADDED) == 0) {
            // do nothing
        } else if (propertyChanged.compareTo(ActiveTorrents.SEED_BECOME) == 0) {
            // do nothing
        } else if (propertyChanged.compareTo(ActiveTorrents.TORRENT_ADDED) == 0) {
            // do nothing
        } else if (propertyChanged.compareTo(ActiveTorrents.TORRENT_ALREADY_ADDED) == 0) {
            WarningDialog d = new WarningDialog((Frame) null, true,
                    "Torrent already added: " + torrentEntry.getVideoName()
                    + " at " + torrentEntry.getTorrentFilename());
            d.setLocation(x + 200, y + 200);
            d.setIconImage(new ImageIcon(image).getImage());
            d.pack();
            d.setVisible(true);
            // TODO - user shouldn't have to click 'ok'. just disappear after 2 secs.
        } else if (propertyChanged.compareTo(ActiveTorrents.TORRENT_AND_DATA_REMOVED) == 0) {
            // do nothing
        } else if (propertyChanged.compareTo(ActiveTorrents.TORRENT_REMOVED) == 0) {
        } else {
            logger.error("No changed property defined for " + propertyChanged);
        }
    }

    protected void showJPopupMenu(MouseEvent e) {
//        if (e.isPopupTrigger() && menu != null) {
        if (menu != null) {
            showJPopupMenu(e.getXOnScreen(), e.getYOnScreen());
        }
    }

    protected void showJPopupMenu(int x, int y) {
        Dimension size = menu.getPreferredSize();
        dialog.setLocation(x, y - size.height);
        dialog.setVisible(true);
        menu.show(dialog.getContentPane(), 0, 0);
        // popup works only for focused windows
        dialog.toFront();
    }

    public JPopupMenu getJPopupMenu() {
        return menu;
    }

    public void setJPopupMenu(JPopupMenu menu) {
        if (this.menu != null) {
            this.menu.removePopupMenuListener(popupListener);
        }
        this.menu = menu;
        menu.addPopupMenuListener(popupListener);
    }

    private void createGui() {
        setJPopupMenu(createJPopupMenu());
        mdi = new VodMdi(this.swingInterface);
        try {
            // TODO: Bug in setting transparency of SystemTray Icon in 
            // Linux - http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6453521
            SystemTray.getSystemTray().add(this);
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }

    JPopupMenu createJPopupMenu() {
        final JPopupMenu m = new JPopupMenu();
        final int xCoords = m.getX();
        final int yCoords = m.getY();

        x = xCoords;
        y = yCoords;

//        JMenuItem prefItem = new JMenuItem("Preferences...");
//
//        prefItem.addActionListener(
//                new ActionListener() {
//
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                PreferencesDialog d = new PreferencesDialog((Frame) null, true);
//                d.setIconImage(new ImageIcon(image).getImage());
//                d.setLocation(xCoords+200, yCoords+200);
//                d.pack();
//                d.setVisible(true);
//            }
//        }
//        );
//        m.add(prefItem);


//        JMenuItem seedItem = new JMenuItem("Create Torrent");
//        seedItem.addActionListener(new ActionListener() {
//
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                CreateTorrentDialog d = new CreateTorrentDialog((Frame) null, true,
//                        swingInterface);
//                d.setLocation(xCoords + 200, yCoords + 200);
//                d.setIconImage(new ImageIcon(image).getImage());
//                d.pack();
//                d.setVisible(true);
//            }
//        });
//        m.add(seedItem);

//        JMenuItem showTorrentsItem = new JMenuItem("Show Torrents");
//        showTorrentsItem.addActionListener(new ActionListener() {
//
//            public void actionPerformed(ActionEvent e) {
//                ActiveStreamsDialog d = new ActiveStreamsDialog((Frame) null, true,
//                        swingInterface);
//                d.setLocation(xCoords + 200, yCoords + 200);
//                d.setIconImage(new ImageIcon(image).getImage());
//                d.pack();
//                d.setVisible(true);
//            }
//        });
//        m.add(showTorrentsItem);


        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                logger.info("Exit action performed by user");
                Kompics.shutdown();
                System.exit(0);
            }
        });
        m.add(exitItem);

//public void actionPerformed(ActionEvent e) {
//    //Handle open button action.
//    if (e.getSource() == openButton) {
//        int returnVal = fc.showOpenDialog(FileChooserDemo.this);
//
//        if (returnVal == JFileChooser.APPROVE_OPTION) {
//            File file = fc.getSelectedFile();
//            //This is where a real application would open the file.
//            logger.debug("Opening: " + file.getName() + "." );
//        } else {
//            logger.debug("Open command cancelled by user.");
//        }
//   }
//}







        return m;


    }
}
