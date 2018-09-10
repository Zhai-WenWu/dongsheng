package amodule.shortvideo.tools;

import android.text.TextUtils;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.Map;

import acore.tools.StringManager;

/**
 * 短视频上传数据bean
 */
public class ShortVideoPublishBean implements Serializable{
    private String id;//数据库中的id
    private String name;//标题描述
    private String imageUrl;//图片
    private String imagePath;//图片地址
    private String imageSize;//图片宽高
    private String videoUrl;//视频url
    private String videoPath;//视频地址
    private String videoSize;//视频宽高
    private String videoTime;//视频的时长秒数
    private String musicCode;//音乐code
    private String topicCode;//话题code
    private String address;//地址信息

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getImageSize() {
        return imageSize;
    }

    public void setImageSize(String imageSize) {
        this.imageSize = imageSize;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getVideoPath() {
        return videoPath;
    }

    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
    }

    public String getVideoSize() {
        return videoSize;
    }

    public void setVideoSize(String videoSize) {
        this.videoSize = videoSize;
    }

    public String getVideoTime() {
        return videoTime;
    }

    public void setVideoTime(String videoTime) {
        this.videoTime = videoTime;
    }

    public String getMusicCode() {
        return musicCode;
    }

    public void setMusicCode(String musicCode) {
        this.musicCode = musicCode;
    }

    public String getTopicCode() {
        return topicCode;
    }

    public void setTopicCode(String topicCode) {
        this.topicCode = topicCode;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * 本地数据判断
     * @return
     */
    public boolean isLocalDataEmpty(){
        return TextUtils.isEmpty(name)||TextUtils.isEmpty(imagePath)||TextUtils.isEmpty(videoPath);
    }

    /**
     * 网络数据判断
     * @return
     */
    public boolean isDataEmpty(){
        return TextUtils.isEmpty(name)||TextUtils.isEmpty(imageUrl)||TextUtils.isEmpty(imageUrl);
    }

    /**
     * json字符串
     * @return
     */
    public String toJsonString(){
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id",id);
            jsonObject.put("name",name);
            jsonObject.put("imageUrl",imageUrl);
            jsonObject.put("imagePath",imagePath);
            jsonObject.put("imageSize",imageSize);
            jsonObject.put("videoUrl",videoUrl);
            jsonObject.put("videoPath",videoPath);
            jsonObject.put("videoSize",videoSize);
            jsonObject.put("videoTime",videoTime);
            jsonObject.put("musicCode",musicCode);
            jsonObject.put("topicCode",topicCode);
            jsonObject.put("address",address);
            return jsonObject.toString();
        }catch (Exception e){
            e.printStackTrace();
        }
        return "";
    }
    public void jsonToBean(String json){
        Map<String,String> map = StringManager.getFirstMap(json);
        setName(getMapKeyData(map,"id"));
        setName(getMapKeyData(map,"name"));
        setImageUrl(getMapKeyData(map,"imageUrl"));
        setImagePath(getMapKeyData(map,"imagePath"));
        setImageSize(getMapKeyData(map,"imageSize"));
        setVideoUrl(getMapKeyData(map,"videoUrl"));
        setVideoPath(getMapKeyData(map,"videoPath"));
        setVideoSize(getMapKeyData(map,"videoSize"));
        setVideoTime(getMapKeyData(map,"videoTime"));
        setMusicCode(getMapKeyData(map,"musicCode"));
        setTopicCode(getMapKeyData(map,"topicCode"));
    }
    private String getMapKeyData(Map<String,String> map ,String key){
        if(map.containsKey(key)){
            return map.get(key);
        }
        return "";
    }
}
