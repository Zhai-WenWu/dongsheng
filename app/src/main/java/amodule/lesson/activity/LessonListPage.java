package amodule.lesson.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.xiangha.R;

import java.util.List;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.LoginManager;
import acore.logic.XHClick;
import acore.override.activity.base.BaseAppCompatActivity;
import acore.override.helper.XHActivityManager;
import acore.observer.IObserver;
import acore.observer.ObserverManager;
import amodule._common.helper.WidgetDataHelper;
import amodule.lesson.controler.data.LessonListDataController;
import amodule.lesson.controler.view.LessonListViewController;
import amodule.lesson.listener.IDataListener;
import amodule.vip.VipDataController;

public class LessonListPage extends BaseAppCompatActivity implements IObserver {

    private LessonListDataController mDataController;
    private LessonListViewController mViewController;
    private String mStyle;
    private String mCode;

    private VipDataController mVipDataController;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActivity("",2,0,R.layout.back_title_bar,R.layout.lesson_list_page);
        initData();
        if (TextUtils.isEmpty(mStyle) || TextUtils.isEmpty(mCode))
            return;
        initController();
        addListener();
        startLoadData();
        ObserverManager.getInstance().registerObserver(this, ObserverManager.NOTIFY_VIPSTATE_CHANGED);
    }

    private void initData() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mStyle = bundle.getString(WidgetDataHelper.KEY_STYLE);
            mCode = bundle.getString(WidgetDataHelper.KEY_CODE);
        }
    }

    private void startLoadData() {
        loadManager.setLoading(mViewController.getPtrFrame(), mViewController.getListView(), mDataController.getAdapter(), true,
                v -> mDataController.loadData(true, this),
                v -> mDataController.loadData(false, this));
        mVipDataController.loadVIPButtonData();
    }

    private void initController() {
        mDataController = new LessonListDataController(this, mStyle, mCode);
        mViewController = new LessonListViewController(this, mStyle);
        mVipDataController = new VipDataController();
    }

    private void addListener() {
        mDataController.setOnDataListener(new IDataListener<List<Map<String, String>>>() {
            @Override
            public void onGetData(List<Map<String, String>> maps, boolean refresh) {
                loadManager.loading(mViewController.getListView(),maps == null || maps.size() == 0);
            }

            @Override
            public void onDataReady(List<Map<String, String>> maps, boolean refresh, int flag) {
                String title = mDataController.getTitle();
                if (mViewController != null) {
                    if (!TextUtils.isEmpty(title) && (!mViewController.titleShowing() || refresh)) {
                        mViewController.setTitle(title);
                        mViewController.setTitleViewVisibility(View.VISIBLE);
                    } else if(TextUtils.isEmpty(title)) {
                        mViewController.setTitleViewVisibility(View.GONE);
                    }
                    if (refresh) {
                        mViewController.refreshComplete();
                    }
                }
                loadManager.loadOver(flag,mViewController.getListView(),mDataController.getLoadCount());
            }
        });
        mViewController.setContentStatisticCallback((id, twoLevel, threeLevel, position) -> {
            boolean isVIP = LoginManager.isVIP();
            XHClick.mapStat(this, isVIP ? "vip_chief_list" : "nonvip_chief_list", "课程数据", "");
        });
        mViewController.setVIPBtnStatisticCallback((id, twoLevel, threeLevel, position) -> {
            boolean isVIP = LoginManager.isVIP();
            XHClick.mapStat(this, isVIP ? "vip_chief_list" : "nonvip_chief_list", isVIP ? "底部续费按钮" : "底部开通按钮", "");
        });
        mVipDataController.setDataCallback(() -> {
            if (mViewController != null) {
                boolean show = mVipDataController.isVipBtnShow();
                mViewController.setVIPButtonVisibility(show ? View.VISIBLE : View.GONE);
                if (!show)
                    return;
                String title = mVipDataController.getTitle();
                String textColor = mVipDataController.getTextColor();
                String bgColor = mVipDataController.getBgColor();
                if (TextUtils.isEmpty(title) || TextUtils.isEmpty(textColor) || TextUtils.isEmpty(bgColor)) {
                    return;
                }
                mViewController.setVIPButton(title, Color.parseColor(textColor), Color.parseColor(bgColor));
                mViewController.setVIPButtonClickListener(v -> {
                    String url = mVipDataController.getUrl();
                    if (!TextUtils.isEmpty(url))
                        AppCommon.openUrl(XHActivityManager.getInstance().getCurrentActivity(), url, false);
                });
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mDataController != null)
            mDataController.onResume(this);
        if (mVipDataController != null)
            mVipDataController.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mViewController != null) {
            mViewController.onDestroy();
            mViewController = null;
        }
        if (mDataController != null) {
            mDataController.onDestroy();
            mDataController = null;
        }
        if (mVipDataController != null) {
            mVipDataController.onDestroy();
        }
        ObserverManager.getInstance().unRegisterObserver(this);
    }

    @Override
    public void notify(String name, Object sender, Object data) {
        if (!TextUtils.isEmpty(name)) {
            switch (name) {
                case ObserverManager.NOTIFY_VIPSTATE_CHANGED:
                    if (mDataController != null)
                        mDataController.setNeedRefresh(true);
                    if (mVipDataController != null)
                        mVipDataController.setNeedRefresh(true);
                    break;
            }
        }
    }
}
