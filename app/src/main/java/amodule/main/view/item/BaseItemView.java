package amodule.main.view.item;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.BitmapRequestBuilder;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.xiangha.R;

import java.math.BigDecimal;
import java.util.Map;

import acore.tools.FileManager;
import aplug.basic.LoadImage;
import aplug.basic.SubAnimTarget;
import aplug.basic.SubBitmapTarget;

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
        if(null == v) return;
        if(TextUtils.isEmpty(value)){
            v.setVisibility(GONE);
            return;
        }
        v.setVisibility(View.VISIBLE);

        if (value.indexOf("http") == 0) {// 异步请求网络图片
            if (v.getTag(TAG_ID) != null && v.getTag(TAG_ID).equals(value))
                return;
            v.setImageResource(mImgResource);
            v.setScaleType(mScaleType);
            if (value.length() < 10)
                return;
            v.setTag(TAG_ID, value);
            if (v.getContext() == null) return;
            BitmapRequestBuilder<GlideUrl, Bitmap> bitmapRequest = LoadImage.with(v.getContext())
                    .load(value)
                    .setSaveType(mImgLevel)
                    .build();
            if (bitmapRequest != null){
                AlphaAnimation animation = new AlphaAnimation(0f,1f);
                animation.setDuration(300);
                bitmapRequest.animate(animation);
                bitmapRequest.into(getSubAnimTarget(v, value));
            }
        }
    }

    protected SubBitmapTarget getTarget(final ImageView v, final String url) {
        return new SubBitmapTarget() {
            @Override
            public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> arg1) {
                if (bitmap != null && v.getTag(TAG_ID) != null && v.getTag(TAG_ID).equals(url)) {
                    v.setImageBitmap(bitmap);
                }
            }
        };
    }

    protected SubAnimTarget getSubAnimTarget(final ImageView v,final String url){
        return new SubAnimTarget(v) {
            @Override
            protected void setResource(Bitmap bitmap) {
                if (bitmap != null && v.getTag(TAG_ID) != null && v.getTag(TAG_ID).equals(url)) {
                    v.setImageBitmap(bitmap);
                }
            }
        };
    }
}
