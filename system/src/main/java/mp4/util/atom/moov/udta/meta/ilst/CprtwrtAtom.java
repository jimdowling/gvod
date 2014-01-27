package mp4.util.atom.moov.udta.meta.ilst;

import mp4.util.atom.AppleMetaAtom;
import mp4.util.atom.Atom;

public class CprtwrtAtom extends AppleMetaAtom {

	public CprtwrtAtom(CprtwrtAtom old) {
		super(old);
	}

	public CprtwrtAtom() {
		super(new byte[] { Atom.COPYRIGHT_BYTE_VALUE, 'w', 'r', 't' } );
		
		}
	public String getComposer() {
		return getStringMetadata();
	}
}
