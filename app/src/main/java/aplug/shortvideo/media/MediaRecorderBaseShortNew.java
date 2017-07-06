package aplug.shortvideo.media;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceHolder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import acore.tools.Tools;
import aplug.recordervideo.cammer.IMediaRecorder;
import aplug.recordervideo.tools.SortComparator;
import aplug.recordervideo.tools.ToolsCammer;

/**
 * 视频处理基础处理和参数设置
 * 对视频的预览
 */
public abstract class MediaRecorderBaseShortNew implements SurfaceHolder.Callback,Camera.PreviewCallback,IMediaRecorder {

    /**
     *小视频高度
     */
    public static int SMALL_VIDEO_HEIGHT =360;
    /**
     * 小视频宽度
     */
    public static int SMALL_VIDEO_WIDTH =480;

    /** 未知错误 */
    public static final int MEDIA_ERROR_UNKNOWN = 1;
    /** 预览画布设置错误 */
    public static final int MEDIA_ERROR_CAMERA_SET_PREVIEW_DISPLAY = 101;
    /** 预览错误 */
    public static final int MEDIA_ERROR_CAMERA_PREVIEW = 102;
    /** 自动对焦错误 */
    public static final int MEDIA_ERROR_CAMERA_AUTO_FOCUS = 103;
    /** 录制错误监听 */
    protected OnErrorListener mOnErrorListener;
    /** 录制已经准备就绪的监听 */
    protected OnPreparedListener mOnPreparedListener;

    protected Activity mCon;
    /** 摄像头对象 */
    protected Camera mCamera;
    /** 摄像头参数 */
    protected Camera.Parameters mParameters = null;
    /** 画布 */
    public  SurfaceHolder mSurfaceHolder;

    /** 摄像头类型（前置/后置），默认后置 */
    protected int mCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
    /** 最大帧率 */
    public static final int MAX_FRAME_RATE = 25;
    /** 最小帧率 */
    public static final int MIN_FRAME_RATE = 15;
    /** 帧率 */
    protected int mFrameRate = MIN_FRAME_RATE;

    // 3:2
    protected int previewW,previewH;
    /** 状态标记 */
    protected boolean mPrepared, mStartPreview, mSurfaceCreated;

    protected List<Camera.Size> recorderSizes = new ArrayList<>();

    public MediaRecorderBaseShortNew(Activity con, SurfaceHolder surfaceHolder){
        mCon = con;
        mSurfaceHolder = surfaceHolder;
        mSurfaceHolder.addCallback(this);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        Log.i("zhangyujian","surfaceCreated");
        this.mSurfaceHolder = surfaceHolder;
        this.mSurfaceCreated = true;
        if (mPrepared && !mStartPreview)
            startPreview();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        this.mSurfaceHolder = surfaceHolder;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        Log.i("zhangyujian","surfaceDestroyed");
        if(mCamera != null){
            mCamera.stopPreview();
            mCamera.release();
        }
    }

    /**
     * 开始预览
     */
    public void prepare() {
        Log.i("zhangyujian","prepare");
        mPrepared = true;
        if (mSurfaceCreated)
            startPreview();
    }

    /** 开始预览 */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public void startPreview() {
        Log.i("zhangyujian","startPreview");
        if (mStartPreview || mSurfaceHolder == null)
            return;
        else
            mStartPreview = true;

        try {
            if (mCameraId == Camera.CameraInfo.CAMERA_FACING_BACK)
                mCamera = Camera.open();
            else
                mCamera = Camera.open(mCameraId);

            mCamera.setDisplayOrientation(90);
            try {
                mCamera.setPreviewDisplay(mSurfaceHolder);
            } catch (IOException e) {
                if (mOnErrorListener != null) {
                    mOnErrorListener.onVideoError(MEDIA_ERROR_CAMERA_SET_PREVIEW_DISPLAY, 0);
                }
                e.printStackTrace();
            }

            //设置摄像头参数
            mParameters = mCamera.getParameters();
            prepareCameraParaments();
            mParameters.setRecordingHint(true);
            setPreviewCallback();
            mCamera.setParameters(mParameters);
            mCamera.startPreview();

            onStartPreviewSuccess();
            if (mOnPreparedListener != null)mOnPreparedListener.onPrepared();
        } catch (Exception e) {
            e.printStackTrace();
            if (mOnErrorListener != null) {
                mOnErrorListener.onVideoError(MEDIA_ERROR_CAMERA_PREVIEW, 0);
            }
        }
    }

    /** 预览调用成功，子类可以做一些操作 */
    protected void onStartPreviewSuccess() {

    }

    /** 设置回调 */
    protected void setPreviewCallback() {
        Camera.Size size = mParameters.getPreviewSize();
        if (size != null) {
            PixelFormat pf = new PixelFormat();
            PixelFormat.getPixelFormatInfo(mParameters.getPreviewFormat(), pf);
            int buffSize = size.width * size.height * pf.bitsPerPixel / 8;
            try {
                mCamera.addCallbackBuffer(new byte[buffSize]);
                mCamera.addCallbackBuffer(new byte[buffSize]);
                mCamera.addCallbackBuffer(new byte[buffSize]);
//                mCamera.setPreviewCallbackWithBuffer(this);
            } catch (OutOfMemoryError e) {
                Log.e("zhangyujian", "startPreview...setPreviewCallback...", e);
            }
            Log.e("zhangyujian", "startPreview...setPreviewCallbackWithBuffer...width:" + size.width + " height:" + size.height);
        } else {
//            mCamera.setPreviewCallback(this);
        }
    }

    /**
     * 预处理一些拍摄参数
     * 注意：自动对焦参数cam_mode和cam-mode可能有些设备不支持，导致视频画面变形，需要判断一下，已知有"GT-N7100", "GT-I9308"会存在这个问题
     */
    @SuppressWarnings("deprecation")
    public void prepareCameraParaments() {
        Log.i("zhangyujian","prepareCameraParaments");
        try {
            List<Integer> rates = mParameters.getSupportedPreviewFrameRates();
            if (rates != null) {
                if (rates.contains(MAX_FRAME_RATE)) {
                    mFrameRate = MAX_FRAME_RATE;
                } else {
                    Collections.sort(rates);
                    for (int i = rates.size() - 1; i >= 0; i--) {
                        if (rates.get(i) <= MAX_FRAME_RATE) {
                            mFrameRate = rates.get(i);
                            break;
                        }
                    }
                }
            }

            mParameters.setPreviewFrameRate(mFrameRate);
//        mParameters.setPreviewFpsRange(5 * 1000, 20 * 1000);
            Comparator comp = new SortComparator();
            //获取系统预览支持的尺寸集合
            List<Camera.Size> supportPreviewlist = mParameters.getSupportedPreviewSizes();
            List<Camera.Size> previewlist = new ArrayList<>();
            //获取满足16：9的比例
            if (supportPreviewlist != null && supportPreviewlist.size() > 0) {
                for (Camera.Size size : supportPreviewlist) {
                    if (size.height / 3 * 4 == size.width) {
                        previewlist.add(size);
                    }
                }
                Collections.sort(previewlist, comp);
            }
            if(previewlist.size() == 0){
                Tools.showToast(mCon,"不支持拍摄视频");
                mCon.finish();
            }
            //获取支持拍摄的尺寸集合
            List<Camera.Size> list = mParameters.getSupportedVideoSizes();
            if (list != null && list.size() > 0) {
                for (Camera.Size size : list) {
                    if (size.height / 3 * 4 == size.width) {
                        recorderSizes.add(size);
                        Log.i("zhangyujian","16:9 sizeW H:" + size.width + "  " + size.height);
                    }
                }
                Collections.sort(recorderSizes, comp);
            }
            if(recorderSizes.size() == 0){
                recorderSizes.addAll(previewlist);
            }
            Camera.Size size= checkVideoSize(previewlist);
            if(size==null)
            size = previewlist.get(0);
            previewW = size.width;
            previewH = size.height;
            // 设置surfaceView分辨率
            mSurfaceHolder.setFixedSize(previewW, previewH);
            mParameters.setPreviewSize(previewW, previewH);
            Log.i("zhangyujian","previewH:" + previewH + " previewW:: " + previewW);

            // 设置输出视频流尺寸，采样率
            mParameters.setPreviewFormat(ImageFormat.NV21);

            //设置自动连续对焦
            String mode = getAutoFocusMode();
            if (!TextUtils.isEmpty(mode)) {
                mParameters.setFocusMode(mode);
            }

            //设置人像模式，用来拍摄人物相片，如证件照。数码相机会把光圈调到最大，做出浅景深的效果。而有些相机还会使用能够表现更强肤色效果的色调、对比度或柔化效果进行拍摄，以突出人像主体。
            //		if (mCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT && isSupported(mParameters.getSupportedSceneModes(), Camera.Parameters.SCENE_MODE_PORTRAIT))
            //			mParameters.setSceneMode(Camera.Parameters.SCENE_MODE_PORTRAIT);

            if (isSupported(mParameters.getSupportedWhiteBalance(), "auto"))
                mParameters.setWhiteBalance("auto");

            //是否支持视频防抖
            if ("true".equals(mParameters.get("video-stabilization-supported")))
                mParameters.set("video-stabilization", "true");

            if (!ToolsCammer.isDevice("GT-N7100", "GT-I9308", "GT-I9300")) {
                mParameters.set("cam_mode", 1);
                mParameters.set("cam-mode", 1);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private Camera.Size checkVideoSize(List<Camera.Size> previewlist){
        Camera.Size size=null;
        for(int i=0;i<previewlist.size();i++){
            int waith= previewlist.get(i).height*3/4;
            int height= previewlist.get(i).height;
            if(waith%16==0&&height%16==0){
                break;
            }
        }
        return size;
    }

    /** 停止预览 */
    public void stopPreview() {
        if (mCamera != null) {
            try {
                mCamera.stopPreview();
                mCamera.setPreviewCallback(null);
                // camera.lock();
                mCamera.release();
            } catch (Exception e) {
                Log.e("zhangyujian", "stopPreview...");
            }
            mCamera = null;
        }
        mStartPreview = false;
    }

//***********************************基础参数设置 start***************************************************************
    /** 连续自动对焦 */
    public String getAutoFocusMode() {
        if (mParameters != null) {
            //持续对焦是指当场景发生变化时，相机会主动去调节焦距来达到被拍摄的物体始终是清晰的状态。
            List<String> focusModes = mParameters.getSupportedFocusModes();
            if ((Build.MODEL.startsWith("GT-I950") || Build.MODEL.endsWith("SCH-I959") || Build.MODEL.endsWith("MEIZU MX3")) && isSupported(focusModes, "continuous-picture")) {
                return "continuous-picture";
            } else if (isSupported(focusModes, "continuous-video")) {
                return "continuous-video";
            } else if (isSupported(focusModes, "auto")) {
                return "auto";
            }
        }
        return null;
    }

    /** 检测是否支持指定特性 */
    public boolean isSupported(List<String> list, String key) {
        return list != null && list.contains(key);
    }


    /**
     * 手动对焦
     * @param focusAreas 对焦区域
     * @return
     */
    @SuppressLint("NewApi")
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public boolean manualFocus(Camera.AutoFocusCallback cb, List<Camera.Area> focusAreas) {
        if (mCamera != null && focusAreas != null && mParameters != null && ToolsCammer.hasICS()) {
            try {
                mCamera.cancelAutoFocus();
                // getMaxNumFocusAreas检测设备是否支持
                if (mParameters.getMaxNumFocusAreas() > 0) {
                    // mParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_MACRO);//
                    // Macro(close-up) focus mode
                    mParameters.setFocusAreas(focusAreas);
                }

                if (mParameters.getMaxNumMeteringAreas() > 0)
                    mParameters.setMeteringAreas(focusAreas);

                mParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_MACRO);
                mCamera.setParameters(mParameters);
                mCamera.autoFocus(cb);
                return true;
            } catch (Exception e) {
                if (mOnErrorListener != null) {
                    mOnErrorListener.onVideoError(MEDIA_ERROR_CAMERA_AUTO_FOCUS, 0);
                }
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 切换闪关灯，默认关闭
     */
    public boolean toggleFlashMode() {
        if (mParameters != null) {
            try {
                final String mode = mParameters.getFlashMode();
                if (TextUtils.isEmpty(mode) || Camera.Parameters.FLASH_MODE_OFF.equals(mode))
                    setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                else
                    setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public boolean toggleIsOpen(){
        if (mParameters != null) {
            try {
                final String mode = mParameters.getFlashMode();
                if (TextUtils.isEmpty(mode) || Camera.Parameters.FLASH_MODE_OFF.equals(mode))
                    return false;
                else
                    return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 设置闪光灯
     * @param value
     */
    private boolean setFlashMode(String value) {
        if (mParameters != null && mCamera != null) {
            try {
                if (Camera.Parameters.FLASH_MODE_TORCH.equals(value) || Camera.Parameters.FLASH_MODE_OFF.equals(value)) {
                    mParameters.setFlashMode(value);
                    mCamera.setParameters(mParameters);
                }
                return true;
            } catch (Exception e) {
                Log.e("zhangyujian", "setFlashMode", e);
            }
        }
        return false;
    }

    /** 切换前置/后置摄像头 */
    public void switchCamera() {
        if (mCameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
            switchCamera(Camera.CameraInfo.CAMERA_FACING_FRONT);
        } else {
            switchCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
        }
    }

    /** 切换前置/后置摄像头 */
    public void switchCamera(int cameraFacingFront) {
        switch (cameraFacingFront) {
            case Camera.CameraInfo.CAMERA_FACING_FRONT:
            case Camera.CameraInfo.CAMERA_FACING_BACK:
                mCameraId = cameraFacingFront;
                stopPreview();
                startPreview();
                break;
        }
    }
    //***********************************基础参数设置 end***************************************************************

    public boolean isBackCamera(){
        if (mCameraId == Camera.CameraInfo.CAMERA_FACING_BACK)
            return true;
        else
            return false;
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        camera.addCallbackBuffer(data);
    }

    /** 设置错误监听 */
    public void setOnErrorListener(OnErrorListener l) {
        mOnErrorListener = l;
    }

    /** 设置预处理监听 */
    public void setOnPreparedListener(OnPreparedListener l) {
        mOnPreparedListener = l;
    }

    /**
     * 预处理监听
     */
    public interface OnPreparedListener {
        /**
         * 预处理完毕，可以开始录制了
         */
        void onPrepared();
    }

    /**
     * 错误监听
     */
    public interface OnErrorListener {
        /**
         * 视频录制错误
         *
         * @param what
         * @param extra
         */
        void onVideoError(int what, int extra);
    }
}
