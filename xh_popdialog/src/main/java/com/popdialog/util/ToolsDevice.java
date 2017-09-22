package com.popdialog.util;

import android.app.ActivityManager;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import java.util.List;

/**
 * PackageName : com.popdialog.util
 * Created by MrTrying on 2017/9/19 16:25.
 * E_mail : ztanzeyu@gmail.com
 */

public class ToolsDevice {

    /**
     * 检测该包名在手机中的状态;
     *
     * @param packageName 完整的包名;
     *
     * @return 状态标志 0-未安装，1-已安装，2-运行在后台，3-当前运行
     */
    public static int isAppInPhone(Context context, String packageName) {
        int res = 0;
        try {
            context.getPackageManager().getPackageInfo(packageName, 0);
        } catch (Exception e) {
            return res;
        }
        res = 1;
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        // 该包名是否在当前运行;isTop1就可以了;
        List<ActivityManager.RunningTaskInfo> tasksInfo = activityManager.getRunningTasks(100);
        for (ActivityManager.RunningTaskInfo info : tasksInfo) {
            if (info != null && info.baseActivity != null && info.baseActivity.getPackageName().equals(packageName))
                res = 2;
            if (info != null && info.baseActivity != null && info.topActivity.getPackageName().equals(packageName)) {
                res = 3;
                break;
            }
        }
        return res;
    }

    /**
     * 获取手机像素高宽
     */
    public static DisplayMetrics getWindowPx(Context context) {
        DisplayMetrics metric = new DisplayMetrics();
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (manager != null) manager.getDefaultDisplay().getMetrics(metric);
        return metric;
    }
}
