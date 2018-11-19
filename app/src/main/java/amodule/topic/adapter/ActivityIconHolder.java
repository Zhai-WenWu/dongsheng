package amodule.topic.adapter;

import android.support.annotation.Nullable;
import android.view.View;

import acore.widget.rvlistview.holder.RvBaseViewHolder;
import amodule.topic.model.TopicItemModel;

class ActivityIconHolder extends RvBaseViewHolder<TopicItemModel> {
    public ActivityIconHolder(View inflate) {
        super(inflate);
    }

    @Override
    public void bindData(int position, @Nullable TopicItemModel data) {

    }
}
