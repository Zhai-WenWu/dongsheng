package aplug.web.tools;

import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import acore.override.XHApplication;
import acore.override.helper.XHActivityManager;
import acore.tools.FileManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import amodule.main.Main;
import aplug.basic.InternetCallback;
import aplug.basic.ReqEncyptInternet;
import aplug.basic.ReqInternet;
import third.mall.aplug.MallInternetCallback;
import third.mall.aplug.MallReqInternet;
import third.mall.aplug.MallStringManager;

/**
 * 电商webview的模版控制的类
 * 严谨外部使用
 */
public class TemplateWebViewControl {
    /**
     * 处理模版数据
     * 存储数据，的key是通过当前url获取出来
     *
     * @param requestMethod
     */
    public void handleXHMouldData(final String requestMethod) {
//        final String key=StringManager.stringToMD5(url.toLowersuCase()).toLowerCase();
        long time= System.currentTimeMillis();
        Log.i("zyj","time::handleXHMouldData::"+(time-XHTemplateManager.starttime));
        final String path = FileManager.getSDDir() + "long/" + requestMethod;
        final String readStr = FileManager.readFile(path);
        final Object versionSign = FileManager.loadShared(XHApplication.in(), requestMethod, "versionSign");
        LinkedHashMap<String, String> mapParams = new LinkedHashMap<>();
        mapParams.put("versionSign", versionSign == null || TextUtils.isEmpty(readStr) ? "" : String.valueOf(versionSign));
        String url = StringManager.api_getDishMould;
        ReqEncyptInternet.in().doEncypt(url, mapParams, new InternetCallback(XHApplication.in()) {
            @Override
            public void loaded(int flag, String url, final Object msg) {
                AnalyzData(flag,url,msg,requestMethod,path,readStr, versionSign);
            }
        });
    }

    /**
     * 处理电商模版
     * @param requestMethod
     */
    private void handlerDsMouldData(final String requestMethod) {
        final String path = FileManager.getSDDir() + "long/" + requestMethod;
        final String readStr = FileManager.readFile(path);
        final Object versionSign = FileManager.loadShared(XHApplication.in(), requestMethod, "versionSign");
        LinkedHashMap<String, String> mapParams = new LinkedHashMap<>();
        String version= versionSign == null || TextUtils.isEmpty(readStr) ? "" : String.valueOf(versionSign);
        String url = MallStringManager.mall_api_getTemplate+"?request_method="+requestMethod+"&version_sign="+version;
        MallReqInternet.in().doGet(url, new MallInternetCallback(XHApplication.in()) {
            @Override
            public void loadstat(int flag, final String url, final Object msg, Object... stat) {
                AnalyzData(flag,url,msg,requestMethod,path,readStr, versionSign);
            }
        });
    }

    /**
     * 处理解析数据
     * @param flag
     * @param url
     * @param msg
     * @param requestMethod--方法名称
     * @param path--文件
     * @param readStr---html代码
     * @param versionSign--版本
     */
    private void AnalyzData(int flag, final String url, final Object msg,String requestMethod,String path,String readStr,Object versionSign){
        if (flag >= ReqInternet.REQ_OK_STRING) {
            long time= System.currentTimeMillis();
            if (!TextUtils.isEmpty(String.valueOf(msg)) && !"[]".equals(String.valueOf(msg))) {
                Map<String, String> map = StringManager.getFirstMap(msg);
                String data = map.get("html");
                versionSign = map.get("versionSign");
                if (!TextUtils.isEmpty(data)) {//返回数据---有新版本处理
                    File file = FileManager.saveFileToCompletePath(path, data, false);
                    if (file != null)
                        FileManager.saveShared(XHApplication.in(), requestMethod, "versionSign", String.valueOf(versionSign));
                    if (mouldCallBack != null)
                        mouldCallBack.load(true, data, requestMethod, String.valueOf(versionSign));
                } else {//无数据标示已经是最新版本。
                    if (mouldCallBack != null && !TextUtils.isEmpty(readStr))
                        mouldCallBack.load(true, readStr, requestMethod, String.valueOf(versionSign));
                }
            } else {
                if (mouldCallBack != null && !TextUtils.isEmpty(readStr))
                    mouldCallBack.load(true, readStr, requestMethod, String.valueOf(versionSign));
            }
        } else {
            if (mouldCallBack != null)
                mouldCallBack.load(false, "", requestMethod, String.valueOf(versionSign));
        }
    }

    /**
     * 设置数据回调
     *
     * @param callBack
     */
    public void setMouldCallBack(MouldCallBack callBack) {
        this.mouldCallBack = callBack;
    }

    private MouldCallBack mouldCallBack;

    /**
     * 电商数据回调
     */
    public interface MouldCallBack {
        public void load(boolean isSuccess, String data, String requestMothed, String version);
    }

    /**
     * 获取单个模版更新
     *
     * @param requestMethod
     */
    public void getH5MDWithRequestMed(String requestMethod) {
        long time= System.currentTimeMillis();
        Log.i("zyj","time::getH5MDWithRequestMed::"+(time-XHTemplateManager.starttime));
        if (TextUtils.isEmpty(requestMethod)) {
            return;
        }
        if (requestMethod.startsWith("Ds")) {//电商请求
            handlerDsMouldData(requestMethod);
        } else if (requestMethod.startsWith("Xh")) {//香哈
            handleXHMouldData(requestMethod);
        }
    }
}
