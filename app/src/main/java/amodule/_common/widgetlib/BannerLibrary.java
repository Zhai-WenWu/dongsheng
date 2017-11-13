package amodule._common.widgetlib;

/**
 * PackageName : amodule._common.widgetlib
 * Created by MrTrying on 2017/11/13 13:27.
 * E_mail : ztanzeyu@gmail.com
 */

public class BannerLibrary extends AbsWidgetLibrary {

    private static volatile BannerLibrary instance = null;

    private BannerLibrary(){}

    public static synchronized BannerLibrary of(){
        if(null == instance){
            instance = new BannerLibrary();
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
