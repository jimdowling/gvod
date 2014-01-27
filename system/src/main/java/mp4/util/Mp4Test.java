package mp4.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class Mp4Test extends Mp4Split {

	private File dst;
	private File src;
	long startPos;
	
	public Mp4Test(File src, File dst, long startPos) throws IOException 
	{
		this.src = src;
		this.dst = dst;
		this.startPos = startPos;
	}	
	
	public void write() throws IOException {
		time = startPos / 1000.0f;
		InputStream is = new RandomAccessFileInputStream(src);
		mp4file = new DataInputStream(is);

		long offsetMdata = calcSplitMp4(false);
		OutputStream os = new FileOutputStream(dst);
		DataOutputStream dataOut = new DataOutputStream(os);
                long len = lenSplitMp4();
                System.out.println("New len: " + len
                        + "  Mdat offset in orig: " + offsetMdata);

                writeMdat = true;
		writeSplitMp4(dataOut);
		dataOut.close();
		mp4file.close();		
	}
	
	public static void main(String [ ] args) {
		try {
			Mp4Test test = new Mp4Test(new File(args[0]), new File(args[1]), Integer.parseInt(args[2]));
			test.write();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
}
