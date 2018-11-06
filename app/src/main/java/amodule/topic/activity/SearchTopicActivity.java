package amodule.topic.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiangha.R;

import acore.override.activity.base.BaseActivity;
import amodule.search.view.MultiTagView;
import amodule.topic.data.HistoryDataUtil;

public class SearchTopicActivity extends BaseActivity {

    private MultiTagView topicHistoryTable;
    private RelativeLayout topicHistoryRv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActivity("添加话题", 2, 0, 0, R.layout.search_topic_layout);
//        LoadImage.with(this).load("").build().
        initTitle();
        initView();
    }

    private void initView() {
        topicHistoryTable = findViewById(R.id.topic_history_table);
        EditText searchWordEd = findViewById(R.id.ed_search_word);
        ImageView topicHistoryDeleteImage = findViewById(R.id.iv_topic_history_delete);
        topicHistoryRv = findViewById(R.id.rv_topic_history);
        if (HistoryDataUtil.getHistoryWords().size()>0){
            initHistoryTable();
        }
        findViewById(R.id.search).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HistoryDataUtil.saveSearchWord(searchWordEd.getText().toString());
            }
        });
        topicHistoryDeleteImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HistoryDataUtil.deleteHistoryWord();
                topicHistoryRv.setVisibility(View.GONE);
            }
        });
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

    private void initHistoryTable() {
        topicHistoryRv.setVisibility(View.VISIBLE);
        topicHistoryTable.addTags(HistoryDataUtil.getHistoryWords(), new MultiTagView.MutilTagViewCallBack() {
            @Override
            public void onClick(int tagIndexr) {

            }
        });
    }

}
