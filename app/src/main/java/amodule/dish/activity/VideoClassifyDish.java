package amodule.dish.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import xh.basic.internet.UtilInternet;
import xh.basic.tool.UtilString;
import acore.override.activity.base.BaseActivity;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.widget.DownRefreshList;
import amodule.dish.adapter.AdapterClassifyDish;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;

import com.xiangha.R;

public class VideoClassifyDish extends BaseActivity {
	
	private DownRefreshList lv_sur;
	private AdapterClassifyDish adapter;
	private ArrayList<Map<String, String>> listDataMySuro;
	
	private int currentPage = 0, everyPage = 0;
	
	private String code = "",name="";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			name = bundle.getString("name");
			code = bundle.getString("id");
		}
		initActivity(name + "分类视频", 2, 0, R.layout.c_view_bar_title, R.layout.a_dish_classify);
		init();
		initData();
	}
	
	private void init(){
		lv_sur = (DownRefreshList)findViewById(R.id.lv_sur);
		lv_sur.setDivider(null);
		listDataMySuro = new ArrayList<Map<String, String>>();
		adapter = new AdapterClassifyDish(this, lv_sur, listDataMySuro, 0, null, null);
	}
	
	/*
	 * 加载数据,false加载  true 更新
	 */
	private void initData() {
		loadManager.showProgressBar();
		loadManager.setLoading(lv_sur, adapter, true, new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				loadFromServer(false);
			}
		}, new OnClickListener() {

			@Override
			public void onClick(View v) {
				loadFromServer(true);
			}
		});
	}
	
	/**
	 * 获取网络数据
	 * @param isForward 是否是向上加载
	 */
	private void loadFromServer(final boolean isForward) {
		// 向上加载/加载上一页.
		if (isForward) {
			currentPage = 1;
		}
		// 向下加载;
		else {
			currentPage ++;
		}
		loadManager.changeMoreBtn(UtilInternet.REQ_OK_STRING, -1, -1, currentPage,listDataMySuro.size() == 0);
		String getUrl = StringManager.api_getVideoClassifyDish + "?code=" + code + "&page=" + currentPage;
		ReqInternet.in().doGet(getUrl, new InternetCallback() {
			@Override
			public void loaded(int flag, String url, Object returnObj) {
				int loadCount = 0;
				if(currentPage == 1){
//					progressBar.setVisibility(View.GONE);
					lv_sur.setVisibility(View.VISIBLE);
				}
				if (flag >= UtilInternet.REQ_OK_STRING) {
					if(isForward) listDataMySuro.clear();
					loadCount = parseInfo(returnObj);
					adapter.notifyDataSetChanged();
				}
				if (everyPage == 0)
					everyPage = loadCount;
				currentPage = loadManager.changeMoreBtn(flag, everyPage, loadCount, currentPage,listDataMySuro.size() == 0);
				lv_sur.onRefreshComplete();
			}
		});
	}
	
	private int parseInfo(Object returnObj) {
		ArrayList<Map<String, String>> listMySelf = UtilString.getListMapByJson(returnObj);
		for(int i = 0; i < listMySelf.size(); i += 2){
			Map<String, String> m = new HashMap<String, String>();
			Map<String, String> leftMap = listMySelf.get(i);
			if(!leftMap.containsKey("hasVideo")) leftMap.put("hasVideo", "1");
			String left = Tools.map2Json(leftMap);
			m.put("left", left);
			if(listMySelf.size() > i + 1){
				Map<String, String> rightMap = listMySelf.get(i+1);
				if(!rightMap.containsKey("hasVideo")) rightMap.put("hasVideo", "1");
				String right = Tools.map2Json(rightMap);
				m.put("right", right);
			}
			listDataMySuro.add(m);
		}
		return listMySelf.size();
	}

}
