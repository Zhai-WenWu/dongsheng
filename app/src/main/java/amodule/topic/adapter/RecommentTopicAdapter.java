package amodule.topic.adapter;

import android.content.Context;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import acore.widget.rvlistview.adapter.RvBaseAdapter;
import acore.widget.rvlistview.holder.RvBaseViewHolder;
import amodule.topic.holder.RecommentTopicItemHolder;
import amodule.topic.holder.TopicItemHolder;
import amodule.topic.model.TopicItemModel;

public class RecommentTopicAdapter extends RvBaseAdapter<Map<String, String>> {


    public RecommentTopicAdapter(Context context, @Nullable List<Map<String, String>> data) {
        super(context, data);
    }

    @Override
    public RvBaseViewHolder<Map<String, String>> onCreateViewHolder(ViewGroup parent, int viewType) {
        return new RecommentTopicItemHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.recomment_topic_list_item_layout, parent,false));
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }
}
