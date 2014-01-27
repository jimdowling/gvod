/**
 * 
 */
package mp4.util.atom;

/**
 * The composition time to sample mapping atom.  This atom provides the offset
 * between the decoding time and the composition time.  The table is 
 * evaluated as follows:
 *   CT(n) = DT(n) + CTTS(n) where CTTS(n) is the uncompressed table entry for 
 *   sample n
 */
public class CttsAtom extends TimeToSampleAtom {
  
  /**
   * Constructor 
   */
  public CttsAtom() {
    super(new byte[]{'c','t','t','s'});
  }
  
  /**
   * Copy constructor.  Perform deep copy
   * @param old the version to copy
   */
  public CttsAtom(CttsAtom old) {
    super(old);
  }
  
  /**
   * Return the sample offset for the specified table index
   * @param index the ctts table index
   * @return the sample offset
   */
  public long getSampleOffset(int index) {
    return getSampleValue(index);
  }
  
  /**
   * SEt the sample offset of the specified table index
   * @param index the ctts table index entry
   * @param offset the new sample offset
   */
  public void setSampleOffset(int index, long offset) {
    setSampleValue(index, offset);
  }
  
  /**
   * Cut the atom at the specified sample.  This method creates a new
   * ctts with the new data.  This method search through the table
   * looking for the appropriate sample.  Once found, a new table entry
   * needs to be created, but the subsequent entries remain the same.
   * Any preceding entry is ignored.
   * 
   * @param sampleNum the sample where the atom should be cut
   * @return a new ctts atom with the new data
   */
  public CttsAtom cut(long sampleNum) {
    CttsAtom cutCtts = new CttsAtom();
    super.cut(sampleNum, cutCtts);
    return cutCtts;
  }
  
  @Override
  public void accept(AtomVisitor v) throws AtomException {
    v.visit(this); 
  }
}