package amodule._common.widgetlib;

/**
 * PackageName : amodule._common.widgetlib
 * Created by MrTrying on 2017/11/13 13:30.
 * E_mail : ztanzeyu@gmail.com
 */

public class ScrollLibrary extends AbsWidgetLibrary {
    private static volatile ScrollLibrary instance = null;

    private ScrollLibrary(){}

    public static synchronized ScrollLibrary of(){
        if(null == instance){
            instance = new ScrollLibrary();
        }
        return instance;
    }

    @Override
    public final int findWidgetViewID(String type) {
        return 0;
    }

    @Override
    public final int findWidgetLayoutID(String type) {
        return 0;
    }
}
