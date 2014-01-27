/**
 * 
 */
package mp4.util.atom;

import java.io.DataOutput;
import java.io.IOException;

/**
 * The data information container atom.
 */
public class DinfAtom extends ContainerAtom {
  // the data reference atom
  private DrefAtom dref;
  
  /**
   * Construct an empty data inforamation atom
   */
  public DinfAtom() {
    super(new byte[]{'d','i','n','f'});
  }
  
  /**
   * Copy constructor.  Performs a deep copy.
   * @param old the version to copy
   */
  public DinfAtom(DinfAtom old) {
    super(old);
    if (old.dref != null)
    	dref = new DrefAtom(old.dref);
  }
  
  public DrefAtom getDref() {
    return dref;
  }
  
  /**
   * Add a child atom, a dref, to the dinf atom. If the atom is
   * not a dref, then throw a run-time exception.
   * @param child the dref atom 
   */
  @Override
  public void addChild(Atom child) {
    if (child instanceof DrefAtom) {
      dref = (DrefAtom) child;
    }
    else {
      //throw new AtomError("Can't add " + child + " to dref");
      addUnknownChild(child);
    }
  }
 
  /**
   * Recompute the size of the dinf atom.  This is a noop size the contents
   * do not change.
   */
  @Override
  protected void recomputeSize() {
	long newSize = ATOM_HEADER_SIZE ;
	if (dref != null)
		newSize += dref.size();
	newSize += unknownChildrenSize();
    setSize(newSize);
  }

  /**
   * Cut the dinf atom, which does need to change the contents.  So,
   * this method return a copy of the atom.
   * @return a copy of the dinf atom.
   */
  public DinfAtom cut() {
    return new DinfAtom(this);
  }

  @Override
  public void accept(AtomVisitor v) throws AtomException {
    v.visit(this); 
  }
 
  /**
   * Write the dinf atom data to the specified output
   * @param out where the data goes
   * @throws IOException if there is an error writing the data
   */
  @Override
  public void writeData(DataOutput out) throws IOException {
    writeHeader(out);
    if (dref != null)
    	dref.writeData(out);
    writeUnknownChildren(out);
  }
}