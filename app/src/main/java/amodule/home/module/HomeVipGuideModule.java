package amodule.home.module;

public class HomeVipGuideModule {
    /**
     * Vip的状态：1，未开通；2，即将到期；3，已经过期
     */
    private String mVipMaturityStatus;
    private String mTitle;
    private String mSubtitle;
    private String mDesc;
    private String mGotoUrl;

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getDesc() {
        return mDesc;
    }

    public void setDesc(String desc) {
        mDesc = desc;
    }

    public String getGotoUrl() {
        return mGotoUrl;
    }

    public void setGotoUrl(String gotoUrl) {
        mGotoUrl = gotoUrl;
    }

    public String getVipMaturityStatus() {
        return mVipMaturityStatus;
    }

    public void setVipMaturityStatus(String vipMaturityStatus) {
        mVipMaturityStatus = vipMaturityStatus;
    }

    public String getSubtitle() {
        return mSubtitle;
    }

    public void setSubtitle(String subtitle) {
        mSubtitle = subtitle;
    }
}
