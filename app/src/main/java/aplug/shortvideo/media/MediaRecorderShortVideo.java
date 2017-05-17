package aplug.shortvideo.media;

import android.app.Activity;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.util.Log;
import android.view.SurfaceHolder;

import java.io.File;
import java.io.IOException;

import aplug.recordervideo.cammer.MediaRecorderSystem;

import static android.media.CamcorderProfile.get;

/**
 * 小视频的参数view
 * 主要处理是视频的录制
 */
public class MediaRecorderShortVideo extends MediaRecorderBaseShortNew implements android.media.MediaRecorder.OnErrorListener,MediaRecorder.OnInfoListener{

    private MediaRecorder mMediaRecorder;
    private boolean isOk = false;
    private MediaRecorderSystem.OnRecorderCallback mCallback;
    private int recorderW = 1280,recorderH = 960;
    private AudioRecordersShortVideo audioRecordersShortVideo;
    private String audioPath;
    private Activity activity;
    public MediaRecorderShortVideo(Activity con, SurfaceHolder surfaceHolder) {
        super(con, surfaceHolder);
        activity=con;
        audioRecordersShortVideo= new AudioRecordersShortVideo(activity);
    }
    @Override
    public void startRecording(String filePath, MediaRecorderSystem.OnRecorderCallback callback) {
        mCallback = callback;
        startRecoding(filePath);
    }

    /**
     * 设置音频的数据
     */
    public void setAudioPath(String path){
        this.audioPath= path;
        audioRecordersShortVideo.setPath(audioPath);
    }

    @Override
    public void prepare() {
        super.prepare();
        audioRecordersShortVideo.creatAudioRecord();
    }

    @Override
    public void stopRecording() {
        if (mMediaRecorder != null) {
            //设置后不会崩
            mMediaRecorder.setOnErrorListener(null);
            mMediaRecorder.setPreviewDisplay(null);
            try {
                mMediaRecorder.stop();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (RuntimeException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(audioRecordersShortVideo!=null){
            audioRecordersShortVideo.stopRecord();
        }
        if (mCamera != null) {
            try {
                mCamera.lock();
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onError(MediaRecorder mr, int what, int extra) {
        try {
            if (mr != null)
                mr.reset();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (mOnErrorListener != null)
            mOnErrorListener.onVideoError(what, extra);
    }

    @Override
    public void onInfo(MediaRecorder mr, int what, int extra) {
    }

    private void startRecoding(String filePath){
        audioRecordersShortVideo.startRecord();
        try {
            mCamera.unlock();
            if (mMediaRecorder == null) {
                mMediaRecorder = new MediaRecorder();
            } else {
                mMediaRecorder.reset();
            }

            mMediaRecorder.setCamera(mCamera);
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT); // 视频源
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4); // 输出格式为mp4

            mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);// 视频编码
            //设置视频输出的格式
            CamcorderProfile mProfile = get(CamcorderProfile.QUALITY_HIGH);
            //设置视频编码比特率，多于2W的，直接设置成2W
            if (mProfile.videoBitRate >2 * 10000 * 100)
                mMediaRecorder.setVideoEncodingBitRate(2 * 10000 * 100); //最终出来的视频比特率是2W多一点
            else
                mMediaRecorder.setVideoEncodingBitRate(mProfile.videoBitRate);
//            Log.i("FRJ","mProfile.videoBitRate:" + mProfile.videoBitRate);

            //设置录制的视频帧率,设置最高25帧/s   -----部分手机不支持
//            if(mProfile.videoFrameRate >= 25)
//                mMediaRecorder.setVideoFrameRate(25);
//            else
            mMediaRecorder.setVideoFrameRate(mProfile.videoFrameRate);

            if(recorderW == 0) {
                Camera.Size size = recorderSizes.get(0);
                recorderW = size.width;
                recorderH = size.height;
                for (Camera.Size recorderSize : recorderSizes) {
                    if (recorderSize.width == previewW) {
                        recorderW = recorderSize.width;
                        recorderH = recorderSize.height;
                        break;
                    }
                }
                Log.i("FRJ", "recorderW:" + recorderW);
                Log.i("FRJ", "recorderH:" + recorderH);
            }
            mMediaRecorder.setVideoSize(recorderW, recorderH);// 视频尺寸

            File tempFile = new File(filePath);
            if(!tempFile.getParentFile().exists()){
                tempFile.getParentFile().mkdirs();
            }
//            Log.i(TAG,"filePath:" + filePath);
//            Log.i(TAG,"save Path:" + tempFile.getPath());

            mMediaRecorder.setOutputFile(filePath);
            mMediaRecorder.setPreviewDisplay(mSurfaceHolder.getSurface()); // 预览

            mMediaRecorder.setOnInfoListener(this);
            mMediaRecorder.setOnErrorListener(this);
//
//            mMediaRecorder.setMaxFileSize(maxFileSizeInBytes);
            mMediaRecorder.prepare();
            mMediaRecorder.start();
            isOk = true;
            mCallback.onStarSucess();
        } catch (IllegalStateException e) {
            starError();
            e.printStackTrace();
        } catch (IOException e) {
            starError();
            e.printStackTrace();
        }catch (Exception e){
            starError();
            e.printStackTrace();
        }
    }

    private void starError(){
        releaseMediaRecorder();
        mCallback.onStarFail();
    }
    public void releaseMediaRecorder(){
        if (mMediaRecorder != null) {
            mMediaRecorder.reset();   // clear recorder configuration
            mMediaRecorder.release(); // release the recorder object
            mMediaRecorder = null;
            mCamera.lock();           // lock camera for later use
        }

    }
    public void releaseCamera(){
        if (mCamera != null){
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }
    /** 是否前置摄像头 */
    public boolean isFrontCamera() {
        return mCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT;
    }

    /**
     * 设置宽高
     * @param waith
     * @param height
     */
    public void setWaithAndHeight(int waith,int height){
        this.recorderW= waith;
        this.recorderH= height;
    }
}
