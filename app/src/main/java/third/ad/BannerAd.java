package third.ad;

import android.app.Activity;
import android.graphics.Bitmap;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.BitmapRequestBuilder;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.Target;
import com.xiangha.R;

import java.util.Map;

import acore.override.XHApplication;
import acore.tools.ImgManager;
import acore.tools.ToolsDevice;
import acore.widget.ImageViewVideo;
import aplug.basic.LoadImage;
import aplug.basic.SubBitmapTarget;
import third.ad.scrollerAd.XHAllAdControl;

/**
 * banner广告，策略：服务端返回是否显示banner广告，若显示，则banner信息随着返回
 *
 * @author FangRuijiao
 */
public class BannerAd {
    private Activity mAct;
    private ImageView mAdImage;
    /** banner广告无需刷新，次标记标识是否已经加载过 */
    private boolean mIsHasShow = false;
    private OnBannerListener mListener;

    public int marginLeft = 0, marginRight = 0;
    private XHAllAdControl mXHAllAdControl;

    /**
     * @param act
     * @param adControl
     * @param imageView
     */
    public BannerAd(Activity act, XHAllAdControl adControl, ImageView imageView) {
        mAct = act;
        mXHAllAdControl = adControl;
        mAdImage = imageView;
//		mImgViewSingle = (ImageViewVideo)mLayoutParent.findViewById(R.id.ad_banner_item_iv_single);
    }

    public BannerAd(Activity act, XHAllAdControl adControl, ImageView imageView, OnBannerListener listener) {
        mAct = act;
        mXHAllAdControl = adControl;
        mAdImage = imageView;
//		mImgViewSingle = (ImageViewVideo)mLayoutParent.findViewById(R.id.ad_banner_item_iv_single);
        mListener = listener;
    }

    public void onShowAd(Map<String, String> map) {
        if (!mIsHasShow) {
            mIsHasShow = true;
            setActivityData(map);
        }
    }

    private void setActivityData(Map<String, String> map) {
        if (mAdImage == null) {
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
        mXHAllAdControl.onAdClick(0, "");
        adClick();
    }

    public interface OnBannerListener {
        void onShowAd();

        void onClickAd();

        void onImgShow(int imgH);
    }

}
