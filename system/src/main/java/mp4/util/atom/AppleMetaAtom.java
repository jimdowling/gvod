/**
 * 
 */
package mp4.util.atom;

import java.io.DataOutput;
import java.io.IOException;

/**
 * The user data atom.
 */
public class AppleMetaAtom extends ContainerAtom {

  protected DataAtom dataAtom;
  
  /**
   * Copy constructor.  Perform deep copy.
   * @param old the version to copy
   */
  public AppleMetaAtom(AppleMetaAtom old) {
    super(old);
    if (old.data != null)
    	dataAtom = new DataAtom(old.dataAtom);
  }
  
  public AppleMetaAtom(byte[] typ) {
	  super(typ);
  }
  
  @Override
  public void accept(AtomVisitor v) throws AtomException {
    v.visit(this); 
  }
  
  
	@Override
	public void addChild(Atom child) {
		// Let's only care about the first data item
		if (dataAtom == null && child instanceof DataAtom)
			  dataAtom = (DataAtom)child;
		  else 
			  addUnknownChild(child);
	}  
	  
	public DataAtom getDataAtom() {
		  return dataAtom;
	}
	
	public byte[] getMetaData() {
		if (dataAtom != null)
			return dataAtom.getMetadata();
		else
			return null;
	}
	  
	 /**
	   * Write the avc1 atom data to the specified output
	   * @param out where the data goes
	   * @throws IOException if there is an error writing the data
	   */
	  @Override
	  public void writeData(DataOutput out) throws IOException {
	    writeHeader(out);
	    data.writeData(out);
	    if (dataAtom != null)
	    	dataAtom.writeData(out);
	    writeUnknownChildren(out);
	  }  
	  
	  public String getStringMetadata() {
		  if (dataAtom == null)
			  return null;
		  if (dataAtom.getFlag()[2] != DataAtom.TEXT_TYPE_FLAG)
			  return null;
		  return new String(getMetaData());
	  }
}