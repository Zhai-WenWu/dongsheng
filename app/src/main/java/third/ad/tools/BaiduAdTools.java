package third.ad.tools;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.ViewGroup;

import com.baidu.mobad.feeds.BaiduNative;
import com.baidu.mobad.feeds.NativeErrorCode;
import com.baidu.mobad.feeds.NativeResponse;
import com.baidu.mobad.feeds.RequestParameters;
import com.baidu.mobads.SplashAd;
import com.baidu.mobads.SplashAdListener;

import java.util.List;

/**
 * PackageName : third.ad.tools
 * Created by MrTrying on 2017/7/10 10:47.
 * E_mail : ztanzeyu@gmail.com
 */

public class BaiduAdTools {

    private static volatile BaiduAdTools mBaiduAdTools;

    public static BaiduAdTools newInstance(){
        if(mBaiduAdTools == null){
            mBaiduAdTools = new BaiduAdTools();
        }
        return mBaiduAdTools;
    }

    /**
     * 百度开屏广告
     * @param activity
     * @param parent 父布局
     * @param adid 广告位id
     * @param callback 回调
     */
    public void showSplashAD(Activity activity, ViewGroup parent,String adid,final BaiduSplashAdCallback callback){
//       //YLKLog.i("tzy_AD","showSplashAD");
        SplashAd splashAd = new SplashAd(activity, parent, new SplashAdListener() {
            @Override
            public void onAdPresent() {
//               //YLKLog.i("tzy_AD","baidu splash onAdPresent");
                if(null != callback)
                    callback.onAdPresent();
            }

            @Override
            public void onAdDismissed() {
//               //YLKLog.i("tzy_AD","baidu splash onAdDismissed");
                if(null != callback)
                    callback.onAdDismissed();
            }

            @Override
            public void onAdFailed(String s) {
//               //YLKLog.i("tzy_AD","baidu splash onAdFailed : msg = " + s);
                if(null != callback)
                    callback.onAdFailed(s);
            }

            @Override
            public void onAdClick() {
//               //YLKLog.i("tzy_AD","baidu splash onAdClick");
                if(null != callback)
                    callback.onAdClick();

            }
        },adid,true);
    }

    public void loadNativeAD(Context context, String adid, final BaiduNativeCallbck callback){
        BaiduNative baiduNative = new BaiduNative(context, adid, new BaiduNative.BaiduNativeNetworkListener() {
            @Override
            public void onNativeLoad(List<NativeResponse> list) {
               //YLKLog.i("tzy", "onNativeLoad: ");
                if(null != callback)
                    callback.onNativeLoad(list);
            }

            @Override
            public void onNativeFail(NativeErrorCode nativeErrorCode) {
               //YLKLog.i("tzy", "onNativeFail: " + nativeErrorCode);
                if(null != callback)
                    callback.onNativeFail(nativeErrorCode);
            }
        });
        RequestParameters requestParameters = new RequestParameters.Builder()
                .downloadAppConfirmPolicy(RequestParameters.DOWNLOAD_APP_CONFIRM_ALWAYS)//总是询问
                .build();
        baiduNative.makeRequest(requestParameters);
    }

    public void getNativeData(final NativeResponse nativeResponse,final OnHandlerDataCallback callback){
        if(null == nativeResponse){
            return;
        }
        String title = nativeResponse.getTitle();
        String desc = nativeResponse.getDesc();
        String iconUrl = nativeResponse.getIconUrl();
        String imageUrl = nativeResponse.getImageUrl();
//        boolean isBigPic = nativeResponse.getMainPicWidth() >= 720;
        boolean isBigPic = nativeResponse.isDownloadApp();
        callback.onHandlerData(title,desc,iconUrl,imageUrl,isBigPic);
    }

    public static interface BaiduSplashAdCallback {
        public void onAdPresent();
        public void onAdDismissed();
        public void onAdFailed(String s);
        public void onAdClick();
    }

    public static interface BaiduNativeCallbck{
        public void onNativeLoad(List<NativeResponse> list);
        public void onNativeFail(NativeErrorCode nativeErrorCode);
    }

    /** 信息流广告获取内容时的灰度接口 */
    public static abstract class OnHandlerDataCallback {
        public abstract void onHandlerData(String title, String desc, String iconUrl, String imageUrl,boolean isBigPic);
    }

}
