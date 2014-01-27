/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package se.sics.gvod.system.swing;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import javax.swing.table.AbstractTableModel;
import org.apache.log4j.Logger;
import se.sics.gvod.config.VodConfig;
import se.sics.gvod.system.util.ActiveTorrents;
import se.sics.gvod.system.util.ActiveTorrents.TorrentEntry;

/**
 *
 * @author jdowling
 */
public class ActiveStreamsTableModel
        extends AbstractTableModel
{
        public static final String[] COLUMN_NAMES =
        {
            "Name"
            ,"Percent Downloaded"
            ,"Size (MB)"
//            ,"Status"
//                , "Sharing Ratio"
        };



    public ActiveStreamsTableModel() {
    }

    public synchronized void removeTorrent(int index, boolean deleteVideo) {
        TorrentEntry entry = ActiveTorrents.getTorrentEntry(index);
        if (entry != null) {
            String torrentFilename = entry.getTorrentFilename();
            String videoFilename = VodConfig.getVideoDir() + File.separator + entry.getVideoName();
            ActiveTorrents.removeTorrent(torrentFilename);
            if (deleteVideo == true) {
                try {
                    org.apache.commons.io.FileUtils.forceDelete(new File(videoFilename));
                } catch (IOException ex) {
                    java.util.logging.Logger.getLogger(ActiveStreamsTableModel.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            fireTableRowsDeleted(index, index);
        } else {
            Logger.getLogger(ActiveStreamsTableModel.class).warn("Could not find torrent to delete.");
        }
    }

    @Override
    public int getRowCount() {
        return  ActiveTorrents.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMN_NAMES.length;
    }

    @Override
    public String getColumnName(int column) {
        return COLUMN_NAMES[column];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0: return String.class;
            case 1: return String.class;
            case 2: return Integer.class;
            case 3: return String.class;
            case 4: return Double.class;
        }
        throw new ArrayIndexOutOfBoundsException("Num of cols was " + COLUMN_NAMES.length
                + " index was " + columnIndex);
    }


    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {

        switch (columnIndex)
        {
            case 0:
                // videoName
                return ActiveTorrents.getTorrentEntry(rowIndex).getVideoName();
            case 1:
                // percentage
                return ActiveTorrents.getTorrentEntry(rowIndex).getPercent();
            case 2:
                // size
                return ActiveTorrents.getTorrentEntry(rowIndex).getSize() / (1024 * 1024);
            case 3:
                // status
                return ActiveTorrents.getTorrentEntry(rowIndex).isSeed();
            case 4:
                // sharing ratio
                return 0;
            default:
        }

        return "";
    }

    @Override
  public boolean isCellEditable(int row, int column) {
    return false;
  }
    
  public void updatePercentage(Integer rowId) {
      fireTableRowsUpdated(rowId, rowId);
      fireTableDataChanged();
  }
}
