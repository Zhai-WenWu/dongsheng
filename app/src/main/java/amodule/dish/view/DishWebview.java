package amodule.dish.view;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.xiangha.R;

import aplug.web.ShowWeb;
import aplug.web.tools.JsAppCommon;
import aplug.web.tools.WebviewManager;
import aplug.web.view.XHWebView;

/**
 * 菜谱详情-h5页面，——用于付费
 */
public class DishWebview extends RelativeLayout{
    private Context context;
    private XHWebView xhWebView;
    private WebviewManager webViewManager;
    private JsAppCommon jsAppCommon;
    private RelativeLayout dish_webview_rela;
    private String url="http://appweb.ixiangha.com:9813/qa/dishQaList?dishCode=90384610";
    public DishWebview(Context context) {
        super(context);
        init(context);
    }

    public DishWebview(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DishWebview(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }
    public void init(Context context){
        this.context= context;
        Log.i("xianghaTag","DishWebview::init");
        LayoutInflater.from(context).inflate(R.layout.view_dish_webview,this,true);
        dish_webview_rela= (RelativeLayout) findViewById(R.id.dish_webview_rela);
        xhWebView= (XHWebView) findViewById(R.id.dish_webview);
    }
    public void initWeb(Activity activity){
        Log.i("xianghaTag","DishWebview:::initWeb::");
//        webViewManager = new WebviewManager(activity,null,true);
//        xhWebView = webViewManager.createWebView(0);
        jsAppCommon=new JsAppCommon(activity, xhWebView,null,null);
        xhWebView.addJavascriptInterface(jsAppCommon, jsAppCommon.TAG);
        WebviewManager.initWebSetting(xhWebView);
//        webViewManager.setJSObj(xhWebView, );
        jsAppCommon.setUrl(url);
        xhWebView.loadUrl(url);
    }
}
