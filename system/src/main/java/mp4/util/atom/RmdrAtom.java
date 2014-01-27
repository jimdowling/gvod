/**
 * 
 */
package mp4.util.atom;

/**
 * @author system-3
 *
 */
public class RmdrAtom extends LeafAtom {

	public RmdrAtom() {
		super(new byte[]{'r','m','d','r'});
		// TODO Auto-generated constructor stub
	}

	public RmdrAtom(RmdrAtom rmdrAtom) {
		super(rmdrAtom);
		// TODO Auto-generated constructor stub
	}
	

	/* (non-Javadoc)
	 * @see mp4.util.atom.Atom#accept(mp4.util.atom.AtomVisitor)
	 */
	@Override
	public void accept(AtomVisitor v) throws AtomException {
		// TODO Auto-generated method stub
		v.visit(this);
	}

}
