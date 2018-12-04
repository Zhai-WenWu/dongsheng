package amodule.lesson.view;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiangha.R;

public class CourseDetailTitleView extends RelativeLayout {

    private TextView mCourseTitleTv;
    private TextView mCourseSubtitleTv;

    public CourseDetailTitleView(Context context) {
        this(context, null);
    }

    public CourseDetailTitleView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CourseDetailTitleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View view = LayoutInflater.from(context).inflate(R.layout.view_course_title, this, true);
        mCourseTitleTv = findViewById(R.id.tv_course_title);
        mCourseSubtitleTv = findViewById(R.id.tv_course_subtitle);
        initData();
    }

    private void initData() {
        StringBuilder stringBuilder = new StringBuilder("VIP·共");
        stringBuilder.append("33");
        stringBuilder.append("节·");
        stringBuilder.append("23");
        stringBuilder.append("人学");
        SpannableString spannableString = new SpannableString(stringBuilder);
        ForegroundColorSpan colorSpan = new ForegroundColorSpan(Color.parseColor("#fa273b"));
        spannableString.setSpan(colorSpan, 0, 3, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        mCourseSubtitleTv.setText(spannableString);
    }
}
