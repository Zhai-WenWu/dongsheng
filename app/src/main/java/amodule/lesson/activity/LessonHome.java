package amodule.lesson.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.xiangha.R;

import acore.logic.load.LoadManager;
import acore.override.activity.base.BaseAppCompatActivity;
import acore.tools.IObserver;
import acore.tools.ObserverManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import amodule.lesson.adapter.LessonHomeAdapter;
import amodule.lesson.controler.data.LessonHomeDataController;
import amodule.lesson.controler.view.LessonHomeViewController;
import aplug.basic.InternetCallback;
import aplug.basic.ReqEncyptInternet;
import aplug.basic.ReqInternet;

import static acore.tools.ObserverManager.NOTIFY_VIPSTATE_CHANGED;

public class LessonHome extends BaseAppCompatActivity implements IObserver {

    LessonHomeViewController mViewController;
    LessonHomeDataController mDataController;
    //是否加载
    volatile boolean LoadOver = false;
    boolean HeaderDataLoaded = false;
    LessonHomeAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewController = new LessonHomeViewController(this);
        initActivity("", 2, 0, R.layout.back_title_bar, R.layout.a_lesson_home);

        //初始化
        initialize();
        //加载数据
        loadData();

        ObserverManager.getInstence().registerObserver(this, NOTIFY_VIPSTATE_CHANGED);
    }

    private void initialize() {
        //初始化 UI 控制
        mViewController.onCreate();
        //初始化数据控制
        mDataController = new LessonHomeDataController(this);
        mDataController.setNotifyDataSetChangedCallback(() -> mAdapter.notifyDataSetChanged());

        mAdapter = new LessonHomeAdapter(this, mDataController.getData());
    }

    private void loadData() {
        if (!LoadOver) {
            loadManager.setLoading(
                    mViewController.getRefreshLayout(),
                    mViewController.getRvListView(),
                    mAdapter,
                    true,
                    v -> inerRefresh(),
                    v -> {
                        if (HeaderDataLoaded)
                            EntryptData(!LoadOver);
                    }
            );
            loadManager.getSingleLoadMore(mViewController.getRvListView()).setVisibility(View.GONE);
            if (!ToolsDevice.isNetworkAvailable(this)) {
                loadManager.hideProgressBar();
            }
        }
        loadHeaderData();
    }

    private void loadHeaderData() {
        mDataController.laodRemoteHeaderData(new InternetCallback() {
            @Override
            public void loaded(int i, String s, Object o) {
                HeaderDataLoaded = true;
                if (i >= ReqEncyptInternet.REQ_OK_STRING) {
                    if (mViewController != null)
                        mViewController.setHeaderData(StringManager.getListMapByJson(o));
                }
                isRefreshingHeader = false;
                if (mViewController != null) {
                    mViewController.refreshComplete();
                }
                if (!LoadOver) {
                    setDataControllerCallback();
                    EntryptData(true);
                }
            }
        });
    }

    private void setDataControllerCallback() {
        mDataController.setOnLoadDataCallback(new LessonHomeDataController.OnLoadDataCallback() {
            @Override
            public void onPrepare(boolean refersh) {
                loadManager.changeMoreBtn(mViewController.getRvListView(), ReqInternet.REQ_OK_STRING, -1, -1, LoadOver ? 2 : 1, !LoadOver);
                if (refersh) {
                    loadManager.hideProgressBar();
                    mViewController.returnListTop();
                }
                Button loadmore = loadManager.getSingleLoadMore(mViewController.getRvListView());
                if (null != loadmore) {
                    loadmore.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onAfter(boolean refersh, int flag, int loadCount) {
                if (refersh) {
                    isRefreshingFeed = false;
                }
                loadManager.hideProgressBar();
                if (ToolsDevice.isNetworkAvailable(LessonHome.this)) {
                    loadManager.changeMoreBtn(mViewController.getRvListView(), flag, LoadManager.FOOTTIME_PAGE,
                            refersh ? mDataController.getData().size() : loadCount, 0, refersh);
                } else {

                }
            }

            @Override
            public void onSuccess(boolean refresh) {

            }

            @Override
            public void onFailed(boolean refresh) {
                if (!ToolsDevice.isNetworkAvailable(LessonHome.this)) {
                    loadManager.changeMoreBtn(mViewController.getRvListView(),
                            ReqInternet.REQ_OK_STRING,
                            LoadManager.FOOTTIME_PAGE,
                            -1, 0, refresh);
                }
            }
        });
    }

    private void EntryptData(boolean refresh) {
        //已经load
        LoadOver = true;
        mDataController.laodRemoeteExtraData(refresh);
    }

    boolean isRefreshingHeader = false;
    boolean isRefreshingFeed = false;

    public void refresh() {
        Log.i("tzy_data", "refresh()");
        mViewController.autoRefresh();
    }

    private void inerRefresh() {
        Log.i("tzy", "inerRefresh: ");
        if (isRefreshingHeader || isRefreshingFeed) {
            return;
        }
        isRefreshingHeader = true;
        isRefreshingFeed = true;
        loadData();
        EntryptData(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ObserverManager.getInstence().unRegisterObserver(this);
    }

    @Override
    public void notify(String name, Object sender, Object data) {
        if (!TextUtils.isEmpty(name)) {
            switch (name) {
                case ObserverManager.NOTIFY_VIPSTATE_CHANGED:
                    inerRefresh();
                    break;
            }
        }
    }
}
