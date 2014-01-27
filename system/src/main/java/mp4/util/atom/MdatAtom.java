/**
 * 
 */
package mp4.util.atom;

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;

/**
 * The media data atom.  This atom holds the video frames and sound tracks.
 */
public class MdatAtom extends LeafAtom {
    // the input stream contains the data.  we use the stream instead of a data file
    // because the amount of data is so large.

    private static final int BUFFER_SIZE = 1024 * 1024;
    private DataInputStream in;
    private boolean hasMark = false;
    byte[] input = new byte[BUFFER_SIZE];

    /**
     * Construct an empty mdat atom
     */
    public MdatAtom() {
        super(new byte[]{'m', 'd', 'a', 't'});
    }

    /**
     * Copy constructor for the mdat atom.  Performs a deep copy
     * @param old the version to copy
     */
    public MdatAtom(MdatAtom old) {
        super(old);
    }

    /**
     * Set the data input stream for the mdata atom.  The input stream points
     * at the start of the video and sound data.
     * @param in the input stream.
     */
    public void setInputStream(DataInputStream in) {
        this.in = in;
        if (in.markSupported()) {
            in.mark(Integer.MAX_VALUE);
            hasMark = true;
        }
    }

    /**
     * Cut the mdat atom by skipping the specified number of bytes.  This
     * action does not create a new mdat atom.  Instead, it alters the existing
     * atom to skip over the appropriate data.
     * @param skip the number of bytes to skip
     * @return an altered mdat atom that has skipped over the approriate data.
     */
    public MdatAtom cut(long skip) {
        try {
            if (hasMark) {
                in.reset();
            }
            in.skip(skip);
            if (in.markSupported()) {
                in.mark(Integer.MAX_VALUE);
            }
            if (dataSize() > 0) {
                long newSize = dataSize() - skip;
                setSize(ATOM_HEADER_SIZE + newSize);
            }
        } catch (IOException e) {
            throw new AtomError("Unable to cut the mdat atom");
        }
        return this;
    }

    /**
     * TODO
    MdatAtom.writeData(DataOutput out) reads too much data (fills the whole
    buffer), and writes them all (even data past the numBytesToRead limit). use
    something like this instead:
    out.write(input, 0, readToEof?read:(int)Math.min(read, numBytesToRead));
     *
     *
     * Write the video and sound data to the specified output
     * @param out the specified output
     * @throws IOException if there is a problem writing the data
     */
    @Override
    public void writeData(DataOutput out) throws IOException {
        writeHeader(out);
        long numBytesToRead = dataSize();
        // read data in using 1 MB chunks
        boolean readToEof = (numBytesToRead == 0);
        while (readToEof || numBytesToRead > 0) {


            int read = in.read(input);
            if (read < 1) {
                break;
            }
            if (read > numBytesToRead) {
                out.write(input, 0, (int) numBytesToRead);
            } else {
                out.write(input, 0, read);
            }
            numBytesToRead -= read;
        }
    }

    public void writeChunk(DataOutput out, long offset, long size) throws IOException {
        if (!hasMark) {
            in.mark(Integer.MAX_VALUE);
            hasMark = true;
        }
        in.reset();
        in.mark(Integer.MAX_VALUE);
        in.skip(offset);
        while (size > 0) {
            int read = in.read(input, 0, (int) Math.min(size, input.length));
            out.write(input, 0, read);
            size -= read;
        }

    }

    @Override
    public void accept(AtomVisitor v) throws AtomException {
        v.visit(this);
    }

    @Override
    public void writeHeader(DataOutput out) throws IOException {
        byte[] sizeData = new byte[ATOM_WORD];
        //unsignedIntToByteArray(sizeData, 0, size);
        // size 0 means to-the-end-of-file
        Arrays.fill(sizeData, (byte) 0);
        out.write(sizeData);
        out.write(type);
    }

    public void writeHeader(DataOutput out, long len) throws IOException {
        byte[] sizeData = new byte[ATOM_WORD];
        unsignedIntToByteArray(sizeData, 0, len);
        // size 0 means to-the-end-of-file
//        Arrays.fill(sizeData, (byte) 0);
        out.write(sizeData);
        out.write(type);
    }
}
