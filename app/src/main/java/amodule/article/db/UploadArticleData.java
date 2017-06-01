package amodule.article.db;

import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Map;

import acore.tools.StringManager;
import aplug.basic.InternetCallback;
import aplug.basic.ReqEncyptInternet;

/**
 * Created by Fang Ruijiao on 2017/5/22.
 */

public class UploadArticleData {
    // --数据库字段名;
    public static String article_id = "id";
    public static String article_title = "title";
    public static String article_classCode = "classCode";
    public static String article_content = "content";
    public static String article_isOriginal = "food"; //是否原创 1-转载 2-原创
    public static String article_repAddress = "repAddress"; //转载地址
    public static String article_img = "img"; //首图
    public static String article_video = "video"; //视频
    public static String article_videoImg = "videoImg";  //视频首图
    public static String article_imgs = "imgs"; //图片集合
    public static String article_code = "code";  //文章code
    public static String article_imgUrl = "imgUrl";  //图片url
    public static String article_videoUrl = "videoUrl";
    public static String article_videoImgUrl = "videoImgUrl";
    public static String article_uploadType = "uploadType";  //上传类型包括：后台发布、发布中

    private int id = -1;
    private String code;
    private String title;
    private String classCode;
    private String content;
    private int isOriginal; //是否原创 1-转载 2-原创
    private String repAddress; //转载地址
    private String img; //首图
    private String imgUrl; //首图url
    private String video; //视频
    private String videoUrl; //视频url
    private String videoImg;  //视频首图
    private String videoImgUrl;  //视频首图url
    private String imgs; //所有图片集合

    private String uploadType; //上传类型包括：后台发布、发布中

    public void setCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public void setVideoImgUrl(String videoImgUrl) {
        this.videoImgUrl = videoImgUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public void setImgs(String imgs) {
        this.imgs = imgs;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public String getVideoImgUrl() {
        return videoImgUrl;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setImgArray(ArrayList<Map<String,String>> arrayList){
        JSONArray jsonArray = new JSONArray();
        Map<String,String> map;
        JSONObject jsonObject;
        try {
            for (int i = 0; i < arrayList.size(); i++) {
                jsonObject = new JSONObject();
                map = arrayList.get(i);
                for (String key : map.keySet()) {
                    jsonObject.put(key, map.get(key));
                }
                jsonArray.put(jsonObject);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        imgs = jsonArray.toString();
    }

    public String getImgs() {
        return imgs;
    }

    public void setUploadType(String uploadType) {
        this.uploadType = uploadType;
    }

    public String getUploadType() {
        return uploadType;
    }

    public ArrayList<Map<String,String>> getImgArray(){
        return StringManager.getListMapByJson(imgs);
    }

    public void setClassCode(String classCode) {
        this.classCode = classCode;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public void setIsOriginal(int isOriginal) {
        this.isOriginal = isOriginal;
    }

    public void setRepAddress(String repAddress) {
        this.repAddress = repAddress;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setVideo(String video) {
        this.video = video;
    }

    public void setVideoImg(String videoImg) {
        this.videoImg = videoImg;
    }

    public String getClassCode() {
        return classCode;
    }

    public String getContent() {
        return content;
    }

    public int getId() {
        return id;
    }

    public String getImg() {
        return img;
    }

    public int getIsOriginal() {
        return isOriginal;
    }

    public String getRepAddress() {
        return repAddress;
    }

    public String getTitle() {
        return title;
    }

    public String getVideo() {
        return video;
    }

    public String getVideoImg() {
        return videoImg;
    }

    public void upload(String url,InternetCallback callback){
        StringBuffer uploadTextData = new StringBuffer();
        uploadTextData.append("title=");
        uploadTextData.append(getTitle());
        uploadTextData.append("&classCode=");
        uploadTextData.append(getClassCode());
        uploadTextData.append("&content=");
        try {
            uploadTextData.append(URLEncoder.encode(getContent(), HTTP.UTF_8));
        }catch (Exception e){
            e.printStackTrace();
        }
        uploadTextData.append("&isOriginal=");
        uploadTextData.append(String.valueOf(getIsOriginal()));
        uploadTextData.append("&repAddress=");
        uploadTextData.append(getRepAddress());
        uploadTextData.append("&code=");
        uploadTextData.append(getCode());
        ReqEncyptInternet.in().doEncypt(url, uploadTextData.toString(), callback);
    }
}
