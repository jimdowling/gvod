/**
 * 
 */
package mp4.util.atom;

/**
 * Sync sample atoms identify key frames in the media.
 */
public class StssAtom extends LeafAtom {
  private static final int ENTRIES_OFFSET = 4;
  private static final int TABLE_OFFSET = 8;
  private static final int ENTRY_SIZE = 4;
  private static final int KEY_FRAME = 0;
  
  /**
   * Construct an empty stts atom
   */
  public StssAtom() {
    super(new byte[]{'s','t','s','s'});
  }
  
  /**
   * Copy constructor.  Performs a deep copy.
   * @param old the version to copy
   */
  public StssAtom(StssAtom old) {
    super(old);
  }
  
  /**
   * Allocate space for the atom's data
   */
  @Override
  public void allocateData(long numEntries) {
    long size = TABLE_OFFSET + (numEntries * ENTRY_SIZE);
    super.allocateData(size);
  }

  /**
   * Return the number of entries in the sync sample table
   * @return the number of entries in the able
   */
  public long getNumEntries() {
    return data.getUnsignedInt(ENTRIES_OFFSET);
  }
  
  /**
   * Set the number of entries in the sync sample table.
   * @param numEntries the number of entries
   */
  public void setNumEntries(long numEntries) {
    data.addUnsignedInt(ENTRIES_OFFSET, numEntries);
  }
  
  /**
   * Return the ith entry in the table
   * @param i the entry number
   * @return the ith entry in the table
   */
  public long getSampleEntry(long i) {
    return data.getUnsignedInt(TABLE_OFFSET + ((int)i * ENTRY_SIZE) + KEY_FRAME);
  }

  /**
   * Set the ith entry in the table.
   * @param index the table index number
   * @param keyFrame the value inserted in to the table
   */
  public void setSampleEntry(int index, long keyFrame) {
    data.addUnsignedInt(TABLE_OFFSET + (index * ENTRY_SIZE) + KEY_FRAME, keyFrame);
  }
  
  /**
   * Return the key frame for the specified index.
   * @param sampleNum the sample number
   * @return the key frame
   */
  public long getKeyFrame(long sampleNum) {
    if (sampleNum > Integer.MAX_VALUE) {
      return 0;
    }
    long numEntries = getNumEntries();
    long lastKeyFrame = 0;
    for (long i = 0; i < numEntries; i++) {
      long keyFrame = getSampleEntry(i);
      if (sampleNum < keyFrame) {
        return lastKeyFrame;
      }
      lastKeyFrame = keyFrame;
    }
    return sampleNum;
  }
  
  /**
   * Cut the stss table at the specified sample point and create
   * a new atom with the subsequent entries
   * @param bs the byte stream with the new data
   * @param sampleNum the sample number 
   * @return the new stss atom
   */
  public StssAtom cut(long sampleNum) {
    // find the first entry
    long numEntries = getNumEntries();
    long i;
    for (i = 0; i < numEntries; i++) {
      if (sampleNum == getSampleEntry(i)) {
        break;
      }
    }
    assert sampleNum == getSampleEntry(i);
    StssAtom cutStss = new StssAtom();
    // create the new table
    cutStss.allocateData(numEntries - i);
    cutStss.setNumEntries(numEntries - i);
    for (int entryNumber = 0; i < numEntries; i++, entryNumber++) {
      cutStss.setSampleEntry(entryNumber, getSampleEntry(i) - sampleNum + 1);
    }
    return cutStss;
  }
  
  @Override
  public void accept(AtomVisitor v) throws AtomException {
    v.visit(this); 
  }
}