package amodule._common.widgetlib;

import android.text.TextUtils;

import static amodule._common.widgetlib.IWidgetLibrary.NO_FIND_ID;

/**
 * PackageName : amodule._common
 * Created by MrTrying on 2017/11/10 19:13.
 * E_mail : ztanzeyu@gmail.com
 */

public class AllWeightLibrary {

    private static volatile AllWeightLibrary instance = null;

    private AllWeightLibrary(){}

    public static synchronized AllWeightLibrary of(){
        if(null == instance){
            instance = new AllWeightLibrary();
        }
        return instance;
    }

    public final int findWidgetViewID(String type,String style) {
        if(TextUtils.isEmpty(type) || TextUtils.isEmpty(style)){
            return NO_FIND_ID;
        }
        switch (type){
            case "1":
                return BannerLibrary.of().findWidgetViewID(style);
            case "2":
                return FunctionNavLibrary.of().findWidgetViewID(style);
            case "3":
                return HorizontalViewLibrary.of().findWidgetViewID(style);
            default:
                return NO_FIND_ID;
        }
    }

    public final int findWidgetLayoutID(String type,String style) {
        if(TextUtils.isEmpty(type) || TextUtils.isEmpty(style)){
            return NO_FIND_ID;
        }
        switch (type){
            case "1":
                return BannerLibrary.of().findWidgetViewID(style);
            case "2":
                return FunctionNavLibrary.of().findWidgetViewID(style);
            case "3":
                return HorizontalViewLibrary.of().findWidgetViewID(style);
            default:
                return NO_FIND_ID;
        }
    }
}
