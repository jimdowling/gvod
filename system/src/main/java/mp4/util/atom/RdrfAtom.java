package mp4.util.atom;

public class RdrfAtom extends LeafAtom {

	private final int ATOM_SIZE = 4; // atom size is always 4 bytes
	private final int ATOM_TYPE = 4; // atom type is always 4 bytes.
	
	private final int hdroffset = 8;
	
	private final int ATOM_FLAGS_START  = 8;// atom flags size is 4 bytes.
	private final int ATOM_FLAGS_SZ 	= 4;// atom flags size is 4 bytes.
	private final int ATOM_FLAGS_END 	= 12;// atom flags size is 4 bytes.
	
	
	private final int ATOM_DATAREFERENCE_TYPE_START = 12; // according to qtff specs atom data reference type is 4 bytes.
	private final int ATOM_DATAREFERENCE_TYPE_SZ    = 4; // according to qtff specs atom data reference type is 4 bytes.
	private final int ATOM_DATAREFERENCE_TYPE_END   = 12 +4; // according to qtff specs atom data reference type is 4 bytes.
	
	
	private final int ATOM_DATAREFERENCE_SIZE_START = 16; // according to qtff specs atom data reference size is 4 bytes.
	private final int ATOM_DATAREFERENCE_SIZE_SZ    = 4; // 32bit according to qtff specs atom data reference size is 4 bytes.
	private final int ATOM_DATAREFERENCE_SIZE_END   = 16+4; //
	private int data_reference_value = 0; // it depends on the value mentioned in data reference size 
	
	private long data_ref_size =0;
	

	  
	public RdrfAtom() 
	{
		super(new byte[]{'r','d','r','f'});
		// TODO Auto-generated constructor stub
	}

	 /**
	   * Copy constructor.  Performs a deep copy
	   * @param old the version to copy
	   */
	  public RdrfAtom(RdrfAtom old) {
	    super(old);
	  }
	  
	@Override
	public void accept(AtomVisitor v) throws AtomException {
		// TODO Auto-generated method stub
		v.visit(this);
	}

	public String getAtomFlags()
	{
		String s = new String(data.getData(ATOM_FLAGS_START-hdroffset,ATOM_FLAGS_END-hdroffset));
		System.out.println("getAtomFlags() ->s = "+s);
		 return s; 
	}
	
	public String getDataRefType()
	{
		String s = new String(data.getData(ATOM_DATAREFERENCE_TYPE_START-hdroffset,ATOM_DATAREFERENCE_TYPE_END-hdroffset));
		System.out.println("getDataRefType() ->s = "+s);
		 return s;
	}
	
	public String getDataRefSize()
	
	{
		data_ref_size = data.getUnsignedInt(ATOM_DATAREFERENCE_SIZE_START-hdroffset);
		String subbarao = ""+data_ref_size;
	
		return subbarao;
		
		/*
		String ret =  new String(data.getData(ATOM_DATAREFERENCE_SIZE_START-hdroffset,ATOM_DATAREFERENCE_SIZE_END-hdroffset));
		 System.out.println("getDataRefSize()->ret = "+ret);
		 data_ref_size = Long.parseLong(ret);
		 return ret;*/
	}
	
	public String getDataRef()
	{
		String s = new String(data.getData(ATOM_DATAREFERENCE_SIZE_END-hdroffset,ATOM_DATAREFERENCE_SIZE_END+(int)data_ref_size-hdroffset)); 
	
		 return s;
	}
	
}
