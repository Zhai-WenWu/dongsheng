package acore.override.activity.base;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import java.util.Map;

import acore.tools.LogManager;
import acore.tools.StringManager;
import amodule.main.Main;
import aplug.basic.ReqInternet;
import aplug.basic.XHConf;
import aplug.web.tools.JSAction;
import aplug.web.tools.WebviewManager;
import aplug.web.view.XHWebView;
import third.mall.aplug.MallReqInternet;
import third.mall.aplug.MallStringManager;

public class WebActivity extends BaseActivity{
	private static WebActivity mShowWeb;
	public XHWebView webview = null;
	public WebviewManager webViewManager = null;
	
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
		// 程序如果未初始化但却有定时器执行，则停止它。主要用于外部吊起应用时
		if (Main.allMain == null && Main.timer != null) {
			Main.stopTimer();
		}
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
//		String cookieKey = StringManager.apiUrl.replace(StringManager.apiTitle, "").replace("/", "");
		if(theUrl.indexOf(MallStringManager.domain)>-1){//电商 ds.xiangha.com
			Map<String,String> header=MallReqInternet.in().getHeader(mShowWeb);
			String cookieKey_mall=MallStringManager.mall_web_apiUrl.replace(MallStringManager.appWebTitle, "");
			String cookieStr=header.containsKey("Cookie")?header.get("Cookie"):"";
			String[] cookie = cookieStr.split(";");
			CookieManager cookieManager = CookieManager.getInstance();
			cookieManager.setAcceptCookie(true);
			for (int i = 0; i < cookie.length; i++) {
				cookieManager.setCookie(cookieKey_mall, cookie[i]);
			}
			CookieSyncManager.getInstance().sync();
			LogManager.print(XHConf.log_tag_net,"d", "设置webview的cookie："+cookieStr);
		}else if (theUrl.indexOf(StringManager.domain) > -1) {//菜谱  .xiangha.com
			Map<String,String> header = ReqInternet.in().getHeader(mShowWeb);
			String cookieStr=header.containsKey("Cookie")?header.get("Cookie"):"";
			String[] cookie = cookieStr.split(";");
			CookieManager cookieManager = CookieManager.getInstance();
			for (int i = 0; i < cookie.length; i++) {
				cookieManager.setCookie(StringManager.domain, cookie[i]);
			}
			CookieSyncManager.getInstance().sync();
			LogManager.print(XHConf.log_tag_net,"d", "设置webview的cookie："+cookieStr);
		}
	}
}