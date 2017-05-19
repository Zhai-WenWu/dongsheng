package third.ad.option;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.LoginManager;
import acore.tools.StringManager;
import third.ad.scrollerAd.XHAllAdControl;
import third.ad.scrollerAd.XHScrollerAdParent;

/**
 * 广告控制类
 * Created by Fang Ruijiao on 2017/4/24.
 */
public abstract class AdOptionParent {

    private final String[] AD_IDS;
    /** 广告插入到数据的位置集合 * */
    protected ArrayList<Integer> adIdList;

    protected XHAllAdControl xhAllAdControl;
    protected ArrayList<Map<String, String>> adArray = new ArrayList<>();

    protected int cunrrentIndex = 0;

    public AdOptionParent(String[] adPlayIds, Integer[] adIndexs){
        AD_IDS = adPlayIds;
        adIdList = new ArrayList();
        for(Integer index : adIndexs) {
            adIdList.add(index);
        }
    }

    public void getAdData(final Context context, String statisticKey) {
        getAdData(context,statisticKey,"");
    }

    public void getAdData(final Context context, final String statisticKey, final String controlTag) {
        Log.i("FRJ","开始获取  广告  数据-------------:" + controlTag);
        adArray.clear();
        //子线程中执行
        new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList<String> adPosList = new ArrayList<>();
                for (String posStr : AD_IDS) {
                    adPosList.add(posStr);
                }
                xhAllAdControl = new XHAllAdControl(adPosList, new XHAllAdControl.XHBackIdsDataCallBack() {
                    @Override
                    public void callBack(Map<String, String> map) {
                        if (map != null && map.size() > 0) {
                            Log("getAdData size:" + map.size());
                            for (int i = 0; i < AD_IDS.length; i++) {
                                String homeAdStr = map.get(AD_IDS[i]);
                                Log("ad——ids:" + AD_IDS[i]);
                                if(!TextUtils.isEmpty(homeAdStr)) {
                                    ArrayList<Map<String, String>> adList = StringManager.getListMapByJson(homeAdStr);
                                    Map<String, String> adMap = adList.get(0);
                                    if (adMap != null && adMap.size() > 0) {
                                        //广告为自己的类型
                                        if(XHScrollerAdParent.ADKEY_BANNER.equals(adMap.get("adClass")) && !TextUtils.isEmpty(adMap.get("imgUrl2"))){
                                            adMap.put("imgUrl",adMap.get("imgUrl2"));
                                        }
                                        Map<String, String> newMap = getAdListItemData(adMap.get("title"), adMap.get("desc"),
                                                adMap.get("iconUrl"), adMap.get("imgUrl"), adMap.get("type"));
                                        if(newMap != null) {
                                            if(!newMap.containsKey("adClass")) newMap.put("adClass",adMap.get("type"));
                                            newMap.put("imgs", adMap.get("imgs")); //api广告图片集合
                                            newMap.put("adType", adMap.get("adType")); //自由广告时，1：活动 2:广告
                                            newMap.put("stype", adMap.get("stype")); //腾讯api广告的样式类型
                                            newMap.put("indexOnData", adMap.get("index")); //数据角标位
                                            newMap.put("index", String.valueOf(i + 1)); //在数据源中的位置
                                            newMap.put("adstyle", "ad");
                                            newMap.put("isShow", "1");
                                            newMap.put("controlTag",controlTag); //给当前广告Control添加标记
                                            newMap.put("timeTag",String.valueOf(System.currentTimeMillis()));
                                            adArray.add(newMap);
                                            Log("adArray.add  ad——ids:" + AD_IDS[i]);
                                        }
                                    }
                                }else{
                                    Log("---------------广告位没有数据----------");
                                    Map<String, String> newMap = new HashMap<>();
                                    adArray.add(newMap);
                                }
                            }
                        }
                    }
                }, (Activity) context,statisticKey);
            }
        }).start();

    }

    public void refrush(){
        cunrrentIndex = 0;
    }

    public void setIndexs(Integer[] adIndexs){
        adIdList = new ArrayList();
        for(Integer index : adIndexs) {
            adIdList.add(index);
        }
    }

    public ArrayList<Map<String, String>> getNewAdData(ArrayList<Map<String, String>> old_list,boolean isBack) {
        Log("getNewAdData");
        //显示广告
        return  LoginManager.isShowAd() ? getBdData(old_list,isBack) : old_list;

    }

    protected ArrayList<Map<String, String>> getBdData(ArrayList<Map<String, String>> old_list,boolean isBack) {
        Log("getBdData adArray.size():" + adArray.size());
        if (adArray.size() > 0 && adIdList.size() > 0) {
            Map<String, String> dataMap;
            int idIndex = 0;
            if(!isBack) cunrrentIndex = 0;
            for (int size = adArray.size(),idSize = adIdList.size(); cunrrentIndex < size && idIndex < idSize; cunrrentIndex++,idIndex++) {
                int index = adIdList.get(idIndex);
                if (index < old_list.size()) {
                    dataMap = old_list.get(index);
                    if(getDataIsOk(dataMap)) {
                        //判断此广告位是否添加广告，如果此广告位已添加广告，则不添加
                        if (!"ad".equals(dataMap.get("adstyle"))) {
                            Map<String, String> map = new HashMap<>();
                            map.putAll(adArray.get(cunrrentIndex));
                            old_list.add(index, map);
                            Log("add Ok");
                        }
                    }
                }else{
                    break;
                }
            }
        }
        Log("getBdData cunrrentIndex:" + cunrrentIndex);
        return old_list;
    }

    /**
     * 判断广告数据是不是已经加载完毕
     */
    public boolean getHasData(){
        return adArray.size() > 0;
    }

    public boolean getDataIsOk(Map<String, String> dataMap){
        if(dataMap.size() < 3) return false;
        String timeTag = dataMap.get("timeTag");
        if(TextUtils.isEmpty(timeTag) || System.currentTimeMillis() - Long.parseLong(timeTag) < 30 * 60 * 1000){
            return true;
        }
        return false;
    }

    public boolean getIsLoadNext(){
        boolean flag = adArray.size() > 0 && cunrrentIndex > adArray.size() - 2;
        Log("判断是否需要预加载 " + flag);
        return flag;
    }

    public boolean getIsHasNewData(){
        return cunrrentIndex < adArray.size();
    }

    public void onAdShow(Map<String, String> map, View view) {
        Log("onAdShow");
        if (!"2".equals(map.get("isShow"))) {
            Log("--------------onAdShow----------------");
            xhAllAdControl.onAdBind(Integer.parseInt(map.get("indexOnData")), view, map.get("index"));
            map.put("isShow", "2");
        }
    }

    public void onAdClick(Map<String, String> map) {
        Log("onAdClick() imgs:" + map.get("imgs"));
        Log("onAdClick() indexOnData:" + map.get("indexOnData") + "   index:" + map.get("index") + "  adClass:" + map.get("adClass") + "   stype:" + map.get("stype") + "   type:" + map.get("type"));
        xhAllAdControl.onAdClick(Integer.parseInt(map.get("indexOnData")), map.get("index"));
    }

    public void onAdHintClick(Activity act, Map<String, String> map,final String eventID, final String twoLevel){
        Log("onAdHintClick");
        AppCommon.onAdHintClick(act,xhAllAdControl,Integer.parseInt(map.get("indexOnData")), map.get("index"),eventID,twoLevel);
    }

    public void Log(String content){
        Log.i("FRJ",content);
    }

    /**
     * 把广告数据组装成各自ListView想要的key，value形式
     * @param title
     * @param desc
     * @param iconUrl
     * @param imageUrl
     * @param adTag----广告类型：百度、广点通、自由
     */
    public abstract Map<String, String> getAdListItemData(final String title, final String desc, final String iconUrl, String imageUrl, String adTag);
}
