package aplug.web.tools;

import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
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
    private boolean isCallBack = false;//是否已经回调

    /**
     * 处理模版数据
     * 存储数据，的key是通过当前url获取出来
     *
     * @param requestMethod
     */
    public void handleXHMouldData(final String requestMethod) {
        isCallBack = false;
        final String path = FileManager.getSDDir() + "long/" + requestMethod;
        final String readStr = FileManager.readFile(path);
        final Object versionSign = FileManager.loadShared(XHApplication.in(), requestMethod, "versionSign");
        LinkedHashMap<String, String> mapParams = new LinkedHashMap<>();
        mapParams.put("versionSign", versionSign == null || TextUtils.isEmpty(readStr) ? "" : String.valueOf(versionSign));
        mapParams.put("requestMethod", requestMethod);
        String url = StringManager.api_getTemplate;
        if (mouldCallBack != null && !TextUtils.isEmpty(readStr)) {
            isCallBack = true;
            mouldCallBack.load(true, readStr, requestMethod, versionSign == null ? "" : String.valueOf(versionSign));
        }
        ReqEncyptInternet.in().doEncypt(url, mapParams, new InternetCallback(XHActivityManager.getInstance().getCurrentActivity()) {
            @Override
            public void loaded(int flag, String url, final Object msg) {
                AnalyzData(flag, url, msg, requestMethod, path, readStr, versionSign);
            }
        });
    }

    /**
     * 处理电商模版
     *
     * @param requestMethod
     */
    private void handlerDsMouldData(final String requestMethod) {
        isCallBack = false;
        final String path = FileManager.getSDDir() + "long/" + requestMethod;
        final String readStr = FileManager.readFile(path);
        final Object versionUrl = FileManager.loadShared(XHApplication.in(), requestMethod, "url");
        String version = versionUrl == null || TextUtils.isEmpty(readStr) ? "" : String.valueOf(versionUrl);
        String url = MallStringManager.mall_api_getTemplate + "?request_method=" + requestMethod + "&url=" + version;
        if (mouldCallBack != null && !TextUtils.isEmpty(readStr)) {
            isCallBack = true;
            mouldCallBack.load(true, readStr, requestMethod, versionUrl == null ? "" : String.valueOf(versionUrl));
        }
        MallReqInternet.in().doGet(url, new MallInternetCallback(XHActivityManager.getInstance().getCurrentActivity()) {
            @Override
            public void loadstat(final int flag, final String url, final Object msg, Object... stat) {
                if (flag >= ReqInternet.REQ_OK_STRING) {
                    Log.i("wyl", "msg::" + msg);
                    Map<String, String> map = StringManager.getFirstMap(msg);
                    if (map.containsKey("url") && !TextUtils.isEmpty(map.get("url"))) {
                        final String dataUrl = map.get("url");
                        final String finalDataUrl = Uri.decode(dataUrl);
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                ReqInternet.in().getInputStream(finalDataUrl, new InternetCallback(XHActivityManager.getInstance().getCurrentActivity()) {
                                    @Override
                                    public void loaded(int flag, String url, Object msg) {
                                        if(flag>=ReqInternet.REQ_OK_IS) {
                                            try {
                                                String data = readInfoStream((InputStream) msg);
                                                if(!TextUtils.isEmpty(data)){
                                                    File file = FileManager.saveFileToCompletePath(path, data, false);
                                                    if (file != null)
                                                        FileManager.saveShared(XHApplication.in(), requestMethod, "url", String.valueOf(dataUrl));
                                                    if (mouldCallBack != null && !isCallBack) {
                                                        mouldCallBack.load(true, data, requestMethod, String.valueOf(dataUrl));
                                                    }
                                                }else{
                                                    if (mouldCallBack != null && !TextUtils.isEmpty(readStr) && !isCallBack) {
                                                        mouldCallBack.load(true, readStr, requestMethod, String.valueOf(dataUrl));
                                                    }
                                                }
                                            } catch (Exception e) {
                                                if (mouldCallBack != null && !TextUtils.isEmpty(readStr) && !isCallBack) {
                                                    mouldCallBack.load(true, readStr, requestMethod, String.valueOf(dataUrl));
                                                }
                                            }
                                        }else{
                                            if (mouldCallBack != null && !TextUtils.isEmpty(readStr) && !isCallBack) {
                                                mouldCallBack.load(true, readStr, requestMethod, String.valueOf(dataUrl));
                                            }
                                        }

                                    }
                                });
                            }
                        });
                    }
                }
            }
        });
//
    }

    /**
     * 处理解析数据
     *
     * @param flag
     * @param url
     * @param msg
     * @param requestMethod--方法名称
     * @param path--文件
     * @param readStr---html代码
     * @param versionSign--版本
     */
    private void AnalyzData(int flag, final String url, final Object msg, String requestMethod, String path, String readStr, Object versionSign) {
        if (flag >= ReqInternet.REQ_OK_STRING) {
            long time = System.currentTimeMillis();
            if (!TextUtils.isEmpty(String.valueOf(msg)) && !"[]".equals(String.valueOf(msg))) {
                Map<String, String> map = StringManager.getFirstMap(msg);
                String data = map.get("html");
                versionSign = map.get("versionSign");
                if (!TextUtils.isEmpty(data)) {//返回数据---有新版本处理
                    File file = FileManager.saveFileToCompletePath(path, data, false);
                    if (file != null)
                        FileManager.saveShared(XHApplication.in(), requestMethod, "versionSign", String.valueOf(versionSign));
                    if (mouldCallBack != null && !isCallBack) {
                        mouldCallBack.load(true, data, requestMethod, String.valueOf(versionSign));
                    }
                } else {//无数据标示已经是最新版本。
                    if (mouldCallBack != null && !TextUtils.isEmpty(readStr) && !isCallBack) {
                        mouldCallBack.load(true, readStr, requestMethod, String.valueOf(versionSign));
                    }
                }
            } else {
                if (mouldCallBack != null && !TextUtils.isEmpty(readStr) && !isCallBack) {
                    mouldCallBack.load(true, readStr, requestMethod, String.valueOf(versionSign));
                }
            }
        } else {
            if (mouldCallBack != null && !isCallBack) {
                mouldCallBack.load(false, "", requestMethod, String.valueOf(versionSign));
            }
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
        if (TextUtils.isEmpty(requestMethod)) {
            return;
        }
        if (requestMethod.startsWith("Ds")) {//电商请求
            handlerDsMouldData(requestMethod);
        } else if (requestMethod.startsWith("xh")) {//香哈
            handleXHMouldData(requestMethod);
        }
    }
    public String readInfoStream(InputStream input) throws Exception {
        if (input == null) {
            throw new Exception("输入流为null");
        }
        //字节数组
        byte[] bcache = new byte[2048];
        int readSize = 0;//每次读取的字节长度
        ByteArrayOutputStream infoStream = new ByteArrayOutputStream();
        try {
            //一次性读取2048字节
            while ((readSize = input.read(bcache)) > 0) {
                //将bcache中读取的input数据写入infoStream
                infoStream.write(bcache,0,readSize);
            }
        } catch (IOException e1) {
            throw new Exception("输入流读取异常");
        } finally {
            try {
                //输入流关闭
                input.close();
            } catch (IOException e) {
                throw new Exception("输入流关闭异常");
            }
        }
        try {
            return infoStream.toString("utf-8");
        } catch (UnsupportedEncodingException e) {
            throw new Exception("输出异常");
        }
    }
}
