package mp4.util.atom;

public class RmvcAtom extends LeafAtom {

	public RmvcAtom() {
		super(new byte[]{'r','m','v','c'});
		// TODO Auto-generated constructor stub
	}
	
	public RmvcAtom(RmvcAtom old) {
		super(old);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void accept(AtomVisitor v) throws AtomException {
		// TODO Auto-generated method stub
		v.visit(this);
	}

}
