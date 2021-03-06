package amodule._common.widget.horizontal;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.XHClick;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.widget.rvlistview.RvListView;
import acore.widget.rvlistview.adapter.RvBaseAdapter;
import amodule._common.delegate.IBindMap;
import amodule._common.delegate.IHandlerClickEvent;
import amodule._common.delegate.IResetCallback;
import amodule._common.delegate.ISaveStatistic;
import amodule._common.delegate.ISetIsCache;
import amodule._common.delegate.ISetShowIndex;
import amodule._common.delegate.ISetStatisticPage;
import amodule._common.delegate.IStatictusData;
import amodule._common.delegate.IStatisticCallback;
import amodule._common.delegate.ITitleStaticCallback;
import amodule._common.delegate.IUpdatePadding;
import amodule._common.delegate.StatisticCallback;
import amodule._common.helper.WidgetDataHelper;
import amodule._common.widget.baseview.BaseSubTitleView;
import amodule.home.adapter.HorizontalAdapter1;
import amodule.home.adapter.HorizontalAdapter2;
import amodule.home.adapter.HorizontalAdapter3;
import amodule.home.viewholder.XHBaseRvViewHolder;

import static acore.logic.stat.StatConf.STAT_TAG;
import static amodule._common.helper.WidgetDataHelper.KEY_PARAMETER;
import static amodule._common.helper.WidgetDataHelper.KEY_STYLE;

/**
 * Description :
 * PackageName : amodule._common.widget.horizontal
 * Created by MrTrying on 2017/11/13 15:51.
 * Author : mrtrying
 * E_mail : ztanzeyu@gmail.com
 */

public class HorizontalRecyclerView extends RelativeLayout implements IBindMap,ISetStatisticPage,
        IStatictusData,ISaveStatistic,IHandlerClickEvent,IStatisticCallback,ITitleStaticCallback,
        IResetCallback, ISetShowIndex, IUpdatePadding ,ISetIsCache{

    private RvListView mRecyclerView;
    private BaseSubTitleView mSubTitleView;
    private RvBaseAdapter mRecyclerAdapter;
    private StatisticCallback mStatisticCallback,mTitleStatisticCallback;

    private int mShowIndex = -1;
    private boolean isCache;
    public HorizontalRecyclerView(Context context) {
        this(context,null);
    }

    public HorizontalRecyclerView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public HorizontalRecyclerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    private void initialize() {
        setVisibility(GONE);
    }

    @Override
    public void setData(Map<String, String> map) {
        if (map == null || map.isEmpty()){
            setVisibility(GONE);
            return;
        }
        Map<String,String> dataMap = StringManager.getFirstMap(map.get(WidgetDataHelper.KEY_DATA));
        if (null == dataMap || dataMap.isEmpty()){
            setVisibility(GONE);
            return;
        }
        boolean isResetData = false;
        Map<String,String> parameterMap = StringManager.getFirstMap(map.get(KEY_PARAMETER));
        if (mSubTitleView != null) {
            mSubTitleView.setData(parameterMap);
            isResetData = true;
        }
        ArrayList<Map<String, String>> list = StringManager.getListMapByJson(dataMap.get(WidgetDataHelper.KEY_LIST));
        if (mRecyclerAdapter != null) {
            mRecyclerAdapter.updateData(list);
            isResetData = true;
            mRecyclerView.scrollToPosition(0);
        }
        if(list.isEmpty()){
            setVisibility(GONE);
            return;
        }
        //设置顶部边距
        int paddingTop = mShowIndex == 0 ? Tools.getDimen(getContext(),R.dimen.dp_10) : 0;
        updatePadding(getPaddingLeft(),paddingTop,getPaddingRight(),getPaddingBottom());

        if (isResetData)
            return;
        String style = map.get(KEY_STYLE);
        if (getChildCount() == 0 && style != null) {
            switch (style) {
                case "1":
                case "6":
                    inflate(getContext(), R.layout.horizontal_recyclerview_layout1, this);
                    mRecyclerAdapter = new HorizontalAdapter1(getContext(), list);
                    ((HorizontalAdapter1)mRecyclerAdapter).setCache(isCache);
                    break;
                case "2":
                case "4":
                case "5":
                    inflate(getContext(), R.layout.horizontal_recyclerview_layout2, this);
                    mRecyclerAdapter = new HorizontalAdapter2(getContext(), list);
                    ((HorizontalAdapter2)mRecyclerAdapter).setCache(isCache);
                    break;
                case "3":
                    inflate(getContext(), R.layout.horizontal_recyclerview_layout1, this);
                    mRecyclerAdapter = new HorizontalAdapter3(getContext(), list);
                    ((HorizontalAdapter3)mRecyclerAdapter).setCache(isCache);
                    break;
                default:
                    inflate(getContext(), R.layout.horizontal_recyclerview_layout1, this);
                    mRecyclerAdapter = new HorizontalAdapter1(getContext(), list);
                    ((HorizontalAdapter1)mRecyclerAdapter).setCache(isCache);
                    break;
            }
            mSubTitleView = (BaseSubTitleView) findViewById(R.id.subtitle_view);
            if(mSubTitleView instanceof ITitleStaticCallback){
                ((ITitleStaticCallback)mSubTitleView).setTitleStaticCallback(mTitleStatisticCallback);
            }
            mSubTitleView.setData(parameterMap);
            moduleType = StringManager.getFirstMap(parameterMap.get("title")).get("text1");

            mRecyclerView = (RvListView) findViewById(R.id.recycler_view);
            mRecyclerView.setTag(STAT_TAG,"");
            mRecyclerView.setFocusable(false);
            final LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
            mRecyclerView.setLayoutManager(layoutManager);
            mRecyclerView.setAdapter(mRecyclerAdapter);
            mRecyclerView.setOnItemClickListener((view, holder, position) -> {
                if (holder != null && holder instanceof XHBaseRvViewHolder) {
                    XHBaseRvViewHolder viewHolder = (XHBaseRvViewHolder) holder;
                    Map<String, String> data = viewHolder.getData();
                    if (data == null || data.isEmpty())
                        return;
                    String url = data.get(WidgetDataHelper.KEY_URL);
                    AppCommon.openUrl((Activity)HorizontalRecyclerView.this.getContext(), url, true);
                    statistic(position,data);
                }
            });
            mRecyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
                @Override
                public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                    super.getItemOffsets(outRect, view, parent, state);
                    int position = parent.getChildAdapterPosition(view) - mRecyclerView.getHeaderViewsSize();
                    if (position == 0) {
                        outRect.left = getPxByDp(R.dimen.dp_20);
                        outRect.right = getPxByDp(R.dimen.dp_5);
                    } else if (position == list.size() - 1) {
                        outRect.left = getPxByDp(R.dimen.dp_5);
                        outRect.right = getPxByDp(R.dimen.dp_20);
                    } else {
                        outRect.left = getPxByDp(R.dimen.dp_5);
                        outRect.right = getPxByDp(R.dimen.dp_5);
                    }
                    outRect.bottom = getPxByDp(R.dimen.dp_10);
                }
            });
        }
        setVisibility(VISIBLE);
    }

    private void statistic(int position,Map<String, String> data) {
//        Log.i("XHClick", "click: ");
        if(mStatisticCallback != null){
            if(mSubTitleView.getData() != null){
                Map<String,String> map = StringManager.getFirstMap(mSubTitleView.getData().get("title"));
                mStatisticCallback.onStatistic(id,map.get("text1"),map.get("text1")+"位置"+(position+1),position+1);
            }
        }else{
            if(!TextUtils.isEmpty(id) && !TextUtils.isEmpty(twoLevel)){
                if(TextUtils.isEmpty(threeLevel))
                    XHClick.mapStat(getContext(),id,twoLevel + (position + 1),"");
                else
                    XHClick.mapStat(getContext(),id,twoLevel,threeLevel+(position + 1));
            }
        }
    }

    private int getPxByDp(int resId) {
        return getResources().getDimensionPixelSize(resId);
    }

    String id, twoLevel, threeLevel;

    @Override
    public void setStatictusData(String id, String twoLevel, String threeLevel) {
        this.id = id;
        this.twoLevel = twoLevel;
        this.threeLevel = threeLevel;
        if(mSubTitleView != null){
            mSubTitleView.setStatictusData(id,twoLevel,threeLevel);
        }
    }

    private String moduleType = "";
    @Override
    public void saveStatisticData(String page) {
        //列表
    }

    @NonNull
    private String getModeType() {
        return moduleType != null ? moduleType : "";
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
    public void setTitleStaticCallback(StatisticCallback callback) {
        mTitleStatisticCallback = callback;
    }

    String page="";
    @Override
    public void setStatisticPage(String page) {
        this.page = page;
    }

    @Override
    public void reset() {
        if(mRecyclerView != null){
            mRecyclerView.scrollToPosition(0);
        }
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
    public void setCache(boolean isCache) {
        this.isCache = isCache;
    }
}
