package third.push;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;

import acore.logic.AppCommon;
import acore.logic.XHClick;
import acore.tools.FileManager;
import acore.tools.LogManager;
import amodule.main.Main;
import third.push.broadcast.ClickNotificationBroadcast;
import third.push.broadcast.DismissNotificationBroadcast;
import third.push.model.NotificationData;

public class NotificationManager {

    public NotificationManager() {
    }

    //清除通知
    public void notificationClear(Context context, int customDefineNotifyId) {
        android.app.NotificationManager nManger = (android.app.NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nManger.cancel(customDefineNotifyId);
    }

    /**
     * 弹出一个消息提示
     *
     * @param context
     * @param data
     */
    public void notificationActivity(Context context, NotificationData data) {
        if (data == null) {
            return;
        }
        //开关
        if (data.type != XHClick.NOTIFY_SELF && data.url != null) {
            if (data.url.indexOf("dishInfo.app?") > -1
                    && FileManager.loadShared(context, FileManager.msgInform, FileManager.caipu) != ""
                    && FileManager.loadShared(context, FileManager.msgInform, FileManager.caipu).equals("2")) {
                return;
            } else if (data.url.indexOf("dishList.app?") > -1
                    && FileManager.loadShared(context, FileManager.msgInform, FileManager.menu) != ""
                    && FileManager.loadShared(context, FileManager.msgInform, FileManager.menu).equals("2")) {
                return;
            } else if (data.url.indexOf("nousInfo.app?") > -1
                    && FileManager.loadShared(context, FileManager.msgInform, FileManager.zhishi) != ""
                    && FileManager.loadShared(context, FileManager.msgInform, FileManager.zhishi).equals("2")) {
                return;
            } else if (data.url.indexOf("subjectInfo.app?") > -1
                    && data.url.indexOf("newsId") == -1
                    && FileManager.loadShared(context, FileManager.msgInform, FileManager.quan) != ""
                    && FileManager.loadShared(context, FileManager.msgInform, FileManager.quan).equals("2")) {
                return;
            } else if (data.url.indexOf("subjectInfo.app?") > -1
                    && data.url.indexOf("newsId") > -1
                    && FileManager.loadShared(context, FileManager.msgInform, FileManager.zan) != ""
                    && FileManager.loadShared(context, FileManager.msgInform, FileManager.zan).equals("2")) {
                return;
            }
        }
        //统计
        if (data.type == XHClick.NOTIFY_A
                || data.type == XHClick.NOTIFY_SELF) {
            XHClick.statisticsPush(context, XHClick.STATE_CREATENOTIFY, Build.VERSION.SDK_INT);
        }
        showNotify(context, data);

    }

    /**
     * 展示通知
     *
     * @param context
     * @param data
     */

    private void showNotify(Context context, NotificationData data) {
        if (data == null) {
            return;
        }
        android.app.NotificationManager nManger = (android.app.NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification.Builder builder = new Notification.Builder(context)
                .setAutoCancel(true)
                .setTicker(data.ticktext)
                .setContentTitle(data.ticktext)
                .setContentText(data.ticktext)
                .setContentIntent(getContentIntent(context, data))
                .setSmallIcon(data.iconResId)
                .setWhen(System.currentTimeMillis());
        Notification notification = builder.getNotification();
        //设置dimiss intent
        if (PushPraserService.TYPE_UMENG.equals(data.channel)
                && !TextUtils.isEmpty(data.umengMessage)) {
            LogManager.print("d", "Push_start");
            PendingIntent dismissPendingIntent = getDismissPendingIntent(context, data);
            notification.deleteIntent = dismissPendingIntent;
        }

        String msgSing = (String) FileManager.loadShared(context, FileManager.msgInform, FileManager.informSing);
        String msgShork = (String) FileManager.loadShared(context, FileManager.msgInform, FileManager.informShork);
        //控制声音
        if (msgSing != "" && msgSing.equals("1")) {
            notification.defaults |= Notification.DEFAULT_SOUND;
        }
        //控制震动
        if (msgShork != "" && msgShork.equals("1")) {
            notification.defaults |= Notification.DEFAULT_VIBRATE;
        }
        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        nManger.notify("xiangha", data.notificationId, notification);
        //统计
        if (data.type == XHClick.NOTIFY_A
                || data.type == XHClick.NOTIFY_SELF) {
            XHClick.statisticsPush(context, XHClick.STATE_SHOW, Build.VERSION.SDK_INT);
        }
        XHClick.statisticsNotify(context, data, "show");
    }

    /**
     * 生成content PendingIntent
     *
     * @param context
     * @param data
     *
     * @return
     */
    private PendingIntent getContentIntent(Context context, NotificationData data) {
        Intent intent = null;
        if (data != null) {
            try {
                if (data.startAvtiviyWhenClick != null) {
                    intent = new Intent();
                    if (data.url != null)
                        intent.putExtra("url", data.url);
                    intent.setClass(context, data.startAvtiviyWhenClick);
                } else if (data.url != null && data.url.length() > 5) {
                    intent = AppCommon.parseURL(context, new Bundle(), data.url);
                }
            } catch (Exception e) {

            }
            if (data.type == XHClick.NOTIFY_A || data.type == XHClick.NOTIFY_SELF) {
                XHClick.statisticsPush(context, XHClick.STATE_PRASEURL, Build.VERSION.SDK_INT);
            }
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
        Intent clickIntent = new Intent(context, ClickNotificationBroadcast.class); // 点击真正的Intent
        clickIntent.putExtra("realIntent", intent);
        //添加umeng统计数据
        if (!TextUtils.isEmpty(data.umengMessage)) {
            clickIntent.putExtra("umengMessage", data.umengMessage);
        }
        return PendingIntent.getBroadcast(context, 0, clickIntent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    /**
     * 生成Dismiss PendingIntent
     *
     * @param context
     * @param data
     *
     * @return
     */
    private PendingIntent getDismissPendingIntent(Context context, NotificationData data) {
        Intent deleteIntent = new Intent();
        deleteIntent.setClass(context, DismissNotificationBroadcast.class);
        if (!TextUtils.isEmpty(data.umengMessage)) {
            deleteIntent.putExtra("umengMessage", data.umengMessage);
        }
        //添加统计数据
        deleteIntent.putExtra("type", data.type);
        deleteIntent.putExtra("url", data.url);
        deleteIntent.putExtra("value", data.value);
        deleteIntent.putExtra("channel", data.channel);
        PendingIntent deletePendingIntent = PendingIntent.getBroadcast(context,
                (int) (System.currentTimeMillis() + 1),
                deleteIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        LogManager.print("d", "Push_getDismissPendingIntent");
        return deletePendingIntent;
    }
}

