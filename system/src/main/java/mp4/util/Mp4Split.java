package mp4.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;


import mp4.util.atom.Atom;
import mp4.util.atom.AtomException;
import mp4.util.atom.MdatAtom;
import mp4.util.atom.MoovAtom;
import mp4.util.atom.MvhdAtom;
import mp4.util.atom.TrakAtom;

/**
 * This class is used to split an mpeg4 file.
 *
 * The mpeg4 file format is the 
 * <a href="http://developer.apple.com/DOCUMENTATION/QuickTime/QTFF/qtff.pdf">Quicktime format</a>
 * 
 * The mpeg4 format is also ISO standard 14496-12.  That is, Part 12 of
 * the ISO mpeg4 specificatio.
 * 
 * Splitting the mpeg4 file requires rewritting the stbl atom container
 * with new data and cutting off the mdat section.
 */
public class Mp4Split extends Mp4Parser {

    public String inputFile;
    public String outputFile;
    public boolean writeMdat = true;
    public static float time;
    protected MoovAtom cutMoov;
    protected MdatAtom cutMdat;
    Mp4InterleaveWriter iwriter = null;
    protected boolean force32bit = false;
    protected boolean force32bit_times = false;

    public Mp4Split(DataInputStream mp4file) {
        super(mp4file);
    }

    public Mp4Split() {
    }

    /**
     * Constructor for the Mpeg-4 file splitter.  It opens the
     * @param fn
     * @throws IOException
     */
    /*
    public Mp4Split(String fn) {
    try {
    mp4file = new DataInputStream(new FileInputStream(fn));
    MP4Log.log("DBG: file size " + new File(fn).length());
    } catch (FileNotFoundException e) {
    MP4Log.log("File not found " + fn);
    System.exit(-1);
    }
    }
     */
  


    public long lenHeaders() throws IOException {
        return
                ftyp.size()
                + cutMoov.size()
                + cutMdat.getHeaderSize()
                ;
    }

    public long lenSplitMp4() throws IOException {
        return
                ftyp.size()
                + cutMoov.size()
                + cutMdat.size()
                ;
    }

    public void writeMdatHeader(DataOutputStream dos, long len) throws IOException {
        cutMdat.writeHeader(dos, len);
    }

    public void writeMdatBody(DataOutputStream dos) throws IOException {
        cutMdat.writeData(dos);
    }

    public void writeProgressiveMp4(DataOutputStream dos, long mDatLen) throws IOException {
        ftyp.writeData(dos);
        cutMoov.writeData(dos);
        long headerLen = ftyp.size() + cutMoov.size() + cutMdat.getHeaderSize();
        cutMdat.writeHeader(dos, mDatLen+headerLen);
        System.out.println("Header written len: " + headerLen + " Body:" + mDatLen);
    }

    public void writeSplitMp4(DataOutputStream dos) throws IOException {
        ftyp.writeData(dos);
        if (iwriter != null) {
            iwriter.write(dos, writeMdat);
        } else {
            cutMoov.writeData(dos);
            if (this.writeMdat) {
                cutMdat.writeData(dos);
            }
        }
    }

    public float getTimeFromByteOffset(long offset) {

        return 0.0f;
    }


    public void calcSplitMp4() throws IOException {
        calcSplitMp4(false);
    }

    /**
     *
     * @param reinterleave
     * @return file offset at which to start reading the media
     * @throws IOException
     */
    public long calcSplitMp4(boolean reinterleave) throws IOException {
        long mdatOffset = 0;
        try {
            mdatOffset = parseMp4();

                MP4Log.log("DBG: old mdat offset: " + mdatOffset);
            // Remove all but one video, one audio track
            // this makes some things a little easier
            boolean hasVideo = false;
            boolean hasAudio = false;
            Iterator<TrakAtom> it = moov.getTracks();
            while (it.hasNext()) {
                TrakAtom trak = it.next();
                if (!trak.isEnabled()) {
                    it.remove();
                    continue;
                }
                if (trak.getMdia().getHdlr().isVideo()) {
                    if (hasVideo) {
                        it.remove();
                    } else {
                        hasVideo = true;
                    }
                } else if (trak.getMdia().getHdlr().isSound()) {
                    if (hasAudio) {
                        it.remove();
                    } else {
                        hasAudio = true;
                    }
                } else {
                    it.remove();
                }
            }

            //float common = moov.findAdjustedTime(time);

            cutMoov = moov.cut(time);

            // Remove the IODS atom
            // because it may point to tracks we have already removed
            cutMoov.setIods(null);


            cutMoov.recomputeSize();

            long startMdataOffset = cutMoov.firstDataByteOffset();

            long mdatSkip = cutMoov.firstDataByteOffset()
                    - moov.firstDataByteOffset();
            cutMdat = mdat.cut(mdatSkip);

            int mdatHeaderSize = cutMdat.getHeaderSize();

            // update stco segment by mdatSkip + difference in moov size
            long newMoovSz = ftyp.size() + cutMoov.size();
            long updateAmount = mdatSkip + (mdatOffset - newMoovSz);
            if (mdat.isLargeAtom()) {
                updateAmount += Atom.LARGE_SIZE_SIZE;
                mdatHeaderSize+= Atom.LARGE_SIZE_SIZE;
            }

            MP4Log.log("DBG: updateAmount " + updateAmount);
            cutMoov.fixupOffsets(-updateAmount);


            MP4Log.log("DBG: movie skip " + mdatSkip);

            MP4Log.log("DBG: Cut Movie time "
                    + cutMoov.getMvhd().getDurationNormalized() + " sec ");

            if (reinterleave) {
                iwriter = new Mp4InterleaveWriter(cutMoov,
                        cutMdat,
                        ftyp.size() + cutMoov.size() + Atom.ATOM_HEADER_SIZE);
                iwriter.calcInterleave();
            } else {
                // In any case, mdat is going to be at the end, and 0 is a legal size in MP4s
                // meaning everything to the end of the file is part of the atom.  To simplify 64-bit handling, let's just do it.
                //cutMdat.setSize(0);
            }


            return startMdataOffset + mdatHeaderSize;
//            return updateAmount + mdatHeaderSize;
        } catch (AtomException e) {
            MP4Log.log("Error parseing Mp4 file " + e);
            throw new IOException(e.getMessage());
        }
    }

    public long progSplitMp4(boolean reinterleave) throws IOException {
        long mdatOffset = 0;
        try {
            mdatOffset = parseMp4();

                MP4Log.log("DBG: old mdat offset: " + mdatOffset);
            // Remove all but one video, one audio track
            // this makes some things a little easier
            boolean hasVideo = false;
            boolean hasAudio = false;
            Iterator<TrakAtom> it = moov.getTracks();
            while (it.hasNext()) {
                TrakAtom trak = it.next();
                if (!trak.isEnabled()) {
                    it.remove();
                    continue;
                }
                if (trak.getMdia().getHdlr().isVideo()) {
                    if (hasVideo) {
                        it.remove();
                    } else {
                        hasVideo = true;
                    }
                } else if (trak.getMdia().getHdlr().isSound()) {
                    if (hasAudio) {
                        it.remove();
                    } else {
                        hasAudio = true;
                    }
                } else {
                    it.remove();
                }
            }

            //float common = moov.findAdjustedTime(time);

            cutMoov = moov.cut(time);

            // Remove the IODS atom
            // because it may point to tracks we have already removed
            cutMoov.setIods(null);


            cutMoov.recomputeSize();

//            long startMdataOffset = cutMoov.firstDataByteOffset();

            long mdatSkip = cutMoov.firstDataByteOffset()
                    - moov.firstDataByteOffset();
            cutMdat = mdat.cut(mdatSkip);

            int mdatHeaderSize = cutMdat.getHeaderSize();

            // update stco segment by mdatSkip + difference in moov size
            long newMoovSz = ftyp.size() + cutMoov.size();
            long updateAmount = mdatSkip + (mdatOffset - newMoovSz);
            if (mdat.isLargeAtom()) {
                updateAmount += Atom.LARGE_SIZE_SIZE;
                mdatHeaderSize+= Atom.LARGE_SIZE_SIZE;
            }

            MP4Log.log("DBG: updateAmount " + updateAmount);
            cutMoov.fixupOffsets(-updateAmount);


            MP4Log.log("DBG: movie skip " + mdatSkip);

            MP4Log.log("DBG: Cut Movie time "
                    + cutMoov.getMvhd().getDurationNormalized() + " sec ");

            if (reinterleave) {
                iwriter = new Mp4InterleaveWriter(cutMoov,
                        cutMdat,
                        ftyp.size() + cutMoov.size() + Atom.ATOM_HEADER_SIZE);
                iwriter.calcInterleave();
            } else {
                // In any case, mdat is going to be at the end, and 0 is a legal size in MP4s
                // meaning everything to the end of the file is part of the atom.  To simplify 64-bit handling, let's just do it.
                //cutMdat.setSize(0);
            }


            return mdatSkip + mdatOffset + mdatHeaderSize;
//            return startMdataOffset + mdatHeaderSize;
//            return updateAmount + mdatHeaderSize;
        } catch (AtomException e) {
            MP4Log.log("Error parseing Mp4 file " + e);
            throw new IOException(e.getMessage());
        }
    }

    /*
    public void splitMp4() {
    try {
    FtypAtom ftyp = (FtypAtom) parseAtom();
    MoovAtom moov = (MoovAtom) parseAtom();
    MdatAtom mdat = (MdatAtom) parseAtom();

    MP4Log.log("DBG: moov size " + moov.dataSize());
    MP4Log.log("DBG: mdat size " + mdat.dataSize());

    MoovAtom cutMoov = moov.cut(time);
    MP4Log.log("DBG: moov chunk " + moov.firstDataByteOffset());
    MP4Log.log("DBG: cut moov chunk " + cutMoov.firstDataByteOffset());
    long mdatSkip = cutMoov.firstDataByteOffset() - moov.firstDataByteOffset();
    MdatAtom cutMdat = mdat.cut(mdatSkip);

    // update stco segment by mdatSkip + difference in moov size
    long updateAmount = mdatSkip + (moov.size() - cutMoov.size());

    MP4Log.log("DBG: updateAmount " + updateAmount);
    cutMoov.fixupOffsets(-updateAmount);

    MP4Log.log("DBG: movie skip " + mdatSkip);

    MP4Log.log("DBG: Cut Movie time " + cutMoov.getMvhd().getDurationNormalized() + " sec ");

    DataOutputStream dos = new DataOutputStream(new FileOutputStream(outputFile));
    ftyp.writeData(dos);
    cutMoov.writeData(dos);
    if (Mp4Split.mdat) {
    cutMdat.writeData(dos);
    }

    } catch (AtomException e) {
    MP4Log.log("Error parseing Mp4 file " + e);
    } catch (FileNotFoundException e) {
    MP4Log.log("Error creating file output stream");
    } catch (IOException e) {
    MP4Log.log("Error writing output ");
    e.printStackTrace();
    }
    }
     */
    /**
     * Process the command line arguments.
     * @param args the user-specified arguments
     */
    protected void processArgs(String[] args) {
        int i = 0;
        while (i < args.length) {
            String arg = args[i];
            if (arg.equals("-in")) {
                inputFile = args[++i];
            } else if (arg.equals("-out")) {
                outputFile = args[++i];
            } else if (arg.equals("-time")) {
                time = Float.valueOf(args[++i]);
            } else if (arg.equals("-no_mdat")) {
                writeMdat = false;
            } else {
                help();
            }
            i++;
        }
        if (inputFile == null) {
            help();
        }
    }

    private static void help() {
        MP4Log.log("Mp4Split <args>");
        MP4Log.log("  -in <inputfile.mp4>");
        MP4Log.log("  -out <outputfile.mp4>");
        MP4Log.log("  -time <seconds>");
        MP4Log.log("  [-no_mdat]");
        System.exit(-1);
    }

    public void runCmdLine(String[] args) {
        processArgs(args);
        try {
            mp4file = new DataInputStream(new FileInputStream(new File(inputFile)));
            calcSplitMp4();
            writeSplitMp4(new DataOutputStream(new FileOutputStream(new File(outputFile))));
        } catch (FileNotFoundException e) {
            MP4Log.log("Err: FileNoutFound: " + e.getMessage());
        } catch (IOException e) {
            MP4Log.log("Err: IOException: " + e.getMessage());
        }
        MP4Log.log("Complete.");
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        Mp4Split splitter = new Mp4Split();
        splitter.runCmdLine(args);
    }
}
