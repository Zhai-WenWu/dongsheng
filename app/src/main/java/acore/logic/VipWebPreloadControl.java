package acore.logic;

import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;

import acore.override.helper.XHActivityManager;
import aplug.web.tools.JsAppCommon;
import aplug.web.tools.WebviewManager;
import aplug.web.view.XHWebView;

/**
 * PACKAGE_NAME: acore.logic
 * USER: shiliangliang
 * DATE: 2018/12/4
 * E_MAIL: sll879227535@gmail.com
 */
public class VipWebPreloadControl {

    private static volatile VipWebPreloadControl mInstance;
    private static final Object mObj = new Object();
    private XHWebView mXHWebView;

    private VipWebPreloadControl() {
        mXHWebView = new XHWebView(XHActivityManager.getInstance().getCurrentActivity());
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mXHWebView.setLayoutParams(lp);
        JsAppCommon appCommon = new JsAppCommon(XHActivityManager.getInstance().getCurrentActivity(), mXHWebView, null, null);
        mXHWebView.addJavascriptInterface(appCommon, appCommon.TAG);
        WebviewManager.initWebSetting(mXHWebView);
        mXHWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        mXHWebView.clearCache(false);
        WebviewManager.syncXHCookie();
    }

    public static synchronized VipWebPreloadControl getInstance() {
        if (mInstance == null) {
            synchronized (mObj) {
                if (mInstance == null) {
                    mInstance = new VipWebPreloadControl();
                    return mInstance;
                }
            }
        }
        return mInstance;
    }

    public void vipWebDelayed(long millis) {
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                mXHWebView.loadUrl("https://appweb.xiangha.com/vip/myvip?payset=2");
            }
        }, millis);
    }

}
