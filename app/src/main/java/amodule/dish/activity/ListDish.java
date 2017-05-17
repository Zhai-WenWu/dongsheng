package amodule.dish.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.ListView;
import android.widget.TextView;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.Map;

import acore.logic.XHClick;
import acore.override.activity.base.BaseActivity;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import amodule.dish.adapter.AdapterListDish;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;
import third.share.BarShare;
import xh.basic.internet.UtilInternet;
import xh.basic.tool.UtilString;

/**
 * Title:ListDish.java Copyright: Copyright (c) 2014~2017
 *
 * @author zeyu_t
 * @date 2014年10月14日
 */
@SuppressLint("InflateParams")
public class ListDish extends BaseActivity {

	private TextView dishTitle, dishInfo,dishName;
	
	private AdapterListDish adapter = null;
	private ArrayList<Map<String, String>> arrayList = null;

	private int currentPage = 0,everyPage = 0, loadPage = 0 ;
	private String name = "", g1 = "", type = "";
	private String shareImg = "";
	public boolean moreFlag = true, offLineOver = false, infoVoer = false,isToday=false;
	private String shareName = "";
	private String data_type = "";//推荐列表过来的数据
	private Long startTime;//统计使用的时间
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle bundle = this.getIntent().getExtras();
		startTime = System.currentTimeMillis();
		if(bundle != null){
			type = bundle.getString("type");
			g1 = bundle.getString("g1");
			name = bundle.getString("name");
			data_type= bundle.getString("data_type");
			shareName = name;
		}
		if ("recommend".equals(type) || "typeRecommend".equals(type))
			initActivity(name, 2, 0, R.layout.c_view_bar_title_time, R.layout.a_dish_caidan_list);
		else
			initActivity(type.equals("caidan") ? "精选菜单" : name, 2, 0, R.layout.c_view_bar_title, R.layout.a_dish_caidan_list);
		initMenu();
		initBarView();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		arrayList.clear();
		System.gc();
		long nowTime=System.currentTimeMillis();
		if(startTime>0&&(nowTime-startTime)>0&&!TextUtils.isEmpty(data_type)){
			XHClick.saveStatictisFile("ListDish","info",data_type,type,"","stop",String.valueOf((nowTime-startTime)/1000),"","","","");
		}
	}

	//初始化
	private void initMenu() {
		ListView listView = (ListView) findViewById(R.id.dish_menu_listview);
		if (type.equals("recommend") || type.equals("typeRecommend")) {
			TextView title_time = (TextView) findViewById(R.id.title_time);
			title_time.setText("" + Tools.getAssignTime("yyyy-MM-dd",0));
		} else {
			View view = LayoutInflater.from(ListDish.this).inflate(R.layout.a_dish_head_caidan_view, null);
			dishTitle = (TextView) view.findViewById(R.id.dish_menu_name);
			dishName = (TextView) view.findViewById(R.id.dish_menu_classify_name);
			dishInfo = (TextView) view.findViewById(R.id.dish_menu_info);
			dishInfo.setClickable(true);
			listView.addHeaderView(view, null, false);
		}
		arrayList = new ArrayList<Map<String, String>>();
		// 绑定列表数据
		adapter = new AdapterListDish(this, listView, arrayList, 
				R.layout.a_dish_item_menu, 
				new String[] {"name","allClick", "favorites", "nickName" ,"isToday"},
				new int[] { R.id.dish_recom_tv_name,	R.id.dish_recom_tv_allClick, R.id.dish_recom_tv_favorites,
					R.id.dish_recom_tv_nickName, R.id.dish_recom_item_today},
				type);
		adapter.imgWidth = ToolsDevice.getWindowPx(this).widthPixels - Tools.getDimen(this.getApplicationContext(), R.dimen.dp_20);//20=10*2
		adapter.scaleType = ScaleType.CENTER_CROP;
		adapter.isAnimate = true;
		
		
		loadManager.setLoading(listView, adapter, true, new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				loadData();
			}
		});
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				Intent intent = new Intent(ListDish.this, DetailDish.class);
				if (!type.equals("recommend") && !type.equals("typeRecommend")) 
					position--;
				if(position > -1 && position < arrayList.size()){
					intent.putExtra("code", arrayList.get(position).get("code"));
					intent.putExtra("name", arrayList.get(position).get("name"));
					startActivity(intent);
				}
			}
		});
	}

	private void initBarView() {
		// titleBar初始化
		ImageView img_share = (ImageView) findViewById(R.id.rightImgBtn2);
		img_share.setImageResource(R.drawable.z_z_topbar_ico_share);
		img_share.setVisibility(View.VISIBLE);
		img_share.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				doShare();
			}
		});
	}

	@SuppressLint("NewApi")
	public void loadData() {
		currentPage++;
		loadManager.changeMoreBtn(UtilInternet.REQ_OK_STRING, -1, -1, currentPage,arrayList.size() == 0);
		String url = null;
		if (type.equals("recommend")) 
			url = StringManager.api_getDishList + "?type=" + type + "&page=" + currentPage;
		 else  if(type.equals("typeRecommend"))
			url = StringManager.api_getDishList + "?type=" + type + "&g1="+g1+"&page=" + currentPage;
		else
			url = StringManager.api_getDishList + "?type=" + type + "&g1=" + g1 + "&page=" + currentPage;
		ReqInternet.in().doGet(url, new InternetCallback(this) {
			@Override
			public void loaded(int flag, String url, Object returnObj) {
				if (flag >= UtilInternet.REQ_OK_STRING) {
					ArrayList<Map<String, String>> returnList = UtilString.getListMapByJson(returnObj);
					if (!type.equals("recommend") && !type.equals("typeRecommend")) {
						String title = returnList.get(0).get("classifyName");
						String classifyName = returnList.get(0).get("name");
						int dp_20 = Tools.getDimen(ListDish.this.getApplicationContext(), R.dimen.dp_20);
						if (title != null && title.length() > 0) {
							dishTitle.setText(title);
							dishTitle.setPadding(0, dp_20, 0, 0);
							dishName.setPadding(0, 0, 0, 0);
						} else {
							dishTitle.setVisibility(View.GONE);
							dishName.setPadding(0, dp_20, 0, 0);
						}
						shareName = classifyName;
						dishName.setText(classifyName);
						if (!infoVoer) {
							String info = returnList.get(0).get("info");
							if (info.length() > 5) {
								int dp_15 = Tools.getDimen(ListDish.this.getApplicationContext(), R.dimen.dp_15);
								dishInfo.setPadding(dp_15 , dp_15 , dp_15 , 0);
								dishInfo.setLineSpacing(Tools.getDimen(ListDish.this.getApplicationContext(), R.dimen.dp_8), 1);
								info = info.replace("\n", "\n\t\t\t\t");
								info = "\t\t\t\t" + info;
								final String oldText = info;
								if (oldText.length() > 200) {
									final String newText = oldText.substring(0, 150) + "……查看更多>>";
									dishInfo.setText(newText);
									dishInfo.setOnClickListener(new OnClickListener() {
										@Override
										public void onClick(View v) {
											if (moreFlag) {
												moreFlag = false;
												dishInfo.setText(oldText);
											} else {
												moreFlag = true;
												dishInfo.setText(newText);
											}
										}
									});
								} else
									dishInfo.setText(oldText);
							} else 
								dishInfo.setVisibility(View.GONE);
							infoVoer=true;
						}
						returnList = UtilString.getListMapByJson(returnList.get(0).get("dishs"));
					}
					for (int i = 0; i < returnList.size(); i++) {
						Map<String, String> map = returnList.get(i);
						if (i == 0) shareImg = returnList.get(i).get("img");
						map.put("allClick", map.get("allClick") + "浏览");
						map.put("favorites", map.get("favorites") + "收藏");
						if(type.equals("typeRecommend") && map.get("isToday").equals("1") && !isToday){
							map.put("isToday", "往期推荐");
							isToday=true;
						}else
							map.put("isToday", "hide");
						map.put("isDel", "hide");
						if(!map.containsKey("hasVideo")){
							map.put("hasVideo", "1");
						}
						String nickName = map.get("nickName");
						if(nickName == null || nickName.equals("") || nickName.equals("null"))
							map.put("nickName", "hide");
						arrayList.add(map);
					}
					loadPage = returnList.size();
					adapter.notifyDataSetChanged();
				} else {
					toastFaildRes(flag,true,returnObj);
				}
				if (everyPage == 0) everyPage = loadPage;
				currentPage = loadManager.changeMoreBtn(flag, everyPage, loadPage, currentPage,arrayList.size() == 0);
				// 如果总数据为空,显示没有消息
				if (flag >= UtilInternet.REQ_OK_STRING && arrayList.size() == 0) {
					findViewById(R.id.dish_menu_noData).setVisibility(View.VISIBLE);
				}
				// 否则显示结果
				else
					findViewById(R.id.dish_menu_listview).setVisibility(View.VISIBLE);
			}
		});
	}

	protected void doShare() {
		XHClick.mapStat(this, "a_share400", "菜谱", "菜单详情页");
		if (TextUtils.isEmpty(shareName)) {
			shareName = "精选菜单";
		}
		String imgType = BarShare.IMG_TYPE_WEB;
		String title = "";
		String clickUrl = "";
		String content = "";
		if (type.equals("recommend")) {
			clickUrl = StringManager.wwwUrl + "caipu/recommend/";
		} else  {
			clickUrl = StringManager.wwwUrl + "caipu/caidan/" + g1;
		}
		// 是推荐菜单
		barShare = new BarShare(ListDish.this,"菜单详情", "菜谱");
		if (type.equals("caidan")) {
			// 是推荐菜单
			title = shareName + "，果断收藏！";
			content = shareName + "，各种精选菜谱，非常有用，推荐一下。（香哈菜谱）";
		} else {
			title = "今日推荐菜谱-" + Tools.getAssignTime("MM月dd日",0);
			clickUrl = StringManager.third_downLoadUrl;
			content = "今日推荐菜谱很不错，每天可以尝试不同的菜，吃货必备呀 ";
		}
		barShare.setShare(imgType, title, content, shareImg, clickUrl);
		barShare.openShare();
	}
}
