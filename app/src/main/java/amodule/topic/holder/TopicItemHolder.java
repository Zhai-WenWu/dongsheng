package amodule.topic.holder;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.BitmapRequestBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.xiangha.R;

import org.eclipse.jetty.util.security.Constraint;

import acore.logic.AppCommon;
import acore.logic.XHClick;
import acore.tools.ColorUtil;
import acore.tools.FileManager;
import acore.tools.ToolsDevice;
import acore.widget.rvlistview.holder.RvBaseViewHolder;
import amodule.dish.activity.ShortVideoDetailActivity;
import amodule.topic.activity.TopicInfoActivity;
import amodule.topic.model.ImageModel;
import amodule.topic.model.LabelModel;
import amodule.topic.model.TopicItemModel;
import amodule.topic.model.VideoModel;
import aplug.basic.LoadImage;

public class TopicItemHolder extends RvBaseViewHolder<TopicItemModel> implements View.OnClickListener {
    private TopicItemModel mTopicItemModel;

    private ImageView mImg;
    private TextView mLabel;
    public TopicItemHolder(@NonNull View topicItemView) {
        super(topicItemView);
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
        itemView.setOnClickListener(this);
    }

    @Override
    public void bindData(int position, @Nullable TopicItemModel data) {
        mTopicItemModel = data;
        if (data != null) {
            LabelModel labelModel = data.getLabelModel();
            if (labelModel != null && !TextUtils.isEmpty(labelModel.getTitle())) {
                mLabel.setText(labelModel.getTitle());
                mLabel.setTextColor(ColorUtil.parseColor(labelModel.getColor()));
                GradientDrawable drawable = new GradientDrawable();
                drawable.setColor(ColorUtil.parseColor(labelModel.getBgColor()));
                drawable.setCornerRadius(itemView.getResources().getDimensionPixelSize(R.dimen.dp_2));
                mLabel.setBackground(drawable);
                mLabel.setVisibility(View.VISIBLE);
            } else {
                mLabel.setVisibility(View.GONE);
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

    @Override
    public void onClick(View v) {
        if (mTopicItemModel != null) {
            AppCommon.openUrl(mTopicItemModel.getGotoUrl(), true);
            XHClick.mapStat(itemView.getContext(), ShortVideoDetailActivity.STA_ID, "用户内容", "内容详情点击量");
        }
    }
}
