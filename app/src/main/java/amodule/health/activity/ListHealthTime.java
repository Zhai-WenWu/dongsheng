package amodule.health.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextPaint;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TextView;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import acore.logic.AppCommon;
import acore.logic.SetDataView;
import acore.logic.XHClick;
import acore.override.activity.base.BaseActivity;
import acore.override.adapter.AdapterSimple;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import acore.widget.LayoutScroll;
import amodule.dish.activity.DetailDish;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;
import aplug.feedback.activity.Feedback;
import third.share.BarShare;
import xh.basic.internet.UtilInternet;
import xh.basic.tool.UtilString;

/**
 * Title:ListHealthTime.java Copyright: Copyright (c) 2014~2017
 * 
 * @author zeyu_t
 * @date 2014年10月14日
 */
@SuppressLint({ "ResourceAsColor", "NewApi" })
public class ListHealthTime extends BaseActivity {
	private TableLayout tableLayout;
	private TextView tv_food, health_time_tv, health_time_state;
	private TextView time;
	private HorizontalScrollView time_scrollView;
	private LinearLayout ll_time = null;
	private LinearLayout tv_info;
	
	private TimerTask task = null;
	private Timer timer = null;
	private Handler handler = null;
	private AdapterSimple timeAdapter;
	private ArrayList<Map<String, String>> listData;
	private ArrayList<Map<String, String>> showData;
	private ArrayList<Map<String, String>> timeList;

	public int hour;
	public String name = "", code = "", ico_id = "";
	private String[] allDatas = new String[] { "子时", "丑时", "寅时", "卯时", "辰时", "巳时", "午时", "未时", "申时", "酉时", "戌时", "亥时" };
	private String[] allTimes = new String[] { "23:00-1:00", "1:00-3:00", "3:00-5:00", "5:00-7:00", "7:00-9:00", "9:00-11:00",
			"11:00-13:00", "13:00-15:00", "15:00-17:00", "17:00-19:00", "19:00-21:00", "21:00-23:00" };
	private String[] allStates = new String[] { "养胆", "养肝", "养肺", "养大肠", "养胃", "养脾", "养心", "养小肠", "养膀胱", "养肾", "养心包", "养三焦" };
	public LayoutScroll scrollLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			name = bundle.getString("name");
			ico_id = bundle.getString("ico_id");
			if (bundle.containsKey("code"))
				code = bundle.getString("code");
		}
		if (code.equals("") && name.equals(""))
			hour = getNowHour();
		else
			for (int i = 0; i < allDatas.length; i++) {
				if (code.equals(allDatas[i]) || name.equals(allDatas[i]))
					hour = i;
			}
		initActivity(name, 2, 0, R.layout.c_view_bar_title_time, R.layout.a_health_time_main);
		initBarView();
		time_scrollView = (HorizontalScrollView) findViewById(R.id.health_time_HScrollView);
		ll_time = (LinearLayout) findViewById(R.id.ll_time);

		tv_info = (LinearLayout) findViewById(R.id.tv_info);
		tv_food = (TextView) findViewById(R.id.tv_food);
		health_time_tv = (TextView) findViewById(R.id.health_time_tv);
		health_time_state = (TextView) findViewById(R.id.health_time_state);
		time = (TextView) findViewById(R.id.title_time);
		time.setVisibility(View.VISIBLE);
		setDates(hour);
		setTimer();

		loadManager.setLoading(new OnClickListener() {
			@Override
			public void onClick(View v) {
				loadData();
			}
		});
		scrollLayout = (LayoutScroll)findViewById(R.id.scroll_body);
		// 设置滚动相关
		new Handler().postDelayed(new Runnable() {
			@Override 
			public void run() {
				int searchHeight = (int) getResources().getDimension(R.dimen.dp_46);
				int scrollHeight=getWindow().findViewById(Window.ID_ANDROID_CONTENT).getHeight() ;
				scrollLayout.setLayoutParams(new LinearLayout.LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT,scrollHeight));
				scrollLayout.init(searchHeight);
			}
		},100);
		ScrollView scrollView1=(ScrollView) findViewById(R.id.scrollView1);
		scrollLayout.setTouchView(scrollView1);
	}
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		finish();
	}
	private void initBarView() {
		// 分享功能
		ImageView rightBtn = (ImageView) findViewById(R.id.rightImgBtn2);
		rightBtn.setVisibility(View.VISIBLE);
		rightBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				XHClick.mapStat(ListHealthTime.this, "a_share400", "养生", "时辰养生");
				barShare = new BarShare(ListHealthTime.this, "时辰养生","养生");
				String type = BarShare.IMG_TYPE_RES;
				String title = "【" + name + "】中医推荐";
				String clickUrl = StringManager.third_downLoadUrl;
				String content = "我在用香哈菜谱【时辰养生】，中医推荐的养生方法，推荐你也试试~";
				String imgUrl = null;
				if (ico_id != null && !ico_id.equals(""))
					imgUrl = ico_id;
				else
					imgUrl = R.drawable.share_launcher + "";
				barShare.setShare(type, title, content, imgUrl, clickUrl);
				barShare.openShare();
			}
		});
		
//		slideview=(SlideViewHide) findViewById(R.id.slideview);
//		new Handler().postDelayed(new Runnable() {
//			
//			@Override
//			public void run() {
//				int scrollHeight=getWindow().findViewById(Window.ID_ANDROID_CONTENT).getHeight() - 132;
//				slideview.setLayoutParams(new RelativeLayout.LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT,scrollHeight));
//				slideview.initView(ListHealthTime.this);
//			}
//		}, 100);
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		new Handler().post(new Runnable() {
			@Override
			public void run() {
				time_scrollView.smoothScrollTo(0, 0);
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				if (ll_time != null && ll_time.getChildAt(hour) != null) {
					LinearLayout layout = (LinearLayout) ll_time.getChildAt(hour).findViewById(R.id.time_layout);
					layout.setBackgroundResource(R.drawable.tv_circle_green);
					((TextView) layout.getChildAt(0)).setTextColor(Color.parseColor("#FFFFFF"));
					((TextView) layout.getChildAt(1)).setTextColor(Color.parseColor("#FFFFFF"));
					// ImageView default_img = (ImageView)
					// ll_time.getChildAt(hour).findViewById(R.id.default_img);
					// default_img.setBackgroundResource(R.drawable.tv_circle_green);
					int[] location = new int[2];
					layout.getLocationOnScreen(location);
					int x = location[0];
					int xTo = x + layout.getWidth() / 2 - ToolsDevice.getWindowPx(ListHealthTime.this).widthPixels / 2;
					time_scrollView.smoothScrollTo(xTo, 0);
				}
			}
		}, 500);
	}

	public void loadData() {
		timeList = new ArrayList<Map<String, String>>();
		for (int i = 0; i < allDatas.length; i++) {
			Map<String, String> map = new HashMap<String, String>();
			map.put("data", allDatas[i]);
			map.put("state", allStates[i]);
			timeList.add(map);
		}
		ll_time.removeAllViews();
		timeAdapter = new AdapterSimple(time_scrollView, timeList, 
				R.layout.a_health_item_default, 
				new String[] { "data", "state" },
				new int[] { R.id.tv_time, R.id.tv_state });
		timeAdapter.viewWidth = (ToolsDevice.getWindowPx(this).widthPixels - 24) / 7;
		timeAdapter.viewHeight = (ToolsDevice.getWindowPx(this).widthPixels - 24) / 7-Tools.getDimen(this, R.dimen.dp_9);//9=4.5*2
		SetDataView.ClickFunc[] timeClick = { new SetDataView.ClickFunc() {

			@Override
			public void click(int index, View v) {
				hour = index;
				String getUrl = StringManager.api_nousList + "?type=" + "whatHour&name=" + allDatas[hour];
				getData(getUrl, hour);
				for (int i = 0; i < 12; i++) {
					LinearLayout layout = (LinearLayout) ll_time.getChildAt(i).findViewById(R.id.time_layout);
					if (i == hour) {
						layout.setBackgroundResource(R.drawable.tv_circle_green);
						((TextView) layout.getChildAt(0)).setTextColor(Color.parseColor("#FFFFFF"));
						((TextView) layout.getChildAt(1)).setTextColor(Color.parseColor("#FFFFFF"));
					} else {
						layout.setBackgroundColor(android.R.color.transparent);
						((TextView) layout.getChildAt(0)).setTextColor(Color.parseColor("#000000"));
						((TextView) layout.getChildAt(1)).setTextColor(Color.parseColor("#000000"));
					}
				}
			}
		} };
		SetDataView.horizontalView(time_scrollView, timeAdapter, null, timeClick);
		listData = new ArrayList<Map<String, String>>();
		showData = new ArrayList<Map<String, String>>();
		tableLayout = (TableLayout) findViewById(R.id.tb_ingredish);
		String getUrl = "";
		if (!code.equals(""))
			getUrl = StringManager.api_nousList + "?type=whatHour&name=" + code;
		else if (!name.equals(""))
			getUrl = StringManager.api_nousList + "?type=whatHour&name=" + allDatas[hour];
		else
			getUrl = StringManager.api_nousList + "?type=" + "whatHour";
		getData(getUrl, hour);
	}

	private void getData(String getUrl, final int index) {
		loadManager.showProgressBar();
		ReqInternet.in().doGet(getUrl, new InternetCallback(this.getApplicationContext()) {
			@Override
			public void loaded(int flag, String url, Object returnObj) {
				if (flag >= UtilInternet.REQ_OK_STRING) {
					// 配置数据
					health_time_tv.setText(allDatas[index]);
					health_time_tv.setPadding(0, ToolsDevice.dp2px(ListHealthTime.this, 1), 0, 0);
					health_time_tv.getPaint().setFakeBoldText(true);
					// if (allStates[index].length() == 2)
					// health_time_state.setLineSpacing(2, 1);
					// else
					// health_time_state.setLineSpacing(2, 0.8f);
					health_time_state.setText(allStates[index]);
					Map<String, String> mapReturn = UtilString.getListMapByJson(returnObj).get(0);
//					String data = mapReturn.get("name");
					setDates(index);
					String[] infos = new String[2];
					infos[0] = mapReturn.get("subtitle");
					infos[1] = mapReturn.get("info");
					tv_info.removeAllViews();
					for (int i = 0; i < 2; i++) {
						TextView tv = new TextView(ListHealthTime.this);
						LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT,
								android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
						tv.setTextColor(Color.parseColor("#333333"));
						tv.setTextSize(Tools.getDimenSp(ListHealthTime.this, R.dimen.sp_15));
						tv.setText(infos[i]);
						int dp_20 = Tools.getDimen(ListHealthTime.this, R.dimen.dp_20);
						if (i == 0) {
							tv.setPadding(dp_20 , 0 , 0 , dp_20 );
							TextPaint tp = tv.getPaint();
							tp.setFakeBoldText(true);
						} else {
							tv.setPadding(0, 0, 0, dp_20);
							tv.setLineSpacing(Tools.getDimen(ListHealthTime.this, R.dimen.dp_5), 1);
						}
						tv_info.addView(tv, lp);
					}
					ArrayList<Map<String, String>> listReturn = UtilString.getListMapByJson(mapReturn.get("nous"));
					listData.clear();
					for (int i = 0; i < listReturn.size(); i++) {
						Map<String, String> map = listReturn.get(i);
						listData.add(map);
					}
					load();
				} else{
					toastFaildRes(flag,false,returnObj);
				}
				loadManager.hideProgressBar();
				timeAdapter.notifyDataSetChanged();
				findViewById(R.id.scrollView1).setVisibility(View.VISIBLE);
				loadManager.loadOver(flag, 1,true);
			}
		});
	}

	public void load() {
		// 结果显示
		tableLayout.removeAllViews();
		showData.clear();
		for (int i = 0; i < listData.size(); i++) {
			Map<String, String> map = listData.get(i);
			map.put("num", i + "");
			showData.add(listData.get(i));
		}
		// 向tableView添加行
		AdapterSimple simpleAdapter = new AdapterSimple(tableLayout, showData,
				R.layout.a_health_item_time, 
				new String[] { "img", "title", "content", "num" }, 
				new int[] { R.id.iv_img, R.id.tv_title, R.id.tv_content, R.id.itemNum });
		simpleAdapter.scaleType = ScaleType.CENTER_CROP;
		// 单击GridView项的监听回调
		SetDataView.ClickFunc[] clicker = new SetDataView.ClickFunc[] { new SetDataView.ClickFunc() {
			@Override
			public void click(int index, View view) {
				TextView text = (TextView) view.findViewById(R.id.itemNum);
				if (text.getText().length() > 0) {
					index = Integer.parseInt(text.getText().toString());
					String url = "nousInfo.app?code=" + listData.get(index).get("code");
					AppCommon.openUrl(ListHealthTime.this, url , true);
				}
			}
		} };
		SetDataView.view(tableLayout, 1, simpleAdapter, new int[] { R.id.search_fake_layout }, clicker);
	}

	private void setDates(int hour) {
		tv_food.setText(allDatas[hour] + "养生知识");
		TextView titleV = (TextView) findViewById(R.id.title);
		name = allDatas[hour] + "养生";
		titleV.setText(name);
		for (int i = 0; i < allDatas.length; i++) {
			if (allDatas[i].equals(allDatas[hour])) {
				// Log.i("xiangha_log", allTimes[i]);
				time.setText(allTimes[i]);
			}
		}
	}

	public static int getNowHour() {
		String[] time = Tools.getAssignTime("HH:mm:ss", 0).split(":");
		int hour = Integer.parseInt(time[0]);
		// 根据当前小时得到数组下标
		int flag = hour / 2 + hour % 2;
		if (flag == 12) flag = 0;
		return flag;
	}

	//设置定时器
	@SuppressLint("HandlerLeak")
	private void setTimer() {
		String[] time = Tools.getAssignTime("HH:mm:ss", 0).split(":");
		int secon = 60 - Integer.parseInt(time[2]);
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				String[] time = Tools.getAssignTime("HH:mm:ss", 0).split(":");
				int hour = Integer.parseInt(time[0]);
				int flag = getNowHour();
				// 时辰改变则重开页面
				if (!allDatas[flag].equals(allDatas[(hour == 23 ? 0 : hour + 1) / 2])) {
					Intent intent = new Intent(ListHealthTime.this, ListHealthTime.class);
					intent.putExtra("name", allDatas[(hour == 23 ? 0 : hour + 1) / 2] + "养生");
					ListHealthTime.this.onBackPressed();
					ListHealthTime.this.startActivity(intent);
				}
			}
		};
		timer = new Timer();
		task = new TimerTask() {
			@Override
			public void run() {
				handler.sendEmptyMessage(0);
			}
		};
		timer.schedule(task, 1000 * secon, 1000 * 2);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		timer.cancel();
		task.cancel();
		handler.removeCallbacksAndMessages(null);
		
		if(listData!=null)listData.clear();
		if(showData!=null)showData.clear();
		if(timeList!=null)timeList.clear();
	}

	// 反馈
	public void clickFeekback(View view) {
		Intent intent_feek = new Intent(ListHealthTime.this, Feedback.class);
		intent_feek.putExtra("feekUrl", "养生时辰-"+allDatas[hour]);
		startActivity(intent_feek);
	}

	@Override
	public void onBackPressed() {
		if (timer != null) {
			timer.cancel();
			timer.purge();
		}
		super.onBackPressed();
	}
}
