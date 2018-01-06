//package third.push;
//
//import android.app.ActivityManager;
//import android.content.Context;
//import android.text.TextUtils;
//
//import com.xiangha.R;
//
//import java.util.ArrayList;
//import java.util.Map;
//
//import acore.logic.AppCommon;
//import acore.logic.XHClick;
//import acore.tools.FileManager;
//import acore.tools.StringManager;
//import acore.tools.ToolsDevice;
//import amodule.main.Main;
//import aplug.feedback.activity.Feedback;
//import third.push.model.NotificationData;
//import third.push.xg.XGLocalPushServer;
//import third.qiyu.QiYvHelper;
//
///**
// * PackageName : third.push.model
// * Created by MrTrying on 2016/8/16 11:08.
// * E_mail : ztanzeyu@gmail.com
// */
//public class PushPraser {
//	public static final String PUSH_ID = "notify_come_count";
//	public static final String TYPE_XG = "xg";
//	public static final String TYPE_UMENG = "umeng";
//	private Context mContext;
//	public static int msgCountSubject = 0;
//
//	public PushPraser(Context context) {
//		mContext = context;
//	}
//
//	/**
//	 * 根据api文档,处理接受到的数据;
//	 * @param alertTitle
//	 * @param msgAlert
//	 * @param extrajson
//     * @param type : 推送类型：信鸽、umeng
//     */
//	public void parsePushData(String alertTitle, String msgAlert, String extrajson, String type) {
//		ArrayList<Map<String, String>> msgt = StringManager.getListMapByJson(extrajson);
//		if (msgt.size() > 0) {
//			Map<String, String> msgMap = msgt.get(0);
//			//用于解决：服务端会两个推送都推，根据pushCode存储本地情况，判断是否已经接受了推送
//			if (FileManager.ifFileModifyByCompletePath(FileManager.getDataDir() + msgMap.get("pushCode"), -1) != null) {
////				XHClick.onEvent(mContext, PUSH_ID, type, "2");
//				return;
//			} else {
////				XHClick.onEvent(mContext, PUSH_ID, type, "1");
//				FileManager.saveFileToCompletePath(FileManager.getDataDir() + msgMap.get("pushCode"), "", false);
//			}
//			NotificationData data = new NotificationData();
//			data.setContent(msgAlert);
//			data.setTicktext(msgAlert);
//			data.setTitle(alertTitle);
//			data.setIconResId(R.drawable.ic_launcher);
//			//获取notifycationid
//			String idStr = FileManager.loadShared(mContext, FileManager.xmlFile_appInfo, FileManager.xmlKey_notifycationId).toString();
//			if (TextUtils.isEmpty(idStr)) {
//				idStr = "1";
//			}
//			int id = Integer.parseInt(idStr);
//			data.setNotificationId(id++);
//			//保存本次推送id
//			FileManager.saveShared(mContext, FileManager.xmlFile_appInfo, FileManager.xmlKey_notifycationId, String.valueOf(id));
//
//			if (msgMap != null) {
//				//处理推送的消息类型
//				if (msgMap.get("t") != null) {
//					data.setType(Integer.valueOf(msgMap.get("t")));
//				}
//				//处理url
//				if (msgMap.get("d") != null) {
//					data.setUrl(msgMap.get("d"));
//					String[] strArr = data.url.split(".app");
//					int lastIndex = -1;
//					if (strArr.length > 0)
//						lastIndex = strArr[0].lastIndexOf("/");
//					if (lastIndex > -1) {
//						data.value = strArr[0].substring(lastIndex);
//					} else {
//						data.value = strArr[0];
//					}
//				}
//				//统计
//				if (data.type == XHClick.NOTIFY_SELF || data.type == XHClick.NOTIFY_A) {
//					XHClick.statisticsPush(mContext, XHClick.STATE_RECEIVE, android.os.Build.VERSION.SDK_INT);
//				}
//				XHClick.statisticsNotify(mContext, data, "come");
//				//接收消息总开关,已开启
//				String newMSG = (String) FileManager.loadShared(mContext, "msgInform", "newMSG");
//				//当t == XHClick.NOTIFY_SELF 时是自我唤醒,必须走.
//				if (data.type == XHClick.NOTIFY_SELF || newMSG == "" || newMSG.equals("1")) {
//					// A：一般用于推送活动、头条  C：一般是美食贴消息
//					switch (data.type) {
//						// 显示通知，不存在消息列表中
//						case XHClick.NOTIFY_A:
//							//判断应用是否开着
//							if (mContext != null && ToolsDevice.isAppInPhone(mContext, mContext.getPackageName()) < 2) {
//								new NotificationManager().notificationActivity(mContext, data);
//							} else if (data.url.indexOf("dialog.app") > -1) { //判断是否是开启反馈的url
//								if (mContext != null) {
//									ActivityManager manager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
//									ActivityManager.RunningTaskInfo info = manager.getRunningTasks(1).get(0);
//									//获取当前activity类名判断是否为com.xiangha.Feekback
//									if (Feedback.handler != null && info.topActivity.getClassName().equals("com.xiangha.Feekback"))
//										Feedback.notifySendMsg(Feedback.MSG_FROM_NOTIFY);
//								} else {
//									MessageTipController.feekbackMessage++;
//									QiYvHelper.getInstance().getUnreadCount(new QiYvHelper.NumberCallback() {
//										@Override
//										public void onNumberReady(int count) {
//											if (count >= 0) {
//												MessageTipController.qiyvMessage = count;
//												if (count > 0)
//													Main.setNewMsgNum(2, MessageTipController.getMessageNum());
//											}
//										}
//									});
//									//防止七鱼回调不回来
//									Main.setNewMsgNum(2, MessageTipController.getMessageNum());
//									new NotificationManager().notificationActivity(mContext, data);
//								}
//							} else {
//								new NotificationManager().notificationActivity(mContext, data);
//							}
//							break;
//						// 显示通知，存在消息列表中
//						case XHClick.NOTIFY_B:
//							if (mContext != null && ToolsDevice.isAppInPhone(mContext, mContext.getPackageName()) < 2) {
//								data.setStartAvtiviyWhenClick(Main.class);
//								new NotificationManager().notificationActivity(mContext, data);
//							} else {
//								MessageTipController.quanMessage++;
//								QiYvHelper.getInstance().getUnreadCount(new QiYvHelper.NumberCallback() {
//									@Override
//									public void onNumberReady(int count) {
//										if (count >= 0) {
//											MessageTipController.qiyvMessage = count;
//											if (count > 0)
//												Main.setNewMsgNum(2, MessageTipController.getMessageNum());
//										}
//									}
//								});
//								//防止七鱼回调不回来
//								Main.setNewMsgNum(2, MessageTipController.getMessageNum());
//								new NotificationManager().notificationActivity(mContext, data);
//							}
//							break;
//						// 显示通知，存在消息列表中，使用app不通知
//						case XHClick.NOTIFY_C:
//							MessageTipController.quanMessage++;
//							QiYvHelper.getInstance().getUnreadCount(new QiYvHelper.NumberCallback() {
//								@Override
//								public void onNumberReady(int count) {
//									if (count >= 0) {
//										MessageTipController.qiyvMessage = count;
//										if (count > 0)
//											Main.setNewMsgNum(2, MessageTipController.getMessageNum());
//									}
//								}
//							});
//							//防止七鱼回调不回来
//							Main.setNewMsgNum(2, MessageTipController.getMessageNum());
//							if (mContext != null && ToolsDevice.isAppInPhone(mContext, mContext.getPackageName()) < 2) {
//								if (data.url.indexOf("subjectInfo.app?") > -1) {
//									// 叠加消息数量
//									msgCountSubject++;
//									if (msgCountSubject > 1) {
//										// 消息数量大于1,去消息中心;
//										alertTitle = alertTitle + " 有" + msgCountSubject + "条新回复";
//										data.setTitle(alertTitle);
//									}
//									// 弹出通知
//									new NotificationManager().notificationClear(mContext, 0);
//									data.setNotificationId(0);
//									data.setStartAvtiviyWhenClick(Main.class);
//									new NotificationManager().notificationActivity(mContext, data);
//								} else {
//									data.setStartAvtiviyWhenClick(Main.class);
//									new NotificationManager().notificationActivity(mContext, data);
//								}
//							}
//							break;
//						// 显示通知，不存在消息列表中，未启动时通知
//						case XHClick.NOTIFY_D:
//							if (mContext != null && ToolsDevice.isAppInPhone(mContext, mContext.getPackageName()) < 2) {
//								data.setStartAvtiviyWhenClick(Main.class);
//								new NotificationManager().notificationActivity(mContext, data);
//							}
//							break;
//						// 自我唤醒
//						case XHClick.NOTIFY_SELF:
//							if (data.url.indexOf("nous") > -1) {
//								new XGLocalPushServer(mContext).saveLocalPushRecord(mContext, FileManager.xmlKey_localZhishi);
//							}
//							data.setStartAvtiviyWhenClick(Main.class);
//							new NotificationManager().notificationActivity(mContext, data);
//							break;
//					}
//				}
//			}
//		}
//	}
//}
