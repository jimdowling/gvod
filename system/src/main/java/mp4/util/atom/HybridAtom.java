package mp4.util.atom;

import java.io.DataOutput;
import java.io.IOException;

public abstract class HybridAtom extends ContainerAtom {
	  protected static final int VERSION_OFFSET = 0;
	  protected static final int FLAG_OFFSET = 1;
	  protected static final int FLAG_SIZE = 3;

	protected HybridAtom(byte[] type) {
		super(type);
	}
	
	protected HybridAtom(HybridAtom old) {
		super(old);
		data = new ByteStream(old.data);		
	}

//	  /**
//	   * Return the version value for the atom. Currently, we support only 0, which
//	   * means 
//	   * @return
//	   */
//	  public byte getVersion() {
//	    return data.getData(VERSION_OFFSET);
//	  }
//	  
//	  /**
//	   * Set the version value for the atom.
//	   * @param version the atom's version
//	   */
//	  public void setVersion(byte version) {
//	    data.addData(VERSION_OFFSET, version);
//	  }
//	  
//	  /**
//	   * Return the flag data from the atom as a byte array
//	   * @return the flag data from the atom as a byte array
//	   */
//	  public byte[] getFlag() {
//	    return data.getData(FLAG_OFFSET, FLAG_SIZE);
//	  }
//	  
//	  /**
//	   * Add the flag data to the byte stream
//	   * @param flag the flag info
//	   */
//	  public void setFlag(byte[] flag) {
//	    data.addData(FLAG_OFFSET, flag);
//	  }
	  	  
	  /**
	   * Write the byte stream to the specified output.
	   * @param out where the output goes
	   * @throws IOException if there is a problem writing the data
	   */
	  public void writeData(DataOutput out) throws IOException {
	    writeHeader(out);
	    data.writeData(out);
	    writeUnknownChildren(out);	    
	  }
	  

}
