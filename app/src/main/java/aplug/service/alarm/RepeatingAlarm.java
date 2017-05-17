package aplug.service.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import acore.logic.XHClick;
import aplug.service.base.ServiceManager;

/**
 * PackageName : aplug.service.alarm
 * Created by MrTrying on 2016/7/8 14:50.
 * E_mail : ztanzeyu@gmail.com
 */
public class RepeatingAlarm extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		XHClick.onEventValue(context.getApplicationContext(),"start_protect","protect","start",1);
		ServiceManager.startProtectService(context);
	}
}
