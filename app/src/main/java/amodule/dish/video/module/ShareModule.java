package amodule.dish.video.module;

import java.util.Objects;

public class ShareModule {

    private String mUrl;
    private String mTitle;
    private String mContent;
    private String mImg;

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String url) {
        mUrl = url;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getContent() {
        return mContent;
    }

    public void setContent(String content) {
        mContent = content;
    }

    public String getImg() {
        return mImg;
    }

    public void setImg(String img) {
        mImg = img;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ShareModule that = (ShareModule) o;
        return Objects.equals(mUrl, that.mUrl) &&
                Objects.equals(mTitle, that.mTitle) &&
                Objects.equals(mContent, that.mContent) &&
                Objects.equals(mImg, that.mImg);
    }

    @Override
    public int hashCode() {

        return Objects.hash(mUrl, mTitle, mContent, mImg);
    }
}
