package amodule._common.widgetlib;

/**
 * PackageName : amodule._common.widgetlib
 * Created by MrTrying on 2017/11/13 13:30.
 * E_mail : ztanzeyu@gmail.com
 */

public class HorizontalViewLibrary implements IWidgetLibrary {
    private static volatile HorizontalViewLibrary instance = null;

    private HorizontalViewLibrary(){}

    public static synchronized HorizontalViewLibrary of(){
        if(null == instance){
            instance = new HorizontalViewLibrary();
        }
        return instance;
    }

    @Override
    public final int findWidgetViewID(String style) {
        return NO_FIND_ID;
    }

    @Override
    public final int findWidgetLayoutID(String style) {
        return NO_FIND_ID;
    }
}
