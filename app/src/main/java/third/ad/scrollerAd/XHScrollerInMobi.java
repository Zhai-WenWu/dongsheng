package third.ad.scrollerAd;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.inmobi.ads.InMobiAdRequestStatus;
import com.inmobi.ads.InMobiNative;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import acore.logic.AppCommon;
import acore.override.XHApplication;
import acore.override.helper.XHActivityManager;
import acore.tools.ToolsDevice;
import xh.basic.tool.UtilString;

/**
 * inMobi广告
 */
public class XHScrollerInMobi extends XHScrollerAdParent{
    private String tag="zhangyujian";
    private Activity activity;
    private  InMobiNative nativeAd;
    private Map<String,String> mapData;//获取数据集合
    private String adid;
    public XHScrollerInMobi(Activity activity,String data,String mAdPlayId, int num) {
        super(mAdPlayId, num);
        this.activity= activity;
        LinkedHashMap<String, String> map_link = UtilString.getMapByString(data, "&", "=");
        if(map_link.containsKey("adid"))
            adid=map_link.get("adid");
        key="sdk_inmobi";
    }
   public XHScrollerInMobi(String data,String mAdPlayId, int num) {
        super(mAdPlayId, num);
        LinkedHashMap<String, String> map_link = UtilString.getMapByString(data, "&", "=");
        if(map_link.containsKey("adid"))
            adid=map_link.get("adid");
        key="sdk_inmobi";
    }

    @Override
    public void onResumeAd(String oneLevel,String twoLevel) {
        if(nativeAd!=null&&this.view!=null){
            Log.i(tag,"广告展示:::"+XHScrollerAdParent.ADKEY_INMOBI+":::位置::"+twoLevel);
            InMobiNative.bind(this.view,nativeAd);
            onAdShow(oneLevel,twoLevel,key);
        }
    }
    @Override
    public void onPsuseAd() {
        view=null;
    }

    @Override
    public void onThirdClick(String oneLevel,String twoLevel) {
        if(nativeAd!=null){
            Log.i(tag,"广告点击:::"+XHScrollerAdParent.ADKEY_INMOBI+":::位置::"+twoLevel);
            nativeAd.reportAdClick(null); //此方法参数通常传null}
            onAdClick(oneLevel,twoLevel,key);
        }
        //实现点击跳转页面
        if(mapData!=null&&mapData.containsKey("landingURL")){
//            AppCommon.openUrl(activity,mapData.get("landingURL"),true);
            AppCommon.openUrl(XHActivityManager.getInstance().getCurrentActivity(),mapData.get("landingURL"),true);
        }
    }

    @Override
    public void getAdDataWithBackAdId(@NonNull final XHAdDataCallBack xhAdDataCallBack) {
        if(!isShow()){//判断是否显示---不显示
            xhAdDataCallBack.onFail(XHScrollerAdParent.ADKEY_INMOBI);
            return;
        }
        if(TextUtils.isEmpty(adid)){
            xhAdDataCallBack.onFail(XHScrollerAdParent.ADKEY_INMOBI);
            return; }
        Handler handler= new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
//                nativeAd = new InMobiNative(activity, Long.parseLong(adid),
                nativeAd = new InMobiNative(XHActivityManager.getInstance().getCurrentActivity(), Long.parseLong(adid),
                        new InMobiNative.NativeAdListener() {
                            @Override
                            public void onAdLoadSucceeded(InMobiNative inMobiNative) {
                                try {
                                    //成功
                                    JSONObject content = new JSONObject((String) inMobiNative.getAdContent());
                                    //json字符串解析数据
                                    if(!TextUtils.isEmpty(content.getString("title"))&&!TextUtils.isEmpty(content.getString("landingURL"))){
                                        Map<String,String> map = new HashMap<>();
                                        map.put("type",XHScrollerAdParent.ADKEY_INMOBI);
                                        map.put("title",content.getString("title"));
                                        map.put("landingURL",content.getString("landingURL"));
                                        map.put("desc",content.getString("description"));
                                        map.put("iconUrl",content.getJSONObject("icon").getString("url"));
                                        map.put("imgUrl",content.getJSONObject("screenshots").getString("url"));
                                        map.put("hide","1");//2隐藏，1显示

                                        if(!TextUtils.isEmpty(content.getJSONObject("screenshots").getString("url"))&&
                                                !TextUtils.isEmpty(content.getString("title"))){
                                            mapData= map;
                                            xhAdDataCallBack.onSuccees(XHScrollerAdParent.ADKEY_INMOBI, map);
                                        }else {
                                            xhAdDataCallBack.onFail(XHScrollerAdParent.ADKEY_INMOBI);
                                        }
                                    }else
                                        xhAdDataCallBack.onFail(XHScrollerAdParent.ADKEY_INMOBI);

                                    //回调数据
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onAdLoadFailed(InMobiNative inMobiNative, InMobiAdRequestStatus inMobiAdRequestStatus) {
                                //失败
                                xhAdDataCallBack.onFail(XHScrollerAdParent.ADKEY_INMOBI);
                            }
                            @Override
                            public void onAdDismissed(InMobiNative inMobiNative) {
                                //广告被点击之后回到app
                            }
                            @Override
                            public void onAdDisplayed(InMobiNative inMobiNative) {
                            }
                            @Override
                            public void onUserLeftApplication(InMobiNative inMobiNative) {
                            }
                        });
                //加载广告
                nativeAd.load();
                Map<String, String> map = new HashMap<>();
                map.put("x-forwarded-for", "8.8.8.8");
                map.put("iem", ToolsDevice.getPhoneIMEI(XHApplication.in()));
                nativeAd.setExtras(map);
            }
        });

    }
    /**
     * inmobi的返回数据结构
     * {
     *  "landingURL":""
     *  "icon":{
     *          "aspectRatio":""
     *          "url":""
     *          "height":""
     *          "width":""
     *          }
     *   "title":""
     *   "rating":""
     *   "cta":""
     *   "screenshots":{
     *           "aspectRatio":""
     *           "url":""
     *           "height":""
     *           "width":""
     *           }
     *   "description":""
     * }
     *
     */
}
