package third.umeng;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.umeng.onlineconfig.OnlineConfigAgent;

import org.json.JSONObject;

import acore.override.XHApplication;

/**
 * Description :
 * PackageName : third.umeng
 * Created on 2017/11/22 13:56.
 * Author : mrtrying
 * E_mail : ztanzeyu@gmail.com
 */

public class OnlineConfigControler {
    /*--------------------------------------------- 获取参数的KEY  ---------------------------------------------*/
    public static final String KEY_HOMENOTICE = "homeNotice";
    /*--------------------------------------------- KEY END ---------------------------------------------*/

    //单例对象
    private static OnlineConfigControler mInstance;
    //是否加载过
    private volatile boolean isLoaded = false;

    private OnlineConfigControler(){
        //更新数据
        updateData(null);
    }

    /***/
    public synchronized static OnlineConfigControler getInstance(){
        if(null == mInstance){
            mInstance = new OnlineConfigControler();
        }
        return mInstance;
    }

    /**
     *
     * @param callback
     */
    public void getJsonObject(@NonNull OnLoadJsonObjectCallback callback){
        if(null == XHApplication.in())
            return;
        if(isLoaded){
            getConfigParamsJson(callback);
        } else {
            updateData(callback);
        }
    }

    /**
     *
     * @param key
     * @param callback
     */
    public void getConfigByKey(String key,@NonNull OnLoadConfigByKeyCallbcak callback){
        getJsonObject(jsonObject -> {
            if(null == callback
                    || null == jsonObject
                    || null == key)
                return;
            callback.onLoad(jsonObject.optString(key));
        });
    }

    /**
     * 刷新数据
     * @param callback
     */
    public void updateData(@Nullable OnLoadJsonObjectCallback callback){
        if(null == XHApplication.in())
            return;
        OnlineConfigAgent.getInstance().setOnlineConfigListener(jsonObject -> {
            isLoaded = true;
            getConfigParamsJson(callback);
        });
        OnlineConfigAgent.getInstance().updateOnlineConfig(XHApplication.in());
    }

    /**
     * 获取所有在线参数
     * @param callback
     */
    private void getConfigParamsJson(@Nullable OnLoadJsonObjectCallback callback){
        if(null == callback || null == XHApplication.in()) return;
        JSONObject newestJsonObject = OnlineConfigAgent.getInstance().getConfigParamsJson(XHApplication.in());
        post(() -> callback.onLoad(newestJsonObject));
    }

    private void post(Runnable runnable){
        new Handler(Looper.getMainLooper()).post(runnable);
    }

    /*--------------------------------------------- interface ---------------------------------------------*/

    public interface OnLoadJsonObjectCallback {
        void onLoad(JSONObject jsonObject);
    }

    public interface OnLoadConfigByKeyCallbcak{
        void onLoad(String value);
    }
}
