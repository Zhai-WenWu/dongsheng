package amodule.lesson.controler.data;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import acore.logic.load.LoadManager;
import acore.override.activity.base.BaseAppCompatActivity;
import acore.tools.FileManager;
import acore.tools.StringManager;
import acore.widget.rvlistview.RvListView;
import acore.widget.rvlistview.adapter.RvBaseAdapter;
import amodule.home.adapter.HorizontalAdapter1;
import amodule.home.adapter.HorizontalAdapter2;
import amodule.home.adapter.HorizontalAdapter3;
import amodule.lesson.listener.IDataListener;
import aplug.basic.InternetCallback;
import aplug.basic.ReqEncyptInternet;
import cn.srain.cube.views.ptr.PtrClassicFrameLayout;

/**
 * Description : //TODO
 * PackageName : amodule.lesson.controler.data
 * Created by mrtrying on 2017/12/19 11:23:19.
 * e_mail : ztanzeyu@gmail.com
 */
public class LessonListDataController {
    private BaseAppCompatActivity mAct;

    private IDataListener<List<Map<String, String>>> mListener;
    public LoadManager mLoadManager = null;
    private int mCurrentPage = 0;
    private int mEveryPageCount = 10;
    private List<Map<String, String>> mDatas;
    private RvBaseAdapter<Map<String, String>> mRecyclerAdapter;

    public LessonListDataController(BaseAppCompatActivity appCompatActivity, String style) {
        mAct = appCompatActivity;
        mLoadManager = mAct.loadManager;
        initData(style);
    }

    private void initData(String style) {
        mDatas = new ArrayList<>();
        if (!TextUtils.isEmpty(style)) {
            if (style != null) {
                switch (style) {
                    case "1":
                    case "6":
                        mRecyclerAdapter = new HorizontalAdapter1(mAct, mDatas);
                        break;
                    case "2":
                    case "4":
                    case "5":
                        mRecyclerAdapter = new HorizontalAdapter2(mAct, mDatas);
                        break;
                    case "3":
                        mRecyclerAdapter = new HorizontalAdapter3(mAct, mDatas);
                        break;
                    default:
                        mRecyclerAdapter = new HorizontalAdapter1(mAct, mDatas);
                        break;
                }
            }
        }
    }

    public void startLoadData(PtrClassicFrameLayout refreshLayout, RvListView gridView) {
        mLoadManager.setLoading(refreshLayout, gridView, mRecyclerAdapter, true,
                v -> getData(true),
                v -> getData(false));
    }

    public void onResume() {

    }

    public void onPause() {

    }

    public void onDestroy() {

    }

    private void getData(boolean refresh) {
        if (mListener != null)
            mListener.onGetData(refresh);
        if (refresh)
            mCurrentPage = 0;
        else
            mCurrentPage++;
        mLoadManager.changeMoreBtn(ReqEncyptInternet.REQ_OK_STRING, -1, -1, mCurrentPage, mDatas == null || mDatas.size() == 0);
        ReqEncyptInternet.in().doEncypt(StringManager.API_RECOMMEND, "", new InternetCallback(mAct) {
            @Override
            public void loaded(int flag, String s, Object o) {

                //TODO 测试--------start
                String hoemDataStr = FileManager.readFile(FileManager.getSDCacheDir() + "homeDataCache").trim();
                ArrayList<Map<String, String>> ls = StringManager.getListMapByJson(hoemDataStr);


                ArrayList<Map<String, String>> list = new ArrayList<>();
                for (Map<String, String> map:
                ls){
                    if (TextUtils.equals(map.get("widgetType"), "3")) {
                        String wd = map.get("widgetData");
                        Map<String, String> map1 = StringManager.getFirstMap(wd);
                        String data1 = map1.get("data");
                        Map<String, String> map2 = StringManager.getFirstMap(data1);
                        String list1 = map2.get("list");
                        list = StringManager.getListMapByJson(list1);
                        break;
                    }
                }

                //TODO 测试--------end

                int loadCount = 0;
                if (flag >= ReqEncyptInternet.REQ_OK_STRING) {
                    loadCount = list.size();
                    ArrayList<Map<String, String>> datas = list;
                    if (mCurrentPage > 0) {
                        mDatas.addAll(datas);
                        mRecyclerAdapter.notifyDataSetChanged();
                    } else {
                        mRecyclerAdapter.updateData(datas);
                    }
                }
                if (mEveryPageCount == 0) {
                    mEveryPageCount = loadCount;
                }
                onDataReady(refresh);
                mLoadManager.changeMoreBtn(flag, mEveryPageCount, loadCount, mCurrentPage, false);
            }
        });
    }

    private void onDataReady(boolean refresh) {
        if (mListener != null)
            mListener.onDataReady(mDatas, refresh);
    }

    public void setOnDataListener(IDataListener<List<Map<String, String>>> listener) {
        mListener = listener;
    }

}
