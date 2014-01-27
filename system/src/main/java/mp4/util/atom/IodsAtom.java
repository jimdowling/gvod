/**
 * 
 */
package mp4.util.atom;

public class IodsAtom extends LeafAtom {
  
    public IodsAtom() {
      super(new byte[]{'i','o','d','s'});
    }
    
    /**
     * Copy constructor.  Performs a deep copy.
     * @param old the version to copy
     */
    public IodsAtom(IodsAtom old) {
      super(old);
    }
    
    /**
     * Cut the iods atom.  Nothing changes for this atom, so just create a copy
     * @return a copy of the iods atom.
     */
    public IodsAtom cut() {
      return new IodsAtom(this);
    }
    
    @Override
    public void accept(AtomVisitor v) throws AtomException {
      v.visit(this); 
    }
 }