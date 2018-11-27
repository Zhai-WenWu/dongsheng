package aplug.service.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import aplug.service.base.ServiceManager;

/**
 * PackageName : aplug.service.alarm
 * Created by MrTrying on 2016/7/8 14:50.
 * E_mail : ztanzeyu@gmail.com
 */
public class PushAlarm {
	public static void startTimingWake(Context context){
		Intent intent = new Intent(context, RepeatingAlarm.class);
		PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

		// Schedule the alarm!
		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
//		设置每天启动闹钟时间
//		Calendar calendar = Calendar.getInstance();
//		calendar.set(Calendar.HOUR_OF_DAY, 18);
//		calendar.set(Calendar.MINUTE, 49);
//		calendar.set(Calendar.SECOND, 10);
//		calendar.set(Calendar.MILLISECOND, 0);

		//每一小时重复一下闹钟
		am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), ServiceManager.ALARM_INTERVAL, sender);
	}

	public static void closeTimingWake(Context context){
		// Create the same intent, and thus a matching IntentSender, for
		// the one that was scheduled.
		Intent intent = new Intent(context,RepeatingAlarm.class);
		PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);

		// And cancel the alarm.
		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		am.cancel(sender);
	}
}
