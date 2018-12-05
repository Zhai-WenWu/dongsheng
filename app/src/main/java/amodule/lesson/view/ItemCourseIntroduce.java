package amodule.lesson.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.xiangha.R;

import java.util.Map;
import java.util.Objects;

/**
 * Description :
 * PackageName : amodule.lesson.view
 * Created by mrtrying on 2018/12/3 16:10.
 * e_mail : ztanzeyu@gmail.com
 */
public class ItemCourseIntroduce extends FrameLayout {
    final int LAYOUT_ID = R.layout.item_course_introduce;

    private TextView mTitleText,mDescText;

    public ItemCourseIntroduce(@NonNull Context context) {
        super(context);
        initialize(context,null,0);
    }

    public ItemCourseIntroduce(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs,0);
    }

    public ItemCourseIntroduce(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context, attrs, defStyleAttr);
    }

    private void initialize(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        LayoutInflater.from(context).inflate(LAYOUT_ID,this);
        mTitleText = findViewById(R.id.title);
        mDescText = findViewById(R.id.desc);
    }

    public void setData(Map<String,String> data){
        if(data == null || data.isEmpty()){
            setVisibility(GONE);
            return;
        }
        innerSetData(data);
        setVisibility(VISIBLE);
    }

    private void innerSetData(Map<String,String> data) {
        mTitleText.setText(checkStrNull(data.get("title")));
        mDescText.setText(checkStrNull(data.get("info")));
    }

    private String checkStrNull(String text){
        return TextUtils.isEmpty(text) ? "" : text;
    }

}
