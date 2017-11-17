package amodule._common.widget.horizontal;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
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
import amodule._common.delegate.IStatictusData;
import amodule._common.widget.baseview.BaseSubTitleView;
import amodule.home.adapter.HorizontalAdapter1;
import amodule.home.adapter.HorizontalAdapter2;
import amodule.home.adapter.HorizontalAdapter3;

import static amodule._common.helper.WidgetDataHelper.KEY_PARAMETER;
import static amodule._common.helper.WidgetDataHelper.KEY_STYLE;

/**
 * Description :
 * PackageName : amodule._common.widget.horizontal
 * Created by MrTrying on 2017/11/13 15:51.
 * Author : mrtrying
 * E_mail : ztanzeyu@gmail.com
 */

public class HorizontalRecyclerView extends RelativeLayout implements IBindMap,IStatictusData {

    private RvListView mRecyclerView;
    private BaseSubTitleView mSubTitleView;
    private RvBaseAdapter mRecyclerAdapter;
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
        Log.i("tzy","map = " + map.toString());
        if (map == null || map.isEmpty()){
            setVisibility(GONE);
            return;
        }
        boolean isResetData = false;
        if (mSubTitleView != null) {
            mSubTitleView.setData(map);
            isResetData = true;
        }
        Map<String,String> dataMap = StringManager.getFirstMap(map.get("data"));
        ArrayList<Map<String, String>> list = StringManager.getListMapByJson(dataMap.get("list"));
        if (mRecyclerAdapter != null) {
            mRecyclerAdapter.updateData(list);
            isResetData = true;
            mRecyclerView.scrollToPosition(0);
        }
        if (isResetData)
            return;
        String style = map.get(KEY_STYLE);
        if (getChildCount() == 0 && style != null) {
            switch (style) {
                case "1":
                    inflate(getContext(), R.layout.horizontal_recyclerview_layout1, this);
                    mRecyclerAdapter = new HorizontalAdapter1(getContext(), list);
                    break;
                case "2":
                    inflate(getContext(), R.layout.horizontal_recyclerview_layout2, this);
                    mRecyclerAdapter = new HorizontalAdapter2(getContext(), list);
                    break;
                case "3":
                    inflate(getContext(), R.layout.horizontal_recyclerview_layout1, this);
                    mRecyclerAdapter = new HorizontalAdapter3(getContext(), list);
                    break;
                default:
                    inflate(getContext(), R.layout.horizontal_recyclerview_layout1, this);
                    mRecyclerAdapter = new HorizontalAdapter1(getContext(), list);
                    break;
            }
            mSubTitleView = (BaseSubTitleView) findViewById(R.id.subtitle_view);
            Map<String,String> parameterMap = StringManager.getFirstMap(map.get(KEY_PARAMETER));
            mSubTitleView.setData(parameterMap);

            mRecyclerView = (RvListView) findViewById(R.id.recycler_view);
            mRecyclerView.setFocusable(false);
            LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
            mRecyclerView.setLayoutManager(layoutManager);
            mRecyclerView.setAdapter(mRecyclerAdapter);
            mRecyclerView.setOnItemClickListener((view, holder, position) -> {
                String url = list.get(position).get("url");
                AppCommon.openUrl((Activity)HorizontalRecyclerView.this.getContext(), url, true);
                if(!TextUtils.isEmpty(id) && !TextUtils.isEmpty(twoLevel)){
                    XHClick.mapStat(getContext(),id,twoLevel,threeLevel+position);
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
                }
            });
        }
        setVisibility(VISIBLE);
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
}
