package third.ad;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.BitmapRequestBuilder;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.Target;
import com.qq.e.ads.nativ.NativeADDataRef;
import com.xiangha.R;

import java.util.List;
import java.util.Random;

import acore.override.XHApplication;
import acore.tools.ToolsDevice;
import aplug.basic.LoadImage;
import aplug.basic.SubBitmapTarget;
import third.ad.tools.GdtAdTools;
import xh.basic.tool.UtilImage;

public class GdtAdNew extends AdParent {
    private List<NativeADDataRef> nativeADDataRefList;

    private Activity mAct;
    private Context mCon;
    private RelativeLayout mAdLayout;
    private int mResouceId;
    private String mAdId;
    private Bitmap mBmp;
    private int mAdWhat, mImgMaxW = 0, mImgMinH = 0, mRefreshTime;
    private AdListener mListener;
    /**
     * 首页的点击事件比较特殊，故加标记特效处理
     */
    public boolean isMain = false;
    private String mFrom;

    public GdtAdNew(Context con, String from, RelativeLayout adLayout, String adId,
                    final int resouceId, int adWhat, AdListener listener) {
        init(con, from, adLayout, adId, adWhat, resouceId, 0, 0, listener);
    }

    public GdtAdNew(Context con, String from, RelativeLayout adLayout, String adId,
                    int adWhat[], final int[] resoucesId, AdListener listener) {
        Random random = new Random();
        int randIndex = random.nextInt(adWhat.length);
        init(con, from, adLayout, adId, adWhat[randIndex], resoucesId[randIndex], 0, 0, listener);
    }

    public GdtAdNew(Context con, String from, RelativeLayout adLayout, String adId, final int resouceId,
                    int imgMaxW, int imgMinH, int adWhat, AdListener listener) {
        init(con, from, adLayout, adId, adWhat, resouceId, imgMaxW, imgMinH, listener);
    }

    public GdtAdNew(Activity act, String from, RelativeLayout rl, int refreshTime, String bannerId, int adWhat) {
        mAct = act;
        mFrom = from;
        mAdLayout = rl;
        mRefreshTime = refreshTime;
        mAdId = bannerId;
        mAdWhat = adWhat;
    }

    private void init(Context con, String from, RelativeLayout adLayout, String adId, int adWhat, final int resouceId, int imgMaxW, int imgMinH, AdListener listener) {
        mCon = con;
        mFrom = from;
        mAdLayout = adLayout;
        mAdId = adId;
        mAdWhat = adWhat;
        mResouceId = resouceId;
        mImgMaxW = imgMaxW;
        mImgMinH = imgMinH;
        mListener = listener;
    }


    @Override
    public boolean isShowAd(String adPlayId, final AdIsShowListener listener) {
        boolean isShow = super.isShowAd(adPlayId, listener);
        if (isShow) {
            //banner广告
            if (mAdWhat == CREATE_AD_BANNER) {
                listener.onIsShowAdCallback(this, true);
            } else {
                GdtAdTools.newInstance().loadNativeAD(mCon, mAdId, 1,
                        new GdtAdTools.GdtNativeCallback() {
                            @Override
                            public void onNativeLoad(List<NativeADDataRef> data) {
                                if (data.size() > 0) {
                                    nativeADDataRefList = data;
                                    listener.onIsShowAdCallback(GdtAdNew.this, true);
                                } else {
                                    listener.onIsShowAdCallback(GdtAdNew.this, false);
                                }
                            }

                            @Override
                            public void onADStatusChanged(NativeADDataRef nativeADDataRef) {
                            }

                            @Override
                            public void onNativeFail(NativeADDataRef nativeADDataRef, String msg) {
                            }
                        });
            }
        } else {
            listener.onIsShowAdCallback(this, isShow);
        }
        return isShow;
    }

    @Override
    public void onResumeAd() {
        onAdShow(mFrom, TONGJI_GDT);
        View view = null;
        mAdLayout.setVisibility(View.VISIBLE);
        switch (mAdWhat) {
            case CREATE_AD:
                Log.i("tzy","CREATE_AD");
                if (mAdLayout.getChildCount() > 0) {
                    view = mAdLayout.getChildAt(0);
                } else {
                    view = LayoutInflater.from(mCon).inflate(mResouceId, mAdLayout);
                    view.setVisibility(View.GONE);
                    if (mListener != null) mListener.onAdCreate();
                }
                initAd(view);
                break;
            case CREATE_AD_RANDOM:
                Log.i("tzy","CREATE_AD_RANDOM");
            case CREATE_AD_SHADE:
                Log.i("tzy","CREATE_AD_SHADE");
                if (mAdLayout.getChildCount() > 0) {
                    view = mAdLayout.getChildAt(0);
                } else {
                    view = LayoutInflater.from(mCon).inflate(mResouceId, mAdLayout);
                    view.setVisibility(View.GONE);
                    if (mListener != null) mListener.onAdCreate();
                }
                Log.i("tzy","CREATE_AD_SHADE");
                createNativeADRandom(view, mAdWhat);
                break;
            case CREATE_AD_BANNER:
                Log.i("tzy","CREATE_AD_BANNER");
                if (mListener != null)
                    mListener.onAdCreate();
                GdtAdTools.newInstance().showBannerAD(mAct, mAdLayout, mRefreshTime, mAdId, new GdtAdTools.onBannerAdListener() {
                    @Override
                    public void onClick() {
                        onAdClick(mFrom, TONGJI_GDT);
                    }
                });
                break;
            default:
                Toast.makeText(mCon, "传的adWhat不对", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    public void onPsuseAd() {

    }

    @Override
    public void onDestroyAd() {
        if (mAdWhat == CREATE_AD_BANNER) {

        }
    }

    private void initAd(final View adView) {
        GdtAdTools.newInstance().getNativeData(adView, nativeADDataRefList.get(0),
                GdtAdTools.newInstance().new AddAdView() {
                    @Override
                    public void addAdView(final String title, final String desc, String iconUrl,
                                          String imageUrl, OnClickListener clickListener) {
                        String url = "";
                        if (!TextUtils.isEmpty(imageUrl)) {
                            url = imageUrl;
                        } else if (!TextUtils.isEmpty(iconUrl)) {
                            url = iconUrl;
                        }
                        BitmapRequestBuilder<GlideUrl, Bitmap> bitmapRequest = LoadImage.with(XHApplication.in())
                                .load(url)
                                .setRequestListener(new RequestListener<GlideUrl, Bitmap>() {
                                    @Override
                                    public boolean onResourceReady(Bitmap arg0, GlideUrl arg1, Target<Bitmap> arg2, boolean arg3, boolean arg4) {
                                        return false;
                                    }

                                    @Override
                                    public boolean onException(Exception arg0, GlideUrl arg1, Target<Bitmap> arg2, boolean arg3) {
                                        return false;
                                    }
                                })
                                .build();
                        if (bitmapRequest != null)
                            bitmapRequest.into(new SubBitmapTarget() {

                                @Override
                                public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> arg1) {
                                    if (bitmap.getHeight() < mImgMinH) {
                                        adView.setVisibility(View.GONE);
                                    } else {
                                        final ImageView imgView = (ImageView) adView.findViewById(R.id.view_ad_img);
//								imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                                        final TextView textView = (TextView) adView.findViewById(R.id.view_ad_text);
                                        mBmp = bitmap;
                                        textView.setText(title + "，" + desc);
                                        UtilImage.setImgViewByWH(imgView, mBmp, mImgMaxW, 0, false);
                                        adView.setVisibility(View.VISIBLE);
                                    }
                                }
                            });
                    }

                    @Override
                    public void onClick() {
                        super.onClick();
                        onAdClick(mFrom, TONGJI_GDT);
                    }
                }, isMain);
//        GdtAdTools.newInstance().getData(adView, listData.get(0), GdtAdTools.newInstance().new AddAdView() {
//
//            @Override
//            public void addAdView(final String title, final String desc, String iconUrl, String imageUrl, final OnClickListener clickListener) {
//                String url = "";
//                if (!TextUtils.isEmpty(imageUrl)) {
//                    url = imageUrl;
//                } else if (!TextUtils.isEmpty(iconUrl)) {
//                    url = iconUrl;
//                }
//                BitmapRequestBuilder<GlideUrl, Bitmap> bitmapRequest = LoadImage.with(XHApplication.in())
//                        .load(url)
//                        .setRequestListener(new RequestListener<GlideUrl, Bitmap>() {
//                            @Override
//                            public boolean onResourceReady(Bitmap arg0, GlideUrl arg1, Target<Bitmap> arg2, boolean arg3, boolean arg4) {
//                                return false;
//                            }
//
//                            @Override
//                            public boolean onException(Exception arg0, GlideUrl arg1, Target<Bitmap> arg2, boolean arg3) {
//                                return false;
//                            }
//                        })
//                        .build();
//                if (bitmapRequest != null)
//                    bitmapRequest.into(new SubBitmapTarget() {
//
//                        @Override
//                        public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> arg1) {
//                            if (bitmap.getHeight() < mImgMinH) {
//                                adView.setVisibility(View.GONE);
//                            } else {
//                                final ImageView imgView = (ImageView) adView.findViewById(R.id.view_ad_img);
////								imageView.setScaleType(ImageView.ScaleType.FIT_XY);
//                                final TextView textView = (TextView) adView.findViewById(R.id.view_ad_text);
//                                mBmp = bitmap;
//                                textView.setText(title + "，" + desc);
//                                UtilImage.setImgViewByWH(imgView, mBmp, mImgMaxW, 0, false);
//                                adView.setVisibility(View.VISIBLE);
//                            }
//                        }
//                    });
//            }
//
//            @Override
//            public void onClick() {
//                super.onClick();
//                onAdClick(mFrom, TONGJI_GDT);
//            }
//
//        }, isMain);
    }

    /**
     * 创建图比例为tag:3:2?屏幕宽，高度为banner广告的高
     *
     * @param adView
     * @param tag
     */
    private void createNativeADRandom(final View adView, final int tag) {
        if (null != nativeADDataRefList && nativeADDataRefList.size() > 0) {
            GdtAdTools.newInstance().getNativeData(adView, nativeADDataRefList.get(0),
                    GdtAdTools.newInstance().new AddAdView() {
                        @Override
                        public void addAdView(final String title, final String desc, String iconUrl,
                                              String imageUrl, final OnClickListener clickListener) {
                            String url = "";
                            if (!TextUtils.isEmpty(imageUrl)) {
                                url = imageUrl;
                            } else if (!TextUtils.isEmpty(iconUrl)) {
                                url = iconUrl;
                            }
                            BitmapRequestBuilder<GlideUrl, Bitmap> bitmapRequest = LoadImage.with(XHApplication.in())
                                    .load(url)
                                    .setRequestListener(new RequestListener<GlideUrl, Bitmap>() {
                                        @Override
                                        public boolean onResourceReady(Bitmap arg0, GlideUrl arg1, Target<Bitmap> arg2, boolean arg3, boolean arg4) {
                                            return false;
                                        }

                                        @Override
                                        public boolean onException(Exception arg0, GlideUrl arg1, Target<Bitmap> arg2, boolean arg3) {
                                            adView.setVisibility(View.GONE);
                                            return false;
                                        }
                                    })
                                    .build();
                            if (bitmapRequest != null)
                                bitmapRequest.into(new SubBitmapTarget() {
                                    @Override
                                    public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> arg1) {
                                        initView(adView, title, desc, bitmap, tag);
                                    }
                                });
                        }

                        @Override
                        public void onClick() {
                            super.onClick();
                            onAdClick(mFrom, TONGJI_GDT);
                        }

                    }, isMain);
        }
    }

//    /**
//     * 创建图比例为tag:3:2?屏幕宽，高度为banner广告的高
//     *
//     * @param adView
//     * @param tag
//     */
//    private void createMSSPADRandom(final View adView, final int tag) {
//        if (listData != null && listData.size() > 0) {
//            GdtAdTools.newInstance().getData(adView, listData.get(0), GdtAdTools.newInstance().new AddAdView() {
//
//                @Override
//                public void addAdView(final String title, final String desc, String iconUrl, String imageUrl, final OnClickListener clickListener) {
//                    String url = "";
//                    if (!TextUtils.isEmpty(imageUrl)) {
//                        url = imageUrl;
//                    } else if (!TextUtils.isEmpty(iconUrl)) {
//                        url = iconUrl;
//                    }
//                    BitmapRequestBuilder<GlideUrl, Bitmap> bitmapRequest = LoadImage.with(XHApplication.in())
//                            .load(url)
//                            .setRequestListener(new RequestListener<GlideUrl, Bitmap>() {
//                                @Override
//                                public boolean onResourceReady(Bitmap arg0, GlideUrl arg1, Target<Bitmap> arg2, boolean arg3, boolean arg4) {
//                                    return false;
//                                }
//
//                                @Override
//                                public boolean onException(Exception arg0, GlideUrl arg1, Target<Bitmap> arg2, boolean arg3) {
//                                    adView.setVisibility(View.GONE);
//                                    return false;
//                                }
//                            })
//                            .build();
//                    if (bitmapRequest != null)
//                        bitmapRequest.into(new SubBitmapTarget() {
//                            @Override
//                            public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> arg1) {
//                                initView(adView, title, desc, bitmap, tag);
//                            }
//                        });
//                }
//
//                @Override
//                public void onClick() {
//                    super.onClick();
//                    onAdClick(mFrom, TONGJI_GDT);
//                }
//
//            }, isMain);
//        }
//    }

    private void initView(View adView, String title, String desc, Bitmap bmp, int tag) {
        ImageView imgView = (ImageView) adView.findViewById(R.id.view_ad_img);
//		imgView.setScaleType(ImageView.ScaleType.FIT_XY);
        int imgH = 280, imgW = 1160;
        int windW = imgView.getWidth() > 0 ? imgView.getWidth() : ToolsDevice.getWindowPx(mCon).widthPixels;
        int viewH = windW * imgH / imgW;
        int viewW = windW;
        switch (tag) {
            case CREATE_AD_SHADE:
                viewW = windW;
                break;
            case CREATE_AD_RANDOM:
                viewW = viewH / 2 * 3;
                break;
        }
        initTitle(adView, title, desc);
        android.view.ViewGroup.LayoutParams lp = UtilImage.setImgViewByWH(imgView, bmp, viewW, viewH, true);
        if (adView.findViewById(R.id.view_ad_content_layout) != null) {
            RelativeLayout contentLayout = (RelativeLayout) adView.findViewById(R.id.view_ad_content_layout);
            RelativeLayout.LayoutParams params = (LayoutParams) contentLayout.getLayoutParams();
            params.width = RelativeLayout.LayoutParams.MATCH_PARENT;
            params.height = lp.height;
            contentLayout.setLayoutParams(params);
        }
        adView.setVisibility(View.VISIBLE);
    }

    private void initTitle(View adView, String title, String desc) {
        TextView titleView = (TextView) adView.findViewById(R.id.view_ad_text);
        String bastTitle = title;
        if (title.length() < 10) {
            bastTitle += "，" + desc.substring(0, 10 - title.length());
            if (desc.length() > 10 - title.length())
                bastTitle += "...";
        } else {
            bastTitle = bastTitle.subSequence(0, 10) + "...";
        }
        titleView.setText(bastTitle);
    }

    /** 信息流广告，先判断有imag用img，没有用icon，图片大小由传过来的控件控制 */
    public static final int CREATE_AD = 1099;
    /** 信息流广告，只显示大图，图片大小默认定宽不定高，可以通过参数传过来 */
    public static final int CREATE_AD_WH_MAX = 1100;
    public static final int CREATE_AD_SHADE = 1101;
    public static final int CREATE_AD_RANDOM = 1102;
    /**
     * banner广告
     */
    public static final int CREATE_AD_BANNER = 1103;
    /**
     * 信息流广告，只显示大图，图片大小默认定宽不定高
     */
    public static final int CREATE_AD_BIG_BITMAP = 1102;
}
