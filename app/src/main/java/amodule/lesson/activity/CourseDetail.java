package amodule.lesson.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mob.tools.RxMob;
import com.xiangha.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import acore.logic.stat.intefaces.OnClickListenerStat;
import acore.override.activity.base.BaseAppCompatActivity;
import acore.tools.StringManager;
import acore.tools.ToolsDevice;
import amodule.lesson.controler.data.CourseDataController;
import amodule.lesson.view.StudySyllabusView;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;
import third.share.BarShare;
import third.video.VideoPlayerController;

/**
 * Description :
 * PackageName : amodule.lesson.activity
 * Created by mrtrying on 2018/12/3 15:27.
 * e_mail : ztanzeyu@gmail.com
 */
public class CourseDetail extends BaseAppCompatActivity {
    public static final String EXTRA_CHAPTER_CODE = "chapterCode";
    public static final String EXTRA_CODE = "code";
    public static final String EXTRA_GROUP = "group";
    public static final String EXTRA_CHILD = "child";
    //    private RvListView mCourseList;
    private StudySyllabusView mStudySyllabusView;
    private LinearLayout mBottomLableLayout;
    private Map<String, String> mTopInfoMap;
    private VideoPlayerController mVideoPlayerController;
    private String mCode = "0";
    private String mChapterCode = "0";
    private final int SELECT_COURSE = 1;
    private int mGroupSelectIndex = 0;
    private int mChildSelectIndex = -1;
    private Map<String, String> shareMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initExtraData();
        initActivity("", 2, 0, 0, R.layout.a_course_detail);
        initView();
    }

    private void initExtraData() {
        mChapterCode = getIntent().getStringExtra(EXTRA_CHAPTER_CODE);
//        mCode = getIntent().getStringExtra(EXTRA_CODE);
        mGroupSelectIndex = getIntent().getIntExtra(EXTRA_GROUP, 0);
        mChildSelectIndex = getIntent().getIntExtra(EXTRA_CHILD, -1);
    }

    private void initView() {
        mBottomLableLayout = findViewById(R.id.ll_bottom);
        mStudySyllabusView = findViewById(R.id.view_syllabus);
        LinearLayout topAnimalLayout = findViewById(R.id.ll_top_animal);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                topAnimalLayout.setVisibility(View.GONE);
                TranslateAnimation mShowAction = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
                        Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
                        0.0f, Animation.RELATIVE_TO_SELF, -1.0f);
                mShowAction.setDuration(500);
                topAnimalLayout.clearAnimation();
                topAnimalLayout.setAnimation(mShowAction);
            }
        }, 5000);

        RelativeLayout videoLayout = findViewById(R.id.video_layout);
        ViewGroup.LayoutParams layoutParams = videoLayout.getLayoutParams();
        double i = ((double) 211) / 375;
        int DW = ToolsDevice.getWindowPx(this).widthPixels;
        int H = (int) (DW * i);
        layoutParams.height = H;
        videoLayout.setLayoutParams(layoutParams);
        // TODO: 2018/12/7
        mVideoPlayerController = new VideoPlayerController(this, videoLayout, "https://ws1.sinaimg.cn/large/0065oQSqgy1fxno2dvxusj30sf10nqcm.jpg");
        mVideoPlayerController.setVideoUrl("http://221.228.226.23/11/t/j/v/b/tjvbwspwhqdmgouolposcsfafpedmb/sh.yinyuetai.com/691201536EE4912BF7E4F1E2C67B8119.mp4");
        loadManager.setLoading(v -> loadInfo());
    }


    public void initTitle() {
        ImageView shareBtn = findViewById(R.id.share_icon_white);
        shareBtn.setVisibility(View.VISIBLE);
        TextView titleV = (TextView) findViewById(R.id.title);
        titleV.setMaxWidth(ToolsDevice.getWindowPx(this).widthPixels - ToolsDevice.dp2px(this, 45 + 40));
        titleV.setText(mTopInfoMap.get("name"));
        shareMap = StringManager.getFirstMap(mTopInfoMap.get("shareData"));
        OnClickListenerStat shareClick = new OnClickListenerStat() {
            @Override
            public void onClicked(View v) {
                doShare();
            }
        };
        shareBtn.setOnClickListener(shareClick);
        findViewById(R.id.back_white).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void doShare() {
        barShare = new BarShare(this, "", "");
        barShare.setShare(BarShare.IMG_TYPE_WEB, shareMap.get("title"), shareMap.get("content"),
                shareMap.get("img"), shareMap.get("url"));
        barShare.openShare();
    }

    private void loadInfo() {
        CourseDataController.loadLessonInfoData(mChapterCode, mCode, new InternetCallback() {
            @Override
            public void loaded(int flag, String s, Object o) {
                if (flag >= ReqInternet.REQ_OK_STRING) {
                    mTopInfoMap = StringManager.getFirstMap(o);
                    initTitle();
                    List<Map<String, String>> labelDataList = StringManager.getListMapByJson(mTopInfoMap.get("labelData"));
                    if (mBottomLableLayout != null && mBottomLableLayout.getChildCount() > 0) {
                        mBottomLableLayout.removeAllViews();
                    }
                    for (int i = 0; i < labelDataList.size(); i++) {
                        View lableView = createLableView(labelDataList.get(i), mBottomLableLayout, i);
                        mBottomLableLayout.addView(lableView);
                    }
                    loadCourseListData();
                }
            }
        });
    }

    private void loadCourseListData() {
        CourseDataController.loadCourseListData(mCode, "2", new InternetCallback() {
            @Override
            public void loaded(int i, String s, Object o) {
                if (i >= ReqInternet.REQ_OK_STRING) {
                    Map<String, String> resultMap = StringManager.getFirstMap(o);
                    initCourseListData(resultMap);
                    loadManager.loadOver(i);
                }
            }
        });
    }

    private View createLableView(Map<String, String> stringStringMap, LinearLayout parent, int i) {
        View view = LayoutInflater.from(this).inflate(R.layout.view_study_bottom_lable_item, parent, false);
        if (i == 0) {
            view.findViewById(R.id.lable_line).setVisibility(View.GONE);
        }
        TextView textView = view.findViewById(R.id.label_text);
        String title = stringStringMap.get("title");
        textView.setText(title);
        // TODO: 2018/12/7
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (title) {
                    case "学习要点":
                        Intent StudyPointIntent = new Intent(CourseDetail.this, StudyPoint.class);
                        StudyPointIntent.putExtra(StudyPoint.EXTRA_CODE, mCode);
                        StudyPointIntent.putExtra(StudyIntroduction.EXTRA_TITLE, title);
                        startActivity(StudyPointIntent);
                        break;
                    case "常见问题":
                        Intent StudyAskIntent = new Intent(CourseDetail.this, StudyAsk.class);
                        StudyAskIntent.putExtra(StudyIntroduction.EXTRA_TITLE, title);
                        startActivity(StudyAskIntent);
                        break;
                    case "课程简介":
                        Intent StudyIntroductionIntent = new Intent(CourseDetail.this, StudyIntroduction.class);
                        StudyIntroductionIntent.putExtra(StudyIntroduction.EXTRA_TITLE, title);
                        StudyIntroductionIntent.putExtra(StudyIntroduction.EXTRA_DESC, mTopInfoMap.get("desc"));
                        startActivity(StudyIntroductionIntent);
                        break;
                }
            }
        });
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) view.getLayoutParams();
        params.width = 0;
        params.weight = 1;
        return view;
    }

    private void initCourseListData(Map<String, String> courseListMap) {
        //课程横划
        mStudySyllabusView.setData(courseListMap, mGroupSelectIndex, mChildSelectIndex);
        //课程横划点击回调
        mStudySyllabusView.setOnSyllabusSelect(new StudySyllabusView.OnSyllabusSelect() {
            @Override
            public void onSelect(int position) {
//                    mCode = data.getStringExtra("code");
                mChildSelectIndex = position;
                loadInfo();
            }
        });
        TextView mClassNumTv = mStudySyllabusView.findViewById(R.id.tv_class_num);
        mClassNumTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //课程页
                Intent intent = new Intent(CourseDetail.this, CourseList.class);
                intent.putExtra(CourseDetail.EXTRA_GROUP, mGroupSelectIndex);
                intent.putExtra(CourseDetail.EXTRA_CHILD, mChildSelectIndex);
                intent.putExtra(CourseList.EXTRA_FROM_STUDY, true);
                startActivityForResult(intent, SELECT_COURSE);
            }
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