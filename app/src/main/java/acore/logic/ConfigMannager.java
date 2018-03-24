package acore.logic;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.util.Map;

import acore.tools.FileManager;
import acore.tools.StringManager;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;

import static xh.basic.tool.UtilFile.readFile;

/**
 * Description :
 * PackageName : acore.logic
 * Created by mrtrying on 2018/1/25 11:05:01.
 * e_mail : ztanzeyu@gmail.com
 */
public class ConfigMannager {

    public static final String KEY_ADVIDEO = "videoAD";
    public static final String KEY_ADVIDEO_TEST = "videoADTest";
    public static final String KEY_RANDPROMOTION = "randPromotion";
    public static final String KEY_DIVERSION = "diversion";
    public static final String KEY_GOODCOMMENT = "goodComment";
    public static final String KEY_PUSHJSON = "pushJson";
    public static final String KEY_RANDPROMOTIONNEW = "randpromotionurlnew";
    public static final String KEY_VIVOAD = "vivoAD";
    public static final String KEY_NAVTOWEBSTAT= "navToWebStat";
    public static final String KEY_BAIDUAPPID= "baiduappid";
    public static final String KEY_CAIPUVIP= "caipuVIP";
    public static final String KEY_LOGPOSTTIME= "logPostTime";
    public static final String KEY_IMAGEACCEPT= "imageAccept";
    public static final String KEY_NETPROTOCOL= "netProtocol";
    public static final String KEY_APPPUSHTIMERANGE= "apppushtimerange";
    public static final String KEY_NEW_AD_CONFIG = "newAdConfig";

    /**
     * 保存config
     */
    public static void saveConfigData(Context context) {
        ReqInternet.in().doGet(StringManager.api_getConf, new InternetCallback() {
            @Override
            public void loaded(int flag, String url, final Object msg) {
                if (flag >= ReqInternet.REQ_OK_STRING) {
                    //更新视频数据
                    new Thread(() -> FileManager.saveFileToCompletePath(getConfigPath(), msg.toString(), false)).start();
                    AdVideoConfigTool.of().updateAdVideoData(msg.toString());
                }
            }
        });
    }

    @NonNull
    private static String getConfigPath() {
        return FileManager.getDataDir() + FileManager.file_config;
    }

    /**
     * 获取config数据
     *
     * @param key 若key为空，返回所有config数据
     * @return
     */
    public static String getConfigByLocal(String key) {
        String data = "";
        String configData = readFile(getConfigPath());
        if (TextUtils.isEmpty(key)) {
            return configData;
        }
        Map<String, String> map = StringManager.getFirstMap(configData);
        if (map != null && map.containsKey(key)) {
            data = map.get(key);
        }
        return data;
    }


}
