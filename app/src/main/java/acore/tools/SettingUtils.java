package acore.tools;

import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;

import acore.override.XHApplication;

/**
 * Created by sll on 2018/1/8.
 */

public class SettingUtils {

    public static class NotificationSetting {

//        public static void requestApp

    }

    public static class ApplicationSetting {

        public static void openApplicationDetailSettings() {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, getPackageUri());
            XHApplication.in().getApplicationContext().startActivity(intent);
        }

    }

    public static void openSystemSettings() {
        Intent intent = new Intent(Settings.ACTION_SETTINGS);
        XHApplication.in().getApplicationContext().startActivity(intent);
    }

    private static Uri getPackageUri() {
        return Uri.parse("package:" + XHApplication.in().getPackageName());
    }
}
