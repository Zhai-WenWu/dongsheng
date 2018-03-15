package amodule.quan.tool;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import acore.logic.ActivityMethodManager;
import acore.logic.LoginManager;
import acore.tools.StringManager;
import acore.tools.SyntaxTools;
import amodule.quan.adapter.AdapterCircle;
import third.ad.scrollerAd.XHAllAdControl;
import third.ad.scrollerAd.XHScrollerAdParent;
import third.ad.tools.AdPlayIdConfig;

import static third.ad.control.AdControlNormalDish.tag_yu;

/**
 * 美食圈数据控制
 *
 *----------无限加载使用最简单的方式，直接保存当前的数据和对象，并对广告对象进行分别打tag，并管理
 * @author yujian
 */
public class QuanAdvertControl implements ActivityMethodManager.IAutoRefresh{
    public static final String[] AD_IDS = new String[]{
            AdPlayIdConfig.SUBJECT_LIST_1,
            AdPlayIdConfig.SUBJECT_LIST_2,
            AdPlayIdConfig.SUBJECT_LIST_3,
            AdPlayIdConfig.SUBJECT_LIST_4,
            AdPlayIdConfig.SUBJECT_LIST_5,
            AdPlayIdConfig.SUBJECT_LIST_6};
    private ArrayList<Map<String, String>> mAdList = new ArrayList<>();
    private ArrayList<Integer> ad_list = new ArrayList<Integer>();
    private DataCallBack callBack;
    private Map<String,XHAllAdControl> mapAd= new HashMap<>();//存储广告集合
    private int nowIndex=0;//当前使用对广告位置，
    private Context context;
    public void setCallBack(DataCallBack callBacks) {
        this.callBack = callBacks;
    }

    public QuanAdvertControl(Context context){
        this.context= context;
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
        String isGourmet = LoginManager.userInfo.get("isGourmet");
        //是美食家
        if (!TextUtils.isEmpty(isGourmet) && Integer.parseInt(isGourmet) == 2) {
            return old_list;
        }
        Log.i(tag_yu,"old_list.size::"+old_list.size()+"::cid:::"+cid+":mid::"+mid+"::beforeNum:"+beforeNum);
        return getBdData(old_list, cid, mid, beforeNum);
    }


    private ArrayList<Map<String, String>> getBdData(ArrayList<Map<String, String>> old_list, String cid, String mid, int beforeNum) {
        logtzy(tag_yu, "showCid::" + cid + "::showMid::" + mid + ":::广告集合大小：：" + mAdList.size());
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
            logtzy(tag_yu, "temp size:" + temp.size());
            if (temp.size() > 0) {
                for (int i = 0, size = temp.size(); i < size; i++) {
                    int index = ad_list.get(i);
                    logtzy(tag_yu, "index:" + index);
                    if (i == ad_list.size() - 1) {
                        ad_list.add(ad_list.get(i) + 10 + ad_list.size() - 1);
                    }
                    if (index >= beforeNum && index <= old_list.size()) {
                        nowIndex=i;
                        old_list.add(index, temp.get(i));
                        if(nowIndex<6&&nowIndex==4){
                            getAdData(context,"1");
                        }else if(nowIndex%6==4){
                            int num= nowIndex/6;
                            getAdData(context,String.valueOf(num+1));
                        }
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
                           final String adstyle,String adTag,String controlTag,String adType) {
        String url = "";
        if (!TextUtils.isEmpty(imageUrl)) {
            url = imageUrl;
        } else if (!TextUtils.isEmpty(iconUrl)) {
            url = iconUrl;
        }
        final String imgUrl = url;
        Map<String, String> map = new HashMap<>();
        map.put("controlTag",controlTag);
        map.put("title", "");
        map.put("hideAdTag", adTag);
        map.put("adType", adType);
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
            e.printStackTrace();
        }

        mAdList.add(map);

    }


    /**
     * 获取数据---第一次。
     * @param context
     */
    public void getAdData(final Context context) {
        mAdList.clear();
        mapAd.clear();
        getAdData(context,"0");
    }

    /**
     * 获取数据---第一次。
     * @param context
     */
    public void getAdData(final Context context, final String controlTag) {
        ArrayList<String> adPosList = new ArrayList<>();

        for (String posStr : AD_IDS) {
            adPosList.add(posStr);
        }
        XHAllAdControl xhAllAdControl = new XHAllAdControl(adPosList, new XHAllAdControl.XHBackIdsDataCallBack() {

            @Override
            public void callBack(boolean isRefresh,Map<String, String> map) {

                if (map != null && map.size() > 0) {
                    for (int i = 0; i < AD_IDS.length; i++) {
                        String homeAdStr = map.get(AD_IDS[i]);
                        ArrayList<Map<String, String>> homeList = StringManager.getListMapByJson(homeAdStr);
                        if(homeList!=null&&homeList.size()>0) {
                            Map<String, String> homeAdMap = homeList.get(0);
                            if (homeAdMap != null && homeAdMap.size() > 0) {
                                if (XHScrollerAdParent.ADKEY_BANNER.equals(homeAdMap.get("type"))) {
                                    setAdList((i + 1) + "", homeAdMap.get("index"), homeAdMap.get("title"), homeAdMap.get("desc"),
                                            homeAdMap.get("iconUrl"), homeAdMap.get("imgUrl2"), "ad", homeAdMap.get("adType"),controlTag,homeAdMap.get("type"));
                                } else {
                                    setAdList((i + 1) + "", homeAdMap.get("index"), homeAdMap.get("title"), homeAdMap.get("desc"),
                                            homeAdMap.get("iconUrl"), homeAdMap.get("imgUrl"), "ad", homeAdMap.get("adType"),controlTag,homeAdMap.get("type"));
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
        }, (Activity) context, "community_list",true);
        //需要判断百度图片大小
        xhAllAdControl.setJudgePicSize(true);
        mapAd.put(controlTag,xhAllAdControl);
    }
    private void logtzy(String tag, String info) {
        Log.i(tag, info);
    }

    @Override
    public void autoRefreshSelfAD() {

    }

    public interface DataCallBack {
        void dataBack();
    }

    /**
     * 广告的展示
     * @param controlTag
     * @param indexInData
     * @param view
     * @param promotionIndex
     */
    public void onAdBind(String controlTag,int indexInData,View view,String promotionIndex){
        if(!TextUtils.isEmpty(controlTag) && mapAd.get(controlTag) != null)
            mapAd.get(controlTag).onAdBind(indexInData,view,promotionIndex);
    }

    /**
     * 广告的点击
     * @param controlTag
     * @param view
     * @param indexInData
     * @param promotionIndex
     */
    public void onAdClick(String controlTag,View view,int indexInData,String promotionIndex){
        if(!TextUtils.isEmpty(controlTag) && mapAd.get(controlTag) != null)
            mapAd.get(controlTag).onAdClick(view,indexInData,promotionIndex);
    }

    /**
     * 获取xhallcontrol对象
     * @param controlTag
     * @return
     */
    public XHAllAdControl getXhAllAdControl(String controlTag){
        return mapAd.get(controlTag);
    }

    /**
     * 判断是否要刷新
     * @return
     */
    public boolean isNeedRefresh(){
        if(mapAd.size()>0){
            return mapAd.get("0").isNeedRefersh();
        }
        return false;
    }

    /**
     * 刷新
     */
    public void refreshData(){
        getAdData(context);
    }
}
