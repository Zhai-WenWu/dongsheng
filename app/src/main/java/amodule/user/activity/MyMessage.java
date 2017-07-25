package amodule.user.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.LoginManager;
import acore.logic.XHClick;
import acore.override.activity.mian.MainBaseActivity;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import acore.widget.DownRefreshList;
import amodule.main.Main;
import amodule.user.activity.login.LoginByAccout;
import amodule.user.adapter.AdapterMainMsg;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;
import aplug.feedback.activity.Feedback;
import xh.basic.internet.UtilInternet;
import xh.basic.tool.UtilString;

public class MyMessage extends MainBaseActivity {
	private DownRefreshList listMessage;
	private TextView feekback_msg_num,msg_title_sort;
	
	private static Handler handler = null;
	private AdapterMainMsg adapter;
	private ArrayList<Map<String, String>> listDataMessage;
	
	public final static int MSG_ONREFRESH_MESSAGE = 9;// 刷新完成
	public final static int MSG_LOGIN_OUT_CLEAR_VIEW = 11;
	public final static int MSG_LOGIN_ON = 12;
	public final static int MSG_FEEKBACK_ONREFURESH = 13;
	private String pageTime="";
	private int currentPage = 0, everyPage = 0;
	private boolean clickFlag = true , isCreated=false;
	private boolean isShowData=true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.a_common_message);
		Main.allMain.allTab.put("MyMessage", this);
		init();
//		initTitle();
		XHClick.track(this, "浏览消息列表页");
	}

	private void initTitle() {
		if(Tools.isShowTitle()) {
			int dp_45 = Tools.getDimen(this, R.dimen.dp_45);
			int height = dp_45 + Tools.getStatusBarHeight(this);

			RelativeLayout bar_title = (RelativeLayout) findViewById(R.id.msg_title_bar_rela);
			RelativeLayout.LayoutParams layout = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, height);
			bar_title.setLayoutParams(layout);
			bar_title.setPadding(0, Tools.getStatusBarHeight(this), 0, 0);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if(LoginManager.isLogin()&&isShowData){
			onRefresh();
		}
	}
	/* 
	 * 设置消息当从消息返回时
	 */
	@Override
	protected void onPause() {
		super.onPause();
	}
	/**
	 * 外面调用的刷新
	 */
	public void onRefresh() {
		// 标示用户登录啦
		if(LoginManager.isLogin()){
			findViewById(R.id.no_login_rela).setVisibility(View.GONE);
			findViewById(R.id.tv_login_notify).setVisibility(View.GONE);
			findViewById(R.id.tv_noData).setVisibility(View.GONE);
			if (ToolsDevice.getNetActiveState(this)) {
				load(true);
				setRefresh();
			}
			AppCommon.quanMessage = 0;
			Main.setNewMsgNum(3,AppCommon.quanMessage);
		}else{
			findViewById(R.id.no_login_rela).setVisibility(View.VISIBLE);
			findViewById(R.id.tv_login_notify).setVisibility(View.VISIBLE);
			findViewById(R.id.tv_noData).setVisibility(View.GONE);
		}
	}

	public void login(View view) {
		Intent intent = new Intent(MyMessage.this, LoginByAccout.class);
		startActivity(intent);
	}

	private void init() {
		// title初始化
		TextView title = (TextView) findViewById(R.id.msg_title_tv);
		// title.setPadding(Tools.dp2px(this, 30), 0, 0, 0);
		title.setText("消息");
		msg_title_sort = (TextView) findViewById(R.id.msg_title_sort);
		msg_title_sort.setText("未读");
		msg_title_sort.setVisibility(View.VISIBLE);
		title.setOnClickListener(titleClick);
		msg_title_sort.setOnClickListener(titleClick);
		ImageView img_back = (ImageView) findViewById(R.id.leftImgBtn);
		TextView tv_back = (TextView) findViewById(R.id.leftText);
		tv_back.setClickable(true);
		img_back.setClickable(true);
//		tv_back.setOnClickListener(backClick);
//		img_back.setOnClickListener(backClick);
		tv_back.setVisibility(View.GONE);
		img_back.setVisibility(View.GONE);
		findViewById(R.id.no_admin_linear).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startFeekback();
			}
		});

		// 结果显示
		listMessage = (DownRefreshList) findViewById(R.id.lv_message);
		listMessage.setVisibility(View.GONE);
		listMessage.bigDownText = "下拉刷新";
		listMessage.bigReleaseText = "松开刷新";
		RelativeLayout headerView = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.a_common_message_header, null);
		feekback_msg_num = (TextView) headerView.findViewById(R.id.feekback_msg_num);
		listMessage.addHeaderView(headerView, null, false);
		headerView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startFeekback();
			}
		});
		setFeekbackMsg();
		listDataMessage = new ArrayList<Map<String, String>>();
		adapter = new AdapterMainMsg(this, listMessage, listDataMessage, 0, null, null);
//		adapter.imgResource = R.drawable.bg_round_zannum;
		loadManager.setLoading(listMessage, adapter, true, new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if(isCreated)
					load(false);
				else{
					isCreated=true;
					load(true);
				}
			}
		}, new OnClickListener() {
			@Override
			public void onClick(View v) {
				load(true);
			}
		});
		handler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				int what = msg.what;
				switch (what) {
				case MSG_LOGIN_OUT_CLEAR_VIEW:// 退出登录时,清理消息界面[切换账号等];
				case MSG_LOGIN_ON:// 登录时,刷新消息界面[切换账号等];
					currentPage = 0;
					everyPage = 0;
					listDataMessage.clear();
					load(false);
					AppCommon.quanMessage = 0;
					break;
				case MSG_FEEKBACK_ONREFURESH:
					setFeekbackMsg();
					break;
				}
			}
		};
	}

	/**
	 * 刷新view
	 */
	public void setRefresh(){

		listMessage.onRefreshStart();
	}
	private void startFeekback(){
		Intent intent = new Intent(MyMessage.this, Feedback.class);
		startActivity(intent);
	}
	private void setFeekbackMsg() {
		if (feekback_msg_num != null){
			if (AppCommon.feekbackMessage == 0) {
				feekback_msg_num.setVisibility(View.GONE);
			} else {
				feekback_msg_num.setVisibility(View.VISIBLE);
				feekback_msg_num.setText("" + AppCommon.feekbackMessage);
			}
		}
	}

	/**
	 * 加载数据
	 */
	private void load(final boolean isForward) {
		String getUrl=null;
		if (isForward) {
			currentPage = 1;
			everyPage = 0;
			AppCommon.quanMessage=0;
//			AppCommon.setNewMsgNum(AppCommon.quanMessage+AppCommon.feekbackMessage);
			getUrl = StringManager.api_message + "?type="+(clickFlag?"all":"asc")+"&page="+currentPage;
		} else {
			currentPage++;
			getUrl=StringManager.api_message + "?type="+(clickFlag?"all":"asc")+"&page="+currentPage+"&pageTime=" + pageTime;
		}
		loadManager.changeMoreBtn(UtilInternet.REQ_OK_STRING, -1, -1, currentPage,listDataMessage.size() == 0);
		ReqInternet.in().doGet(getUrl, new InternetCallback(this) {
			@Override
			public void loaded(int flag, String url, Object returnObj) {
				int loadCount = 0;
				if (flag >= UtilInternet.REQ_OK_STRING) {
					if (isForward) listDataMessage.clear();
					ArrayList<Map<String, String>> listReturn = UtilString.getListMapByJson(returnObj);
					for (int i = 0; i < listReturn.size(); i++) {
						Map<String, String> map = listReturn.get(i);
						map.put("addTimeShow", map.get("addTimeShow"));
						pageTime=map.get("pageTime");
						if (map.get("msgType").equals("1")) {
							map.put("adminName", "");
							map.put("admin", "");
							// 点赞图标显示时,不显示评论内容;
							if (map.get("type") !=null && map.get("type").equals("3")) {
								map.put("content", "");
								map.put("isLike", "ico" + R.drawable.z_quan_home_body_ico_good_active);
							} else
								map.put("isLike", "");
							// 主题图片显示时,不显示主题标题
							if (map.get("img")!=null && map.get("img").length() > 5) {
								map.put("title", "hide");
							} else
								map.put("img", "hide");
							String info_url = map.get("url");
							Map<String, String> info_map = UtilString.getMapByString(info_url.substring(info_url.indexOf("subjectInfo.php?") + 16),
									"&", "=");
							map.put("subjectCode", info_map.get("code"));
							map.put("floorNum", info_map.get("floorNum"));
							// 添加回复人回复信息;
							if (listReturn.get(i).containsKey("customer")) {
								ArrayList<Map<String, String>> customer = UtilString.getListMapByJson(listReturn.get(i).get("customer"));
								map.put("nickName", customer.get(0).get("nickName"));
								map.put("nickImg", customer.get(0).get("img"));
								map.put("nickCode", customer.get(0).get("code"));
								map.put("isGourmet", customer.get(0).get("isGourmet"));
							}
						} else if (map.get("msgType").equals("2")) {
							map.put("nickName", "");
							map.put("nickImg", "id" + R.drawable.z_me_ico_mypage);
							map.put("isLike", "");
							map.put("admin", "官方");
							map.put("adminName", "管理员");
						} else if (map.get("msgType").equals("3")) {
							map.put("admin", "");
							map.put("adminName", "");
							map.put("isLike", "");
							if (listReturn.get(i).containsKey("customer")) {
								ArrayList<Map<String, String>> customer = UtilString.getListMapByJson(listReturn.get(i).get("customer"));
								map.put("nickName", customer.get(0).get("nickName"));
								map.put("nickImg", customer.get(0).get("img"));
								map.put("nickCode", customer.get(0).get("code"));
								map.put("isGourmet", customer.get(0).get("isGourmet"));
							}
						}
						if (!map.get("content").equals(""))
							map.put("content", map.get("content") + " ");
						if (map.get("state").equals("1"))
							map.put("bgColor", "#FFFDE3");
						listDataMessage.add(map);
					}
					loadCount = listReturn.size();
					if (isForward&&loadCount == 0 && LoginManager.isLogin()) {
						isShowData=true;
//						 findViewById(R.id.tv_login_notify).setVisibility(View.VISIBLE);
						findViewById(R.id.tv_noData).setVisibility(View.VISIBLE);
					}else{
						isShowData=false;
						findViewById(R.id.tv_noData).setVisibility(View.GONE);}
					// 标示用户登录啦
					if(LoginManager.isLogin()){
						isShowData=false;
						findViewById(R.id.no_login_rela).setVisibility(View.GONE);
						findViewById(R.id.tv_login_notify).setVisibility(View.GONE);
						findViewById(R.id.tv_noData).setVisibility(View.GONE);
						if (isForward&&loadCount == 0 && LoginManager.isLogin()) {
							findViewById(R.id.tv_noData).setVisibility(View.VISIBLE);
						}else findViewById(R.id.tv_noData).setVisibility(View.GONE);
					}else{
						findViewById(R.id.no_login_rela).setVisibility(View.VISIBLE);
						findViewById(R.id.tv_login_notify).setVisibility(View.VISIBLE);
						findViewById(R.id.tv_noData).setVisibility(View.GONE);
						isShowData=true;
					}
				}
				listMessage.setVisibility(View.VISIBLE);
				if (everyPage == 0)
					everyPage = loadCount;
				currentPage = loadManager.changeMoreBtn(flag, everyPage, loadCount, currentPage,listDataMessage.size() == 0);
				if (isForward)
					adapter.notifyDataSetInvalidated();
				else
					adapter.notifyDataSetChanged();
				// 如果总数据为空,显示没有消息
				listMessage.onRefreshComplete();
			}
		});
	}

	/**
	 * 通知消息跟新
	 * @param id 数据id;
	 * @param type 数据类型 ,新消息;正在发送中;,发送成功;,发送失败;
	 */
	public static void notifiMessage(int type, int id, String returnObj) {
		Message msg = new Message();
		msg.arg1 = id;
		msg.obj = returnObj;
		switch (type) {
		case MSG_LOGIN_OUT_CLEAR_VIEW:
			msg.what = MSG_LOGIN_OUT_CLEAR_VIEW;
			break;
		case MSG_LOGIN_ON:
			msg.what = MSG_LOGIN_ON;
			break;
		case MSG_FEEKBACK_ONREFURESH:
			msg.what = MSG_FEEKBACK_ONREFURESH;
			break;
		case MSG_ONREFRESH_MESSAGE:
			msg.what = MSG_ONREFRESH_MESSAGE;
			break;
		}
		if (handler != null)
			handler.sendMessage(msg);
	}

	/**
	 * 暂缓开启 点击title未读前置
	 */
	private OnClickListener titleClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			//7.29新添加统计
			XHClick.mapStat(MyMessage.this, "a_switch_message", "消息中心", "");
			clickFlag=!clickFlag;
			if(clickFlag){
				msg_title_sort.setText("未读");
			}else{
				msg_title_sort.setText("全部");
			}
			load(true);
		}
	};
}
