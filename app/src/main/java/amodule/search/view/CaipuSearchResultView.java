package amodule.search.view;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import acore.logic.load.LoadManager;
import acore.override.activity.base.BaseActivity;
import acore.tools.StringManager;
import amodule.search.adapter.AdapterCaipuSearch;
import amodule.search.data.SearchDataImp;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;
import cn.srain.cube.views.ptr.PtrClassicFrameLayout;
import xh.basic.internet.UtilInternet;
import xh.basic.tool.UtilString;

import static com.xiangha.R.id.v_no_data_search;

/**
 * Created by ：airfly on 2016/10/14 15:21.
 */

public class CaipuSearchResultView extends LinearLayout {

    private AtomicInteger actIn;
    private Context context;
    private BaseActivity mActivity;
    private LinearLayout ll_noData;
    private String searchKey;


    private int currentCaipuPage;
    private int currentCaiDanPage;
    private int currentZhishiPage;
    private LoadManager loadManager;
    private CopyOnWriteArrayList<Map<String, String>> mListCaipuData = new CopyOnWriteArrayList<>();
    private CopyOnWriteArrayList<Map<String, String>> mListShicaiData = new CopyOnWriteArrayList<>();
    private CopyOnWriteArrayList<Map<String, String>> mListCaidanData = new CopyOnWriteArrayList<>();
    private CopyOnWriteArrayList<Map<String, String>> mListZhishiData = new CopyOnWriteArrayList<>();
    private PtrClassicFrameLayout refresh_list_view_frame;
    private ListView list_search_result;
    private boolean isFirstPage;
    private CaidanResultView caidan_result;
    private AdapterCaipuSearch adapterCaipuSearch;
    private ZhishiResultView zhishi_result;
    private SearchVIPLessonView mLessonView;
    private AdapterCaipuSearch.CaipuSearchResultCallback caipuSearchResultCallback;
    private int firstCaipuLoadFlag;
    private View mParentView;
    private int adNum;
    private AtomicBoolean isRefreash = new AtomicBoolean(false);

    private boolean mLessonDataReady = false;
    private boolean mDishDataReady = false;
    private String mLessonCode;
    private Map<String, String> mDishStrMap;

    public CaipuSearchResultView(Context context) {
        this(context, null);
    }

    public CaipuSearchResultView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CaipuSearchResultView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.c_view_compose_search, this, true);
        this.context = context;
    }


    public void init(BaseActivity activity, View parentView) {
        mActivity = activity;
        mParentView = parentView;
        initData();
        initView();
    }


    private void initView() {

        refresh_list_view_frame = (PtrClassicFrameLayout) findViewById(R.id.refresh_list_view_frame);
        refresh_list_view_frame.setVisibility(View.VISIBLE);
        list_search_result = (ListView) findViewById(R.id.list_search_result);
        ll_noData = (LinearLayout) findViewById(v_no_data_search);
        ll_noData.setVisibility(View.GONE);
        list_search_result.setDivider(null);
        mLessonView = new SearchVIPLessonView(context);
        RelativeLayout header = new RelativeLayout(context);
        header.addView(mLessonView);
        list_search_result.addHeaderView(header);

        initCaidanResultView();
        initZhishiResultView();

        createCallback();
        adapterCaipuSearch = new AdapterCaipuSearch(mActivity, list_search_result, caipuSearchResultCallback);
    }


    private void initData() {
        loadManager = mActivity.loadManager;
        actIn = new AtomicInteger(3);
    }


    private void initCaidanResultView() {
        caidan_result = (CaidanResultView) findViewById(R.id.caidan_result);
        caidan_result.init(mActivity, mParentView);
        caidan_result.setVisibility(View.GONE);
    }

    private void initZhishiResultView() {
        zhishi_result = (ZhishiResultView) findViewById(R.id.zhishi_result);
        zhishi_result.init(mActivity, mParentView);
        zhishi_result.setVisibility(View.GONE);
    }


    private void createCallback() {
        caipuSearchResultCallback = new AdapterCaipuSearch.CaipuSearchResultCallback() {
            @Override
            public void searchMoreZhishi() {

                zhishi_result.search(searchKey);
                zhishi_result.setVisibility(View.VISIBLE);
                refresh_list_view_frame.setVisibility(View.INVISIBLE);
            }

            @Override
            public void searchMoreCaidan() {

                caidan_result.search(searchKey);
                caidan_result.setVisibility(View.VISIBLE);
                refresh_list_view_frame.setVisibility(View.INVISIBLE);
            }
        };
    }

    public void showCaipuSearchResultView() {
        if (refresh_list_view_frame != null) {
            refresh_list_view_frame.setVisibility(View.VISIBLE);
        }
    }


    public void search(String key) {
        adapterCaipuSearch.refreshAdData();
        clearSearchResult();
        searchKey = key;
        loadManager.setLoading(refresh_list_view_frame, list_search_result, adapterCaipuSearch,
                true, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        adapterCaipuSearch.refreshAdData();

                        clearSearchResult();
                        isRefreash.set(true);
                        searchVIPLesson();
                        searchCaipu();
                        searchCaiDan();
                        searchZhiShi();
                    }
                }, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        isRefreash.set(false);
                        searchCaipu();
                    }

                });
        searchVIPLesson();
        searchCaiDan();
        searchZhiShi();
    }

    public void onClearcSearchWord() {

        clearSearchResult();
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                adapterCaipuSearch.refresh(false, mListCaipuData, mListShicaiData, mListCaidanData, mListZhishiData);
                adNum = 0;
            }
        });
    }


    private void clearSearchResult() {
        mDishDataReady = false;
        mLessonDataReady = false;
        mDishStrMap = null;
        mLessonCode = "";
        mListCaipuData.clear();
        mListShicaiData.clear();
        mListCaidanData.clear();
        mListZhishiData.clear();
        currentCaipuPage = 0;
        currentCaiDanPage = 0;
        currentZhishiPage = 0;
        actIn = new AtomicInteger(3);
        ll_noData.setVisibility(View.GONE);
        adapterCaipuSearch.clearAdList();
        adapterCaipuSearch.notifyDataSetChanged();
    }

    private void searchVIPLesson() {
        mLessonView.searchLesson(searchKey, new SearchVIPLessonView.LessonCallback() {
            @Override
            public void callback(String code) {
                mLessonCode = code;
                mLessonDataReady = true;
                onLessonAndDishDataReady();
            }
        });
    }


    private void searchCaipu() {
        Log.e("TAG", "searchCaipu: -----------");

        currentCaipuPage++;
        isFirstPage = currentCaipuPage == 1;

        loadManager.changeMoreBtn(list_search_result, ReqInternet.REQ_OK_STRING, -1, -1, currentCaipuPage, isFirstPage);
        if (isRefreash.get()) {
            loadManager.hideProgressBar();
            isRefreash.set(false);
        }
        new SearchDataImp().getCaipuAndShicaiResult(context, searchKey, currentCaipuPage, new InternetCallback() {
            @Override
            public void loaded(int flag, String url, Object returnObj) {
                mDishDataReady = true;
                if (flag >= ReqInternet.REQ_OK_STRING) {
                    if (currentCaipuPage == 1) {
                        mListCaipuData.clear();
                        mListShicaiData.clear();
                    }
                    mDishStrMap = StringManager.getFirstMap(returnObj);
                    onLessonAndDishDataReady();
                } else {
                }

                if (isFirstPage) {
                    firstCaipuLoadFlag = flag;
                    onDownFirstPageComplete();
                }
            }
        });

    }

    private void onLessonAndDishDataReady() {
        if (mLessonDataReady && mDishDataReady) {
            if (mDishStrMap != null && mDishStrMap.containsKey("dishs")) {
                String caipuStr = mDishStrMap.get("dishs");
                ArrayList<Map<String, String>> tempList = StringManager.getListMapByJson(caipuStr);
                int invalideIndex = -1;
                for (int k = 0; k < tempList.size(); k++) {
                    Map<String, String> map1 = tempList.get(k);
                    if (map1.containsKey("customers")) {
                        Map<String, String> customer = StringManager.getFirstMap(map1.get("customers"));
                        map1.put("cusNickName", customer.get("nickName"));
                        map1.put("cusImg", customer.get("img"));
                        map1.put("cusCode", customer.get("code"));
                        map1.remove("customers");
                    }
                    map1.put("allClick", map1.get("allClick") + "浏览");
                    map1.put("favorites", map1.get("favorites") + "收藏");
                    if (isFirstPage && TextUtils.equals(mLessonCode, map1.get("code")) && invalideIndex == -1) {
                        invalideIndex = k;
                    }
                }
                if (invalideIndex != -1) {
                    tempList.remove(invalideIndex);
                }
                mListCaipuData.addAll(tempList);

                if (isFirstPage
                        && mDishStrMap != null
                        && mDishStrMap.containsKey("theIngre")) {
                    String shicaiStr = mDishStrMap.get("theIngre");
                    ArrayList<Map<String, String>> tempList2 = StringManager.getListMapByJson(shicaiStr);
                    for (Map<String, String> map2 : tempList2) {
                        map2.put("allClick", map2.get("allClick") + "浏览");
                        map2.put("name", map2.get("name") + "百科");
                    }
                    mListShicaiData.addAll(tempList2);
                }

                if (!isFirstPage) {
                    int loadCount = mListCaidanData.size() + mListCaipuData.size() + mListShicaiData.size() + mListZhishiData.size() + adNum;
                    if (adapterCaipuSearch == null) {
                        adapterCaipuSearch = (AdapterCaipuSearch) list_search_result.getAdapter();
                    }
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            adNum = adapterCaipuSearch.refresh(isFirstPage, mListCaipuData, mListShicaiData, mListCaidanData, mListZhishiData);
                        }
                    });
                    currentCaipuPage = loadManager.changeMoreBtn(list_search_result, ReqInternet.REQ_OK_STRING, LoadManager.FOOTTIME_PAGE, loadCount, currentCaipuPage, isFirstPage);
                }
            } else {
                if (!isFirstPage) {
                    setLoadMoreBtn();
                }
            }

        }
    }

    private void searchCaiDan() {

        currentCaiDanPage++;
        new SearchDataImp().getCaidanResult(context, searchKey, currentCaiDanPage, new InternetCallback() {
            @Override
            public void loaded(int flag, String url, Object returnObj) {
                if (flag >= UtilInternet.REQ_OK_STRING) { // 表示成功
                    if (currentCaiDanPage == 1) {
                        mListCaidanData.clear();
                    }
                    ArrayList<Map<String, String>> listReturn = UtilString.getListMapByJson(returnObj);
                    HashMap<String, String> tempMap = new HashMap<>();
                    if (listReturn != null && listReturn.size() > 0) {
                        Map<String, String> map = listReturn.get(0);
                        if (map != null && map.size() > 0) {
                            tempMap.putAll(map);
                            if (map.containsKey("caidan") && !map.get("caidan").equals("null")) {
                                listReturn = UtilString.getListMapByJson(map.get("caidan"));
                                for (Map<String, String> returnMap : listReturn) {
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
                                    mListCaidanData.add(tempMap);
                                }
                            }

                        }

                    }
                }
                if (currentCaiDanPage == 1) {
                    onDownFirstPageComplete();
                }
            }
        });
    }

    private void searchZhiShi() {

        currentZhishiPage++;
        new SearchDataImp().getZhishiResult(context, searchKey, currentZhishiPage, new InternetCallback() {
            @Override
            public void loaded(int flag, String url, Object returnObj) {
                if (flag >= UtilInternet.REQ_OK_STRING) {
                    if (currentZhishiPage == 1) {
                        mListZhishiData.clear();
                    }
                    // 解析数据
                    ArrayList<Map<String, String>> list = UtilString.getListMapByJson(returnObj);
                    if (list != null && list.size() > 0) {
                        Map<String, String> map = list.get(0);
                        HashMap<String, String> tempMap = new HashMap<String, String>();
                        if (map != null && map.size() > 0) {
                            tempMap.putAll(map);
                        }

                        ArrayList<Map<String, String>> list3 = UtilString.getListMapByJson(map.get("nous"));
                        for (int i = 0; i < list3.size(); i++) {
                            tempMap.put("img", list3.get(i).get("img"));
                            tempMap.put("title", list3.get(i).get("title"));
                            tempMap.put("content", list3.get(i).get("content"));
                            tempMap.put("allClick", list3.get(i).get("allClick") + "浏览");
                            tempMap.put("code", list3.get(i).get("code"));
                            tempMap.put("classifyName", list3.get(i).get("classifyName"));
                            tempMap.remove("nous");
                            mListZhishiData.add(tempMap);
                        }

                    }
                }
                if (currentZhishiPage == 1) {
                    onDownFirstPageComplete();
                }

            }
        });
    }


    private void onDownFirstPageComplete() {
        if (actIn.decrementAndGet() != 0)
            return;

        loadManager.hideProgressBar();
        loadManager.hideLoadFaildBar();
        if (mListCaipuData.size() == 0
                && mListShicaiData.size() == 0
                && mListCaidanData.size() == 0
                && mListZhishiData.size() == 0) {
            ll_noData.setVisibility(View.VISIBLE);
            return;
        } else {
            ll_noData.setVisibility(View.GONE);
        }

        if (adapterCaipuSearch == null) {
            adapterCaipuSearch = (AdapterCaipuSearch) list_search_result.getAdapter();
        }

        adapterCaipuSearch.clearAdList();
        adNum = 0;
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                adNum = adapterCaipuSearch.refresh(true, mListCaipuData, mListShicaiData, mListCaidanData, mListZhishiData);
            }
        });
        currentCaipuPage = loadManager.changeMoreBtn(list_search_result, firstCaipuLoadFlag, LoadManager.FOOTTIME_PAGE,
                mListCaidanData.size() + mListCaipuData.size() + mListShicaiData.size() + mListZhishiData.size() + adNum, currentCaipuPage, true);
        if (mListCaipuData.size() < 1)
            setLoadMoreBtn();
        refresh_list_view_frame.refreshComplete();
    }


    private void setLoadMoreBtn() {
        Button moreBtn = loadManager.getSingleLoadMore(list_search_result);
        moreBtn.setEnabled(false);
        moreBtn.setText("- 学名厨做菜, 用香哈 -");
    }


}
