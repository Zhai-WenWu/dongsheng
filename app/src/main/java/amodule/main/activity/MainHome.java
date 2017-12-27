package amodule.main.activity;

import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.SpecialWebControl;
import acore.logic.XHClick;
import acore.override.activity.mian.MainBaseActivity;
import acore.tools.FileManager;
import acore.tools.IObserver;
import acore.tools.ObserverManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.widget.PagerSlidingTabStrip;
import amodule.main.Main;
import amodule.main.bean.HomeModuleBean;
import amodule.main.view.ChangeSendDialog;
import amodule.main.view.home.BaseHomeFragment;
import amodule.main.view.home.HomeFragment;
import amodule.other.activity.ClassifyHealthActivity;
import amodule.search.avtivity.HomeSearch;
import aplug.basic.InternetCallback;
import aplug.basic.ReqEncyptInternet;
import aplug.basic.ReqInternet;

/**
 * 新的框架首页
 */
public class MainHome extends MainBaseActivity implements IObserver {
    public final static String tag = "zhangyujian";
    public final static String recommedType = "recomv1";//推荐类型
    public final static String recommedType_statictus = "recom";//推荐类型-用于统计

    private PagerSlidingTabStrip home_tab;
    private ViewPager viewpager;
    private boolean isRefresh = false;
    private ArrayList<Map<String, String>> moduleData = new ArrayList<>();
    private ArrayList<HomeModuleBean> listBean = new ArrayList<>();

    private HomePagerAdapter mAdapter;
    private boolean isinit = false;//view是否已经绘制完成
    private LinearLayout mFenlei;
    private RelativeLayout mSearch;
    private ImageView mMoreBtn;
    private int itemPosition = 0;//当前所在位置.
    private int resumeCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_home);
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        Main.allMain.allTab.put("MainIndex", this);
        initView();
        initData();
        initModuleData();
        String logPostTime = AppCommon.getConfigByLocal("logPostTime");
        if(!TextUtils.isEmpty(logPostTime)){
            Map<String,String> map=StringManager.getFirstMap(logPostTime);
            if(map!=null&&map.containsKey("postTime")&&!TextUtils.isEmpty(map.get("postTime"))) {
                XHClick.HOME_STATICTIS_TIME = Integer.parseInt(map.get("postTime"), 10) * 1000;
            }
        }
        ObserverManager.getInstence().registerObserver(this, ObserverManager.NOTIFY_VIPSTATE_CHANGED);
    }

    /**
     * 创建View
     */
    private void initView() {
//        String colors = Tools.getColorStr(this, R.color.common_top_bg);
//        Tools.setStatusBarColor(this, Color.parseColor(colors));
        home_tab = (acore.widget.PagerSlidingTabStrip) findViewById(R.id.home_tab);
        viewpager = (ViewPager) findViewById(R.id.viewpager);
        mFenlei = (LinearLayout) findViewById(R.id.fenlei_linear);
        mSearch = (RelativeLayout) findViewById(R.id.ed_search_layout_main);
        mMoreBtn = (ImageView) findViewById(R.id.btn_back);
        viewpager.setOffscreenPageLimit(5);
//        initTopView();
        addListener();
    }

    /** 添加监听事件 */
    private void addListener() {
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.fenlei_linear:
                        XHClick.mapStat(MainHome.this, "a_index530", "顶部", "点击分类按钮");
                        startActivity(new Intent(MainHome.this, ClassifyHealthActivity.class));
                        break;
                    case R.id.ed_search_layout_main:
                        XHClick.mapStat(MainHome.this, "a_index530", "顶部", "点击搜索框");
                        startActivity(new Intent(MainHome.this, HomeSearch.class));
                        break;
                    case R.id.btn_back:
                        XHClick.mapStat(MainHome.this, "a_index530", "顶部", "点击发布按钮");
                        new ChangeSendDialog(MainHome.this).show();
                        if ("video".equals(listBean.get(itemPosition).getType())) {
                            BaseHomeFragment homeFragment = getFragmentByPos(itemPosition);
                            if (homeFragment != null && homeFragment instanceof HomeFragment)
                                ((HomeFragment)homeFragment).stopVideo();
                        }
                        break;

                }
            }
        };
        mFenlei.setOnClickListener(onClickListener);
        mSearch.setOnClickListener(onClickListener);
        mMoreBtn.setOnClickListener(onClickListener);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && !isinit) {
            isinit = true;
        }
    }

    private void initData() {
        mAdapter = new HomePagerAdapter(getSupportFragmentManager(), listBean);
        viewpager.setAdapter(mAdapter);
        viewpager.setCurrentItem(0);
        viewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                home_tab.notifyDataSetChanged();
                if (recommedType.equals(listBean.get(itemPosition).getType())|| recommedType.equals(listBean.get(position).getType())) {//推荐列表
                    if (recommedType.equals(listBean.get(itemPosition).getType()) && !recommedType.equals(listBean.get(position).getType())) {//从推荐列表出去
                        //读取开始时间，并获取当前时间，统计
                        setRecommedStatistic();
                    } else if (!recommedType.equals(listBean.get(itemPosition).getType()) && recommedType.equals(listBean.get(position).getType())) {//进入推荐列表
                        //设置开始时间
                        setRecommedTime(System.currentTimeMillis());
                    } else {//重复点击推荐列表
                        //暂不处理

                    }
                }
                if (itemPosition != position && "video".equals(listBean.get(itemPosition).getType())) {
                    BaseHomeFragment homeFragment = getFragmentByPos(itemPosition);
                    if (homeFragment != null && homeFragment instanceof HomeFragment)
                        ((HomeFragment)homeFragment).stopVideo();
                }
                itemPosition = position;
//                setFragmentCurrentPage(position);
                //刷新广告数据
                refreshAdData(position);
                Log.i(tag, "viewpager::onPageSelected::" + position);
                XHClick.mapStat(MainHome.this, "a_index530", "二级导航栏", "点击/滑动到" + listBean.get(position).getTitle() + "按钮");
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        home_tab.setViewPager(viewpager);
        home_tab.setOnTabReselectedListener(new PagerSlidingTabStrip.OnTabReselectedListener() {
            @Override
            public void onTabReselected(int position) {
                Log.i(tag, "home_tab::OnTabReselected::" + position);
                refreshFragment(position);
            }
        });

    }

    /**
     * 初始化模块数据
     * 策略：第一次从内置数据中读取数据，后通过接口更新模块数据，延迟加载策略
     */
    private void initModuleData() {
        final String modulePath = FileManager.getDataDir() + FileManager.file_homeTopModle;
        String moduleJson = FileManager.readFile(modulePath);
        if (TextUtils.isEmpty(moduleJson)) {
            moduleJson = FileManager.getFromAssets(this, "homeTopModle");
//            Log.i(tag, "moduleJson::内置：：：" + moduleJson);
            final String finalModuleJson = moduleJson;
            FileManager.saveFileToCompletePath(modulePath, finalModuleJson.toString(), false);

        } else {
//            Log.i(tag, "moduleJson::sd卡：：：" + moduleJson);
        }
        initHomeModuleData(moduleJson);
        setRequestModuleData();
    }

    /**
     * 请求模块数据
     */
    private void setRequestModuleData() {
        final String modulePath = FileManager.getDataDir() + FileManager.file_homeTopModle;
        String url = StringManager.API_GET_LEVEL;
        ReqEncyptInternet.in().doEncyptAEC(url, "version=" + "v1", new InternetCallback() {
            @Override
            public void loaded(int flag, String url, final Object o) {
                if (flag >= ReqInternet.REQ_OK_STRING) {
                    //moduleData处理模块数据
//                    initHomeModuleData(o);
                    FileManager.saveFileToCompletePath(modulePath, o.toString(), false);

                } else {
                    Log.i(tag, "object::" + o);
                }
            }
        });
    }

    /**
     * 处理模块
     *
     * @param object
     */
    private void initHomeModuleData(Object object) {
        if (object == null) return;
        //处理模块数据
        ArrayList<Map<String, String>> listModule = StringManager.getListMapByJson(object);
        int size = listModule.size();
        if (listModule != null && size > 0) {
            moduleData = listModule;
        } else return;
        for (int i = 0; i < size; i++) {
            HomeModuleBean bean = new HomeModuleBean();
            bean.setTitle(listModule.get(i).get("title"));
            bean.setType(listModule.get(i).get("type"));
            bean.setWebUrl(listModule.get(i).get("webUrl"));
            bean.setIsSelf(listModule.get(i).get("isSelf"));
            bean.setOpenMode(listModule.get(i).get("openMode"));
            String level = listModule.get(i).get("level");
            if (!TextUtils.isEmpty(level)) {
                bean.setTwoData(level);//设置二级数据内容
            }
            bean.setPosition(i);
            listBean.add(bean);
        }
        mAdapter.notifyDataSetChanged();
        home_tab.notifyDataSetChanged();
    }

    /**
     * 请求数据
     *
     * @param refresh
     */
    public void loadData(boolean refresh) {

    }

    /**
     * 刷新内容视图
     *
     * @param isRefresh
     */
    public void refreshContentView(boolean isRefresh) {
        if(isRefresh){
            refreshFragment(itemPosition);
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("zyj","mainHome::onresume");
        refreshAdData();
        //为了解决首页打开webview后再调用此句再打开的webView的大小就不是0*0啦
        onResumeFake();
        if(recommedType.equals(listBean.get(itemPosition).getType())) {
            Log.i("zyj","mainHome::onPause");
            setRecommedTime(System.currentTimeMillis());
        }

        if (mNeedRefCurrFm) {
            mNeedRefCurrFm = false;
            refreshContentView(true);
        }
    }

    public void onResumeFake(){
        if(resumeCount != 0)
            SpecialWebControl.initSpecialWeb(this,rl,"index","","");
        resumeCount++;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(recommedType.equals(listBean.get(itemPosition).getType())) {
            Log.i("zyj","mainHome::onPause");
            setRecommedStatistic();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ObserverManager.getInstence().unRegisterObserver(this);
    }

    private boolean mNeedRefCurrFm;
    @Override
    public void notify(String name, Object sender, Object data) {
        if (!TextUtils.isEmpty(name)) {
            switch (name) {
                case ObserverManager.NOTIFY_VIPSTATE_CHANGED://VIP 状态发生改变需要刷新
                    mNeedRefCurrFm = true;
                    List<Fragment> fragments = getSupportFragmentManager().getFragments();
                    if (fragments != null && fragments.size() > 0) {
                        for (Fragment fragment : fragments) {
                            if (fragment instanceof HomeFragment) {
                                HomeFragment homeFragment = (HomeFragment) fragment;
                                if (homeFragment.isCreated())
                                    homeFragment.notifyNeedRefCurrData();
                            } else if (fragment instanceof BaseHomeFragment) {
                                BaseHomeFragment homeFragment = (BaseHomeFragment) fragment;
                                if (homeFragment.isCreated())
                                    homeFragment.setNeedRefCurrData();
                            }
                        }
                    }
                    break;
            }
        }
    }

    public class HomePagerAdapter extends FragmentStatePagerAdapter {
        private ArrayList<HomeModuleBean> mPlates;

        public HomePagerAdapter(FragmentManager fm, ArrayList<HomeModuleBean> titles) {
            super(fm);
            this.mPlates = titles;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mPlates.get(position).getTitle();
        }

        @Override
        public Fragment getItem(int position) {
            HomeModuleBean plateData = mPlates.get(position);
            BaseHomeFragment fragment = null;
            if (fragment == null) {
                if ("H5".equals(plateData.getType())) {
                    fragment = BaseHomeFragment.instance(plateData);
                } else {
                    fragment = HomeFragment.newInstance(plateData);
                }
            }
            Bundle bundle = fragment.getArguments();
            fragment.setArguments(bundle);
            return fragment;
        }

        @Override
        public int getCount() {
            return mPlates.size();
        }
    }

    /**
     * 刷新
     *
     * @param position
     */
    private void refreshFragment(final int position) {
        //调用页面的刷新方法
        BaseHomeFragment homeFragment = getFragmentByPos(position);
        if (homeFragment == null)
            return;
        if (homeFragment instanceof HomeFragment) {
            HomeFragment fragment = (HomeFragment) homeFragment;
            fragment.returnListTop();
            fragment.refresh();
        } else if (homeFragment instanceof BaseHomeFragment) {
            homeFragment.loadWebData(true);
        }
    }

    /**
     * 获取某个位置的HomeFragment
     * @param position
     * @return 如果不存在，则返回null。
     */
    private BaseHomeFragment getFragmentByPos(int position) {
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if (fragments != null && fragments.size() > 0) {
            for (Fragment fragment : fragments) {
                if (fragment instanceof HomeFragment) {
                    HomeFragment homeFragment = (HomeFragment) fragment;
                    if (homeFragment.getmoduleBean().getPosition() == position) {
                        return homeFragment;
                    }
                } else if (fragment instanceof BaseHomeFragment) {
                    BaseHomeFragment homeFragment = (BaseHomeFragment) fragment;
                    if (homeFragment.getModuleBean().getPosition() == position)
                        return homeFragment;

                }
            }
        }
        return  null;
    }

    /**
     * 统计推荐列表使用时间
     */
    private void setRecommedStatistic(){
        long startTime=0;
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if (fragments != null && fragments.size() > 0) {
            for (Fragment fragment : fragments) {
                if (fragment instanceof HomeFragment) {
                    HomeFragment homeFragment = (HomeFragment) fragment;
                    if (homeFragment.isRecom()) {//推荐模块
                        startTime=homeFragment.getStatrTime();
                        break;
                    }
                }
            }
        }
        long nowTime= System.currentTimeMillis();
        if(startTime>0){
            Log.i("zyj","stop::"+String.valueOf((nowTime-startTime)/1000));
            XHClick.saveStatictisFile("home",recommedType_statictus,"","","","stop",String.valueOf((nowTime-startTime)/1000),"","","","");
            //置数据
            setRecommedTime(0);
        }
        //展现
    }
    /**
     * 推荐列表设置开始时间
     */
    public void setRecommedTime(long time){
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if (fragments != null && fragments.size() > 0) {
            for (Fragment fragment : fragments) {
                if (fragment instanceof HomeFragment) {
                    HomeFragment homeFragment = (HomeFragment) fragment;
                    if (homeFragment.isRecom()) {//推荐模块
                        homeFragment.setStatrTime(time);
                        break;
                    }
                }
            }
        }
    }

    /**
     * 保存统计数据
     * ---推荐模块的停留时间
     * ---推荐模块的展示列表数量
     */
    public void saveNowStatictis(){
        if(recommedType.equals(listBean.get(itemPosition).getType())){//当前模块类型是推荐
            //保存推荐模块事件
            setRecommedStatistic();
            //保存展示条数
            List<Fragment> fragments = getSupportFragmentManager().getFragments();
            if (fragments != null && fragments.size() > 0) {
                for (Fragment fragment : fragments) {
                    if (fragment instanceof HomeFragment) {
                        HomeFragment homeFragment = (HomeFragment) fragment;
                        if (homeFragment.isRecom()) {//推荐模块
                            homeFragment.setStatisticShowNum();
                            break;
                        }
                    }
                }
            }
        }
    }

    /**
     * 刷新广告策略。--全部刷新
     */
    public void refreshAdData(){
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if(fragments==null||fragments.size()<=0)return;
        int size= fragments.size();
        for(int position=0;position<size;position++) {
            if (fragments != null && fragments.size() > position) {
                if (fragments.get(position) instanceof HomeFragment) {
                    ((HomeFragment) fragments.get(position)).isNeedRefresh(false);
                }
            }
        }
    }
    /**
     * 刷新广告策略。---单个刷新
     */
    public void refreshAdData(int position){
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if (fragments != null && fragments.size() > position) {
            if (fragments.get(position) instanceof HomeFragment) {
                ((HomeFragment) fragments.get(position)).isNeedRefresh(false);
            }
        }
    }

    public void setCurrentTab(int itemPosition){
        if(viewpager == null || itemPosition < 0 || itemPosition >= viewpager.getChildCount() ){
            return;
        }
        viewpager.setCurrentItem(itemPosition);
    }

}

