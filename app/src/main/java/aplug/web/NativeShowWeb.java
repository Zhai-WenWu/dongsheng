package aplug.web;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.xiangha.R;

import acore.logic.AppCommon;
import acore.logic.UrlFilter;
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
        webView.setWebViewClient(new WebViewClient() {
            // 设置title
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                String title = view.getTitle();
                if (!TextUtils.isEmpty(title)) {
                    ((TextView)findViewById(R.id.title)).setText(title);
//                    titleView.setCenterText(title);
                }
            }
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.i("yule", "shouldOverrideUrlLoading: " + "url =" + url);
                final String newUrl = UrlFilter.filterAdToDownloadUrl(url);
                if (!TextUtils.isEmpty(newUrl)){
                    Log.i("yule", "shouldOverrideUrlLoading: " + "开始下载" + newUrl);
                    AppCommon.openUrl(newUrl + "&showDialog=2",false);
                    return true;
                }
                if (shouldOverrideUrlLoadingByApp(view, url)) {
                    return true;
                }
                return super.shouldOverrideUrlLoading(view, url);
            }

            /**
             * 根据url的scheme处理跳转第三方app的业务
             */
            private boolean shouldOverrideUrlLoadingByApp(WebView view, String url) {
                Log.i("yule", "shouldOverrideUrlLoadingByApp: " + "url =" + url);
                if (url.startsWith("http") || url.startsWith("https") || url.startsWith("ftp")) {
                    //不处理http, https, ftp的请求
                    return false;
                }
                Intent intent;
                try {

                    intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                    view.getContext().startActivity(intent);
                } catch (Exception e) {
                    Log.i("yule", "shouldOverrideUrlLoadingByApp: " + "e" + e);
                    return false;
                }
                return true;
            }
        });

        WebSettings settings = webView.getSettings();
        settings.setDefaultZoom(WebSettings.ZoomDensity.MEDIUM);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        settings.setAppCacheEnabled(false);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setSavePassword(false);//解决因用户输入信息，导致H5出错

        settings.setSupportZoom(false);
        settings.setBuiltInZoomControls(false);
        settings.setJavaScriptEnabled(true);
        settings.setSavePassword(false);
        settings.setDefaultTextEncodingName("utf-8");
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
        settings.setTextZoom(100);//不跟随系统字体

        //兼容https,在部分版本上资源显示不全的问题
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        if(!TextUtils.isEmpty(url)) {
            webView.loadUrl(url);
        }
    }

    private void initData() {
        Intent intent = getIntent();
        if(intent == null) return;
        url = intent.getStringExtra("url");
    }
}
