package amodule.topic.holder;

import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import com.xiangha.R;

import java.util.Map;

import acore.widget.rvlistview.holder.RvBaseViewHolder;

public class RecommentTopicItemHolder extends RvBaseViewHolder<Map<String, String>> {
    public RecommentTopicItemHolder(View view) {
        super(view);
    }

    @Override
    public void bindData(int position, @Nullable Map<String, String> data) {
        TextView recommentTopicItemTV = itemView.findViewById(R.id.recomment_topic_item);
        recommentTopicItemTV.setText(data.get("name"));
    }
}
