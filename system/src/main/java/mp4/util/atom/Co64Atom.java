/**
 * 
 */
package mp4.util.atom;



public class Co64Atom extends StcoAtom {
  public final static int CO64_ENTRY_SIZE = 8;
  public Co64Atom() {
    super(new byte[]{'c','o','6','4'});
    this.entrySize = CO64_ENTRY_SIZE;
  }
  
  /**
   * Copy constructor.  Perform a deep copy.
   * @param old the version to copy
   */
  public Co64Atom(Co64Atom old) {
	  super(old);
  }

  @Override
  public void accept(AtomVisitor v) throws AtomException {
    v.visit(this);
  }
  
  /**
   * Get the chunk offset for the specified chunk.  The chunk
   * values are 1 based, while the table is 0 based.
   * @param chunk the chunk number
   * @return the ofset for the chunk
   */
  public long getChunkOffset(long chunk) {
    if (chunk > Integer.MAX_VALUE) {
      return 0;
    }
    return data.getLong(TABLE_OFFSET + ((int)(chunk - 1) * entrySize));
  }
  
  /**
   * Set the chunk offset for specified table index
   * @param index the table index number
   * @param chunk the chunk offset
   */
  public void setChunkOffset(int index, long chunk) {
    data.addLong(TABLE_OFFSET + (index * entrySize), chunk);
  }

}