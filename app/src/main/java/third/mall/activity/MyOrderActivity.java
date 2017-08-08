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
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xianghatest.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import acore.logic.XHClick;
import acore.tools.FileManager;
import acore.tools.Tools;
import acore.widget.LayoutScroll;
import acore.widget.PagerSlidingTabStrip;
import amodule.main.view.CommonBottomView;
import amodule.main.view.CommonBottonControl;
import third.mall.alipay.MallPayActivity;
import third.mall.aplug.MallCommon;
import third.mall.bean.OrderBean;
import third.mall.fragment.MallOrderFragment;
import xh.basic.tool.UtilFile;
import xh.basic.tool.UtilString;

/**
 * 我的订单页面
 * @author yujian
 */
public class MyOrderActivity extends MallOrderBaseActivity implements OnClickListener {
	private PagerSlidingTabStrip tabs;
	private ViewPager viewpager;
	private myPagerAdapter adapter;
	private ArrayList<OrderBean> listBean = new ArrayList<OrderBean>();
	private static Map<String, MallOrderFragment> fragmentMap = new HashMap<String, MallOrderFragment>();// fragment集合
	public boolean isRefresh = false;
	public ArrayList<String> ids = new ArrayList<String>();// 临时数据集合
	private ArrayList<String> list_statistic= new ArrayList<String>();
	private String icon_but="";
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
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			for(int i= 1;i<100;i++){
				if(!TextUtils.isEmpty(bundle.getString("fr"+i))){
					list_statistic.add("fr"+i+"="+bundle.getString("fr"+i));
				}else{
					break;
				}
			}
			if(!TextUtils.isEmpty(bundle.getString("xhcode"))){
				list_statistic.add("xhcode="+bundle.getString("xhcode"));
			}

			if(!TextUtils.isEmpty(bundle.getString("icon_but"))){
				icon_but=bundle.getString("icon_but");
			}
		}
		level = 3;
		initView();
		initData();
		MallPayActivity.pay_state=false;
		MallPayActivity.mall_state=false;
		initTitle();
		XHClick.track(this, "浏览订单列表页");
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
		title.setText("我的订单");
		findViewById(R.id.back).setOnClickListener(this);
//		findViewById(R.id.feedbak_layout).setOnClickListener(this);
		ImageView imageView = (ImageView) findViewById(R.id.mall_order_favorable_img);
//		imageView.setImageResource(R.drawable.z_home_feedback_ico);
		imageView.setVisibility(View.GONE);
		TextView info = (TextView) findViewById(R.id.mall_order_favorable_info);
		info.setTextSize(Tools.getDimenSp(this,R.dimen.sp_14));
		info.setText("优惠券");
		info.setVisibility(View.GONE);
		info.setClickable(false);

		tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
		viewpager = (ViewPager) findViewById(R.id.pager);
		initFileData();
		adapter = new myPagerAdapter(getSupportFragmentManager(), listBean);
		viewpager.setAdapter(adapter);
		viewpager.setOffscreenPageLimit(5);// 预加载的有5个
		tabs.setViewPager(viewpager);
		MallCommon.getSaveMall(this);
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
	}

	private void initFileData() {
		Object msg = UtilFile.loadShared(this, FileManager.MALL_ORDERLIST, FileManager.MALL_ORDERLIST);
		ArrayList<Map<String, String>> listMapByJson = UtilString.getListMapByJson(msg);
		if(listMapByJson.size() <= 0){
			String asset_msg=UtilFile.getFromAssets(this, FileManager.MALL_ORDERLIST);
			listMapByJson= UtilString.getListMapByJson(asset_msg);
		}
		if (listMapByJson.size() > 0) {
			for (int i = 0, size = listMapByJson.size(); i < size; i++) {
				OrderBean bean = new OrderBean();
				bean.setId(listMapByJson.get(i).get("code"));
				bean.setTitle(listMapByJson.get(i).get("title"));
				listBean.add(bean);
			}
			if (adapter != null) {
				adapter.notifyDataSetChanged();
				tabs.notifyDataSetChanged();
			}
		}
	}

	private void initData() {
		viewpager.addOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int arg0) {
				tabs.notifyDataSetChanged();
				if (isRefresh) {
					if (ids.size() >= listBean.size()) {
						isRefresh = false;
						ids.clear();
						return;
					}
					String id = listBean.get(arg0).getId();
					for (int i = 0, size = ids.size(); i < size; i++) {
						if (ids.get(i).equals(id)) {
							return;
						}
					}
					MallOrderFragment fragment = fragmentMap.get(id);
					if (fragment != null) {
						if (fragment.LoadState){
							fragment.refresh();
						}
						ids.add(id);
					}
				}
				final int index= arg0;
				new Handler().postDelayed(new Runnable() {
					@Override 
					public void run() {
						String id = listBean.get(index).getId();
						MallOrderFragment fragment = fragmentMap.get(id);
						if(fragment!=null)
							scrollLayout.setTouchView(fragment.getListView());
					}
					
				},100);
				XHClick.mapStat(MyOrderActivity.this, "a_mail_orders","顶部导航切换",listBean.get(arg0).getTitle());
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});
		scrollLayout.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				if(listBean.size()>0){
					String id = listBean.get(0).getId();
					MallOrderFragment fragment = fragmentMap.get(id);
					if(fragment!=null)
						scrollLayout.setTouchView(fragment.getListView());
				}
			}
		}, 100);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back:
		case R.id.shoppingcat_go:
			Log.i("back", "back");
			this.finish();
			break;
		case R.id.feedbak_layout:
			XHClick.mapStat(MyOrderActivity.this, "a_mail_orders","优惠券","");
			this.startActivity(new Intent(this, MallMyFavorableActivity.class));
			break;
		}
	}

	class myPagerAdapter extends FragmentPagerAdapter {
		ArrayList<OrderBean> titles;

		public myPagerAdapter(FragmentManager fm, ArrayList<OrderBean> titles) {
			super(fm);
			this.titles = titles;
		}

		@Override
		public Fragment getItem(int position) {
			String id = titles.get(position).getId();
			Fragment fragment = MallOrderFragment.getIntanse(titles.get(position),icon_but);
			fragmentMap.put(id, (MallOrderFragment) fragment);
			return fragment;
		}

		@Override
		public int getCount() {
			return titles.size();
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return titles.get(position).getTitle();
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case OrderStateActivity.request_order:
			if (data != null) {
				String position = data.getExtras().getString("position");
				String id = data.getExtras().getString("code");
				if (!TextUtils.isEmpty(position) && !TextUtils.isEmpty(id)) {
					MallOrderFragment fragment = fragmentMap.get(id);
					if (resultCode == OrderStateActivity.result_del) {// 删除订单
						fragment.listData.remove(Integer.parseInt(position));
						fragment.adapter.notifyDataSetChanged();
//						if (Integer.parseInt(id) > 0) {
							isRefresh = true;
							ids.add(id);
//						}
					} else if (resultCode == OrderStateActivity.result_cancel || resultCode == OrderStateActivity.result_sure) {// 取消订单,确认收货
						if (Integer.parseInt(id) == 0) {// 全部
							fragment.refresh();
						} else {// 其他
							fragment.listData.remove(Integer.parseInt(position));
							fragment.adapter.notifyDataSetChanged();
						}
						isRefresh = true;
						ids.add(id);
					}

				}
			}
			break;

		default:
			break;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		CommonBottomView.BottomViewBuilder.getInstance().refresh(mCommonBottomView);
	}

}
