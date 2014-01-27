package mp4.util.atom.moov.udta.meta.ilst;

import mp4.util.atom.AppleMetaAtom;


public class CatgAtom extends AppleMetaAtom {

	public CatgAtom(CatgAtom old) {
		super(old);
	}

	public CatgAtom() {
		super(new byte[] { 'c', 'a', 't', 'g' } );
	}
	
	public String getCategory() {
		return getStringMetadata();
	}

}
