package amodule._common.widgetlib;

/**
 * PackageName : amodule._common.widgetlib
 * Created by MrTrying on 2017/11/13 13:28.
 * E_mail : ztanzeyu@gmail.com
 */

public class FunctionNavLibrary extends AbsWidgetLibrary {

    private static volatile FunctionNavLibrary instance = null;

    private FunctionNavLibrary(){}

    public static synchronized FunctionNavLibrary of(){
        if(null == instance){
            instance = new FunctionNavLibrary();
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
