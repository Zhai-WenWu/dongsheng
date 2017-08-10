package amodule.health.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TextView;

import com.xianghatest.R;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import acore.logic.LoginManager;
import acore.logic.SetDataView;
import acore.logic.XHClick;
import acore.override.activity.base.BaseActivity;
import acore.tools.FileManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import amodule.health.adapter.HealthTestAdapter;
import amodule.user.activity.login.LoginByAccout;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;
import xh.basic.tool.UtilFile;
import xh.basic.tool.UtilString;

public class HealthTest extends BaseActivity {
	private ScrollView health_test_scroll;
	private TableLayout health_test_table;
	private RelativeLayout sex_select_layout;
	private LinearLayout sex_selection_layout;
	private TextView health_sex_selection_1, health_sex_selection_2, sex_answer, sex_text, sex_num;
	private Button health_test_submit;
	
	private Handler handler;
	private HealthTestAdapter adapter;
	// 上传使用的参数
	private Map<String, String> mapParams;
	private ArrayList<Map<String, String>> testList = new ArrayList<Map<String, String>>();

	private static final int ONREFREUSH = 1;
	private int screenWidth = 0, screenHight = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initActivity("体质测试", 2, 0, R.layout.c_view_bar_title, R.layout.a_health_test);
		handler = new Handler(new Callback() {
			@Override
			public boolean handleMessage(Message msg) {
				switch (msg.what) {
				case ONREFREUSH:
					onRefresh();
					break;
				}
				return false;
			}
		});
		loadManager.showProgressBar();
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				initData();
				initView();
				init();
			}
		}, 100);
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		tijiaoTest(true);
	}

	@Override
	public void onBackPressed() {
		//统计
		XHClick.onEventValue(this , "constitution315" , "constitution" , mapParams.size()+"" , mapParams.size());
		super.onBackPressed();
	}

	private void initData() {
		screenWidth = ToolsDevice.getWindowPx(this).widthPixels;
		screenHight = ToolsDevice.getWindowPx(this).heightPixels;
		String answer_map = UtilFile.readFile(UtilFile.getDataDir() + FileManager.file_constitution);
		answer_map = answer_map.replace("{", "").replace("}", "");
		if (!answer_map.equals(""))
			mapParams = UtilString.getMapByString(answer_map,",", "=");
		else
			mapParams = new LinkedHashMap<String, String>();
		// 数据加载
		String json = UtilFile.getFromAssets(HealthTest.this, FileManager.file_healthQuestion);
		if (!json.equals("")) {
			ArrayList<Map<String, String>> list = UtilString.getListMapByJson(json);
			json = null;
			for (int i = 0; i < list.size(); i++) {
				Map<String, String> map = list.get(i);
				ArrayList<Map<String, String>> listQuestion = UtilString.getListMapByJson(map.get("question"));
				for (int j = 0; j < listQuestion.size(); j++) {
					Map<String, String> mapQuestion = listQuestion.get(j);
					if (j == 0)
						mapQuestion.put("classifyName", map.get("name"));
					String num = mapQuestion.get("num");
					String id = mapQuestion.get("id");

					mapQuestion.put("num", num.length() == 1 ? num + "  、" : num + "、");
					mapQuestion.put("id", id);
					if (mapParams.containsKey(id))
						mapQuestion.put("selected", mapParams.get(id));
					mapQuestion.put("name", mapQuestion.get("name"));
					mapQuestion.put("sex", mapQuestion.get("sex"));
					testList.add(mapQuestion);
				}
			}
		}
	}
	
	private void initView() {
		// title初始化
		((TextView) findViewById(R.id.rightText)).setText("重置");
		findViewById(R.id.rightText).setVisibility(View.VISIBLE);

		health_test_scroll = (ScrollView) findViewById(R.id.health_test_scroll);
		sex_select_layout = (RelativeLayout) findViewById(R.id.health_sex_select_question_layout);
		sex_selection_layout = (LinearLayout) findViewById(R.id.health_sex_selection_layout);
		health_test_table = (TableLayout) findViewById(R.id.health_test_table);
		sex_text = (TextView) findViewById(R.id.health_sex_select_question_text);
		sex_num = (TextView) findViewById(R.id.health_sex_select_question_num);
		health_sex_selection_1 = (TextView) findViewById(R.id.health_sex_selection_1);
		health_sex_selection_2 = (TextView) findViewById(R.id.health_sex_selection_2);
		sex_answer = (TextView) findViewById(R.id.health_sex_select_answer);
		health_test_submit = (Button) findViewById(R.id.health_test_submit);
	}

	private void init() {
		if (adapter == null) {
			adapter = new HealthTestAdapter(health_test_table, testList, 
					R.layout.a_health_test_item, 
					new String[] { "classifyName", "num","name" }, 
					new int[] { R.id.health_item_select_classify_title, R.id.health_item_select_question_num,R.id.health_item_select_question_text });
			adapter.viewHeight = (screenWidth - Tools.getDimen(HealthTest.this, R.dimen.dp_80)) / 5;//80=15*2+25+5*5
		}
		SetDataView.view(health_test_table, 1, adapter, 
				new int[] { R.id.health_item_select_question_layout, R.id.health_item_selection_1,R.id.health_item_selection_2, 
					R.id.health_item_selection_3, R.id.health_item_selection_4, R.id.health_item_selection_5 },
				new SetDataView.ClickFunc[] { new SetDataView.ClickFunc() {
					@Override
					public void click(int index, View view) {
						String answer = "";
						if (index == 0)
							answer = sex_answer.getText().toString();
						else {
							TextView textView = (TextView) health_test_table.getChildAt(index - 1).findViewById(R.id.health_item_select_answer);
							answer = textView.getText().toString();
						}
						if (answer.equals("")) {
						}
						else {
							LinearLayout selection_layout = (LinearLayout) view.findViewById(R.id.health_selection_layout);
							if (selection_layout.getVisibility() == View.VISIBLE)
								selection_layout.setVisibility(View.GONE);
							else if (selection_layout.getVisibility() == View.GONE)
								selection_layout.setVisibility(View.VISIBLE);
						}
					}
				}, new SetDataView.ClickFunc() {

					@Override
					public void click(int index, View view) {
						LinearLayout selection_layout = (LinearLayout) view.getParent();
						selection_layout.setVisibility(View.GONE);
						RelativeLayout relativeLayout = (RelativeLayout) selection_layout.getParent();
						TextView textView = (TextView) relativeLayout.findViewById(R.id.health_item_select_answer);
						switch (view.getId()) {
						case R.id.health_item_selection_1:
							setNextSeletion(index, 1, selection_layout, textView);
							break;
						case R.id.health_item_selection_2:
							setNextSeletion(index, 2, selection_layout, textView);
							break;
						case R.id.health_item_selection_3:
							setNextSeletion(index, 3, selection_layout, textView);
							break;
						case R.id.health_item_selection_4:
							setNextSeletion(index, 4, selection_layout, textView);
							break;
						case R.id.health_item_selection_5:
							setNextSeletion(index, 5, selection_layout, textView);
							break;
						}
					}
				} });
		setListener();
		if (mapParams.containsKey("0")) {
			if (mapParams.get("0").equals("1"))
				setSexMale();
			else
				setSexFemale();
			sex_selection_layout.setVisibility(View.GONE);
			sex_text.setTextColor(Color.parseColor("#999999"));
			sex_num.setTextColor(Color.parseColor("#999999"));
		} else
			health_test_table.getChildAt(0).findViewById(R.id.health_selection_layout).setVisibility(View.GONE);
		health_test_scroll.setVisibility(View.VISIBLE);
		for (int i = 0; i < testList.size(); i++) {
			final int index = i;
			if (!mapParams.containsKey(testList.get(i).get("id")) && mapParams.size() != 0) {
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						int[] location = new int[2];
						health_test_table.getChildAt(index).getLocationInWindow(location);
						int y = location[1];
						health_test_scroll.smoothScrollBy(0, y - screenHight / 3);
					}
				}, 300);
				break;
			}
		}
		loadManager.hideProgressBar();
	}

	private void setNextSeletion(final int position, int count, LinearLayout selection_layout, TextView textView) {
		String[] ANSWERS = { "", "没有", "很少", "有时", "经常", "总是" };
		textView.setText(ANSWERS[count]);
		selection_layout.setVisibility(View.GONE);
		selection_layout.getChildAt(count - 1).setSelected(true);
		if (position + 1 < health_test_table.getChildCount()) {
			health_test_table.getChildAt(position + 1).findViewById(R.id.health_selection_layout).setVisibility(View.VISIBLE);
			if (position == health_test_table.getChildCount() - 3)
				health_test_table.getChildAt(position + 2).findViewById(R.id.health_selection_layout).setVisibility(View.VISIBLE);
		}
		final LinearLayout question_layout = (LinearLayout) health_test_table.getChildAt(position).findViewById(R.id.health_selection_layout);
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				int[] location = new int[2];
				question_layout.getLocationInWindow(location);
				int y = location[1];
				if (position + 1 < health_test_table.getChildCount()){
					RelativeLayout classify_layout=(RelativeLayout) health_test_table.getChildAt(position+1).findViewById(R.id.health_item_select_classify);
					if(classify_layout.getVisibility()==View.VISIBLE)
						health_test_scroll.smoothScrollBy(0, y - screenHight / 3 + (Tools.getMeasureHeight(classify_layout)+Tools.getDimen(HealthTest.this, R.dimen.dp_40)));
					else
						health_test_scroll.smoothScrollBy(0, y - screenHight/ 3);
				}else
					health_test_scroll.smoothScrollBy(0, y - screenHight / 3);
			}
		}, 100);
		
		RelativeLayout parentLayout = (RelativeLayout) question_layout.getParent();
		RelativeLayout grandLayout = (RelativeLayout) parentLayout.getParent();
		TextView title = (TextView) parentLayout.findViewById(R.id.health_item_select_question_text);
		TextView question_num = (TextView) grandLayout.findViewById(R.id.health_item_select_question_num);
		
		title.setTextColor(Color.parseColor("#999999"));
		question_num.setTextColor(Color.parseColor("#999999"));
		for (int i = 1; i < 6; i++) {
			LinearLayout answer_layout = (LinearLayout) selection_layout.getChildAt(i - 1);
			TextView text1 = (TextView) answer_layout.getChildAt(0);
			TextView text2 = (TextView) answer_layout.getChildAt(1);
			if (count == i) {
				answer_layout.setBackgroundResource(R.drawable.bg_round_green_test);
				text1.setTextColor(Color.WHITE);
				text2.setTextColor(Color.WHITE);
			} else {
				answer_layout.setBackgroundResource(R.drawable.bg_btn_test_selection);
				text1.setTextColor(Color.parseColor("#333333"));
				text2.setTextColor(Color.BLACK);
			}
		}
		saveAnswer(true, testList.get(position).get("id"), String.valueOf(count));
	}

	private void setListener() {
		sex_select_layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (sex_selection_layout.getVisibility() == View.VISIBLE) {
					sex_text.setTextColor(Color.parseColor("#999999"));
					sex_num.setTextColor(Color.parseColor("#999999"));
					sex_selection_layout.setVisibility(View.GONE);
				} else {
					sex_text.setTextColor(Color.parseColor("#333333"));
					sex_num.setTextColor(Color.parseColor("#333333"));
					sex_selection_layout.setVisibility(View.VISIBLE);
				}
			}
		});
		health_sex_selection_1.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setSexSelection(true);
			}
		});
		health_sex_selection_2.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setSexSelection(false);
			}
		});
		health_test_submit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				tijiaoTest(false);
			}
		});
		findViewById(R.id.rightText).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				handler.sendEmptyMessage(ONREFREUSH);
			}
		});
	}

	// 设置性别选择 控制最后一道题的显示
	private void setSexSelection(boolean flag) {
		sex_selection_layout.setVisibility(View.GONE);
		sex_text.setTextColor(Color.parseColor("#999999"));
		sex_num.setTextColor(Color.parseColor("#999999"));
		if (flag)
			setSexMale();
		else
			setSexFemale();
		health_test_table.getChildAt(0).findViewById(R.id.health_selection_layout).setVisibility(View.VISIBLE);
		saveAnswer(flag, "0", null);
	}

	// 保存答案
	private void saveAnswer(boolean flag, String id, String answer) {
		if (answer == null)
			mapParams.put(id, flag ? "1" : "2");
		else{
			mapParams.put(id, answer);
		}
		UtilFile.delDirectoryOrFile(UtilFile.getDataDir() + FileManager.file_constitution);
		UtilFile.saveFileToCompletePath(UtilFile.getDataDir() + FileManager.file_constitution, mapParams.toString(), false);
	}

	private void setSexFemale() {
		sex_answer.setText(health_sex_selection_2.getText());
		health_sex_selection_1.setBackgroundResource(R.drawable.bg_btn_test_selection);
		health_sex_selection_1.setTextColor(Color.parseColor("#333333"));
		health_sex_selection_2.setBackgroundResource(R.drawable.bg_round_green_test);
		health_sex_selection_2.setTextColor(Color.WHITE);
		if(health_test_table.getChildCount()>2){
			if(health_test_table.getChildAt(health_test_table.getChildCount() - 1) != null)
				health_test_table.getChildAt(health_test_table.getChildCount() - 1).setVisibility(View.GONE);
			if(health_test_table.getChildAt(health_test_table.getChildCount() - 2) != null)
				health_test_table.getChildAt(health_test_table.getChildCount() - 2).setVisibility(View.VISIBLE);
		}
	}

	private void setSexMale() {
		sex_answer.setText(health_sex_selection_1.getText());
		health_sex_selection_1.setBackgroundResource(R.drawable.bg_round_green_test);
		health_sex_selection_1.setTextColor(Color.WHITE);
		health_sex_selection_2.setBackgroundResource(R.drawable.bg_btn_test_selection);
		health_sex_selection_2.setTextColor(Color.parseColor("#333333"));
		if(health_test_table.getChildCount()>2){
			if(health_test_table.getChildAt(health_test_table.getChildCount() - 1) != null)
				health_test_table.getChildAt(health_test_table.getChildCount() - 1).setVisibility(View.VISIBLE);
			if(health_test_table.getChildAt(health_test_table.getChildCount() - 2) != null)
				health_test_table.getChildAt(health_test_table.getChildCount() - 2).setVisibility(View.GONE);
		}
	}

	private void onRefresh() {
		loadManager.showProgressBar();
		UtilFile.delDirectoryOrFile(UtilFile.getDataDir() + FileManager.file_constitution);
		if(health_test_table.getChildCount()>0)
			health_test_table.removeAllViews();
		testList.clear();
		mapParams.clear();
		health_test_scroll.setVisibility(View.GONE);
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				sex_answer.setText("");
				sex_selection_layout.setVisibility(View.VISIBLE);
				health_sex_selection_1.setBackgroundResource(R.drawable.bg_btn_test_selection);
				health_sex_selection_1.setTextColor(Color.parseColor("#333333"));
				health_sex_selection_2.setBackgroundResource(R.drawable.bg_btn_test_selection);
				health_sex_selection_2.setTextColor(Color.parseColor("#333333"));
				sex_text.setTextColor(Color.parseColor("#333333"));
				sex_num.setTextColor(Color.parseColor("#333333"));
				health_test_scroll.smoothScrollTo(0, 0);
				initData();
				initView();
				init();
				loadManager.hideProgressBar();
			}
		}, 100);
	}
	
	private void tijiaoTest(boolean isAuto) {
		if (mapParams.size() >= 61) {
			String crowd = LoginManager.userInfo.get("crowd");
			if (LoginManager.isLogin()) {
				if(isAuto && !TextUtils.isEmpty(crowd)){
					return;
				}
				LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
				map.put("testQue[0]", "0:" + mapParams.get("0"));
				Iterator<Map.Entry<String, String>> it = mapParams.entrySet().iterator();
				int i = 1;
				while (it.hasNext()) {
					Map.Entry<String, String> entry = it.next();
					map.put("testQue[" + i++ + "]", entry.getKey() + ":" + entry.getValue());
				}
				Tools.showToast(HealthTest.this, "正在提交，请稍候~");
				ReqInternet.in().doPost(StringManager.api_setHealthTest, map, new InternetCallback(getApplicationContext()) {
					@Override
					public void loaded(int flag, String url, Object returnObj) {
						if(flag > 1){
							UtilFile.saveFileToCompletePath(UtilFile.getDataDir() + FileManager.file_healthResult, returnObj.toString(), false);
							Intent intent = new Intent(HealthTest.this, MyPhysique.class);
							intent.putExtra("params", returnObj.toString());
							startActivity(intent);
							finish();
						}
					}
				});
			} else {
				Intent intent=new Intent(HealthTest.this,LoginByAccout.class);
				startActivity(intent);
			}
		}else
			Tools.showToast(this, "请完成所有测试题后，再提交答案~");
	}
}
