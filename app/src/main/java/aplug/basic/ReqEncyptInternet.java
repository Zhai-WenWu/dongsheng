package aplug.basic;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import acore.override.XHApplication;
import acore.override.helper.XHActivityManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import xh.basic.internet.InterCallback;
import xh.basic.internet.UtilInternet;

/**
 * 加密强求接口.-----不支持跨页面并发请求（未正确发起的请求，在切换页面会全部清除）。
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
    private int loginNum=0;
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
     * 加密策略，此处支持并发请求多个接口，
     * 需要注意：已存在listInternet中多请求会在，activity的onDestroy会全部清除
     * @param actionUrl
     * @param param
     * @param callback
     */
    public void doEncypt(String actionUrl, String param, InternetCallback callback){
        loginNum=0;
        //处理数据
        long time= System.currentTimeMillis();
        if(!isLoginSign && ReqEncryptCommon.getInstance().isencrypt()&&
                (ReqEncryptCommon.getInstance().getNowTime()+ReqEncryptCommon.getInstance().getTimeLength()*1000)>=time){
            setRequest(actionUrl,param,callback);
        }else{
            getLoginApp(actionUrl,param,callback);
        }
    }

    /**
     * 测试map字段加密
     * @param actionUrl
     * @param param
     * @param callback
     */
    public void doEncypt(String actionUrl, JSONObject jsonObject, InternetCallback callback){
        loginNum=0;
        //处理数据
        long time= System.currentTimeMillis();
        if(!isLoginSign && ReqEncryptCommon.getInstance().isencrypt()&&
                (ReqEncryptCommon.getInstance().getNowTime()+ReqEncryptCommon.getInstance().getTimeLength()*1000)>=time){
            String encryptparams=ReqEncryptCommon.getInstance().getData(jsonObject);
            callback.setEncryptparams(encryptparams);
            doGet(actionUrl,callback);

        }else{
//            getLoginApp(actionUrl,param,callback);
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

    /**
     * 处理请求。
     * @param actionUrl
     * @param param
     */
    private void setRequest(final String actionUrl, final String param, final InternetCallback callback){
        //处理请求
        String encryptparams=ReqEncryptCommon.getInstance().getData(param);
        InternetCallback internetCallback= new InternetCallback(XHApplication.in()) {
            @Override
            public void loaded(int flag, String url, Object object) {
//                flag=ReqInternet.REQ_CODE_ERROR;
//                object="4002";
                Log.i("zhangyujian","object::"+object);
                if(flag==ReqInternet.REQ_CODE_ERROR&& object != "" && isNumeric((String) object)){
                    int errorCode= Integer.parseInt((String) object);
                    if(errorCode>4000){//请求签名错误
                        getLoginApp(actionUrl,param,callback);
                    }else if(errorCode>2000){//不能救
                        Tools.showToast(XHApplication.in(),"请呼叫技术支持");
                    }
                }else{
                    callback.loaded(flag,url,object);
                }
            }
        };
        internetCallback.setEncryptparams(encryptparams);
        doGet(actionUrl,internetCallback);


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
            Log.i("zhangyujian","loginNum:::"+loginNum);
            if(loginNum>=3){//最多3次请求
                return;
            }
            ++loginNum;
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
                            Log.i("zhangyujian","size:::"+size);
                            for(int i=size-1;i>=0;i--){
                                Map<String,Object> mapurl=listInternet.get(i);
                                if(mapurl!=null) {
                                    if(mapurl.get("callback")!=null){
                                        InternetCallback callback = (InternetCallback) mapurl.get("callback");
                                        if(callback!=null) {
                                            Log.i("zhangyujian","mapurl.get(\"url\"):::"+mapurl.get("url"));
                                            setRequest((String)mapurl.get("url"),(String)mapurl.get("param"),callback);
                                        }
                                    }

                                }
                            }
                            clearListIntenert();
                        }

                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 清楚全部接口
     */
    public void clearListIntenert(){
        if(listInternet!=null&&listInternet.size()>0)listInternet.clear();
    }
    public static boolean isNumeric(String str) {
        for (int i=0; i<str.length();i++){
            if(!Character.isDigit(str.charAt(i))){
                return false;
            }
        }
        return true;
    }

}
