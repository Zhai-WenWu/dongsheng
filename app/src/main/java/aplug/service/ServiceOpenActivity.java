package aplug.service;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;

import com.umeng.analytics.MobclickAgent;

import acore.logic.XHClick;
import acore.tools.Tools;
import aplug.service.base.ServiceManager;
import aplug.service.listener.ScreenObserver;

/**
 * PackageName : aplug.service
 * Created by MrTrying on 2016/7/8 15:18.
 * E_mail : ztanzeyu@gmail.com
 */
public class ServiceOpenActivity extends Activity {
	private ScreenObserver screenObserver = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//统计
		XHClick.onEventValue(getApplicationContext(),"start_activity","activity","start",1);
		closeActivity(Tools.getRandom(ServiceManager.ACTIVITY_LIFE_MIN,ServiceManager.ACTIVITY_LIFE_MAX));
		screenObserver = new  ScreenObserver(this);
		screenObserver.startObserver(new ScreenObserver.ScreenStateListener() {
			@Override
			public void onScreenOn() {
				ServiceOpenActivity.this.finish();
			}

			@Override public void onScreenOff() {}

			@Override
			public void onUserPresent() {
				ServiceOpenActivity.this.finish();
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		MobclickAgent.onPause(this);
		screenObserver.shutdownObserver();
	}

	private void closeActivity(int minute){
		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				ServiceOpenActivity.this.finish();
			}
		}, minute * 60 * 1000);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		ServiceOpenActivity.this.finish();
		return super.onTouchEvent(event);
	}
}
