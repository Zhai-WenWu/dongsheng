package amodule._common.widget;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;

import java.util.Map;

import amodule._common.delegate.IBindMap;

/**
 * PackageName : amodule._common.weight
 * Created by MrTrying on 2017/11/13 09:36.
 * E_mail : ztanzeyu@gmail.com
 */

public class TestLayout extends ConstraintLayout implements IBindMap{
    public TestLayout(Context context) {
        super(context);
    }

    public TestLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TestLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setData(Map<String, String> stringStringMap) {

    }
}
