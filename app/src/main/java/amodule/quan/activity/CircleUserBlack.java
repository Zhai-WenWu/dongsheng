package amodule.quan.activity;

import java.util.ArrayList;
import java.util.Map;

import xh.basic.internet.UtilInternet;
import xh.basic.tool.UtilString;
import acore.override.activity.base.BaseActivity;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import acore.widget.DownRefreshList;
import amodule.quan.adapter.AdapterCircleUser;
import amodule.user.activity.FriendHome;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;

import com.xianghatest.R;

public class CircleUserBlack extends BaseActivity implements OnClickListener {
	
	private DownRefreshList mLvSur;
	private AdapterCircleUser mAdapter;
	private ArrayList<Map<String, String>> mListData;
	private String mCid;
	
	private int currentPage = 0, everyPage = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initActivity("黑名单", 2, 0, R.layout.c_cirecle_user_bar_title, R.layout.a_circle_black_user);
		mCid = getIntent().getStringExtra("cid");
		if(mCid == null || TextUtils.isEmpty(mCid)){
			Toast.makeText(this, "数据错误", Toast.LENGTH_SHORT).show();
			finish();
		}
		init();
	}
	
	/**
	 * 初始化
	 */
	private void init(){
		findViewById(R.id.c_circle_bar_num).setVisibility(View.GONE);
		ImageView soIv = (ImageView)findViewById(R.id.c_circle_bar_so);
		soIv.setOnClickListener(this);
		mLvSur = (DownRefreshList)findViewById(R.id.a_circle_black_user_list);
		mListData = new ArrayList<Map<String, String>>();
		mAdapter = new AdapterCircleUser(this,mLvSur,mUserOptionListener, mListData, R.layout.a_circle_user_item,
				new String[]{"nickName","img","isGourmet","isQuan","isManager","addBlack","addManager"},
				new int[]{ R.id.a_circle_user_item_name,R.id.a_circle_user_item_iv,R.id.a_circle_user_item_gourmet,
							R.id.a_circle_user_item_quan,R.id.a_circle_user_item_manager,
							R.id.a_circle_user_item_add_black,R.id.a_circle_user_item_add_manager});
		
		mAdapter.imgResource=R.drawable.bg_round_zannum;
		mAdapter.roundType = 1;
		mAdapter.roundImgPixels = ToolsDevice.dp2px(this, 500);
		mAdapter.mRightLenght = mAdapter.mShowOne;
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
	}
	
	private AdapterCircleUser.CircleUserOptionListener mUserOptionListener = new AdapterCircleUser.CircleUserOptionListener() {
		
		@Override
		public void oRemoManager(int position) {}
		
		@Override
		public void oAddManager(int position) {}
		
		@Override
		public void oAddBlack(final View parentView,final int position) {
			String code = mListData.get(position).get("code");
			ReqInternet.in().doPost(StringManager.api_circleCustomerPower, "&type=2" + "&customerId=" + code + "&cid=" + mCid, new InternetCallback(CircleUserBlack.this) {
				
				@Override
				public void loaded(int flag, String url, Object msg) {
					if(flag >= UtilInternet.REQ_OK_STRING){
						mListData.remove(position);
						mLvSur.removeViewInLayout(parentView);
						mAdapter.notifyDataSetChanged();
					}else{
						Tools.showToast(CircleUserBlack.this, msg.toString());
					}
				}
			});
		}

		@Override
		public void onItemClick(int position) {
			Map<String, String> map = mListData.get(position);
			Intent it = new Intent(CircleUserBlack.this,FriendHome.class);
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
		String getUrl = StringManager.api_circleCustomerBlackList + "?cid=" + mCid + "&page=" + currentPage;
		ReqInternet.in().doGet(getUrl, new InternetCallback(this) {
			@Override
			public void loaded(int flag, String url, Object returnObj) {
				int loadCount = 0;
				if (flag >= UtilInternet.REQ_OK_STRING) {
					//刷新清理数据
					if(isForward) mListData.clear();
					ArrayList<Map<String, String>> listReturn = UtilString.getListMapByJson(returnObj);
					if(listReturn != null && listReturn.size() > 0){
						mLvSur.setVisibility(View.VISIBLE);
						Map<String, String> data = listReturn.get(0); 
						// 解析数据
						ArrayList<Map<String, String>> listList = UtilString.getListMapByJson(data.get("list"));
						for (int i = 0; i < listList.size(); i++) {
							Map<String, String> mapReturn = listList.get(i);
							/** 职务  */
							//如果当前用户是圈子或者管理员
							mapReturn.put("isQuan","");
							mapReturn.put("isManager","");
							mapReturn.put("addBlack","移出黑名单");
							mapReturn.put("addManager","");
							if(mapReturn.containsKey("isGourmet") && "2".equals(mapReturn.get("isGourmet")))
								mapReturn.put("isGourmet","ico" + R.drawable.z_user_gourmet_ico);
							else
								mapReturn.put("isGourmet","hide");
							mListData.add(mapReturn);
						}
						loadCount = listList.size();
						mAdapter.notifyDataSetChanged();
					}else if(currentPage == 1){
						mLvSur.setVisibility(View.GONE);
						findViewById(R.id.a_circle_black_user_hint).setVisibility(View.VISIBLE);
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
			Intent itSo = new Intent(CircleUserBlack.this,CircleUserSo.class);
			itSo.putExtra("cid", mCid);
			itSo.putExtra("type", CircleUserSo.TYPE_CIRCLE_USER_BLACK);
			itSo.putExtra("title", "黑名单");
			startActivity(itSo);
			break;
		}
	}
}
