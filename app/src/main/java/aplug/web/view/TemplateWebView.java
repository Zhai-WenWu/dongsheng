package aplug.web.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;


import com.xh.manager.DialogManager;
import com.xh.manager.ViewManager;
import com.xh.view.HButtonView;
import com.xh.view.MessageView;
import com.xh.view.TitleView;

import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.load.LoadManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import amodule.dish.activity.DetailDishWeb;
import amodule.main.Main;
import aplug.web.tools.JSAction;
import aplug.web.tools.JsAppCommon;
import aplug.web.tools.JsBase;
import aplug.web.tools.TemplateWebViewControl;
import aplug.web.tools.XHTemplateManager;
import xh.basic.internet.UtilInternet;
import xh.basic.tool.UtilString;

/**
 * 模版web view
 */
@SuppressLint({"JavascriptInterface", "SetJavaScriptEnabled"})
public class TemplateWebView extends XHWebView{
    public static final String ERROR_HTML_URL = "file:///android_asset/error.html";
    private Activity act;
    private LoadManager loadManager;
    private OnWebviewStateCallBack onWebviewStateCallBack;
    private String mMouldVersion;
    public TemplateWebView(Context context) {
        super(context);
        init(context);
    }

    public TemplateWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public TemplateWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    /**
     * 初始化
     * @param context
     */
    private void init(Context context){
        initWeb();
    }

    public void initBaseData(Activity act,LoadManager loadManager){
        this.act = act;
        this.loadManager = loadManager;
        JsAppCommon jsObj = new JsAppCommon(act,this,null,null);
        setJSObj(jsObj);
        setWebViewClient();
        setWebChromeClient();
    }
    @JavascriptInterface
    public void setJSObj(JsBase jsObj) {
        this.addJavascriptInterface(jsObj, jsObj.TAG);
    }

    public void setWebViewCallBack(OnWebviewStateCallBack onWebviewStateCallBack){
        this.onWebviewStateCallBack=onWebviewStateCallBack;
    }
    public interface OnWebviewStateCallBack {
        public void onLoadFinish();
        public void onLoadStart();
    }
    /**
     * 初始化view
     */
    private void initWeb() {
        WebSettings settings = this.getSettings();
        settings.setDefaultZoom(WebSettings.ZoomDensity.MEDIUM);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        settings.setAppCacheEnabled(false);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);

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
        this.setVerticalScrollBarEnabled(false);
        this.setHorizontalScrollBarEnabled(false);
    }
    /**
     * 设置WebViewClient
     */
    private void setWebViewClient() {
        this.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(final WebView view, String url, Bitmap favicon) {
                Log.i("zyj","onPageStarted::");
                if (onWebviewStateCallBack != null) {
                    onWebviewStateCallBack.onLoadStart();
                }
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                Log.i("zyj","onPageFinished::");
                if (JSAction.loadAction.length() > 0) {
                    view.loadUrl("javascript:" + JSAction.loadAction + ";");
                    JSAction.loadAction = "";
                }
                if (url.indexOf(StringManager.api_exchangeList) != 0 && url.indexOf(StringManager.api_scoreList) != 0) {
                    //读取title设置title
                    view.loadUrl("javascript:window.appCommon.setTitle(document.title);");
                }
                if(loadManager!=null)
                loadManager.loadOver(UtilInternet.REQ_OK_STRING, 1, true);
//                // 获取焦点已让webview能打开键盘，评论页输入框不在webview，所以不获取焦点
//                if (url.indexOf("subjectComment.php") == -1) {
//                    view.requestFocus();
//                }
                // 读取cookie的sessionId
                CookieManager cookieManager = CookieManager.getInstance();
                Map<String, String> map = UtilString.getMapByString(cookieManager.getCookie(url), ";", "=");
                String sessionId = UtilInternet.cookieMap.get("USERID");
                if (map.get("USERID") != null && !map.get("USERID").equals(sessionId == null ? "" : sessionId)) {
                    UtilInternet.cookieMap.put("USERID", map.get("USERID"));
                }
                if (onWebviewStateCallBack != null) {
                    onWebviewStateCallBack.onLoadFinish();
                }
            }

            // 当前页打开
            @Override
            public boolean shouldOverrideUrlLoading(final WebView view, String url) {
                Log.i(Main.TAG,"url::"+url);
                String XH_PROTOCOL = "xiangha://welcome?";
                // 如果识别到外部开启链接，则解析
                if(act instanceof DetailDishWeb) {
                    if (url.startsWith(XH_PROTOCOL) && url.length() > XH_PROTOCOL.length()) {
                        String tmpUrl = url.substring(XH_PROTOCOL.length());
                        if (tmpUrl.startsWith("url=")) {
                            tmpUrl = tmpUrl.substring("url=".length());
                        }
                        if (TextUtils.isEmpty(tmpUrl)) {
                            url = StringManager.wwwUrl;
                        } else {
                            url = tmpUrl;
                        }
                    }
                    Log.i(Main.TAG, "url:22:" + url);
                    try {
                        if (url.contains("?")) {
                            Map<String, String> urlRule = AppCommon.geturlRule(act);
                            String[] urls = url.split("\\?");
                            String urlKey = urls[0];
                            if (urls[0].lastIndexOf("/") >= 0) {
                                urlKey = urls[0].substring(urls[0].lastIndexOf("/") + 1);
                            }
                            if (urlRule == null || urlRule.get(urlKey) == null) {
                                Log.i(Main.TAG, "url:33:" + url);
                                AppCommon.openUrl(act, url, true);
                            } else {
                                Log.i(Main.TAG, "name::" + act.getComponentName().getClassName());
                                Log.i(Main.TAG, "urlKey::" + urlKey + "::::" + urlRule.get(urlKey));
                                if (act != null && act.getComponentName().getClassName().equals(urlRule.get(urlKey))) {
                                    String params = url.substring(urlKey.length() + 1, url.length());
                                    Log.i(Main.TAG, "params::" + params);
                                    if (onTemplateCallBack != null) {
                                        onTemplateCallBack.readLoad(params);
                                    }
                                } else {
                                    Log.i(Main.TAG, "url:44:" + url);
                                    AppCommon.openUrl(act, url, true);
                                }
                            }

                        } else {
                            Log.i(Main.TAG, "url:55:" + url);
                            AppCommon.openUrl(act, url, true);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }else{
                    AppCommon.openUrl(act, url, true);
                }

                return true;
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                TemplateWebView.this.loadUrl(ERROR_HTML_URL);
            }
        });
    }
    /**
     * 设置WebChromeClient
     */
    private void setWebChromeClient() {
        this.setWebChromeClient(new WebChromeClient() {
                @Override
                public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                    Tools.showToast(view.getContext(), message);
                    result.cancel();
                    if(loadManager != null)loadManager.hideProgressBar();
                    return true;
                }

            @Override
            public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
                showTip(message, result);
                return true;
            }

            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                super.onShowCustomView(view, callback);
            }

            //弹出提示
            private void showTip(String message, final JsResult result) {
                final DialogManager dialogManager = new DialogManager(TemplateWebView.this.getContext());
                dialogManager.createDialog(new ViewManager(dialogManager)
                        .setView(new TitleView(TemplateWebView.this.getContext()).setText("提示"))
                        .setView(new MessageView(TemplateWebView.this.getContext()).setText(message))
                        .setView(new HButtonView(TemplateWebView.this.getContext())
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

    /**
     * 初始化数据
     * @param requestMethod
     * @param originData
     * @param nowData
     */
    public void loadData(String requestMethod, final String[] originData, final String[] nowData ){
        XHTemplateManager xhTemplateManager = new XHTemplateManager();
        xhTemplateManager.getSingleTemplate(requestMethod,new TemplateWebViewControl.MouldCallBack() {
            @Override
            public void load(boolean isSuccess, String data, String requestMothed, String version) {
                try {
                    mMouldVersion = version;
                    if (isSuccess) {
                        Log.i(Main.TAG, "模版开始渲染");
                        if (originData != null && originData.length > 0 && nowData != null && nowData.length > 0) {
                            int lenght = originData.length;
                            int nowLenght = nowData.length;
                            for (int i = 0; i < lenght; i++) {
                                data = data.replace(originData[i], i < nowLenght ? nowData[i] : "");
                            }
                        }
                        final String html = data;
                        loadDataWithBaseURL(null, html, "text/html", "utf-8", null);//当前位置可能内存溢出。
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
        //避免webview乱跳。
        this.setFocusable(false);
    }

    /**
     * 刷新
     * @param dataUrl
     */
    public void refreshWebviewMethod(String dataUrl){
        loadUrl(dataUrl);
    }

    public interface OnTemplateCallBack{
        public void readLoad(String param);
    }
    private OnTemplateCallBack onTemplateCallBack;
    public void setOnTemplateCallBack(OnTemplateCallBack onTemplateCallBack){
        this.onTemplateCallBack= onTemplateCallBack;
    }
}
