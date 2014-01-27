/**
 * 
 */
package mp4.util.atom;

/**
 * The video media information header atom.
 */
public class VmhdAtom extends LeafAtom implements IMhdAtom {
  
  /**
   * Constructor for the video media information atom
   */
  public VmhdAtom() {
    super(new byte[]{'v','m','h','d'});
  }
  
  /**
   * Copy constructor.  Performs a deep copy.
   * @param old the version to copy
   */
  public VmhdAtom(VmhdAtom old) {
    super(old);
  }
  
  /**
   * Create a deep copy of the video information header.
   * @return a copy of the video information header.
   */
  //@Override
  public IMhdAtom copy() {
    return new VmhdAtom(this);
  }
  
  /**
   * The cut method for a video media header does not do anything, so
   * just return a new copy.
   * @return a new copy of the video media header
   */
  public VmhdAtom cut() {
    return new VmhdAtom(this);
  }
  
  @Override
  public void accept(AtomVisitor v) throws AtomException {
    v.visit(this); 
  }
}