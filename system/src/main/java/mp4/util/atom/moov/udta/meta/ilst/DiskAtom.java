package mp4.util.atom.moov.udta.meta.ilst;

import mp4.util.atom.AppleMetaAtom;

public class DiskAtom extends AppleMetaAtom {

	public DiskAtom(DiskAtom old) {
		super(old);
	}

	public DiskAtom() {
		super(new byte[] { 'd', 'i', 's', 'k' } );
	}
}
