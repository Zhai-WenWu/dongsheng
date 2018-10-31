package amodule.main.activity;

import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import acore.broadcast.ConnectionChangeReceiver;
import acore.logic.ConfigMannager;
import acore.logic.MessageTipController;
import acore.logic.SpecialWebControl;
import acore.logic.XHClick;
import acore.logic.load.LoadManager;
import acore.override.activity.mian.MainBaseActivity;
import acore.tools.IObserver;
import acore.tools.ObserverManager;
import acore.tools.StringManager;
import acore.tools.ToolsDevice;
import amodule._common.conf.GlobalVariableConfig;
import amodule._common.helper.WidgetDataHelper;
import amodule.home.HomeDataControler;
import amodule.home.HomeViewControler;
import amodule.home.delegate.IVipGuideModuleCallback;
import amodule.home.module.HomeVipGuideModule;
import amodule.main.Main;
import amodule.main.adapter.HomeAdapter;
import amodule.main.delegate.ISetMessageTip;
import aplug.basic.InternetCallback;
import aplug.basic.ReqEncyptInternet;
import aplug.basic.ReqInternet;
import third.ad.control.AdControlParent;
import third.ad.tools.AdPlayIdConfig;

import static acore.logic.ConfigMannager.KEY_LOGPOSTTIME;
import static acore.logic.stat.StatisticsManager.STAT_DATA;
import static acore.tools.ObserverManager.NOTIFY_AUTO_LOGIN;
import static acore.tools.ObserverManager.NOTIFY_LOGIN;
import static acore.tools.ObserverManager.NOTIFY_LOGOUT;
import static acore.tools.ObserverManager.NOTIFY_VIPSTATE_CHANGED;

/**
 * 首页
 * 采用mvc
 */
public class MainHomePage extends MainBaseActivity implements IObserver,ISetMessageTip {
    public final static String KEY = "MainIndex";
    public final static String recommedType = "recomv2";//推荐类型
    public final static String recommedType_statictus = "recom";//推荐类型-用于统计
    public final static String STATICTUS_ID_HOMEPAGE = "a_index580";
    public final static String STATICTUS_ID_PULISH = "a_post_button580";

    //数据控制
    HomeDataControler mDataControler;
    //UI控制
    HomeViewControler mViewContrloer;
    //adapter
    HomeAdapter mHomeAdapter;
    //是否加载
    volatile boolean LoadOver = false;

    boolean mRecommendFirstLoadEnable = false;

    boolean mRecommendFirstLoad = true;

    private ConnectionChangeReceiver mReceiver;
    private Handler handler = new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewContrloer = new HomeViewControler(this);
        setContentView(R.layout.a_home_page);
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        setActivity();
        //初始化
        initialize();
        //加载数据
        loadData();

        initPostTime();
        //注册通知
        ObserverManager.getInstance().registerObserver(this, NOTIFY_VIPSTATE_CHANGED, NOTIFY_LOGIN, NOTIFY_LOGOUT,NOTIFY_AUTO_LOGIN);
        registerConnectionReceiver();
    }

    private void setActivity() {
        if(Main.allMain != null && Main.allMain.allTab != null
                && !Main.allMain.allTab.containsKey(KEY)){
            Main.allMain.allTab.put(KEY, this);//这个Key值不变
        }
    }

    //初始化
    private void initialize() {
        //初始化 UI 控制
        mViewContrloer.onCreate();
        mViewContrloer.getRvListView().setOnTouchListener((v, event) -> isRefreshingHeader);
        mViewContrloer.setGetStatDataCallback(position -> mDataControler.getData().get(position).get(STAT_DATA));
        //初始化数据控制
        mDataControler = new HomeDataControler(this);
        mDataControler.setInsertADCallback((listDatas, isBack) -> {
            return insertAd(listDatas, isBack);
        });
        mDataControler.setNotifyDataSetChangedCallback(() -> {
            notifyDataChanged();
        });
        mDataControler.setEntryptDataCallback(this::EntryptData);
        //初始化adapter
        mHomeAdapter = new HomeAdapter(this, mDataControler.getData(), mDataControler.getAdControl());
        mHomeAdapter.setHomeModuleBean(mDataControler.getHomeModuleBean());
        mHomeAdapter.setViewOnClickCallBack(isOnClick -> refresh());
        mHomeAdapter.setListType(HomeAdapter.LIST_TYPE_STAGGERED);
    }

    private ArrayList<Map<String, String>> insertAd(ArrayList<Map<String, String>> listDatas, boolean isBack) {
        AdControlParent adControlParent = mDataControler.getAdControl();
        if (adControlParent != null
                && mDataControler.getUpDataSize() > 0
                && !isBack)
            adControlParent.setLimitNum(mDataControler.getUpDataSize());
        return adControlParent != null ?
                adControlParent.getNewAdData(listDatas, isBack) : listDatas;
    }

    private void registerConnectionReceiver() {
        mReceiver = new ConnectionChangeReceiver(new ConnectionChangeReceiver.ConnectionChangeListener() {
            @Override
            public void disconnect() {
                if (null != mViewContrloer) {
                    mViewContrloer.showNetworkTip();
                }
            }

            @Override
            public void wifi() {
                if (null != mViewContrloer) {
                    mViewContrloer.hindNetworkTip();
                }
            }

            @Override
            public void mobile() {
                if (null != mViewContrloer) {
                    mViewContrloer.hindNetworkTip();
                }
            }
        });
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mReceiver, filter);
    }

    private void initPostTime() {
        String logPostTime = ConfigMannager.getConfigByLocal(KEY_LOGPOSTTIME);
        if (!TextUtils.isEmpty(logPostTime)) {
            Map<String, String> map = StringManager.getFirstMap(logPostTime);
            String postTimeValue = map.get("postTime");
            if (!TextUtils.isEmpty(postTimeValue)) {
                XHClick.HOME_STATICTIS_TIME = Integer.parseInt(postTimeValue, 10) * 1000;
            }
        }
    }

    long startLoadTime;

    public void loadData() {
        startLoadTime = System.currentTimeMillis();
        if (!LoadOver) {
            assert mViewContrloer != null;
            loadManager.setLoading(
                    mViewContrloer.getRefreshLayout(),
                    mViewContrloer.getRvListView(),
                    mHomeAdapter,
                    true,
                    v -> {
                        innerRefresh();
                        if (mViewContrloer != null)
                            mViewContrloer.refreshBuoy();
                    },
                    v -> {
                        if (mRecommendFirstLoadEnable) {
                            EntryptData(mRecommendFirstLoad);
                        } else {
                            mRecommendFirstLoadEnable = true;
                        }
                    }
            );
            mViewContrloer.addOnScrollListener();
            if (!ToolsDevice.isNetworkAvailable(this)) {
                loadManager.hideProgressBar();
            }
        }
        loadCacheData();
        mViewContrloer.setTipMessage();
    }

    public void handleVipGuideStatus() {
        setVipGuide();
    }

    private void setVipGuide() {
        MainHomePage.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mDataControler != null) {
                    mDataControler.getHomeVipGuideModule(new IVipGuideModuleCallback() {
                        @Override
                        public void onModuleCallback(HomeVipGuideModule module) {
                            if (mViewContrloer != null) {
                                mViewContrloer.setHomeVipBannerModule(module);
                                mViewContrloer.setHomeVipBannerViewVisible(module != null);
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadRemoteData();
    }

    private void loadCacheData() {
        mDataControler.loadCacheHomeData(getHeaderCallback(true));
    }
    private boolean isRefresh= false;//是否是第一次刷新
    private void loadRemoteData() {
        startLoadTime = System.currentTimeMillis();
        mDataControler.loadServiceHomeData(getHeaderCallback(false));
        if(isRefresh){
            mDataControler.isNeedRefresh(true);
        }
        isRefresh=true;
        loadAdData();
    }

    private void loadAdData() {
        ArrayList<String> arr = new ArrayList<>();
        Collections.addAll(arr,AdPlayIdConfig.HOME_BANNEER_LIST);
        mDataControler.loadAdData(arr, (isRefresh, map) -> {
                    mViewContrloer.setAdController(mDataControler.getAllAdController());
                    mViewContrloer.setAdData(map, arr, isRefresh);},
                this, "sy_banner");
    }

    private void loadTopData() {
        mDataControler.loadServiceTopData(new InternetCallback() {
            @Override
            public void loaded(int i, String s, Object o) {
                if (i >= ReqEncyptInternet.REQ_OK_STRING
                        && mViewContrloer != null)
                    mViewContrloer.setTopData(StringManager.getListMapByJson(o));
            }
        });
    }

    /**
     * 获取header数据回调
     *
     * @param isCache 是否是缓存
     * @return 回调
     */
    public InternetCallback getHeaderCallback(boolean isCache) {
        handler.postDelayed(() -> mViewContrloer.refreshComplete(),5*1000);
        return new InternetCallback() {
            @Override
            public void loaded(int i, String s, Object o) {
                loadManager.hideProgressBar();
                mRecommendFirstLoad = true;
                mViewContrloer.refreshComplete();
                LoadOver = true;
                if (i >= ReqEncyptInternet.REQ_OK_STRING) {
                    handler.postDelayed(() -> {
                        if (mViewContrloer != null) {
                            ArrayList<Map<String, String>> list = StringManager.getListMapByJson(o);
                            if (list.size() > 2) {
                                if (mDataControler != null) {
                                    mDataControler.clearData();
                                }
                                Map<String, String> recommendList = list.remove(list.size() - 1);
                                mViewContrloer.setHeaderData(list, isCache);
                                if (recommendList != null && !recommendList.isEmpty()) {
                                    Map<String, String> widgetData = StringManager.getFirstMap(recommendList.get(WidgetDataHelper.KEY_WIDGET_DATA));
                                    Map<String, String> data = StringManager.getFirstMap(widgetData.get(WidgetDataHelper.KEY_DATA));
                                    ArrayList<Map<String, String>> listData = StringManager.getListMapByJson(data.get(WidgetDataHelper.KEY_LIST));
                                    if (mDataControler != null) {
                                        listData = insertAd(listData, false);
                                        mHomeAdapter.setCache(isCache);
                                        mDataControler.addOuputSideData(listData);
                                        mDataControler.setNextUrl(data.get("nexturl"));
                                        notifyDataChanged();
                                    }

                                    Map<String,String> parameterMap = StringManager.getFirstMap(widgetData.get(WidgetDataHelper.KEY_PARAMETER));
                                    parameterMap = StringManager.getFirstMap(parameterMap.get(WidgetDataHelper.KEY_TITLE));
                                    if(mViewContrloer != null && !TextUtils.isEmpty(parameterMap.get("text1"))){
                                        mViewContrloer.setFeedTitleText(parameterMap.get("text1"));
                                    }
                                }
                            } else {
                                mViewContrloer.setHeaderData(list, isCache);
                            }
                        }
                    },300);
                    if (!isCache && mDataControler != null) {
                        mDataControler.saveCacheHomeData((String) o);
                    }
                }
                if (!isCache && mDataControler != null) {
                    loadTopData();
                }
                isRefreshingHeader = false;

            }
        };
    }

    /**
     * 请求数据入口
     *
     * @param firstLoad，是否第一次加载
     */
    private void EntryptData(final boolean firstLoad) {
//        Log.i("tzy_data", "EntryptData::" + refresh);
        //已经load
        LoadOver = true;
        mRecommendFirstLoad = false;
        mDataControler.loadServiceFeedData(firstLoad, new HomeDataControler.OnLoadDataCallback() {
            @Override
            public void onPrepare() {
                loadManager.loading(mViewContrloer.getRvListView(),  !LoadOver);
                Button loadmore = loadManager.getSingleLoadMore(mViewContrloer.getRvListView());
                if (null != loadmore) {
                    loadmore.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onAfter(int flag, int loadCount) {
                loadManager.hideProgressBar();
                mViewContrloer.setFeedheaderVisibility(!mDataControler.getData().isEmpty());
                if (ToolsDevice.isNetworkAvailable(MainHomePage.this)) {
                    loadManager.loadOver(flag,mViewContrloer.getRvListView(), loadCount);
                } else {

                }
            }

            @Override
            public void onSuccess() {
            }

            @Override
            public void onFailed() {
                if (!ToolsDevice.isNetworkAvailable(MainHomePage.this)) {
                    loadManager.loadOver(0,mViewContrloer.getRvListView(), 1);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        setActivity();
        if (mDataControler != null) {
            mDataControler.isNeedRefresh(false);
        }
        onResumeFake();
        if (mNeedRefCurrFm) {
            mNeedRefCurrFm = false;
            refresh();
            GlobalVariableConfig.clearAttentionModules();
            GlobalVariableConfig.clearFavoriteModules();
            GlobalVariableConfig.clearGoodModules();
        }
        mViewContrloer.setMessage(MessageTipController.newInstance().getMessageNum());

    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mViewContrloer != null){
            mViewContrloer.setStatisticShowNum();
        }
        if(handler!=null){
            handler.removeCallbacksAndMessages(null);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ObserverManager.getInstance().unRegisterObserver(this);
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }
    }

    private boolean mNeedRefCurrFm;

    @Override
    public void notify(String name, Object sender, Object data) {
        if (!TextUtils.isEmpty(name)) {
            switch (name) {
                case NOTIFY_VIPSTATE_CHANGED://VIP 状态发生改变需要刷新
                    Log.i("tzy", "VIP 状态发生改变需要刷新");
                    mNeedRefCurrFm = true;
                    setVipGuide();
                    break;
                case NOTIFY_AUTO_LOGIN:
                case NOTIFY_LOGIN:
                case NOTIFY_LOGOUT:
                    setVipGuide();
                    break;
            }
        }
    }

    boolean isRefreshingHeader = false;

    public void refresh() {
        Log.i("tzy_data", "refresh()");
        mViewContrloer.autoRefresh();
        GlobalVariableConfig.clearFavoriteModules();
        GlobalVariableConfig.clearAttentionModules();
        setVipGuide();
    }

    private void innerRefresh() {
        if (isRefreshingHeader) {
            return;
        }
        isRefreshingHeader = true;
        if(mViewContrloer != null){
            mViewContrloer.returnListTop();
        }
        loadRemoteData();
    }

    private void onResumeFake() {
        SpecialWebControl.initSpecialWeb(this, rl, "index", "", "");
    }

    @Override
    public void setMessageTip(int tipCournt) {
        mViewContrloer.setMessage(tipCournt);
    }

    private void notifyDataChanged() {
        if (mHomeAdapter != null && mViewContrloer.getRvListView() != null) {
            if (!mViewContrloer.getRvListView().isComputingLayout()) {
                mHomeAdapter.notifyDataSetChanged();
            }
        }
    }

    /**
     * 隐藏gif
     */
    public void handleNoGif(){
        mHomeAdapter.notifyDataSetChanged();
    }
}
