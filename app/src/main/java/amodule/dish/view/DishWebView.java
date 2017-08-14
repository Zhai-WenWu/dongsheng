package amodule.dish.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.Map;

import acore.logic.AppCommon;
import acore.tools.FileManager;
import acore.tools.StringManager;
import amodule.dish.tools.DishMouldControl;
import aplug.basic.ReqInternet;
import aplug.web.tools.JSAction;
import aplug.web.tools.JsAppCommon;
import aplug.web.tools.WebviewManager;
import aplug.web.view.XHWebView;
import third.mall.aplug.MallReqInternet;
import third.mall.aplug.MallStringManager;
import xh.basic.internet.UtilInternet;
import xh.basic.tool.UtilString;

import static amodule.main.Main.timer;
import static aplug.web.tools.WebviewManager.ERROR_HTML_URL;

/**
 *菜谱详情页webview
 */
public class DishWebView extends XHWebView {

    private Activity mAct;
    public static final String TAG = "zyj";

    private String dishCode;
    private String mHtmlData;
    private String mMouldVersion;

    public static final String OPEN_NEW = "OPEN_NEW";
    public static final String OPEN_SELF = "OPEN_SELF";

    private String mOpenFlag = OPEN_NEW;

    public DishWebView(Context context) {
        super(context);
        init(context);
    }

    public DishWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DishWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    protected void init(Context context){
        mAct = (Activity) context;
        WebviewManager.initWebSetting(this);


        setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(final WebView view, String url, Bitmap favicon) {
                if (!ERROR_HTML_URL.equals(url)) {
                    DishWebView.this.setUrl(url);
                }
                Log.i("zyj","onPageStarted");
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                Log.i("zyj","onPageFinished");
                super.onPageFinished(view, url);
                if (JSAction.loadAction.length() > 0) {
                    view.loadUrl("javascript:" + JSAction.loadAction + ";");
                    JSAction.loadAction = "";
                }
                if (url.indexOf(StringManager.api_exchangeList) != 0 && url.indexOf(StringManager.api_scoreList) != 0) {
                    //读取title设置title
                    view.loadUrl("javascript:window.appCommon.setTitle(document.title);");
                }
                if (timer != null) {
                    timer.cancel();
                    timer.purge();
                }
                // 获取焦点已让webview能打开键盘，评论页输入框不在webview，所以不获取焦点
                if (url.indexOf("subjectComment.php") == -1) {
//                    view.requestFocus();
                }
                // 读取cookie的sessionId
                CookieManager cookieManager = CookieManager.getInstance();
                Map<String, String> map = UtilString.getMapByString(cookieManager.getCookie(url), ";", "=");
                String sessionId = UtilInternet.cookieMap.get("USERID");
                if (map.get("USERID") != null && !map.get("USERID").equals(sessionId == null ? "" : sessionId)) {
                    UtilInternet.cookieMap.put("USERID", map.get("USERID"));
                }
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(dishWebViewCallBack != null) dishWebViewCallBack.onLoadFinish();
                    }
                },1*1000);

                view.setFocusable(false);
                /**有问题客户端关闭该功能	*/
                //获取网页高度，重新设置webview高度
//				webview.loadUrl("javascript:appCommon.resize(document.body.getBoundingClientRect().height)");
            }

            // 当前页打开
            @Override
            public boolean shouldOverrideUrlLoading(final WebView view, final String url) {
                if (OPEN_SELF.equals(mOpenFlag)) {
                    view.loadUrl(url);
                    return false;
                }
//                view.setFocusable(false);
                AppCommon.openUrl(mAct, url, true);
                return true;
            }


            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler sslhandler, SslError error) {
                sslhandler.proceed();
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                DishWebView.this.loadUrl(ERROR_HTML_URL);
            }
        });
        WebviewManager.setWebChromeClient(this,null);
        setHorizontalScrollBarEnabled(false);
        setVerticalScrollBarEnabled(false);
        JsAppCommon jsObj = new JsAppCommon((Activity) this.getContext(),this,null,null);
        addJavascriptInterface(jsObj, jsObj.TAG);
        setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
    }

    public void loadDishData(String code,String dishInfo,String authorInfo){
        if(TextUtils.isEmpty(code)){
            return;
        }
        dishCode = code;
        loadMould(code,dishInfo,authorInfo);
    }

    public String getMouldVersion(){
        return mMouldVersion;
    }
    /**
     * 根据code，加载模板
     * @param code
     */
    private void loadMould(final String code, final String dishInfo, final String authorInfo){
        DishMouldControl.getDishMould(new DishMouldControl.OnDishMouldListener() {
            @Override
            public void loaded(boolean isSucess, String data,String mouldVersion) {
                mMouldVersion = mouldVersion;
                if(isSucess){
                    data = data.replace("<{code}>",code);
//                    data = data.replace("<{dishMes}>",dishInfo);
//                    data = data.replace("<{customerMes}>",authorInfo);
                    final String html = data;
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            loadDataWithBaseURL(null,html,"text/html","utf-8", null);
//                            loadData(html,"text/html; charset=UTF-8", null);
//                            loadUrl("http://www.ixiangha.com:9813/test/main7/caipuInfo?code=89006552");
                        }
                    });
                }
            }
        });
    }

    /**
     * 当h5所有加载完毕后回调，包括请求网络数据
     * @param html ：要保存的内容
     */
    public void onLoadFinishCallback(String html){
        mHtmlData = html;
    }

    /**
     * 设置用料信息
     * @param ingreStr
     */
    public void setIngreStr(String ingreStr){
        if(dishWebViewCallBack != null) dishWebViewCallBack.setOnIngre(ingreStr);
    }

    /**
     * 设置拥有用料信息后，回调回去
     * @param listener
     */
    public void setWebViewCallBack(DishWebViewCallBack listener){
        dishWebViewCallBack = listener;
    }
    public DishWebViewCallBack dishWebViewCallBack;

    /**
     * webView 监听
     */
    public interface DishWebViewCallBack{
        //设置页面加载的数据
        public void setOnIngre(String ingre);
        public void onLoadFinish();
    }
}
