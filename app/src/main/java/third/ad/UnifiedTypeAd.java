package third.ad;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.BitmapRequestBuilder;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.Target;
import com.xiangha.R;

import java.util.Map;

import acore.override.XHApplication;
import acore.tools.ToolsDevice;
import acore.widget.ScrollLinearListLayout;
import aplug.basic.LoadImage;
import aplug.basic.SubBitmapTarget;
import third.ad.scrollerAd.XHAllAdControl;
import xh.basic.tool.UtilImage;

/**
 * Created by ：fei_teng on 2017/3/6 16:51.
 */

public class UnifiedTypeAd extends AdParent {


    private Activity mAct;
    private Handler handler;
    private RelativeLayout mAdLayout;
    private int mResouceId;
    private AdListener mListener;
    private Map<String, String> mAdMap;
    /**
     * 首页的点击事件比较特殊，故加标记特效处理
     */
    public boolean isMain = false;
    private AdIsShowListener mAdIsShowListener;

    public String style = null;
    public int maginLeft = 0, maginRight = 0;

    public static final String styleBanner = "styleBanner";
    private int indexInData;
    private String indexOnShow;
    private XHAllAdControl xhAllAdControl;

    public UnifiedTypeAd(Activity context, RelativeLayout adLayout,
                         int resouceId, AdListener listener) {
        mAct = context;
        mAdLayout = adLayout;
        mResouceId = resouceId;
        mListener = listener;
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == 1) {
                    mAdIsShowListener.onIsShowAdCallback(UnifiedTypeAd.this, true);
                } else {
                    mAdIsShowListener.onIsShowAdCallback(UnifiedTypeAd.this, false);
                }
            }
        };
    }


    public void setData(XHAllAdControl xhAllAdControl, int index, Map<String, String> adData,
                        String indexOnShow) {
        this.xhAllAdControl = xhAllAdControl;
        this.indexInData = index;
        this.mAdMap = adData;
        this.indexOnShow = indexOnShow;
    }


    @Override
    public boolean isShowAd(String adPlayId, AdIsShowListener listener) {
        mAdIsShowListener = listener;
        if (mAdMap != null && mAdMap.size() > 0) {
            handler.sendEmptyMessage(1);
        } else {
            handler.sendEmptyMessage(0);
        }

        return true;
    }

    @Override
    public void onResumeAd() {
        View view;
        mAdLayout.setVisibility(View.VISIBLE);
        if (mAdLayout.getChildCount() > 0) {
            view = mAdLayout.getChildAt(0);
        } else {
            view = LayoutInflater.from(mAct).inflate(mResouceId, mAdLayout);
            view.setVisibility(View.GONE);
            if (mListener != null) mListener.onAdCreate();

        }
        initAd(view);
    }

    private void initAd(final View adView) {
        if (mAdMap != null) {
            final String title = mAdMap.get("title");
            final String content = mAdMap.get("desc");
            String imgUrl = mAdMap.get("imgUrl");
            String iconUrl = mAdMap.get("iconUrl");


            BitmapRequestBuilder<GlideUrl, Bitmap> bitmapRequest = LoadImage.with(XHApplication.in())
                    .load(imgUrl)
                    .setRequestListener(new RequestListener<GlideUrl, Bitmap>() {
                        @Override
                        public boolean onResourceReady(Bitmap arg0, GlideUrl arg1, Target<Bitmap> arg2, boolean arg3, boolean arg4) {
                            return false;
                        }

                        @Override
                        public boolean onException(Exception arg0, GlideUrl arg1, Target<Bitmap> arg2, boolean arg3) {
                            return false;
                        }
                    }).build();
            if (bitmapRequest != null)
                bitmapRequest.into(new SubBitmapTarget() {
                    @Override
                    public void onResourceReady(final Bitmap bitmap, GlideAnimation<? super Bitmap> arg1) {
                        final ImageView imageView = (ImageView) adView.findViewById(R.id.view_ad_img);
                        UtilImage.setImgViewByWH(imageView, bitmap, 0, 0, false);
                        final TextView textView = (TextView) adView.findViewById(R.id.view_ad_text);
                        if (textView != null) {
                            if (TextUtils.isEmpty(title)) textView.setText(content);
                            else textView.setText(title + "，" + content);
                        }
                        if (styleBanner.equals(style)) {
                            int imgViewWidth = ToolsDevice.getWindowPx(mAct).widthPixels - maginLeft - maginLeft;
                            int imgHeight = imgViewWidth * bitmap.getHeight() / bitmap.getWidth();
                            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                            UtilImage.setImgViewByWH(imageView, bitmap, imgViewWidth, imgHeight, true);
                        }
                        adView.setVisibility(View.VISIBLE);


                        View.OnClickListener listener = new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                xhAllAdControl.onAdClick(indexInData, indexOnShow);
                            }
                        };
                        if (isMain) {
                            adView.setOnClickListener(ScrollLinearListLayout.getOnClickListener(listener));
                        } else {
                            adView.setOnClickListener(listener);
                        }
                    }
                });

            if (!"2".equals(mAdMap.get("isShow"))) {
                xhAllAdControl.onAdBind(indexInData, adView, indexOnShow);
                mAdMap.put("isShow", "2");
            }
        }
    }

    @Override
    public void onPsuseAd() {

    }

    @Override
    public void onDestroyAd() {


    }
}
