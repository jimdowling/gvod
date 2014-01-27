/**
 * 
 */
package mp4.util.atom.moov.udta.meta;

import mp4.util.atom.AtomException;
import mp4.util.atom.AtomVisitor;
import mp4.util.atom.ContainerAtom;

/**
 * The user data atom.
 */
public class IlstAtom extends ContainerAtom {

  /**
   * Constructor for a user-data atom.
   */
  public IlstAtom() {
    super(new byte[]{'i','l','s','t'});
  }
  
  /**
   * Copy constructor.  Perform deep copy.
   * @param old the version to copy
   */
  public IlstAtom(IlstAtom old) {
    super(old);
  }
  


  @Override
  public void accept(AtomVisitor v) throws AtomException {
    v.visit(this); 
  }
}