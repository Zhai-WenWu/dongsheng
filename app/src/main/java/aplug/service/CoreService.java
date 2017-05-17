package aplug.service;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import java.util.Calendar;

import acore.logic.XHClick;
import acore.tools.FileManager;
import acore.tools.Tools;
import aplug.service.base.NormalService;
import aplug.service.base.ServiceManager;
import aplug.service.listener.ScreenObserver;
import third.push.xg.XGPushServer;

/**
 * PackageName : aplug.service
 * Created by MrTrying on 2016/7/8 14:39.
 * E_mail : ztanzeyu@gmail.com
 */
public class CoreService extends NormalService {

	private static final int[] TIMES = {1,2,3,4,5,6,7};
	/** 屏幕监听 */
	private ScreenObserver screenObserver = null;
	private boolean flag = false;

	@Override
	public void onCreate() {
		super.onCreate();
		appFirstStart(getApplicationContext());
		XHClick.onEventValue(getApplicationContext(), "start_core", "core", "start", 1);
		screenObserver = new ScreenObserver(getApplicationContext());
		screenObserver.startObserver(new ScreenObserver.ScreenStateListener() {
			@Override
			public void onScreenOn() {
				flag = false;
			}

			@Override
			public void onScreenOff() {
				flag = true;
			}

			@Override
			public void onUserPresent() {
				flag = false;
			}
		});
		startBehindActivity();
	}

	private void startBehindActivity() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					try {
						Thread.sleep(ServiceManager.CORE_POLLING);
						if (flag) {
							/**XG注册*/
							registerXG();
							/**打开activity*/
							openActivity();
						}
					} catch (Exception e) {
						e.printStackTrace();
					}

				}
			}
		}).start();
	}

	private void registerXG() {
		try{
			XHClick.onEventValue(getApplicationContext(), "register_xg", "xg", "register", 1);
			String userCode = FileManager.loadShared(getApplicationContext(), FileManager.xmlFile_userInfo, "userCode").toString();
			new XGPushServer(getApplicationContext()).initPush(userCode);
		}catch (Exception e){
			//针对oppo手机处理
		}
	}

	private void openActivity() {
		//获取当前时间
		long currentTimeMillis = System.currentTimeMillis();
		java.util.Date curDate = new java.util.Date(currentTimeMillis);
		String date = Tools.timeFormat(currentTimeMillis, "yyyyMMdd");
		for (int index = 0; index < TIMES.length; index++) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(curDate);
			//对当前时间±60min
			cal.add(Calendar.MINUTE, 0 == Tools.getRandom(0, 2) ? -Tools.getRandom(0, 60) : Tools.getRandom(0, 60));
			int hour = cal.getTime().getHours();
			//判断是否达到开启式时间点
			if (hour >= TIMES[index]) {
				start(String.valueOf(TIMES[index]), date);
				break;
			}
		}
	}

	/**
	 *
	 * @param key
	 * @param value
	 */
	private void start(String key, String value) {
		//获取时间点的最近启动日期
		String time = FileManager.loadShared(getApplicationContext(), FileManager.xmlFile_wake, key).toString();
		//判断今天是否开启过 && 是否符合随机时间
		if (!value.equals(time) && isOpenActivityByRandom(getApplicationContext())) {
			//记录
			FileManager.saveShared(getApplicationContext(), FileManager.xmlFile_wake, key, value);
			Intent intent = new Intent(this,ServiceOpenActivity.class);
			intent.addCategory(Intent.CATEGORY_HOME);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			CoreService.this.startActivity(intent);
		}
	}

	/**
	 * 随机时间处理
	 * @param context
	 *
	 * @return
	 */
	private boolean isOpenActivityByRandom(Context context) {
		String timeStr = FileManager.loadShared(context, FileManager.xmlFile_appInfo, FileManager.xmlKey_firstStart).toString();
		double random = Math.random();
		if (TextUtils.isEmpty(timeStr)) {
			timeStr = String.valueOf(System.currentTimeMillis());
		}
		long millis = Long.parseLong(timeStr);
		int dayCount = getGapCount(millis);
		switch (dayCount) {
			//dayCount + 1天以内
			case 0:
				return false;
			case 1:
				return random <= 1.7567;
			case 2:
				return random <= 1.1883;
			case 3:
				return random <= 0.9739;
			case 4:
				return random <= 0.8473;
			case 5:
				return random <= 0.7647;
			case 6:
				return random <= 0.7130;
			default:
				return otherStatus(dayCount, random);
		}
	}

	/**
	 * 处理其他情况
	 *
	 * @param dayCount
	 * @param random
	 *
	 * @return
	 */
	private boolean otherStatus(int dayCount, double random) {
		if(dayCount < 1){
			return false;
		}else if (dayCount <= 14) {
			//7天~14天
			return random <= 0.6717 - (dayCount - 7) * 0.025;
		} else {
			//14天以后
			double standard = 0.4960 - (dayCount - 14) * 0.0084;
			return random <= (standard > 0 ? standard : 0.001);
		}
	}

	/**
	 * 计算第一次启动时间与当前时间差
	 *
	 * @param startMillis
	 *
	 * @return
	 */
	private static int getGapCount(long startMillis) {
		java.util.Date startDate = new java.util.Date(startMillis);
		Calendar fromCalendar = Calendar.getInstance();
		fromCalendar.setTime(startDate);
		fromCalendar.set(Calendar.HOUR_OF_DAY, 0);
		fromCalendar.set(Calendar.MINUTE, 0);
		fromCalendar.set(Calendar.SECOND, 0);
		fromCalendar.set(Calendar.MILLISECOND, 0);

		java.util.Date endDate = new java.util.Date(System.currentTimeMillis());
		Calendar toCalendar = Calendar.getInstance();
		toCalendar.setTime(endDate);
		toCalendar.set(Calendar.HOUR_OF_DAY, 0);
		toCalendar.set(Calendar.MINUTE, 0);
		toCalendar.set(Calendar.SECOND, 0);
		toCalendar.set(Calendar.MILLISECOND, 0);

		return (int) ((toCalendar.getTime().getTime() - fromCalendar.getTime().getTime()) / (1000 * 60 * 60 * 24));
	}

	/**
	 * 存储第一次启动时间
	 *
	 * @param context
	 */
	private void appFirstStart(Context context) {
		String timeStr = FileManager.loadShared(context, FileManager.xmlFile_appInfo, FileManager.xmlKey_firstStart).toString();
		if (TextUtils.isEmpty(timeStr)) {
			timeStr = String.valueOf(System.currentTimeMillis());
			FileManager.saveShared(context, FileManager.xmlFile_appInfo, FileManager.xmlKey_firstStart, timeStr);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		screenObserver.shutdownObserver();
	}
}