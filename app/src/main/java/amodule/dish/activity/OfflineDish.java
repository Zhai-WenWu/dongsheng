package amodule.dish.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ImageView.ScaleType;
import android.widget.ListView;
import android.widget.TextView;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.LoginManager;
import acore.logic.XHClick;
import acore.override.activity.base.BaseActivity;
import acore.tools.FileManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import amodule.dish.adapter.AdapterListDish;
import amodule.dish.db.DataOperate;
import amodule.dish.tools.DishMouldControl;
import amodule.user.activity.login.LoginByAccout;
import xh.basic.internet.UtilInternet;
import xh.basic.tool.UtilString;

@SuppressLint("InflateParams")
public class OfflineDish extends BaseActivity {
	private ListView listView = null;
	private Button rightBtn;
	private View rultView;
	private TextView rultHint;
	
	private AdapterListDish adapter = null;
	private ArrayList<Map<String, String>> arrayList = null;

	private int currentPage = 0,everyPage = 0,loadPage = 0;
	public boolean moreFlag = true, offLineOver = false, infoVoer = false;
	
	public boolean isBlankSpace = true,isAddHeadView = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initActivity("离线菜谱", 2, 0, R.layout.c_view_bar_title,R.layout.a_dish_offline);
		initView();
		initData();
		setListener();
	}
	
	private void initView() {
		rightBtn = (Button) findViewById(R.id.rightBtn1);
		listView = (ListView) findViewById(R.id.offLine_List);
		LayoutInflater inflater = LayoutInflater.from(this);
		rultView = inflater.inflate(R.layout.a_dish_offline_title_hint, null);
		rultView.setVisibility(View.GONE);
		rultHint = (TextView)rultView.findViewById(R.id.tv_hint);
		rultHint.setText("升级后，离线上限增加到" + AppCommon.nextDownDish +"，查看我的等级>>");
		rultHint.setOnClickListener(onLookLevel);
	}
	
	private OnClickListener onLookLevel = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			if (LoginManager.isLogin()) {
				String url = StringManager.api_getCustomerRank + "?code="+ LoginManager.userInfo.get("code");
				AppCommon.openUrl(OfflineDish.this, url, true);
			} else {
				Tools.showToast(OfflineDish.this, "登录后即可查看您的等级");
				startActivity(new Intent(OfflineDish.this, LoginByAccout.class));
			}
		}
	};

	private void initData() {
		rightBtn.setText("清空");
		arrayList = new ArrayList<Map<String, String>>();
		// 绑定列表数据
		adapter = new AdapterListDish(this , listView , arrayList,
				R.layout.a_dish_item_menu,
				new String[] {"name", "allClick", "favorites","nickName"},
				new int[] {  R.id.dish_recom_tv_name , R.id.dish_recom_tv_allClick , R.id.dish_recom_tv_favorites ,
						R.id.dish_recom_tv_nickName }, 
				"offline");
		adapter.imgWidth = ToolsDevice.getWindowPx(this).widthPixels - Tools.getDimen(this, R.dimen.dp_20);//20=10*2
		adapter.scaleType = ScaleType.CENTER_CROP;
		adapter.isAnimate=true;
		if(AppCommon.nextDownDish > 0 && AppCommon.nextDownDish < AppCommon.maxDownDish){
			isAddHeadView = true;
			rultView.setVisibility(View.VISIBLE);
			listView.addHeaderView(rultView, null, true);
		}else {
			rultView.setVisibility(View.GONE);
			isAddHeadView = false;
		}
	}

	// 设置离线列表的title
	private void setListener() {
		rightBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Dialog dialog = new AlertDialog.Builder(OfflineDish.this)
					.setTitle("清空离线菜谱")
					.setMessage("您确定要清空全部离线菜谱吗？")
					.setPositiveButton("清空",new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,int which) {
								int x = Integer.parseInt(DataOperate.buyBurden(OfflineDish.this, "x"));
								//统计
								XHClick.onEventValue(OfflineDish.this, "dishDownload315", "dishDownload", "清空" , -x);
								DataOperate.deleteBuyBurden(OfflineDish.this, "");
								Tools.showToast(getApplicationContext(), "清除成功");
								arrayList.clear();
								adapter.notifyDataSetChanged();
								loadManager.hideProgressBar();
								loadManager.changeMoreBtn(UtilInternet.REQ_OK_STRING, 10, 0,1,true);
								rightBtn.setVisibility(View.GONE);
								findViewById(R.id.dish_offline_noData).setVisibility(View.VISIBLE);
								if(AppCommon.nextDownDish > 0 && AppCommon.nextDownDish < AppCommon.maxDownDish){
									TextView tv = (TextView)findViewById(R.id.title_hint);
									tv.setVisibility(View.VISIBLE);
									tv.setText("升级后，离线上限增加到" + AppCommon.nextDownDish +"，查看我的等级>>");
									tv.setOnClickListener(onLookLevel);
								}
								rultView.setVisibility(View.GONE);
							}
						})
					.setNegativeButton("取消",new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,int which) {
								dialog.cancel();
							}
						}).create();
				dialog.show();
			}
		});
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,int position, long arg3) {
				int offlineDishLimit = DataOperate.getDownDishLimit(OfflineDish.this);
				if(isAddHeadView)position --; //原因，添加了一个HeadView
				if(position > -1 && position < offlineDishLimit){
					Intent intent = new Intent(OfflineDish.this, DetailDish.class);
					intent.putExtra("code", arrayList.get(position).get("code"));
					intent.putExtra("name", arrayList.get(position).get("name"));
					startActivity(intent);
				}
			}
		});
		// 长按删除,只在离线菜谱页面
		listView.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,int positon, long arg3) {
				if(isAddHeadView) positon --; //原因，添加了一个HeadView
				final int newPositon = positon; 
				final Map<String, String> map = arrayList.get(newPositon);
				new AlertDialog.Builder(OfflineDish.this)
						.setIcon(android.R.drawable.ic_dialog_alert)
						.setTitle("取消删除")
						.setMessage("确定要删除离线菜谱?")
						.setPositiveButton("确定",new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,int which) {
								//统计
								XHClick.onEventValue(OfflineDish.this, "dishDownload315", "dishDownload", "删除" , -1);
								DataOperate.deleteBuyBurden(OfflineDish.this,map.get("code"));
								String path = DishMouldControl.getOffDishPath() + map.get("code");
								FileManager.delDirectoryOrFile(path);
								arrayList.remove(newPositon);
								adapter.notifyDataSetChanged();
								if(arrayList.size()==0)
									onBackPressed();
								Tools.showToast(OfflineDish.this,"删除成功");
							}
						})
						.setNegativeButton("取消",new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,int which) {
							}
						}).create().show();
				return true;
			}
		});
		loadManager.setLoading(listView, adapter, true, new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				loadOffLine();
			}
		});
	}
	
	// 设置离线列表
	private void loadOffLine() {
		currentPage++;
		setOffLine(UtilString.getListMapByJson(DataOperate.loadPageBuyBurden(OfflineDish.this, currentPage)));
		isBlankSpace = false;
	}

	// 处理离线菜谱数据
	protected void setOffLine(ArrayList<Map<String, String>> listMapByJson) {
		for(Map<String, String> mapReturn : listMapByJson){
			mapReturn.put("code", mapReturn.get("code"));
			mapReturn.put("name", mapReturn.get("name"));
			mapReturn.put("img", mapReturn.get("img"));
			if(!mapReturn.containsKey("hasVideo")){
				mapReturn.put("hasVideo", "1");
			}
			try{
				String nickName = UtilString.getListMapByJson(mapReturn.get("customer")).get(0).get("nickName");
				mapReturn.put("nickName", nickName);
			}catch(Exception e){
				mapReturn.put("nickName", "hide");
			}
			mapReturn.put("isFav", "hide");
			mapReturn.put("isDel", "hide");
			mapReturn.put("isToday", "hide");
			mapReturn.put("allClick", mapReturn.get("allClick") + "浏览");
			mapReturn.put("favorites", mapReturn.get("favorites") + "收藏");
			arrayList.add(mapReturn);
		}
		loadPage = listMapByJson.size();
		adapter.notifyDataSetChanged();
		if (everyPage == 0) everyPage = 10;
		currentPage = loadManager.changeMoreBtn(UtilInternet.REQ_OK_STRING, everyPage, loadPage,currentPage,isBlankSpace);
		loadManager.hideProgressBar();
		if (arrayList.size() == 0) {
			rightBtn.setVisibility(View.GONE);
			findViewById(R.id.dish_offline_noData).setVisibility(View.VISIBLE);
			if(AppCommon.nextDownDish > 0 && AppCommon.nextDownDish < AppCommon.maxDownDish){
				TextView tv = (TextView)findViewById(R.id.title_hint);
				tv.setVisibility(View.VISIBLE);
				tv.setText("升级后，离线上限增加到" + AppCommon.nextDownDish +"，查看我的等级>>");
				tv.setOnClickListener(onLookLevel);
			}
			rultView.setVisibility(View.GONE);
		}
		// 否则显示结果
		else {
			rightBtn.setVisibility(View.VISIBLE);
			if (listView.getVisibility() == View.GONE)
				findViewById(R.id.offLine_List).setVisibility(View.VISIBLE);
		}
	}
}
