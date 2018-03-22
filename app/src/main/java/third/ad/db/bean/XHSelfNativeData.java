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
    public static final String IMG_TYPE_LOGO = "logoImg";

    private String id;
    private String positionId;
    private String title;
    private String desc;
    private String brandName;
    private int showNum;
    private String url;
    private String adType;//1-自有活动，2-自有广告
    private String dbType;//1-下载类型，2-落地页
    private String updateTime;
    private HashMap<String, String> imgs = new HashMap<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPositionId() {
        return positionId;
    }

    public void setPositionId(String positionId) {
        this.positionId = positionId;
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

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
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

    public String getAdType() {
        return adType;
    }

    public void setAdType(String type) {
        this.adType = type;
    }

    public String getDbType() {
        return dbType;
    }

    public void setDbType(String dbType) {
        this.dbType = dbType;
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

    public void setLogoImage(String imageUrl) {
        if (imgs != null && !TextUtils.isEmpty(imageUrl)) {
            imgs.put(IMG_TYPE_LOGO, imageUrl);
        }
    }

    public String getBigImage() {
        return imgs != null ? imgs.get(IMG_TYPE_BIG) : "";
    }

    public String getLittleImage() {
        return imgs != null ? imgs.get(IMG_TYPE_LITTLE) : "";
    }

    public String getLogoImage(){
        return imgs != null ? imgs.get(IMG_TYPE_LOGO) : "";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        XHSelfNativeData that = (XHSelfNativeData) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        return updateTime != null ? updateTime.equals(that.updateTime) : that.updateTime == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (positionId != null ? positionId.hashCode() : 0);
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (desc != null ? desc.hashCode() : 0);
        result = 31 * result + (brandName != null ? brandName.hashCode() : 0);
        result = 31 * result + showNum;
        result = 31 * result + (url != null ? url.hashCode() : 0);
        result = 31 * result + (adType != null ? adType.hashCode() : 0);
        result = 31 * result + (dbType != null ? dbType.hashCode() : 0);
        result = 31 * result + (updateTime != null ? updateTime.hashCode() : 0);
        result = 31 * result + (imgs != null ? imgs.hashCode() : 0);
        return result;
    }
}
