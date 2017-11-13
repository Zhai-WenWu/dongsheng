package amodule._common.widgetlib;

/**
 * PackageName : amodule._common.helper
 * Created by MrTrying on 2017/11/13 12:45.
 * E_mail : ztanzeyu@gmail.com
 */

public abstract class AbsWidgetLibrary {
    final int NO_FIND_ID = 0;

    public abstract int findWidgetViewID(String type);

    public abstract int findWidgetLayoutID(String type);
}
