package amodule.lesson.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.xiangha.R;

import java.util.List;
import java.util.Map;

import acore.logic.XHClick;
import acore.logic.load.LoadManager;
import acore.override.activity.base.BaseAppCompatActivity;
import acore.tools.StringManager;
import acore.tools.ToolsDevice;
import amodule.lesson.adapter.LessonHomeAdapter;
import amodule.lesson.controler.data.LessonHomeDataController;
import amodule.lesson.controler.view.LessonHomeHeaderControler;
import amodule.lesson.controler.view.LessonHomeViewController;
import amodule.main.activity.MainHomePage;
import aplug.basic.InternetCallback;
import aplug.basic.ReqEncyptInternet;
import aplug.basic.ReqInternet;

public class LessonHome extends BaseAppCompatActivity {

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
        initActivity("",2,0,R.layout.back_title_bar,R.layout.a_lesson_home);

        //初始化
        initialize();
        //加载数据
        loadData();
    }

    private void initialize() {
        //初始化 UI 控制
        mViewController.onCreate();
        //初始化数据控制
        mDataController = new LessonHomeDataController(this);
        mDataController.setNotifyDataSetChangedCallback(() -> mAdapter.notifyDataSetChanged());

        mAdapter = new LessonHomeAdapter(this,mDataController.getData());
    }

    private void loadData() {
        if(!LoadOver){
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
            if(!ToolsDevice.isNetworkAvailable(this)){
                loadManager.hideProgressBar();
            }
        }
        loadHeaderData();
    }

    private void loadHeaderData() {
        mDataController.laodRemoteHeaderData(new InternetCallback(this) {
            @Override
            public void loaded(int i, String s, Object o) {
                HeaderDataLoaded = true;
                if(i >= ReqEncyptInternet.REQ_OK_STRING){
                    if (mViewController != null)
                        mViewController.setHeaderData(StringManager.getListMapByJson(o));
                }
                isRefreshingHeader = false;
                mViewController.refreshComplete();
                if(!LoadOver){
                    EntryptData(true);
                }
            }
        });
    }

    private void EntryptData(boolean refresh) {
        //已经load
        LoadOver = true;
        mDataController.setOnLoadDataCallback(new LessonHomeDataController.OnLoadDataCallback() {
            @Override
            public void onPrepare() {
                loadManager.changeMoreBtn(mViewController.getRvListView(), ReqInternet.REQ_OK_STRING, -1, -1, LoadOver ? 2 : 1, !LoadOver);
                if (refresh) {
                    loadManager.hideProgressBar();
                    mViewController.returnListTop();
                }
                Button loadmore = loadManager.getSingleLoadMore(mViewController.getRvListView());
                if(null != loadmore){
                    loadmore.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onAfter(boolean refersh, int flag, int loadCount) {
                if(refresh){
                    isRefreshingFeed = false;
                }
                loadManager.hideProgressBar();
                if (ToolsDevice.isNetworkAvailable(LessonHome.this)) {
                    loadManager.changeMoreBtn(mViewController.getRvListView(), flag, LoadManager.FOOTTIME_PAGE,
                            refresh ? mDataController.getData().size() : loadCount, 0, refresh);
                }else {

                }
            }

            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailed() {
                if (!ToolsDevice.isNetworkAvailable(LessonHome.this)) {
                    loadManager.changeMoreBtn(mViewController.getRvListView(),
                            ReqInternet.REQ_OK_STRING,
                            LoadManager.FOOTTIME_PAGE,
                            -1, 0, refresh);
                }
            }
        });
        mDataController.laodRemoeteExtraData(refresh,new InternetCallback(this) {
            @Override
            public void loaded(int i, String s, Object o) {
                if(i >= ReqEncyptInternet.REQ_OK_STRING){

                }
            }
        });
    }

    boolean isRefreshingHeader = false;
    boolean isRefreshingFeed = false;
    public void refresh() {
        Log.i("tzy_data","refresh()");
        mViewController.autoRefresh();
    }

    private void inerRefresh(){
        if(isRefreshingHeader || isRefreshingFeed){
            return;
        }
        isRefreshingHeader = true;
        isRefreshingFeed = true;
        loadData();
        EntryptData(true);
    }


}
