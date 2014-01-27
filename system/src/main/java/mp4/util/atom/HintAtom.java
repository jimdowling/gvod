package mp4.util.atom;

/**
 * A hint track reference type
 */
public class HintAtom extends LeafAtom implements ITrefTypeAtom {

  /**
   * Construct an empty hint track reference atom
   */
  public HintAtom() {
    super(new byte[]{'h','i','n','t'});
  }
  
  /**
   * Copy constructor for hint atom.  Performs a deep copy
   * @param old the version to copy
   */
  public HintAtom(HintAtom old) {
    super(old);
  }
  
  @Override
  public void accept(AtomVisitor v) throws AtomException {
    v.visit(this);
  }

  /**
   * Create a deep copy of the hint atom
   * @return a deeop copy of the hint atom
   */
  //@Override
  public ITrefTypeAtom copy() {
    return new HintAtom(this);
  }

}
