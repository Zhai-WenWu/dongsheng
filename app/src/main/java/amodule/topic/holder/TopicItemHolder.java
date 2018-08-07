package amodule.topic.holder;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.xiangha.R;

import org.eclipse.jetty.util.security.Constraint;

import acore.logic.AppCommon;
import acore.tools.FileManager;
import acore.tools.ToolsDevice;
import acore.widget.rvlistview.holder.RvBaseViewHolder;
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
                mLabel.setTextColor(Color.parseColor(labelModel.getColor()));
                mLabel.setBackgroundColor(Color.parseColor(labelModel.getBgColor()));
                mLabel.setVisibility(View.VISIBLE);
            } else {
                mLabel.setVisibility(View.GONE);
            }
            ImageModel imageModel = data.getImageModel();
            VideoModel videoModel = data.getVideoModel();
            if (videoModel != null && !videoModel.isEmpty()) {
                String videoImg = videoModel.getVideoImg();
                String videoGif = videoModel.getVideoGif();
                if (!TextUtils.isEmpty(videoGif)) {
                    Glide.with(itemView.getContext()).load(videoGif).asGif().diskCacheStrategy(DiskCacheStrategy.SOURCE).placeholder(R.drawable.i_nopic).into(mImg);
                } else if (!TextUtils.isEmpty(videoImg)) {
                    loadImage(videoImg, mImg);
                } else {
                    mImg.setImageResource(R.drawable.i_nopic);
                }
            } else if (imageModel != null && !TextUtils.isEmpty(imageModel.getImageUrl())) {
                loadImage(imageModel.getImageUrl(), mImg);
            } else {
                mImg.setImageResource(R.drawable.i_nopic);
            }
        } else {
            mLabel.setVisibility(View.GONE);
        }
    }

    private void loadImage(String url, ImageView view) {
        LoadImage.with(itemView.getContext())
                .load(url).setPlaceholderId(R.drawable.i_nopic)
                .setSaveType(FileManager.save_cache)
                .build().into(view);
    }

    @Override
    public void onClick(View v) {
        if (mTopicItemModel != null) {
            AppCommon.openUrl(mTopicItemModel.getGotoUrl(), true);
        }
    }
}
