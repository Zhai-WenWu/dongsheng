package amodule.answer.activity;

import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.xiangha.R;

import acore.override.activity.base.WebActivity;
import aplug.web.tools.JsAppCommon;
import aplug.web.tools.WebviewManager;

/**
 * 问答项目所有的全屏的Web页面
 * Created by sll on 2017/7/17.
 */

public class AskAnswerFullWebActivity extends WebActivity {

    private JsAppCommon mJsAppCommon;
    private String mUrl = "";
    private String mTabFlag = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        super.onCreate(savedInstanceState);
        initActivity("",0,0,0, R.layout.a_full_screen_web);
        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null) {
            mTabFlag =  bundle.getString("tab");
            mUrl = bundle.getString("url");
        }
        webViewManager = new WebviewManager(this,loadManager,true);
        webview = webViewManager.createWebView(R.id.XHWebview);
        webViewManager.setJSObj(webview, mJsAppCommon=new JsAppCommon(this, webview,loadManager,barShare));
        mJsAppCommon.setUrl(mUrl);
        // 设置加载
        loadManager.setLoading(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadData();
            }
        });
    }

    @Override
    public void loadData() {
        loadManager.setLoading(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selfLoadUrl(mUrl, true);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
