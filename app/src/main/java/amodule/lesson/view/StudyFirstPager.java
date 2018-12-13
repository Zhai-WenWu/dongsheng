package amodule.lesson.view;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiangha.R;

import java.util.List;
import java.util.Map;

import acore.tools.StringManager;
import acore.tools.ToolsDevice;
import third.video.VideoPlayerController;

public class StudyFirstPager extends RelativeLayout {

    private StudySyllabusView mStudySyllabusView;
    private LinearLayout mBtnLayout;
    private LinearLayout mBtnBottomLayout;
    private VideoPlayerController mVideoPlayerController;
    //    private int mGroupSelectIndex = 0;
//    private int mChildSelectIndex = -1;
    private Activity mActivity;
    private RelativeLayout videoLayout;
    private int mGroupSelectIndex;
    private int mChildSelectIndex;

    public LinearLayout getmBtnLayout() {
        return mBtnLayout;
    }

    public StudyFirstPager(Context context) {
        this(context, null);
    }

    public StudyFirstPager(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StudyFirstPager(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mActivity = (Activity) context;
        LayoutInflater.from(context).inflate(R.layout.view_first_pager, this, true);
        mBtnLayout = findViewById(R.id.ll_btn_top);
        mBtnBottomLayout = findViewById(R.id.ll_btn_bottom);
        mStudySyllabusView = findViewById(R.id.view_syllabus);
        videoLayout = findViewById(R.id.video_layout);
    }

    public void initData(Map<String, Map<String, String>> mData, int groupSelectIndex, int childSelectIndex) {
        this.mGroupSelectIndex = groupSelectIndex;
        this.mChildSelectIndex = childSelectIndex;
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) videoLayout.getLayoutParams();
        int type = 0;
        int H;
        switch (type) {
            case 0://长方形
                double i = ((double) 211) / 375;
                int DW = ToolsDevice.getWindowPx(mActivity).widthPixels;
                H = (int) (DW * i);
                layoutParams.height = H;
                layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
                break;
            case 1://正方形
                H = ToolsDevice.getWindowPx(mActivity).widthPixels;
                layoutParams.height = H;
                break;
            case 2://全屏
                layoutParams.addRule(RelativeLayout.ABOVE, R.id.ll_btn_top);
                mStudySyllabusView.setChangeTvColer(true);
                break;
        }
        videoLayout.setLayoutParams(layoutParams);

        // TODO: 2018/12/7
        mVideoPlayerController = new VideoPlayerController(mActivity, videoLayout, "https://ws1.sinaimg.cn/large/0065oQSqgy1fxno2dvxusj30sf10nqcm.jpg");
        mVideoPlayerController.setVideoUrl("http://221.228.226.23/11/t/j/v/b/tjvbwspwhqdmgouolposcsfafpedmb/sh.yinyuetai.com/691201536EE4912BF7E4F1E2C67B8119.mp4");


        List<Map<String, String>> labelDataList = StringManager.getListMapByJson(mData.get("lessonInfo").get("labelData"));
        if (mBtnLayout != null && mBtnLayout.getChildCount() > 0) {
            mBtnLayout.removeAllViews();
        }
        if (mBtnBottomLayout != null && mBtnBottomLayout.getChildCount() > 0) {
            mBtnBottomLayout.removeAllViews();
        }
        for (int i = 0; i < labelDataList.size(); i++) {
            View lableView = createLableView(labelDataList.get(i), mBtnLayout, i,false);
            mBtnLayout.addView(lableView);
            View lableViewBottom = createLableView(labelDataList.get(i), mBtnBottomLayout, i,true);
            mBtnBottomLayout.addView(lableViewBottom);
        }
//        initCourseListData(mData.get("syllabusInfo"));
        mStudySyllabusView.setData(mData.get("syllabusInfo"), mGroupSelectIndex, mChildSelectIndex);
    }


    private View createLableView(Map<String, String> stringStringMap, LinearLayout parent, int i, boolean isTop) {
        View view = LayoutInflater.from(mActivity).inflate(R.layout.view_study_bottom_lable_item, parent, false);
        if (i == 0) {
            view.findViewById(R.id.lable_line).setVisibility(View.GONE);
        }
        TextView textView = view.findViewById(R.id.label_text);
        if (!isTop){
            textView.setTextColor(getResources().getColor(R.color.ysf_black_333333));
        }
        String title = stringStringMap.get("title");
        textView.setText(title);
        // TODO: 2018/12/7
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickBottomView.clickView(i);
                switch (title) {
                    case "学习要点":
//                        Intent StudyPointIntent = new Intent(CourseDetail.this, StudyPoint.class);
//                        StudyPointIntent.putExtra(StudyPoint.EXTRA_CODE, mCode);
//                        StudyPointIntent.putExtra(StudyIntroduction.EXTRA_TITLE, title);
//                        startActivity(StudyPointIntent);
                        break;
                    case "常见问题":
//                        Intent StudyAskIntent = new Intent(CourseDetail.this, StudyAsk.class);
//                        StudyAskIntent.putExtra(StudyIntroduction.EXTRA_TITLE, title);
//                        startActivity(StudyAskIntent);
                        break;
                    case "课程简介":
//                        Intent StudyIntroductionIntent = new Intent(CourseDetail.this, StudyIntroduction.class);
//                        StudyIntroductionIntent.putExtra(StudyIntroduction.EXTRA_TITLE, title);
//                        StudyIntroductionIntent.putExtra(StudyIntroduction.EXTRA_DESC, mTopInfoMap.get("desc"));
//                        startActivity(StudyIntroductionIntent);
                        break;
                }
            }
        });
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) view.getLayoutParams();
        params.width = 0;
        params.weight = 1;
        return view;
    }

    private OnClickBottomView onClickBottomView;

    public void setOnClickBottomView(OnClickBottomView onClickBottomView) {
        this.onClickBottomView = onClickBottomView;
    }

    public interface OnClickBottomView {
        void clickView(int i);
    }
}
