package amodule.topic.activity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiangha.R;

import org.eclipse.jetty.util.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import acore.override.activity.base.BaseActivity;
import acore.tools.StringManager;
import acore.widget.rvlistview.RvListView;
import amodule.search.view.MultiTagView;
import amodule.topic.Controller.ReqController;
import amodule.topic.Controller.ReqView;
import amodule.topic.adapter.RecommentTopicAdapter;
import amodule.topic.adapter.TopicSearchAdapter;
import amodule.topic.data.HistoryDataUtil;
import aplug.basic.LoadImage;

/**
 *
 */
public class SearchTopicActivity extends BaseActivity implements ReqView {

    private MultiTagView topicHistoryTable;
    private RelativeLayout topicHistoryRl;
    private EditText searchWordEd;
    private RvListView topicSearchRv;
    private ReqController reqController;
    private RvListView topicRecommentRv;
    private LinearLayout hotTopicRecommendLl;
    private FrameLayout serachFl;
    private TopicSearchAdapter topicSearchAdapter;
    private RecommentTopicAdapter recommentTopicAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActivity("添加话题", 2, 0, 0, R.layout.search_topic_layout);
//        LoadImage.with(this).load(url).build().into(imageView);
        initTitle();
        initView();
        getReqController().getTopicRecom();
    }

    private void initView() {
        topicHistoryTable = findViewById(R.id.topic_history_table);
        searchWordEd = findViewById(R.id.ed_search_word);
        ImageView topicHistoryDeleteImage = findViewById(R.id.iv_topic_history_delete);
        topicHistoryRl = findViewById(R.id.rv_topic_history);
        topicRecommentRv = findViewById(R.id.recycler_view);
        hotTopicRecommendLl = findViewById(R.id.ll_hot_topic_recommend);
        if (HistoryDataUtil.getHistoryWords().size() > 0) {
            initHistoryTable();
        }
        topicSearchRv = findViewById(R.id.serach_recycler_view);
        serachFl = findViewById(R.id.fl_serach);
        topicSearchAdapter = new TopicSearchAdapter(this, null);
        topicSearchRv.setAdapter(topicSearchAdapter);
        initSearchData(null);
//        HistoryDataUtil.saveSearchWord(searchWordEd.getText().toString());
        topicHistoryDeleteImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HistoryDataUtil.deleteHistoryWord();
                topicHistoryRl.setVisibility(View.GONE);
            }
        });
    }

    /**
     * 搜索数据填充
     *
     * @param data
     */
    private void initSearchData(List<Map<String, String>> data) {
        if (data != null) {
            if (data.size() > 0) {
                topicSearchAdapter.setData(data);
                topicSearchRv.setVisibility(View.VISIBLE);
            } else {
                topicSearchRv.setVisibility(View.GONE);
            }
        }

        searchWordEd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    serachFl.setVisibility(View.VISIBLE);
                    if (data != null) {
                        if (data.size() > 0) {
                            topicSearchRv.setVisibility(View.VISIBLE);
                        } else {
                            topicSearchRv.setVisibility(View.GONE);
                        }
                    } else {
                        topicSearchRv.setVisibility(View.GONE);
                    }

                    getReqController().getTopicSearch(s.toString().trim(), 1);
                } else {
                    serachFl.setVisibility(View.GONE);
                }
            }
        });

    }


    /**
     * 搜索历史数据填充
     */
    private void initHistoryTable() {
        topicHistoryRl.setVisibility(View.VISIBLE);
        topicHistoryTable.addTags(HistoryDataUtil.getHistoryWords(), new MultiTagView.MutilTagViewCallBack() {
            @Override
            public void onClick(int tagIndexr) {

            }
        });
    }


    /**
     * @param data 推荐数据填充
     */
    private void initRecomment(List<Map<String, String>> data) {
        List<String> topicRecommentList = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            topicRecommentList.add(data.get(i).get("name"));
        }

        recommentTopicAdapter = new RecommentTopicAdapter(this, topicRecommentList);
        topicRecommentRv.setAdapter(recommentTopicAdapter);
    }

    private void initTitle() {
        View titleView = findViewById(R.id.title_view);
        TextView title = titleView.findViewById(R.id.title);
        title.setText("添加话题");
        titleView.findViewById(R.id.back_ll).findViewById(R.id.leftImgBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public ReqController getReqController() {
        if (reqController == null) {
            reqController = new ReqController(this);
        }
        return reqController;
    }

    @Override
    public void upData(String url, List<Map<String, String>> data) {
        if (url.contains(StringManager.API_TOPIC_RECOM)) {
            if (data != null && data.size() > 0) {
                initRecomment(data);
            } else {
                hotTopicRecommendLl.setVisibility(View.GONE);
            }
        } else if (url.contains(StringManager.API_TOPIC_SEARCH)) {
            if (data != null) {
                initSearchData(data);
            }
        }
    }
}
