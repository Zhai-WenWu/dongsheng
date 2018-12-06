package amodule.lesson.view.introduction;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiangha.R;

import java.util.List;
import java.util.Map;

import acore.tools.StringManager;
import acore.tools.ToolsDevice;

import static amodule.lesson.view.introduction.CourseIntroductionViewPager.TYPE_IMAGE;
import static amodule.lesson.view.introduction.CourseIntroductionViewPager.TYPE_KEY;
import static amodule.lesson.view.introduction.CourseIntroductionViewPager.TYPE_VIDEO;

/**
 * Description :
 * PackageName : amodule.lesson.view
 * Created by mrtrying on 2018/12/4 17:22.
 * e_mail : ztanzeyu@gmail.com
 */
public class CourseIntroduceHeader extends RelativeLayout {
    final int LAYOUT_ID = R.layout.view_course_introduce_header;
    private CourseIntroductionViewPager mViewPager;
    private TextView mCourseName, mChapterText, mLessonText, mDurationText;

    private int videoHeight=0;

    public CourseIntroduceHeader(Context context) {
        super(context);
        initialize(context, null, 0);
    }

    public CourseIntroduceHeader(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs, 0);
    }

    public CourseIntroduceHeader(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context, attrs, defStyleAttr);
    }

    private void initialize(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        LayoutInflater.from(context).inflate(LAYOUT_ID, this);
        mViewPager = findViewById(R.id.course_viewpager);
        videoHeight = (int) (ToolsDevice.getWindowPx(context).widthPixels / 375f * 300);
        mViewPager.getLayoutParams().height = videoHeight;
        mCourseName = findViewById(R.id.course_name);
        mChapterText = findViewById(R.id.chapter_text);
        mLessonText = findViewById(R.id.lesson_text);
        mDurationText = findViewById(R.id.duration_text);
    }

    public void setData(Map<String, String> data) {
        if (data == null) {
            return;
        }
        //设置viewpager数据
        List<Map<String, String>> images = StringManager.getListMapByJson(data.get("images"));
        for(Map<String,String> value:images){
            value.put(TYPE_KEY, TYPE_IMAGE);
            value.put("img",value.remove(""));
        }
        Map<String,String> videoMap = StringManager.getFirstMap(data.get("adVideo"));
        if(!videoMap.isEmpty()){
            videoMap.put(TYPE_KEY,TYPE_VIDEO);
            images.add(0,videoMap);
        }
        mViewPager.setData(images);
        mCourseName.setText(checkStrNull(data.get("name")));
        mChapterText.setText(checkStrNull(data.get("chapterText")));
        mLessonText.setText(checkStrNull(data.get("lessonText")));
        mDurationText.setText(checkStrNull(data.get("duration")));
    }

    public void onResume() {
        if (mViewPager != null)
            mViewPager.onResume();
    }

    public void onPause() {
        if (mViewPager != null)
            mViewPager.onPause();
    }

    public void onDestroy() {
        if (mViewPager != null)
            mViewPager.onDestroy();
    }

    public boolean onBackPressed() {
        return mViewPager != null && mViewPager.onBackPressed();
    }

    private String checkStrNull(String text) {
        return TextUtils.isEmpty(text) ? "" : text;
    }

    public int getVideoHeight() {
        return videoHeight;
    }
}
