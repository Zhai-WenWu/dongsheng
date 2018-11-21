package amodule.search.view;

import android.content.Context;
import android.content.Intent;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.annimon.stream.Stream;
import com.xiangha.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import acore.logic.XHClick;
import acore.override.activity.base.BaseActivity;
import acore.tools.ChannelUtil;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import amodule.dish.db.DataOperate;
import amodule.dish.tools.UploadDishSpeechTools;
import amodule.search.avtivity.HomeSearch;
import amodule.search.data.SearchConstant;
import amodule.search.view.DefaultSearchView.DefaultViewCallback;
import amodule.user.activity.BrowseHistory;

/**
 * Created by ：airfly on 2016/10/12 09:40.
 */

public class GlobalSearchView extends LinearLayout implements View.OnClickListener {

    private BaseActivity mActivity;
    private Context context;
    private boolean isBack = false;
    private String searchKey = "";
    private String jsonData = "";
    private boolean once = true;
    private int searchType;
    private MatchWordsView matchwordsView;
    private DefaultSearchView defaultView;
    private List<View> viewList = new ArrayList<>();
    private CaipuSearchResultView caipuView;
    private HaYouSearchResultView hayouView;
    private TieZiSearchResultView tieziView;
    private EditText edSearch;
    private ImageView iv_history;
    private ImageView clear_global;
    private ImageView search_speeach;
    private final String NO_SEARCH_TEXT_HINT = "请输入您想要的";
    private final String DEFAULT_HINT = "搜菜谱、食材等";
    private String currentHint = DEFAULT_HINT;

    public GlobalSearchView(Context context) {
        this(context, null);
    }

    public GlobalSearchView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GlobalSearchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.c_view_search, this, true);
        this.context = context;
    }

    /**
     * 初始化正常搜索
     *
     * @param activity 页面
     * @param searchWord 搜索词
     * @param searchType 搜索类型
     */
    public void init(BaseActivity activity, String searchWord, int searchType) {
        init(activity, searchWord, searchType,true);
    }

    public void init(BaseActivity activity, String searchWord, int searchType,boolean isNowSearch) {
        mActivity = activity;
        this.searchType = searchType;
        initView();
        once = false;
        if (!TextUtils.isEmpty(searchWord)) {
            if(isNowSearch){
                setSearchMsg(searchWord, searchType);
                setHorizon(searchWord);
                search();
            }else{
                this.searchKey = searchWord;
                this.searchType = searchType;
                currentHint = searchWord;
                edSearch.setHint(searchKey);
            }
        } else {
            XHClick.track(defaultView.getContext(), "浏览搜索默认页");
        }
    }

    /**
     * 处理搜索词的匹配参数
     *
     * @param searchWord 匹配搜索词
     */
    private void setHorizon(String searchWord) {
        Map<String, String> map = new HashMap<>();
        map.put("name", searchWord);
        caipuView.handleSearchWord(StringManager.getJsonByMap(map).toString());
    }

    /**
     * 初始化匹配数据
     *
     * @param activity 页面
     * @param jsonData json数据
     */
    public void init(BaseActivity activity, String jsonData) {
        mActivity = activity;
        this.searchType = SearchConstant.SEARCH_CAIPU;
        this.jsonData = jsonData;

        initView();

        String[] searchWords = caipuView.handleSearchWord(jsonData);
        searchKey = searchWords[1];
        edSearch.setText(searchWords[0]);
        search();
    }

    /** 初始化View */
    private void initView() {
        edSearch = findViewById(R.id.ed_search_word);
        clear_global = findViewById(R.id.btn_ed_clear_global);
        search_speeach = findViewById(R.id.a_global_search_speeach);
        iv_history = findViewById(R.id.history);
        UploadDishSpeechTools speechTools = UploadDishSpeechTools.createUploadDishSpeechTools();
        speechTools.initSpeech(mActivity);
        showClearBtn(false);
        initDefaultSearchView();
        initMatchwordsView();
        initCaipuView();
        initHayouView();
        initTieZiView();
        setHintInfo();
        setListener();
        edSearch.requestFocus();
        ToolsDevice.keyboardControl(true, mActivity, edSearch);

    }

    /** 初始化菜谱搜索列表 */
    private void initCaipuView() {
        caipuView = findViewById(R.id.v_result_search);
        caipuView.setVisibility(View.GONE);
        caipuView.init(mActivity);
        viewList.add(caipuView);
    }

    /** 初始化帖子搜索列表 */
    private void initTieZiView() {
        tieziView = findViewById(R.id.v_tiezi_search);
        tieziView.setVisibility(View.GONE);
        tieziView.init(mActivity);
        viewList.add(tieziView);
    }

    /** 初始化哈友搜索列表 */
    private void initHayouView() {
        hayouView = findViewById(R.id.v_hayou_search);
        hayouView.setVisibility(View.GONE);
        hayouView.init(mActivity);
        viewList.add(hayouView);
    }

    /** 初始化匹配词 */
    private void initMatchwordsView() {
        matchwordsView = findViewById(R.id.v_matchwords_search);
        matchwordsView.setVisibility(View.GONE);
        viewList.add(matchwordsView);

        MatchWordsView.MatchWordsCallback matchWordsCallback = (searchKey, searchType) -> {

            if (TextUtils.isEmpty(searchKey.trim()))
                return;

            setSearchMsg(searchKey, searchType);
            search(true);
        };
        matchwordsView.init(mActivity, matchWordsCallback);
    }

    /** 初始化匹配词 */
    private void initDefaultSearchView() {

        defaultView = findViewById(R.id.v_default_search);
        defaultView.setVisibility(View.VISIBLE);
        viewList.add(defaultView);
        DefaultViewCallback defaultViewCallback = new DefaultViewCallback() {
            @Override
            public void disableEditFocus(boolean isClearFocus) {
                clearEditViewFocus(isClearFocus);
            }

            @Override
            public void onSearchMsgChanged(String searchKey, int type) {
                Log.i("tzy", "onSearchMsgChanged: searchKey="+searchKey);
                setSearchMsg(searchKey, type);
            }

            @Override
            public void toSearch(String searchKey, int searchType) {
                Log.i("tzy", "toSearch: searchKey="+searchKey);
                setSearchMsg(searchKey, searchType);
                search(false);
            }

        };

        defaultView.init(mActivity, defaultViewCallback, searchType);
    }

    public void showDefaultSearchView(){
        edSearch.requestFocus();
    }

    private void clearEditViewFocus(boolean isClearFocus) {
        if (isClearFocus) {
//            Log.i("tzy","clearEditViewFocus 执行了");
            edSearch.clearFocus();
            ToolsDevice.keyboardControl(false, mActivity, edSearch);
        }
    }

    // 设置Listener
    private void setListener() {
        findViewById(R.id.btn_ed_clear_global).setOnClickListener(this);
        findViewById(R.id.a_global_search_speeach).setOnClickListener(this);
        findViewById(R.id.btn_search_global).setOnClickListener(this);
        findViewById(R.id.btn_back).setOnClickListener(this);
        // 聚焦,显示搜索历史
        edSearch.setOnFocusChangeListener((v, hasFocus) -> {
            //TODO
            resetEdHint();
            if (hasFocus) {
                if (TextUtils.isEmpty(searchKey)) {
                    showView(defaultView);
                    defaultView.setHistoryVisiable(true);
                    showClearBtn(false);
                } else {
                    XHClick.track(matchwordsView.getContext(), "浏览搜索输入页");
                    showSpeciView(SearchConstant.VIEW_MATCH_WORDS);
                    matchwordsView.getMatchWords(searchKey);
                    showClearBtn(true);
                }
                clearEditViewFocus(false);
                XHClick.mapStat(mActivity, "a_search_input", "点搜索框-总", "");
            } else {
                showClearBtn(false);
            }
        });
        // 控制返回键和回车键
        edSearch.setOnKeyListener((v, keyCode, event) -> {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    if (!mActivity.keyBoard_visible) {
                        if (isBack) {
                            isBack = false;
                            defaultView.setHistoryVisiable(findViewById(R.id.bar_search_bottom).getVisibility() != View.VISIBLE);
                            clearEditViewFocus(true);
                        } else
                            isBack = true;
                        return true;
                    }
                case KeyEvent.KEYCODE_ENTER:
                case KeyEvent.ACTION_DOWN:
                    String str = edSearch.getText().toString();
                    if (str.trim().length() == 0){
                        if(TextUtils.equals(edSearch.getHint(),DEFAULT_HINT)){
                            Tools.showToast(getContext(), NO_SEARCH_TEXT_HINT);
                            mActivity.loadManager.hideProgressBar();
                            return true;
                        }
                    }else{
                        searchKey = str;
                    }
                    setSearchMsg(searchKey, searchType);
                    search();
                    return true;
                default:
                    return false;
            }
        });

        edSearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (edSearch.getText().toString().length() > 0) {
                    if (edSearch.isFocused()) {
                        showClearBtn(true);
                    }
                } else {
                    showClearBtn(false);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(!TextUtils.equals(edSearch.getHint(),DEFAULT_HINT)){
                    resetEdHint();
                }
                String temp = s.toString().trim();
                if (TextUtils.isEmpty(jsonData)) {
                    searchKey = temp;
                } else {
                    jsonData = "";
                }

                caipuView.onClearSearchWord();
                if (TextUtils.isEmpty(temp)) {
                    showSpeciView(SearchConstant.VIEW_DEFAULT_SEARCH);
                } else {
                    showSpeciView(SearchConstant.VIEW_MATCH_WORDS);
                    matchwordsView.getMatchWords(searchKey);
                }
            }
        });

        iv_history.setOnClickListener(v -> {
            XHClick.mapStat(mActivity, HomeSearch.STATISTICS_ID, "浏览记录的按钮点击", "");
            mActivity.startActivity(new Intent(mActivity, BrowseHistory.class));
        });
    }


    private void search() {
        search(false);
    }

    private void search(boolean isMatchWords) {
        search(searchKey, searchType, isMatchWords);
    }

    private void search(String key, int type, boolean isMatchWords) {
        if (TextUtils.isEmpty(key))
            return;

        if ("xiangha".equals(searchKey)) {
            Toast.makeText(context, ChannelUtil.getChannel(context), Toast.LENGTH_SHORT).show();
            return;
        }

        edSearch.clearFocus();
        ToolsDevice.keyboardControl(false, mActivity, edSearch);

        DataOperate.saveSearchWord(key);
        switch (type) {
            case SearchConstant.SEARCH_CAIPU:
                if (once) {
                    once = false;
                } else {
                    setHorizon(key);
                }
                showView(caipuView);
                caipuView.search(key);
                XHClick.mapStat(mActivity, "a_search_input", "搜菜谱", isMatchWords ? "点击联想词" : "直接搜索");
                break;
            case SearchConstant.SEARCH_HAYOU:
                showView(hayouView);
                hayouView.search(key);
                XHClick.mapStat(mActivity, "a_search_input", "搜哈友", "");
                break;
            case SearchConstant.SEARCH_MEISHITIE:
                showView(tieziView);
                tieziView.search(key);
                XHClick.mapStat(mActivity, "a_search_input", "搜贴子", "");
                break;
            default:
                break;
        }
//        Log.i("tzy","搜索");
        if (SearchConstant.SEARCH_CAIPU == searchType) {
            clearEditViewFocus(true);
        }
        XHClick.track(mActivity, "浏览搜索结果页");
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            // 返回
            case R.id.btn_back:
                mActivity.onBackPressed();
                break;
            // 开始搜索
            case R.id.btn_search_global:
                String str = edSearch.getText().toString();
                if (str.trim().length() == 0) {
                    if(TextUtils.equals(edSearch.getHint(),DEFAULT_HINT)){
                        Tools.showToast(getContext(), NO_SEARCH_TEXT_HINT);
                        mActivity.loadManager.hideProgressBar();
                        return;
                    }
                }else{
                    searchKey = str;
                }
                setSearchMsg(searchKey, searchType);
                search();
                break;
            // 清除搜索词
            case R.id.btn_ed_clear_global:
                setSearchMsg("", searchType);
                findViewById(R.id.btn_ed_clear_global).setVisibility(View.GONE);
                clearEditViewFocus(true);
                showSpeciView(SearchConstant.VIEW_DEFAULT_SEARCH);
                XHClick.track(defaultView.getContext(), "浏览搜索默认页");
                break;
            //语音输入
            case R.id.a_global_search_speeach:
                XHClick.mapStat(mActivity, "a_search_input", "点语音搜索-总", "");
                UploadDishSpeechTools.createUploadDishSpeechTools().startSpeech(edSearch);
                break;
            default:
                break;
        }
    }


    private void showSpeciView(int viewFlag) {

        View tempView = null;
        switch (viewFlag) {
            case SearchConstant.VIEW_DEFAULT_SEARCH:
                tempView = defaultView;
                break;
            case SearchConstant.VIEW_MATCH_WORDS:
                tempView = matchwordsView;
                break;
            case SearchConstant.VIEW_CAIPU_RESULT:
                tempView = caipuView;
                break;
            case SearchConstant.VIEW_HAYOU_RESULT:
                tempView = hayouView;
                break;
            case SearchConstant.VIEW_TIEZI_RESULT:
                tempView = tieziView;
                break;
            default:
                break;
        }
        if (tempView != null) {
            showView(tempView);
        }
    }

    private void setHintInfo() {
        if (SearchConstant.SEARCH_HAYOU == searchType) {
            edSearch.setHint("搜哈友");
        } else if (SearchConstant.SEARCH_MEISHITIE == searchType) {
            edSearch.setHint("搜贴子");
        }
    }

    private void showClearBtn(boolean isShow) {
        clear_global.setVisibility(isShow ? View.VISIBLE : GONE);
        search_speeach.setVisibility(isShow ? View.GONE : VISIBLE);
    }

    private void showView(View view) {

        view.setVisibility(VISIBLE);
        Stream.of(viewList).filter(value -> view != value)
                .forEach(value -> value.setVisibility(GONE));
        iv_history.setVisibility((view == caipuView || view == tieziView) ? VISIBLE : INVISIBLE);

        if (view == defaultView) {
            defaultView.refresh();
        }
    }

    private void setSearchMsg(String key, int type) {
        searchKey = key;
        searchType = type;
        edSearch.setText(key);
    }

    private void resetEdHint() {
        edSearch.setHint(DEFAULT_HINT);
    }

}
