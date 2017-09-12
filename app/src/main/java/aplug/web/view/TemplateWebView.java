package aplug.web.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
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

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import acore.logic.AppCommon;
import acore.logic.load.LoadManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import aplug.basic.XHConf;
import aplug.web.tools.JSAction;
import aplug.web.tools.JsAppCommon;
import aplug.web.tools.JsBase;
import aplug.web.tools.TemplateWebViewControl;
import aplug.web.tools.WebviewManager;
import aplug.web.tools.XHTemplateManager;
import xh.basic.internet.UtilInternet;
import xh.basic.tool.UtilString;

/**
 * 模版web view
 */
@SuppressLint({"JavascriptInterface", "SetJavaScriptEnabled"})
public class TemplateWebView extends XHWebView{
    private Activity mAct;
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
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW); }
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
            public boolean shouldOverrideUrlLoading(final WebView view, final String url) {
                AppCommon.openUrl(act, url, true);
                return true;
            }
            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler sslhandler, SslError error) {
                sslhandler.proceed();
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
                AlertDialog.Builder builder = new AlertDialog.Builder(TemplateWebView.this.getContext());
                builder.setTitle("提示");
                builder.setMessage(message);
                builder.setPositiveButton(android.R.string.ok, new AlertDialog.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        result.confirm();
                    }
                });
                builder.setNeutralButton(android.R.string.cancel, new AlertDialog.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        result.cancel();
                    }

                }).setCancelable(false).create().show();
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
                mMouldVersion = version;
                if(isSuccess){
                    if(originData!=null&&originData.length>0&&nowData!=null&&nowData.length>0) {
                        int lenght= originData.length;
                        int nowLenght=nowData.length;
                        for(int i=0;i<lenght;i++) {
                            data = data.replace(originData[i], i<nowLenght?nowData[i]:"");
                        }
                    }
                    final String html = data;
                    loadDataWithBaseURL(null,html,"text/html","utf-8", null);
                }
            }
        });
    }

    /**
     * 刷新
     * @param dataUrl
     */
    public void refreshWebviewMethod(String dataUrl){
        loadUrl(dataUrl);
    }
}
