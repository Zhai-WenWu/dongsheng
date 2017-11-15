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
import java.util.LinkedHashMap;
import java.util.Map;

import acore.logic.SpecialWebControl;
import acore.logic.XHClick;
import acore.logic.load.LoadManager;
import acore.override.activity.mian.MainBaseActivity;
import acore.tools.StringManager;
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

public class MainHomePage extends MainBaseActivity {
    public static final String KEY = "MainIndex";

    //数据控制
    HomeDataControler mDataControler;
    //UI控制
    HomeViewControler mViewContrloer;

    HomeAdapter mHomeAdapter;

    private Handler mHandler;
    //是否加载
    boolean LoadOver = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_home_page);
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        Main.allMain.allTab.put(KEY, this);//这个Key值不变
        //初始化
        initialize();
    }

    //初始化
    private void initialize() {
        mViewContrloer = new HomeViewControler(this);
        mDataControler = new HomeDataControler(this);
        mDataControler.setInsertADCallback((listDatas,isBack) -> {
            AdControlParent adControlParent = mViewContrloer.getAdControl();
            if(adControlParent != null
                    && mDataControler.getUpDataSize() > 0
                    && !isBack)
                adControlParent.setLimitNum(mDataControler.getUpDataSize());
            return adControlParent != null ?
                adControlParent.getNewAdData(listDatas,isBack) : listDatas;
        });
        mDataControler.setNotifyDataSetChangedCallback(()->{
            if(mHomeAdapter != null) mHomeAdapter.notifyDataSetChanged();
        });
        mHomeAdapter = new HomeAdapter(this, mDataControler.getData(), mViewContrloer.getAdControl());
        mHomeAdapter.setHomeModuleBean(mDataControler.getHomeModuleBean());
        mHomeAdapter.setViewOnClickCallBack(isOnClick -> refresh());
        mHomeAdapter.notifyDataSetChanged();
        mHandler = new Handler(msg -> {
            //待定
            return false;
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mDataControler.loadCacheHomeData(getHeaderCallback(true));
        mDataControler.loadServiceHomeData(getHeaderCallback(false));
        mDataControler.loadServiceTopData(new InternetCallback(this) {
            @Override
            public void loaded(int i, String s, Object o) {
                if (i >= ReqEncyptInternet.REQ_OK_STRING)
                    mViewContrloer.setTopData(StringManager.getListMapByJson(o));
            }
        });
        //TODO 美观
        if (!LoadOver) {
            loadManager.setLoading(mViewContrloer.getRvListView(),
                    mHomeAdapter,
                    true,
                    v -> EntryptData(!LoadOver)
            );
            loadManager.getSingleLoadMore(mViewContrloer.getRvListView()).setVisibility(View.GONE);
        }
        loadManager.hideProgressBar();
    }

    /**
     * 获取header数据回调
     *
     * @param isCache 是否是缓存
     *
     * @return
     */
    public InternetCallback getHeaderCallback(boolean isCache) {
        return new InternetCallback(this) {
            @Override
            public void loaded(int i, String s, Object o) {
                if (i >= ReqEncyptInternet.REQ_OK_STRING) {
                    if (!isCache) {
                        mDataControler.saveCacheHomeData((String) o);
                    }
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
            //TODO setStatisticShowNum();
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
            public void onAfter(boolean refresh,int flag,int loadCount) {
                loadManager.hideProgressBar();
                loadManager.changeMoreBtn(mViewContrloer.getRvListView(), flag, LoadManager.FOOTTIME_PAGE,
                        refresh ? mDataControler.getData().size() : loadCount, 0, refresh);
            }

            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailed() {

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
            state = isForceRefresh;//强制刷新
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
    }


    private int resumeCount = 0;

    public void onResumeFake() {
        if (resumeCount != 0)
            SpecialWebControl.initSpecialWeb(this, rl, "index", "", "");
        resumeCount++;
    }

    public void refresh() {

    }
}
