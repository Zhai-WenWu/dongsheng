/**
 * @author Jerry
 * 2013-1-17 上午11:43:51
 * Copyright: Copyright (c) xiangha.com 2011
 */

package acore.override.activity.mian;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;

import com.xianghatest.R;

import acore.logic.ActivityMethodManager;
import acore.logic.load.LoadManager;
import acore.override.XHApplication;
import acore.override.activity.base.BaseActivity;
import amodule.main.Main;
import third.ad.AdsShow;
import third.mall.aplug.MallCommon;

public class MainBaseActivity extends AppCompatActivity {
	protected int level = 1;
	public RelativeLayout rl;
	public LoadManager loadManager;
	private ActivityMethodManager mActMagager;
	public AdsShow[] mAds;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mActMagager = new ActivityMethodManager(this);
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
		return mActMagager.onCreateOptionsMenu(menu);
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		mActMagager.onMenuItemSelected(0, item);
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onBackPressed() {
		if(BaseActivity.mUpDishPopWindowDialog != null && BaseActivity.mUpDishPopWindowDialog.isHasShow()) {
			BaseActivity.mUpDishPopWindowDialog.closePopWindowDialog();
			BaseActivity.mUpDishPopWindowDialog = null;
		}else{
			// 程序如果未初始化但却有定时器执行，则停止它。主要用于外部吊起应用时
			if (Main.allMain == null && Main.timer != null) {
				Main.stopTimer();
			}
			if (Main.allMain != null ){
				if(Main.timer != null) {
					Main.stopTimer();
				}
				Main.allMain.doExit(this, true);
			}
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if(XHApplication.in()==null){
			Intent i = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());    
			i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);    
			startActivity(i); 
			return;
		}
		if(mAds != null){
			for(AdsShow ad : mAds){
				ad.onResumeAd();
			}
		}
		mActMagager.onResume(level);
		if(BaseActivity.mUpDishPopWindowDialog != null && BaseActivity.mUpDishPopWindowDialog.isHasShow()) {
			BaseActivity.mUpDishPopWindowDialog.onResume();
		}
	}
	
	
	@Override
	protected void onPause() {
		super.onPause();
		if(mAds != null){
			for(AdsShow ad : mAds){
				ad.onPauseAd();
			}
		}
		mActMagager.onPause();
		//用完即回收
		if(MallCommon.interfaceMall!=null)
			MallCommon.interfaceMall=null;
		if(BaseActivity.mUpDishPopWindowDialog != null && BaseActivity.mUpDishPopWindowDialog.isHasShow()) {
			BaseActivity.mUpDishPopWindowDialog.onPause();
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
	}

	@Override
	public void startActivity(Intent intent) {
		super.startActivity(intent);
		// 设置切换动画，从右边进入，左边退出
		overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mActMagager.onDestroy();
	}
}
