package third.ad.control;

import android.app.Activity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.annimon.stream.Stream;
import com.annimon.stream.function.Consumer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import acore.logic.ActivityMethodManager;
import acore.override.helper.XHActivityManager;
import third.ad.option.AdOptionHomeDish;
import third.ad.option.AdOptionParent;
import third.ad.tools.AdPlayIdConfig;

/**
 *  feed流页面的，广告特殊逻辑，分为三部分
 * 1、第一次进入两个数据---提高加载速度
 * 2、向下加载广告----固定广告位，固定广告数量
 * 3、向上不断翻页----整个广告位体循环请求。
 * 刷新策略：
 * 1、以第一个广告的加载时间为标准，一个过期全部过期。
 */
public class AdControlHomeDish extends AdControlParent implements ActivityMethodManager.IAutoRefresh{
    public static String tag_yu="zyj";
    private static volatile AdControlHomeDish mAdControlHomeDishUnload;
    public static String Control_up="up";
    public static String Control_down="down";


    private String statisticKey = "index_listgood";
    //推荐页面向下加载时的广告
    private static final Integer[] AD_INSTERT_INDEX_0 = new Integer[]{3, 9};
    //推荐页面向上加载时的广告
    private static final Integer[] AD_INSTERT_INDEX = new Integer[]{3, 9, 16, 24, 32, 40, 48, 56, 64, 72};

    private Map<Integer,AdOptionHomeDish> adControlMap; //广告控制集合
    private Map<Integer,AdOptionHomeDish> downAdControlMap; //down广告控制集合
    private int currentControlTag = -1;
    private int adControlNum = -1;
    private int nextAdNum = 0;
    //向下加载的tag
    private int downCurrentControlTag=0;
    private int downadControlNum = 0;
    private int downNextAdNum = 0;
    private Map<String,String> downAdState=new HashMap<>();//存储广告请求当前状态
    private Map<String,String> downAd=new HashMap<>();//是否要请求下一个数据块。

    private AdControlHomeDish(){
        adControlMap = new HashMap<>();
        downAdControlMap= new HashMap<>();
//        AdOptionHomeDish downLoadAdControl0 = new AdOptionHomeDish(AdPlayIdConfig.MAIN_HOME_RECOMENT_LIST_0);
//        downLoadAdControl0.newRunableGetAdData(XHActivityManager.getInstance().getCurrentActivity(),statisticKey,"0",Control_down);
//        downAdControlMap.put(0,downLoadAdControl0);
//        AdOptionHomeDish downLoadAdControl1 = new AdOptionHomeDish(AdPlayIdConfig.MAIN_HOME_RECOMENT_LIST);
//        downLoadAdControl1.newRunableGetAdData(XHActivityManager.getInstance().getCurrentActivity(),statisticKey,"1",Control_down);
//        downAdControlMap.put(1,downLoadAdControl1);


        Log.i(tag_yu,"首页加载数据");
    }

    public static AdControlHomeDish getInstance(){
        if(mAdControlHomeDishUnload == null){
            mAdControlHomeDishUnload = new AdControlHomeDish();
        }
        return mAdControlHomeDishUnload;
    }

    /**
     * 第二次加载广告数据
     */
    public AdControlHomeDish getTwoLoadAdData(){
        AdOptionHomeDish downLoadAdControl1 = new AdOptionHomeDish(AdPlayIdConfig.MAIN_HOME_RECOMENT_LIST);
        downLoadAdControl1.newRunableGetAdData(XHActivityManager.getInstance().getCurrentActivity(),statisticKey,"0",Control_down);
        downAdControlMap.put(0,downLoadAdControl1);

        AdOptionHomeDish adControlParent = new AdOptionHomeDish(AdPlayIdConfig.MAIN_HOME_RECOMENT_LIST);
        adControlParent.newRunableGetAdData(XHActivityManager.getInstance().getCurrentActivity(),statisticKey,String.valueOf(++adControlNum),Control_up);
        adControlMap.put(adControlNum,adControlParent);
        return mAdControlHomeDishUnload;
    }

    public ArrayList<Map<String, String>> getAutoRefreshAdData(ArrayList<Map<String, String>> old_list) {
        final AdOptionHomeDish adOptionHomeDish = getCurrentControl(false);
        if(adOptionHomeDish == null){
            return old_list;
        }else{
            Log.i(tag_yu,"getLimitNum()::"+getLimitNum());
            adOptionHomeDish.setLimitNum(getLimitNum());
            if(downCurrentControlTag>1)
                adOptionHomeDish.setStartIndex(getIndexAd((downCurrentControlTag-1)*10));
            adOptionHomeDish.setAdLoadNumberCallBack(new AdOptionParent.AdLoadNumberCallBack() {
                @Override
                public void loadNumberCallBack(int Number) {
                    Log.i(tag_yu,"*********Number****************:::"+Number+":::::tag::"+adOptionHomeDish.getControlTag());
                    String tag=adOptionHomeDish.getControlTag();
                    if(!TextUtils.isEmpty(tag)) {
                        int tagIndex= Integer.parseInt(tag);
                        if (adLoadNumberCallBack != null&&downAdState.containsKey(String.valueOf(tagIndex+1))) {
                            adLoadNumberCallBack.loadNumberCallBack(Number);
                        }
                        downAd.put(String.valueOf(tagIndex),String.valueOf(Number));
                    }
                }
            });
            old_list = adOptionHomeDish.getNewAdData(old_list,false);
        }
        return old_list;
    }
    /**
     * 加载g广告数据
     * @param old_list ：原数据体
     * @param isBack ：是否是向上加载的数据
     * @return
     */
    @Override
    public ArrayList<Map<String, String>> getNewAdData(ArrayList<Map<String, String>> old_list,boolean isBack) {
        //向上加载数据,则循环找到广告
        final AdOptionHomeDish adOptionHomeDish = getCurrentControl(isBack);
        if(adOptionHomeDish == null){
            return old_list;
        }else{
            Log.i(tag_yu,"getLimitNum()::"+getLimitNum());
            if(getLimitNum()>0&&!isBack)
                adOptionHomeDish.setLimitNum(getLimitNum());
           if(!isBack) {
               if(downCurrentControlTag>1)
                adOptionHomeDish.setStartIndex(getIndexAd((downCurrentControlTag-1)*10));
           }
            adOptionHomeDish.setAdLoadNumberCallBack(new AdOptionParent.AdLoadNumberCallBack() {
                @Override
                public void loadNumberCallBack(int Number) {
                    Log.i(tag_yu,"*********Number****************:::"+Number+":::::tag::"+adOptionHomeDish.getControlTag());
                    String tag=adOptionHomeDish.getControlTag();
                    if(!TextUtils.isEmpty(tag)) {
                        int tagIndex= Integer.parseInt(tag);
                        if (adLoadNumberCallBack != null&&downAdState.containsKey(String.valueOf(tagIndex+1))) {
                            adLoadNumberCallBack.loadNumberCallBack(Number);
                        }
                        downAd.put(String.valueOf(tagIndex),String.valueOf(Number));
                    }
                }
            });
            old_list = adOptionHomeDish.getNewAdData(old_list,isBack);

            Log.i(tag_yu,"预加载 控制类**************************:" + isBack+"：："+adOptionHomeDish.getIsLoadNext()+"：：："+downCurrentControlTag+":::"+downAdControlMap.size()+":::"+downNextAdNum);
            //判断是否需要提前加载广告数据
            if(isBack &&  adOptionHomeDish.getIsLoadNext() && currentControlTag > adControlMap.size() - nextAdNum){//向上加载数据
                AdOptionHomeDish adControl = new AdOptionHomeDish(AdPlayIdConfig.MAIN_HOME_RECOMENT_LIST);
                adControl.getAdData(XHActivityManager.getInstance().getCurrentActivity(),statisticKey,String.valueOf(++adControlNum),Control_up);
                adControlMap.put(adControlNum,adControl);
                Log.i(tag_yu,"up预加载 控制类:" + currentControlTag);
            }else if(!isBack &&adOptionHomeDish.getIsLoadNext()&&downCurrentControlTag>=downAdControlMap.size()-downNextAdNum-1){//向下翻页。
                AdOptionHomeDish adControl = new AdOptionHomeDish(AdPlayIdConfig.MAIN_HOME_RECOMENT_LIST);
                adControl.getAdData(XHActivityManager.getInstance().getCurrentActivity(),statisticKey,String.valueOf(++downadControlNum),Control_down);
                adControl.setAdDataCallBack(new AdOptionParent.AdDataCallBack() {
                    @Override
                    public void adDataBack(int tag, int nums) {
                        downAdState.put(String.valueOf(tag),String.valueOf(nums));
                        if (adLoadNumberCallBack != null&&downAd.containsKey(String.valueOf(tag))) {
                            adLoadNumberCallBack.loadNumberCallBack(Integer.parseInt(downAd.get(String.valueOf(tag))));
                        }
                    }
                });
                downAdControlMap.put(downadControlNum,adControl);
                Log.i(tag_yu,"down*********************预加载 控制类:" + downCurrentControlTag+"*********"+downadControlNum);
            }
            return old_list;
        }
    }

    private AdOptionHomeDish getCurrentControl(boolean isBack){
        AdOptionHomeDish adOptionHomeDish = null;

        if(isBack) {
            Log.i(tag_yu, "up这个控制adControlMap::"+adControlMap.size()+"，切换下一个currentControlTag :" + currentControlTag);
            if (adControlMap.size() > nextAdNum) {
                if (currentControlTag < nextAdNum) {
                    currentControlTag = nextAdNum;
                }
                if (currentControlTag < adControlMap.size()) {
                    adOptionHomeDish = adControlMap.get(currentControlTag);
                    if (adOptionHomeDish != null && !adOptionHomeDish.getIsHasNewData()) {
                        currentControlTag++;
                        Log.i(tag_yu, "up这个控制类没有了数据，切换下一个currentControlTag :" + currentControlTag);
                        adOptionHomeDish = getCurrentControl(isBack);
                    }
                }
            }
        }else{
            Log.i(tag_yu, "down **********************************这个控制类制adControlMap::"+downAdControlMap.size()+"一个currentControlTag :" + downCurrentControlTag);
            if(downAdControlMap.size()>downNextAdNum){
                if(downCurrentControlTag<downNextAdNum){
                    downCurrentControlTag= downNextAdNum;
                }
                if(downCurrentControlTag<downAdControlMap.size()){
                    adOptionHomeDish = downAdControlMap.get(downCurrentControlTag);
                    if (adOptionHomeDish != null && !adOptionHomeDish.getIsHasNewData()) {
                        downCurrentControlTag++;
                        Log.i(tag_yu, "down *******************************这个控制类没有了数据，切换下一个currentControlTag :" + downCurrentControlTag);
                        adOptionHomeDish = getCurrentControl(isBack);
                    }
                }
            }
        }
        return adOptionHomeDish;
    }

    @Override
    public void onAdClick(Map<String, String> map) {
        String controlTag = map.get("controlTag");
        String controlState= map.get("controlState");
        if(TextUtils.isEmpty(controlTag)){
            return;
        }
        Log.i(tag_yu,"onAdHintClick::"+controlState+" controlTag:" + controlTag);
        if(!TextUtils.isEmpty(controlState)&&Control_up.equals(controlState)){//上刷新
            AdOptionHomeDish adOptionHomeDish = adControlMap.get(Integer.parseInt(controlTag));
            adOptionHomeDish.onAdClick(map);
        }else if(!TextUtils.isEmpty(controlState)&&Control_down.equals(controlState)){//下加载
            AdOptionHomeDish adOptionHomeDish = downAdControlMap.get(Integer.parseInt(controlTag));
            adOptionHomeDish.onAdClick(map);
        }
    }

    @Override
    public void onAdHintClick(Activity act, Map<String, String> map, String eventID, String twoLevel) {
        String controlTag = map.get("controlTag");
        String controlState= map.get("controlState");
        if(TextUtils.isEmpty(controlTag)){
            return;
        }
        Log.i(tag_yu,"onAdHintClick::"+controlState+" controlTag:" + controlTag);
        if(!TextUtils.isEmpty(controlState)&&Control_up.equals(controlState)){//上刷新
            AdOptionHomeDish adOptionHomeDish = adControlMap.get(Integer.parseInt(controlTag));
            adOptionHomeDish.onAdHintClick(act,map,eventID,twoLevel);
        }else if(!TextUtils.isEmpty(controlState)&&Control_down.equals(controlState)){//下加载
            AdOptionHomeDish adOptionHomeDish = downAdControlMap.get(Integer.parseInt(controlTag));
            adOptionHomeDish.onAdHintClick(act,map,eventID,twoLevel);
        }
    }

    @Override
    public void onAdShow(Map<String, String> map, View view) {
        String controlTag = map.get("controlTag");
        String controlState= map.get("controlState");
        if(TextUtils.isEmpty(controlTag)){
            return;
        }
        Log.i(tag_yu,"onAdShow::"+controlState+" controlTag:" + controlTag);
        if(!TextUtils.isEmpty(controlState)&&Control_up.equals(controlState)){//上刷新
            AdOptionHomeDish adOptionHomeDish = adControlMap.get(Integer.parseInt(controlTag));
            adOptionHomeDish.onAdShow(map,view);
        }else if(!TextUtils.isEmpty(controlState)&&Control_down.equals(controlState)){//下加载
            AdOptionHomeDish adOptionHomeDish = downAdControlMap.get(Integer.parseInt(controlTag));
            adOptionHomeDish.onAdShow(map,view);
        }
    }

    @Override
    public boolean isNeedRefresh() {
        AdOptionHomeDish adOptionHomeDish = null;
        if(downAdControlMap.size()>0) {
            adOptionHomeDish=downAdControlMap.get(0);
            if (adOptionHomeDish != null) {
                return adOptionHomeDish.isNeedRefresh();
            }
        }
        return false;
    }

    @Override
    public void refreshData() {
        Log.i(tag_yu,"刷新数据::refreshData");
        //刷新数据
        adControlMap.clear();
        downAdControlMap.clear();
        //重至数据
        currentControlTag = -1;
        adControlNum = -1;
        nextAdNum = 0;
        //向下加载的tag
        downCurrentControlTag=0;
        downadControlNum = 0;
        downNextAdNum = 0;
//        AdOptionHomeDish downLoadAdControl0 = new AdOptionHomeDish(AdPlayIdConfig.MAIN_HOME_RECOMENT_LIST_0);
//        downLoadAdControl0.getAdData(XHActivityManager.getInstance().getCurrentActivity(),statisticKey,"0",Control_down);
//        downAdControlMap.put(0,downLoadAdControl0);
//        downLoadAdControl0.setAdDataCallBack(new AdOptionParent.AdDataCallBack() {
//            @Override
//            public void adDataBack(int tag, int nums) {
//                Log.i(tag_yu,"刷新数据::tag：：："+tag+"：：："+nums);
//            }
//        });

        AdOptionHomeDish downLoadAdControl1 = new AdOptionHomeDish(AdPlayIdConfig.MAIN_HOME_RECOMENT_LIST);
        downLoadAdControl1.getAdData(XHActivityManager.getInstance().getCurrentActivity(),statisticKey,"0",Control_down);
        downAdControlMap.put(0,downLoadAdControl1);
        downLoadAdControl1.setAdDataCallBack(new AdOptionParent.AdDataCallBack() {
            @Override
            public void adDataBack(int tag, int nums) {

                Log.i(tag_yu,"*****____________________________________");
                downAdState.put(String.valueOf(tag),String.valueOf(nums));
                if (adLoadNumberCallBack != null&&downAd.containsKey(String.valueOf(tag))) {
                    adLoadNumberCallBack.loadNumberCallBack(Integer.parseInt(downAd.get(String.valueOf(tag))));
                }
                if(adDataCallBack!=null)adDataCallBack.adDataBack(1,nums);
                Log.i(tag_yu,"刷新数据::tag：：："+tag+"：：："+nums);
            }
        });

        AdOptionHomeDish adControlParent = new AdOptionHomeDish(AdPlayIdConfig.MAIN_HOME_RECOMENT_LIST);
        adControlParent.getAdData(XHActivityManager.getInstance().getCurrentActivity(),statisticKey,String.valueOf(++adControlNum),Control_up);
        adControlMap.put(adControlNum,adControlParent);
    }

    public AdOptionParent.AdLoadNumberCallBack adLoadNumberCallBack;
    public void setAdLoadNumberCallBack(AdOptionParent.AdLoadNumberCallBack adLoadNumberCallBack){
        this.adLoadNumberCallBack=adLoadNumberCallBack;
    }

    @Override
    public void autoRefreshSelfAD() {
        refreshSelfAd(0,downAdControlMap);
    }

    private synchronized void refreshSelfAd(final int index,Map<Integer,AdOptionHomeDish> adControlMap) {
        if(adControlMap != null
                && !adControlMap.isEmpty()
                && index < adControlMap.size()){
            AdOptionHomeDish adOptionHomeDish = adControlMap.get(index);
            if(adOptionHomeDish != null){
                adOptionHomeDish.setRefreshCallback(new ActivityMethodManager.IAutoRefreshCallback() {
                    @Override
                    public void refreshSelfAD() {
                        //刷新
                        autoRefreshCallback();
                        //执行下一个刷新
                        int nextIndex = index;
                        nextIndex++;
                        refreshSelfAd(nextIndex,adControlMap);
                    }
                });
                adOptionHomeDish.autoRefreshSelfAD();
            }
        }
    }

}