package com.quze.videorecordlib.util;


import java.util.List;

import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Size;
import android.util.Log;

/**
 * Description :
 * PackageName : com.quze.videorecordlib.util
 * Created by mrtrying on 2018/8/17 16:17.
 * e_mail : ztanzeyu@gmail.com
 */
public class CameraUtil {

    public static final int CAMERA_FACING_BACK = 0;
    public static final int CAMERA_FACING_FRONT = 1;
    public static final int CAMERA_NONE = 2;

    public static int HasBackCamera() {
        int numberOfCameras = Camera.getNumberOfCameras();
        CameraInfo cameraInfo = new CameraInfo();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == CAMERA_FACING_BACK) {
                return i;
            }
        }
        return 2;
    }

    public static int HasFrontCamera() {
        int numberOfCameras = Camera.getNumberOfCameras();
        CameraInfo cameraInfo = new CameraInfo();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == CAMERA_FACING_FRONT) {
                return i;
            }
        }
        return 2;
    }

    public static String getCameraPixels(int paramInt) {
        if (paramInt == 2)
            return "无";
        Camera localCamera = Camera.open(paramInt);
        Camera.Parameters localParameters = localCamera.getParameters();
        localParameters.set("camera-id", 1);
        List<Size> localList = localParameters.getSupportedPictureSizes();
        if (localList != null) {
            int heights[] = new int[localList.size()];
            int widths[] = new int[localList.size()];
            for (int i = 0; i < localList.size(); i++) {
                Size size = (Size) localList.get(i);
                int sizehieght = size.height;
                int sizewidth = size.width;
                heights[i] = sizehieght;
                widths[i] = sizewidth;
            }
            int pixels = getMaxNumber(heights) * getMaxNumber(widths);
            localCamera.release();
            return String.valueOf(pixels / 10000) + " 万";
        } else
            return "无";
    }

    public static boolean isSupportSize(int paramInt,int width,int height){
        Camera localCamera = null;
        try{
            localCamera = Camera.open(paramInt);
            Camera.Parameters localParameters = localCamera.getParameters();
            localParameters.set("camera-id", 1);
            List<Size> localList = localParameters.getSupportedPictureSizes();
            boolean isSupport = false;
            if (localList != null) {
                for (int i = 0; i < localList.size(); i++) {
                    //1280*720
                    Size size = localList.get(i);
                    Log.i("tzy", "getMaxSize: " + size.height + "*"+size.width);
                    if(size.height >= width && size.width >= height){
                        isSupport = true;
                        break;
                    }
                }
            }
            localCamera.release();
            return isSupport;
        }catch (Exception e){
            return true;
        }
    }

    public static int getMaxNumber(int[] paramArray) {
        int temp = paramArray[0];
        for (int i = 0; i < paramArray.length; i++) {
            if (temp < paramArray[i]) {
                temp = paramArray[i];
            }
        }
        return temp;
    }
}

