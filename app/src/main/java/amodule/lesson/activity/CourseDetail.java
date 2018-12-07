package amodule.lesson.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import acore.logic.AppCommon;
import acore.override.activity.base.BaseAppCompatActivity;
import acore.tools.StringManager;
import amodule.lesson.controler.data.CourseDataController;
import amodule.lesson.view.StudySyllabusView;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;
import third.video.VideoPlayerController;

/**
 * Description :
 * PackageName : amodule.lesson.activity
 * Created by mrtrying on 2018/12/3 15:27.
 * e_mail : ztanzeyu@gmail.com
 */
public class CourseDetail extends BaseAppCompatActivity{
    public static final String EXTRA_CODE = "code";
    public static final String EXTRA_TYPE = "type";
    public static final String EXTRA_GROUP = "group";
    public static final String EXTRA_CHILD = "child";
    //    private RvListView mCourseList;
    private StudySyllabusView mStudySyllabusView;
    private LinearLayout mBottomLableLayout;
    private Map<String, String> mTopInfoMap;
    private VideoPlayerController mVideoPlayerController;
    private String mCode = "0";
    private String mChapterCode = "0";
    private String mType = "1";
    private final int SELECT_COURSE = 1;
    private int mGroupSelectIndex = 0;
    private int mChildSelectIndex = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initExtraData();
        initActivity("", 2, 0, R.layout.c_view_bar_title, R.layout.a_course_detail);
        initView();
    }

    private void initExtraData() {
//        mCode = getIntent().getStringExtra(EXTRA_CODE);
//        mType = getIntent().getStringExtra(EXTRA_TYPE);
    }

    private void initView() {
        mBottomLableLayout = findViewById(R.id.ll_bottom);
        mStudySyllabusView = findViewById(R.id.view_syllabus);

        RelativeLayout videoLayout = findViewById(R.id.video_layout);
        // TODO: 2018/12/7
        mVideoPlayerController = new VideoPlayerController(this, videoLayout, "https://ws1.sinaimg.cn/large/0065oQSqgy1fxno2dvxusj30sf10nqcm.jpg");
        mVideoPlayerController.setVideoUrl("http://221.228.226.23/11/t/j/v/b/tjvbwspwhqdmgouolposcsfafpedmb/sh.yinyuetai.com/691201536EE4912BF7E4F1E2C67B8119.mp4");
        loadManager.setLoading(v -> loadInfo());
    }

    private void loadInfo() {
        CourseDataController.loadLessonInfoData(mCode, new InternetCallback() {
            @Override
            public void loaded(int flag, String s, Object o) {
                if (flag >= ReqInternet.REQ_OK_STRING) {
                    mTopInfoMap = StringManager.getFirstMap(o);
                    List<Map<String, String>> labelDataList = StringManager.getListMapByJson(mTopInfoMap.get("labelData"));
                    if (mBottomLableLayout != null && mBottomLableLayout.getChildCount() > 0) {
                        mBottomLableLayout.removeAllViews();
                    }
                    for (int i = 0; i < labelDataList.size(); i++) {
                        View lableView = createLableView(labelDataList.get(i), mBottomLableLayout);
                        mBottomLableLayout.addView(lableView);
                    }
                    loadCourseList();
                }
            }

        });
    }

    private View createLableView(Map<String, String> stringStringMap, LinearLayout parent) {
        View view = LayoutInflater.from(this).inflate(R.layout.view_study_bottom_lable_item, parent, false);
        TextView textView = view.findViewById(R.id.label_text);
        String title = stringStringMap.get("title");
        textView.setText(title);
        view.setOnClickListener(v -> {
            AppCommon.openUrl(stringStringMap.get("url"),true);
        });
        // TODO: 2018/12/7
        view.setOnClickListener(v -> {
            switch (title){
                case "学习要点":
                    Intent StudyPointIntent = new Intent(CourseDetail.this, StudyPoint.class);
                    startActivity(StudyPointIntent);
                    break;
                case "常见问题":
                    Intent StudyAskIntent = new Intent(CourseDetail.this, StudyAsk.class);
                    startActivity(StudyAskIntent);
                    break;
                case "课程简介":
                    Intent StudyIntroductionIntent = new Intent(CourseDetail.this, StudyIntroduction.class);
                    StudyIntroductionIntent.putExtra(StudyIntroduction.EXTRA_TITLE, title);
                    StudyIntroductionIntent.putExtra(StudyIntroduction.EXTRA_DESC, mTopInfoMap.get("desc") + "这是描述");
                    startActivity(StudyIntroductionIntent);
                    break;
            }
        });
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) view.getLayoutParams();
        params.width = 0;
        params.weight = 1;
        return view;
    }

    private void loadCourseList() {
        CourseDataController.loadCourseListData(mCode, mType, new InternetCallback() {
            @Override
            public void loaded(int i, String s, Object o) {
                if (i >= ReqInternet.REQ_OK_STRING) {
                    Map<String, String> resultMap = StringManager.getFirstMap(o);
                    initCourseListData(resultMap);
                }
            }
        });
    }

    private void initCourseListData(Map<String, String> courseListMap) {
        //课程横划
        ArrayList<Map<String, String>> info = StringManager.getListMapByJson(courseListMap.get("chapterList"));
        Map<String, String> lessonListMap = info.get(mGroupSelectIndex);//第几章
        mStudySyllabusView.setData(lessonListMap, mChildSelectIndex);
        //课程横划点击回调
        mStudySyllabusView.setOnSyllabusSelect(position -> {
//                    mCode = data.getStringExtra("code");
            mChildSelectIndex = position;
            loadInfo();
        });
        TextView mClassNumTv = mStudySyllabusView.findViewById(R.id.tv_class_num);
        mClassNumTv.setOnClickListener(v -> {
            //课程页
            Intent intent = new Intent(CourseDetail.this, CourseList.class);
            intent.putExtra(CourseDetail.EXTRA_GROUP, mGroupSelectIndex);
            intent.putExtra(CourseDetail.EXTRA_CHILD, mChildSelectIndex);
            intent.putExtra(CourseList.EXTRA_FROM_STUDY, true);
            startActivityForResult(intent, SELECT_COURSE);
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            switch (requestCode) {
                case SELECT_COURSE:
//                    mCode = data.getStringExtra("code");
                    mGroupSelectIndex = data.getIntExtra(EXTRA_GROUP, 0);
                    mChildSelectIndex = data.getIntExtra(EXTRA_CHILD, -1);
                    loadInfo();
                    break;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mVideoPlayerController != null) {
            mVideoPlayerController.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mVideoPlayerController != null) {
            mVideoPlayerController.onPause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mVideoPlayerController != null) {
            mVideoPlayerController.onDestroy();
        }
    }

    @Override
    public void onBackPressed() {
        if (mVideoPlayerController != null && mVideoPlayerController.onBackPressed()) {
            return;
        }
        super.onBackPressed();
    }
}
