package aplug.web;

import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.xiangha.R;

import acore.override.activity.base.WebActivity;
import aplug.web.tools.JSAction;
import aplug.web.tools.JsAppCommon;
import aplug.web.tools.WebviewManager;

/**
 * Created by Fang Ruijiao on 2017/3/15.
 */
public class FullScreenWeb extends WebActivity {

    private JsAppCommon jsAppCommon;
    protected String url = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        super.onCreate(savedInstanceState);
//        ToolsDevice.modifyStateTextColor(this);
        initActivity("",0,0,0,R.layout.a_full_screen_web);
        Bundle bundle = this.getIntent().getExtras();
        // 正常调用
        if (bundle != null) {
            url = bundle.getString("url");
            JSAction.loadAction = bundle.getString("doJs") != null ? bundle.getString("doJs") : "";
        }

        webViewManager = new WebviewManager(this,loadManager,true);
        webview = webViewManager.createWebView(R.id.XHWebview);
        webViewManager.setJSObj(webview, jsAppCommon=new JsAppCommon(this, webview,loadManager,barShare));
        jsAppCommon.setUrl(url);
        // 设置加载
        loadManager.setLoading(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadData();
            }
        });
    }

    /**
     * 重写initLoading,将自己传递给MainActivity 主要是获取自身的webView的url;
     */
    @Override
    public void loadData() {
        loadManager.setLoading(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selfLoadUrl(url, true);
            }
        });
    }
}
