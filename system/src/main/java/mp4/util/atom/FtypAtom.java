/**
 * 
 */
package mp4.util.atom;

/**
 * The file type atom.  This is the first atom in the mp4 stream.
 */
public class FtypAtom extends LeafAtom {
  
  public static final int MAJOR_BRAND_OFFSET = 8;
  
  /**
   * Construct an empty ftyp atom
   */
  public FtypAtom() {
    super(new byte[]{'f','t','y','p'});
  }
  
  /**
   * Copy constructor. Performs a deep copy.
   * @param old the version to copy
   */
  public FtypAtom(FtypAtom old) {
    super(old);
  }
  
  @Override
  public void accept(AtomVisitor v) throws AtomException {
    v.visit(this);
  }
  
  /**
   * Return the ISO registered brand name.
   * @return the ISO registered brand name
   */
  public byte[] getMajorBrand() {
    return data.getData(MAJOR_BRAND_OFFSET, MAJOR_BRAND_OFFSET + ATOM_WORD);
  }
}