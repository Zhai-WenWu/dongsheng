package aplug.web;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.xiangha.R;

import java.util.Map;

import acore.logic.XHClick;
import acore.override.activity.base.WebActivity;
import acore.tools.IObserver;
import acore.tools.ObserverManager;
import aplug.web.tools.JSAction;
import aplug.web.tools.JsAppCommon;
import aplug.web.tools.WebviewManager;

/**
 * 全屏weview
 */
public class FullScreenWeb extends WebActivity implements IObserver {

    protected JsAppCommon jsAppCommon;
    protected String url = "";

    private String code = "";
    private String data_type = "";//推荐列表过来的数据
    private String module_type = "";
    private Long startTime;//统计使用的时间

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
            code = bundle.getString("code");
            data_type = bundle.getString("data_type");
            module_type = bundle.getString("module_type");
        }
        startTime = System.currentTimeMillis();

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

        ObserverManager.getInstance().registerObserver(this, ObserverManager.NOTIFY_YIYUAN_BIND, ObserverManager.NOTIFY_SHARE);
    }

    /**
     * 重写initLoading,将自己传递给MainActivity 主要是获取自身的webView的url;
     */
    @Override
    public void loadData() {
        if (TextUtils.isEmpty(url))
            return;
        loadManager.setLoading(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selfLoadUrl(url, true);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(null != loadManager){
            loadManager.hideProgressBar();
        }
    }

    @Override
    protected void onDestroy() {
        long nowTime = System.currentTimeMillis();
        if (startTime > 0 && (nowTime - startTime) > 0 && !TextUtils.isEmpty(data_type) && !TextUtils.isEmpty(module_type)) {
            XHClick.saveStatictisFile("FullScreenWeb", module_type, data_type, code, "", "stop", String.valueOf((nowTime - startTime) / 1000), "", "", "", "");
        }
        super.onDestroy();

        ObserverManager.getInstance().unRegisterObserver(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void finish() {
        super.finish();
    }

    @Override
    public void notify(String name, Object sender, Object data) {
        if (!TextUtils.isEmpty(name)) {
            switch (name) {
                case ObserverManager.NOTIFY_SHARE:
                    if (!TextUtils.isEmpty(shareCallback) && data != null) {
                        Map<String, String> dataMap = (Map<String, String>) data;
                        webview.loadUrl("javascript:" + shareCallback + "(" + TextUtils.equals("2", dataMap.get("status")) + "," + "\'" + dataMap.get("callbackParams") + "\'" + ")");
                        Log.i("tzy", "javascript:" + shareCallback + "(" + TextUtils.equals("2", dataMap.get("status")) + "," + "\'" + dataMap.get("callbackParams") + "\'" + ")");
                    }
                    break;
                case ObserverManager.NOTIFY_YIYUAN_BIND:
                    if (data != null) {
                        if (data instanceof Map) {
                            Map<String, String> state = (Map<String, String>) data;
                            if (TextUtils.equals("2", state.get("state")))
                                if (webview != null){
                                    setCookie(url);
                                    webview.loadUrl(url);
                                }
                        }
                    }
                    break;
            }
        }
    }
}
