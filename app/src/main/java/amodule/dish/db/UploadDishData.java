package amodule.dish.db;

import java.io.Serializable;

import acore.override.data.UploadData;

public class UploadDishData extends UploadData implements Serializable {
    // --数据库字段名;
    public static String ds_id = "id";
    public static String ds_code = "code";
    public static String ds_name = "name";
    public static String ds_cover = "cover";//菜谱效果图
    public static String ds_food = "food";
    public static String ds_burden = "burden";
    public static String ds_makes = "makes";
    public static String ds_capture = "capture";
    public static String ds_tips = "tips";
    public static String ds_story = "story";
    public static String ds_addTime = "addTime";
    public static String ds_readyTime = "readyTime";
    public static String ds_cookTime = "cookTime";
    public static String ds_taste = "taste";
    public static String ds_diff = "diff";
    public static String ds_exclusive = "exclusive";
    public static String ds_uploadTimeCode = "uploadTimeCode";
    public static String ds_videoType = "videoType";
    //js吊起上次菜谱用的的
    public static String ds_activityId = "activityId";
    public static String ds_dishType = "subjectType";
//	ds_subjectType包括
    /**
     * 发布中
     */
    public static final String UPLOAD_ING = "发布中";
    /**
     * 后台发布
     */
    public static final String UPLOAD_ING_BACK = "后台发布";
    /**
     * 草稿
     */
    public static final String UPLOAD_DRAF = "草稿";
    /**
     * 发布失败
     */
    public static final String UPLOAD_FAIL = "发布失败";
    public static final String UPLOAD_PAUSE = "暂停";
    public static final String UPLOAD_SUCCESS = "成功";

    private String name = "";
    private String cover = "";
    /**
     * 封面
     */
    private String coverUrl = "";

    /**
     *  合成视频信息
     *  videoPath  videoUrl videoMd5
     */
    private String captureVideoInfo = "";

    /**
     * 辅料  Ingredient
     */
    private String burden = "";
    private String food = "";
    private String makes = "";
    private String tips = ""; //配置小贴士
    private String addTime = "";
    private String story = "";
    private String dishType = "";
    private String activityId = "";
    private String readyTime = "";
    private String cookTime = "";
    private String taste = "";
    private String diff = "";
    private String exclusive = "";
    private String videType = "1";
    //------------不存数据库的---------------//
    private String removeName = "";
    private boolean isCheckAgr = true;


    public String getReadyTime() {
        return readyTime;
    }

    public void setReadyTime(String readyTime) {
        this.readyTime = readyTime;
    }

    public String getCookTime() {
        return cookTime;
    }

    public void setCookTime(String cookTime) {
        this.cookTime = cookTime;
    }

    public String getTaste() {
        return taste;
    }

    public void setTaste(String taste) {
        this.taste = taste;
    }

    public String getDiff() {
        return diff;
    }

    public void setDiff(String diff) {
        this.diff = diff;
    }

    public String getExclusive() {
        return exclusive;
    }

    public void setExclusive(String exs) {
        exclusive = exs;
    }

    public String getRemoveName() {
        return removeName;
    }

    public void setRemoveName(String removeName) {
        this.removeName = removeName;
    }

    public String getName() {
        return name;
    }

    /**
     * 设置标题
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    public String getStory() {
        return story;
    }

    public void setStory(String story) {
        this.story = story;
    }

    public String getDishType() {
        return dishType;
    }

    public void setDishType(String dishType) {
        this.dishType = dishType;
    }

    /**
     * 效果图
     *
     * @return
     */
    public String getCover() {
        return cover;
    }

    /**
     * 效果图
     *
     * @return
     */
    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getBurden() {
        return burden;
    }

    /**
     * 设置辅料
     *
     * @param burden
     */
    public void setBurden(String burden) {
        this.burden = burden;
    }

    /**
     * 食材
     *
     * @return
     */
    public String getFood() {
        return food;
    }

    /**
     * 设置食材
     *
     * @param food
     */
    public void setFood(String food) {
        this.food = food;
    }

    public String getMakes() {
        return makes;
    }

    public void setMakes(String makes) {
        this.makes = makes;
    }

    /**
     * 小贴士
     */
    public String getTips() {
        return tips;
    }

    /**
     * 小贴士
     *
     * @param tips
     */
    public void setTips(String tips) {
        this.tips = tips;
    }

    /**
     * 添加此菜谱时的时间
     */
    public String getAddTime() {
        return addTime;
    }

    /**
     * 添加此菜谱时的时间
     *
     * @param addTime
     */
    public void setAddTime(String addTime) {
        this.addTime = addTime;
    }

    public void setActivityId(String id) {
        this.activityId = id;
    }

    public String getActivityId() {
        return this.activityId;
    }

    public void setCheckGreement(boolean isCheck) {
        isCheckAgr = isCheck;
    }

    public boolean getCheckGreement() {
        return isCheckAgr;
    }

    public String getVideType() {
        return videType;
    }

    public void setVideType(String videoType) {
        this.videType = videoType;
    }

    public boolean getIsVidewDish() {
        return "2".equals(videType);
    }

    public void setVideType(boolean isVideoType) {
        if (isVideoType) {
            videType = "2";
        } else {
            videType = "1";
        }
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }



    public String getCaptureVideoInfo() {
        return captureVideoInfo;
    }

    public void setCaptureVideoInfo(String captureVideoInfo) {
        this.captureVideoInfo = captureVideoInfo;
    }
}
