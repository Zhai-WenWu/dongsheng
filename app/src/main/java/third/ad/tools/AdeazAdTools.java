package third.ad.tools;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.webkit.WebView;

import com.download.container.DownloadCallBack;
import com.download.down.DownLoad;
import com.download.tools.FileUtils;
import com.xianghatest.R;

import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import acore.logic.AppCommon;
import acore.tools.FileManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;
import xh.basic.internet.UtilInternet;
import xh.basic.tool.UtilString;

import static xh.basic.tool.UtilString.getListMapByJson;

/**
 * Created by Fang Ruijiao on 2016/8/30.
 */
public class AdeazAdTools {
    //广告id list
    public static final String MIAN_HOME = "1016693";
    public static final String DISH_COMMEND_1 = "1016693";
    public static final String DISH_COMMEND_2 = "1016693";
    public static final String SEARCH_DISH_LIST_1 = "1016694";
    public static final String SEARCH_DISH_LIST_2 = "1016695";
    public static final String SEARCH_DISH_LIST_3 = "1016696";

    //测试url
//    private static final String url = "http://sx.g.fastapi.net/s2s?";
    private static final String url = "http://x.fastapi.net/s2s?";

    public static void getSmsAdData(Context con,String adId,final OnSmsAdCallback callback){
        StringBuffer adUrl = new StringBuffer(url);
        getAdData(con,adUrl,adId);

        final Map<String, Boolean> isCallBack = new HashMap<>();
        isCallBack.put("isCallBack",false);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(!isCallBack.get("isCallBack")){
                    isCallBack.put("isCallBack",true);
                    callback.onAdFail();
                }
            }
        },2 * 1000);
        ReqInternet.in().doGet(adUrl.toString(), new InternetCallback(con) {
            @Override
            public void loaded(int flag, String url, Object returnObj) {
                if(isCallBack.get("isCallBack")) return;
                isCallBack.put("isCallBack",true);
                if (flag >= UtilInternet.REQ_OK_STRING) {
                    ArrayList<Map<String, String>> listReturn = getListMapByJson(returnObj);
                    if(listReturn == null || listReturn.size() == 0 || !listReturn.get(0).containsKey("ad")) {
                        callback.onAdFail();
                        return;
                    }
                    listReturn = getListMapByJson(listReturn.get(0).get("ad"));
                    if(listReturn == null || listReturn.size() == 0) {
                        callback.onAdFail();
                        return;
                    }
                    callback.onAdShow(listReturn);
                }else{
                    callback.onAdFail();
                }
            }
        });
    }

    private static void getAdData(Context con,StringBuffer adUrl,String adId) {
        adUrl.append("ip=");
        adUrl.append(URLEncoder.encode("client"));
        adUrl.append("&ua=");
        adUrl.append(URLEncoder.encode(new WebView(con).getSettings().getUserAgentString()));
        adUrl.append("&si=");
        adUrl.append(URLEncoder.encode(adId));
        adUrl.append("&v=");
        adUrl.append(URLEncoder.encode("1.2"));
        adUrl.append("&identify_type=");
        adUrl.append(URLEncoder.encode("imei"));

        JSONObject object = new JSONObject();
        try{
            object.put("udid", ToolsDevice.getPhoneIMEI(con));
            object.put("identify_type","imei");
            object.put("android_id",ToolsDevice.getAndroidId(con));
            object.put("vendor",android.os.Build.MANUFACTURER);//厂商，例如：MI 3
            object.put("model",android.os.Build.MODEL);//型号：4.4.1
            object.put("os",1);//系统
            object.put("os_version",android.os.Build.VERSION.RELEASE);//系统版本号：5.0.1
            object.put("network",ToolsDevice.getNetWorkSimpleNum(con));//联网方式 0: 其它，1: WIFI，2:2G，3: 3G，4: 4G
            object.put("operator",ToolsDevice.getOperatorNum(con));//运营商 0: 其它 1：移动，2：联通，3：电信
            DisplayMetrics metric = ToolsDevice.getWindowPx(con);
            object.put("width",metric.widthPixels);
            object.put("height",metric.heightPixels);
        }catch (Exception e){
            e.printStackTrace();
        }
        adUrl.append("&device=" + object.toString());
    }

    public static void onAdClick(final Activity act, Map<String, String> map){
        try{
            if("0".equals(map.get("action"))){
                try {
                    final DownLoad downLoad = new DownLoad(act);
                    downLoad.setNotifaction("开始下载", map.get("title") + ".apk", "正在下载", R.drawable.ic_launcher, false);
                    downLoad.starDownLoad(map.get("url"), FileManager.getCameraDir(), map.get("title"), true, new DownloadCallBack() {

                        @Override
                        public void starDown() {
                            super.starDown();
                            Tools.showToast(act, "开始下载");
                        }

                        @Override
                        public void downOk(Uri uri) {
                            super.downOk(uri);
                            FileUtils.install(act, uri);
                            downLoad.cacelNotification();
                        }

                        @Override
                        public void downError(String s) {
                            Tools.showToast(act, "下载失败：" + s);
                            downLoad.cacelNotification();
                        }
                    });
                } catch (Exception e) {
                    Tools.showToast(act, "下载异常");
                    e.printStackTrace();
                }
            }else{
                AppCommon.openUrl(act,map.get("url"),false);
            }
            String clk = map.get("clk");
            ArrayList<Map<String, String>> listReturn = getListMapByJson(clk);
            if(listReturn != null && listReturn.size() > 0) {
                for(int i = 0 ; i < listReturn.size(); i++){
                    doGetOnAdClick(act,listReturn.get(i).get(""));
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void onShowAd(final Context con, Map<String,String> map){
        try{
            String imp = map.get("imp");
            ArrayList<Map<String, String>> listReturn = getListMapByJson(imp);
            if(listReturn != null && listReturn.size() > 0) {
                Map<String, String> clickMap = listReturn.get(0);
                for(String key : clickMap.keySet()){
    //                Log.i("FRJ","key:" + key);
    //                Log.i("FRJ","values:" + clickMap.get(key));
                    final long delay = Integer.parseInt(key) * 1000;
                    final ArrayList<Map<String, String>> clickArray = UtilString.getListMapByJson(clickMap.get(key));
                    if(clickArray != null && clickArray.size() > 0){
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                for(int i = 0 ; i < clickArray.size(); i++){
                                    doGetOnAdClick(con,clickArray.get(i).get(""));
                                }
                            }
                        },delay);
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private static void doGetOnAdClick(Context con,String url){
        ReqInternet.in().doGet(url, new InternetCallback(con) {
            @Override
            public void loaded(int flag, String url, Object returnObj) {

            }
        });
    }

    public interface OnSmsAdCallback{
        public void onAdShow(ArrayList<Map<String, String>> listReturn);
        public void onAdFail();
    }
}
