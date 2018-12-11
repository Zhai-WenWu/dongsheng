package amodule.lesson.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import com.xiangha.R;

import java.util.List;
import java.util.Map;

public class StudySecondPager extends RelativeLayout {

    private View view;

    public StudySecondPager(Context context) {
        this(context, null);
    }

    public StudySecondPager(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
    }

    public StudySecondPager(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        view = LayoutInflater.from(context).inflate(R.layout.view_first_pager, this, true);
    }

    public void initData(Map<String, Map<String, String>>  mData) {
    }
}
