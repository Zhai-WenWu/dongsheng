package amodule.dish.activity;

import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.List;

import acore.logic.XHClick;
import acore.override.activity.base.BaseActivity;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.widget.LayoutScroll;
import acore.widget.PagerSlidingTabStrip;
import amodule.dish.view.TimeView;
import amodule.dish.view.TimeView.ShareImg;
import third.share.BarShare;

public class TimeDish extends BaseActivity{
	private ArrayList<View> pagerList = new ArrayList<View>();
	private String name = "", g1 = "1", type = "";
	private String shareImg = "";
	private Myadapter pagerAdapter;
	private ViewPager viewpager;
	private LayoutScroll scrollLayout;
	private int page;
	private PagerSlidingTabStrip mTitleTab;
	private ArrayList<String> lists;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initData();
		initActivity("",2,0,R.layout.a_dish_time_title,R.layout.a_dish_caipu_viewpager);
		initMenu();
		initBarView();
		XHClick.track(TimeDish.this,"浏览早中晚菜谱推荐列表");
	}

	private void initData(){
		Bundle bundle = this.getIntent().getExtras();
		lists = new ArrayList<>();
		lists.add("早餐");
		lists.add("中餐");
		lists.add("晚餐");
		if (bundle != null) {
			type = bundle.getString("type");
			g1 = bundle.getString("g1");
			name = bundle.getString("name");
			if(name == null){
				if (g1 != null){
					switch (Integer.parseInt(g1)) {
						case 1:
							name="早餐推荐";
							break;
						case 2:
							name="中餐推荐";
							break;
						case 3:
							name="晚餐推荐";
							break;
					}
				}
			}
		}
	}

	private void initMenu() {
		viewpager = (ViewPager) findViewById(R.id.dish_time_viewpager);
		// 设置view宽度
		DisplayMetrics metric = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metric);

		// 添加view
		TimeView mview = new TimeView(this, type, "1",new ShareImg() {

			@Override
			public void showImg(String str) {
				if(!TextUtils.isEmpty(str))
					shareImg=str;
			}
		});// 早餐
		View view1 = mview.onCreateView();
		pagerList.add(view1);
		TimeView nview = new TimeView(this, type, "2",new ShareImg() {

			@Override
			public void showImg(String str) {
				if(!TextUtils.isEmpty(str))
					shareImg=str;
			}
		});// 中餐
		View view2 = nview.onCreateView();
		pagerList.add(view2);
		TimeView eview = new TimeView(this, type, "3",new ShareImg() {

			@Override
			public void showImg(String str) {
				if(!TextUtils.isEmpty(str))
					shareImg=str;
			}
		});// 晚餐
		View view3 = eview.onCreateView();
		pagerList.add(view3);

		pagerAdapter = new Myadapter(pagerList);
		viewpager.setAdapter(pagerAdapter);
		page = Integer.parseInt(g1)-1;
		viewpager.setOffscreenPageLimit(3);
		viewpager.addOnPageChangeListener(pageListener);
		pagerAdapter.notifyDataSetChanged();

	}



	private void initBarView() {
		mTitleTab = (PagerSlidingTabStrip)findViewById(R.id.dish_time_title_slide);
		mTitleTab.setViewPager(viewpager);
		mTitleTab.setListener();

		//设置选中当前tab是的点击事件
		mTitleTab.setOnTabReselectedListener(new PagerSlidingTabStrip.OnTabReselectedListener() {
			@Override
			public void onTabReselected(int position) {
				viewpager.setCurrentItem(position);
			}
		});

		View currentTab = mTitleTab.getmTabsContainer().getChildAt(Integer.valueOf(g1)-1);
		mTitleTab.select(currentTab);
		viewpager.setCurrentItem(Integer.valueOf(g1)-1);
		ImageView img_share = (ImageView) findViewById(R.id.dish_time_shareImgV);
		img_share.setVisibility(View.VISIBLE);
		img_share.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				doShare();
			}
		});
		scrollLayout = (LayoutScroll)findViewById(R.id.scroll_body);
	}

	@Override
	protected void onResume() {
		super.onResume();
	}
	private class Myadapter extends PagerAdapter {
		private List<View> views;

		public Myadapter(List<View> views) {
			this.views = views;
		}

		@Override
		public int getCount() {
			return views.size();
		}

		public CharSequence getPageTitle(int position) {
			return lists.get(position);
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		@Override
		public void destroyItem(View container, int position, Object object) {
			((ViewPager) container).removeView(views.get(position));
		}

		@Override
		public Object instantiateItem(View container, int position) {
			((ViewPager) container).addView(views.get(position));
			return views.get(position);
		}
	}

	private ViewPager.OnPageChangeListener pageListener = new ViewPager.OnPageChangeListener() {

		@Override
		public void onPageScrollStateChanged(int arg0) { }

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) { }

		@Override
		public void onPageSelected(int position) {
			// 实现切换的位置
//			mTitleSlide.setIndex(position);

			View currentTab = mTitleTab.getmTabsContainer().getChildAt(position);
			mTitleTab.select(currentTab);
		}

	};

	/**
	 * 移动方法
	 * @param v 需要移动的View
	 * @param startX 起始x坐标
	 * @param toX 终止x坐标
	 * @param startY 起始y坐标
	 * @param toY 终止y坐标
	 */
	private void moveFrontBg(View v, int startX, int toX, int startY, int toY) {
		TranslateAnimation anim = new TranslateAnimation(startX, toX, startY, toY);
		anim.setDuration(200);
		anim.setFillAfter(true);
		v.startAnimation(anim);
	}

	protected void doShare() {
		if (name == "") {
			Tools.showToast(this.getApplicationContext(), "数据错误,不能分享");
			return;
		}
		String imgType = BarShare.IMG_TYPE_WEB;
		String title = "";
		String clickUrl = "";
		String content = "";
		if (type.equals("recommend")) {
			clickUrl = StringManager.wwwUrl + "caipu/recommend/";
		} else  {
			clickUrl = StringManager.wwwUrl + "caipu/caidan/" + g1;
		}
		// 是推荐菜单
		barShare = new BarShare(TimeDish.this, "三餐推荐","菜谱");
		if (type.equals("caidan")) {
			title = name + "，果断收藏！";
			content = name + "，各种精选菜谱，非常有用，推荐一下。（香哈菜谱）";
		} else {
			title = "今日推荐菜谱-" + Tools.getAssignTime("MM月dd日",0);
			clickUrl = StringManager.third_downLoadUrl;
			content = "今日推荐菜谱很不错，每天可以尝试不同的菜，吃货必备呀 ";
		}
		barShare.setShare(imgType, title, content, shareImg, clickUrl);
		barShare.openShare();
	}
}
