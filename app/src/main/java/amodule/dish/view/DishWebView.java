package amodule.dish.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.Map;

import acore.logic.AppCommon;
import acore.tools.FileManager;
import acore.tools.StringManager;
import amodule.dish.tools.DishMouldControl;
import aplug.web.tools.JSAction;
import aplug.web.tools.JsAppCommon;
import aplug.web.tools.WebviewManager;
import aplug.web.view.XHWebView;
import xh.basic.internet.UtilInternet;
import xh.basic.tool.UtilString;

import static amodule.main.Main.timer;
import static aplug.web.tools.WebviewManager.ERROR_HTML_URL;

/**
 * Created by Fang Ruijiao on 2017/7/17.
 */
public class DishWebView extends XHWebView {

    private Activity mAct;
    public static final String TAG = "dishMould";

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
        JsAppCommon jsObj = new JsAppCommon((Activity) this.getContext(),this,null,null);
        addJavascriptInterface(jsObj, jsObj.TAG);
        setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
    }

    public void loadDishData(String code){
        loadDishData(code,false);
    }

    public void loadDishData(String code,boolean isReadLocal){
        if(TextUtils.isEmpty(code)){
            return;
        }
        dishCode = code;
        if(isReadLocal) {
            String htmlPath = String.valueOf(FileManager.loadShared(getContext(), FileManager.file_dishMould, code));
            mMouldVersion = String.valueOf(FileManager.loadShared(getContext(), FileManager.file_dishMouldVersion, code));
            Log.i(TAG, "loadDishData() htmlPath:" + htmlPath);
            if (TextUtils.isEmpty(htmlPath)) {
                loadMould(code);
                return;
            }
            String htmlStr = FileManager.readFile(htmlPath);
            Log.i(TAG, "loadDishData() htmlStr:" + htmlStr);
            if (TextUtils.isEmpty(htmlStr)) {
                loadMould(code);
                return;
            }
            loadDataWithBaseURL(null,htmlStr,"text/html","utf-8", null);
        }else{
            loadMould(code);
        }
    }

    public String getMouldVersion(){
        return mMouldVersion;
    }

    public boolean saveDishData(){
        Log.i(TAG,"saveDishData()");
        if(TextUtils.isEmpty(dishCode) || TextUtils.isEmpty(mHtmlData)){
            return false;
        }
        String path = DishMouldControl.getOffDishPath() + dishCode;
        Log.d(TAG,"path:" + path);
        FileManager.saveFileToCompletePath(path,mHtmlData,false);
        FileManager.saveShared(getContext(),FileManager.file_dishMould,dishCode,path);
        FileManager.saveShared(getContext(),FileManager.file_dishMouldVersion,dishCode,mMouldVersion);
        return true;
    }

    public void deleteDishData(){
        if(TextUtils.isEmpty(dishCode)){
            return;
        }
        String path = DishMouldControl.getOffDishPath() + dishCode;
        FileManager.delDirectoryOrFile(path);
    }

    private void loadMould(final String code){
        DishMouldControl.getDishMould(new DishMouldControl.OnDishMouldListener() {
            @Override
            public void loaded(boolean isSucess, String data,String mouldVersion) {
                mMouldVersion = mouldVersion;
                Log.d(TAG,"getDishMould() isSucess:" + isSucess);
                if(isSucess){
                    data = data.replace("<{code}>",code);
                    final String html = data;
                    DishWebView.this.post(new Runnable() {
                        @Override
                        public void run() {
//                            Log.d(TAG,"loadMould() html:" + html);
//                            String path = DishMouldControl.getOffDishPath() + dishCode;
//                            FileManager.saveFileToCompletePath(path,html,false);
//                            loadData(html,"text/html; charset=UTF-8", null);
                            loadDataWithBaseURL(null,html,"text/html","utf-8", null);
                        }
                    });
                }
            }
        });
    }

    /**
     * 当h5所有加载完毕后回调，包括请求网络数据
     */
    public void onLoadFinishCallback(String html){
        mHtmlData = html;
    }

    public void setIngreStr(String ingreStr){
        if(mOnIngreListener != null) mOnIngreListener.setOnIngre(ingreStr);
    }

    public void setOnIngreListener(OnIngreListener listener){
        mOnIngreListener = listener;
    }
    public OnIngreListener mOnIngreListener;
    public interface OnIngreListener{
        public void setOnIngre(String ingre);
    }
}
