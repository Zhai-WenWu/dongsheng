package amodule.lesson.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.Map;

import acore.logic.load.LoadManager;
import acore.override.activity.base.BaseAppCompatActivity;
import acore.tools.StringManager;
import acore.widget.rvlistview.RvListView;
import amodule.lesson.adapter.CourseVideoContentAdapter;
import amodule.lesson.controler.data.CourseDataController;
import amodule.lesson.view.StudyAskView;
import amodule.lesson.view.StudySyllabusView;
import amodule.lesson.view.StudylIntroductionView;
import amodule.lesson.view.StudyTitleView;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;

/**
 * Description :
 * PackageName : amodule.lesson.activity
 * Created by mrtrying on 2018/12/3 15:27.
 * e_mail : ztanzeyu@gmail.com
 */
public class CourseDetail extends BaseAppCompatActivity {

    private RvListView mCourseList;
    private ArrayList<String> mVideoContentList;
    private StudyTitleView studyTitleView;
    private Map<String, String> mTopInfoMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActivity("", 2, 0, R.layout.c_view_bar_title, R.layout.a_course_detail);
        initView();
        loadInfo();
    }

    private void initView() {
        loadManager = new LoadManager(this, rl);
        rl = (RelativeLayout) findViewById(R.id.activityLayout);
        mCourseList = findViewById(R.id.course_list);
        mVideoContentList = new ArrayList<>();
        mCourseList.setAdapter(new CourseVideoContentAdapter(this, mVideoContentList));
    }

    private void loadInfo() {
        CourseDataController.loadChapterTopData("", new InternetCallback() {
            @Override
            public void loaded(int i, String s, Object o) {
                if (i >= ReqInternet.REQ_OK_STRING) {
                    mTopInfoMap = StringManager.getFirstMap(o);
                    CourseDataController.loadCourseListData("", "", new InternetCallback() {
                        @Override
                        public void loaded(int i, String s, Object o) {
                            if (i >= ReqInternet.REQ_OK_STRING) {
                                Map<String, String> mCourseListMap = StringManager.getFirstMap(o);
                                initCourseListData(mCourseListMap);
                            }
                        }
                    });
                }
            }

        });
    }


    private void initCourseListData(Map<String, String> courseListMap) {
        //标题
        studyTitleView = new StudyTitleView(this);
        studyTitleView.setTitleData(mTopInfoMap.get("name"));
        studyTitleView.setSubTitleData(courseListMap.get("subTitle"));
        mCourseList.addHeaderView(studyTitleView);

        //课程横划
        StudySyllabusView studySyllabusView = new StudySyllabusView(this);
        TextView mClassNumTv = studySyllabusView.findViewById(R.id.tv_class_num);
        mCourseList.addHeaderView(studySyllabusView);
        mClassNumTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //课程页
                startActivity(new Intent(CourseDetail.this, CourseList.class));
            }
        });

        CourseDataController.loadChapterDescData("", new InternetCallback() {
            @Override
            public void loaded(int i, String s, Object o) {
                if (i >= ReqInternet.REQ_OK_STRING) {
                    Map<String, String> mDescoMap = StringManager.getFirstMap(o);
                    initDescData(mDescoMap);
                }
            }

        });
    }


    private void initDescData(Map<String, String> descoMap) {
        //简介
        Map<String, String> desc = StringManager.getFirstMap(descoMap.get("desc"));
        if (desc != null && desc.size() > 0) {
            StudylIntroductionView courseDetailClassView = new StudylIntroductionView(this, desc);
            mCourseList.addHeaderView(courseDetailClassView);
        }

        //问答
        StudyAskView studyAskView = new StudyAskView(this);
        mCourseList.addFooterView(studyAskView);
    }


}
