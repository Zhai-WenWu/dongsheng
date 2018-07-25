package amodule.topic.holder;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.xiangha.R;

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
    private int mScreenW;
    private TopicItemModel mTopicItemModel;

    private ImageView mImg;
    private TextView mLabel;
    public TopicItemHolder(@NonNull View topicItemView) {
        super(topicItemView);
        mScreenW = ToolsDevice.getWindowPx(itemView.getContext()).widthPixels;
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
            if (imageModel != null) {
                int imgH = Integer.parseInt(imageModel.getImageH());
                int imgW = Integer.parseInt(imageModel.getImageW());
                int[] wh = new int[2];
                wh[0] = imgW;
                wh[1] = imgH;
                computeItemWH(wh);
                ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(wh[0], wh[1]);
                itemView.setLayoutParams(lp);
                itemView.invalidate();
            }
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

    private int[] computeItemWH(int[] wh) {
        int originalW = wh[0];
        int originalH = wh[1];
        wh[0] = mScreenW / 3;
        wh[1] = wh[0] * originalH / originalW;
        return wh;
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
