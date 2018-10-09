package amodule.home.viewholder;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;

import com.bumptech.glide.BitmapRequestBuilder;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.xiangha.R;

import java.util.Map;

import acore.logic.stat.RvMapViewHolderStat;
import acore.tools.FileManager;
import acore.tools.Tools;
import acore.widget.rvlistview.holder.RvBaseViewHolder;
import aplug.basic.LoadImage;
import aplug.basic.SubAnimTarget;
import aplug.basic.SubBitmapTarget;
import aplug.basic.XHConf;

import static android.view.View.GONE;

/**
 * Created by sll on 2017/11/23.
 */

public class XHBaseRvViewHolder extends RvMapViewHolderStat {

    private Map<String, String> mDataMap;

    public XHBaseRvViewHolder(@NonNull View itemView,View parent) {
        super(itemView, parent);
    }

    public XHBaseRvViewHolder(@NonNull View itemView, String m) {
        super(itemView, m);
    }

    public XHBaseRvViewHolder(@NonNull View itemView, String m, String f1) {
        super(itemView, m, f1);
    }

    @Override
    public void overrideBindData(int position, @Nullable Map<String, String> data) {
        mDataMap = data;
    }

    protected void setViewImage(final ImageView v, String value) {
        setViewImage(v, value, 0);
    }

    protected void setViewImage(final ImageView v, String value, int placeHolderRedId) {
        setViewImage(v, value, 0, true);
    }

    protected void setViewImage(final ImageView v, String value, int placeHolderRedId, boolean animated) {
        if(null == v) return;
        if(TextUtils.isEmpty(value)){
            v.setVisibility(GONE);
            return;
        }
        v.setVisibility(View.VISIBLE);
        if (value.indexOf("http") == 0) {// 异步请求网络图片
            if (v.getTag(R.string.tag) != null && v.getTag(R.string.tag).equals(value))
                return;
            if (value.length() < 10)
                return;
            v.setTag(R.string.tag, value);
            if (v.getContext() == null) return;
            BitmapRequestBuilder<GlideUrl, Bitmap> bitmapRequest = LoadImage.with(v.getContext())
                    .load(value).setPlaceholderId(placeHolderRedId == 0 ? XHConf.img_placeholderID : placeHolderRedId)
                    .setSaveType(FileManager.save_cache)
                    .build();
            if (bitmapRequest != null){
                if (animated) {
                    AlphaAnimation animation = new AlphaAnimation(0f, 1f);
                    animation.setDuration(300);
                    bitmapRequest.animate(animation);
                }
                bitmapRequest.into(getSubAnimTarget(v, value));
            }
        }
    }

    protected SubBitmapTarget getTarget(final ImageView v, final String url) {
        return new SubBitmapTarget() {
            @Override
            public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> arg1) {
                if (bitmap != null && v.getTag(R.string.tag) != null && v.getTag(R.string.tag).equals(url)) {
                    v.setImageBitmap(bitmap);
                }
            }
        };
    }

    protected SubAnimTarget getSubAnimTarget(final ImageView v, final String url){
        return new SubAnimTarget(v) {
            @Override
            protected void setResource(Bitmap bitmap) {
                if (bitmap != null && v.getTag(R.string.tag) != null && v.getTag(R.string.tag).equals(url)) {
                    v.setImageBitmap(bitmap);
                }
            }
        };
    }

    /**
     * 根据原始宽高和间距动态计算等比宽高
     * @param originalW 原始宽
     * @param originalH 原始高
     * @param spaceW 空白间距
     * @return 两个元素的数组，0位置表示计算后的宽，1位置表示计算后的高
     */
    protected int[] computeItemWH(int originalW, int originalH, int spaceW, int showNum) {
        int[] wh = new int[2];
        if (originalW == 0 || originalH == 0 || showNum <= 0)
            return wh;
        double w = 1.0 * (Tools.getPhoneWidth() - spaceW) / showNum;
        wh[0] = (int) w;
        wh[1] = (int) (w / originalW * originalH);
        return wh;
    }

    public Map<String, String> getData() {
        return mDataMap;
    }
}
