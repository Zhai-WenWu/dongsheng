package amodule.home.adapter;

import android.content.Context;
import android.support.annotation.Nullable;
import android.view.ViewGroup;

import java.util.List;
import java.util.Map;

import acore.widget.rvlistview.adapter.RvBaseAdapter;
import acore.widget.rvlistview.holder.RvBaseViewHolder;
import amodule.home.view.RecyclerItem3;
import amodule.home.viewholder.ViewHolder3;

/**
 * Created by sll on 2017/11/14.
 */

public class HorizontalAdapter3 extends RvBaseAdapter<Map<String, String>> {
    private List<Map<String, String>> mDatas;
    public HorizontalAdapter3(Context context, @Nullable List<Map<String, String>> data) {
        super(context, data);
        mDatas = data;
    }

    @Override
    public RvBaseViewHolder<Map<String, String>> onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder3(new RecyclerItem3(getContext()));
    }

    @Override
    public void onBindViewHolder(RvBaseViewHolder<Map<String, String>> holder, int position) {
        if (mDatas == null || mDatas.isEmpty() || mDatas.size() <= position)
            return;
        holder.bindData(position, mDatas.get(position));
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }
}
