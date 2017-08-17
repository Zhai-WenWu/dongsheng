package third.mall.aplug;

import android.text.TextUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import acore.tools.FileManager;
import acore.tools.StringManager;
import amodule.main.Main;
import aplug.basic.InternetCallback;
import aplug.basic.ReqEncyptInternet;
import aplug.basic.ReqInternet;
import third.mall.wx.MD5;

/**
 * 电商webview的模版控制的类
 */
public class MallMouldWebViewControl {
    private static MallMouldWebViewControl mallMouldWebViewControl;
    public static MallMouldWebViewControl getInstance(){
        if(mallMouldWebViewControl==null){
            synchronized (MallMouldWebViewControl.class){
                if(mallMouldWebViewControl==null){
                    mallMouldWebViewControl= new MallMouldWebViewControl();
                }
            }
        }
        return mallMouldWebViewControl;
    }

    /**
     * 处理模版数据
     * 存储数据，的key是通过当前url获取出来
     * @param url
     */
    public void handleMouldData(final String url){
        final String key=StringManager.stringToMD5(url.toLowerCase()).toLowerCase();
        final String path = FileManager.getSDDir() + "long/" + key;
        final String readStr = FileManager.readFile(path);
        final Object versionSign = FileManager.loadShared(Main.allMain.getApplicationContext(),key,"versionSign");
        LinkedHashMap<String,String> mapParams = new LinkedHashMap<>();
        mapParams.put("versionSign",versionSign == null || TextUtils.isEmpty(readStr) ? "" : String.valueOf(versionSign));
        ReqEncyptInternet.in().doEncypt(url,mapParams, new InternetCallback(Main.allMain.getApplicationContext()) {
            @Override
            public void loaded(int i, String s, final Object o) {
                if(i >= ReqInternet.REQ_OK_STRING){
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            ArrayList<Map<String,String>> arrayList = StringManager.getListMapByJson(o);
                            if(arrayList.size() > 0) {
                                Map<String,String> map = arrayList.get(0);
                                String data = map.get("html");
                                String versionSign = map.get("versionSign");
                                if(!TextUtils.isEmpty(data)) {//返回数据---有新版本处理
                                    File file = FileManager.saveFileToCompletePath(path, data, false);
                                    if (file != null)
                                        FileManager.saveShared(Main.allMain.getApplicationContext(), key, "versionSign", versionSign);
                                    if (mallMouldCallBack != null)
                                        mallMouldCallBack.load(true, data, url,String.valueOf(versionSign));
                                    return;
                                }else{//无数据标示已经是最新版本。
                                    if (mallMouldCallBack != null&&!TextUtils.isEmpty(readStr))
                                        mallMouldCallBack.load(true, readStr, url,String.valueOf(versionSign));
                                }
                            }else{
                                if (mallMouldCallBack != null&&!TextUtils.isEmpty(readStr))
                                    mallMouldCallBack.load(true, readStr, url,String.valueOf(versionSign));
                            }
                            if(mallMouldCallBack != null) mallMouldCallBack.load(false,"",url,String.valueOf(versionSign));
                        }
                    }).start();
                }else{
                    if(mallMouldCallBack != null) mallMouldCallBack.load(false,"",url,String.valueOf(versionSign));
                }
            }
        });
    }

    /**
     * 设置数据回调
     * @param callBack
     */
    public void setMallMouldCallBack(MallMouldCallBack callBack){
        this.mallMouldCallBack = callBack;
    }
    private  MallMouldCallBack mallMouldCallBack;

    /**
     * 电商数据回调
     */
    public interface MallMouldCallBack{
        public void load(boolean isSuccess,String data,String url,String version);
    }

}
