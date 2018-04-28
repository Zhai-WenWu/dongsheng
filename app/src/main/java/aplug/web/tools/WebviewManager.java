package aplug.web.tools;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.annimon.stream.Stream;
import com.xh.manager.DialogManager;
import com.xh.manager.ViewManager;
import com.xh.view.HButtonView;
import com.xh.view.MessageView;
import com.xh.view.TitleView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import acore.logic.AppCommon;
import acore.logic.UrlFilter;
import acore.logic.load.LoadManager;
import acore.override.XHApplication;
import acore.tools.LogManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import aplug.basic.ReqInternet;
import aplug.basic.XHConf;
import aplug.web.view.XHWebView;
import third.mall.aplug.MallReqInternet;
import third.mall.aplug.MallStringManager;
import xh.basic.internet.UtilInternet;
import xh.basic.tool.UtilString;

@SuppressLint({"JavascriptInterface", "SetJavaScriptEnabled"})
public class WebviewManager {
    public static final String ERROR_HTML_URL = "file:///android_asset/error.html";
    private Activity act;
    private LoadManager loadManager;
    private List<XHWebView> mWwebArray;
    private boolean state = true;

    private String mOpenMode;//1：当前WebView页面打开 2：APP内新页面打开 3：跳转到其他APP打开  标注：其他的走默认方式 默认："".

    /**
     * 初始化
     *
     * @param act
     * @param loadManager
     * @param state---true为正常web页面 false:为电商首页
     */
    public WebviewManager(Activity act, LoadManager loadManager, boolean state) {
        this.act = act;
        this.loadManager = loadManager;
        mWwebArray = new ArrayList<>();
        this.state = state;
    }

    public void setOpenMode(String openMode) {
        mOpenMode = openMode;
    }

    public XHWebView createWebView(int id) {
        return createWebView(id, true);
    }

    public XHWebView createWebView(int id, boolean isCookieSync) {
        if (act == null) {
            return null;
        }
        XHWebView webview = null;
        if (id > 0) {
            webview = (XHWebView) act.findViewById(id);
            if (null == webview) {
                throw new RuntimeException("Id : " + id + " , not found this id.");
            }
        } else {
            webview = new XHWebView(act);
        }

        if (isCookieSync) {
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.setAcceptCookie(true);
            cookieManager.removeSessionCookie();//移除无过期时间的cookie
        }
        //初始化WebSetting
        initWebSetting(webview);
        webview.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        webview.clearCache(false);
        //设置WebViewClient
        setWebViewClient(webview);
        //设置WebChromeClient
        setWebChromeClient(webview, loadManager);
        mWwebArray.add(webview);
        return webview;
    }

    @JavascriptInterface
    public void setJSObj(XHWebView webview, JsBase jsObj) {
        webview.addJavascriptInterface(jsObj, jsObj.TAG);
    }

    public void setJSObjs(XHWebView webview, JsBase[] jsObjs) {
        for (JsBase jsObj : jsObjs) {
            setJSObj(webview, jsObj);
        }
    }

    /**
     * 初始化WebSetting
     *
     * @param webview
     */
    public static void initWebSetting(XHWebView webview) {
        WebSettings settings = webview.getSettings();
        settings.setDefaultZoom(WebSettings.ZoomDensity.MEDIUM);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        settings.setAppCacheEnabled(false);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setSavePassword(false);//解决因用户输入信息，导致H5出错

        settings.setSupportZoom(false);
        settings.setBuiltInZoomControls(false);
        settings.setJavaScriptEnabled(true);
        settings.setSavePassword(false);
        settings.setDefaultTextEncodingName("utf-8");
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
        settings.setTextZoom(100);//不跟随系统字体

        //兼容https,在部分版本上资源显示不全的问题
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
    }

    /**
     * 设置WebViewClient
     *
     * @param webview
     */
    private void setWebViewClient(final XHWebView webview) {
        webview.setWebViewClient(new WebViewClient() {
            private Timer timer;
            private Handler handler = new Handler();

            @Override
            public void onPageStarted(final WebView view, String url, Bitmap favicon) {
                timer = new Timer();
                TimerTask tt = new TimerTask() {

                    @Override
                    public void run() {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (webview.getProgress() < 90) {
                                    if (!state) {
                                        loadManager.loadOver(UtilInternet.REQ_OK_STRING, 1, true);
                                    }
                                }
                            }
                        });
                    }
                };
                timer.schedule(tt, XHConf.net_timeout);
                if (!ERROR_HTML_URL.equals(url)) {
                    webview.setUrl(url);
                }
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (JSAction.loadAction.length() > 0) {
                    view.loadUrl("javascript:" + JSAction.loadAction + ";");
                    JSAction.loadAction = "";
                }
                if (url.indexOf(StringManager.api_exchangeList) != 0 && url.indexOf(StringManager.api_scoreList) != 0) {
                    //读取title设置title
                    view.loadUrl("javascript:window.appCommon.setTitle(document.title);");
                }
                loadManager.loadOver(UtilInternet.REQ_OK_STRING, 1, true);
                if (timer != null) {
                    timer.cancel();
                    timer.purge();
                }
                // 获取焦点已让webview能打开键盘，评论页输入框不在webview，所以不获取焦点
                if (url.indexOf("subjectComment.php") == -1) {
                    view.requestFocus();
                }
                // 读取cookie的sessionId

                CookieManager cookieManager = CookieManager.getInstance();
                Map<String, String> map = UtilString.getMapByString(cookieManager.getCookie(url), ";", "=");
                String sessionId = UtilInternet.cookieMap.get("USERID");
                if (map.get("USERID") != null && !map.get("USERID").equals(sessionId == null ? "" : sessionId)) {
                    UtilInternet.cookieMap.put("USERID", map.get("USERID"));
                }
                if (onWebviewLoadFinish != null) {
                    onWebviewLoadFinish.onLoadFinish();
                }
                /**有问题客户端关闭该功能	*/
                //获取网页高度，重新设置webview高度
//				webview.loadUrl("javascript:appCommon.resize(document.body.getBoundingClientRect().height)");
            }

            // 当前页打开
            @Override
            public boolean shouldOverrideUrlLoading(final WebView view, final String url) {
                if (!TextUtils.isEmpty(mOpenMode)) {
                    switch (mOpenMode) {
                        case "1"://当前WebView内打开
                            if (!TextUtils.isEmpty(url) && url.startsWith("http"))
                                view.loadUrl(url);
                            return true;
                        case "2"://APP内新页面打开
                            AppCommon.openUrl(act, url, true);
                            return true;
                        case "3"://跳转到其他APP打开，不处理
                            return super.shouldOverrideUrlLoading(view, url);
                    }
                } else {
                    final String newUrl = UrlFilter.filterAdDownloadUrl(url);
                    if (state) {
                        loadManager.setLoading(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                AppCommon.openUrl(act, newUrl, true);
                            }
                        });
                        if (!TextUtils.equals(newUrl, url))
                            loadManager.hideProgressBar();
                    } else {
                        AppCommon.openUrl(act, newUrl, true);
                    }
                }
                return true;
            }

            @Override
            public void onReceivedSslError(WebView webView, SslErrorHandler sslErrorHandler, SslError sslError) {
                sslErrorHandler.proceed();
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                webview.loadUrl(ERROR_HTML_URL);
            }
        });
    }

    /**
     * 设置WebChromeClient
     *
     * @param webview
     */
    public static void setWebChromeClient(final XHWebView webview, final LoadManager loadManager) {
        webview.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                Tools.showToast(view.getContext(), message);
                result.cancel();
                if (loadManager != null) loadManager.hideProgressBar();
                return true;
            }

            @Override
            public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
                showTip(message, result);
                return true;
            }

//            @Override
//            public void onShowCustomView(View view, IX5WebChromeClient.CustomViewCallback customViewCallback) {
//                super.onShowCustomView(view, customViewCallback);
//            }

            //弹出提示
            private void showTip(String message, final JsResult result) {
                final DialogManager dialogManager = new DialogManager(webview.getContext());
                dialogManager.createDialog(new ViewManager(dialogManager)
                        .setView(new TitleView(webview.getContext()).setText("提示"))
                        .setView(new MessageView(webview.getContext()).setText(message))
                        .setView(new HButtonView(webview.getContext())
                                .setNegativeText(android.R.string.cancel, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        dialogManager.cancel();
                                        result.cancel();
                                    }
                                })
                                .setPositiveText(android.R.string.ok, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        dialogManager.cancel();
                                        result.confirm();
                                    }
                                }))).setCancelable(false).show();
            }

        });
    }

    public static void syncXHCookie() {
        Map<String, String> header = ReqInternet.in().getHeader(XHApplication.in());
       //YLKLog.i("tzy", "syncXHCookie: header = " + header);
        String cookieStr = Uri.decode(header.containsKey("XH-Client-Data") ? header.get("XH-Client-Data") : "");
        Map<String, String> cookieMap = StringManager.getFirstMap(cookieStr);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        Stream.of(cookieMap)
                .forEach(value -> {
                    String cookieValue = value.getKey() + "=" + value.getValue();
                    cookieManager.setCookie(StringManager.domain, cookieValue);
                });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.flush();
        } else {
            CookieSyncManager.createInstance(XHApplication.in());
            CookieSyncManager.getInstance().sync();
        }
        LogManager.print("tzy", "d", "设置webview的cookie：" + cookieStr);
    }

    public static void syncDSCookie() {
        Map<String, String> header = MallReqInternet.in().getHeader(XHApplication.in());
        String cookieKey_mall = MallStringManager.mall_web_apiUrl.replace(MallStringManager.appWebTitle, "");
        String cookieStr = Uri.decode(header.containsKey("XH-Client-Data") ? header.get("XH-Client-Data") : "");
        Map<String, String> cookieMap = StringManager.getFirstMap(cookieStr);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        Stream.of(cookieMap)
                .forEach(value -> {
                    String cookieValue = value.getKey() + "=" + value.getValue();
                    cookieManager.setCookie(cookieKey_mall, cookieValue);
                });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.flush();
        } else {
            CookieSyncManager.createInstance(XHApplication.in());
            CookieSyncManager.getInstance().sync();
        }
        LogManager.print(XHConf.log_tag_net, "d", "设置webview的cookie：" + cookieStr);
    }

    private OnWebviewLoadFinish onWebviewLoadFinish;

    public interface OnWebviewLoadFinish {
        public void onLoadFinish();
    }

    public void setOnWebviewLoadFinish(OnWebviewLoadFinish onWebviewLoadFinish) {
        this.onWebviewLoadFinish = onWebviewLoadFinish;
    }
}
