package mp4.util.atom;

/**
 * The decoding time-to-sample atom stores duration info for a media's 
 * samples providing a mapping from the time in a media to a data sample. 
 * 
 * The atom contains a table of sample entries. Each entry contains a 
 * sample count and sample duration.  The delta's are computed as:
 *   DT(n+1) = DT(n) + STTS(n) where STTS(n) is the uncompressed table
 *   entry for sample n
 * The sum of all the deltas gives the length of the media in the track.
 * The edit list atom provides the initial CT value if it is non-empty.
 */
public class SttsAtom extends TimeToSampleAtom {
  /**
   * Constructor for the time-to-sample atom
   */
  public SttsAtom() {
    super(new byte[]{'s','t','t','s'});
  }
  
  /**
   * Copy constructor
   * @param old the atom to copy
   */
  public SttsAtom(TimeToSampleAtom old) {
    super(old);
  }
  
  /**
   * Return the sample duration for the specified table index
   * @param index the stts table index
   * @return the sample duration for the index entry
   */
  public long getSampleDuration(int index) {
    return getSampleValue(index);
  }
  
  /**
   * Set the sample duration for the specified table index.
   * @param index the stts table index
   * @param duration the new sample duration
   */
  public void setSampleDuration(int index, long duration) {
    setSampleValue(index, duration);
  }
  
  /**
   * Cut the atom at the specified sample.  This method creates a new 
   * stts atom with the new data.  This method searches through the table
   * looking for the appropriate sample.  Once found a new table entry
   * needs to be created, but the subsequent entries remain the same.
   * Any preceding entry is ignored.
   * 
   * @param sampleNum the sample where the atom should be cut
   * @return a new stts atom with the new data
   */
  public SttsAtom cut(long sampleNum) {
    SttsAtom cutStts = new SttsAtom();
    super.cut(sampleNum, cutStts);
    return cutStts;
  }
  
  @Override
  public void accept(AtomVisitor v) throws AtomException {
    v.visit(this); 
  }


  long lowerBoundTime = 0;
  long lowerBoundSample = 1;
  int lowerBoundPos = 0;
  /**
   * Convert the sample number to a time, in media time-scale.
   * @param sampleNum the sample number
   * @return the time for the sample, in media time scale.
   */  
  public long sampleToTime(long sampleNum) {
	if (lowerBoundSample > sampleNum) {
	    lowerBoundTime = 0;
	    lowerBoundSample = 1;
	    lowerBoundPos = 0;
	}
    long numEntries = getNumEntries();
    for (int i = lowerBoundPos; i < numEntries; i++ ) {
      long count = getSampleCount(i);
      long duration = getSampleDuration(i);
      if ((sampleNum - lowerBoundSample) < count) {
        return ((sampleNum - lowerBoundSample) * duration) + lowerBoundTime;
      }
      lowerBoundPos = i+1;
      lowerBoundTime += count * duration;
      lowerBoundSample += count;
    }
    return 0;
  }

  public long getTotalSampleCount() {
	    long numEntries = getNumEntries();
	    long sampleCnt = 0;
	    for (int i = 0; i < numEntries; i++ ) {
	      long count = getSampleCount(i);
	      sampleCnt += count;
	    }	  
	    return sampleCnt;
  }
  /**
   * Given a time in the media return the data sample.
   * @param time the media time value
   * @return the sample number for the specified time
   */
  public long timeToSample(long time) {
    long entries = getNumEntries();
    long lowerBoundTime = 0;
    long lowerBoundSample = 1;
    for (int i = 0; i < entries; i++) {
      long count = getSampleCount(i);
      long duration = getSampleDuration(i);
      if ((time - lowerBoundTime) < (count * duration)) {
        return ((time - lowerBoundTime) / duration) + lowerBoundSample;
      }
      lowerBoundTime += count * duration;
      lowerBoundSample += count;
    }
    return 1;    
  }
}