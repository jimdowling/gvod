package mp4.util.atom.moov.udta.meta.ilst;

import mp4.util.atom.AppleMetaAtom;

public class TvenAtom extends AppleMetaAtom {

	public TvenAtom(TvenAtom old) {
		super(old);
	}

	public TvenAtom() {
		super(new byte[] { 't', 'v', 'e', 'n' } );
	}
	
	public String getEpisodeNumber() {
		return getStringMetadata();
	}

}
