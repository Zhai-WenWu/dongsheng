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
import java.security.MessageDigest;
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
    public void handleXHModuleData(final String requestMethod) {
        isCallBack=false;
        String url= StringManager.API_TEMPLATE_GETTEMPLATENAME;
        String params= "requestMethod="+requestMethod;
        ReqEncyptInternet.in().doEncypt(url, params, new InternetCallback() {
            @Override
            public void loaded(int flag, String url, final Object msg) {
                if (flag >= ReqInternet.REQ_OK_STRING) {
                    Map<String,String> listMap= StringManager.getFirstMap(msg);
                    if(listMap!=null&&listMap.containsKey("templateName")){
                        String requestMethod=listMap.get("templateName");
                        String version_key=listMap.get("versionSign");
                        String path = FileManager.getSDDir() + "long/" + requestMethod;
                        String readStr = null;
                        try {
                            readStr = readInfoStream(FileManager.loadFile(path));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        Object versionUrl = FileManager.loadShared(XHApplication.in(), requestMethod, "version_sign");
                        String version = versionUrl == null || TextUtils.isEmpty(readStr) ? "" : String.valueOf(versionUrl);
                        //对数据进行校验。md5验证
                        if(versionUrl != null &&!TextUtils.isEmpty(String.valueOf(versionUrl))&& !TextUtils.isEmpty(readStr)){
                            String md5Data= MD5(readStr).toLowerCase();
                           //YLKLog.i(Main.TAG,"md5Data::"+md5Data+":::"+versionUrl);
                            if(!String.valueOf(versionUrl).equals(md5Data)){
                                version="";
                                readStr="";
                            }
                        }
                        if (mouldCallBack != null ) {
                            if(TextUtils.isEmpty(readStr)){
                               //YLKLog.i(Main.TAG,"当前没有下载下来数据，从资源文件中取::"+requestMethod);
                                readStr=FileManager.getFromAssets(XHActivityManager.getInstance().getCurrentActivity(),requestMethod);
                            }
                            if(!TextUtils.isEmpty(readStr)) {
                                isCallBack = true;
                               //YLKLog.i(Main.TAG,"有资源数据，直接返回::"+requestMethod);
                                mouldCallBack.load(true, readStr, requestMethod, versionUrl == null ? "" : String.valueOf(versionUrl));
                            }
                        }
                        if(TextUtils.isEmpty(version)||!version.equals(version_key)){
                            handlerQiniuGetData(listMap,requestMethod,path,readStr,"versionSign");
                        }
                    }
                }
            }
        });
    }

    /**
     * 处理单个模版信息
     * @param requestMethod
     */
    public void handlerDsModuleData(String requestMethod){
        isCallBack=false;
        String url= MallStringManager.mall_api_getTemplateName+"?request_method="+requestMethod;
        MallReqInternet.in().doGet(url, new MallInternetCallback() {
            @Override
            public void loadstat(int flag, String url, Object msg, Object... stat)  {
                if(flag>=ReqInternet.REQ_OK_STRING){
                    Map<String,String> listMap= StringManager.getFirstMap(msg);
                    if(listMap!=null&&listMap.containsKey("template_name")){
                        String requestMethod=listMap.get("template_name");
                        String version_key=listMap.get("version_sign");
                        String path = FileManager.getSDDir() + "long/" + requestMethod;
                        String readStr = null;
                        try {
                            readStr = readInfoStream(FileManager.loadFile(path));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        Object versionUrl = FileManager.loadShared(XHApplication.in(), requestMethod, "version_sign");
                        String version = versionUrl == null || TextUtils.isEmpty(readStr) ? "" : String.valueOf(versionUrl);

                        //对数据进行校验。md5验证
                        if(versionUrl != null &&!TextUtils.isEmpty(String.valueOf(versionUrl))&& !TextUtils.isEmpty(readStr)){
                            String md5Data= MD5(readStr).toLowerCase();
                           //YLKLog.i(Main.TAG,"md5Data::"+md5Data+":::"+versionUrl);
                            if(!String.valueOf(versionUrl).equals(md5Data)){
                                version="";
                                readStr="";
                            }
                        }
                        if (mouldCallBack != null ) {
                            if(TextUtils.isEmpty(readStr)){
                               //YLKLog.i(Main.TAG,"当前没有下载下来数据，从资源文件中取：："+requestMethod);
                                readStr=FileManager.getFromAssets(XHActivityManager.getInstance().getCurrentActivity(),requestMethod);
                            }
                            if(!TextUtils.isEmpty(readStr)) {
                                isCallBack = true;
                               //YLKLog.i(Main.TAG,"有资源数据，直接返回：："+requestMethod);
                                mouldCallBack.load(true, readStr, requestMethod, versionUrl == null ? "" : String.valueOf(versionUrl));
                            }
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
               //YLKLog.i("wyl","finalDataUrl::;"+finalDataUrl);
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        ReqInternet.in().getInputStream(finalDataUrl, new InternetCallback() {
                            @Override
                            public void loaded(int flag, String url, final Object msg) {
                                if (flag >= ReqInternet.REQ_OK_IS) {
                                    try {
                                        new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                try {
                                                    final String data = readInfoStream((InputStream) msg);
                                                    long time= System.currentTimeMillis();
                                                    String dataMd5 = MD5(data).toLowerCase();
                                                    long timenow= System.currentTimeMillis();
                                                   //YLKLog.i(Main.TAG,"dataMd5::"+dataMd5+":::"+version_sign+":::"+(timenow-time));
                                                    if(version_sign.equals(dataMd5)) {
                                                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                handler(data, path, requestMethod, readStr, version_sign);
                                                            }
                                                        });
                                                    }else{
                                                        if (mouldCallBack != null  && !isCallBack) {
                                                           //YLKLog.i(Main.TAG,"服务端返回数据777::"+requestMethod);
                                                            mouldCallBack.load(true, TextUtils.isEmpty(readStr) ? "" : readStr, requestMethod, String.valueOf(version_sign));
                                                        }

                                                    }
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }).start();

                                    } catch (Exception e) {
                                        if (mouldCallBack != null && !TextUtils.isEmpty(readStr) && !isCallBack) {
                                           //YLKLog.i(Main.TAG,"服务端返回数据444::"+requestMethod);
                                            mouldCallBack.load(true, readStr, requestMethod, String.valueOf(version_sign));
                                        }
                                    }
                                } else if (mouldCallBack != null && !TextUtils.isEmpty(readStr) && !isCallBack) {
                                   //YLKLog.i(Main.TAG,"服务端返回数据555::"+requestMethod);
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
               //YLKLog.i(Main.TAG,"服务端返回数据222::"+requestMethod);
                mouldCallBack.load(true, data, requestMethod, String.valueOf(version_sign));
            }
        } else {
            if (mouldCallBack != null && !TextUtils.isEmpty(readStr) && !isCallBack) {
               //YLKLog.i(Main.TAG,"服务端返回数据333::"+requestMethod);
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
            handleXHModuleData(requestMethod);
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
               //YLKLog.i("wyl", "状态::return");
                return infoStream.toString("utf-8");
            } catch (UnsupportedEncodingException e) {
                throw new Exception("输出异常");
            }
        }
    }

    /**
     * md5加密
     * @param pwd
     * @return
     */
    public static String MD5(String pwd) {
        //用于加密的字符
        char md5String[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                'A', 'B', 'C', 'D', 'E', 'F' };
        try {
            //使用平台的默认字符集将此 String 编码为 byte序列，并将结果存储到一个新的 byte数组中
            byte[] btInput = pwd.getBytes();

            //信息摘要是安全的单向哈希函数，它接收任意大小的数据，并输出固定长度的哈希值。
            MessageDigest mdInst = MessageDigest.getInstance("MD5");

            //MessageDigest对象通过使用 update方法处理数据， 使用指定的byte数组更新摘要
            mdInst.update(btInput);

            // 摘要更新之后，通过调用digest（）执行哈希计算，获得密文
            byte[] md = mdInst.digest();

            // 把密文转换成十六进制的字符串形式
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {   //  i = 0
                byte byte0 = md[i];  //95
                str[k++] = md5String[byte0 >>> 4 & 0xf];    //    5
                str[k++] = md5String[byte0 & 0xf];   //   F
            }

            //返回经过加密后的字符串
            return new String(str);

        } catch (Exception e) {
            return null;
        }
    }

}
