package third.thirdTools;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.text.TextUtils;
import android.widget.Toast;

import acore.override.helper.XHActivityManager;
import third.share.tools.ShareTools;

/**
 * Date:2018/3/15.
 * Desc:
 * Author:SLL
 * Email:
 */

public class ThirdHelper {

    public static void openThirdApp(String platform) {
        if (platform == null || TextUtils.isEmpty(platform))
            return;
        ThirdAppName name = ThirdAppName.getNameByPlatform(platform);
        if (name == null)
            return;
        switch (name) {
            case QQ:
                break;
            case QQ_ZONE:
                break;
            case WECHAT:
                openWechatApp();
                break;
            case WECHAT_MOMENTS:
                break;
            case SINA:
                break;
        }
    }

    private static void openWechatApp() {
        Activity activity = XHActivityManager.getInstance().getCurrentActivity();
        try {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            ComponentName cmp = new ComponentName("com.tencent.mm","com.tencent.mm.ui.LauncherUI");
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setComponent(cmp);
            activity.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(activity, "检查到您手机没有安装微信，请安装后使用该功能", Toast.LENGTH_LONG).show();
        }
    }

    public enum ThirdAppName {
        QQ(ShareTools.QQ_NAME),
        WECHAT(ShareTools.WEI_XIN),
        WECHAT_MOMENTS(ShareTools.WEI_QUAN),
        QQ_ZONE(ShareTools.QQ_ZONE),
        SINA(ShareTools.SINA_NAME);

        private String mPlatform;
        ThirdAppName(String platform) {
            mPlatform = platform;
        }

        public String getPlatform() {
            return mPlatform;
        }

        public static ThirdAppName getNameByPlatform(String platform) {
            if (TextUtils.isEmpty(platform))
                return null;
            ThirdAppName name = null;
            for (ThirdAppName appName : ThirdAppName.values()) {
                if (TextUtils.equals(platform, appName.getPlatform()))
                    name = appName;
            }
            return name;
        }
    }
}
