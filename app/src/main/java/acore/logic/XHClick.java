package acore.logic;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.tencent.stat.StatService;
import com.umeng.analytics.MobclickAgent;
import com.umeng.analytics.dplus.UMADplus;
import com.umeng.message.UTrack;
import com.umeng.message.entity.UMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import acore.override.XHApplication;
import acore.tools.LogManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;
import aplug.basic.XHConf;
import third.push.model.NotificationData;
import third.push.model.NotificationEvent;
import xh.basic.tool.UtilFile;
import xh.basic.tool.UtilLog;

/**
 * @Description:
 * @Title: XiangHaClick.java Copyright: Copyright (c) xiangha.com 2014~2017
 * @author: luomin 修改 zeyu_t
 * @date: 2015年3月10日 上午10:59:36
 */
public class XHClick {
    public static int HOME_STATICTIS_TIME = 10 * 1000;
    public static int PAGE_STATICTIS_TIME = 5 * 60 * 1000;//页面停留时间修改
    public static final int NOTIFY_A = 1;
    public static final int NOTIFY_B = 2;
    public static final int NOTIFY_C = 3;
    public static final int NOTIFY_D = 4;
    public static final int NOTIFY_SELF = 5;

    private static Handler handlerPageStatictis;//s7统计
    private static Runnable runnablePageStatictis;

    /**
     * 评价按钮
     */
    public static final String comcomment_icon = "comcomment_icon";

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
            onMtaEvent(context, eventID, twoLevel, threeLevel);//mta统计
        }
        onEvent(context, eventID, "全部", twoLevel);
        onMtaEvent(context, eventID, "全部", twoLevel);//mta统计
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
            onMtaEvent(context, eventID, twoLevel, threeLevel);//mta统计
        }
        onEventValue(context, eventID, "全部", twoLevel, number);
        onMtaEvent(context, eventID, "全部", twoLevel);//mta统计

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
            onMtaEvent(context, eventID, map_key, map_value);//mta统计
        }
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
    }

    /**
     * 腾讯统计
     *
     * @param context
     * @param eventID
     * @param twoLevel
     * @param threeLevel
     */
    private static void onMtaEvent(Context context, String eventID, String twoLevel, String threeLevel) {
        Properties prop = new Properties();
        prop.setProperty(twoLevel, threeLevel);
//        StatService.trackCustomKVEvent(context,eventID,prop);
        StatService.trackCustomEvent(context, eventID, twoLevel, threeLevel);
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

    /**
     * 判断是否需要统计
     */
    private static boolean isStatistics(Context context) {
        return !LoginManager.isManager() && VersionOp.getVerName(context).length() < 6;
    }

    /**
     * 发送请求.在某一个类中开启(在Main中开启和关闭),循环执行.
     */
    public static void sendLiveTime(final Context context) {
        //********页面时间统计
        handlerPageStatictis = new Handler(Looper.getMainLooper());
        runnablePageStatictis = new Runnable() {
            @Override
            public void run() {
                //循环倒计时--10分钟统计
                handlerPageStatictis.postDelayed(runnablePageStatictis, PAGE_STATICTIS_TIME);
            }
        };
        handlerPageStatictis.postDelayed(runnablePageStatictis, PAGE_STATICTIS_TIME);

    }

    /**
     * 软件退出时执行
     *
     * @param allActivity
     */
    public static void finishToSendPath(Context allActivity) {
        registerUserUseTimeSuperProperty(allActivity);
    }

    /**
     * 停止计时器
     * 在软件退出时执行此方法
     */
    public static void closeHandler() {
        if(handlerPageStatictis != null && runnablePageStatictis != null){
            handlerPageStatictis.removeCallbacks(runnablePageStatictis);
        }
    }

    public static final String KEY_NOTIFY_CLICK = "notify_click";
    public static final int VALUE_NOTIFY_CLICK = 1;
    /**
     * 消息推送点击统计
     * @param intent
     */
    public static void statisticsNotifyClick(Intent intent) {
        NotificationData data = new NotificationData();
        data.type = intent.getIntExtra("type", 0);
        data.value = intent.getStringExtra("value");
        data.url = intent.getStringExtra("url");
        data.channel = intent.getStringExtra("channel");
        Context context = XHApplication.in();
        statisticsNotify(context, data, NotificationEvent.EVENT_CLICK);
        String message = intent.getStringExtra("umengMessage");
        if (!TextUtils.isEmpty(message)) {
            try {
                UMessage msg = new UMessage(new JSONObject(message));
                UTrack.getInstance(context).setClearPrevMessage(true);
                UTrack.getInstance(context).trackMsgClick(msg);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
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
            return;
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
                if("show".equals(eventAct)){
                    onEvent(context, "a_push_local" , "本地推送数量");
                }else if("click".equals(eventAct)){
                    onEvent(context, "a_push_local",  "本地推送点击量");
                }
                break;
            default:
                onEvent(context, "notify_default", data.type + "_" + eventAct);
                break;
        }
        if (Tools.isAppOnForeground()) {
            if (TextUtils.equals(eventAct, NotificationEvent.EVENT_SHOW)) {
                XHClick.mapStat(context, "a_push_inapp", "APP内推送-展示量", data.value);
            } else if (TextUtils.equals(eventAct, NotificationEvent.EVENT_CLICK)) {
                XHClick.mapStat(context, "a_push_inapp", "APP内推送-点击量", data.value);
            }
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
    public static void statisticsShare(final String from, final String link, final String type, Map<String, String> extraParams) {
        String actionUrl = StringManager.api_statisticShare;
        StringBuffer param = new StringBuffer();
        try {
            param.append("from=").append(URLEncoder.encode(from, "utf-8")).append("&link=").append(link).append("&type=").append(type);
            if (extraParams != null && !extraParams.isEmpty()) {
                for (String key : extraParams.keySet()) {
                    param.append("&").append(key).append("=").append(extraParams.get(key));
                }
            }
            ReqInternet.in().doPost(actionUrl, param.toString(), new InternetCallback() {

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

    /**
     * DNS切换次数
     */
    public static int switchDNSCount = 0;
    /**
     * DNS切换统计ID
     */
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
        ReqInternet.in().doPost(StringManager.api_setClickList, params.toString(), new InternetCallback() {
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
                } else if (Integer.parseInt(monthTime) != month) {
                    Map<String, String> map = new HashMap<String, String>();
                    map.put("month", month + "");
                    map.put("lastDay", day + "");
                    map.put("days", 1 + "");
                    UtilFile.saveShared(context, "super_property", map);
                    UMADplus.registerSuperProperty(context, "自然月内启动app天数", "1天");
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
            }
        }
    }
}
