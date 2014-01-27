package mp4.util.atom.moov.udta.meta.ilst;

import mp4.util.atom.AppleMetaAtom;

public class RtngAtom extends AppleMetaAtom {

	public RtngAtom(RtngAtom old) {
		super(old);
	}

	public RtngAtom() {
		super(new byte[] { 'r', 't', 'n', 'g' } );
	}
}
