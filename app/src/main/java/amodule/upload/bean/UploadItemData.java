package amodule.upload.bean;

import android.text.TextUtils;

import com.qq.e.comm.util.Md5Util;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import acore.tools.FileManager;
import acore.tools.Tools;

/**
 * Created by ：fei_teng on 2016/10/27 21:17.
 */

public class UploadItemData {


    private static final String UPLOAD_API = "main3/caipu/uploadDish";


    //单项数据上传状态
    public static final int STATE_WAITING = 0;//等待
    public static final int STATE_FAILD = 1; //失败
    public static final int STATE_SUCCESS = 2;//成功
    public static final int STATE_PAUSE = 3;//暂停
    public static final int STATE_RUNNING = 4;//正在上传

    //单项数据类型
    public static final int TYPE_IMG = 1;   //普通图片
    public static final int TYPE_BREAKPOINT_IMG = 2;//断点上传图片
    public static final int TYPE_VIDEO = 3;//视频
    public static final int TYPE_TEXT = 4; // 内容
    public static final int TYPE_LAST_TEXT = 5;// 最终数据

    //单项数据处在上传列表中的位置
    public static final int POS_HEAD = 1;
    public static final int POS_BODY = 2;
    public static final int POS_TAIL = 3;

    private String uploadUrl; //最终上传的url
    private String name;
    private int type;
    private String path;
    private String videoImage;
    private int pos;
    private int index;
    private int totleLength;
    private int state;
    private long speed;
    private int progress;
    private LinkedHashMap<String, String> uploadMsg;
    private String recMsg;
    private String makeStep;
    private String makesInfo;
    private String videoInfo;
    private String hashCode;
    private String uniqueId;

    public void setUploadUrl(String uploadUrl) {
        this.uploadUrl = uploadUrl;
    }

    public String getUploadUrl() {
        return uploadUrl;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getTotleLength() {
        return totleLength;
    }

    public void setTotleLength(int totleLength) {
        this.totleLength = totleLength;
    }

    public LinkedHashMap<String, String> getUploadMsg() {
        return uploadMsg;
    }

    public void setUploadMsg(LinkedHashMap<String, String> uploadMsg) {
        this.uploadMsg = uploadMsg;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public int getProgress() {
        return progress;
    }


    public String getUniqueId() {
        if(TextUtils.isEmpty(uniqueId)){
            uniqueId = Md5Util.encode(UPLOAD_API);
        }
        return uniqueId;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
        totleLength = (int) FileManager.getFileSize(path);
        uniqueId = Md5Util.encode(path + UPLOAD_API + System.currentTimeMillis());
    }

    public void setVideoImage(String videoImage) {
        this.videoImage = videoImage;
    }

    public String getVideoImage() {
        return videoImage;
    }

    public String getRecMsg() {
        return recMsg;
    }

    public void setRecMsg(String recMsg) {
        this.recMsg = recMsg;
    }

    public void setMakeStep(String makeStep) {
        this.makeStep = makeStep;
    }

    public String getMakeStep() {
        return makeStep;
    }

    public String getVideoInfo() {
        return videoInfo;
    }

    public void setVideoInfo(String videoInfo) {
        this.videoInfo = videoInfo;
    }

    public String getMakesInfo() {
        return makesInfo;
    }

    public void setMakesInfo(String makesInfo) {
        this.makesInfo = makesInfo;
    }

    public long getSpeed() {
        return speed;
    }

    public void setSpeed(long speed) {
        this.speed = speed;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }


    public Map<String, String> translateToMap() {

        HashMap<String, String> map = new HashMap<String, String>();

        map.put("name", name);
        map.put("type", type + "");
        map.put("path", path);
        map.put("videoImage", videoImage);
        map.put("pos", pos + "");
        map.put("index", index + "");
        map.put("speed", speed + "");
        map.put("progress", progress + "");
        map.put("state", state + "");
        map.put("stateInfo", getStatInfo());
        map.put("totleLength", totleLength == 0 ? "10KB" : Tools.getPrintSize(totleLength));
        map.put("uploadMsg", uploadMsg + "");
        map.put("recMsg", recMsg);
        map.put("makeStep", getStep());
        map.put("makesInfo", makesInfo);
        map.put("videoInfo", videoInfo);

        return map;
    }

    private String getStatInfo() {

        String stateInfo = "";
        switch (state) {
            case UploadItemData.STATE_FAILD:
                stateInfo = "上传失败，点击";
                break;
            case UploadItemData.STATE_SUCCESS:
                stateInfo = "成功";
                break;
            case UploadItemData.STATE_PAUSE:
                stateInfo = "已暂停";
                break;
            case UploadItemData.STATE_RUNNING:
                stateInfo = Tools.getPrintSize(speed) + "/s";
                break;
            case UploadItemData.STATE_WAITING:
                stateInfo = "等待上传";
                break;

        }
        return stateInfo;
    }


    private String getStep() {

        String stepStr;
        if (!TextUtils.isEmpty(makeStep)) {
            if (makeStep.trim().length() == 1)
                stepStr = "第" + makeStep.trim() + "步";
            else
                stepStr = makeStep;
        } else
            stepStr = "未知";

        return stepStr;
    }

    public String getHashCode() {
        return hashCode;
    }

    public void setHashCode(String hashCode) {
        this.hashCode = hashCode;
    }
}
