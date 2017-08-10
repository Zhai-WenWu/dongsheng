package amodule.main.view.item;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.BitmapRequestBuilder;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.xianghatest.R;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Map;

import acore.tools.FileManager;
import acore.tools.ToolsDevice;
import aplug.basic.LoadImage;
import aplug.basic.SubBitmapTarget;
import xh.basic.tool.UtilImage;

/**
 * Created by sll on 2017/4/18.
 */

public class BaseItemView extends RelativeLayout {

    protected final int TAG_ID = R.string.tag;
    protected int mImgResource = R.drawable.i_nopic;
    protected int mRoundImgPixels = 0, mImgWidth = 0, mImgHeight = 0,// 以像素为单位
            mRoundType = 1; // 1为全圆角，2上半部分圆角
    protected boolean mImgZoom = false; // 是否允许图片拉伸来适应设置的宽或高
    protected String mImgLevel = FileManager.save_cache; // 图片保存等级
    protected ImageView.ScaleType mScaleType = ImageView.ScaleType.CENTER_CROP;
    protected boolean mIsAnimate = false;// 控制图片渐渐显示

    protected Map<String, String> mDataMap;
    public String viewType="";
    protected int mPosition = 0;
    public BaseItemView(Context context) {
        super(context);
    }

    public BaseItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BaseItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setData(Map<String, String> dataMap, int position) {
        if (dataMap == null || dataMap.size() < 1)
            return;
        mDataMap = dataMap;
        mPosition = position;
    }

    /**
     * 设置数据类型
     * 必须在setData方法之前调用
     */
    public void setViewType(String type){
        this.viewType= type;
    }
    public Map<String, String> getData() {
        return mDataMap;
    }

    public interface OnItemClickListener{
        void onItemClick();
    }

    protected String handleNumber(String num) {
        if (TextUtils.isEmpty(num))
            return "";
        if (Integer.parseInt(num) < 10000)
            return num;
        BigDecimal bd = new BigDecimal(Integer.parseInt(num) * 1.0 / 10000);
        bd = bd.setScale(1, BigDecimal.ROUND_HALF_UP);
        return bd + "万";
    }

    protected boolean viewIsVisible(View view) {
        return view == null ? false : view.getVisibility() == View.VISIBLE;
    }

    protected void setViewImage(final ImageView v, String value) {
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

    protected SubBitmapTarget getTarget(final ImageView v, final String url) {
        return new SubBitmapTarget() {
            @Override
            public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> arg1) {
                ImageView img = null;
                if (v.getTag(TAG_ID).equals(url))
                    img = v;
                if (img != null && bitmap != null) {
                    // 图片圆角和宽高适应auther_userImg
                    if (v.getId() == R.id.iv_userImg || v.getId() == R.id.auther_userImg) {
                        v.setScaleType(ImageView.ScaleType.CENTER_CROP);
//						bitmap = UtilImage.toRoundCorner(v.getResources(), bitmap, 1, ToolsDevice.dp2px(context, 500));
//                        v.setImageBitmap(UtilImage.makeRoundCorner(bitmap));
                        v.setImageBitmap(UtilImage.toRoundCorner(v.getResources(),bitmap,1,500));
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
}
