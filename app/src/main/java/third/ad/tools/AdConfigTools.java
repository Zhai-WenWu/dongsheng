package third.ad.tools;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
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

//    public String getAdConfigDataString(String adPlayId) {
//        String data = FileManager.readFile(FileManager.getDataDir() + FileManager.file_ad);
//        Map<String, String> map = StringManager.getFirstMap(data);
//        return map.get(adPlayId);
//    }

//    public Map<String, String> getAdConfigData(String adPlayId) {
//        String data = FileManager.readFile(FileManager.getDataDir() + FileManager.file_ad);
//        Map<String, String> map = StringManager.getFirstMap(data);
//        map = StringManager.getFirstMap(map.get(adPlayId));
//        return map;
//    }

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
     * @param event
     * @param gg_position_id
     * @param gg_business
     * @param gg_business_id
     */
    public void postStatistics(@NonNull String event, @NonNull String gg_position_id, @NonNull String gg_business, @NonNull String gg_business_id) {
        LinkedHashMap<String, String> params = new LinkedHashMap<>();
        //时间
        params.put("app_time", Tools.getAssignTime("yyyy-MM-dd HH:mm:ss", 0));
        //行为事件
        params.put("event", event);
        //广告位id
        params.put("gg_position_id", gg_position_id);
        //广告商
        if (!TextUtils.isEmpty(gg_business)) {
            params.put("gg_business", gg_business);
        }
        //广告商id
        if (!TextUtils.isEmpty(gg_business_id)) {
            params.put("gg_business_id", gg_business_id);
        }
        Log.i("tongji", "postStatistics: params=" + params.toString());
        ReqInternet.in().doPost(StringManager.api_monitoring_9, params, new InternetCallback() {
            @Override
            public void loaded(int flag, String url, Object returnObj) {
            }
        });
    }

    /**
     * 美食圈列表广告统计
     *
     * @param context
     * @param map
     * @param onClickSite 点击的位置 overall：整体、user：用户、time：时间、quanName：圈子名称、content：评论、like：赞
     */
//    public void postTongjiQuan(Context context, Map<String, String> map, String onClickSite, String event) {
//        String url = StringManager.api_monitoring_5;
//        if (TextUtils.isEmpty(onClickSite)) onClickSite = "overall";
//        else if ("用户头像".equals(onClickSite)) {
//            onClickSite = "user";
//        } else if ("用户昵称".equals(onClickSite)) {
//            onClickSite = "user";
//        } else if ("贴子内容".equals(onClickSite)) {
//            onClickSite = "overall";
//        } else if ("评论".equals(onClickSite)) {
//            onClickSite = "content";
//        } else {
//            onClickSite = "overall";
//        }
//
//        ReqInternet.in().doGet(url + "?adType=圈子广告位" + "&adid=" + map.get("showAdid") + "&cid=" + map.get("showCid") +
//                "&mid=" + map.get("showMid") + "site=" + map.get("showSite") + "&event=" + event + "&clickSite=" + onClickSite, new InternetCallback() {
//            @Override
//            public void loaded(int flag, String url, Object msg) {
//            }
//        });
//    }

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
