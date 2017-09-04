package amodule.search.view;

import android.content.Context;
import android.content.Intent;
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

import com.xiangha.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import acore.logic.load.LoadManager;
import acore.override.activity.base.BaseActivity;
import acore.tools.StringManager;
import amodule.dish.activity.ListDish;
import amodule.search.adapter.AdapterSearch;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;
import xh.basic.internet.UtilInternet;
import xh.basic.tool.UtilString;

/**
 * Created by ：airfly on 2016/10/21 11:02.
 */
public class CaidanResultView extends RelativeLayout {
    private BaseActivity mActivity;
    private LoadManager mLoadManager;
    private ListView mListview;
    private ImageView returnTop;
    private AdapterSearch mAdapter;
    private CopyOnWriteArrayList<Map<String, String>> mListData = new CopyOnWriteArrayList<>();
    private String searchKey;
    private int mCurrentPage,everyPage;
    private AtomicBoolean isSearching;
    private LinearLayout ll_noData;
    private View mParentView;

    public CaidanResultView(Context context) {
        this(context, null);
    }

    public CaidanResultView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CaidanResultView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.c_view_second_detail_search, this, true);
    }


    public void init(BaseActivity activity,View parentView) {
        mActivity = activity;
        mLoadManager = mActivity.loadManager;
        mParentView = parentView;
        initData();
        initView();
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

                Map<String, String> caidanMap = mListData.get(position);
                Intent intent = new Intent(mActivity, ListDish.class);
                intent.putExtra("name", caidanMap.get("name"));
                intent.putExtra("type", "caidan");
                intent.putExtra("g1", caidanMap.get("code"));
                mActivity.startActivity(intent);
            }
        });

        mAdapter = new AdapterSearch(mListview, mListData,
                R.layout.c_search_result_caidan_item,
                new String[]{"img1", "img2", "name", "dishNum", "allClick"},
                new int[]{R.id.iv_img_left_caidan, R.id.iv_img_right_caidan,
                        R.id.tv_tag_caidan, R.id.tv_num_caidan, R.id.tv_observed_caidan});
    }

    public void search(String key) {
        if (TextUtils.isEmpty(key))
            return;

        searchKey = key;
        mCurrentPage = 0;
        mListData.clear();
        mAdapter.notifyDataSetChanged();
        mLoadManager.setLoading(mListview, mAdapter, true, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getData();
            }
        });
        ((GlobalSearchView) mParentView).setSecondLevelView(this);
    }

    private void getData() {

        if (isSearching.get())
            return;
        isSearching.set(true);
        mCurrentPage++;
        //更新加载按钮状态
        mLoadManager.changeMoreBtn(mListview, ReqInternet.REQ_OK_STRING, -1, -1, mCurrentPage, mCurrentPage == 1);
        String url = StringManager.api_soList + "?type=caidan&s=" + searchKey + "&page=" + mCurrentPage;
        ReqInternet.in().doGet(url, new InternetCallback(mActivity) {
            @Override
            public void loaded(int flag, String url, Object returnObj) {
                int loadPage = 0;
                if (flag >= UtilInternet.REQ_OK_STRING) { // 表示成功
                    if (mCurrentPage == 1) {
                        mListData.clear();
                    }
                    Map<String, String> tempMap;
                    Map<String, String> map = StringManager.getFirstMap(returnObj);
                        if (map != null && map.size() > 0) {
                            if (map.containsKey("caidan") && !map.get("caidan").equals("null")) {
                                ArrayList<Map<String, String>> listReturn = UtilString.getListMapByJson(map.get("caidan"));
                                for (Map<String, String> returnMap : listReturn) {
                                    tempMap = new HashMap<>();
                                    ArrayList<Map<String, String>> imgs = UtilString.getListMapByJson(returnMap.get("imgs"));
                                    for (int index = 0; index < 2; index++) {
                                        if (index < imgs.size()) {
                                            tempMap.put("img" + (index + 1), imgs.get(index).get(""));
                                        } else {
                                            tempMap.put("img" + (index + 1), "hide");
                                        }
                                    }
                                    tempMap.putAll(returnMap);
                                    tempMap.put("allClick", tempMap.get("allClick") + "浏览");
                                    tempMap.put("dishNum", tempMap.get("dishNum") + "道菜");
                                    tempMap.remove("caidan");
                                    mListData.add(tempMap);
                                }
                                loadPage = listReturn.size();
                                if (mAdapter != null) {
                                    mAdapter.notifyDataSetChanged();
                                } else if (mListview != null && (mAdapter = (AdapterSearch) mListview.getAdapter()) != null) {
                                    mAdapter.notifyDataSetChanged();
                                }
                            }
                        }
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
