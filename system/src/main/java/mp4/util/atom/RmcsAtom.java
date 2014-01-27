package mp4.util.atom;

public class RmcsAtom extends LeafAtom {

	public RmcsAtom() {
		super(new byte[]{'r','m','c','s'});
		// TODO Auto-generated constructor stub
	}

	public RmcsAtom(RmcsAtom old) {
		super(old);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void accept(AtomVisitor v) throws AtomException {
		// TODO Auto-generated method stub
		v.visit(this);
	}

}
