package mp4.util.atom.moov.udta.meta.ilst;

import mp4.util.atom.AppleMetaAtom;

public class StikAtom extends AppleMetaAtom {

	public StikAtom(StikAtom old) {
		super(old);
	}

	public StikAtom() {
		super(new byte[] { 's', 't', 'i', 'k' } );
	}
}
