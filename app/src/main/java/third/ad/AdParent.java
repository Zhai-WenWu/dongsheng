package third.ad;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;

import com.xiangha.R;

import acore.logic.XHClick;
import acore.override.XHApplication;
import acore.tools.Tools;
import third.ad.scrollerAd.XHScrollerAdParent;
import third.ad.tools.AdConfigTools;

/**
 * 广告父类
 *
 * @author FangRuijiao
 */
public abstract class AdParent {
    public static final String ADKEY_GDT = "isGdt";
    public static final String ADKEY_BANNER = "isBanner";
    public static final String ADKEY_TX = "";

    public static final String TONGJI_BANNER = "banner";
    public static final String TONGJI_TX_API = "tencent_api";

    /** 该广告的类型 */
    protected String mAdKey = "";
    protected String mAdType = "";
    protected AdClickListener mListener;
    protected String mAdPlayId = "";

    /**
     * 设置此广告是否需要：当出现在屏幕内后才显示
     */
    public boolean isNeedOnScreen = false;

    /**
     * 在父类先判断接口中广告是否显示，若显示再在子类中判断是否有数据
     *
     * @param adPlayId : 广告体id
     * @param listener
     */
    public boolean isShowAd(String adPlayId, AdIsShowListener listener) {
        mAdPlayId = adPlayId;
        initAdKey();
        return AdConfigTools.getInstance().isShowAd(adPlayId, mAdKey);
    }

    /**
     * 广告曝光，onResume时调用
     */
    public abstract void onResumeAd();

    /**
     * 广告不显示：onPause时调用
     */
    public abstract void onPsuseAd();

    public abstract void onDestroyAd();

    public void initAdKey() {
        if (this instanceof BannerAd) {
            mAdKey = ADKEY_BANNER;
            mAdType = XHScrollerAdParent.ADKEY_BANNER;
        } else if (this instanceof TencenApiAd) {
            mAdKey = ADKEY_TX;
            mAdType = XHScrollerAdParent.ADKEY_API;
        }
    }

    public void setOnAdClick(AdClickListener listener) {
        mListener = listener;
    }

    private void postTongji(String event, String adid) {
        AdConfigTools.getInstance().postStatistics(event, mAdPlayId, mAdType, adid);
    }

    /**
     * 用于异步返回广告是否显示
     *
     * @author FangRuijiao
     */
    public interface AdIsShowListener {
        public void onIsShowAdCallback(AdParent adParent, boolean isShow);
    }

    public interface AdClickListener {
        public void onAdClick();
    }

    public static abstract class AdListener {
        public void onAdCreate() {

        }

        public void onAdOver(View adView, Bitmap msg, int tag) {

        }

        public int getImgHeight(Context con) {
            return Tools.getDimen(con, R.dimen.dp_52);
        }
    }

    /**
     * 广告展示统计
     */
    protected void onAdShow(String oneLevel, String twoLevel, String threeLevel,String mId) {
        //自己网站上的统计
        postTongji("show", mId);
        XHClick.mapStat(XHApplication.in(), oneLevel, twoLevel, threeLevel);
    }

    /**
     * 广告点击统计
     *
     * @param oneLevel
     * @param twoLevel
     * @param threeLevel
     * @param key
     * @param mId
     */
    protected void onAdClick(String oneLevel, String twoLevel, String threeLevel, String key, String mId) {
        postTongji("click", mId);
        //umeng的统计
        XHClick.mapStat(XHApplication.in(), oneLevel, twoLevel, threeLevel);
        //其他统计
        AdConfigTools.getInstance().clickAds(mAdPlayId, key, mId);
        if (mListener != null) {
            mListener.onAdClick();
        }
    }

}
