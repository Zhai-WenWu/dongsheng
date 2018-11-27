package acore.logic.stat;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import acore.logic.LoginManager;
import acore.logic.SpecialOrder;
import acore.override.XHApplication;
import acore.tools.ChannelManager;
import acore.tools.FileManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;
import third.location.LocationHelper;

import static acore.logic.stat.UnburiedStatisticsSQLite.GXHTJ;
import static acore.logic.stat.UnburiedStatisticsSQLite.Normal;


/**
 * Description :
 * PackageName : acore.logic.statistics
 * Created by mrtrying on 2018/8/1 16:49.
 * e_mail : ztanzeyu@gmail.com
 */
public class StatisticsManager {
    public static final String TAG = "Unburied";
    public static final String STAT_DATA = "statJson";
    public static final String IS_STAT = "_isStatShow";
    public static final String TRUE_VALUE = "2";
    static final int MAX_DATA_COUNT = 500;

    private static Handler handlerStatistics;//s6统计
    private static Runnable runnableStatistics;
    public static int STATISTICS_TIME = 10 * 1000;

    public static JSONObject statisticsJson = new JSONObject();

    /**
     * 发送请求.在某一个类中开启(在Main中开启和关闭),循环执行.
     */
    public static void sendLiveTime(final Context context) {
        //********首页统计10秒循环统计
        handlerStatistics = new Handler(Looper.getMainLooper());
        runnableStatistics = () -> {
            //循环统计feed流数据
            forceSendStatisticsData();
            //循环倒计时--10秒统计
            handlerStatistics.postDelayed(runnableStatistics, STATISTICS_TIME);
        };
        handlerStatistics.postDelayed(runnableStatistics, STATISTICS_TIME);
    }

    /**
     * 停止计时器
     * 在软件退出时执行此方法
     */
    public static void closeHandler() {
        if (handlerStatistics != null && runnableStatistics != null)
            handlerStatistics.removeCallbacks(runnableStatistics);
    }

    /** 强制上传统计首页请求数据 */
    public static void forceSendStatisticsData() {
        final ArrayList<String> list = UnburiedStatisticsSQLite.instance().selectAllDataByType(Normal);
        if(list != null && !list.isEmpty()){
            new Handler(Looper.getMainLooper()).post(() -> sendStatisticsData(list,StringManager.API_STATISTIC_S9));
        }
        final ArrayList<String> listGXHTJ = UnburiedStatisticsSQLite.instance().selectAllDataByType(GXHTJ);
        if(listGXHTJ != null && !listGXHTJ.isEmpty()){
            new Handler(Looper.getMainLooper()).post(() -> sendStatisticsData(listGXHTJ,StringManager.API_STATISTIC_S9_GXH));
        }
        UnburiedStatisticsSQLite.instance().deleteAllData();
    }

    public static void saveData(StatModel model){
        if (model == null){
            return;
        }
        JSONObject jsonObject = new JSONObject();
        try {
            long t = System.currentTimeMillis() / 1000;
            putValue(jsonObject, "t", String.valueOf(t));
            putValue(jsonObject, "e", model.e);
            putValue(jsonObject, "p", model.p);
            putValue(jsonObject, "m", model.m);
            putValue(jsonObject, "statJson", model.statJson);
            putValue(jsonObject, "pos", model.pos);
            putValue(jsonObject, "btn", model.btn);
            putValue(jsonObject, "s1", model.s1);
            putValue(jsonObject, "s2", model.s2);
            putValue(jsonObject, "s3", model.s3);
            putValue(jsonObject, "n1", model.n1);
            putValue(jsonObject, "n2", model.n2);
            Log.i(TAG, "saveData: " + jsonObject);
            if (SpecialOrder.isOpenSwitchStatLayout(XHApplication.in())) {
                DesktopLayout.of(XHApplication.in()).insertData(jsonObject.toString());
            }
            //存数据库
            String type = !TextUtils.isEmpty(model.statJson) && model.statJson.contains(GXHTJ) ? GXHTJ : Normal;
            UnburiedStatisticsSQLite.instance().insterData(jsonObject.toString(),type);
        } catch (Exception exc) {
            exc.printStackTrace();
        }

        if (UnburiedStatisticsSQLite.instance().getDataCount() > MAX_DATA_COUNT) {
            //获取全部数据
            final ArrayList<String> list = UnburiedStatisticsSQLite.instance().selectAllDataByType(Normal);
            new Handler(Looper.getMainLooper()).post(() -> sendStatisticsData(list,StringManager.API_STATISTIC_S9));
            final ArrayList<String> listGXHTJ = UnburiedStatisticsSQLite.instance().selectAllDataByType(GXHTJ);
            new Handler(Looper.getMainLooper()).post(() -> sendStatisticsData(listGXHTJ,StringManager.API_STATISTIC_S9_GXH));
            //删除全部数据
            UnburiedStatisticsSQLite.instance().deleteAllData();
        }
    }

    private static void sendStatisticsData(ArrayList<String> data,String api) {
        try {
            if(data == null || data.isEmpty()){
                return;
            }
            String url = Tools.isDebug(XHApplication.in()) && StringManager.domain.contains(".ixiangha.com")
                    ? api.replace("https://stat.xiangha.com","http://stat.ixiangha.com") : api;
            handleParamsDevicePart();
            JSONArray jsonArray = new JSONArray();
            for (String value : data) {
                jsonArray.put(new JSONObject(value));
            }
            statisticsJson.put("data", jsonArray);
            LinkedHashMap<String, String> params = new LinkedHashMap<>();
            params.put("log_json", statisticsJson.toString());
            if (SpecialOrder.isOpenSwitchStatLayout(XHApplication.in())) {
                DesktopLayout.of(XHApplication.in()).insertData("--"+url+" 已上传--");
            }
            ReqInternet.in().doPost(url, params, new InternetCallback() {
                @Override
                public void loaded(int flag, String url, Object object) {
                    if (flag >= ReqInternet.REQ_OK_STRING) {
                        Log.i(TAG, "上传数据s1");
                        Map<String, String> result = StringManager.getFirstMap(object);
                        if (!TextUtils.isEmpty(result.get("error"))) {
                            Log.e(TAG, "errorInfo: " + result.get("error"));
                        }
                    } else {

                    }

                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private static void handleParamsDevicePart() {
        String deviceJson = FileManager.loadShared(XHApplication.in(), FileManager.xmlFile_appInfo, FileManager.xmlKey_device_statistics).toString();
        try {
            if (TextUtils.isEmpty(deviceJson) || statisticsJson.length() <= 0) {
                //设备唯一标示
                statisticsJson.put("devCode", ToolsDevice.getXhCode(XHApplication.in()));
                //客户端系统
                statisticsJson.put("sys", "and");
                //手机型号
                statisticsJson.put("model", android.os.Build.MODEL);
                //系统版本
                statisticsJson.put("sysVer", android.os.Build.VERSION.RELEASE);
                //	客户端版本
                statisticsJson.put("appVer", ToolsDevice.getVerName(XHApplication.in()));
                //项目编译版本
                statisticsJson.put("build", "26");
                //分辨率
                DisplayMetrics metric = ToolsDevice.getWindowPx(XHApplication.in());
                statisticsJson.put("pRes", metric.widthPixels + "*" + metric.heightPixels);
                //渠道
                statisticsJson.put("channel", ChannelManager.getInstance().getChannel(XHApplication.in()));
                //包名称
                statisticsJson.put("pack", ToolsDevice.getPackageName(XHApplication.in()));
                //时区
                statisticsJson.put("tz", ToolsDevice.getCurrentTimeZoneInt() + "");
            }
            String location = getLocation();
            //用户secret
            String userSecret = LoginManager.userInfo.get("userCode");
            statisticsJson.put("secret", TextUtils.isEmpty(userSecret) ? "" : userSecret);
            // 经纬度
            if (!TextUtils.isEmpty(location)) {
                statisticsJson.put("geo", location);
            }
            //网络状态
            statisticsJson.put("netState", ToolsDevice.getNetWorkSimpleType(XHApplication.in()));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private static String getLocation() {
        return LocationHelper.getInstance().getLongitude() + "#" + LocationHelper.getInstance().getLatitude();
    }


    /**
     * @param jsonObject
     * @param key
     * @param value
     *
     * @throws JSONException
     */
    private static void putValue(JSONObject jsonObject, String key, String value) throws JSONException {
        value = checkString(value);
        if (jsonObject != null
                && !TextUtils.isEmpty(key)
                && !TextUtils.isEmpty(value)
                ) {
            jsonObject.put(key,Uri.encode(value));
        }
    }

    @NonNull
    private static String checkString(String value) {
        return (value != null && !"null".equals(value)) ? value : "";
    }
}
