package amodule.lesson.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.annimon.stream.Stream;
import com.annimon.stream.function.Consumer;
import com.xiangha.R;

import java.util.List;
import java.util.Map;

import acore.tools.StringManager;

/**
 * Description :
 * PackageName : amodule.lesson.view
 * Created by mrtrying on 2018/12/3 15:59.
 * e_mail : ztanzeyu@gmail.com
 */
public class CourseVerticalView extends FrameLayout {
    final int LAYOUT_ID = R.layout.view_course_vertical_view;
    private TextView mTitleText;
    private LinearLayoutCompat mLinearLayoutCompat;
    public CourseVerticalView(@NonNull Context context) {
        super(context);
        initialize(context, null, 0);
    }

    public CourseVerticalView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs, 0);
    }

    public CourseVerticalView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context, attrs, defStyleAttr);
    }

    private void initialize(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        LayoutInflater.from(context).inflate(LAYOUT_ID, this);
        mTitleText = findViewById(R.id.title);
        mLinearLayoutCompat = findViewById(R.id.course_list_layout);

    }

    public void setData(Map<String, String> data) {
        if (data == null || data.isEmpty()) {
            setVisibility(GONE);
            return;
        }
        mTitleText.setText(checkStrNull(data.get("title")));
        //课程数据设置
        List<Map<String, String>> courseList = StringManager.getListMapByJson(data.get("info"));
        if (courseList.isEmpty()) {
            setVisibility(GONE);
            return;
        }
        if (mLinearLayoutCompat != null) {
            mLinearLayoutCompat.removeAllViews();
        }
        //缺少章节数
        Stream.of(courseList).forEach(value -> {
            View itemView = createCourseItem(value);
            if(itemView != null){
                mLinearLayoutCompat.addView(itemView);
            }
        });

        setVisibility(VISIBLE);
    }

    @Nullable
    private View createCourseItem(Map<String,String> data){
        if(data == null || data.isEmpty()){
            return null;
        }
        View itemView = LayoutInflater.from(getContext()).inflate(R.layout.view_course_vertical_item,null);
        TextView titleText = itemView.findViewById(R.id.title);
        titleText.setText(data.get("title"));
        TextView subTitleText = itemView.findViewById(R.id.sub_title);
        subTitleText.setText(data.get("subTitle"));
        return itemView;
    }


    private String checkStrNull(String text) {
        return TextUtils.isEmpty(text) ? "" : text;
    }
}
