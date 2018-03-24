package amodule.dish.tools;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import acore.tools.StringManager;
import amodule.quan.adapter.AdapterCircle;
import third.ad.scrollerAd.XHAllAdControl;
import third.ad.scrollerAd.XHScrollerAdParent;
import third.ad.tools.AdPlayIdConfig;

/**
 * Created by Administrator on 2016/8/16.
 */
public class ADDishContorl {
    public static final String[] AD_IDS = new String[]{AdPlayIdConfig.DISH_JINGXUAN_LISR_1,
            AdPlayIdConfig.DISH_JINGXUAN_LISR_2};
    private ArrayList<Map<String, String>> mAdList = new ArrayList<>();

    public XHAllAdControl xhAllAdControl;
    private int index_ad = 0;

    public ArrayList<Map<String, String>> getAdList(ArrayList<Map<String, String>> old_list) {
        if (mAdList.size() <= 0)
            return old_list;
        ArrayList<Map<String, String>> lists = old_list;
        if (lists.size() > 0 && lists.size() == 2) {
            lists.add(mAdList.get(0));
        } else if (lists.size() >= 3) {
            lists.add(2, mAdList.get(0));
        }
        if (lists.size() > 0 && lists.size() == 7 && mAdList.size() >= 2) {
            lists.add(mAdList.get(1));
        } else if (lists.size() >= 8 && mAdList.size() >= 2) {
            lists.add(7, mAdList.get(1));
        }
        return lists;
    }

    /**
     * 获取到广告对把数据加到集合中
     *
     * @param context---
     * @param index---数据角标位
     * @param title
     * @param desc
     * @param iconUrl
     * @param imageUrl
     * @param adstyle----广告是百度或广点通
     */
    private void setAdList(Context context, final String index, final String title, final String desc,
                           final String iconUrl, String imageUrl, final String imgs, final String hideAdTag,
                           final String adstyle) {
        String url = "";
        if (!TextUtils.isEmpty(imageUrl)) {
            url = imageUrl;
        } else if (!TextUtils.isEmpty(iconUrl)) {
            url = iconUrl;
        }
        final String imgUrl = url;
        Map<String, String> map = new HashMap<String, String>();
        map.put("title", "");
        map.put("isPromotion", "1");
        map.put("isShow", "false");
        map.put("adStyle", adstyle);
        map.put("promotionIndex", String.valueOf(index));
        map.put("indexAd", String.valueOf(Integer.parseInt(index)+1));
        map.put("content", desc);
        map.put("timeShow", "刚刚");
        map.put("hideAdTag", hideAdTag);
        map.put("commentNum", "");
        Random roll = new Random();
        int num = roll.nextInt(200 - 20);
        map.put("likeNum", String.valueOf(num + 20));
        map.put("isLike", "1");
        map.put("url", "");
        map.put("code", "1");
        map.put("showSite", String.valueOf(index));
        map.put("dataType", String.valueOf(AdapterCircle.DATATYPE_SUBJECT));
        if (!map.containsKey("style")) {
            map.put("style", String.valueOf(AdapterCircle.STYLE_NORMAL));
        }
        JSONArray array = new JSONArray();
        if (!TextUtils.isEmpty(imgs)) {
            ArrayList<Map<String, String>> imgList = StringManager.getListMapByJson(imgs);
            for (int i = 0; i < imgList.size(); i++) {
                Map<String, String> imgMap = imgList.get(i);
                String img = imgMap.get("");
                if (!TextUtils.isEmpty(img) && img.startsWith("http")) {
                    array.put(img);
                }
            }
        }
        if (array.length() < 1) {
            array.put(imgUrl);
        }
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
            e.printStackTrace();
        }

        mAdList.add(map);
    }


    public void getAdData(final Context context) {

        ArrayList<String> adPosList = new ArrayList<>();
        for (String posStr : AD_IDS) {
            adPosList.add(posStr);
        }
        xhAllAdControl = new XHAllAdControl(adPosList, new XHAllAdControl.XHBackIdsDataCallBack() {
            @Override
            public void callBack(boolean isRefresh,Map<String, String> map) {
                if (map != null && map.size() > 0) {
                    for (String adKey : AD_IDS) {
                        String adStr = map.get(adKey);
                        if (!TextUtils.isEmpty(adStr)) {
                            ArrayList<Map<String, String>> adList = StringManager.getListMapByJson(adStr);
                            if (adList != null && adList.size() > 0) {
                                Map<String, String> adDataMap = adList.get(0);

                                if (XHScrollerAdParent.ADKEY_BANNER.equals(adDataMap.get("type"))) {
                                    if (TextUtils.isEmpty(adDataMap.get("imgUrl2")))
                                        continue;
                                    adDataMap.put("imgUrl", adDataMap.get("imgUrl2"));
                                }
                                setAdList(context, adDataMap.get("index"), adDataMap.get("title"),
                                        adDataMap.get("desc"),
                                        adDataMap.get("iconUrl"),
                                        adDataMap.get("imgUrl"),
                                        adDataMap.get("imgs"), adDataMap.get("adType"), "ad");

                            }
                        }
                    }
                }
            }
        }, (Activity) context, "result_works",true);
        xhAllAdControl.registerRefreshCallback();
        //需要判断百度图片大小
        xhAllAdControl.setJudgePicSize(true);
    }

}

