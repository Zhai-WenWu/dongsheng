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

import acore.tools.FileManager;
import acore.tools.Tools;
import acore.widget.rvlistview.holder.RvBaseViewHolder;
import aplug.basic.LoadImage;
import aplug.basic.SubAnimTarget;
import aplug.basic.SubBitmapTarget;

import static android.view.View.GONE;

/**
 * Created by sll on 2017/11/23.
 */

public class XHBaseRvViewHolder extends RvBaseViewHolder<Map<String, String>> {

    private Map<String, String> mDataMap;
    public XHBaseRvViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    @Override
    public void bindData(int position, @Nullable Map<String, String> data) {
        mDataMap = data;
    }

    protected void setViewImage(final ImageView v, String value) {
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
                    .load(value)
                    .setSaveType(FileManager.save_cache)
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
