package amodule.user.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TabHost;
import android.widget.TextView;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import acore.logic.LoginManager;
import acore.logic.XHClick;
import acore.override.activity.base.BaseActivity;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.widget.LayoutScroll;
import acore.widget.TextViewLimitLine;
import amodule.article.activity.ArticleDetailActivity;
import amodule.article.activity.ArticleUploadListActivity;
import amodule.dish.db.UploadDishData;
import amodule.main.Main;
import amodule.main.view.CommonBottomView;
import amodule.main.view.CommonBottonControl;
import amodule.user.Broadcast.UploadStateChangeBroadcasterReceiver;
import amodule.user.view.TabContentView;
import amodule.user.view.UserHomeAnswer;
import amodule.user.view.UserHomeDish;
import amodule.user.view.UserHomeItem;
import amodule.user.view.UserHomeSubject;
import amodule.user.view.UserHomeTitle;
import amodule.user.view.UserHomeTxt;
import amodule.user.view.UserHomeVideo;
import aplug.basic.InternetCallback;
import aplug.basic.ReqEncyptInternet;
import aplug.basic.ReqInternet;
import xh.basic.internet.UtilInternet;
import xh.basic.tool.UtilString;

@SuppressLint("CutPasteId")
public class FriendHome extends BaseActivity {

	private TabHost tabHost;
	private LinearLayout activityLayout_show, tabMainMyself;

	private ArrayList<View> mTabViews = new ArrayList<View>();
	private ArrayList<View> mTabViewsFloat = new ArrayList<View>();
	private ArrayList<TabContentView> mTabContentViews = new ArrayList<TabContentView>();
	private boolean[] mIsLoadeds = null;
	private String userCode = "";
	private CommonBottomView mCommonBottomView;

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
		if(!TextUtils.isEmpty(userCode) && userCode.equals(LoginManager.userInfo.get("code"))){
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

	private boolean mFirstDataReady;
	private boolean mFirstLoaded;
	private boolean mSecondDataReady;
	private boolean mSecondLoaded;
	private boolean mShowErrorMsg = true;
	private ArrayList<Map<String, String>> mTabs;
	private void getData() {
		mTabs = new ArrayList<Map<String, String>>();
		Map<String, String> subjectMap = new HashMap<String, String>();
		subjectMap.put("title", "晒美食");
		subjectMap.put("num", "");
		subjectMap.put("type", "-1");
		mTabs.add(subjectMap);
		Map<String, String> dishMap = new HashMap<String, String>();
		dishMap.put("title", "菜谱");
		dishMap.put("num","");
		dishMap.put("type", "0");
		mTabs.add(dishMap);
		String getUrl = StringManager.api_getUserInfoByCode + "?code=" + userCode;
		ReqInternet.in().doGet(getUrl, new InternetCallback(this) {
			@Override
			public void loaded(int flag, String url, Object returnObj) {
				Map<String, String> userinfo_map = null;
				if (flag >= UtilInternet.REQ_OK_STRING){
					ArrayList<Map<String, String>> list = UtilString.getListMapByJson(returnObj);
					userinfo_map = UtilString.getListMapByJson(list.get(0).get("userinfo")).get(0);
					mTabs.get(0).put("num", userinfo_map.get("subjectCount"));
					mTabs.get(1).put("num", userinfo_map.get("dishCount"));
					mUserHomeTitle.setUserData(list.get(0).get("userinfo"));
					mFirstDataReady = true;
					onDataReady();
				}else {
					onDataFailure(this, flag, returnObj);
				}
				mFirstLoaded = true;
				onLoaded(flag);
			}
		});
		String getSecondUrl = StringManager.API_USERMAIN_LEVEL + "?code=" + userCode;
		ReqEncyptInternet.in().doEncypt(getSecondUrl, "", new InternetCallback(this) {
			@Override
			public void loaded(int i, String s, Object o) {
				if (i >= UtilInternet.REQ_OK_STRING) {
					ArrayList<Map<String, String>> list = StringManager.getListMapByJson(o);
                    mTabs.addAll(list);
					mSecondDataReady = true;
					onDataReady();
				} else {
					onDataFailure(this, i, o);
				}
				mSecondLoaded = true;
				onLoaded(i);
			}
		});
	}

	private void onLoaded(int flag) {
		if (mFirstLoaded && mSecondLoaded)
			loadManager.loadOver(flag, 1, mTabs == null || mTabs.size() == 0);
	}

	private void onDataFailure(InternetCallback callback, int flag, Object returnObj) {
		if (callback == null || !mShowErrorMsg)
			return;
		mShowErrorMsg = false;
		callback.toastFaildRes(flag, true, returnObj);
		callback.finish();
	}

	private void onDataReady() {
		if (!mFirstDataReady || !mSecondDataReady) {
			return;
		}
		handleTabsData();
		setTabHost();
	}

	private void handleTabsData() {
		if (mTabs == null)
			return;
		Iterator<Map<String, String>> iterator = mTabs.iterator();
		while (iterator.hasNext()) {
			Map<String, String> map = iterator.next();
			if (map != null && map.size() > 0 && map.containsKey("num")) {
				String num = map.get("num");
				if (TextUtils.isEmpty(num) || (Integer.parseInt(num) <= 0) && (!TextUtils.isEmpty(LoginManager.userInfo.get("code")) && !LoginManager.userInfo.get("code").equals(userCode))) {
					iterator.remove();
				}
			}
		}
	}

	//加载完数据后调用显示界面
	public void show() {
		if (activityLayout_show != null)
			activityLayout_show.setVisibility(View.VISIBLE);
	}

	private void setTabHost() {
		if (mTabs == null || mTabs.size() <= 0)
			return;
		//获取控件高度
		tabHost = (TabHost) findViewById(R.id.tabhost);
		if (Main.allMain == null || Main.allMain.getLocalActivityManager() == null) {
			Tools.showToast(getApplicationContext(), "加载失败，请稍后重试");
			finish();
			return;
		}
		tabHost.setup(Main.allMain.getLocalActivityManager());
		tabMainMyself = (LinearLayout) findViewById(R.id.a_user_home_title_tab);
		LinearLayout tabMainMyselfFloat = (LinearLayout) findViewById(R.id.tab_float_mainMyself);
		if (mIsLoadeds == null)
			mIsLoadeds = new boolean[mTabs.size()];
		for (int i = 0; i < mTabs.size(); i++) {
			Map<String, String> tabMap = mTabs.get(i);
			if (tabMap != null && tabMap.size() > 0 && tabMap.containsKey("type")) {
				String title = tabMap.get("title");
				String num = tabMap.get("num");
				View tabView = getTabWidget(title, num, getTabClicker(i));
				View tabViewFloat = getTabWidget(title, num, getTabClicker(i));
				TabContentView tabContentView = null;
				String type = tabMap.get("type");
				switch (type) {
					case "1"://视频
						tabContentView = new UserHomeVideo(this, userCode);
						((UserHomeVideo)tabContentView).setOnItemClickListener(new UserHomeItem.OnItemClickListener() {
							@Override
							public void onItemClick(Map<String, String> dataMap) {
								onItemClickListener(dataMap);
							}
						});
						break;
					case "2"://文章
						tabContentView = new UserHomeTxt(this, userCode);
						((UserHomeTxt)tabContentView).setOnItemClickListener(new UserHomeItem.OnItemClickListener() {
							@Override
							public void onItemClick(Map<String, String> dataMap) {
								onItemClickListener(dataMap);
							}
						});
						break;
					case "3"://问答
						tabContentView = new UserHomeAnswer(this, userCode);
						((UserHomeAnswer)tabContentView).setOnItemClickListener(new UserHomeItem.OnItemClickListener() {
							@Override
							public void onItemClick(Map<String, String> dataMap) {
								onItemClickListener(dataMap);
							}
						});
						break;
					case "-1"://晒美食
						tabContentView = new UserHomeSubject(this, userCode);
						break;
					case "0"://菜谱
						tabContentView = new UserHomeDish(this, userCode);
						break;

				}
				if (tabContentView != null) {
					tabContentView.setDataMap(tabMap);
				}
				mIsLoadeds[i] = false;
				mTabViews.add(tabView);
				mTabViewsFloat.add(tabViewFloat);
				mTabContentViews.add(tabContentView);
				tabHost.addTab(tabHost.newTabSpec(i + "").setIndicator(title).setContent(tabContentView));
				tabMainMyself.addView(tabView);
				tabMainMyselfFloat.addView(tabViewFloat);
			}
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

	private void onItemClickListener(Map<String, String> dataMap) {
		if (dataMap == null || dataMap.size() < 1)
			return;
		String dataFrom = dataMap.get("dataFrom");
		if ("1".equals(dataFrom)) {//dataFrom:数据来源，本地:1；网络:2,或者null、""、不存在该字段；
			String uploadType = dataMap.get("uploadType");
			String hasUploadPage = dataMap.get("hasUploadPage");
			Map<String, String> tabMap = mTabContentViews.get(tabIndex).getDataMap();
			if (tabMap != null && tabMap.size() > 0) {
				String type = tabMap.get("type");
				switch (type) {
					case "2"://文章列表
						if ("2".equals(hasUploadPage)) {
							Intent intent = new Intent(FriendHome.this, ArticleUploadListActivity.class);
							FriendHome.this.startActivity(intent);
							return;
						} else if (UploadDishData.UPLOAD_FAIL.equals(uploadType)) {
							//TODO 通知重新上传
							return;
						}
						break;
					case "1"://视频列表
						if (UploadDishData.UPLOAD_FAIL.equals(uploadType)) {
							//TODO 通知重新上传
							return;
						}
						break;
				}
			}
		} else {
			String code = dataMap.get("code");
			if (!TextUtils.isEmpty(code)) {
				Intent intent = new Intent(this, ArticleDetailActivity.class);
				Bundle bundle = new Bundle();
				bundle.putString("code", code);
				intent.putExtras(bundle);
				startActivity(intent);
			}
		}
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
		this.tabIndex = tabIndex;
		//统计
		switch (tabIndex) {
			case 0:
				XHClick.mapStat(this, tongjiId, "导航", "美食贴");
				break;
			case 1:
				XHClick.mapStat(this, tongjiId, "导航", "菜谱");
				break;
		}
		String tag = mTabContentViews.get(tabHost.getCurrentTab()).onPause();
		tabHost.setCurrentTab(tabIndex);
		int tabNum = tabHost.getTabWidget().getChildCount();
		for (int i = 0; i < tabNum; i++) {
			tabSelectStyle(mTabViews.get(i), i == tabHost.getCurrentTab());
			tabSelectStyle(mTabViewsFloat.get(i), i == tabHost.getCurrentTab());
		}
		if (!mIsLoadeds[tabIndex]) {
			mTabContentViews.get(tabIndex).initLoad();
			mIsLoadeds[tabIndex] = true;
		}
		mTabContentViews.get(tabIndex).onResume(tag);
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
		for (int i = 0; i < mIsLoadeds.length; i++)
			mIsLoadeds[i] = false;
		tabChanged(tabHost.getCurrentTab());
	}

	@Override
	protected void onResume() {
		super.onResume();
		CommonBottomView.BottomViewBuilder.getInstance().refresh(mCommonBottomView);
		if(isRefresh && mTabContentViews != null && mTabContentViews.size() > tabIndex && mTabContentViews.get(tabIndex) != null)
			mTabContentViews.get(tabIndex).onResume("resume");
	}

	@Override
	protected void onPause() {
		super.onPause();
		isRefresh = false;
		//view失焦点
		for(int i=0;i<mTabContentViews.size();i++){
			if(mTabContentViews.get(i) instanceof UserHomeSubject){
				((UserHomeSubject)mTabContentViews.get(i)).onViewPause();
			}
		}
	}

	@Override
	public void finish() {
		for (int i = 0; i < mTabContentViews.size(); i++){
			if(mTabContentViews.get(i) != null)mTabContentViews.get(i).finish();
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
						if(mTabContentViews != null && mTabContentViews.size()> tabIndex && mTabContentViews.get(tabIndex) != null)
							mTabContentViews.get(tabIndex).onResume("resume");
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
