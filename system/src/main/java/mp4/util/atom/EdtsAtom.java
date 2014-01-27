/**
 * 
 */
package mp4.util.atom;

import java.io.DataOutput;
import java.io.IOException;

/**
 * The edit list container atom.
 */
public class EdtsAtom extends ContainerAtom {
  // the edit list (optional)
  private ElstAtom elst;
  
  /**
   * Construct an empty edit list container atom.
   */
  public EdtsAtom() {
    super(new byte[]{'e','d','t','s'});
  }
  
  /**
   * Copy constructor.  Performs a deep copy.
   * @param old the version to copy
   */
  public EdtsAtom(EdtsAtom old) {
    super(old);
    if (old.elst != null) {
      elst = new ElstAtom(old.elst);
    }
  }
  
  /**
   * Create an edit list container with the specified edit list
   * @param editList the edit list to add to the container
   */
  public EdtsAtom(ElstAtom editList) {
    this();
    this.elst = editList;
    recomputeSize();
  }
  
  /**
   * Return the edit list atom.  Null if there isn't an edit list
   * @return  the edit list atom.
   */
  public ElstAtom getElst() { 
    return elst; 
  }
  
  /**
   * Set the edit list atom for the edit list container
   * @param elst the edist list atom
   */
  public void setElst(ElstAtom elst) {
    this.elst = elst;
  }

  /**
   * Add an elst atom to the edts atom.  If it's not an elst atom,
   * then throw a run-time exception.
   * @param child the elst atom to add
   */
  @Override
  public void addChild(Atom child) {
    if (child instanceof ElstAtom) {
      elst = (ElstAtom) child;
    }
    else {
      //throw new AtomError("Can't add " + child + " to edts");
      addUnknownChild(child);
    }
  }

  /**
   * Recompute the size of the edts atom, which is a noop since the contents
   * do not change.
   */
  @Override
  protected void recomputeSize() {
	long newSize = ATOM_HEADER_SIZE + elst.size();
	newSize += unknownChildrenSize();
    setSize(newSize);    
  }

  /**
   * Cut the edit list atom, which does not change the contents.  This method
   * returns a copy.
   * @return a copy of the edit list atom
   */
  public EdtsAtom cut() {
    return new EdtsAtom(this);
  }
  
  @Override
  public void accept(AtomVisitor v) throws AtomException {
    v.visit(this);
  }

  /**
   * Write the edts atom data to the specified output
   * @param out where the data goes
   * @throws IOException if there is an error writing the data
   */  
  @Override
  public void writeData(DataOutput out) throws IOException {
    writeHeader(out);
    if (elst != null) {
      elst.writeData(out);
    }
    writeUnknownChildren(out);
  }

  /**
   * Update the specified time with information in the edit list
   * @param time the time in seconds
   * @param mediaTS the media time scale
   * @param movieTS the movie time scale
   * @return the updated time in the media time scale
   */
  public long editTime(float time, long mediaTS, long movieTS) {
    if (elst == null) {
      return (long)(time * mediaTS);
    }
    return elst.editTime(time, mediaTS, movieTS);
  }
}