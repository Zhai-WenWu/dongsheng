package com.popdialog.util;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import com.popdialog.GoodCommentDialogControl;

import static java.lang.System.currentTimeMillis;

/**
 * PackageName : com.popdialog.util
 * Created by MrTrying on 2017/9/20 10:32.
 * E_mail : ztanzeyu@gmail.com
 */

public class GoodCommentManager {

    public static boolean okShow = false;//展示用户开关
    public static int contentTime = 5;

    /**
     * 去好评
     *
     * @param type 好评类型
     */
    public static void setGoodComment(String type, Activity activity) {
        FileManager.saveShared(activity, FileManager.GOODCOMMENT_TYPE, FileManager.GOODCOMMENT_TYPE, type);
        FileManager.saveShared(activity, type, type, String.valueOf(currentTimeMillis()));
        setMoreHot(activity);
    }

    /**
     * 直接跳到应用市场评分
     */
    private static void setMoreHot(Activity activity) {
        if (activity == null)
            return;
        String str = "market://details?id=" + activity.getPackageName();
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(str));
            activity.startActivity(intent);

        } catch (Exception e) {
        } finally {
            activity = null;
        }
    }

    /**
     * 获取记录去好评的的时间
     * 好评类型
     */
    public static void setStictis(Activity activity,GoodCommentDialogControl.OnCommentTimeStatisticsCallback onCommentTimeStatisticsCallback) {
        String type = (String) FileManager.loadShared(activity, FileManager.GOODCOMMENT_TYPE, FileManager.GOODCOMMENT_TYPE);
        if (!TextUtils.isEmpty(type)) {
            String type_time = (String) FileManager.loadShared(activity, type, type);
            if (!TextUtils.isEmpty(type_time)) {
                long time = Long.parseLong(type_time);
                if (time > 0) {
                    long time_interval = currentTimeMillis() - time;
                    if (time_interval >= contentTime * 1000) {
                        FileManager.saveShared(activity, FileManager.GOODCOMMENT_INFO, FileManager.GOODCOMMENT_TIME, String.valueOf(currentTimeMillis()));
                        okShow = true;
                        if(onCommentTimeStatisticsCallback != null){
                            onCommentTimeStatisticsCallback.onStatistics(type, ">" + contentTime);
                        }
                    } else {
                        String temp = String.valueOf(time_interval / 1000);
                        if (temp.contains(".")) {
                            temp = temp.substring(0, temp.indexOf("."));
                        }
                        if(onCommentTimeStatisticsCallback != null){
                            onCommentTimeStatisticsCallback.onStatistics(type, temp);
                        }
                    }
                }
            }
            contentTime = 5;
            FileManager.saveShared(activity, type, type, "0");
            activity = null;
        }
    }
}
