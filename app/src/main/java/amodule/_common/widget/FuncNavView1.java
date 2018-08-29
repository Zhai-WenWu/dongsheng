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
import acore.logic.XHClick;
import acore.override.helper.XHActivityManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.widget.rvlistview.RvListView;
import amodule._common.delegate.IBindMap;
import amodule._common.delegate.IHandlerClickEvent;
import amodule._common.delegate.ISaveStatistic;
import amodule._common.delegate.ISetShowIndex;
import amodule._common.delegate.IStatictusData;
import amodule._common.delegate.IStatisticCallback;
import amodule._common.delegate.IUpdatePadding;
import amodule._common.delegate.StatisticCallback;
import amodule._common.helper.WidgetDataHelper;
import amodule._common.utility.WidgetUtility;
import amodule.home.view.HomeFuncNavView1;

/**
 * Description :
 * PackageName : amodule._common.widget
 * Created by MrTrying on 2017/11/14 11:40.
 * Author : mrtrying
 * E_mail : ztanzeyu@gmail.com
 */

public class FuncNavView1 extends HomeFuncNavView1 implements IBindMap,IStatictusData,
        ISaveStatistic,IHandlerClickEvent,IStatisticCallback, ISetShowIndex, IUpdatePadding {

    private StatisticCallback mStatisticCallback;

    private int mShowIndex = -1;
    private int mScreenWidth;

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
        mScreenWidth = Tools.getPhoneWidth();
    }

    @Override
    public void setData(Map<String, String> data) {
//        Log.i("tzy","data = " + data);
        if (null == data || data.isEmpty()){
            setVisibility(GONE);
            return;
        }

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
        int centerSpacing = 0;
        int firstLastSpacing = getResources().getDimensionPixelSize(R.dimen.dp_20);
        int itemWidth = getResources().getDimensionPixelSize(R.dimen.dp_55);
        int itemSize = mapArrayList.size();
        if (itemSize > 5) {
            centerSpacing = (mScreenWidth - 2 * firstLastSpacing - itemWidth * 5) / 5;
        } else if (itemSize > 1){
            centerSpacing = (mScreenWidth - 2 * firstLastSpacing - itemWidth * itemSize) / (itemSize - 1);
        } else if (itemSize == 1){
            firstLastSpacing = (mScreenWidth - itemWidth) / 2;
        }
        setItemSpacing(firstLastSpacing,  centerSpacing, firstLastSpacing);
        adapterFuncNav1.notifyDataSetChanged();
        setVisibility(VISIBLE);
        listView.setOnItemClickListener(new RvListView.OnItemClickListener() {
            @Override
            public void onItemClick(View view, RecyclerView.ViewHolder holder, int position) {
                if(position<mapArrayList.size()) {
                    AppCommon.openUrl(XHActivityManager.getInstance().getCurrentActivity(), mapArrayList.get(position).get("url"), true);
                    statistic(position, mapArrayList.get(position));
                }
            }
        });
    }


    private void statistic(int index,Map<String, String> data) {
        XHClick.saveStatictisFile("home", "homeSmallNav", data.get("type"), "", "",
                "click", "", "", "", data.get("text1"), "");
        if(mStatisticCallback != null){
            mStatisticCallback.onStatistic(id,twoLevel,data.get("text1"),index);
        }else{
            if(!TextUtils.isEmpty(id) && !TextUtils.isEmpty(twoLevel)){
                XHClick.mapStat(getContext(),id,twoLevel,data.get("text1"));
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
}
