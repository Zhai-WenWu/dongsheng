package amodule.lesson.activity;

import android.os.Bundle;
import android.text.TextUtils;

import com.xiangha.R;

import java.util.List;
import java.util.Map;

import acore.override.activity.base.BaseAppCompatActivity;
import amodule._common.helper.WidgetDataHelper;
import amodule.lesson.controler.data.LessonListDataController;
import amodule.lesson.controler.view.LessonListViewController;
import amodule.lesson.listener.IDataListener;
import aplug.basic.ReqEncyptInternet;

public class LessonListPage extends BaseAppCompatActivity {

    private LessonListDataController mDataController;
    private LessonListViewController mViewController;
    private String mStyle;
    private String mTitle;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActivity("",2,0,R.layout.back_title_bar,R.layout.lesson_list_page);
        initData();
        if (TextUtils.isEmpty(mStyle))
            return;
        initController();
        addListener();
        startLoadData();
    }

    private void initData() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mStyle = bundle.getString(WidgetDataHelper.KEY_STYLE);
            mTitle = bundle.getString(WidgetDataHelper.KEY_TITLE);
        }
    }

    private void startLoadData() {
        loadManager.setLoading(mViewController.getPtrFrame(), mViewController.getListView(), mDataController.getAdapter(), true,
                v -> mDataController.loadData(true, this),
                v -> mDataController.loadData(false, this));
    }

    private void initController() {
        mDataController = new LessonListDataController(this, mStyle);
        mViewController = new LessonListViewController(this);
        mViewController.setTitle(mTitle);
    }

    private void addListener() {
        mDataController.setOnDataListener(new IDataListener<List<Map<String, String>>>() {
            @Override
            public void onGetData(List<Map<String, String>> maps, boolean refresh) {
                loadManager.changeMoreBtn(ReqEncyptInternet.REQ_OK_STRING, -1, -1, mDataController.getCurrentPage(), maps == null || maps.size() == 0);
            }

            @Override
            public void onDataReady(List<Map<String, String>> maps, boolean refresh, int flag) {
                if (refresh && mViewController != null) {
                    mViewController.refreshComplete();
                }
                loadManager.changeMoreBtn(flag, mDataController.getEveryPageCount(), mDataController.getLoadCount(), mDataController.getCurrentPage(), false);
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
    }
}
