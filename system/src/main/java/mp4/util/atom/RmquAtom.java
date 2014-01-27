package mp4.util.atom;

public class RmquAtom extends LeafAtom {

	 public RmquAtom() {
		super(new byte[]{'r','m','q','u'});
		// TODO Auto-generated constructor stub
	}

	 public RmquAtom(RmquAtom old) {
			super(old);
			// TODO Auto-generated constructor stub
		}
	 
	@Override
	public void accept(AtomVisitor v) throws AtomException {
		// TODO Auto-generated method stub
		v.visit(this);
	}

}
