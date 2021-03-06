package amodule.lesson.view.info;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.request.animation.GlideAnimation;
import com.xiangha.R;

import java.util.List;
import java.util.Map;

import acore.logic.XHClick;
import acore.tools.ImgManager;
import acore.tools.StringManager;
import acore.tools.Tools;
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
    private TextView mMoreText;
    private LinearLayout mMoreLayout;

    private OnClickMoreCallbcak mClickMoreCallbcak;
    private OnShowMoreCallback mOnShowMoreCallback;

    private Map<String,String> mData;

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
        mMoreText = (TextView) findViewById(R.id.more_text);
        mMoreLayout = (LinearLayout) findViewById(R.id.more_layout);
        //设置
        mMoreLayout.setOnClickListener(v -> handleMoreClick(mType));
    }

    @Override
    public void setData(@NonNull Map<String, String> data) {
        if(mData != null
                && mData.equals(data)){
            return;
        }
        mData = data;

        showImage(data.get("img"));
        showMoreLayout(data);
        setExtraData(StringManager.getListMapByJson(data.get("bottom")));

        findViewById(R.id.bottom_line_top).setVisibility(!TextUtils.isEmpty(data.get("img"))&&"2".equals(data.get("isEnd"))?VISIBLE:GONE);
        findViewById(R.id.bottom_line).setVisibility(!TextUtils.isEmpty(data.get("img"))&&("2".equals(data.get("isEnd"))||"1".equals(data.get("isEnd")))?VISIBLE:GONE);
    }

    private void showMoreLayout(Map<String, String> data) {
        if (data != null
                && data.containsKey("end")
                && !TextUtils.isEmpty(data.get("text2"))) {
            mType = data.get("end");
            mMoreText.setText(data.get("text2"));
            mMoreLayout.setVisibility(VISIBLE);
            Log.i("tzy", "showMoreLayout: " + mType);
            if(mOnShowMoreCallback != null && !"2".equals(data.get("isShow"))){
                data.put("isShow","2");
                mOnShowMoreCallback.onShow(mType);
            }
        } else {
            mMoreLayout.setVisibility(GONE);
        }

    }

    private void showImage(String imageUrl) {
        mImageView.setImageResource(R.drawable.bg_grey_e0e0e0);
        if (TextUtils.isEmpty(imageUrl)) {
            mImageView.setVisibility(GONE);
            return;
        }
        updateImageHieght(imageUrl);
        LoadImage.with(getContext())
                .load(imageUrl)
                .build()
                .into(new SubBitmapTarget() {
                    @Override
                    public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> glideAnimation) {
                        ImgManager.setImgViewByWH(mImageView, bitmap, mImageWidth, 0, false);
                        mImageView.setVisibility(VISIBLE);
                    }
                });
    }

    private void updateImageHieght(String imageUrl) {
        if(imageUrl.contains("?")){
            Log.i("tzy", "updateImageHieght: " + imageUrl);
            String[] urls = imageUrl.split("\\?");
            if(urls.length == 2
                    && !TextUtils.isEmpty(urls[1])
                    && urls[1].contains("_")){
                String[] sizeValue = urls[1].split("_");
                if(sizeValue.length == 2){
                    int width = Tools.parseIntOfThrow(sizeValue[0],0);
                    int height = Tools.parseIntOfThrow(sizeValue[1],0);
                    Log.i("tzy", "updateImageHieght: image w="+width+" , h="+height);
                    if(width != 0 && height != 0){
                        ViewGroup.LayoutParams layoutParams = mImageView.getLayoutParams();
                        layoutParams.width = mImageWidth;
                        layoutParams.height = mImageWidth * height/ width;
                        Log.i("tzy", "updateImageHieght: layoutParams w="+layoutParams.width+" , h="+layoutParams.height);
                        mImageView.setLayoutParams(layoutParams);
                    }
                }
            }
        }
    }

    @Override
    public void setExtraData(List<Map<String, String>> array) {
        if (mBottomExtraLayout != null) {
            mBottomExtraLayout.setData(array, true,false);
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

    public void setType(String type) {
        mType = type;
    }

    public void setClickMoreCallbcak(OnClickMoreCallbcak clickMoreCallbcak) {
        mClickMoreCallbcak = clickMoreCallbcak;
    }

    public void setOnShowMoreCallback(OnShowMoreCallback onShowMoreCallback) {
        mOnShowMoreCallback = onShowMoreCallback;
    }

    public interface OnClickMoreCallbcak {
        void onClickMore(String type);
    }

    public interface OnShowMoreCallback{
        void onShow(String type);
    }
}
