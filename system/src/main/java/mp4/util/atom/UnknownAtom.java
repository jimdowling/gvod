package mp4.util.atom;


public class UnknownAtom extends LeafAtom {

	public UnknownAtom(byte[] word) {
		super(word);
	}

	@Override
	public void accept(AtomVisitor v) throws AtomException {
		v.visit(this);
	}


}
