package amodule._common.widgetlib;

import android.text.TextUtils;

import com.xiangha.R;

/**
 * PackageName : amodule._common.widgetlib
 * Created by MrTrying on 2017/11/13 13:28.
 * E_mail : ztanzeyu@gmail.com
 */

public class FunctionNavLibrary implements IWidgetLibrary {

    private static volatile FunctionNavLibrary instance = null;

    private FunctionNavLibrary() {
    }

    public static synchronized FunctionNavLibrary of() {
        if (null == instance) {
            instance = new FunctionNavLibrary();
        }
        return instance;
    }

    @Override
    public final int findWidgetViewID(String type) {
        if (TextUtils.isEmpty(type)) return NO_FIND_ID;
        switch (type) {
            case "1":
                return R.id.fun_nav_1;
            case "2":
                return R.id.fun_nav_2;
            default:
                return NO_FIND_ID;
        }
    }

    @Override
    public final int findWidgetLayoutID(String type) {
        if (TextUtils.isEmpty(type)) return NO_FIND_ID;
        switch (type) {
            case "1":
                return R.layout.widget_func_nav1;
            case "2":
                return R.layout.widget_func_nav2;
            default:
                return NO_FIND_ID;
        }
    }
}
