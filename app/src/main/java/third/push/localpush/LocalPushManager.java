package third.push.localpush;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.text.TextUtils;

import com.xiangha.R;

import java.util.UUID;

import acore.logic.AppCommon;
import acore.logic.XHClick;
import acore.tools.FileManager;
import acore.tools.LogManager;
import amodule.main.Main;
import third.push.model.NotificationData;

/**
 * Created by sll on 2018/1/19.
 */

public class LocalPushManager {

    public static final String TAG_MANAGER = LocalPushManager.class.getSimpleName();
    public static final String TAG_OPERATION_CLICK = "FLAG_OPERATION_CLICK";
    public static final String TAG_OPERATION_DISMISS = "TAG_OPERATION_DISMISS";

    public static final int MSG_TOTAL_COUNT = 10;

    private LocalPushManager() {}

    public static void cancelAllNotification(Context context) {
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.cancelAll();
    }

    public static void stopLocalPush(Context context) {
        new LocalPushDataManager(context).setTagNum(FileManager.xmlKey_localZhishi, String.valueOf(MSG_TOTAL_COUNT));
        cancelAlarm(context, null, null);
    }

    public static void cancelAlarm(Context context, PendingIntent operationClick, PendingIntent operationDismiss) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent myIntent = new Intent(context, LocalPushReceiver.class);
        myIntent.putExtra(TAG_MANAGER, LocalPushReceiver.TAG);
        if (operationClick != null)
            myIntent.putExtra(TAG_OPERATION_CLICK, operationClick);
        if (operationDismiss != null)
            myIntent.putExtra(TAG_OPERATION_DISMISS, operationDismiss);
        LocalPushDataManager dataManager = new LocalPushDataManager(context);
        String requestCodeStr = dataManager.getRequestCode();
        if (!TextUtils.isEmpty(requestCodeStr)) {
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, Integer.parseInt(requestCodeStr), myIntent, PendingIntent.FLAG_CANCEL_CURRENT);
            alarmManager.cancel(pendingIntent);
        }
    }

    /**
     * @param context
     * @param triggerAtMillis Time in milliseconds that the alarm should go off, using the appropriate clock (depending on the alarm type).
     * @param operationClick Action to perform when the alarm goes off;
     * @param operationDismiss
     */
    public static void execute(Context context, long triggerAtMillis, PendingIntent operationClick, PendingIntent operationDismiss) {
        if (context == null)
            return;
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent myIntent = new Intent(context, LocalPushReceiver.class);
        myIntent.putExtra(TAG_MANAGER, LocalPushReceiver.TAG);
        if (operationClick != null)
            myIntent.putExtra(TAG_OPERATION_CLICK, operationClick);
        if (operationDismiss != null)
            myIntent.putExtra(TAG_OPERATION_DISMISS, operationDismiss);
        LocalPushDataManager dataManager = new LocalPushDataManager(context);
        String requestCodeStr = dataManager.getRequestCode();
        if (!TextUtils.isEmpty(requestCodeStr)) {
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, Integer.parseInt(requestCodeStr), myIntent, PendingIntent.FLAG_CANCEL_CURRENT);
            alarmManager.cancel(pendingIntent);
        }
        UUID uuid = UUID.randomUUID();
        int requestCode = (int) uuid.getLeastSignificantBits();
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, myIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        alarmManager.set(AlarmManager.RTC, triggerAtMillis, pendingIntent);
        dataManager.saveRequestCode(String.valueOf(requestCode));
    }

    public static void pushNotification(Context context, Intent receiverIntent) {
        LocalPushDataManager dataManager = new LocalPushDataManager(context);
        NotificationData notificationData = dataManager.nextData(FileManager.xmlKey_localZhishi, MSG_TOTAL_COUNT);
        if (notificationData == null)
            return;
        pushNotification(context, receiverIntent, notificationData);
        dataManager.saveLocalPushRecord(FileManager.xmlKey_localZhishi);
        dataManager.clearRequestCode();
        NotificationData nextData = dataManager.nextData(FileManager.xmlKey_localZhishi, MSG_TOTAL_COUNT);
        if (nextData != null) {
            execute(context, nextData.getNotificationTime(), null, null);
        }
    }

    public static void pushNotification(Context context, Intent receiverIntent, NotificationData data) {
        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        Intent intent = null;
        if (data.startAvtiviyWhenClick != null) {
            intent = new Intent();
            if (data.url != null)
                intent.putExtra("url", data.url);
            intent.setClass(context, data.startAvtiviyWhenClick);
        } else if (data.url != null && data.url.length() > 5) {
            intent = AppCommon.parseURL(context, new Bundle(), data.url);
        }
        if (intent == null) {
            intent = new Intent();
            //添加统计数据
            intent.putExtra("type", data.type);
            intent.putExtra("url", data.url);
            intent.putExtra("value", data.value);
            intent.putExtra("channel", data.channel);
            intent.setClass(context, Main.class);
            LogManager.reportError("推送通知无法解析，默认开欢迎页：" + data.url, null);
        }
        intent.putExtra("from", "notify");
        intent.setAction(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
        //添加umeng统计数据
        if (!TextUtils.isEmpty(data.umengMessage)) {
            intent.putExtra("umengMessage", data.umengMessage);
        }
        //开启点击统计
        intent.putExtra(XHClick.KEY_NOTIFY_CLICK, 1);
        UUID uuid = UUID.randomUUID();
        int requestCode = (int) uuid.getLeastSignificantBits();
        PendingIntent pendingNotificationIntent = PendingIntent.getActivity(context.getApplicationContext(), requestCode, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setAutoCancel(true)
                .setContentTitle(data.title)
                .setContentText(data.content)
                .setTicker(data.ticktext)
                .setContentIntent(pendingNotificationIntent)
                .setPriority(Notification.PRIORITY_HIGH)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_launcher);

        // 通知的具体内容
        Notification notification = builder.getNotification();

        // 点击后自动消失
        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        // 添加声音和震动
        notification.defaults |= Notification.DEFAULT_SOUND;
        notification.defaults |= Notification.DEFAULT_VIBRATE;
        UUID uuid2 = UUID.randomUUID();
        int requestCode2 = (int) uuid2.getLeastSignificantBits();
        manager.notify("xhangha", requestCode2, notification);
    }

}
