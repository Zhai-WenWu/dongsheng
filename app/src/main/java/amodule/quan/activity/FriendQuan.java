package amodule.quan.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.Map;

import acore.override.activity.base.BaseActivity;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import acore.widget.DownRefreshList;
import amodule.quan.adapter.AdapterQuanFriend;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;
import xh.basic.internet.UtilInternet;
import xh.basic.tool.UtilString;

/**
 * @author aimin 创建时间：2014-10-10 下午2:05:54
 */
public class FriendQuan extends BaseActivity {
	private DownRefreshList listFriend;
	private EditText searchEdit;
	
	private AdapterQuanFriend adapterFriend;
	private ArrayList<Map<String, String>> dataFriend, searchList;

	private int currentPageFriend = 0, everyPageFriend = 10;
	private String value;
	private boolean LoadOver = false;
	private boolean isSearch = false;

	public static final String FRIENDS_LIST_RESULT = "friendsList";
	public static final int REQUEST_CODE_QUAN_FRIEND = 2000;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initActivity("通知好友", 2, 0, R.layout.c_view_bar_title, R.layout.a_quan_friend);
		LoadOver = false;
		// if(Activity.equals("UploadS")){
		value = getIntent().getStringExtra("value");
		// }
		init();
	}

	@SuppressLint("HandlerLeak")
	private void init() {
		// titleBar初始化
		TextView leftImgBtn = (TextView) findViewById(R.id.leftText);
		leftImgBtn.setText("关闭");
		leftImgBtn.setVisibility(View.VISIBLE);
		int dp_10 = Tools.getDimen(FriendQuan.this, R.dimen.dp_10);
		int dp_5 = Tools.getDimen(FriendQuan.this, R.dimen.dp_5);
		leftImgBtn.setPadding(dp_10 , dp_5, dp_10 , dp_10 );
		findViewById(R.id.leftImgBtn).setVisibility(View.GONE);
		TextView rightText = (TextView) findViewById(R.id.rightText);
		rightText.setText("确定");
		rightText.setVisibility(View.VISIBLE);
		rightText.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String chooseListStr = adapterFriend.getChooseList();
				Intent intent = new Intent();
				intent.putExtra(FRIENDS_LIST_RESULT,chooseListStr);
				FriendQuan.this.setResult(RESULT_OK,intent);
				finish();
			}
		});
		searchEdit = (EditText) findViewById(R.id.ed_search);
		searchEdit.addTextChangedListener(searchTextWatcher);
		loadManager.showProgressBar();
		listFriend = (DownRefreshList) findViewById(R.id.friend_list);
		listFriend.setDivider(null);
		dataFriend = new ArrayList<Map<String, String>>();
		searchList = new ArrayList<Map<String, String>>();
		adapterFriend = new AdapterQuanFriend(this, listFriend, searchList, 
				R.layout.a_quan_item_friend, 
				new String[] { "userImg", "userName", "flag" }, 
				new int[] { R.id.friend_iv_userImg, R.id.friend_tv_name, R.id.friend_iv_choose }, value);
		adapterFriend.roundImgPixels = ToolsDevice.dp2px(this, 500);
		adapterFriend.roundType = 1;
		getData();
	}

	private void getData() {
		if (!LoadOver) {
			loadManager.showProgressBar();
			loadManager.setLoading(listFriend, adapterFriend, true , new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					if(!isSearch){
						getFriendData(!LoadOver);
					}
				}
			}, new OnClickListener() {

				@Override
				public void onClick(View v) {
					getFriendData(true);
				}
			});
			LoadOver = true;
		}
	}

	private void getFriendData(final boolean isForward) {
		if (isForward) {
			isSearch = false;
			currentPageFriend = 0;
			searchEdit.setText("");
			String getUrl = StringManager.api_getFriendList /*+ "?page=" + currentPageFriend*/;
			refershNetworkData(getUrl);
		} else{
			currentPageFriend++;
			getLocalData();
		}
	}

	private void getLocalData() {
		int loadCount = 0;
		int length = currentPageFriend * 10 + 10 < dataFriend.size() ? currentPageFriend * 10 + 10 : dataFriend.size();
		for(int index = currentPageFriend * 10 ; index < length ; index ++){
			searchList.add(dataFriend.get(index));
			loadCount++;
		}
		adapterFriend.notifyDataSetChanged();
		loadManager.loadOver(50,listFriend,loadCount);
	}

	private void refershNetworkData(String getUrl) {
		ReqInternet.in().doGet(getUrl, new InternetCallback() {
			@Override
			public void loaded(int flag, String url, Object returnObj) {
				int loadCount = 0; 
				if (flag >= UtilInternet.REQ_OK_STRING) {
					dataFriend.clear();
					searchList.clear();
					// 解析数据
					ArrayList<Map<String, String>> list = UtilString.getListMapByJson(returnObj);
					for (int i = 0; i < list.size(); i++) {
						Map<String, String> map = list.get(i);
						// 解析用户昵称
						map.put("userName", map.get("nickName"));
						map.put("userCode", map.get("code"));
						map.put("userImg", map.get("img"));
						map.put("flag", "false");
						
						dataFriend.add(map);
					}
					int length = currentPageFriend * 10 + 10 < dataFriend.size() ? currentPageFriend * 10 + 10 : dataFriend.size();
					for(int index = currentPageFriend * 10 ; index < length ; index ++){
						searchList.add(dataFriend.get(index));
						loadCount++;
					}
					adapterFriend.notifyDataSetChanged();
//					handler.sendEmptyMessage(MSG_FRIEND_OK);
					// 如果是重新加载的,选中第一个tab.
					listFriend.setSelection(1);
				}
				loadManager.loadOver(flag,listFriend,loadCount);
				listFriend.onRefreshComplete();
			}
		});
	}

	// 搜索监听
	TextWatcher searchTextWatcher = new TextWatcher() {

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			Button loadMore = loadManager.getSingleLoadMore(listFriend);
			searchList.clear();
			searchList.addAll(dataFriend);
			String searchWord = s.toString();
			isSearch = searchWord.length() != 0;
			if (isSearch) {
				if(loadMore != null){
					loadMore.setVisibility(View.GONE);
				}
				for (int i = 0; i < searchList.size(); i++)
					if (searchList.get(i).get("nickName").indexOf(searchWord) < 0)
						searchList.remove(i--);
			} else if(loadMore != null){
				loadMore.setVisibility(View.VISIBLE);
			}
			adapterFriend.notifyDataSetChanged();
			listFriend.onRefreshComplete();
		}

		@Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
		@Override public void afterTextChanged(Editable s) { }
	};
}
