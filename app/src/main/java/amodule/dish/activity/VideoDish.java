package amodule.dish.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import acore.logic.XHClick;
import acore.override.activity.base.BaseActivity;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import amodule.dish.adapter.AdapterDishVideoViewPager;
import amodule.dish.view.VideoDishItemView;
import amodule.dish.view.VideoDishItemView.OnListScrollListener;
import amodule.search.avtivity.HomeSearch;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;
import xh.basic.internet.UtilInternet;
import xh.basic.tool.UtilString;

/**
 * 视频分类页
 * @author FangRuijiao
 * @data 2016-04-08
 */
public class VideoDish extends BaseActivity{
	
	public static final String STATISTICS_ID = "a_menu_recommend";

//	private PagerSlidingTabStrip mSlidingTab;
	private HorizontalScrollView mHorScro;
	private LinearLayout mHSLinear;
	private LayoutInflater inflater;
	private ViewPager mViewPager;
	private AdapterDishVideoViewPager mViewPagerAdapter;
	private ArrayList<VideoDishItemView> pagerVideoDishItemViewList;
	private ArrayList<View> pagerViewList;
	private List<Map<String,String>> listVideoTitle;
	private Handler handler;
	
	private int screenWidth;
	private boolean isVisible = true;
	private Animation scale_to_nothing,scale_to_visibilty;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initActivity("美食视频", 2, 0, R.layout.a_dish_video_title, R.layout.a_dish_video);
		init();
		initData();
	}
	
	private void init(){
//		mSlidingTab = (PagerSlidingTabStrip)findViewById(R.id.a_dish_video_title_sliding);
		mHorScro = (HorizontalScrollView)findViewById(R.id.a_dish_video_title_sh);
		mHSLinear = (LinearLayout)findViewById(R.id.a_dish_video_hs_ll);
		mViewPager = (ViewPager)findViewById(R.id.a_dish_video_viewpager);
		pagerVideoDishItemViewList = new ArrayList<VideoDishItemView>();
		pagerViewList = new ArrayList<View>();
		listVideoTitle = new ArrayList<Map<String,String>>();
		mViewPagerAdapter = new AdapterDishVideoViewPager(pagerViewList);
		mViewPager.setAdapter(mViewPagerAdapter);
		mViewPager.setOffscreenPageLimit(5);
		mViewPager.addOnPageChangeListener(pageListener);
		
		ImageView sousuo = (ImageView)findViewById(R.id.rightImgBtn2);
		sousuo.setVisibility(View.VISIBLE);
		sousuo.setImageResource(R.drawable.z_z_topbar_ico_so);
		int left = Tools.getDimen(this, R.dimen.dp_12_5);
		int top =  Tools.getDimen(this, R.dimen.dp_13);
		int right = Tools.getDimen(this, R.dimen.dp_12_5);
		int bottom = Tools.getDimen(this, R.dimen.dp_10);
		sousuo.setPadding(left , top, right, bottom);
		sousuo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				XHClick.mapStat(VideoDish.this, STATISTICS_ID ,"搜索点击量","");
				Intent it2 = new Intent(VideoDish.this,HomeSearch.class);
				it2.putExtra("type", "caipu");
				startActivity(it2);
				XHClick.track(VideoDish.this,"点击视频列表页的搜索按钮");
			}
		});
		
//		mSlidingTab.setViewPager(mViewPager);
//		mSlidingTab.setListener();
//		mSlidingTab.setmDelegatePageListener(pageListener);
		handler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				initPagerView();
				mViewPagerAdapter.notifyDataSetChanged();
//				mSlidingTab.notifyDataSetChanged();
			}
		};
		loadManager.setFailClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				initData();
			}
		});
		inflater = LayoutInflater.from(this);
		screenWidth = ToolsDevice.getWindowPx(this).widthPixels;
		scale_to_nothing = AnimationUtils.loadAnimation(VideoDish.this, R.anim.out_to_top);
		scale_to_visibilty = AnimationUtils.loadAnimation(VideoDish.this, R.anim.in_from_top);
		scale_to_nothing.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation animation) {
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				mHorScro.setVisibility(View.GONE);
			}
		});
		scale_to_visibilty.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation animation) { 
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				mHorScro.setVisibility(View.VISIBLE);
			}
		});
	}
	
	private void initPagerView(){
		Map<String,String> map;
		for (int i = 0; i < listVideoTitle.size(); i++) {
			map = listVideoTitle.get(i);
			VideoDishItemView fragment = new VideoDishItemView(this, map.get("id"));
			View onCreateView = fragment.onCreateView();
			pagerVideoDishItemViewList.add(fragment);
			if (i == 0) {
				onSwitchPage(i);
				//统计知识页面访问情况(计算事件)
//				XHClick.onEventValue(getApplicationContext(), "pageNous", "pageNous", "精选", 1);
			}
			pagerViewList.add(onCreateView);
		}
	}
	
	private void initData(){
		mHSLinear.removeAllViews();
		String getUrl = StringManager.api_getRecommendDish;
		ReqInternet.in().doGet(getUrl, new InternetCallback(this) {
			@Override
			public void loaded(int flag, String url, Object returnObj) {
				if(flag >= UtilInternet.REQ_OK_STRING){
					listVideoTitle.clear();
					ArrayList<Map<String, String>> listMySelf = UtilString.getListMapByJson(returnObj);
					Map<String, String> mm = listMySelf.get(0);
					String videoType = mm.get("videoType");
					listMySelf = UtilString.getListMapByJson(videoType);
					Map<String,String> map;
					for(int i = 0; i < listMySelf.size(); i ++){
						map = listMySelf.get(i);
						listVideoTitle.add(map);
						View view = inflater.inflate(R.layout.a_dish_video_item_title, null);
						TextView tv = (TextView)view.findViewById(R.id.a_dish_video_item_title_tv);
						tv.setText(map.get("name"));
						tv.setTag(i);
						if(i == 0){
							setTitle(tv,true);
						}
						tv.setOnClickListener(onSwitchListener);
						mHSLinear.addView(view);
					}
					handler.sendEmptyMessage(0);
				}else{
					loadManager.showLoadFaildBar();
				}
			}
		});
	}
	
	private OnClickListener onSwitchListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			onTitleClick(v);
		}
	};
	
	private void onTitleClick(View v){
		TextView tv = (TextView)v;
		setTitle(tv,true);
		int index = Integer.parseInt(String.valueOf(tv.getTag()));
		onSwitchPage(index);
		mViewPager.setCurrentItem(index);
		for(int i = 0; i < mHSLinear.getChildCount(); i++){
			View parentView = mHSLinear.getChildAt(i);
			TextView nameTv = (TextView)parentView.findViewById(R.id.a_dish_video_item_title_tv);
			if(nameTv != tv){
				setTitle(nameTv,false);
			}else{
				int itemWidth = parentView.getWidth();  
				mHorScro.smoothScrollTo(parentView.getLeft() - (screenWidth / 2 - itemWidth / 2), 0);
			}
		}
	}
	
	private void setTitle(TextView tv,boolean isChoose){
		if(isChoose){
			tv.setTextColor(Color.WHITE);
			tv.setBackgroundResource(R.drawable.bg_round_black);
		}else{
			tv.setTextColor(Color.BLACK);
			tv.setBackgroundResource(R.drawable.bg_round3_white);
		}
	}
	
	/**
	 * ViewPager切换监听方法
	 * */
	public OnPageChangeListener pageListener = new OnPageChangeListener() {

		@Override
		public void onPageScrollStateChanged(int arg0) {}
		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) { }
		@Override
		public void onPageSelected(int position) {
			onSwitchPage(position);
			View parentView = mHSLinear.getChildAt(position);
			TextView nameTv = (TextView)parentView.findViewById(R.id.a_dish_video_item_title_tv);
			onTitleClick(nameTv);
			XHClick.mapStat(VideoDish.this, STATISTICS_ID ,"视频分类标签点击/切换量", String.valueOf(position + 1));
		}
	};
	
	private void onSwitchPage(int position){
		VideoDishItemView fragment = pagerVideoDishItemViewList.get(position);
		if (!fragment.LoadOver) {
			fragment.init(new OnListScrollListener() {
				
				@Override
				public void onScrollUp() {
					if(isVisible){
						isVisible = false;
						mHorScro.clearAnimation();
						mHorScro.startAnimation(scale_to_nothing);
					}
				}
				
				@Override
				public void onScrollDown() {
					if(!isVisible){
						isVisible = true;
						mHorScro.clearAnimation();
						mHorScro.startAnimation(scale_to_visibilty);
					}
				}
			});
		}
		isVisible = true;
		mHorScro.clearAnimation();
		mHorScro.setVisibility(View.VISIBLE);
		fragment.onSwitchOnResume();
	}
}
