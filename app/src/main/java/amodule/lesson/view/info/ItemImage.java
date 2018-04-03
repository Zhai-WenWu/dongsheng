package amodule.lesson.view.info;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.bumptech.glide.request.animation.GlideAnimation;
import com.xiangha.R;

import java.util.List;
import java.util.Map;

import acore.tools.ImgManager;
import acore.tools.StringManager;
import amodule._common.delegate.IBindExtraArrayMap;
import amodule._common.delegate.IBindMap;
import amodule._common.widget.baseWidget.BaseExtraLinearLayout;
import aplug.basic.LoadImage;
import aplug.basic.SubBitmapTarget;

/**
 * Description :
 * PackageName : amodule.lesson.view.info
 * Created by tanze on 2018/3/30 11:05.
 * e_mail : ztanzeyu@gmail.com
 */
public class ItemImage extends LinearLayout implements IBindMap, IBindExtraArrayMap {

    private BaseExtraLinearLayout mBottomExtraLayout;
    private ImageView mImageView;
    private RelativeLayout mMoreLayout;

    private OnClickMoreCallbcak mClickMoreCallbcak;

    private int mImageWidth = 0;

    private String mType = "";

    public ItemImage(Context context) {
        this(context, null);
    }

    public ItemImage(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ItemImage(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initializeUI();
    }

    private void initializeUI() {
        LayoutInflater.from(getContext()).inflate(R.layout.item_lesson_image, this);
        mBottomExtraLayout = (BaseExtraLinearLayout) findViewById(R.id.bottom_extra_layout);
        mImageView = (ImageView) findViewById(R.id.image);
        mMoreLayout = (RelativeLayout) findViewById(R.id.more_layout);
        //设置
        mMoreLayout.setOnClickListener(v -> handleMoreClick(mType));
    }

    @Override
    public void setData(Map<String, String> data) {

        setExtraData(StringManager.getListMapByJson(data.get("bottom")));
    }

    private void showImage(String imageUrl) {
        if (TextUtils.isEmpty(imageUrl)) {
            return;
        }
        LoadImage.with(getContext())
                .load(imageUrl)
                .build()
                .into(new SubBitmapTarget() {
                    @Override
                    public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> glideAnimation) {
                        ImgManager.setImgViewByWH(mImageView, bitmap, mImageWidth, 0, false);
                    }
                });
    }

    @Override
    public void setExtraData(List<Map<String, String>> array) {
        if (mBottomExtraLayout != null) {
            mBottomExtraLayout.setData(array, true);
        }
    }

    private void handleMoreClick(String type) {
        if (mClickMoreCallbcak != null) {
            mClickMoreCallbcak.onClickMore(type);
        }
    }

    public void setImageWidth(int imageWidth) {
        mImageWidth = imageWidth;
    }

    public void setType(String type){
        mType = type;
    }

    public void setClickMoreCallbcak(OnClickMoreCallbcak clickMoreCallbcak) {
        mClickMoreCallbcak = clickMoreCallbcak;
    }

    public interface OnClickMoreCallbcak {
        void onClickMore(String type);
    }
}
