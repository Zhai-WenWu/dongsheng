package amodule.health.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.xiangha.R;

import java.util.List;
import java.util.Map;

import acore.logic.LoginManager;
import acore.logic.XHClick;
import acore.override.activity.base.BaseActivity;
import acore.tools.FileManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;
import third.push.xg.XGPushServer;
import third.share.BarShare;
import xh.basic.internet.UtilInternet;
import xh.basic.tool.UtilString;

public class MyPhysique extends BaseActivity {
	private TextView my_physique, physique_rate, physique_desc, test_again;
	private TextView physique_check_btn;

	private Handler handler;
	private static final int PARAMS_LOAD_OVER=1;
	private String physique = "", rate = "", info = "", g1 = "";
	private String params = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initActivity("我的体质", 2, 0, R.layout.c_view_bar_title, R.layout.a_health_my_physique);
		handler=new Handler(new Callback() {
			
			@Override
			public boolean handleMessage(Message msg) {
				switch(msg.what){
				case PARAMS_LOAD_OVER:
					initBarView();
					initUI();
				break;
				}
				return false;
			}
		});
		Bundle bundle = this.getIntent().getExtras();
		if (bundle != null) {
			params = bundle.getString("params");
		}
		if(params != null && params.length()>10){ 
			LoginManager.modifyUserInfo(MyPhysique.this, "crowd", params);
			parseData(params);
		}else if(LoginManager.isLogin()){
			getUserCrowdData();
		}else{
			Tools.showToast(MyPhysique.this, "获取体质信息失败，请重试....");
			finish();
		}
	}

	private boolean parseData(String json) {
		List<Map<String,String>> crowdData = StringManager.getListMapByJson(json);
		if(crowdData.size() >0){
			Map<String, String> map = crowdData.get(0);
			physique = map.get("name");
			rate = map.get("bili");
			info = map.get("info");
			g1 = map.get("pinyin");
			handler.sendEmptyMessage(PARAMS_LOAD_OVER);
			return true;
		}
		return false;
	}

	private void getUserCrowdData() {
		//如果体质测试结果为null,有用户则请求数据进行加载
		String params = "type=getData&devCode=" + XGPushServer.getXGToken(this);
		ReqInternet.in().doPost(StringManager.api_getUserInfo, params,new InternetCallback() {
			@Override
			public void loaded(int flag, String url, Object returnObj) {
				if (flag >= UtilInternet.REQ_OK_STRING){
					List<Map<String,String>> dataReturn = UtilString.getListMapByJson(returnObj);
					if(dataReturn.size() > 0){
						Map<String, String> mapReturn = dataReturn.get(0);
						//修改用户养生信息
						LoginManager.modifyUserInfo(MyPhysique.this, "crowd", mapReturn.get("crowd"));
						String crowd = mapReturn.get("crowd");
						if(crowd != null && crowd.length()>10){
							parseData(crowd);
						}else{
							Tools.showToast(MyPhysique.this, "获取体质信息失败，请重试....");
							MyPhysique.this.finish();
						}
					}else{
						Tools.showToast(MyPhysique.this, "获取体质信息失败，请重试....");
						MyPhysique.this.finish();
					}
				}
			}
		});
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		loadManager.hideProgressBar();
	}

	private void initUI() {
		ImageView btnRight=(ImageView) findViewById(R.id.rightImgBtn2);
		btnRight.setVisibility(View.VISIBLE);
		btnRight.setImageResource(R.drawable.z_z_topbar_ico_share);
		btnRight.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				doShare();
			}
		});
		
		my_physique = (TextView) findViewById(R.id.health_my_physiqueContent);
		physique_rate = (TextView) findViewById(R.id.health_physique_rate);
		physique_desc = (TextView) findViewById(R.id.health_physique_desc);
		test_again = (TextView) findViewById(R.id.health_physique_test_again);
		physique_check_btn = (TextView) findViewById(R.id.health_physique_check_btn);
		my_physique.setText(physique);
		test_again.setText("<<再次测试我的体质");
		// 设置指定位置文字的颜色
		String str = "约" + rate + "的人为" + physique;
		SpannableStringBuilder style = new SpannableStringBuilder(str);
		// 设置指定位置文字的颜色
		style.setSpan(new ForegroundColorSpan(Color.parseColor("#22BA41")), 1, str.indexOf("的"), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
		style.setSpan(new AbsoluteSizeSpan(24, true), 1, str.indexOf("的"), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		physique_rate.setText(style);
		physique_desc.setText(info);
		physique_check_btn.setText("查看" + physique + "养生");
		findViewById(R.id.health_sroll_physiqueContent).setVisibility(View.VISIBLE);
		findViewById(R.id.health_physique_share).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				shareTest(v);
			}
		});
		physique_check_btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				checkTest(v);
			}
		});
	}

	private void initBarView() {
		// titleBar初始化
		ImageView shareBtn = (ImageView) findViewById(R.id.rightImgBtn2);
		shareBtn.setVisibility(View.VISIBLE);
		shareBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				doShareResult();
			}
		});
	}

	// 再一次测试
	public void testAgain(View view) {
		FileManager.delete(FileManager.getDataDir() + FileManager.file_healthResult);
		FileManager.delete(FileManager.getDataDir() + FileManager.file_constitution);
		Intent intent = new Intent(this, HealthTest.class);
		startActivity(intent);
		finish();
	}

	// 邀请家人测试
	public void shareTest(View view) {
		doShare();
	}

	// 查看测试结果
	public void checkTest(View view) {
		Intent intent = new Intent(MyPhysique.this, DetailHealth.class);
		intent.putExtra("name", physique);
		intent.putExtra("code", g1);
		intent.putExtra("type", "tizhi");
		intent.putExtra("ico_id",R.drawable.z_yangs_home_ico_list_10);
		startActivity(intent);
	}

	private void doShare() {
		XHClick.mapStat(this, "a_share400", "养生", "体质养生－邀请家人测试");
		// 分享测试
		barShare = new BarShare(this, "体质养生－邀请家人测试","养生");
		String type = BarShare.IMG_TYPE_RES;
		String title = "中医九种体质测试，太准了";
		String content = "我在用香哈菜谱【体质养生】，中医推荐养生方法，你也应该测一下~ ";
		String imgUrl = "" + R.drawable.z_yangs_home_ico_list_10;
		String clickUrl = StringManager.wwwUrl + "app/download";
		barShare.setShare(type, title, content, imgUrl, clickUrl);
		barShare.openShare();
	}

	private void doShareResult() {
		XHClick.mapStat(this, "a_share400", "养生", "体质养生－体质测试结果");
		// 分享结果
		barShare = new BarShare(this, "体质养生－体质测试结果","养生");
		String type = BarShare.IMG_TYPE_RES;
		String title = "中医九种体质测试，太准了";
		String content = "我和" + rate + "的人一样是" + physique + "，你呢？（香哈菜谱）" ;
		String imgUrl = "" + R.drawable.z_yangs_home_ico_list_10;
		String clickUrl = StringManager.wwwUrl + "app/download";
		barShare.setShare(type, title, content, imgUrl, clickUrl);
		barShare.openShare();
	}
}
