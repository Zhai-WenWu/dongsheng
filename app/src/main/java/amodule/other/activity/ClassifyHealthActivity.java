package amodule.other.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import acore.logic.XHClick;
import acore.override.activity.base.BaseFragmentActivity;
import acore.tools.Tools;
import acore.widget.PagerSlidingTabStrip;
import amodule.other.fragment.ClassifyHealthFragment;

/**
 * Created by sll on 2017/4/24.
 */

public class ClassifyHealthActivity extends BaseFragmentActivity {
    public static final String STATISTICS_ID = "a_quan_homepage430";

    private ArrayList<Map<String, String>> mDatas;

    /**
     * 由外部定义的选中页
     */
    private int mSelectedPos = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        initActivity("", 2, 0, 0, R.layout.classify_health_layout);
        mSelectedPos = getIntent().getIntExtra("selectedPos", 0);
        initData();
        initStatusBar();
        initView();
    }

    /**
     * 初始化数据
     */
    private void initData() {
        mDatas = new ArrayList<>();
        Map<String, String> map = new HashMap<>();
        map.put("title", "菜谱分类");
        map.put("type", "caipu");
        map.put("coverStr", "搜菜谱  如：糖醋排骨  或  鸡蛋");
        map.put("eventId", "a_menu_table");
        map.put("statistics", "other_detail_sort");
        mDatas.add(map);
        Map<String, String> map1 = new HashMap<>();
        map1.put("title", "美食养生");
        map1.put("type", "jiankang");
        map1.put("coverStr", "搜养生内容");
        map1.put("eventId", "a_health_chart");
        map1.put("statistics", "other_health_sort");
        mDatas.add(map1);
    }

    /**
     * 初始化View
     */
    private void initView() {
        PagerSlidingTabStrip mTabStrip = (PagerSlidingTabStrip) findViewById(R.id.tab);
        ViewPager mViewPager = (ViewPager) findViewById(R.id.viewpager);
        ImageView backImg = (ImageView) findViewById(R.id.back_img);
        backImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClassifyHealthActivity.this.finish();
            }
        });
        ClassifyHealthPagerAdapter adapter = new ClassifyHealthPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(adapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                XHClick.mapStat(ClassifyHealthActivity.this, STATISTICS_ID, "顶部tab切换", mDatas.get(position).get("title"));
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        mTabStrip.setViewPager(mViewPager);
        mTabStrip.setListener();
        if (mSelectedPos > (mDatas.size() - 1))
            mSelectedPos = 0;
        mViewPager.setCurrentItem(mSelectedPos);
        if (mSelectedPos == 0) {
            XHClick.mapStat(ClassifyHealthActivity.this, STATISTICS_ID, "顶部tab切换", mDatas.get(mSelectedPos).get("title"));
            XHClick.mapStat(ClassifyHealthActivity.this, "a_classify", mDatas.get(mSelectedPos).get("title") + "按钮", "");
        }
    }

    /**
     * 初始化状态栏
     */
    private void initStatusBar() {
//        if (Tools.isShowTitle()) {
//            int topbarHeight = Tools.getDimen(this, R.dimen.topbar_height);
//            int height = topbarHeight + Tools.getStatusBarHeight(this);
//            RelativeLayout bar_title = (RelativeLayout) findViewById(R.id.bar_title);
//            RelativeLayout.LayoutParams layout = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, height);
//            bar_title.setLayoutParams(layout);
//            bar_title.setPadding(0, Tools.getStatusBarHeight(this), 0, 0);
//        }
        findViewById(R.id.bar_title).setBackgroundColor(getResources().getColor(R.color.common_top_bg));
        String colors = Tools.getColorStr(this, R.color.common_top_bg);
        Tools.setStatusBarColor(this, Color.parseColor(colors));
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public class ClassifyHealthPagerAdapter extends FragmentStatePagerAdapter {



        public ClassifyHealthPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            ClassifyHealthFragment fragment = new ClassifyHealthFragment();
            Map<String, String> map = mDatas.get(position);
            Bundle bundle = new Bundle();
            bundle.putString("title", map.get("title"));
            bundle.putString("type", map.get("type"));
            bundle.putString("coverStr", map.get("coverStr"));
            bundle.putString("eventId", map.get("eventId"));
            bundle.putString("statistics", map.get("statistics"));
            fragment.setArguments(bundle);
            return fragment;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mDatas.get(position).get("title");
        }

        @Override
        public int getCount() {
            return mDatas.size();
        }
    }
}
