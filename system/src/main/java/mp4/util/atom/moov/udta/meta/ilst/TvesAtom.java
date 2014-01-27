package mp4.util.atom.moov.udta.meta.ilst;

import mp4.util.atom.AppleMetaAtom;

public class TvesAtom extends AppleMetaAtom {

	public TvesAtom(TvesAtom old) {
		super(old);
	}

	public TvesAtom() {
		super(new byte[] { 't', 'v', 'e', 's' } );
	}
}
