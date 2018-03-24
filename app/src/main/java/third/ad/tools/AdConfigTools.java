package third.ad.tools;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import acore.logic.LoginManager;
import acore.logic.XHClick;
import acore.override.XHApplication;
import acore.tools.FileManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;
import third.ad.AdParent;
import third.ad.db.XHAdSqlite;
import third.ad.db.bean.AdBean;

import static third.ad.tools.AdPlayIdConfig.FULL_SRCEEN_ACTIVITY;

public class AdConfigTools {
    private volatile static AdConfigTools mAdConfigTools;

    private String showAdId = "cancel";

    public boolean isLoadOver = false;

    private AdConfigTools() {
    }

    public ArrayList<Map<String, String>> list = new ArrayList<>();//服务端广告集合

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
        // 请求网络信息
        ReqInternet.in().doGet(StringManager.api_adData, new InternetCallback() {
            @Override
            public void loaded(int flag, String url, final Object returnObj) {
                if (flag >= ReqInternet.REQ_OK_STRING) {
                    //更新广告配置
                    XHAdSqlite adSqlite = XHAdSqlite.newInstance(XHApplication.in());
                    adSqlite.updateConfig((String) returnObj);
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
    }

    /**
     * 请求美食圈列表广告
     *
     * @param context
     */
    public void setRequest(Context context) {
        String url = StringManager.api_getQuanList;
        ReqInternet.in().doGet(url, new InternetCallback() {
            @Override
            public void loaded(int flag, String url, Object msg) {
                if (flag >= ReqInternet.REQ_OK_STRING) {
                    list = StringManager.getListMapByJson(msg);
                }
            }
        });
    }

    public AdBean getAdConfig(String adPlayId) {
        XHAdSqlite adSqlite = XHAdSqlite.newInstance(XHApplication.in());
        return adSqlite.getAdConfig(adPlayId);
    }

    /**
     * 通过搜菜谱，输入指定指令显示对应广告
     *
     * @param ad
     */
    public void changeAd(String ad) {
        if ("gdt".equals(ad)) {
            showAdId = AdParent.ADKEY_GDT;
        } else if ("banner".equals(ad)) {
            showAdId = AdParent.ADKEY_BANNER;
        } else if ("cancel".equals(ad)) {
            showAdId = "cancel";
        }
    }

    public boolean isShowAd(String adPlayId, String adKey) {
        if ("cancel".equals(showAdId)) {
            String isGourmet = LoginManager.userInfo.get("isGourmet");
            //是美食家，但不是banner广告则返回不显示广告
            if ("2".equals(isGourmet) && !AdParent.ADKEY_BANNER.equals(adKey)) {
                return false;
            }
            AdBean adBean = getAdConfig(adPlayId);
            if (adBean != null) {
                switch (adKey) {
                    case AdParent.ADKEY_GDT:
                        return "2".equals(adBean.isGdt);
                    case AdParent.ADKEY_BANNER:
                        return "2".equals(adBean.isBanner);
                    default:
                        break;
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
        if (AdParent.TONGJI_TX_API.equals(channel))
            XHClick.mapStat(context, "ad_show", twoLevel, threeLevel);
    }

    public void onAdClick(Context context, String channel, String twoLevel, String threeLevel) {
        if (TextUtils.isEmpty(twoLevel)) return;
        if (AdParent.TONGJI_TX_API.equals(channel))
            XHClick.mapStat(context, "ad_click", twoLevel, threeLevel);
    }

    /**
     *
     * @param event：行为事件
     * @param gg_position_id：广告位id
     * @param gg_business：广告商
     * @param gg_business_id：广告商id
     */
    public void postStatistics(@NonNull String event, @NonNull String gg_position_id, @NonNull String gg_business, @NonNull String gg_business_id) {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        //时间
        map.put("app_time", Tools.getAssignTime("yyyy-MM-dd HH:mm:ss", 0));
        //行为事件
        map.put("event", event);
        //广告位id
        if (!TextUtils.isEmpty(gg_position_id)) {
            map.put("gg_position_id", gg_position_id);
        }
        //广告商
        if (!TextUtils.isEmpty(gg_business)) {
            map.put("gg_business", gg_business);
        }
        //广告商id
        if (!TextUtils.isEmpty(gg_business_id)) {
            map.put("gg_business_id", gg_business_id);
        }
        JSONObject jsonObject = MapToJsonEncode(map);
        LinkedHashMap<String, String> params = new LinkedHashMap<>();
        params.put("log_json", jsonObject.toString());
        Log.i("tzy", "postStatistics: params=" + params.toString());
//        requestStatistics(StringManager.api_monitoring_9,params);
        requestStatistics(StringManager.api_adsNumber,params);
    }

    private void requestStatistics(String url, LinkedHashMap<String, String> params) {
        ReqInternet.in().doPost(url, params, new InternetCallback() {
            @Override
            public void loaded(int flag, String url, Object returnObj) {
            }
        });
    }

    public static JSONObject MapToJsonEncode(Map<String, String> maps) {

        JSONObject jsonObject = new JSONObject();
        if (maps == null || maps.size() <= 0) return jsonObject;

        Iterator<Map.Entry<String, String>> enty = maps.entrySet().iterator();
        try {
            while (enty.hasNext()) {
                Map.Entry<String, String> entry = enty.next();
                jsonObject.put(entry.getKey(), Uri.encode(entry.getValue()));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
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

    private Runnable mStatisticsRun = new Runnable() {
        @Override
        public void run() {
//            requestStatistics(StringManager.api_adsNumber, );
        }
    };

}
