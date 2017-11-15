package amodule.home.viewholder;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Map;

import acore.widget.rvlistview.holder.RvBaseViewHolder;
import amodule.home.view.RecyclerItem2;

/**
 * Created by sll on 2017/11/14.
 */

public class ViewHolder2 extends RvBaseViewHolder<Map<String, String>> {

    public RecyclerItem2 mItemView;
    public ViewHolder2(@NonNull RecyclerItem2 itemView) {
        super(itemView);
        mItemView = itemView;
    }

    @Override
    public void bindData(int position, @Nullable Map<String, String> data) {
        mItemView.setData(data, position);
    }
}
