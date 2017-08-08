package amodule.quan.activity;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.xianghatest.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import acore.logic.AppCommon;
import acore.override.activity.base.BaseActivity;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import acore.widget.DownRefreshList;
import amodule.quan.adapter.AdapterCircle;
import amodule.quan.view.CircleHeaderView;
import amodule.quan.view.CircleHeaderView.ItemCallback;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;
import third.share.BarShare;

/**
 * 圈子模块页面
 * 
 * @author yujian
 *
 */
public class CircleModule extends BaseActivity implements OnClickListener {

	private DownRefreshList circle_list;
	private int mCurrentPage = 0;
	private int mEveryPageNum = 0;
	private String mPageTime = "";
	private String cid, mid, name,style;
	private AdapterCircle mAdapter;
	private ArrayList<Map<String, String>> mListData = new ArrayList<Map<String, String>>();
	private ImageView mRefreshIcon;
	private ArrayList<Map<String, String>> mRobRoNoticeData = new ArrayList<>();
	private CircleHeaderView mCircleHeaderView;
	private boolean mLoadOver = false;
	private String noDataNotice_1 = "" , noDataNotice_2 = "",noDataUrl = "",noDataName="";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initActivity("", 0, 0, R.layout.c_view_bar_title_circle_home, R.layout.a_circle_module);
		init();
	}

	private void init() {
		Bundle bundle = this.getIntent().getExtras();
		if (bundle != null) {
			cid = bundle.getString("cid");
			mid = bundle.getString("mid");
			name = bundle.getString("name");
			style= bundle.getString("style");
		}
		initView();
		initData();
	}

	private void initView() {
		TextView title = (TextView) findViewById(R.id.title);
		if (!TextUtils.isEmpty(name)) {
			title.setText(name);
		}
		findViewById(R.id.circle_share).setVisibility(View.GONE);
		circle_list = (DownRefreshList) findViewById(R.id.circle_list);
		mRefreshIcon = (ImageView) findViewById(R.id.circle_refresh_ico);
		findViewById(R.id.circle_refresh_layout).setOnClickListener(this);

	}

	private void initData() {
		mAdapter = new AdapterCircle(this, circle_list, mListData);
		mCircleHeaderView = new CircleHeaderView(this);
		circle_list.addHeaderView(mCircleHeaderView);
		if (!mLoadOver) {
			loadManager.setLoading(circle_list, mAdapter, true, new OnClickListener() {
				@Override
				public void onClick(View v) {
					setRquest(false);
				}
			}, new OnClickListener() {
				@Override
				public void onClick(View v) {
					setRquest(true);
				}
			});
			mLoadOver = true;
		}
	}

	private void setRquest(final boolean isRefresh) {
		if (isRefresh) {
			mCurrentPage = 0;
			mEveryPageNum = 0;
			mPageTime = "";
		}
		mCurrentPage++;
		String url = StringManager.api_circleSubjectList + "?cid=" + cid + "&mid=" + mid;
		String param = "&page=" + mCurrentPage + "&pageTime=" + mPageTime;
		// 更新加载按钮状态
		loadManager.changeMoreBtn(circle_list, ReqInternet.REQ_OK_STRING, -1, -1, mCurrentPage, isRefresh);
		if (isRefresh) {
			loadManager.hideProgressBar();
		}
		ReqInternet.in().doGet(url + param, new InternetCallback(this) {

			@Override
			public void loaded(int flag, String url, Object msg) {

				int loadCount = 0;
				if (flag >= ReqInternet.REQ_OK_STRING) {
					if (isRefresh) {
						mListData.clear();
					}
					List<Map<String, String>> returnData = StringManager.getListMapByJson(msg);
					String promptJson =  returnData.get(0).get("prompt");
					List<Map<String,String>> promptData = StringManager.getListMapByJson(promptJson);
					if(promptData.size() > 0){
						Map<String,String> prompt = promptData.get(0);
						noDataNotice_1 = prompt.get("desc");
						noDataNotice_2 = prompt.get("aStr");
						noDataUrl = prompt.get("aUrl");
						noDataName= prompt.get("name");
					}
					returnData = StringManager.getListMapByJson(returnData.get(0).get("data"));
					if (returnData.size() > 0) {
						for (int index = 0, length = returnData.size(); index < length; index++) {
							Map<String, String> map = returnData.get(index);
							// 请求的第一页数据中，包含公告、置顶、活动的item数据是不记录在每页的数据count中的
							String style = map.get("style");
							if (style != null) {
								if (style.equals("2") || style.equals("3")) {
									mRobRoNoticeData.add(map);
								} else if (style.equals("4")) {
									// 活动、暂时不处理

								} else {
									map.put("dataType", "1");
									map.put("isSafa", "yes");
									// 添加是否定位字段
									mListData.add(map);
									if (!style.equals("5") && !style.equals("6")) {
										loadCount++;
									}
								}
							}
							if (!style.equals("6")) {
								mPageTime = map.get("floorTime");
							}
						}
					}
					// 添加置顶和公告data
					mCircleHeaderView.initMiddleView(mRobRoNoticeData);
					
				} else {
					mRefreshIcon.clearAnimation();
				}
				if (mEveryPageNum == 0) {
					mEveryPageNum = loadCount;
				}
				mAdapter.notifyDataSetChanged();
				mCurrentPage = loadManager.changeMoreBtn(flag, mEveryPageNum, loadCount, mCurrentPage, isRefresh);
				setRefreshIconVisibility(mCurrentPage);
				circle_list.onRefreshComplete();
				if (isRefresh)
					circle_list.setSelection(1);
				//如果没有数据显示提示
				if(mListData.size() ==0){
					if(!TextUtils.isEmpty(style)&&"nearby".equals(style)){
						mCircleHeaderView.showMyViewNoData(createView());
					}else{
						mCircleHeaderView.showNoDataView(noDataNotice_1, noDataNotice_2, new ItemCallback() {
							@Override
							public void onClick(String content) {
								if(!TextUtils.isEmpty(noDataUrl)){
									AppCommon.openUrl(CircleModule.this, noDataUrl, true);
								}
							}
						});
					}
//					if(loadMore != null){
//						loadMore.setVisibility(View.INVISIBLE);
//					}
				}else{
					mCircleHeaderView.hideNoDataView();
				}
			}
		});
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.circle_refresh_layout:
			if (ToolsDevice.getNetActiveState(this)) {
				Animation refreshAnim = AnimationUtils.loadAnimation(this, R.anim.feekback_progress_anim);
				mRefreshIcon.startAnimation(refreshAnim);
				setRquest(true);
			} else {
				Animation single = AnimationUtils.loadAnimation(this, R.anim.single_quan);
				mRefreshIcon.startAnimation(single);
				Tools.showToast(this, "网络错误，请检查网络或重试");
			}
			break;

		}
	}

	/**
	 * 判断刷新按钮是否显示 使用当前页数判断
	 * */
	private void setRefreshIconVisibility(int currentPage) {
		if (currentPage >= 3) {
			findViewById(R.id.circle_refresh_layout).setVisibility(View.VISIBLE);
			mRefreshIcon.clearAnimation();
		} else {
			findViewById(R.id.circle_refresh_layout).setVisibility(View.GONE);
			mRefreshIcon.clearAnimation();
		}
	}
	private View createView(){
		View view=LayoutInflater.from(this).inflate(R.layout.circle_item_no_nearby_data, null);
		view.findViewById(R.id.share_pepole_tv).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startShare();
			}
		});
		return view;
	}

	private void startShare() {
		String imgType = BarShare.IMG_TYPE_RES;
		String title = "香哈菜谱，让“吃饭”花样多多";
		String clickUrl = "http://a.app.qq.com/o/simple.jsp?pkgname=com.xianghatest";// 此地址为应用商地址，运营要求！
		String content = "自从有了香哈，每天都有不同吃法，真是又好用又省心，家人都称赞我是一级大厨！一起来玩吧。";
		String imgUrl = "" + R.drawable.umen_share_launch;

		Resources res = getResources();

		Bitmap bmp = BitmapFactory.decodeResource(res, R.drawable.share_launcher);
		BarShare barShare = new BarShare(this, "邀请好友","");

		if (bmp == null) {
			barShare.setShare(imgType, title, content, imgUrl, clickUrl);
		} else {
			barShare.setShare(title, content, bmp, clickUrl);
		}
		barShare.openShare();
	}

}
