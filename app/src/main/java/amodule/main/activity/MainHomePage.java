package amodule.main.activity;

import android.graphics.PixelFormat;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.xiangha.R;

import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.SpecialWebControl;
import acore.logic.XHClick;
import acore.logic.load.LoadManager;
import acore.override.activity.mian.MainBaseActivity;
import acore.tools.IObserver;
import acore.tools.ObserverManager;
import acore.tools.StringManager;
import acore.tools.ToolsDevice;
import amodule.home.HomeDataControler;
import amodule.home.HomeViewControler;
import amodule.main.Main;
import amodule.main.adapter.HomeAdapter;
import aplug.basic.InternetCallback;
import aplug.basic.ReqEncyptInternet;
import aplug.basic.ReqInternet;
import third.ad.control.AdControlParent;

/**
 * Description :
 * PackageName : amodule.home
 * Created by MrTrying on 2017/11/13 13:53.
 * Author : mrtrying
 * E_mail : ztanzeyu@gmail.com
 */

public class MainHomePage extends MainBaseActivity implements IObserver {
    public final static String KEY = "MainIndex";
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
    boolean LoadOver = false;

    boolean HeaderDataLoaded = false;

    protected long startTime = -1;//开始的时间戳

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mViewContrloer = new HomeViewControler(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_home_page);
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        Main.allMain.allTab.put(KEY, this);//这个Key值不变
        //初始化
        initialize();
        //加载数据
        loadData();

        initPostTime();
        //注册通知
        ObserverManager.getInstence().registerObserver(this, ObserverManager.NOTIFY_VIPSTATE_CHANGED);
    }

    //初始化
    private void initialize() {
        //初始化 UI 控制
        mViewContrloer.onCreate();
        //初始化数据控制
        mDataControler = new HomeDataControler(this);
        mDataControler.setInsertADCallback((listDatas, isBack) -> {
            AdControlParent adControlParent = mDataControler.getAdControl();
            if (adControlParent != null
                    && mDataControler.getUpDataSize() > 0
                    && !isBack)
                adControlParent.setLimitNum(mDataControler.getUpDataSize());
            return adControlParent != null ?
                    adControlParent.getNewAdData(listDatas, isBack) : listDatas;
        });
        mDataControler.setNotifyDataSetChangedCallback(() -> {
            if (mHomeAdapter != null) mHomeAdapter.notifyDataSetChanged();
        });
        mDataControler.setEntryptDataCallback(this::EntryptData);
        //初始化adapter
        mHomeAdapter = new HomeAdapter(this, mDataControler.getData(), mDataControler.getAdControl());
        mHomeAdapter.setHomeModuleBean(mDataControler.getHomeModuleBean());
        mHomeAdapter.setViewOnClickCallBack(isOnClick -> refresh());
    }

    private void initPostTime() {
        String logPostTime = AppCommon.getConfigByLocal("logPostTime");
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
                    mViewContrloer.getRvListView(),
                    mHomeAdapter,
                    true,
                    v -> {
                        if (HeaderDataLoaded)
                            EntryptData(!LoadOver);
                    }
            );
            loadManager.getSingleLoadMore(mViewContrloer.getRvListView()).setVisibility(View.GONE);
            mViewContrloer.addOnScrollListener();
        }
        loadCacheData();
        mViewContrloer.setTipMessage();
    }

    @Override
    protected void onStart() {
        super.onStart();
        startLoadTime = System.currentTimeMillis();
        loadRemoteData();
    }

    private void loadCacheData(){
        mDataControler.loadCacheHomeData(getHeaderCallback(true));
    }

    private void loadRemoteData(){
        mDataControler.loadServiceHomeData(getHeaderCallback(false));
        mDataControler.loadServiceTopData(new InternetCallback(this) {
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
     *
     * @return 回调
     */
    public InternetCallback getHeaderCallback(boolean isCache) {
        return new InternetCallback(this) {
            @Override
            public void loaded(int i, String s, Object o) {
                Log.i("tzy",(isCache ? "cacheTime = " : "serviceTime = ") + (System.currentTimeMillis() - startLoadTime) + "ms");
                HeaderDataLoaded = true;
                if (i >= ReqEncyptInternet.REQ_OK_STRING) {
                    if (mViewContrloer != null)
                        mViewContrloer.setHeaderData(StringManager.getListMapByJson(o), isCache);
                    Log.i("tzy" , "setHeaderData " + (isCache ? "cacheTime = " : "serviceTime = ") + (System.currentTimeMillis() - startLoadTime) + "ms");
                    if (!isCache && mDataControler != null) {
                        mDataControler.saveCacheHomeData((String) o);
                        isRefreshingHeader = false;
                    }
                }
                if(!LoadOver){
                    EntryptData(true);
                }
            }
        };
    }

    /**
     * 请求数据入口
     *
     * @param refresh，是否刷新
     */
    private void EntryptData(final boolean refresh) {
        //已经load
        LoadOver = true;
        if (refresh && mDataControler != null) {
            mDataControler.isNeedRefresh(false);
        }
        if (mDataControler != null) {
            Log.i("tzy", "EntryptData::" + mDataControler.isNeedRefCurrData());
            if (mDataControler.isNeedRefCurrData()) {
                //需要刷新当前数据
                mDataControler.setNeedRefCurrData(false);
                mDataControler.setBackUrl("");
                mDataControler.clearData();
                if (mHomeAdapter != null)
                    mHomeAdapter.notifyDataSetChanged();
            }
        }

        if (refresh) {//向上翻页
            if (mDataControler != null)
                mDataControler.refreshADIndex();
            mViewContrloer.setStatisticShowNum();
        }
        mDataControler.loadServiceFeedData(refresh, new HomeDataControler.OnLoadDataCallback() {
            @Override
            public void onPrepare() {
                if (refresh) {
                    XHClick.mapStat(MainHomePage.this, "a_recommend", "刷新效果", "下拉刷新");
                    loadManager.hideProgressBar();
                    mViewContrloer.returnListTop();
                }
                Button loadmore = loadManager.getSingleLoadMore(mViewContrloer.getRvListView());
                if(null != loadmore){
                    loadmore.setVisibility(View.VISIBLE);
                }
                loadManager.changeMoreBtn(mViewContrloer.getRvListView(), ReqInternet.REQ_OK_STRING, -1, -1, LoadOver ? 2 : 1, refresh);
            }

            @Override
            public void onAfter(boolean refresh, int flag, int loadCount) {
                if(refresh){
                    isRefreshingFeed = false;
                }
                loadManager.hideProgressBar();
                mViewContrloer.setFeedheaderVisibility(!mDataControler.getData().isEmpty());
                if (ToolsDevice.isNetworkAvailable(MainHomePage.this)) {
                    loadManager.changeMoreBtn(mViewContrloer.getRvListView(), flag, LoadManager.FOOTTIME_PAGE,
                            refresh ? mDataControler.getData().size() : loadCount, 0, refresh);
                }
            }

            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailed() {
                if (!ToolsDevice.isNetworkAvailable(MainHomePage.this)) {
                    loadManager.changeMoreBtn(mViewContrloer.getRvListView(),
                            ReqInternet.REQ_OK_STRING,
                            LoadManager.FOOTTIME_PAGE,
                            -1, 0, refresh);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mDataControler != null) {
            mDataControler.isNeedRefresh(false);
        }
        onResumeFake();
        Log.i("zyj", "mainHome::onPause");
        setRecommedTime(System.currentTimeMillis());
        if (mNeedRefCurrFm) {
            mNeedRefCurrFm = false;
            refresh();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        setRecommedStatistic();
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
                    Log.i("tzy","VIP 状态发生改变需要刷新");
                    mNeedRefCurrFm = true;
                    break;
            }
        }
    }

    boolean isRefreshingHeader = false;
    boolean isRefreshingFeed = false;
    public void refresh() {
        if(isRefreshingHeader || isRefreshingFeed){
            return;
        }
        isRefreshingHeader = true;
        isRefreshingFeed = true;
        loadRemoteData();
        EntryptData(true);
    }

    private void onResumeFake() {
        SpecialWebControl.initSpecialWeb(this, rl, "index", "", "");
    }

    /** 统计推荐列表使用时间 */
    private void setRecommedStatistic() {
        long nowTime = System.currentTimeMillis();
        if (startTime > 0) {
            Log.i("zyj", "stop::" + String.valueOf((nowTime - startTime) / 1000));
            XHClick.saveStatictisFile("home", recommedType_statictus, "", "", "", "stop", String.valueOf((nowTime - startTime) / 1000), "", "", "", "");
            //置数据
            setRecommedTime(0);
        }
    }

    public void setRecommedTime(long time) {
        this.startTime = time;
    }

}
