package amodule.user.view;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.xh.manager.DialogManager;
import com.xh.manager.ViewManager;
import com.xh.view.HButtonView;
import com.xh.view.MessageView;
import com.xh.view.TitleView;
import com.xiangha.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.LoginManager;
import acore.logic.load.LoadManager;
import acore.override.activity.base.BaseActivity;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import acore.widget.DownRefreshList;
import amodule.nous.activity.HomeNous;
import amodule.user.adapter.AdapterFavoriteNous;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;
import xh.basic.internet.UtilInternet;
import xh.basic.tool.UtilString;

public class FavoriteNous  {
	private DownRefreshList listNous;
	private View view;
	
	private BaseActivity mAct;
	// 加载管理
	LoadManager loadManager = null;
	private static Handler handler = null;
	private AdapterFavoriteNous adapter;
	private ArrayList<Map<String, String>> listDataNous;
	
	private final int MSG_NOUS_OK = 1;
	private int currentPage = 0, everyPage = 0;
	private String url;
	public boolean LoadOver = false;
	public FavoriteNous(BaseActivity act, String url,String name) {
		this.mAct = act;
		this.url = url;
	}

	/**
	 * 当ViewPager切换到其它界面,此方法会重新执行
	 */
//	@Override
	public View onCreateView() {
		view = LayoutInflater.from(mAct).inflate(R.layout.favorite_nous, null);
		loadManager = mAct.loadManager;
		currentPage=0;
		LoadOver = false;
		return view;
	}
	
	public void onDestroy(){
		mAct = null;
		view = null;
		adapter = null;
		listDataNous.clear();
	}

	public void init() {
		// 结果显示
		mAct.loadManager.showProgressBar();
		view.findViewById(R.id.btn_goFavorite).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent it = new Intent(mAct,HomeNous.class);
				mAct.startActivity(it);
			}
		});
		listNous = (DownRefreshList) view.findViewById(R.id.nous_list);
		listNous.setDivider(null);
		listDataNous = new ArrayList<Map<String, String>>();
		adapter = new AdapterFavoriteNous(mAct, listNous, listDataNous, 
				R.layout.a_nous_item_myfavorite, new String[] { "img", "title", "content",
				"allClick" }, new int[] { R.id.iv_nousCover, R.id.tv_nousTitle, R.id.tv_nousContent1, R.id.tv_allClick }){
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View view = super.getView(position, convertView, parent);
				setOnclick(listDataNous.get(position),view,position);
				return view;
			}
		};
		adapter.contentWidth = ToolsDevice.getWindowPx(mAct).widthPixels - Tools.getDimen(mAct, R.dimen.dp_120);//12=15*2+80+10
//		mact.recycleViews.put(listNous, new int[] { R.id.iv_nousCover, R.id.nous_image });
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				int what = msg.what;
				switch (what) {
				case MSG_NOUS_OK: // tab Hot数据加载完成;
					mAct.loadManager.hideProgressBar();
					listNous.setVisibility(View.VISIBLE);
					break;
				}
			}
		};
		getData();
	}
	// 绑定点击动作
	private void setOnclick(final Map<String, String> map, View view, final int i) {
		view.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if(map.size()==3){
					if(map.containsKey("url")&&map.get("url").length()!=0){
						AppCommon.openUrl(mAct, map.get("url"), true);
					}
				}else{
					AppCommon.openUrl(mAct, "nousInfo.app?code=" + map.get("code"), true);
				}
			}
		});
		view.setOnLongClickListener(new View.OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				final DialogManager dialogManager = new DialogManager(mAct);
				dialogManager.createDialog(new ViewManager(dialogManager)
						.setView(new TitleView(mAct).setText("取消收藏"))
						.setView(new MessageView(mAct).setText("确定要取消收藏?"))
						.setView(new HButtonView(mAct)
								.setNegativeText("取消", new View.OnClickListener() {
									@Override
									public void onClick(View v) {
										dialogManager.cancel();
									}
								})
								.setPositiveText("确定", new View.OnClickListener() {
									@Override
									public void onClick(View v) {
										dialogManager.cancel();
										doFavorite(map);
										listDataNous.remove(map);
										notifyDataChange();
									}
								}))).show();
				return true;
			}
		});
	}
	// 收藏响应
	private void doFavorite(final Map<String, String> map) {
//		AppCommon.onFavoriteClick(mAct,"nous", map.get("code"), new InternetCallback(mAct) {
//			@Override
//			public void loaded(int flag, String url, Object returnObj) {
//
//			}
//		});
	}

	private void getData() {
		if (!LoadOver) {
			mAct.loadManager.showProgressBar();
			loadManager.setLoading(listNous, adapter, true, new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					getNousData(false);
				}
			}, new OnClickListener() {

				@Override
				public void onClick(View v) {
					getNousData(true);
				}
			});
			LoadOver = true;
		}
	}
	
	//刷新
	public void loader(){
		getNousData(true);
	}
	
	private void getNousData(final boolean isForward) {
		if (isForward) {
			currentPage = 1;
		} else
			currentPage++;
		mAct.loadManager.changeMoreBtn(listNous,UtilInternet.REQ_OK_STRING, -1, -1, currentPage,listDataNous.size() == 0);
		String getUrl;
		if (url.equals("")) {
//			getUrl = StringManager.api_nousList + "?type=new" + "&page=" + currentPage;
			getUrl = StringManager.api_getUSerData + "?code=" +LoginManager.userInfo.get("code")+ "&type=favNous&page=" + currentPage;
		} else {
			getUrl = StringManager.api_nousList + "?type=classify&pinyin=" + url + "&page=" + currentPage;
		}
		ReqInternet.in().doGet(getUrl, new InternetCallback() {
			@Override
			public void loaded(int flag, String url, Object returnObj) {
				int loadCount = 0;
				if (flag >= UtilInternet.REQ_OK_STRING) {
					if(isForward) listDataNous.clear();
					// 未取到关注数据，打开榜单
					if (currentPage == 1 && returnObj.toString().length() < 100) {
						return;
					}
					// 解析数据
					ArrayList<Map<String, String>> list = UtilString.getListMapByJson(returnObj);
					ArrayList<Map<String, String>> objInfo = UtilString.getListMapByJson(list.get(0).get("obj"));
					for (int i = 0; i < objInfo.size(); i++) {
						loadCount++;
						Map<String, String> map2 = new HashMap<String, String>();
						map2.put("img", objInfo.get(i).get("img"));
						map2.put("title", objInfo.get(i).get("title"));
						map2.put("content", objInfo.get(i).get("content"));
						map2.put("allClick", objInfo.get(i).get("allClick") + "浏览");
						map2.put("code", objInfo.get(i).get("code"));
						listDataNous.add(map2);
					}
					handler.sendEmptyMessage(MSG_NOUS_OK);
					// 如果是重新加载的,选中第一个tab.
					if (isForward)
						listNous.setSelection(1);
					if (everyPage == 0)
						everyPage = loadCount;
					currentPage = loadManager.changeMoreBtn(listNous,flag, everyPage, loadCount, currentPage,listDataNous.size() == 0);
					notifyDataChange();
				}
			}
		});
	}

	private void notifyDataChange(){
		adapter.notifyDataSetChanged();
		if (listDataNous.size() == 0) {
			view.findViewById(R.id.myself_favorite_noData).setVisibility(View.VISIBLE);
			listNous.setVisibility(View.GONE);
		}else {
			view.findViewById(R.id.myself_favorite_noData).setVisibility(View.GONE);
			listNous.setVisibility(View.VISIBLE);
		}
		listNous.onRefreshComplete();
	}

}
