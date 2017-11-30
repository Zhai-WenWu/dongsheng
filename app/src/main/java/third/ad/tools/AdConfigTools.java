package third.ad.tools;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import com.bumptech.glide.request.animation.GlideAnimation;
import com.popdialog.util.FullScreenManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import acore.logic.LoginManager;
import acore.logic.XHClick;
import acore.override.XHApplication;
import acore.tools.FileManager;
import acore.tools.StringManager;
import acore.tools.ToolsDevice;
import amodule.main.Main;
import aplug.basic.InternetCallback;
import aplug.basic.LoadImage;
import aplug.basic.ReqInternet;
import aplug.basic.SubBitmapTarget;
import third.ad.AdParent;

public class AdConfigTools {
    private volatile static AdConfigTools mAdConfigTools;

    private String showAdId = "cancel";

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
        // 请求网络信息
        ReqInternet.in().doGet(StringManager.api_adData, new InternetCallback(XHApplication.in()) {
            @Override
            public void loaded(int flag, String url, final Object returnObj) {
                if (flag >= ReqInternet.REQ_OK_STRING) {
                    FileManager.saveFileToCompletePath(FileManager.getDataDir() + FileManager.file_ad, (String) returnObj, false);
                    Map<String, String> map = StringManager.getFirstMap(returnObj);
                    map = StringManager.getFirstMap(map.get(AdPlayIdConfig.FULLSCREEN));
                    map = StringManager.getFirstMap(map.get("banner"));
                    ArrayList<Map<String, String>> imgsList = StringManager.getListMapByJson(map.get("imgs"));
                    JSONArray array = new JSONArray();
                    JSONObject object = new JSONObject();
                    if (imgsList != null && imgsList.size() > 0) {
                        Map<String, String> imgsMap = imgsList.get(0);
                        try {
                            String indexImg3Str = imgsMap.get("indexImg3");
                            //判断 indexImg3 是否为 null
                            if (!TextUtils.isEmpty(indexImg3Str)) {
                                object.put("img", indexImg3Str);
                            } else {
                                //走以前的逻辑
                                DisplayMetrics dm = ToolsDevice.getWindowPx(Main.allMain);
                                float beishu = (float) (dm.heightPixels * 1.0) / dm.widthPixels;
                                if (Math.abs(beishu - (2900 / 1700)) > Math.abs(beishu - (2730 / 2000))) {
                                    object.put("img", imgsMap.get("indexImg2"));
                                } else {
                                    object.put("img", imgsMap.get("indexImg1"));
                                }
                            }

                            object.put("url", map.get("url"));
                            object.put("showNum", map.get("showNum"));
                            object.put("times", map.get("times"));
                            object.put("delay", map.get("delay"));
                            array.put(object);
                        } catch (Exception ignored) {
                            ignored.printStackTrace();
                        }
                        FullScreenManager.saveWelcomeInfo(context, array.toString(),
                                (imageUrl, callback) -> LoadImage.with(XHApplication.in())
                                        .load(imageUrl)
                                        .setSaveType(LoadImage.SAVE_LONG)
                                        .build()
                                        .into(new SubBitmapTarget() {
                                            @Override
                                            public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> arg1) {
                                                if (callback != null) {
                                                    callback.onAfterLoadImage(bitmap);
                                                }
                                            }
                                        }));
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
        ReqInternet.in().doGet(url, new InternetCallback(context) {
            @Override
            public void loaded(int flag, String url, Object msg) {
                if (flag >= ReqInternet.REQ_OK_STRING) {
                    list = StringManager.getListMapByJson(msg);
                }
            }
        });
    }

    public Map<String, String> getAdConfigData(String adPlayId) {
        String data = FileManager.readFile(FileManager.getDataDir() + FileManager.file_ad);
        Map<String, String> map = StringManager.getFirstMap(data);
        map = StringManager.getFirstMap(map.get(adPlayId));
        return map;
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

            Map<String, String> mData = getAdConfigData(adPlayId);
            if ("2".equals(mData.get(adKey))) {
                return true;
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
     * 普通广告位统计
     *
     * @param adPlayId ： 广告位id
     * @param channel  ：渠道 baidu、jingdong、banner
     * @param bannerId ：bannerId
     * @param event    事件  click：点击   show：展现
     * @param adType   广告类型  普通广告位，开屏广告位
     */
    public void postTongji(String adPlayId, String channel, String bannerId, String event, String adType) {
        StringBuffer urlBuffer = new StringBuffer(StringManager.api_monitoring_5)
                .append("?").append("adType=").append(adType)
                .append("&").append("id=").append(adPlayId)
                .append("&").append("channel=").append(channel)
                .append("&").append("bannerId=").append(bannerId)
                .append("&").append("event=").append(event);
        ReqInternet.in().doGet(urlBuffer.toString(), new InternetCallback(XHApplication.in()) {
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
    public void postTongjiQuan(Context context, Map<String, String> map, String onClickSite, String event) {
        String url = StringManager.api_monitoring_5;
        if (TextUtils.isEmpty(onClickSite)) onClickSite = "overall";
        else if ("用户头像".equals(onClickSite)) {
            onClickSite = "user";
        } else if ("用户昵称".equals(onClickSite)) {
            onClickSite = "user";
        } else if ("贴子内容".equals(onClickSite)) {
            onClickSite = "overall";
        } else if ("评论".equals(onClickSite)) {
            onClickSite = "content";
        } else {
            onClickSite = "overall";
        }

        ReqInternet.in().doGet(url + "?adType=圈子广告位" + "&adid=" + map.get("showAdid") + "&cid=" + map.get("showCid") +
                "&mid=" + map.get("showMid") + "site=" + map.get("showSite") + "&event=" + event + "&clickSite=" + onClickSite, new InternetCallback(context) {
            @Override
            public void loaded(int flag, String url, Object msg) {
            }
        });
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
        ReqInternet.in().doPost(StringManager.api_clickAds, map, new InternetCallback(XHApplication.in()) {
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
        ReqInternet.in().doPost(StringManager.api_clickAds, map, new InternetCallback(XHApplication.in()) {
            @Override
            public void loaded(int flag, String url, Object returnObj) {

            }
        });
    }
}
