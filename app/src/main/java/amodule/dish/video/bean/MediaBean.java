package amodule.dish.video.bean;

import android.text.TextUtils;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * 视频bean
 */

public class MediaBean implements Serializable{

    public float startTime=0;//开始时间
    public float endTime;//结束时间
    public int id;//唯一标识
    public String path;//原视频路径
    private float allTime;//原视频的时间
    private float cutTime;//要被截取的时间长度
    private String cutPath;//截取文件路径

    private String indexStartTime;//当前在合成视频的开始时间
    private String indexEndTime;////当前在合成视频的结束时间
    private String videoUrl;//视频上传成功后，获取到的url

    public String getCutPath() {
        return cutPath;
    }

    public void setCutPath(String cutPath) {
        this.cutPath = cutPath;
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

    public float getStartTime() {
        return startTime;
    }

    public void setStartTime(float startTime) {
        this.startTime = startTime;
        setCutTime();

    }
    public void setEndTime(float endtime) {
        this.endTime = endtime;
        setCutTime();
    }

    public float getEndTime() {
        if(endTime<=0)
            return allTime;
        return endTime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    private void setCutTime(){
        if(startTime>=0&&endTime>0){
            cutTime=(endTime*10-startTime*10)/10;
        }
    }

    public float getCutTime() {
        return cutTime;
    }

    /**
     * 当前bean转成json输出
     */
    public JSONObject getJson(){
        try{
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("startTime",this.getStartTime());
            jsonObject.put("endTime",this.getEndTime());
            jsonObject.put("path",this.getPath());
            jsonObject.put("allTime",this.getAllTime());
            jsonObject.put("cutPath",this.getCutPath());
            return jsonObject;
        }catch (Exception e){

        }
        return null;

    }

    /**
     * 将json成bean
     * @param jsonObject
     */
    public void setJson(JSONObject jsonObject){
        if(jsonObject!=null){
            MediaBean mediaBean= new MediaBean();
            try {
                mediaBean.setAllTime(Float.parseFloat(TextUtils.isEmpty(jsonObject.get("allTime").toString()) ? "":jsonObject.get("allTime").toString()));
                mediaBean.setStartTime(Float.parseFloat(TextUtils.isEmpty(jsonObject.get("startTime").toString()) ? "":jsonObject.get("startTime").toString()));
                mediaBean.setEndTime(Float.parseFloat(TextUtils.isEmpty(jsonObject.get("endTime").toString()) ? "":jsonObject.get("endTime").toString()));
                mediaBean.setPath(TextUtils.isEmpty(jsonObject.get("path").toString()) ? "":jsonObject.get("path").toString());
                mediaBean.setCutPath(TextUtils.isEmpty(jsonObject.get("cutPath").toString()) ? "":jsonObject.get("cutPath").toString());
                mediaBean.setAllTime(Float.parseFloat(TextUtils.isEmpty(jsonObject.get("allTime").toString()) ? "":jsonObject.get("allTime").toString()));
            }catch (Exception e){

            }
        }
    }
}
