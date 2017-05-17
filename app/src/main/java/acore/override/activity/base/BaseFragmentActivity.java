package acore.override.activity.base;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.util.Util;
import com.xiangha.R;

import acore.logic.ActivityMethodManager;
import acore.logic.load.LoadManager;
import acore.override.XHApplication;
import acore.tools.LogManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import amodule.main.Main;
import amodule.main.view.CommonBottomView;
import amodule.main.view.CommonBottonControl;
import third.share.BarShare;

public class BaseFragmentActivity extends FragmentActivity {
	public RelativeLayout rl;
	protected int level = 2;
	public BarShare barShare;
	public LoadManager loadManager = null;
	public boolean keyBoard_visible = false;
	protected ActivityMethodManager mActMagager;
	public CommonBottomView mCommonBottomView;
	public String className;
	public CommonBottonControl control;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mActMagager = new ActivityMethodManager(this);
	}

	/**
	 * Activity标题初始化
	 * 
	 * @param title ：标题
	 * @param level ：等级
	 * @param color :背景色
	 * @param barTitleXml：标题xml
	 * @param contentXml：主内容xml
	 */
	public void initActivity(String title, int level, int color, int barTitleXml, int contentXml) {
		this.level = level;
		className=this.getComponentName().getClassName();
		control= new CommonBottonControl();
		if (barTitleXml > 0) {
			this.requestWindowFeature(Window.FEATURE_NO_TITLE); // 声明使用自定义标题
			View view_all = LayoutInflater.from(this).inflate(R.layout.a_all,null);
			RelativeLayout all_title= (RelativeLayout) view_all.findViewById(R.id.all_title);
			RelativeLayout all_content= (RelativeLayout) view_all.findViewById(R.id.all_content);
			all_content.addView(control.setCommonBottonView(className,this,contentXml));
			View view_title = LayoutInflater.from(this).inflate(barTitleXml,null);
			if(Tools.isShowTitle()) {
				int dp_45 = Tools.getDimen(this, R.dimen.dp_45);
				int height = dp_45 + Tools.getStatusBarHeight(this);
				RelativeLayout.LayoutParams layout = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, height);
				all_title.setLayoutParams(layout);
				all_title.setPadding(0, Tools.getStatusBarHeight(this), 0, 0);
			}

			all_title.addView(view_title);
			setContentView(view_all);
			mCommonBottomView=control.mCommonBottomView;
			getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, barTitleXml);
			TextView titleV = (TextView) findViewById(R.id.title);
			
			titleV.setMaxWidth(ToolsDevice.getWindowPx(this).widthPixels - ToolsDevice.dp2px(this, 45 + 40));
			titleV.setText(title);
			if (color > 0)
				findViewById(R.id.barTitle).setBackgroundResource(color);
			// 设置返回按钮;
			View btn_back = findViewById(R.id.ll_back);
			ImageView img_back = (ImageView) findViewById(R.id.leftImgBtn);
			TextView tv_back = (TextView) findViewById(R.id.leftText);
			btn_back.setClickable(true);
			tv_back.setClickable(true);
			img_back.setClickable(true);
			OnClickListener backClick = getBackBtnAction();
			btn_back.setOnClickListener(backClick);
			tv_back.setOnClickListener(backClick);
			img_back.setOnClickListener(backClick);
			btn_back.setVisibility(View.VISIBLE);
			tv_back.setVisibility(View.INVISIBLE);
			img_back.setVisibility(View.VISIBLE);
		} else {
			this.requestWindowFeature(Window.FEATURE_NO_TITLE);
			setContentView(control.setCommonBottonView(className,this,contentXml));
			mCommonBottomView=control.mCommonBottomView;
		}
		if(Tools.isShowTitle()) {
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
		}
		setCommonStyle();
	}

	protected OnClickListener getBackBtnAction() {
		return new OnClickListener() {
			@Override
			public void onClick(View v) {
				BaseFragmentActivity.this.onBackPressed();
			}
		};
	}
	
	/**
	 * 设置公用属性
	 */
	public void setCommonStyle() {
		// 设置样式
		rl = (RelativeLayout) findViewById(R.id.activityLayout);
		if (rl != null) {
			loadManager = new LoadManager(this, rl);
			// 设置压缩模式下键盘的监听
			rl.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
				private int preHeight = 0;

				@Override
				public void onGlobalLayout() {
					int heightDiff = rl.getRootView().getHeight() - rl.getHeight();
					if (preHeight == heightDiff) {
						return;
					}
					preHeight = heightDiff;
					if (heightDiff > (ToolsDevice.getWindowPx(BaseFragmentActivity.this).heightPixels / 4)) {
						if (!keyBoard_visible)
							keyBoard_visible = true;
					} else {
						if (keyBoard_visible)
							keyBoard_visible = false;
					}
				}
			});
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return mActMagager.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		mActMagager.onMenuItemSelected(featureId, item);
		return super.onMenuItemSelected(featureId, item);
	}

	@Override
	public void onBackPressed() {
		// 程序如果未初始化但却有定时器执行，则停止它。主要用于外部吊起应用时
		if (Main.allMain == null && Main.timer != null) {
			Main.stopTimer();
		}
		super.onBackPressed();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (XHApplication.in() == null) {
			Intent i = getBaseContext().getPackageManager()
					.getLaunchIntentForPackage(getBaseContext().getPackageName());
			i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(i);
			return;
		}
		mActMagager.onResume(level);
		if(mCommonBottomView!=null)
			CommonBottomView.BottomViewBuilder.getInstance().refresh(mCommonBottomView);
	}

	@Override
	protected void onPause() {
		super.onPause();
		mActMagager.onPause();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
        if(Util.isOnMainThread()) {
            Glide.get(XHApplication.in()).clearMemory();
            LogManager.print("d", "***********Glide is already clearMemory...");
        }
	}

	@Override
	public void startActivity(Intent intent) {
		super.startActivity(intent);
		// 设置切换动画，从右边进入，左边退出
		overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
	}

	@Override
	public void startActivityForResult(Intent intent, int requestCode) {
		super.startActivityForResult(intent, requestCode);
		// 设置切换动画，从右边进入，左边退出
		overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
	}

	@Override
	public void finish() {
		super.finish();
		overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
	}
}
