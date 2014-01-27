package mp4.util.atom;

public class DataAtom extends LeafAtom {

	/* Apple metadata data atom */
	  public static final int METADATA_OFFSET = 8;
	  
	  public static final int UINT8_TYPE_FLAG = 0;
	  public static final int UINT8_TYPE_FLAG2 = 21;  
	  public static final int TEXT_TYPE_FLAG = 1;
	  public static final int JPG_TYPE_FLAG = 13;
	  public static final int PNG_TYPE_FLAG = 14;

	  /**
	   * Constructor that creates an empty data atom.
	   */
	  public DataAtom() {
	    super(new byte[]{'d','a','t','a'});
	  }
	  
	  /**
	   * Copy constructor.  Performs a deep copy
	   * @param old the version to copy
	   */
	  public DataAtom(DataAtom old) {
	    super(old);
	  }

	@Override
	public void accept(AtomVisitor v) throws AtomException {
		v.visit(this);
	}

	public byte[] getMetadata() {
		return data.getData(METADATA_OFFSET, (int)dataSize());
	}
	

}
