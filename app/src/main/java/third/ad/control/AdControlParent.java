package third.ad.control;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import java.util.ArrayList;
import java.util.Map;

import third.ad.option.AdOptionParent;

/**
 * Created by Fang Ruijiao on 2017/5/5.
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

    public void setIndexs(Integer[] adIndexs){
        if(mAdOptionParent != null) mAdOptionParent.setIndexs(adIndexs);
    }

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
}
