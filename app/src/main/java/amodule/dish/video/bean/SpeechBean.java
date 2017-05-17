package amodule.dish.video.bean;

import java.io.Serializable;

/**
 * Created by Administrator on 2016/11/30.
 */

public class SpeechBean implements Serializable{
    private String text;//当前的文字
    private String path;//合成出当前音频的文件路径
    private float vCutTime;//当前视频的截取时间
    private float aTime;//当前音频时间
    private boolean success=false;//当前是否合成成功
    private String pathAac;
    private int dif_time=0;//时间的前后差值（1000为1秒）
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public float getvCutTime() {
        return vCutTime;
    }

    public void setvCutTime(float vCutTime) {
        this.vCutTime = vCutTime;
    }

    public float getaTime() {
        return aTime;
    }

    public void setaTime(float aTime) {
        this.aTime = aTime;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getPathAac() {
        return pathAac;
    }

    public void setPathAac(String pathAac) {
        this.pathAac = pathAac;
    }

    public int getDif_time() {
        return dif_time;
    }

    public void setDif_time(int dif_time) {
        this.dif_time = dif_time;
    }
}
