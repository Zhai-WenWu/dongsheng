package acore.logic;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.RelativeLayout;

import org.apache.http.protocol.HTTP;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import acore.logic.load.LoadManager;
import acore.override.activity.base.BaseActivity;
import acore.override.activity.mian.MainBaseActivity;
import acore.tools.StringManager;
import acore.tools.Tools;
import aplug.basic.ReqInternet;
import aplug.web.tools.WebviewManager;
import aplug.web.view.XHWebView;

/**
 * PackageName : acore.logic
 * Created by MrTrying on 2016/9/29 17:18.
 * E_mail : ztanzeyu@gmail.com
 */

public class SpecialWebControl {
    /** 概率最大值 */
    private static final int PROBABILITY_MAX = 100000;
    /** 默认名 */
    private static final String DEFAULT_NAME = "%E5%81%9A%E6%B3%95{2}";
    /** 替换字段1 */
    private static final String REPLACE_NAME = "{1}";
    /** 替换字段1 */
    private static final String REPLACE_RANDOM = "{2}";
    /** referer数组1 */
    private static final String[] referersArray_1 = {"http://m.sogou.com/web/searchList.jsp?&keyword={1}&bid=sogou-mobp-a3bf6e4db644",
            "http://so.m.sm.cn/s?q={1}&uc_param_str=dnntnwvepjbprsvdsme&from=ucframe",
            "http://www.baidu.com/from=844b/s?word={1}&oq={1}",
            "http://m.so.com/s?q={1}&src=suglist&srcg=360aphone&mso_from=360_browser"};
    /** referer数组2 */
    private static final String[] referersArray_2 = {"http://wisd.sogou.com/?from={2}a",
            "http://m.so.com/?tn=&from={2}k",
            "http://so.m.sm.cn/s?uc_param_str=dnntnwvepffrgibijbp{2}"};
    /** 储存次数信息 */
    public static Map<String, Integer> createCount = new HashMap<>();

    /**
     * @param context
     * @param type
     * @param name
     * @param code
     */
    public static void initSpecialWeb(Activity context, @NonNull String type, String name, String code) {
        String configData = AppCommon.getConfigByLocal("navToWebStat");
        Map<String, String> data = StringManager.getFirstMap(configData);
        if (data != null && data.containsKey(type)) {
            ArrayList<Map<String, String>> dataArray = StringManager.getListMapByJson(data.get(type));
            for (Map<String, String> urlMap : dataArray) {
                String url = urlMap.get("url");
                int maxCount = 2;
                try {
                    maxCount = Integer.parseInt(urlMap.get("num"));
                } catch (Exception e) {
                }
                if (!TextUtils.isEmpty(url)) {
                    String keyUrl = url;
                    if (!TextUtils.isEmpty(code)) {
                        keyUrl = url.replace("{code}", code);
                    }
                    createWeb(context, url, keyUrl, type, name, maxCount);
                }
            }
        }
    }

    /**
     * @param context
     * @param url
     */
    private static void createWeb(Activity context, String keyUrl, String url, @NonNull String type, String name, int maxCount) {
        try {
            LoadManager loadManager = null;
            RelativeLayout rootLayout = null;
            if (context != null) {
                if (context instanceof BaseActivity) {
                    loadManager = ((BaseActivity) context).loadManager;
                    rootLayout = ((BaseActivity) context).rl;
                } else if (context instanceof MainBaseActivity) {
                    loadManager = ((MainBaseActivity) context).loadManager;
                    rootLayout = ((MainBaseActivity) context).rl;
                }
            }
            if (loadManager == null || rootLayout == null) {
                return;
            }
            //是否能请求
            if (!canRequest(type, keyUrl, maxCount)) {
                return;
            }
            //同步cookie并获得webview
            XHWebView webView = syncCookie(context, loadManager, url);
            rootLayout.addView(webView, 0, 0);
            //设置referer
            Map<String, String> referer = getWebReferer(name);
            if (referer == null) {
                webView.loadUrl(url);
            } else {
                webView.loadUrl(url, referer);
            }
        } catch (Exception e) {

        }
    }

    /**
     * 判断是否能请求
     *
     * @param type
     * @param keyUrl
     * @param maxCount
     *
     * @return
     */
    private static boolean canRequest(String type, String keyUrl, int maxCount) {
        if (createCount == null) {
            createCount = new HashMap<>();
        }
        int currentCount = 0;
        String key = type + keyUrl;
        if (createCount.containsKey(key)) {
            currentCount = createCount.get(key);
        }
        if (maxCount == -1 || currentCount >= maxCount) {
            return false;
        }
        currentCount++;
        createCount.put(key, currentCount);
        return true;
    }

    /**
     * 同步cookie
     *
     * @param context
     * @param loadManager
     * @param url
     *
     * @return
     */
    private static XHWebView syncCookie(Activity context, LoadManager loadManager, String url) {
        WebviewManager webviewManager = new WebviewManager(context, loadManager, false);
        XHWebView webView = webviewManager.createWebView(0);
        Map<String, String> header = ReqInternet.in().getHeader(context);
        String cookieStr = header.containsKey("Cookie") ? header.get("Cookie") : "";
        String[] cookie = cookieStr.split(";");
        CookieManager cookieManager = CookieManager.getInstance();
        for (int i = 0; i < cookie.length; i++) {
            cookieManager.setCookie(url, cookie[i]);
        }
        cookieManager.setCookie(url, "xhWebStat=1");
        CookieSyncManager.getInstance().sync();
        return webView;
    }

    /**
     * 获得Referer
     *
     * @param name
     *
     * @return
     *
     * @throws UnsupportedEncodingException
     */
    private static Map<String, String> getWebReferer(String name) throws UnsupportedEncodingException {
        Map<String, String> refererMap = new HashMap<>();
        final int probability = Tools.getRandom(1, PROBABILITY_MAX + 1);
        if (probability <= PROBABILITY_MAX * 0.13) {
            //0.13	不要referer
            refererMap = null;
        } else if (probability <= PROBABILITY_MAX * 0.35) {
            //0.22
            int randomIndex = Tools.getRandom(0, referersArray_1.length);
            if (TextUtils.isEmpty(name)) {
                name = DEFAULT_NAME.replace(REPLACE_RANDOM, String.valueOf(Tools.getRandom(1, PROBABILITY_MAX + 1)));
            } else {
                name = URLEncoder.encode(name, HTTP.UTF_8);
            }
            String refererStr = referersArray_1[randomIndex].replace(REPLACE_NAME, name);
            refererMap.put("Referer", refererStr);
        } else if (probability <= PROBABILITY_MAX * 0.97) {
            //0.62
            int randomIndex = Tools.getRandom(0, referersArray_2.length);
            String refererStr = referersArray_2[randomIndex].replace(REPLACE_RANDOM, String.valueOf(Tools.getRandom(1, PROBABILITY_MAX + 1)));
            refererMap.put("Referer", refererStr);
        } else if (probability <= PROBABILITY_MAX * 1) {
            //0.03
            refererMap.put("Referer", "http://m.xiangha.com/");
        }
        return refererMap;
    }
}
