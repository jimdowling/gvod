package mp4.util.atom.moov.udta.meta.ilst;

import mp4.util.atom.AppleMetaAtom;
import mp4.util.atom.Atom;

public class CprtgrpAtom extends AppleMetaAtom {

	public CprtgrpAtom(CprtgrpAtom old) {
		super(old);
	}

	public CprtgrpAtom() {
		super(new byte[] { Atom.COPYRIGHT_BYTE_VALUE, 'g', 'r', 'p' } );
	}
	
	public String getGrouping() {
		return getStringMetadata();
	}

}
