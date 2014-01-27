/**
 * 
 */
package mp4.util.atom;

/**
 * This atom represents the size of each sample in the media.
 */
public class StszAtom extends LeafAtom {
  private static final int SAMPLE_SIZE_OFFSET = 4;
  private static final int ENTRIES_OFFSET = 8;
  private static final int TABLE_OFFSET = 12;
  private static final int ENTRY_SIZE = 4;
  
  /**
   * Constructor for stsz atom
   */
  public StszAtom() {
    super(new byte[]{'s','t','s','z'});
  }
  
  /**
   * Copy constructor for stsz atom
   * @param old the object to copy
   */
  public StszAtom(StszAtom old) {
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
   * Get the sample size.  If this value is non-zero, then it is the size
   * of all the samples. If this value is zero, then the atom contains a
   * table with the size of each sample
   * @return the sample size entry in the atom
   */
  public long getSampleSize() {
    return data.getUnsignedInt(SAMPLE_SIZE_OFFSET);
  }
  
  /**
   * Set the sample size.
   * @param sampleSize the sample size
   */
  public void setSampleSize(long sampleSize) {
    data.addUnsignedInt(SAMPLE_SIZE_OFFSET, sampleSize);
  }
  
  /**
   * Return the number of entries in the table
   * @return the number of entries in the table
   */
  public long getNumEntries() {
    return data.getUnsignedInt(ENTRIES_OFFSET);
  }
  
  /**
   * Set the number of entries in the stsz atom table
   * @param numEntries the number of entries
   */
  public void setNumEntries(long numEntries) {
    data.addUnsignedInt(ENTRIES_OFFSET, numEntries);
  }
  
  /**
   * Return the size of the specified sample.  The sample numbers are 1 based.
   * @param sampleNum the sample numbers
   * @return the size of the sample
   */
  public long getTableSampleSize(long sampleNum) {
    if (sampleNum > Integer.MAX_VALUE) {
      return 0;
    }
    return data.getUnsignedInt(TABLE_OFFSET + ((int)(sampleNum - 1)* ENTRY_SIZE));
  }
  /**
   * Return the size of the specified sample.  The sample numbers are 1 based.
   * @param sampleNum the sample numbers
   * @return the size of the sample
   */
   public long getSampleSize(long sampleNum) {
	   if (getSampleSize() > 0)
		   return getSampleSize();
	   else
		   return getTableSampleSize(sampleNum);
   }
  /**
   * Set the sample size for the specified table entry
   * @param sampleNum the sample number
   * @param sampleSize the sample size
   */
  public void setTableSampleSize(int sampleNum, long sampleSize) {
    data.addUnsignedInt(TABLE_OFFSET + ((sampleNum - 1) * ENTRY_SIZE), sampleSize);
  }
  
  /**
   * Cut the stsz table at the specified sample.  Performs the update in place
   * and fills the rest of the atom with dummy data.
   * @param sampleNum the sample where the split occurs
   */
  public StszAtom cut(long sampleNum) {
    StszAtom cutStsz = new StszAtom();    
    long sampleSize = getSampleSize();
    if (sampleSize > 0) {
      cutStsz.allocateData(0);
      cutStsz.setSampleSize(sampleSize);
      cutStsz.setNumEntries(getNumEntries() - sampleNum + 1);
    }
    else {
      long numEntries = getNumEntries();
      cutStsz.allocateData(numEntries - sampleNum + 1);
      cutStsz.setSampleSize(0);
      cutStsz.setNumEntries(numEntries - sampleNum + 1);
      int entryNumber = 1;
      for (long i = sampleNum; i <= numEntries; i++, entryNumber++) {
        cutStsz.setTableSampleSize(entryNumber, getTableSampleSize(i));
      }
    }
    return cutStsz;
  }
  
  @Override
  public void accept(AtomVisitor v) throws AtomException {
    v.visit(this); 
  }
}