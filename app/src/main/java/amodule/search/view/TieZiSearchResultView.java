package amodule.search.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import acore.logic.load.LoadManager;
import acore.override.activity.base.BaseActivity;
import acore.tools.StringManager;
import amodule.search.adapter.ComposeSearchAdapter;
import amodule.search.data.SearchDataImp;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;
import xh.basic.internet.UtilInternet;

/**
 * Created by ：airfly on 2016/10/17 20:33.
 */

public class TieZiSearchResultView extends RelativeLayout {

    private Context context;
    private BaseActivity mActivity;
    private ListView mListview;
    private ImageView returnTop;
    private String searchKey;
    private int mCurrentPage = 0, everyPage = 0;

    /**
     * 贴子的数据集合
     */
    private ArrayList<Map<String, String>> mListData = new ArrayList<>();
    private ComposeSearchAdapter mAdapter;
    private LoadManager loadManager;
    private AtomicBoolean isSearching;
    private LinearLayout ll_noData;


    public TieZiSearchResultView(Context context) {
        this(context, null);
    }

    public TieZiSearchResultView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TieZiSearchResultView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.c_view_tiezi_search, this, true);
        this.context = context;
    }

    public void init(BaseActivity activity) {
        mActivity = activity;
        loadManager = mActivity.loadManager;
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

        mAdapter = new ComposeSearchAdapter(mActivity, mListview, mListData);
    }


    public void search(String key) {

        if (TextUtils.isEmpty(key))
            return;
        searchKey = key;
        mCurrentPage = 0;
        mListData.clear();
        mAdapter.notifyDataSetChanged();
        loadManager.setLoading(mListview, mAdapter, true, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getData();
            }
        });
    }

    private void getData() {

        if (isSearching.get())
            return;
        isSearching.set(true);

        mCurrentPage++;
        //更新加载按钮状态
        loadManager.changeMoreBtn(mListview, ReqInternet.REQ_OK_STRING, -1, -1, mCurrentPage, mCurrentPage == 1);
        new SearchDataImp().getTieziResult(context, searchKey, mCurrentPage, new InternetCallback(context) {
            @Override
            public void loaded(int flag, String url, Object msg) {
                int loadPage = 0;
                if (flag >= ReqInternet.REQ_OK_STRING) {
                    if (mCurrentPage == 1) {
                        mListData.clear();
                    }
                    List<Map<String, String>> returnData = StringManager.getListMapByJson(msg);

                    for (int i = 0; i < returnData.size(); i++) {
                        Map<String, String> map = returnData.get(i);
                        if (map != null && map.containsKey("subjects")) {
                            String tieziStr = map.get("subjects");
                            ArrayList<Map<String, String>> tempList = StringManager.getListMapByJson(tieziStr);
                            mListData.addAll(tempList);
                            loadPage = tempList.size();

                            if (mAdapter != null) {
                                mAdapter.notifyDataSetChanged();
                            } else if (mListview != null && (mAdapter = (ComposeSearchAdapter) mListview.getAdapter()) != null) {
                                mAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                }

                //如果没有数据显示提示
                if (flag >= UtilInternet.REQ_OK_STRING && mListData.size() == 0) {
                    returnTop.setVisibility(View.GONE);
                    ll_noData.setVisibility(View.VISIBLE);
                    loadManager.hideLoadFaildBar();
                } else {
                    returnTop.setVisibility(View.VISIBLE);
                    ll_noData.setVisibility(View.GONE);
                }
                isSearching.set(false);
                everyPage = everyPage == 0 ? loadPage : everyPage;
                mCurrentPage = loadManager.changeMoreBtn(mListview, flag, everyPage, loadPage, mCurrentPage, mListData.size()==0);
            }
        });
    }


    private void returnListTop() {
        if (mListview != null) {
            mListview.setSelection(0);
        }
    }


}
