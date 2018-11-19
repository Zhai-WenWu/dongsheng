package amodule.topic.adapter;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;

import com.xiangha.R;

import acore.tools.Tools;
import acore.widget.rvlistview.holder.RvBaseViewHolder;
import amodule.topic.model.TopicItemModel;

/**
 * 话题页顶部活动图片列表Holder
 */
class ActivityIconHolder extends RvBaseViewHolder<TopicItemModel> {
    private ImageView mImageView;
    private int mScreenW;
    public ActivityIconHolder(View inflate) {
        super(inflate);
        mImageView = findViewById(R.id.image);
        mScreenW = Tools.getPhoneWidth();
    }

    @Override
    public void bindData(int position, @Nullable TopicItemModel data) {
        Bitmap bitmap = data.getBitmap();
        if (bitmap != null) {
            int bitmapW = bitmap.getWidth();
            int bitmapH = bitmap.getHeight();
            int dstH = mScreenW * bitmapH / bitmapW;
            mImageView.setImageBitmap(Bitmap.createScaledBitmap(bitmap, mScreenW, dstH, true));
        }else{
            BitmapDrawable bitmapDrawable = (BitmapDrawable) mImageView.getResources().getDrawable(R.drawable.i_nopic);
            Bitmap bitmap2 = bitmapDrawable.getBitmap();
            int dstH = mScreenW * data.getImageHieght() / data.getImageWidth();
            mImageView.setImageBitmap(Bitmap.createScaledBitmap(bitmap2, mScreenW, dstH, true));
        }
    }
}
