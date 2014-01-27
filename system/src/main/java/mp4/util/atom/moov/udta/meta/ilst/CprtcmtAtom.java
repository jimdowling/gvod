package mp4.util.atom.moov.udta.meta.ilst;

import mp4.util.atom.AppleMetaAtom;
import mp4.util.atom.Atom;

public class CprtcmtAtom extends AppleMetaAtom {

	public CprtcmtAtom(CprtcmtAtom old) {
		super(old);
	}

	public CprtcmtAtom() {
		super(new byte[] { Atom.COPYRIGHT_BYTE_VALUE, 'c', 'm', 't' } );
	}
	
	public String getComment() {
		return getStringMetadata();
	}

}
