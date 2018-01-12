package third.push.xg;

import com.tencent.android.tpush.XGPushManager;

import acore.override.XHApplication;

/**
 * Created by sll on 2018/1/12.
 */

public class XGTagManager {

    public static final String APP_NEW = "app:new";
    public static final String OFFICIAL = "official";

    public XGTagManager() {
    }

    public void addXGTag(String tag) {
        XGPushManager.setTag(XHApplication.in(), tag);
    }

    public void removeXGTag(String tag) {
        XGPushManager.deleteTag(XHApplication.in(), tag);
    }
}
