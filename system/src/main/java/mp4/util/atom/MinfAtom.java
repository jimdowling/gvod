/**
 * 
 */
package mp4.util.atom;

import java.io.DataOutput;
import java.io.IOException;

/**
 * The media information container atom.
 */
public class MinfAtom extends ContainerAtom {
  // the video/sounds/base media information header
  private IMhdAtom mhd;
  // the data information atom
  private DinfAtom dinf;
  // the sample table atom
  private StblAtom stbl;
  
  /**
   * Construct an empty minfo atom
   */
  public MinfAtom() {
    super(new byte[]{'m','i','n','f'});
  }
  
  /**
   * Copy constructor.  Performs a deep copy.
   * @param old the version to copy
   */
  public MinfAtom(MinfAtom old) {
    super(old);
    mhd = old.mhd.copy();
    dinf = new DinfAtom(old.dinf);
    stbl = new StblAtom(old.stbl);
  }
  
  /**
   * Return the media header.
   * @return the media header
   */
  public IMhdAtom getMhd() {
    return mhd;
  }
  
  /**
   * Set the media header.
   * @param mhd the new media header
   */
  public void setMhd(IMhdAtom mhd) {
    this.mhd = mhd;
  }
    
  /**
   * Return the data information atom
   * @return the data information atom
   */
  public DinfAtom getDinf() {
    return dinf;
  }
  
  /**
   * Set the data information atom.
   * @param dinf the new data information atom
   */
  public void setDinf(DinfAtom dinf) {
    this.dinf = dinf;
  }
  
  /**
   * Return the sample table atom
   * @return the sample table atom.
   */
  public StblAtom getStbl() {
    return stbl;
  }
  
  /**
   * Set the sample table atom.
   * @param stbl the new sample table atom
   */
  public void setStbl(StblAtom stbl) {
    this.stbl = stbl;
  }
  
  /**
   * Add an atom to the minf atom container.  Throws a run-time exception 
   * if the the atom is not contained in a minf container.
   * @param child the atom to add
   */
  @Override
  public void addChild(Atom child) {
    if (child instanceof VmhdAtom || child instanceof SmhdAtom ||
        child instanceof GmhdAtom /* || child instanceof UnkHdAtom */) {
      mhd = (IMhdAtom) child;
    }
    else if (child instanceof DinfAtom) {
      dinf = (DinfAtom) child;
    }
    else if (child instanceof StblAtom) {
      stbl = (StblAtom) child;
    }
    else {
      //throw new AtomError("Can't add " + child + " to minf");
      addUnknownChild(child);
    }
  }

  /**
   * Recompute the size of the minf atom, which needs to be done if
   * any of the child atom sizes have changed.
   */
  @Override
public void recomputeSize() {
    long newSize = mhd.size() + dinf.size() + stbl.size();
    newSize += unknownChildrenSize();
    setSize(ATOM_HEADER_SIZE + newSize);
  }

  /**
   * Cut the atom at the specified time.
   * @param time the media/track normalized time
   * @return a new minf atom
   */
  public MinfAtom cut(long time) {
    MinfAtom cutMinf = new MinfAtom();
    cutMinf.setMhd(mhd.cut());
    cutMinf.setDinf(dinf.cut());
    cutMinf.setStbl(stbl.cut(time));
    cutMinf.recomputeSize();
    return cutMinf;
  }

  @Override
  public void accept(AtomVisitor v) throws AtomException {
    v.visit(this); 
  }

  /**
   * Write the minf atom data to the specified output
   * @param out where the data goes
   * @throws IOException if there is an error writing the data
   */
  @Override
  public void writeData(DataOutput out) throws IOException {
    writeHeader(out);
    mhd.writeData(out);
    dinf.writeData(out);
    stbl.writeData(out);
    writeUnknownChildren(out);
  }
}