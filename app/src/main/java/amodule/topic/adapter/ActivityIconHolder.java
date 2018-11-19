package amodule.topic.adapter;

import android.graphics.Bitmap;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;

import com.xiangha.R;

import acore.widget.rvlistview.holder.RvBaseViewHolder;
import amodule.topic.model.TopicItemModel;

/**
 * 话题页顶部活动图片列表Holder
 */
class ActivityIconHolder extends RvBaseViewHolder<TopicItemModel> {
    private ImageView mImageView;
    public ActivityIconHolder(View inflate) {
        super(inflate);
        mImageView = findViewById(R.id.image);
    }

    @Override
    public void bindData(int position, @Nullable TopicItemModel data) {
        Bitmap bitmap = data.getBitmap();
        if (bitmap != null) {
            mImageView.setImageBitmap(bitmap);
        }
    }
}
