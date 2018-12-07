package amodule.lesson.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;

import com.xiangha.R;

import acore.override.activity.base.BaseAppCompatActivity;
import acore.tools.StringManager;
import acore.tools.Tools;
import amodule.lesson.view.StudylIntroductionView;

public class StudyIntroduction extends BaseAppCompatActivity {
    public static final String EXTRA_TITLE = "title";
    public static final String EXTRA_DESC = "desc";

    private String titleStr,descStr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //初始化数据
        initExtraData();
        initActivity(titleStr, 2, 0, R.layout.c_view_bar_title,R.layout.a_study_introduction);
        //初始化UI
        initUI();
    }

    private void initUI() {
        TextView contentText = findViewById(R.id.content_text);
        contentText.setText(descStr);
    }

    private void initExtraData() {
        titleStr = getIntent().getStringExtra(EXTRA_TITLE);
        titleStr = TextUtils.isEmpty(titleStr) ? "" : titleStr;
        descStr = getIntent().getStringExtra(EXTRA_DESC);
    }
}
