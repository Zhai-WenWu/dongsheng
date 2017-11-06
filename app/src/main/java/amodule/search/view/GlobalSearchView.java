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

import com.xiangha.R;

import java.util.ArrayList;
import java.util.List;

import acore.logic.XHClick;
import acore.override.activity.base.BaseActivity;
import acore.tools.ChannelUtil;
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
    private int searchType;
    private MatchWordsView matchwordsView;
    private DefaultSearchView defaultView;
    private CaipuSearchResultView caipuView;
    private List<View> viewList = new ArrayList<View>();
    private HaYouSearchResultView hayouView;
    private TieZiSearchResultView tieziView;
    private View secondLevelView;
    private EditText edSearch;
    private ImageView iv_history;
    private ImageView clear_global;
    private ImageView search_speeach;


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

    public void init(BaseActivity activity, String searchWord, int searchType) {
        mActivity = activity;
        this.searchType = searchType;
        initView();

        if (!TextUtils.isEmpty(searchWord)) {
            setSearchMsg(searchWord, searchType);
            search();
        }else {
            XHClick.track(defaultView.getContext(), "浏览搜索默认页");
        }
    }

    /**
     * 初始化View
     */
    private void initView() {
        edSearch = (EditText) findViewById(R.id.ed_search_word);
        clear_global = (ImageView) findViewById(R.id.btn_ed_clear_global);
         search_speeach = (ImageView) findViewById(R.id.a_global_search_speeach);
        iv_history = (ImageView) findViewById(R.id.history);
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
        showSpeciView(SearchConstant.VIEW_DEFAULT_SEARCH);
        edSearch.requestFocus();
        ToolsDevice.keyboardControl(true,mActivity,edSearch);

    }

    private void initHayouView() {
        hayouView = (HaYouSearchResultView) findViewById(R.id.v_hayou_search);
        hayouView.setVisibility(View.GONE);
        hayouView.init(mActivity);
        viewList.add(hayouView);
    }

    private void initTieZiView() {
        tieziView = (TieZiSearchResultView) findViewById(R.id.v_tiezi_search);
        tieziView.setVisibility(View.GONE);
        tieziView.init(mActivity);
        viewList.add(tieziView);
    }

    private void initCaipuView() {

        caipuView = (CaipuSearchResultView) findViewById(R.id.v_result_search);
        caipuView.setVisibility(View.GONE);
        caipuView.init(mActivity, this);
        viewList.add(caipuView);
    }

    private void initMatchwordsView() {

        matchwordsView = (MatchWordsView) findViewById(R.id.v_matchwords_search);
        matchwordsView.setVisibility(View.GONE);
        viewList.add(matchwordsView);

        MatchWordsView.MatchWordsCallback matchWordsCallback = new MatchWordsView.MatchWordsCallback() {
            @Override
            public void onItemClick(String searchKey, int searchType) {

                if (TextUtils.isEmpty(searchKey.trim()))
                    return;

                setSearchMsg(searchKey, searchType);
                search(true);
            }
        };
        matchwordsView.init(mActivity, matchWordsCallback);
    }


    private void initDefaultSearchView() {

        defaultView = (DefaultSearchView) findViewById(R.id.v_default_search);
        defaultView.setVisibility(View.VISIBLE);
        viewList.add(defaultView);
        DefaultViewCallback defaultViewCallback = new DefaultViewCallback() {
            @Override
            public void disableEditFocus(boolean isClearFocus) {
                clearEditViewFocus(isClearFocus);
            }

            @Override
            public void onSearchMsgChanged(String searchKey, int type) {
                setSearchMsg(searchKey, type);
            }

            @Override
            public void toSearch(String searchKey, int searchType) {
                setSearchMsg(searchKey, searchType);
                search(false);
            }

        };

        defaultView.init(mActivity, defaultViewCallback, searchType);
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
        edSearch.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if ( secondLevelView != null) {
                    setHintInfo();
                    showClearBtn(hasFocus && !TextUtils.isEmpty(searchKey));
                    return;
                }
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
            }
        });
        // 控制返回键和回车键
        edSearch.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_BACK:
                        if (!mActivity.keyBoard_visible) {
                            if (isBack) {
                                isBack = false;
//                                ((HomeSearch) mActivity).scrollLayout.allow = true;
                                defaultView.setHistoryVisiable(findViewById(R.id.bar_search_bottom).getVisibility() != View.VISIBLE);
                                clearEditViewFocus(true);
                            } else
                                isBack = true;
                            return true;
                        }
                    case KeyEvent.KEYCODE_ENTER:
                    case KeyEvent.ACTION_DOWN:
                        String str = edSearch.getText().toString();
                        if (str.trim().length() == 0) {
                            Tools.showToast(getContext(), "请输入查询关键字");
                            mActivity.loadManager.hideProgressBar();
                            return true;
                        }
                        searchKey = str;
                        search();
                        return true;
                    default:
                        return false;
                }
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

                String temp = s.toString().trim();

                if (secondLevelView != null) {
                    secondLevelSearch(secondLevelView, temp);
                    return;
                }
                    caipuView.onClearcSearchWord();
                if (TextUtils.isEmpty(temp)) {
                    showSpeciView(SearchConstant.VIEW_DEFAULT_SEARCH);
                } else {
                    searchKey = temp;
//                    if (SearchConstant.SEARCH_CAIPU != searchType) {
//                        search();
//                    } else {
                        showSpeciView(SearchConstant.VIEW_MATCH_WORDS);
                        matchwordsView.getMatchWords(searchKey);
//                        XHClick.track(matchwordsView.getContext(), "浏览搜索输入页");
//                    }
                }
            }
        });

        iv_history.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                XHClick.mapStat(mActivity, HomeSearch.STATISTICS_ID, "浏览记录的按钮点击", "");
                mActivity.startActivity(new Intent(mActivity, BrowseHistory.class));
            }
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

        if("xiangha".equals(searchKey)){
            Toast.makeText(context, ChannelUtil.getChannel(context),Toast.LENGTH_SHORT).show();
            return;
        }

        edSearch.clearFocus();
        ToolsDevice.keyboardControl(false,mActivity,edSearch);

        DataOperate.saveSearchWord(key);
        switch (type) {
            case SearchConstant.SEARCH_CAIPU:
                showView(caipuView);

                caipuView.search(key);
                if (isMatchWords) {
                    XHClick.mapStat(mActivity, "a_search_input", "搜菜谱", "点击联想词");
                } else {
                    XHClick.mapStat(mActivity, "a_search_input", "搜菜谱", "直接搜索");
                }
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
                    Tools.showToast(mActivity, "请输入查询关键字");
                    mActivity.loadManager.hideProgressBar();
                    return;
                }
                setSearchMsg(str, searchType);
                search();
                break;
            // 清除搜索词
            case R.id.btn_ed_clear_global:
                setSearchMsg("", searchType);
                findViewById(R.id.btn_ed_clear_global).setVisibility(View.GONE);
                clearEditViewFocus(true);
                if (secondLevelView != null) {
                    return;
                } else {
                    showSpeciView(SearchConstant.VIEW_DEFAULT_SEARCH);
                    XHClick.track(defaultView.getContext(), "浏览搜索默认页");
                }
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
        hideSecondLevelView();
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

    private void secondLevelSearch(View secondLevelView, String key) {
        if (!TextUtils.isEmpty(key)) {
            if (secondLevelView instanceof CaidanResultView) {
                CaidanResultView caidan_result = (CaidanResultView) secondLevelView;
                caidan_result.search(key);
            } else if (secondLevelView instanceof ZhishiResultView) {
                ZhishiResultView zhishi_result = (ZhishiResultView) secondLevelView;
                zhishi_result.search(key);
            }
        }

    }

    private void setHintInfo() {
        if (SearchConstant.SEARCH_HAYOU == searchType) {
            edSearch.setHint("搜哈友");
        } else if (SearchConstant.SEARCH_MEISHITIE == searchType) {
            edSearch.setHint("搜贴子");
        } else if (secondLevelView instanceof CaidanResultView) {
            edSearch.setHint("搜菜单");
        } else if (secondLevelView instanceof ZhishiResultView) {
            edSearch.setHint("搜知识");
        }
    }

    private void showClearBtn(boolean isShow){
        if(isShow){
            clear_global.setVisibility(View.VISIBLE);
            search_speeach.setVisibility(View.GONE);
        }else{
            clear_global.setVisibility(View.GONE);
            search_speeach.setVisibility(View.VISIBLE);
        }
    }


    private void showView(View view) {

        view.setVisibility(View.VISIBLE);
        for (int i = 0; i < viewList.size(); i++) {
            View tempView = viewList.get(i);
            if (tempView != view) {
                tempView.setVisibility(View.GONE);
            }
        }

        if (view == caipuView
                || view == tieziView) {
            iv_history.setVisibility(View.VISIBLE);
        } else {
            iv_history.setVisibility(View.INVISIBLE);
        }

        if (view == defaultView) {
//            XHClick.track(defaultView.getContext(), "浏览搜索默认页");
            defaultView.refresh();
        }
    }

    private void setSearchMsg(String key, int type) {
        searchKey = key;
        searchType = type;
        edSearch.setText(key);
    }

    private void resetEdHint() {
        edSearch.setHint("搜菜谱、食材等");
    }

    public void setSecondLevelView(View view) {
        if (view != null) {
            secondLevelView = view;
            iv_history.setVisibility(View.GONE);
            setHintInfo();
        }
    }

    //判断当前页面上是否有二级页面，如果有隐藏二级界面
    public boolean hideSecondLevelView() {
        boolean flag = false;
        if (secondLevelView != null &&
                secondLevelView instanceof ZhishiResultView
                || secondLevelView instanceof CaidanResultView) {
            secondLevelView.setVisibility(View.GONE);
            secondLevelView = null;
            iv_history.setVisibility(View.VISIBLE);
            caipuView.showCaipuSearchResultView();
            resetEdHint();
            flag = true;
        }
        return flag;
    }


}