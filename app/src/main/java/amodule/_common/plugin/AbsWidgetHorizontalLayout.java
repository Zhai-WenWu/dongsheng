package amodule._common.plugin;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import java.util.List;

import amodule._common.delegate.IBindData;

/**
 * PackageName : amodule._common.plugin
 * Created by MrTrying on 2017/11/10 19:09.
 * E_mail : ztanzeyu@gmail.com
 */

public abstract class AbsWidgetHorizontalLayout<T> extends LinearLayout implements IBindData<T> {
    public AbsWidgetHorizontalLayout(Context context) {
        this(context,null);
    }

    public AbsWidgetHorizontalLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public AbsWidgetHorizontalLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOrientation(HORIZONTAL);
        //初始化
        initialize();
    }

    public abstract void initialize();

    public abstract void addTopView(List<T> array);

    public abstract void addBottom(List<T> array);

}
