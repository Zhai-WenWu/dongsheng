package amodule.topic.model;

import java.util.Objects;

/**
 * 地址信息
 */
public class AddressModel {
    private String mCode;
    private String mAddress;
    private String mColor;
    private String mBgColor;
    private String mGotoUrl;

    public String getCode() {
        return mCode;
    }

    public void setCode(String code) {
        mCode = code;
    }

    public String getAddress() {
        return mAddress;
    }

    public void setAddress(String address) {
        mAddress = address;
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
        AddressModel that = (AddressModel) o;
        return Objects.equals(mCode, that.mCode) &&
                Objects.equals(mAddress, that.mAddress) &&
                Objects.equals(mColor, that.mColor) &&
                Objects.equals(mBgColor, that.mBgColor) &&
                Objects.equals(mGotoUrl, that.mGotoUrl);
    }

    @Override
    public int hashCode() {

        return Objects.hash(mCode, mAddress, mColor, mBgColor, mGotoUrl);
    }
}
