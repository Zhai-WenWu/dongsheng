package third.push.xg;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;


import com.tencent.android.tpush.XGLocalMessage;
import com.tencent.android.tpush.XGPushManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.XHClick;
import acore.tools.FileManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;
import aplug.service.alarm.PushAlarm;

@SuppressLint("SimpleDateFormat")
public class XGLocalPushServer {
    private Context mContext;
    private Map<String,String> dataMap;

    public XGLocalPushServer(Context context) {
        this.mContext = context;
    }

    /**
     * 初始化本地推送
     */
    public void initLocalPush() {
        //获取config
        String apppushtimerangeStr = AppCommon.getConfigByLocal("apppushtimerange");
        dataMap = StringManager.getFirstMap(apppushtimerangeStr);
        if("1".equals(dataMap.get("open"))){
            //取消本地推送
            XGPushManager.clearLocalNotifications(mContext);
            return;
        }

        int firstday = 3;
        FileManager.saveShared(mContext, FileManager.xmlFile_localPushTag, FileManager.xmlKey_localZhishi, String.valueOf(0));
        String localPushData = FileManager.readFile(FileManager.getDataDir() + FileManager.file_nousLocalPushData);
        //防止没有数据
        if (TextUtils.isEmpty(localPushData) && localPushData.length() < 10) {
            localPushData = FileManager.getFromAssets(mContext, FileManager.file_nousLocalPushData);
        }
        ArrayList<Map<String, String>> data = StringManager.getListMapByJson(localPushData);
        if (data.size() > 0) {
            initNousLocalPush(mContext, firstday + 3, data, FileManager.xmlKey_localCaidan);
            initDishLocalPush(mContext, firstday, data.size());
        }
        PushAlarm.startTimingWake(mContext);

    }

    /**
     * 初始化菜谱推送
     *
     * @param context
     * @param firstday
     * @param size
     */
    private void initDishLocalPush(Context context, int firstday, int size) {
        for (int i = 0; i < size; i++) {
            String title = "今天的晚餐准备好了，快去看看吧~";
            String content = "今天的晚餐准备好了，快去看看吧~";
            String url = "dishList.app?type=typeRecommend&g1=3";
            // 设置key,value
            HashMap<String, String> map = new HashMap<>();
            map.put("t", XHClick.NOTIFY_SELF + "");
            map.put("d", url);
            selfAwakening(context, firstday + i * 6, title, content, map);
        }
    }

    /**
     * 初始化知识推送
     *
     * @param context
     * @param startDay
     * @param data
     * @param xmlKey
     */
    private void initNousLocalPush(Context context, int startDay, ArrayList<Map<String, String>> data, String xmlKey) {
        for (int i = 0; i < data.size(); i++) {
            int index = (i + getTagNum(context, xmlKey)) % data.size();
            String title = data.get(index).get("title");
            String content = data.get(index).get("content");
            String url = "nousInfo.app?code=" + data.get(index).get("code");
            // 设置key,value
            HashMap<String, String> map = new HashMap<>();
            map.put("t", XHClick.NOTIFY_SELF + "");
            map.put("d", url);
            selfAwakening(context, startDay + i * 6, title, content, map);
        }
    }

    /**
     * 获取本地推送的num
     *
     * @param context
     * @param xmlKey
     * @return
     */
    private static int getTagNum(Context context, String xmlKey) {
        String tag = FileManager.loadShared(context, FileManager.xmlFile_localPushTag, xmlKey) + "";
        int num = 0;
        if (!tag.equals(""))
            num = Integer.parseInt(tag);
        return num;
    }

    /**
     * 自我唤醒
     *
     * @param day     延迟天数
     * @param content 通知内容
     */
    private void selfAwakening(Context context, int day, String title, String content, HashMap<String, String> map) {
        XGLocalMessage local_msg = new XGLocalMessage();
        // 设置本地消息类型，1:通知，2:消息
        local_msg.setType(2);
        // 设置消息标题
        local_msg.setTitle(title);
        // 设置消息内容
        local_msg.setContent(content);
        // 设置消息日期，格式为：20140502
        long currentMillis = System.currentTimeMillis();
        local_msg.setBuilderId(currentMillis);
        java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("yyyyMMdd");
        java.util.Date curDate = new java.util.Date(currentMillis);
        Calendar cal = Calendar.getInstance();
        cal.setTime(curDate);
        cal.add(Calendar.DATE, day);
        String date = formatter.format(cal.getTime());
        local_msg.setDate(date);

        //默认数据
        int hour = 19;
        int length = 180;
        //获取数据
        if(dataMap != null){
            if(dataMap.containsKey("location") && !TextUtils.isEmpty(dataMap.get("location")))
                hour = Integer.parseInt(dataMap.get("location"));
            if(dataMap.containsKey("length") && !TextUtils.isEmpty(dataMap.get("length")))
                length = Integer.parseInt(dataMap.get("length"));
        }
        cal.set(Calendar.HOUR_OF_DAY,hour);
        cal.set(Calendar.MINUTE,00);
        int randomMin = Tools.getRandom(0,length);
        cal.add(Calendar.MINUTE,randomMin);

        formatter = new java.text.SimpleDateFormat("HH");
        String hourStr = formatter.format(cal.getTime());
        // 设置消息触发的小时(24小时制)，例如：22代表晚上10点
        local_msg.setHour(hourStr);

        formatter = new java.text.SimpleDateFormat("mm");
        String minStr = formatter.format(cal.getTime());
//		 获取消息触发的分钟，例如：05代表05分
        local_msg.setMin(minStr);
        //设置map
        local_msg.setCustomContent(map);
        XGPushManager.addLocalNotification(context, local_msg);
        Log.d("-------------------", "" + local_msg.toString());
    }

    /**
     * 保存本地推送次数
     *
     * @param context
     * @param type
     */
    public void saveLocalPushRecord(Context context, String type) {
        int count = getTagNum(context, type);
        FileManager.saveShared(context, FileManager.xmlFile_localPushTag, type, (++count) + "");
        //如果记录的次数超过本地数据的话，重新初始化
        String localPushData = FileManager.readFile(FileManager.getDataDir() + FileManager.file_nousLocalPushData);
        ArrayList<Map<String, String>> data = StringManager.getListMapByJson(localPushData);
        if (count >= data.size()) {
            initLocalPush();
        }
    }

    final static int MAX_PAGE = 2;
    public int page = 1;
    ArrayList<Map<String, String>> nousData = new ArrayList<>();

    /**
     * 获取知识的本地推送数据
     */
    public void getNousLocalPushData() {
        if (FileManager.ifFileModifyByCompletePath(FileManager.getDataDir() + FileManager.file_nousLocalPushData, 24 * 60) != null) {
            return;
        }
        if (page > MAX_PAGE) {
            String json = Tools.list2Json(nousData);
            FileManager.saveFileToCompletePath(FileManager.getDataDir() + FileManager.file_nousLocalPushData, json, false);
            return;
        }
        String url = StringManager.api_nousList + "?type=new" + "&page=" + page++;
        ReqInternet.in().doGet(url, new InternetCallback(mContext) {
            @Override
            public void loaded(int i, String s, Object o) {
                if (i >= ReqInternet.REQ_OK_STRING) {
                    ArrayList<Map<String, String>> returnData = StringManager.getListMapByJson(o);
                    if (returnData.size() > 0) {
                        Map<String, String> map = returnData.get(0);
                        returnData = StringManager.getListMapByJson(map.get("nous"));
                        nousData.addAll(returnData);
                        getNousLocalPushData();
                    }
                }
            }
        });
    }

}