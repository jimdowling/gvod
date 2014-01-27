package mp4.util.atom;

/**
 * A free atom represents space that can be ignored in the file.
 */
public class FreeAtom extends LeafAtom {

  /**
   * Construct an empty free atom.
   */
  public FreeAtom() {
    super(new byte[]{'f','r','e','e'});
  }
  
  /**
   * Copy constructor.  Performs a deep copy.
   * @param old the version to copy
   */
  public FreeAtom(FreeAtom old) {
    super(old);
  }
  
  @Override
  public void accept(AtomVisitor v) throws AtomException {
    v.visit(this);
  }

}
