package third.aliyun.work;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.content.PermissionChecker;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.aliyun.struct.common.CropKey;
import com.aliyun.struct.common.ScaleMode;
import com.aliyun.struct.common.VideoQuality;
import com.aliyun.struct.encoder.VideoCodecs;
import com.aliyun.struct.recorder.CameraType;
import com.aliyun.struct.recorder.FlashType;
import com.aliyun.struct.snap.AliyunSnapVideoParam;
import com.quze.videorecordlib.VideoRecorderCommon;
import com.xh.manager.DialogManager;
import com.xh.manager.ViewManager;
import com.xh.view.HButtonView;
import com.xh.view.MessageView;
import com.xh.view.TitleView;

import java.io.File;
import java.util.ArrayList;

import acore.logic.ConfigMannager;
import acore.logic.LoginManager;
import acore.override.XHApplication;
import acore.override.activity.base.BaseLoginActivity;
import acore.tools.OsSystemSetting;
import acore.tools.StringManager;
import acore.tools.Tools;
import amodule.user.activity.login.LoginByAccout;
import third.aliyun.edit.util.Common;

/**
 *
 */
public class AliyunCommon {
    public static volatile AliyunCommon aliyunCommon =null;
    public static AliyunVideoDataCallBack aliyunVideoDataCallBack;
    public static String corpPath = "";//裁剪视频
    public static String videoPath = "";//合成后的视频
    public static String imgPath = "";//合成图片

    public boolean locationPermissionState;
    public boolean camerPermissionState;

    public static AliyunCommon getInstance(){
        if(aliyunCommon==null){
            synchronized (AliyunCommon.class){
                if(aliyunCommon==null){
                    aliyunCommon= new AliyunCommon();
                }
            }
        }
        return aliyunCommon;
    }
    /**
     * 设置接口回调----对外暴露
     * @param callBack
     */
    public void setAliyunVideoDataCallBack(AliyunVideoDataCallBack callBack){
        aliyunVideoDataCallBack=callBack;
    }

    /**
     * 开启阿里云视频合成----对外暴露
     * @param context
     */
    public void startAliyunVideo(Context context){
        corpPath="";
        videoPath="";
        imgPath="";
        deleteState=false;
        deleteAllActivity();
        Intent intent = new Intent(context,MediaActivity.class);
        intent.putExtra(CropKey.VIDEO_RATIO,0);
        intent.putExtra(CropKey.VIDEO_SCALE,CropKey.SCALE_CROP);
        intent.putExtra(CropKey.VIDEO_QUALITY, VideoQuality.HD);
        intent.putExtra(CropKey.VIDEO_FRAMERATE,25);
        intent.putExtra(CropKey.VIDEO_GOP,125);
        intent.putExtra(CropKey.VIDEO_BITRATE,0);
        context.startActivity(intent);
    }

    public void startRecord(Context context) {
        if (context == null) {
            return;
        }
        Common.requestMusic();
        String packName = context.getClass().getSimpleName();
        if (!LoginManager.isLogin()) {
            context.startActivity(new Intent(context, LoginByAccout.class));
            return;
        }else if(!LoginManager.isBindMobilePhone()){
            BaseLoginActivity.gotoBindPhoneNum(context);
            return;
        }

//        if (PublishManager.getInstance().isPublishing()) {
//            Toast.makeText(context, R.string.publish_ing_hint, Toast.LENGTH_SHORT).show();
//            return;
//        }
        String[] PERMISSIONS = {Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.RECORD_AUDIO};
        for (String permission : PERMISSIONS) {
            if (permission.equals(Manifest.permission.CAMERA)) {
                camerPermissionState = PermissionChecker.checkPermission(XHApplication.in(), permission, android.os.Process.myPid(), android.os.Process.myUid(), XHApplication.in().getPackageName()) == PackageManager.PERMISSION_GRANTED;
            } else {
                locationPermissionState = PermissionChecker.checkPermission(XHApplication.in(), permission, android.os.Process.myPid(), android.os.Process.myUid(), XHApplication.in().getPackageName()) == PackageManager.PERMISSION_GRANTED;
            }
        }
        if (!camerPermissionState) {
            Tools.showToast(context, "请给予相应的权限");
            showCameraPermissionsDialog(context);
            return;
        }
//        if (!locationPermissionState) {
//            Tools.showToast(context, "请给予相应的权限");
//            showLocationPermissionsDialog(context);
//            return;
//        }
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/com.xiangha/long/aliyun/";
        int recordTime = 30;
        int recordMinTime = 3;

        if(getRecordTime("recordTime")>0){
            recordTime=getRecordTime("recordTime");
        }
        if(getRecordTime("recordMinTime")>0){
            recordMinTime=getRecordTime("recordMinTime");
        }

        VideoRecorderCommon.instance().startRecord(context, path, recordMinTime*1000, recordTime*1000, true, true);
    }

    public void showCameraPermissionsDialog(Context context) {
        DialogManager dialogManager = new DialogManager(context);
        dialogManager.createDialog(new ViewManager(dialogManager)
                .setView(new TitleView(context).setText("开启访问相机权限"))
                .setView(new MessageView(context).setText("该权限需要您手动设置，请跳转到设置页面进行操作"))
                .setView(new HButtonView(context).setNegativeText("取消", v -> dialogManager.cancel())
                        .setPositiveText("确定", v -> {
                            OsSystemSetting.openPermissionSettings();
                            dialogManager.cancel();
                        })
                ))
                .show();
    }
    /**
     * 关闭页面----对外暴露
     */
    public void closeAliyunActivity(){
        deleteAllActivity();
        deleteCropVideo();
    }
    /**
     * 结束阿里云video----对外暴露
     * @param state true--上传成功，false---上传失败
     */
    public void endAliyunVideo(boolean state){
        if(state){//删除全部文件
            deletePath(corpPath);
            deletePath(videoPath);
            deletePath(imgPath);
        }else{//不删除如何文件
            deletePath(corpPath);
        }
    }

    /**
     * 删除合成后视频和图片
     */
    public void deleteComPoundVideoAndImg(){
        if(arrayActivity!=null&&arrayActivity.size()>0) {
            deletePath(videoPath);
            deletePath(imgPath);
        }
    }
    /**
     * 删除裁剪视频
     */
    public void deleteCropVideo(){
        deletePath(corpPath);
    }
    public interface AliyunVideoDataCallBack{
        public void videoCallBack(String videoPath, String imgPath, String otherData);
    }
    public static void deletePath(String path){
        if(TextUtils.isEmpty(path)){
            return;
        }
        Log.i("xianghaTag","path:---:"+path);
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
    }
    private ArrayList<Activity> arrayActivity = new ArrayList<>();

    /**
     * 添加activity
     * @param activity
     */
    public void addActivity(Activity activity){
        deleteState=false;
        arrayActivity.add(activity);
    }
    private boolean deleteState=false;
    /**
     * finish
     * @param activity
     */
    public void finishActivity(Activity activity){
        if(deleteState){
            return;
        }
        if(arrayActivity.contains( activity)){
            arrayActivity.remove(activity);
            activity=null;
        }
    }

    /**
     * 删除全部数据
     */
    public void deleteAllActivity(){
        deleteState=true;
        for(int i= 0;i<arrayActivity.size();i++){
            Activity activity = arrayActivity.get(i);
            if(activity!=null){
                activity.finish();
            }
        }
        arrayActivity.clear();
    }
    public void startRecoderVideo(Context context){
        AliyunSnapVideoParam recordParam = new AliyunSnapVideoParam.Builder()
                .setResolutionMode(AliyunSnapVideoParam.RESOLUTION_720P)
                .setRatioMode(AliyunSnapVideoParam.RATIO_MODE_9_16)
                .setRecordMode(AliyunSnapVideoParam.RECORD_MODE_AUTO)
                .setFilterList(new String[]{})
                .setBeautyLevel(80)
                .setBeautyStatus(true)
                .setCameraType(CameraType.FRONT)
                .setFlashType(FlashType.ON)
                .setNeedClip(true)
                .setMaxDuration(30000)
                .setMinDuration(1000)
                .setVideoQuality(VideoQuality.HD)
                .setGop(5)
                .setVideoBitrate(0)
                .setVideoCodec(VideoCodecs.H264_HARDWARE)
                /**
                 * 裁剪参数
                 */
                .setMinVideoDuration(4000)
                .setMaxVideoDuration(29 * 1000)
                .setMinCropDuration(3000)
                .setFrameRate(25)
                .setCropMode(ScaleMode.PS)
                .build();
        AliyunVideoRecorder.startRecord(context,recordParam);
    }
    public static int getRecordTime(String key){
        int recordTime = 0;
        if(!TextUtils.isEmpty(LoginManager.userInfo.get(key))){
            recordTime= Integer.parseInt(LoginManager.userInfo.get(key));
        }
        if(recordTime<=0){
            String videoRecord=ConfigMannager.getConfigByLocal("videoRecord");
            if(!TextUtils.isEmpty(videoRecord)){
                String tempKey= StringManager.getFirstMap(videoRecord).get(key);
                if(!TextUtils.isEmpty(tempKey)){
                    recordTime= Integer.parseInt(tempKey);
                }
            }
        }
        return recordTime;

    }
}
