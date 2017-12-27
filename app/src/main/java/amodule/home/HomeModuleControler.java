package amodule.home;

import android.content.Context;
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
    private static boolean isRequest = false;

    public HomeModuleBean getHomeModuleByType(@Nullable Context context, @Nullable String mType) {
        return getHomeModuleByType(false,context,mType);
    }

    /**
     * 获取数据模块
     *
     * @param context 上下文
     * @param mType 模块类型，默认null为首页
     *
     * @return 数据类型对象
     */
    @Nullable
    public HomeModuleBean getHomeModuleByType(boolean isRefresh,@Nullable Context context, @Nullable String mType) {
        HomeModuleBean mModuleBean = null;
        if(null == context) return null;
        final String modulePath = FileManager.getDataDir() + FileManager.file_homeTopModle;
        String moduleJson = FileManager.readFile(modulePath);
        if (TextUtils.isEmpty(moduleJson)) {
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
        setRequestModuleData(isRefresh);
        return mModuleBean;
    }

    /**
     * 请求模块数据
     */
    private void setRequestModuleData(boolean isRefresh) {
        if (isRequest && !isRefresh) return;
        final String modulePath = FileManager.getDataDir() + FileManager.file_homeTopModle;
        ReqEncyptInternet.in().doEncyptAEC(StringManager.API_GET_LEVEL, "version=" + "v1",
                new InternetCallback() {
                    @Override
                    public void loaded(int flag, String url, final Object o) {
                        if (flag >= ReqInternet.REQ_OK_STRING) {
                            isRequest = true;
                            FileManager.saveFileToCompletePath(modulePath, o.toString(), false);
                        }
                    }
                });
    }
}
