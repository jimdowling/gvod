package mp4.util.atom;

import java.io.DataOutput;
import java.io.IOException;

/**
 * Common interface for the media information header atoms
 */
public interface IMhdAtom {

  /**
   * Return a deep copy of the media information header
   * @return a copy of the media inforamtion header object
   */
  public IMhdAtom copy();
  
  /**
   * Return the media information header size
   * @return the media information header size
   */
  public long size();
  
  /**
   * Cut the media information header.  This method just returns a copy
   * of the object
   * @return a new copy of the media information header.
   */
  public IMhdAtom cut();
  
  /**
   * Write the media header to the specified output file.
   * @param out the output where the data is written
   * @throws IOException
   */
  public void writeData(DataOutput out) throws IOException;
}
