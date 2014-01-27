/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * GvodMdi.javav
 *
 * Created on 14-Mar-2011, 11:05:44
 */
package se.sics.gvod.system.swing;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import se.sics.gvod.config.VodConfig;
import se.sics.gvod.system.swing.dialogs.CreateTorrentDialog;
import se.sics.gvod.system.util.ActiveTorrents;
import se.sics.gvod.system.util.ActiveTorrents.TorrentEntry;

/**
 *
 * @author jdowling
 */
public class VodMdi extends javax.swing.JFrame implements PropertyChangeListener,
        TableModelListener {

    private final SwingComponent swingInterface;
    private MenuListModel menuListModel =
            new MenuListModel();
    private JPopupMenu menu;
    private MenuEntry todayMenu = new MenuEntry("Today", false);
    private MenuEntry thisWeekMenu = new MenuEntry("This week", false);
    private MenuEntry olderMenu = new MenuEntry("Older", false);
    private MenuEntryListCellRenderer menuEntryListCellRenderer =
            new MenuEntryListCellRenderer();
    private static final JDialog dialog;
    private JTable libraryTable;
    private ActiveStreamsTableModel libraryTableModel =
            new ActiveStreamsTableModel();
    private static Image image;

    static {
        dialog = new JDialog((Frame) null);
        dialog.setUndecorated(true);
        dialog.setAlwaysOnTop(true);
    }
    private static PopupMenuListener popupListener = new PopupMenuListener() {
        @Override
        public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
        }

        @Override
        public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            dialog.setVisible(false);
        }

        @Override
        public void popupMenuCanceled(PopupMenuEvent e) {
            dialog.setVisible(false);
        }
    };

    /**
     * Creates new form GvodMdi
     */
    public VodMdi(SwingComponent swingInterface) {
        initComponents();
        this.swingInterface = swingInterface;
        ActiveTorrents.addPropertyChangeListener(this);
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                hideWindow();
            }
        });

        menuListModel.addEntry(todayMenu);
        menuListModel.addEntry(thisWeekMenu);
        menuListModel.addEntry(olderMenu);


        createLibrary();

        image = createImage("if.png", "gvod icon");
        if (image != null) {
            this.setIconImage(image);
        }

        libraryTableModel.addTableModelListener(this);
    }
    
    // dangerous - TODO remove
    public ActiveStreamsTableModel getTableModel() {
        return libraryTableModel;
    }

    protected static Image createImage(String path, String description) {
        URL imageURL = SwingComponent.class.getResource(path);

        if (imageURL == null) {
            System.err.println("Resource not found: " + path);
            return null;
        } else {
            return (new ImageIcon(imageURL, description)).getImage();
        }
    }

    private void createLibrary() {

        libraryTable = new JTable(libraryTableModel);
        JScrollPane libraryScrollPane = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        libraryTable.setModel(libraryTableModel);
        libraryTable.setFillsViewportHeight(true);
        libraryTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        libraryTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        libraryTable.setAutoCreateRowSorter(true);

//        for (int i = 0; i < ActiveStreamsTableModel.COLUMN_NAMES.length; i++) {
//            TableColumn col = libraryTable.getColumnModel().getColumn(i);
//            col.setCellRenderer(new ActiveStreamsTableCellRenderer());
//        }
        libraryScrollPane.setBounds(0, 0, 800, 600);
        libraryScrollPane.setViewportView(libraryTable);
        mainPanel.add(libraryScrollPane);

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
                mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).
                addComponent(libraryScrollPane, javax.swing.GroupLayout.Alignment.LEADING,
                javax.swing.GroupLayout.PREFERRED_SIZE,
                800, Short.MAX_VALUE));
        mainPanelLayout.setVerticalGroup(
                mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).
                addComponent(libraryScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE,
                javax.swing.GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE));


//        mainPanel.add(libraryScrollPane, javax.swing.JLayeredPane.DEFAULT_LAYER);

//        libraryTable.setDefaultRenderer(Color.class, new ActiveStreamsTableCellRenderer(true));


//        libraryTable.addListSelectionListener(new ListSelectionListener() {
//            @Override
//            public void valueChanged(ListSelectionEvent evt) {
//                listSelectionChanged(evt, false);
//            }
//        });


        libraryTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                libraryTableMouseClicked(evt);
            }

            @Override
            public void mousePressed(java.awt.event.MouseEvent evt) {
                libraryTableMousePressed(evt);
            }
        });

    }

    @Override
    public void tableChanged(TableModelEvent e) {
        dialog.repaint();
        dialog.getContentPane().repaint();
        mainPanel.repaint();
        libraryTable.repaint();
        
    }

    private void libraryTableMousePressed(java.awt.event.MouseEvent evt) {
        int xCoords = evt.getXOnScreen();
        int yCoords = evt.getYOnScreen();

        setJPopupMenu(createJPopupMenu(xCoords, yCoords));
        showJPopupMenu(evt);
    }

    private void libraryTableMouseClicked(java.awt.event.MouseEvent evt) {
        int rowId = this.libraryTable.getSelectedRow();
        if (rowId != -1) {
            viewSelectedVideo();
        }

    }

    public void setJPopupMenu(JPopupMenu menu) {
        if (this.menu != null) {
            this.menu.removePopupMenuListener(popupListener);
        }
        this.menu = menu;
        menu.addPopupMenuListener(popupListener);
    }

    private void viewSelectedVideo() {
        String videoName = getSelectedVideo();
        String url = "http://localhost:" + VodConfig.getMediaPort()
                + "/?view=" + videoName;
        try {
            java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(TrayUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private String getSelectedVideo() {

        int row = this.libraryTable.getSelectedRow();
        if (row != -1) {
            return (String) libraryTable.getValueAt(row, 0);
        }
        return "";
    }

    JPopupMenu createJPopupMenu(int xCoords, int yCoords) {
        final JPopupMenu m = new JPopupMenu();

//        JMenuItem openItem = new JMenuItem("Watch video");
//        openItem.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//
//                viewSelectedVideo();
//            }
//        });
//        m.add(openItem);


        JMenuItem prefItem = new JMenuItem("Remove from library");
        prefItem.addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        removeVideo(false);
                    }
                });
        m.add(prefItem);
        
        JMenuItem delItem = new JMenuItem("Delete completely.");
        delItem.addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        removeVideo(true);
                    }
                });
        m.add(delItem);
        return m;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {

        if (evt.getSource() instanceof TorrentEntry) {
            entryChanged((TorrentEntry) evt.getSource(), evt.getPropertyName(),
                    (TorrentEntry) evt.getOldValue());
        }
    }

    private void entryChanged(TorrentEntry newValue,
            String propertyChanged, TorrentEntry lastValue) {

        if (propertyChanged.compareTo(ActiveTorrents.PEER_ALREADY_RUNNING) == 0) {
            // do nothing
        } else if (propertyChanged.compareTo(ActiveTorrents.PEER_STARTED) == 0) {
            // redraw?
        } else if (propertyChanged.compareTo(ActiveTorrents.PEER_STOPPED) == 0) {
        } else if (propertyChanged.compareTo(ActiveTorrents.SEED_ADDED) == 0) {
        } else if (propertyChanged.compareTo(ActiveTorrents.SEED_BECOME) == 0) {
        } else if (propertyChanged.compareTo(ActiveTorrents.TORRENT_ADDED) == 0) {
        } else if (propertyChanged.compareTo(ActiveTorrents.TORRENT_UPDATED) == 0) {
            int rowId = ActiveTorrents.getRowId(newValue);
            libraryTableModel.updatePercentage(rowId);
        } else if (propertyChanged.compareTo(ActiveTorrents.TORRENT_ALREADY_ADDED) == 0) {
            // do nothing
        } else if (propertyChanged.compareTo(ActiveTorrents.TORRENT_AND_DATA_REMOVED) == 0) {
        } else if (propertyChanged.compareTo(ActiveTorrents.TORRENT_REMOVED) == 0) {
            Logger.getLogger(VodMdi.class.getName()).log(Level.SEVERE, null,
                    "Removed torrent propertyChanged");
        } else {
            Logger.getLogger(VodMdi.class.getName()).log(Level.SEVERE, null,
                    "No changed property defined for " + propertyChanged);
        }
    }

    public void hideWindow() {
        this.setVisible(false);
    }

    private void removeVideo(boolean deleteVideo) {
        int row = this.libraryTable.getSelectedRow();
        if (row != -1) {
            this.libraryTableModel.removeTorrent(row, deleteVideo);
        }
    }

    protected void showJPopupMenu(MouseEvent e) {
        if (e.isPopupTrigger() && menu != null) {
            Dimension size = menu.getPreferredSize();
            showJPopupMenu(e.getXOnScreen(), e.getYOnScreen() - size.height);
        }
    }

    protected void showJPopupMenu(int x, int y) {
        dialog.setLocation(x, y);
        dialog.setVisible(true);
        menu.show(dialog.getContentPane(), 0, 0);
        // popup works only for focused windows
        dialog.toFront();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        menuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        exitMenuItem = new javax.swing.JMenuItem();
        editMenu = new javax.swing.JMenu();
        createStreamMenuItem = new javax.swing.JMenuItem();
        helpMenu = new javax.swing.JMenu();
        aboutMenuItem = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setName("gvodFrame"); // NOI18N

        mainPanel.setAlignmentX(0.0F);
        mainPanel.setAlignmentY(0.0F);
        mainPanel.setMinimumSize(new java.awt.Dimension(0, 0));

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1051, Short.MAX_VALUE)
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 561, Short.MAX_VALUE)
        );

        fileMenu.setText("File");

        jMenuItem1.setText("Open Video");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        fileMenu.add(jMenuItem1);

        exitMenuItem.setText("Exit");
        exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        editMenu.setText("Upload Video");

        createStreamMenuItem.setText("Upload Video ");
        createStreamMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createStreamMenuItemActionPerformed(evt);
            }
        });
        editMenu.add(createStreamMenuItem);

        menuBar.add(editMenu);

        helpMenu.setText("Help");

        aboutMenuItem.setText("About");
        aboutMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aboutMenuItemActionPerformed(evt);
            }
        });
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        setJMenuBar(menuBar);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mainPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(mainPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        getAccessibleContext().setAccessibleName("ecvideo");

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitMenuItemActionPerformed
        hideWindow();
    }//GEN-LAST:event_exitMenuItemActionPerformed

    private void createStreamMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createStreamMenuItemActionPerformed

        CreateTorrentDialog d = new CreateTorrentDialog(this, true,
                swingInterface);
        d.setLocation(200, 200);
        d.setIconImage(new ImageIcon(image).getImage());
        d.pack();
        d.setVisible(true);
        libraryTableModel.fireTableDataChanged();
    }//GEN-LAST:event_createStreamMenuItemActionPerformed

    private void aboutMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutMenuItemActionPerformed
        JOptionPane.showMessageDialog(this, "Gvod @Copyright http://clommunity-project.eu, 2013");
    }//GEN-LAST:event_aboutMenuItemActionPerformed

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed

        final JFileChooser fc = new JFileChooser(VodConfig.getTorrentDir());

        FileFilter filter = new FileNameExtensionFilter("VoD files", "data");
        fc.addChoosableFileFilter(filter);
        fc.setFileFilter(filter);

        int returnVal = fc.showDialog(null, "Select video-on-demand file");

        File videoFile = null;
        
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            videoFile = fc.getSelectedFile();
            try {
                String videoFilename = videoFile.getCanonicalPath().toString();
                boolean succeed = swingInterface.getMain().loadTorrentFromFile(videoFilename);
                if (!succeed) {
                     JOptionPane.showMessageDialog(this, videoFilename + " already loaded.");
                }
            } catch (IOException ex) {
                java.util.logging.Logger.getLogger(CreateTorrentDialog.class.getName()).log(Level.SEVERE, null, ex);
            }
            //This is where a real application would open the file.
            Logger.getLogger("VodMidi").log(Level.FINE, "Opening: {0}.", videoFile.getName());
        } else {
            Logger.getLogger("VodMidi").fine("Open command cancelled by user.");
        }        
        
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem aboutMenuItem;
    private javax.swing.JMenuItem createStreamMenuItem;
    private javax.swing.JMenu editMenu;
    private javax.swing.JMenuItem exitMenuItem;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JMenuBar menuBar;
    // End of variables declaration//GEN-END:variables
}
