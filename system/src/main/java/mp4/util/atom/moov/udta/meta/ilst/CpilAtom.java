package mp4.util.atom.moov.udta.meta.ilst;

import mp4.util.atom.AppleMetaAtom;


public class CpilAtom extends AppleMetaAtom {

	public CpilAtom(CpilAtom old) {
		super(old);
	}

	public CpilAtom() {
		super(new byte[] { 'c', 'p', 'i', 'l' } );
	}
}
