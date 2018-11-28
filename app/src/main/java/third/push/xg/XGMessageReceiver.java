package third.push.xg;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.tencent.android.tpush.XGPushBaseReceiver;
import com.tencent.android.tpush.XGPushClickedResult;
import com.tencent.android.tpush.XGPushRegisterResult;
import com.tencent.android.tpush.XGPushShowedResult;
import com.tencent.android.tpush.XGPushTextMessage;

import org.json.JSONException;
import org.json.JSONObject;


import acore.tools.LogManager;
import third.push.PushPraserService;

/**
 * 继承信鸽的类，用于接收推送消息
 */
@SuppressLint({ "NewApi", "SimpleDateFormat" })
public class XGMessageReceiver extends XGPushBaseReceiver {
	private boolean isShow = false ;//发布是改成false

	private void show(Context context, String text) {
		if(isShow)
			Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
	}

	/**
	 * 有新推送，会调用次方法
	 * @param context
	 * @param message
     */
	@Override
	public void onTextMessage(Context context, XGPushTextMessage message) {
		//通过解析之后的content拼接title
		String extrajson=message.getCustomContent();
		String content =message.getContent();
		//开启PushPraserService
		Intent serviceIntent = new Intent();
		serviceIntent.putExtra("title",message.getTitle());
		serviceIntent.putExtra("text",content);
		serviceIntent.putExtra("custom",extrajson);
		serviceIntent.putExtra("channel",PushPraserService.TYPE_XG);
		new PushPraserService().handleData(context,serviceIntent);
	}

	@SuppressLint("SimpleDateFormat")
	@Override
	public void onNotifactionShowedResult(Context context, XGPushShowedResult notifiShowedRlt) {
		if (context == null || notifiShowedRlt == null) {
			return;
		}
	}

	@Override
	public void onNotifactionClickedResult(Context context, XGPushClickedResult message) {
		if (context == null || message == null) {
			return;
		}
		String text = null;
		if (message.getActionType() == XGPushClickedResult.NOTIFACTION_CLICKED_TYPE) {
			// 通知在通知栏被点击啦。。。。。
			// APP自己处理点击的相关动作
			// 这个动作可以在activity的onResume也能监听，请看第3点相关内容
			text = "通知被打开 :" + message;
		} else if (message.getActionType() == XGPushClickedResult.NOTIFACTION_DELETED_TYPE) {
			// 通知被清除啦。。。。
			// APP自己处理通知被清除后的相关动作
			text = "通知被清除 :" + message;
		}
		// 获取自定义key-value
		String customContent = message.getCustomContent();
		if (customContent != null && customContent.length() != 0) {
			try {
				JSONObject obj = new JSONObject(customContent);
				// key1为前台配置的key
				if (!obj.isNull("key")) {
					String value = obj.getString("key");
					LogManager.print("d", "get custom value:" + value);
				}
				// ...
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		// APP自主处理的过程。。。
		
		// 通知点击完成，删除数据库记录
		show(context, text);
	}

	@Override
	public void onRegisterResult(Context context, int errorCode, XGPushRegisterResult message) {
		if (context == null || message == null) 
			return;
		String text = null;
		if (errorCode == XGPushBaseReceiver.SUCCESS) {
			text = message + "注册成功";
			// 通过message.getToken()获取token
			// 在这里拿token
//			String token = message.getToken();
		} else {
			text = message + "注册失败，错误码：" + errorCode;
		}
		LogManager.print("d", text);
		show(context, text);
	}

	@Override public void onSetTagResult(Context context, int arg1, String arg2) {}
	@Override public void onDeleteTagResult(Context context, int arg1, String arg2) {}
	@Override public void onUnregisterResult(Context context, int arg1) {}
}
