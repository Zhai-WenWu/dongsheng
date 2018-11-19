package amodule.topic.adapter;

import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.xiangha.R;

import acore.override.XHApplication;
import acore.widget.rvlistview.holder.RvBaseViewHolder;
import amodule.topic.model.TopicItemModel;

public class TopicTabHolder extends RvBaseViewHolder<TopicItemModel> {

    private FrameLayout mHotView;
    private FrameLayout mNewView;
    private TextView mHotTabTv;
    private View mHotTabBottomView;
    private TextView mNewTabTV;
    private View mNewTabBottomView;
    private TopicItemModel mData;

    public TopicTabHolder(View inflate) {
        super(inflate);
        mHotView = findViewById(R.id.fl_hot);
        mNewView = findViewById(R.id.fl_new);
        mHotTabTv = findViewById(R.id.tv_hot_tab);
        mHotTabBottomView = findViewById(R.id.view_hot_tab_bottom);
        mNewTabTV = findViewById(R.id.tv_new_tab);
        mNewTabBottomView = findViewById(R.id.view_new_tab_bottom);
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, inflate.getResources().getDimensionPixelSize(R.dimen.dp_44));
        inflate.setLayoutParams(lp);
    }

    @Override
    public void bindData(int position, @Nullable TopicItemModel data) {
        this.mData = data;

        mHotView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHotTabTv.setTextColor(XHApplication.in().getResources().getColor(R.color.white));
                mHotTabBottomView.setVisibility(View.VISIBLE);
                mNewTabTV.setTextColor(XHApplication.in().getResources().getColor(R.color.c_777777));
                mNewTabBottomView.setVisibility(View.INVISIBLE);
                data.setTabTag(data.TAB_HOT);
                onTabClick.onClick(data);
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
                onTabClick.onClick(data);
            }
        });
    }

    public void setHotClick() {
        mHotTabTv.setTextColor(XHApplication.in().getResources().getColor(R.color.white));
        mHotTabBottomView.setVisibility(View.VISIBLE);
        mNewTabTV.setTextColor(XHApplication.in().getResources().getColor(R.color.c_777777));
        mNewTabBottomView.setVisibility(View.INVISIBLE);

        mData.setTabTag(mData.TAB_HOT);
    }

    public void setNewClick() {
        mHotTabTv.setTextColor(XHApplication.in().getResources().getColor(R.color.c_777777));
        mHotTabBottomView.setVisibility(View.INVISIBLE);
        mNewTabTV.setTextColor(XHApplication.in().getResources().getColor(R.color.white));
        mNewTabBottomView.setVisibility(View.VISIBLE);
        mData.setTabTag(mData.TAB_NEW);
    }

    OnTabClick onTabClick;
    public interface OnTabClick{
        void onClick(TopicItemModel data);
    }

    public void setOnTabClick(OnTabClick onTabClick) {
        this.onTabClick = onTabClick;
    }
}
