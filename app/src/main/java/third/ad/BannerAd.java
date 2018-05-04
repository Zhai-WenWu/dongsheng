package third.ad;

import android.app.Activity;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.BitmapRequestBuilder;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.Target;

import java.util.Map;

import acore.override.XHApplication;
import acore.tools.ImgManager;
import acore.tools.ToolsDevice;
import aplug.basic.LoadImage;
import aplug.basic.SubBitmapTarget;
import third.ad.scrollerAd.XHAllAdControl;

import static third.ad.scrollerAd.XHScrollerAdParent.ADKEY_BANNER;

/**
 * banner广告，策略：服务端返回是否显示banner广告，若显示，则banner信息随着返回
 *
 * @author FangRuijiao
 */
public class BannerAd {
    private Activity mAct;
    private ImageView mAdImage;
    private OnBannerListener mListener;

    public int marginLeft = 0, marginRight = 0;
    private XHAllAdControl mXHAllAdControl;

    /**
     * @param act 上下文
     * @param adControl 广告控制
     * @param imageView 显示图片额imageview
     */
    public BannerAd(Activity act, @NonNull XHAllAdControl adControl, ImageView imageView) {
        mAct = act;
        mXHAllAdControl = adControl;
        mAdImage = imageView;
    }

    public void onShowAd(Map<String, String> map) {
        setActivityData(map);
    }

    private void setActivityData(Map<String, String> map) {
        if (mAdImage == null)
            return;
        if (map == null || map.isEmpty() || !ADKEY_BANNER.equals(map.get("type"))) {
            mAdImage.setVisibility(View.GONE);
            return;
        }
        String mImgUrl = map.get("imgUrl");
        //设置活动图
        BitmapRequestBuilder<GlideUrl, Bitmap> bitmapRequest = LoadImage.with(XHApplication.in())
                .load(mImgUrl)
                .setRequestListener(new RequestListener<GlideUrl, Bitmap>() {
                    @Override
                    public boolean onResourceReady(Bitmap arg0, GlideUrl arg1, Target<Bitmap> arg2, boolean arg3, boolean arg4) {
                        return false;
                    }

                    @Override
                    public boolean onException(Exception arg0, GlideUrl arg1, Target<Bitmap> arg2, boolean arg3) {
                        mAdImage.setVisibility(View.GONE);
                        return false;
                    }
                })
                .build();
        if (bitmapRequest != null){
            bitmapRequest.into(new SubBitmapTarget() {
                @Override
                public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> arg1) {
                    int imgViewWidth = mAdImage.getWidth() > 0 ?
                            mAdImage.getWidth() : ToolsDevice.getWindowPx(mAct).widthPixels - marginLeft - marginRight;
                    int imgHeight = imgViewWidth * bitmap.getHeight() / bitmap.getWidth();
                    mAdImage.setScaleType(ImageView.ScaleType.FIT_XY);
                    ImgManager.setImgViewByWH(mAdImage, bitmap, imgViewWidth, imgHeight, true);

                    mAdImage.setVisibility(View.VISIBLE);
                    if (mListener != null)
                        mListener.onImgShow(imgHeight);
                    adShow();
                }
            });
            mAdImage.setOnClickListener(v -> onAdClick());
        }
    }

    private void adShow(){
        if (mListener != null) {
            mListener.onShowAd();
        }
    }

    private void adClick(){
        if (mListener != null) {
            mListener.onClickAd();
        }
    }

    public void onAdClick() {
        if(mXHAllAdControl != null){
            mXHAllAdControl.onAdClick(0, "");
        }
        adClick();
    }

    public void setOnBannerListener(OnBannerListener listener){
        mListener = listener;
    }

    public interface OnBannerListener {
        void onShowAd();

        void onClickAd();

        void onImgShow(int imgH);
    }

}
