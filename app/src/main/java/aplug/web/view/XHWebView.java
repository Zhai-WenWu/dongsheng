package aplug.web.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.WebView;


import java.util.Map;

public class XHWebView extends WebView {
    private String mUrl = "";
    private int webViewNum = 0;
    private OnWebNumChangeCallback onWebNumChangeCallback;

    public XHWebView(Context context) {
        super(context);
    }

    public XHWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public XHWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public String getmUrl() {
        return mUrl;
    }

    public void setUrl(String mUrl) {
        this.mUrl = mUrl;
    }

    public int getWebViewNum() {
        return webViewNum;
    }

    public void setWebViewNum(int webViewNum) {
        this.webViewNum = webViewNum;
    }

    public void upWebViewNum() {
        webViewNum++;
        if (onWebNumChangeCallback != null)
            onWebNumChangeCallback.onChange(webViewNum);
    }

    public void downWebViewNum() {
        webViewNum--;
        if (onWebNumChangeCallback != null)
            onWebNumChangeCallback.onChange(webViewNum);
    }

    @Override
    public String getUrl() {
        return mUrl;
    }

    @Override
    public void loadUrl(String url) {
        super.loadUrl(url);
        if (url == null)
            return;
        if (url.startsWith("http"))
            this.mUrl = url;
    }

    @Override
    public void loadUrl(String url, Map<String, String> additionalHttpHeaders) {
        super.loadUrl(url, additionalHttpHeaders);
        if (url == null)
            return;
        if (url.startsWith("http"))
            this.mUrl = url;
    }

    public interface OnWebNumChangeCallback {
        void onChange(int num);
    }

    public OnWebNumChangeCallback getOnWebNumChangeCallback() {
        return onWebNumChangeCallback;
    }

    public void setOnWebNumChangeCallback(OnWebNumChangeCallback onWebNumChangeCallback) {
        this.onWebNumChangeCallback = onWebNumChangeCallback;
    }

    private String mBackData;

    public void setBackData(String loadUrl) {
        mBackData = loadUrl;
    }

    public boolean handleBackSelf() {
        return !TextUtils.isEmpty(mBackData);
    }

    public void handleBack() {
        loadUrl(mBackData);
        mBackData = null;
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);

        float webcontent = getContentHeight() * getScale();// webview的高度
        float webnow = getHeight() + getScrollY();// 当前webview的高度
        if (getScrollY() == 0) {
            scrollChanged.scrollTop();
        } else {
            scrollChanged.scroll();
        }

    }

    ScrollChanged scrollChanged;

    public void setScrollChanged(ScrollChanged scrollChanged) {
        this.scrollChanged = scrollChanged;
    }

    public interface ScrollChanged {
        void scrollTop();

        void scroll();
    }

}
