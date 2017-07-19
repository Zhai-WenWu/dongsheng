package amodule.dish.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import acore.logic.AppCommon;
import acore.tools.FileManager;
import aplug.web.view.XHWebView;

/**
 * Created by Fang Ruijiao on 2017/7/17.
 */

public class DishWebView extends XHWebView {

    private String TAG = "dishMould";

    private String dishCode,htmlData;

    public DishWebView(Context context) {
        super(context);
        init();
    }

    public DishWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DishWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init(){
        WebSettings webSettings = getSettings();
//        webSettings.setDomStorageEnabled(true);
//        webSettings.setDatabaseEnabled(true);
//        String dbPath = FileManager.getSDDir() + "long/dbPath";
//        webSettings.setDatabasePath(dbPath);

        webSettings.setJavaScriptEnabled(true);

        addJavascriptInterface(new InJavaScriptLocalObj(), "local_obj");
        setWebViewClient(new WebViewClient() {
            @Override
            public void onLoadResource(WebView view, String url) {
                Log.i(TAG, "onLoadResource url=" + url);

                super.onLoadResource(view, url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.i(TAG, "intercept url=" + url);
                view.loadUrl(url);
                return true;
            }

            // 页面开始时调用
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                Log.e(TAG, "onPageStarted");
                super.onPageStarted(view, url, favicon);
            }

            // 页面加载完成调用
            @Override
            public void onPageFinished(WebView view, String url) {
                view.loadUrl("javascript:window.local_obj.showSource('<head>'+" +
                        "document.getElementsByTagName('html')[0].innerHTML+'</head>');");
                super.onPageFinished(view, url);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode,String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                Log.e(TAG, "description:" + description + "    errorCode:" + errorCode + "    failingUrl:" + failingUrl);
            }
        });
    }

    public void loadDishData(String code){
        if(TextUtils.isEmpty(code)){
            return;
        }
        dishCode = code;
        String htmlPath = String.valueOf(FileManager.loadShared(getContext(),FileManager.file_dishMould,code));
        Log.i(TAG,"loadDishData() htmlPath:" + htmlPath);
        if(TextUtils.isEmpty(htmlPath)){
            loadMould(code);
            return;
        }
        String htmlStr = FileManager.readFile(htmlPath);
        if(TextUtils.isEmpty(htmlStr)){
            loadMould(code);
            return;
        }
//        loadData(htmlStr,"text/html; charset=UTF-8", null);
        loadDataWithBaseURL(null,htmlStr,"text/html","utf-8", null);
    }

    public boolean saveDishData(){
        if(TextUtils.isEmpty(htmlData) || TextUtils.isEmpty(dishCode)){
            return false;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                String path = FileManager.getSDDir() + "long/" + dishCode;
                Log.d(TAG,"path:" + path);
                FileManager.saveFileToCompletePath(path,htmlData,false);
                FileManager.saveShared(getContext(),FileManager.file_dishMould,dishCode,path);
            }
        }).start();
        return true;
    }

    private void loadMould(String code){
        String html = AppCommon.getDishMould();
        boolean isContains = html.contains("dishCode = '';");
        Log.d(TAG,"loadMould() isContains:" + isContains);
//        Log.d(TAG,"loadMould() replay:" + "dishCode = '" + code + "';");
//        html = html.replace("dishCode = '';","dishCode = '" + code + "';");
//        FileManager.saveFileToCompletePath(FileManager.getSDDir() + "long/html.txt",html,false);
//        loadData(html,"text/html; charset=UTF-8", null);
        loadDataWithBaseURL(null,html,"text/html","utf-8", null);
    }

    final class InJavaScriptLocalObj {
        @JavascriptInterface
        public void showSource(String html) {
            Log.d(TAG, html);
            htmlData = html;
            Log.d(TAG,"showSource() htmlData:" + htmlData);
        }
    }

}
