package amodule.article.db;

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

    private int id = -1;
    private String title;
    private String classCode;
    private String content;
    private int isOriginal; //是否原创 1-转载 2-原创
    private String repAddress; //转载地址
    private String img; //首图
    private String video; //视频
    private String videoImg;  //视频首图

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
}
