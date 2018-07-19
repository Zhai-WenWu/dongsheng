package third.aliyun.work;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.aliyun.struct.common.CropKey;
import com.aliyun.struct.common.ScaleMode;
import com.aliyun.struct.common.VideoQuality;
import com.aliyun.struct.encoder.VideoCodecs;
import com.aliyun.struct.recorder.CameraType;
import com.aliyun.struct.recorder.FlashType;
import com.aliyun.struct.snap.AliyunSnapVideoParam;

import java.io.File;
import java.util.ArrayList;

/**
 *
 */
public class AliyunCommon {
    public static volatile AliyunCommon aliyunCommon =null;
    public static AliyunVideoDataCallBack aliyunVideoDataCallBack;
    public static String corpPath = "";//裁剪视频
    public static String videoPath = "";//合成后的视频
    public static String imgPath = "";//合成图片

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
}
