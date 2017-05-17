package aplug.recordervideo.cammer;

import android.app.Activity;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.util.Log;
import android.view.SurfaceHolder;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import aplug.recordervideo.tools.FileToolsCammer;

/**
 * Created by Fang Ruijiao on 2016/10/12.
 */

public class MediaRecorderSystem extends MediaRecorderBase implements MediaRecorder.OnErrorListener,MediaRecorder.OnInfoListener {

    private MediaRecorder mMediaRecorder;
    private OnRecorderCallback mCallback;
    private boolean isOk = false;
    private int recorderW = 1920,recorderH = 1080;

    public MediaRecorderSystem(Activity con, SurfaceHolder surfaceHolder) {
        super(con, surfaceHolder);
    }

    @Override
    public void startRecording(String filePath,OnRecorderCallback callback){
        mCallback = callback;
        startRecoding(filePath);
    }

    public void takePicture(){
        try{
            mCamera.takePicture(null, null, new Camera.PictureCallback() {
                @Override
                public void onPictureTaken(byte[] data, Camera camera) {
                    if(data != null && data.length > 100) {
                        try {
                            FileToolsCammer.saveToSDCard(mCon, data);
                            Toast.makeText(mCon, "照片已保存", Toast.LENGTH_SHORT).show();
                            camera.startPreview();
                        } catch (Exception e) {
                            Toast.makeText(mCon, "拍照失败，请使用系统拍照功能", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    }else{
                        Toast.makeText(mCon,"拍照失败，请使用系统拍照功能",Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }catch (Exception e){
            Toast.makeText(mCon,"拍照失败，请使用系统拍照功能",Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void startRecoding(String filePath){
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
            //设置视频输出的格式
            CamcorderProfile mProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
            //设置视频编码比特率，多于2W的，直接设置成2W
            if (mProfile.videoBitRate > 20 * 1000 * 1000)
                mMediaRecorder.setVideoEncodingBitRate(20 * 1000 * 1000); //最终出来的视频比特率是2W多一点
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

            mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);// 视频编码


//            mMediaRecorder.setMaxDuration(maxDurationInMs);

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

    /**
     * 停止拍摄，则：
     */
    @Override
    public void stopRecording(){
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

    @Override
    public void onInfo(MediaRecorder mr, int what, int extra) {

    }

    public interface OnRecorderCallback{
        public void onStarSucess();
        public void onStarFail();
    }

}
