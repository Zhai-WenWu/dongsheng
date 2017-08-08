package aplug.web;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.xianghatest.R;

import acore.logic.XHClick;
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

    @Override
    protected void onDestroy() {
        long nowTime = System.currentTimeMillis();
        if (startTime > 0 && (nowTime - startTime) > 0 && !TextUtils.isEmpty(data_type) && !TextUtils.isEmpty(module_type)) {
            XHClick.saveStatictisFile("FullScreenWeb", module_type, data_type, code, "", "stop", String.valueOf((nowTime - startTime) / 1000), "", "", "", "");
        }
        super.onDestroy();
    }
}
