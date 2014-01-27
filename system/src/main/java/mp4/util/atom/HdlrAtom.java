/**
 * 
 */
package mp4.util.atom;

/**
 * The media handler type atom.  This indicates the media type, i.e.
 * video, sound, etc.
 */
@SuppressWarnings("unused")
public class HdlrAtom extends LeafAtom {
  // this is component_type in QT spec.
  private static final int PREDEFINED_OFFSET = 4;
  // this is component_subtype in QT spec.
  private static final int HANDLER_TYPE_OFFSET = 8;
  // this is component_manufacturer, component_flags, and flags_mask in QT spec.
  private static final int RESERVED_OFFSET = 12;
  // this is component name in QT spec.
  private static final int NAME_OFFSET = 24;
  
  private static final String VIDEO = "vide";
  private static final String SOUND = "soun";
  private static final String HINT = "hint";
  
  /**
   * Construct an empty hdlr atom.
   */
  public HdlrAtom() {
    super(new byte[]{'h','d','l','r'});
  }
  
  /**
   * Copy constructor. Performs a deep copy
   * @param old the version to copy
   */
  public HdlrAtom(HdlrAtom old) {
    super(old);
  }
  
  /**
   * Return the media handler type as a string.
   * @return the media handler type as a string.
   */
  public String getHandlerType() {
    return new String(data.getData(HANDLER_TYPE_OFFSET, HANDLER_TYPE_OFFSET + ATOM_WORD));
  }
  
  /**
   * Return the name of the track.  The name is used for debugging purposes.
   * @return the name of the track.
   */
  public String getName() {
    int length = 0;
    for (int i = NAME_OFFSET; i < size(); i++, length++) {
      if (data.getData(i) == 0) {
        break;
      }
    }
    if (length == 0) {
      return "";
    }
    return new String(data.getData(NAME_OFFSET, length));
  }
  
  /**
   * Cut the hdlr atom, which does not change the contents.  This method
   * returns a copy.
   * @return a copy of the hdlr atom
   */
  public HdlrAtom cut() {
    return new HdlrAtom(this);
  }
  
  @Override
  public void accept(AtomVisitor v) throws AtomException {
    v.visit(this); 
  }
  
  /**
   * Return true if this is a video media handler.
   * @return true if this is a video media hander.
   */
  public boolean isVideo() {
    return getHandlerType().equals(VIDEO);
  }
  
  /**
   * Return true if this is a sound media handler.
   * @return true if this is a sound media handler.
   * @return
   */
  public boolean isSound() {
    return getHandlerType().equals(SOUND);
  }
  
}