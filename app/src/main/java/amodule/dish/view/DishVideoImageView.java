package amodule.dish.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.BitmapRequestBuilder;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.xianghatest.R;

import acore.override.view.ItemBaseView;
import aplug.basic.LoadImage;
import aplug.basic.SubBitmapTarget;
import xh.basic.tool.UtilImage;

/**
 * Created by Administrator on 2016/8/15.
 */

public class DishVideoImageView extends ItemBaseView {
    private ImageView imageview_rela;
    private TextView time_tv;

    public DishVideoImageView(Context context) {
        super(context, R.layout.view_dish_video);
    }

    public DishVideoImageView(Context context, AttributeSet attrs) {
        super(context, attrs, R.layout.view_dish_video);
    }

    public DishVideoImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr, R.layout.view_dish_video);
    }

    @Override
    public void init() {
        super.init();
        imageview_rela = (ImageView) findViewById(R.id.imageview_rela);
        time_tv = (TextView) findViewById(R.id.time);
    }

    /**
     * 设置数据
     *
     * @param img
     * @param time
     */
    public View setData(String img, String time) {
        setViewImage(imageview_rela, img);
        if (!TextUtils.isEmpty(time)) {
            time_tv.setVisibility(View.VISIBLE);
            time_tv.setText(time);
        } else time_tv.setVisibility(View.GONE);
        return this;
    }

    public View setImageScaleType(String img, String time, @NonNull ImageView.ScaleType type) {
        if (TextUtils.isEmpty(img)) return this;
        if (!TextUtils.isEmpty(time)) {
            time_tv.setVisibility(View.VISIBLE);
            time_tv.setText(time);
        } else time_tv.setVisibility(View.GONE);
        BitmapRequestBuilder<GlideUrl, Bitmap> requestBuilder = LoadImage.with(context)
                .load(img)
                .setImageRound(roundImgPixels)
                .setPlaceholderId(roundImgPixels == 0 ? imgResource : R.drawable.bg_round_user_icon)
                .setErrorId(roundImgPixels == 0 ? imgResource : R.drawable.bg_round_user_icon)
                .setSaveType(imgLevel)
                .build();
        requestBuilder.into(new SubBitmapTarget() {
            @Override
            public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> glideAnimation) {
                if (bitmap != null) {
                    if (bitmap.getHeight() > bitmap.getWidth())
                        imageview_rela.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    else
                        imageview_rela.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    UtilImage.setImgViewByWH(imageview_rela, bitmap, imgWidth, imgHeight, imgZoom);
                }

            }
        });
        return this;
    }
}
