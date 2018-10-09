package amodule.topic.model;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TopicModel that = (TopicModel) o;
        return Objects.equals(mCode, that.mCode) &&
                Objects.equals(mTitle, that.mTitle) &&
                Objects.equals(mColor, that.mColor) &&
                Objects.equals(mBgColor, that.mBgColor) &&
                Objects.equals(mGotoUrl, that.mGotoUrl);
    }

    @Override
    public int hashCode() {

        return Objects.hash(mCode, mTitle, mColor, mBgColor, mGotoUrl);
    }
}
