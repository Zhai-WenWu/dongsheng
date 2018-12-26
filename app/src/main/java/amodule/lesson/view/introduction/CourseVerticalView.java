package amodule.lesson.view.introduction;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.annimon.stream.Stream;
import com.annimon.stream.function.Consumer;
import com.xiangha.R;

import java.util.List;
import java.util.Map;

import acore.logic.stat.intefaces.OnClickListenerStat;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.widget.rcwidget.RCConstraintLayout;

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
    private boolean isOneLesson = false;
    private String lessonNum;
    private boolean syllabusItemCanClick;

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
        View view = LayoutInflater.from(context).inflate(LAYOUT_ID, this, false);
        addView(view);
        mTitleText = findViewById(R.id.title);
        mLinearLayoutCompat = findViewById(R.id.course_list_layout);
        RCConstraintLayout shadow_layout = findViewById(R.id.shadow_layout);
        LinearLayout root = findViewById(R.id.root_layout);
        root.setPadding(root.getPaddingLeft() - shadow_layout.getPaddingLeft(),
                root.getPaddingTop(),
                root.getPaddingRight() - shadow_layout.getPaddingRight(),
                shadow_layout.getPaddingBottom());
        mTitleText.setPadding(shadow_layout.getPaddingLeft(), 0,
                0, Tools.getDimen(context, R.dimen.dp_12) - shadow_layout.getPaddingTop());
    }

    public void setData(Map<String, String> data) {
        if (data == null || data.isEmpty()) {
            setVisibility(GONE);
            return;
        }
        mTitleText.setText(checkStrNull(data.get("title")));
        //课程数据设置
        List<Map<String, String>> lessonList = StringManager.getListMapByJson(data.get("chapterList"));
        if (!lessonList.isEmpty()) {
            //如果只有一章，则显示课的数据列表
            int chapterNum = Tools.parseIntOfThrow(data.get("chapterNum"), 1);
            lessonNum = String.valueOf(chapterNum);
            if (chapterNum == 1) {
                lessonList = StringManager.getListMapByJson(lessonList.get(0).get("lessonList"));
                syllabusItemCanClick = lessonList.size() > 4;
                isOneLesson = lessonList.size() <= 1;
                Stream.of(lessonList).forEach(value -> value.put("subTitleRight",value.remove("subTitle")));
            }
        }
        if (mLinearLayoutCompat != null) {
            mLinearLayoutCompat.removeAllViews();
        }
        if (isOneLesson) {
            setVisibility(GONE);
            return;
        }
        //缺少章节数
        boolean hasFooter = lessonList.size() > 4;
        final int length = hasFooter ? 4 : lessonList.size();
        for (int i = 0; i < length; i++) {
            Map<String, String> value = lessonList.get(i);
            createCourseItem(i, value, mLinearLayoutCompat);
        }
        if (hasFooter) {
            addFooterView(data, mLinearLayoutCompat);
        }

        setVisibility(VISIBLE);
    }

    private void addFooterView(Map<String, String> data, LinearLayoutCompat parent) {
        View footView = LayoutInflater.from(getContext()).inflate(R.layout.view_course_vertical_footer, parent, false);
        footView.setOnClickListener(v -> {
            if (mFooterClickListener != null) {
                mFooterClickListener.onClick(v);
            }
        });
        parent.addView(footView);
        TextView see_all = footView.findViewById(R.id.see_all);
        see_all.setText(TextUtils.isEmpty(lessonNum) ? "查看全部 " : "查看全部（" + lessonNum + "节）");
    }

    private void createCourseItem(int position, Map<String, String> data, LinearLayoutCompat parent) {
        if (data == null || data.isEmpty()) {
            return;
        }
        View itemView = LayoutInflater.from(getContext()).inflate(R.layout.view_course_vertical_item, parent, false);
        TextView titleText = itemView.findViewById(R.id.title);
        titleText.setText(data.get("title"));
        TextView subTitleText = itemView.findViewById(R.id.sub_title);
        subTitleText.setText(data.get("subTitle"));
        TextView subTitleRightText = itemView.findViewById(R.id.sub_title_right);
        subTitleRightText.setText(data.get("subTitleRight"));
        ImageView right_arrow = itemView.findViewById(R.id.right_arrow);
        right_arrow.setVisibility(TextUtils.isEmpty(data.get("subTitleRight"))?VISIBLE:GONE);
        itemView.setOnClickListener(new OnClickListenerStat() {
            @Override
            public void onClicked(View v) {
                if (mOnItemClickCallback != null&&syllabusItemCanClick) {
                    mOnItemClickCallback.onItemClick(position, data);
                }
            }
        });
        parent.addView(itemView);
    }

    public void setLessonNum(String lessonNum) {
        this.lessonNum = lessonNum;
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(isOneLesson ? GONE : visibility);
    }

    private String checkStrNull(String text) {
        return TextUtils.isEmpty(text) ? "" : text;
    }

    OnClickListener mFooterClickListener;

    public void setFooterOnClickListener(OnClickListener listener) {
        mFooterClickListener = listener;
    }

    OnItemClickCallback mOnItemClickCallback;

    public void setOnItemClickCallback(OnItemClickCallback onItemClickCallback) {
        mOnItemClickCallback = onItemClickCallback;
    }

    public interface OnItemClickCallback {
        void onItemClick(int position, Map<String, String> data);
    }
}
