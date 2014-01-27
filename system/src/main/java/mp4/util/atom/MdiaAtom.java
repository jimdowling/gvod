/**
 * 
 */
package mp4.util.atom;

import java.io.DataOutput;
import java.io.IOException;

/**
 * Container atom for the media information on a track
 */
public class MdiaAtom extends ContainerAtom {
  // media header
  private MdhdAtom mdhd;
  // the media's handler type
  private HdlrAtom hdlr;
  // media information container
  private MinfAtom minf;
  
  /**
   * Constructor for media inforamtion atom
   */
  public MdiaAtom() {
    super(new byte[]{'m','d','i','a'});
  }
  
  /**
   * Copy constructor.  Performs a deep copy
   * @param old the object to copy
   */
  public MdiaAtom(MdiaAtom old) {
    super(old);
    mdhd = new MdhdAtom(old.mdhd);
    hdlr = new HdlrAtom(old.hdlr);
    minf = new MinfAtom(old.minf);
  }
  
  /**
   * Return the media header atom
   * @return the media header atom
   */
  public MdhdAtom getMdhd() {
    return mdhd;
  }
  
  /**
   * Set the media header atom.
   * @param mdhd the new media header atom
   */
  public void setMdhd(MdhdAtom mdhd) {
    this.mdhd = mdhd;
  }
  
  /**
   * Return the handler type atom
   * @return the handler type atom
   */
  public HdlrAtom getHdlr() {
    return hdlr;
  }
  
  /**
   * Set the media handler.
   * @param hdlr the new handler
   */
  public void setHdlr(HdlrAtom hdlr) {
    this.hdlr = hdlr;
  }
  
  /**
   * Return the media information atom
   * @return the media information atom
   */
  public MinfAtom getMinf() {
    return minf;
  }
  
  /**
   * Set the minf atom for the media.
   * @param minf the new minf atom
   */
  public void setMinf(MinfAtom minf) {
    this.minf = minf;
  }
  
  /**
   * Add an atom to the mdia container.  A run-time exception is thrown
   * if the atom is not recognized.
   * @param child the atom to add
   */
  @Override
  public void addChild(Atom child) {
    if (child instanceof MdhdAtom) {
      mdhd = (MdhdAtom) child;
    }
    else if (child instanceof HdlrAtom) {
      hdlr = (HdlrAtom) child;
    }
    else if (child instanceof MinfAtom) {
      minf = (MinfAtom) child;
    }
    else {
      //throw new AtomError("Can't add " + child + " to mdia");
      addUnknownChild(child);
    }
  }

  /**
   * Recompute the size ofhte mdia atom, which needs to be done if the
   * contained atoms change
   */
  @Override
public void recomputeSize() {
	long newSize  = ATOM_HEADER_SIZE + mdhd.size() + hdlr.size() + minf.size();
	newSize += unknownChildrenSize();
    setSize(newSize);
  }

  /**
   * Cut the mdia atom at the specified time and return a new object
   * @param time the time at which the atom is cut
   * @return a new mdia atom with new data
   */
  public MdiaAtom cut(long time) {
    MdiaAtom cutMdia = new MdiaAtom();
    cutMdia.setMdhd(mdhd.cut());
    cutMdia.setHdlr(hdlr.cut());
    cutMdia.setMinf(minf.cut(time));
    // update the duration of the media
    long newDuration = cutMdia.getMinf().getStbl().getStts().computeDuration();
    cutMdia.getMdhd().setDuration(newDuration);
    cutMdia.recomputeSize();
    return cutMdia;
  }
  
  @Override
  public void accept(AtomVisitor v) throws AtomException {
    v.visit(this); 
  }
  
  /**
   * Write the mdia atom data to the specified output
   * @param out where the data goes
   * @throws IOException if there is an error writing the data
   */
  @Override
  public void writeData(DataOutput out) throws IOException {
    writeHeader(out);
    mdhd.writeData(out);
    hdlr.writeData(out);
    minf.writeData(out);
    writeUnknownChildren(out);
  }
}