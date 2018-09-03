package com.quze.videorecordlib;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.aliyun.struct.common.VideoQuality;
import com.aliyun.struct.recorder.CameraType;
import com.aliyun.struct.snap.AliyunSnapVideoParam;

import java.io.File;
import java.util.ArrayList;

import static com.aliyun.struct.snap.AliyunSnapVideoParam.RESOLUTION_720P;

/**
 * Description :
 * PackageName : com.quze.videorecordlib
 * Created by mrtrying on 2018/8/15 11:30.
 * e_mail : ztanzeyu@gmail.com
 */
public class VideoRecorderCommon {

    private volatile static VideoRecorderCommon instance = null;

    private ArrayList<Activity> arrayActivity = new ArrayList<>();

    private StartMediaActivityCallback startMediaActivityCallback;
    private StartDarftActivityCallback startDarftActivityCallback;
    private StartEditActivityCallback startEditActivityCallback;

    private VideoRecorderCommon() {
    }

    public static VideoRecorderCommon instance() {
        if (instance == null) {
            synchronized (VideoRecorderCommon.class) {
                if (instance == null) {
                    instance = new VideoRecorderCommon();
                }
            }
        }
        return instance;
    }

    public void startRecord(Context context, String outputPath, int minDuration, int maxDuration,
                            boolean isNeedDraft, boolean isNeedGallery) {
        AliyunSnapVideoParam snapVideoParam = new AliyunSnapVideoParam();
        snapVideoParam.setVideoQuality(VideoQuality.SSD);
        snapVideoParam.setResolutionMode(AliyunSnapVideoParam.RESOLUTION_720P);
        snapVideoParam.setCameraType(CameraType.BACK);
        snapVideoParam.setMinDuration(minDuration);
        snapVideoParam.setMaxDuration(maxDuration);
        VideoRecorder.startRecord(context, snapVideoParam, outputPath, isNeedDraft, isNeedGallery);
    }

    /**
     * 添加activity
     *
     * @param activity
     */
    public void addActivity(Activity activity) {
        deleteState = false;
        arrayActivity.add(activity);
    }

    private boolean deleteState = false;

    /**
     * finish
     *
     * @param activity
     */
    public void finishActivity(Activity activity) {
        if (deleteState) {
            return;
        }
        if (arrayActivity.contains(activity)) {
            arrayActivity.remove(activity);
        }
    }

    /**
     * 删除全部数据
     */
    public void deleteAllActivity() {
        deleteState = true;
        for (int i = 0; i < arrayActivity.size(); i++) {
            Activity activity = arrayActivity.get(i);
            if (activity != null) {
                activity.finish();
            }
        }
        arrayActivity.clear();
    }

    public static void deletePath(String path) {
        if (TextUtils.isEmpty(path)) {
            return;
        }
        Log.i("xianghaTag", "path:---:" + path);
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
    }

    public void setStartMediaActivityCallback(StartMediaActivityCallback startMediaActivityCallback) {
        this.startMediaActivityCallback = startMediaActivityCallback;
    }

    public void handleStartMediaActivityCallback() {
        if (startMediaActivityCallback != null) {
            startMediaActivityCallback.startMediaActivity();
        }
    }

    public void setStartDarftActivityCallback(StartDarftActivityCallback startDarftActivityCallback) {
        this.startDarftActivityCallback = startDarftActivityCallback;
    }

    public void handleStartDarftActivityCallback() {
        if (startDarftActivityCallback != null) {
            startDarftActivityCallback.startDarftActivity();
        }
    }

    public void setStartEditActivityCallback(StartEditActivityCallback startEditActivityCallback) {
        this.startEditActivityCallback = startEditActivityCallback;
    }

    public void handleStartEditActivityCallback(Bundle bundle) {
        if (startEditActivityCallback != null) {
            startEditActivityCallback.startEditActivity(bundle);
        }
    }

    public interface StartMediaActivityCallback {
        void startMediaActivity();
    }

    public interface StartDarftActivityCallback {
        void startDarftActivity();
    }

    public interface StartEditActivityCallback {
        void startEditActivity(Bundle bundle);
    }
}
