package mp4.util.atom.moov.udta.meta.ilst;

import mp4.util.atom.AppleMetaAtom;

public class EgidAtom extends AppleMetaAtom {

	public EgidAtom(EgidAtom old) {
		super(old);
	}

	public EgidAtom() {
		super(new byte[] { 'e', 'g', 'i', 'd' } );
	}
}
