package acore.logic.login;

import android.content.Context;
import android.text.TextUtils;

import acore.tools.FileManager;
import acore.tools.ToolsDevice;

/**
 * Created by ：fei_teng on 2017/2/15 14:54.
 */

public class CountComputer {

    private static final String TIP_REMIND = "tip_remind";


    public static int getTipCount(Context context) {

        int tipCount = 0;
        String tipStr = (String) FileManager.loadShared(context, TIP_REMIND, ToolsDevice.getVerName(context));
        if (!TextUtils.isEmpty(tipStr)) {
            tipCount = Integer.valueOf(tipStr);
        }
        return tipCount;
    }

    public static void saveTipCount(Context context) {

        int tipCount = getTipCount(context);
        FileManager.saveShared(context, TIP_REMIND, ToolsDevice.getVerName(context), tipCount + 1 + "");
    }
}
