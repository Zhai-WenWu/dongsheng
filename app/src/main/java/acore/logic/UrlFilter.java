package acore.logic;

import android.net.Uri;
import android.text.TextUtils;

import com.annimon.stream.Stream;
import com.annimon.stream.function.Consumer;

import java.util.ArrayList;
import java.util.Map;

import acore.tools.StringManager;

/**
 * @Description : //TODO
 * PackageName : acore.logic
 * Created by mrtrying on 2017/12/14 16:16:52.
 * e_mail : ztanzeyu@gmail.com
 */
public class UrlFilter {

    static final String KEY_AdDownLoad = "AdDownLoad";

    public static String filterAdDownloadUrl(String url) {
        if (!TextUtils.isEmpty(url)) {
            String ruleValue = AppCommon.getConfigByLocal(KEY_AdDownLoad);
            if(TextUtils.isEmpty(ruleValue))
                return url;
            Map<String, String> ruleMap = StringManager.getFirstMap(ruleValue);
            if(null == ruleMap || ruleMap.isEmpty())
                return url;
            for (Map.Entry<String, String> entry : ruleMap.entrySet()) {
                if (isMatchdispatch(url, entry.getKey(), entry.getValue())) {
                    StringBuffer newUrl = new StringBuffer("download.app?")
                            .append("url=").append(Uri.encode(url))
                            .append("&").append("appname=xiangha_ad");
                    return newUrl.toString();
                }
            }
        }
        return url;
    }

    static final String CASE_START = "start";
    static final String CASE_END = "end";
    static final String CASE_CONTAIN = "contain";
    private static boolean isMatchdispatch(String url, String type, String value) {
        switch (type) {
            case CASE_START:
                return isMatch(url, value, String::startsWith);
            case CASE_END:
                return isMatch(url, value,String::endsWith);
            case CASE_CONTAIN:
                return isMatch(url, value, String::contains);
            default:
                return false;
        }
    }

    private static boolean isMatch(String url, String value,OnMatchAction action) {
        if(!TextUtils.isEmpty(url)){
            ArrayList<Map<String,String>> rules = StringManager.getListMapByJson(value);
            for(Map<String,String> map:rules){
                if(action.isMatch(url,map.get("")))
                    return true;
            }
        }
        return false;
    }

    interface OnMatchAction{
        boolean isMatch(String url,String rule);
    }
}
