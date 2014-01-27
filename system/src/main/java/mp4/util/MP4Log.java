package mp4.util;


public class MP4Log {

	public static interface MP4Logger {
		public void log(String str);
	}
	public static MP4Logger logger;

	public static void log(String str) {
		if (logger != null)
			logger.log(str);
		else
			System.err.println(str);
	}
}
