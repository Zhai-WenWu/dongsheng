package acore.notification.controller;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;

import com.annimon.stream.Stream;
import com.popdialog.util.PushManager;
import com.xh.manager.DialogManager;
import com.xh.manager.ViewManager;
import com.xh.view.HButtonView;
import com.xh.view.TitleMessageView;
import com.xh.view.TitleView;
import com.xiangha.BuildConfig;

import java.util.HashMap;
import java.util.Map;

import acore.logic.VersionOp;
import acore.logic.XHClick;
import acore.logic.stat.StatisticsManager;
import acore.logic.stat.intefaces.OnClickListenerStat;
import acore.notification.BuildProperties;
import acore.override.XHApplication;
import acore.override.helper.XHActivityManager;
import acore.tools.FileManager;
import acore.tools.ToolsDevice;

/**
 * Created by sll on 2018/1/8.
 */

public class NotificationSettingController {
    private static final String  MODULE_NAME = "GuidetoOpenPushDialog";
    private static final String  SHOW = "GuidetoOpenPushDialog_show";
    private static final String  SUCCESS = "GuidetoOpenPushDialog_success";

    public static final String push_show_home = "push_show_home";
    public static final String push_show_message = "push_show_message";
    public static final String push_show_reciew = "push_show_reciew";
    public static final String push_show_feedBack = "push_show_feedBack";
    public static final String push_show_subject = "push_show_subject";

    public static final String pushSetHome="不错过每个精品美食推荐、哈友评论等精彩内容";
    public static final String pushSetMessage="哈友评论你后会及时获得通知";
    public static final String pushSetReview="哈友评论你后会及时获得通知";
    public static final String pushSetFeedBack="及时接收小秘书回复，更好地解决你的问题";
    public static final String pushSetSubject="哈友评论你后会及时获得通知";

    /**
     * all版本控制只显示一次
     */
    public static void showNotification(String key, String message){
        if(XHActivityManager.getInstance().getCurrentActivity()==null|| PushManager.isNotificationEnabled(XHActivityManager.getInstance().getCurrentActivity()))return;
        if(TextUtils.isEmpty((CharSequence) FileManager.loadShared(XHApplication.in(),FileManager.app_notification, key + VersionOp.getVerName(XHApplication.in())))){
            showNotificationPermissionDialog(message,key);
//            FileManager.saveShared(XHApplication.in(),FileManager.app_notification,key,"2");
        }
    }

    private static void showNotificationPermissionDialog(String message, String key){
        if (XHActivityManager.getInstance().getCurrentActivity() != null) {
            String p = XHActivityManager.getInstance().getCurrentActivity().getClass().getSimpleName();
            final DialogManager dialogManager = new DialogManager(XHActivityManager.getInstance().getCurrentActivity());
            ViewManager viewManager = new ViewManager(dialogManager);
            viewManager.setView(new TitleView(XHApplication.in()).setText("开启消息通知"))
                    .setView(new TitleMessageView(XHApplication.in()).setText(message))
                    .setView(new HButtonView(XHApplication.in())
                            .setNegativeText("取消", new OnClickListenerStat(MODULE_NAME) {
                                @Override
                                public void onClicked(View v) {
                                    dialogManager.cancel();
                                    if(!TextUtils.isEmpty(key))
                                        XHClick.mapStat(XHApplication.in(),"a_push_guidelayer",getStatisticKey(key),"点击关闭");
                                }
                            }).setPositiveText("立即开启", new OnClickListenerStat(MODULE_NAME) {

                                @Override
                                public void onClicked(View v) {
                                    dialogManager.cancel();
                                    openNotificationSettings();
                                    FileManager.saveShared(XHApplication.in(),FileManager.app_notification,FileManager.push_setting_state,"2");
                                    FileManager.saveShared(XHApplication.in(),FileManager.app_notification,FileManager.push_setting_message,getStatisticKey(key));
                                    if(!TextUtils.isEmpty(key))XHClick.mapStat(XHApplication.in(),"a_push_guidelayer",getStatisticKey(key),"点击浮条");
                                }
                            }));
            dialogManager.createDialog(viewManager);
            dialogManager.show();

            StatisticsManager.specialAction(p,MODULE_NAME,"",SHOW,"","","");
            FileManager.saveShared(XHApplication.in(),FileManager.app_notification, key + VersionOp.getVerName(XHApplication.in()),"2");
            clearUnUseKey();
        }
    }

    private static void clearUnUseKey() {
        Map<String,String> map = (Map<String, String>) FileManager.loadShared(XHApplication.in(),FileManager.app_notification,"");
        Map<String,String> current = new HashMap<>();
        Stream.of(map)
                .filter(value -> value.getKey().contains(VersionOp.getVerName(XHApplication.in())))
                .forEach(value -> current.put(value.getKey(),value.getValue()));
        //清空
        FileManager.delShared(XHApplication.in(),FileManager.app_notification,"");
        //重新保存
        FileManager.saveShared(XHApplication.in(),FileManager.app_notification,current);
    }

    public static void openNotificationSettings() {
        try {
            openAndroidSystemNotificationSettings();
        } catch (Exception e) {
            e.printStackTrace();
            BuildProperties properties = ToolsDevice.getBuildProperties();
            String rom_type = ToolsDevice.getRomType(properties);
            switch (rom_type) {
                case ToolsDevice.EMUI:
                    openEMUINotificationSettings();
                    break;
                case ToolsDevice.MIUI:
                    if (properties != null) {
                        openMIUINotificationSettings(properties);
                    } else {
                        openSettings();
                    }
                    break;
                case ToolsDevice.FLYME:
                    openFlymeNotificationSettings();
                    break;
                case ToolsDevice.OPPO:
                    openOPPONotificationSettings();
                    break;
                case ToolsDevice.VIVO:
                    openVIVONotificationSettings();
                    break;
                default:
                    try {
                        toSetting();
                    } catch (Exception e1) {
                        e1.printStackTrace();
                        openSettings();
                    }
                    break;
            }
        }
    }

    private static void openSettings() {
        Intent intent = new Intent(Settings.ACTION_SETTINGS);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        XHActivityManager.getInstance().getCurrentActivity().startActivity(intent);
    }

    private static void toSetting() {
        Intent localIntent = new Intent();
        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
        localIntent.setData(Uri.fromParts("package", XHActivityManager.getInstance().getCurrentActivity().getPackageName(), null));
        XHActivityManager.getInstance().getCurrentActivity().startActivity(localIntent);
    }

    private static void openEMUINotificationSettings() {
        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("packageName", BuildConfig.APPLICATION_ID);
        ComponentName comp = new ComponentName("com.huawei.systemmanager", "com.huawei.permissionmanager.ui.MainActivity");
        intent.setComponent(comp);
        XHActivityManager.getInstance().getCurrentActivity().startActivity(intent);
    }

    private static void openMIUINotificationSettings(BuildProperties properties) {
        String name = properties.getProperty(ToolsDevice.KEY_MIUI_VERSION_NAME);
        if (TextUtils.isEmpty(name))
            return;
        Context context = XHActivityManager.getInstance().getCurrentActivity();
        Intent intent;
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

    private static void openFlymeNotificationSettings() {
        Context context = XHActivityManager.getInstance().getCurrentActivity();
        Intent intent = new Intent("com.meizu.safe.security.SHOW_APPSEC");
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.putExtra("packageName", BuildConfig.APPLICATION_ID);
        context.startActivity(intent);
    }

    private static void openOPPONotificationSettings() {
        Context context = XHActivityManager.getInstance().getCurrentActivity();
        Intent intent = new Intent("com.meizu.safe.security.SHOW_APPSEC");
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.putExtra("packageName", BuildConfig.APPLICATION_ID);
        context.startActivity(intent);
    }

    private static void openVIVONotificationSettings() {
        Context context = XHActivityManager.getInstance().getCurrentActivity();
        Intent intent = context.getPackageManager().getLaunchIntentForPackage("com.iqoo.secure");
        if(intent != null) {
            context.startActivity(intent);
        }
    }

    private static void openAndroidSystemNotificationSettings() {
        Context context = XHActivityManager.getInstance().getCurrentActivity();
        Intent intent = new Intent();
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName());
        } else if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
            intent.putExtra("app_package", context.getPackageName());
            intent.putExtra("app_uid", context.getApplicationInfo().uid);
        } else if (android.os.Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
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

    private static String getStatisticKey(String name){
        if(name.equals(VersionOp.getVerName(XHApplication.in()))){
            return "首页浮条点击";
        }
        switch (name){
            case push_show_message:
                return "进行消息页面后浮条点击";
            case push_show_subject:
                return "成功发帖子后浮条点击";
            case push_show_reciew:
                return "发送评论成功后浮条点击";
            case push_show_feedBack:
                return "发送小秘书后浮条点击";
        }
        return "";
    }

    public static void statOpenSuccess(String p){
        StatisticsManager.specialAction(p,MODULE_NAME,"",SUCCESS,"","","");
    }
}
