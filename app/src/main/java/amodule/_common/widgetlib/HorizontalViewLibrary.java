package amodule._common.widgetlib;

import android.text.TextUtils;

import com.xiangha.R;

/**
 * PackageName : amodule._common.widgetlib
 * Created by MrTrying on 2017/11/13 13:30.
 * E_mail : ztanzeyu@gmail.com
 */

public class HorizontalViewLibrary implements IWidgetLibrary {
    private static volatile HorizontalViewLibrary instance = null;

    private HorizontalViewLibrary() {
    }

    public static synchronized HorizontalViewLibrary of() {
        if (null == instance) {
            instance = new HorizontalViewLibrary();
        }
        return instance;
    }

    @Override
    public final int findWidgetViewID(String style) {
        if (TextUtils.isEmpty(style)) return NO_FIND_ID;
        switch (style) {
            case "1":
            case "2":
            case "3":
            case "4":
            case "5":
            case "6":
                return R.id.horizontal_recyclerview_1;
            default:
                return NO_FIND_ID;
        }

    }

    @Override
    public final int findWidgetLayoutID(String style) {
        if (TextUtils.isEmpty(style)) return NO_FIND_ID;
        switch (style) {
            case "1":
            case "2":
            case "3":
            case "4":
            case "5":
            case "6":
                return R.layout.widget_horizontal;
            default:
                return NO_FIND_ID;
        }
    }
}
