package amodule._common.widget;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.xiangha.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.ConfigMannager;
import acore.logic.XHClick;
import acore.logic.stat.intefaces.OnItemClickListenerRvStat;
import acore.override.helper.XHActivityManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.widget.rvlistview.RvListView;
import amodule._common.delegate.IBindMap;
import amodule._common.delegate.IHandlerClickEvent;
import amodule._common.delegate.ISaveStatistic;
import amodule._common.delegate.ISetIsCache;
import amodule._common.delegate.ISetShowIndex;
import amodule._common.delegate.IStatictusData;
import amodule._common.delegate.IStatisticCallback;
import amodule._common.delegate.IUpdatePadding;
import amodule._common.delegate.StatisticCallback;
import amodule._common.helper.WidgetDataHelper;
import amodule._common.utility.WidgetUtility;
import amodule.home.view.HomeFuncNavView1;

import static acore.logic.ConfigMannager.KEY_HOME_FUN_NAV_STAT;
import static acore.logic.stat.StatisticsManager.STAT_DATA;

/**
 * Description :
 * PackageName : amodule._common.widget
 * Created by MrTrying on 2017/11/14 11:40.
 * Author : mrtrying
 * E_mail : ztanzeyu@gmail.com
 */

public class FuncNavView1 extends HomeFuncNavView1 implements IBindMap,IStatictusData,
        ISaveStatistic,IHandlerClickEvent,IStatisticCallback, ISetShowIndex, IUpdatePadding,ISetIsCache {

    private StatisticCallback mStatisticCallback;

    private int mShowIndex = -1;
    private boolean isCache;

    public FuncNavView1(Context context) {
        super(context);
    }

    public FuncNavView1(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public FuncNavView1(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void initData() {
        super.initData();
    }

    @Override
    public void setData(Map<String, String> data) {
//        Log.i("tzy","data = " + data);
        if (null == data || data.isEmpty()){
            setVisibility(GONE);
            return;
        }
        adapterFuncNav1.setCache(isCache);
        Map<String,String> dataMap = StringManager.getFirstMap(data.get("data"));
        ArrayList<Map<String, String>> arrayList = StringManager.getListMapByJson(dataMap.get("list"));
        if (arrayList.isEmpty()){
            setVisibility(GONE);
            return;
        }
        //设置顶部边距
        int paddingTop = mShowIndex == 0 ? Tools.getDimen(getContext(),R.dimen.dp_10)  : 0;
        setPadding(getPaddingLeft(),paddingTop,getPaddingRight(),getPaddingBottom());
        setDataToView(arrayList);
    }

    protected void setDataToView(List<Map<String,String>> data){
        if(null == data || data.isEmpty()){
            setVisibility(GONE);
            return;
        }
//        final int length = Math.min(navIds.size(),data.size());
        mapArrayList.clear();
        for(int index = 0 ; index < data.size() ; index ++){
            mapArrayList.add(data.get(index));
//            Log.i("xianghaTag","数据为：：：：——————"+data.get(index));
        }
        computeItemSpacing();
        adapterFuncNav1.notifyDataSetChanged();
        setVisibility(VISIBLE);
        listView.setOnItemClickListener(new OnItemClickListenerRvStat() {
            @Override
            public void onItemClicked(View view, RecyclerView.ViewHolder holder, int position) {
                if(position<mapArrayList.size()) {
                    AppCommon.openUrl(XHActivityManager.getInstance().getCurrentActivity(), mapArrayList.get(position).get("url"), true);
                    statistic(position, mapArrayList.get(position));
                }
            }

            @Override
            protected String getStatData(int position) {
                return mapArrayList.get(position).get(STAT_DATA);
            }
        });
    }


    private void statistic(int index,Map<String, String> data) {
        XHClick.saveStatictisFile("home", "homeSmallNav", data.get("type"), "", "",
                "click", "", "", "", data.get("text1"), "");
        final String key = data.get("text1");
        String statData = ConfigMannager.getConfigByLocal(KEY_HOME_FUN_NAV_STAT);
        Map<String,String> statMap = StringManager.getFirstMap(statData);
        if(!TextUtils.isEmpty(key) && !TextUtils.isEmpty(statMap.get(key))){
            Log.i("tzy", "statistic: " + statMap.get(key));
            XHClick.mapStat(getContext(),statMap.get(key),"","");
            return;
        }
        if(mStatisticCallback != null){
            mStatisticCallback.onStatistic(id,twoLevel,key,index);
        }else{
            if(!TextUtils.isEmpty(id) && !TextUtils.isEmpty(twoLevel)){
                XHClick.mapStat(getContext(),id,twoLevel,key);
            }
        }
    }

    String id, twoLevel, threeLevel;

    @Override
    public void setStatictusData(String id, String twoLevel, String threeLevel) {
        this.id = id;
        this.twoLevel = twoLevel;
        this.threeLevel = threeLevel;
    }

    @Override
    public void saveStatisticData(String page) {

    }

    @Override
    public boolean handlerClickEvent(String url, String moduleType, String dataType, int position) {
        return false;
    }

    @Override
    public void setStatisticCallback(StatisticCallback statisticCallback) {
        mStatisticCallback = statisticCallback;
    }

    @Override
    public void setShowIndex(int showIndex) {
        mShowIndex = showIndex;
    }

    @Override
    public void updatePadding(int l, int t, int r, int b) {
        setPadding(l, t, r, b);
    }

    @Override
    public void setCache(boolean cache) {
        isCache = cache;
    }
}
