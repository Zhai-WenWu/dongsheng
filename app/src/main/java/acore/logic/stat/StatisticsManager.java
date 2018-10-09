package acore.logic.stat;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import acore.tools.ChannelUtil;
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
    public static final String EVENT_STAY = "stay";
    public static final String EVENT_LIST_SHOW = "show";
    public static final String EVENT_BTN_CLICK = "btnC";
    public static final String EVENT_LIST_CLICK = "listC";
    public static final String EVENT_VIDEO_VIEW = "vv";
    public static final String EVENT_SPECIAL_ACTION = "action";
    public static final String EMPTY = "";
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

    /** 强制上床统计首页请求数据 */
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

    /**
     * @param p
     * @param stayTime
     */
    public static void pageStay(String p, String stayTime) {
        saveData(EVENT_STAY, p, EMPTY, EMPTY, EMPTY, EMPTY, stayTime, EMPTY,null);
    }

    /**
     * @param p
     * @param m
     * @param pos
     * @param s1
     */
    public static void listShow(String p, String m, String pos, String s1, @Nullable String statData) {
//        if (statData == null || statData.isEmpty()) {
//            throw new IllegalArgumentException("statData is null.");
//        }
        saveData(EVENT_LIST_SHOW, p, m, pos, EMPTY, s1, EMPTY,EMPTY, statData);
    }

    public static void btnClick(String p, String m, String btn) {
        btnClick(p, m, "0", btn, EMPTY);
    }

    public static void btnClick(String p, String m, String pos, String btn, String s1) {
        saveData(EVENT_BTN_CLICK, p, m, pos, btn, s1, EMPTY, EMPTY,null);
    }

    public static void listClick(String p, String m, String pos, String s1, @Nullable String statData) {
        saveData(EVENT_LIST_CLICK, p, m, pos, EMPTY, s1, EMPTY,EMPTY, statData);
    }

    public static void videoView(String p, String m, String pos, String n1, String n2,@Nullable String statData) {
        saveData(EVENT_VIDEO_VIEW, p, m, pos, EMPTY, EMPTY, n1, n2,statData);
    }

    public static void specialAction(String p, String m, String pos, String btn, String s1, String n1,String statData) {
        saveData(EVENT_SPECIAL_ACTION, p, m, pos, btn, s1, n1, EMPTY,statData);
    }

    /**
     * @param e   事件
     * @param p   页面名称
     * @param m   模块名称
     * @param pos 位置
     * @param btn 按钮名称
     * @param s1  附属字段
     * @param n1  附属字段
     */
    public static void saveData(String e, String p, String m, String pos, String btn,
                                String s1, String n1, String n2,String statData) {
        JSONObject jsonObject = new JSONObject();
        try {
            long t = System.currentTimeMillis() / 1000;
            putValue(jsonObject, "t", String.valueOf(t));
            putValue(jsonObject, "e", e);
            putValue(jsonObject, "p", p);
            putValue(jsonObject, "m", m);
            putValue(jsonObject, "pos", pos);
            putValue(jsonObject, "btn", btn);
            putValue(jsonObject, "s1", s1);
            putValue(jsonObject, "n1", n1);
            putValue(jsonObject, "n2", n2);
            putValue(jsonObject, "statJson", statData);
            Log.i(TAG, "saveData: " + jsonObject);
            if (SpecialOrder.isOpenSwitchStatLayout(XHApplication.in())) {
                DesktopLayout.of(XHApplication.in()).insertData(jsonObject.toString());
            }
            //存数据库
            String type = !TextUtils.isEmpty(statData) && statData.contains(GXHTJ) ? GXHTJ : Normal;
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
            String url = Tools.isDebug(XHApplication.in()) ? api.replace("https://stat.xiangha.com","http://stat.ixiangha.com") : api;
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
                statisticsJson.put("devCode", ToolsDevice.getXhIMEI(XHApplication.in()));
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
                statisticsJson.put("channel", ChannelUtil.getChannel(XHApplication.in()));
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
