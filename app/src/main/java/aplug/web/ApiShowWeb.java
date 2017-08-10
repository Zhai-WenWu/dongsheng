package aplug.web;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xianghatest.R;

import java.util.Map;

import acore.override.activity.base.WebActivity;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;
import aplug.web.tools.JSAction;
import aplug.web.tools.JsAppCommon;
import aplug.web.tools.WebviewManager;
import third.share.BarShare;
import xh.basic.internet.UtilInternet;
import xh.basic.tool.UtilString;

public class ApiShowWeb extends WebActivity {
	protected String  htmlData = "",apiUrl = "",name = "";
	protected Button rightBtn;
	protected ImageView favoriteNousImageView;
	protected String nousCodeString = "";
	protected RelativeLayout shareLayout;
	protected RelativeLayout favLayout,homeLayout;
	protected TextView favoriteNousTextView,title;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle bundle = this.getIntent().getExtras();
		// 正常调用
		if (bundle != null) {
			apiUrl = bundle.getString("url");
			name = bundle.getString("name");
			JSAction.loadAction = bundle.getString("doJs") != null ? bundle.getString("doJs") : "";
		}
		initActivity("", 2, 0, R.layout.c_view_bar_nouse_title, R.layout.xh_webview);
		webViewManager = new WebviewManager(this,loadManager,false);
		webview = webViewManager.createWebView(R.id.XHWebview);
		webViewManager.setJSObj(webview, new JsAppCommon(this, webview,loadManager,barShare));
		initTitleView();
		setTitle();
		// 设置加载
		loadManager.setLoading(new OnClickListener() {
			@Override
			public void onClick(View v) {
				loadData();
			}
		});
		webview.upWebViewNum();
	}
	
	private void initTitleView(){
		title = (TextView)findViewById(R.id.title);
		rightBtn = (Button) findViewById(R.id.rightBtn1);
		shareLayout = (RelativeLayout) findViewById(R.id.shar_layout);
		favLayout = (RelativeLayout) findViewById(R.id.fav_layout);
		homeLayout = (RelativeLayout) findViewById(R.id.home_layout);
//		收藏按钮图片
		favoriteNousImageView = (ImageView) findViewById(R.id.img_fav);
		favoriteNousTextView = (TextView) findViewById(R.id.tv_fav);
	}
	
	protected void setTitle() {}

	
	@Override
	public void loadData() {
		ReqInternet.in().doGet(apiUrl, new InternetCallback(this) {
			@Override
			public void loaded(int flag, String url, Object returnObj) {
				if (flag >= UtilInternet.REQ_OK_STRING) {
					Map<String, String> am = UtilString.getListMapByJson(returnObj).get(0);
					String content = am.get("content");
					if(content.length() > 28){
						content = content.substring(0,28)+"..."+"(香哈菜谱)";
					}else{
						content = content + "(香哈菜谱)";
					}
					htmlData = am.get("html");
					webview.loadDataWithBaseURL("http://nativeapp.xiangha.com/", htmlData, "text/html", "utf-8", null);
					barShare = new BarShare(ApiShowWeb.this, name,"");
					String zhishiurl = am.get("url");
					barShare.setShare(BarShare.IMG_TYPE_WEB, am.get("title"), content, am.get("img"), zhishiurl);
					rightBtn.setVisibility(View.VISIBLE);
				} else
					rightBtn.setVisibility(View.GONE);
			}
		});
	}

}
