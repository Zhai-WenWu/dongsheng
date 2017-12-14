package acore.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.view.ViewGroup;

import java.util.List;
import java.util.Map;

import acore.widget.rvlistview.adapter.RvBaseAdapter;
import acore.widget.rvlistview.holder.RvBaseViewHolder;

/**
 * 通用ViewHolder 的adpter
 */
public class RvBaseViewHolderAdapter extends RvBaseAdapter<Map<String,String>> {
    private RvBaseViewHolder viewHolder;
    public RvBaseViewHolderAdapter(Context context, @Nullable List<Map<String, String>> data ,RvBaseViewHolder<Map<String, String>> viewHolder ) {
        super(context, data);
        this.viewHolder= viewHolder;
    }

    @Override
    public RvBaseViewHolder<Map<String, String>> onCreateViewHolder(ViewGroup parent, int viewType) {
        return viewHolder;
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }
}
