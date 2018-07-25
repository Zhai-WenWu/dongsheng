package amodule.topic.model;

/**
 * 话题信息
 */
public class TopicModel {

    private String mCode;
    private String mTitle;
    private String mColor;
    private String mBgColor;
    private String mGotoUrl;

    public String getCode() {
        return mCode;
    }

    public void setCode(String code) {
        mCode = code;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getColor() {
        return mColor;
    }

    public void setColor(String color) {
        mColor = color;
    }

    public String getBgColor() {
        return mBgColor;
    }

    public void setBgColor(String bgColor) {
        mBgColor = bgColor;
    }

    public String getGotoUrl() {
        return mGotoUrl;
    }

    public void setGotoUrl(String gotoUrl) {
        mGotoUrl = gotoUrl;
    }
}
