package amodule.home;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import acore.tools.FileManager;
import acore.tools.StringManager;
import amodule.main.Main;
import amodule.main.bean.HomeModuleBean;
import aplug.basic.InternetCallback;
import aplug.basic.ReqEncyptInternet;
import aplug.basic.ReqInternet;

/**
 * Description :
 * PackageName : amodule.home
 * Created by MrTrying on 2017/11/14 19:16.
 * Author : mrtrying
 * E_mail : ztanzeyu@gmail.com
 */

public class HomeModuleControler {
    private static volatile boolean isRequesting = false;

    @Nullable
    public HomeModuleBean getHomeModuleByType(@NonNull Context context, @Nullable String mType) {
        HomeModuleBean mModuleBean = null;
        //请求数据
        setRequestModuleData(null);
        //读取本地数据
        final String modulePath = FileManager.getDataDir() + FileManager.file_homeTopModle;
        String moduleJson = FileManager.readFile(modulePath);
        if (TextUtils.isEmpty(moduleJson) && context != null) {
            moduleJson = FileManager.getFromAssets(context, "homeTopModle");
            FileManager.saveFileToCompletePath(modulePath, moduleJson, false);
        }
        ArrayList<Map<String, String>> listModule = StringManager.getListMapByJson(moduleJson);
        for (int i = 0 , size = listModule.size(); i < size; i++) {
            Map<String,String> moduleMap = listModule.get(i);
            if (TextUtils.isEmpty(mType)
                    || TextUtils.equals(moduleMap.get("type"), mType)) {
                mModuleBean = new HomeModuleBean();
                mModuleBean.setTitle(moduleMap.get("title"));
                mModuleBean.setType(moduleMap.get("type"));
                mModuleBean.setWebUrl(moduleMap.get("webUrl"));
                mModuleBean.setIsSelf(moduleMap.get("isSelf"));
                mModuleBean.setOpenMode(moduleMap.get("openMode"));
                //设置二级数据内容
                String level = moduleMap.get("level");
                if (!TextUtils.isEmpty(level)) mModuleBean.setTwoData(level);
                //设置position
                mModuleBean.setPosition(i);
                return mModuleBean;
            }
        }
        return mModuleBean;
    }

    /** 请求模块数据 */
    private void setRequestModuleData(InternetCallback callback) {
        if (isRequesting) return;
        isRequesting = true;
        final String modulePath = FileManager.getDataDir() + FileManager.file_homeTopModle;
        ReqEncyptInternet.in().doEncyptAEC(StringManager.API_GET_LEVEL, "version=" + "v2",
                new InternetCallback() {
                    @Override
                    public void loaded(int flag, String url, final Object o) {
                        isRequesting = false;
                        if (flag >= ReqInternet.REQ_OK_STRING) {
                            FileManager.saveFileToCompletePath(modulePath, o.toString(), false);
                            if(callback != null){
                                callback.loaded(ReqInternet.REQ_OK_STRING,url,o);
                            }
                        }
                    }
                });
    }
}
