package amodule.main.activity;

import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.annimon.stream.Stream;
import com.xiangha.R;

import java.util.ArrayList;
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
import third.ad.control.AdControlHomeDish;
import third.ad.control.AdControlParent;

import static third.ad.control.AdControlHomeDish.tag_yu;

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
    public final static String STATICTUS_ID_HOMEPAGE = "a_index";
    public final static String STATICTUS_ID_PULISH = "a_post_button580";

    //数据控制
    HomeDataControler mDataControler;
    //UI控制
    HomeViewControler mViewContrloer;
    //adapter
    HomeAdapter mHomeAdapter;

    private Handler mHandler;
    //是否加载
    boolean LoadOver = false;

    protected long startTime = -1;//开始的时间戳

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_home_page);
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        Main.allMain.allTab.put(KEY, this);//这个Key值不变
        //初始化
        initialize();

        String logPostTime = AppCommon.getConfigByLocal("logPostTime");
        if (!TextUtils.isEmpty(logPostTime)) {
            Map<String, String> map = StringManager.getFirstMap(logPostTime);
            if (map.containsKey("postTime")
                    && !TextUtils.isEmpty(map.get("postTime"))) {
                XHClick.HOME_STATICTIS_TIME = Integer.parseInt(map.get("postTime"), 10) * 1000;
            }
        }
        ObserverManager.getInstence().registerObserver(this, ObserverManager.NOTIFY_VIPSTATE_CHANGED);
    }

    //初始化
    private void initialize() {
        //初始化 UI 控制
        mViewContrloer = new HomeViewControler(this);
        //初始化数据控制
        mDataControler = new HomeDataControler(this);
        mDataControler.setInsertADCallback((listDatas, isBack) -> {
            AdControlParent adControlParent = mViewContrloer.getAdControl();
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
        mDataControler.setOnNeedRefreshCallback(this::isNeedRefresh);
        //初始化adapter
        mHomeAdapter = new HomeAdapter(this, mDataControler.getData(), mViewContrloer.getAdControl());
        mHomeAdapter.setHomeModuleBean(mDataControler.getHomeModuleBean());
        mHomeAdapter.setViewOnClickCallBack(isOnClick -> refresh());

        mHandler = new Handler(msg -> {
            //待定
            return false;
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadData();
    }

    public void loadData() {
        mDataControler.loadCacheHomeData(getHeaderCallback(true));
        loadHeaderData();
        if (!LoadOver) {
            loadManager.setLoading(mViewContrloer.getRvListView(),
                    mHomeAdapter,
                    true,
                    v -> EntryptData(!LoadOver)
            );
            loadManager.getSingleLoadMore(mViewContrloer.getRvListView()).setVisibility(View.GONE);
            mViewContrloer.addOnScrollListener();
        }
    }

    public void loadHeaderData() {
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
                if (i >= ReqEncyptInternet.REQ_OK_STRING) {
                    if (!isCache && mDataControler != null) {
                        mDataControler.saveCacheHomeData((String) o);
                    }
                    if (mViewContrloer != null)
                        mViewContrloer.setHeaderData(StringManager.getListMapByJson(o), isCache);
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
        if (refresh) {
            isNeedRefresh(false);
        }
        Log.i("tzy", "EntryptData::" + mDataControler.isNeedRefCurrData());
        if (mDataControler.isNeedRefCurrData()) {
            //需要刷新当前数据
            mDataControler.setNeedRefCurrData(false);
            mDataControler.setBackUrl("");
            mDataControler.clearData();
            if (mHomeAdapter != null)
                mHomeAdapter.notifyDataSetChanged();
        }
        //已经load
        LoadOver = true;

        if (refresh) {//向上翻页
            mViewContrloer.refreshADIndex();
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
                loadManager.getSingleLoadMore(mViewContrloer.getRvListView()).setVisibility(View.VISIBLE);
                loadManager.changeMoreBtn(mViewContrloer.getRvListView(), ReqInternet.REQ_OK_STRING, -1, -1, LoadOver ? 2 : 1, refresh);
            }

            @Override
            public void onAfter(boolean refresh, int flag, int loadCount) {
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

    /**
     * 刷新广告数据
     *
     * @param isForceRefresh 是否强制刷新广告
     */
    private void isNeedRefresh(boolean isForceRefresh) {
        AdControlHomeDish adControlHomeDish = mViewContrloer.getAdControl();
        if (adControlHomeDish == null
                || mDataControler.getData() == null
                || mHomeAdapter == null)
            return;//条件过滤
        boolean state = adControlHomeDish.isNeedRefresh();
        Log.i(tag_yu, "isNeedRefresh::::" + state + " :: 推荐 ; isForceRefresh = " + isForceRefresh);
        if (isForceRefresh)
            state = true;//强制刷新
        if (state) {
            //重新请求广告
            adControlHomeDish.setAdDataCallBack((tag, nums) -> {
                if (tag >= 1 && nums > 0) {
                    handlerMainThreadUIAD();
                }
            });
            adControlHomeDish.refreshData();
            //推荐首页
            adControlHomeDish.setAdLoadNumberCallBack(Number -> {
                if (Number > 7) {
                    handlerMainThreadUIAD();
                }
            });
            //去掉全部的广告位置
            ArrayList<Map<String, String>> listTemp = new ArrayList<>();
            Stream.of(mDataControler.getData()).forEach(map -> {
                if (map.containsKey("adstyle")
                        && "ad".equals(map.get("adstyle"))) {
                    listTemp.add(map);
                }
            });
            Log.i(tag_yu, "删除广告");
            if (listTemp.size() > 0) {
                mDataControler.getData().removeAll(listTemp);
            }
            mHomeAdapter.notifyDataSetChanged();
        }
    }

    /** 处理广告在主线程中处理 */
    protected void handlerMainThreadUIAD() {
        mHandler.post(() -> {
            mDataControler.setData(mViewContrloer.getAdControl().getNewAdData(mDataControler.getData(), false));
            if (mHomeAdapter != null)
                mHomeAdapter.notifyDataSetChanged();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        isNeedRefresh(false);
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
                    mNeedRefCurrFm = true;
                    break;
            }
        }
    }

    public void refresh() {
        if (mViewContrloer != null) {
            mViewContrloer.refreshBouy();
        }
        loadHeaderData();
        EntryptData(true);
    }

    private int resumeCount = 0;

    public void onResumeFake() {
        if (resumeCount != 0)
            SpecialWebControl.initSpecialWeb(this, rl, "index", "", "");
        resumeCount++;
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
