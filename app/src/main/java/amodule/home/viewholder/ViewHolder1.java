package amodule.home.viewholder;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Map;

import acore.widget.rvlistview.holder.RvBaseViewHolder;
import amodule.home.view.RecyclerItem1;

/**
 * Created by sll on 2017/11/14.
 */

public class ViewHolder1 extends RvBaseViewHolder<Map<String, String>> {

    public RecyclerItem1 mItemView;
    public ViewHolder1(@NonNull RecyclerItem1 itemView) {
        super(itemView);
        mItemView = itemView;
    }

    @Override
    public void bindData(int position, @Nullable Map<String, String> data) {
        mItemView.setData(data, position);
    }
}
