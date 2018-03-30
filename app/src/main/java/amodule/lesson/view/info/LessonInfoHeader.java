package amodule.lesson.view.info;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import java.util.Map;

import amodule._common.delegate.IBindMap;

/**
 * Description :
 * PackageName : amodule.vip.view
 * Created by tanze on 2018/3/29 17:11.
 * e_mail : ztanzeyu@gmail.com
 */
public class LessonInfoHeader extends RelativeLayout implements IBindMap{
    public LessonInfoHeader(Context context) {
        super(context);
    }

    public LessonInfoHeader(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LessonInfoHeader(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setData(Map<String, String> stringStringMap) {

    }
}
