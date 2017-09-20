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

//    /**
//     * 处理模版数据
//     * 存储数据，的key是通过当前url获取出来
//     *
//     * @param requestMethod
//     */
//    public void handleXHMouldData(final String requestMethod) {
//        isCallBack = false;
//        final String path = FileManager.getSDDir() + "long/" + requestMethod;
//        final String readStr = FileManager.readFile(path);
//        final Object versionSign = FileManager.loadShared(XHApplication.in(), requestMethod, "version_sign");
//        LinkedHashMap<String, String> mapParams = new LinkedHashMap<>();
//        mapParams.put("versionSign", versionSign == null || TextUtils.isEmpty(readStr) ? "" : String.valueOf(versionSign));
//        mapParams.put("requestMethod", requestMethod);
//        String url = StringManager.api_getXhTemplate;
//        if (mouldCallBack != null && !TextUtils.isEmpty(readStr)) {
//            isCallBack = true;
//            Log.i("wyl", "状态:2:111" );
//            mouldCallBack.load(true, readStr, requestMethod, versionSign == null ? "" : String.valueOf(versionSign));
//        }
//        ReqEncyptInternet.in().doEncypt(url, mapParams, new InternetCallback(XHActivityManager.getInstance().getCurrentActivity()) {
//            @Override
//            public void loaded(int flag, String url, final Object msg) {
//                if (flag >= ReqInternet.REQ_OK_STRING) {
//                    handlerQiniuGetData(msg, requestMethod, path, readStr, "versionSign");
//                }
//            }
//        });
//    }

//    /**
//     * 处理电商模版
//     *
//     * @param requestMethod
//     */
//    private void handlerDsMouldData(final String requestMethod) {
//        isCallBack = false;
//        final String path = FileManager.getSDDir() + "long/" + requestMethod;
//        final String readStr = FileManager.readFile(path);
//        final Object versionUrl = FileManager.loadShared(XHApplication.in(), requestMethod, "version_sign");
//        String version = versionUrl == null || TextUtils.isEmpty(readStr) ? "" : String.valueOf(versionUrl);
//        String url = MallStringManager.mall_api_dsTemplate + "?request_method=" + requestMethod + "&version_sign=" + version;
//        if (mouldCallBack != null && !TextUtils.isEmpty(readStr)) {
//            isCallBack = true;
//            Log.i("wyl", "状态::111" );
//            mouldCallBack.load(true, readStr, requestMethod, versionUrl == null ? "" : String.valueOf(versionUrl));
//        }
//        MallReqInternet.in().doGet(url, new MallInternetCallback(XHActivityManager.getInstance().getCurrentActivity()) {
//            @Override
//            public void loadstat(final int flag, final String url, final Object msg, Object... stat) {
//                if (flag >= ReqInternet.REQ_OK_STRING) {
//                    handlerQiniuGetData(msg, requestMethod, path, readStr, "version_sign");
//                }
//            }
//        });
//    }

    /**
     * 处理单个模版信息
     * @param requestMethod
     */
    public void handlerDsModuleData(String requestMethod){
        isCallBack=false;
        String url= MallStringManager.mall_api_getTemplateName+"?request_method="+requestMethod;
        MallReqInternet.in().doGet(url, new MallInternetCallback(XHActivityManager.getInstance().getCurrentActivity()) {
            @Override
            public void loadstat(int flag, String url, Object msg, Object... stat) {
                if(flag>=ReqInternet.REQ_OK_STRING){
                    Map<String,String> listMap= StringManager.getFirstMap(msg);
                    if(listMap!=null&&listMap.containsKey("template_name")){
                        String requestMethod=listMap.get("template_name");
                        String version_key=listMap.get("version_sign");
                        final String path = FileManager.getSDDir() + "long/" + requestMethod;
                        final String readStr = FileManager.readFile(path);
                        final Object versionUrl = FileManager.loadShared(XHApplication.in(), requestMethod, "version_sign");
                        String version = versionUrl == null || TextUtils.isEmpty(readStr) ? "" : String.valueOf(versionUrl);
                        if (mouldCallBack != null && !TextUtils.isEmpty(readStr)) {
                            isCallBack = true;
                            Log.i("wyl","回调：：111");
                            mouldCallBack.load(true, readStr, requestMethod, versionUrl == null ? "" : String.valueOf(versionUrl));
                        }
                        if(TextUtils.isEmpty(version)||!version.equals(version_key)){
                            handlerQiniuGetData(listMap,requestMethod,path,readStr,"version_sign");
                        }
                    }
                }
            }
        });
    }
    /**
     * 处理从七牛那请求数据
     * @param map
     * @param requestMethod  模块名称
     * @param path   文件保存路径
     * @param readStr  文件内容
     * @param versionKey 不同模版的key不同。
     */
    public void handlerQiniuGetData(final Map<String,String> map, final String requestMethod, final String path, final String readStr,String versionKey){
            if (map.containsKey("url") && !TextUtils.isEmpty(map.get("url"))) {
                String dataUrl = map.get("url");
                final String version_sign=map.get(versionKey);
                final String finalDataUrl = Uri.decode(dataUrl);
                Log.i("wyl","finalDataUrl::;"+finalDataUrl);
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        ReqInternet.in().getInputStream(finalDataUrl, new InternetCallback(XHActivityManager.getInstance().getCurrentActivity()) {
                            @Override
                            public void loaded(int flag, String url, final Object msg) {
                                if (flag >= ReqInternet.REQ_OK_IS) {
                                    try {
                                        new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                try {
                                                    final String data = readInfoStream((InputStream) msg);
                                                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            handler(data,path,requestMethod,readStr,version_sign);
                                                        }
                                                    });
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }).start();

                                    } catch (Exception e) {
                                        if (mouldCallBack != null && !TextUtils.isEmpty(readStr) && !isCallBack) {
                                            Log.i("wyl","回调：：444");
                                            mouldCallBack.load(true, readStr, requestMethod, String.valueOf(version_sign));
                                        }
                                    }
                                } else if (mouldCallBack != null && !TextUtils.isEmpty(readStr) && !isCallBack) {
                                    Log.i("wyl","回调：：555");
                                    mouldCallBack.load(true, readStr, requestMethod, String.valueOf(version_sign));
                                }
                            }
                        });
                    }
                });
            }
    }

    private void handler(String data,String path,String requestMethod,String readStr,String version_sign){
        if (!TextUtils.isEmpty(data)) {
            File file = FileManager.saveFileToCompletePath(path, data, false);
            if (file != null)
                FileManager.saveShared(XHApplication.in(), requestMethod, "version_sign", String.valueOf(version_sign));
            if (mouldCallBack != null && !isCallBack) {
                Log.i("wyl","回调：：222");
                mouldCallBack.load(true, data, requestMethod, String.valueOf(version_sign));
            }
        } else {
            if (mouldCallBack != null && !TextUtils.isEmpty(readStr) && !isCallBack) {
                Log.i("wyl","回调：：333");
                mouldCallBack.load(true, readStr, requestMethod, String.valueOf(version_sign));
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
            handlerDsModuleData(requestMethod);
        } else if (requestMethod.startsWith("xh")) {//香哈
//            handleXHMouldData(requestMethod);
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
                infoStream.write(bcache, 0, readSize);
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
            try {
                Log.i("wyl", "状态::return");
                return infoStream.toString("utf-8");
            } catch (UnsupportedEncodingException e) {
                throw new Exception("输出异常");
            }
        }
    }
}
