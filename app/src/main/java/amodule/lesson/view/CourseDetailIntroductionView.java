package amodule.lesson.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiangha.R;

import java.util.Map;

public class CourseDetailIntroductionView extends RelativeLayout {
    public CourseDetailIntroductionView(Context context, Map<String, String> desc) {
        this(context, null, desc);
    }

    public CourseDetailIntroductionView(Context context, @Nullable AttributeSet attrs, Map<String, String> desc) {
        this(context, attrs, 0, desc);
    }

    public CourseDetailIntroductionView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, Map<String, String> desc) {
        super(context, attrs, defStyleAttr);
        View view = LayoutInflater.from(context).inflate(R.layout.view_course_abstract_class, this, true);
        TextView subTitleTv = view.findViewById(R.id.tv_subtitle);
        TextView infoTv = view.findViewById(R.id.tv_info);
        subTitleTv.setText(desc.get("subTitle"));
        infoTv.setText(desc.get("info"));
    }
}
