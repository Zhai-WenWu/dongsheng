package amodule.search.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import acore.logic.load.LoadManager;
import acore.override.activity.base.BaseActivity;
import acore.tools.StringManager;
import acore.tools.Tools;
import amodule.search.adapter.AdapterCaipuSearch;
import amodule.search.adapter.SearchHorizonAdapter;
import amodule.search.avtivity.HomeSearch;
import amodule.search.data.SearchDataImp;
import aplug.basic.InternetCallback;
import cn.srain.cube.views.ptr.PtrClassicFrameLayout;

import static amodule.search.avtivity.HomeSearch.EXTRA_JSONDATA;
import static com.xiangha.R.id.v_no_data_search;
import static xh.basic.internet.UtilInternet.REQ_OK_STRING;

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
    private LoadManager loadManager;
    private CopyOnWriteArrayList<Map<String, String>> mListCaipuData = new CopyOnWriteArrayList<>();
    private CopyOnWriteArrayList<Map<String, String>> mListShicaiData = new CopyOnWriteArrayList<>();
    private PtrClassicFrameLayout refresh_list_view_frame;
    private ListView list_search_result;
    private AdapterCaipuSearch adapterCaipuSearch;
    private SearchVIPLessonView mLessonView;
    private SearchHorizonLayout mSearchHorizonLayout;
    private int adNum;
    private AtomicBoolean isRefreash = new AtomicBoolean(false);

    private String mLessonCode;
//    private Map<String, String> mDishStrMap;

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
        initData();
        initView();
    }

    private void initView() {
        mSearchHorizonLayout = findViewById(R.id.search_horizon_layout);
        refresh_list_view_frame = findViewById(R.id.refresh_list_view_frame);
        refresh_list_view_frame.setVisibility(View.VISIBLE);
        list_search_result = findViewById(R.id.list_search_result);
        ll_noData = findViewById(v_no_data_search);
        ll_noData.setVisibility(View.GONE);
        list_search_result.setVisibility(INVISIBLE);
        list_search_result.setDivider(null);
        mLessonView = new SearchVIPLessonView(context);
        View view = new View(getContext());
        view.setBackgroundColor(Color.WHITE);
        view.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT,Tools.getDimen(getContext(),R.dimen.dp_10)));
        list_search_result.addHeaderView(view);
        RelativeLayout header = new RelativeLayout(context);
        header.addView(mLessonView);
        list_search_result.addHeaderView(header);

        adapterCaipuSearch = new AdapterCaipuSearch(mActivity, list_search_result);
    }

    private void initData() {
        loadManager = mActivity.loadManager;
        actIn = new AtomicInteger(2);
    }

    public void search(String key) {
        adapterCaipuSearch.refreshAdData();
        clearSearchResult();
        searchKey = key;
        adapterCaipuSearch.setSearchKey(searchKey);
        loadManager.setLoading(refresh_list_view_frame, list_search_result, adapterCaipuSearch, true,
                v -> {
                    adapterCaipuSearch.refreshAdData();

                    clearSearchResult();
                    isRefreash.set(true);
                    searchVIPLesson();
                    searchCaipu();
                },
                v -> {
                    isRefreash.set(false);
                    searchCaipu();
                });
        searchVIPLesson();
    }

    public void onClearcSearchWord() {

        clearSearchResult();
        new Handler(Looper.getMainLooper()).post(() -> {
            adapterCaipuSearch.refresh(false, mListCaipuData, mListShicaiData);
            adNum = 0;
        });
    }

    /**
     *
     * @param jsonData json数据
     * @return 返回数组，0是显示的搜索文字，1是真是的搜索词
     */
    public String[] handleSearchWord(String jsonData){
        String[] searchWords = {"",""};
        List<Map<String,String>> strList = StringManager.getListMapByJson(jsonData);
        mSearchHorizonLayout.setWordList(strList);
        StringBuffer sb = new StringBuffer();
        for(int i=0;i<strList.size();i++){
            Map<String,String> map = strList.get(i);
            String word = map.get("name");
            if(TextUtils.isEmpty(word)){
                continue;
            }
            sb.append(map.get("name"));
            if(i!=strList.size() - 1){
                sb.append(" ");
            }
        }
        searchWords[0] = sb.toString();
        String word1="",word2="";
        for(int i=0;i<strList.size();i++){
            Map<String,String> map = strList.get(i);
            String type = map.get("type");
            String word = map.get("name");
            if(TextUtils.isEmpty(word)){
                continue;
            }
            if(TextUtils.equals("3",type)){
                searchWords[1] = word;
                return searchWords;
            }else if(TextUtils.isEmpty(type) || TextUtils.equals("2",type)){
                word1 = word;
            }else if(TextUtils.equals("1",type)){
                word2 = word;
            }
        }
        searchWords[1] = word1 + " " + word2;
        return searchWords;
    }


    private void clearSearchResult() {
        mLessonCode = "";
        mListCaipuData.clear();
        mListShicaiData.clear();
        currentCaipuPage = 0;
        actIn = new AtomicInteger(2);
        ll_noData.setVisibility(View.GONE);
        adapterCaipuSearch.clearAdList();
        adapterCaipuSearch.notifyDataSetChanged();
    }

    private void searchVIPLesson() {
        mLessonView.searchLesson(searchKey, code -> {
            mLessonCode = code;
            removeLessonDish();
            onDownFirstPageComplete();
        });
    }


    private void searchCaipu() {
        Log.e("TAG", "searchCaipu: -----------");

        currentCaipuPage++;

        loadManager.loading(list_search_result, currentCaipuPage == 1);
        if (isRefreash.get()) {
            loadManager.hideProgressBar();
            isRefreash.set(false);
        }
        new SearchDataImp().getCaipuAndShicaiResult(context, searchKey, currentCaipuPage, new InternetCallback() {
            final boolean currentIsRefresh = currentCaipuPage == 1;
            @Override
            public void loaded(int flag, String url, Object returnObj) {
                if (flag >= REQ_OK_STRING) {
                    if (currentCaipuPage == 1) {
                        mListCaipuData.clear();
                        mListShicaiData.clear();
                    }
                    readyDishData(currentIsRefresh, StringManager.getFirstMap(returnObj));
                    if (currentIsRefresh) {
                        onDownFirstPageComplete();
                    }
                }
            }
        });
    }

    private void removeLessonDish(){
        if(TextUtils.isEmpty(mLessonCode)){
            return;
        }
        for(int i=0;i<mListCaipuData.size();i++){
            if(TextUtils.equals(mLessonCode,mListCaipuData.get(i).get("code"))){
                mListCaipuData.remove(i);
                return;
            }
        }
    }

    private void readyDishData(boolean isRefresh, Map<String, String> dishMap) {
        if (dishMap != null && dishMap.containsKey("dishs")) {
            String caipuStr = dishMap.get("dishs");
            ArrayList<Map<String, String>> tempList = StringManager.getListMapByJson(caipuStr);
            for (int k = 0; k < tempList.size(); k++) {
                Map<String, String> map1 = tempList.get(k);
                if (map1.containsKey("customers")) {
                    Map<String, String> customer = StringManager.getFirstMap(map1.get("customers"));
                    String nickName = customer.get("nickName");
                    if (!TextUtils.isEmpty(nickName) && nickName.length() >= 8) {
                        nickName = nickName.substring(0, 7) + "...";
                    }
                    map1.put("cusNickName", nickName);
                    map1.put("cusImg", customer.get("img"));
                    map1.put("cusCode", customer.get("code"));
                    map1.remove("customers");
                }
                if (map1.containsKey("video")) {
                    Map<String, String> video = StringManager.getFirstMap(map1.get("video"));
                    if (!video.isEmpty() && video.containsKey("duration")) {
                        map1.put("duration", video.get("duration"));
                    }
                }
                map1.put("allClick", map1.get("allClick") + "浏览");
                map1.put("favorites", map1.get("favorites") + "收藏");
            }
            mListCaipuData.addAll(tempList);
            removeLessonDish();
            if (isRefresh
                    && dishMap != null
                    && dishMap.containsKey("theIngre")) {
                String shicaiStr = dishMap.get("theIngre");
                ArrayList<Map<String, String>> tempList2 = StringManager.getListMapByJson(shicaiStr);
                for (Map<String, String> map2 : tempList2) {
                    map2.put("name", map2.get("name"));
                }
                mListShicaiData.addAll(tempList2);
            }

            if (!isRefresh) {
                int loadCount = mListCaipuData.size() + mListShicaiData.size() + adNum;
                if (adapterCaipuSearch == null) {
                    adapterCaipuSearch = (AdapterCaipuSearch) list_search_result.getAdapter();
                }
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        adNum = adapterCaipuSearch.refresh(isRefresh, mListCaipuData, mListShicaiData);
                    }
                });
                loadManager.loadOver(REQ_OK_STRING, list_search_result, loadCount);
            }
        } else {
            if (!isRefresh) {
                setLoadMoreBtn();
            }
        }
    }

    private void onDownFirstPageComplete() {
        if (actIn.decrementAndGet() != 0)
            return;
        loadManager.hideProgressBar();
        loadManager.hideLoadFaildBar();
        if (mListCaipuData.size() == 0
                && mListShicaiData.size() == 0) {
            ll_noData.setVisibility(View.VISIBLE);
            list_search_result.setVisibility(INVISIBLE);
            return;
        } else {
            ll_noData.setVisibility(View.GONE);
            list_search_result.setVisibility(VISIBLE);
        }

        if (adapterCaipuSearch == null) {
            adapterCaipuSearch = (AdapterCaipuSearch) list_search_result.getAdapter();
        }

        adapterCaipuSearch.clearAdList();
        adNum = 0;
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                adNum = adapterCaipuSearch.refresh(true, mListCaipuData, mListShicaiData);
            }
        });
        loadManager.loadOver(50,list_search_result, 1);
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
