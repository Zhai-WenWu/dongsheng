package amodule.main.view.home;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.xianghatest.R;

import java.util.ArrayList;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.XHClick;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.widget.GalleryViewPager;
import acore.widget.HomeSwitchImage;
import acore.widget.HomeSwitchImage.OnSyncDataDelegate;
import acore.widget.ScrollLinearListLayout;
import acore.widget.ZoomLayout;
import acore.widget.ZoomLayout.CriticalHeightDelegate;
import amodule.main.Main;
import amodule.main.activity.MainHomePageNew;
import amodule.search.avtivity.HomeSearch;
import aplug.feedback.activity.Feedback;

@SuppressLint("ClickableViewAccessibility")
public class HomeHeaderAndListControl implements OnClickListener{
	/** 首页的对象 */
	private MainHomePageNew mHomePage;
	/**  */
	private ScrollLinearListLayout mHeaderListView;
	/** 头部缩放动画布局 */
	private ZoomLayout mZommLayout;
	/** 加载贴子的listview */
	private ListView mListView;
	/** 头部三餐的Gallery */
	private GalleryViewPager mGalleryViewPager;
	/** 显示Gallery的指示器的layout */
	private LinearLayout mGalleryIndicatorLayout;
	/** 跟随页面滑动的搜索框 */
	private RelativeLayout mFakeSearchLayout;
	/** 隐藏在页面顶部的搜索框 */
	private RelativeLayout mRealSearchLayout;
	
	private ArrayList <HomeSwitchImage> mSwitchImageArray;
	private int mStateBarHeight = -1;
	private boolean mIsStartSpecialEffects = true;
	
	public HomeHeaderAndListControl(MainHomePageNew homePage){
		this.mHomePage = homePage;
		initView();
		setListener();
	}

	private void initView() {
		mRealSearchLayout = (RelativeLayout) mHomePage.findViewById(R.id.a_home_search_real_layout);
		mFakeSearchLayout = (RelativeLayout) mHomePage.findViewById(R.id.a_home_search_fake_layout);
		if(Tools.isShowTitle()) {
			int dp_45 = Tools.getDimen(mHomePage, R.dimen.dp_45);
			int height = dp_45 + Tools.getStatusBarHeight(mHomePage);
			RelativeLayout all_title_home = (RelativeLayout) mHomePage.findViewById(R.id.all_title_home);
			RelativeLayout.LayoutParams layout = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, height);
			all_title_home.setLayoutParams(layout);
			all_title_home.setPadding(0, Tools.getStatusBarHeight(mHomePage), 0, 0);
		}
		mHeaderListView = (ScrollLinearListLayout) mHomePage.findViewById(R.id.a_home_stick_list);
		mZommLayout = (ZoomLayout) mHomePage.findViewById(R.id.a_home_zoom_layout);
		mGalleryIndicatorLayout = (LinearLayout) mHomePage.findViewById(R.id.a_home_gallery_indicator);
		//设置可缩放的layout
		mHeaderListView.setZoomLayout(mZommLayout);
		mGalleryViewPager = (GalleryViewPager) mHomePage.findViewById(R.id.a_home_gallery);
		//获取listview
		mListView = mHeaderListView.getListView();
		mListView.post(new Runnable() {
			@Override
			public void run() {
				//获取状态栏高度
				Rect outRect = new Rect();  
				mHomePage.getWindow().getDecorView().getWindowVisibleDisplayFrame(outRect); 
				mStateBarHeight = outRect.top;
			}
		});
		mIsStartSpecialEffects = Tools.getPhoneInformation(mHomePage);
		mZommLayout.setIsZoom(mIsStartSpecialEffects);
	}
	
	/** 设置监听 */
	private void setListener() {
		//设置缩放是切换图片的listener
		mZommLayout.setCriticalHeightDelegate(new CriticalHeightDelegate() {
			@Override
			public void onActivate() {
				XHClick.mapStat(mHomePage, MainHomePageNew.STATISTICS_SWITCH_ID, "三餐推荐", "下拉切换");
				if(mGalleryViewPager != null && mSwitchImageArray != null){
					int index = mGalleryViewPager.getCurrentItem();
					if(index < mSwitchImageArray.size()){
						HomeSwitchImage switchImage = mSwitchImageArray.get(index);
						if(switchImage != null){
							switchImage.switchImage(1);
						}
					}
				}
			}
		});
		//设置列表滑动式两个搜索框的显示和隐藏
		mHeaderListView.addOnScrollListener(new OnScrollListener() {
			@Override public void onScrollStateChanged(AbsListView view, int scrollState) {}
			
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				if (mFakeSearchLayout == null || mRealSearchLayout == null || mHomePage == null)
					return;
				int[] location = new int[2];
				mFakeSearchLayout.getLocationOnScreen(location);
				if(mStateBarHeight != -1){
					if(location[1] - mStateBarHeight <= Tools.getStatusBarHeight(mHomePage)){
						mFakeSearchLayout.setVisibility(View.GONE);
						mRealSearchLayout.setVisibility(View.VISIBLE);
						mHomePage.findViewById(R.id.all_title_home).setVisibility(View.VISIBLE);
					}else{
						mFakeSearchLayout.setVisibility(View.VISIBLE);
						mRealSearchLayout.setVisibility(View.GONE);
						mHomePage.findViewById(R.id.all_title_home).setVisibility(View.GONE);
					}
				}
			}
		});
		mHeaderListView.setTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (Main.allMain != null && Main.allMain.getBuoy() != null) {
					if ("2".equals(Main.allMain.getBuoy().getFloatIndex())) {
						Main.allMain.getBuoy().sendEmptyMessage(1);
					}
				}
				return false;
			}
		});
		mRealSearchLayout.setOnClickListener(ScrollLinearListLayout.getOnClickListener(this));
		mFakeSearchLayout.setOnClickListener(ScrollLinearListLayout.getOnClickListener(this));
		mHomePage.findViewById(R.id.a_home_search_layout).setOnClickListener(this);
		mHomePage.findViewById(R.id.z_home_feedbak_layout).setOnClickListener(this);
	}

	/** 再次调用相当于刷新 */
	public void setData(Map<String,String> map){
		setGallery(StringManager.getListMapByJson(map.get("recommend")));
	}

	public void setGallery(final ArrayList<Map<String, String>> sliderList) {
		//如果views不为空，则clear数据
		if(mSwitchImageArray == null){
			mSwitchImageArray = new ArrayList<>();
		}else{
			mSwitchImageArray.clear();
		}
		//移除Gallery的Indecator
		if(mGalleryIndicatorLayout != null && mGalleryIndicatorLayout.getChildCount() > 0){
			mGalleryIndicatorLayout.removeAllViews();
		}
		for (int i = 0; i < sliderList.size(); i++) {
			Map<String, String> map = sliderList.get(i);
			mSwitchImageArray.add(getGalleryView(map));
			addIndicator();
		}
		//当数量大于1时，需要添加头尾，按照《晚，早，中，晚，早》顺序
		if (sliderList.size() > 1) {
			mSwitchImageArray.add(0, getGalleryView(sliderList.get(sliderList.size() - 1)));
			mSwitchImageArray.add(getGalleryView(sliderList.get(0)));
			//添加首尾两个view的同步显示
			setOnSyncDataDelegate(1,sliderList.size());
			setOnSyncDataDelegate(3,sliderList.size());
		}
		mGalleryViewPager.init(mSwitchImageArray, 10000,false, new GalleryViewPager.Helper() {
			@Override 
			public void onChange(View view , int position) {
				XHClick.mapStat(mHomePage, MainHomePageNew.STATISTICS_SWITCH_ID, "三餐推荐", "左右切换");
				//设置当前indicator显示
				setCurrentIndicator(position);
			}

			@Override
			public void onClick(View view, int position) {
				//这是Gallery中view的点击
				Map<String, String> map = sliderList.get(position);
				String url = map.get("url");
				if(!TextUtils.isEmpty(url)){
					XHClick.track(view.getContext(), "点击三餐推荐");
					AppCommon.openUrl(mHomePage, url, true);
					XHClick.mapStat(mHomePage , MainHomePageNew.STATISTICS_ID, "三餐和搜索", "三餐推荐");
				}
			}
		},true);
		//根据time选中当前时段的餐
		if (sliderList.size() > 0 && sliderList.get(0).containsKey("time")) {
			String time = sliderList.get(0).get("time");
			if (time != "" && time != null) {
				int position = Integer.parseInt(time);
				mGalleryViewPager.setCurrentItem(position);
				setCurrentIndicator(position - 1);
			}
		}
	}
	
	/** 设置view的同步更新 */
	private void setOnSyncDataDelegate(final int i , final int size) {
		mSwitchImageArray.get(i).setOnSyncDataDelegate(new OnSyncDataDelegate() {
			@Override
			public void onSyncCurrentImage(int nextIndex, Bitmap nextBitmap, String hasVideo) {
				HomeSwitchImage switchImage = mSwitchImageArray.get((i + size) % (size * 2));
				if(switchImage != null){
					switchImage.setCurrentImage(nextIndex, nextBitmap, hasVideo);
					switchImage.setCurrentFakeImage(nextBitmap, hasVideo);
				}
			}
		});
	}

	/** 设置当前选中的indicator */
	private void setCurrentIndicator(int position) {
		final int length = mGalleryIndicatorLayout.getChildCount();
		for(int index = 0 ; index < length ; index ++){
			View indicator = mGalleryIndicatorLayout.getChildAt(index);
			if(indicator != null){
				indicator.setSelected(position == index);
			}
		}
	}
	
	/** 添加Gallery的indicator */
	private void addIndicator() {
		try{
			ImageView view = new ImageView(mHomePage);
			int padding = Tools.getDimen(mHomePage, R.dimen.dp_2_5);
			view.setPadding(padding, 0, padding, 0);
			view.setImageResource(R.drawable.selector_home_gallery_indicator);
			int width = Tools.getDimen(mHomePage, R.dimen.dp_10);
			mGalleryIndicatorLayout.addView(view, width,ViewGroup.LayoutParams.MATCH_PARENT);
		}catch (Exception e){e.printStackTrace();}

	}

	/**
	 * 获取轮转图中一个view
	 * @param map
	 * @return
	 */
	private HomeSwitchImage getGalleryView(Map<String, String> map) {
		HomeSwitchImage switchImage = new HomeSwitchImage(mHomePage);
		switchImage.initData(map);
		//如果开启特效需要开启图片预读
		if(mIsStartSpecialEffects){
			switchImage.preLoadImage();
		}
		return switchImage;
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.a_home_search_real_layout:
			break;
		case R.id.a_home_search_layout:
		case R.id.a_home_search_fake_layout:
			XHClick.track(mFakeSearchLayout.getContext(), "点击首页的搜索框");
			XHClick.mapStat(mHomePage , MainHomePageNew.STATISTICS_ID, "三餐和搜索", "搜索");
			Intent search = new Intent(mHomePage , HomeSearch.class);
			mHomePage.startActivity(search);
			break;
		case R.id.z_home_feedbak_layout:
			XHClick.mapStat(mHomePage , MainHomePageNew.STATISTICS_ID, "意见反馈", "");
			Intent feedback = new Intent(mHomePage , Feedback.class);
			mHomePage.startActivity(feedback);
			break;
		default:
			break;
		}
	}
	
	public ListView getListView(){
		return mListView;
	}
	
	public ScrollLinearListLayout getScrollLinearListLayout(){
		return mHeaderListView;
	}

}
