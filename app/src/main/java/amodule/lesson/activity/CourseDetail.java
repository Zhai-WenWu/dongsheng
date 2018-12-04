package amodule.lesson.activity;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.Map;

import acore.logic.load.LoadManager;
import acore.override.activity.base.BaseAppCompatActivity;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.widget.rvlistview.RvListView;
import amodule.lesson.adapter.CourseVideoContentAdapter;
import amodule.lesson.view.CourseDetailAskView;
import amodule.lesson.view.CourseDetailIntroductionView;
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
    private CourseDetailRadioButtonView radioButtonMotionless;
    private int titleHeight;
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
        ArrayList<String> strings = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            strings.add("i");
        }
        mCourseList.setAdapter(new CourseVideoContentAdapter(this, strings));
        radioButtonMotionless = findViewById(R.id.radiobutton_move);
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
        int height = Tools.getMeasureHeight(courseDetailTitleView);
        mCourseList.addHeaderView(courseDetailTitleView);

        //课程表
        // TODO: 2018/12/4

        //tab按钮
        initTabView();

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


    private void initTabView() {
        radioButtonMove = new CourseDetailRadioButtonView(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, 40, 1.0f);
        for (int i = 0; i < 3; i++) {
            RadioButton radioButton = new RadioButton(this);
            radioButton.setGravity(Gravity.CENTER);
            radioButton.setId(i);
            radioButton.setButtonDrawable(null);
            radioButton.setText("btn" + i);
            //@color/cuouse_radiobutton_textcolor
            radioButton.setTextColor(getResources().getColor(R.color.cuouse_radiobutton_textcolor));
            radioButton.setTextSize(16);
            radioButton.setBackgroundColor(getResources().getColor(R.color.transparent));
//            if (i == 0)
//                radioButton.setChecked(true);
            radioButtonMove.getView().addView(radioButton, lp);


            RadioButton radioButton1 = new RadioButton(this);
            radioButton1.setGravity(Gravity.CENTER);
            radioButton1.setId(i);
            radioButton1.setButtonDrawable(null);
            radioButton1.setText("btn" + i);
            radioButton1.setTextColor(getResources().getColor(R.color.cuouse_radiobutton_textcolor));
            radioButton1.setTextSize(16);
            radioButton1.setBackgroundColor(getResources().getColor(R.color.transparent));
//            if (i == 0)
//                radioButton1.setChecked(true);
            radioButtonMotionless.getView().addView(radioButton1, lp);
            RadioButton child = (RadioButton) radioButtonMove.getView().getChildAt(0);
            child.setChecked(true);
        }

        mCourseList.addHeaderView(radioButtonMove);

        radioButtonMotionless.addOnCheckedChangedListener(new CourseDetailRadioButtonView.OnCheckedChangedListener() {
            @Override
            public void onClickChange(int checkedId) {
                radioButtonMove.setClickIndex(checkedId);
            }
        });

        radioButtonMove.addOnCheckedChangedListener(new CourseDetailRadioButtonView.OnCheckedChangedListener() {
            @Override
            public void onClickChange(int checkedId) {
                radioButtonMotionless.setClickIndex(checkedId);
                // TODO: 2018/12/4 定位
                Log.i("radioButtonMove", checkedId + "");
            }
        });

        View emptyView = new View(this);
        mCourseList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                emptyView.scrollBy(0,dy);
//                    Log.i("logi", "top = " + radioButtonMove.getTop() + "   scrollY = " + scrollY);
                if (emptyView.getScrollY() > radioButtonMove.getTop()) {
                    radioButtonMotionless.setVisibility(View.VISIBLE);
                } else {
                    radioButtonMotionless.setVisibility(View.GONE);
                }

            }
        });

    }


}
