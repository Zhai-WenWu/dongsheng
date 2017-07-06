package amodule.search.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import acore.logic.AppCommon;
import acore.logic.load.LoadManager;
import acore.override.activity.base.BaseActivity;
import acore.tools.StringManager;
import amodule.search.adapter.AdapterSearch;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;
import xh.basic.internet.UtilInternet;
import xh.basic.tool.UtilString;

/**
 * Created by ：airfly on 2016/10/21 17:45.
 */

public class ZhishiResultView extends RelativeLayout {

    private BaseActivity mActivity;
    private LoadManager mLoadManager;
    private ListView mListview;
    private ImageView returnTop;
    private AdapterSearch nousAdapter;
    private CopyOnWriteArrayList<Map<String, String>> mListData = new CopyOnWriteArrayList<>();
    private String searchKey;
    private int mCurrentPage;
    private AtomicBoolean isSearching;
    private int everyPage;
    private LinearLayout ll_noData;
    private View mParentView;

    public ZhishiResultView(Context context) {
        this(context, null);
    }

    public ZhishiResultView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ZhishiResultView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.c_view_second_detail_search, this, true);
    }

    public void init(BaseActivity activity,View parentView) {
        mActivity = activity;
        mParentView = parentView;
        mLoadManager = mActivity.loadManager;
        initData();
        initView();
    }


    public void search(String key) {

        if (TextUtils.isEmpty(key))
            return;

        searchKey = key;
        mCurrentPage = 0;

        mListData.clear();
        nousAdapter.notifyDataSetChanged();

        mLoadManager.setLoading(mListview, nousAdapter, true, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getData();
            }
        });
        ((GlobalSearchView) mParentView).setSecondLevelView(this);
    }

    private void initData() {
        isSearching = new AtomicBoolean(false);
    }

    private void initView() {
        mListview = (ListView) findViewById(R.id.v_scroll);
        ll_noData = (LinearLayout) findViewById(R.id.v_no_data_search);
        ll_noData.setVisibility(View.GONE);
        returnTop = (ImageView) findViewById(R.id.return_top);
        returnTop.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                returnTop.clearAnimation();
                RotateAnimation animation = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                animation.setInterpolator(new LinearInterpolator());
                animation.setDuration(500);
                animation.setFillAfter(true);// 特效animation设置
                returnTop.startAnimation(animation);
                returnListTop();
            }
        });

        mListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Map<String, String> zhishiMap = mListData.get(position);
                AppCommon.openUrl(mActivity, "nousInfo.app?code=" + zhishiMap.get("code"), true);
            }
        });

        nousAdapter = new AdapterSearch(mListview, mListData, R.layout.c_search_result_zhishi_item,
                new String[]{"img", "title", "classifyName", "allClick","aboveLine","bottomLine","line"},
                new int[]{R.id.iv_img_zhishi, R.id.tv_des_zhishi, R.id.tv_cate_zhishi, R.id.tv_observed_zhishi,R.id.above_line,R.id.v_zhishi_item_tail,R.id.line});
        nousAdapter.setViewBinder(new SimpleAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Object data, String textRepresentation) {
                switch (view.getId()){
                    case R.id.above_line:
                    case R.id.v_zhishi_item_tail:
                    case R.id.line:
                        view.setVisibility("hide".equals(data)?GONE:VISIBLE);
                        return true;
                }
                return false;
            }
        });

    }

    private void getData() {

        if (isSearching.get())
            return;
        isSearching.set(true);
        mCurrentPage++;
        //更新加载按钮状态
        mLoadManager.changeMoreBtn(mListview, ReqInternet.REQ_OK_STRING, -1, -1, mCurrentPage, mListData.size() == 0);
        String url = StringManager.api_soList + "?type=zhishi&s=" + searchKey + "&page=" + mCurrentPage;
        ReqInternet.in().doGet(url, new InternetCallback(mActivity.getApplicationContext()) {
            @Override
            public void loaded(int flag, String url, Object returnObj) {
                int loadPage = 0;
                if (flag >= UtilInternet.REQ_OK_STRING) { // 表示成功
                    if (mCurrentPage == 1) {
                        mListData.clear();
                    }
                    Map<String, String> map = StringManager.getFirstMap(returnObj);
                    if (map.containsKey("nous") && !"null".equals(map.get("nous"))) {
                        ArrayList<Map<String, String>> listReturn = StringManager.getListMapByJson(map.get("nous"));

                        // 解析菜谱
                        for (Map<String, String> mapReturn : listReturn) {
                            mapReturn.put("allClick", mapReturn.get("allClick") + "浏览");
                            mapReturn.put("img", mapReturn.get("img").equals("") ? "" : mapReturn.get("img"));
                            mapReturn.put("aboveLine", "hide");
                            mapReturn.put("bottomLine", "hide");
                            mapReturn.put("line", "show");
                            mListData.add(mapReturn);
                        }
                        loadPage = listReturn.size();
                        if (nousAdapter != null) {
                            nousAdapter.notifyDataSetChanged();
                        } else if (mListview != null && (nousAdapter = (AdapterSearch) mListview.getAdapter()) != null) {
                            nousAdapter.notifyDataSetChanged();
                        }
                    }
                } else {
                    toastFaildRes(flag, true, returnObj);
                }
                hideProgresBar();
                //如果没有数据显示提示
                if (flag >= UtilInternet.REQ_OK_STRING && mListData.size() == 0) {
                    returnTop.setVisibility(View.GONE);
                    ll_noData.setVisibility(View.VISIBLE);
                    mLoadManager.hideLoadFaildBar();
                } else {
                    returnTop.setVisibility(View.VISIBLE);
                    ll_noData.setVisibility(View.GONE);
                }
                isSearching.set(false);
                everyPage = everyPage == 0 ? loadPage : everyPage;
                mCurrentPage = mLoadManager.changeMoreBtn(mListview, flag, everyPage, loadPage, mCurrentPage, mListData.size() == 0);
            }
        });
    }

    private void returnListTop() {
        if (mListview != null) {
            mListview.setSelection(0);
        }
    }

    private void hideProgresBar() {
        mLoadManager.hideProgressBar();
        mActivity.loadManager.hideProgressBar();
    }
}
