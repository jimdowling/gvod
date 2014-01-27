package mp4.util.atom;

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;

/**
 * This class represents an atom in the mpeg-4 file.  See the mpeg-4 file
 * documentation:
 * <ul>
 *   <li><a href="http://standards.iso.org/ittf/PubliclyAvailableStandards/index.html">ISO Standard 14496-12:2005</a>
 *   <li><a href="developer.apple.com/DOCUMENTATION/QuickTime/QTFF/qtff.pdf ">QuickTime File Format Specification</a>
 * </ul>
 * 
 * In the ISO spec, an Atom is a Box.  We use the QuickTime name, Atom.
 */
public abstract class Atom {
	
  // the raw mpeg4 data for the atom
  protected ByteStream data;

  // the size of the atom, an unsigned int so we use a long
  protected long size;
  // the type, represented using the characters in the byte stream
  protected byte[] type;
  
  // The basic unit size of an atom, in bytes
  public static final int ATOM_WORD = 4;
  // Number of bytes for 64-bit atom size
  public static final int LARGE_SIZE_SIZE= 8;  
  // The canonical atom size, which includes the type and the size
  public static final int ATOM_HEADER_SIZE = 8;

  public static final byte COPYRIGHT_BYTE_VALUE = -87;

  protected int headerSize = ATOM_HEADER_SIZE;
  protected boolean is64BitAtom = false;
  
  /**
   * Create an atom with the specified size and type
   * @param size the size inclusive of the size and type 
   * @param type the atom's type
   */
  protected Atom(long size, byte[] type) {
    this.size = size;
    this.type = type;
  }
  
  /**
   * Copy constructor.
   * @param old the atom to copy
   */
  protected Atom(Atom old) {
    this.size = old.size;
    this.type = old.type;
  }
  
  /**
   * Create an atom but the size isn't specified, and needs
   * to be filled in later.
   * @param type the atom's type
   */
  protected Atom(byte[] type) {
    this.type = type;
  }
  
  /**
   * Set the size of the atom
   * @param size the atom's size
   */
  public void setSize(long size) {
    this.size = size;
  }
  
  /**
   * Return the size of the atom
   * @return
   */
  public long size() {
    return size;
  }  
  /**
   * Return the size of the data part of the atom
   * @return the size of the atom's data part
   */
  public long dataSize() {
	if (size == 0)
		return 0;
    return size - headerSize;
  }
  
  public long pureDataSize() {
	  return dataSize();
  }

 /**
   * Return the atom's type as an integer
   * @return the atom's type as an integer
   */
  public int getType() {
    return getAtomValue(type);
  }
  
  /**
   * @return true if this atom is a container atom
   */
  public abstract boolean isContainer();
  
  /**
   * The visitor pattern accept method
   * @param v an atom visitor
   */
  public abstract void accept(AtomVisitor v) throws AtomException;
  
  
  /**
   * Write the atom header data to the output stream.  The header data
   * includes the size and type information.
   * @param out where the output goes
   * @throws IOException if there is an error writing the data
   */
  public void writeHeader(DataOutput out) throws IOException {
    byte[] sizeData = new byte[ATOM_WORD];
    unsignedIntToByteArray(sizeData, 0, size);
    out.write(sizeData);
    out.write(type);
  }
  
  /**
   * Return the atom as a string
   * @return the string for the atom
   */
  public String toString() {
    return "Atom " + new String(type) + " size " + size;
  }
  
  /**
   * Utility function that converts a byte array to an integer starting
   * at the specified array index.
   * The byte array must be at least 4 bytes in length.
   * @param b the byte array
   * @param off offset to start the conversion
   * @return the integer value of the byte array
   */
  public static final int byteArrayToInt(byte[] b, int off) {
    return (int) byteArrayToUnsignedInt(b, off);
  }
  
  /**
   * Java doesn't have unsigned types, so we need to use the next
   * larger signed type.
   * @param b the byte array
   * @param off offset to start the conversion
   * @return the unsigned integer value of the byte array
   */
  public static final long byteArrayToUnsignedInt(byte[] b, int off) {
    return ((long)(b[off] & 0xff) << 24) |
    ((long)(b[off+1] & 0xff) << 16) |
    ((long)(b[off+2] & 0xff) << 8) |
    (long)(b[off+3] & 0xff);  
  }
  

  /**
   * Java doesn't have unsigned types, so we need to use the next
   * larger signed type.
   * @param b the byte array
   * @param off offset to start the conversion
   * @return the unsigned integer value of the byte array
   */
  public static final long byteArrayToLong(byte[] b, int off) {
    return ((long)(b[off] & 0xff) << 56) |
    ((long)(b[off+1] & 0xff) << 48) |
    ((long)(b[off+2] & 0xff) << 40) |
    ((long)(b[off+3] & 0xff) << 32) |
    ((long)(b[off+4] & 0xff) << 24) |
    ((long)(b[off+5] & 0xff) << 16) |
    ((long)(b[off+6] & 0xff) << 8) |
    (long)(b[off+7] & 0xff);  
  }
  
  /**
   * Write the unsigned int to the byte array
   * @param b the byte array
   * @param off the offset into the byte array
   * @param data the data
   */
  public static final void unsignedIntToByteArray(byte b[], int off, long data) {
    b[off] = (byte) ((data >> 24) & 0xff);
    b[off+1] = (byte) ((data >> 16) & 0xff);
    b[off+2] = (byte) ((data >> 8) & 0xff);
    b[off+3] = (byte) (data & 0xff);
  }
    
  /**
   * Convert the atom type characters to an integer value
   * @param val the characters
   * @return the integer
   */
  public static final int getAtomValue(byte[] val) {
    return Atom.byteArrayToInt(val, 0);
  }
  
  /**
   * Convert the type name to a class name.  The class name converts
   * the first character of the type to uppercase, then prepends
   * the package name, and appends 'Atom' string.
   * 
   * @param typ the type represented as a byte array
   * @return the class name for the type
   */
  public static String typeToClassName(byte[] typ, String prefix) {
  if (prefix == null)
	  prefix = "";
    String str = new String(typ);
    // \u00a9
    StringBuilder strBuffer = new StringBuilder();
    for (int i=0;i<str.length();i++) {
	    Character firstChar = str.charAt(i);
	    String firstStr;
	    if (typ[i] == COPYRIGHT_BYTE_VALUE) // (c) copyright sign
	    	firstStr = "Cprt";
	    else if (Character.isLetter(firstChar)) {
	    	firstStr= Character.toString(firstChar);
	    	if (i == 0)
	    		firstStr = firstStr.toUpperCase();
	    }
	    else if (Character.isDigit(firstChar)) {
	    	if (i == 0)
	    		firstStr = "N" + firstChar;
	    	else
	    		firstStr = firstChar.toString();
	    }
	    else
	    	firstStr = "X" + (((int)typ[i])&0xff);
	    strBuffer.append(firstStr);
	    
    }
    String clsName = strBuffer.toString();
    return "mp4.util.atom." + prefix + new String(clsName) + "Atom";
  }
  
  /**
   * Write the byte stream to the specified output.
   * @param out where the output goes
   * @throws IOException if there is a problem writing the data
   */
  public void writeData(DataOutput out) throws IOException {
    writeHeader(out);
    if (data != null && data.length() > 0)
    	data.writeData(out);
  }
  
  /**
   * Read the data from the input stream in to the atom.
   * @param in the input stream
   * @throws AtomException
   */
  public void readData(DataInputStream in) throws AtomException {
	  data = new ByteStream(pureDataSize());
	  if (pureDataSize() > 0) {
	    try {
	      data.read(in);
	    } catch (IOException e) {
	      throw new AtomException("IOException while reading mp4 file");
	    }
	}
  }
  
  /**
   * Allocate space for the data needed by the atom.
   * @param size the size of data in bytes
   */
  public void allocateData(long size) {
    assert data == null;
    data = new ByteStream(size);
    data.reserveSpace(size);
    setSize(size + headerSize);
  }
  
  public boolean isLargeAtom() {
	  return is64BitAtom;
  }
  
  public void setLargeAtom(boolean big) {
	   is64BitAtom = big;
	  if (big)
		  headerSize = ATOM_HEADER_SIZE + LARGE_SIZE_SIZE;
	  else
		  headerSize = ATOM_HEADER_SIZE;
  }
  
  public static boolean typeEquals(byte[] t1, byte[] t2) {
	  return Arrays.equals(t1, t2);
  }

public static Atom typeToAtom(byte[] word, String classPrefix) throws AtomException {
	classPrefix = classPrefix.toLowerCase();
	try {
		Atom atom = null;
		String typeForClass = Atom.typeToClassName(word, classPrefix);
		try {
			Class<?> cls = Class.forName(typeForClass);
			atom = (Atom) cls.newInstance();
			//MP4Log.log(getPrefix() + "AtomClass: " + cls + " (size:" + size + ")");
		} catch (ClassNotFoundException e) {
			if (classPrefix != null && classPrefix.length() > 0) {
				typeForClass = Atom.typeToClassName(word, null);
				try {
					Class<?> cls = Class.forName(typeForClass);
					atom = (Atom) cls.newInstance();
					//MP4Log.log(getPrefix() + "AtomClass: " + cls + " (size:" + size + ")");
				} catch (ClassNotFoundException e1) {					
				}
			}
			if (atom == null)
				atom = new UnknownAtom(word);
		}
		return atom;
	} catch (InstantiationException e) {
		throw new AtomException("Unable to instantiate atom");
	} catch (IllegalAccessException e) {
		throw new AtomException("Unabel to access atom object");
	}

}

    public int getHeaderSize() {
        return headerSize;
    }

}