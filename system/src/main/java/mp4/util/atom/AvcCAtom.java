package mp4.util.atom;

public class AvcCAtom extends LeafAtom {
	  protected static final int PROFILE_OFFSET = 1;
	  protected static final int PROFILE_COMPAT_OFFSET = 2;
	  protected static final int PROFILE_LEVEL_OFFSET = 3;


	  /**
	   * Constructor that creates an empty avcc atom.
	   */
	  public AvcCAtom() {
	    super(new byte[]{'a','v','c','C'});
	  }
	  
	  /**
	   * Copy constructor.  Performs a deep copy
	   * @param old the version to copy
	   */
	  public AvcCAtom(AvcCAtom old) {
	    super(old);
	  }

	@Override
	public void accept(AtomVisitor v) throws AtomException {
		v.visit(this);
	}
	
	public int getProfile() {
		return ((int)data.getData(PROFILE_OFFSET))&0xff;
	}
	
	public int getProfileLevel() {
		return ((int)data.getData(PROFILE_LEVEL_OFFSET))&0xff;
	}
	
	public int getProfileCompatability() {
		return ((int)data.getData(PROFILE_COMPAT_OFFSET))&0xff;		
	}

	public void setProfile(int p) {
		data.addData(PROFILE_OFFSET, (byte)(p&0xff));
	}
	
	public void setProfileLevel(int p) {
		data.addData(PROFILE_LEVEL_OFFSET, (byte)(p&0xff));
	}
	
	public void setProfileCompatability(int p) {
		data.addData(PROFILE_COMPAT_OFFSET, (byte)(p&0xff));
	}

}
