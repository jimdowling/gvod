package mp4.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import mp4.util.atom.AtomException;
import mp4.util.atom.TrakAtom;

public class JavaMP4Splitter extends MP4Streamer {

    public static class Splitter extends Mp4Split {

        private PipedInputStream pi;

        public Splitter(File f, long startPos, final boolean reinterleave)
                throws IOException {
            time = startPos / 1000.0f;
            int bufSize = 16384;
            // There is a lot of seeking for interleaving, don't use a big buffer
            if (reinterleave) {
                bufSize = 4096;
            }
            InputStream is = new RandomAccessFileInputStream(f, bufSize);
            mp4file = new DataInputStream(is);
            try {
                calcSplitMp4(reinterleave);
                /*
                TrakAtom trak = findAvc1Trak();
                if (trak != null)
                trak.getMdia().getMinf().getStbl().getStsd().getAvc1().getAvcc().setProfileCompatability(0);
                 */
                pi = new PipedInputStream();

                if (startPos < Long.MAX_VALUE) {
                    final PipedOutputStream po = new PipedOutputStream(pi);
                    // writeSplitMp4(new DataOutputStream(po));
                    (new Thread() {

                        @Override
                        public void run() {
                            try {
                                writeSplitMp4(new DataOutputStream(po));
                                mp4file.close();
                                po.close();

                            } catch (IOException e) {
//								Log.error("IOException: " + e);
                                try {
                                    po.close();
                                    mp4file.close();
                                } catch (IOException e1) {
                                }
                            }
                        }
                    }).start();
                } else {
                    mp4file.close();
                }
            } catch (IOException e) {
                try {
                    mp4file.close();
                } catch (IOException e1) {
                }
                throw e;
            }

        }

        public InputStream getInputStream() {
            return pi;
        }

        public float getCutDuration() {
            return (float) cutMoov.getMvhd().getDuration()
                    / (float) cutMoov.getMvhd().getTimeScale();
        }

        public int getWidth() {
            TrakAtom trak = findAvc1Trak();
            if (trak == null) {
                return 0;
            }
            int width = trak.getMdia().getMinf().getStbl().getStsd().getAvc1().getWidth();
            return width;
        }

        private TrakAtom findAvc1Trak() {
            Iterator<TrakAtom> it = cutMoov.getTracks();
            while (it.hasNext()) {
                TrakAtom a = it.next();
                if (a.getMdia().getHdlr().isVideo()) {
                    if (a.getMdia().getMinf().getStbl().getStsd().getAvc1() != null) {
                        return a;
                    }
                }
            }
            return null;
        }

        public int getProfileLevel() {
            TrakAtom trak = findAvc1Trak();
            if (trak == null) {
                return 0;
            }
            int p = trak.getMdia().getMinf().getStbl().getStsd().getAvc1().getProfileLevel();
            return p;
        }

        public int getProfile() {
            TrakAtom trak = findAvc1Trak();
            if (trak == null) {
                return 0;
            }
            int p = trak.getMdia().getMinf().getStbl().getStsd().getAvc1().getProfile();
            return p;
        }

        public int getHeight() {
            TrakAtom trak = findAvc1Trak();
            if (trak == null) {
                return 0;
            }
            int height = trak.getMdia().getMinf().getStbl().getStsd().getAvc1().getHeight();
            return height;
        }

        public List<String> getFormats() {
            List<String> formats = new ArrayList<String>();
            Iterator<TrakAtom> it = cutMoov.getTracks();
            while (it.hasNext()) {
                TrakAtom a = it.next();
                byte[] dformat = a.getMdia().getMinf().getStbl().getStsd().getDataFormat();
                if (dformat != null) {
                    String dfStr = new String(dformat);
                    formats.add(dfStr);
                }
            }
            return formats;
        }
    }
    Splitter split;

    private JavaMP4Splitter(Splitter sp) {
        super(sp.getInputStream());
        split = sp;
    }

    public JavaMP4Splitter(File f, long startPos, boolean reinterleave)
            throws IOException {
        this(new Splitter(f, startPos, reinterleave));
        /*
        getWidth();
        getHeight();
        getProfileLevel();
        getProfile();
        getFormats();
         */
        // mp4 = (StreamableMP4)this.in;
    }

    @Override
    public List<String> getFormats() {
        return split.getFormats();
    }

    @Override
    public int getHeight() {
        return split.getHeight();
    }

    @Override
    public int getProfile() {
        return split.getProfile();
    }

    @Override
    public int getProfileLevel() {
        return split.getProfileLevel();
    }

    @Override
    public long getSubDuration() {
        return (long) (split.getCutDuration() * 1000L);
    }

    @Override
    public int getWidth() {
        return split.getWidth();
    }
}
