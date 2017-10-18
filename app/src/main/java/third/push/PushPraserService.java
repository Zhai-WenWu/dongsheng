package third.push;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.XHClick;
import acore.tools.FileManager;
import acore.tools.StringManager;
import acore.tools.ToolsDevice;
import amodule.main.Main;
import aplug.feedback.activity.Feedback;
import third.push.model.NotificationData;
import third.push.xg.XGLocalPushServer;
import third.qiyu.QiYvHelper;

/**
 * PackageName : third.push.model
 * Created by MrTrying on 2016/8/16 11:08.
 * E_mail : ztanzeyu@gmail.com
 */
public class PushPraserService extends Service{
	public static final String PUSH_ID = "notify_come_count";
	public static final String TYPE_XG = "xg";
	public static final String TYPE_UMENG = "umeng";
	public static int msgCountSubject = 0;

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if(null != intent){
			//处理必要信息
			String title = intent.getStringExtra("title");
			String text = intent.getStringExtra("text");
			String custom = intent.getStringExtra("custom");
			String channel = intent.getStringExtra("channel");
			//判断是否是umeng推送
			String message = null;
			if(TYPE_UMENG.equals(channel)){
				message = intent.getStringExtra("message");
			}
			parsePushData(getBaseContext(),title,text,custom,channel,message);
		}
		return super.onStartCommand(intent, flags, startId);
	}

	/**
	 * 根据api文档,处理接受到的数据;
	 * @param context
	 * @param alertTitle
	 * @param msgAlert
	 * @param extrajson
     * @param channel : 推送类型：信鸽、umeng
     */
	public void parsePushData(Context context,String alertTitle, String msgAlert, String extrajson, String channel,String message) {
		ArrayList<Map<String, String>> msgt = StringManager.getListMapByJson(extrajson);
		if (msgt.size() > 0) {
			Map<String, String> msgMap = msgt.get(0);
			//用于解决：服务端会两个推送都推，根据pushCode存储本地情况，判断是否已经接受了推送
			if (FileManager.ifFileModifyByCompletePath(FileManager.getDataDir() + msgMap.get("pushCode"), -1) != null) {
//				XHClick.onEvent(context, PUSH_ID, channel, "2");
				return;
			} else {
//				XHClick.onEvent(context, PUSH_ID, channel, "1");
				FileManager.saveFileToCompletePath(FileManager.getDataDir() + msgMap.get("pushCode"), "", false);
			}
			//创建NotificationData
			NotificationData data = new NotificationData();
			data.setContent(msgAlert);
			data.setTicktext(msgAlert);
			data.setTitle(alertTitle);
			data.setIconResId(R.drawable.ic_launcher);
			data.setChannel(channel);
			if(TYPE_UMENG.equals(channel) && !TextUtils.isEmpty(message)){
				data.setUmengMessage(message);
			}
			//获取notifycationid
			String idStr = FileManager.loadShared(context, FileManager.xmlFile_appInfo, FileManager.xmlKey_notifycationId).toString();
			if (TextUtils.isEmpty(idStr)) {
				idStr = "1";
			}
			int id = Integer.parseInt(idStr);
			data.setNotificationId(id++);
			//保存本次推送id
			FileManager.saveShared(context, FileManager.xmlFile_appInfo, FileManager.xmlKey_notifycationId, String.valueOf(id));

			if (msgMap != null) {
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
				//统计
				if (data.type == XHClick.NOTIFY_SELF || data.type == XHClick.NOTIFY_A) {
					XHClick.statisticsPush(context, XHClick.STATE_RECEIVE, android.os.Build.VERSION.SDK_INT);
				}
				XHClick.statisticsNotify(context, data, "come");
				//接收消息总开关,已开启
				String newMSG = (String) FileManager.loadShared(context, FileManager.msgInform, FileManager.newMSG);
				//当t == XHClick.NOTIFY_SELF 时是自我唤醒,必须走.
				if (data.type == XHClick.NOTIFY_SELF || newMSG == "" || newMSG.equals("1")) {
					// A：一般用于推送活动、头条  C：一般是美食贴消息
					switch (data.type) {
						// 显示通知，不存在消息列表中
						case XHClick.NOTIFY_A:
							//判断应用是否开着
							if (context != null && ToolsDevice.isAppInPhone(context, context.getPackageName()) < 2) {
								new NotificationManager().notificationActivity(context, data);
							} else if (data.url.indexOf("dialog.app") > -1) { //判断是否是开启反馈的url
								if (context != null) {
									ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
									ActivityManager.RunningTaskInfo info = manager.getRunningTasks(1).get(0);
									//获取当前activity类名判断是否为com.xiangha.Feekback
									if (Feedback.handler != null && info.topActivity.getClassName().equals("com.xiangha.Feekback"))
										Feedback.notifySendMsg(Feedback.MSG_FROM_NOTIFY);
								} else {
									AppCommon.feekbackMessage++;
									QiYvHelper.getInstance().getUnreadCount(new QiYvHelper.NumberCallback() {
										@Override
										public void onNumberReady(int count) {
											Main.setNewMsgNum(3, AppCommon.quanMessage + AppCommon.feekbackMessage + AppCommon.myQAMessage + count);
										}
									});
									new NotificationManager().notificationActivity(context, data);
								}
							} else {
								new NotificationManager().notificationActivity(context, data);
							}
							break;
						// 显示通知，存在消息列表中
						case XHClick.NOTIFY_B:
							if (context != null && ToolsDevice.isAppInPhone(context, context.getPackageName()) < 2) {
								data.setStartAvtiviyWhenClick(Main.class);
								new NotificationManager().notificationActivity(context, data);
							} else {
								AppCommon.quanMessage++;
								QiYvHelper.getInstance().getUnreadCount(new QiYvHelper.NumberCallback() {
									@Override
									public void onNumberReady(int count) {
										Main.setNewMsgNum(3, AppCommon.quanMessage + AppCommon.feekbackMessage + AppCommon.myQAMessage + count);
									}
								});
								new NotificationManager().notificationActivity(context, data);
							}
							break;
						// 显示通知，存在消息列表中，使用app不通知
						case XHClick.NOTIFY_C:
							AppCommon.quanMessage++;
							QiYvHelper.getInstance().getUnreadCount(new QiYvHelper.NumberCallback() {
								@Override
								public void onNumberReady(int count) {
									Main.setNewMsgNum(3, AppCommon.quanMessage + AppCommon.feekbackMessage + AppCommon.myQAMessage + count);
								}
							});
							if (context != null && ToolsDevice.isAppInPhone(context, context.getPackageName()) < 2) {
								if (data.url.indexOf("subjectInfo.app?") > -1) {
									// 叠加消息数量
									msgCountSubject++;
									if (msgCountSubject > 1) {
										// 消息数量大于1,去消息中心;
										alertTitle = alertTitle + " 有" + msgCountSubject + "条新回复";
										data.setTitle(alertTitle);
									}
									// 弹出通知
									new NotificationManager().notificationClear(context, 0);
									data.setNotificationId(0);
									data.setStartAvtiviyWhenClick(Main.class);
									new NotificationManager().notificationActivity(context, data);
								} else {
									data.setStartAvtiviyWhenClick(Main.class);
									new NotificationManager().notificationActivity(context, data);
								}
							}
							break;
						// 显示通知，不存在消息列表中，未启动时通知
						case XHClick.NOTIFY_D:
							if (context != null && ToolsDevice.isAppInPhone(context, context.getPackageName()) < 2) {
								data.setStartAvtiviyWhenClick(Main.class);
								new NotificationManager().notificationActivity(context, data);
							}
							break;
						// 自我唤醒
						case XHClick.NOTIFY_SELF:
							if (data.url.indexOf("nous") > -1) {
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

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}
