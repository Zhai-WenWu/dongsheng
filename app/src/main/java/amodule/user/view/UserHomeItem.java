package amodule.user.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.BitmapRequestBuilder;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.xiangha.R;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;

import acore.tools.FileManager;
import acore.tools.StringManager;
import acore.tools.ToolsDevice;
import amodule.main.view.item.BaseItemView;
import amodule.main.view.item.HomeItem;
import aplug.basic.LoadImage;
import aplug.basic.SubBitmapTarget;
import third.ad.control.AdControlParent;
import xh.basic.tool.UtilImage;

/**
 * Created by sll on 2017/5/24.
 */

public class UserHomeItem extends BaseItemView {

    private final int TAG_ID = R.string.tag;
    private int mImgResource = R.drawable.i_nopic;
    private int mRoundImgPixels = 0, mImgWidth = 0, mImgHeight = 0,// 以像素为单位
            mRoundType = 1; // 1为全圆角，2上半部分圆角
    private boolean mImgZoom = false; // 是否允许图片拉伸来适应设置的宽或高
    private String mImgLevel = FileManager.save_cache; // 图片保存等级
    private ImageView.ScaleType mScaleType = ImageView.ScaleType.CENTER_CROP;
    private boolean mIsAnimate = false;// 控制图片渐渐显示

    protected String mItemType1 = "1";//视频
    protected String mItemType2 = "2";//文章
    protected String mItemType3 = "3";//问答


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

    private void setViewImage(final ImageView v, String value) {
        v.setVisibility(View.VISIBLE);
        // 异步请求网络图片
        if (value.indexOf("http") == 0) {
            if (v.getTag(TAG_ID) != null && v.getTag(TAG_ID).equals(value))
                return;
            if (v.getId() == R.id.iv_userImg || v.getId() == R.id.auther_userImg) {
                mRoundImgPixels = ToolsDevice.dp2px(v.getContext(), 500);
                v.setImageResource(R.drawable.bg_round_user_icon);
            } else {
                v.setImageResource(mImgResource);
            }
            v.setScaleType(ImageView.ScaleType.CENTER_CROP);
            if (value.length() < 10)
                return;
            v.setTag(TAG_ID, value);
            if (v.getContext() == null) return;
            BitmapRequestBuilder<GlideUrl, Bitmap> bitmapRequest = LoadImage.with(v.getContext())
                    .load(value)
                    .setSaveType(mImgLevel)
                    .build();
            if (bitmapRequest != null)
                bitmapRequest.into(getTarget(v, value));
        }
        // 直接设置为内部图片
        else if (value.indexOf("ico") == 0) {
            InputStream is = v.getResources().openRawResource(Integer.parseInt(value.replace("ico", "")));
            Bitmap bitmap = UtilImage.inputStreamTobitmap(is);
            bitmap = UtilImage.toRoundCorner(v.getResources(), bitmap, mRoundType, mRoundImgPixels);
            UtilImage.setImgViewByWH(v, bitmap, mImgWidth, mImgHeight, mImgZoom);
        }
        // 隐藏
        else if (value.equals("hide") || value.length() == 0)
            v.setVisibility(View.GONE);
            // 直接加载本地图片
        else if (!value.equals("ignore")) {
            if (v.getTag(TAG_ID) != null && v.getTag(TAG_ID).equals(value))
                return;
            v.setScaleType(ImageView.ScaleType.CENTER_CROP);
            v.setImageResource(mImgResource);
            v.setTag(TAG_ID, value);
            BitmapRequestBuilder<GlideUrl, Bitmap> bitmapRequest = LoadImage.with(v.getContext())
                    .load(value)
                    .setSaveType(mImgLevel)
                    .build();
            if (bitmapRequest != null)
                bitmapRequest.into(getTarget(v, value));
        }
        // 如果为ignore,则忽略图片
    }

    private SubBitmapTarget getTarget(final ImageView v, final String url) {
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
//						bitmap = UtilImage.toRoundCorner(v.getResources(), bitmap, 1, ToolsDevice.dp2px(context, 500));
                        v.setImageBitmap(UtilImage.makeRoundCorner(bitmap));
                    } else {
                        v.setScaleType(mScaleType);
                        UtilImage.setImgViewByWH(v, bitmap, mImgWidth, mImgHeight, mImgZoom);
                        if (mIsAnimate) {
//							AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1);
//							alphaAnimation.setDuration(300);
//							v.setAnimation(alphaAnimation);
                        }
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

    public interface DeleteCallback {
        void delete(int position);
    }

}
