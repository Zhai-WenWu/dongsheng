package acore.override.activity.base;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import java.util.Map;

import acore.override.XHApplication;
import acore.tools.LogManager;
import acore.tools.StringManager;
import aplug.basic.XHConf;
import aplug.basic.XHInternetCallBack;
import aplug.web.tools.JSAction;
import aplug.web.tools.WebviewManager;
import aplug.web.view.XHWebView;
import third.mall.aplug.MallStringManager;

public class WebActivity extends BaseActivity {
	private static WebActivity mShowWeb;
	public XHWebView webview = null;
	public WebviewManager webViewManager = null;

	public String shareCallback = "";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mShowWeb = this;
	}
	
	@Override
	protected void onResume() {
		// 处理在webview上的特殊返回
		if (webview != null) {
			if(JSAction.resumeAction.length() > 0){
				if(JSAction.resumeAction.indexOf("allDish_") == 0){
					webview.loadUrl("javascript:" + JSAction.resumeAction.replace("allDish_", "") + ";");
				}
			}
		}
		super.onResume();
	}
	
	@Override
	protected void onRestart() {
		super.onRestart();
		loadManager.hideProgressBar();
	}

	@Override
	protected void onPause() {
		super.onPause();
		if(null != loadManager){
			loadManager.hideProgressBar();
		}
	}
	
	@Override
	public OnClickListener getBackBtnAction() {
		return new OnClickListener() {
			@Override
			public void onClick(View v) {
				WebActivity.this.onBackPressed();
			}
		};
	}
	
	// 初始化加载webview，供子类重写
	public void loadData() {}
	
	@Override
	public void onBackPressed() {
		// 处理在webview上的特殊返回
		if (webview != null) {
			if (webview.handleBackSelf()) {
				webview.handleBack();
			} else if(JSAction.backAction.length() > 0){
				// 用JS来代替返回
				if (JSAction.backAction.indexOf("back_") == 0) {
					webview.loadUrl("javascript:" + JSAction.backAction.replace("back_", "") + ";");
				} else if (JSAction.backAction.startsWith("finish")) {
					this.finish();
					webview.downWebViewNum();
				} else { // 返回后要在上一个页面执行JS，或清空要执行的js
					JSAction.resumeAction = JSAction.backAction.equals("no") ? "" : JSAction.backAction;
					this.finish();
					webview.downWebViewNum();
				}
			} else if (webview.canGoBack()) {
				loadManager.showProgressBar();
				webview.downWebViewNum();
				webview.goBack();
			} else {
				this.finish();
				webview.downWebViewNum();
			}
			JSAction.backAction = "";
		} else {
			super.onBackPressed();
		}
	}
	
	@Override
	public void finish() {
		//播放视频是退出需要loadUrl("")，重置web停止播放
		if(webview != null){
			webview.loadUrl("");
		}
		mShowWeb = null;
		super.finish();
	}
	
	@Override
	protected void onDestroy() {
		if(webview != null){
			webview.stopLoading();
			webview.removeAllViews();
			webview.destroy();
		}
		super.onDestroy();
	}
	
	//供外部吊起reload
	public static void reloadWebView(){
		if(mShowWeb.webview != null) {
			setCookie(mShowWeb.webview.getUrl());
			mShowWeb.webview.reload();
		}
	}
	
	public boolean selfLoadUrl(final String theUrl,boolean openThis){
		if (webview != null  && loadManager != null && openThis ) {//openthis为false，不进行任何操作
			loadManager.setLoading(new OnClickListener() {
				@Override
				public void onClick(View v) {
					setCookie(theUrl);
					LogManager.print(XHConf.log_tag_net,"d","------------------打开网页------------------\n"+theUrl);
					if (TextUtils.isEmpty(theUrl))
						return;
					webview.loadUrl(theUrl);
				}
			});
			return true;
		}
		return false;
	}

	protected static void setCookie(String theUrl){
		if (TextUtils.isEmpty(theUrl))
			return;
		if(theUrl.contains(MallStringManager.domain) || theUrl.contains(StringManager.domain)
				|| theUrl.contains(".ixiangha.com")){
			Map<String,String> mapCookie= XHInternetCallBack.getCookieMap();
			String cookieKey=theUrl;
			if(theUrl.contains(MallStringManager.domain)){
				cookieKey=MallStringManager.mall_web_apiUrl.replace(MallStringManager.appWebTitle, "");
				if(!TextUtils.equals(MallStringManager.domain,MallStringManager.defaultDomain)){
					cookieKey = "m" + MallStringManager.domain;
				}
			}else if(theUrl.contains(StringManager.domain)){
				cookieKey=StringManager.domain;
				if(!TextUtils.equals(StringManager.domain,StringManager.defaultDomain)){
					cookieKey = StringManager.appWebTitle+StringManager.domain;
				}
			}
			CookieManager cookieManager = CookieManager.getInstance();
			cookieManager.setAcceptCookie(true);
			for(String str:mapCookie.keySet()){
				String temp=str+"="+mapCookie.get(str);
				if(temp.indexOf("device")==0) temp=temp.replace(" ", "");
				LogManager.print(XHConf.log_tag_net,"d", "设置cookie："+temp);
				cookieManager.setCookie(cookieKey, temp);
			}
			CookieSyncManager.createInstance(XHApplication.in().getApplicationContext());
			CookieSyncManager.getInstance().sync();
			LogManager.print(XHConf.log_tag_net,"d", "设置webview的cookie："+mapCookie.toString());
		}
	}

	protected void handleWebCallback(Map<String, String> data) {
		if (!TextUtils.isEmpty(shareCallback) && data != null) {
			webview.loadUrl("javascript:" + shareCallback + "(" + TextUtils.equals("2", data.get("status")) + "," + "\'" + data.get("callbackParams") + "\'" + ")");
			Log.i("tzy", "javascript:" + shareCallback + "(" + TextUtils.equals("2", data.get("status")) + "," + "\'" + data.get("callbackParams") + "\'" + ")");
		}
	}
}
