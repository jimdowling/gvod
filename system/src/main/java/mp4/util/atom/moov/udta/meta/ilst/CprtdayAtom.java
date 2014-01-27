package mp4.util.atom.moov.udta.meta.ilst;

import mp4.util.atom.AppleMetaAtom;
import mp4.util.atom.Atom;

public class CprtdayAtom extends AppleMetaAtom {

	public CprtdayAtom(CprtdayAtom old) {
		super(old);
	}

	public CprtdayAtom() {
		super(new byte[] { Atom.COPYRIGHT_BYTE_VALUE, 'd', 'a', 'y' } );
	}
	
	public String getYear() {
		return new String(getMetaData());
	}

}
