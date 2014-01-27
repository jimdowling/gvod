package mp4.util.atom.moov.udta.meta.ilst;

import mp4.util.atom.AppleMetaAtom;

public class TvshAtom extends AppleMetaAtom {

	public TvshAtom(TvshAtom old) {
		super(old);
	}

	public TvshAtom() {
		super(new byte[] { 't', 'v', 's', 'h' } );
	}
	
	public String getShowName() {
		return getStringMetadata();
	}

}
