package aplug.recordervideo.tools;

import android.content.Context;
import android.content.pm.FeatureInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.view.Display;
import android.view.WindowManager;


import java.io.File;
import java.util.List;

import acore.override.XHApplication;

/**
 * Created by Fang Ruijiao on 2016/10/11.
 */

public class ToolsCammer {

    public static final String EMPTY = "";

    /** 判断是否支持闪光灯 */
    public static boolean isSupportCameraLedFlash(PackageManager pm) {
        if (pm != null) {
            FeatureInfo[] features = pm.getSystemAvailableFeatures();
            if (features != null) {
                for (FeatureInfo f : features) {
                    if (f != null && PackageManager.FEATURE_CAMERA_FLASH.equals(f.name)) //判断设备是否支持闪光灯
                        return true;
                }
            }
        }
        return false;
    }

    /** >=4.0 14 */
    public static boolean hasICS() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;
    }

    /**
     * 检测当前设备是否是特定的设备
     *
     * @param devices
     * @return
     */
    public static boolean isDevice(String... devices) {
        String model = getDeviceModel();
        if (devices != null && model != null) {
            for (String device : devices) {
                if (model.indexOf(device) != -1) {
                    return true;
                }
            }
        }
        return false;
    }

    /** 获取屏幕宽度 */
    @SuppressWarnings("deprecation")
    public static int getScreenHeight(Context context) {
        Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        return display.getHeight();
    }

    public static Bitmap getFrameAtTime(String filePath) {
        File file = new File(filePath);
        if(!file.exists()) return null;
        Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(filePath, MediaStore.Images.Thumbnails.MINI_KIND);
        if (bitmap != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            bitmap.setConfig(Bitmap.Config.RGB_565);
        return bitmap;
    }

    /**
     * 获取指定时间的视频图片
     * @param filePath
     * @param time
     * @return
     */
    public static Bitmap getFrameAtTime(String filePath,long time) {
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(filePath);
            bitmap = retriever.getFrameAtTime(time);
        } catch (IllegalArgumentException ex) {
            // Assume this is a corrupt video file
        } catch (RuntimeException ex) {
            // Assume this is a corrupt video file.
        } finally {
            try {
                retriever.release();
            } catch (RuntimeException ex) {
                // Ignore failures while cleaning up.
            }
        }

        if (bitmap == null) return null;
        return bitmap;
    }

    public static float getLongTime(String filePath) {

        MediaPlayer mediaPlayer = MediaPlayer.create(XHApplication.in(), Uri.fromFile(new File(filePath)));
        try {
            mediaPlayer.setDataSource(filePath);
            mediaPlayer.prepare();
            int videoW = mediaPlayer.getVideoWidth();
            int videoH = mediaPlayer.getVideoHeight();
            if(videoW / 16.0 * 9 != videoH){
//                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
                return 0;
            }
            float longTime = (float) ((mediaPlayer.getDuration() * 10) / 10.0);
//            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            return longTime;
        } catch (Exception e) {
//            Log.i("FRJ","getLongYime()");
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 获得设备型号
     *
     * @return
     */
    public static String getDeviceModel() {
        return trim(Build.MODEL);
    }

    /**
     *
     * @param str
     * @return
     */
    public static String trim(String str) {
        return str == null ? EMPTY : str.trim();
    }

    /**
     * 检查手机是否支持1080拍摄
     * @param isBack ：判断是是否是后摄像头
     * @return
     */
    public static boolean checkSuporRecorder(boolean isBack){
        Camera mCamera;
        if(isBack)
            mCamera = Camera.open();
        else
            mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
        /** 摄像头参数 */
        Camera.Parameters mParameters = mCamera.getParameters();// 获取各项参数
        boolean isSupportRecevor = false;
            //获取支持拍摄的尺寸集合
            List<Camera.Size> list = mParameters.getSupportedVideoSizes();
            if (list != null && list.size() > 0) {
                for (Camera.Size size : list) {
                    if (size.height == 1080 && size.width == 1920) {
                        isSupportRecevor = true;
                        break;
                    }
                }
            }
        mCamera.release();        // release the camera for other applications
        mCamera = null;
        return isSupportRecevor;
    }
}
