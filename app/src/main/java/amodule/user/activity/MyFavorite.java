package amodule.user.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import acore.logic.LoginManager;
import acore.logic.SetDataView;
import acore.logic.XHClick;
import acore.override.activity.base.BaseActivity;
import acore.override.adapter.AdapterSimple;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import amodule.search.avtivity.FavoriteSearch;
import amodule.user.view.FavoriteDish;
import amodule.user.view.FavoriteNous;
import amodule.user.view.FavoriteQuan;

public class MyFavorite extends BaseActivity {
	private ViewPager viewpager;
	private HorizontalScrollView hsv_favorite;
	private LinearLayout ll_favorite;
	private FavoriteDish favoriteDish;//收藏的菜谱
	private FavoriteQuan favoriteQuan;//收藏的美食贴
	private FavoriteNous fragmentNous;//收藏的香哈头条
	
	public static Handler handler = null;
	private ArrayList<View> pagerList = new ArrayList<>();
	private ArrayList<Map<String, String>> topList;
	
	public final static int MSG_DATA_OK = 1;
	public final static int NEED_ONREFRESH = 2;
	private int win_width;
	private boolean needRefresh = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initActivity("", 2, 0, 0,R.layout.a_nous_home);
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				int what = msg.what;
				switch (what) {
				case MSG_DATA_OK:
					loadManager.hideProgressBar();
					needRefresh = true;
					break;
				case NEED_ONREFRESH:
//					if (viewpager != null) {
//						if (viewpager.getCurrentItem() == 0 && favoriteDish != null) {
//							favoriteDish.load(true);
//						}else if (viewpager.getCurrentItem() == 1 && favoriteQuan != null) {
//							favoriteQuan.loader();
//						}else if (viewpager.getCurrentItem() == 2 && fragmentNous != null) {
//							fragmentNous.loader();
//						}
//					}
					break;
				}
			}
		};
		initData();
		init();
		setTop();
//		initTitle();
	}

	/**
	 * 初始化区分数据模块
	 */
	private void initTitle() {
		if(Tools.isShowTitle()) {
			int dp_45 = Tools.getDimen(this, R.dimen.dp_45);
			int height = dp_45 + Tools.getStatusBarHeight(this);

			RelativeLayout bar_title = (RelativeLayout) findViewById(R.id.title_rela_all);
			RelativeLayout.LayoutParams layout = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, height);
			bar_title.setLayoutParams(layout);
			bar_title.setPadding(0, Tools.getStatusBarHeight(this), 0, 0);
		}
	}
	@Override
	protected void onResume() {
		super.onResume();
		//页面停留时间统计
		XHClick.getViewPageItemStartTime("MyFavorite_caiPu");
	}
	
	@Override
	protected void onPause() {
		//页面停留时间统计
		XHClick.getViewPageItemStopTime();
		super.onPause();
	}
	
	@Override
	protected void onRestart() {
		super.onRestart();
		needRefresh = LoginManager.isLogin();
		if (needRefresh){
			notifyMessage(NEED_ONREFRESH);
		}
		else
			finish();
	}
	
	
	public static void notifyMessage(int what) {
		if(handler == null) return;
		switch (what) {
		case MSG_DATA_OK:
			handler.sendEmptyMessage(MSG_DATA_OK);
			break;
		case NEED_ONREFRESH:
			handler.sendEmptyMessage(NEED_ONREFRESH);
			break;
		}
	}
	
	private void setTop() {
		hsv_favorite = (HorizontalScrollView) findViewById(R.id.hsv_nous);
		ll_favorite = (LinearLayout) findViewById(R.id.ll_nous);
		ll_favorite.removeAllViews();
		AdapterSimple adapter = new AdapterSimple(hsv_favorite, topList, 
				R.layout.a_nous_home_item_top, 
				new String[] { "name" },
				new int[] { R.id.hsv_quan_tv });
		SetDataView.ClickFunc[] expertClick = { new SetDataView.ClickFunc() {

			@Override
			public void click(int index, View v) {
				int length = topList.size();
				for (int i = 0; i < length; i++) {
					View view = ll_favorite.getChildAt(i);
					if (i == index) {
						view.setSelected(true);
//						view.findViewById(R.id.quan_tab_bg_select).setVisibility(View.VISIBLE);
						TextView tv = (TextView)view.findViewById(R.id.hsv_quan_tv);
						tv.setTextSize(Tools.getDimenSp(MyFavorite.this, R.dimen.sp_15));
					} else
						view.setSelected(false);
//						view.findViewById(R.id.quan_tab_bg_select).setVisibility(View.GONE);
						TextView tv = (TextView)view.findViewById(R.id.hsv_quan_tv);
						tv.setTextSize(Tools.getDimenSp(MyFavorite.this, R.dimen.sp_13));
					}
					viewpager.setCurrentItem(index);
//					ll_favorite.getChildAt(index).findViewById(R.id.quan_tab_bg_select).setVisibility(View.VISIBLE);
					TextView tv = (TextView)ll_favorite.getChildAt(index).findViewById(R.id.hsv_quan_tv);
					tv.setTextSize(Tools.getDimenSp(MyFavorite.this, R.dimen.sp_15));
			}
		} };
		SetDataView.horizontalView(hsv_favorite, adapter, null, expertClick);
		 // 将屏幕宽度均分,每个tab占有等宽
//		for (int i = 0; i < 3; i++) {
//			RelativeLayout layout = (RelativeLayout) ll_favorite.getChildAt(i);
//			LayoutParams lp = layout.getLayoutParams();
//			lp.width = win_width / 3;
//		}
		ll_favorite.getChildAt(0).setSelected(true);
//		ll_favorite.getChildAt(0).findViewById(R.id.quan_tab_bg_select).setVisibility(View.VISIBLE);
		TextView tv = (TextView)ll_favorite.getChildAt(0).findViewById(R.id.hsv_quan_tv);
		tv.setTextSize(Tools.getDimenSp(MyFavorite.this, R.dimen.sp_15));
	}

	private void init() {
		TextView title = (TextView)findViewById(R.id.title);
		title.setText("我的收藏");
		ImageView rightImgBtn2 = (ImageView)findViewById(R.id.rightImgBtn2);
		int padding = ToolsDevice.dp2px(this, 15);
		rightImgBtn2.setPadding(padding, padding, padding, padding);
		rightImgBtn2.setVisibility(View.VISIBLE);
		rightImgBtn2.setImageResource(R.drawable.z_z_topbar_ico_so);
		int left = Tools.getDimen(this, R.dimen.dp_12_5);
		int top =  Tools.getDimen(this, R.dimen.dp_13);
		int right = Tools.getDimen(this, R.dimen.dp_12_5);
		int bottom = Tools.getDimen(this, R.dimen.dp_10);
		rightImgBtn2.setPadding(left , top, right, bottom);
		rightImgBtn2.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent it2 = new Intent(MyFavorite.this,FavoriteSearch.class);
				startActivity(it2);
			}
		});
		viewpager = (ViewPager) findViewById(R.id.nous_viewpager);
		//我的收藏 ----菜谱
		favoriteDish = new FavoriteDish(this);
		View favoriteDishvView = favoriteDish.onCreateView();
		pagerList.add(favoriteDishvView);
		//统计
		XHClick.onEventValue(this, "pageFav", "pageFav", "菜谱", 1);
		
		//我的收藏 ----美食贴
		favoriteQuan = new FavoriteQuan(this);
		View fragmentHotGoodvView = favoriteQuan.onCreateView();
		pagerList.add(fragmentHotGoodvView);
		//我的收藏-----香哈头条
		fragmentNous = new FavoriteNous(this,"","精选");
		View fragmentNousvView = fragmentNous.onCreateView();
		pagerList.add(fragmentNousvView);

		Myadapter pagerAdapter = new Myadapter(pagerList);
		viewpager.setAdapter(pagerAdapter);
		//监听
		viewpager.addOnPageChangeListener(pageListener);
		viewpager.setCurrentItem(0);
		pagerAdapter.notifyDataSetChanged();
		findViewById(R.id.back).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
	}

	private void initData() {
		win_width = ToolsDevice.getWindowPx(this).widthPixels;
		topList = new ArrayList<Map<String, String>>();
		// 罗明
		String names[] = { "菜谱", "美食贴", "头条" };
		for (int i = 0; i < names.length; i++) {
			HashMap<String, String> topMap = new HashMap<String, String>();
			topMap.put("name", names[i]);
			topList.add(topMap);
		}
	}
	public class Myadapter extends PagerAdapter{

		private List<View> views;
		public Myadapter(List<View> views){
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
		
	}
	/**
	 * ViewPager切换监听方法
	 * */
	public OnPageChangeListener pageListener = new OnPageChangeListener() {
		
		@Override public void onPageScrollStateChanged(int arg0) { }

		@Override 	public void onPageScrolled(int arg0, float arg1, int arg2) { }

		@Override
		public void onPageSelected(int position) {
			selectTab(position);//监听viewpager滑动时的头部tab切换
			//动态加载,滑到哪一页,加载哪一页.第一次打开页面不会执行,只有切换的时候执行
			switch (position) {
			case 0:
				if (!favoriteDish.loadOver) {
					favoriteDish.init();
				}
				//当打开我的收藏的第一页
				//页面停留时间统计
				XHClick.getViewPageItemStopTime();
				XHClick.getViewPageItemStartTime("MyFavorite_caiPu");
				//统计
				XHClick.onEventValue(MyFavorite.this, "pageFav", "pageFav", "菜谱", 1);
				break;
			case 1:
				//选择第二页
				if (!favoriteQuan.LoadOver) {
					favoriteQuan.init();
				}
				//页面停留时间统计
				XHClick.getViewPageItemStopTime();
				XHClick.getViewPageItemStartTime("MyFavorite_meiShiTie");
				//统计
				XHClick.onEventValue(MyFavorite.this, "pageFav", "pageFav", "美食贴", 1);
				break;
			case 2:
				//选择第三页
				if (!fragmentNous.LoadOver) {
					fragmentNous.init();
				}
				//页面停留时间统计
				XHClick.getViewPageItemStopTime();
				XHClick.getViewPageItemStartTime("MyFavorite_zhiShi");
				//统计
				XHClick.onEventValue(MyFavorite.this, "pageFav", "pageFav", "头条", 1);
				break;
			default:
				break;
			}
		}
	};
	
	private void selectTab(int position) {
		// 判断是否选中
		int length = topList.size();
		for (int j = 0; j < length; j++) {
			View view = ll_favorite.getChildAt(j); 
			if (j == position) {
				view.setSelected(true);
//				view.findViewById(R.id.quan_tab_bg_select).setVisibility(View.VISIBLE);
				TextView tv = (TextView)view.findViewById(R.id.hsv_quan_tv);
				tv.setTextSize(Tools.getDimenSp(MyFavorite.this, R.dimen.sp_15));
			} else {
				view.setSelected(false);
				TextView tv = (TextView)view.findViewById(R.id.hsv_quan_tv);
				tv.setTextSize(Tools.getDimenSp(MyFavorite.this, R.dimen.sp_13));
//				view.findViewById(R.id.quan_tab_bg_select).setVisibility(View.GONE);
			}
		}
		hsv_favorite.smoothScrollTo(win_width * position / 4, 0);
		View checkView = ll_favorite.getChildAt(position);
		int k = checkView.getMeasuredWidth();
		int l = checkView.getLeft();
		int i2 = l + k / 2 - win_width / 2;
		hsv_favorite.smoothScrollTo(i2, 0);
	}
}
