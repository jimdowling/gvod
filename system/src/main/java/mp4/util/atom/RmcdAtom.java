package mp4.util.atom;

public class RmcdAtom extends LeafAtom {

	public RmcdAtom(RmcdAtom rmcdAtom) {
		super(rmcdAtom);
		// TODO Auto-generated constructor stub
	}

	public RmcdAtom() {
		super(new byte[]{'r','m','c','d'});
		// TODO Auto-generated constructor stub
	}

	
	@Override
	public void accept(AtomVisitor v) throws AtomException {
		// TODO Auto-generated method stub
		v.visit(this);
	}

}
