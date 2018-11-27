package acore.logic;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.widget.Toast;

import java.util.LinkedHashMap;
import java.util.Map;

import acore.observer.ObserverManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import amodule._common.conf.FavoriteTypeEnum;
import aplug.basic.InternetCallback;
import aplug.basic.ReqEncyptInternet;

import static acore.observer.ObserverManager.NOTIFY_FAVORITE;

/**
 * Description : 收藏功能辅助类
 * PackageName : acore.logic
 * Created by MrTrying on 2017/11/3 11:21.
 * Author : mrtrying
 * E_mail : ztanzeyu@gmail.com
 */

public class FavoriteHelper {

    private volatile static FavoriteHelper mInstance;

    private FavoriteHelper() {
    }

    public static FavoriteHelper instance() {
        if (null == mInstance) {
            synchronized (FavoriteHelper.class) {
                if (null == mInstance) {
                    mInstance = new FavoriteHelper();
                }
            }
        }
        return mInstance;
    }

    /**
     * 获取收藏状态
     * @param context 上下文
     * @param code 内容code
     * @param type 对应类型
     * @param callback 回调
     */
    public void getFavoriteStatus(Context context, @NonNull String code, @NonNull FavoriteTypeEnum type,
                                  @Nullable final FavoriteStatusCallback callback) {
        LinkedHashMap<String, String> params = new LinkedHashMap<>();
        params.put("code", code);
        params.put("type", type.getType());
        ReqEncyptInternet.in().doEncypt(StringManager.API_GET_FAVORITE_STATUS, params,
                new InternetCallback() {
                    @Override
                    public void loaded(int i, String s, Object o) {
                        if(i >= ReqEncyptInternet.REQ_OK_STRING){
                            Map<String,String> map = StringManager.getFirstMap(o);
                            final String state = map.get("state");
                            if(null != callback){
                                if(TextUtils.isEmpty(state))
                                    callback.onFailed();
                                else
                                    callback.onSuccess("2".equals(state));
                            }
                        }else{
                            if(null != callback)
                                callback.onFailed();
                        }
                    }
                });
    }

    /**
     * 设置收藏状态
     * @param context 上下文
     * @param code 内容code
     * @param type 对应类型
     * @param typeName 内容标题
     * @param callback 回调
     */
    public void setFavoriteStatus(Context context, @NonNull String code, String typeName,@NonNull FavoriteTypeEnum type,
                                  @Nullable final FavoriteStatusCallback callback) {
        if(!ToolsDevice.isNetworkAvailable(context)){
            Toast.makeText(context, "请检查网络设置", Toast.LENGTH_SHORT).show();
            return;
        }
        LinkedHashMap<String, String> params = new LinkedHashMap<>();
        params.put("code", code);
        params.put("type", type.getType());
        if(!TextUtils.isEmpty(typeName))
            params.put("typeName", typeName);
        ReqEncyptInternet.in().doEncypt(StringManager.API_SET_FAVORITE_STATUS, params,
                new InternetCallback() {
                    @Override
                    public void loaded(int i, String s, Object o) {
                        if(i >= ReqEncyptInternet.REQ_OK_STRING){
                            Map<String,String> map = StringManager.getFirstMap(o);
                            final String state = map.get("state");
                            if(null != callback){
                                if(TextUtils.isEmpty(state))
                                    callback.onFailed();
                                else{
                                    callback.onSuccess("2".equals(state));
                                    Tools.showToast(context,"2".equals(state)?"收藏成功":"取消收藏");
                                }
                            }
                            ObserverManager.getInstance().notify(NOTIFY_FAVORITE,null, map);
                        }else{
                            if(null != callback)
                                callback.onFailed();
                            ObserverManager.getInstance().notify(NOTIFY_FAVORITE,null, null);
                        }
                    }
                });
    }

    public interface FavoriteStatusCallback{
        void onSuccess(boolean isFav);
        void onFailed();
    }

}
