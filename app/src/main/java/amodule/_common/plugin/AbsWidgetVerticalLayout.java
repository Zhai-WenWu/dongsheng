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

public abstract class AbsWidgetVerticalLayout<T> extends LinearLayout implements IBindData<T>{
    public AbsWidgetVerticalLayout(Context context) {
        this(context,null);
    }

    public AbsWidgetVerticalLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public AbsWidgetVerticalLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOrientation(VERTICAL);
        //初始化
        initialize();
    }

    public abstract void initialize();

    public abstract void updateTopView(List<T> array);

    public abstract void updateBottom(List<T> array);

}
