package amodule.vip;

import java.util.LinkedHashMap;

import acore.override.XHApplication;
import aplug.basic.InternetCallback;
import aplug.basic.ReqEncyptInternet;

/**
 * Description :
 * PackageName : amodule.vip
 * Created by mrtrying on 2017/12/19 17:02:00.
 * e_mail : ztanzeyu@gmail.com
 */
public class VipDataController {

    public void loadVIPButtonData(HandlerDataCallback callback){
//        String url = "";
//        LinkedHashMap<String,String> params = new LinkedHashMap<>();
//        ReqEncyptInternet.in().doEncypt(url, params, new InternetCallback(XHApplication.in()) {
//            @Override
//            public void loaded(int i, String s, Object o) {
//                if(i>=ReqEncyptInternet.REQ_OK_STRING){
//                    if(null != callback) callback.handlerData(o);
//                }
//            }
//        });
    }

    public interface HandlerDataCallback{
        void handlerData(Object obj);
    }
}
