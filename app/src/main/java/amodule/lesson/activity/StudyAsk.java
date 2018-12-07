package amodule.lesson.activity;

import android.os.Bundle;

import com.xiangha.R;

import acore.override.activity.base.BaseAppCompatActivity;

public class StudyAsk extends BaseAppCompatActivity {
    public static final String EXTRA_DESC = "desc";
    public static final String EXTRA_TITLE = "title";
    private String titleStr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        titleStr = getIntent().getStringExtra(EXTRA_TITLE);
        initActivity(titleStr, 2, 0, R.layout.c_view_bar_title, R.layout.view_course_ask);
    }
}
