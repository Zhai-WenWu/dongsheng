package amodule.dish.tools;

import android.os.Handler;
import android.os.Message;
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

/**
 * Created by Fang Ruijiao on 2017/7/20.
 */

public class DishMouldControl {

    public static void setDishMould(final OnDishMouldListener listener){
        final String path = FileManager.getSDDir() + "long/" + FileManager.file_dishMould;
        String readStr = FileManager.readFile(path);
        Object versionSign = FileManager.loadShared(Main.allMain.getApplicationContext(),FileManager.file_dishMould,"versionSign");
        LinkedHashMap<String,String> mapParams = new LinkedHashMap<>();
        mapParams.put("versionSign",versionSign == null || TextUtils.isEmpty(readStr) ? "" : String.valueOf(versionSign));
        ReqEncyptInternet.in().doEncypt(StringManager.api_getDishMould,mapParams, new InternetCallback(Main.allMain.getApplicationContext()) {
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
                                File file = FileManager.saveFileToCompletePath(path, data, false);
                                if(file != null) FileManager.saveShared(Main.allMain.getApplicationContext(),FileManager.file_dishMould,"versionSign",map.get("versionSign"));
                                if(listener != null) listener.loaded(true,data);
                                return;
                            }
                            if(listener != null) listener.loaded(false,"");
                        }
                    }).start();
                }else{
                    if(listener != null) listener.loaded(false,"");
                }
            }
        });
    }

    public static void getDishMould(final OnDishMouldListener listener){
        final Handler handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                setDishMould(listener);
            }
        };
        new Thread(new Runnable() {
            @Override
            public void run() {
                String path = FileManager.getSDDir() + "long/" + FileManager.file_dishMould;
                String readStr = FileManager.readFile(path);
//                Log.i("detailDish","getDishMould() path:" + path);
//                Log.i("detailDish","getDishMould() readStr:" + readStr);
                if (TextUtils.isEmpty(readStr)){
                    handler.sendEmptyMessage(0);
                }else {
                    listener.loaded(true, readStr);
                }
            }
        }).start();
    }

    public static String getOffDishPath(){
        return FileManager.getSDDir() + "long/";
    }

    public interface OnDishMouldListener{
        public void loaded(boolean isSucess,String data);
    }
}
