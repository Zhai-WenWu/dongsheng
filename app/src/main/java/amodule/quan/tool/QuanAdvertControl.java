package amodule.quan.tool;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import acore.logic.LoginManager;
import acore.tools.StringManager;
import acore.tools.SyntaxTools;
import amodule.quan.adapter.AdapterCircle;
import third.ad.scrollerAd.XHAllAdControl;
import third.ad.scrollerAd.XHScrollerAdParent;
import third.ad.tools.AdConfigTools;
import third.ad.tools.AdPlayIdConfig;

/**
 * 美食圈数据控制
 * --单例模式
 *
 * @author yujian
 */
public class QuanAdvertControl {
    public static final String[] AD_IDS = new String[]{
            AdPlayIdConfig.SUBJECT_LIST_1,
            AdPlayIdConfig.SUBJECT_LIST_2,
            AdPlayIdConfig.SUBJECT_LIST_3,
            AdPlayIdConfig.SUBJECT_LIST_4,
            AdPlayIdConfig.SUBJECT_LIST_5,
            AdPlayIdConfig.SUBJECT_LIST_6};
    private ArrayList<Map<String, String>> mAdList = new ArrayList<Map<String, String>>();
    private ArrayList<Integer> ad_list = new ArrayList<Integer>();
    private DataCallBack callBack;
    private  XHAllAdControl xhAllAdControl;

    public void setCallBack(DataCallBack callBacks) {
        this.callBack = callBacks;
    }


    /**
     * 数据中加入广告数据
     *
     * @param old_list  原数据
     * @param cid       圈子id
     * @param mid       模块id
     * @param beforeNum 已经加入过的广告集合的数量
     * @return
     */
    public ArrayList<Map<String, String>> getAdvertAndQuanData(ArrayList<Map<String, String>> old_list, String cid, String mid, int beforeNum) {
        if (AdConfigTools.getInstance().list.size() > 0) {
            ArrayList<Map<String, String>> temp = new ArrayList<Map<String, String>>();
            for (int i = 0, size = AdConfigTools.getInstance().list.size(); i < size; i++) {
                Map<String, String> map = AdConfigTools.getInstance().list.get(i);
                map.put("dataType", String.valueOf(AdapterCircle.DATATYPE_SUBJECT));
                if (!map.containsKey("style")) {
                    map.put("style", String.valueOf(AdapterCircle.STYLE_NORMAL));
                }
                if (cid.equals(map.get("showCid")) && mid.equals(map.get("showMid"))) {
                    temp.add(map);
                }
                if (!map.containsKey("imgs")) map.put("imgs", map.get("img"));
            }
            if (temp.size() > 0) {
                for (int i = 0, size = temp.size(); i < size; i++) {
                    int index = Integer.parseInt(temp.get(i).get("showSite"));
                    index -= 1;
                    if (index >= beforeNum && index <= old_list.size()) {
                        old_list.add(index, temp.get(i));
                    }
                }
            }
        }
        String isGourmet = LoginManager.userInfo.get("isGourmet");
        //是美食家
        if (!TextUtils.isEmpty(isGourmet) && Integer.parseInt(isGourmet) == 2) {
            return old_list;
        }
        return getBdData(old_list, cid, mid, beforeNum);
    }


    private ArrayList<Map<String, String>> getBdData(ArrayList<Map<String, String>> old_list, String cid, String mid, int beforeNum) {
        logtzy("tzy", "showCid::" + cid + "::showMid::" + mid + ":::广告集合大小：：" + mAdList.size());
        if ("17".equals(mid)) {//活跃榜不显示广告
            return old_list;
        }
        if (ad_list.size() == 0) {
            ad_list.add(4);
        }
        if (mAdList.size() > 0) {
            ArrayList<Map<String, String>> temp = new ArrayList<>();
            for (int i = 0, size = mAdList.size(); i < size; i++) {
                Map<String, String> map = mAdList.get(i);
                if (map != null) {
                    map.put("showCid", cid);
                    map.put("showMid", mid);
                    temp.add(map);
                }
            }
            logtzy("tzy", "temp size:" + temp.size());
            if (temp.size() > 0) {
                for (int i = 0, size = temp.size(); i < size; i++) {
                    int index = ad_list.get(i);
                    logtzy("tzy", "index:" + index);
                    if (i == ad_list.size() - 1) {
                        ad_list.add(ad_list.get(i) + 10 + ad_list.size() - 1);
                    }
                    if (index >= beforeNum && index <= old_list.size()) {
                        old_list.add(index, temp.get(i));
                    }
                }
            }
        }
        return old_list;
    }

    /**
     * 获取到广告对把数据加到集合中
     *
     * @param index---数据角标位
     * @param title
     * @param desc
     * @param iconUrl
     * @param imageUrl
     * @param adstyle----广告是百度或广点通
     */
    private void setAdList(final String index, final String indexInData, final String title,
                           final String desc, final String iconUrl, String imageUrl,
                           final String adstyle,String adTag) {
        String url = "";
        if (!TextUtils.isEmpty(imageUrl)) {
            url = imageUrl;
        } else if (!TextUtils.isEmpty(iconUrl)) {
            url = iconUrl;
        }
        final String imgUrl = url;
        logtzy("tzy", "imageUrl url:" + url);
        logtzy("tzy", "iconUrl url:" + url);
        Map<String, String> map = new HashMap<>();
        map.put("title", "");
        map.put("hideAdTag", adTag);
        map.put("isPromotion", "1");
        map.put("adStyle", adstyle);
        map.put("promotionIndex", String.valueOf(index));
        map.put("indexInData", indexInData);
        map.put("content", desc);
        map.put("timeShow", "刚刚");
        map.put("commentNum", "");
        Random roll = new Random();
        int num = roll.nextInt(200 - 20);
        map.put("likeNum", String.valueOf(num + 20));
        map.put("isLike", "1");
        map.put("url", "");
        map.put("isShow", "false");
        map.put("code", "1");
        map.put("showSite", String.valueOf(index));
        map.put("dataType", String.valueOf(AdapterCircle.DATATYPE_SUBJECT));
        if (!map.containsKey("style")) {
            map.put("style", String.valueOf(AdapterCircle.STYLE_NORMAL));
        }
        JSONArray array = new JSONArray();
        array.put(imgUrl);
        map.put("imgs", array.toString());

        JSONArray customerArray = new JSONArray();
        JSONObject customerObj = new JSONObject();
        try {
            String su_title = title;
            if (!TextUtils.isEmpty(title) && title.length() > 10) {
                su_title = title.substring(0, 10);
            }
            customerObj.put("nickName", su_title);
            customerObj.put("img", iconUrl);
            customerObj.put("url", "");
            customerArray.put(customerObj);
            map.put("customer", customerArray.toString());
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        mAdList.add(map);

    }


    public void getAdData(final Context context) {
        mAdList.clear();
        ArrayList<String> adPosList = new ArrayList<>();

        for (String posStr : AD_IDS) {
            adPosList.add(posStr);
        }
        xhAllAdControl = new XHAllAdControl(adPosList, new XHAllAdControl.XHBackIdsDataCallBack() {

            @Override
            public void callBack(Map<String, String> map) {

                if (map != null && map.size() > 0) {
                    for (int i = 0; i < AD_IDS.length; i++) {
                        String homeAdStr = map.get(AD_IDS[i]);
                        ArrayList<Map<String, String>> homeList = StringManager.getListMapByJson(homeAdStr);
                        if(homeList!=null&&homeList.size()>0) {
                            Map<String, String> homeAdMap = homeList.get(0);
                            if (homeAdMap != null && homeAdMap.size() > 0) {
                                if (XHScrollerAdParent.ADKEY_BANNER.equals(homeAdMap.get("type"))) {
                                    setAdList((i + 1) + "", homeAdMap.get("index"), homeAdMap.get("title"), homeAdMap.get("desc"),
                                            homeAdMap.get("iconUrl"), homeAdMap.get("imgUrl2"), "ad", homeAdMap.get("adType"));
                                } else {
                                    setAdList((i + 1) + "", homeAdMap.get("index"), homeAdMap.get("title"), homeAdMap.get("desc"),
                                            homeAdMap.get("iconUrl"), homeAdMap.get("imgUrl"), "ad", homeAdMap.get("adType"));
                                }
                            }
                        }
                    }
                    if(mAdList.size()>0){
                        SyntaxTools.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                    callBack.dataBack();
                            }
                        });
                    }
                }
            }
        }, (Activity) context, "community_list");
    }

    public XHAllAdControl getXhAllAdControl(){
        return xhAllAdControl;
    }

    private void logtzy(String tag, String info) {
        Log.i(tag, info);
    }

    public interface DataCallBack {
        void dataBack();
    }
}
