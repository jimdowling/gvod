package mp4.util.atom;

/**
 * The cdsc track reference type.  This isn't really an Atom, but
 * we create a seperate class to make it easier to read the data.
 */
public class ChapAtom extends LeafAtom implements ITrefTypeAtom {

  /**
   * Construct an empty cdsc track reference type
   */
  public ChapAtom() {
    super(new byte[]{'c','h','a','p'});
  }

  /**
   * Copy constructor for the cdsc track reference type.
   * @param old the version to copy
   */
  public ChapAtom(ChapAtom old) {
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
    return new ChapAtom(this);
  }

}
