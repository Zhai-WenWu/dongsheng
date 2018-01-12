package third.push;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.text.TextUtils;

import com.xiangha.R;

import java.text.SimpleDateFormat;
import java.util.Date;

import acore.logic.AppCommon;
import acore.logic.XHClick;
import acore.override.XHApplication;
import acore.tools.FileManager;
import acore.tools.LogManager;
import acore.tools.Tools;
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
        if (data == null || context == null) {
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
        boolean headerUp = checkAppForeground();
        if (data.hasImage())
            showNotifyWithImg(context, data, headerUp);
        else
            showNotify(context, data, headerUp);

    }

    /**
     * 展示通知
     *
     * @param context
     * @param data
     */

    private void showNotify(Context context, NotificationData data, boolean headerUp) {
        if (data == null) {
            return;
        }
        NotificationManagerCompat nManger = NotificationManagerCompat.from(context);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setAutoCancel(true)
                .setTicker(data.ticktext)
                .setContentTitle(data.title)
                .setContentText(data.content)
                .setContentIntent(getContentIntent(context, data))
                .setSmallIcon(data.iconResId)
                .setWhen(System.currentTimeMillis());
        if(headerUp) {
            builder.setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setFullScreenIntent(getContentIntent(context, data), true);
        }
        Notification notification = getNotification(context, builder, data);
        nManger.notify("xiangha", data.notificationId, notification);
        pushStatics(context, data);
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

    private boolean checkAppForeground() {
        return Tools.isAppOnForeground();
    }

    private void showNotifyWithImg(Context context, NotificationData data, boolean headerUp) {
        ImageRemoteViews remoteViews = new ImageRemoteViews(XHApplication.in().getPackageName(), R.layout.notification_imgtxt_view_layout);
        remoteViews.setTextViewText(R.id.title, data.title);
        remoteViews.setTextViewText(R.id.desc, data.content);
        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
        remoteViews.setTextViewText(R.id.time, format.format(new Date(System.currentTimeMillis())));
        remoteViews.loadImage(data.imgUrl, context, new ImageRemoteViews.Callback() {
            @Override
            public void callback(Bitmap bitmap) {

                remoteViews.setImageViewBitmap(R.id.img, bitmap);

                NotificationManagerCompat manger = NotificationManagerCompat.from(context);
                NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
//                        .setCustomHeadsUpContentView(remoteViews)
                        .setAutoCancel(true)
                        .setColor(Color.TRANSPARENT)
                        .setContent(remoteViews)
                        .setTicker(data.ticktext)
                        .setContentTitle(data.title)
                        .setContentText(data.content)
                        .setContentIntent(getContentIntent(context, data))
                        .setSmallIcon(data.iconResId);
                if(headerUp) {
                    builder.setPriority(Notification.PRIORITY_HIGH)
                            .setFullScreenIntent(getContentIntent(context, data), true);
                }
                Notification notification = getNotification(context, builder, data);
                manger.notify("xiangha", data.notificationId, notification);
                pushStatics(context, data);
            }
        });
    }

    private Notification getNotification(Context context, NotificationCompat.Builder builder, NotificationData data) {
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
        return notification;
    }

    private void pushStatics(Context context, NotificationData data) {
        //统计
        if (data.type == XHClick.NOTIFY_A
                || data.type == XHClick.NOTIFY_SELF) {
            XHClick.statisticsPush(context, XHClick.STATE_SHOW, Build.VERSION.SDK_INT);
        }
        XHClick.statisticsNotify(context, data, "show");
    }
}

