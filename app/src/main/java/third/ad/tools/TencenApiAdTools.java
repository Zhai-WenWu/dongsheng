package third.ad.tools;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.qq.e.comm.util.Md5Util;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;

import acore.logic.AppCommon;
import acore.dialogManager.VersionOp;
import acore.override.XHApplication;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;
import third.location.LocationSys;
import xh.basic.tool.UtilFile;

import static xh.basic.tool.UtilString.getListMapByJson;

/**
 * Created by Fang Ruijiao on 2016/12/6.
 */
public class TencenApiAdTools {

    private volatile static TencenApiAdTools tencenApiAdTools;
    //-----------------广告id list-----------------------
    //首页知识id  尺寸：230*152   loid:101
    public static final String TX_ID_HOME_ZHISHI = "_adb_14678_10147654";
    //首页最新佳作 尺寸：640:330  loid:202
    public static final String TX_ID_HOME_GOOD_DISH_1 = "_adb_14678_10147655";
    public static final String TX_ID_HOME_GOOD_DISH_2 = "_adb_14678_10147656";
    public static final String TX_ID_HOME_GOOD_DISH_3 = "_adb_14678_10147657";
    public static final String TX_ID_HOME_GOOD_DISH_4 = "_adb_14678_10147658";
    public static final String TX_ID_HOME_GOOD_DISH_5 = "_adb_14678_10147659";
    public static final String TX_ID_HOME_GOOD_DISH_6 = "_adb_14678_10147660";
    //搜索默认页banner  尺寸：582:166  loid:1
    public static final String TX_ID_SEARCH_DIFAULT = "_adb_14678_10147661";
    //搜索菜谱列表  尺寸：230*152   loid:101
    public static final String TX_ID_SEARCH_1 = "_adb_14678_10147662";
    public static final String TX_ID_SEARCH_2 = "_adb_14678_10147663";
    public static final String TX_ID_SEARCH_3 = "_adb_14678_10147664";
    public static final String TX_ID_SEARCH_4 = "_adb_14678_10147665";
    public static final String TX_ID_SEARCH_5 = "_adb_14678_10147666";
    public static final String TX_ID_SEARCH_6 = "_adb_14678_10147667";
    //菜谱详情用料上方banner   尺寸：582:166  loid:1
    public static final String TX_ID_DISH_DETAIL_BURDEN_TOP = "_adb_14678_10147668";
    //菜谱详情分享下方banner   尺寸：582:166  loid:1
    public static final String TX_ID_DISH_DETAIL_BURDEN_BOTTOM = "_adb_14678_10147669";
    //菜谱详情页相关推荐   尺寸：230*152   loid:101
    public static final String TX_ID_DISH_DETAIL_SCOMMEND_1 = "_adb_14678_10147670";
    //菜谱详情页相关推荐   尺寸：640:330  loid:202
    public static final String TX_ID_DISH_DETAIL_SCOMMEND_2 = "_adb_14678_10147671";
    //菜谱详情页精选作品   尺寸：640:330  loid:202
    public static final String TX_ID_DISH_DETAIL_SUBJECT_1 = "_adb_14678_10147672";
    public static final String TX_ID_DISH_DETAIL_SUBJECT_2 = "_adb_14678_10147673";
    //美食圈列表 尺寸：640:330  loid:202
    public static final String TX_ID_QUAN_1= "_adb_14678_10147674";
    public static final String TX_ID_QUAN_2= "_adb_14678_10147675";
    public static final String TX_ID_QUAN_3= "_adb_14678_10147676";
    public static final String TX_ID_QUAN_4= "_adb_14678_10147677";
    public static final String TX_ID_QUAN_5= "_adb_14678_10147678";
    public static final String TX_ID_QUAN_6= "_adb_14678_10147679";
    //美食圈美食贴详情banner  尺寸：582:166  loid:1
    public static final String TX_ID_QUAN_DETAIL= "_adb_14678_10147680";

    //请求url
    private final String reqUrl = "http://u.l.qq.com/bpxiangh";
//    private final String reqUrl = "http://tpap.l.qq.com/bpxiangh";
    private final String mAppId = "com.xiangha";
    private final String mpublisherId = "_adb_u2011";

    private String mLat,mLon,ua;

    private TencenApiAdTools(){}

    public static TencenApiAdTools getTencenApiAdTools(){
        if(tencenApiAdTools == null){
            synchronized (TencenApiAdTools.class) {
                if(tencenApiAdTools == null){
                    tencenApiAdTools = new TencenApiAdTools();
                }
            }
        }
        return tencenApiAdTools;
    }

    public void getApiAd(final Context con, final String adId, final String loid, final OnTencenAdCallback callback){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String params = getParams(con,adId,loid);
                    String data = requestByPost(reqUrl,params);
                    if(!TextUtils.isEmpty(data)){
                        ArrayList<Map<String, String>> array = getListMapByJson(data);
                        if(callback != null) callback.onAdShow(array);
//                        if(array.size() > 0) {
//                            Map<String, String> map = array.get(0);
//                            String bidid = map.get("bidid");
//                            String id = map.get("id");
//                            String seatbid = map.get("seatbid");
//                            array = getListMapByJson(seatbid);
//                            if(array.size() > 0) {
//                                map = array.get(0);
//                                String bid = map.get("bid");
//                                array = getListMapByJson(bid);
//                                if(array.size() > 0) {
//                                    map = array.get(0);
//                                    String w = map.get("w");
//                                    String h = map.get("h");
//                                    String bid_id = map.get("id");
//                                    String impid = map.get("impid");
//                                    String ext = map.get("ext");
//                                    array = getListMapByJson(ext);
//                                    if(array.size() > 0) {
//                                        map = array.get(0);
//                                        String aurl = map.get("aurl");
//                                        //点击的监测地址数组(最多三个）
//                                        String cmurl = map.get("cmurl");
//                                        String curl = map.get("curl");
//                                        //曝光监测地址数组（最多五个）
//                                        String murl = map.get("murl");
//                                        String stype = map.get("stype");
//                                        String type = map.get("type");
//                                    }
//                                }
//                            }
//                        }
                    }else{
                        if(callback != null) callback.onAdFail();
                    }

                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static void onShowAd(final Context con,String showUrl){
        try{
            ArrayList<Map<String,String>> array = StringManager.getListMapByJson(showUrl);
            if(array.size() > 0) {
                Map<String, String> map = array.get(0);
                for (String key : map.keySet()) {
                    doGetOnAdClickOrShow(con,  map.get(key));
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void onClickAd(Activity act, String clickUrl, String tjClickUrl){
        try{
            AppCommon.openUrl(act,clickUrl,true);
            ArrayList<Map<String,String>> array = StringManager.getListMapByJson(tjClickUrl);
            if(array.size() > 0) {
                Map<String, String> map = array.get(0);
                for (String key : map.keySet()) {
                    doGetOnAdClickOrShow(XHApplication.in(), map.get(key));
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private static void doGetOnAdClickOrShow(Context con,String url){
        ReqInternet.in().doGet(url, new InternetCallback(con) {
            @Override
            public void loaded(int flag, String url, Object returnObj) {

            }
        });
    }

    private String getParams(Context con,String adId,String loid){
        JSONObject bidRequestJsonObj = new JSONObject();
        try {
            String md5Time = Tools.getMD5(String.valueOf(System.currentTimeMillis()));
            //当前exchange下的app唯一标识
            bidRequestJsonObj.put("id", md5Time);
            //超时时长，单位ms. 默认200ms
            bidRequestJsonObj.put("tmax", 200);
            //广告imp组
            JSONArray impJsonArray = new JSONArray();
            //---广告imp对象
            JSONObject impJsonObj = new JSONObject();
            //---广告曝光的唯一ID
            impJsonObj.put("id", md5Time);
            //---广告资源属性对象
            JSONObject bannerJsonObj = new JSONObject();
            //------广告位高度，单位像素。
            bannerJsonObj.put("h", 65);
            //------广告位宽度，单位像素。
            bannerJsonObj.put("w", 600);
            impJsonObj.put("banner", bannerJsonObj);
//            //---广告位宽度，单位像素。
            impJsonObj.put("bidfloor", "100");
            //---ext
            JSONObject extJsonObj = new JSONObject();
            //---Location 对象
            JSONObject locationJsonObj = new JSONObject();
            //------广告位ID
            locationJsonObj.put("loc", adId);
            //------频道id
            locationJsonObj.put("channel_id", "12345");
            //------相对位置序列
            locationJsonObj.put("seq", "10");
            //------广告形式类型（支持多种以逗号分割如1,2） 具体取值见《广告形式规格.xls》
            locationJsonObj.put("loid", loid);
            extJsonObj.put("location", locationJsonObj);
            impJsonObj.put("ext", extJsonObj);
            impJsonArray.put(impJsonObj);
            bidRequestJsonObj.put("imp", impJsonArray);
            //App 对象
            JSONObject appJsonObj = new JSONObject();
            //---当前exchange下的app唯一标识
            appJsonObj.put("id", mAppId);
            appJsonObj.put("name", "香哈菜谱");
            appJsonObj.put("ver", VersionOp.getVerName(con));
            //------媒体的属性对象
            JSONObject publisherJsonObj = new JSONObject();
            //---------由SSP指定的媒体ID
            publisherJsonObj.put("id", mpublisherId);
            publisherJsonObj.put("name", "测试媒体");
            appJsonObj.put("publisher", publisherJsonObj);
            bidRequestJsonObj.put("app", appJsonObj);
            //设备信息
            JSONObject deviceJsonObj = new JSONObject();
            deviceJsonObj.put("ua",ua);
            if(!TextUtils.isEmpty(mLat)) {
                JSONObject geoJsonObj = new JSONObject();
                geoJsonObj.put("lat", mLat);
                geoJsonObj.put("lon", mLon);
                geoJsonObj.put("type", "1");
                deviceJsonObj.put("geo", geoJsonObj);
            }
//            deviceJsonObj.put("ip", ToolsDevice.getIp());
//            deviceJsonObj.put("ipv6", ToolsDevice.getIp());
            deviceJsonObj.put("devicetype",getDevicetype(con));
            deviceJsonObj.put("make", android.os.Build.MANUFACTURER);
            deviceJsonObj.put("model", "Android");
            deviceJsonObj.put("os", "Android");
            deviceJsonObj.put("osv", android.os.Build.VERSION.RELEASE);
            deviceJsonObj.put("hwv", android.os.Build.ID);
            deviceJsonObj.put("w", ToolsDevice.getWindowPx(con).widthPixels);
            deviceJsonObj.put("h", ToolsDevice.getWindowPx(con).heightPixels);
            DisplayMetrics dm = new DisplayMetrics();
            dm = con.getResources().getDisplayMetrics();
            int densityDPI = dm.densityDpi;
            deviceJsonObj.put("ppi",densityDPI);
            deviceJsonObj.put("pxratio",DensityUtil(con));
            deviceJsonObj.put("connectiontype",getConnectiontyp(con));
            deviceJsonObj.put("didmd5", Md5Util.encode(ToolsDevice.getPhoneIMEI(con)));
            deviceJsonObj.put("dpidmd5", Md5Util.encode(ToolsDevice.getAndroidId(con)));
            deviceJsonObj.put("macmd5", ToolsDevice.getMacAddressFromWifiInfo(con));
            bidRequestJsonObj.put("device", deviceJsonObj);
        }catch (Exception e){
            e.printStackTrace();
        }
        return bidRequestJsonObj.toString();
    }

    public int DensityUtil(Context context) {
        // 获取当前屏幕
        DisplayMetrics dm = new DisplayMetrics();
        dm = context.getApplicationContext().getResources().getDisplayMetrics();
        // 密度因子
       return dm.densityDpi / 160;
    }

    public void getLocation(){
        WebView webview = new WebView(XHApplication.in());
        webview.layout(0, 0, 0, 0);
        WebSettings settings = webview.getSettings();
        ua = settings.getUserAgentString();
        final LocationSys locationSys = new LocationSys(XHApplication.in());
        locationSys.starLocation(new LocationSys.LocationSysCallBack() {
            @Override
            public void onLocationFail() {
                locationSys.stopLocation();
            }

            @Override
            public void onLocationSuccess(String country, String countryCode, String province, String city, String district, String lat, String lng) {
                super.onLocationSuccess(country, countryCode, province, city, district, lat, lng);
                locationSys.stopLocation();
                mLat = lat;
                mLon = lng;
            }
        });

    }

    private String getDevicetype(Context context){
        return "4";
    }

    private int getConnectiontyp(Context context){
        String networkType = ToolsDevice.getNetWorkType(context);
        if (!TextUtils.isEmpty(networkType)) {
            if (networkType.startsWith("wifi")) {
                return 2;
            } else if (networkType.startsWith("2G")) {
                return 4;
            } else if (networkType.startsWith("3G")) {
                return 5;
            } else if (networkType.startsWith("4G")) {
                return 6;
            } else if (networkType.startsWith("mobile")) {
                return 0;
            }
        }
        return 0;
    }

    // Post方式请求
    private String requestByPost(String httpUrl,String params) throws Throwable {
        // 请求的参数转换为byte数组
//	    String params = URLEncoder.encode(param, "UTF-8");
        byte[] postData = params.getBytes();
        // 新建一个URL对象
        URL url = new URL(httpUrl);
        // 打开一个HttpURLConnection连接
        HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
        // 设置连接超时时间
        urlConn.setConnectTimeout(5 * 1000);
        // Post请求必须设置允许输出
        urlConn.setDoOutput(true);
        // Post请求不能使用缓存
        urlConn.setUseCaches(false);
        // 设置为Post请求
        urlConn.setRequestMethod("POST");
        urlConn.setInstanceFollowRedirects(true);
        // 配置请求Content-Type
        urlConn.setRequestProperty("Content-Type",
                "application/json");
        // 开始连接
        urlConn.connect();
        // 发送请求参数
        DataOutputStream dos = new DataOutputStream(urlConn.getOutputStream());
        dos.write(postData);
        dos.flush();
        dos.close();
        // 判断请求是否成功
        if (urlConn.getResponseCode() == 200) {
            // 获取返回的数据
            byte[] data = UtilFile.inputStream2Byte(urlConn.getInputStream());
            String returnData = new String(data, "UTF-8");
//	        Log.i(TAG_POST, "Post请求方式成功，返回数据如下：");
            return returnData;
        } else {
            return "";
        }
    }

    public interface OnTencenAdCallback{
        public void onAdShow(ArrayList<Map<String, String>> listReturn);
        public void onAdFail();
    }
}
