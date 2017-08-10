package amodule.dish.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.ListView;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.Map;

import acore.logic.XHClick;
import acore.override.activity.base.BaseActivity;
import acore.override.adapter.AdapterSimple;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import amodule.search.avtivity.HomeSearch;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;
import third.share.BarShare;
import xh.basic.internet.UtilInternet;
import xh.basic.tool.UtilString;

/**
 * Title:MenuDish.java Copyright: Copyright (c) 2014~2017
 * 
 * @author zeyu_t
 * @date 2014年10月14日
 */
public class MenuDish extends BaseActivity {
	private ListView listView = null;
	private ImageView rightBtnShare,rightBtnSearch;
	
	private AdapterSimple menuAdapter = null;
	private ArrayList<Map<String, String>> arrayList = null;

	private int currentPage = 0,everyPage = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initActivity("精选菜单", 2, 0, R.layout.c_view_bar_title, R.layout.a_dish_menu);
		initView();
		initData();
		setListener();
	}
	
	public void initView(){
		listView = (ListView) findViewById(R.id.dish_list_menu);
		rightBtnShare = (ImageView) findViewById(R.id.rightImgBtn2);
		rightBtnSearch = (ImageView) findViewById(R.id.rightImgBtn4);
	}
	
	private void initData() {
		// 分享功能
		rightBtnShare.setImageResource(R.drawable.z_z_topbar_ico_share);
		rightBtnShare.setVisibility(View.VISIBLE);
		// 搜索按钮
		rightBtnSearch.setImageResource(R.drawable.z_z_topbar_ico_so);
		rightBtnSearch.setVisibility(View.VISIBLE);
		int left = Tools.getDimen(this, R.dimen.dp_12_5);
		int top =  Tools.getDimen(this, R.dimen.dp_13);
		int right = Tools.getDimen(this, R.dimen.dp_12_5);
		int bottom = Tools.getDimen(this, R.dimen.dp_10);
		rightBtnSearch.setPadding(left , top, right, bottom);
		listView.setDivider(null);
		arrayList = new ArrayList<Map<String, String>>();
		menuAdapter = new AdapterSimple(listView, arrayList, 
				R.layout.a_dish_item_jingxuan_menu, 
				new String[] { "name", "allClick", "img1","img2", "img3" }, 
				new int[] { R.id.dish_jingxuan_menuName, R.id.dish_jingxuan_hote, R.id.dish_jingxuan_iv_left,R.id.dish_jingxuan_iv_middle, R.id.dish_jingxuan_iv_right });
		menuAdapter.scaleType = ScaleType.FIT_XY;
		menuAdapter.imgWidth = ToolsDevice.getWindowPx(this).widthPixels * 90 / 640;
	}
	
	private void setListener() {
		rightBtnShare.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				doShare();
			}
		});
		rightBtnSearch.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MenuDish.this, HomeSearch.class);
				intent.putExtra("type", "caidan");
				startActivity(intent);
			}
		});
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				Intent intent = new Intent(MenuDish.this, ListDish.class);
				intent.putExtra("param", "type=caidan" + "&g1=" + arrayList.get(position).get("code"));
				intent.putExtra("name", arrayList.get(position).get("name"));
				intent.putExtra("type", "caidan");
				intent.putExtra("g1", arrayList.get(position).get("code"));
				startActivity(intent);
			}
		});
		loadManager.setLoading(listView, menuAdapter, true, new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				loadData();
			}
		});
	}

	public void loadData() {
		currentPage++;
		if(currentPage==1)
			listView.setVisibility(View.GONE);
		loadManager.changeMoreBtn(UtilInternet.REQ_OK_STRING, -1, -1, currentPage,arrayList.size() == 0);
		String url = StringManager.api_getMenuData + "?type=list&page=" + currentPage;
		ReqInternet.in().doGet(url, new InternetCallback(this) {

			@Override
			public void loaded(int flag, String url, Object returnObj) {
				int loadPage = 0;
				if (flag >= UtilInternet.REQ_OK_STRING) { // 表示成功
					ArrayList<Map<String, String>> listReturn = UtilString.getListMapByJson(returnObj);
					Map<String, String> map = null;
					String[] simgs = null;
					for (int i = 0; i < listReturn.size(); i++) {
						map = listReturn.get(i);
						String imgs = map.get("imgs");
						simgs = imgs.split(",");
						if(simgs != null && simgs.length >= 3){
							map.put("img1", simgs[0]);
							map.put("img2", simgs[1]);
							map.put("img3", simgs[2]);
						} else {
							map.put("img1", "ico"+R.drawable.i_nopic);
							map.put("img2", "ico"+R.drawable.i_nopic);
							map.put("img3", "ico"+R.drawable.i_nopic);
						}
						map.remove("imgs");
						map.put("allClick", map.get("dishNum") + "道菜 / " + map.get("allClick") + "次浏览");
						arrayList.add(map);
					}
					loadPage = listReturn.size();
					menuAdapter.notifyDataSetChanged();
				}
				if (everyPage == 0) everyPage = loadPage;
				currentPage = loadManager.changeMoreBtn(flag, everyPage, loadPage, currentPage,arrayList.size() == 0);
				if(arrayList.size()>0){
					listView.setVisibility(View.VISIBLE);
				}else if(arrayList.size()<=0&&currentPage==1){ 
					Button loadMore = loadManager.getSingleLoadMore(null);
					if(loadMore != null)
						loadMore.setVisibility(View.GONE);
					listView.setVisibility(View.GONE);
				}
				// 如果总数据为空,显示没有消息
				if (flag >= UtilInternet.REQ_OK_STRING && arrayList.size() == 0) {
					findViewById(R.id.dish_list_menu_noData).setVisibility(View.VISIBLE);
					findViewById(R.id.dish_list_menu).setVisibility(View.GONE);
				}
				// 否则显示结果
				else{
					findViewById(R.id.dish_list_menu).setVisibility(View.VISIBLE);
					findViewById(R.id.dish_list_menu_noData).setVisibility(View.GONE);
				}
			}
		});
	}
	
	//分享
	private void doShare() {
		XHClick.mapStat(MenuDish.this, "a_share400", "菜谱", "菜单列表");
		barShare = new BarShare(MenuDish.this, "精选菜单","菜单");
		String type = BarShare.IMG_TYPE_RES;
		String shareImg = "" + R.drawable.share_launcher;
		String title = "精选菜单大全，强烈推荐！";
		String clickUrl = StringManager.wwwUrl + "caipu/caidan";
		String content = "最近一直在用香哈菜谱，内容好、分类全，还可以离线下载菜谱~";
		barShare.setShare(type, title, content, shareImg, clickUrl);
		barShare.openShare();
	}
}
