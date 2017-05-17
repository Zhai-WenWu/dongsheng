package aplug.recordervideo.db;

public class RecorderVideoData {
	// --数据库字段名;
	public static String video_id = "id";
	public static String video_add_time = "addTime"; //视频拍摄时间
	public static String video_long_time = "longTime"; //视频时长
	public static String video_show_time = "showTime"; //显示的视频时长
	public static String video_path = "videoPath"; //视频本地路径
	public static String video_img_path = "videoImgPath"; //视频第一帧图片本地路径

	public static String video_time = "time"; //视频本地路径
	public static String video_state = "videoState"; //视频本地路径
	public static String video_isDelete = "videoIsDelete"; //视频本地路径



	private int videoId;
	private long videoAddTime;
	private float videoLongTime;
	private String videoShowTime;
	private String videoPath = "";
	private String videoImgPath = "";

	public void setVideoId(int videoId) {
		this.videoId = videoId;
	}


	public void setVideoPath(String videoPath) {
		this.videoPath = videoPath;
	}

	public void setVideoAddTime(long videoAddTime) {
		this.videoAddTime = videoAddTime;
	}

	public void setVideoLongTime(float videoLongTime) {
		this.videoLongTime = videoLongTime;
	}

	public void setVideoShowTime(String videoShowTime) {
		this.videoShowTime = videoShowTime;
	}

	public void setVideoImgPath(String videoImgPath) {
		this.videoImgPath = videoImgPath;
	}

	public int getVideoId() {
		return videoId;
	}

	public String getVideoPath() {
		return videoPath;
	}

	public long getVideoAddTime() {
		return videoAddTime;
	}

	public float getVideoLongTime() {
		return videoLongTime;
	}

	public String getVideoShowTime() {
		return videoShowTime;
	}

	public String getVideoImgPath() {
		return videoImgPath;
	}
}
