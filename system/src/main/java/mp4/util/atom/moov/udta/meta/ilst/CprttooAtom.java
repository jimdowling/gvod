package mp4.util.atom.moov.udta.meta.ilst;

import mp4.util.atom.AppleMetaAtom;
import mp4.util.atom.Atom;

public class CprttooAtom extends AppleMetaAtom {

	public CprttooAtom(CprttooAtom old) {
		super(old);
	}

	public CprttooAtom() {
		super(new byte[] { Atom.COPYRIGHT_BYTE_VALUE, 't', 'o', 'o' } );
	}
	
	public String getCreator() {
		return getStringMetadata();
	}

}
