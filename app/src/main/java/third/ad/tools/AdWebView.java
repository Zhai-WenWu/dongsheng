package third.ad.tools;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiangha.R;

import acore.logic.XHClick;
import acore.override.activity.base.WebActivity;
import aplug.web.tools.JSAction;
import aplug.web.tools.JsAppCommon;
import aplug.web.tools.WebviewManager;

public class AdWebView extends WebActivity implements OnClickListener{
	
	protected String url = "", htmlData = "";
	protected ImageView favoriteNousImageView;
	protected String nousCodeString = "";
	protected RelativeLayout shareLayout;
	protected RelativeLayout favLayout,homeLayout;
	protected TextView favoriteNousTextView,title;
	
	private String tongjiId = "ad_jingdong";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle bundle = this.getIntent().getExtras();
		// 正常调用
		if (bundle != null) {
			url = bundle.getString("url");
			JSAction.loadAction = bundle.getString("doJs") != null ? bundle.getString("doJs") : "";
		}
		initActivity("", 2, 0, R.layout.ad_webview_item_title, R.layout.ad_webview);
		webViewManager = new WebviewManager(this,loadManager,true);
		webview = webViewManager.createWebView(R.id.XHWebview);
		webViewManager.setJSObj(webview, new JsAppCommon(this, webview,loadManager,barShare));
		htmlData = "";
		initTitleView();
		// 设置加载
		loadManager.setLoading(new OnClickListener() {
			@Override
			public void onClick(View v) {
				loadData();
			}
		});
		webview.upWebViewNum();
	}
	
	@Override
	protected void onRestart() {
		super.onRestart();
		loadManager.hideProgressBar();
		webview.upWebViewNum();
	}
	
	@Override
	public OnClickListener getBackBtnAction() {
		return new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				XHClick.mapStat(AdWebView.this,tongjiId, "点击返回", "");
				AdWebView.this.finish();
			}
		};
	}
	
	private void initTitleView(){
		View v = findViewById(R.id.rightImgBtn);
		v.setVisibility(View.VISIBLE);
		v.setOnClickListener(this);
		findViewById(R.id.ad_webview_more_ll).setOnClickListener(this);
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
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.rightImgBtn: //更多
			XHClick.mapStat(AdWebView.this,tongjiId, "点击更多", "");
			findViewById(R.id.ad_webview_more_ll).setVisibility(View.VISIBLE);
			break;
		case R.id.ad_webview_more_ll: //弹框其layout
			findViewById(R.id.ad_webview_more_ll).setVisibility(View.GONE);
			break;
		case R.id.ad_webview_freshen:
			XHClick.mapStat(AdWebView.this,tongjiId, "点击更多", "点击刷新");
			webview.reload();
			findViewById(R.id.ad_webview_more_ll).setVisibility(View.GONE);
			break;
		case R.id.ad_webview_browser:
			XHClick.mapStat(AdWebView.this,tongjiId, "点击更多", "点击在浏览器打开");
			Intent intent = new Intent();        
            intent.setAction("android.intent.action.VIEW");    
            Uri content_url = Uri.parse(webview.getmUrl());   
            intent.setData(content_url);  
            startActivity(intent);
            findViewById(R.id.ad_webview_more_ll).setVisibility(View.GONE);
			break;
		case R.id.ad_webview_cancel:
			XHClick.mapStat(AdWebView.this,tongjiId, "点击更多", "点击取消");
			findViewById(R.id.ad_webview_more_ll).setVisibility(View.GONE);
			break;
		}
	}

}
