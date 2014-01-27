package se.sics.ipasdistances;

import java.io.FileOutputStream;
import java.io.IOException;

class BinaryWriter {

	private FileOutputStream output;

	public BinaryWriter(String fileName) throws IOException {
		output = new FileOutputStream(fileName);
	}

	public void write(int size) throws IOException {
		byte[] b = new byte[4];
		for (int i = 3; i >= 0; i--) {
			int asByte = (size >>> (8 * i)) & 0xFF;
			b[3-i] = (byte) asByte;
		}
		write(b);
	}

	public void close() throws IOException {
		output.close();
	}

	public void write(byte[] array) throws IOException {
		output.write(array);
	}

	public void write(long ts) throws IOException {
		int ts1 = (int) ((ts >>> 32) & 0xFFFFFFFF);
		int ts2 = (int) (ts & 0xFFFFFFFF);
		write(ts1);
		write(ts2);
	}
}
