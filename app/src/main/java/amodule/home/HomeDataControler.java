package amodule.home;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import acore.tools.FileManager;
import acore.tools.StringManager;
import amodule.main.activity.MainHomePage;
import amodule.main.bean.HomeModuleBean;
import aplug.basic.InternetCallback;
import aplug.basic.ReqEncyptInternet;

/**
 * Description : //TODO
 * PackageName : amodule.home
 * Created by MrTrying on 2017/11/13 14:51.
 * Author : mrtrying
 * E_mail : ztanzeyu@gmail.com
 */

public class HomeDataControler {

    private final String CACHE_PATH = "homeDataCache";
    private MainHomePage mActivity;
    private String backUrl, nextUrl;
    private HomeModuleBean mHomeModuleBean;
    private ArrayList<Map<String, String>> mData = new ArrayList<>();

    public HomeDataControler(MainHomePage activity) {
        this.mActivity = activity;
        mHomeModuleBean = new HomeModuleControler().getHomeModuleByType(activity, null);
    }

    //读取缓存数据
    public void loadCacheHomeData(InternetCallback callback) {
        final Handler handler = new Handler(Looper.getMainLooper(),
                msg -> {
                    callback.loaded(ReqEncyptInternet.REQ_OK_STRING, "", msg.obj);
                    return false;
                });
        new Thread(() -> {
            String hoemDataStr = FileManager.readFile(CACHE_PATH).toString().trim();
            if (!TextUtils.isEmpty(hoemDataStr)) {
                Message msg = handler.obtainMessage(0, hoemDataStr);
                handler.sendMessage(msg);
            }
        }).start();
    }

    public void saveCacheHomeData(String data) {
        FileManager.scynSaveFile(CACHE_PATH, data, false);
    }

    //获取服务端首页数据
    public void loadServiceHomeData(@Nullable InternetCallback callback) {
        String url = StringManager.API_HOMEPAGE_6_0;
        LinkedHashMap<String, String> params = new LinkedHashMap<>();
        ReqEncyptInternet.in().doEncypt(url, params, callback);
    }

    public void loadServiceTopData(InternetCallback callback) {
        String url = StringManager.API_RECOMMEND_TOP;
        ReqEncyptInternet.in().doEncyptAEC(url, "", callback);
    }

    //获取服务端Feed流数据
    public void loadServiceFeedData(boolean refresh, InternetCallback callback) {

    }

    public ArrayList<Map<String, String>> getData() {
        return mData;
    }

    public HomeModuleBean getHomeModuleBean() {
        return mHomeModuleBean;
    }

    public void setBackUrl(String backUrl) {
        this.backUrl = backUrl;
    }

    public void setNextUrl(String nextUrl) {
        this.nextUrl = nextUrl;
    }
}
