package mp4.util.atom;

/**
 * Sample-to-chunk atom.  A chunk contains one or more samples.  The chunks
 * in a media may have different sizes, and the samples within a chunk may
 * have different sizes.
 */
public class StscAtom extends LeafAtom {
  // layout of entries in the atom
  private static final int ENTRIES_OFFSET = 4;
  private static final int TABLE_OFFSET = 8;
  // layout of each table entry
  private static final int FIRST_CHUNK = 0;
  private static final int SAMPLES_PER_CHUNK = 4;
  private static final int DESCRIPTION_ID = 8;
  // size of the table entry
  private static final int ENTRY_SIZE = 12;
  
  /**
   * Constructor for the stsc atom
   */
  public StscAtom() {
    super(new byte[]{'s','t','s','c'});
  }
  
  /**
   * Copy constructor.
   * @param old the atom to copy
   */
  public StscAtom(StscAtom old) {
    super(old);
  }
    
  /**
   * Allocate space for the stsc atom data.  The actual data is filled in 
   * later
   * @param numEntries the number of entries to add
   */
  @Override
  public void allocateData(long numEntries) {
    long size = TABLE_OFFSET + (numEntries * ENTRY_SIZE);
    super.allocateData(size);
  }

  /**
   * Return the number of entries in the table
   * @return the number of entries in the table
   */
  public long getNumEntries() {
    return data.getUnsignedInt(ENTRIES_OFFSET);
  }
  
  /**
   * Set the number of entries in the stsc atom
   * @param numEntries the number of entries
   */
  public void setNumEntries(long numEntries) {
    data.addUnsignedInt(ENTRIES_OFFSET, numEntries);
  }
  
  /**
   * Return the first chunk value for the specified entry
   * @param index the table index
   * @return the first chunk value for the specified entry
   */
  public long getFirstChunk(int index) {
    return data.getUnsignedInt(TABLE_OFFSET + (index * ENTRY_SIZE) + FIRST_CHUNK);
  }
  
  /**
   * Set the first chunk value for the specified entry
   * @param index the table index
   * @param chunk the chunk value for the specified entry
   */
  public void setFirstChunk(int index, long chunk) {
    data.addUnsignedInt(TABLE_OFFSET + (index * ENTRY_SIZE) + FIRST_CHUNK, chunk);
  }
  
  /**
   * Return the samples per chunk value for the specified entry
   * @param index the table index
   * @return the samples per chunk value for the specified entry
   */
  public long getSamplesPerChunk(int index) {
    return data.getUnsignedInt(TABLE_OFFSET + (index * ENTRY_SIZE) + SAMPLES_PER_CHUNK);
  }
  
  /**
   * Set the samples per chunk value for the specified entry
   * @param index the table index
   * @param spc the samples per chunk value for the specified entry
   */
  public void setSamplesPerChunk(int index, long spc) {
    data.addUnsignedInt(TABLE_OFFSET + (index * ENTRY_SIZE) + SAMPLES_PER_CHUNK, spc);
  }
  
  /**
   * Return the description id for the specified entry
   * @param index the table index
   * @return the description id for the specified entry
   */
  public long getDescriptionId(int index) {
    return data.getUnsignedInt(TABLE_OFFSET + (index * ENTRY_SIZE) + DESCRIPTION_ID);
  }
  
  /**
   * Set the description Id for the specified entry
   * @param index the table index
   * @param id the description id for the specified entry
   */
  public void setDescriptionId(int index, long id) {
    data.addUnsignedInt(TABLE_OFFSET + (index * ENTRY_SIZE) + DESCRIPTION_ID, id);
  }
  
  int lowerBoundPos = 0;
  int lowerBoundSampleNum = 1;

  /**
   * Given a sample return the chunk that the sample is located in.
   * @param sampleNum the sample number
   * @return the chunk number that contains the sample
   */
  public long sampleToChunk(long sampleNum) {
    long entries = getNumEntries();
    if (entries == 0) {
      return 0;
    }
    if (lowerBoundSampleNum > sampleNum) {
        lowerBoundSampleNum = 1;
        lowerBoundPos = 0;
    	
    }
    int i;
    for (i = lowerBoundPos; i < entries - 1; i++) {
      long maxSamplesInChunk = (getFirstChunk(i+1) - getFirstChunk(i)) * getSamplesPerChunk(i);
      if (sampleNum < lowerBoundSampleNum + maxSamplesInChunk) {
        break;
      }
      lowerBoundSampleNum += maxSamplesInChunk;
      lowerBoundPos = i+1;
    }
    long chunkNum = ((sampleNum - lowerBoundSampleNum) / getSamplesPerChunk(i)) + getFirstChunk(i);
    return chunkNum;
  }
  
  /**
   * Cut the atom at the specified sample number. The update is done in place
   * and the rest of the atom is filled with a free atom.
   * @param sampleNum the sample number
   * @return the 
   */
  public StscAtom cut(long sampleNum) {    
    long numEntries = getNumEntries();
    int i;
    int sampleNumCounter = 1;
    for (i = 0; i < numEntries - 1; i++) {
      long maxSamplesInChunk = (getFirstChunk(i+1) - getFirstChunk(i)) * getSamplesPerChunk(i);
      if (sampleNum < sampleNumCounter + maxSamplesInChunk) {
        // we've found the stsc with the sample, so we can create the new table
        break;
      }
      sampleNumCounter += maxSamplesInChunk;
    }
    
    StscAtom cutStsc = new StscAtom();
    // create the new table.
    long newNumEntries = numEntries - i;
    int entryNumber = 0;
    long chunkNum = ((sampleNum - sampleNumCounter) / getSamplesPerChunk(i)) + getFirstChunk(i);
    // check if the number of samples per chunk has changed
    if ((sampleNum - sampleNumCounter) % getSamplesPerChunk(i) > 0) {
      // the number of samples per chunk has changed, so we need to split
      // the table entry into two entries
      boolean split = (i == (numEntries-1)) || (getFirstChunk(i+1) - getFirstChunk(i) > 1);
      if (split)
    	  newNumEntries++;
      cutStsc.allocateData(newNumEntries);
      cutStsc.setNumEntries(newNumEntries);
      // the first entry is for the new number of samples per chunk
      cutStsc.setFirstChunk(entryNumber, 1);
      long spc = getSamplesPerChunk(i) - (sampleNum - sampleNumCounter) % getSamplesPerChunk(i);
      cutStsc.setSamplesPerChunk(entryNumber, spc);
      cutStsc.setDescriptionId(entryNumber, getDescriptionId(i));
      entryNumber++;
      if (split) {
	      // the second entry is for the existing samples per chunk, but the number of samples has changed
	      cutStsc.setFirstChunk(entryNumber, 2);
	      cutStsc.setSamplesPerChunk(entryNumber, getSamplesPerChunk(i));
	      cutStsc.setDescriptionId(entryNumber, getDescriptionId(i));
	      entryNumber++;	      
      }
      i++;
    }
    else {
      cutStsc.allocateData(newNumEntries);
      cutStsc.setNumEntries(newNumEntries);
      cutStsc.setFirstChunk(entryNumber, 1);
      cutStsc.setSamplesPerChunk(entryNumber, getSamplesPerChunk(i));
      cutStsc.setDescriptionId(entryNumber, getDescriptionId(i));
      entryNumber++;
      i++;
    }
    // copy the rest of the table, and change the chunk numbers
    for (; i < numEntries; i++, entryNumber++) {
      cutStsc.setFirstChunk(entryNumber, getFirstChunk(i) - chunkNum + 1);
      cutStsc.setSamplesPerChunk(entryNumber, getSamplesPerChunk(i));
      cutStsc.setDescriptionId(entryNumber, getDescriptionId(i));
    }
    return cutStsc;
  }
  
  @Override
  public void accept(AtomVisitor v) throws AtomException {
    v.visit(this); 
  }
}