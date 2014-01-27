package mp4.util.atom.moov.udta.meta.ilst;

import mp4.util.atom.AppleMetaAtom;

public class PurlAtom extends AppleMetaAtom {

	public PurlAtom(PurlAtom old) {
		super(old);
	}

	public PurlAtom() {
		super(new byte[] { 'p', 'u', 'r', 'l' } );
	}
}
