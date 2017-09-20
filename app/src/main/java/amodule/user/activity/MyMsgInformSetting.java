package amodule.user.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import com.popdialog.util.PushManager;
import com.xiangha.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import acore.override.activity.base.BaseActivity;
import acore.tools.FileManager;
import acore.tools.StringManager;
import acore.widget.switchView;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;
import xh.basic.tool.UtilFile;

/**
 * 消息通知设置
 * @author luomin
 */
public class MyMsgInformSetting extends BaseActivity{
	private switchView msg_newMSG_sb;//接收全部消息通知
	private switchView msg_nous_sb;//香哈头条推荐
	private switchView msg_subject_sb;//美食贴通知
	private switchView msg_jxMenu_sb;//精选菜单通知
	private switchView msg_jxCaiPu_sb;//精选菜谱通知
	private switchView msg_quan_zan_sb;//美食圈评论.点赞
	private switchView msg_informSing_sb;//通知声音
	private switchView msg_informShork_sb;//通知震动
	private LinearLayout msgInform_ll;//通知开关所在总布局
	private RelativeLayout login_hint;//登录提示
	private View msg_unClick;//遮罩
	
	private String newMSG;
	private String nous;
	private String quan;
	private String jxMenu;
	private String jxDish;
	private String zan;

	private boolean isOnPause = false;
	String informSing ,informShork;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initActivity("通知设置", 2, 0, R.layout.c_view_bar_title, R.layout.a_my_msginform);
		initView();
		init();
		setListener();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if(isOnPause){
			isOnPause = false;
			doMsgShow();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		isOnPause = true;
	}

	@Override
	public void finish() {
		super.finish();
		
		JSONArray array1 = new JSONArray();
		JSONObject stoneObject = new JSONObject();
		String url = StringManager.apiUrl+"home5/setInfoSwitch";  
		String msgString;
		try {
			stoneObject.put("mSwitch", newMSG);
			stoneObject.put("subject", quan);
			stoneObject.put("zhishi", nous);
			stoneObject.put("quan", zan);
			stoneObject.put("menu", jxMenu);
			stoneObject.put("caipu", jxDish);
			array1.put(stoneObject);
			msgString = "list="+ array1.get(0).toString();
			ReqInternet.in().doPost(url, msgString, new InternetCallback(this) {
				
				@Override
				public void loaded(int flag, String url, Object returnObj) {
				}
			});
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	/**
	 * 开关状态改变监听
	 */
	private void setListener() {
		//http://api.huher.com/home5/setInfoSwitch?isc=caipu&ope=2
		msg_newMSG_sb.setOnChangeListener(new switchView.OnSwitchChangeListener() {
			
			@Override
			public void onChange(final boolean state) {
				PushManager.requestPermission(MyMsgInformSetting.this);
			}
			
		});
		msg_subject_sb.setOnChangeListener(new switchView.OnSwitchChangeListener() {
			
			@Override
			public void onChange( final boolean state) {
				doMSGSetting(state,FileManager.quan);
				if (state) {
					quan = "1";
				}else {
					quan = "2";
				}
			}

		});
		msg_nous_sb.setOnChangeListener(new switchView.OnSwitchChangeListener() {
			
			@Override
			public void onChange( boolean state) {
				doMSGSetting(state,FileManager.zhishi);
				if (state) {
					nous = "1";
				}else {
					nous = "2";
				}
			}
		});
		msg_quan_zan_sb.setOnChangeListener(new switchView.OnSwitchChangeListener() {
			
			@Override
			public void onChange(boolean state) {
				doMSGSetting(state,FileManager.zan);
				if (state) {
					zan = "1";
				}else {
					zan = "2";
				}
			}
		});
		msg_jxMenu_sb.setOnChangeListener(new switchView.OnSwitchChangeListener() {
			
			@Override
			public void onChange( boolean state) {
				doMSGSetting(state,FileManager.menu);
				if (state) {
					jxMenu = "1";
				}else {
					jxMenu = "2";
				}
			}
		});
		msg_jxCaiPu_sb.setOnChangeListener(new switchView.OnSwitchChangeListener() {
			
			@Override
			public void onChange( boolean state) {
				doMSGSetting(state,FileManager.caipu);
				if (state) {
					jxDish = "1";
				}else {
					jxDish = "2";
				}
			}
		});
		msg_informSing_sb.setOnChangeListener(new switchView.OnSwitchChangeListener() {
			
			@Override
			public void onChange( boolean state) {
				informSing = state ? "1" : "2";
				UtilFile.saveShared(getApplicationContext(), FileManager.msgInform, FileManager.informSing, informSing);
			}
		});
		msg_informShork_sb.setOnChangeListener(new switchView.OnSwitchChangeListener() {
			
			@Override
			public void onChange( boolean state) {
				informShork = state ? "1" : "2";
				UtilFile.saveShared(getApplicationContext(), FileManager.msgInform, FileManager.informShork, informShork);
			}
		});
	}

	private void initView() {
		ScrollView scrollView = (ScrollView) findViewById(R.id.msg_inform_scrollview);
		scrollView.setVisibility(View.VISIBLE);
		loadManager.hideProgressBar();
		msg_newMSG_sb = (switchView) findViewById(R.id.msg_newMSG_sb);
		msg_subject_sb = (switchView) findViewById(R.id.msg_subject_sb);
		msg_nous_sb = (switchView) findViewById(R.id.msg_nous_sb);
		msg_jxCaiPu_sb = (switchView) findViewById(R.id.msg_jxCaiPu_sb);
		msg_jxMenu_sb = (switchView) findViewById(R.id.msg_jxMenu_sb);
		msg_quan_zan_sb = (switchView) findViewById(R.id.msg_quan_zan_sb);
		msg_informSing_sb = (switchView) findViewById(R.id.msg_informsing_sb);
		msg_informShork_sb = (switchView) findViewById(R.id.msg_informshork_sb);
		msgInform_ll = (LinearLayout) findViewById(R.id.msgInform_ll);
		login_hint = (RelativeLayout) findViewById(R.id.login_hint);
		msg_unClick = findViewById(R.id.msg_unClick);
	}

	/**
	 * 第一次打开消息通知,设置默认开关
	 */
	private void init() {
		Map<String, String> msgMap = new HashMap<>();
		if (UtilFile.loadShared(getApplicationContext(), FileManager.msgInform, FileManager.quan) == "") {
			msgMap.put(FileManager.newMSG, "1");
			msgMap.put(FileManager.quan, "1");
			msgMap.put(FileManager.zhishi, "1");
			msgMap.put(FileManager.zan, "1");
			msgMap.put(FileManager.menu, "1");
			msgMap.put(FileManager.caipu, "1");
			msgMap.put(FileManager.informSing, "2");
			msgMap.put(FileManager.informShork, "2");
			UtilFile.saveShared(getApplicationContext(), FileManager.msgInform, msgMap);
		}
		doMsgShow();
	}
	
	/**
	 * 展示页面
	 */
	private void doMsgShow() {
		newMSG = (String) UtilFile.loadShared(getApplicationContext(), FileManager.msgInform, FileManager.newMSG);
		boolean isNotifi = PushManager.isNotificationEnabled(this);
		//Log.i("FRJ","isNotifi:" + isNotifi + ";   newMSG:" + newMSG);
		//判断总开关是否已关闭
		if (newMSG.equals("2")) { //2为关闭
			msgInform_ll.setVisibility(View.GONE);
			if(isNotifi){
				doMSGSetting(isNotifi,FileManager.newMSG);
				newMSG = "1";
				onChange(isNotifi);
				msgInform_ll.setVisibility(View.VISIBLE);
			}
		} else {
			msgInform_ll.setVisibility(View.VISIBLE);
			if(!isNotifi){
				doMSGSetting(isNotifi,FileManager.newMSG);
				newMSG = "2";
				onChange(isNotifi);
				msgInform_ll.setVisibility(View.GONE);
			}
		}
		quan = (String) UtilFile.loadShared(getApplicationContext(), FileManager.msgInform, FileManager.quan);
		nous = (String) UtilFile.loadShared(getApplicationContext(), FileManager.msgInform, FileManager.zhishi);
		zan = (String) UtilFile.loadShared(getApplicationContext(), FileManager.msgInform, FileManager.zan);
		jxMenu = (String) UtilFile.loadShared(getApplicationContext(), FileManager.msgInform, FileManager.menu);
		jxDish = (String) UtilFile.loadShared(getApplicationContext(), FileManager.msgInform, FileManager.caipu);
		informSing = (String) UtilFile.loadShared(getApplicationContext(), FileManager.msgInform, FileManager.informSing);
		informShork = (String) UtilFile.loadShared(getApplicationContext(), FileManager.msgInform, FileManager.informShork);

		msg_newMSG_sb.mSwitchOn = newMSG.equals("1");
		msg_subject_sb.mSwitchOn = quan.equals("1");
		msg_nous_sb.mSwitchOn = nous.equals("1");
		msg_quan_zan_sb.mSwitchOn = zan.equals("1");
		msg_jxMenu_sb.mSwitchOn = jxMenu.equals("1");
		msg_jxCaiPu_sb.mSwitchOn = jxDish.equals("1");
		msg_informSing_sb.mSwitchOn = informSing.equals("1");
		msg_informShork_sb.mSwitchOn = informShork.equals("1");

		msg_newMSG_sb.setViewState();
		msg_subject_sb.setViewState();
		msg_nous_sb.setViewState();
		msg_quan_zan_sb.setViewState();
		msg_jxMenu_sb.setViewState();
		msg_jxCaiPu_sb.setViewState();
		msg_informSing_sb.setViewState();
		msg_informShork_sb.setViewState();
	}

	private void onChange(boolean isOpen){
		String state = isOpen ? "1" : "2";
		Map<String, String> msgMap = new HashMap<>();
		msgMap.put(FileManager.newMSG, state);
		msgMap.put(FileManager.quan, state);
		msgMap.put(FileManager.zhishi, state);
		msgMap.put(FileManager.zan, state);
		msgMap.put(FileManager.menu, state);
		msgMap.put(FileManager.caipu, state);
		msgMap.put(FileManager.informSing, informSing);
		msgMap.put(FileManager.informShork, informShork);
		UtilFile.saveShared(getApplicationContext(), FileManager.msgInform, msgMap);
	}
	
	/**
	 * @param state 开关状态  true  false
	 * @param type  开关的类型,需要改变状态的开关
	 */
	private void doMSGSetting(final boolean state, final String type) {
//		String stateString  = state? "1" : "2";
		UtilFile.saveShared(getApplicationContext(), FileManager.msgInform, type, state ? "1" : "2");
//		String url = StringManager.apiUrl + "home5/setInfoSwitch?isc="+type+"&ope="+ stateString;
//		ReqInternet.doGet(url, new InternetCallback() {
//			
//			@Override
//			public void loaded(int flag, String url, Object returnObj) {
//				if (flag >= ReqInternet.REQ_OK_STRING) {
//					
//				}
//			}
//		});
	}
}
