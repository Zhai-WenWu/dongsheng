package amodule.lesson.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.ArrayMap;
import android.view.View;
import android.view.ViewTreeObserver;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.Map;

import acore.logic.stat.intefaces.OnClickListenerStat;
import acore.override.activity.base.BaseAppCompatActivity;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import amodule.lesson.adapter.VerticalAdapter;
import amodule.lesson.controler.data.CourseDataController;
import amodule.lesson.view.StudyFirstPager;
import amodule.lesson.view.StudySecondPager;
import amodule.lesson.view.StudySyllabusView;
import amodule.lesson.view.VerticalViewPager;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;
import aplug.web.view.XHWebView;
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
    private Map<String, String> mLessonInfo;
    private Map<String, String> mSyllabusInfo;
    private VideoPlayerController mVideoPlayerController;
    private String mCode = "0";
    private String mChapterCode = "0";
    private final int SELECT_COURSE = 1;
    private int mGroupSelectIndex = 0;
    private int mChildSelectIndex = -1;
    private Map<String, String> shareMap;
    private VerticalViewPager viewPager;
    private Map<String, Map<String, String>> mData = new ArrayMap<>();
    private VerticalAdapter mVerticalAdapter;
    private StudyFirstPager studyFirstPager;
    private StudySecondPager studySecondPager;
    private LinearLayout mFirstPageBottomBtn;
    private boolean mLoadAgain;
    private int mSecondSelectTndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initExtraData();
        initActivity("", 2, 0, 0, R.layout.a_course_detail);
        initView();
    }

    private void initExtraData() {
//        mChapterCode = getIntent().getStringExtra(EXTRA_CHAPTER_CODE);
//        mCode = getIntent().getStringExtra(EXTRA_CODE);
//        mGroupSelectIndex = getIntent().getIntExtra(EXTRA_GROUP, 0);
    }

    private void initView() {
        viewPager = findViewById(R.id.viewpager);
        studyFirstPager = new StudyFirstPager(CourseDetail.this);
        studySecondPager = new StudySecondPager(CourseDetail.this);
        mFirstPageBottomBtn = studyFirstPager.getBtnLayout();
        mVerticalAdapter = new VerticalAdapter();
        viewPager.setAdapter(mVerticalAdapter);
        initCallBack();
        loadManager.setLoading(v -> loadLessonInfo());
    }

    /**
     * 回调处理
     */
    private void initCallBack() {
        //底部按钮绑定
        studyFirstPager.setOnClickBottomView(new StudyFirstPager.OnClickBottomView() {
            @Override
            public void clickView(int i) {
                for (int j = 0; j < mFirstPageBottomBtn.getChildCount(); j++) {
                    if (j == i)
                        continue;
                    mFirstPageBottomBtn.getChildAt(j).findViewById(R.id.hor_line).setVisibility(View.GONE);
                }
                mFirstPageBottomBtn.getChildAt(i).findViewById(R.id.hor_line).setVisibility(View.VISIBLE);
                viewPager.setCurrentItem(1, true);
                studySecondPager.setSelect(i);
            }
        });
        studySecondPager.getViewPager().addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mSecondSelectTndex = position;
                for (int j = 0; j < mFirstPageBottomBtn.getChildCount(); j++) {
                    if (j == position)
                        continue;
                    mFirstPageBottomBtn.getChildAt(j).findViewById(R.id.hor_line).setVisibility(View.GONE);
                }
                mFirstPageBottomBtn.getChildAt(position).findViewById(R.id.hor_line).setVisibility(View.VISIBLE);
                viewPager.setCurrentItem(1, true);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        //视频播放完成回调
        studyFirstPager.setOnVideoFinish(new StudyFirstPager.OnVideoFinish() {
            @Override
            public void videoFinish() {
                viewPager.setCurrentItem(0);
                loadAgain();
            }
        });

        //滑动冲突解决
        boolean[] canScroll = new boolean[3];
        for (int i = 0; i < canScroll.length; i++) {
            canScroll[i] = true;
        }
        studySecondPager.getSecondPagerWebAdapter().setmScrollInterface(new XHWebView.ScrollInterface() {
            @Override
            public void onSChanged(WebView webView, int l, int t, int oldl, int oldt) {
                if (webView.getScrollY() == 0) {
                    canScroll[mSecondSelectTndex] = true;
                } else {
                    canScroll[mSecondSelectTndex] = false;
                }
            }
        });
        viewPager.setWebScrollTop(new VerticalViewPager.OnWebScrollTop() {
            @Override
            public boolean canScroll() {
                return canScroll[mSecondSelectTndex];
            }
        });

        //底部按钮悬浮展示
        int bottomBtnHeight = Tools.getDimen(this, R.dimen.dp_49);
        viewPager.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int viewpagerHeight = viewPager.getHeight();
                double i = ((double) bottomBtnHeight) / viewpagerHeight;
                viewPager.setScale(i);
            }
        });
    }


    /**
     * 标题栏
     */
    public void initTitle() {
        RelativeLayout topBarWhite = findViewById(R.id.top_bar_white);
        ImageView shareBtn = findViewById(R.id.share_icon_white);
        shareBtn.setVisibility(View.VISIBLE);
        TextView titleTv = (TextView) findViewById(R.id.title);
        TextView titleBottomTv = (TextView) findViewById(R.id.title_bottom);
        titleTv.setMaxWidth(ToolsDevice.getWindowPx(this).widthPixels - ToolsDevice.dp2px(this, 45 + 40));
        titleTv.setText(mLessonInfo.get("name"));
        titleBottomTv.setMaxWidth(ToolsDevice.getWindowPx(this).widthPixels - ToolsDevice.dp2px(this, 45 + 40));
        titleBottomTv.setText(mLessonInfo.get("name"));
        shareMap = StringManager.getFirstMap(mLessonInfo.get("shareData"));
        //返回按钮控制
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
                if (viewPager.getShowPosition() > 0) {
                    viewPager.setCurrentItem(0, true);
                } else {
                    finish();
                }
            }
        });

        //title渐变
        viewPager.setScrollDistance(new VerticalViewPager.OnScrollDistance() {
            @Override
            public void scrollDistance(float distance) {
                if (distance > 0 && distance < 1) {
                    topBarWhite.setAlpha(distance);
                    mFirstPageBottomBtn.setAlpha(distance);
                }
            }

            @Override
            public void scrollEnd(int state) {
                if (state == 0) {
                    int showPosition = viewPager.getShowPosition();
                    if (showPosition == 0) {
                        topBarWhite.setAlpha(0);
                        mFirstPageBottomBtn.setAlpha(0);
                    } else if (showPosition == 1) {
                        topBarWhite.setAlpha(1);
                        mFirstPageBottomBtn.setAlpha(1);
                    }
                }
            }
        });
    }

    /**
     * 分享
     */
    private void doShare() {
        barShare = new BarShare(this, "", "");
        barShare.setShare(BarShare.IMG_TYPE_WEB, shareMap.get("title"), shareMap.get("content"),
                shareMap.get("img"), shareMap.get("url"));
        barShare.openShare();
    }

    /**
     * 学习页数据请求
     */
    private void loadLessonInfo() {
        CourseDataController.loadLessonInfoData(mChapterCode, mCode, new InternetCallback() {
            @Override
            public void loaded(int flag, String s, Object o) {
                if (flag >= ReqInternet.REQ_OK_STRING) {
                    mLessonInfo = StringManager.getFirstMap(o);
                    mData.put("lessonInfo", mLessonInfo);
                    initTitle();
                    loadManager.loadOver(flag);
                    if (mLoadAgain){
                        initSyllabusData();
                    }else {
                        loadSyllabusInfo();
                    }
                    mLoadAgain = false;
                }
            }
        });
    }

    /**
     * 课程表数据请求
     */
    private void loadSyllabusInfo() {
        CourseDataController.loadCourseListData(mCode, "2", new InternetCallback() {

            @Override
            public void loaded(int i, String s, Object o) {
                if (i >= ReqInternet.REQ_OK_STRING) {
                    mSyllabusInfo = StringManager.getFirstMap(o);
                    Map<String, String> lesson = StringManager.getListMapByJson(mSyllabusInfo.get("chapterList")).get(0);
                    ArrayList<Map<String, String>> lessonList = StringManager.getListMapByJson(lesson.get("chapterList"));
                    for (int j = 0; j < lessonList.size(); j++) {
                        if (lessonList.get(i).get("code").equals(mCode)) {
                            mChildSelectIndex = i;
                        }
                    }

                    initSyllabusData();
                }
            }
        });
    }


    /**
     * 数据填充
     */
    private void initSyllabusData() {
        mData.put("syllabusInfo", mSyllabusInfo);
        studyFirstPager.initData(mData, mChildSelectIndex);
        mVideoPlayerController = studyFirstPager.getVideoPlayerController();
        studySecondPager.initData(mData);

        mVerticalAdapter.setView(studyFirstPager, studySecondPager);
        mVerticalAdapter.notifyDataSetChanged();

        //课程横划
        StudySyllabusView mStudySyllabusView = studyFirstPager.findViewById(R.id.view_syllabus);
        //课程横划点击回调
        mStudySyllabusView.setOnSyllabusSelect(new StudySyllabusView.OnSyllabusSelect() {
            @Override
            public void onSelect(int position, String code) {
                mCode = code;
                mChildSelectIndex = position;
                loadAgain();
            }
        });
        TextView mClassNumTv = mStudySyllabusView.findViewById(R.id.tv_class_num);
        mClassNumTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //课程页
                Intent intent = new Intent(CourseDetail.this, CourseList.class);
//                intent.putExtra(CourseDetail.EXTRA_GROUP, mGroupSelectIndex);
//                intent.putExtra(CourseDetail.EXTRA_CHILD, mChildSelectIndex);
                intent.putExtra(CourseList.EXTRA_FROM_STUDY, true);
                // TODO: 2018/12/13  
                intent.putExtra(CourseList.EXTRA_CODE, mCode);
                intent.putExtra(CourseList.EXTRA_TYPE, "2");
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
                    mCode = data.getStringExtra("code");
                    mChildSelectIndex = data.getIntExtra(EXTRA_CHILD, -1);
                    loadAgain();
                    break;
            }
        }
    }

    /**
     * 重新请求学习页数据
     */
    private void loadAgain() {
        mData.clear();
        mLoadAgain = true;
        loadManager.setLoading(v -> loadLessonInfo());
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
        if (viewPager.getShowPosition() > 0) {
            viewPager.setCurrentItem(0, true);
        } else
            super.onBackPressed();
    }
}
