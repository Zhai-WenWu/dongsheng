package amodule.topic.holder;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.BitmapRequestBuilder;
import com.xiangha.R;

import acore.logic.stat.RvBaseViewHolderStat;
import acore.tools.FileManager;
import acore.tools.ToolsDevice;
import amodule.topic.model.ImageModel;
import amodule.topic.model.LabelModel;
import amodule.topic.model.TopicItemModel;
import amodule.topic.model.VideoModel;
import aplug.basic.LoadImage;

public class TopicItemHolder extends RvBaseViewHolderStat<TopicItemModel> {
    private TopicItemModel mTopicItemModel;

    private ImageView mImg;
    private TextView mLabel;
    private ImageView mRanking;

    public TopicItemHolder(@NonNull View topicItemView) {
        super(topicItemView, topicItemView.getContext().getClass().getSimpleName());
        int originalW = 124;
        int originalH = 165;
        int screenW = ToolsDevice.getWindowPx(itemView.getContext()).widthPixels;
        int newW = (screenW - topicItemView.getResources().getDimensionPixelSize(R.dimen.dp_2)) / 3;
        int newH = newW * originalH / originalW;
        ConstraintLayout.LayoutParams lp = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, newH);
        topicItemView.setLayoutParams(lp);
        initView();
    }

    private void initView() {
        mImg = itemView.findViewById(R.id.img);
        mLabel = itemView.findViewById(R.id.label);
        mRanking = itemView.findViewById(R.id.iv_hot_ranking);
    }

    @Override
    public boolean isShown(TopicItemModel data) {
        return data.getIsShow();
    }

    @Override
    public void hasShown(TopicItemModel data) {
        data.setIsShow(true);
    }

    @Override
    public String getStatJson(TopicItemModel data) {
        return data.getStatJson();
    }

    @Override
    public void overrideBindData(int position, @Nullable TopicItemModel data) {
        mTopicItemModel = data;
        if (data != null) {
            LabelModel labelModel = data.getLabelModel();
            if (labelModel != null && !TextUtils.isEmpty(labelModel.getTitle())) {
                mLabel.setText(labelModel.getTitle());
                mLabel.setTextColor(Color.parseColor(labelModel.getColor()));
                GradientDrawable drawable = new GradientDrawable();
                drawable.setColor(Color.parseColor(labelModel.getBgColor()));
                drawable.setCornerRadius(itemView.getResources().getDimensionPixelSize(R.dimen.dp_2));
                mLabel.setBackground(drawable);
                mLabel.setVisibility(View.VISIBLE);
            } else {
                mLabel.setVisibility(View.GONE);
            }

            if (data.getIsHot()) {
                switch (data.getHotNo()) {
                    case 0:
                        mRanking.setImageResource(R.drawable.topic_item_no1);
                        mLabel.setVisibility(View.GONE);
                        mRanking.setVisibility(View.VISIBLE);
                        break;
                    case 1:
                        mRanking.setImageResource(R.drawable.topic_item_no2);
                        mLabel.setVisibility(View.GONE);
                        mRanking.setVisibility(View.VISIBLE);
                        break;
                    case 2:
                        mRanking.setImageResource(R.drawable.topic_item_no3);
                        mLabel.setVisibility(View.GONE);
                        mRanking.setVisibility(View.VISIBLE);
                        break;
                    default:
                        mRanking.setVisibility(View.GONE);
                        break;
                }
            } else {
                mRanking.setVisibility(View.GONE);
            }

            ImageModel imageModel = data.getImageModel();
            VideoModel videoModel = data.getVideoModel();
            if (imageModel != null && !TextUtils.isEmpty(imageModel.getImageUrl())) {
                loadImage(imageModel.getImageUrl(), mImg);
            } else if (videoModel != null && !videoModel.isEmpty()) {
                String videoImg = videoModel.getVideoImg();
                if (!TextUtils.isEmpty(videoImg)) {
                    loadImage(videoImg, mImg);
                } else {
                    mImg.setImageResource(R.drawable.i_nopic);
                }
            } else {
                mImg.setImageResource(R.drawable.i_nopic);
            }
        } else {
            mImg.setImageResource(R.drawable.i_nopic);
            mLabel.setVisibility(View.GONE);
        }
    }

    private void loadImage(String url, ImageView view) {
        view.setTag(R.string.tag, url);
        view.setImageResource(R.drawable.i_nopic);
        BitmapRequestBuilder builder = LoadImage.with(itemView.getContext())
                .load(url).setPlaceholderId(R.drawable.i_nopic).setErrorId(R.drawable.i_nopic)
                .setSaveType(FileManager.save_cache)
                .build();
        if (builder != null) {
            builder.into(view);
        }
    }
}
