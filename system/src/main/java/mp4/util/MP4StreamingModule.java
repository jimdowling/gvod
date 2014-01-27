package mp4.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;


public class MP4StreamingModule {

//        extends BaseVideoHandlerModule implements StreamBabyModule {
	

	//public static AllowableFormats streamableFormats = new AllowableFormats(new Formats(new String[] { VideoHandlerModule.CONTAINER_MP4 }, new String[] { "*" }, new String[] { "*" }), null);
//	public static AllowableFormats streamableFormats = new AllowableFormats(
//			// allowed
//			Arrays.asList(new Format[] {
//					new Format(VideoFormats.CONTAINER_MP4, VideoFormats.VIDEO_CODEC_H264, VideoFormats.AUDIO_CODEC_AC3),
//					new Format(VideoFormats.CONTAINER_MP4, VideoFormats.VIDEO_CODEC_H264, VideoFormats.AUDIO_CODEC_AAC)
//			}),
//			// disallowed
//			null
//		);
//
//	public static String[] mp4Exts = new String[] { "mp4" };
//
//	public static ConfigEntry cfgFillVideoInfo = new ConfigEntry(
//			"mp4mod.fillvidinfo",
//			"true",
//			"Allow mp4module to parse video informaton"
//			);
//
//
//	public static ConfigEntry cfgStreamableFormats = new ConfigEntry(
//			"mp4mod.streamformats",
//			"default",
//			"list of formats mp4module should attempt to stream"
//			);
//
//	public static ConfigEntry cfgNotStreamableFormats = new ConfigEntry(
//			"mp4mod.streamformats.disallow",
//			"default",
//			"list of formats mp4module should not attempt to stream"
//			);
//
//	@Override
//	public AllowableFormats getStreamableFormats() {
//		return streamableFormats;
//	}
//
//	@Override
//	public boolean fillVideoInformation(URI uri, VideoInformation vidinfo) {
//		if (!cfgFillVideoInfo.getBool())
//			return false;
//		if (!Utils.isFile(uri))
//			return false;
//		String filename = new File(uri).getAbsolutePath();
//		int dotPos = filename.lastIndexOf('.');
//		if (dotPos < 0)
//			return false;
//		String ext = filename.substring(dotPos+1).toLowerCase();
//		if (!Arrays.asList(mp4Exts).contains(ext))
//			return false;
//		try {
//			MP4Streamer mp4 = MP4StreamFactory.getInstance(new File(uri), Long.MAX_VALUE, false);
//			if (mp4.getSubDuration() <= 0)
//				return false;
//			vidinfo.setWidth(mp4.getWidth());
//			vidinfo.setHeight(mp4.getHeight());
//			vidinfo.setContainerFormat(VideoFormats.CONTAINER_MP4);
//			vidinfo.setDuration(mp4.getSubDuration());
//			List<String> formats = mp4.getFormats();
//			if (formats.contains("avc1"))
//				vidinfo.setVideoCodec(VideoFormats.VIDEO_CODEC_H264);
//			else
//				vidinfo.setVideoCodec(VideoFormats.UNKNOWN_FORMAT);
//			if (formats.contains("mp4a"))
//				vidinfo.setAudioCodec(VideoFormats.AUDIO_CODEC_AAC);
//			else if (formats.contains("ac-3"))
//				vidinfo.setAudioCodec(VideoFormats.AUDIO_CODEC_AC3);
//
//			else
//				vidinfo.setAudioCodec(VideoFormats.UNKNOWN_FORMAT);
//
//			Log.debug(filename + ":" + vidinfo);
//			mp4.close();
//			return true;
//		} catch (IOException e) {
//			return false;
//		}
//
//	}
//
//	@Override
//	public VideoInputStream openStreamableVideo(URI uri, VideoInformation vidinfo, long startPosition)
//			throws IOException {
//		if (!Utils.isFile(uri))
//			return null;
//
//		MP4Streamer mis = MP4StreamFactory.getInstance(new File(uri), startPosition, StreamBabyConfig.cfgMp4Interleave.getBool());
//		long subDur = mis.getSubDuration();
//		if (subDur <= 0)
//			subDur = vidinfo.getDuration();
//		return new VideoInputStreamWrapper(subDur, mis, vidinfo, "video/mp4");
//	}
//	public Object getModule(int moduleType) {
//		if (moduleType == StreamBabyModule.STREAMBABY_MODULE_VIDEO)
//			return this;
//		else
//			return null;
//	}

	public static final int FILL_VIDEO_PRIORITY = 35;
	public static final int STREAM_VIDEO_PRIORITY = 60;

//	@Override
//	public boolean initialize(StreamBabyModule parentMod) {
//		super.initialize(parentMod);
//		MP4StreamFactory.initialize();
//		getPriorities().fillVideoPriority = FILL_VIDEO_PRIORITY;
//		getPriorities().streamPriority = STREAM_VIDEO_PRIORITY;
//		streamableFormats = configFormats(cfgStreamableFormats, cfgNotStreamableFormats, streamableFormats);
//		//if (!cfgStreamableFormats.getValue().equals("default") || !cfgNotStreamableFormats.getValue().equals("default"))
//			//streamableFormats = new AllowableFormats(cfgStreamableFormats.getValue(), cfgNotStreamableFormats.getValue());
//
//
//		return true;
//	}
	public boolean isProfileOk(int profile, int level) {
		return (profile <= 100 && level <= 41);
	}

//	public boolean canStream(URI uri) {
//            , VideoInformation vinfo
//        }) {
//		boolean b = super.canStream(uri, vinfo);
//		if (!b)
//			return b;
//
//
//		// Ok, so far so good.
//		Integer profile = (Integer)vinfo.getCodecExtra("mp4_profile");
//		Integer level = (Integer)vinfo.getCodecExtra("mp4_level");
//		if (level != null && profile != null) {
//			return isProfileOk(profile.intValue(), level.intValue());
//		}
//
//		try {
//			MP4Streamer mp4 = MP4StreamFactory.getInstance(new File(uri).getAbsoluteFile(), Long.MAX_VALUE, false);
//			mp4.close();
//			vinfo.setCodecExtra("mp4_profile", new Integer(mp4.getProfile()));
//			vinfo.setCodecExtra("mp4_level", new Integer(mp4.getProfileLevel()));
//			return isProfileOk(mp4.getProfile(), mp4.getProfileLevel());
//		} catch (IOException e) {
//			return false;
//		}
//	}

	
	public static void main(String[] argv) throws Exception {
		String src = argv[0];
		String dst = argv[1];
		long pos = Long.parseLong(argv[2]);
		System.err.println("In: " + src + ", out: " + dst + ", pos: " + pos);
		MP4Streamer mp4 = MP4StreamFactory.getInstance(new File(src).getAbsoluteFile(), pos,
                        true);
//                        StreamBabyConfig.cfgMp4Interleave.getBool());
		System.err.println("SubDur: " + mp4.getSubDuration()/1000.0);
		OutputStream os = new FileOutputStream(new File(dst));
		final int IO_BUFFER_SIZE = 4 * 1024;

		byte[] b = new byte[IO_BUFFER_SIZE];
		int read;
		while ((read = mp4.read(b)) != -1) {
			os.write(b, 0, read);
		}

                System.err.println("Content length: " + new File(dst).length());

		os.close();
		mp4.close();
		System.out.println("Done");
	}

//	public String getStreamableMimeType(URI uri, VideoInformation vinfo) {
//		return "video/mp4";
//	}
//
//	public String getTranscodeMimeType(URI uri, VideoInformation vinfo, int qual) {
//		return null;
//	}



}
