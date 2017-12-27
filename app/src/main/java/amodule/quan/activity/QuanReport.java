package amodule.quan.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiangha.R;

import acore.logic.AppCommon;
import acore.logic.LoginManager;
import acore.logic.XHClick;
import acore.override.activity.base.BaseActivity;
import acore.tools.StringManager;
import acore.tools.Tools;
import amodule.user.activity.login.LoginByAccout;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;
import xh.basic.internet.UtilInternet;

public class QuanReport extends BaseActivity{
	private LinearLayout user_report_collection;//用户举报的条目集合
	private LinearLayout admin_report_conlection;//管理员举报的条目集合
	private LinearLayout admin_report_conlection_quan;//美食圈管理员权限
	private ImageView quan_report_blacklist;//黑名单开关
	private TextView admin_report_tv_item;//管理员的标志栏
	private Button btn_quan_report_commit;//提交按钮
	private RelativeLayout quan_report_blacklist_relativelayout;//黑名单条目
	private String[] userReportInfos = new String[]{"广告骚扰","与美食无关","政治敏感","色情低俗","人身攻击/谩骂/诽谤","冒用/侵权"};
	private String nickName = "" ,code = "", subjectCode , repType , isQuan = "";
	private int userSelect = 0 , operation = 0 , userSelectItem = 100 , adminSelectItem = 100;
	private boolean isToLogin = false,isBlack = false , 
			isAdmin , isUserSelect = false , 
			isAdminSelect = false , isBlackChange = false;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initActivity("举报", 2, 0, R.layout.report_view_bar_title, R.layout.a_quan_report_new);
		loadManager.hideProgressBar();
		Bundle bundle = this.getIntent().getExtras();
		if (bundle != null) {
			isQuan = bundle.getString("isQuan");//0 / 1       不是  / 是
			nickName = bundle.getString("nickName");//获取的举报类型,比如 举报某某某
			code = bundle.getString("code");
			repType  = bundle.getString("repType");
			subjectCode = bundle.getString("subjectCode");
		}
		initView();
		initListener();
	}
	@Override
	protected void onResume() {
		super.onResume();
		doLogin();//主要是判断是否登录,根据不同类型的用户显示不同的举报选项.
	}
	
	private void doLogin() {
		if (!LoginManager.isLogin() && !isToLogin) {
			isToLogin = true;
			Intent intent = new Intent(this,LoginByAccout.class);
			this.startActivity(intent);
			return;
		}else if (isToLogin && !LoginManager.isLogin()) {
			QuanReport.this.finish();
			return;
		}
		//请求数据
		ReqInternet.in().doPost(StringManager.api_getCheckIngore, "customerCode=" + code, new InternetCallback() {
			@Override
			public void loaded(int flag, String url, Object returnObj) {
				if (flag >= UtilInternet.REQ_OK_STRING) {
					if (returnObj.equals("2")) {
						isBlack = true;
						isBlackChange = true;
						quan_report_blacklist.setSelected(true);
					}else {
						isBlack = false;
						isBlackChange = false;
						quan_report_blacklist.setSelected(false);
					}
				}else {
					isBlack = false;
					isBlackChange = false;
					quan_report_blacklist.setSelected(false);
				}
				quan_report_blacklist.setVisibility(View.VISIBLE);
			}
		});
		isAdmin = LoginManager.isManager();//返回true为是管理员   返回false 为不是管理员
		if (!isAdmin) {//不是管理员
			admin_report_tv_item.setVisibility(View.GONE);
			admin_report_conlection.setVisibility(View.GONE);
			admin_report_conlection_quan.setVisibility(View.GONE);
		} else {//是管理员
			admin_report_tv_item.setVisibility(View.VISIBLE);
			if (isQuan != "" && isQuan != null) {
				if (isQuan.equals("0")) {
					admin_report_conlection.setVisibility(View.VISIBLE);
					admin_report_conlection_quan.setVisibility(View.GONE);
				}else {
					admin_report_conlection.setVisibility(View.GONE);
					admin_report_conlection_quan.setVisibility(View.VISIBLE);
				}
			}
		}
	}

	private void initView() {
		user_report_collection = (LinearLayout) findViewById(R.id.user_report_collection);
		quan_report_blacklist = (ImageView) findViewById(R.id.black_switch_btn);
		quan_report_blacklist_relativelayout = (RelativeLayout) findViewById(R.id.quan_report_blacklist_relativelayout);
		admin_report_tv_item = (TextView) findViewById(R.id.admin_report_tv_item);
		admin_report_conlection = (LinearLayout) findViewById(R.id.admin_report_conlection);
		btn_quan_report_commit = (Button) findViewById(R.id.btn_quan_report_commit);
		admin_report_conlection_quan = (LinearLayout) findViewById(R.id.admin_report_conlection_quan);
		quan_report_blacklist.setVisibility(View.GONE);
		if (isQuan != "" && isQuan != null) {
			quan_report_blacklist_relativelayout.setVisibility(isQuan.equals("1") ? View.GONE : View.VISIBLE);
		}
		TextView title = (TextView) findViewById(R.id.title);
		if (nickName != "") {
			title.setText(" " + nickName);
		}
	}
	
	private void initListener() {
		for (int i = 0; i < user_report_collection.getChildCount(); i++) {
			user_report_collection.getChildAt(i).setOnClickListener(onReportclick(i));
		}
		for (int i = 0; i < admin_report_conlection.getChildCount(); i++) {
			admin_report_conlection.getChildAt(i).setOnClickListener(onAdminReportClick(i));
		}
		for (int i = 0; i < admin_report_conlection_quan.getChildCount(); i++) {
			admin_report_conlection_quan.getChildAt(i).setOnClickListener(onAdminQuanReportClick(i));
		}
		//提交
		btn_quan_report_commit.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (isQuan != "" && isQuan != null) {
					
					if (isQuan.equals("0")) {//不是从美食圈过来的
						if (isBlackChange == isBlack && userSelect == 0) {
							Tools.showToast(QuanReport.this, "请选择理由");
							return;
						}else if (operation > 0 && userSelect == 0) {
							Tools.showToast(QuanReport.this, "请选择理由");
							return;
						}
						if (isBlackChange^isBlack) {
							String param = isBlack ?  "type=ignore&cusCode=" + code: "type=restore&cusCode=" + code;
							ReqInternet.in().doPost(StringManager.api_setUserData, param, new InternetCallback() {
								
								@Override
								public void loaded(int flag, String url, Object returnObj) {
									Tools.showToast(QuanReport.this, returnObj.toString());
									if (flag >= UtilInternet.REQ_OK_STRING) {
										isBlackChange = isBlack;
									}
									if (userSelect == 0 && operation == 0) {
										QuanReport.this.setResult(100);
										QuanReport.this.finish();
									}
								}
							});
						}
					}else {
						//是美食圈过来的
						if (userSelect == 0) {
							Tools.showToast(QuanReport.this, "请选择理由");
							return;
						}
					}
				}else {
					Tools.showToast(QuanReport.this, "信息错误,请返回重新选择");
					return;
				}
				String infoString = "";
				if (userSelect == 0 && operation == 0) {
					return;
				}else if (userSelect > 0 ) {
					infoString = userReportInfos[userSelect-1];
				}
				btn_quan_report_commit.setClickable(false);
				String param="type=report&subjectCode="+subjectCode
						+"&repType="+repType+"&repTypeCode=" + code
						+ "&content" + "" + "&info="+ infoString + "&operation=" + operation;
				ReqInternet.in().doPost(StringManager.api_quanSetSubject, param, new InternetCallback() {
					
					@Override
					public void loaded(int flag, String url, Object returnObj) {
						if(flag < ReqInternet.REQ_OK_STRING)
							Tools.showToast(QuanReport.this, returnObj.toString());
						//统计举报类型.
						doTongJi();
						if(operation>0)
							QuanReport.this.setResult(100);
						QuanReport.this.finish();
					}
				});
			}
		});
		quan_report_blacklist.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				quan_report_blacklist.setSelected(!quan_report_blacklist.isSelected());
				isBlack = quan_report_blacklist.isSelected();
			}
		});
		findViewById(R.id.icon_report).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				AppCommon.openUrl(QuanReport.this, StringManager.api_agreementReport, true);
			}
		});
	}
	private OnClickListener onAdminQuanReportClick(final int item) {
		return new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				for (int i = 0; i < admin_report_conlection_quan.getChildCount(); i++) {
					RelativeLayout child = (RelativeLayout) admin_report_conlection_quan.getChildAt(i);
					ImageView imageView = (ImageView) child.getChildAt(0);
					if (item == i) {
						if (adminSelectItem == item && isAdminSelect) {
							imageView.setImageResource(R.drawable.j_select);
							operation = 0;
							isAdminSelect = false;
						} else {
							adminSelectItem = item;
							isAdminSelect = true;
							imageView.setImageResource(R.drawable.j_select_active);
							operation = i + 4;
						}
					} else {
						imageView.setImageResource(R.drawable.j_select);
					}
				}
			}
		};
	}
	private OnClickListener onAdminReportClick(final int item) {
		return new OnClickListener() {
			@Override
			public void onClick(View v) {
				for (int i = 0; i < admin_report_conlection.getChildCount(); i++) {
					RelativeLayout child = (RelativeLayout) admin_report_conlection.getChildAt(i);
					ImageView imageView = (ImageView) child.getChildAt(0);
					if (item == i) {
						if (adminSelectItem == item && isAdminSelect) {
							imageView.setImageResource(R.drawable.j_select);
							operation = 0;
							isAdminSelect = false;
						}else {
							adminSelectItem = item;
							isAdminSelect = true;
							imageView.setImageResource(R.drawable.j_select_active);
							operation = i + 1;
						}
					} else {
						imageView.setImageResource(R.drawable.j_select);
					}
				}
			}
		};
	}
	private OnClickListener onReportclick(final int item) {
		return new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				for (int i = 0; i < user_report_collection.getChildCount(); i++) {
					RelativeLayout child = (RelativeLayout) user_report_collection.getChildAt(i);
					ImageView imageView = (ImageView) child.getChildAt(0);
					if (item == i) {
						if (userSelectItem == item && isUserSelect) {
							imageView.setImageResource(R.drawable.j_select);
							userSelect = 0;
							isUserSelect = false;
						} else {
							userSelectItem = item;
							imageView.setImageResource(R.drawable.j_select_active);
							userSelect = i + 1;
							isUserSelect = true;
						}
					} else {
						imageView.setImageResource(R.drawable.j_select);
					}
				}
			}
		};
	}
	/**
	 * 统计举报类型
	 */
	private void doTongJi() {
		if (repType.equals("1")) {
			//统计 举报贴(计算事件)
			XHClick.onEventValue(this,"quanOperate","quanOperate","举报贴",1);
		}else if (repType.equals("2")) {
			//统计 举报楼(计算事件)
			XHClick.onEventValue(this,"quanOperate","quanOperate","举报楼",1);
		}else if (repType.equals("3")) {
			//统计 举报回复(计算事件)
			XHClick.onEventValue(this,"quanOperate","quanOperate","举报回复",1);
		}else if (repType.equals("4")) {
			//统计 举报用户(计算事件)
			XHClick.onEventValue(this,"quanOperate","quanOperate","举报用户",1);
		}
	}
}
