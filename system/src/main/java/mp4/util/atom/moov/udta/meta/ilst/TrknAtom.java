package mp4.util.atom.moov.udta.meta.ilst;

import mp4.util.atom.AppleMetaAtom;

public class TrknAtom extends AppleMetaAtom {

	public TrknAtom(TrknAtom old) {
		super(old);
	}

	public TrknAtom() {
		super(new byte[] { 't', 'r', 'k', 'n' } );
	}
}
