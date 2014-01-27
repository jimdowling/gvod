/**
 * 
 */
package mp4.util.atom;

import java.io.DataOutput;
import java.io.IOException;
import mp4.util.MP4Log;


/**
 * The container atom for a single track of a presentation. Movie presentation typically
 * have two tracks, one for sound and one for video.
 */
public class TrakAtom extends ContainerAtom {
  // the track header
  private TkhdAtom tkhd;
  // the track reference container
  private TrefAtom tref;
  // the media information
  private MdiaAtom mdia;
  // the edit list container
  private EdtsAtom edts;
  // user data
  private UdtaAtom udta;
  
  /**
   * Constructor
   */
  public TrakAtom() {
    super(new byte[]{'t','r','a','k'});
  }
  
  /**
   * Copy constructor.  Performs a deep copy.
   * @param old the version to copy
   */
  public TrakAtom(TrakAtom old) {
    super(old);
    tkhd = new TkhdAtom(old.tkhd);
    if (old.tref != null) {
      tref = new TrefAtom(old.tref);
    }
    mdia = new MdiaAtom(old.mdia);
    if (old.edts != null) {
      edts = new EdtsAtom(old.edts);
    }
    if (old.udta != null) {
      udta = new UdtaAtom(old.udta);
    }
  }

  /**
   * Return the track header atom.
   * @return the track header atom
   */
  public TkhdAtom getTkhd() { 
    return tkhd; 
  }

  /**
   * Set the track header atom
   * @param tkhd the new track header atom
   */
  public void setTkhd(TkhdAtom tkhd) {
    this.tkhd = tkhd;
  }
  
  /**
   * Return the track reference atom, or null if there isn't one.
   * @return the track reference atom
   */
  public TrefAtom getTref() {
    return tref;
  }
  
  /**
   * Set the track reference atom
   * @param tref the new track reference atom
   */
  public void setTref(TrefAtom tref) {
    this.tref = tref;
  }

  /**
   * Return the media container
   * @return the media container
   */
  public MdiaAtom getMdia() { 
    return mdia; 
  }
  
  /**
   * Set the media container atom
   * @param mdia the new media container
   */
  public void setMdia(MdiaAtom mdia) {
    this.mdia = mdia;
  }

  /**
   * Return the edit list atom
   * @return return edit list atom
   */
  public EdtsAtom getEdts() {
    return edts;
  }
  
  /**
   * Set the track edit list.
   * @param edts the new track edit list
   */
  public void setEdts(EdtsAtom edts) {
    this.edts = edts;
  }
  
  /**
   * Return the user-data atom
   * @return the user-data atom
   */
  public UdtaAtom getUdta() {
    return udta;
  }
  
  /**
   * Set the user-data atom
   * @param udta the new user-data atom
   */
  public void setUdta(UdtaAtom udta) {
    this.udta = udta;
  }
  
  /**
   * Add an atom to the container.  If the atom is not recognized, then
   * a run-time error is thrown.
   * @param child the atom to add to the trak atom
   */
  @Override
  public void addChild(Atom child) {
    if (child instanceof TkhdAtom) {
      tkhd = (TkhdAtom) child;
    }
    else if (child instanceof TrefAtom) {
      tref = (TrefAtom) child;
    }
    else if (child instanceof MdiaAtom) {
      mdia = (MdiaAtom) child;
    }
    else if (child instanceof EdtsAtom) {
      edts = (EdtsAtom) child;
    }
    else if (child instanceof UdtaAtom) {
      udta = (UdtaAtom) child;
    }
    else {
      //throw new AtomError("Can't add " + child + " to trak");
      addUnknownChild(child);
    }
  }
  
  /**
   * Recompute the size of the track atom, which needs to be done if
   * the contents change.
   */
  @Override
public void recomputeSize() {
    long newSize = tkhd.size() + mdia.size();
    if (tref != null) {
      newSize += tref.size();
    }
    if (edts != null) {
      newSize += edts.size();
    }
    if (udta != null) {
      newSize += udta.size();
    }
    newSize += unknownChildrenSize();
    setSize(ATOM_HEADER_SIZE + newSize);
  }


  
  /**
   * Cut the track atom at the specified time (seconds).  The time needs to be normalized
   * to the media's time-scale.
   * @param time the normalized time, in seconds, to cut the track 
   * @param l
   * @param movieTimeScale the time-scale for the movie
   * @return a new track atom that has been cut
   */
  public TrakAtom cut(long pos, long scale) {
    TrakAtom cutTrak = new TrakAtom();
    long mediaTimeScale = mdia.getMdhd().getTimeScale();
    long mediaTime;
    if (mediaTimeScale == scale) {
    	mediaTime = pos;
    } else {
    	mediaTime = (long)(((float)pos/(float)scale) * mediaTimeScale);
    }
    //= (long)(Math.rint(time * mediaTimeScale));
    MP4Log.log("DBG: media time " + mediaTime);
//    if (edts != null) {
//      mediaTime = edts.editTime(time, mediaTimeScale, movieTimeScale);
//      MP4Log.log("DBG: media time after edit " + mediaTime);
//    }
    cutTrak.setTkhd(tkhd.cut());
    cutTrak.setMdia(mdia.cut(mediaTime));
    if (edts != null) {
      cutTrak.setEdts(edts.cut());
    }
    if (udta != null) {
      cutTrak.setUdta(udta.cut());
    }
    if (tref != null) {
      cutTrak.setTref(tref.cut());
    }
    cutTrak.recomputeSize();
    return cutTrak;
  }

  @Override
  public void accept(AtomVisitor v) throws AtomException {
    v.visit(this); 
  }

  /**
   * Write the trak atom data to the specified output
   * @param out where the data goes
   * @throws IOException if there is an error writing the data
   */
  @Override
  public void writeData(DataOutput out) throws IOException {
    writeHeader(out);
    tkhd.writeData(out);
    mdia.writeData(out);
    if (edts != null) {
      edts.writeData(out);
    }
    if (udta != null) {
      udta.writeData(out);
    }
    if (tref != null) {
      tref.writeData(out);
    }
    writeUnknownChildren(out);
  }
  
  /**
   * Change the duration of the track.  This requires changing the duration in the track
   * header and the edit list.  The duration is in the movie timescale.
   * @param duration the new track duration in the movie timescale.
   */
  public void fixupDuration(long duration) {
    tkhd.setDuration(duration);
    if (edts != null) {
      edts.getElst().setDuration(duration);
    }
  }

  /**
   * Fixup the chunk offsets values located in the stco atom.  This needs
   * to be done if the size of any atoms has changed since the chunk offset
   * values are absolute values from the start of the file.
   * @param delta the amount to update each chunk offset.
   */
  public void fixupOffsets(long delta) {
    getMdia().getMinf().getStbl().getStco().fixupOffsets(delta);
  }
  
  /**
   * Return the duration converted to the specified time-scale.
   * @param timeScale the normalized time-scale value
   * @return the duration converted to the specified time-scale.
   */
  public long convertDuration(long timeScale) {
    return (long)(getMdia().getMdhd().getDuration() * 
        ((double)timeScale / (double)getMdia().getMdhd().getTimeScale()));
  }
  
  /**
   * Convert the specified duration to the media time scale.
   * @param duration the specified duration to be converted
   * @param timeScale the time-scale of the specified duration
   * @return the duration converted to media time-scale.
   */
  public long convertToMediaScale(long duration, long timeScale) {
    return (long)(duration *
        ((double)getMdia().getMdhd().getTimeScale() / timeScale));
  }
  
  /**
   * Add an edit to the track.
   * @param editTime the media time of the edit
   */
  public void addEdit(long editTime) {
    ElstAtom elst = new ElstAtom(1);
    elst.addEntry(1, tkhd.getDuration(), editTime, 1);
    setEdts(new EdtsAtom(elst));
  }
  
  public boolean isEnabled() {
	  if (tkhd == null)
		  return false;
	  return (tkhd.getFlagValue() & 1) == 1;
  }
  
}