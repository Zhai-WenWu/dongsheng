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
 * 加密强求接口.-----不支持跨页面并发请求（未正确发起的请求，在切换页面会全部清除）。
 * 该类中只有 doEncypt方法可用
 */

public class ReqEncyptInternet extends UtilInternet {
    private static ReqEncyptInternet instance=null;
    private boolean isLoginSign=false;//是否进行loginApp,获取sign
    private ArrayList<Map<String,Object>> listInternet= new ArrayList<>();
    private ReqEncyptInternet() {
        super();
    }
    private int loginNum=0;
    private long now_login_time;
    public static ReqEncyptInternet init() {
        return in();
    }
    public static ReqEncyptInternet in() {
        if(instance==null)
            instance=new ReqEncyptInternet();
        return instance;
    }
    public void doGetEncypt(String actionUrl,InternetCallback callback){
        LinkedHashMap<String,String> map = new LinkedHashMap<>();
        map.put("mode","doGet");
        doEncypt(actionUrl,map,callback);
    }
    public void doGetEncypt(String actionUrl,String param,InternetCallback callback){
        LinkedHashMap<String,String> map = new LinkedHashMap<>();
        if(!TextUtils.isEmpty(param)){
            map=StringManager.getMapByString(param,"&","=");
        }
        map.put("mode","doGet");
        doEncypt(actionUrl,map,callback);
    }
    public void doGetEncypt(String actionUrl, LinkedHashMap<String,String> map, InternetCallback callback){
        if(map == null) map = new LinkedHashMap<>();
        map.put("mode","doGet");
        doEncypt(actionUrl,map,callback);
    }

    public void doPostEncypt(String actionUrl, LinkedHashMap<String,String> map, InternetCallback callback){
        if(map == null) map = new LinkedHashMap<>();
        map.put("mode","doPost");
        doEncypt(actionUrl,map,callback);
    }
    public void doPostEncypt(String actionUrl, String param, InternetCallback callback){
        LinkedHashMap<String,String> map = new LinkedHashMap<>();
        if(!TextUtils.isEmpty(param)){
            map=StringManager.getMapByString(param,"&","=");
        }
        map.put("mode","doPost");
        doEncypt(actionUrl,map,callback);
    }

    /**
     * 加密策略，此处支持并发请求多个接口，
     * 需要注意：已存在listInternet中多请求会在，activity的onDestroy会全部清除
     * @param actionUrl
     * @param param
     * @param callback
     */
    public void doEncypt(String actionUrl, String param, InternetCallback callback){
        LinkedHashMap<String,String> map = new LinkedHashMap<>();
        if(!TextUtils.isEmpty(param)){
             map=StringManager.getMapByString(param,"&","=");
        }
        doEncypt(actionUrl,map,callback);
    }

    /**
     * 加密策略：传递参数使用，map,
     * @param actionUrl
     * @param map
     * @param callback
     */
    public void doEncypt(String actionUrl, LinkedHashMap<String,String> map, InternetCallback callback){
        loginNum=0;
        //处理数据
        long time= System.currentTimeMillis();
        if(!isLoginSign && ReqEncryptCommon.getInstance().isencrypt()&&
                (ReqEncryptCommon.getInstance().getNowTime()+ReqEncryptCommon.getInstance().getTimeLength()*1000)>=time){
            setRequest(actionUrl,map,callback);
        }else{
            getLoginApp(actionUrl,map,callback);
        }
    }

    /**

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
        doPost(actionUrl,"",callback);


    }

    /**
     * 处理请求。
     * @param actionUrl
     * @param map
     */
    private void setRequest(final String actionUrl, final LinkedHashMap <String,String> map, final InternetCallback callback){
        long now_request_time=System.currentTimeMillis();
        int time= (int) (now_request_time-now_login_time)/1000;
        //处理当前sign，获取要传输的sign
        String sign_no= ReqEncryptCommon.decrypt(ReqEncryptCommon.getInstance().getSign(),ReqEncryptCommon.password);
        String sign_yes= ReqEncryptCommon.encrypt(sign_no+"_"+time,ReqEncryptCommon.password);
        String now_parms="";

//        Log.i("zhangyujian","sign_no::"+sign_no);
//        Log.i("zhangyujian","sign_yes::"+sign_yes);
//        Log.i("zhangyujian","time::"+time);
//        Log.i("zhangyujian","now_parms::"+now_parms);
        String encryptparams=ReqEncryptCommon.getInstance().getData(now_parms,sign_yes);
        InternetCallback internetCallback= new InternetCallback() {

            @Override
            public void getPower(int flag, String url, Object obj) {
                if(callback != null)
                    callback.getPower(flag, url, obj);
            }

            @Override
            public void loaded(int flag, String url, Object object) {
//                Log.i("zhangyujian","flag:" + flag + "   object::"+object);
                if(flag==ReqInternet.REQ_CODE_ERROR && object != "" && isNumeric((String) object)){
                    int errorCode= Integer.parseInt((String) object);
                    if(errorCode>4000){//请求签名错误
                        getLoginApp(actionUrl,map,callback);
                    }else if(errorCode>2000){//不能救
                        Tools.showToast(XHApplication.in(),"请呼叫技术支持");
                        callback.loaded(flag,url,object);
                    }else{
                        callback.loaded(flag,url,object);
                    }

                }else{
                    callback.loaded(flag,url,object);
                }
            }
        };
        internetCallback.setEncryptparams(encryptparams);
        if(map.containsKey("mode")&&"doGet".equals(map.get("mode"))){
            String getUrl=actionUrl;
            map.remove("mode");
            if(map.size()>0){
                int index=0;
                for(String str:map.keySet()){
                    getUrl+= ((index==0 ?"?":"&")+str+"="+map.get(str));
                    ++index;
                }
            }
            doGet(getUrl,internetCallback);
        }else {//目前把post和无指定请求类型都当post处理
            if(map.containsKey("mode")){
                map.remove("mode");
            }
            doPost(actionUrl, map, internetCallback);
        }


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

    public void getLoginApp( String actionUrl, LinkedHashMap<String,String> mapParam, InternetCallback actionCallback) {
        try {
            Log.i("zhangyujian","loginNum:::"+loginNum);
            if(loginNum>=3){//最多3次请求
                return;
            }
            ++loginNum;
            if(mapParam!=null&&actionCallback!=null) {
                HashMap<String, Object> map = new HashMap<>();
                map.put("url", actionUrl);
                map.put("param", mapParam);
                map.put("callback", actionCallback);
                listInternet.add(map);
            }
            if(isLoginSign){//当前已经请求
                return;//不处理
            }
            isLoginSign=true;
            String url = StringManager.API_LOGIN_APP;
            String token = ReqEncryptCommon.getInstance().getToken();
            final String params = "token=" + URLEncoder.encode(token, "utf-8");
            ReqInternet.in().doPost(url, params, new InternetCallback() {
                @Override
                public void loaded(int flag, String url, Object object) {
//                    Log.i("zhangyujian","getLoginApp() falg:" + flag + "  url:" + url + "  object:" + object);
                    isLoginSign=false;
                    if (flag >= ReqInternet.REQ_OK_STRING) {
                        //loginApp的的时间戳。
                        now_login_time=System.currentTimeMillis();
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

                            handlerEncryptParam();
                            //加盟数据并处理数据
                            int size= listInternet.size();
//                            Log.i("zhangyujian","size:::"+size);
                            for(int i=size-1;i>=0;i--){
                                Map<String,Object> mapurl=listInternet.get(i);
                                if(mapurl!=null) {
                                    if(mapurl.get("callback")!=null){
                                        InternetCallback callback = (InternetCallback) mapurl.get("callback");
                                        if(callback!=null) {
//                                            Log.i("zhangyujian","mapurl.get(\"url\"):::"+mapurl.get("url"));
                                            setRequest((String)mapurl.get("url"),(LinkedHashMap)mapurl.get("param"),callback);
                                        }
                                    }

                                }
                            }
                            clearListIntenert();
                        }

                    }else{
                        //加盟数据并处理数据
                        int size= listInternet.size();
//                        Log.i("zhangyujian","size:::"+size);
                        for(int i=size-1;i>=0;i--){
                            Map<String,Object> mapurl=listInternet.get(i);
                            if(mapurl!=null) {
                                if(mapurl.get("callback")!=null){
                                    InternetCallback callback = (InternetCallback) mapurl.get("callback");
                                    if(callback!=null) {
//                                        Log.i("zhangyujian","mapurl.get(\"url\"):::"+mapurl.get("url"));
                                        setRequest((String)mapurl.get("url"),(LinkedHashMap)mapurl.get("param"),callback);
                                    }
                                }

                            }
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
        if(TextUtils.isEmpty(str)){
            return false;
        }
        for (int i=0; i<str.length();i++){
            if(!Character.isDigit(str.charAt(i))){
                return false;
            }
        }
        return true;
    }

    public SignEncyptCallBck signEncyptCallBck;

    public interface  SignEncyptCallBck{
        public void getSignEncyntParam(String encryptparam);
    }

    /**
     * 获取加密key
     * @param signEncyptCallBck
     */
    public void getSignEncryptParam(SignEncyptCallBck signEncyptCallBck){
        loginNum=0;
        this.signEncyptCallBck=signEncyptCallBck;
        //处理数据
        long time= System.currentTimeMillis();
        if(!isLoginSign && ReqEncryptCommon.getInstance().isencrypt()&&
                (ReqEncryptCommon.getInstance().getNowTime()+ReqEncryptCommon.getInstance().getTimeLength()*1000)>=time){
            handlerEncryptParam();
        }else{
            getLoginApp("",null,null);
        }
    }

    /**
     * 处理加密回调
     */
    private void handlerEncryptParam(){
        if(signEncyptCallBck!=null){
            String encryptparam=getEncryptParam();
            if(signEncyptCallBck!=null&&!TextUtils.isEmpty(encryptparam)){
                signEncyptCallBck.getSignEncyntParam(encryptparam);
                signEncyptCallBck=null;
            }
        }
    }
    /**
     * 获取当前加密值
     * @return
     */
    public String getEncryptParam(){
        long now_request_time=System.currentTimeMillis();
        int time= (int) (now_request_time-now_login_time)/1000;
        //处理当前sign，获取要传输的sign
        String sign_no= ReqEncryptCommon.decrypt(ReqEncryptCommon.getInstance().getSign(),ReqEncryptCommon.password);
        String sign_yes= ReqEncryptCommon.encrypt(sign_no+"_"+time,ReqEncryptCommon.password);
        String now_parms="";

        String encryptparams=ReqEncryptCommon.getInstance().getData(now_parms,sign_yes);
        //当前了然过期，需要重新请求验证
        return encryptparams;
    }

    /**
     * 获取当前加密值
     * @return
     */
    public String getEncryptParamNew(){
        long timeNow= System.currentTimeMillis();
        if(!isLoginSign && ReqEncryptCommon.getInstance().isencrypt()&&
                (ReqEncryptCommon.getInstance().getNowTime()+ReqEncryptCommon.getInstance().getTimeLength()*1000)>=timeNow) {
            long now_request_time = System.currentTimeMillis();
            int time = (int) (now_request_time - now_login_time) / 1000;
            //处理当前sign，获取要传输的sign
            String sign_no = ReqEncryptCommon.decrypt(ReqEncryptCommon.getInstance().getSign(), ReqEncryptCommon.password);
            String sign_yes = ReqEncryptCommon.encrypt(sign_no + "_" + time, ReqEncryptCommon.password);
            String now_parms = "";

            String encryptparams = ReqEncryptCommon.getInstance().getData(now_parms, sign_yes);
            //当前了然过期，需要重新请求验证
            return encryptparams;
        }else{

            return "";
        }
    }
//    public void handlerloginAppSync(){
//        try {
//            Log.i("zhangyujian","oginAppSync 开始:");
//            if (isLoginSign) {//当前已经请求
//                return;//不处理
//            }
//            isLoginSign = true;
//            String url = StringManager.api_circleFind + "?page=1" ;;
////            String token = ReqEncryptCommon.getInstance().getToken();
////            LinkedHashMap<String,String> maps= new LinkedHashMap<String,String>();
////            maps.put("token",URLEncoder.encode(token, "utf-8"));
//            ReqInternet.in().doPostSync(url, new LinkedHashMap<String,String>(), new InternetCallback(XHApplication.in()) {
//                @Override
//                public void loaded(int flag, String url, Object object) {
//                    Log.i("zhangyujian","getLoginApp() falg:" + flag + "  url:" + url + "  object:" + object);
//                    isLoginSign=false;
//                    if (flag >= ReqInternet.REQ_OK_STRING) {
//                        //loginApp的的时间戳。
//                        now_login_time = System.currentTimeMillis();
//                        Map<String, String> map = StringManager.getFirstMap(object);
//                        if (map.containsKey("gy")) {
//                            ReqEncryptCommon.getInstance().setNowTime(System.currentTimeMillis());
//                            String GY = ReqEncryptCommon.getInstance().decrypt(map.get("gy"), ReqEncryptCommon.password);
//                            ReqEncryptCommon.getInstance().setGY(GY);
//                            String sign = map.get("sign");
//                            ReqEncryptCommon.getInstance().setSign(sign);
//                            if (map.containsKey("aliveTime")) {
//                                String timeLength = map.get("aliveTime");
//                                ReqEncryptCommon.getInstance().setTimeLength(Long.parseLong(timeLength));
//                            }
//                            ReqEncryptCommon.getInstance().setIsencrypt(true);
//                        }
//                    }
//                }
//            });
////            Log.i("zhangyujian","data:::"+data);
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//    }
}
