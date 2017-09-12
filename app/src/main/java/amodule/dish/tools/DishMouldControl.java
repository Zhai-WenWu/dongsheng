package amodule.dish.tools;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import acore.override.XHApplication;
import acore.tools.FileManager;
import acore.tools.StringManager;
import amodule.main.Main;
import aplug.basic.InternetCallback;
import aplug.basic.ReqEncyptInternet;
import aplug.basic.ReqInternet;

/**
 * 模板信息管理类
 * Created by XiangHa on 2017/7/20.
 */
public class DishMouldControl {

    /**
     * 请求模版数据，并进行校验。
     * @param listener
     */
    public static void reqDishMould(final OnDishMouldListener listener){
        final String path = FileManager.getSDDir() + "long/" + FileManager.file_dishMould;
        final String readStr = FileManager.readFile(path);
        final Object versionSign = FileManager.loadShared(XHApplication.in(),FileManager.file_dishMould,"versionSign");
        LinkedHashMap<String,String> mapParams = new LinkedHashMap<>();
        mapParams.put("versionSign",versionSign == null || TextUtils.isEmpty(readStr) ? "" : String.valueOf(versionSign));
        ReqEncyptInternet.in().doEncypt(StringManager.api_getDishMould,mapParams, new InternetCallback(XHApplication.in()) {
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
                                        FileManager.saveShared(XHApplication.in(), FileManager.file_dishMould, "versionSign", versionSign);
                                    if (listener != null)
                                        listener.loaded(true, data, String.valueOf(versionSign));
                                    return;
                                }else{//无数据标示已经是最新版本。
                                    if (listener != null&&!TextUtils.isEmpty(readStr))
                                        listener.loaded(true, readStr, String.valueOf(versionSign));
                                }
                            }else{
                                if (listener != null&&!TextUtils.isEmpty(readStr))
                                    listener.loaded(true, readStr, String.valueOf(versionSign));
                            }
                        }
                    }).start();
                }else{
                    if(listener != null) listener.loaded(false,"",String.valueOf(versionSign));
                }
            }
        });
    }

    public static void getDishMould(final OnDishMouldListener listener){
        reqDishMould(listener);
    }

    public static String getOffDishPath(){
        return FileManager.getSDDir() + "long/offDish/";
    }

    public interface OnDishMouldListener{
        public void loaded(boolean isSucess,String data,String mouldVersion);
    }
}
