package amodule.user.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.request.animation.GlideAnimation;
import com.xiangha.R;

import java.util.Map;

import amodule.main.view.item.BaseItemView;
import amodule.main.view.item.HomeItem;
import aplug.basic.SubBitmapTarget;
import third.ad.control.AdControlParent;
import xh.basic.tool.UtilImage;

/**
 * Created by sll on 2017/5/24.
 */

public class UserHomeItem extends BaseItemView {

    protected AdControlParent mAdControlParent;
    private View mLineTop;


    protected boolean mIsAd;

    public UserHomeItem(Context context, int layoutId) {
        super(context);
        LayoutInflater.from(context).inflate(layoutId, this, true);
        initView();
    }

    public UserHomeItem(Context context, AttributeSet attrs, int layoutId) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(layoutId, this, true);
        initView();
    }

    public UserHomeItem(Context context, AttributeSet attrs, int defStyleAttr, int layoutId) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(layoutId, this, true);
        initView();
    }

    protected void initView() {
        mLineTop = findViewById(R.id.line_top);


    }

    public void setAdControl(AdControlParent adControlParent) {
        this.mAdControlParent = adControlParent;
    }

    @Override
    public void setData(Map<String, String> dataMap, int position) {
        super.setData(dataMap, position);
        if (mDataMap != null) {
            bindData();
        }
    }

    protected void bindData() {
        resetData();
        resetView();

        String adStyle = mDataMap.get("adstyle");
        if (!TextUtils.isEmpty(adStyle) && adStyle.equals("ad")) {
            mIsAd = true;
        }
    }

    protected void resetData() {
        mIsAd = false;
    }

    protected void resetView() {
        if (mLineTop != null) {
            mLineTop.setVisibility(mPosition > 0 ? View.VISIBLE : View.GONE);
        }
    }

    protected void loadImage(String url, ImageView view) {
        if (view == null || TextUtils.isEmpty(url))
            return;
        setViewImage(view, url);
    }

    @Override
    protected SubBitmapTarget getTarget(final ImageView v, final String url) {
        return new SubBitmapTarget() {
            @Override
            public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> arg1) {
                if (mCallback != null) {
                    if (mCallback != null)
                        mCallback.callback(bitmap);
                    return;
                }
                ImageView img = null;
                if (v.getTag(TAG_ID).equals(url))
                    img = v;
                if (img != null && bitmap != null) {
                    // 图片圆角和宽高适应auther_userImg
                    if (v.getId() == R.id.iv_userImg || v.getId() == R.id.auther_userImg) {
                        v.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        v.setImageBitmap(UtilImage.makeRoundCorner(bitmap));
                    } else {
                        v.setScaleType(mScaleType);
                        UtilImage.setImgViewByWH(v, bitmap, mImgWidth, mImgHeight, mImgZoom);
                    }
                }
            }
        };
    }

    private HomeItem.ADImageLoadCallback mCallback;
    protected void loadImage(String url, ImageView view, HomeItem.ADImageLoadCallback callback) {
        mCallback = callback;
        loadImage(url, view);
    }

    public interface OnItemClickListener {
        void onItemClick(UserHomeItem itemView, Map<String, String> dataMap);
    }

    protected OnItemClickListener mOnItemClickListener;
    public void setOnItemClickListener(OnItemClickListener clickListener) {
        mOnItemClickListener = clickListener;
    }

    public void notifyUploadStatusChanged(String uploadType) {

    }

}
