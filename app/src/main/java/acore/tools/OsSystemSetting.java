package acore.tools;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import acore.notification.BuildProperties;
import acore.override.XHApplication;
import acore.override.helper.XHActivityManager;

/**
 * Created by yujian on 2018/6/7.
 */

public class OsSystemSetting {

    public static void openPermissionSettings() {
        try {
            openAndroidSystemPermissionSettings();
        } catch (Exception e) {
            e.printStackTrace();
            BuildProperties properties = ToolsDevice.getBuildProperties();
            String rom_type = ToolsDevice.getRomType(properties);
            switch (rom_type) {
                case ToolsDevice.EMUI:
                    openEMUIPermissionSettings();
                    break;
                case ToolsDevice.MIUI:
                    if (properties != null) {
                        openMIUIPermissionSettings(properties);
                    } else {
                        openSettings();
                    }
                    break;
                case ToolsDevice.FLYME:
                    openFlymePermissionSettings();
                    break;
                case ToolsDevice.OPPO:
                    openOPPOPermissionSettings();
                    break;
                case ToolsDevice.VIVO:
                    openVIVOPermissionSettings();
                    break;
                default:
                    openSettings();
                    break;
            }
        }
    }

    public static void openSettings() {
        Intent intent = new Intent(Settings.ACTION_SETTINGS);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        XHActivityManager.getInstance().getCurrentActivity().startActivity(intent);
    }

    private static void openEMUIPermissionSettings() {
        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("packageName", "com.xiangha");
        ComponentName comp = new ComponentName("com.huawei.systemmanager", "com.huawei.permissionmanager.ui.MainActivity");
        intent.setComponent(comp);
        XHActivityManager.getInstance().getCurrentActivity().startActivity(intent);
    }

    private static void openMIUIPermissionSettings(BuildProperties properties) {
        String name = properties.getProperty(ToolsDevice.KEY_MIUI_VERSION_NAME);
        if (TextUtils.isEmpty(name))
            return;
        Context context = XHActivityManager.getInstance().getCurrentActivity();
        Intent intent = null;
        switch (name) {
            case "V5":
                intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, getPackageUri());
                break;
            case "V6":
            case "V7":
                intent = new Intent("miui.intent.action.APP_PERM_EDITOR");
                intent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.AppPermissionsEditorActivity");
                intent.putExtra("extra_pkgname", context.getPackageName());
                break;
            case "V8":
                intent = new Intent("miui.intent.action.APP_PERM_EDITOR");
                intent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.PermissionsEditorActivity");
                intent.putExtra("extra_pkgname", context.getPackageName());
                break;
            default:
                intent = new Intent("miui.intent.action.APP_PERM_EDITOR");
                intent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.PermissionsEditorActivity");
                intent.putExtra("extra_pkgname", context.getPackageName());
                break;
        }
        context.startActivity(intent);
    }

    private static void openFlymePermissionSettings() {
        Context context = XHActivityManager.getInstance().getCurrentActivity();
        Intent intent = new Intent("com.meizu.safe.security.SHOW_APPSEC");
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.putExtra("packageName", "com.xiangha");
        context.startActivity(intent);
    }

    private static void openOPPOPermissionSettings() {
        Context context = XHActivityManager.getInstance().getCurrentActivity();
        Intent intent = new Intent("com.meizu.safe.security.SHOW_APPSEC");
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.putExtra("packageName", "com.xiangha");
        context.startActivity(intent);
    }

    private static void openVIVOPermissionSettings() {
        Context context = XHActivityManager.getInstance().getCurrentActivity();
        Intent intent = context.getPackageManager().getLaunchIntentForPackage("com.iqoo.secure");
        if(intent != null) {
            context.startActivity(intent);
        }
    }

    private static void openAndroidSystemPermissionSettings() {
        Context context = XHActivityManager.getInstance().getCurrentActivity();
        Intent intent = new Intent();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.putExtra("app_package", context.getPackageName());
            intent.putExtra("app_uid", context.getApplicationInfo().uid);
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.setData(Uri.parse("package:" + context.getPackageName()));
        } else {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            intent.setData(Uri.fromParts("package", context.getPackageName(), null));
        }
        context.startActivity(intent);
    }
    private static Uri getPackageUri() {
        return Uri.parse("package:" + XHApplication.in().getPackageName());
    }

}
