package amodule.quan.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import acore.override.activity.base.BaseAppCompatActivity;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import acore.widget.DownRefreshList;
import amodule.quan.adapter.AdapterCircle;
import amodule.quan.view.CircleHeaderView;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;

public class CircleRobSofa extends BaseAppCompatActivity implements OnClickListener{
	private DownRefreshList mListView;
	private RelativeLayout mRefreshlayout;
	private static LinearLayout mNoSofaLayout;
	private ImageView mRefreshIcon;
	/** 
	 * 圈子列表的头部局
	 * 包含置顶，公告，活动，发贴界面，发贴失败界面
	 *  */
	private CircleHeaderView mCircleHeaderView;
	
	private static AdapterCircle mAdapter;
	private static ArrayList<Map<String,String>> mData = new ArrayList<>();
	private ArrayList<Map<String,String>> mRobRoNoticeData = new ArrayList<>();
	private int mCurrentPage = 0 , mEveryPageNum = 0;
	private boolean mLoadOver = false;
	private String mCid = "";
	private String mPageTime = "";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initActivity("抢沙发", 2, 0, R.layout.c_view_bar_title, R.layout.a_circle_rob_sofa);
		initView();
		initData();
		initLoad();
	}
	
	/**初始化view*/
	private void initView(){
		mListView = (DownRefreshList) findViewById(R.id.circle_list);
		mCircleHeaderView = new CircleHeaderView(this);
		mListView.addHeaderView(mCircleHeaderView);
		mRefreshlayout = (RelativeLayout) findViewById(R.id.circle_refresh_layout);
		mRefreshIcon = (ImageView) findViewById(R.id.circle_refresh_ico);
		mNoSofaLayout = (LinearLayout) findViewById(R.id.circle_safa_null_layout);
		
		mRefreshlayout.setOnClickListener(this);
	}
	
	/**初始化data*/
	private void initData() {
		Intent intent = getIntent();
		mCid = intent.getStringExtra("cid");
		mAdapter = new AdapterCircle(this, mListView, mData);
	}
	
	/**初始化加载*/
	private void initLoad() {
		if(!mLoadOver){
			loadManager.setLoading(mListView, mAdapter, true, new OnClickListener() {
				@Override
				public void onClick(View v) {
					loadData(false);
				}
			}, new OnClickListener() {
				@Override
				public void onClick(View v) {
					loadData(true);
				}
			});
			mLoadOver = true;
		}
	}
	
	/**
	 * 获取数据 
	 * @param isRefresh
	 * 	是否刷新
	 */
	private void loadData(final boolean isRefresh){
		if(isRefresh){
			mCurrentPage = 0;
			mEveryPageNum = 0;
			mPageTime = "";
		}
		mCurrentPage ++;
		String url = StringManager.api_circleSafaList + "?cid=" + mCid + "&page=" + mCurrentPage + "&pageTime=" + mPageTime;
		loadManager.changeMoreBtn(ReqInternet.REQ_OK_STRING, -1, -1, mCurrentPage,isRefresh);
		ReqInternet.in().doGet(url, new InternetCallback() {
			@Override
			public void loaded(int flag, String url, Object msg) {
				int loadCount = 0;
				if(flag >= ReqInternet.REQ_OK_STRING){
					if(isRefresh){
						mData.clear();
					}
					List<Map<String,String>> returnData = StringManager.getListMapByJson(msg);
					if(returnData.size() > 0){
						for(int index = 0 ,length = returnData.size() ; index < length ; index ++){
							Map<String,String> map = returnData.get(index);
							//请求的第一页数据中，包含公告、置顶、活动的item数据是不记录在每页的数据count中的
							String style = map.get("style");
							if(style != null){
								if(style.equals("2") 
										|| style.equals("3")){
									mRobRoNoticeData.add(map);
								} else if(style.equals("4") ){
									//活动、暂时不处理
									
								}else{
									map.put("dataType", "1");
									map.put("isSafa", "yes");
									//添加是否定位字段
									map.put("isLocation", "1");
									mData.add(map);
									if(!style.equals("5") && !style.equals("6")){
										loadCount++;
									}
								}
							}
							if(!style.equals("6")){
								mPageTime = map.get("floorTime");
							}
						}
					}
					//添加置顶和公告data
					mCircleHeaderView.initMiddleView(mRobRoNoticeData);
				}else{
					mRefreshIcon.clearAnimation();
				}
				if(mEveryPageNum == 0){
					mEveryPageNum = loadCount;
				}
				mAdapter.notifyDataSetChanged();
				mCurrentPage = loadManager.changeMoreBtn(flag, mEveryPageNum, loadCount, mCurrentPage,isRefresh);
				setRefreshIconVisibility(mCurrentPage);
				mListView.onRefreshComplete();
				if (isRefresh)
					mListView.setSelection(1);
				sofaIsNull();
			}
		});
	}
	
	/**
	 * 
	 * @param code
	 * 	贴子的code，不能为null
	 */
	public static void removeSofaItem(String code){
		if(mData != null && mAdapter != null){
			for(int index = 0 ;index < mData.size() ; index ++){
				Map<String,String> map = mData.get(index);
				if(code != null && code.equals(map.get("code"))){
					mData.remove(index);
					break;
				}
			}
			mAdapter.notifyDataSetChanged();
			sofaIsNull();
		}
	}
	
	private static void sofaIsNull(){
		if(mNoSofaLayout == null){
			return;
		}
		if(mData.size() > 0){
			mNoSofaLayout.setVisibility(View.GONE);
		}else{
			mNoSofaLayout.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * 判断刷新按钮是否显示
	 * 使用当前页数判断
	 * */
	private void setRefreshIconVisibility(int currentPage) {
		if (currentPage >= 3) {
			mRefreshlayout.setVisibility(View.VISIBLE);
			mRefreshIcon.clearAnimation();
		}else {
			mRefreshlayout.setVisibility(View.GONE);
			mRefreshIcon.clearAnimation();
		}
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.circle_refresh_layout:
			if (ToolsDevice.getNetActiveState(this)) {
				Animation refreshAnim = AnimationUtils.loadAnimation(this, R.anim.feekback_progress_anim);
				mRefreshIcon.startAnimation(refreshAnim);
				loadData(true);
			} else {
				Animation single = AnimationUtils.loadAnimation(this, R.anim.single_quan);
				mRefreshIcon.startAnimation(single);
				Tools.showToast(this, "网络错误，请检查网络或重试");
			}
			break;
		}
	}

}
