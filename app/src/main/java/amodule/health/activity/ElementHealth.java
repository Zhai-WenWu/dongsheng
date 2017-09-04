package amodule.health.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.SimpleAdapter.ViewBinder;
import android.widget.TableLayout;
import android.widget.TextView;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.Map;

import acore.logic.SetDataView;
import acore.override.activity.base.BaseActivity;
import acore.override.adapter.AdapterSimple;
import acore.tools.StringManager;
import acore.tools.Tools;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;
import xh.basic.internet.UtilInternet;
import xh.basic.tool.UtilString;

/**
 * Title:ElementHealth.java Copyright: Copyright (c) 2014~2017
 * 微量元素高的食材列表
 * @author zeyu_t
 * @date 2014年10月14日
 */
public class ElementHealth extends BaseActivity {
	private TableLayout elementTable;
	private TextView element_info;
	
	private ArrayList<Map<String, String>> elementInfo = new ArrayList<Map<String, String>>();
	
	private String name = "", code = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			name = bundle.getString("name");
			code = bundle.getString("pinyin");
		}
		String title = "";
		if (name != null && name.lastIndexOf("(") > -1)
			title = name.substring(0, name.lastIndexOf("("));
		else
			title = name;
		initActivity("含" + title + "高的食物", 2, 0, R.layout.c_view_bar_title, R.layout.a_health_element);
		init();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		elementInfo.clear();
	}

	private void init() {
		findViewById(R.id.title).setVisibility(View.GONE);
		element_info = (TextView) findViewById(R.id.element_info_tv);
		elementTable = (TableLayout) findViewById(R.id.element_table);

		// 设置加载
		loadManager.setLoading(new OnClickListener() {

			@Override
			public void onClick(View v) {
				contentLoad();
			}
		});
	}

	private void contentLoad() {
		loadManager.showProgressBar();
		String url = StringManager.api_getIngreList + "?type=element&g1=" + code;
		ReqInternet.in().doGet(url, new InternetCallback(getApplicationContext()) {
			@Override
			public void loaded(int flag, String url, Object returnObj) {
				if (flag >= UtilInternet.REQ_OK_STRING) {
					ArrayList<Map<String, String>> classify = UtilString.getListMapByJson(returnObj);
					if(classify.size() > 0){
						((TextView)findViewById(R.id.title)).setText("含" + classify.get(0).get("name") + "高的食物");
						findViewById(R.id.title).setVisibility(View.VISIBLE);
						String info = classify.get(0).get("info");
						element_info.setText(Tools.getSegmentedStr(info));
						classify = UtilString.getListMapByJson(classify.get(0).get("classify"));
						for(Map<String, String> classifyMap : classify){
							ArrayList<Map<String, String>> ingre = UtilString.getListMapByJson(classifyMap.get("ingre"));
							int j = 0;
							for(Map<String, String> ingreMap : ingre){
								ingreMap.put("classifyName", j++ == 0 ? classifyMap.get("name") : "hide");
								elementInfo.add(ingreMap);
							}
						}
						loadManager.hideProgressBar();
						setTableData(elementInfo);
					}
				} else
					findViewById(R.id.health_no_data).setVisibility(View.VISIBLE);
			}
		});
	}

	private void setTableData(ArrayList<Map<String, String>> infoList) {
		// 列表加载
		elementTable.setVisibility(View.VISIBLE);
		AdapterSimple adapter = new AdapterSimple(elementTable, infoList, 
				R.layout.a_health_item_element, 
				new String[] { "classifyName","img", "name", "content" }, 
				new int[] { R.id.element_classify_tv, R.id.element_img, R.id.element_name_tv,R.id.element_content });
		adapter.setViewBinder(new ViewBinder() {
			@Override
			public boolean setViewValue(View view, Object data, String textRepresentation) {
				if(view == null || data == null){
					return false;
				}
				int id = view.getId();
				switch(id){
				case R.id.element_content:
					String text = data.toString();
					SpannableStringBuilder style = new SpannableStringBuilder(text);
					// 设置指定位置文字的颜色
					style.setSpan(new ForegroundColorSpan(Color.parseColor("#999999")), text.indexOf("/"), text.length(),
							Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
					((TextView)view).setText(style);
					return true;
				}
				return false;
			}
		});
		SetDataView.view(elementTable, 1, adapter, new int[] { R.id.element_name_tv },
				new SetDataView.ClickFunc[] { new SetDataView.ClickFunc() {
					@Override
					public void click(int index, View v) {
						Map<String, String> ingre = elementInfo.get(index);
						Intent intent = new Intent(ElementHealth.this, DetailIngre.class);
						intent.putExtra("name", ingre.get("name"));
						intent.putExtra("code", ingre.get("code"));
						intent.putExtra("form", "养生宜吃");
						startActivity(intent);
					};
				} });
		findViewById(R.id.health_no_data).setVisibility(View.VISIBLE);
		loadManager.hideProgressBar();
	}
}
