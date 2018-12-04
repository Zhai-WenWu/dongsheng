package amodule.lesson.activity;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.RelativeLayout;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.Map;

import acore.logic.load.LoadManager;
import acore.override.activity.base.BaseAppCompatActivity;
import acore.tools.StringManager;
import acore.widget.rvlistview.RvListView;
import amodule.lesson.adapter.CourseVideoContentAdapter;
import amodule.lesson.view.CourseDetailAskView;
import amodule.lesson.view.CourseDetailIntroductionView;
import amodule.lesson.view.CourseDetailClassCardView;
import amodule.lesson.view.CourseDetailRadioButtonView;
import amodule.lesson.view.CourseDetailTitleView;

/**
 * Description :
 * PackageName : amodule.lesson.activity
 * Created by mrtrying on 2018/12/3 15:27.
 * e_mail : ztanzeyu@gmail.com
 */
public class CourseDetail extends BaseAppCompatActivity {

    private Map<String, String> mInfoMap;
    private RvListView mCourseList;
    private CourseDetailRadioButtonView radioButtonMove;

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
        mCourseList.setAdapter(new CourseVideoContentAdapter(this,new ArrayList()));
        radioButtonMove = findViewById(R.id.radiobutton_move);

        mCourseList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                Log.i("zww","onScrolled");

            }
        });

    }

    private void loadInfo() {

        mInfoMap = StringManager.getFirstMap(Json.o1);
        initData();
//        ReqEncyptInternet.in().doGetEncypt(StringManager.API_COURSE_CHAPTERDESC, "code=" + "1000", new InternetCallback() {
//
//            @Override
//            public void loaded(int i, String s, Object o) {
//                if (i >= ReqInternet.REQ_OK_STRING) {
//                    mInfoMap = StringManager.getFirstMap(o);
//                    initData();
//                }
//            }
//
//        });
    }


    private void initData() {
        //标题
        CourseDetailTitleView courseDetailTitleView = new CourseDetailTitleView(this);
        mCourseList.addHeaderView(courseDetailTitleView);

        //课程表
        CourseDetailClassCardView courseDetailClassCardView = new CourseDetailClassCardView(this);
        mCourseList.addHeaderView(courseDetailClassCardView);

        //tab按钮
        CourseDetailRadioButtonView radioButtonMotionless = new CourseDetailRadioButtonView(this);
        mCourseList.addHeaderView(radioButtonMotionless);

        //简介
        Map<String, String> desc = StringManager.getFirstMap(mInfoMap.get("desc"));
        if (desc != null && desc.size() > 0) {
            CourseDetailIntroductionView courseDetailClassView = new CourseDetailIntroductionView(this, desc);
            mCourseList.addHeaderView(courseDetailClassView);
        }

        //问答
        CourseDetailAskView courseDetailAskView = new CourseDetailAskView(this);
        mCourseList.addFooterView(courseDetailAskView);
    }


}
