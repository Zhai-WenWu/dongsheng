package amodule.vip;

import java.util.LinkedHashMap;

import acore.override.XHApplication;
import acore.tools.StringManager;
import aplug.basic.InternetCallback;
import aplug.basic.ReqEncyptInternet;

/**
 * Description :
 * PackageName : amodule.vip
 * Created by mrtrying on 2017/12/19 17:02:00.
 * e_mail : ztanzeyu@gmail.com
 */
public class VipDataController {

    public static final String KEY_BTN_SHOW = "vipButtonIsShow";
    public static final String KEY_BTN_DATA = "vipButton";

    public void loadVIPButtonData(HandlerDataCallback callback){
        String url = StringManager.API_SCHOOL_VIPBUTTON;
        LinkedHashMap<String,String> params = new LinkedHashMap<>();
        ReqEncyptInternet.in().doEncypt(url, params, new InternetCallback(XHApplication.in()) {
            @Override
            public void loaded(int i, String s, Object o) {
                if(i>=ReqEncyptInternet.REQ_OK_STRING){
                    if(null != callback) callback.handlerData(o);
                }
            }
        });
    }

    public interface HandlerDataCallback{
        void handlerData(Object obj);
    }
}
