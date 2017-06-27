package acore.logic;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;

import com.tencent.stat.StatService;
import com.umeng.analytics.MobclickAgent;
import com.umeng.analytics.dplus.UMADplus;

import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import acore.dialogManager.VersionOp;
import acore.logic.load.LoadManager;
import acore.override.XHApplication;
import acore.tools.ChannelUtil;
import acore.tools.FileManager;
import acore.tools.LogManager;
import acore.tools.StringManager;
import acore.tools.ToolsDevice;
import amodule.main.Main;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;
import aplug.basic.XHConf;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import third.push.model.NotificationData;
import xh.basic.tool.UtilFile;
import xh.basic.tool.UtilLog;
import xh.basic.tool.UtilString;

import static acore.tools.ToolsDevice.getWindowPx;

/**
 * @Description:
 * @Title: XiangHaClick.java Copyright: Copyright (c) xiangha.com 2014~2017
 * @author: luomin 修改 zeyu_t
 * @date: 2015年3月10日 上午10:59:36
 */
public class XHClick {
    public static final int CIRCULATION_TIME = 30000;
    public static final int NOTIFY_A = 1;
    public static final int NOTIFY_B = 2;
    public static final int NOTIFY_C = 3;
    public static final int NOTIFY_D = 4;
    public static final int NOTIFY_SELF = 5;

    private static long startTime;//开始的时间戳
    private static long stopTime;//结束的时间戳
    private static String path = "";//记录页面停留时间的字符串
    private static Handler handler;
    private static Runnable runnable;

    private static long viewPageStartTime;//viewpage 子tab的页面停留时间(开始的时间戳)
    private static long viewPageStopTime;//viewpage 子tab的页面停留时间(结束的时间戳)
//	private static String itemName = "";//viewpager 子tab的页面名称.(名称用拼音表示)


    /**
     * 开启viewpager的页面开启时间
     *
     * @param name item页面的名称
     */
    public static void getViewPageItemStartTime(String name) {
        viewPageStartTime = System.currentTimeMillis();
        path += name + ">";
    }

    /**
     * 获取viewpager中的item页面停止时间
     */
    public static void getViewPageItemStopTime() {
        viewPageStopTime = System.currentTimeMillis();
        getViewPageItemLiveTime();
    }

    /** 计算出viewpager中的item页面停留的时间 */
    private static void getViewPageItemLiveTime() {
        double liveTime = (viewPageStopTime - viewPageStartTime) / 1000.0;
        //设置只保留一位小数
        NumberFormat nf = new DecimalFormat("0.0");
        try {
            liveTime = Double.parseDouble(nf.format(liveTime).trim());
        } catch (Exception e) {
        }
        //调用统计停留时间的方法
        ViewPageItemLiveTime(liveTime);
    }

    /**
     * 统计时间,并添加入path中发送服务端
     *
     * @param liveTime 页面停留时间
     */
    private static void ViewPageItemLiveTime(double liveTime) {
        if (liveTime > 9999)
            liveTime = 9999.9;
        path += liveTime + ",";
    }

    /**
     * 层级事件统计
     *
     * @param context
     * @param eventID    ：事件ID
     * @param twoLevel   ：二级内容
     * @param threeLevel ：三级内容,若没有则传 ""
     */
    public static void mapStat(Context context, String eventID, String twoLevel, String threeLevel) {
        if (!TextUtils.isEmpty(threeLevel)) {
            onEvent(context, eventID, twoLevel, threeLevel);
            onMtaEvent(context,eventID,twoLevel,threeLevel);//mta统计
        }
        onEvent(context, eventID, "全部", twoLevel);
        onMtaEvent(context,eventID,"全部",twoLevel);//mta统计
    }

    /**
     * 层级事件统计
     *
     * @param context
     * @param eventID
     * @param twoLevel
     * @param threeLevel
     * @param number
     */
    public static void mapStat(Context context, String eventID, String twoLevel, String threeLevel, int number) {
        if (!TextUtils.isEmpty(threeLevel)) {
            onEventValue(context, eventID, twoLevel, threeLevel, number);
            onMtaEvent(context,eventID,twoLevel,threeLevel);//mta统计
        }
        onEventValue(context, eventID, "全部", twoLevel, number);
        onMtaEvent(context,eventID,"全部",twoLevel);//mta统计

    }


    /**
     * 计算事件统计
     *
     * @param context   上下文
     * @param eventID   事件ID
     * @param map_key
     * @param map_value
     * @param value     数值
     */
    public static void onEventValue(Context context, String eventID, String map_key, String map_value, int value) {
        if (isStatistics(context)) {
            Map<String, String> map = new HashMap<String, String>();
            map.put(map_key, map_value);
            MobclickAgent.onEventValue(context, eventID, map, value);
            onMtaEvent(context,eventID,map_key,map_value);//mta统计
        }
        showToast(context, "统计_计算_" + eventID + "：" + map_key + "，" + map_value + "，" + value);
    }

    /**
     * 计数事件统计	(有map)
     *
     * @param context   上下文
     * @param eventID   事件ID
     * @param map_key
     * @param map_value
     */
    public static void onEvent(Context context, String eventID, String map_key, String map_value) {
        if (isStatistics(context)) {
            Map<String, String> map = new HashMap<String, String>();
            map.put(map_key, map_value);
            MobclickAgent.onEvent(context, eventID, map);
        }
        showToast(context, "统计_计数_" + eventID + "：" + map_key + "，" + map_value);
    }

    /**
     * 腾讯统计
     * @param context
     * @param eventID
     * @param twoLevel
     * @param threeLevel
     */
    private static void onMtaEvent(Context context, String eventID,String twoLevel, String threeLevel){
        Properties prop = new Properties();
        prop.setProperty(twoLevel, threeLevel);
//        StatService.trackCustomKVEvent(context,eventID,prop);
        StatService.trackCustomEvent(context,eventID,twoLevel, threeLevel);
    }
    /**
     * 百度统计
     * @param context
     * @param eventID
     * @param twoLevel
     * @param threeLevel
     */
    private static void onbaiduEvent(Context context, String eventID,String twoLevel, String threeLevel){
//        com.baidu.mobstat.StatService.onEvent(context,eventID,twoLevel);
    }
    /**
     * 计数事件统计	(无map)
     *
     * @param context 上下文
     * @param eventID 事件ID
     * @param value
     */
    public static void onEvent(Context context, String eventID, String value) {
        if (isStatistics(context)) {
            MobclickAgent.onEvent(context, eventID, value);
            StatService.trackCustomEvent(context, eventID, value);
//            com.baidu.mobstat.StatService.onEvent(context,eventID,value);
        }
        showToast(context, "统计_计算_" + eventID + "：" + value);
    }

    /**
     * 统计超级属性
     *
     * @param context
     * @param property
     * @param propertyValue
     */
    public static void registerSuperProperty(Context context, String property, Object propertyValue) {
        if (isStatistics(context)) {
            UMADplus.registerSuperProperty(context, property, propertyValue);
        }
    }

    /**
     * 统计普通属性
     *
     * @param context
     * @param eventName
     */
    public static void track(Context context, final String eventName) {
        showToast(context, "统计_普通属性_" + eventName);
        if (isStatistics(context)) {
            UMADplus.track(context, eventName);
        }
    }

    /**
     * 统计普通属性
     *
     * @param context
     * @param eventName
     * @param key
     * @param value
     */
    public static void track(Context context, String eventName, String key, Object value) {
        showToast(context, "统计_普通属性_" + eventName + "_" + key + "_" + value);
        if (isStatistics(context)) {
            if (TextUtils.isEmpty(key) || value == null) {
                track(context, eventName);
                return;
            }
            track(context, eventName, new String[]{key}, new Object[]{value});
        }
    }

    /**
     * 统计普通属性
     *
     * @param context
     * @param eventName
     * @param keys
     * @param values
     */
    public static void track(Context context, String eventName, String[] keys, Object[] values) {
        if (isStatistics(context)) {
            if (keys == null || keys.length == 0
                    || values == null || values.length == 0) {
                track(context, eventName);
                return;
            }
            final int length = keys.length <= values.length ? keys.length : values.length;
            HashMap<String, Object> property = new HashMap<>();
            for (int index = 0; index < length; index++) {
                property.put(keys[index], values[index]);
            }
            track(context, eventName, property);
        }
    }

    /**
     * 普通属性
     *
     * @param context
     * @param eventName
     * @param property
     */
    private static void track(Context context, String eventName, HashMap<String, Object> property) {
        if (isStatistics(context)) {
            if (property == null) {
                track(context, eventName);
                return;
            }
            UMADplus.track(context, eventName, property);
        }
    }

    /** 判断是否需要统计 */
    private static boolean isStatistics(Context context) {
        return !LoginManager.isManager() && VersionOp.getVerName(context).length() < 6;
    }

    /** 获得activity页面开始时间戳 */
    public static void getStartTime(Context context) {
        startTime = System.currentTimeMillis();
        if (context != null) {
            Class<?> cls = context.getClass();
            if (cls.getSimpleName().equals("MainQuan") || cls.getSimpleName().equals("HomeNous")) {
                return;
            } else {
                path += cls.getSimpleName() + ">";
            }
            showToast(context, path);
        }
    }

    /**
     * 获取activity页面关闭时间戳
     * 必须要先调用getStartTime()的方法,不然获取的停留时间是错误的且为负值
     * 调用该方法后,程序会自动计算你获取的startTime 和 stopTime 的事件差.计算出停留的秒数.
     */
    public static void getStopTime(Context activity) {
        stopTime = System.currentTimeMillis();
        if (activity != null) {
            //调用页面停留时间计算方法
            getLiveTime(activity);
        }
    }

    /**
     * 获得页面停留的时间
     *
     * @param activity
     */
    private static void getLiveTime(Context activity) {
        double liveTime = (stopTime - startTime) / 1000.0;
        //设置只保留一位小数
        NumberFormat nf = new DecimalFormat("0.0");
        try {
            liveTime = Double.parseDouble(nf.format(liveTime).trim());
        } catch (Exception e) {
        }
        //调用统计停留时间的方法
        ActivityLiveTime(activity, liveTime);
    }

    /**
     * 获取页面存活时间的字符串
     * 以    { "页面的class名"+"_"+停留时间   }来记录.以","分隔.
     *
     * @param allActivity
     * @param liveTime
     */
    private static void ActivityLiveTime(Context allActivity, double liveTime) {
        if (allActivity != null) {
            Class<?> cls = allActivity.getClass();
            if (liveTime > 9999)
                liveTime = 9999.9;
            //分页面的统计
            if (cls.getSimpleName().equals("MainQuan")
                    || cls.getSimpleName().equals("HomeNous")) {
                return;
            } else {
                path += liveTime + ",";
            }
            showToast(allActivity, path);
        }
    }

    /**
     * 发送请求.在某一个类中开启(在Main中开启和关闭),循环执行.
     */
    public static void sendLiveTime(final Context context) {
        handler = new Handler(Looper.getMainLooper());
        runnable = new Runnable() {
            @Override
            public void run() {
                if (!path.equals(""))
                    //  发送请求的操作
                    ReqInternet.in().doGet(StringManager.api_stat, new InternetCallback(context) {

                        @Override
                        public void loaded(int flag, String url, Object msg) {
                            path = "";
                        }

                        @Override
                        public Map<String, String> getReqHeader(Map<String, String> header, String url, Map<String, String> params) {
                            header.put("Cookie", "path=" + path + ";");
                            return super.getReqHeader(header, url, params);
                        }
                    });

                //循环统计feed流数据
                loopHandlerStatictis();
                //循环计时器 每隔30秒执行一次
                handler.postDelayed(runnable, CIRCULATION_TIME);
            }
        };
        //启动计时器 30秒后执行
        handler.postDelayed(runnable, CIRCULATION_TIME);
    }

	/**
	 * 软件退出时执行
	 *
	 * @param allActivity
	 */
	public static void finishToSendPath(Context allActivity) {
		newHomeStatictis(true,"");
		registerUserUseTimeSuperProperty(allActivity);
		stopTime = System.currentTimeMillis();
		double liveTime = (stopTime - startTime) / 1000.0;
		//设置只保留一位小数
		NumberFormat nf = new DecimalFormat("0.0 ");
		try {
			liveTime = Double.parseDouble(nf.format(liveTime).trim());
		} catch (Exception e) {
		}
		if (allActivity != null) {
			if (liveTime > 9999)
				liveTime = 9999.9;
			path += liveTime + ",";
			showToast(allActivity, path);
		}
		if (!path.equals(""))
			// 发送请求的操作
			ReqInternet.in().doGet(StringManager.api_stat, new InternetCallback(allActivity) {

                @Override
                public void loaded(int flag, String url, Object msg) {
                    path = "";
                    UtilLog.print(XHConf.log_tag_stat, "d", "GET:" + url + "成功");
                }

                @Override
                public Map<String, String> getReqHeader(Map<String, String> header, String url, Map<String, String> params) {
                    header.put("Cookie", "path=" + path + ";");
                    return super.getReqHeader(header, url, params);
                }
            });
    }

	/**
	 * 当按下home按钮的时候监听到,发送请求
	 */
	public static void HomeKeyListener(Context allActivity) {
		newHomeStatictis(true,"");
		stopTime = System.currentTimeMillis();
		double liveTime = (stopTime - startTime) / 1000.0;
		//设置只保留一位小数
		NumberFormat nf = new DecimalFormat("0.0 ");
		try {
			liveTime = Double.parseDouble(nf.format(liveTime).trim());
		} catch (Exception e) {
		}
		if (allActivity != null) {
			if (liveTime > 9999)
				liveTime = 9999.9;
			path += liveTime + ",";
			showToast(allActivity, path);
		}

        if (!path.equals(""))
            // 发送请求的操作
            ReqInternet.in().doGet(StringManager.api_stat, new InternetCallback(allActivity) {

                @Override
                public void loaded(int flag, String url, Object msg) {
                    path = "";
                }

                @Override
                public Map<String, String> getReqHeader(Map<String, String> header, String url, Map<String, String> params) {
                    header.put("Cookie", "path=" + path + ";");
                    return super.getReqHeader(header, url, params);
                }
            });
    }

    /**
     * 停止计时器
     * 在软件退出时执行此方法
     */
    public static void closeHandler() {
        if(handler != null && runnable!=null)
            handler.removeCallbacks(runnable);
    }

    /**
     * 仅用于测试的.
     *
     * @param context
     */
    private static void showToast(Context context, String content) {
//		Log.d("------统计------",content);
        UtilLog.print(XHConf.log_tag_stat, "d", content);
//		Tools.showToast(context, content);
    }

    /**
     * 消息推送所有统计
     *
     * @param context
     * @param data
     * @param eventAct
     */
    public static void statisticsNotify(Context context, NotificationData data, String eventAct) {
        if (data == null) {
            onEvent(context, "notify_default", "0_" + eventAct);
        }
        switch (data.type) {
            // 显示通知，不存在消息列表中
            case NOTIFY_A:
                onEvent(context, "notifyA_" + eventAct, data.channel, data.value + "");
                LogManager.print("d", "notifyA_" + eventAct + data.channel, data.value + "");
                break;
            // 显示通知，存在消息列表中
            case NOTIFY_B:
                onEvent(context, "notifyB_" + eventAct, data.channel, data.value + "");
                break;
            // 显示通知，存在消息列表中，使用app不通知
            case NOTIFY_C:
                onEvent(context, "notifyC_" + eventAct, data.channel, data.value + "");
                break;
            // 显示通知，不存在消息列表中，未启动时通知
            case NOTIFY_D:
                onEvent(context, "notifyD_" + eventAct, data.channel, data.value + "");
                break;
            // 自我唤醒通知
            case NOTIFY_SELF:
                onEvent(context, "notifySelf_" + eventAct, data.value + "");
                break;
            default:
                onEvent(context, "notify_default", data.type + "_" + eventAct);
                break;
        }
    }

    public static final String STATISTICS_PUSH = "push_";
    public static final String STATE_RECEIVE = "receive";
    public static final String STATE_CREATENOTIFY = "createnotify";
    public static final String STATE_PRASEURL = "praseurl";
    public static final String STATE_SHOW = "show";

    /**
     * 收到-解析数据-创建通知-解析url-展示通知
     *
     * @param context
     * @param state      消息阶段
     * @param APIVersion API版本
     */
    public static void statisticsPush(Context context, String state, int APIVersion) {
        onEvent(context, STATISTICS_PUSH + state, String.valueOf(APIVersion));
    }

    /**
     * 分享统计
     *
     * @param from 来源
     * @param link url链接
     * @param type 分享平台
     */
    public static void statisticsShare(final String from, final String link, final String type) {
        String actionUrl = StringManager.api_statisticShare;
        String param;
        try {
            param = "from=" + URLEncoder.encode(from, HTTP.UTF_8) + "&link=" + link + "&type=" + type;
            Log.i("tzy","param = " + param);
            ReqInternet.in().doPost(actionUrl, param, new InternetCallback(XHApplication.in()) {

                @Override
                public void loaded(int flag, String url, Object returnObj) {
                    if (flag > 1)
                        UtilLog.print(XHConf.log_tag_stat, "d", "统计_分享_from=" + from + "_link=" + link + "_type=" + type + "_成功");
                }
            });
        } catch (UnsupportedEncodingException e) {
            UtilLog.reportError("分享统计URLEncoder异常", e);
        }
    }

    /** DNS切换次数 */
    public static int switchDNSCount = 0;
    /** DNS切换统计ID */
    public static final String switchDNSID = "img_dns_switch";

    /**
     * 统计DNS统计
     *
     * @param context
     */
    public static void statisticsSwitchDNS(Context context) {
        switchDNSCount++;
        String network = acore.tools.ToolsDevice.getNetWorkSimpleType(context);
        if (switchDNSCount <= 100) {
            onEvent(context, switchDNSID, network, String.valueOf(switchDNSCount));
        } else {
            onEvent(context, switchDNSID, network, "100+");
        }
    }

    public final static int MAX_CODES_COUNT = 30;
    public static ArrayList<String> subjectCodes = new ArrayList<>();

    /**
     * @param context
     */
    public synchronized static void sendBrowseCodes(Context context) {
        if (subjectCodes == null || subjectCodes.size() == 0) {
            return;
        }
        ArrayList<String> codesClone = (ArrayList<String>) subjectCodes.clone();
        subjectCodes.clear();
        StringBuffer params = new StringBuffer();
        params.append("codes=");
        final int length = codesClone.size();
        for (int index = 0; index < length; index++) {
            params.append(codesClone.get(index)).append(",");
        }
        ReqInternet.in().doPost(StringManager.api_setClickList, params.toString(), new InternetCallback(context) {
            @Override
            public void loaded(int flag, String url, Object msg) {
            }
        });
    }

    /**
     * @param context
     * @param code
     */
    public static void saveCode(Context context, String code) {
        if (subjectCodes == null) {
            subjectCodes = new ArrayList<>();
        }
        if (context == null || TextUtils.isEmpty(code)) {
            return;
        }
        subjectCodes.add(code);
        if (subjectCodes.size() >= MAX_CODES_COUNT) {
            sendBrowseCodes(context);
        }
    }

    /**
     * 超级属性--注册时间
     *
     * @param context
     */
    public static void registerUserRegTimeSuperProperty(Context context) {
        if (context != null) {
            String tempTime = LoginManager.getUserRegTime(context);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                tempTime = String.valueOf(sdf.parse(tempTime).getTime());
                if (!TextUtils.isEmpty(tempTime)) {
                    double time = (System.currentTimeMillis() - Long.parseLong(tempTime)) * 1.00;
                    time = time / (1000 * 60 * 60 * 24 * 1.00);
                    String valueTime = "";
                    if (time > 0 && time <= 7)
                        valueTime = "1-7天";
                    else if (time > 7 && time <= 30)
                        valueTime = "8-30天";
                    else if (time > 30 && time <= 90)
                        valueTime = "1月-3月";
                    else if (time > 90 && time <= 180)
                        valueTime = "3月-6月";
                    else if (time > 180 && time <= 360)
                        valueTime = "6月-1年";
                    else if (time > 360 && time <= 720)
                        valueTime = "1年-2年";
                    else if (time > 720)
                        valueTime = "2年以上";
                    if (!TextUtils.isEmpty(valueTime)) {
                        UMADplus.registerSuperProperty(context, "注册时间", valueTime);
                        showToast(context, "注册时间：" + valueTime);
                    }
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 记录App第一次启动的时间
     *
     * @param context
     */
    public static void saveFirstStartTime(Context context) {
        if (context != null) {
            Object firstStartObj = UtilFile.loadShared(context, "super_property", "firstStartTime");
            if (firstStartObj != null && !TextUtils.isEmpty(firstStartObj.toString()))
                return;
            Map<String, String> map = new HashMap<String, String>();
            map.put("firstStartTime", System.currentTimeMillis() + "");
            UtilFile.saveShared(context, "super_property", map);
        }
    }

    /**
     * 超级属性--使用时间
     *
     * @param context
     */
    public static void registerUserUseTimeSuperProperty(Context context) {
        if (context != null) {
            Object object = UtilFile.loadShared(context, "super_property", "firstStartTime");
            if (object != null) {
                String firstUseTime = object.toString();
                if (!TextUtils.isEmpty(firstUseTime)) {
                    double time = ((System.currentTimeMillis() - Long.parseLong(firstUseTime)) * 1.00) / (1000 * 60 * 60 * 24 * 1.00);
                    String valueTime = "";
                    if (time > 0 && time <= 7)
                        valueTime = "1-7天";
                    else if (time > 7 && time <= 30)
                        valueTime = "8-30天";
                    else if (time > 30 && time <= 90)
                        valueTime = "1月-3月";
                    else if (time > 90 && time <= 180)
                        valueTime = "3月-6月";
                    else if (time > 180 && time <= 360)
                        valueTime = "6月-1年";
                    else if (time > 360 && time <= 720)
                        valueTime = "1年-2年";
                    else if (time > 720)
                        valueTime = "2年以上";
                    if (!TextUtils.isEmpty(valueTime)) {
                        UMADplus.registerSuperProperty(context, "使用时间", valueTime);
                        showToast(context, "使用时间：" + valueTime);
                    }
                }
            }
        }
    }

    /**
     * 超级属性--自然月内启动app天数
     *
     * @param context
     */
    public static void registerMonthSuperProperty(Context context) {
        if (context != null) {
            int year = Calendar.getInstance().get(Calendar.YEAR);
            int month = Calendar.getInstance().get(Calendar.MONTH);
            int day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
            System.out.println("year = " + year + "  month =" + month + "  day = " + day);
            Object yearObj = UtilFile.loadShared(context, "super_property", "year");
            Object monthObj = UtilFile.loadShared(context, "super_property", "month");
            Object lastDayObj = UtilFile.loadShared(context, "super_property", "lastDay");
            Object daysObj = UtilFile.loadShared(context, "super_property", "days");
            if (!TextUtils.isEmpty(monthObj + "") && !TextUtils.isEmpty(lastDayObj + "") && !TextUtils.isEmpty(daysObj + "") && !TextUtils.isEmpty(yearObj + "")) {
                String yearTime = yearObj.toString();
                String monthTime = monthObj.toString();
                String lastDay = lastDayObj.toString();
                String days = daysObj.toString();
                if (Integer.parseInt(yearTime) != year) {
                    Map<String, String> map = new HashMap<String, String>();
                    map.put("year", year + "");
                    map.put("month", month + "");
                    map.put("lastDay", day + "");
                    map.put("days", 1 + "");
                    UtilFile.saveShared(context, "super_property", map);
                    UMADplus.registerSuperProperty(context, "自然月内启动app天数", "1天");
                    showToast(context, "自然月内启动app天数：" + "1天");
                } else if (Integer.parseInt(monthTime) != month) {
                    Map<String, String> map = new HashMap<String, String>();
                    map.put("month", month + "");
                    map.put("lastDay", day + "");
                    map.put("days", 1 + "");
                    UtilFile.saveShared(context, "super_property", map);
                    UMADplus.registerSuperProperty(context, "自然月内启动app天数", "1天");
                    showToast(context, "自然月内启动app天数：" + "1天");
                } else {
                    String daysStr = "";
                    int dayValue = Integer.parseInt(days) + day != Integer.parseInt(lastDay) ? 1 : 0;
                    Map<String, String> map = new HashMap<String, String>();
                    map.put("lastDay", day + "");
                    map.put("days", String.valueOf(dayValue));
                    UtilFile.saveShared(context, "super_property", map);
                    if (dayValue == 1)
                        daysStr = "1天";
                    else if (dayValue > 1 && dayValue <= 3)
                        daysStr = "2-3天";
                    else if (dayValue > 3 && dayValue <= 7)
                        daysStr = "4-7天";
                    else if (dayValue > 7 && dayValue <= 14)
                        daysStr = "8-14天";
                    else if (dayValue > 14 && dayValue <= 21)
                        daysStr = "15-21天";
                    else if (dayValue > 21 && dayValue <= 31)
                        daysStr = "22-31天";
                    if (!TextUtils.isEmpty(daysStr)) {
                        UMADplus.registerSuperProperty(context, "自然月内启动app天数", daysStr);
                        showToast(context, "自然月内启动app天数：" + daysStr);
                    }
                }
            } else {
                Map<String, String> map = new HashMap<String, String>();
                map.put("year", year + "");
                map.put("month", month + "");
                map.put("lastDay", day + "");
                map.put("days", 1 + "");
                UtilFile.saveShared(context, "super_property", map);
                UMADplus.registerSuperProperty(context, "自然月内启动app天数", "1天");
                showToast(context, "自然月内启动app天数：" + "1天");
            }
        }
    }

    /**
     *轮训统计首页请求数据
     */
    private static void loopHandlerStatictis(){
        String data=FileManager.loadShared(XHApplication.in(),FileManager.STATICTIS_S6,FileManager.STATICTIS_S6).toString();
        if(!TextUtils.isEmpty(data)){
            newHomeStatictis(true,"");
        }
    }
	/**
	 * 首页统计
     * @param isResetData 是否要重置参数数据
     * @param data 参数数据块
	 */
	public static synchronized void newHomeStatictis(final boolean isResetData, String data){
		try {
            Main.allMain.handlerHomeStatistics();
			String url = StringManager.API_STATISTIC_S6;
			String baseData= getStatictisParams();
			LinkedHashMap<String,String> map= new LinkedHashMap<>();
            if(TextUtils.isEmpty(data))
			    data = FileManager.loadShared(XHApplication.in(), FileManager.STATICTIS_S6, FileManager.STATICTIS_S6).toString();
			Map<String,String> baseMap= UtilString.getMapByString(baseData,"&","=");
			JSONObject jsonObject = MapToJson(baseMap);
			if(!TextUtils.isEmpty(data)){
				JSONArray jsonArray = new JSONArray();
				String [] strs= data.split("&&");
				int lenght=strs.length;
				for(int i=0;i<lenght;i++){
					if(!TextUtils.isEmpty(strs[i])){
						jsonArray.put(MapToJson(UtilString.getMapByString(strs[i],"&","=")));
					}
				}
				jsonObject.put("log_data",jsonArray);
			}

			ReqInternet.in().doPost(url, RequestBody.create(MediaType.parse("application/json"),jsonObject.toString().getBytes()),map, new InternetCallback(XHApplication.in()) {
				@Override
				public void loaded(int flag, String url, Object object) {
					if(flag>=ReqInternet.REQ_OK_STRING){
                        if(isResetData)
                            FileManager.saveShared(XHApplication.in(),FileManager.STATICTIS_S6,FileManager.STATICTIS_S6,"");
					}else{

					}

				}
			});
		}catch (Exception e){
			e.printStackTrace();
		}
	}

	/**
	 * 处理统计基础数据
	 * @return
	 */
	private static String getStatictisParams(){
		String params="xh_code="+ ToolsDevice.getXhIMEI(XHApplication.in());
		if (LoginManager.userInfo.containsKey("userCode")) {
			params += "&user_code=" + LoginManager.userInfo.get("userCode") + ";";
		}
		String device = UtilFile.loadShared(XHApplication.in(), FileManager.xmlFile_appInfo, FileManager.xmlKey_device_statictis).toString();
		if(!TextUtils.isEmpty(device)){
			params+="&"+device;
		}else {
			String devices="";
			devices += "system=and";
//			String mtype = android.os.Build.MODEL; // 手机型号---出现中文不使用该字段
//			mtype = mtype.replace("#", "_");
//			devices += "&model=" + mtype;
			devices += "&model=";
			String mVersion = android.os.Build.VERSION.RELEASE; // android版本号
			devices += "&system_version=" + mVersion;
			devices += "&app_version=" + VersionOp.getVerName(XHApplication.in());
			DisplayMetrics metric = getWindowPx(XHApplication.in());
			devices += "&phone_res=" + metric.widthPixels + "*" + metric.heightPixels;
			String channalID = ChannelUtil.getChannel(XHApplication.in());
			devices += "&channel=" + channalID;
			UtilFile.saveShared(XHApplication.in(), FileManager.xmlFile_appInfo, FileManager.xmlKey_device_statictis,devices);
			params+="&"+devices;
		}

		params+="&net_state="+ToolsDevice.getNetWorkType(XHApplication.in());
		params+="&memory="+ToolsDevice.getAvailMemory(XHApplication.in());
		params+="&package_name="+ToolsDevice.getPackageName(XHApplication.in());
		params+="&app_id="+StringManager.appID;
		params+="&token="+ LoadManager.tok;
		return params;
	}

	/**
	 * 新的统计策略
	 * @param page_title 页面名称
	 * @param mode_type 模块名称，list header, foot
	 * @param data_type 数据类型 目前:图文菜谱，视频菜谱，美食贴专辑，涨知识
	 * @param item_code 个体标识 code唯一标示
	 * @param show_num 显示数量 数量，浏览数据数量
	 * @param event 事件 动作事件 info(详情),up（上翻）,down（下翻）,click（点击）list()
	 * @param stop_time 停留时间(s)
	 * @param uri_name 页面路由标识,请求数据的接口
	 * @param position 当前在列表的位置
	 * @param button_name 按钮名称
	 * @param deep 页面深度
	 */
	public synchronized static void saveStatictisFile(String page_title,String mode_type,String data_type,String item_code,
													  String show_num,String event,String stop_time,String uri_name,
													  String position,String button_name,String deep){
		try {
			final String data=FileManager.loadShared(XHApplication.in(),FileManager.STATICTIS_S6,FileManager.STATICTIS_S6).toString();

			//对数据进行存储
			long time = System.currentTimeMillis();
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String app_time = formatter.format(time);
//            Log.i("zhangyujian","数据时间::"+app_time+":::"+event);
			String params="";
			params+="app_time="+app_time;
			params+="&page_title="+page_title;
			params+="&mode_type="+mode_type;
			params+="&data_type="+data_type;
			params+="&item_code="+item_code;
			params+="&show_num="+show_num;
			params+="&event="+event;
			params+="&stop_time="+stop_time;
			params+="&uri_name="+uri_name;
			params+="&position="+position;
			params+="&button_name="+button_name;
			params+="&deep="+deep;
            Log.i("zhangyujian","加载数据：：："+params);
            if(TextUtils.isEmpty(data)){

				FileManager.saveShared(XHApplication.in(),FileManager.STATICTIS_S6,FileManager.STATICTIS_S6,params);
			}else{
                String[] strs= data.split("&&");
                if(strs.length>=500){
                    FileManager.saveShared(XHApplication.in(),FileManager.STATICTIS_S6,FileManager.STATICTIS_S6,"");
//                    Log.i("zhangyujian","数据超过1000条，请求数据");
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            newHomeStatictis(false,data);
                        }
                    });

                }else{
                    FileManager.saveShared(XHApplication.in(),FileManager.STATICTIS_S6,FileManager.STATICTIS_S6,data+"&&"+params);
                }
			}
		}catch (Exception e){
			e.printStackTrace();
		}
	}
	/**
	 * map转json
	 *
	 * @param maps
	 * @return
	 */
	public static JSONObject MapToJson(Map<String, String> maps) {

		JSONObject jsonObject = new JSONObject();
		if(maps==null||maps.size()<=0)return jsonObject;

		Iterator<Map.Entry<String, String>> enty = maps.entrySet().iterator();
		try {
			while (enty.hasNext()) {
				Map.Entry<String, String> entry = enty.next();
				jsonObject.put(entry.getKey(), entry.getValue());
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonObject;
	}
}
