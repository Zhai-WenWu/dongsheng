package aplug.web;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.webkit.WebView;

import com.xiangha.R;

import acore.override.activity.base.BaseActivity;

/**
 * PackageName : aplug.web
 * Created by MrTrying on 2017/8/7 16:53.
 * E_mail : ztanzeyu@gmail.com
 */

public class NativeShowWeb extends BaseActivity {

    private WebView webView ;

    private String url = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActivity("",2,0, R.layout.c_view_bar_nouse_title,R.layout.a_native_web_layout);
        initData();
        initView();
    }

    private void initView() {
        webView = (WebView) findViewById(R.id.webview);

        if(!TextUtils.isEmpty(url))
            webView.loadUrl(url);
    }

    private void initData() {
        Intent intent = getIntent();
        if(intent == null) return;
        url = intent.getStringExtra("url");
    }
}
