package amodule.user.view;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import acore.logic.load.LoadManager;
import acore.override.activity.base.BaseActivity;
import acore.tools.StringManager;
import acore.tools.ToolsDevice;
import acore.widget.DownRefreshList;
import amodule.user.activity.FriendHome;
import amodule.user.adapter.AdapterFansFollwers;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;
import xh.basic.internet.UtilInternet;
import xh.basic.tool.UtilString;

public class FansFollwersFragment {
	private DownRefreshList listView;
	private View view;
	private TextView tv_noData; 
	
	private BaseActivity mAct;
	// 加载管理
	private LoadManager loadManager = null;
	private static Handler handler = null;
	private AdapterFansFollwers adapter;
	private ArrayList<Map<String, String>> listData;
	
	private final int MSG_NOUS_OK = 1;
	private String type,userCode;
	private int currentPage = 0, everyPage = 0;
	private boolean LoadOver;
	public FansFollwersFragment(){
		super();
	}
	
	public FansFollwersFragment(BaseActivity act, String type,String userCode) {
		super();
		this.mAct = act;
		this.type = type;
		this.userCode=userCode;
	}

//	@Override
	public View onCreateView() {
		view = LayoutInflater.from(mAct).inflate(R.layout.a_my_fans_follower_fragment, null);
		listView = (DownRefreshList) view.findViewById(R.id.my_list_fans_follower);
		listView.setDivider(null);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Intent intent = new Intent(mAct, FriendHome.class);
				String userCode = listData.get(position - 1).get("code");
				Bundle bundle = new Bundle();
				bundle.putString("code", userCode);
				intent.putExtras(bundle);
				mAct.startActivity(intent);
			}
		});
		LoadOver = false;
		loadManager = mAct.loadManager;
//		loadManager = new LoadManager(mAct, mAct.rl);
		init();
		return view;
	}

	private void init() {
		loadManager.showProgressBar();
		tv_noData = (TextView) view.findViewById(R.id.tv_noData);
		listData = new ArrayList<Map<String, String>>();
		adapter = new AdapterFansFollwers(mAct,listView, listData, 
				R.layout.a_my_item_fans,
				new String[] { "img", "nickName", "folState"}, 
				new int[] { R.id.fans_user_img, R.id.fans_user_name,R.id.fans_user_item_choose});
		adapter.imgResource=R.drawable.bg_round_zannum;
		adapter.roundType = 1;
		adapter.roundImgPixels = ToolsDevice.dp2px(mAct, 500);
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				int what = msg.what;
				switch (what) {
				case MSG_NOUS_OK: // tab Hot数据加载完成;
					mAct.loadManager.hideProgressBar();
					break;
				}
			}
		};
		getData();
	}

	private void getData() {
		if (!LoadOver) {
			HashMap<String, String> map = new HashMap<String, String>();
			map.put("个人关注", type.equals("fans") ? "粉丝" : "关注");
			mAct.loadManager.showProgressBar();
			loadManager.setLoading(listView, adapter, true, new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					getFansData(false);
				}
			}, new OnClickListener() {

				@Override
				public void onClick(View v) {
					getFansData(true);
				}
			});
			LoadOver = true;
		}
	}

	private void getFansData(final boolean isForward) {
		Button loadMore = loadManager.getSingleLoadMore(listView);
		if (isForward) {
			currentPage = 1;
			if (loadMore != null)
				loadMore.setVisibility(View.GONE);
		} else {
			currentPage++;
		}
		loadManager.changeMoreBtn(listView,UtilInternet.REQ_OK_STRING, -1, -1, currentPage,listData.size() == 0);
		String getUrl = StringManager.api_getUSerData + "?code="+userCode+"&type=" + type + "&page=" + currentPage;
		ReqInternet.in().doGet(getUrl, new InternetCallback() {
			@Override
			public void loaded(int flag, String url, Object returnObj) {
				int loadCount = 0;
				if (flag >= UtilInternet.REQ_OK_STRING) {
					//刷新清理数据
					if(isForward) listData.clear();
					// 解析数据
					ArrayList<Map<String, String>> listReturn = UtilString.getListMapByJson(returnObj);
					Map<String, String> map = listReturn.get(0);
					if (map.containsKey("obj") && !map.get("obj").equals("null")) {
						listReturn = UtilString.getListMapByJson(map.get("obj"));
						for (int i = 0; i < listReturn.size(); i++) {
							Map<String, String> mapReturn = listReturn.get(i);
							// if(type.equals("fans"))
							// mapReturn.put("folState", "hide");
							// else
							mapReturn.put("lv", "lv" + mapReturn.get("lv"));
							mapReturn.put("folState", "folState" + mapReturn.get("folState"));
							listData.add(mapReturn);
						}
					}
					loadCount = listReturn.size();
					handler.sendEmptyMessage(MSG_NOUS_OK);
					adapter.notifyDataSetChanged();
					// 如果是重新加载的,选中第一个tab.
					if (isForward)
						listView.setSelection(1);
				}
				if (everyPage == 0)
					everyPage = loadCount;
				currentPage = loadManager.changeMoreBtn(listView,flag, everyPage, loadCount, currentPage,listData.size() == 0);
				listView.onRefreshComplete();
				if(flag > 1)
					if(listData.size()!=0){
						listView.setVisibility(View.VISIBLE);
						tv_noData.setVisibility(View.GONE);
					}else{
						if(!type.equals("fans"))
							tv_noData.setText("暂时还没有关注哦~");
						tv_noData.setVisibility(View.VISIBLE);
						listView.setVisibility(View.GONE);
					}
			}
		});
	}
}
