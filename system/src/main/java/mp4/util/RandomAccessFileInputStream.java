package mp4.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class RandomAccessFileInputStream extends RandomAccessInputStream {

	public static  int DEFAULT_BUFFER_SIZE = 16 * 1024;
	byte[] buf = null;
	long filePos = 0;
	int bufp;
	int curBufSize = 0;
	RandomAccessFile fp;
	long markPos;
	long fileLength = -1;
	byte[] singlebyteb = new byte[1];

	
	public RandomAccessFileInputStream(File file, int bufferSize)
			throws FileNotFoundException {
		fp = new RandomAccessFile(file, "r");
		try {
			fileLength = fp.length();
		} catch(IOException e) { }
		buf = new byte[bufferSize];
		resetBuffer();
	}
	
	public int available() {
		return (int)(fileLength == -1 ? -1 : fileLength - filePos);
	}
	
	public RandomAccessFileInputStream(File file)
	throws FileNotFoundException {
		this(file, DEFAULT_BUFFER_SIZE);
	}

	public RandomAccessFileInputStream(String filename)
	throws FileNotFoundException {
		this(new File(filename), DEFAULT_BUFFER_SIZE);
	}

	/* (non-Javadoc)
	 * @see com.unwiredappeal.tivo.utils.RandomAccessInputStream#read()
	 */
	@Override
	public int read() throws IOException {
		int len = read(singlebyteb);
		if (len == 0)
			return -1;
		else
			return ((int)singlebyteb[0])&0xff;
//		return fp.read();
	}

	public void fillBuffer() throws IOException {
		if (bufp >= curBufSize) {
			if (filePos != fp.getFilePointer())
				fp.seek(filePos);
			int len = fp.read(buf, 0, buf.length);
			curBufSize = Math.max(len, 0);
			bufp = 0;
		}
	}
	/* (non-Javadoc)
	 * @see com.unwiredappeal.tivo.utils.RandomAccessInputStream#read(byte[], int, int)
	 */
	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		int cnt = 0;
		while(len > 0) {
			fillBuffer();
			if (curBufSize == 0)
				return cnt;
			for (int i=0;i<len && bufp < curBufSize;i++) {
				b[off+cnt] = buf[bufp];
				cnt++;
				bufp++;
				len--;
				filePos++;
			}
			//return fp.read(b, off, len);
		}
		return cnt;
	}
	
	/* (non-Javadoc)
	 * @see com.unwiredappeal.tivo.utils.RandomAccessInputStream#read(byte[])
	 */
	@Override
	public int read(byte[] b) throws IOException {
		return read(b, 0, b.length);
	}
	
	/* (non-Javadoc)
	 * @see com.unwiredappeal.tivo.utils.RandomAccessInputStream#close()
	 */
	public void close() throws IOException {
		fp.close();
	}
	
	/* (non-Javadoc)
	 * @see com.unwiredappeal.tivo.utils.RandomAccessInputStream#getFilePointer()
	 */
	public long getFilePointer() throws IOException {
		return filePos;
	}

	/*
	public int skip(int n) throws IOException {
		return fp.skipBytes(n);
	}
	*/
	
	/* (non-Javadoc)
	 * @see com.unwiredappeal.tivo.utils.RandomAccessInputStream#seek(long)
	 */
	public void seek(long pos) throws IOException {
		long offset = pos - filePos;
		fp.seek(pos);
		filePos = pos;
		long newbufp = bufp + offset;
		if (newbufp < 0 || newbufp > buf.length)
			resetBuffer();
		else
			bufp = (int)newbufp;
	}
	
	/* (non-Javadoc)
	 * @see com.unwiredappeal.tivo.utils.RandomAccessInputStream#length()
	 */
	public long length() throws IOException {
		return fp.length();
	}
	
	  /* (non-Javadoc)
	 * @see com.unwiredappeal.tivo.utils.RandomAccessInputStream#markSupported()
	 */
	public boolean markSupported() {
		  return true;
	  }
	  
	  /* (non-Javadoc)
	 * @see com.unwiredappeal.tivo.utils.RandomAccessInputStream#mark(int)
	 */
	public void mark(int readLimit) {
		  try {
			markPos = getFilePointer();
		} catch (IOException e) {
		}
	  }
	  
	  /* (non-Javadoc)
	 * @see com.unwiredappeal.tivo.utils.RandomAccessInputStream#reset()
	 */
	public void reset() throws IOException {
		  seek(markPos);
	  }
	  
	  private void resetBuffer() {
		  bufp = buf.length;
	  }
	  
	  /* (non-Javadoc)
	 * @see com.unwiredappeal.tivo.utils.RandomAccessInputStream#skip(long)
	 */
	public long skip(long n) throws IOException {
		  long cpos = getFilePointer();
		  long left = length() - cpos;
		  n = Math.min(n, left);
		  seek(cpos+n);
		  return n;
	  }
	  

}
