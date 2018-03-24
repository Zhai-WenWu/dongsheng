package third.ad.tools;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.qq.e.ads.banner.ADSize;
import com.qq.e.ads.banner.AbstractBannerADListener;
import com.qq.e.ads.banner.BannerView;
import com.qq.e.ads.nativ.NativeAD;
import com.qq.e.ads.nativ.NativeADDataRef;
import com.qq.e.ads.splash.SplashAD;
import com.qq.e.ads.splash.SplashADListener;

import java.util.List;

import acore.widget.ScrollLinearListLayout;

public class GdtAdTools {

    private final String APPID = "1150004142";

    private static volatile GdtAdTools mGdtAdTools;

    private GdtAdTools() {
    }

    public static GdtAdTools newInstance() {
        if (mGdtAdTools == null) {
            mGdtAdTools = new GdtAdTools();
        }
        return mGdtAdTools;
    }

    /**
     * 设置 开屏广告
     *
     * @param act           展示广告的activity
     * @param parent        展示广告的大容器
     * @param skipContainer 自定义的跳过按钮
     * @param adid          广告id
     * @param gdtListener   回调
     */
    public void showSplashAD(Activity act, ViewGroup parent, View skipContainer, String adid,
                             final GdtSplashAdListener gdtListener) {
        SplashAD splashAD = new SplashAD(act, parent, skipContainer, APPID, adid,
                new SplashADListener() {
                    @Override
                    public void onADDismissed() {
                        if (null != gdtListener) {
                            gdtListener.onAdDismissed();
                        }
                    }

                    @Override
                    public void onNoAD(int i) {
                        if (null != gdtListener) {
                            gdtListener.onAdFailed("onNoAD " + String.valueOf(i));
                        }
                    }

                    @Override
                    public void onADPresent() {
                        if (null != gdtListener) {
                            gdtListener.onAdPresent();
                        }
                    }

                    @Override
                    public void onADClicked() {
                        if (null != gdtListener) {
                            gdtListener.onAdClick();
                        }
                    }

                    @Override
                    public void onADTick(long l) {
                        if (null != gdtListener) {
                            gdtListener.onADTick(l);
                        }
                    }
                }, 50000);
    }

    /**
     * 创建Banner广告AdView对象
     *
     * @param parent      广告容器
     * @param act
     * @param adid        广告id
     * @param refreshTime 广告轮播时间，为0或30~120之间的数字，单位为s,0标识不自动轮播
     * @param listener
     */
    public void showBannerAD(Activity act, RelativeLayout parent, int refreshTime, String adid,
                             final onBannerAdListener listener) {
        BannerView bannerView = new BannerView(act, ADSize.BANNER, APPID, adid);
        bannerView.setRefresh(refreshTime);
        bannerView.setADListener(new AbstractBannerADListener() {
            @Override
            public void onADClicked() {
                super.onADClicked();
                if (null != listener) {
                    listener.onClick();
                }
            }

            @Override
            public void onNoAD(int arg0) {
            }

            @Override
            public void onADReceiv() {
            }
        });
        parent.addView(bannerView);
        // 发起广告请求，收到广告数据后会展示数据
        bannerView.loadAD();
    }

    /**
     * 信息流广告获取
     *
     * @param context  上下文
     * @param adid     广告id
     * @param adCount  // 一次拉取的广告条数：范围1-30
     * @param callback 回调
     */
    public void loadNativeAD(Context context, String adid, int adCount, final GdtNativeCallback callback) {
        NativeAD nativeAD = new NativeAD(context, APPID, adid,
                new NativeAD.NativeAdListener() {
                    @Override
                    public void onADLoaded(List<NativeADDataRef> list) {
                        Log.i("tzy", "GDT NactiveAD loaded");
                        if (null != callback) {
                            callback.onNativeLoad(list);
                        }
                    }

                    @Override
                    public void onNoAD(int i) {
                        Log.i("tzy", "GDT NactiveAD onNoAD");
                        if (null != callback) {
                            callback.onNativeFail(null, "onNoAD:code = " + i);
                        }
                    }

                    @Override
                    public void onADStatusChanged(NativeADDataRef nativeADDataRef) {
//                        Log.i("tzy", "GDT NactiveAD onADStatusChanged");
                        if (null != callback) {
                            callback.onADStatusChanged(nativeADDataRef);
                        }
                    }

                    @Override
                    public void onADError(NativeADDataRef nativeADDataRef, int i) {
                        Log.i("tzy", "GDT NactiveAD onADError");
                        if (null != callback) {
                            callback.onNativeFail(nativeADDataRef, "adError:code = " + i);
                        }
                    }
                });
//        Log.i("tzy", "GDT NactiveAD start load");
        nativeAD.loadAD(adCount);// 一次拉取的广告条数：范围1-30
    }

    /**
     * 信息流广告 内容获取
     * @param view
     * @param nativeADDataRef
     * @param callback
     */
    public void getNativeData(View view, final NativeADDataRef nativeADDataRef, final AddAdView callback) {
        if(nativeADDataRef == null){
            return;
        }
        String title = nativeADDataRef.getTitle();
        String desc = nativeADDataRef.getDesc();
        String iconUrl = nativeADDataRef.getIconUrl();
        String imageUrl = nativeADDataRef.getImgUrl();
        if (null != view) {
            final OnClickListener clickListener = new OnClickListener() {
                @Override
                public void onClick(View v) {
                    nativeADDataRef.onClicked(v);
                    callback.onClick();
                }
            };
            view.setOnClickListener(clickListener);
            nativeADDataRef.onExposured(view);
        }
        callback.addAdView(title, desc, iconUrl, imageUrl, null);
    }

    /** 开屏AD接口 */
    public interface GdtSplashAdListener {
        public void onAdPresent();

        public void onAdFailed(String reason);

        public void onAdDismissed();

        public void onAdClick();

        public void onADTick(long millisUntilFinished);
    }

    /**
     * 信息流广告获取内容时的灰度接口
     */
    public abstract class AddAdView {
        public abstract void addAdView(String title, String desc, String iconUrl, String imageUrl, OnClickListener clickListener);

        public void onClick() {
        }
    }

    /** 信息流获取数据的接口 */
    public interface GdtNativeCallback {
        public void onNativeLoad(List<NativeADDataRef> data);

        public void onADStatusChanged(NativeADDataRef nativeADDataRef);

        public void onNativeFail(NativeADDataRef nativeADDataRef, String msg);
    }

    public interface onBannerAdListener {
        public void onClick();
    }
}
