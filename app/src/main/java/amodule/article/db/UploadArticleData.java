package amodule.article.db;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;


import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import acore.tools.StringManager;
import aplug.basic.InternetCallback;
import aplug.basic.ReqEncyptInternet;

/**
 * Created by XiangHa on 2017/5/22.
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
    public static String article_imgs = "imgs"; //图片集合
    public static String article_videos = "videos"; //视频集合
    public static String article_code = "code";  //文章code
    public static String article_imgUrl = "imgUrl";  //图片url
    public static String article_uploadType = "uploadType";  //上传类型包括：后台发布、发布中

    private int id = -1;
    private String code;
    private String title;
    private String classCode;
    private String content;
    private int isOriginal = -1; //是否原创 1-转载 2-原创
    private String repAddress; //转载地址
    private String img; //首图
    private String imgUrl; //首图url
    private String imgs; //所有图片集合
    private String videos; //所有视频集合

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


    public void setImgs(String imgs) {
        this.imgs = imgs;
    }

    public void setVideos(String videos) {
        this.videos = videos;
    }

    public void setVideoArray(ArrayList<Map<String,String>> arrayList){
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
        videos = jsonArray.toString();
    }

    public String getVideos() {
        return videos;
    }

    public String getImgUrl() {
        return imgUrl;
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

    public ArrayList<Map<String,String>> getVideoArray(){
        return StringManager.getListMapByJson(videos);
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

    public void setIsOriginal(String isOriginal) {
        if(TextUtils.isEmpty(isOriginal)){
            this.isOriginal = 1;
        }else{
            this.isOriginal = Integer.parseInt(isOriginal);
        }
    }

    public void setRepAddress(String repAddress) {
        this.repAddress = repAddress;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public void upload(String url,InternetCallback callback){
        Log.i("articleUpload","upload()  title:" + getTitle());
        LinkedHashMap map = new LinkedHashMap();
//        map.put("title",getTitle());
        map.put("title", Uri.encode(getTitle(), "utf-8"));
        map.put("classCode",getClassCode());
        try{
//            map.put("content",getContent());
            map.put("content", Uri.encode(getContent(), "utf-8"));
        }catch (Exception e){
            e.printStackTrace();
        }
        map.put("isOriginal",String.valueOf(getIsOriginal()));
        map.put("repAddress",getRepAddress());
        map.put("code",getCode());
        ReqEncyptInternet.in().doEncypt(url, map, callback);
    }
}
