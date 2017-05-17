package amodule.user.activity;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TabHost;
import android.widget.TextView;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.Map;

import acore.logic.LoginManager;
import acore.logic.XHClick;
import acore.override.activity.base.BaseActivity;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.widget.LayoutScroll;
import acore.widget.TextViewLimitLine;
import amodule.main.Main;
import amodule.main.view.CommonBottomView;
import amodule.main.view.CommonBottonControl;
import amodule.user.Broadcast.UploadStateChangeBroadcasterReceiver;
import amodule.user.view.TabContentView;
import amodule.user.view.UserHomeDish;
import amodule.user.view.UserHomeSubject;
import amodule.user.view.UserHomeTitle;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;
import xh.basic.internet.UtilInternet;
import xh.basic.tool.UtilString;

@SuppressLint("CutPasteId")
public class FriendHome extends BaseActivity {

	private TabHost tabHost;
	private LinearLayout activityLayout_show, tabMainMyself;

	private View[] tabViews, tabViewsFloat;
	private TabContentView[] tabContent = {null, null};

	private boolean loadTabs[] = {false, false};
	private String userCode = "";
	private CommonBottomView mCommonBottomView;
	private String subjectNum,dishNum;

	private UserHomeTitle mUserHomeTitle;
	public LayoutScroll scrollLayout;
	public LinearLayout backLayout;
	public TextViewLimitLine friend_info;

	private int tabIndex = 0;
	private String tongjiId = "a_user";

	public static boolean isAlive = false,isRefresh = false;

	//接收菜谱视频上传状态
	UploadStateChangeBroadcasterReceiver receiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		//sufureView页面闪烁
		getWindow().setFormat(PixelFormat.TRANSLUCENT);
		Bundle bundle = this.getIntent().getExtras();
		if (bundle != null) {
			userCode = bundle.getString("code");
			tabIndex = bundle.getInt("index");
			//消息是否读过
			if (bundle.getString("newsId") != null) {
				String params = "type=news&p1=" + bundle.getString("newsId");
				ReqInternet.in().doPost(StringManager.api_setUserData, params, new InternetCallback(this) {
					@Override
					public void loaded(int flag, String url, Object returnObj) {
					}
				});
			}
		}
		if(userCode.equals(LoginManager.userInfo.get("code"))){
			tongjiId = "a_my";
		}
		String className = this.getComponentName().getClassName();
		CommonBottonControl control = new CommonBottonControl();
		setContentView(control.setCommonBottonView(className, this, R.layout.a_my_friend_home));
		XHClick.track(this,"浏览个人主页");
		mCommonBottomView = control.mCommonBottomView;
		level = 4;
		setCommonStyle();

		scrollLayout = (LayoutScroll) findViewById(R.id.scroll_body);;
		// 滑动设置
		backLayout = (LinearLayout) findViewById(R.id.a_user_home_title);
		friend_info = (TextViewLimitLine) findViewById(R.id.a_user_home_title_info);
		activityLayout_show = (LinearLayout) findViewById(R.id.a_user_home_title);
		activityLayout_show.setVisibility(View.INVISIBLE);

		//设置当向上滑动，浮动tab出现时，假状态栏的高度
		if(Tools.isShowTitle()) {
			View view = findViewById(R.id.a_user_home_float_title_view);
			LinearLayout.LayoutParams params = (LayoutParams) view.getLayoutParams();
			params.height = Tools.getStatusBarHeight(this);
		}else{
			findViewById(R.id.a_user_home_float_title_view).setVisibility(View.GONE);
		}

		mUserHomeTitle = new UserHomeTitle(this,findViewById(R.id.a_user_home_title),userCode);

		findViewById(R.id.a_user_home_title_back).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		loadManager.setLoading(new OnClickListener() {
			@Override
			public void onClick(View v) {
				getData();
			}
		});
		friend_info.addOnClick(new TextViewLimitLine.OnClickListener() {
			@Override
			public void onClick(View v,boolean isNeedRefrash) {
				if(isNeedRefrash) doReload();
			}
		});

		isAlive = true;

		registerBrocaster();
	}

	private void getData() {
		String getUrl = StringManager.api_getUserInfoByCode + "?code=" + userCode;
		ReqInternet.in().doGet(getUrl, new InternetCallback(this) {
			@Override
			public void loaded(int flag, String url, Object returnObj) {
				Map<String, String> userinfo_map = null;
				if (flag >= UtilInternet.REQ_OK_STRING){
					ArrayList<Map<String, String>> list = UtilString.getListMapByJson(returnObj);
					userinfo_map = UtilString.getListMapByJson(list.get(0).get("userinfo")).get(0);
					subjectNum = userinfo_map.get("subjectCount");
					dishNum = userinfo_map.get("dishCount");
					mUserHomeTitle.setUserData(list.get(0).get("userinfo"));
					setTabHost();
				}else {
					toastFaildRes(flag, true, returnObj);
					finish();
				}
				loadManager.loadOver(flag, 1, userinfo_map == null || userinfo_map.size() == 0);
			}
		});
	}

	//加载完数据后调用显示界面
	public void show() {
		if (activityLayout_show != null)
			activityLayout_show.setVisibility(View.VISIBLE);
	}

	private void setTabHost() {
		//获取控件高度
		tabHost = (TabHost) findViewById(R.id.tabhost);
		if (Main.allMain == null || Main.allMain.getLocalActivityManager() == null) {
			Tools.showToast(getApplicationContext(), "加载失败，请稍后重试");
			finish();
			return;
		}
		tabHost.setup(Main.allMain.getLocalActivityManager());
		String[] tabTitle = {"美食帖", "菜谱"};
		String[] tabNum = {subjectNum, dishNum};
		tabMainMyself = (LinearLayout) findViewById(R.id.a_user_home_title_tab);
		LinearLayout tabMainMyselfFloat = (LinearLayout) findViewById(R.id.tab_float_mainMyself);
		tabContent[0] = new UserHomeSubject(this,userCode);
		tabContent[1] = new UserHomeDish(this, userCode);
		tabViews = new View[tabContent.length];
		tabViewsFloat = new View[tabContent.length];
		for (int i = 0; i < tabContent.length; i++) {
			tabHost.addTab(tabHost.newTabSpec(i + "").setIndicator(tabTitle[i]).setContent(tabContent[i]));
			tabViews[i] = getTabWidget(tabTitle[i], tabNum[i],getTabClicker(i));
			tabViewsFloat[i] = getTabWidget(tabTitle[i], tabNum[i],getTabClicker(i));
			tabMainMyself.addView(tabViews[i]);
			tabMainMyselfFloat.addView(tabViewsFloat[i]);
		}
		tabChanged(tabIndex);
	}

	private OnClickListener getTabClicker(final int i) {
		return new OnClickListener() {
			@Override
			public void onClick(View v) {
				tabChanged(i);
			}
		};
	}


	// 获取tab标签卡
	private View getTabWidget(String title, String num, OnClickListener clicker) {
		View view = View.inflate(this, R.layout.tab_item_img_text, null);
		TextView tv = (TextView) view.findViewById(R.id.tab_title);
		tv.setText(title);
		TextView data = (TextView) view.findViewById(R.id.tab_data);
		data.setText(num);
		view.setOnClickListener(clicker);
		LayoutParams lp = new LayoutParams(0, android.view.ViewGroup.LayoutParams.MATCH_PARENT);
		lp.weight = 1;
		view.setLayoutParams(lp);
		return view;
	}

	// 切换tab
	private void tabChanged(int tabIndex) {
		//统计
		switch (tabIndex) {
			case 0:
				XHClick.mapStat(this, tongjiId, "导航", "美食贴");
				break;
			case 1:
				XHClick.mapStat(this, tongjiId, "导航", "菜谱");
				break;
		}
		String tag = tabContent[tabHost.getCurrentTab()].onPause();
		tabHost.setCurrentTab(tabIndex);
		int tabNum = tabHost.getTabWidget().getChildCount();
		for (int i = 0; i < tabNum; i++) {
			tabSelectStyle(tabViews[i], i == tabHost.getCurrentTab());
			tabSelectStyle(tabViewsFloat[i], i == tabHost.getCurrentTab());
		}
		if (!loadTabs[tabIndex]) {
			tabContent[tabIndex].initLoad();
			loadTabs[tabIndex] = true;
		}
		tabContent[tabIndex].onResume(tag);
	}

	// 设置tab选中的样式
	private void tabSelectStyle(View tabView, boolean isSelect) {
		TextView tv = (TextView) tabView.findViewById(R.id.tab_title);
		String color = Tools.getColorStr(this,R.color.comment_color);
		if (isSelect)
			tv.setTextColor(Color.parseColor(color));
		else
			tv.setTextColor(Color.parseColor("#333333"));
	}

	// 重载
	public void doReload() {
		for (int i = 0; i < loadTabs.length; i++)
			loadTabs[i] = false;
		tabChanged(tabHost.getCurrentTab());
	}

	@Override
	protected void onResume() {
		super.onResume();
		CommonBottomView.BottomViewBuilder.getInstance().refresh(mCommonBottomView);
		if(isRefresh && tabContent != null && tabContent.length > tabIndex && tabContent[tabIndex] != null)
			tabContent[tabIndex].onResume("resume");
	}

	@Override
	protected void onPause() {
		super.onPause();
		isRefresh = false;
		//view失焦点
		for(int i=0;i<tabContent.length;i++){
			if(tabContent[i] instanceof UserHomeSubject){
				((UserHomeSubject)tabContent[i]).onViewPause();
			}
		}
	}

	@Override
	public void finish() {
		for (int i = 0; i < tabContent.length; i++){
			if(tabContent[i] != null)tabContent[i].finish();
		}
		super.finish();
		isAlive = false;
	}

	private void registerBrocaster() {
		 receiver = new UploadStateChangeBroadcasterReceiver(
				new UploadStateChangeBroadcasterReceiver.ReceiveBack() {
					@Override
					public void onGetReceive(String state) {
						CommonBottomView.BottomViewBuilder.getInstance().refresh(mCommonBottomView);
						if(tabContent != null && tabContent.length > tabIndex && tabContent[tabIndex] != null)
							tabContent[tabIndex].onResume("resume");
					}
				}
		);
		receiver.register(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(receiver!=null){
			unregisterReceiver(receiver);
		}
	}
}
