package amodule.main.activity;

import android.annotation.SuppressLint;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.View;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import acore.logic.LoginManager;
import acore.logic.XHClick;
import acore.override.XHApplication;
import acore.override.activity.mian.MainBaseActivity;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import acore.widget.PagerSlidingTabStrip;
import amodule.home.view.HomePushIconView;
import amodule.main.view.circle.CircleMainFragment;
import amodule.quan.db.PlateData;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;

/**
 * 新的社区
 *
 * @author tanzeyu
 */
public class MainCircle extends MainBaseActivity implements View.OnClickListener {
    public static final String KEY = "MainCircle";
    public static final String STATISTICS_ID = "a_quan_homepage430";
    /** 顶部tab */
    private PagerSlidingTabStrip mTabs;
    /** ViewPager */
    private ViewPager mViewPager;

    private HomePushIconView mPushIconView;

    /** 板块信息集合 */
    private ArrayList<PlateData> mPlateDataArray = new ArrayList<>();
    /***/
    private int index = 0;

    private boolean defaultHasUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //sufureView页面闪烁
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        setContentView(R.layout.a_circle_home);
//        initActivity("", 2, 0, 0, R.layout.a_circle_home);

        //
        initView();

        defaultHasUser = LoginManager.isLogin();
        XHClick.track(MainCircle.this, "浏览美食圈列表页");
    }

    /** 初始化UI */
    private void initView() {
        mTabs = (PagerSlidingTabStrip) findViewById(R.id.circle_tab);
        mViewPager = (ViewPager) findViewById(R.id.circle_viewpager);
        mViewPager.setOffscreenPageLimit(5);

        mPushIconView = (HomePushIconView) findViewById(R.id.circle_pulish);
        //设置监听

        mPushIconView.setOnClickListener(this);
        findViewById(R.id.back).setOnClickListener(this);
        findViewById(R.id.back_ll).setVisibility(View.GONE);
        //获取模块数据
        loadManager.setLoading(v -> loadModuleData());
    }

    private void loadModuleData(){
        ReqInternet.in().doGet(StringManager.api_indexModules, new InternetCallback() {
            @Override
            public void loaded(int flag, String url, Object msg) {
                if (flag >= ReqInternet.REQ_OK_STRING) {
                    List<Map<String, String>> returnData = StringManager.getListMapByJson(msg);
                    if (returnData.size() > 0) {
                        Map<String, String> circleInfo = returnData.get(0);
                        initPlate(circleInfo.get("indexRecTabQuan"));
                    }
                }
                loadManager.loadOver(flag, 1, true);
            }
        });
    }

    /**
     * 初始化板块
     *
     * @param moduleStr json数据
     */
    private void initPlate(String moduleStr) {
        if (!TextUtils.isEmpty(moduleStr)) {
            // 清空模块数据
            if (mPlateDataArray != null) {
                mPlateDataArray.clear();
            }
            List<Map<String, String>> modules = StringManager.getListMapByJson(moduleStr);
            final int datLength = modules.size();
            for (int index = 0; index < modules.size(); index++) {
                Map<String, String> module = modules.get(index);
                String url = module.get("url");
                if (url.indexOf("?") < 0 || url.indexOf("?") + 1 >= url.length()) {
                    continue;
                }
                PlateData plateData = new PlateData();
                plateData.setLocation(module.get("isLocation"));
                plateData.setPosition(index);
                //
                String param = url.substring(url.indexOf("?") + 1);
                String[] params = param.split("&");
                for (String str : params) {
                    String[] keyAndValue = str.split("=");
                    if (keyAndValue.length != 2) {
                        continue;
                    }
                    if ("cid".equals(keyAndValue[0])) {
                        plateData.setCid(keyAndValue[1]);
                    } else if ("mid".equals(keyAndValue[0])) {
                        plateData.setMid(keyAndValue[1]);
                    } else if ("name".equals(keyAndValue[0])) {
                        plateData.setName(keyAndValue[1]);
                    }
                }
                plateData.setPosition(index);
                if (plateData.getName().contains("最新")) {
                    plateData.setShowAllQuan(true);
                    plateData.setStiaticID("a_quan_homepage_newest");
                }
                if (plateData.getName().contains("推荐")
                        || plateData.getName().contains("发现")) {
                    plateData.setShowAd(true);
                    plateData.setShowScrollTop(true);
                    plateData.setStiaticID("a_quan_homepage_recommend");
                }
                if (plateData.getName().contains("关注")) {
                    plateData.setShowRecUser(true);
                    plateData.setStiaticID("a_quan_homepage_follow");
                    if (!LoginManager.isLogin()) {
                        modules.remove(module);
                        index--;
                        continue;
                    }
                }
                mPlateDataArray.add(plateData);
            }
            if (mPlateDataArray.size() == 3
                    && datLength == 3) {
                index = 1;
            } else {
                index = 0;
            }
        }
        if (mPlateDataArray.size() == 0) {
            loadManager.showLoadFaildBar();
            Tools.showToast(this, "初始化错误");
            loadManager.showLoadFaildBar();
            return;
        }
        /* PagerAdapter */
        CirclePagerAdapter adapter = new CirclePagerAdapter(getSupportFragmentManager(), mPlateDataArray);
        mViewPager.setAdapter(adapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                XHClick.mapStat(MainCircle.this, STATISTICS_ID, "顶部tab切换", mPlateDataArray.get(position).getName());
                setFragmentCurrentPage(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        mTabs.setViewPager(mViewPager);
        mTabs.setListener();
        //设置选中当前tab是的点击事件
        mTabs.setOnTabReselectedListener(this::refreshFragment);

        mTabs.post(this::setLeftMargin);
        if (index < mPlateDataArray.size()) {
            mViewPager.setCurrentItem(index);
        }
    }

    private void setLeftMargin(){
        int screenWidth = ToolsDevice.getWindowPx(MainCircle.this).widthPixels;
        int dp_45 = Tools.getDimen(MainCircle.this, R.dimen.dp_45);
        int realWidth = mTabs.getmTabsContainer().getWidth();
        if ((screenWidth - realWidth) / 2 < dp_45) {
            View view = findViewById(R.id.marginLeftView);
            view.getLayoutParams().width = 0;
        }
    }

    /**
     * 刷新
     *
     * @param position 下标
     */
    private void refreshFragment(final int position) {
        //调用页面的刷新方法
        @SuppressLint("RestrictedApi") List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if (fragments != null && fragments.size() > 0) {
            for (Fragment fragment : fragments) {
                if (fragment instanceof CircleMainFragment) {
                    CircleMainFragment circleMainFragment = (CircleMainFragment) fragment;
                    if (circleMainFragment.getmPlateData().getPosition() == position) {
                        circleMainFragment.returnListTop();
                        circleMainFragment.refresh();
                    }
                }
            }
        }
    }

    /**
     * 刷新
     *
     * @param position 小标
     */
    private void setFragmentCurrentPage(final int position) {
        //调用页面的刷新方法
        @SuppressLint("RestrictedApi") List<Fragment> fragments = getSupportFragmentManager().getFragments();
        for (Fragment fragment : fragments) {
            if (fragment instanceof CircleMainFragment) {
                CircleMainFragment circleMainFragment = (CircleMainFragment) fragment;
                if (circleMainFragment.getmPlateData().getPosition() == position) {
                    circleMainFragment.setQuanmCurrentPage();
                }
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (defaultHasUser != LoginManager.isLogin()) {
            defaultHasUser = LoginManager.isLogin();
            loadModuleData();
        }
    }

    /** 刷新 */
    public void refresh() {
        if (mViewPager != null) {
            final int position = mViewPager.getCurrentItem();
            refreshFragment(position);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.circle_pulish:
                mPushIconView.showPulishMenu();
                break;
            case R.id.back:
                onBackPressed();
                break;
            default:
                break;
        }
    }

    /** CirclePagerAdapter */
    public class CirclePagerAdapter extends FragmentStatePagerAdapter {
        private ArrayList<PlateData> mPlates;

        CirclePagerAdapter(FragmentManager fm, ArrayList<PlateData> titles) {
            super(fm);
            this.mPlates = titles;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mPlates.get(position).getName();
        }

        @Override
        public Fragment getItem(int position) {
            PlateData plateData = mPlates.get(position);
            CircleMainFragment fragment = CircleMainFragment.newInstance(plateData);
            Bundle bundle = fragment.getArguments();
            bundle.putString(CircleMainFragment.CIRCLENAME, "美食圈");
            fragment.setArguments(bundle);
            return fragment;
        }

        @Override
        public int getCount() {
            return mPlates.size();
        }
    }
}
