package amodule.home.adapter;

import android.content.Context;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.xiangha.R;

import java.util.List;
import java.util.Map;

import acore.widget.rvlistview.adapter.RvBaseAdapter;
import acore.widget.rvlistview.holder.RvBaseViewHolder;
import amodule.home.viewholder.ViewHolder3;

/**
 * Created by sll on 2017/11/14.
 */

public class HorizontalAdapter3 extends RvBaseAdapter<Map<String, String>> {
    private List<Map<String, String>> mDatas;
    private boolean isCache;
    public HorizontalAdapter3(Context context, @Nullable List<Map<String, String>> data) {
        super(context, data);
        mDatas = data;
    }

    @Override
    public RvBaseViewHolder<Map<String, String>> onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder3(LayoutInflater.from(mContext).inflate(R.layout.recyclerview_item3, null),parent){
            @Override
            public boolean canStat() {
                return !isCache;
            }
        };
    }

    @Override
    public void onBindViewHolder(RvBaseViewHolder<Map<String, String>> holder, int position) {
        if (mDatas == null || mDatas.isEmpty() || mDatas.size() <= position)
            return;
        super.onBindViewHolder(holder,position);
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    public void setCache(boolean cache) {
        isCache = cache;
    }
}
