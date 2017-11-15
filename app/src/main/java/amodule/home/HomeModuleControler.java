package amodule.home;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.ArrayList;
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
    public static boolean isRequest = false;

    /**
     * 获取数据模块
     *
     * @param context
     * @param mType
     *
     * @return
     */
    @Nullable
    public HomeModuleBean getHomeModuleByType(Context context, @Nullable String mType) {
        HomeModuleBean mModuleBean = null;
        if(null == context) return mModuleBean;
        final String modulePath = FileManager.getDataDir() + FileManager.file_homeTopModle;
        String moduleJson = FileManager.readFile(modulePath);
        if (TextUtils.isEmpty(moduleJson)) {
            moduleJson = FileManager.getFromAssets(context, "homeTopModle");
            final String finalModuleJson = moduleJson;
            FileManager.saveFileToCompletePath(modulePath, finalModuleJson.toString(), false);
        }
        ArrayList<Map<String, String>> listModule = StringManager.getListMapByJson(moduleJson);
        int size = listModule.size();
        for (int i = 0; i < size; i++) {
            if (TextUtils.isEmpty(mType)
                    || TextUtils.equals(listModule.get(i).get("type"), mType)) {
                mModuleBean = new HomeModuleBean();
                mModuleBean.setTitle(listModule.get(i).get("title"));
                mModuleBean.setType(listModule.get(i).get("type"));
                mModuleBean.setWebUrl(listModule.get(i).get("webUrl"));
                mModuleBean.setIsSelf(listModule.get(i).get("isSelf"));
                mModuleBean.setOpenMode(listModule.get(i).get("openMode"));
                String level = listModule.get(i).get("level");
                if (!TextUtils.isEmpty(level)) {
                    mModuleBean.setTwoData(level);//设置二级数据内容
                }
                mModuleBean.setPosition(i);
                return mModuleBean;
            }
            continue;
        }
        setRequestModuleData();
        return mModuleBean;
    }

    /**
     * 请求模块数据
     */
    private void setRequestModuleData() {
        if (isRequest) return;
        final String modulePath = FileManager.getDataDir() + FileManager.file_homeTopModle;
        String url = StringManager.API_GET_LEVEL;
        ReqEncyptInternet.in().doEncyptAEC(url, "version=" + "v1",
                new InternetCallback(Main.allMain) {
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
