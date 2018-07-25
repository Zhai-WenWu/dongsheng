package amodule.topic.model;

import android.text.TextUtils;

import java.util.Map;

/**
 * 视频数据
 */
public class VideoModel {
    private boolean mIsAutoPlay;//是否自动播放
    private String mVideoTime;//视频时长
    private String mVideoW;//视频宽
    private String mVideoH;//视频高
    private Map<String, String> mVideoUrlMap;
    private String mVideoImg;//视频封面图
    private String mVideoGif;//视频封面动态图

    public boolean isAutoPlay() {
        return mIsAutoPlay;
    }

    public void setAutoPlay(boolean autoPlay) {
        mIsAutoPlay = autoPlay;
    }

    public String getVideoTime() {
        return mVideoTime;
    }

    public void setVideoTime(String videoTime) {
        mVideoTime = videoTime;
    }

    public String getVideoW() {
        return mVideoW;
    }

    public void setVideoW(String videoW) {
        mVideoW = videoW;
    }

    public String getVideoH() {
        return mVideoH;
    }

    public void setVideoH(String videoH) {
        mVideoH = videoH;
    }

    public Map<String, String> getVideoUrlMap() {
        return mVideoUrlMap;
    }

    public void setVideoUrlMap(Map<String, String> videoUrlMap) {
        mVideoUrlMap = videoUrlMap;
    }

    public String getVideoImg() {
        return mVideoImg;
    }

    public void setVideoImg(String videoImg) {
        mVideoImg = videoImg;
    }

    public String getVideoGif() {
        return mVideoGif;
    }

    public void setVideoGif(String videoGif) {
        mVideoGif = videoGif;
    }

    public boolean isEmpty() {
        return mVideoUrlMap == null || TextUtils.isEmpty(mVideoUrlMap.get("defaultUrl"));
    }
}
