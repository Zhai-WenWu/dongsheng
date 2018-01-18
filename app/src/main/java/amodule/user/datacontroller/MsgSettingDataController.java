package amodule.user.datacontroller;

import android.text.TextUtils;

import java.util.Map;

import acore.override.XHApplication;
import acore.tools.FileManager;
import xh.basic.tool.UtilFile;

/**
 * Created by sll on 2018/1/12.
 */

public class MsgSettingDataController {

    public MsgSettingDataController() {
    }

    public void saveAllData(Map<String, String> settingMap) {
        if (settingMap == null || settingMap.isEmpty())
            return;
        UtilFile.saveShared(XHApplication.in(), FileManager.msgInform, settingMap);
    }

    public void saveData(String key, String value) {
        if (TextUtils.isEmpty(key))
            return;
        UtilFile.saveShared(XHApplication.in(), FileManager.msgInform, key, value);
    }

    public boolean checkValueNullByKey (String key) {
        boolean ret = true;
        if (TextUtils.isEmpty(key))
            return ret;
        String value = (String) UtilFile.loadShared(XHApplication.in(), FileManager.msgInform, key);
        if (TextUtils.isEmpty(value))
            return ret;
        return !ret;
    }

    public boolean checkNewMsgOpen(String key) {
        boolean ret = false;
        if (TextUtils.isEmpty(key))
            return ret;
        String value = (String) UtilFile.loadShared(XHApplication.in(), FileManager.msgInform, key);
        if (TextUtils.isEmpty(value))
            return ret;
        ret = TextUtils.equals(value, "1");
        return ret;
    }

    public boolean checkOpenByKey(String key) {
        boolean ret = true;
        if (TextUtils.isEmpty(key))
            return ret;
        String value = (String) UtilFile.loadShared(XHApplication.in(), FileManager.msgInform, key);
        if (TextUtils.isEmpty(value))
            return ret;
        ret = TextUtils.equals(value, "1");
        return ret;
    }

    /**
     * official	100	 运营推送的消息，用于后台推送的类型
     * comments	200	 评论，用于美食圈等评论消息
     * feedback	400	 小秘书，用于后台推送的小秘书内消息
     * good	500	 点赞，用于美食圈被人点赞后的消息
     * qa	 600	 问答，用于提问、回答、追问追答等消息类型
     * silence	 10000-20000	预留静默消息类型区间，此区间内消息类型属于不在客户端展示的消息类型
     * @param type
     * @param silenceRunnable
     * @return
     */
    public boolean checkOpenByType(String type, Runnable silenceRunnable) {
        boolean ret = true;
        if (TextUtils.isEmpty(type))
            return ret;
        String key = "";
        int value = Integer.parseInt(type);
        if (value >= 10000 && value <= 20000) {
            ret = false;
            if (silenceRunnable != null)
                silenceRunnable.run();
            return ret;
        }
        switch (value) {
            case 100:
                key = "official";
                break;
            case 200:
                key = "comments";
                break;
            case 400:
                key = "feedback";
                break;
            case 500:
                key = "good";
                break;
            case 600:
                key = "qa";
                break;
        }
        if (TextUtils.isEmpty(key))
            return ret;
        ret = checkOpenByKey(key);
        return ret;
    }
}
