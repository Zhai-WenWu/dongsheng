package amodule.lesson.view;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiangha.R;

import java.util.Map;

public class StudyTitleView extends RelativeLayout {

    private TextView mCourseTitleTv;
    private TextView mCourseSubtitleTv;
    private Map<String, String> mData;

    public StudyTitleView(Context context) {
        this(context, null);
    }

    public StudyTitleView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StudyTitleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.view_course_title,this,true);
        mCourseTitleTv = findViewById(R.id.tv_course_title);
        mCourseSubtitleTv = findViewById(R.id.tv_course_subtitle);
    }

    public void setTitleData(String titleData) {
        mCourseTitleTv.setText(titleData);
    }

    public void setSubTitleData(String subTitleData) {
        StringBuilder stringBuilder = new StringBuilder("VIP·共");
        stringBuilder.append(subTitleData);
        SpannableString spannableString = new SpannableString(stringBuilder);
        ForegroundColorSpan colorSpan = new ForegroundColorSpan(Color.parseColor("#fa273b"));
        spannableString.setSpan(colorSpan, 0, 3, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        mCourseSubtitleTv.setText(spannableString);
    }
}
