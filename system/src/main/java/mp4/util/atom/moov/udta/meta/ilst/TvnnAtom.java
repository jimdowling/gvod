package mp4.util.atom.moov.udta.meta.ilst;

import mp4.util.atom.AppleMetaAtom;

public class TvnnAtom extends AppleMetaAtom {

	public TvnnAtom(TvnnAtom old) {
		super(old);
	}

	public TvnnAtom() {
		super(new byte[] { 't', 'v', 'n', 'n' } );
	}
	
	public String getNetworkName() {
		return getStringMetadata();
	}

}
