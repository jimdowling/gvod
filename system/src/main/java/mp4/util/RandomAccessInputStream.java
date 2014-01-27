package mp4.util;

import java.io.IOException;
import java.io.InputStream;

public abstract class RandomAccessInputStream extends InputStream {

	public abstract int read() throws IOException;

	public abstract int read(byte[] b, int off, int len) throws IOException;

	public abstract int read(byte[] b) throws IOException;

	public abstract void close() throws IOException;

	public abstract long getFilePointer() throws IOException;

	public abstract void seek(long pos) throws IOException;

	public abstract long length() throws IOException;

	public abstract boolean markSupported();

	public abstract void mark(int readLimit);

	public abstract void reset() throws IOException;

	public abstract long skip(long n) throws IOException;

}