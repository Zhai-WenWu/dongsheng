package third.ad.tools;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.annimon.stream.Stream;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import acore.logic.ConfigMannager;
import acore.logic.LoginManager;
import acore.override.XHApplication;
import acore.override.helper.XHActivityManager;
import acore.tools.FileManager;
import acore.tools.StringManager;
import third.ad.db.XHAdSqlite;
import third.ad.db.bean.AdBean;
import third.ad.db.bean.XHSelfNativeData;

import static third.ad.scrollerAd.XHScrollerAdParent.ADKEY_BAIDU;
import static third.ad.scrollerAd.XHScrollerAdParent.ADKEY_GDT;
import static third.ad.scrollerAd.XHScrollerAdParent.TAG_BAIDU;
import static third.ad.scrollerAd.XHScrollerAdParent.TAG_BANNER;
import static third.ad.scrollerAd.XHScrollerAdParent.TAG_GDT;
import static third.ad.tools.AdPlayIdConfig.WELCOME;

/**
 * PackageName : third.ad.tools
 * Created by MrTrying on 2017/5/10 14:28.
 * E_mail : ztanzeyu@gmail.com
 */

public class WelcomeAdTools {

    private static volatile WelcomeAdTools mInstance = null;
    private static final String CONFIGKEY = "kaipingAgainConfig";


    /**
     * 切换最短时间
     */
    private int splashmins = 80;

    //广告处理
    private ArrayList<Map<String, String>> mAdData = new ArrayList<>();
    private int index_ad = 0;
    private String adPositionId = "";
    /**
     * 广点通回调
     */
    private GdtCallback mGdtCallback;
    /**
     * 自有AD回调
     */
    private XHBannerCallback mXHBannerCallback;
    private BaiduCallback mBaiduCallback;
    private boolean isTwoShow = false;

    private WelcomeAdTools() {
        String splashConfigDataStr = ConfigMannager.getConfigByLocal(CONFIGKEY);
        if (TextUtils.isEmpty(splashConfigDataStr)) {
            return;
        }
        Map<String, String> data = StringManager.getFirstMap(splashConfigDataStr);
        splashmins = Integer.parseInt(data.get("againTime"));
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

    public void handlerAdData(final boolean isCache) {
        this.handlerAdData(isCache, null, false);
    }

    /**
     * 广告入口
     */
    public void handlerAdData(final boolean isCache, AdNoDataCallBack CallBack, boolean isTwoShow) {
        this.isTwoShow = isTwoShow;
        mAdData.clear();
        index_ad = 0;
        this.mAdNoDataCallBack = CallBack;

        Log.i("tzy", "WelcomeAdTools handlerAdData.");
        XHAdSqlite adSqlite = XHAdSqlite.newInstance(XHApplication.in());
        AdBean adBean = adSqlite.getAdConfig(WELCOME);
        if (adBean == null) {
            String splashConfigValue = FileManager.readFile(FileManager.getDataDir() + "ad");
            Map<String, String> splashConfig = StringManager.getFirstMap(splashConfigValue);
            splashConfig = StringManager.getFirstMap(splashConfig.get(WELCOME));
            splashConfig = StringManager.getFirstMap(splashConfig.get("adConfig"));
            JSONArray jsonArray = new JSONArray();
            for (int i = 0; i < splashConfig.size(); i++) {
                String config = splashConfig.get(String.valueOf(i + 1));
                try {
                    JSONObject jsonObject = new JSONObject(config);
                    jsonArray.put(jsonObject);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            if(jsonArray.length() > 0){
                adBean = new AdBean();
                adBean.adConfig = jsonArray.toString();
            }
        }
        if (adBean == null || TextUtils.isEmpty(adBean.adConfig)) {
            if (mAdNoDataCallBack != null) {
                mAdNoDataCallBack.noAdData();
            }
            return;
        }
        adPositionId = adBean.adPositionId;
        List<Map<String, String>> configArray = StringManager.getListMapByJson(adBean.adConfig);
        Stream.of(configArray)
                .filter(value -> "2".equals(value.get("open")))
                .forEach(value -> mAdData.add(value));
        //开启广告
        nextAd(isCache);
    }

    /**
     * 下一个广告数据
     */
    private void nextAd(boolean isCache) {
        Log.i("tzy", "WelcomeAdTools nextAd.");
        if (index_ad == 0 && isCache) {
            return;
        }
        if (mAdData.size() > index_ad) {
            String type = mAdData.get(index_ad).get("type");
            switch (type) {
                case TAG_GDT:
                    if (LoginManager.isShowAd()) {
                        displayGdtAD();
                    } else {
                        index_ad++;
                        nextAd(isCache);
                    }
                    break;
                case TAG_BANNER:
                    getXHBanner();
                    break;
                case TAG_BAIDU:
                    if (LoginManager.isShowAd()) {
                        displayBaiduAD();
                    } else {
                        index_ad++;
                        nextAd(isCache);
                    }
                    break;
                default:
                    break;
            }
        } else {
            if (mAdNoDataCallBack != null)
                mAdNoDataCallBack.noAdData();
        }
    }

    //展示AD
    private void displayGdtAD() {
        String adid = isTwoShow ? "2090116985265199" : analysData(mAdData.get(index_ad).get("data"));
        if (TextUtils.isEmpty(adid) || null == mGdtCallback) {
            index_ad++;
            nextAd(false);
            return;
        }
        Log.i("zhangyujian", "adid:::" + adid);

        GdtAdTools.newInstance().showSplashAD(
                XHActivityManager.getInstance().getCurrentActivity(),
                mGdtCallback.getADLayout(),
                mGdtCallback.getTextSikp(),
                adid,
                new third.ad.tools.GdtAdTools.GdtSplashAdListener() {
                    @Override
                    public void onAdPresent() {
                        Log.i("zhangyujian", "GDT：：onAdPresent");
                        mGdtCallback.onAdPresent();
                        AdConfigTools.getInstance().postStatistics("show", WELCOME, adPositionId, ADKEY_GDT, "");
                    }

                    @Override
                    public void onAdFailed(String reason) {
                        Log.i("zhangyujian", "GDT：：onAdFailed");
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
                        AdConfigTools.getInstance().postStatistics("click", WELCOME, adPositionId, ADKEY_GDT, "");
                        mGdtCallback.onAdClick();
                    }

                    @Override
                    public void onADTick(long millisUntilFinished) {
                        mGdtCallback.onADTick(millisUntilFinished);
                    }
                });
    }

    /**
     * 处理xh自有的广告
     */
    private void getXHBanner() {
        String adid = analysData(mAdData.get(index_ad).get("data"));
        if (TextUtils.isEmpty(adid)) {
            index_ad++;
            nextAd(false);
            return;
        }
        XHSelfAdTools.getInstance().loadNativeData(Collections.singletonList(adid), new XHSelfAdTools.XHSelfCallback() {
            @Override
            public void onNativeLoad(ArrayList<XHSelfNativeData> list) {
                if (list == null || list.isEmpty()) {
                    index_ad++;
                    nextAd(false);
                    return;
                }
                XHSelfNativeData nativeData = list.get(0);
                if (nativeData != null
                        && !TextUtils.isEmpty(nativeData.getBigImage())
                        && ("1".equals(nativeData.getAdType()) || LoginManager.isShowAd())) {
                    if (null != mXHBannerCallback) {
                        mXHBannerCallback.onAdLoadSucceeded(nativeData);
                    }
                } else {
                    index_ad++;
                    nextAd(false);
                }
            }

            @Override
            public void onNativeFail() {
                index_ad++;
                nextAd(false);
            }
        });

    }

    private void displayBaiduAD() {
        final String adid = analysData(mAdData.get(index_ad).get("data"));
        if (TextUtils.isEmpty(adid) || null == mBaiduCallback) {
            index_ad++;
            nextAd(false);
            return;
        }
        Log.i("tzy", "displayBaiduAD");
        new Handler(Looper.getMainLooper()).post(
                () -> BaiduAdTools.newInstance().showSplashAD(XHActivityManager.getInstance().getCurrentActivity(),
                        mBaiduCallback.getADLayout(),
                        adid,
                        new BaiduAdTools.BaiduSplashAdCallback() {
                            @Override
                            public void onAdPresent() {
                                Log.i("zhangyujian", "displayBaiduAD::onAdPresent");
                                AdConfigTools.getInstance().postStatistics("show", WELCOME, adPositionId, ADKEY_BAIDU, "");
                                mBaiduCallback.onAdPresent();
                            }

                            @Override
                            public void onAdDismissed() {
                                mBaiduCallback.onAdDismissed();
                            }

                            @Override
                            public void onAdFailed(String s) {
                                Log.i("zhangyujian", "displayBaiduAD::onAdFailed");
                                index_ad++;
                                nextAd(false);
                                mBaiduCallback.onAdFailed(s);
                            }

                            @Override
                            public void onAdClick() {
                                AdConfigTools.getInstance().postStatistics("click", WELCOME, adPositionId, ADKEY_BAIDU, "");
                                mBaiduCallback.onAdClick();
                            }
                        }));
    }

    private String analysData(String data) {
        LinkedHashMap<String, String> map_link = StringManager.getMapByString(data, "&", "=");
        String adid = data;
        if (map_link.containsKey("adid"))
            adid = map_link.get("adid");
        return adid;
    }

    public interface GdtCallback {
        void onAdPresent();

        void onAdFailed(String reason);

        void onAdDismissed();

        void onAdClick();

        void onADTick(long millisUntilFinished);

        ViewGroup getADLayout();

        View getTextSikp();
    }


    public interface XHBannerCallback {
        void onAdLoadSucceeded(XHSelfNativeData nativeData);
    }

    public interface BaiduCallback {
        void onAdPresent();

        void onAdDismissed();

        void onAdFailed(String s);

        void onAdClick();

        ViewGroup getADLayout();
    }

    public void setmGdtCallback(GdtCallback mGdtCallback) {
        this.mGdtCallback = mGdtCallback;
    }

    public void setmXHBannerCallback(XHBannerCallback mXHBannerCallback) {
        this.mXHBannerCallback = mXHBannerCallback;
    }

    public void setBaiduCallback(BaiduCallback mBaiduCallback) {
        this.mBaiduCallback = mBaiduCallback;
    }

    public int getSplashmins() {
        return splashmins;
    }

    public interface AdNoDataCallBack {
        /*** 没有广告数据*/
        void noAdData();
    }

    private AdNoDataCallBack mAdNoDataCallBack;

}
