package mp4.util;

import java.io.FilterInputStream;
import java.io.InputStream;
import java.util.List;

public abstract class MP4Streamer extends FilterInputStream{

	protected MP4Streamer(InputStream in) {
		super(in);
		// TODO Auto-generated constructor stub
	}

	public abstract long  getSubDuration();

	public abstract List<String> getFormats();
	public abstract int getWidth();
	public abstract int getHeight();

	public abstract int getProfile();
	public abstract  int getProfileLevel();

}
