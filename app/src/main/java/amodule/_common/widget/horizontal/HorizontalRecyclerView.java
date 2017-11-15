package amodule._common.widget.horizontal;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.Map;

import acore.logic.AppCommon;
import acore.tools.StringManager;
import acore.widget.rvlistview.RvListView;
import acore.widget.rvlistview.adapter.RvBaseAdapter;
import amodule._common.delegate.IBindMap;
import amodule._common.widget.baseview.BaseSubTitleView;
import amodule.home.adapter.HorizontalAdapter1;
import amodule.home.adapter.HorizontalAdapter2;
import amodule.home.adapter.HorizontalAdapter3;

/**
 * Description :
 * PackageName : amodule._common.widget.horizontal
 * Created by MrTrying on 2017/11/13 15:51.
 * Author : mrtrying
 * E_mail : ztanzeyu@gmail.com
 */

public class HorizontalRecyclerView extends RelativeLayout implements IBindMap {

    private RvListView mRecyclerView;
    private BaseSubTitleView mSubTitleView;
    private RvBaseAdapter mRecyclerAdapter;
    public HorizontalRecyclerView(Context context) {
        super(context);
    }

    public HorizontalRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HorizontalRecyclerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setData(Map<String, String> map) {
        if (map == null || map.isEmpty())
            return;
        boolean isResetData = false;
        if (mSubTitleView != null) {
            mSubTitleView.setData(map);
            isResetData = true;
        }
        ArrayList<Map<String, String>> list = StringManager.getListMapByJson(map.get("list"));
        if (mRecyclerAdapter != null) {
            mRecyclerAdapter.updateData(list);
            isResetData = true;
        }
        if (isResetData)
            return;
        String style = map.get("style");
        if (getChildCount() == 0 && style != null) {
            switch (style) {
                case "horizontal":
                    inflate(getContext(), R.layout.horizontal_recyclerview_layout1, this);
                    mRecyclerAdapter = new HorizontalAdapter1(getContext(), list);
                    break;
                case "horizontal2":
                    inflate(getContext(), R.layout.horizontal_recyclerview_layout2, this);
                    mRecyclerAdapter = new HorizontalAdapter2(getContext(), list);
                    break;
                case "horizontal3":
                    inflate(getContext(), R.layout.horizontal_recyclerview_layout1, this);
                    mRecyclerAdapter = new HorizontalAdapter3(getContext(), list);
                    break;
                default:
                    inflate(getContext(), R.layout.horizontal_recyclerview_layout1, this);
                    mRecyclerAdapter = new HorizontalAdapter1(getContext(), list);
                    break;
            }
            mSubTitleView = (BaseSubTitleView) findViewById(R.id.subtitle_view);
            mSubTitleView.setData(map);
            mRecyclerView = (RvListView) findViewById(R.id.recycler_view);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
            mRecyclerView.setAdapter(mRecyclerAdapter);
            mRecyclerView.setOnItemClickListener(new RvListView.OnItemClickListener() {
                @Override
                public void onItemClick(View view, RecyclerView.ViewHolder holder, int position) {
                    String url = list.get(position).get("url");
                    AppCommon.openUrl((Activity)HorizontalRecyclerView.this.getContext(), url, true);
                }
            });
        }
    }
}
