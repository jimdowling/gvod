package mp4.util.atom.moov.udta.meta.ilst;

import mp4.util.atom.AppleMetaAtom;


public class CprtAtom extends AppleMetaAtom {

	public CprtAtom(CprtAtom old) {
		super(old);
	}

	public CprtAtom() {
		super(new byte[] { 'c', 'p', 'r', 't' } );
	}
	
	public String getCopyright() {
		return getStringMetadata();
	}

}
