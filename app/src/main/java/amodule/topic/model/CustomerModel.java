package amodule.topic.model;

/**
 * 用户信息
 */
public class CustomerModel {
    private String mUserCode;
    private String mNickName;
    private String mHeaderImg;
    private boolean mIsFollow;
    private String mGotoUrl;

    public String getUserCode() {
        return mUserCode;
    }

    public void setUserCode(String userCode) {
        mUserCode = userCode;
    }

    public String getNickName() {
        return mNickName;
    }

    public void setNickName(String nickName) {
        mNickName = nickName;
    }

    public String getHeaderImg() {
        return mHeaderImg;
    }

    public void setHeaderImg(String headerImg) {
        mHeaderImg = headerImg;
    }

    public boolean isFollow() {
        return mIsFollow;
    }

    public void setFollow(boolean follow) {
        mIsFollow = follow;
    }

    public String getGotoUrl() {
        return mGotoUrl;
    }

    public void setGotoUrl(String gotoUrl) {
        mGotoUrl = gotoUrl;
    }
}
