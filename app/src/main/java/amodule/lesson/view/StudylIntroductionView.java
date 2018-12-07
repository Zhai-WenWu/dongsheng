package amodule.lesson.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiangha.R;

import org.eclipse.jetty.util.StringUtil;

import java.util.Map;

public class StudylIntroductionView extends RelativeLayout {

    private TextView infoTv;

    public StudylIntroductionView(Context context) {
        this(context, null);
    }

    public StudylIntroductionView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StudylIntroductionView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View view = LayoutInflater.from(context).inflate(R.layout.view_course_abstract_class, this, true);
        infoTv = view.findViewById(R.id.tv_info);
    }

    public void setData(String desc) {
        infoTv.setText(desc);
    }

    public void setVideoTitle(String videoTitle) {
        if (!TextUtils.isEmpty(videoTitle)) {
            infoTv.setText(videoTitle);
        } else {
            infoTv.setVisibility(GONE);
        }
    }
}
