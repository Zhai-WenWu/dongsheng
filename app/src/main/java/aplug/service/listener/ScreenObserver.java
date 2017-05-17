package aplug.service.listener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.PowerManager;

/**
 * PackageName : aplug.service.listener
 * Created by MrTrying on 2016/7/7 15:50.
 * E_mail : ztanzeyu@gmail.com
 */
public class ScreenObserver {
	private final Context mContext;
	private final ScreenBroadcastReceiver mScreenReceiver;
	private ScreenStateListener mScreenStateListener;

	public ScreenObserver(Context context) {
		mContext = context;
		mScreenReceiver = new ScreenBroadcastReceiver();
	}

	/**
	 * 开启屏幕监听
	 * 开启之后必须调用shutdownObserver()销毁监听
	 * */
	public void startObserver(ScreenStateListener listener) {
		mScreenStateListener = listener;
		registerListener();
		getScreenState();
	}

	/**关闭屏幕监听*/
	public void shutdownObserver() {
		unregisterListener();
	}

	/**
	 * 获取screen状态
	 */
	private void getScreenState() {
		if (mContext == null) {
			return;
		}

		PowerManager manager = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
		if (manager.isScreenOn()) {
			if (mScreenStateListener != null) {
				mScreenStateListener.onScreenOn();
			}
		} else {
			if (mScreenStateListener != null) {
				mScreenStateListener.onScreenOff();
			}
		}
	}

	private void registerListener() {
		if (mContext != null) {
			IntentFilter filter = new IntentFilter();
			filter.addAction(Intent.ACTION_SCREEN_ON);
			filter.addAction(Intent.ACTION_SCREEN_OFF);
			filter.addAction(Intent.ACTION_USER_PRESENT);
			mContext.registerReceiver(mScreenReceiver, filter);
		}
	}

	private void unregisterListener() {
		if (mContext != null)
			mContext.unregisterReceiver(mScreenReceiver);
	}

	private class ScreenBroadcastReceiver extends BroadcastReceiver {
		private String action = null;

		@Override
		public void onReceive(Context context, Intent intent) {
			action = intent.getAction();
			if (Intent.ACTION_SCREEN_ON.equals(action)) { // 开屏
				mScreenStateListener.onScreenOn();
			} else if (Intent.ACTION_SCREEN_OFF.equals(action)) { // 锁屏
				mScreenStateListener.onScreenOff();
			} else if (Intent.ACTION_USER_PRESENT.equals(action)) { // 解锁
				mScreenStateListener.onUserPresent();
			}
		}
	}

	public interface ScreenStateListener {// 返回给调用者屏幕状态信息
		void onScreenOn();
		void onScreenOff();
		void onUserPresent();
	}
}
