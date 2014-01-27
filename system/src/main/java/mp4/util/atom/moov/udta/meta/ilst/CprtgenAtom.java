package mp4.util.atom.moov.udta.meta.ilst;

import mp4.util.atom.AppleMetaAtom;
import mp4.util.atom.Atom;

public class CprtgenAtom extends AppleMetaAtom {

	public CprtgenAtom(CprtgenAtom old) {
		super(old);
	}

	public CprtgenAtom() {
		super(new byte[] { Atom.COPYRIGHT_BYTE_VALUE, 'g', 'e', 'n' } );
		
		}
	public String getGenre() {
		return getStringMetadata();

	}
}
