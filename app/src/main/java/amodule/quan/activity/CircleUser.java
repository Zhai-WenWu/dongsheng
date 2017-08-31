package amodule.quan.activity;

import java.util.ArrayList;
import java.util.Map;

import third.share.BarShare;
import third.share.tools.ShareTools;
import xh.basic.internet.UtilInternet;
import xh.basic.tool.UtilString;
import acore.logic.XHClick;
import acore.override.activity.base.BaseActivity;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import acore.widget.DownRefreshList;
import amodule.quan.adapter.AdapterCircleUser;
import amodule.quan.view.CircleHeaderView;
import amodule.user.activity.FriendHome;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;

import com.xianghatest.R;

/**
 * 关注圈子列表
 * @author FangRuijiao
 */
public class CircleUser extends BaseActivity implements OnClickListener{
	private DownRefreshList mLvSur;
	private CircleHeaderView headerView;
	private TextView mTvUserNum;
	private ImageView mSoIv;
	private AdapterCircleUser mAdapter;
	private ArrayList<Map<String, String>> mListData;
	private View mBlackView;
	private TextView mBlackViewName;
	private String mCid;
	
	private int currentPage = 0, everyPage = 0;
	private String mMyDuty = "3";
	private int mBackNumber = 0;
	
	public static String mAddManager = "设为管理员",mRemoManager = "取消管理员";
	
	private String mDesc,mImg,mTitle,mCode;
	
	private boolean isHaveLoad = false;
	
	private final static String STATISTICS_ID = "a_quan_member";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initActivity("圈子成员", 2, 0, R.layout.c_cirecle_user_bar_title, R.layout.a_circle_user);
		mCid = getIntent().getStringExtra("cid");
		if(mCid == null || TextUtils.isEmpty(mCid)){
			Toast.makeText(this, "数据错误", Toast.LENGTH_SHORT).show();
			finish();
		}
		mDesc = getIntent().getStringExtra("desc");
		mImg = getIntent().getStringExtra("img");
		mTitle = getIntent().getStringExtra("name");
		mCode = getIntent().getStringExtra("code");
		init();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if(isHaveLoad)
			getCaredUserListData(true);
	}



	/**
	 * 初始化
	 */
	private void init(){
		mSoIv = (ImageView)findViewById(R.id.c_circle_bar_so);
		mSoIv.setVisibility(View.GONE);
		mSoIv.setOnClickListener(this);
		mTvUserNum = (TextView)findViewById(R.id.c_circle_bar_num);
		headerView = new CircleHeaderView(this);
		headerView.setVisibility(View.GONE);
		headerView.initTopView("邀请好友一起玩", getResources().getString(R.color.common_bg), "#333333",new CircleHeaderView.ItemCallback(){

			@Override
			public void onClick(String con) {
				BarShare barShare = new BarShare(CircleUser.this, "圈子成员","生活圈");
				String clickUrl = StringManager.api_circleShare + "/" + mCode;
				barShare.setShare(ShareTools.IMG_TYPE_WEB,mTitle,mDesc + clickUrl, mImg, clickUrl);
				barShare.openShare();
//				STATISTICS_ID
				XHClick.mapStat(CircleUser.this, STATISTICS_ID, "邀请朋友一起玩", "");
			}
		});
		mLvSur = (DownRefreshList)findViewById(R.id.a_circle_user_list);
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
		mLvSur.addHeaderView(headerView);
		LinearLayout ll = new LinearLayout(this);
		ll.setGravity(Gravity.CENTER_VERTICAL);
		LayoutInflater inflater = LayoutInflater.from(this);
		mBlackView = inflater.inflate(R.layout.a_circle_user_item, null);
		mBlackViewName = (TextView)mBlackView.findViewById(R.id.a_circle_user_item_name);
		ImageView mBlackViewIv = (ImageView)mBlackView.findViewById(R.id.a_circle_user_item_iv);
		mBlackViewIv.setImageResource(R.drawable.z_me_ico_mypage);
		mBlackView.setOnClickListener(this);
		mBlackView.setVisibility(View.GONE);
		ll.addView(mBlackView);
		mLvSur.addHeaderView(ll);
	}
	
	
	
	private AdapterCircleUser.CircleUserOptionListener mUserOptionListener = new AdapterCircleUser.CircleUserOptionListener() {
		
		@Override
		public void oRemoManager(final int position) {
			final Map<String, String> map = mListData.get(position);
			String code = map.get("code");
			ReqInternet.in().doPost(StringManager.api_circleCustomerPower, "&type=4" + "&customerId=" + code + "&cid=" + mCid, new InternetCallback(CircleUser.this) {
				
				@Override
				public void loaded(int flag, String url, Object msg) {
					if(flag >= UtilInternet.REQ_OK_STRING){
						map.put("isManager", "hide");
						map.put("addManager", CircleUser.mAddManager);
						mAdapter.notifyDataSetChanged();
					}else{
						Tools.showToast(CircleUser.this, msg.toString());
					}
				}
			});
		}
		
		@Override
		public void oAddManager(final int position) {
			String code = mListData.get(position).get("code");
			ReqInternet.in().doPost(StringManager.api_circleCustomerPower, "&type=3" + "&customerId=" + code + "&cid=" + mCid, new InternetCallback(CircleUser.this) {
				
				@Override
				public void loaded(int flag, String url, Object msg) {
					if(flag >= UtilInternet.REQ_OK_STRING){
						Map<String, String> map = mListData.get(position);
						map.put("isManager", "管理员");
						map.put("addManager", CircleUser.mRemoManager);
						mAdapter.notifyDataSetChanged();
					}else{
						Tools.showToast(CircleUser.this, msg.toString());
					}
				}
			});
		}
		
		@Override
		public void oAddBlack(final View parentView,final int position) {
			String code = mListData.get(position).get("code");
			ReqInternet.in().doPost(StringManager.api_circleCustomerPower, "&type=1" + "&customerId=" + code + "&cid=" + mCid, new InternetCallback(CircleUser.this) {
				
				@Override
				public void loaded(int flag, String url, Object msg) {
					if(flag >= UtilInternet.REQ_OK_STRING){
						mBackNumber ++;
						mBlackViewName.setText("黑名单   (" + mBackNumber + ")");
						mListData.remove(position);
						mLvSur.removeViewInLayout(parentView);
						mAdapter.notifyDataSetChanged();
					}else{
						Tools.showToast(CircleUser.this, msg.toString());
					}
				}
			});
		}

		@Override
		public void onItemClick(int position) {
			Map<String, String> map = mListData.get(position);
			Intent it = new Intent(CircleUser.this,FriendHome.class);
			it.putExtra("code", map.get("code"));
			startActivity(it);
			XHClick.mapStat(CircleUser.this, STATISTICS_ID, "成员列表点击", "");
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
		String getUrl = StringManager.api_circleCustomerList + "?cid=" + mCid + "&page=" + currentPage;
		ReqInternet.in().doGet(getUrl, new InternetCallback(this) {
			@Override
			public void loaded(int flag, String url, Object returnObj) {
				isHaveLoad = true;
				int loadCount = 0;
				if (flag >= UtilInternet.REQ_OK_STRING) {
					//刷新清理数据
					if(isForward) mListData.clear();
					ArrayList<Map<String, String>> listReturn = UtilString.getListMapByJson(returnObj);
					if(listReturn != null && listReturn.size() > 0){
						Map<String, String> data = listReturn.get(0); 
						if(currentPage == 1){
							headerView.setVisibility(View.VISIBLE);
//							boolean isManager = LoginManager.isManager();
//							if(isManager) mMyDuty = "1";
//							else{
								ArrayList<Map<String, String>> currentList = UtilString.getListMapByJson(data.get("current"));
								if(currentList != null && currentList.size() > 0){
									mMyDuty = currentList.get(0).get("duty");
								}
//							}
							if("1".equals(mMyDuty)){
								mAdapter.mRightLenght = mAdapter.mShowAll;
								mBlackView.setVisibility(View.VISIBLE);
								mSoIv.setVisibility(View.VISIBLE);
							}
							else{
								if("2".equals(mMyDuty)){
									mBlackView.setVisibility(View.VISIBLE);
									mSoIv.setVisibility(View.VISIBLE);
								}
								mAdapter.mRightLenght = mAdapter.mShowOne;
							}
							mTvUserNum.setText(data.get("customer_number") + "人");
							mBackNumber = Integer.parseInt(data.get("customer_back_number"));
							mBlackViewName.setText("黑名单   (" + mBackNumber + ")");
						}
						// 解析数据
						ArrayList<Map<String, String>> listList = UtilString.getListMapByJson(data.get("list"));
						for (int i = 0; i < listList.size(); i++) {
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
									mapReturn.put("addManager",mRemoManager);
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
									mapReturn.put("addManager",mAddManager);
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
						loadCount = listList.size();
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
		case R.id.c_circle_bar_so:
			Intent itSo = new Intent(CircleUser.this,CircleUserSo.class);
			itSo.putExtra("cid", mCid);
			itSo.putExtra("type", CircleUserSo.TYPE_CIRCLE_USER);
			itSo.putExtra("title", "圈子成员");
			startActivity(itSo);
			break;
		case R.id.a_circle_black_user_layout:
			Intent it = new Intent(CircleUser.this,CircleUserBlack.class);
			it.putExtra("cid", mCid);
			startActivity(it);
			break;
		}
	}
}
