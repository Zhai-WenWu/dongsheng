package third.mall.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xianghatest.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import acore.tools.Tools;
import acore.widget.LayoutScroll;
import acore.widget.PagerSlidingTabStrip;
import amodule.main.view.CommonBottomView;
import amodule.main.view.CommonBottonControl;
import aplug.feedback.activity.Feedback;
import third.mall.aplug.MallCommon;
import third.mall.fragment.MallMyFavorableFragment;
import third.mall.override.MallOrderBaseActivity;

/**
 * 我的优惠券信息
 * @author yujian
 *
 */
public class MallMyFavorableActivity extends MallOrderBaseActivity implements OnClickListener{

	private PagerSlidingTabStrip tabs;
	private ViewPager viewpager;
	public boolean isRefresh = false;
	public ArrayList<String> ids = new ArrayList<String>();// 临时数据集合
	private static Map<String, MallMyFavorableFragment> fragmentMap = new HashMap<String, MallMyFavorableFragment>();// fragment集合
	private ArrayList<Map<String,String>> lists= new ArrayList<Map<String,String>>();
	private CommonBottomView mCommonBottomView;
	private LayoutScroll scrollLayout;
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE); // 声明使用自定义标题
		String className=this.getComponentName().getClassName();
		CommonBottonControl control = new CommonBottonControl();
		setContentView(control.setCommonBottonView(className,this,R.layout.a_mall_myorder_new));
		mCommonBottomView=control.mCommonBottomView;
		level = 3;
		initView();
		initData();
		initTitle();
	}

	private void initTitle() {
		if(Tools.isShowTitle()) {
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
		}
		String colors = Tools.getColorStr(this, R.color.common_top_bg);
		Tools.setStatusBarColor(this, Color.parseColor(colors));
//		if(Tools.isShowTitle()) {
//			int dp_45 = Tools.getDimen(this, R.dimen.dp_45);
//			int height = dp_45 + Tools.getStatusBarHeight(this);
//
//			RelativeLayout bar_title = (RelativeLayout) findViewById(R.id.title_all_rela);
//			RelativeLayout.LayoutParams layout = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, height);
//			bar_title.setLayoutParams(layout);
//			bar_title.setPadding(0, Tools.getStatusBarHeight(this), 0, 0);
//		}
	}

	private void initView() {
		TextView title = (TextView) findViewById(R.id.title);
		title.setText("我的优惠券");
		findViewById(R.id.back).setOnClickListener(this);
		findViewById(R.id.feedbak_layout).setOnClickListener(this);
		tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
		viewpager = (ViewPager) findViewById(R.id.pager);
		setShowData();
		myPagerAdapter adapter = new myPagerAdapter(getSupportFragmentManager(), lists);
		viewpager.setAdapter(adapter);
		viewpager.setOffscreenPageLimit(3);// 预加载的有5个
		tabs.setViewPager(viewpager);
		MallCommon.getSaveMall(this);
	}
	private void setShowData() {
		Map<String,String> map = new HashMap<String, String>();
		map.put("id", "1");
		map.put("name", "未使用");
		lists.add(map);
		Map<String,String> map_1 = new HashMap<String, String>();
		map_1.put("id", "2");
		map_1.put("name", "已使用");
		lists.add(map_1);
		Map<String,String> map_2 = new HashMap<String, String>();
		map_2.put("id", "3");
		map_2.put("name", "已过期");
		lists.add(map_2);
		scrollLayout = (LayoutScroll)findViewById(R.id.scroll_body);
//		// 设置滚动相关
		new Handler().postDelayed(new Runnable() {
			@Override 
			public void run() {
				int searchHeight = (int) getResources().getDimension(R.dimen.dp_45);
				int scrollHeight=getWindow().findViewById(Window.ID_ANDROID_CONTENT).getHeight();
				scrollLayout.setLayoutParams(new LinearLayout.LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT,scrollHeight));
				scrollLayout.init(searchHeight);
			}
		},100);
		scrollLayout.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				setIndexListView(0);
			}
		}, 100);
		
	}
	private void initData() {
		tabs.setListener();
		viewpager.addOnPageChangeListener(new OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int arg0) {
				setIndexListView(arg0);
			}
			
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}
			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back:
			this.finish();
			break;
			case R.id.feedbak_layout:
				Intent intent = new Intent(MallMyFavorableActivity.this, Feedback.class);
				startActivity(intent);
				break;
		}
	}

	/**
	 * 获取fragment的listview对象
	 * @param arg0
	 */
	private void setIndexListView(int arg0){
		String id = lists.get(arg0).get("id");
		MallMyFavorableFragment fragment = fragmentMap.get(id);
		if(fragment!=null)
			scrollLayout.setTouchView(fragment.getListView());
	}
	class myPagerAdapter extends FragmentPagerAdapter {
		ArrayList<Map<String,String>> titles;

		public myPagerAdapter(FragmentManager fm, ArrayList<Map<String,String>> titles) {
			super(fm);
			this.titles = titles;
		}

		@Override
		public Fragment getItem(int position) {
			String id = titles.get(position).get("id");
			Fragment fragment = MallMyFavorableFragment.getInstance(id);
			fragmentMap.put(id, (MallMyFavorableFragment) fragment);
			return fragment;
		}

		@Override
		public int getCount() {
			return titles.size();
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return titles.get(position).get("name");
		}
	}
	@Override
	protected void onResume() {
		super.onResume();
		CommonBottomView.BottomViewBuilder.getInstance().refresh(mCommonBottomView);
	}
}
