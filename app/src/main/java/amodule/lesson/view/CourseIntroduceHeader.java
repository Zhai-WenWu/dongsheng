package amodule.lesson.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.annimon.stream.Stream;
import com.annimon.stream.function.Consumer;
import com.xiangha.R;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import acore.tools.StringManager;
import acore.tools.ToolsDevice;

import static amodule.lesson.view.CourseIntroductionViewPager.TYPE_IMAGE;
import static amodule.lesson.view.CourseIntroductionViewPager.TYPE_KEY;
import static amodule.lesson.view.CourseIntroductionViewPager.TYPE_VIDEO;

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
        //TODO
        mViewPager = findViewById(R.id.course_viewpager);
        mViewPager.getLayoutParams().height = (int) (ToolsDevice.getWindowPx(context).widthPixels / 375f * 300);
        mCourseName = findViewById(R.id.course_name);
        mChapterText = findViewById(R.id.chapter_text);
        mLessonText = findViewById(R.id.lesson_text);
        mDurationText = findViewById(R.id.duration_text);
    }

    public void setData(Map<String, String> data) {
        //TODO
//        if (data == null) {
//            return;
//        }
        //设置viewpager数据

        List<Map<String, String>> images = StringManager.getListMapByJson(data.get("images"));
        for(int i=0;i<5;i++){
            Map<String,String> map = new HashMap<>();
            images.add(map);
        }
        Stream.of(images).forEach(value -> {
            value.put(TYPE_KEY, TYPE_IMAGE);
            value.put("img",value.remove(""));
            value.put("img","http://s3.cdn.xiangha.com/caipu/201801/2309/230946169944.jpg/MjgweDIyMA");
        });
        Map<String,String> videoMap = StringManager.getFirstMap(data.get("adVideo"));
//        if(!videoMap.isEmpty()){
            videoMap.put(TYPE_KEY,TYPE_VIDEO);
            videoMap.put("img","http://s3.cdn.xiangha.com/caipu/201801/2309/230946169944.jpg/MjgweDIyMA");
            videoMap.put("url","http://221.228.226.23/11/t/j/v/b/tjvbwspwhqdmgouolposcsfafpedmb/sh.yinyuetai.com/691201536EE4912BF7E4F1E2C67B8119.mp4");
            images.add(0,videoMap);
//        }
        mViewPager.setData(images);
        //TODO 设置标题数据
//        mCourseName.setText(checkStrNull(data.get("name")));
//        mChapterText.setText(checkStrNull(data.get("chapterText")));
//        mLessonText.setText(checkStrNull(data.get("lessonText")));
//        mDurationText.setText(checkStrNull(data.get("duration")));
    }

    private String checkStrNull(String text) {
        return TextUtils.isEmpty(text) ? "" : text;
    }
}
