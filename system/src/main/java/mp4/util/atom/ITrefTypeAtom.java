package mp4.util.atom;

import java.io.DataOutput;
import java.io.IOException;

public interface ITrefTypeAtom {

  /**
   * Return the atom's size.
   * @return the atom's size
   */
  public long size();
  
  /**
   * Create a copy of the tref type
   * @return a new copy of the tref type atom
   */
  public ITrefTypeAtom copy();
  
  /**
   * Write the data to the specified output
   * @param out where the data is written
   */
  public void writeData(DataOutput out) throws IOException;
}
