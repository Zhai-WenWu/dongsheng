package acore.logic;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import acore.logic.stat.DesktopLayout;
import acore.tools.FileManager;
import acore.tools.Tools;
import amodule.dish.activity.MenuDish;
import third.ad.XHAdAutoRefresh;

import static android.content.Context.MODE_PRIVATE;

/**
 * Description : 特殊指令处理类
 * PackageName : acore.logic
 * Created by MrTrying on 2017/11/7 11:06.
 * Author : mrtrying
 * E_mail : ztanzeyu@gmail.com
 */

public class SpecialOrder {
    public static final String SP_FILENAME = "SpecialCommand";
    private static final String ORDER_PREFIX = "//";
    private static final String GrowingIOOrder = "//growingioopen";
    private static final String START_MENU = "//startmenu";
    private static final String SET_AD_REFRESH_INTERVAL_TIME = "//setadrefreshtime";
    public static final String OPEN_STAT = "//stat";

    public static SpecialOrder of() {
        return new SpecialOrder();
    }

    /**
     * 执行指令
     *
     * @param order 指令
     *
     * @return 是否为有效指令
     */
    public boolean handlerOrder(Context context, String order) {
        if (null == context || TextUtils.isEmpty(order)) {
            return false;
        }
        switch (order) {
            case GrowingIOOrder:
                String isInputOrder = FileManager.loadShared(context, FileManager.file_appData, FileManager.xmlKey_growingioopen).toString();
                boolean isOpen = "true".equals(isInputOrder);
                FileManager.saveShared(context, FileManager.file_appData, FileManager.xmlKey_growingioopen, isOpen ? "false" : "true");
                Tools.showToast(context, isOpen ? "GrowingIO随即模式" : "GrowingIO强制开启模式");
                return true;
            case START_MENU:
                context.startActivity(new Intent(context, MenuDish.class));
                return true;
            case SET_AD_REFRESH_INTERVAL_TIME:
                XHAdAutoRefresh.intervalTime = 5 * 3 * 1000;
                Tools.showToast(context, "自有广告刷新间隔为15s\n重启App后重置");
                return true;
            case OPEN_STAT:
                switchStatLayoutVisibility(context);
                return true;
            default:
                return order.startsWith(ORDER_PREFIX);
        }
    }

    /*--------------------------------------显示统计浮框--------------------------------------*/

    public static void switchStatLayoutVisibility(Context context) {
        boolean openSwitch = isOpenSwitchStatLayout(context);
        openSwitch = !openSwitch;

        if (openSwitch) {
            DesktopLayout.of(context).showDesk();
        } else {
            DesktopLayout.of(context).closeDesk();
        }
        setSwitchOpen(context,OPEN_STAT, openSwitch);
    }

    public static boolean isOpenSwitchStatLayout(@NonNull Context context) {
        return isOpen(context, OPEN_STAT);
    }

    /*--------------------------------------公共方法--------------------------------------*/

    private static boolean isOpen(@NonNull Context context, String key) {
        if (context == null) return false;
        SharedPreferences preferences = context.getSharedPreferences(SP_FILENAME, MODE_PRIVATE);
        return preferences.getBoolean(key, false);
    }

    private static void setSwitchOpen(@NonNull Context context,String key, boolean openSwitch) {
        if (context == null) return;
        SharedPreferences.Editor editor = context.getSharedPreferences(SP_FILENAME, MODE_PRIVATE).edit();
        editor.putBoolean(key, openSwitch);
        editor.apply();
    }

    private static String getCurrentValue(@NonNull Context context,String key){
        if (context == null) return "";
        SharedPreferences preferences = context.getSharedPreferences(SP_FILENAME, MODE_PRIVATE);
        return preferences.getString(key, "");
    }

    private static void setCurrentValue(@NonNull Context context,String key,@NonNull String value){
        if (context == null) return;
        SharedPreferences.Editor editor = context.getSharedPreferences(SP_FILENAME, MODE_PRIVATE).edit();
        editor.putString(key, value);
        editor.apply();
    }
}
