/**
 * 
 * @author intBird 20140213.
 * 
 */
package amodule.user.view;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xianghatest.R;

import java.util.ArrayList;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.LoginManager;
import acore.logic.load.LoadManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import acore.widget.DownRefreshList;
import amodule.quan.view.NormalContentView;
import amodule.quan.view.NormarlContentItemImageVideoView;
import amodule.quan.view.VideoImageView;
import amodule.user.activity.FriendHome;
import amodule.user.adapter.AdapterUserSubject;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;
import xh.basic.internet.UtilInternet;
import xh.basic.tool.UtilLog;
import xh.basic.tool.UtilString;

public class UserHomeSubject extends TabContentView {
	private View headView;
	private FriendHome mAct;
	private String userCode = "";
	private boolean isMyselft = false;
	private String tongjiId = "a_user";
	private LoadManager loadManager;
	public ArrayList<Map<String, String>> listDataMySb;
	public AdapterUserSubject adapter;

	private int currentPage = 0, everyPage = 0;
	private boolean isRefresh = false;
	//对视频的处理
	private LinearLayout video_layout;
	private VideoImageView videoImageView;
	private boolean isAutoPaly = false;
	private int headerCount=0;//存在listview头数据

	private TextView subjectNum;
	private LinearLayout mTabMainMyself;
	private TextView mEmpty;

	public UserHomeSubject(FriendHome act, String code) {
		view = View.inflate(act, R.layout.myself_subject, null);
		this.mAct = act;
		userCode = code;
		if(!TextUtils.isEmpty(LoginManager.userInfo.get("code")) && LoginManager.userInfo.get("code").equals(userCode)){
			isMyselft = true;
			tongjiId = "a_my";
		}
		// 设定scrollLayout的高度
		scrollLayout = mAct.scrollLayout;
		// 滑动设置
		backLayout = mAct.backLayout;
		friend_info = mAct.friend_info;
		init();
	}

	@Override
	public void onResume(String tag) {
		super.onResume(tag);
		theListView.setSelection(1);
		if(subjectNum == null) {
			mTabMainMyself = (LinearLayout) mAct.findViewById(R.id.a_user_home_title_tab);
			subjectNum = (TextView) mTabMainMyself.getChildAt(0).findViewById(R.id.tab_data);
		}
	}

	private void init() {
		// 结果显示
		loadManager = mAct.loadManager;
		mEmpty = (TextView) view.findViewById(R.id.tv_noData);
		theListView = (DownRefreshList) view.findViewById(R.id.list_myself_subject);
		theListView.setDivider(null);
		listDataMySb = new ArrayList<>();
		adapter = new AdapterUserSubject(mAct, theListView, listDataMySb, 0, null, null,tongjiId,new NormalContentView.DeleteSubjectCallBack() {
			@Override
			public void deleteSubjectPosition(int position) {
				String numS = String.valueOf(subjectNum.getText());
				if(!TextUtils.isEmpty(numS)){
					try{
						int num = Integer.parseInt(numS);
						subjectNum.setText(String.valueOf(--num));
					}catch (Exception e){
						e.printStackTrace();
					}
				}
				listDataMySb.remove(position);
				adapter.notifyDataSetChanged();
			}
		});
		adapter.scaleType = ScaleType.CENTER_CROP;
		adapter.isAnimate = true;
		isAutoPaly = "wifi".equals(ToolsDevice.getNetWorkSimpleType(mAct));
	}

	@Override
	public void initLoad() {
		currentPage = 0;
		isRefresh = true;
		theListView.setVisibility(View.GONE);
		if (theListView.getAdapter() == null) {
			headView = new View(mAct);
			setHeadViewHeight();
		}
		loadManager.setLoading(theListView, adapter, scrollLayout, backLayout, headView, new OnClickListener() {
			@Override
			public void onClick(View v) {
				currentPage++;
				loadFromServer();
			}
		}, new OnClickListener() {
			@Override
			public void onClick(View v) {
				mAct.doReload();
			}
		}, listDataMySb.size() == 0, new LoadManager.ViewScrollCallBack() {
			final int topRedundant = Tools.getDimen(mAct,R.dimen.dp_45) + Tools.getStatusBarHeight(mAct);
			final int bottomRedundant = Tools.getDimen(mAct,R.dimen.dp_50);
			final int Min = topRedundant;
			final int Max = (ToolsDevice.getWindowPx(mAct).heightPixels  - topRedundant - bottomRedundant) * 3 / 5 + topRedundant;
			int currentPlayPosition = -1;
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
			}
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				Log.i("zhangyujian","onScrollStateChanged");
				if(videoImageView!=null&&videoImageView.getIsPlaying()){//滑动暂停
					stopVideo();
				}
				if(!isAutoPaly){
					return;
				}
				final int length = view.getChildCount();
				if(scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE){
					int count = 0;
					int index = 0;
					for(; index < length ; index ++){
						View itemView = view.getChildAt(index);
						int top = itemView.getTop();
						int height = itemView.getHeight();
						final int value = height * 4 / 7 + top;
						if(itemView instanceof NormalContentView){
							if(value <= Max && value >= Min){
								((NormalContentView)itemView).startVideoView();
								currentPlayPosition = view.getPositionForView(itemView);
								Log.i("zhangyujian","自动数据的位置:::"+((NormalContentView)itemView).getPositionNow());
								setVideoLayout(itemView,((NormalContentView)itemView).getPositionNow());
//                              mAdapter.setCurrentPlayPosition(currentPlayPosition - mListview.getHeaderViewsCount());
							}else{
								count++;
//                              ((NormalContentView)itemView).stopVideoView();
							}
						}
					}
				}
			}
		});
//		if(!isBlankSpace)mAct.loadManager.hideProgressBar();
		adapter.setVideoClickCallBack(new NormarlContentItemImageVideoView.VideoClickCallBack() {
			@Override
			public void videoImageOnClick(int position) {
				int firstVisiPosi = theListView.getFirstVisiblePosition();
				//要获得listview的第n个View,则需要n减去第一个可见View的位置。+1是因为有header
				View parentView = theListView.getChildAt(position-firstVisiPosi + 2);//测试数据有2个header
				setVideoLayout(parentView,position);
			}
		});
	}

	/**
	 * 处理view,video
	 * @param parentView
	 * @param position
	 */
	private void setVideoLayout(View parentView, final int position){
		if(listDataMySb.get(position).containsKey("selfVideo") && !TextUtils.isEmpty(listDataMySb.get(position).get("selfVideo"))) {
			Map<String, String> videoData = StringManager.getFirstMap(listDataMySb.get(position).get("selfVideo"));
			if(videoImageView==null)
				videoImageView = new VideoImageView(mAct,false);
			videoImageView.setImageBg(videoData.get("sImgUrl"));
			videoImageView.setVideoData(videoData.get("videoUrl"));
			videoImageView.setVisibility(View.VISIBLE);
			if (video_layout != null && video_layout.getChildCount() > 0) {
				video_layout.removeAllViews();
			}

			video_layout = (LinearLayout) parentView.findViewById(R.id.video_layout);
			video_layout.addView(videoImageView);
			videoImageView.onBegin();
			videoImageView.setVideoClickCallBack(new VideoImageView.VideoClickCallBack() {
				@Override
				public void setVideoClick() {
					stopVideo();
					goNextActivity(position);
				}
			});
		}
	}
	/**
	 * 暂停播放
	 */
	private void stopVideo(){
		if(videoImageView!=null){
			videoImageView.onVideoPause();
			videoImageView.setVisibility(View.GONE);
		}
	}
	/**
	 * 详情页
	 */
	private void goNextActivity(int position){
		String isSafa="";
		Map<String,String> map = listDataMySb.get(position);
		if (map.containsKey("isSafa"))
			isSafa = map.get("isSafa");
		if (map.containsKey("style") && map.get("style").equals("6"))
			isSafa = "qiang";
		AppCommon.openUrl(mAct, "subjectInfo.app?code=" + map.get("code") + "&isSafa=" + isSafa, true);
	}
	private void setHeadViewHeight(){
		final int tabHost_h = Tools.getDimen(mAct, R.dimen.dp_51);
		final int bigImg_h = Tools.getDimen(mAct, R.dimen.dp_200) + Tools.getStatusBarHeight(mAct);
		final int userinfo_h = Tools.getTargetHeight(friend_info);
		try {
			if (friend_info.getText() == null || friend_info.getText().toString().equals(""))
				headView.setLayoutParams(new AbsListView.LayoutParams(
						android.view.ViewGroup.LayoutParams.MATCH_PARENT,
						tabHost_h + bigImg_h));
			else
				headView.setLayoutParams(new AbsListView.LayoutParams(
						android.view.ViewGroup.LayoutParams.MATCH_PARENT,
						tabHost_h + bigImg_h + userinfo_h));
		} catch (Exception e) {
			UtilLog.reportError("MyselfSubject头部局异常", e);
		}

	}

	private void loadFromServer() {
		loadManager.changeMoreBtn(theListView,UtilInternet.REQ_OK_STRING, -1, -1, currentPage,listDataMySb.size() == 0);
		String getUrl = StringManager.api_getPostByCode + "?code=" + userCode + "&page=" + currentPage;
		ReqInternet.in().doGet(getUrl, new InternetCallback(mAct) {
			@Override
			public void loaded(int flag, String url, Object returnObj) {
				loadData(flag, returnObj);
				setHeadViewHeight();
			}
		});
	}

	private void loadData(int flag, Object returnObj) {
		int loadCount = 0;
		if (flag >= UtilInternet.REQ_OK_STRING) {
			loadCount = parseInfo(returnObj);
		} else {
			Tools.showToast(mAct, returnObj.toString());
		}
		if (everyPage == 0)
			everyPage = loadCount;
		currentPage = loadManager.changeMoreBtn(theListView,flag, everyPage, loadCount, currentPage,listDataMySb.size() == 0);
	}

	/**
	 * 解析数据
	 * @param returnObj 要解析的数据
	 */
	public int parseInfo(Object returnObj) {
		if (isRefresh) {
			listDataMySb.clear();
			isRefresh = false;
		}
		int loadCount = 0;
		ArrayList<Map<String, String>> listMySelf = UtilString.getListMapByJson(returnObj);
		if(listMySelf.size() > 0) {
			listMySelf = StringManager.getListMapByJson(listMySelf.get(0).get("data"));
			// subject,Like;
			for (int i = 0; i < listMySelf.size(); i++) {
				loadCount++;
				Map<String, String> map = listMySelf.get(i);
				if (isMyselft) map.put("isMe", "2");
				else map.put("isMe", "1");
				if (!TextUtils.isEmpty(map.get("num")) && !map.get("num").equals("1"))
					map.put("title", "【跟贴】" + map.get("title"));
				listDataMySb.add(map);
			}
		}
		// 如果总数据为空,显示没有美食贴
		if (listDataMySb.size() == 0) {
			RelativeLayout.LayoutParams emptyParams = (RelativeLayout.LayoutParams) mEmpty.getLayoutParams();
			emptyParams.topMargin = mTabMainMyself.getTop() + mTabMainMyself.getHeight();
			mEmpty.setVisibility(View.VISIBLE);
		} else {
			mEmpty.setVisibility(View.GONE);
			adapter.notifyDataSetChanged();
			theListView.setVisibility(View.VISIBLE);
		}
		return loadCount;
	}

	/**
	 * 失去焦点
	 */
	public void onViewPause(){
		stopVideo();
	}
}
