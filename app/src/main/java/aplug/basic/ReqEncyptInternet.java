package aplug.basic;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import acore.override.XHApplication;
import acore.tools.StringManager;
import acore.tools.Tools;
import xh.basic.internet.InterCallback;
import xh.basic.internet.UtilInternet;

/**
 * 加密强求接口.
 * 该类中只有 doEncypt方法可用
 */

public class ReqEncyptInternet extends UtilInternet {
    private static ReqEncyptInternet instance=null;
    private static Context initContext=null;
    private boolean isLoginSign=false;//是否进行loginApp,获取sign
    private ArrayList<Map<String,Object>> listInternet= new ArrayList<>();
    private ReqEncyptInternet(Context context) {
        super(context);
    }
    public static ReqEncyptInternet init(Context context) {
        initContext=context;
        return in();
    }
    public static ReqEncyptInternet in() {
        if(instance==null)
            instance=new ReqEncyptInternet(initContext);
        return instance;
    }

    /**
     * 加密策略
     * @param actionUrl
     * @param param
     * @param callback
     */
    public void doEncypt(String actionUrl, String param, InternetCallback callback){
        //处理数据
        long time= System.currentTimeMillis();
        if(ReqEncryptCommon.getInstance().isencrypt()&&
                (ReqEncryptCommon.getInstance().getNowTime()+ReqEncryptCommon.getInstance().getTimeLength()*1000)>=time){
            String encryptparams=ReqEncryptCommon.getInstance().getData(param);
            callback.setEncryptparams(encryptparams);
            doGet(actionUrl,callback);
        }else{
            getLoginApp(actionUrl,param,callback);
        }
    }

    /**
     * 加密策略,只执行AEC加密
     * @param actionUrl
     * @param param
     * @param callback
     */
    public void doEncyptAEC(String actionUrl, String param, InternetCallback callback){
        if(!TextUtils.isEmpty(param)){
            Map<String,String> map =  StringManager.getMapByString(param,"&","=");
            String json= Tools.map2Json(map);
            String data=ReqEncryptCommon.getInstance().encrypt(json,ReqEncryptCommon.password);
            callback.setEncryptparams(data);
        }
        doGet(actionUrl,callback);


    }
    @Override
    public void doGet(String url, InterCallback callback) {
        url = StringManager.replaceUrl(url);
        super.doGet(url, callback);
    }

    @Override
    public void doPost(String actionUrl, String param, InterCallback callback) {
		 actionUrl = StringManager.replaceUrl(actionUrl);
        super.doPost(actionUrl, param, callback);
    }

    @Override
    public void doPost(String actionUrl, LinkedHashMap<String, String> map, InterCallback callback) {
        actionUrl = StringManager.replaceUrl(actionUrl);
        super.doPost(actionUrl, map, callback);
    }

    public void getLoginApp( String actionUrl, String actionParam, InternetCallback actionCallback) {
        try {
            HashMap<String,Object> map= new HashMap<>();
            map.put("url",actionUrl);
            map.put("param",actionParam);
            map.put("callback",actionCallback);
            listInternet.add(map);
            if(isLoginSign){//当前已经请求
                return;//不处理
            }
            isLoginSign=true;
            String url = StringManager.API_LOGIN_APP;
            String token = ReqEncryptCommon.getInstance().getToken();
            final String params = "token=" + URLEncoder.encode(token, "utf-8");
            ReqInternet.in().doPost(url, params, new InternetCallback(XHApplication.in()) {
                @Override
                public void loaded(int flag, String url, Object object) {
                    isLoginSign=false;
                    if (flag >= ReqInternet.REQ_OK_STRING) {
                        Map<String, String> map = StringManager.getFirstMap(object);
                        if (map.containsKey("gy")) {
                            ReqEncryptCommon.getInstance().setNowTime(System.currentTimeMillis());
                            String GY = ReqEncryptCommon.getInstance().decrypt(map.get("gy"), ReqEncryptCommon.password);
                            ReqEncryptCommon.getInstance().setGY(GY);
                            String sign = map.get("sign");
                            ReqEncryptCommon.getInstance().setSign(sign);
                            if(map.containsKey("aliveTime")){
                                String timeLength=map.get("aliveTime");
                                ReqEncryptCommon.getInstance().setTimeLength(Long.parseLong(timeLength));
                            }
                            ReqEncryptCommon.getInstance().setIsencrypt(true);
                            //加盟数据并处理数据

                            int size= listInternet.size();
                            for(int i=size-1;i>=0;i--){
                                Map<String,Object> mapurl=listInternet.get(i);
                                if(mapurl!=null) {
                                    String encryptparams = ReqEncryptCommon.getInstance().getData((String) mapurl.get("param"));
                                    if(mapurl.get("callback")!=null){
                                        InternetCallback callback = (InternetCallback) mapurl.get("callback");
                                        if(callback!=null) {
                                            callback.setEncryptparams(encryptparams);
                                            doGet((String) mapurl.get("url"), callback);
                                        }
                                    }

                                }
                            }
                            listInternet.clear();
//                            String encryptparams=ReqEncryptCommon.getInstance().getData(actionParam);
//                            actionCallback.setEncryptparams(encryptparams);
//                            doGet(actionUrl,actionCallback);
                        }

                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void clearListIntenert(){
        if(listInternet!=null&&listInternet.size()>0)listInternet.clear();
    }
}
