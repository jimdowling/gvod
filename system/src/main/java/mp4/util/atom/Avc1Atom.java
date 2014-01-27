package mp4.util.atom;

import java.io.DataOutput;
import java.io.IOException;

public class Avc1Atom extends LeafAtom {

	  //public static final int DATA_SIZE = 78;
	  public static final int AVCC_OFFSET = 78;

	  private static final int WIDTH_OFFSET = 24;
	  private static final int HEIGHT_OFFSET = 26;
	  //private AvcCAtom avcc;
	  
	  protected static final int AVCC_PROFILE_OFFSET = 1;
	  protected static final int AVCC_PROFILE_COMPAT_OFFSET = 2;
	  protected static final int AVCC_PROFILE_LEVEL_OFFSET = 3;

	  protected static final byte[] avcCType = new byte[] { 'a', 'v', 'c', 'C' };
	  /**
	   * Constructor that creates an empty avc1 atom.
	   */
	  public Avc1Atom() {
	    super(new byte[]{'a','v','c','1'});
	  }
	  
	  /**
	   * Copy constructor.  Performs a deep copy
	   * @param old the version to copy
	   */
	  public Avc1Atom(Avc1Atom old) {
	    super(old);
	    /*
	    if (old.avcc != null)
	    	this.avcc = new AvcCAtom(old.avcc);
	    	*/
	  }

	@Override
	public void accept(AtomVisitor v) throws AtomException {
		v.visit(this);
	}
	
	public int getWidth() {
		return data.getUnsignedShort(WIDTH_OFFSET);
	}

	public int getHeight() {
		return data.getUnsignedShort(HEIGHT_OFFSET);
	}
	
	public int getProfile() {
		if (!hasAvcc())
			return 0;
		return ((int)data.getData(AVCC_OFFSET + ATOM_HEADER_SIZE + AVCC_PROFILE_OFFSET))&0xff;
	}
	
	public int getProfileLevel() {
		if (!hasAvcc())
			return 0;		
		return ((int)data.getData(AVCC_OFFSET + ATOM_HEADER_SIZE + AVCC_PROFILE_LEVEL_OFFSET))&0xff;
	}
	
	public int getProfileCompatability() {
		if (!hasAvcc())
			return 0;		
		return ((int)data.getData(AVCC_OFFSET + ATOM_HEADER_SIZE + AVCC_PROFILE_COMPAT_OFFSET))&0xff;		
	}

	public void setProfile(int p) {
		if (!hasAvcc())
			return;		
		data.addData(AVCC_OFFSET + ATOM_HEADER_SIZE + AVCC_PROFILE_OFFSET, (byte)(p&0xff));
	}
	
	public void setProfileLevel(int p) {
		if (hasAvcc())
			return;				
		data.addData(AVCC_OFFSET + ATOM_HEADER_SIZE + AVCC_PROFILE_LEVEL_OFFSET, (byte)(p&0xff));
	}
	
	public void setProfileCompatability(int p) {
		if (hasAvcc())
			return;				
		data.addData(AVCC_OFFSET + ATOM_HEADER_SIZE + AVCC_PROFILE_COMPAT_OFFSET, (byte)(p&0xff));
	}

	public boolean hasAvcc() {
		return Atom.typeEquals(data.getData(AVCC_OFFSET+4, AVCC_OFFSET+8), avcCType);
	}
	
	/*
	public long pureDataSize() {
		  return DATA_SIZE;
	}

	
	@Override
	public void addChild(Atom child) {
		if (child instanceof AvcCAtom)
			  avcc = (AvcCAtom)child;
		  else 
			  addUnknownChild(child);
	}  
	*/
	
	/*
	public AvcCAtom getAvcc() {
		  return avcc;
	}
	 */ 
	 /**
	   * Write the avc1 atom data to the specified output
	   * @param out where the data goes
	   * @throws IOException if there is an error writing the data
	   */
	  @Override
	  public void writeData(DataOutput out) throws IOException {
	    writeHeader(out);
	    data.writeData(out);
	    /*
	    if (avcc != null)
	    	avcc.writeData(out);	   
	    writeUnknownChildren(out);
	    */
	  }
}
