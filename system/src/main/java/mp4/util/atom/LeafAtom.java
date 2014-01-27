/**
 * 
 */
package mp4.util.atom;

public abstract class LeafAtom extends Atom {
  
  protected static final int VERSION_OFFSET = 0;
  protected static final int FLAG_OFFSET = 1;
  protected static final int FLAG_SIZE = 3;
  
  /**
   * Constructor for a leaf Atom
   * @param type the atom's type
   */
  protected LeafAtom(byte[] type) {
    super(type);
  }
  
  /**
   * Copy constructor for the leaf atom.  Performs a deep copy
   * @param old the version to copy
   */
  protected LeafAtom(LeafAtom old) {
    super(old);
    data = new ByteStream(old.data);
  }
  
  /**
   * Return the version value for the atom. Currently, we support only 0, which
   * means 
   * @return
   */
  public byte getVersion() {
    return data.getData(VERSION_OFFSET);
  }
  
  /**
   * Set the version value for the atom.
   * @param version the atom's version
   */
  public void setVersion(byte version) {
    data.addData(VERSION_OFFSET, version);
  }
  
  /**
   * Return the flag data from the atom as a byte array
   * @return the flag data from the atom as a byte array
   */
  public byte[] getFlag() {
    return data.getData(FLAG_OFFSET, FLAG_OFFSET+FLAG_SIZE);
  }
  
  /**
   * Add the flag data to the byte stream
   * @param flag the flag info
   */
  public void setFlag(byte[] flag) {
    data.addData(FLAG_OFFSET, flag);
  }
  
  /**
   * Return false since a leaf is not a container of other aotoms
   * @return false
   */
  public boolean isContainer() {
    return false;
  }
  
  public int getFlagValue() {
	  byte[] data = getFlag();
	    return ((int)(data[0] & 0xff) << 16) |
	    ((int)(data[1] & 0xff) << 8) |
	    (int)(data[2] & 0xff);  
  
  }
  
   
}