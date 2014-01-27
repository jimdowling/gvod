/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.memmappedfiles;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.collections.Buffer;
import org.apache.commons.collections.buffer.CircularFifoBuffer;
import se.sics.gvod.bootstrap.server.BootstrapServerMysql;
import se.sics.gvod.config.VodConfig;
import se.sics.gvod.system.storage.MetaInfoExec;
import se.sics.gvod.system.storage.Storage;
import se.sics.gvod.system.storage.StorageFcByteBuf;
import se.sics.gvod.system.storage.StorageMemMapWholeFile;
import se.sics.gvod.system.util.ActiveTorrents;

/**
 *
 * @author jdowling
 */
public class MemoryMappedFileLoader {

    private static final boolean CREATE_TORRENT_FILES = false;
    private static final boolean MEM_MAP_FILE = false;

    public MemoryMappedFileLoader() {
    }

    public static void main(String[] args) {
        Random r = new Random(1111);
        List<MetaInfoExec> metaInfos = new ArrayList<MetaInfoExec>();
        List<Storage> storageInfos = new ArrayList<Storage>();

        try {
            VodConfig.init(args);

            if (CREATE_TORRENT_FILES) {
                File folder = new File(VodConfig.getTorrentDir());
                File[] listOfFiles = folder.listFiles();
                if (listOfFiles == null) {
                    throw new IOException("Invalid file dir for torrents");
                }
                for (File f : listOfFiles) {
                    String fname = f.getCanonicalPath();
                    int postfixPos = fname.lastIndexOf(".");
                    String postFix = fname.substring(postfixPos);
                    String metaInfoName = fname.substring(0, postfixPos) + ".data";
                    if (postFix.compareTo(".mp4") == 0) {
                        Storage se;
                        if (MEM_MAP_FILE) {
                            se = new StorageMemMapWholeFile(f, 680, 480, VodConfig.getBootstrapServer(), 5000,
                                metaInfoName, VodConfig.getMonitorServer());
                        } else {
                            se = new StorageFcByteBuf(f, 680, 480, VodConfig.getBootstrapServer(), 5000,
                                metaInfoName, VodConfig.getMonitorServer());
                        }
                        MetaInfoExec mie = (MetaInfoExec) se.getMetaInfo();
                        metaInfos.add(mie);
                        if (MEM_MAP_FILE) {
                            ((StorageMemMapWholeFile) se).writePieceHashesToFile();
                        } else {
                            ((StorageFcByteBuf) se).writePieceHashesToFile();
                        }
//                        se.writePieceHashesToFile();
                        storageInfos.add(se);
                        FileOutputStream fos = null;
                        try {
                            File fm = new File(metaInfoName);
                            if (fm.exists()) {
                                fm.delete();
                            }
                            fos = new FileOutputStream(fm);
                            fos.write(mie.getData());
                        } catch (FileNotFoundException ex) {
                            java.util.logging.Logger.getLogger(BootstrapServerMysql.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (IOException ex) {
                            java.util.logging.Logger.getLogger(BootstrapServerMysql.class.getName()).log(Level.SEVERE, null, ex);
                        } finally {
                            if (fos != null) {
                                try {
                                    fos.close();
                                } catch (IOException ex) {
                                    java.util.logging.Logger.getLogger(BootstrapServerMysql.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                        }

                    }
                }
            } else {
                // load the existing torrent files
                ActiveTorrents.updateTorrentLibrary();
                List<String> videos = ActiveTorrents.getListVideos();

                for (String video : videos) {
                    ActiveTorrents.TorrentEntry te = ActiveTorrents.getTorrentEntry(video);
                    String fname = te.getTorrentFilename();
                    FileInputStream fis = new FileInputStream(fname);
                    MetaInfoExec mie = new MetaInfoExec(fis, fname);
                    StorageMemMapWholeFile se = new StorageMemMapWholeFile(mie, VodConfig.getTorrentDir());
                    se.check(false); // true - very slow
                    metaInfos.add(mie);
                    storageInfos.add(se);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(MemoryMappedFileLoader.class.getName()).log(Level.SEVERE, null, ex);
        }

        int sizeInKb = 1024 * 1024 * 1;
        sizeInKb -= 1024 * 500;
        Buffer buf = new CircularFifoBuffer(sizeInKb);
        long i = 0;

        long startTime = System.currentTimeMillis();
        while (true) {
            i = (i == Long.MAX_VALUE) ? Long.MIN_VALUE : i + 1;
            try {
                int f = Math.abs(r.nextInt() % metaInfos.size());
                Storage se = storageInfos.get(f);
                if (se == null) {
                    throw new NullPointerException("Storage not found");
                }
                long sz = se.getLength();
                int pieceId = (int) ((Math.abs(r.nextLong()) % sz) / 1024);
                byte[] piece = se.getSubpiece(pieceId);
                buf.add(piece);
                if (i % 1024 == 0) {
                    System.out.println(new Date(System.currentTimeMillis()) + " read (MB): "
                            + Long.toString(i / 1024l) + " time taken: " + (System.currentTimeMillis() - startTime));
                }
            } catch (IOException ex) {
                Logger.getLogger(MemoryMappedFileLoader.class.getName()).log(Level.SEVERE, null, ex);
                System.exit(-1);
            }
        }
    }
}