package aplug.basic;

import android.annotation.SuppressLint;
import android.content.Context;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import acore.tools.StringManager;
import xh.basic.internet.InterCallback;
import xh.basic.internet.UtilInternet;
import xh.basic.internet.UtilInternetImg;

public class ReqInternet extends UtilInternet {
    @SuppressLint("StaticFieldLeak")
    private static ReqInternet instance = null;
    @SuppressLint("StaticFieldLeak")
    private static Context initContext = null;

    private ReqInternet(Context context) {
        super(context);
    }

    public static ReqInternet init(Context context) {
        initContext = context;
        return in();
    }

    public static ReqInternet in() {
        if (instance == null)
            instance = new ReqInternet(initContext);
        return instance;
    }

    /**
     * 单独获取标准header头
     *
     * @param context
     *
     * @return
     */
    public Map<String, String> getHeader(Context context) {
        InternetCallback callback = new InternetCallback(context) {
            @Override
            public void loaded(int flag, String url, Object msg) {
            }
        };
        Map<String, String> header = callback.getReqHeader(new HashMap<String, String>(), "", new LinkedHashMap<String, String>());
        callback.finish();
        return header;
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

    /**
     * 上传图片接口，不同是，上传图片的超时时间变了
     *
     * @param actionUrl
     * @param map
     * @param callback
     */
    public void doPostImg(String actionUrl, LinkedHashMap<String, String> map, InterCallback callback) {
        actionUrl = StringManager.replaceUrl(actionUrl);
        UtilInternetImg.in().doPost(actionUrl, map, callback);
    }

    @Override
    public void doPost(String actionUrl, LinkedHashMap<String, String> map, InterCallback callback) {
        actionUrl = StringManager.replaceUrl(actionUrl);
        super.doPost(actionUrl, map, callback);
    }

    @Override
    public void upLoadMP4(String actionUrl, String key, String path, InterCallback interCallback) {
        actionUrl = StringManager.replaceUrl(actionUrl);
        super.upLoadMP4(actionUrl, key, path, interCallback);
    }

}
