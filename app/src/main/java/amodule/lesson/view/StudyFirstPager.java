package amodule.lesson.view;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiangha.R;

import java.util.List;
import java.util.Map;

import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import third.video.VideoPlayerController;

public class StudyFirstPager extends RelativeLayout {

    private StudySyllabusView mStudySyllabusView;
    private LinearLayout mBtnLayout;
    private LinearLayout mBtnBottomLayout;
    private VideoPlayerController mVideoPlayerController;
    private Activity mActivity;
    private RelativeLayout videoLayout;
    private int mChildSelectIndex;
    private RelativeLayout mVideoRv;
    private View mAlphaView;

    public LinearLayout getBtnLayout() {
        return mBtnLayout;
    }

    public StudyFirstPager(Context context) {
        this(context, null);
    }

    public VideoPlayerController getVideoPlayerController() {
        return mVideoPlayerController;
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
        mVideoRv = findViewById(R.id.rv_video);
        mAlphaView = findViewById(R.id.view_alpha);
    }

    public void initData(Map<String, Map<String, String>> mData, int childSelectIndex) {
        this.mChildSelectIndex = childSelectIndex;
        Map<String, String> video = StringManager.getFirstMap(mData.get("lessonInfo").get("video"));
        mVideoPlayerController = new VideoPlayerController(mActivity, videoLayout, video.get("img"));
        int urlWidth = Integer.parseInt(video.get("width"));
        int urlHeight = Integer.parseInt(video.get("height"));
        String ratio = video.get("ratio");
        int DW = ToolsDevice.getWindowPx(mActivity).widthPixels;

        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) videoLayout.getLayoutParams();
        if (ratio.equals("1:1") || urlHeight == urlWidth) {//正方形
            mAlphaView.setVisibility(GONE);
            layoutParams.height = DW;
        } else if (urlHeight < urlWidth) {//长方形
            mAlphaView.setVisibility(GONE);
            double scale = ((double) 9) / 16;
            layoutParams.height = (int) (DW * scale);
            mVideoPlayerController.hideFullScreen();
            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        } else {//全屏幕
            mAlphaView.setVisibility(VISIBLE);
            layoutParams.addRule(RelativeLayout.ABOVE, R.id.ll_btn_top);
            mStudySyllabusView.setChangeTvColer(true);
            mVideoPlayerController.hideFullScreen();
            mVideoPlayerController.setBottomContainerBottomMargin(Tools.getDimen(mActivity, R.dimen.dp_145));
        }

        mVideoPlayerController.setVideoUrl(video.get("url"));
        mVideoPlayerController.setOnPlayingCompletionListener(new VideoPlayerController.OnPlayingCompletionListener() {
            @Override
            public void onPlayingCompletion() {
                onVideoFinish.videoFinish();
            }
        });

        List<Map<String, String>> labelDataList = StringManager.getListMapByJson(mData.get("lessonInfo").get("labelData"));
        if (mBtnLayout != null && mBtnLayout.getChildCount() > 0) {
            mBtnLayout.removeAllViews();
        }
        if (mBtnBottomLayout != null && mBtnBottomLayout.getChildCount() > 0) {
            mBtnBottomLayout.removeAllViews();
        }
        for (int i = 0; i < labelDataList.size(); i++) {
            View lableView = createLableView(labelDataList.get(i), mBtnLayout, i, true);
            mBtnLayout.addView(lableView);
            View lableViewBottom = createLableView(labelDataList.get(i), mBtnBottomLayout, i, false);
            mBtnBottomLayout.addView(lableViewBottom);
        }
        mStudySyllabusView.setData(mData.get("syllabusInfo"), mChildSelectIndex);
    }


    private View createLableView(Map<String, String> stringStringMap, LinearLayout parent,
                                 int i, boolean isTop) {
        View view;
        if (isTop) {
            view = LayoutInflater.from(mActivity).inflate(R.layout.view_study_top_lable_item, parent, false);
            View horLine = findViewById(R.id.hor_line);
            if (i != 0 && horLine != null) {
                horLine.setVisibility(VISIBLE);
            }
        } else {
            view = LayoutInflater.from(mActivity).inflate(R.layout.view_study_bottom_lable_item, parent, false);
            View verLine = view.findViewById(R.id.lable_line);
            if (i == 0 && verLine != null) {
                verLine.setVisibility(View.GONE);
            }
        }
        TextView textView = view.findViewById(R.id.label_text);
        String title = stringStringMap.get("title");
        textView.setText(title);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickBottomView.clickView(i);
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

    private OnVideoFinish onVideoFinish;

    public void setOnVideoFinish(OnVideoFinish onVideoFinish) {
        this.onVideoFinish = onVideoFinish;
    }

    public interface OnVideoFinish {
        void videoFinish();
    }
}
