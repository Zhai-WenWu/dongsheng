package third.ad.tools;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.inmobi.ads.InMobiAdRequestStatus;
import com.inmobi.ads.InMobiNative;
import com.inmobi.sdk.InMobiSdk;

import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

/**
 * InMobi，sdk使用封装 --单例模式
 * 存储InMobi的基本信息
 * 1、初始化操作
 * 2、请求信息流数据
 * 3、请求banner数据
 */
public class InMobiAdTools {
    private String tag = "zhangyujian";
    //appId---初始化
    public static final String ACCOUNTID = "15883ed431c444b6b7bd97db35f2285c";

    private volatile static InMobiAdTools inMobiAdTools = null;

    private InMobiAdTools() {
    }

    //对单例模式加锁
    public static InMobiAdTools getInstance() {
        if (inMobiAdTools == null) {
            synchronized (InMobiAdTools.class) {
                if (inMobiAdTools == null)
                    inMobiAdTools = new InMobiAdTools();
            }
        }
        return inMobiAdTools;
    }

    /**
     * 初始化
     *
     * @param context
     */
    public void initSdk(Context context) {
        InMobiSdk.init(context, ACCOUNTID);
        InMobiSdk.setLogLevel(InMobiSdk.LogLevel.DEBUG);
    }

    /**
     * 信息流广告数据获取
     *
     * @param activity
     * @param PLACEMENT_ID
     */
    public void getData(Activity activity, long PLACEMENT_ID, final InMobiNativeCallBack inMobiNativeCallBack) {
        Log.i(tag, "PLACEMENT_ID::" + PLACEMENT_ID);
        //创建对象
        final InMobiNative nativeAd = new InMobiNative(activity, PLACEMENT_ID,
                new InMobiNative.NativeAdListener() {
                    @Override
                    public void onAdLoadSucceeded(InMobiNative inMobiNative) {
                        try {
                            Log.i(tag, "onAdLoadSucceeded");
                            //成功
                            JSONObject content = new JSONObject((String) inMobiNative.getAdContent());
                            //json字符串解析数据
                            String title = content.getString(AdJsonKeys.AD_TITLE);
                            String landingUrl = content.getString(AdJsonKeys.AD_CLICK_URL);
                            String imageUrl = content.getJSONObject(AdJsonKeys.AD_IMAGE_OBJECT).
                                    getString(AdJsonKeys.AD_IMAGE_URL);
                            //回调数据
                            inMobiNativeCallBack.nativeDataCallBack(title,landingUrl,imageUrl,new WeakReference<>(inMobiNative));
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onAdLoadFailed(InMobiNative inMobiNative, InMobiAdRequestStatus inMobiAdRequestStatus) {
                        //失败
                        Log.i(tag, "onAdLoadFailed");
                    }

                    @Override
                    public void onAdDismissed(InMobiNative inMobiNative) {
                        //广告被点击之后回到app
                        Log.i(tag, "onAdDismissed");
                    }

                    @Override
                    public void onAdDisplayed(InMobiNative inMobiNative) {
                        Log.i(tag, "onAdDisplayed");
                    }

                    @Override
                    public void onUserLeftApplication(InMobiNative inMobiNative) {
                        Log.i(tag, "onUserLeftApplication");
                    }
                });
        //加载广告
        nativeAd.load();
        Map<String, String> map = new HashMap<>();
        map.put("x-forwarded-for", "8.8.8.8");
        nativeAd.setExtras(map);
        //展示点击
    }

    public void getData() {
    }

    public void onAdClick() {
    }
    /**
     *信息流广告数据回调
     */
    public interface InMobiNativeCallBack {
        public void nativeDataCallBack(String title, String landingUrl, String imageUrl, WeakReference<InMobiNative> nativeAd);
    }
    public void setInMobiNativeCallBack(InMobiNativeCallBack inMobiNativeCallBack){
        this.inMobiNativeCallBack= inMobiNativeCallBack;
    }
    private InMobiNativeCallBack inMobiNativeCallBack;
    /**
     * 信息流数据实体
     */
    public interface AdJsonKeys {
        String AD_TITLE = "title";//标题
        String AD_CLICK_URL = "landingURL";//点击跳转url
        String AD_IMAGE_OBJECT = "icon";//图片的url
        String AD_IMAGE_URL = "url";//图片的url
    }
}
