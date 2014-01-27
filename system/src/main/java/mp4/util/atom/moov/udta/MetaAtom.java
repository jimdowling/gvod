/**
 * 
 */
package mp4.util.atom.moov.udta;

import mp4.util.atom.AtomException;
import mp4.util.atom.AtomVisitor;
import mp4.util.atom.HybridAtom;


/**
 * The user data atom.
 */
public class MetaAtom extends HybridAtom {
  public static final int DATA_SIZE = 4;

  /**
   * Constructor for a user-data atom.
   */
  public MetaAtom() {
    super(new byte[]{'m','e','t','a'});
  }
  
  /**
   * Copy constructor.  Perform deep copy.
   * @param old the version to copy
   */
  public MetaAtom(MetaAtom old) {
    super(old);
  }
  
  @Override
  public void accept(AtomVisitor v) throws AtomException {
    v.visit(this); 
  }
  
  public long pureDataSize() {
	  return DATA_SIZE;
  }
}