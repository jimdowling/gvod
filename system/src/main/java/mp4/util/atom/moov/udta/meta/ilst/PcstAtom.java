package mp4.util.atom.moov.udta.meta.ilst;

import mp4.util.atom.AppleMetaAtom;

public class PcstAtom extends AppleMetaAtom {

	public PcstAtom(PcstAtom old) {
		super(old);
	}

	public PcstAtom() {
		super(new byte[] { 'p', 'c', 's', 't' } );
	}
}
