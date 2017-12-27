package amodule.lesson.controler.data;

import android.content.Context;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import acore.override.activity.base.BaseAppCompatActivity;
import acore.tools.StringManager;
import acore.widget.rvlistview.adapter.RvBaseAdapter;
import amodule.home.adapter.HorizontalAdapter1;
import amodule.home.adapter.HorizontalAdapter2;
import amodule.lesson.listener.IDataListener;
import aplug.basic.InternetCallback;
import aplug.basic.ReqEncyptInternet;

/**
 * Description :
 * PackageName : amodule.lesson.controler.data
 * Created by mrtrying on 2017/12/19 11:23:19.
 * e_mail : ztanzeyu@gmail.com
 */
public class LessonListDataController {
    private IDataListener<List<Map<String, String>>> mListener;
    private int mCurrentPage;
    private int mEveryPageCount = 10;
    private List<Map<String, String>> mDatas;
    private RvBaseAdapter<Map<String, String>> mRecyclerAdapter;
    private int mLoadCount;
    private boolean mNeedRef;
    private String mCode;

    private String mTitle;

    public LessonListDataController(BaseAppCompatActivity appCompatActivity, String style, String code) {
        mCode = code;
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
                    default:
                        mRecyclerAdapter = new HorizontalAdapter1(context, mDatas);
                        break;
                }
            }
        }
    }

    public void onResume(Context context) {
        if (mNeedRef) {
            mNeedRef = false;
            loadData(true, context);
        }
    }

    public void onPause() {

    }

    public void onDestroy() {
        mListener = null;
        mDatas = null;
        mRecyclerAdapter = null;
        mNeedRef = false;
    }

    public void loadData(boolean refresh, Context context) {
        if (refresh)
            mCurrentPage = 1;
        else
            mCurrentPage++;
        if (mListener != null)
            mListener.onGetData(mDatas, refresh);
        ReqEncyptInternet.in().doEncypt(StringManager.API_SCHOOL_CHAPTERLIST, "code=" + mCode + "&page=" + mCurrentPage, new InternetCallback() {
            @Override
            public void loaded(int flag, String s, Object o) {

                Map<String, String> map = StringManager.getFirstMap(o);
                mTitle = map.get("title");
                ArrayList<Map<String, String>> list = StringManager.getListMapByJson(map.get("list"));
                mLoadCount = 0;
                if (flag >= ReqEncyptInternet.REQ_OK_STRING) {
                    mLoadCount = list.size();
                    if (!refresh) {
                        mDatas.addAll(list);
                        mRecyclerAdapter.notifyDataSetChanged();
                    } else {
                        mRecyclerAdapter.updateData(list);
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

    public void setNeedRefresh(boolean refresh) {
        mNeedRef = refresh;
    }

    public String getTitle() {
        return mTitle;
    }
}
