package amodule._common.widgetlib;

/**
 * PackageName : amodule._common
 * Created by MrTrying on 2017/11/10 19:13.
 * E_mail : ztanzeyu@gmail.com
 */

public class AllWeightLibrary extends AbsWidgetLibrary{

    private static volatile AllWeightLibrary instance = null;

    private AllWeightLibrary(){}

    public static synchronized AllWeightLibrary of(){
        if(null == instance){
            instance = new AllWeightLibrary();
        }
        return instance;
    }

    @Override
    public final int findWidgetViewID(String type) {
        return NO_FIND_ID;
    }

    @Override
    public final int findWidgetLayoutID(String type) {
        return NO_FIND_ID;
    }
}
