package amodule.nous.activity;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.XHClick;
import acore.override.activity.base.BaseActivity;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.widget.LayoutScroll;
import acore.widget.PagerSlidingTabStrip;
import amodule.nous.view.FragmentNous;

public class HomeNous extends BaseActivity {
	private ViewPager viewpager;

	private Myadapter pagerAdapter;
	private ArrayList<Map<String, String>> topList;
	private ArrayList<FragmentNous> fragmentNousList = new ArrayList<FragmentNous>();

	private String type = "";
	private PagerSlidingTabStrip tabs;
	private LayoutScroll scrollLayout;
	private int searchHeight;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initActivity("香哈头条",2,0,R.layout.a_dish_video_title,R.layout.a_nous_home_new);
		Bundle bundle = this.getIntent().getExtras();
		if (bundle != null) {
			type = bundle.getString("type");
		}
		initData();
		init();
		initScrollLayout();
		XHClick.track(this,"浏览知识列表页");
	}

	private void initScrollLayout() {
		scrollLayout = (LayoutScroll) findViewById(R.id.scroll_body);
		// 设置滚动相关
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				int scrollHeight = getWindow().findViewById(Window.ID_ANDROID_CONTENT).getHeight();
				if(scrollLayout != null){
					scrollLayout.setLayoutParams(new LinearLayout.LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, scrollHeight));
					scrollLayout.init(searchHeight);
				}
			}
		};
		if (runnable != null) {
			new Handler().postDelayed(runnable, 100);
		}
		scrollLayout.setTouchView(fragmentNousList.get(0).getView());
	}

	@Override
	protected void onResume() {
		////统计viewpager在哪一个页面
		if (viewpager != null) {
			switch (viewpager.getCurrentItem()) {
				case 0:
					XHClick.getViewPageItemStartTime("HomeNous_jinXuan");
					//统计知识页面访问情况(计算事件)
//				XHClick.onEventValue(this, "pageNous", "pageNous", "精选", 1);
					XHClick.mapStat(this, "a_nouse", "导航", "精选", 1);
					break;
				case 1:
					XHClick.getViewPageItemStartTime("HomeNous_jianKangYangSheng");
					//统计知识页面访问情况(计算事件)
//				XHClick.onEventValue(getApplicationContext(), "pageNous", "pageNous", "健康养生", 1);
					XHClick.mapStat(this, "a_nouse", "导航", "健康养生", 1);
					break;
				case 2:
					XHClick.getViewPageItemStartTime("HomeNous_penRenJiQiao");
					//统计知识页面访问情况(计算事件)
//				XHClick.onEventValue(getApplicationContext(), "pageNous", "pageNous", "烹饪技巧", 1);
					XHClick.mapStat(this, "a_nouse", "导航", "烹饪技巧", 1);
					break;
				case 3:
					XHClick.getViewPageItemStartTime("HomeNous_meiShiJinXuan");
					//统计知识页面访问情况(计算事件)
//				XHClick.onEventValue(getApplicationContext(), "pageNous", "pageNous", "美食精选", 1);
					XHClick.mapStat(this, "a_nouse", "导航", "美食精选", 1);
					break;
				case 4:
					XHClick.getViewPageItemStartTime("HomeNous_meiShiZaTan");
					//统计知识页面访问情况(计算事件)
//				XHClick.onEventValue(getApplicationContext(), "pageNous", "pageNous", "美食杂谈", 1);
					XHClick.mapStat(this, "a_nouse", "导航", "美食杂谈", 1);
					break;
				default:
					break;
			}
		}
		super.onResume();
	}

	@Override
	protected void onPause() {
		//页面item停留时间统计
		XHClick.getViewPageItemStopTime();
		super.onPause();
	}


	private void initData() {
		topList = new ArrayList<Map<String, String>>();
		Map<String, String> topMap = new HashMap<String, String>();
		topMap.put("name", "头条");
		topMap.put("pinyin", "");
		topList.add(topMap);
		String nousNavStr = AppCommon.getAppData(this, "nousNav");
		ArrayList<Map<String, String>> list = StringManager.getListMapByJson(nousNavStr);
		if (list.size() > 0) {
			Map<String, String> map = null;
			for (int i = 0; i < list.size(); i++) {
				map = list.get(i);
				map.put("name", map.get("name"));
				map.put("pinyin", map.get("pinyin"));
				topList.add(map);
			}
		}
	}

	private void init() {
		searchHeight = (int) getResources().getDimension(R.dimen.dp_41);
//		findViewById(R.id.ll_back).setVisibility(View.GONE);
		findViewById(R.id.psts).setVisibility(View.VISIBLE);
		// 分享功能
//		ImageView imgView = (ImageView) findViewById(R.id.rightImgBtn2);
//		imgView.setVisibility(View.VISIBLE);
//		imgView.setImageResource(R.drawable.z_z_topbar_ico_so);
//		int left = Tools.getDimen(this, R.dimen.dp_12_5);
//		int top = Tools.getDimen(this, R.dimen.dp_13);
//		int right = Tools.getDimen(this, R.dimen.dp_12_5);
//		int bottom = Tools.getDimen(this, R.dimen.dp_10);
//		imgView.setPadding(left, top, right, bottom);
//		imgView.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				XHClick.mapStat(HomeNous.this, "a_nouse", "搜索", "");
//				Intent intent = new Intent(HomeNous.this, HomeSearch.class);
//				intent.putExtra("type", "zhishi");
//				intent.putExtra("from", "频道");
//				startActivity(intent);
//			}
//		});
		findViewById(R.id.back).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				HomeNous.this.finish();
			}
		});
		viewpager = (ViewPager) findViewById(R.id.nous_viewpager);
		ArrayList<View> pagerList = new ArrayList<>();
		for (int i = 0; i < topList.size(); i++) {
//			if (i == 0) {
//				FragmentNousOne fragment = new FragmentNousOne(this, topList.get(i).get("pinyin"), topList.get(i).get("name"));
//				pagerList.add(fragment);
//			}else {
			FragmentNous fragment = new FragmentNous(this, topList.get(i).get("pinyin"), topList.get(i).get("name"));
			fragmentNousList.add(fragment);
			View onCreateView = fragment.onCreateView();
			if (i == 0) {
				fragment.init();
				//统计知识页面访问情况(计算事件)
				XHClick.onEventValue(getApplicationContext(), "pageNous", "pageNous", "精选", 1);
			}
			pagerList.add(onCreateView);
//			}
		}
		tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
		tabs.setVisibility(View.VISIBLE);
		pagerAdapter = new Myadapter(pagerList);
		viewpager.setAdapter(pagerAdapter);
		viewpager.addOnPageChangeListener(pageListener);
		viewpager.setOffscreenPageLimit(5);
		tabs.setViewPager(viewpager);
		pagerAdapter.notifyDataSetChanged();
		tabs.notifyDataSetChanged();

		if (type != "" && type != null) {
			if (type.equals("ys")) {
				viewpager.setCurrentItem(1);
			} else if (type.equals("jq")) {
				viewpager.setCurrentItem(2);
			} else if (type.equals("jx")) {
				viewpager.setCurrentItem(3);
			} else if (type.equals("zt")) {
				viewpager.setCurrentItem(4);
			}
		}
		View view = tabs.getmTabsContainer().getChildAt(0);
		View v = view.findViewById(R.id.psts_tab_title);
		if (v instanceof TextView) {
			TextView tab = (TextView) v;
			tab.setTextSize(Tools.getDimenSp(HomeNous.this, R.dimen.sp_17));
		}
	}

	public void refresh(){
		int position = viewpager.getCurrentItem();
		FragmentNous fragment1 = fragmentNousList.get(position);
		if (fragment1.LoadOver) {
			fragment1.refresh();
			scrollLayout.animatScroll(0,0,1000);
		}
	}

	public class Myadapter extends PagerAdapter {
		private List<View> views;

		public Myadapter(List<View> views) {
			this.views = views;
		}

		@Override
		public int getCount() {
			return views.size();
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

		@Override
		public CharSequence getPageTitle(int position) {
			return topList.get(position).get("name");
		}
	}


	/**
	 * ViewPager切换监听方法
	 */
	public OnPageChangeListener pageListener = new OnPageChangeListener() {

		@Override
		public void onPageScrollStateChanged(int arg0) {

		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {

		}

		@Override
		public void onPageSelected(int position) {
//			selectTab(position);
			tabs.notifyDataSetChanged();
			View view = tabs.getmTabsContainer().getChildAt(position);
			View v = view.findViewById(R.id.psts_tab_title);
			if (v instanceof TextView) {
				TextView tab = (TextView) v;
				tab.setTextSize(Tools.getDimenSp(HomeNous.this, R.dimen.sp_17));
			}

			switch (position) {
				case 0:
					FragmentNous fragment = fragmentNousList.get(position);
					if (!fragment.LoadOver) {
						fragment.init();
					}                //页面停留时间统计
					XHClick.getViewPageItemStopTime();
					XHClick.getViewPageItemStartTime("HomeNous_jinXuan");
					//统计知识页面访问情况(计算事件)
					XHClick.onEventValue(getApplicationContext(), "pageNous", "pageNous", "精选", 1);
					break;
				case 1:
					FragmentNous fragment1 = fragmentNousList.get(position);
					if (!fragment1.LoadOver) {
						fragment1.init();
					}
					//页面停留时间统计
					XHClick.getViewPageItemStopTime();
					XHClick.getViewPageItemStartTime("HomeNous_jianKangYangSheng");
					//统计知识页面访问情况(计算事件)
					XHClick.onEventValue(getApplicationContext(), "pageNous", "pageNous", "健康养生", 1);
					break;
				case 2:
					FragmentNous fragment2 = fragmentNousList.get(position);
					if (!fragment2.LoadOver) {
						fragment2.init();
					}
					//页面停留时间统计
					XHClick.getViewPageItemStopTime();
					XHClick.getViewPageItemStartTime("HomeNous_penRenJiQiao");
					//统计知识页面访问情况(计算事件)
					XHClick.onEventValue(getApplicationContext(), "pageNous", "pageNous", "烹饪技巧", 1);
					break;
				case 3:
					FragmentNous fragment3 = fragmentNousList.get(position);
					if (!fragment3.LoadOver) {
						fragment3.init();
					}
					//页面停留时间统计
					XHClick.getViewPageItemStopTime();
					XHClick.getViewPageItemStartTime("HomeNous_meiShiJinXuan");
					//统计知识页面访问情况(计算事件)
					XHClick.onEventValue(getApplicationContext(), "pageNous", "pageNous", "美食精选", 1);
					break;
				case 4:
					FragmentNous fragment4 = fragmentNousList.get(position);
					if (!fragment4.LoadOver) {
						fragment4.init();
					}
					//页面停留时间统计
					XHClick.getViewPageItemStopTime();
					XHClick.getViewPageItemStartTime("HomeNous_meiShiZaTan");
					//统计知识页面访问情况(计算事件)
					XHClick.onEventValue(getApplicationContext(), "pageNous", "pageNous", "美食杂谈", 1);
					break;

				default:
					break;
			}
			scrollLayout.setTouchView(fragmentNousList.get(position).getView());
		}
	};

	@Override
	protected void onDestroy() {
		super.onDestroy();
		fragmentNousList.clear();
		pagerAdapter = null;
		scrollLayout = null;
		System.gc();
	}
}
