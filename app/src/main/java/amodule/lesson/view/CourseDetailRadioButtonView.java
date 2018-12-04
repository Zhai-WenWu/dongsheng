package amodule.lesson.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;

import com.xiangha.R;

import java.util.Map;

import amodule.lesson.activity.CourseDetail;

public class CourseDetailRadioButtonView extends RelativeLayout {

    private View view;
    private RadioGroup radioGroup;

    public CourseDetailRadioButtonView(Context context) {
        this(context, null);
    }

    public CourseDetailRadioButtonView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CourseDetailRadioButtonView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        view = LayoutInflater.from(context).inflate(R.layout.view_course_radiobutton, this, true);
        initView();
    }

    public RadioGroup getView() {
        return radioGroup;
    }

    public void setClickIndex(int index) {
        RadioButton child = (RadioButton) view.findViewById(index);
        child.setChecked(true);
    }

    private void initView() {
        radioGroup = view.findViewById(R.id.radiogroup);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (onCheckedChangedListener!=null) {
                    onCheckedChangedListener.onClickChange(checkedId);
                }

            }
        });
    }

    public void addOnCheckedChangedListener(OnCheckedChangedListener onCheckedChangedListener) {
        this.onCheckedChangedListener = onCheckedChangedListener;
    }

    public OnCheckedChangedListener onCheckedChangedListener;

    public interface OnCheckedChangedListener {
        void onClickChange(int checkedId);
    }
}
