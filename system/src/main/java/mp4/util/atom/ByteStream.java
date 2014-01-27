package mp4.util.atom;

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.IOException;

/**
 * A byte stream class contains the data from the mp4 file.  We use this
 * class instead of the byte[] because we need
 *  - an array that can grow & shrink
 *  - to keep track of the current index value in order to append easily
 *  - create a stream that's larger than 2B elements (Integer.MAX_VALUE)
 */
public class ByteStream {
  // the byte stream data
  private byte[] data;
  // the number of bytes used in the stream
  private int used;
  
  // Initial size if one is not specified
  private static final int INIT_SIZE = 4096;
  // The amount we increase the array by
  private static final int GROW_FACTOR = 2;
  
  /**
   * Construct an empty byte stream with the default initial size
   */
  public ByteStream() {
    this(INIT_SIZE);
  }
  
  /**
   * Construct an empty byte stream with the specified initial size
   * @param size the initial size
   */
  public ByteStream(long size) {
    if (size > Integer.MAX_VALUE) {
      throw new AtomError("Unable to handle data size larger than int");
    }
    data = new byte[(int) size];
    this.used = 0;
  }
  
  /**
   * Copy constructor for a byte stream.
   * @param old the stream to copy
   */
  public ByteStream(ByteStream old) {
    data = new byte[old.data.length];
    System.arraycopy(old.data, 0, data, 0, data.length);
    used = old.used;
  }
  
  /**
   * Return the space used by the byte stream
   * @return the space used by the byte stream
   */
  public long length() {
    return used;
  }
  
  /**
   * Reserve space in the byte stream
   * @param size the number of bytes to reserve
   */
  public void reserveSpace(long size) {
    if (used + size > data.length) {
      grow();
    }
    used += size;
  }
  
  /**
   * Read data from an input stream into the byte stream
   * @param in the input stream
   * @return the number of bytes read
   */
  public int read(DataInputStream in) throws IOException {
    int num = in.read(data);
    used += num;
    return num;
  }
  
  /**
   * Add a byte to the end of the array
   * @param b the byte to add
   */
  public void addData(byte b) {
    data[used++] = b;
    if (used == data.length) {
      grow();
    }
  }
 
  /**
   * Add a byte to the byte stream at the specified offset.
   * @param offset the offset from the start
   * @param b the value to add
   */
  public final void addData(int offset, byte b) {
    assert offset + 1 <= used;
    data[offset] = b;
  }
  
  /**
   * Add a byte array to the end of the byte stream
   * @param b the byte array
   * @param len the number of bytes from the byte array 
   */
  public final void addData(byte[] b, int len) {
    if (used + len >= data.length) {
      grow();
    }
    System.arraycopy(b, 0, data, used, len);
  }
  
  /**
   * Add the data to the byte stream at the specified offset
   * @param offset the offset in the byte stream
   * @param b the byte array whose contends are added to the byte stream
   */
  public final void addData(int offset, byte[] b) {
    assert offset + b.length <= used;
    System.arraycopy(b, 0, data, offset, b.length);
  }
  
  /**
   * Get the byte value at the specified offset in the stream
   * @param offset the offset
   * @return the byte value at the specified offset
   */
  public final byte getData(int offset) {
    assert offset <= used;
    return data[offset];
  }
  
  /**
   * Return data of the specified size in a byte array.
   * @param from the starting offset of the data
   * @param to the ending offset of the data
   * @return a copy of the byte data from the stream
   */
  public final byte[] getData(int from, int to) {
    assert from + to <= used;
    // copyOfRange not supported in JDK1.5
    //return Arrays.copyOfRange(data, from, to);
    byte[] na = new byte[to-from];
    System.arraycopy(data, from, na, 0, to-from);
    return na;
  }
    
  /**
   * Java doesn't have unsigned types, so we need to use the next
   * larger signed type.
   * @param b the byte array
   * @param off offset to start the conversion
   * @return the unsigned integer value of the byte array
   */
  public final long getUnsignedInt(int off) {
    return ((long)(data[off] & 0xff) << 24) |
    ((long)(data[off+1] & 0xff) << 16) |
    ((long)(data[off+2] & 0xff) << 8) |
    (long)(data[off+3] & 0xff);  
  }
  

  /**
   * Java doesn't have unsigned types, so we need to use the next
   * larger signed type.
   * @param b the byte array
   * @param off offset to start the conversion
   * @return the unsigned integer value of the byte array
   */
  public final int getUnsignedShort(int off) {
    return ((data[off] & 0xff) << 8) | (data[off+1] & 0xff);  
  }
  
  /**
   * Java doesn't have unsigned types, so we need to use the next
   * larger signed type.
   * @param b the byte array
   * @param off offset to start the conversion
   * @return the unsigned integer value of the byte array
   */
  public final long getLong(int off) {
    return ((long)(data[off] & 0xff) << 56) |
    ((long)(data[off+1] & 0xff) << 48) |
    ((long)(data[off+2] & 0xff) << 40) |
    ((long)(data[off+3] & 0xff) << 32) |
    ((long)(data[off+4] & 0xff) << 24) |
    ((long)(data[off+5] & 0xff) << 16) |
    ((long)(data[off+6] & 0xff) << 8) |    
    (long)(data[off+7] & 0xff);  
  }
  
  /**
   * Get a fixed point value from two 16 bit values located in a
   * 32-bit word.
   * @param off the offset in the byte array where value is located
   * @return the fixed point value of the 32-bit data.
   */
  public final double getFixedPoint(int off) {
    int integerPart = ((data[off] & 0xff) << 8) |
      ((data[off+1] & 0xff));
    int fractionPart = ((data[off+2] & 0xff) << 8) |
      ((data[off+3] & 0xff));
    double val = Double.valueOf(integerPart + "." + fractionPart).doubleValue();
    return val;
  }
  
  /**
   * Add a fixed point value to the byte stream at the specified index.
   * @param off the offset
   * @param integerPart the integer part of the fixed point value
   * @param fractionPart the fraction part of the fixed point value
   */
  public void addFixedPoint(int off, int integerPart, int fractionPart) {
    addUnsignedShort(off, integerPart);
    addUnsignedShort(off+2, fractionPart);
  }
  
  /**
   * Add an unsigned integer (4 bytes) to the byte stream.
   * @param data the data
   */
  public void addUnsignedInt(long val) {
    used += 4;
    if (used >= data.length) {
      grow();
    }
    addUnsignedInt(0, val);
  }
  
  /**
   * Add an unsigned integer to the byte stream at the specified offset.
   * This method assumes the space has already been allocated by advancing
   * the used pointer.
   * @param offset the offset from the start of the byte stream
   * @param val the integer value to add to the stream
   */
  public void addUnsignedInt(int offset, long val) {
    if (offset + 4 > used) {
      throw new AtomError("Not enough space allocated for the data");
    }
    data[offset++] = (byte) ((val >> 24) & 0xff);
    data[offset++] = (byte) ((val >> 16) & 0xff);
    data[offset++] = (byte) ((val >> 8) & 0xff);
    data[offset] = (byte) (val & 0xff);
  }
  /**
   * Add along to byte stream at the specified offset.
   * This method assumes the space has already been allocated by advancing
   * the used pointer.
   * @param offset the offset from the start of the byte stream
   * @param val the integer value to add to the stream
   */
  public void addLong(int offset, long val) {
    if (offset + 8 > used) {
      throw new AtomError("Not enough space allocated for the data");
    }
    data[offset++] = (byte) ((val >> 56) & 0xff);
    data[offset++] = (byte) ((val >> 48) & 0xff);
    data[offset++] = (byte) ((val >> 40) & 0xff);
    data[offset++] = (byte) ((val >> 32) & 0xff);    
    data[offset++] = (byte) ((val >> 24) & 0xff);
    data[offset++] = (byte) ((val >> 16) & 0xff);
    data[offset++] = (byte) ((val >> 8) & 0xff);
    data[offset] = (byte) (val & 0xff);
  }
  
  /**
   * Add an unsigned short (16-bits) to the byte stream at the specified offset.
   * @param offset the byte stream offset
   * @param val the value to add.
   */
  public void addUnsignedShort(int offset, int val) {
    if (offset + 2 > used) {
      throw new AtomError("Not enough space allocated for the data");
    }
    data[offset++] = (byte) ((val >> 8) & 0xff);
    data[offset] = (byte) (val & 0xff);
  }
  
  /**
   * Write the byte stream data to the specified location.
   * @param out where the data goes
   * @throws IOException if there is an error writing the data
   */
  public void writeData(DataOutput out) throws IOException {
    out.write(data, 0, used);
  }
  
  /**
   * Grow the array and copy the data from the old to the new array
   */
  private void grow() {
    byte[] newdata = new byte[data.length * GROW_FACTOR];
    System.arraycopy(data, 0, newdata, 0, used);
    data = newdata;
  }
  
  public void collapse64To32(int offset) {
	    System.arraycopy(data, offset+4, data, offset, used-(offset+4));
	    used -= 4;
  }
}
