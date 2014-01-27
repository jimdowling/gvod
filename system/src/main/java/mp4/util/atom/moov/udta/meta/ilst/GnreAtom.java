package mp4.util.atom.moov.udta.meta.ilst;

import mp4.util.atom.AppleMetaAtom;

public class GnreAtom extends AppleMetaAtom {

	public GnreAtom(GnreAtom old) {
		super(old);
	}

	public GnreAtom() {
		super(new byte[] { 'G', 'n', 'r', 'e' } );
	}
	
	public String getGenre() {
		return getStringMetadata();
	}

}
