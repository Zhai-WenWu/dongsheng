package third.push.localpush;

import android.content.Context;
import android.text.TextUtils;

import java.util.Map;

import acore.logic.AppCommon;
import acore.tools.FileManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import amodule.main.Main;
import third.push.model.NotificationData;

/**
 * Created by sll on 2018/1/19.
 */

public class LocalPushDataManager {

    private Context mContext;
    private Map<String,String> dataMap;

    public LocalPushDataManager(Context context) {
        mContext = context;
    }

    public void initLocalPush() {
        //获取config
        String apppushtimerangeStr = AppCommon.getConfigByLocal("apppushtimerange");
        dataMap = StringManager.getFirstMap(apppushtimerangeStr);
        if("1".equals(dataMap.get("open"))){
            //取消本地推送
            LocalPushManager.stopLocalPush(mContext);
            return;
        }
        resetTagNum(FileManager.xmlKey_localZhishi);
    }

    public NotificationData getNotificationDataByTimes(int times) {
        NotificationData notificationData = new NotificationData();
        notificationData.setTitle("今天的晚餐准备好了，快去看看吧~");
        notificationData.setContent("今天的晚餐准备好了，快去看看吧~");
        notificationData.setUrl("dishList.app?type=typeRecommend&g1=3");
        notificationData.setNotificationTime(getTriggerMillisByTimes(times));
        notificationData.setStartAvtiviyWhenClick(Main.class);
        return notificationData;
    }

    private long getTriggerMillisByTimes(int times) {
        long ret = 0;
        int hour = 19;
        int length = 180;
        //获取数据
        if(dataMap != null){
            if(dataMap.containsKey("location") && !TextUtils.isEmpty(dataMap.get("location")))
                hour = Integer.parseInt(dataMap.get("location"));
            if(dataMap.containsKey("length") && !TextUtils.isEmpty(dataMap.get("length")))
                length = Integer.parseInt(dataMap.get("length"));
        }
        int randomMin = Tools.getRandom(0,length);
        long dayMillis = System.currentTimeMillis() + (times == 1 ? 3 : 9) * 24 * 60 * 60 * 1000;
        long hourMillis = hour * 60 * 60 * 1000;
        long randomMinMillis = randomMin * 60 * 1000;
        ret = dayMillis + hourMillis + randomMinMillis;

        // TODO: 2018/1/19 测试代码，需要删除
        ret = System.currentTimeMillis() + /*3 * 24 * 60 * 60 * 1000L*/2000;
        return ret;
    }

    public void saveLocalPushRecord(String type) {

//        FileManager.xmlKey_localZhishi

        int count = getTagNum(type);
        FileManager.saveShared(mContext, FileManager.xmlFile_localPushTag, type, (++count) + "");
        //如果记录的次数超过本地数据的话，重新初始化
        if (count >= 1) {
            initLocalPush();
        }
    }

    public int getTagNum(String xmlKey) {
        String tag = FileManager.loadShared(mContext, FileManager.xmlFile_localPushTag, xmlKey) + "";
        int num = 0;
        if (!tag.equals(""))
            num = Integer.parseInt(tag);
        return num;
    }

    public void resetTagNum(String xmlKey) {
        FileManager.saveShared(mContext, FileManager.xmlFile_localPushTag, xmlKey, String.valueOf(0));
    }

    public void setTagNum(String xmlKey, String tagNum) {
        FileManager.saveShared(mContext, FileManager.xmlFile_localPushTag, xmlKey, tagNum);
    }

    public NotificationData nextData(String xmlKey, int msgTotalCount) {
        NotificationData notificationData = null;
        if (msgTotalCount < 1)
            return notificationData;
        int times = getTagNum(xmlKey);
        if (times < msgTotalCount)
            notificationData = getNotificationDataByTimes(times ++);
        return notificationData;
    }

    public void saveRequestCode(String requestCode) {
        FileManager.saveShared(mContext, FileManager.xmlFile_localPushTag, FileManager.xmlKey_localIntentRequestCode, requestCode);
    }

    public String getRequestCode() {
        return (String) FileManager.loadShared(mContext, FileManager.xmlFile_localPushTag, FileManager.xmlKey_localIntentRequestCode);
    }

    public void clearRequestCode() {
        saveRequestCode("");
    }

}
