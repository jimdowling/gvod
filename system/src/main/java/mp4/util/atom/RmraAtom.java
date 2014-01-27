/**
 * 
 */
package mp4.util.atom;

import java.io.DataOutput;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * @author system-3
 *
 */
public class RmraAtom extends ContainerAtom {

	 // the list of tracks
	  private List<RmdaAtom> m_rmdaList;

	  
	public RmraAtom() {
		super(new byte[]{'r','m','r','a'});
		// TODO Auto-generated constructor stub
	}

	
	/**
	   * Copy constructor for RmraAtom atom.  Performs a deep copy.
	   * @param old the movie atom to copy
	   */
	  public RmraAtom(RmraAtom old) {
	    super(old);
	    //TODO
	    m_rmdaList = new LinkedList<RmdaAtom>();
	    for (Iterator<RmdaAtom> i = old.getRmdas(); i.hasNext(); ) {
	    	m_rmdaList.add(new RmdaAtom(i.next()));
	      }
	  }
	  

	  

	  
	  /**
	   * Return an iterator with the media's tracks.  For most movies, there are two tracks, the sound 
	   * track and the video track.
	   * @return an iterator with the movie traks.
	   */
	  public Iterator<RmdaAtom> getRmdas() {
	    return m_rmdaList.iterator();
	  }
	  
	/* (non-Javadoc)
	 * @see mp4.util.atom.ContainerAtom#addChild(mp4.util.atom.Atom)
	 */
	@Override
	public void addChild(Atom atom) {
		// TODO Auto-generated method stub
		if (atom instanceof RmdaAtom) {
		      if (m_rmdaList == null) {
		    	  m_rmdaList = new LinkedList<RmdaAtom>();
		      }
		      m_rmdaList.add((RmdaAtom) atom);
		    }
		    else {
		      throw new AtomError("Can't add " + atom + " to moov");
		    }
	}

	/* (non-Javadoc)
	 * @see mp4.util.atom.ContainerAtom#recomputeSize()
	 */
	@Override
	protected void recomputeSize() {
		// TODO Auto-generated method stub
		long newSize = 0;
	    for (Iterator<RmdaAtom> i = getRmdas(); i.hasNext(); ) {
	      newSize += i.next().size();
	    }
	    
	    setSize(ATOM_HEADER_SIZE + newSize);
	}
	

	/* (non-Javadoc)
	 * @see mp4.util.atom.Atom#accept(mp4.util.atom.AtomVisitor)
	 */
	@Override
	public void accept(AtomVisitor v) throws AtomException {
		// TODO Auto-generated method stub
		 v.visit(this);
	}

	/* (non-Javadoc)
	 * @see mp4.util.atom.Atom#writeData(java.io.DataOutput)
	 */
	@Override
	public void writeData(DataOutput out) throws IOException {
		// TODO Auto-generated method stub
		writeHeader(out);
	 
	   
	    for (Iterator<RmdaAtom> i = getRmdas(); i.hasNext(); ) {
	      i.next().writeData(out);
	    }
	   
	}
	
	

}
