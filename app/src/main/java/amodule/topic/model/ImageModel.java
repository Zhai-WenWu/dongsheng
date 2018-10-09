package amodule.topic.model;

import java.util.Objects;

/**
 * 图片数据
 */
public class ImageModel {

    private String mImageW;
    private String mImageH;
    private String mImageUrl;

    public String getImageW() {
        return mImageW;
    }

    public void setImageW(String imageW) {
        mImageW = imageW;
    }

    public String getImageH() {
        return mImageH;
    }

    public void setImageH(String imageH) {
        mImageH = imageH;
    }

    public String getImageUrl() {
        return mImageUrl;
    }

    public void setImageUrl(String imageUrl) {
        mImageUrl = imageUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ImageModel that = (ImageModel) o;
        return Objects.equals(mImageW, that.mImageW) &&
                Objects.equals(mImageH, that.mImageH) &&
                Objects.equals(mImageUrl, that.mImageUrl);
    }

    @Override
    public int hashCode() {

        return Objects.hash(mImageW, mImageH, mImageUrl);
    }
}
