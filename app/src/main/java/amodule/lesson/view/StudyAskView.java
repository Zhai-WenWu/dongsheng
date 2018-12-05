package amodule.lesson.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;

import com.xiangha.R;

public class StudyAskView extends RelativeLayout {
    public StudyAskView(Context context) {
        this(context,null);
    }

    public StudyAskView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public StudyAskView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.view_course_ask, this, true);
    }
}
