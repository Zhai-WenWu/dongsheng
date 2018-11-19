package amodule.topic.adapter;

import android.content.Context;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.List;

import acore.widget.rvlistview.adapter.RvBaseAdapter;
import acore.widget.rvlistview.holder.RvBaseViewHolder;
import amodule.topic.holder.TopicItemHolder;
import amodule.topic.model.TopicItemModel;

public class TopicInfoStaggeredAdapter extends RvBaseAdapter<TopicItemModel> {

    public static final int ITEM_ACTIVITY_IMG = 1;
    public static final int ITEM_TAB = 2;
    public static final int ITEM_TOPIC_VID = 3;
    private TopicTabHolder mTopicTabHolder;
    private TopicTabHolder.OnTabClick mOnTabClick;
    private int mTabIndex = -1;

    public TopicInfoStaggeredAdapter(Context context, @Nullable ArrayList<TopicItemModel> data) {
        super(context, data);
    }

    @Override
    public RvBaseViewHolder<TopicItemModel> onCreateViewHolder(ViewGroup parent, int viewType) {
        RvBaseViewHolder viewHolder = null;
        switch (viewType) {
            case ITEM_ACTIVITY_IMG://图片头部
                viewHolder = new ActivityIconHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.topic_image_layout_item, null));
                break;
            case ITEM_TAB://tab
                viewHolder = new TopicTabHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.topic_tab_layout, null));
                mTopicTabHolder = (TopicTabHolder) viewHolder;
                mTopicTabHolder.setOnTabClick(this::handleTabClick);
                break;
            case ITEM_TOPIC_VID://视频
                viewHolder = new TopicItemHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.topic_list_item_layout, null)){
                    @Override
                    protected void onStat(int position, TopicItemModel data) {
                        super.onStat(position - (mTabIndex + 1), data);
                    }

                    @Override
                    public boolean canStat() {
                        return mTabIndex != -1;
                    }
                };
                break;
        }
        return viewHolder;
    }

    @Override
    public int getItemViewType(int position) {
        if (mData == null || mData.size() == 0)
            return -1;
        return mData.get(position).getItemType();
    }

    public TopicTabHolder getTopicTabHolder() {
        return mTopicTabHolder;
    }

    public void setTabIndex(int tabIndex) {
        mTabIndex = tabIndex;
    }

    public void setOnTabClick(TopicTabHolder.OnTabClick onTabClick) {
        mOnTabClick = onTabClick;
    }

    private void handleTabClick(@Nullable TopicItemModel data) {
        if(mOnTabClick != null){
            mOnTabClick.onClick(data);
        }
    }
}
