package amodule.other.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;

import com.xianghatest.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.LoginManager;
import acore.override.activity.base.BaseActivity;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import acore.widget.DownRefreshList;
import amodule.other.adapter.AdapterActivity;
import amodule.user.activity.login.LoginByAccout;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;
import xh.basic.internet.UtilInternet;
import xh.basic.tool.UtilString;

public class ActivityList extends BaseActivity {
	private boolean LoadOver = false;
	// 加载管理
	private DownRefreshList list_acticity;
	private ArrayList<Map<String, String>> dataActicity;
	private AdapterActivity adapterActicity;
	private int currentPage = 0, everyPage = 0;
	private static Handler handler = null;
	private final int MSG_ACTIVITY_OK = 1;
	private TextView list_activity_text;//无活动时的提示语
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initActivity("美食专题", 2, 0, R.layout.c_view_bar_title, R.layout.a_xh_activity);
		init();
	}

	@SuppressLint("HandlerLeak")
	private void init() {
		TextView rightText = (TextView) findViewById(R.id.rightText);
		rightText.setVisibility(View.GONE);
		rightText.setText("我的积分");
		rightText.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(LoginManager.isLogin()){
					String url = StringManager.api_scoreList + "?code=" + LoginManager.userInfo.get("code");
					AppCommon.openUrl(ActivityList.this, url , true);
				}else{
					Intent intent=new Intent(ActivityList.this,LoginByAccout.class);
					startActivity(intent);
				}
			}
		});
		list_acticity = (DownRefreshList) findViewById(R.id.list_activity);
		list_activity_text = (TextView) findViewById(R.id.list_activity_text);
		list_acticity.setDivider(null);
		list_acticity.paddingBottom = 0;
		dataActicity = new ArrayList<>();
		adapterActicity = new AdapterActivity(this, list_acticity, dataActicity, 
				R.layout.a_xh_item_activity, 
				new String[] { "name", "img","time" ,"allClick"}, 
				new int[] { R.id.activity_name, R.id.activity_img, R.id.activity_time ,R.id.activity_PV});
		adapterActicity.scaleType = ScaleType.CENTER_CROP;
		int dp_10 =Tools.getDimen(this, R.dimen.dp_10);
		adapterActicity.imgWidth = ToolsDevice.getWindowPx(this).widthPixels - dp_10 * 2;
		adapterActicity.imgHeight = (ToolsDevice.getWindowPx(this).widthPixels - dp_10 * 2) * 250 / 640;
		adapterActicity.imgZoom = true;
		
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				int what = msg.what;
				switch (what) {
				case MSG_ACTIVITY_OK: // tab Hot数据加载完成;
					loadManager.hideProgressBar();
					break;
				}
			}
		};
		load();
	}

	private void load() {
		if (!LoadOver) {
			loadManager.showProgressBar();
			loadManager.setLoading(list_acticity, adapterActicity, true, new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					getData(false);
				}
			}, new OnClickListener() {
				@Override
				public void onClick(View v) {
					getData(true);
				}
			});
			LoadOver = true;
		}
	}

	/**
	 * 刷新按钮
	 * @param v
	 */
	public void onRefreshClick(View v) {
		// reloadByTabId(viewPager.getCurrentItem());
		getData(true);
	}

	private void getData(final boolean isForward) {
		if (isForward) {
			currentPage = 1;
		} else
			currentPage++;
		String getUrl = StringManager.api_activityList + "?type=all&page=" + currentPage;
		loadManager.changeMoreBtn(UtilInternet.REQ_OK_STRING, -1, -1, currentPage,dataActicity.size() == 0);

		ReqInternet.in().doGet(getUrl, new InternetCallback(this) {
			@Override
			public void loaded(int flag, String url, Object returnObj) {
				int loadCount = 0;
				if (flag >= UtilInternet.REQ_OK_STRING) {
					if(isForward) dataActicity.clear();
					// 解析数据
					ArrayList<Map<String, String>> listMap = UtilString.getListMapByJson(returnObj);
					for (int i = 0; i < listMap.size(); i++) {
						loadCount++;
						Map<String, String> map = new HashMap<>();
						map.put("name", listMap.get(i).get("name"));
						map.put("type", listMap.get(i).get("type"));
						map.put("img", listMap.get(i).get("img"));
						map.put("url", listMap.get(i).get("url"));
						map.put("state", listMap.get(i).get("state"));
						map.put("time", listMap.get(i).get("time"));
						if (listMap.get(i).containsKey("allClick")) {
							map.put("allClick", listMap.get(i).get("allClick")+"浏览");
						}
						dataActicity.add(map);
					}
					if (dataActicity.size() > 0) {
						list_activity_text.setVisibility(View.GONE);
					}else {
						list_activity_text.setVisibility(View.VISIBLE);
					}
					list_acticity.setVisibility(View.VISIBLE);
					adapterActicity.notifyDataSetChanged();
					handler.sendEmptyMessage(MSG_ACTIVITY_OK);
					// 如果是重新加载的,选中第一个tab.
					if (isForward){
						list_acticity.setSelection(1);
					}
				}

				if (everyPage == 0)
					everyPage = loadCount;
				currentPage = loadManager.changeMoreBtn(flag, everyPage, loadCount, currentPage,dataActicity.size() == 0);
				list_acticity.onRefreshComplete();
			}
		});
	}
}