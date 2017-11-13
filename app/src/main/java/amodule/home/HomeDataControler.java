package amodule.home;

import android.os.Handler;
import android.os.Looper;

import aplug.basic.InternetCallback;


/**
 * Description : //TODO
 * PackageName : amodule.home
 * Created by MrTrying on 2017/11/13 14:51.
 * Author : mrtrying
 * E_mail : ztanzeyu@gmail.com
 */

public class HomeDataControler {

    //保存缓存
    public void saveCacheHomeData(){

    }

    //读取缓存数据
    public void loadCacheHomeData(InternetCallback callback){
        final Handler handler = new Handler(Looper.getMainLooper(),
                msg -> {
                    //TODO
                    return false;
                });
        new Thread(() -> {

        }).start();
    }

    //获取服务端首页数据
    public void loadServiceHomeData(InternetCallback callback){

    }

    //获取服务端Feed流数据
    public void loadServiceFeedData(InternetCallback callback){

    }

}
