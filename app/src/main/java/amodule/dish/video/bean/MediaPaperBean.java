package amodule.dish.video.bean;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by Administrator on 2016/10/19.
 */

public class MediaPaperBean implements Serializable {
    public float startTime=0;//开始时间
    public float endTime=0;//结束时间
    public int stepIndex;
    public String path;//原视频路径
    private float allTime;//原视频的时间
    private float cutTime;//要被截取的时间长度
    private int index;//当前所在位置
    public boolean isCut=false;//是否进行过裁剪
    private String cutPath="";//被裁剪视频后的视频路径
    private String imgPath="";
    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public float getStartTime() {
        return startTime;
    }

    public void setStartTime(float startTime) {
        isCut=true;
        this.startTime = startTime;
        setCutTime();

    }
    public void setEndTime(float endtime) {
        if(endtime>0){
            isCut=true;
            this.endTime = endtime;
            setCutTime();
        }

    }

    public float getEndTime() {
        if(endTime<=0)
            return allTime;
        return endTime;
    }

    public int getStepIndex() {
        return stepIndex;
    }

    public void setStepIndex(int stepIndex) {
        this.stepIndex = stepIndex;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public float getAllTime() {
        return allTime;
    }
    public void setAllTime(float allTime) {
        this.allTime = allTime;
        if(endTime<=0){
            endTime=allTime;
            setCutTime();
        }
    }

    public float getCutTime() {
        return cutTime;
    }

    public void setCutTime() {
        if(startTime>=0&&endTime>0){
            cutTime=(endTime*10-startTime*10)/10;
        }
    }
    public MediaPaperBean jsonToBean(JSONObject jsonObject){
        try{
            this.setPath(jsonObject.getString("path"));
            this.setAllTime(Float.parseFloat(jsonObject.getString("allTime")));
            this.setStartTime(Float.parseFloat(jsonObject.getString("startTime")));
            this.setEndTime(Float.parseFloat(jsonObject.getString("endTime")));
            this.setImgPath(jsonObject.getString("imgPath"));
            return this;
        }catch (Exception e){
        }
        return null;
    }
    public JSONObject beanToJson(){
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("path", this.getPath());
            jsonObject.put("startTime", this.getStartTime());
            jsonObject.put("endTime", this.getEndTime());
            jsonObject.put("allTime", this.getAllTime());
            jsonObject.put("cutTime", this.getCutTime());
            jsonObject.put("cutPath", this.getCutPath());
            jsonObject.put("imgPath", this.getImgPath());
            return  jsonObject;
        }catch (Exception e){

        }
        return null;
    }

    public String getCutPath() {
        return cutPath;
    }

    public void setCutPath(String cutPath) {
        this.cutPath = cutPath;
    }

    public String getImgPath() {
        return imgPath;
    }

    public void setImgPath(String imgPath) {
        this.imgPath = imgPath;
    }
}
