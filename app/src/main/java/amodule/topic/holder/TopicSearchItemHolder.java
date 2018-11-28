package amodule.topic.holder;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import com.xiangha.R;

import java.util.Map;

import acore.widget.rvlistview.holder.RvBaseViewHolder;

public class TopicSearchItemHolder extends RvBaseViewHolder<Map<String, String>> {
    public TopicSearchItemHolder(@NonNull View itemView) {
        super(itemView);
    }

    @Override
    public void bindData(int position, @Nullable Map<String, String> data) {
        TextView topicNameTv = itemView.findViewById(R.id.tv_topic_name);
        TextView topicJoinTv = itemView.findViewById(R.id.tv_topic_join);
        topicNameTv.setText("#"+data.get("name"));
        topicJoinTv.setText(data.get("num")+"人参与");
    }
}
