package amodule.topic.adapter;

import android.support.annotation.Nullable;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.xiangha.R;

import acore.override.XHApplication;
import acore.widget.rvlistview.holder.RvBaseViewHolder;
import amodule.topic.model.TopicItemModel;

class TopicTabHolder extends RvBaseViewHolder<TopicItemModel> {

    private FrameLayout mHotView;
    private FrameLayout mNewView;
    private TextView mHotTabTv;
    private View mHotTabBottomView;
    private TextView mNewTabTV;
    private View mNewTabBottomView;

    public TopicTabHolder(View inflate) {
        super(inflate);
        mHotView = findViewById(R.id.fl_hot);
        mNewView = findViewById(R.id.fl_new);
        mHotTabTv = findViewById(R.id.tv_hot_tab);
        mHotTabBottomView = findViewById(R.id.view_hot_tab_bottom);
        mNewTabTV = findViewById(R.id.tv_new_tab);
        mNewTabBottomView = findViewById(R.id.view_new_tab_bottom);
    }

    @Override
    public void bindData(int position, @Nullable TopicItemModel data) {
        mHotView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHotTabTv.setTextColor(XHApplication.in().getResources().getColor(R.color.white));
                mHotTabBottomView.setVisibility(View.VISIBLE);
                mNewTabTV.setTextColor(XHApplication.in().getResources().getColor(R.color.c_777777));
                mNewTabBottomView.setVisibility(View.INVISIBLE);

                data.setTabTag(data.TAB_HOT);
                itemView.setOnClickListener(this);
            }
        });

        mNewView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHotTabTv.setTextColor(XHApplication.in().getResources().getColor(R.color.c_777777));
                mHotTabBottomView.setVisibility(View.INVISIBLE);
                mNewTabTV.setTextColor(XHApplication.in().getResources().getColor(R.color.white));
                mNewTabBottomView.setVisibility(View.VISIBLE);
                data.setTabTag(data.TAB_NEW);
                itemView.setOnClickListener(this);

            }
        });
    }
}
