package amodule.lesson.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.Map;

import acore.override.activity.base.BaseAppCompatActivity;
import acore.tools.StringManager;
import acore.widget.rvlistview.RvListView;
import amodule.lesson.adapter.CourseVideoContentAdapter;
import amodule.lesson.view.StudyAskView;
import amodule.lesson.view.StudySyllabusView;
import amodule.lesson.view.StudyTitleView;
import amodule.lesson.view.StudylIntroductionView;

public class StudyPoint extends BaseAppCompatActivity {
    public static final String EXTRA_DESC = "desc";

    private CourseVideoContentAdapter mVideoDetailAdapter;
    private ArrayList<Map<String, String>> videoList;
    private RvListView mCourseList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_study_point);
        mCourseList = findViewById(R.id.course_list);
        String extra = getIntent().getStringExtra(EXTRA_DESC);
        videoList = StringManager.getListMapByJson(extra);
        mVideoDetailAdapter = new CourseVideoContentAdapter(this, videoList);
        mCourseList.setAdapter(mVideoDetailAdapter);
    }
}
