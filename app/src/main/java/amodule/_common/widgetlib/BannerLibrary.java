package amodule._common.widgetlib;

import android.text.TextUtils;

import com.xiangha.R;

/**
 * PackageName : amodule._common.widgetlib
 * Created by MrTrying on 2017/11/13 13:27.
 * E_mail : ztanzeyu@gmail.com
 */

public class BannerLibrary implements IWidgetLibrary {

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
        if(TextUtils.isEmpty(type)) return NO_FIND_ID;
        switch (type){
            case "1":
                return R.id.banner_view_1;
            default:
                return NO_FIND_ID;
        }
    }

    @Override
    public final int findWidgetLayoutID(String type) {
        if(TextUtils.isEmpty(type)) return NO_FIND_ID;
        switch (type){
            case "1":
                return R.layout.widget_banner;
            default:
                return NO_FIND_ID;
        }
    }
}
