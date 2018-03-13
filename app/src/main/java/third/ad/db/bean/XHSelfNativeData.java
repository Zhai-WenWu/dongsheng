package third.ad.db.bean;

import android.text.TextUtils;

import java.util.HashMap;

/**
 * Description :
 * PackageName : third.ad.db.bean
 * Created by mrtrying on 2018/3/8 16:46:30.
 * e_mail : ztanzeyu@gmail.com
 */
public class XHSelfNativeData {

    public static final String IMG_TYPE_BIG = "big";
    public static final String IMG_TYPE_LITTLE = "little";

    private String id;
    private String title;
    private String desc;
    private int showNum;
    private String url;
    private String type;//1-自有活动，2-自有广告
    private String updateTime;
    private HashMap<String, String> imgs = new HashMap<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public int getShowNum() {
        return showNum;
    }

    public void setShowNum(int showNum) {
        this.showNum = showNum;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public HashMap<String, String> getImgs() {
        return imgs;
    }

    public void setBigImage(String imageUrl) {
        if (imgs != null && !TextUtils.isEmpty(imageUrl)) {
            imgs.put(IMG_TYPE_BIG, imageUrl);
        }
    }

    public void setLittleImage(String imageUrl) {
        if (imgs != null && !TextUtils.isEmpty(imageUrl)) {
            imgs.put(IMG_TYPE_LITTLE, imageUrl);
        }
    }

    public String getBigImage() {
        return imgs != null ? imgs.get(IMG_TYPE_BIG) : "";
    }

    public String getLittleImage() {
        return imgs != null ? imgs.get(IMG_TYPE_LITTLE) : "";
    }
}
