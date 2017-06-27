package amodule.main.activity;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;

import com.bumptech.glide.BitmapRequestBuilder;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.xiangha.R;

import java.util.ArrayList;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.SpecialWebControl;
import acore.logic.XHClick;
import acore.override.activity.mian.MainBaseActivity;
import acore.tools.FileManager;
import acore.tools.StringManager;
import acore.widget.XHADView;
import amodule.main.Main;
import amodule.main.view.ChangeSendDialog;
import amodule.main.view.home.HomeContentControl;
import amodule.main.view.home.HomeDish;
import amodule.main.view.home.HomeHeaderAndListControl;
import aplug.basic.InternetCallback;
import aplug.basic.LoadImage;
import aplug.basic.SubBitmapTarget;
import third.ad.AdParent;
import third.ad.AdsShow;
import third.ad.tools.AdConfigTools;
import third.ad.tools.AdPlayIdConfig;
import xh.basic.internet.UtilInternet;

/**
 * 新首页
 * @author FangRuijiao
 */
public class MainHomePageNew extends MainBaseActivity {
	public static final String STATISTICS_ID = "a_index430";
	public static final String STATISTICS_SWITCH_ID = "a_index_switch400";
	private HomeContentControl mContentControl;
	private HomeHeaderAndListControl mHeaderAndListControl;
	private HomeDish mHomeDish;
	
	private boolean adOnce = false;
	
	public AdsShow[] mAds;

	private int onResumeNum = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.a_home_main);
		//在Main中保存首页的对象
		Main.allMain.allTab.put("MainIndex", this);
		//如果有外部吊起的url则开启
		Bundle bundle = this.getIntent().getExtras();
		if(bundle != null){
			AppCommon.openUrl(this, bundle.getString("url"), true);
		}
		//初始化Header和list部分的control
		mHeaderAndListControl = new HomeHeaderAndListControl(this);
		//初始化中间内容部分的control
		mContentControl = new HomeContentControl(this);
		//初始化今日佳作列表
		mHomeDish = new HomeDish(this,loadManager);
		initData();
	}

	/*
	 * 返回首页的时候,推荐菜谱回到默认的页面
	 */
	@Override
	protected void onResume() {
		super.onResume();
		Main.mainActivity = this;
		if(Main.allMain != null && Main.allMain.getBuoy() != null){
			Main.allMain.getBuoy().setFloatMenuData();
		}
		//为了解决首页打开webview后再调用此句再打开的webView的大小就不是0*0啦
		if(onResumeNum == 1){
			SpecialWebControl.initSpecialWeb(this,"index","","");
		}
		if(mAds != null){
			for(AdsShow ad : mAds){
				ad.onResumeAd();
			}
		}
		onResumeNum ++;
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		if(mAds != null){
			for(AdsShow ad : mAds){
				ad.onPauseAd();
			}
		}
	}
	
	private void initData(){
		//加载首页数据
		onLoadData(false);
	}

	public void onActivityshow(){
//		SpecialWebControl.initSpecialWeb(this,"index","","");
		showDialog();
	}
	/**
	 * 加载数据
	 */
	public void onLoadData(final boolean isRefresh){
		mHeaderAndListControl.getListView().setSelection(0);
		AppCommon.getIndexData(this, new InternetCallback(this) {
			@Override
			public void loaded(int flag, String url, Object returnObj) {
				if (flag >= UtilInternet.REQ_OK_STRING) {
					@SuppressWarnings("unchecked")
					Map<String, String> map = returnObj != null ? (Map<String, String>) returnObj: null;
					if (((url.equals("file") && !isRefresh) || url.equals("newData"))) {
						mContentControl.setData(map);
						//设置三餐数据
						mHeaderAndListControl.setData(map);
//						//保存弹框推广位数据
//						if(map.containsKey("proBitBox"))
//							AppCommon.saveWelcomeInfo(map.get("proBitBox"));
					}
				}
			}
		});
		//今日佳作列表加载数据,
		mHomeDish.loadData(mHeaderAndListControl.getListView(), mHeaderAndListControl.getScrollLinearListLayout());

	}

	/**
	 * 展示提醒dialog
	 */
	private void showDialog(){
		String msg=(String) FileManager.loadShared(this, FileManager.CIRCLE_HOME_SHOWDIALOG, FileManager.CIRCLE_HOME_SHOWDIALOG);
		if(TextUtils.isEmpty(msg)||!msg.equals("1")){
			final Dialog dialog= new Dialog(this, R.style.dialog);
			Window window= dialog.getWindow();
			window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
			dialog.setContentView(R.layout.dialog_maincircle_backgroup);
			window.findViewById(R.id.maincircle_img_hint_close).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					dialog.cancel();
				}
			});
			window.findViewById(R.id.maincircle_img_hint_send).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					dialog.cancel();
					ChangeSendDialog dialog= new ChangeSendDialog(MainHomePageNew.this);
					dialog.show();
				}
			});
			dialog.show();
			FileManager.saveShared(this, FileManager.CIRCLE_HOME_SHOWDIALOG, FileManager.CIRCLE_HOME_SHOWDIALOG, "1");
		}
	}

}