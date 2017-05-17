package third.push.umeng;

import android.content.Context;
import android.content.Intent;

import com.umeng.message.UmengMessageService;
import com.umeng.message.common.UmLog;
import com.umeng.message.entity.UMessage;

import org.android.agoo.common.AgooConstants;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;

import acore.tools.StringManager;
import third.push.PushPraserService;

/**
 * Developer defined push intent service.
 * Remember to call {@link com.umeng.message.PushAgent#setPushIntentServiceClass(Class)}.
 *
 * @author lucas
 */
//完全自定义处理类
//参考文档的1.6.5
//http://dev.umeng.com/push/android/integration#1_6_5
public class UMPushService extends UmengMessageService {
	private static final String TAG = UMPushService.class.getName();

	@Override
	public void onMessage(Context context, Intent intent) {
		// 需要调用父类的函数，否则无法统计到消息送达
		try {
			//可以通过MESSAGE_BODY取得消息体
			String message = intent.getStringExtra(AgooConstants.MESSAGE_BODY);
			UMessage msg = new UMessage(new JSONObject(message));
			/*message：消息体
			* msg.custom：自定义消息的内容
			* msg.title：通知标题
			* msg.text：通知内容
			* */
			ArrayList<Map<String, String>> data = StringManager.getListMapByJson(msg.custom);
			String title = "";
			String text = "";
			if (data.size() > 0) {
				title = data.get(0).get("title");
				text = data.get(0).get("text");
			}
			// code  to handle message here
			//开启PushPraserService
			Intent serviceIntent = new Intent();
			serviceIntent.setClass(context,PushPraserService.class);
			serviceIntent.putExtra("title",title);
			serviceIntent.putExtra("text",text);
			serviceIntent.putExtra("custom",msg.custom);
			serviceIntent.putExtra("message",message);
			serviceIntent.putExtra("channel",PushPraserService.TYPE_UMENG);
			context.startService(serviceIntent);
		} catch (Exception e) {
			UmLog.e(TAG, e.getMessage());
		}
	}
}
