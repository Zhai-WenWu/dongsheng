package aplug.web.view;

import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebView;

public class XHWebView extends WebView {
	private String mUrl = "";
	private int webViewNum = 0;

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
	
	public void upWebViewNum(){
		webViewNum++;
	}
	
	public void downWebViewNum(){
		webViewNum--;
	}

	

}
