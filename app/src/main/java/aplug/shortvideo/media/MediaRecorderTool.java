package aplug.shortvideo.media;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.os.Build;

/**
 * Created by Administrator on 2016/12/26.
 */

public class MediaRecorderTool {

    /** 是否支持前置摄像头 */
    @SuppressLint("NewApi")
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public static boolean isSupportFrontCamera() {
        if (!DeviceUtils.hasGingerbread()) {
            return false;
        }
        int numberOfCameras = android.hardware.Camera.getNumberOfCameras();
        if (2 == numberOfCameras) {
            return true;
        }
        return false;
    }
}
