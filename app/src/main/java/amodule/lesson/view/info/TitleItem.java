package amodule.lesson.view.info;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import java.util.Map;

import amodule._common.delegate.IBindMap;

/**
 * Description : //TODO
 * PackageName : amodule.lesson.view.info
 * Created by tanze on 2018/3/30 11:06.
 * e_mail : ztanzeyu@gmail.com
 */
public class TitleItem extends LinearLayout implements IBindMap{
    public TitleItem(Context context) {
        super(context);
    }

    public TitleItem(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public TitleItem(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setData(Map<String, String> stringStringMap) {

    }
}
