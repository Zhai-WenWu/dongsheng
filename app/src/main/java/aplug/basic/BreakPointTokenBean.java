package aplug.basic;

import android.text.TextUtils;
import android.util.Log;

import java.io.Serializable;

/**
 * 断点上传token bean对象
 */

public class BreakPointTokenBean implements Serializable{
    private String type="";//当前类型 img,
    private String key="";//key代表数据：：服务端返回的数据
    private String token ="";
    private int expiredTime=0;//过期的时长---(以秒为单位)
    private long startTime=0;//过期时间的开始计时时间
    private String domian="";
    private boolean reqstate=false;//是否在请求中

    public boolean isReqstate() {
        return reqstate;
    }

    public void setReqstate(boolean reqstate) {
        this.reqstate = reqstate;
    }

    public BreakPointTokenBean(String type){
        setType(type);
    }
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getExpiredTime() {
        return expiredTime;
    }

    public void setExpiredTime(int expiredTime) {
        this.expiredTime = expiredTime;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public String getDomian() {
        return domian;
    }

    public void setDomian(String domian) {
        this.domian = domian;
    }

    /**
     * 获取当前token 是否过期状态
     */
    public boolean getTokenState(){
        long nowTime= System.currentTimeMillis()-startTime;
        if(expiredTime<=0)return true;//过期时间为0
        if(TextUtils.isEmpty(key)||TextUtils.isEmpty(token))return true;//数据为null

        if(nowTime>=expiredTime*1000){
           //YLKLog.i("qiniu","token已过期");
            return true;
        }else{
            return false;
        }
    }
}
