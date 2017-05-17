package aplug.recordervideo.cammer;

/**
 * 视频录制接口
 * 
 * @author yixia.com
 *
 */
public interface IMediaRecorder {

	/**
	 * 开始录制
	 * 
	 * @return 录制失败返回null
	 */
	public void startRecording(String filePath, MediaRecorderSystem.OnRecorderCallback callback);
	
	/**
	 * 停止录制
	 */
	public void stopRecording();
	
}
