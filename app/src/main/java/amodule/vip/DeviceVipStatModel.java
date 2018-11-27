package amodule.vip;

import java.io.Serializable;

public class DeviceVipStatModel implements Serializable, IDeviceVipStat {
    public static final String TAG = "DeviceVipStatModel";
    private static final long serialVersionUID = -6352310065877737027L;
    private String mEventID = "devicevip_binding";
    private String mTwoLevelBindSuccess = "bangdingchenggong";
    private String mTwoLevelBindFailed = "bangdingshibai";
    private String mTwoLevelBindDialog = "yindaobangdingtankuang";
    private String mTwoLevelBindSuccTipDialog = "bangdingchenggongtishitankuang";
    private String mTwoLevelVipBindPage = "vipbangdingdengluyemian";
    private String mThreeLevel1;
    private String mThreeLevel2;

    public DeviceVipStatModel(String threeLevel1, String threeLevel2) {
        mThreeLevel1 = threeLevel1;
        mThreeLevel2 = threeLevel2;
    }

    public String getThreeLevel1() {
        return mThreeLevel1;
    }

    public String getThreeLevel2() {
        return mThreeLevel2;
    }

    public String getEventID() {
        return mEventID;
    }

    public String getTwoLevelBindSuccess() {
        return mTwoLevelBindSuccess;
    }

    public String getTwoLevelBindFailed() {
        return mTwoLevelBindFailed;
    }

    public String getTwoLevelBindDialog() {
        return mTwoLevelBindDialog;
    }

    public String getTwoLevelBindSuccTipDialog() {
        return mTwoLevelBindSuccTipDialog;
    }

    public String getTwoLevelVipBindPage() {
        return mTwoLevelVipBindPage;
    }

}
