package mp4.util.atom.moov.udta.meta.ilst;

import mp4.util.atom.AppleMetaAtom;
import mp4.util.atom.Atom;

public class CprtlyrAtom extends AppleMetaAtom {

	public CprtlyrAtom(CprtlyrAtom old) {
		super(old);
	}

	public CprtlyrAtom() {
		super(new byte[] { Atom.COPYRIGHT_BYTE_VALUE, 'l', 'y', 'r' } );
	}
	
	public String getLyrics() {
		return getStringMetadata();
	}

}
