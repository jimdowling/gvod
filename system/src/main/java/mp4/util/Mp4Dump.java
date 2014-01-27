package mp4.util;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;

import mp4.util.atom.Atom;
import mp4.util.atom.AtomException;
import mp4.util.atom.CttsAtom;
import mp4.util.atom.DefaultAtomVisitor;
import mp4.util.atom.ElstAtom;
import mp4.util.atom.FtypAtom;
import mp4.util.atom.HdlrAtom;
import mp4.util.atom.LeafAtom;
import mp4.util.atom.MdhdAtom;
import mp4.util.atom.MvhdAtom;
import mp4.util.atom.StcoAtom;
import mp4.util.atom.StscAtom;
import mp4.util.atom.StsdAtom;
import mp4.util.atom.StssAtom;
import mp4.util.atom.StszAtom;
import mp4.util.atom.SttsAtom;
import mp4.util.atom.TkhdAtom;

public class Mp4Dump extends DefaultAtomVisitor {
  // the input mp4 file
  private DataInputStream mp4file;
  // the output stream
  private PrintStream out;
  // the current indentation level
  private int level;
  // the last media handler processed, need since the stsd requires context
  private HdlrAtom mediaHandler;
  
  private static String outputFile = null;
  private static String inputFile = null; 
  private static int maxEntries = Integer.MAX_VALUE;

  /**
   * Constructor for the Mpeg-4 file reader.  It opens the mp4 file.
   * @param inputfn the Mpeg-4 file name
   * @param outputfn where the output goes, System.out by default
   */
  public Mp4Dump(String inputfn, String outputfn) {
    this.level = 0;
    this.out = System.out;
    this.mediaHandler = null;
    try {
      mp4file = new DataInputStream(new FileInputStream(inputfn));
      if (outputfn != null) {
        this.out = new PrintStream(outputfn);
      }
    } catch (FileNotFoundException e) {
      MP4Log.log("File not found " + inputfn);
      e.printStackTrace();
      System.exit(-1);
    }
  }
  
  /** 
   * Method to indent the output based upon the current level
   */
  private void indent() {
    for (int i = 0; i < level; i++) {
      out.print("\t");
    }
  }
  
  /**
   * Print with indentation the leaf atom header data
   * @param atom the leaf atom to print and indent
   * @throws AtomException
   */
  private void printLeafHeader(LeafAtom atom) throws AtomException {
    indent();
    out.print(atom);
    atom.readData(mp4file);
  }

  
  /**
   * The default action for an atom, which covers both leaf and 
   * container atoms.  For leaf atoms, only the name is printed.
   * Otherwise, the visit method for the atom needs to be implemented.
   * For a container atom, the method increments the indent level
   * and processes the atoms in the container.
   * @param atom the atom to process
   * @throws AtomException is there is an IOException
   */
  @Override
  protected void defaultAction(Atom atom) throws AtomException {
    indent();
    out.println(atom);
    if (atom.isContainer()) {
      level = level + 1;
      long bytesRead = 0;
      long bytesToRead = atom.dataSize();
      while (bytesRead < bytesToRead) {
        bytesRead += printAtom();
      }
      level = level - 1;
    }
    else {
      try {
        // some more ugly code to deal with unsigned vs. signed problems
        long dataSize = atom.dataSize();
        while (dataSize > Integer.MAX_VALUE) {
          mp4file.skipBytes(Integer.MAX_VALUE);
          dataSize -= Integer.MAX_VALUE;
        }
        mp4file.skipBytes((int)dataSize);
      } catch (IOException e) {
        throw new AtomException("Unable to read mp4 file");
      }
    }
  }
  
  @Override
  public void visit(CttsAtom atom) throws AtomException {
    printLeafHeader(atom);
    out.println(" entries " + atom.getNumEntries());
    level = level + 1;
    for (int i = 0; i < atom.getNumEntries() && i < maxEntries; i++) {
      indent();
      out.println(atom.getSampleCount(i) + " " + atom.getSampleOffset(i));
    }
    level = level - 1;
  }

  @Override
  public void visit(ElstAtom atom) throws AtomException {
    printLeafHeader(atom);
    out.println(" entries " + atom.getNumEntries());
    level = level + 1;
    for (int i = 0; i < atom.getNumEntries() && i < maxEntries; i++) {
      indent();
      out.println("duration " + atom.getDuration(i) +
          " time " + atom.getMediaTime(i) + " rate " + atom.getMediaRate(i));
    }
    level = level - 1;
  }

  @Override
  public void visit(FtypAtom atom) throws AtomException {
    printLeafHeader(atom);
    out.println(" " + new String(atom.getMajorBrand()));
  }

  @Override
  public void visit(HdlrAtom atom) throws AtomException {
    printLeafHeader(atom);
    mediaHandler = atom;
    out.println(" " + atom.getHandlerType());
  }

  @Override
  public void visit(MvhdAtom atom) throws AtomException {
    printLeafHeader(atom);
    long ts = atom.getTimeScale();
    long duration = atom.getDuration();
    printDuration(ts, duration);
    out.println();
  }

  @Override
  public void visit(MdhdAtom atom) throws AtomException {
    printLeafHeader(atom);
    long ts = atom.getTimeScale();
    long duration = atom.getDuration();
    printDuration(ts, duration);
    out.println();
  }

  @Override
  public void visit(StcoAtom atom) throws AtomException {
    printLeafHeader(atom);
    out.println(" entries " + atom.getNumEntries());
    level = level + 1;
    for (int i = 0; i < atom.getNumEntries() && i < maxEntries; i++) {
      indent();
      out.println((i+1) + " " + atom.getChunkOffset(i+1));
    }
    level = level - 1;
  }

  @Override
  public void visit(StscAtom atom) throws AtomException {
    printLeafHeader(atom);
    out.println(" entries " + atom.getNumEntries());
    level = level + 1;
    for (int i = 0; i < atom.getNumEntries(); i++) {
      indent();
      out.println(atom.getFirstChunk(i) +
          " " + atom.getSamplesPerChunk(i) +
          " " + atom.getDescriptionId(i));
    }
    level = level - 1;
  }

  @Override
  public void visit(StsdAtom atom) throws AtomException {
    //printLeafHeader(atom);
    out.print(" entries " + atom.getNumEntries());
    /*
    for (int i = 0 ; i < atom.getNumEntries(); i++) {
      if (mediaHandler.isVideo()) {
        out.print(" " + atom.getWidth() + "x" + atom.getHeight());
      }
      else if (mediaHandler.isSound()) {
      
      }
      out.println();
    }
    */
 }

  @Override
  public void visit(StssAtom atom) throws AtomException {
    printLeafHeader(atom);
    long numEntries = atom.getNumEntries();
    out.println(" entries " + numEntries);
    level = level + 1;
    for (int i = 0; i < numEntries && i < maxEntries; i++) {
      indent();
      out.println((i + 1) + " "  + atom.getSampleEntry(i));
    }
    level = level - 1;
  }

  @Override
  public void visit(StszAtom atom) throws AtomException {
    printLeafHeader(atom);
    out.print(" entries " + atom.getNumEntries());
    long size = atom.getSampleSize();
    if (size != 0) {
      out.println(" sample size " + size);
    }
    else {
      out.println();
      level = level + 1;
      long totalSize = 0;
      for (int i = 0; i < atom.getNumEntries() && i < maxEntries; i++) {
        indent();
        out.println((i + 1) + " "  + atom.getTableSampleSize(i+1));
        totalSize += atom.getTableSampleSize(i+1);
      }
      level = level - 1;
    }
 }

  @Override
  public void visit(SttsAtom atom) throws AtomException {
    printLeafHeader(atom);
    out.println(" entries " + atom.getNumEntries());
    level = level + 1;
    long time = 0;
    for (int i = 0; i < atom.getNumEntries() && i < maxEntries; i++) {
      indent();
      out.println(atom.getSampleCount(i) + " " + atom.getSampleDuration(i));
      time += atom.getSampleCount(i) * atom.getSampleDuration(i);
    }
    indent();
    out.println("stts time = " + time);
    level = level - 1;
  }

  @Override
  public void visit(TkhdAtom atom) throws AtomException {
    printLeafHeader(atom);
    //out.print(" track " + atom.getTrackId() + " " + atom.getDuration()); 
    out.println();
  }

  /**
   * Read an atom from the mpeg4 file and print it.
   * @return the number of bytes read
   * @throws AtomException
   */
  private long printAtom() throws AtomException {
    // get the atom size
    byte[] word = new byte[Atom.ATOM_WORD];
    int num;
    try {
      num = mp4file.read(word);
    } catch (IOException e1) {
      throw new AtomException("IOException while reading file");
    }
    // check for end of file
    if (num == -1) {
      return -1;
    }
    if (num != Atom.ATOM_WORD) {
      throw new AtomException("Unable to read enough bytes for atom");
    }
    long size = Atom.byteArrayToUnsignedInt(word, 0);
    // get the atom type
    try {
      num = mp4file.read(word);
    } catch (IOException e1) {
      throw new AtomException("IOException while reading file");
    }
    if (num != Atom.ATOM_WORD) {
      throw new AtomException("Unable to read enough bytes for atom");  
    }
    try {
      Class<?> cls = Class.forName(Atom.typeToClassName(word, null));
      Atom atom = (Atom) cls.newInstance();
      atom.setSize(size);
      atom.accept(this);
    } catch (ClassNotFoundException e) {
      throw new AtomException("Class not found " + e);
    } catch (InstantiationException e) {
      throw new AtomException("Unable to instantiate atom");
    } catch (IllegalAccessException e) {
      throw new AtomException("Unabel to access atom object");
    }
    return size;
  }
    
  public void printDuration(long timeScale, long duration) {
    long totalSeconds = duration/timeScale;
    out.print(" timescale " + timeScale);
    out.print(" duration " + duration);
    out.print(" time ");
    if (totalSeconds > 3600) {
      long hours = totalSeconds / 3600;
      out.print(hours + "h");
      totalSeconds = totalSeconds - (hours * 3600);
    }
    if (totalSeconds > 60) {
      long minutes = totalSeconds / 60;
      out.print(minutes + "m");
      totalSeconds = totalSeconds - (minutes * 60);
    }
    out.print(totalSeconds + "s");
  }
  
  /**
   * Process the command line arguments.
   * @param args the user-specified arguments
   */
  private static void processArgs(String[] args) {
    int i = 0;
    while (i < args.length) {
      String arg = args[i];
      if (arg.equals("-in")) {
        inputFile = args[++i];
      }
      else if (arg.equals("-out")) {
        outputFile = args[++i];
      }
      else if (arg.equals("-top")) {
        maxEntries = Integer.valueOf(args[++i]);
      }
      else {
        help();
      }
      i++;
    }
    if (inputFile == null) {
      help();
    }
  }
  
  private static void help() {
    MP4Log.log("Mp4Dump <args>");
    MP4Log.log("  -in inputfile.mp4");
    MP4Log.log("  [-out outputfile.txt]\tdefault=System.out");
    MP4Log.log("  [-top num]\tdefault=all");
    System.exit(-1);
  }
  
  /**
   * Main for the dump utility
   * @param args the user-specifed arguments
   */
  public static void main(String[] args) {
    processArgs(args);
    Mp4Dump reader = new Mp4Dump(inputFile, outputFile);
    try {
      long size = 0;;
      while (size != -1) {
        size = reader.printAtom();
      }
    } catch (AtomException e) {
      MP4Log.log("Invalid atom descriptor");
      e.printStackTrace();
    }
  }

}
