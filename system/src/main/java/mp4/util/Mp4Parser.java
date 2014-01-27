package mp4.util;

import java.io.DataInputStream;
import java.io.IOException;

import mp4.util.atom.Atom;
import mp4.util.atom.AtomException;
import mp4.util.atom.ContainerAtom;
import mp4.util.atom.DefaultAtomVisitor;
import mp4.util.atom.FtypAtom;
import mp4.util.atom.MdatAtom;
import mp4.util.atom.MoovAtom;

public class Mp4Parser extends DefaultAtomVisitor {

    protected FtypAtom ftyp;
    protected MoovAtom moov;
    protected MdatAtom mdat;
    protected DataInputStream mp4file;
    boolean lastAtom = false;
    private long lastAtomOffset = 0;
    private int indent = 0;
    private String classPrefix = "";

    protected Mp4Parser() {
    }

    public Mp4Parser(DataInputStream mp4file) {
        this.mp4file = mp4file;
    }

    @Override
    protected void defaultAction(Atom atom) throws AtomException {

        // Even some (hybrid) containers have data, so read it
        atom.readData(mp4file);
        lastAtomOffset += atom.pureDataSize();
        if (atom.isContainer()) {
            indent++;
            String oldClassPrefix = classPrefix;
            String cname = atom.getClass().getCanonicalName();
            classPrefix = classPrefix + cname.substring(cname.lastIndexOf('.') + 1, cname.length() - 4) + ".";
            long bytesToRead = atom.dataSize() - atom.pureDataSize();
            while (bytesToRead >= Atom.ATOM_HEADER_SIZE) {
                Atom child = parseAtom();
                ((ContainerAtom) atom).addChild(child);
                //bytesRead += child.size();
                bytesToRead -= child.size();
            }
            // If there is not enough room for a full atom, just skip extra data
            // Some odd atoms are containers but terminate with 4 zero bytes.
            // This should handle those cases
            if (bytesToRead > 0) {
                MP4Log.log("Skipping extra container bytes: " + bytesToRead);
                try {
                    mp4file.skipBytes((int) bytesToRead);
                    lastAtomOffset += bytesToRead;
                } catch (IOException e) {
                    throw new AtomException(e.getMessage());
                }
            }
            indent--;
            classPrefix = oldClassPrefix;
        }
    }

    /**
     * Don't read the mdat atom since that's the biggest segment of the
     * file.  It contains the video and sound data.  Plus, we'll just
     * skip over the beginning when we cut the movie.
     */
    @Override
    public void visit(MdatAtom atom) throws AtomException {
        atom.setInputStream(mp4file);
        try {
            if (atom.dataSize() != 0 && mp4file.markSupported()) {
                mp4file.skip(atom.dataSize());
            } else {
                lastAtom = true;
            }
        } catch (IOException e) {
            throw new AtomException(e.getMessage());
        }
        lastAtomOffset += atom.dataSize();
    }

    private String getPrefix() {
        //String ps = classPrefix.length() > 0 ? classPrefix.substring(0, classPrefix.length()-1) + ":"  : "";
        String ps = "";
        StringBuilder x = new StringBuilder(ps);
        for (int i = 0; i < indent; i++) {
            x.append(" ");
        }
        return x.toString();
    }

    /**
     * Parse an atom from the mpeg4 file.
     *
     * @return the number of bytes read
     * @throws AtomException
     */
    private Atom parseAtom() throws AtomException {
        // get the atom size
        if (lastAtom) {
            throw new AtomException("Already parsed last atom!");
        }
        long aoff = lastAtomOffset;
        //MP4Log.log("Reading atom at offset: " + lastAtomOffset);
        byte[] word = new byte[Atom.ATOM_WORD];
        int num;
        try {
            num = mp4file.read(word);
        } catch (IOException e1) {
            throw new AtomException("IOException while reading file");
        }
        // check for end of file
        if (num == -1) {
            return null;
        }
        if (num != Atom.ATOM_WORD) {
            throw new AtomException("Unable to read enough bytes for atom");
        }
        long size = Atom.byteArrayToUnsignedInt(word, 0);
        // get the atom type
        try {
            num = mp4file.read(word);
        } catch (IOException e1) {
            throw new AtomException("IOException while reading file");
        }
        if (num != Atom.ATOM_WORD) {
            throw new AtomException("Unable to read enough bytes for atom");
        }
        lastAtomOffset += Atom.ATOM_HEADER_SIZE;
        // handle 64-bit data
        boolean isBig = false;
        if (size == 1) {
            isBig = true;
            byte[] bigSize = new byte[Atom.LARGE_SIZE_SIZE];
            try {
                num = mp4file.read(bigSize);
            } catch (IOException e1) {
                throw new AtomException("IOException while reading file");
            }
            if (num != Atom.LARGE_SIZE_SIZE) {
                throw new AtomException("Unable to read enough bytes for atom");
            }
            size = Atom.byteArrayToLong(bigSize, 0);
            lastAtomOffset += Atom.LARGE_SIZE_SIZE;
        }

        Atom atom;
        atom = Atom.typeToAtom(word, classPrefix);
        /*
        if (isBig)
        MP4Log.log(getPrefix() + "^Large size atom");
         */
        String atomName = atom.getClass().getCanonicalName();
        String lgs = "";
        if (isBig) {
            lgs = "(LRG)";
        }
        String typeForClass = Atom.typeToClassName(word, null);

        MP4Log.log(getPrefix() + atomName + "(" + ((int) word[0] & 0xff) + "," + ((int) word[1] & 0xff) + "," + ((int) word[2] & 0xff) + "," + ((int) word[3] & 0xff) + "): " + typeForClass.substring(typeForClass.lastIndexOf('.') + 1) + " (offset: " + aoff + ", size" + lgs + ":" + size + ")");
        //MP4Log.log(getPrefix() + "UnknownAtom(" + ((int)word[0]&0xff) + "," + ((int)word[1]&0xff) + "," + ((int)word[2]&0xff) + "," + ((int)word[3]&0xff) + "): " + Atom.typeToClassName(word) +  " (size:" + size + ")");

        atom.setLargeAtom(isBig);
        atom.setSize(size);
        atom.accept(this);
        return atom;
    }

    public long parseMp4() throws AtomException, IOException {
        long mdatOffset = 0;
        long offset = 0;
        while (ftyp == null || moov == null || mdat == null) {
            Atom atom = parseAtom();
            if (atom == null) {
                throw new IOException("Couldn't find all required MP4 atoms");
            }
            if (atom instanceof FtypAtom) {
                ftyp = (FtypAtom) atom;
            } else if (atom instanceof MoovAtom) {
                moov = (MoovAtom) atom;
            } else if (atom instanceof MdatAtom) {
                mdatOffset = offset;
                // mdatOffset is the start of the mdat atom's data section. 
                mdat = (MdatAtom) atom;
            }
            offset += atom.size();
        }
        return mdatOffset;
    }

    public MoovAtom getMoov() {
        return moov;
    }
}
