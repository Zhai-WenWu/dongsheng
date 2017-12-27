package amodule.quan.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.Map;

import acore.logic.LoginManager;
import acore.override.activity.base.BaseActivity;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import acore.widget.DownRefreshList;
import amodule.quan.adapter.AdapterCircleUser;
import amodule.user.activity.FriendHome;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;
import xh.basic.internet.UtilInternet;
import xh.basic.tool.UtilString;

/**
 * 圈子用户搜索
 */
public class CircleUserSo extends BaseActivity implements OnClickListener {
	
	private String mTitle,mCid;
	private int mType;
	
	private DownRefreshList mLvSur;
	private AdapterCircleUser mAdapter;
	private ArrayList<Map<String, String>> mListData;
	private ImageView mSoIv;
	private EditText mSoContentEdt;
	private View headView;
	
	private int currentPage = 0, everyPage = 0;
	
	private String mMyDuty = "3",mSoContent = "";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initActivity("", 2, 0, 0, R.layout.a_circle_user_so);
		mType = getIntent().getIntExtra("type",-1);
		mTitle = getIntent().getStringExtra("title");
		mCid = getIntent().getStringExtra("cid");
		if(mType == -1 || (mType != TYPE_CIRCLE_USER && mType != TYPE_CIRCLE_USER_BLACK) 
				|| TextUtils.isEmpty(mTitle) || TextUtils.isEmpty(mCid)){
			Toast.makeText(this, "数据错误", Toast.LENGTH_SHORT).show();
			finish();
		}
		initView();
	}
	
	private void initView(){
		findViewById(R.id.leftImgBtn).setOnClickListener(this);
		mSoIv = (ImageView)findViewById(R.id.a_circle_user_so_img);
		findViewById(R.id.a_circle_user_so_ture).setOnClickListener(this);
		mSoContentEdt = (EditText)findViewById(R.id.a_circle_user_so_et);
		mSoContentEdt.setOnFocusChangeListener(new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if(hasFocus)
					mSoIv.setVisibility(View.GONE);
				else
					mSoIv.setVisibility(View.VISIBLE);
			}
		});
		mSoContentEdt.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,int after) {}
			@Override
			public void afterTextChanged(Editable s) {
				mSoContent = s.toString();
			}
		});
		// 控制返回键和回车键
		mSoContentEdt.setOnKeyListener(new OnKeyListener() {
			@Override 
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				switch (keyCode) {
				case KeyEvent.KEYCODE_ENTER:
				case KeyEvent.ACTION_DOWN:
					onSearch();
					return true;
				default:
					return false;
				}
			}
		});
		
		mLvSur = (DownRefreshList)findViewById(R.id.a_circle_user_so_list);
		mListData = new ArrayList<Map<String, String>>();
		mAdapter = new AdapterCircleUser(this,mLvSur,mUserOptionListener, mListData, R.layout.a_circle_user_item,
				new String[]{"nickName","img","isGourmet","isQuan","isManager","addBlack","addManager"},
				new int[]{ R.id.a_circle_user_item_name,R.id.a_circle_user_item_iv,R.id.a_circle_user_item_gourmet,
							R.id.a_circle_user_item_quan,R.id.a_circle_user_item_manager,
							R.id.a_circle_user_item_add_black,R.id.a_circle_user_item_add_manager});
		
		mAdapter.imgResource=R.drawable.bg_round_zannum;
		mAdapter.roundType = 1;
		mAdapter.roundImgPixels = ToolsDevice.dp2px(this, 500);
		loadManager.setLoading(mLvSur, mAdapter, true, new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				getCaredUserListData(false);
			}
		}, new OnClickListener() {

			@Override
			public void onClick(View v) {
				getCaredUserListData(true);
			}
		});
		
		LayoutInflater inflater = LayoutInflater.from(this);
		LinearLayout headLinear = new LinearLayout(this);
		headView = inflater.inflate(R.layout.a_circle_user_so_item_head, null);
		TextView title = (TextView)headView.findViewById(R.id.a_circle_user_so_item_head_name);
		title.setText(mTitle);
		headView.setVisibility(View.GONE);
		headLinear.addView(headView);
		mLvSur.addHeaderView(headLinear);
	}
	
	private AdapterCircleUser.CircleUserOptionListener mUserOptionListener = new AdapterCircleUser.CircleUserOptionListener() {
		
		@Override
		public void oRemoManager(final int position) {
			final Map<String, String> map = mListData.get(position);
			String code = map.get("code");
			ReqInternet.in().doPost(StringManager.api_circleCustomerPower, "&type=4" + "&customerId=" + code + "&cid=" + mCid, new InternetCallback() {
				
				@Override
				public void loaded(int flag, String url, Object msg) {
					if(flag >= UtilInternet.REQ_OK_STRING){
						map.put("isManager", "hide");
						map.put("addManager", CircleUser.mAddManager);
						mAdapter.notifyDataSetChanged();
					}else{
						Tools.showToast(CircleUserSo.this, msg.toString());
					}
				}
			});
		}
		@Override
		public void oAddManager(final int position) {
			String code = mListData.get(position).get("code");
			ReqInternet.in().doPost(StringManager.api_circleCustomerPower, "&type=3" + "&customerId=" + code + "&cid=" + mCid, new InternetCallback() {
				
				@Override
				public void loaded(int flag, String url, Object msg) {
					if(flag >= UtilInternet.REQ_OK_STRING){
						Map<String, String> map = mListData.get(position);
						map.put("isManager", "管理员");
						map.put("addManager", CircleUser.mRemoManager);
						mAdapter.notifyDataSetChanged();
					}else{
						Tools.showToast(CircleUserSo.this, msg.toString());
					}
				}
			});
		}
		
		@Override
		public void oAddBlack(final View parentView,final int position) {
			String code = mListData.get(position).get("code");
			ReqInternet.in().doPost(StringManager.api_circleCustomerPower, "&type=1" + "&customerId=" + code + "&cid=" + mCid, new InternetCallback() {
				
				@Override
				public void loaded(int flag, String url, Object msg) {
					if(flag >= UtilInternet.REQ_OK_STRING){
						mListData.remove(position);
						mLvSur.removeViewInLayout(parentView);
						mAdapter.notifyDataSetChanged();
					}else{
						Tools.showToast(CircleUserSo.this, msg.toString());
					}
				}
			});
		}

		@Override
		public void onItemClick(int position) {
			Map<String, String> map = mListData.get(position);
			Intent it = new Intent(CircleUserSo.this,FriendHome.class);
			it.putExtra("code", map.get("code"));
			startActivity(it);
		}
		
	};
	
	/**
	 * 数据解析展示
	 */
	private void getCaredUserListData(final boolean isForward){
		final Button loadMore = loadManager.getSingleLoadMore(null);
		if (isForward) {
			currentPage = 1;
			if (loadMore != null)
				loadMore.setVisibility(View.GONE);
		} else {
			currentPage++;
		}
		loadManager.changeMoreBtn(UtilInternet.REQ_OK_STRING, -1, -1, currentPage,mListData.size() == 0);
		String getUrl;
		if(mType == TYPE_CIRCLE_USER)
			getUrl = mUircleUser + "?cid=" + mCid + "&page=" + currentPage + "&s=" + mSoContent;
		else 
			getUrl = mUircleBlackUser + "?cid=" + mCid + "&page=" + currentPage + "&s=" + mSoContent;
		ReqInternet.in().doGet(getUrl, new InternetCallback() {
			@Override
			public void loaded(int flag, String url, Object returnObj) {
				int loadCount = 0;
				if (flag >= UtilInternet.REQ_OK_STRING) {
					//刷新清理数据
					if(isForward) mListData.clear();
					ArrayList<Map<String, String>> listReturn = UtilString.getListMapByJson(returnObj);
					// 解析数据
					if(listReturn != null && listReturn.size() > 0){
						Map<String, String> data = listReturn.get(0); 
						ArrayList<Map<String, String>> listList = UtilString.getListMapByJson(data.get("list"));
						loadCount = listList.size();
						if(currentPage == 1){
							boolean isManager = LoginManager.isManager();
							if(isManager) mMyDuty = "1";
							else{
								ArrayList<Map<String, String>> currentList = UtilString.getListMapByJson(data.get("current"));
								if(currentList != null && currentList.size() > 0){
									mMyDuty = currentList.get(0).get("duty");
								}
							}
							if("1".equals(mMyDuty)){
								mAdapter.mRightLenght = mAdapter.mShowAll;
							}
							else{
								mAdapter.mRightLenght = mAdapter.mShowOne;
							}
							if(loadCount > 0){
								headView.setVisibility(View.VISIBLE);
								mLvSur.setVisibility(View.VISIBLE);
								findViewById(R.id.a_circle_user_so_nodata_hint).setVisibility(View.GONE);
							}else{
								headView.setVisibility(View.GONE);
								mLvSur.setVisibility(View.GONE);
								findViewById(R.id.a_circle_user_so_nodata_hint).setVisibility(View.VISIBLE);
							}
						}
						for (int i = 0; i < loadCount; i++) {
							Map<String, String> mapReturn = listList.get(i);
							/** 职务  */
							String duty = mapReturn.get("duty");
							//如果当前用户是圈子或者管理员
							if("1".equals(duty)){ //圈主不能被操作
								mapReturn.put("isQuan","管理员");
								mapReturn.put("isManager","");
								mapReturn.put("addBlack","");
								mapReturn.put("addManager","");
							}else if("2".equals(duty)){ //管理员 
								mapReturn.put("isQuan","");
								mapReturn.put("isManager","管理员");
								if("1".equals(mMyDuty)){ //圈主可以操作管理员
									mapReturn.put("addBlack","加入黑名单");
									mapReturn.put("addManager",CircleUser.mRemoManager);
								}else{ //管理员不可以操作管理员
									mapReturn.put("addBlack","");
									mapReturn.put("addManager","");
								}
							}else { //普通成员，管理员可以加入黑名单，圈主都可以操作
								mapReturn.put("isQuan","");
								mapReturn.put("isManager","");
								mapReturn.put("addManager","");
								mapReturn.put("addBlack","");
								if("1".equals(mMyDuty)){ //圈主可以操作管理员，可以加黑
									mapReturn.put("addManager",CircleUser.mAddManager);
									mapReturn.put("addBlack","加入黑名单");
								}else if("2".equals(mMyDuty)){ //管理员对普通人员只能加黑
									mapReturn.put("addBlack","加入黑名单");
								}
							}
							if(mapReturn.containsKey("isGourmet") && "2".equals(mapReturn.get("isGourmet")))
								mapReturn.put("isGourmet","ico" + R.drawable.z_user_gourmet_ico);
							else
								mapReturn.put("isGourmet","hide");
							mListData.add(mapReturn);
						}
						mAdapter.notifyDataSetChanged();
					}
					// 如果是重新加载的,选中第一个tab.
					if (isForward)
						mLvSur.setSelection(1);
				}
				if (everyPage == 0)
					everyPage = loadCount;
				currentPage = loadManager.changeMoreBtn(flag, everyPage, loadCount, currentPage,mListData.size() == 0);
				mLvSur.onRefreshComplete();
				if(loadMore != null){
					if(!loadMore.isEnabled()){
						loadMore.setText("没有了！");
					}else{
						loadMore.setText("加载更多");
					}
				}
			}
		});
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.a_circle_user_so_ture: //搜索按钮
			onSearch();
			break;
		case R.id.leftImgBtn:
			super.onBackPressed();
			break;
		}
	}
	
	private void onSearch(){
		ToolsDevice.keyboardControl(false, CircleUserSo.this, mSoContentEdt);
		mSoContent = mSoContentEdt.getText().toString();
		findViewById(R.id.a_circle_user_so_nodata_hint).setVisibility(View.GONE);
//		if(TextUtils.isEmpty(mSoContent)){
//			Tools.showToast(this, "搜索内容不能为空哦");
//			return;
//		}
		headView.setVisibility(View.GONE);
		mListData.clear();
		getCaredUserListData(true);
	}
	
	public static final int TYPE_CIRCLE_USER = 20023;
	public static final int TYPE_CIRCLE_USER_BLACK = 20024;
	
	private final String mUircleUser = StringManager.api_circleCustomerList;
	private final String mUircleBlackUser = StringManager.api_circleCustomerBlackList;
}
