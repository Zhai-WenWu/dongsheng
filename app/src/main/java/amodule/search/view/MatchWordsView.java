package amodule.search.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.xianghatest.R;

import java.util.ArrayList;
import java.util.Map;

import acore.logic.SetDataView;
import acore.logic.XHClick;
import acore.override.XHApplication;
import acore.override.activity.base.BaseActivity;
import acore.override.adapter.AdapterSimple;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import amodule.search.data.SearchConstant;
import amodule.search.data.SearchDataImp;
import cn.srain.cube.views.ptr.PtrClassicFrameLayout;
import cn.srain.cube.views.ptr.PtrDefaultHandler;
import cn.srain.cube.views.ptr.PtrFrameLayout;

import static amodule.search.data.SearchConstant.SEARCH_CATEGORY;

/**
 * Created by ：airfly on 2016/10/13 16:25.
 */

public class MatchWordsView extends LinearLayout {

    private Context context;
    private Activity mActivity;
    private ArrayList<Map<String, String>> matchWords;
    private TableLayout tb_matchwords;
    private MatchWordsCallback callback;
    private String searchWord;
    private PtrClassicFrameLayout matchwords_header_list_view_frame;


    public MatchWordsView(Context context) {
        this(context, null);
    }

    public MatchWordsView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MatchWordsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.c_view_match_word_search, this, true);
        this.context = context;
    }


    public void init(BaseActivity activity, MatchWordsCallback callback) {
        mActivity = activity;
        this.callback = callback;
        initView();
        setListener();
    }

    private void initView() {
        matchwords_header_list_view_frame = (PtrClassicFrameLayout) findViewById(R.id.matchwords_header_list_view_frame);
        tb_matchwords = (TableLayout) findViewById(R.id.tb_matchwords);

    }

    private void setListener() {

        matchwords_header_list_view_frame.setPtrHandler(new PtrDefaultHandler() {
            @Override
            public void onRefreshBegin(PtrFrameLayout frame) {
                matchwords_header_list_view_frame.refreshComplete();
                ToolsDevice.keyboardControl(false, context, MatchWordsView.this);
            }
        });

    }


    public void getMatchWords(final String searchKey) {
        searchWord = searchKey;
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (!TextUtils.isEmpty(searchKey)) {
                    matchWords = new SearchDataImp().getMatchWords(XHApplication.in(), searchKey);
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showMatchWordsView();
                        }
                    });
                }
            }
        }).start();
    }

    private void showMatchWordsView() {

        tb_matchwords.removeAllViews();
        AdapterSimple adapterMatchWords = new AdapterSimple(tb_matchwords, matchWords,
                R.layout.c_type_item_default_search,
                new String[]{"matchword"},
                new int[]{R.id.tv_type_search}) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {

                View view = super.getView(position, convertView, parent);
                if (position == matchWords.size() - 1) {
                    view.findViewById(R.id.item_sub_line).setVisibility(View.VISIBLE);
                } else {
                    view.findViewById(R.id.item_sub_line).setVisibility(View.GONE);
                }
                return view;
            }

            @Override
            public void setViewText(TextView v, String text) {
                super.setViewText(v,text);
                if(TextUtils.isEmpty(text) || TextUtils.isEmpty(searchWord))
                    return;
                SpannableStringBuilder style = new SpannableStringBuilder(text);
                int index = 0;
                if ((index = text.indexOf(searchWord, index)) > -1) {
                    for (int j = index; j < index + searchWord.length(); j++) {
                        // 设置指定位置文字的颜色
                        String color = Tools.getColorStr(mParent.getContext(), R.color.c_black_text);
                        style.setSpan(new ForegroundColorSpan(Color.parseColor(color)), j, j + 1,
                                Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                    }
                }
                v.setText(style);
            }
        };
        SetDataView.view(tb_matchwords, 1, adapterMatchWords, null, new SetDataView.ClickFunc[]{new SetDataView.ClickFunc() {
            @Override
            public void click(int index, View view) {
                if (index >= 0) {
                    XHClick.mapStat(mActivity, "a_search_match", "匹配词条", "" + index);

                    if (matchWords == null || matchWords.size() < 1)
                        return;
                    if(matchWords.size()>index) {
                        String s = matchWords.get(index).get("matchword");
                        if (!TextUtils.isEmpty(s)) {
                            searchWord = s;
                            if (callback != null) {
                                callback.onItemClick(s, SearchConstant.SEARCH_CAIPU);
                            }

                        }
                    }
                }
            }
        }}, android.view.ViewGroup.LayoutParams.MATCH_PARENT, ToolsDevice.dp2px(context, 51));

        addTypeView();

    }

    private void addTypeView() {

        for (String type : SEARCH_CATEGORY) {
            View view = LayoutInflater.from(mActivity).inflate(R.layout.c_type_title_default_search, null);
            view.findViewById(R.id.btn_back).setVisibility(View.VISIBLE);
            view.findViewById(R.id.iv_to_search).setVisibility(View.VISIBLE);
            view.findViewById(R.id.item_sub_line).setVisibility(View.VISIBLE);
            ((TextView) view.findViewById(R.id.tv_type_search)).setText(type);
            TableRow.LayoutParams lp = new TableRow.LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    ToolsDevice.dp2px(context, 51));
            view.setLayoutParams(lp);

            View tail_view = LayoutInflater.from(mActivity).inflate(R.layout.c_search_tail_hint_line, null);
            TableRow.LayoutParams lp2 = new TableRow.LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    ToolsDevice.dp2px(context, 5));
            tail_view.setLayoutParams(lp2);


            if (SEARCH_CATEGORY[0].equals(type)) {
                tb_matchwords.addView(view, 0);
                view.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        callback.onItemClick(searchWord, SearchConstant.SEARCH_CAIPU);
                    }
                });
            } else if (SEARCH_CATEGORY[1].equals(type)) {

                tb_matchwords.addView(tail_view, tb_matchwords.getChildCount());
                tb_matchwords.addView(view, tb_matchwords.getChildCount());
                view.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        callback.onItemClick(searchWord, SearchConstant.SEARCH_HAYOU);
                    }
                });

            } else if (SEARCH_CATEGORY[2].equals(type)) {
                tb_matchwords.addView(tail_view, tb_matchwords.getChildCount());
                tb_matchwords.addView(view, tb_matchwords.getChildCount());
                view.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        callback.onItemClick(searchWord, SearchConstant.SEARCH_MEISHITIE);
                    }
                });
            }
        }


        View view2 = new View(context);
        TableRow.LayoutParams lp = new TableRow.LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                ToolsDevice.dp2px(context, 100));
        view2.setLayoutParams(lp);
        tb_matchwords.addView(view2, tb_matchwords.getChildCount());

    }

    public interface MatchWordsCallback {
        void onItemClick(String searchKey, int searchType);
    }
}
