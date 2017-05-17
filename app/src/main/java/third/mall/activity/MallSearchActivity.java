package third.mall.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.Map;

import acore.logic.XHClick;
import acore.override.activity.base.BaseActivity;
import acore.tools.FileManager;
import acore.tools.LogManager;
import aplug.basic.ReqInternet;
import aplug.basic.XHConf;
import aplug.web.tools.JsAppCommon;
import aplug.web.tools.WebviewManager;
import aplug.web.view.XHWebView;
import third.mall.aplug.MallClickContorl;
import third.mall.aplug.MallInternetCallback;
import third.mall.aplug.MallReqInternet;
import third.mall.aplug.MallStringManager;
import third.mall.tool.ToolFile;
import third.mall.view.MallSearchTitle;
import third.mall.view.MallSearchTitle.InterfaceCallBack;
import third.mall.view.SearchHistoryView;
import third.mall.view.SearchHistoryView.HistoryCallBack;
import third.mall.view.SearchHotView;
import third.mall.view.SearchHotView.interfaceCallBack;
import xh.basic.tool.UtilFile;
import xh.basic.tool.UtilString;

/**
 * 电商搜索
 * @author yujian
 *
 */
public class MallSearchActivity extends BaseActivity{

	private MallSearchTitle search_title;
	private SearchHotView hot_view;
	private SearchHistoryView history_view;
	public XHWebView webview = null;
	public WebviewManager webViewManager = null;
	private ArrayList<String> list_statistic= new ArrayList<String>();
	private String url;
	private String mall_stat_statistic;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initActivity("", 3, 0, 0, R.layout.a_mall_search);
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			for(int i= 1;i<100;i++){
				if(!TextUtils.isEmpty(bundle.getString("fr"+i))){
					list_statistic.add("fr"+i+"="+bundle.getString("fr"+i));
				}else{
					break;
				}
			}
			if(!TextUtils.isEmpty(bundle.getString("xhcode"))){
				list_statistic.add("xhcode="+bundle.getString("xhcode"));
			}
		}
		webViewManager = new WebviewManager(this,loadManager,true);
		webview = webViewManager.createWebView(R.id.XHWebview);
		webViewManager.setJSObj(webview, new JsAppCommon(this, webview,loadManager,null));
		initView();
		initData();
	}
	/**
	 * 初始化view
	 */
	private void initView() {
		search_title= (MallSearchTitle) findViewById(R.id.search_title);
		hot_view = (SearchHotView) findViewById(R.id.hot_view);
		history_view = (SearchHistoryView) findViewById(R.id.history_view);
		setOnTouchListener(hot_view);
		setOnTouchListener(history_view);
	}
	private void setOnTouchListener(final View views ){
		views.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					InputMethodManager imm = (InputMethodManager)MallSearchActivity.this. getSystemService(INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(views.getWindowToken(), 0); //强制隐藏键盘  
					break;

				default:
					break;
				}
				return false;
			}
		});

	}
	/**
	 * 初始化数据
	 */
	private void initData() {
		//头
		search_title.setInterface(new InterfaceCallBack() {
			@Override
			public void setSearch(String content) {
				setShowData(true, content);
				XHClick.mapStat(MallSearchActivity.this, "a_mail_search","搜索","");
			}
			@Override
			public void delContent() {
				setShowData(false, "");
			}
			@Override
			public void back() {
				MallSearchActivity.this.finish();
			}
		});
		hot_view.setInterface(new interfaceCallBack() {
			
			@Override
			public void getData(String data) {
				search_title.ed_search_mall.setText(data);
				setShowData(true, data);
				XHClick.mapStat(MallSearchActivity.this, "a_mail_search","热门搜索","");
				
			}
		});
		history_view.setInterface(new HistoryCallBack() {
			
			@Override
			public void setdata(String data) {
				search_title.ed_search_mall.setText(data);
				setShowData(true, data);
				XHClick.mapStat(MallSearchActivity.this, "a_mail_search","历史搜索","");
			}
		});
		setHotRequest();
		search_title.setEditTextFocus(true);

	}
	@Override
	protected void onResume() {
		super.onResume();
	}
	
	/**
	 * 展示搜索结果
	 * @param state
	 */
	private void setShowData(boolean state,String theUrl){
		ToolFile.setSharedPreference(this, FileManager.MALL_SEARCH_HISTORY, theUrl);
		MallClickContorl.getInstance().setStatisticUrl(url, null,mall_stat_statistic, this);;
		String mall_stat=(String) UtilFile.loadShared(this, FileManager.MALL_STAT, FileManager.MALL_STAT);
		theUrl=MallStringManager.replaceUrl(MallStringManager.mall_web_search_index)+"?kw="+theUrl+"&page=1"+"&"+mall_stat;
		
		if(state){
			search_title.setEditTextFocus(false);
			selfLoadUrl(theUrl, true);
			webview.setVisibility(View.VISIBLE);
			hot_view.setVisibility(View.GONE);
			history_view.setVisibility(View.GONE);
		}else{
			webview.loadUrl("");
			webview.setVisibility(View.GONE);
			hot_view.setVisibility(View.VISIBLE);
			history_view.setVisibility(View.VISIBLE);
			history_view.initData();
		}
	}
	/**
	 * 请求热搜词
	 */
	private void setHotRequest(){
		url=MallStringManager.mall_hotWord;
		for (int i = 0,size= list_statistic.size(); i < size; i++) {
			if(i==0){
				url+="?"+list_statistic.get(i);
			}else url+="&"+list_statistic.get(i);
		}
		MallReqInternet.in().doGet(url, new MallInternetCallback(this) {

			@Override
			public void loadstat(int flag, String url, Object msg, Object... stat) {
				if(flag>=ReqInternet.REQ_OK_STRING){
					if(stat!=null&&stat.length>0&& !TextUtils.isEmpty((String)stat[0])){
						mall_stat_statistic=(String) stat[0];
					}
					ArrayList<Map<String,String>> list=UtilString.getListMapByJson(msg);
					hot_view.setData(list);
				}
			}
		});
	}
	public boolean selfLoadUrl(final String theUrl,boolean openThis){
		if (webview != null 
				&& loadManager != null
				&& openThis) {//openthis为false，不进行任何操作
			loadManager.setLoading(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if(theUrl.indexOf(MallStringManager.domain)>-1){//电商
						System.out.println("成功");
						Map<String,String> header=MallReqInternet.in().getHeader(MallSearchActivity.this);
						String cookieKey=MallStringManager.mall_web_apiUrl.replace(MallStringManager.appWebTitle, "");
						String cookieStr=header.containsKey("Cookie")?header.get("Cookie"):"";
						String[] cookie = cookieStr.split(";");
						CookieManager cookieManager = CookieManager.getInstance();
						cookieManager.setAcceptCookie(true);
						for (int i = 0; i < cookie.length; i++) {
							if(cookie[i].indexOf("device")==0) cookie[i]=cookie[i].replace(" ", "");
							LogManager.print(XHConf.log_tag_net,"d", "设置cookie："+i+"::"+cookie[i]);
							cookieManager.setCookie(cookieKey, cookie[i]);
						}
						CookieSyncManager.getInstance().sync();
						LogManager.print(XHConf.log_tag_net,"d", "设置webview的cookie："+cookieStr);
					}

					LogManager.print(XHConf.log_tag_net,"d","------------------打开网页------------------\n"+theUrl);
					webview.loadUrl(theUrl);
				}
			});
			return true;
		}
		return false;
	}
}
