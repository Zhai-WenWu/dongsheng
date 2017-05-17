package amodule.user.activity;

import java.util.ArrayList;
import java.util.List;

import acore.logic.XHClick;
import acore.override.activity.base.BaseActivity;
import acore.tools.Tools;
import amodule.main.view.CommonBottonControl;
import amodule.user.view.FansFollwersFragment;
import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.xiangha.R;

@SuppressLint("ResourceAsColor")
public class FansAndFollwers extends BaseActivity implements OnClickListener {
	public ViewPager viewPager;
	public Button left_btn, right_btn;
	
	private String page = "0",userCode="";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		Bundle bundle = this.getIntent().getExtras();
		if (bundle != null) {
			page = bundle.getString("page");
			userCode=bundle.getString("code");
		}
		className=this.getComponentName().getClassName();
		control= new CommonBottonControl();
		setContentView(control.setCommonBottonView(className,this,R.layout.a_my_fans_follower));
		mCommonBottomView=control.mCommonBottomView;
		setCommonStyle();
		initView();
		initTitle();
	}

	private void initTitle() {
		if (Tools.isShowTitle()) {
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
			int dp_45 = Tools.getDimen(this, R.dimen.dp_45);
			int height = dp_45 + Tools.getStatusBarHeight(this);

			RelativeLayout bar_title = (RelativeLayout) findViewById(R.id.top_bar);
			RelativeLayout.LayoutParams layout = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, height);
			bar_title.setLayoutParams(layout);
			bar_title.setPadding(0, Tools.getStatusBarHeight(this), 0, 0);
		}
	}

	private void initView() {
		RelativeLayout leftImg = (RelativeLayout) findViewById(R.id.back);
		leftImg.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		// TitleBar选择初始化
		findViewById(R.id.leftText).setVisibility(View.VISIBLE);
		findViewById(R.id.leftImgBtn).setVisibility(View.VISIBLE);
		left_btn = (Button) findViewById(R.id.fans_btnLeftChannel);
		left_btn.setOnClickListener(this);
		right_btn = (Button) findViewById(R.id.fans_btnRightChannel);
		right_btn.setOnClickListener(this);
		viewPager = (ViewPager) findViewById(R.id.my_viewpager);
		ArrayList<View> list_fragment = new ArrayList<View>();
		list_fragment.add(new FansFollwersFragment(FansAndFollwers.this, "fans",userCode).onCreateView());
		list_fragment.add(new FansFollwersFragment(FansAndFollwers.this, "follwers",userCode).onCreateView());
		AdapterFF pagerAdapter = new AdapterFF(list_fragment);
		viewPager.setAdapter(pagerAdapter);
		viewPager.addOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {
				selectTab(position);
				//统计
				if (position == 0) {
					XHClick.onEventValue(FansAndFollwers.this, "pageCare", "pageCare", "粉丝", 1);
				}else if (position == 1) {
					XHClick.onEventValue(FansAndFollwers.this, "pageCare", "pageCare", "关注", 1);
				}
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {

			}

			@Override
			public void onPageScrollStateChanged(int position) {

			}
		});
		initViewPager();
	}

	private void initViewPager() {
		if (page.equals("0")) {
			left_btn.setSelected(true);
		} else {
			right_btn.setSelected(true);
		}
		viewPager.setCurrentItem(Integer.valueOf(page));
	}

	public class AdapterFF extends PagerAdapter {
		private List<View> views;
		public AdapterFF(List<View> views){
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

	//选择的Column里面的Tab
	private void selectTab(int position) {
		if (position == 0) {
			left_btn.setSelected(true);
			left_btn.setTextColor(Color.BLACK);
			right_btn.setSelected(false);
			right_btn.setTextColor(R.color.c_gray_e8e8e8);
		} else {
			right_btn.setSelected(true);
			right_btn.setTextColor(Color.BLACK);
			left_btn.setSelected(false);
			left_btn.setTextColor(R.color.c_gray_e8e8e8);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.fans_btnLeftChannel:
			viewPager.setCurrentItem(0);
			break;
		case R.id.fans_btnRightChannel:
			viewPager.setCurrentItem(1);
			break;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			finish();
			break;
		}
		return super.onKeyDown(keyCode, event);
	}
}
