package mp4.util.atom.moov.udta.meta.ilst;

import mp4.util.atom.AppleMetaAtom;

public class PurdAtom extends AppleMetaAtom {

	public PurdAtom(PurdAtom old) {
		super(old);
	}

	public PurdAtom() {
		super(new byte[] { 'p', 'u', 'r', 'd' } );
	}
	
	public String getPurchaseData() {
		return getStringMetadata();
	}

}
