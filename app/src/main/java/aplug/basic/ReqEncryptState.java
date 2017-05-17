package aplug.basic;

import java.net.URLEncoder;
import java.util.Map;

import acore.override.XHApplication;
import acore.tools.StringManager;

/**
 *
 */
public class ReqEncryptState {
    private String paramsData="";//传递的参数
    private void getData(){
        long time= System.currentTimeMillis();
        if(ReqEncryptCommon.getInstance().isencrypt()&&
                (ReqEncryptCommon.getInstance().getNowTime()+ReqEncryptCommon.getInstance().getTimeLength())>=time){
            if(reqEncryptCallBack!=null)reqEncryptCallBack.encryptSuccess(ReqEncryptCommon.getInstance().getData(paramsData));
        }else{
            getLoginApp();
        }
    }
    public void getLoginApp() {
        try {
            String url = StringManager.API_LOGIN_APP;
            String token = ReqEncryptCommon.getInstance().getToken();
            String params = "token=" + URLEncoder.encode(token, "utf-8");
            ReqInternet.in().doPost(url, params, new InternetCallback(XHApplication.in()) {
                @Override
                public void loaded(int flag, String url, Object object) {
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
                            if(reqEncryptCallBack!=null)reqEncryptCallBack.encryptSuccess(ReqEncryptCommon.getInstance().getData(paramsData));
                        }

                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * 接口数据回调
     */
    public interface ReqEncryptCallBack{
        public void encryptSuccess(String data);
    }
    private ReqEncryptCallBack reqEncryptCallBack;
    public void setReqEncryptCallBack(ReqEncryptCallBack reqEncryptCallBack,String paramsData){
        this.reqEncryptCallBack=reqEncryptCallBack;
        this.paramsData=paramsData;
        getData();
    }

}
