package amodule.shortvideo.tools;

import android.text.TextUtils;

import java.io.Serializable;

/**
 * 短视频上传数据bean
 */
public class ShortVideoPublishBean implements Serializable{
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
}