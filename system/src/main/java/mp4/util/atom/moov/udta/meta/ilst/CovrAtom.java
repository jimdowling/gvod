package mp4.util.atom.moov.udta.meta.ilst;

import mp4.util.atom.AppleMetaAtom;


public class CovrAtom extends AppleMetaAtom {

	public CovrAtom(CovrAtom old) {
		super(old);
	}

	public CovrAtom() {
		super(new byte[] { 'c', 'o', 'v', 'r' } );
	}
}
