package amodule.main.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.BitmapRequestBuilder;
import com.bumptech.glide.load.model.GlideUrl;
import com.xiangha.R;

import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.LoginManager;
import acore.logic.XHClick;
import acore.override.activity.mian.MainBaseActivity;
import acore.tools.FileManager;
import acore.tools.LogManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import acore.widget.TagTextView;
import amodule.dish.activity.OfflineDish;
import amodule.dish.db.DataOperate;
import amodule.main.Main;
import amodule.user.activity.BrowseHistory;
import amodule.user.activity.FansAndFollwers;
import amodule.user.activity.FriendHome;
import amodule.user.activity.MyFavorite;
import amodule.user.activity.MyManagerInfo;
import amodule.user.activity.ScoreStore;
import amodule.user.activity.Setting;
import amodule.user.activity.login.LoginByAccout;
import aplug.basic.InternetCallback;
import aplug.basic.LoadImage;
import aplug.basic.ReqInternet;
import aplug.basic.XHConf;
import aplug.feedback.activity.Feedback;
import third.mall.activity.MallMyFavorableActivity;
import third.mall.activity.MyOrderActivity;
import third.mall.alipay.MallPayActivity;
import third.push.xg.XGPushServer;

/**
 * @Description: TODO
 * @Title: MainMyself.java Copyright: Copyright (c) xiangha.com 2014~2017
 * @author: FangRuijiao
 * @date: 2016年11月13日 下午15:22:50
 */
public class MainMyself extends MainBaseActivity implements OnClickListener {
	private int id = 0;
	// 布局
	private RelativeLayout right_myself, userPage;
	private LinearLayout gourp1, gourp2,gourp3;
	private String[] name1 = {"我的收藏","浏览历史","本地下载"},
			name2 = {"我的订单","优惠券","我的会员","我的钱包","我的积分"},
			name3 = {"帮助与反馈","系统设置"};

//	private int[] img1 = {R.drawable.z_me_list_ico_collect,R.drawable.z_me_list_ico_download,R.drawable.z_me_list_ico_see
//			,R.drawable.z_me_list_ico_integral,R.drawable.z_me_list_ico_order},
//			img2 = {R.drawable.z_me_list_ico_setting,R.drawable.z_me_list_ico_vip,R.drawable.z_me_list_ico_money};

	private final String tongjiId = "a_mine";

	//头部控件
	private TextView goManagerInfo,name,myself_please_login;
	private ImageView myself_iv,myself_lv,iv_userType,isVipImg;

	private TextView dishNum,subjectNum, followNum;
	private TagTextView my_renzheng;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.a_common_myself);
		Main.allMain.allTab.put("MainMyself",this);
		loadManager.showProgressBar();
		initUI();
		XHClick.track(this,"浏览我的页面");
	}

	@Override
	protected void onResume() {
		Main.mainActivity = this;
		super.onResume();
		if (LoginManager.isLogin()) {
			// 设置加载
			loadManager.setLoading(new OnClickListener() {
				@Override
				public void onClick(View v) {
					getData();
				}
			});
		} else
			resetData();
		loadManager.hideProgressBar();
		if (Main.allMain != null && Main.allMain.getBuoy() != null) {
			Main.allMain.getBuoy().clearAnimation();
			Main.allMain.getBuoy().hide();
			Main.allMain.getBuoy().setClosed(true);
			Main.allMain.getBuoy().setMove(true);
		}
		//去我的订单
		if(MallPayActivity.pay_state){
			onEventCommon(3);
		}
	}

	// 重置用户个人信息
	private void resetData() {
		goManagerInfo.setVisibility(View.GONE);
		subjectNum.setText("0");
		dishNum.setText("0");
		followNum.setText("0");
		my_renzheng.setVisibility(View.GONE);
		myself_lv.setVisibility(View.GONE);
		myself_please_login.setVisibility(View.VISIBLE);
		right_myself.setVisibility(View.GONE);
		iv_userType.setVisibility(View.INVISIBLE);
		myself_iv.setImageResource(R.drawable.z_me_head);
		loadManager.hideProgressBar();
	}

	private void initUI() {
//		if(Tools.isShowTitle()) {
//			View bar_title = findViewById(R.id.my_title_bar);
//			RelativeLayout.LayoutParams layout = new RelativeLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, Tools.getStatusBarHeight(this));
//			bar_title.setLayoutParams(layout);
//		}
		String colors = Tools.getColorStr(this, R.color.common_top_bg);
		Tools.setStatusBarColor(this, Color.parseColor(colors));

		goManagerInfo = (TextView) findViewById(R.id.goManagerInfo);
		goManagerInfo.setText("马甲");
		goManagerInfo.setTextColor(0xffffff);
		goManagerInfo.setVisibility(View.GONE);
		goManagerInfo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent it = new Intent(MainMyself.this, MyManagerInfo.class);
				startActivity(it);
			}
		});

		right_myself = (RelativeLayout) findViewById(R.id.right_myself);
		iv_userType = (ImageView) findViewById(R.id.iv_userType);
		subjectNum = (TextView) findViewById(R.id.my_subject);
		dishNum = (TextView) findViewById(R.id.my_dish);
		followNum = (TextView) findViewById(R.id.my_flow);
		isVipImg = (ImageView) findViewById(R.id.a_user_home_title_vip);

		gourp1 = (LinearLayout) findViewById(R.id.myself_gourp1);
		gourp2 = (LinearLayout) findViewById(R.id.myself_gourp2);
		gourp3 = (LinearLayout) findViewById(R.id.myself_gourp3);

		LayoutInflater layoutInfater = LayoutInflater.from(this);
		itemInfalter(layoutInfater,gourp1 , name1);
		itemInfalter(layoutInfater,gourp2 , name2);
		itemInfalter(layoutInfater,gourp3 , name3);

		Object isShowVip = FileManager.loadShared(this,FileManager.xmlFile_appInfo,"isShowVip");
		Object isShowMoney = FileManager.loadShared(this,FileManager.xmlFile_appInfo,"isShowMoney");
		Object isShowOpinion = FileManager.loadShared(this,FileManager.xmlFile_appInfo,"isShowOpinion");
		if(isShowVip == null || TextUtils.isEmpty(String.valueOf(isShowVip))){
			gourp2.getChildAt(2).findViewById(R.id.my_new_info).setVisibility(View.VISIBLE);
			gourp2.getChildAt(2).findViewById(R.id.ico_right_myself).setVisibility(View.GONE);
		}
		if(isShowMoney == null || TextUtils.isEmpty(String.valueOf(isShowMoney))){
			gourp2.getChildAt(3).findViewById(R.id.my_new_info).setVisibility(View.VISIBLE);
			gourp2.getChildAt(3).findViewById(R.id.ico_right_myself).setVisibility(View.GONE);
		}
		gourp2.getChildAt(4).findViewById(R.id.ico_right_myself).setVisibility(View.GONE);
		gourp3.getChildAt(1).findViewById(R.id.ico_right_myself).setVisibility(View.GONE);
		if (TextUtils.isEmpty(String.valueOf(isShowOpinion))) {
			gourp3.getChildAt(0).findViewById(R.id.my_new_info).setVisibility(View.VISIBLE);
			gourp3.getChildAt(0).findViewById(R.id.ico_right_myself).setVisibility(View.GONE);
		}
		TextView scoreTv = (TextView) gourp2.getChildAt(4).findViewById(R.id.text_right_myself);
		scoreTv.setText("积分商城惊喜不断");
		TextView setting = (TextView) gourp3.getChildAt(1).findViewById(R.id.text_right_myself);
		setting.setText("版本号：" + ToolsDevice.getVerName(this));

		userPage = (RelativeLayout) findViewById(R.id.rl_userPage);
		name = (TextView) findViewById(R.id.myself_name);
		myself_lv = (ImageView) findViewById(R.id.myself_lv);
		myself_iv = (ImageView) findViewById(R.id.myself_iv);
		myself_please_login = (TextView) findViewById(R.id.myself_please_login);
		my_renzheng = (TagTextView) findViewById(R.id.my_renzheng);

		myself_please_login.setOnClickListener(this);
		userPage.setOnClickListener(this);
		myself_lv.setOnClickListener(this);
		my_renzheng.setOnClickListener(this);
		findViewById(R.id.ll_subject).setOnClickListener(this);
		findViewById(R.id.ll_dish).setOnClickListener(this);
		findViewById(R.id.ll_flow).setOnClickListener(this);

		myself_iv.setImageResource(R.drawable.z_me_head);
		if (LoginManager.isLogin()) {
			right_myself.setVisibility(View.VISIBLE);
			myself_please_login.setVisibility(View.GONE);
		} else {
			right_myself.setVisibility(View.GONE);
			myself_please_login.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * 获取用户信息
	 */
	private void getData() {
		String params = "type=getData&devCode=" + XGPushServer.getXGToken(getApplicationContext());
		ReqInternet.in().doPost(StringManager.api_getUserInfo, params, new InternetCallback(getApplicationContext()) {

			@Override
			public void loaded(int flag, String url, Object returnObj) {
				if (flag >= ReqInternet.REQ_OK_STRING) {
					// loadManager.showProgressBar();
					// 解析数据
					LoginManager.setDataUser(MainMyself.this,returnObj);
					Map<String, String> listMap = StringManager.getListMapByJson(returnObj).get(0);
					// 设置离线菜单MAX数量
					if(!TextUtils.isEmpty(listMap.get("downDish")))
						DataOperate.setDownDishLimit(MainMyself.this, Integer.valueOf(listMap.get("downDish")));
					if(!TextUtils.isEmpty(listMap.get("nextDownDish")))
						AppCommon.nextDownDish = Integer.parseInt(listMap.get("nextDownDish"));
					name.setText(listMap.get("nickName"));

					subjectNum.setText(listMap.get("subjectNum"));
					dishNum.setText(listMap.get("upNum"));
					followNum.setText(listMap.get("followNum"));

					AppCommon.setLvImage(Integer.valueOf(listMap.get("lv")), myself_lv);
					boolean isVip = AppCommon.setVip(MainMyself.this,isVipImg,listMap.get("vip"),tongjiId,"头部");
					if(!isVip){
						isVipImg.setVisibility(View.VISIBLE);
						isVipImg.setImageResource(R.drawable.i_user_home_unvip);
					}

					if (listMap.get("isGourmet") != null){
						boolean isGourmet = AppCommon.setUserTypeImage(Integer.valueOf(listMap.get("isGourmet")), iv_userType);
						if(isGourmet){
							my_renzheng.setText("已认证");
							my_renzheng.setSideColor(getResources().getColor(R.color.comment_color));
							my_renzheng.setTextColor(getResources().getColor(R.color.comment_color));
						}else{
							my_renzheng.setText("申请认证");
							my_renzheng.setTextColor(getResources().getColor(R.color.common_top_text));
							my_renzheng.setSideColor(getResources().getColor(R.color.common_top_text));
						}
						my_renzheng.setVisibility(View.VISIBLE);
					}
					setUserImage(myself_iv, listMap.get("img"));

					myself_please_login.setVisibility(View.GONE);
					if (listMap.get("isManager").equals("3") || listMap.get("isManager").equals("2") || XHConf.log_isDebug) {
						// findViewById(R.id.bt_manager).setVisibility(View.VISIBLE);
						goManagerInfo.setVisibility(View.VISIBLE);
					} else {
						goManagerInfo.setVisibility(View.GONE);
					}
					myself_please_login.setVisibility(View.GONE);
					right_myself.setVisibility(View.VISIBLE);
				}else if(flag == ReqInternet.REQ_CODE_ERROR){
					LoginManager.logout(MainMyself.this);
					resetData();
				}else {
					if (LoginManager.userInfo.get("userCode") == null || LoginManager.userInfo.get("userCode") == "") {
						LoginManager.logout(MainMyself.this);
						resetData();
					}
					loadManager.hideProgressBar();
				}
			}

		});
	}

	private void itemInfalter(LayoutInflater layoutInfater,LinearLayout parent, String[] groupNames) {
		for (int i = 0; i < groupNames.length; i++) {
			View itemView =  layoutInfater.inflate(R.layout.a_common_myself_item, null);
			itemView.setClickable(true);
			itemView.setTag("" + id++);
			itemView.setOnClickListener(this);

			TextView text = (TextView) itemView.findViewById(R.id.text_myself);
			text.setText(groupNames[i]);
			parent.addView(itemView);

			if (i == groupNames.length - 1) {
				itemView.findViewById(R.id.my_item_line).setVisibility(View.GONE);
			}
		}
	}

	@Override
	public void onClick(View v) {
		if (v.getTag() != null) {
			if (!onEventbranch(Integer.valueOf(v.getTag().toString())))
				onEventCommon(Integer.valueOf(v.getTag().toString()));
		} else {
			onEventbranch(v.getId());
		}
	}

	private boolean onEventbranch(int index) {
		if (LoginManager.userInfo.size() == 0) {
			switch (index) {
				case R.id.ll_subject:
				case R.id.ll_dish:
				case R.id.ll_flow:
				case R.id.ll_score:
				case 0: //收藏
				case 7: //积分
				case 3: //订单
				case 4: //优惠券
				case 6: //钱包
					Tools.showToast(this,"请先登录或注册哦~");
				case R.id.myself_lv: //用户等级
				case R.id.myself_please_login:
				case R.id.ico_right_myself:
				case R.id.myself_iv:
					XHClick.mapStat(this, tongjiId, "头部", "登录");
					Intent intent = new Intent(MainMyself.this, LoginByAccout.class);
					startActivity(intent);
					return true;
			}
			return false;
		} else {
			LogManager.print("d", "事件点击index:" + index);
			switch (index) {
				case R.id.rl_userPage:
					// 统计
					XHClick.track(getApplicationContext(), "点击我的页面的头部");
					XHClick.mapStat(this, tongjiId, "头部", "个人主页");
					Intent intent = new Intent(MainMyself.this, FriendHome.class);
					intent.putExtra("code", LoginManager.userInfo.get("code"));
					startActivity(intent);
					return true;
				case R.id.my_renzheng: //认证美食家
					XHClick.track(getApplicationContext(), "点击我的页面的头部");
					XHClick.mapStat(this, tongjiId, "头部", "认证");
					AppCommon.openUrl(this, "http://appweb.xiangha.com/approve/index", true);
					break;
				case R.id.myself_lv: //用户等级
					XHClick.track(getApplicationContext(), "点击我的页面的头部");
					XHClick.mapStat(this, tongjiId, "头部", "等级");
					AppCommon.openUrl(this, StringManager.api_getCustomerRank + "?code=" + LoginManager.userInfo.get("code"), true);
					return true;
				case R.id.ll_subject:
					// 统计
					XHClick.track(getApplicationContext(), "点击我的页面的贴子");
					XHClick.mapStat(this, tongjiId, "导航", "贴子");
					Intent intent_subject = new Intent(MainMyself.this, FriendHome.class);
					intent_subject.putExtra("code", LoginManager.userInfo.get("code"));
					startActivity(intent_subject);
					return true;
				case R.id.ll_dish:
					// 统计
					XHClick.track(getApplicationContext(), "点击我的页面的菜谱");
					XHClick.mapStat(this, tongjiId, "导航", "菜谱");
					Intent intent_dish = new Intent(MainMyself.this, FriendHome.class);
					intent_dish.putExtra("code", LoginManager.userInfo.get("code"));
					intent_dish.putExtra("index",1);
					startActivity(intent_dish);
					return true;
				case R.id.ll_flow:
					// 统计
					XHClick.mapStat(this, tongjiId, "导航", "关注");
					Intent intent_flow = new Intent(MainMyself.this, FansAndFollwers.class);
					intent_flow.putExtra("page","2");
					startActivity(intent_flow);

					return true;
			}
		}
		return false;
	}

	public void onEventCommon(int index) {
		switch (index) {
			case 0://收藏
				XHClick.track(getApplicationContext(), "点击我的页面的收藏");
				XHClick.mapStat(this, tongjiId,"列表","收藏");
				Intent intent_fav = new Intent(MainMyself.this, MyFavorite.class);
				startActivity(intent_fav);
//				Intent intent = new Intent(this,CommentActivity.class);
//				startActivity(intent);
				break;
			case 1://浏览记录
				XHClick.mapStat(this, tongjiId,"列表","看过");
				Intent intent_history = new Intent(MainMyself.this, BrowseHistory.class);
				startActivity(intent_history);
				break;
			case 2://离线菜谱
				// 统计
				XHClick.track(getApplicationContext(), "点击我的页面的下载");
				XHClick.mapStat(this, tongjiId,"列表","下载");
				Intent intent_off = new Intent(MainMyself.this, OfflineDish.class);
				startActivity(intent_off);
				break;
			case 3:
				XHClick.mapStat(this, tongjiId,"列表", "订单");
				Intent intent_order = new Intent(this,MyOrderActivity.class);
				startActivity(intent_order);
				break;
			case 4:
				XHClick.mapStat(this, tongjiId,"列表", "优惠券");
				Intent intent_coupon = new Intent(this,MallMyFavorableActivity.class);
				startActivity(intent_coupon);
				break;
			case 5: //会员
				XHClick.mapStat(this, tongjiId,"列表", "我的会员");
				AppCommon.openUrl(MainMyself.this,StringManager.api_vip,true);
				FileManager.saveShared(this,FileManager.xmlFile_appInfo,"isShowVip","2");
				gourp2.getChildAt(2).findViewById(R.id.my_new_info).setVisibility(View.GONE);
				gourp2.getChildAt(2).findViewById(R.id.ico_right_myself).setVisibility(View.VISIBLE);
				break;
			case 6://钱包
				XHClick.mapStat(this, tongjiId,"列表", "我的钱包");
				AppCommon.openUrl(MainMyself.this,StringManager.api_money,true);
				FileManager.saveShared(this,FileManager.xmlFile_appInfo,"isShowMoney","2");
				gourp2.getChildAt(3).findViewById(R.id.my_new_info).setVisibility(View.GONE);
				gourp2.getChildAt(3).findViewById(R.id.ico_right_myself).setVisibility(View.VISIBLE);
				break;
			case 7:
				XHClick.track(getApplicationContext(), "点击我的页面的积分");
				XHClick.mapStat(this, tongjiId,"列表", "积分商城");
//			AppCommon.openUrl(this, StringManager.api_scoreStore + "?code=" + LoginManager.userInfo.get("code"), true);
				Intent scoreStore = new Intent(MainMyself.this, ScoreStore.class);
				startActivity(scoreStore);
				break;
			case 9://设置
				XHClick.track(getApplicationContext(), "点击我的页面的设置");
				XHClick.mapStat(this, tongjiId, "设置", "");
				Intent intent_setting = new Intent(MainMyself.this, Setting.class);
				intent_setting.putExtra("isGoManagerInfo",goManagerInfo.getVisibility() == View.VISIBLE);
				startActivity(intent_setting);
				break;
			case 8://意见反馈
				FileManager.saveShared(this,FileManager.xmlFile_appInfo,"isShowOpinion","2");
				gourp3.getChildAt(0).findViewById(R.id.my_new_info).setVisibility(View.GONE);
				gourp3.getChildAt(0).findViewById(R.id.ico_right_myself).setVisibility(View.VISIBLE);
				Intent intent2 = new Intent(MainMyself.this, Feedback.class);
				startActivity(intent2);
				break;
		}
	}

	public void setUserImage(final ImageView v, String value) {
		v.setVisibility(View.VISIBLE);
		v.setScaleType(ScaleType.CENTER_CROP);
		v.setImageResource(R.drawable.bg_round_grey_e0e0e0_50);
		BitmapRequestBuilder<GlideUrl, Bitmap> bitmapRequest = LoadImage.with(this)
				.load(value)
				.setImageRound(ToolsDevice.dp2px(MainMyself.this, 500))
				.build();
		if(bitmapRequest != null)
			bitmapRequest.into(v);
	}
}