package amodule.topic.model;

/**
 * 用户信息
 */
public class CustomerModel {
    private String mUserCode;
    private String mNickName;
    private String mHeaderImg;
    private String mIsFollow;
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

    public String getIsFollow() {
        return mIsFollow;
    }

    public void setIsFollow(String isFollow) {
        mIsFollow = isFollow;
    }

    public String getGotoUrl() {
        return mGotoUrl;
    }

    public void setGotoUrl(String gotoUrl) {
        mGotoUrl = gotoUrl;
    }
}
