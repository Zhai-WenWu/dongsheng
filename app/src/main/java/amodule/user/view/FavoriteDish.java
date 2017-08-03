package amodule.user.view;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.LoginManager;
import acore.logic.load.LoadManager;
import acore.override.activity.base.BaseActivity;
import acore.tools.StringManager;
import acore.widget.DownRefreshList;
import amodule.dish.activity.DetailDish;
import amodule.dish.activity.MenuDish;
import amodule.user.activity.MyFavorite;
import amodule.user.activity.login.LoginByAccout;
import amodule.user.adapter.AdapterMyselfFavorite;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;
import xh.basic.internet.UtilInternet;
import xh.basic.tool.UtilString;

public class FavoriteDish {
	public DownRefreshList theListView = null;
	private View view;
	
	private BaseActivity mAct;
	public LoadManager loadManager;
	private AdapterMyselfFavorite adapter;
	public ArrayList<Map<String, String>> listDataMyFav;
	
	private int currentPage = 0, everyPage = 0;
	private String userCode = "";
	public boolean loadOver = false;
	public boolean isOne = true;
	public FavoriteDish(BaseActivity mAct){
		this.mAct = mAct;
	}
	
	public View onCreateView(){
		view = LayoutInflater.from(mAct).inflate(R.layout.a_my_favorite_myself, null);
		loadOver = false;
		if (isOne) {
			init();
			isOne = false;
		}
		return view;
	}
	
	public void onDestroy(){
		mAct = null;
		view = null;
		adapter = null;
		listDataMyFav.clear();
	}
	
	public void init() {
		userCode = LoginManager.userInfo.get("code");
		// 结果显示
		loadManager = mAct.loadManager;
		view.findViewById(R.id.btn_goFavorite).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent it = new Intent(mAct,MenuDish.class);
				mAct.startActivity(it);
			}
		});
		theListView = (DownRefreshList) view.findViewById(R.id.myself_lv_favorite);
		theListView.setDivider(null);
		listDataMyFav = new ArrayList<Map<String, String>>();
		adapter = new AdapterMyselfFavorite(mAct, theListView, listDataMyFav, 0, null, null);
		loadManager.setLoading(theListView, adapter, true, new OnClickListener() {
			@Override
			public void onClick(View v) {
				load(false);
			}
		}, new OnClickListener() {

			@Override
			public void onClick(View v) {
				load(true);
			}
		});
		theListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if(position - 1 >=0 && listDataMyFav.size()>0 ){
					Intent intent = new Intent(mAct, DetailDish.class);
					intent.putExtra("name", listDataMyFav.get(position - 1).get("name"));
					intent.putExtra("code", listDataMyFav.get(position - 1).get("code"));
					mAct.startActivity(intent);
				}
			}
		});
		theListView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				
				final Map<String, String> map = listDataMyFav.get(arg2-1);
				new AlertDialog.Builder(mAct)
					.setIcon(android.R.drawable.ic_dialog_alert)
					.setTitle("取消收藏")
					.setMessage("确定要取消收藏?")
					.setPositiveButton("确定", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							doFavorite(map);
							listDataMyFav.remove(map);
							adapter.notifyDataSetChanged();
						}
					}).setNegativeButton("取消", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
						}
					}).create().show();
				return true;
			}
		});
	}
	
	// 收藏响应
	private void doFavorite(final Map<String, String> map) {
		AppCommon.onFavoriteClick(mAct,"favorites", map.get("code"), new InternetCallback(mAct) {
			@Override
			public void loaded(int flag, String url, Object returnObj) {

			}
		});
	}
	
	 //加载数据
	public void load(final boolean isForward) {
		if (!LoginManager.isLogin()) {
			Intent intent = new Intent(mAct, LoginByAccout.class);
			mAct.startActivity(intent);
			return;
		}
		if (isForward) {
			currentPage = 1;
		} else
			currentPage++;
		loadManager.changeMoreBtn(theListView,UtilInternet.REQ_OK_STRING, -1, -1, currentPage,listDataMyFav.size() == 0);
		String getUrl = StringManager.api_getUSerData + "?code=" + userCode + "&type=favDish&page=" + currentPage;
		ReqInternet.in().doGet(getUrl, new InternetCallback(mAct) {
			@Override
			public void loaded(int flag, String url, Object returnObj) {
				int loadCount = 0;
				if (flag >= UtilInternet.REQ_OK_STRING) {
					loadOver = true;
					if(isForward) listDataMyFav.clear();
					ArrayList<Map<String, String>> listMyDish = UtilString.getListMapByJson(returnObj);
					ArrayList<Map<String, String>> objInfo = UtilString.getListMapByJson(listMyDish.get(0).get("obj"));
					loadCount = objInfo.size();
					for (int i = 0; i < objInfo.size(); i++) {
						Map<String, String> map = objInfo.get(i);
						map.put("allClick", map.get("allClick") + "浏览");
						map.put("favorites", map.get("favorites") + "收藏");
						map.put("isMakeImg", map.get("isMakeImg").equals("2") ? "步骤图" : "hide");
						map.put("isFine", map.get("isFine").equals("2") ? "精" : "hide");
						if(!map.containsKey("hasVideo")){
							map.put("hasVideo", "1");
						}
						map.put("video", "2".equals(map.get("hasVideo")) ? "[视频]" : "hide");
						listDataMyFav.add(objInfo.get(i));
					}
					adapter.notifyDataSetChanged();
					MyFavorite.notifyMessage(MyFavorite.MSG_DATA_OK);
					// 如果是重新加载的,选中第一个tab.
					if (isForward)
						theListView.setSelection(1);
				}
				if (everyPage == 0)
					everyPage = loadCount;
				currentPage = loadManager.changeMoreBtn(theListView,flag, everyPage, loadCount, currentPage,listDataMyFav.size() == 0);
				// 如果总数据为空,显示没有菜谱
				if (flag >= UtilInternet.REQ_OK_STRING && listDataMyFav.size() == 0) {
					view.findViewById(R.id.myself_favorite_noData).setVisibility(View.VISIBLE);
					theListView.setVisibility(View.GONE);
				}
				// 否则显示结果
				else {
					view.findViewById(R.id.myself_favorite_noData).setVisibility(View.GONE);
					theListView.setVisibility(View.VISIBLE);
				}
				theListView.onRefreshComplete();
			}
		});
	}
}
