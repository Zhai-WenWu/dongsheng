package com.popdialog.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.support.v4.app.NotificationManagerCompat;
import android.text.TextUtils;
import android.widget.Toast;

/**
 * PackageName : com.popdialog.util
 * Created by MrTrying on 2017/9/20 09:54.
 * E_mail : ztanzeyu@gmail.com
 */

public class PushManager {

    public static boolean isNotificationEnabled(Context context) {
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        return notificationManagerCompat.areNotificationsEnabled();
    }

    public static void tongjiPush(Context context,OnPushEnableCallback callback) {
        boolean isEnabled = isNotificationEnabled(context);
        String pushTag = String.valueOf(FileManager.loadShared(context, FileManager.PUSH_INFO, FileManager.PUSH_TAG));
        String newTag = isEnabled ? "2" : "1";
        if (!newTag.equals(pushTag)) {
            FileManager.saveShared(context, FileManager.PUSH_INFO, FileManager.PUSH_TAG, newTag);
            if(callback != null){
                callback.onPushEnable(isEnabled);
            }
        }
    }

    /**请求权限*/
    public static void requestPermission(Context context) {
        Toast.makeText(context,"请手动设置通知权限",Toast.LENGTH_SHORT).show();
        if (PushManager.isGoAppSetting()) {
            Intent intent2 = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", context.getPackageName(), null);
            intent2.setData(uri);
            context.startActivity(intent2);
        } else {
            Intent intent = new Intent(Settings.ACTION_SETTINGS);
            context.startActivity(intent);
        }
    }

    public static boolean isGoAppSetting() {
        String brand = android.os.Build.BRAND;
        if (TextUtils.isEmpty(brand)
                || brand.contains("Lenovo"))
            return false;
        return true;
    }

    public interface OnPushEnableCallback{
        void onPushEnable(boolean isEnable);
    }
}
