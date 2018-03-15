/**
 * @author Jerry
 * 2013-1-17 上午11:43:51
 * Copyright: Copyright (c) xiangha.com 2011
 */

package acore.override.activity.mian;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.widget.RelativeLayout;

import com.xiangha.R;

import acore.logic.ActivityMethodManager;
import acore.logic.XHClick;
import acore.logic.load.LoadManager;
import acore.override.XHApplication;
import acore.override.activity.base.BaseActivity;
import acore.tools.PageStatisticsUtils;
import amodule.main.Main;
import third.mall.aplug.MallCommon;

import static android.content.Intent.FLAG_ACTIVITY_NO_USER_ACTION;

public class MainBaseActivity extends AppCompatActivity {
	protected int level = 1;
	public RelativeLayout rl;
	public LoadManager loadManager;
	private ActivityMethodManager mActMagager;
	private long resumeTime;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mActMagager = new ActivityMethodManager(this);

		Intent intent = getIntent();
		int i = intent.getIntExtra(XHClick.KEY_NOTIFY_CLICK, 0);
		if (i == XHClick.VALUE_NOTIFY_CLICK) {
			XHClick.statisticsNotifyClick(intent);
		}
	}
	
	@Override
	public void setContentView(int layoutResID) {
		super.setContentView(layoutResID);
		rl = (RelativeLayout) findViewById(R.id.activityLayout);
		if (rl != null) {
			loadManager = new LoadManager(this,rl);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return false;
	}

	@Override
	public void onBackPressed() {
		if(BaseActivity.mUpDishPopWindowDialog != null && BaseActivity.mUpDishPopWindowDialog.isHasShow()) {
			BaseActivity.mUpDishPopWindowDialog.closePopWindowDialog();
			BaseActivity.mUpDishPopWindowDialog = null;
		}else{
			// 程序如果未初始化但却有定时器执行，则停止它。主要用于外部吊起应用时
			if (Main.allMain != null ){
				Main.allMain.doExit(this, true);
			}
		}
	}
	
	@Override
	protected void onResume() {
		Main.mainActivity = this;
		super.onResume();
		resumeTime = System.currentTimeMillis();
		if(XHApplication.in()==null){
			Intent i = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());    
			i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);    
			startActivity(i); 
			return;
		}
		if (mActMagager != null){
			mActMagager.onResume(level);
		}
		if(BaseActivity.mUpDishPopWindowDialog != null && BaseActivity.mUpDishPopWindowDialog.isHasShow()) {
			BaseActivity.mUpDishPopWindowDialog.onResume();
		}
	}
	
	
	@Override
	protected void onPause() {
		super.onPause();
		PageStatisticsUtils.getInstance().onPausePage(this,resumeTime,System.currentTimeMillis());
		if (mActMagager != null){
			mActMagager.onPause();
		}
		//用完即回收
		if(MallCommon.interfaceMall!=null)
			MallCommon.interfaceMall=null;
		if(BaseActivity.mUpDishPopWindowDialog != null && BaseActivity.mUpDishPopWindowDialog.isHasShow()) {
			BaseActivity.mUpDishPopWindowDialog.onPause();
		}
	}

	@Override
	protected void onUserLeaveHint() {
		super.onUserLeaveHint();
		if (mActMagager != null){
			mActMagager.onUserLeaveHint();
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
	}

	@Override
	public void startActivity(Intent intent) {
		intent.addFlags(FLAG_ACTIVITY_NO_USER_ACTION);
		super.startActivity(intent);
		// 设置切换动画，从右边进入，左边退出
		overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mActMagager != null){
			mActMagager.onDestroy();
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (mActMagager != null){
			mActMagager.onStop();
		}
	}

	public ActivityMethodManager getActMagager() {
		return mActMagager;
	}
}
