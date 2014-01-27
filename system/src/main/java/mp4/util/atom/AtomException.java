package mp4.util.atom;

/**
 * This exception is thrown when there is an invalid atom in the stream.
 */
@SuppressWarnings("serial")
public class AtomException extends Exception {
  public AtomException(String msg) {
    super(msg);
  }
}