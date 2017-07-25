package amodule.dish.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;

import acore.tools.FileManager;
import amodule.dish.tools.DishMouldControl;
import aplug.web.view.XHWebView;

/**
 * Created by Fang Ruijiao on 2017/7/17.
 */

public class DishWebView extends XHWebView {

    private String TAG = "dishMould";

    private String dishCode;

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
        if(TextUtils.isEmpty(dishCode)){
            return false;
        }
        DishWebView.this.loadUrl("javascript:window.local_obj.saveSource('<head>'+" +
                "document.getElementsByTagName('html')[0].innerHTML+'</head>');");
        return true;
    }

    private void loadMould(final String code){
        DishMouldControl.getDishMould(new DishMouldControl.OnDishMouldListener() {
            @Override
            public void loaded(boolean isSucess, String data) {
                if(isSucess){
                    Log.d(TAG,"loadMould() data:" + data);
                    data = data.replace("<{code}>",code);
                    final String html = data;
                    DishWebView.this.post(new Runnable() {
                        @Override
                        public void run() {
//                            loadData(html,"text/html; charset=UTF-8", null);
                            loadDataWithBaseURL(null,html,"text/html","utf-8", null);
                        }
                    });
                }
            }
        });
    }

    final class InJavaScriptLocalObj {
        @JavascriptInterface
        public void saveSource(String html) {
            String path = DishMouldControl.getOffDishPath() + dishCode;
            Log.d(TAG,"path:" + path);
            FileManager.saveFileToCompletePath(path,html,false);
            FileManager.saveShared(getContext(),FileManager.file_dishMould,dishCode,path);
        }

    }

}
