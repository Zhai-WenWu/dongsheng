package amodule.lesson.view.info;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import java.util.Map;

import amodule._common.delegate.IBindMap;

/**
 * Description :
 * PackageName : amodule.lesson.view.info
 * Created by tanze on 2018/3/30 11:05.
 * e_mail : ztanzeyu@gmail.com
 */
public class ImageItem extends LinearLayout implements IBindMap {
    public ImageItem(Context context) {
        super(context);
    }

    public ImageItem(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ImageItem(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setData(Map<String, String> stringStringMap) {

    }
}
