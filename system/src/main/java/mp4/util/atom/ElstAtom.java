/**
 * 
 */
package mp4.util.atom;

/**
 * The edit list atom.
 */
public class ElstAtom extends LeafAtom {
  private static final int ENTRIES_OFFSET = 4;
  private static final int TABLE_OFFSET = 8;
  
  private static final int TRACK_DURATION = 0;
  private static final int MEDIA_TIME = 4;
  private static final int MEDIA_RATE = 8;
  
  private static final int ENTRY_SIZE = 12;
  
  /**
   * Construct an empty elst atom.
   */
  public ElstAtom() {
    super(new byte[]{'e','l','s','t'});
  }
  
  /**
   * Copy constructor.  Performs a deep copy.
   * @param old the version to copy
   */
  public ElstAtom(ElstAtom old) {
    super(old);
  }
  
  /**
   * Construct a single edit list entry.
   * @param size the number of entries to allocate
   */
  public ElstAtom(int size) {
    this();
    allocateData(size);
  }
  
  /**
   * Add an entry to the edit list.
   * @param num the entry number
   * @param duration the edit list duration
   * @param mediaTime the edit time
   * @param mediaRate the edit media rate
   */
  public void addEntry(int num, long duration, long mediaTime, int mediaRate) {
    setNumEntries(num);
    setDuration(num - 1, duration);
    setMediaTime(num - 1, mediaTime);
    setMediaRate(num - 1, mediaRate);
  }
  
  /**
   * Allocate space for the specified number of entries
   * @param numEntries the number of entries in the edit list
   */
  @Override
  public void allocateData(long numEntries) {
    long size = TABLE_OFFSET + (numEntries * ENTRY_SIZE);
    super.allocateData(size);
  }

  /**
   * Set a new duration for each elst entry.
   * @param duration the new duration value
   */
  public void setDuration(long duration) {
    for (int i = 0; i < getNumEntries(); i++) {
      setDuration(i, duration);
    }
  }

  /**
   * Return the number of entries in the edit list
   * @return the number of entries in the edit list
   */
  public long getNumEntries() {
    return data.getUnsignedInt(ENTRIES_OFFSET);
  }
  
  /**
   * Set the number of entries in the edit list
   * @param numEntries the number of edit list entries
   */
  public void setNumEntries(long numEntries) {
    data.addUnsignedInt(ENTRIES_OFFSET, numEntries);
  }
  
  /**
   * Return the track duration for the specified index
   * @param index the table index
   * @return the track duration for the specified index
   */
  public long getDuration(int index) {
    return data.getUnsignedInt(TABLE_OFFSET + (index * ENTRY_SIZE) + TRACK_DURATION);
  }
  
  /**
   * Set the track duration for the specified edit list entry
   * @param index the table index
   * @param val the new duration
   */
  public void setDuration(int index, long val) {
    data.addUnsignedInt(TABLE_OFFSET + (index * ENTRY_SIZE) + TRACK_DURATION, val);
  }
  
  /**
   * Return the media time for the specified index
   * @param index the table index
   * @return the media time for the specified index
   */
  public long getMediaTime(int index) {
    return data.getUnsignedInt(TABLE_OFFSET + (index * ENTRY_SIZE) + MEDIA_TIME);
  }
  
  /**
   * Set the media time for the specified index
   * @param index the elst table index
   * @param time the new time
   */
  public void setMediaTime(int index, long time) {
    data.addUnsignedInt(TABLE_OFFSET + (index * ENTRY_SIZE) + MEDIA_TIME, time);
  }
  
  /**
   * Return the media rate for the specified index.  The media rate is a 
   * fixed point value.  The first 16-bits is the integer and the next 16-bits
   * is the fraction.
   * @param index the table index
   * @return the media rate for the specified index
   */
  public double getMediaRate(int index) {
    return data.getFixedPoint(TABLE_OFFSET + (index * ENTRY_SIZE) + MEDIA_RATE);
  }
  
  /**
   * Sets the integer portion of the media rate for the specified edit list entry.
   * @param index the table index value
   * @param rate the media rate integer portion
   */
  public void setMediaRate(int index, int rate) {
    data.addFixedPoint(TABLE_OFFSET + (index * ENTRY_SIZE) + MEDIA_RATE, rate, 0);
  }
  
  @Override
  public void accept(AtomVisitor v) throws AtomException {
    v.visit(this);
  }
  
  /**
   * Update the specified time with information in the edit list
   * @param time the time in seconds
   * @param mediaTS the media time scale
   * @param movieTS the movie time scale
   * @return the updated time in the media time scale
   */
  public long editTime(float time, long mediaTS, long movieTS) {
    long movieTime = (long)(time * movieTS);
    long mediaTime = (long)(time * mediaTS);
    for (int i = 0; i < getNumEntries(); i++) {
      if (movieTime < getDuration(i) && getMediaTime(i) != -1) {
        // we don't handle dwell edits
        assert getMediaRate(i) != 0;
        return mediaTime + getMediaTime(i);
      }
    }
    return mediaTime;
  }
}