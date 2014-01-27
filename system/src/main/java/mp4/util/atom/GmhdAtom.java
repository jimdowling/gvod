/**
 * 
 */
package mp4.util.atom;

/**
 * The base media information header atom, which indicates that this 
 * media information atom pertains to a base media.  Not sure what that
 * means.
 */
public class GmhdAtom extends LeafAtom implements IMhdAtom {
  
  /**
   * Construct an empty gmhd atom.
   */
  public GmhdAtom() {
    super(new byte[]{'g','m','h','d'});
  }
  
  /**
   * Construct a gmhd atom.  Performs a deep copy.
   * @param old the old version.
   */
  public GmhdAtom(GmhdAtom old) {
    super(old);
  }
  
  /**
   * The visitor pattern for the gmhd atom
   */
  @Override
  public void accept(AtomVisitor v) throws AtomException {
    v.visit(this); 
  }

  /**
   * Return a copy of the gmhd atom.
   * @return a copy of the gmhd atom.
   */
  //@Override
  public IMhdAtom copy() {
    return new GmhdAtom(this);
  }

  /**
   * Cut the gmhd atom, which just returns a copy of the atom.
   * @return a copy of the gmhd atom.
   */
  //@Override
  public IMhdAtom cut() {
    return new GmhdAtom(this);
  }
  
  
}