/**
 * 
 */
package mp4.util.atom;

import java.io.DataOutput;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * @author system-3
 *
 */
public class RmdaAtom extends ContainerAtom {
	
	//must
	 // the movie header atom
	  private RdrfAtom rdrf;
	  
	  //optional!
	  // initial object descriptor 
	  private RmdrAtom rmdr;
	  // the user data atom
	  private RmcsAtom rmcs;
	  // the list of tracks
	// the movie header atom
	  private RmvcAtom rmvc;
	  // initial object descriptor 
	  private RmcdAtom rmcd;
	  // the user data atom
	  private RmquAtom rmqu;
	  // the list of tracks

	/**
	 * @param type
	 */
	public RmdaAtom() {
		super(new byte[]{'r','m','d','a'});
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param old
	 */
	public RmdaAtom(RmdaAtom old) {
		super(old);
		// TODO Auto-generated constructor stub
		rdrf = new RdrfAtom(old.rdrf);
	    if (rmdr != null) {
	    	rmdr = new RmdrAtom(old.rmdr);
	    }
	    if (rmcs != null) {
	    	rmcs = new RmcsAtom(old.rmcs);
	    }
	    if (rmvc != null) {
	    	rmvc = new RmvcAtom(old.rmvc);
	    }
	    if (rmcd != null) {
	    	rmcd = new RmcdAtom(old.rmcd);
	    }
	    if (rmqu != null) {
	    	rmqu = new RmquAtom(old.rmqu);
	    }
	    
	   
	}

	/* (non-Javadoc)
	 * @see mp4.util.atom.ContainerAtom#addChild(mp4.util.atom.Atom)
	 */
	@Override
	public void addChild(Atom atom) {
		// TODO Auto-generated method stub
		  if (atom instanceof RdrfAtom) {
		      this.rdrf = (RdrfAtom) atom;
		    }
		    else if (atom instanceof RmdrAtom) {
		      this.rmdr = (RmdrAtom) atom;
		    }
		    else if (atom instanceof RmcsAtom) {
		      this.rmcs = (RmcsAtom) atom;
		    }
		    else if (atom instanceof RmvcAtom) {
		    	this.rmvc = (RmvcAtom) atom;
		    }
		    else if (atom instanceof RmcdAtom) {
		    	this.rmcd = (RmcdAtom) atom;
		    }
		    else if (atom instanceof RmquAtom) {
		    	this.rmqu = (RmquAtom) atom;
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
		long newSize = rdrf.size();
		 
		 if (rmdr != null) 
		 {
		    newSize += rmdr.size();
		 }
		 if (rmcs != null) 
		 {
		      newSize += rmcs.size();
		 }
		 if (rmvc != null) 
		 {
		     newSize += rmvc.size();
		 }
	    if (rmcd != null) {
	      newSize += rmcd.size();
	    }
	    if (rmqu != null) {
	      newSize += rmqu.size();
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
		rdrf.writeData(out);
	   
		 if (rmdr != null) {
		    	rmdr.writeData(out);
		    }
		if (rmcs != null) {
	    	rmcs.writeData(out);
	    }
	   
	   
	    if (rmvc != null) {
	    	rmvc.writeData(out);
	    }
	   
	    if (rmcd != null) {
	    	rmcd.writeData(out);
	    }
	    if (rmqu != null) {
	    	rmqu.writeData(out);
	    }
	   
	   
	}

	public long getTimeScale() {
		// TODO Auto-generated method stub
		return 0;
	}

	public long getDuration() {
		// TODO Auto-generated method stub
		return 0;
	}

	public RmdaAtom cut() {
		// TODO Auto-generated method stub
		return null;
	}

	public TrakAtom cut(float time, long movieTimeScale) {
		// TODO Auto-generated method stub
		return null;
	}

	public void setDuration(long cutDuration) {
		// TODO Auto-generated method stub
		
	}

	public void fixupOffsets(long delta) {
		// TODO Auto-generated method stub
		
	}

}
