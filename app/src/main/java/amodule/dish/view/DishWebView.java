package amodule.dish.view;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import acore.tools.FileManager;
import acore.tools.Tools;
import amodule.dish.tools.DishMouldControl;
import aplug.web.tools.JsAppCommon;
import aplug.web.tools.WebviewManager;
import aplug.web.view.XHWebView;

/**
 * Created by Fang Ruijiao on 2017/7/17.
 */
public class DishWebView extends XHWebView {

    public static final String TAG = "dishMould";

    private String dishCode;
    private String mHtmlData;

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

    protected void init(){
        WebviewManager.initWebSetting(this);
        WebviewManager.setWebChromeClient(this,null);
        JsAppCommon jsObj = new JsAppCommon((Activity) this.getContext(),this,null,null);
        addJavascriptInterface(jsObj, jsObj.TAG);
        setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
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
        Log.i(TAG,"loadDishData() htmlStr:" + htmlStr);
        if(TextUtils.isEmpty(htmlStr)){
            loadMould(code);
            return;
        }
        loadDataWithBaseURL(null,htmlStr,"text/html","utf-8", null);
    }

    public boolean saveDishData(){
        Log.i(TAG,"saveDishData()");
        Tools.showToast(getContext(),"saveDishData()");
        if(TextUtils.isEmpty(dishCode) || TextUtils.isEmpty(mHtmlData)){
            return false;
        }
        String path = DishMouldControl.getOffDishPath() + dishCode;
        Log.d(TAG,"path:" + path);
        FileManager.saveFileToCompletePath(path,mHtmlData,false);
        FileManager.saveShared(getContext(),FileManager.file_dishMould,dishCode,path);
        return true;
    }

    private void loadMould(final String code){
        DishMouldControl.getDishMould(new DishMouldControl.OnDishMouldListener() {
            @Override
            public void loaded(boolean isSucess, String data) {
                Log.d(TAG,"getDishMould() isSucess:" + isSucess);
                if(isSucess){
//                    Log.d(TAG,"loadMould() data:" + data);
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

    /**
     * 当h5所有加载完毕后回调，包括请求网络数据
     */
    public void onLoadFinishCallback(String html){
        mHtmlData = html;
    }
}
