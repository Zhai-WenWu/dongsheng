package third.ad.control;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import java.util.ArrayList;
import java.util.Map;

import acore.override.helper.XHActivityManager;
import third.ad.option.AdOptionParent;

/**
 * 业务层的控制类，用于加载广告。和刷新等业务上
 */
public class AdControlParent {

    private AdOptionParent mAdOptionParent;
    private int limitNum = 0;//分界点
    public AdControlParent(){
    }

    public AdControlParent(AdOptionParent adOptionParent){
        mAdOptionParent = adOptionParent;
    }

    public void getAdData(Context context,String statisticKey){
        if(mAdOptionParent != null){
            mAdOptionParent.getAdData(context,statisticKey);
        }
    }

    public void refrush(){
        if(mAdOptionParent != null) mAdOptionParent.refrush();
    }

    /**
     * 加载g广告数据
     * @param old_list ：原数据体
     * @param isBack ：是否是向上加载的数据
     * @return
     */
    public ArrayList<Map<String, String>> getNewAdData(ArrayList<Map<String, String>> old_list,boolean isBack) {
        if(mAdOptionParent == null) return old_list;
        return mAdOptionParent.getNewAdData(old_list,isBack);
    }

//    public void setIndexs(Integer[] adIndexs){
//        if(mAdOptionParent != null) mAdOptionParent.setIndexs(adIndexs);
//    }

    public void onAdShow(Map<String, String> map, View view) {
        mAdOptionParent.onAdShow(map,view);
    }

    public void onAdClick(Map<String, String> map) {
        mAdOptionParent.onAdClick(map);
    }

    public void onAdHintClick(Activity act, Map<String, String> map, final String eventID, final String twoLevel){
        mAdOptionParent.onAdHintClick(act,map,eventID,twoLevel);
    }

    public int getLimitNum() {
        return limitNum;
    }

    public void setLimitNum(int limitNum) {
        this.limitNum = limitNum;
    }

    /**
     * 判断当时是否需要刷新
     * @return
     */
    public boolean isNeedRefresh(){
        return mAdOptionParent.isNeedRefresh();
    }

    /**
     * 刷新数据
     */
    public void refreshData(){
        mAdOptionParent.getAdData(XHActivityManager.getInstance().getCurrentActivity());
        mAdOptionParent.setAdDataCallBack(new AdOptionParent.AdDataCallBack() {
            @Override
            public void adDataBack(int tag, int nums) {
                if(adDataCallBack!=null)adDataCallBack.adDataBack(2,nums);
            }
        });
    }
    public AdOptionParent.AdDataCallBack adDataCallBack;
    public void setAdDataCallBack(AdOptionParent.AdDataCallBack callBack){
        this.adDataCallBack= callBack;
    }
    /**
     * 获取当前要插入对广告位
     * @param index
     * @return
     */
    public int getIndexAd(int index){
        int AdIndexStart = 3;//开始角标位
        int endIndex=0;
        if(index<=2){//0,1,2
            if(index==0)endIndex=AdIndexStart;
            else if(index>=1)endIndex=AdIndexStart+6+(index-1)*7;
        }else{//3,4,5,6,7
            endIndex=3+6+7+(index-2)*8;
        }

        return endIndex;
    }
}
