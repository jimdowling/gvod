package mp4.util.atom;

/**
 * The cdsc track reference type.  This isn't really an Atom, but
 * we create a seperate class to make it easier to read the data.
 */
public class CdscAtom extends LeafAtom implements ITrefTypeAtom {

  /**
   * Construct an empty cdsc track reference type
   */
  public CdscAtom() {
    super(new byte[]{'c','d','s','c'});
  }

  /**
   * Copy constructor for the cdsc track reference type.
   * @param old the version to copy
   */
  public CdscAtom(CdscAtom old) {
    super(old);
  }

  @Override
  public void accept(AtomVisitor v) throws AtomException {
    v.visit(this);
  }

  /**
   * Create a deep copy of this concrete atom
   * @return a new copy of the atom
   */
  //@Override
  public ITrefTypeAtom copy() {
    return new CdscAtom(this);
  }

}
