package amodule.topic.adapter;

import android.content.Context;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.List;

import acore.widget.rvlistview.adapter.RvBaseAdapter;
import acore.widget.rvlistview.holder.RvBaseViewHolder;
import amodule.topic.holder.TopicItemHolder;
import amodule.topic.model.TopicItemModel;

public class TopicInfoStaggeredAdapter extends RvBaseAdapter<TopicItemModel> {

    public TopicInfoStaggeredAdapter(Context context, @Nullable ArrayList<TopicItemModel> data) {
        super(context, data);
    }

    @Override
    public RvBaseViewHolder<TopicItemModel> onCreateViewHolder(ViewGroup parent, int viewType) {
        RvBaseViewHolder viewHolder = null;
        switch (viewType) {
            case 3://图片头部
                viewHolder = new ActivityIconHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.topic_list_item_layout, null));
            case 4://tab
                viewHolder = new TopicTabHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.topic_tab_layout, null));
            case 5://视频
                viewHolder = new TopicItemHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.topic_list_item_layout, null));
        }
        return viewHolder;
    }

    @Override
    public int getItemViewType(int position) {
        if (mData == null || mData.size() == 0)
            return -1;
        return mData.get(position).getItemType();
    }
}
