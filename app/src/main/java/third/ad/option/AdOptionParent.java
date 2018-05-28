package third.ad.option;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import acore.logic.ActivityMethodManager;
import acore.logic.AppCommon;
import acore.tools.StringManager;
import third.ad.scrollerAd.XHAllAdControl;
import third.ad.scrollerAd.XHScrollerAdParent;

import static third.ad.control.AdControlHomeDish.tag_yu;
import static third.ad.scrollerAd.XHScrollerSelf.IMG_KEY;

/**
 * 广告控制类---广告真正去请求广告类，
 * 1、广告插入的真正节点，开始位：4。间隔6，7，8，依次递增，最大为间隔8数据提
 */
public abstract class AdOptionParent implements ActivityMethodManager.IAutoRefresh{
    private int limitNum = 0;//分界节点
    private final String[] AD_IDS;//广告ID的集合。

    protected XHAllAdControl xhAllAdControl;
    protected volatile ArrayList<Map<String, String>> adArray = new ArrayList<>();//广告数据的集合

    protected int cunrrentIndex = 0;
    private String statisticKey = "";
    private String controlTag = "";
    private int startIndex = 0;//开始角标位。

    public AdOptionParent(String[] adPlayIds) {
        AD_IDS = adPlayIds;

    }
//*******************************请求广告start*****************************************

    /**
     * 子线程去请求数据广告
     *
     * @param context
     * @param statisticKey
     * @param controlTag
     */
    public void newRunableGetAdData(final Context context, final String statisticKey, final String controlTag, final String controlState) {
        this.statisticKey = statisticKey;
        this.controlTag = controlTag;
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
                getAdData(context, statisticKey, controlTag, controlState);
//            }
//        }).start();
    }

    public void getAdData(final Context context, String statisticKey) {
        this.statisticKey = statisticKey;
        getAdData(context, statisticKey, "", "");
    }

    public void getAdData(Context context) {
        if (!TextUtils.isEmpty(statisticKey)) {
            getAdData(context, statisticKey, "", "");
        }
    }

    public synchronized void getAdData(final Context context, final String statisticKey, final String controlTag, final String controlState) {
        Log.i(tag_yu, "开始获取  广告  数据-------------:" + controlTag);
        this.statisticKey = statisticKey;
        this.controlTag = controlTag;
        adArray.clear();
        ArrayList<String> adPosList = new ArrayList<>();
        adPosList.addAll(Arrays.asList(AD_IDS));
        xhAllAdControl = new XHAllAdControl(adPosList, new XHAllAdControl.XHBackIdsDataCallBack() {
            @Override
            public void callBack(boolean isRefresh,Map<String, String> map) {
                if (map != null && map.size() > 0) {
                    adArray.clear();
                    Log("getAdData size:" + map.size());
                    for (int i = 0; i < AD_IDS.length; i++) {
                        String homeAdStr = map.get(AD_IDS[i]);
                        Log("ad——ids:" + AD_IDS[i]);
                        if (!TextUtils.isEmpty(homeAdStr)) {
                            ArrayList<Map<String, String>> adList = StringManager.getListMapByJson(homeAdStr);
                            Map<String, String> adMap = adList.get(0);
                            if (adMap != null && adMap.size() > 0) {
                                //广告为自己的类型
                                if (XHScrollerAdParent.ADKEY_BANNER.equals(adMap.get("adClass"))
                                        && !TextUtils.isEmpty(adMap.get("imgUrl2"))) {
                                    adMap.put("imgUrl", adMap.get("imgUrl2"));
                                }
                                Map<String, String> newMap = getAdListItemData(
                                        adMap.get("title"),
                                        adMap.get("desc"),
                                        adMap.get("iconUrl"),
                                        adMap.get("imgUrl"),
                                        adMap.get("type"),
                                        adMap.containsKey("isBigPic") ? adMap.get("isBigPic") : ""
                                );
                                if (newMap != null) {
                                    if (!newMap.containsKey("adClass"))
                                        newMap.put("adClass", adMap.get("type"));

                                    if (adMap.containsKey("appImg")) {
                                        newMap.put("appImg", adMap.get("appImg"));
                                    }
                                    if (adMap.containsKey("imgUrl")) {
                                        newMap.put("imgUrl", adMap.get("imgUrl"));
                                    }
                                    if (adMap.containsKey(IMG_KEY)) {
                                        newMap.put(IMG_KEY, adMap.get(IMG_KEY));
                                    }
                                    newMap.put("adType", adMap.get("adType")); //自由广告时，1：活动 2:广告
                                    newMap.put("indexOnData", adMap.get("index")); //数据角标位
                                    newMap.put("index", String.valueOf(i + 1)); //在数据源中的位置
                                    newMap.put("adstyle", "ad");
                                    newMap.put("isShow", "1");
                                    newMap.put("controlTag", controlTag); //给当前广告Control添加标记
                                    newMap.put("controlState", controlState); //给当前广告Control,down 下加载,up 上刷新
                                    newMap.put("timeTag", String.valueOf(System.currentTimeMillis()));
                                    adArray.add(newMap);
                                    Log("adArray.add  ad——ids:" + AD_IDS[i]);
                                } else {
                                    Log("---------------广告位没有数据----------");
                                    newMap = new HashMap<>();
                                    adArray.add(newMap);
                                }
                            } else {
                                Log("---------------广告位没有数据----------");
                                Map<String, String> newMap = new HashMap<>();
                                adArray.add(newMap);
                            }
                        } else {
                            Log("---------------广告位没有数据----------");
                            Map<String, String> newMap = new HashMap<>();
                            adArray.add(newMap);
                        }

                    }
                    if (isRefresh) {
                        if (mRefreshCallback != null) {
                            mRefreshCallback.refreshSelfAD();
                        }
                    }
                    if (adDataCallBack != null) {
                        adDataCallBack.adDataBack(TextUtils.isEmpty(controlTag) ? -1 : Integer.parseInt(controlTag), map.size());
                    }
                }
            }
        }, (Activity) context, statisticKey,
                "sp_list".equals(statisticKey)
                        || "other_threeMeals_list".equals(statisticKey)
                        || "sc_list".equals(statisticKey)
                        || "jz_list".equals(statisticKey));
    }
    //*******************************请求广告end*****************************************

    public void refrush() {
        cunrrentIndex = 0;
    }

    @Override
    public void autoRefreshSelfAD() {
        if(xhAllAdControl != null){
            xhAllAdControl.autoRefreshSelfAD();
        }
    }

    /**
     * 对列表数据就是广告数据。
     *
     * @param old_list
     * @param isBack
     *
     * @return
     */
    public ArrayList<Map<String, String>> getNewAdData(ArrayList<Map<String, String>> old_list, boolean isBack) {
        Log("getNewAdData");
        //显示广告
        return getBdData(old_list, isBack);

    }

    /**
     * 真正对列表数据进行拼装数据
     *
     * @param old_list
     * @param isBack
     *
     * @return
     */
    protected ArrayList<Map<String, String>> getBdData(ArrayList<Map<String, String>> old_list,
                                                       boolean isBack) {
        Log("getBdData adArray.size():" + adArray.size());
        ArrayList<Map<String, String>> tempList = new ArrayList<>();
        Log.i(tag_yu, "getLimitNum::" + getLimitNum());
        if (!isBack && getLimitNum() > 0) {//listDatas，向下翻页
            int limitNum = getLimitNum();
            for (int index = 0; index < limitNum; index++) {
                tempList.add(old_list.get(index));
            }
            for (int index = 0; index < limitNum; index++) {
                old_list.remove(0);
            }
            Log.i(tag_yu, "节点数据为::" + old_list.get(0).get("name"));
        }
        //先移除广告
        int adPositionIndex = TextUtils.isEmpty(controlTag) ? 0 : Integer.parseInt(controlTag);
        final int currentAdPosition = getIndexAd((adPositionIndex + 1) * AD_IDS.length - 1);
        for (int i = startIndex;
             i < old_list.size()
                     && i <= currentAdPosition
//                     && old_list.size() > currentAdPosition
                ;
             i++) {
            Map<String, String> dataMap = old_list.get(i);
            if ("ad".equals(dataMap.get("adstyle"))) {
                Log.i("tzy", "getBdData: remove");
                old_list.remove(dataMap);
                i--;
            }
        }
        if (adArray.size() > 0) {
            Map<String, String> adMap;
            if (!isBack) {
                cunrrentIndex = 0;
            }
            //添加广告
            int showIndex = 0;
            for (int idIndex = 0, size = adArray.size(); cunrrentIndex < size; cunrrentIndex++, idIndex++) {
                //获取当前要插入对角标位
                int index = getIndexAd(idIndex);
                index += startIndex;
                Log.i(tag_yu, "：startIndex：：" + startIndex + "：：：index:::" + index + "::::" + old_list.size());
                if (index > 0 && index < old_list.size()) {
                    if (adArray.size() > 0) showIndex = index;
                    Map<String, String> dataMap = old_list.get(index);
                    String adstyle = isBack ? old_list.get(index - 1).get("adstyle") : dataMap.get("adstyle");
                    //判断此广告位是否添加广告，如果此广告位已添加广告，则不添加
                    adMap = adArray.get(cunrrentIndex);
                    boolean dataIsOk = getDataIsOk(adMap);
                    if (dataIsOk) {
                        handlerAdMap(old_list, adMap, index);
                        Log("ad controlTag:" + adMap.get("controlTag") + "    ad name:" + adMap.get("name") + "   style:" + adMap.get("style"));
                        if (!"ad".equals(adstyle)) {
                            if (!TextUtils.isEmpty(adMap.get("style"))){
                                old_list.add(index, adMap);
                            }
                        }
                    }
                } else {
                    break;
                }
            }
            if (adLoadNumberCallBack != null) {
                Log.i(tag_yu, "old_list.size()-showIndex:::::::::::" + (old_list.size() - showIndex));
                adLoadNumberCallBack.loadNumberCallBack(old_list.size() - showIndex);
            }
        }
        if (!isBack && tempList != null && tempList.size() > 0) {
            old_list.addAll(0, tempList);
        }

        return old_list;
    }

    private void handlerAdMap(ArrayList<Map<String, String>> old_list, Map<String, String> adMap, int index) {
        JSONArray styleData = new JSONArray();
        //腾讯api广告不用根据上一个item样式变;101:表示返回的是一张小图、202:一个大图、301:3张小图
        try {
            if (statisticKey.equals("index_listgood")
                    && XHScrollerAdParent.ADKEY_BAIDU.equals(adMap.get("adClass"))
                    && "1".equals(adMap.get("isBigPic"))) {
                JSONObject styleObject = new JSONObject();
                styleObject.put("url", adMap.get("img"));
                styleObject.put("type", "2");
                styleData.put(styleObject);
                adMap.put("style", "2");
            } else {
                int aboveIndex = index - 1; //广告要跟上一个样式保持一致
                if (aboveIndex < 0) aboveIndex = index;
                Map<String, String> aboveMap = old_list.get(aboveIndex);
                String type = aboveMap.get("style");
                if (TextUtils.isEmpty(type)) {//如果上一个样式的字段不存在则默认右图样式
                    JSONObject styleObject = new JSONObject();
                    styleObject.put("url", adMap.get("img"));
                    styleObject.put("type", "1");
                    styleData.put(styleObject);
                } else {
                    String adImg = adMap.get("img");
                    //对图片根据类型进行选择
                    String ImgKey = "";
                    if (!TextUtils.isEmpty(type) && ("1".equals(type) || "5".equals(type) || "6".equals(type))) {
                        ImgKey = "imgUrl";
                    } else {
                        ImgKey = IMG_KEY;
                    }
//                    Log.i("tzy", "ImgKey::**********************************" + ImgKey);
                    if (adMap.containsKey(ImgKey) && !TextUtils.isEmpty(adMap.get(ImgKey))) {
                        adImg = adMap.get(ImgKey);
//                        Log.i("tzy", "ImgKey::****************2******************" + adImg);
                        adMap.put("img", adImg);
                    }
                    ArrayList<Map<String, String>> imgsMap = StringManager.getListMapByJson(adMap.get("imgs"));
                    if (imgsMap != null && imgsMap.size() > 0) {
                        for (Map<String, String> imgMap : imgsMap) {
                            if (imgMap != null && imgMap.get("") != null) {
                                JSONObject styleObject = new JSONObject();
                                styleObject.put("url", imgMap.get(""));
                                styleObject.put("type", "1");
                                styleData.put(styleObject);
                            }
                        }
                    } else {
                        JSONObject styleObject = new JSONObject();
                        styleObject.put("url", adImg);
                        styleObject.put("type", "1");
                        styleData.put(styleObject);
                    }
                    switch (type) {
                        case "1"://大图
                        case "5"://蒙版
                        case "6"://任意图
                            adMap.put("style", TextUtils.isEmpty(adImg) && (imgsMap == null || imgsMap.isEmpty()) ? "4" : "1");
                            break;
//                                            case "2"://右图
//                                            case "3"://三图
//                                            case "4"://无图
                        default://除大图样式外，其余默认右图，如果没有图片则无图。
                            adMap.put("style", TextUtils.isEmpty(adImg) && (imgsMap == null || imgsMap.isEmpty()) ? "4" : "2");
                            break;
                    }
                }
            }
        } catch (JSONException e) {

        }
        adMap.put("styleData", styleData.toString());
    }

    /**
     * 判断广告数据是不是已经加载完毕
     */
    public boolean getHasData() {
        return adArray.size() > 0;
    }

    /**
     * 判断当前广告是否有效
     *
     * @param dataMap
     *
     * @return
     */
    private boolean getDataIsOk(Map<String, String> dataMap) {
        if (dataMap == null || dataMap.size() < 3) return false;
        String timeTag = dataMap.get("timeTag");
        if (TextUtils.isEmpty(timeTag) || System.currentTimeMillis() - Long.parseLong(timeTag) < 30 * 60 * 1000) {
            return true;
        }
        return false;
    }

    public boolean getIsLoadNext() {
        Log("判断是否需要预加载 ::" + cunrrentIndex + "::::" + adArray.size());
        boolean flag = adArray.size() > 0 && cunrrentIndex > adArray.size() - 2;
        Log("判断是否需要预加载 " + flag);
        return flag;
    }

    public boolean getIsHasNewData() {
        Log("cunrrentIndex::" + cunrrentIndex + "::::adArray.size():::" + adArray.size());
        if (adArray.size() <= 0) return true;
        return cunrrentIndex < adArray.size() - 1;
    }

    public void onAdShow(Map<String, String> map, View view) {
        Log("--------------onAdShow----------------");
        xhAllAdControl.onAdBind(Integer.parseInt(map.get("indexOnData")), view, map.get("index"));
    }

    public void onAdClick(Map<String, String> map) {
        Log("onAdClick() imgs:" + map.get("imgs"));
        Log("onAdClick() indexOnData:" + map.get("indexOnData") + "   index:" + map.get("index") + "  adClass:" + map.get("adClass") + "   type:" + map.get("type"));
        if (map != null)
            xhAllAdControl.onAdClick(Integer.parseInt(map.get("indexOnData")), map.get("index"));
    }

    public void onAdHintClick(Activity act, Map<String, String> map, final String eventID, final String twoLevel) {
        Log("onAdHintClick");
        if (map != null)
            AppCommon.onAdHintClick(act, xhAllAdControl, Integer.parseInt(map.get("indexOnData")), map.get("index"), eventID, twoLevel);
    }

    public void Log(String content) {
        Log.i(tag_yu, content);
    }

    /**
     * 把广告数据组装成各自ListView想要的key，value形式
     *
     * @param title
     * @param desc
     * @param iconUrl
     * @param imageUrl
     * @param adTag----广告类型：百度、广点通、自由
     * @param isBigPic-----是否是下载类型（暂时只有百度添加此参数）
     */
    public abstract Map<String, String> getAdListItemData(final String title, final String desc, final String iconUrl, String imageUrl, String adTag, String isBigPic);

    public int getLimitNum() {
        return limitNum;
    }

    public void setLimitNum(int limitNum) {
        this.limitNum = limitNum;
    }

    /**
     * 获取当前广告是否需要的刷新
     *
     * @return
     */
    public boolean isNeedRefresh() {
        if (xhAllAdControl != null) {
            return xhAllAdControl.isNeedRefersh();
        }
        return false;
    }

    private ActivityMethodManager.IAutoRefreshCallback mRefreshCallback;

    private AdDataCallBack adDataCallBack;

    public void setAdDataCallBack(AdDataCallBack adDataCallBack) {
        this.adDataCallBack = adDataCallBack;
    }

    public void setRefreshCallback(ActivityMethodManager.IAutoRefreshCallback refreshCallback) {
        mRefreshCallback = refreshCallback;
    }

    /**
     * 广告数据回来后回调
     */
    public interface AdDataCallBack {
        public void adDataBack(int tag, int nums);
    }

    /**
     * 获取当前要插入对广告位
     *
     * @param index
     *
     * @return
     */
    public int getIndexAd(int index) {
        int AdIndexStart = 3;//开始角标位
        int endIndex = 0;
        if (startIndex >= 17) {
            endIndex = index * 8;
        } else if (startIndex >= 10) {
            if (index <= 1) {
                if (index == 0) endIndex = 0;
                else endIndex = 7;
            } else {
                endIndex = 7 + (index - 1) * 8;
            }
        } else {
            if (index <= 2) {//0,1,2
                if (index == 0) endIndex = AdIndexStart;
                else if (index >= 1) endIndex = 3 + 6 + (index - 1) * 7;
            } else {//3,4,5,6,7
                endIndex = 3 + 6 + 7 + (index - 2) * 8;
            }
        }


        return endIndex;
    }

    public void setStartIndex(int index) {
        this.startIndex = index;
    }

    /**
     * 广告加载位置的回调。
     */
    public interface AdLoadNumberCallBack {
        public void loadNumberCallBack(int Number);
    }

    public AdLoadNumberCallBack adLoadNumberCallBack;

    public void setAdLoadNumberCallBack(AdLoadNumberCallBack adLoadNumberCallBack) {
        this.adLoadNumberCallBack = adLoadNumberCallBack;
    }

    public String getControlTag() {
        return controlTag;
    }
}
