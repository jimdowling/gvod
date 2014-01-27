package mp4.util.atom.moov.udta.meta.ilst;

import mp4.util.atom.AppleMetaAtom;
import mp4.util.atom.Atom;

public class CprtalbAtom extends AppleMetaAtom {

	public CprtalbAtom(CprtalbAtom old) {
		super(old);
	}

	public CprtalbAtom() {
		super(new byte[] { Atom.COPYRIGHT_BYTE_VALUE, 'a', 'l', 'b' } );
	}
	
	public String getAlbum() {
		return getStringMetadata();
	}

}
