/**
 * 
 */
package mp4.util.atom;

/**
 * The data reference atom.
 */
public class DrefAtom extends LeafAtom {
  
  public DrefAtom() {
    super(new byte[]{'d','r','e','f'});
  }
  
  /**
   * Copy constructor.  Performs a deep copy.
   * @param old the version to copy
   */
  public DrefAtom(DrefAtom old) {
    super(old);
  }
  
  @Override
  public void accept(AtomVisitor v) throws AtomException {
    v.visit(this); 
  }
}