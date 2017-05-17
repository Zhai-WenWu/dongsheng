package aplug.web;

import acore.override.activity.base.WebActivity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import aplug.web.tools.JSAction;
import aplug.web.tools.JsAppCommon;
import aplug.web.tools.WebviewManager;

import com.xiangha.R;

public class VideoShowWeb extends WebActivity {
	private String url = "";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle bundle = this.getIntent().getExtras();
		// 正常调用
		if (bundle != null) {
			url = bundle.getString("url");
			JSAction.loadAction = bundle.getString("doJs") != null ? bundle.getString("doJs") : "";
		}
		initActivity("", 2, 0, 0, R.layout.a_video_web_layout);
		webViewManager = new WebviewManager(this,loadManager,true);
		webview = webViewManager.createWebView(R.id.XHWebview);
		webViewManager.setJSObj(webview, new JsAppCommon(this, webview,loadManager,barShare));
		// 设置加载
		loadManager.setLoading(new OnClickListener() {
			@Override
			public void onClick(View v) {
				loadData();
			}
		});
		webview.upWebViewNum();
	}
	
	/**
	 * 重写initLoading,将自己传递给MainActivity 主要是获取自身的webView的url;
	 */
	@Override
	public void loadData() {
		loadManager.setLoading(new OnClickListener() {
			@Override
			public void onClick(View v) {
				selfLoadUrl(url, true);
			}
		});
		loadManager.hideProgressBar();
	}

}
