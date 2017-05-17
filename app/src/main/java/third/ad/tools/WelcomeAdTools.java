package third.ad.tools;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.inmobi.ads.InMobiAdRequestStatus;
import com.inmobi.ads.InMobiNative;


import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.LoginManager;
import acore.override.helper.XHActivityManager;
import acore.tools.FileManager;
import acore.tools.StringManager;
import third.ad.scrollerAd.XHScrollerAdParent;
import xh.basic.tool.UtilString;

/**
 * PackageName : third.ad.tools
 * Created by MrTrying on 2017/5/10 14:28.
 * E_mail : ztanzeyu@gmail.com
 */

public class WelcomeAdTools {

    private static volatile WelcomeAdTools mInstance = null;

    private InMobiNative welcomeNative = null;

    private InMobiNative temporaryNative = null;
    private static final String CONFIGKEY = "splashconfig";
    /** 表示开启 */
    private static final int OPEN = 2;
    /** 切换最短时间 */
    private int splashmins = 60;
    /** 切换最长时间 */
    private int splashmaxs = 5 * 60;
    /** 是否二次开启 */
    private int open = OPEN;
    /** 开启时间 */
    private int duretimes = 4;
    /** 展示次数，0表示无限次 */
    private int shownum = 0;

    //广告处理
    private ArrayList<String> list_ad = new ArrayList<>();//存储广告类型的集合
    private ArrayList<String> ad_data = new ArrayList<>();//存储对应数据的集合
    private int index_ad = 0;
    /** 广点通回调 */
    private GdtCallback mGdtCallback;
    /** Inmobi回调 */
    private InMobiNativeCallback mInMobiNativeCallback;
    /** 自有AD回调 */
    private XHBannerCallback mXHBannerCallback;

    private WelcomeAdTools() {
        String splashConfigDataStr = AppCommon.getConfigByLocal(CONFIGKEY);
        Map<String, String> data = StringManager.getFirstMap(splashConfigDataStr);
        String[] keys = {"splashmins", "splashmaxs", "open", "duretimes", "shownum"};
        int[] values = {splashmins, splashmaxs, open, duretimes, shownum};
        for (int index = 0; index < keys.length; index++) {
            if (data.containsKey(keys[index])
                    && !TextUtils.isEmpty(data.get(keys[index]))) {
                values[index] = Integer.parseInt(data.get(keys[index]));
            }
        }
    }

    public static synchronized WelcomeAdTools getInstance() {
        if (null == mInstance) {
            synchronized (WelcomeAdTools.class) {
                if (null == mInstance) {
                    mInstance = new WelcomeAdTools();
                }
            }
        }
        return mInstance;
    }

    /**
     * 广告入口
     */
    public void handlerAdData(final boolean isCache) {
        list_ad.clear();
        ad_data.clear();
        index_ad = 0;
        String data = FileManager.readFile(FileManager.getDataDir() + FileManager.file_ad);
        if (TextUtils.isEmpty(data))
            return;
        ArrayList<Map<String, String>> list = StringManager.getListMapByJson(data);
        Map<String, String> map = list.get(0);
        if (map.containsKey(AdPlayIdConfig.WELCOME)) {
            ArrayList<Map<String, String>> listTemp = StringManager.getListMapByJson(map.get(AdPlayIdConfig.WELCOME));
            if (!listTemp.get(0).containsKey("adConfig")) {
                return;
            }
            Map<String, String> configMap = StringManager.getFirstMap(listTemp.get(0).get("adConfig"));
            String banner = listTemp.get(0).get("banner");
            final String[] keys = {"1", "2", "3", "4"};
            for (String key : keys) {
                if (configMap.containsKey(key))
                    handlerData(configMap.get(key), list_ad, banner);
            }
            //开启广告
            nextAd(isCache);
        }
    }

    private void handlerData(String temp, ArrayList<String> list_ad, String banner) {
        Map<String, String> map_ad = StringManager.getFirstMap(temp);
        if (map_ad.get("open").equals("2")) {
            list_ad.add(map_ad.get("type"));
            //处理banner类型数据
            if (XHScrollerAdParent.TAG_BANNER.equals(map_ad.get("type"))) {
                ad_data.add(banner);
            } else {
                ad_data.add(map_ad.get("data"));
            }
        }
    }

    /**
     * 下一个广告数据
     */
    private void nextAd(boolean isCache) {
        if (list_ad.size() > index_ad) {
            if (XHScrollerAdParent.TAG_GDT.equals(list_ad.get(index_ad))) {//gdt
                if (index_ad == 0 && isCache) {
                    return;
                }
                if (LoginManager.isShowAd())
                    displayGdtAD();
            } else if (XHScrollerAdParent.TAG_INMOBI.equals(list_ad.get(index_ad))) {//inmobi
                if (LoginManager.isShowAd())
                    getInMobi(isCache);
            } else if (XHScrollerAdParent.TAG_BANNER.equals(list_ad.get(index_ad))) {//xh
                if (LoginManager.isShowAd())
                    getXHBanner();
            }
        }
    }

    //展示AD
    private void displayGdtAD() {
        final String adid = analysData(ad_data.get(index_ad));
        if (TextUtils.isEmpty(adid) || null == mGdtCallback)
            return;
        GdtAdTools.newInstance().showSplashAD(
                XHActivityManager.getInstance().getCurrentActivity(),
                mGdtCallback.getADLayout(),
                mGdtCallback.getTextSikp(),
                adid,
                new third.ad.tools.GdtAdTools.GdtSplashAdListener() {
                    @Override
                    public void onAdPresent() {
                        mGdtCallback.onAdPresent();
                    }

                    @Override
                    public void onAdFailed(String reason) {
                        index_ad++;
                        nextAd(false);
                        mGdtCallback.onAdFailed(reason);
                    }

                    @Override
                    public void onAdDismissed() {
                        mGdtCallback.onAdDismissed();
                    }

                    @Override
                    public void onAdClick() {
                        mGdtCallback.onAdClick();
                    }

                    @Override
                    public void onADTick(long millisUntilFinished) {
                        mGdtCallback.onADTick(millisUntilFinished);
                    }
                });
    }

    /**
     * 处理inmobi广告
     */
    private void getInMobi(final boolean isCache) {
        String adid = analysData(ad_data.get(index_ad));
        if (TextUtils.isEmpty(adid))
            return;
        if (isCache) {
            welcomeNative = null;
        }
        //需要缓存
        if (null != welcomeNative) {
            if (null != mInMobiNativeCallback) {
                mInMobiNativeCallback.onAdLoadSucceeded(welcomeNative);
                return;
            }
        }
        //
        temporaryNative = new InMobiNative(XHActivityManager.getInstance().getCurrentActivity(),
                Long.parseLong(adid),
                new InMobiNative.NativeAdListener() {
                    @Override
                    public void onAdLoadSucceeded(InMobiNative inMobiNative) {
                        if (isCache) {
                            welcomeNative = inMobiNative;
                        }
                        Log.i("tzy", "WelcomeAdTools onAdLoadSucceeded");
                        if (null != mInMobiNativeCallback) {
                            mInMobiNativeCallback.onAdLoadSucceeded(inMobiNative);
                        }
                    }

                    @Override
                    public void onAdLoadFailed(InMobiNative inMobiNative, InMobiAdRequestStatus inMobiAdRequestStatus) {
                        index_ad++;
                        nextAd(false);
                        Log.i("tzy", "WelcomeAdTools onAdLoadFailed");
                        if (null != mInMobiNativeCallback) {
                            mInMobiNativeCallback.onAdLoadFailed(inMobiNative, inMobiAdRequestStatus);
                        }
                    }

                    @Override
                    public void onAdDismissed(InMobiNative inMobiNative) {
                        Log.i("tzy", "WelcomeAdTools onAdDismissed");
                        if (null != mInMobiNativeCallback) {
                            mInMobiNativeCallback.onAdDismissed(inMobiNative);
                        }
                    }

                    @Override
                    public void onAdDisplayed(InMobiNative inMobiNative) {
                        Log.i("tzy", "WelcomeAdTools onAdDisplayed");
                        if (null != mInMobiNativeCallback) {
                            mInMobiNativeCallback.onAdDisplayed(inMobiNative);
                        }
                    }

                    @Override
                    public void onUserLeftApplication(InMobiNative inMobiNative) {
                        Log.i("tzy", "WelcomeAdTools onUserLeftApplication");
                        if (null != mInMobiNativeCallback) {
                            mInMobiNativeCallback.onUserLeftApplication(inMobiNative);
                        }
                    }
                });
        //加载广告
        temporaryNative.load();
    }

    /**
     * 处理xh自有的广告
     */
    private void getXHBanner() {
        if (TextUtils.isEmpty(ad_data.get(index_ad))) {
            nextAd(false);
            return;
        }
        Map<String, String> dataMap = StringManager.getFirstMap(ad_data.get(index_ad));//banner数据
        String url = "";
        if (dataMap != null && !TextUtils.isEmpty(dataMap.get("imgs"))) {
            Map<String, String> mapImgs = StringManager.getFirstMap(dataMap.get("imgs"));
            if (mapImgs != null && !TextUtils.isEmpty(mapImgs.get("indexImg1"))) {
                url = mapImgs.get("indexImg1");
            } else {
                nextAd(false);
                return;
            }
        } else {
            nextAd(false);
            return;
        }
        final String landingURL = dataMap.get("url");
        if (null != mXHBannerCallback) {
            mXHBannerCallback.onAdLoadSucceeded(url, landingURL);
        }
    }

    private String analysData(String data) {
        LinkedHashMap<String, String> map_link = UtilString.getMapByString(data, "&", "=");
        String adid = "";
        if (map_link.containsKey("adid"))
            adid = map_link.get("adid");
        return adid;
    }

    public interface GdtCallback {
        public void onAdPresent();

        public void onAdFailed(String reason);

        public void onAdDismissed();

        public void onAdClick();

        public void onADTick(long millisUntilFinished);

        public ViewGroup getADLayout();

        public View getTextSikp();
    }

    public interface InMobiNativeCallback {
        public void onAdLoadSucceeded(InMobiNative inMobiNative);

        public void onAdLoadFailed(InMobiNative inMobiNative, InMobiAdRequestStatus inMobiAdRequestStatus);

        public void onAdDismissed(InMobiNative inMobiNative);

        public void onAdDisplayed(InMobiNative inMobiNative);

        public void onUserLeftApplication(InMobiNative inMobiNative);
    }

    public interface XHBannerCallback {
        public void onAdLoadSucceeded(String url, String loadingUrl);

    }

    public void setmInMobiNativeCallback(InMobiNativeCallback mInMobiNativeCallback) {
        this.mInMobiNativeCallback = mInMobiNativeCallback;
    }

    public GdtCallback getmGdtCallback() {
        return mGdtCallback;
    }

    public void setmGdtCallback(GdtCallback mGdtCallback) {
        this.mGdtCallback = mGdtCallback;
    }


    public XHBannerCallback getmXHBannerCallback() {
        return mXHBannerCallback;
    }

    public void setmXHBannerCallback(XHBannerCallback mXHBannerCallback) {
        this.mXHBannerCallback = mXHBannerCallback;
    }

    public int getSplashmins() {
        return splashmins;
    }

    public int getSplashmaxs() {
        return splashmaxs;
    }

    public boolean isOpenSecond() {
        return OPEN == open && LoginManager.isShowAd();
    }

    public int getDuretimes() {
        return duretimes;
    }

    public int getShownum() {
        return shownum;
    }
}
