package third.ad.tools;

import android.content.Context;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import acore.logic.LoginManager;
import acore.logic.XHClick;
import acore.override.XHApplication;
import acore.tools.FileManager;
import acore.tools.StringManager;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;
import third.ad.db.XHAdSqlite;
import third.ad.db.bean.AdBean;

import static third.ad.tools.AdPlayIdConfig.FULL_SRCEEN_ACTIVITY;

public class AdConfigTools extends BaseAdConfigTools {
    private volatile static AdConfigTools mAdConfigTools;

    private String showAdId = "cancel";

    public boolean isLoadOver = false;

    private AdConfigTools() {
    }

    public static AdConfigTools getInstance() {
        if (mAdConfigTools == null) {
            synchronized (AdConfigTools.class) {
                if (mAdConfigTools == null) {
                    mAdConfigTools = new AdConfigTools();
                }
            }
        }
        return mAdConfigTools;
    }

    public void getAdConfigInfo() {
        getAdConfigInfo(null);
    }

    public void getAdConfigInfo(InternetCallback callback) {
        //使用老接口更新全屏广告数据
        ReqInternet.in().doGet(StringManager.api_adData_old, new InternetCallback() {
            @Override
            public void loaded(int flag, String url, Object returnObj) {
                if (flag >= ReqInternet.REQ_OK_STRING) {
                    //更新全屏广告数据
                    Map<String, String> map = StringManager.getFirstMap(returnObj);
                    if (map.containsKey(FULL_SRCEEN_ACTIVITY)) {
                        final String path = FileManager.getDataDir() + FULL_SRCEEN_ACTIVITY + ".xh";
                        FileManager.saveFileToCompletePath(path, map.get(FULL_SRCEEN_ACTIVITY), false);
                    }
                    isLoadOver = true;
                    if (callback != null) {
                        callback.loaded(ReqInternet.REQ_OK_STRING, url, returnObj);
                    }
                }
            }
        });
        // 请求网络信息
        ReqInternet.in().doGet(StringManager.api_adData, new InternetCallback() {
            @Override
            public void loaded(int flag, String url, final Object returnObj) {
                if (flag >= ReqInternet.REQ_OK_STRING) {
                    //更新广告配置
                    XHAdSqlite adSqlite = XHAdSqlite.newInstance(XHApplication.in());
                    adSqlite.updateConfig((String) returnObj);
                }
            }
        });
    }

    public AdBean getAdConfig(String adPlayId) {
        XHAdSqlite adSqlite = XHAdSqlite.newInstance(XHApplication.in());
        return adSqlite.getAdConfig(adPlayId);
    }

    public boolean isShowAd(String adPlayId, String adKey) {
        if ("cancel".equals(showAdId)) {
            String isGourmet = LoginManager.userInfo.get("isGourmet");
            //是美食家，但不是banner广告则返回不显示广告
            if ("2".equals(isGourmet)) {
                return false;
            }
            AdBean adBean = getAdConfig(adPlayId);
            String conf = adBean.adConfig;
            if (!TextUtils.isEmpty(conf)) {
                ArrayList<Map<String, String>> arr = StringManager.getListMapByJson(conf);
                for(Map<String, String> map : arr) {
                    if (TextUtils.equals(map.get("type"), adKey) && TextUtils.equals("2", map
                            .get("open"))) {
                        return true;
                    }
                }
            }
        } else if ("level".equals(showAdId)) {
            return true;
        } else {
            return adKey.equals(showAdId);
        }
        return false;
    }

    public void onAdShow(Context context, String channel, String twoLevel, String threeLevel) {
        if (TextUtils.isEmpty(twoLevel)) return;
        XHClick.mapStat(context, "ad_show", twoLevel, threeLevel);
    }

    public void onAdClick(Context context, String channel, String twoLevel, String threeLevel) {
        if (TextUtils.isEmpty(twoLevel)) return;
        XHClick.mapStat(context, "ad_click", twoLevel, threeLevel);
    }

    /**
     * 广告位 点击
     *
     * @param id       广告位id
     * @param adType   baidu jingdong banner
     * @param adTypeId 广告类型id，第三方广告可传0
     */
    public void clickAds(String id, String adType, String adTypeId) {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        map.put("type", "position");
        map.put("id", id);
        map.put("adType", adType);
        map.put("adTypeId", adTypeId);
        ReqInternet.in().doPost(StringManager.api_clickAds, map, new InternetCallback() {
            @Override
            public void loaded(int flag, String url, Object returnObj) {

            }
        });
    }

    /**
     * 生活圈列表 广告点击
     *
     * @param id 广告id
     */
    public void clickAds(String id) {
        LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
        map.put("type", "quanList");
        map.put("id", id);
        ReqInternet.in().doPost(StringManager.api_clickAds, map, new InternetCallback() {
            @Override
            public void loaded(int flag, String url, Object returnObj) {
            }
        });
    }

}
