package mp4.util;


import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;



public class MP4StreamFactory {
	public static class VirtMemBackedBArray
//                extends StreamableMP4.BaseBArray
//                implements  StreamableMP4.BArray
        {

//		MemChunk memChunk;
		public VirtMemBackedBArray(int size) {
//			super(size);
			//memChunk = MappedFileMemoryManager.manager.alloc(size);
//			memChunk = MappedFileMemoryManager.manager.alloc(size);
		}
//		public void free() {
//			if (memChunk != null)
//				memChunk.free();
//			memChunk = null;
//		}
//
//		public int get(long index) {
//			int ret = memChunk.get((int)index) & 0xff;
//			//System.err.println("get: " + index + ", " + ret);
//			return ret;
//		}
//
//		public void set(long index, int v) {
//			v = v & 0xff;
//			memChunk.set((int)index, v);
//		}
		public void readFromFile(RandomAccessFile fp, long offset, long len)
				throws IOException {
//			final int BSIZE = 4096;
//			byte[] bytes = new byte[BSIZE];
//			while(len > 0) {
//				int rl = Math.min((int)len, BSIZE);
//				fp.read(bytes, 0, rl);//
//				memChunk.write((int)offset, bytes, 0, rl);
//				offset += rl;
//				len -= rl;
//			}
			
		}
		
	}
//	public static class VirtMemBArrayFactory implements BArrayFactory {
//		public static int MAX_BYTE_BACKED_SIZE = 100 * 1024; // 100k
//		public BArray getBArray(int size) {
//			if (size <= MAX_BYTE_BACKED_SIZE)
//				return new StreamableMP4.ByteArrayBackedBArray(size);
//			else
//				return new VirtMemBackedBArray(size);
//		}
//
//	}

//	public static boolean initialize() {
//		StreamableMP4.logger = new StreamableMP4.Logger() {
//			public void log(String s) {
//				Log.debug(s);
//			}
//		};
//
//		StreamableMP4.bfact = new VirtMemBArrayFactory();
//		MP4Log.logger = new MP4Log.MP4Logger() {
//
//			public void log(String str) {
//				Log.debug(str);
//			}
//
//		};
//
//		return true;
//	}

	public static MP4Streamer getInstance(File file, long startPos, boolean reinterleave) throws IOException {
			return new JavaMP4Splitter(file, startPos, reinterleave);
	}
}
