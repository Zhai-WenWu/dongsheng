package third.push;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.tencent.android.tpush.XGPushTextMessage;
import com.xiangha.R;

import java.util.Map;
import java.util.UUID;

import acore.logic.MessageTipController;
import acore.logic.XHClick;
import acore.override.XHApplication;
import acore.override.helper.XHActivityManager;
import acore.tools.FileManager;
import acore.tools.StringManager;
import acore.tools.ToolsDevice;
import amodule.main.Main;
import amodule.user.datacontroller.MsgSettingDataController;
import aplug.feedback.activity.Feedback;
import aplug.service.base.ServiceManager;
import third.push.model.NotificationData;
import third.push.model.NotificationEvent;
import third.push.xg.XGLocalPushServer;

/**
 * PackageName : third.push.model
 * Created by MrTrying on 2016/8/16 11:08.
 * E_mail : ztanzeyu@gmail.com
 * <p>
 * 消息：
 */
public class PushPraserService {
    public static final String TYPE_XG = "xg";

    public void handleData(Context context, Intent intent) {
        if (null != intent) {
            //处理必要信息
            String title = intent.getStringExtra("title");
            String text = intent.getStringExtra("text");
            String custom = intent.getStringExtra("custom");
            parsePushData(context, title, text, custom);
        }
        ServiceManager.startProtectService(XHApplication.in());
    }

    /**
     * 根据api文档,处理接受到的数据;
     *
     * @param context
     * @param alertTitle
     * @param msgAlert
     * @param extrajson
     */
    public void parsePushData(Context context, String alertTitle, String msgAlert, String extrajson) {
        Map<String, String> msgMap = StringManager.getFirstMap(extrajson);
        if (!msgMap.isEmpty()) {
            String type = msgMap.get("type");
            MsgSettingDataController controller = new MsgSettingDataController();
            if (!controller.checkOpenByType(type, null))
                return;

            //创建NotificationData
            NotificationData data = new NotificationData();
            data.setContent(msgAlert);
            data.setTicktext(msgAlert);
            data.setTitle(alertTitle);
            data.setIconResId(R.drawable.ic_launcher);

            UUID uuid = UUID.randomUUID();
            int notificationId = (int) uuid.getLeastSignificantBits();
            data.setNotificationId(notificationId);
            //处理推送的消息类型
            if (msgMap.get("t") != null) {
                data.setType(Integer.valueOf(msgMap.get("t")));
            }
            //处理url
            if (msgMap.get("d") != null) {
                data.setUrl(msgMap.get("d"));
                String[] strArr = data.url.split(".app");
                int lastIndex = -1;
                if (strArr.length > 0)
                    lastIndex = strArr[0].lastIndexOf("/");
                if (lastIndex > -1) {
                    data.value = strArr[0].substring(lastIndex);
                } else {
                    data.value = strArr[0];
                }
            }

            if (msgMap.get("image") != null) {
                data.setImgUrl(msgMap.get("image"));
            }
            //统计
            if (data.type == XHClick.NOTIFY_SELF || data.type == XHClick.NOTIFY_A) {
                XHClick.statisticsPush(context, XHClick.STATE_RECEIVE, android.os.Build.VERSION.SDK_INT);
            }
            XHClick.statisticsNotify(context, data, NotificationEvent.EVENT_COME);
            //接收消息总开关,已开启
            String newMSG = (String) FileManager.loadShared(context, FileManager.msgInform, FileManager.newMSG);
            //当t == XHClick.NOTIFY_SELF 时是自我唤醒,必须走.
            if (data.type == XHClick.NOTIFY_SELF || TextUtils.equals(newMSG,"") || "1".equals(newMSG)) {
                // A：一般用于推送活动、头条  C：一般是美食贴消息
                switch (data.type) {
                    // 显示通知，不存在消息列表中
                    case XHClick.NOTIFY_A:
                        //判断应用是否开着
                        if (context != null && ToolsDevice.isAppInPhone(context, context.getPackageName()) < 2) {
                            new NotificationManager().notificationActivity(context, data);
                        } else if ((data.url.contains("Feedback.app") || data.url.contains("dialog.app"))
                                && ToolsDevice.isAppInPhone(context, context.getPackageName()) == 3) { //判断是否是开启反馈的url
                            if (context != null) {
                                Activity activity = XHActivityManager.getInstance().getCurrentActivity();
                                //获取当前activity类名判断是否为com.xiangha.Feekback
                                if (activity != null && activity instanceof Feedback) {
                                    ((Feedback) activity).notifySendMsg(Feedback.MSG_FROM_NOTIFY);
                                } else {
                                    MessageTipController.newInstance().getCommonData(null);
                                    new NotificationManager().notificationActivity(context, data);
                                }
                            }
                        } else {
                            new NotificationManager().notificationActivity(context, data);
                        }
                        break;
                    // 自我唤醒
                    case XHClick.NOTIFY_SELF:
                        if (data.url.contains("nous")) {
                            new XGLocalPushServer(context).saveLocalPushRecord(context, FileManager.xmlKey_localZhishi);
                        }
                        data.setStartAvtiviyWhenClick(Main.class);
                        new NotificationManager().notificationActivity(context, data);
                        break;
                }
            }
        }
    }
}
