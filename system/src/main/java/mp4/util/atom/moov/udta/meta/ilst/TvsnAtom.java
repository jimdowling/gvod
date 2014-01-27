package mp4.util.atom.moov.udta.meta.ilst;

import mp4.util.atom.AppleMetaAtom;

public class TvsnAtom extends AppleMetaAtom {

	public TvsnAtom(TvsnAtom old) {
		super(old);
	}

	public TvsnAtom() {
		super(new byte[] { 't', 'v', 's', 'n' } );
	}
}
