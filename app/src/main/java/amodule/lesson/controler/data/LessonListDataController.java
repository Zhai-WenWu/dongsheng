package amodule.lesson.controler.data;

import android.content.Context;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import acore.override.activity.base.BaseAppCompatActivity;
import acore.tools.FileManager;
import acore.tools.StringManager;
import acore.widget.rvlistview.adapter.RvBaseAdapter;
import amodule.home.adapter.HorizontalAdapter1;
import amodule.home.adapter.HorizontalAdapter2;
import amodule.home.adapter.HorizontalAdapter3;
import amodule.lesson.listener.IDataListener;
import aplug.basic.InternetCallback;
import aplug.basic.ReqEncyptInternet;

/**
 * Description : //TODO
 * PackageName : amodule.lesson.controler.data
 * Created by mrtrying on 2017/12/19 11:23:19.
 * e_mail : ztanzeyu@gmail.com
 */
public class LessonListDataController {
    private IDataListener<List<Map<String, String>>> mListener;
    private int mCurrentPage = 0;
    private int mEveryPageCount = 10;
    private List<Map<String, String>> mDatas;
    private RvBaseAdapter<Map<String, String>> mRecyclerAdapter;
    private int mLoadCount;

    public LessonListDataController(BaseAppCompatActivity appCompatActivity, String style) {
        initData(style, appCompatActivity);
    }

    private void initData(String style, Context context) {
        mDatas = new ArrayList<>();
        if (!TextUtils.isEmpty(style)) {
            if (style != null) {
                switch (style) {
                    case "1":
                    case "6":
                        mRecyclerAdapter = new HorizontalAdapter1(context, mDatas);
                        break;
                    case "2":
                    case "4":
                    case "5":
                        mRecyclerAdapter = new HorizontalAdapter2(context, mDatas);
                        break;
                    case "3":
                        mRecyclerAdapter = new HorizontalAdapter3(context, mDatas);
                        break;
                    default:
                        mRecyclerAdapter = new HorizontalAdapter1(context, mDatas);
                        break;
                }
            }
        }
    }

    public void onResume() {

    }

    public void onPause() {

    }

    public void onDestroy() {

    }

    public void loadData(boolean refresh, Context context) {
        if (refresh)
            mCurrentPage = 0;
        else
            mCurrentPage++;
        if (mListener != null)
            mListener.onGetData(mDatas, refresh);
        ReqEncyptInternet.in().doEncypt(StringManager.API_RECOMMEND, "", new InternetCallback(context) {
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

                mLoadCount = 0;
                if (flag >= ReqEncyptInternet.REQ_OK_STRING) {
                    mLoadCount = list.size();
                    ArrayList<Map<String, String>> datas = list;
                    if (mCurrentPage > 0) {
                        mDatas.addAll(datas);
                        mRecyclerAdapter.notifyDataSetChanged();
                    } else {
                        mRecyclerAdapter.updateData(datas);
                    }
                }
                if (mEveryPageCount == 0) {
                    mEveryPageCount = mLoadCount;
                }
                onDataReady(refresh, flag);
            }
        });
    }

    private void onDataReady(boolean refresh, int flag) {
        if (mListener != null)
            mListener.onDataReady(mDatas, refresh, flag);
    }

    public void setOnDataListener(IDataListener<List<Map<String, String>>> listener) {
        mListener = listener;
    }

    public RvBaseAdapter<Map<String, String>> getAdapter() {
        return mRecyclerAdapter;
    }

    public int getEveryPageCount() {
        return mEveryPageCount;
    }

    public int getCurrentPage() {
        return mCurrentPage;
    }

    public int getLoadCount() {
        return mLoadCount;
    }



}
