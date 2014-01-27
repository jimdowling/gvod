package mp4.util.atom;

/**
 * The time-to-sample atom is shared by the ctts and stts atoms, which
 * are similar in functionality.  The stts atom is the decoding
 * time-to-sample mapping atom.  The decoding time (DT) atom
 * gives the deltas between successive decoding times.  The ctts atom
 * is the composition time-to-sample atom.  The composition time (CT) atom
 * provides composition times.  When the decoding and composition times are 
 * the same, then the ctts atom is not present.
 */
public abstract class TimeToSampleAtom extends LeafAtom {
  protected static final int ENTRIES_OFFSET = 4;
  protected static final int TABLE_OFFSET = 8;
  protected static final int SAMPLE_COUNT = 0;
  // the semantics of SAMPLE_VALUE depends up on the concrete class
  protected static final int SAMPLE_VALUE = 4;
  protected static final int ENTRY_SIZE = 8;

  /**
   * Constructor passes argument to super class
   * @param type the atom type
   */
  protected TimeToSampleAtom(byte[] type) {
    super(type);
  }
  
  /**
   * Copy constructor.  Performs a deep copy.
   * @param old the atom to copy.
   */
  protected TimeToSampleAtom(TimeToSampleAtom old) {
    super(old);
  }
  /**
   * Allocate space for the data in the atom
   * @param numEntries the number of entries in the atom
   */
  @Override
  public void allocateData(long numEntries) {
    long size = TABLE_OFFSET + (numEntries * ENTRY_SIZE);
    super.allocateData(size);
  }

  /**
   * Return the number of entries in the table.
   * @return the number of entries in the table
   */
  public final long getNumEntries() {
    return data.getUnsignedInt(ENTRIES_OFFSET);
  }
  
  /**
   * Set the number of entries in the stts table
   * @param numEntries the number of entries
   */
  public final void setNumEntries(long numEntries) {
    data.addUnsignedInt(ENTRIES_OFFSET, numEntries);
  }
  
  /**
   * Return the sample count for the specified index
   * @param index the index into the stts table
   * @return the sample count
   */
  public final long getSampleCount(int index) {
    return data.getUnsignedInt(TABLE_OFFSET + (index * ENTRY_SIZE) + SAMPLE_COUNT);
  }
  
  /**
   * Set the sample count for the specified entry
   * @param index the index in the table
   * @param sc the sample count value
   */
  public final void setSampleCount(int index, long sc) {
    data.addUnsignedInt(TABLE_OFFSET + (index * ENTRY_SIZE) + SAMPLE_COUNT, sc);
  }
  
  /**
   * Return the sample value at the specified index.  The meaning of the
   * sample value depends upon the concrete class.  For stts, it is a duration
   * value.  For ctts, it is an offset.
   * @param index the index into the time to sample table
   * @return the sample value
   */
  protected final long getSampleValue(int index) {
    return data.getUnsignedInt(TABLE_OFFSET + (index * ENTRY_SIZE) + SAMPLE_VALUE);
  }
  
  /**
   * Set the sample value for the specified entry. The meaning of the
   * sample value depends upon the concrete class.  For stts, it is a duration
   * value.  For ctts, it is an offset.
   * @param index the table index
   * @param value the value value for the specified entry
   */
  protected final void setSampleValue(int index, long value) {
    data.addUnsignedInt(TABLE_OFFSET + (index * ENTRY_SIZE) + SAMPLE_VALUE, value);
  }
  
  /**
   * Cut the atom at the specified sample number.  This method is
   * used by the subclass methods, stts or ctts.  
   * @param sampleNum the sample number
   * @param cutAtom the stts or ctts atom
   */
  protected void cut(long sampleNum, TimeToSampleAtom cutAtom) {
    // search the table for the specified sample 
    long numEntries = getNumEntries();
    long upperBoundSample = 0;
    int i;
    for (i = 0; i < numEntries; i++) {
      long count = getSampleCount(i);
      upperBoundSample += count;
      if (sampleNum <= upperBoundSample) {
        // we've found the stts entry that contains this sample
        // create a new stts entry with the new count
        break;
      }
    }
    // create the new table
    long newCount = upperBoundSample - sampleNum + 1;
    long newNumEntries = numEntries - i;
    // add the new number of entries to the table
    cutAtom.allocateData(newNumEntries);
    cutAtom.setNumEntries(newNumEntries);
    // add the new first entry 
    int entryNumber = 0;
    cutAtom.setSampleCount(entryNumber, newCount);
    cutAtom.setSampleValue(entryNumber, getSampleValue(i));
    entryNumber++;
    // copy the rest of the entries from the old table to the new table
    for (i++; i < numEntries; i++, entryNumber++) {
      cutAtom.setSampleCount(entryNumber, getSampleCount(i));
      cutAtom.setSampleValue(entryNumber, getSampleValue(i));
    }    
  }

  public abstract void accept(AtomVisitor v) throws AtomException;

  /**
   * Compute the duration of the samples in the track.  This atom contains
   * the duration of each sample.  This method iterates over the table 
   * to compute the duration of all the samples
   * @return the duration of the track
   */
  public long computeDuration() {
    long duration = 0;
    for (long i = 0; i < getNumEntries(); i++) {
      duration += (getSampleCount((int)i) * getSampleValue((int)i));
    }
    return duration;
  }

}
