//package third.ad;
//
//import java.io.DataOutputStream;
//import java.net.HttpURLConnection;
//import java.net.URL;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.Map;
//
//import org.json.JSONArray;
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import third.ad.tools.AdWebView;
//import xh.basic.tool.UtilFile;
//import acore.override.activity.base.BaseActivity;
//import acore.tools.StringManager;
//import acore.tools.ToolsDevice;
//import android.content.Context;
//import android.content.Intent;
//import android.net.ConnectivityManager;
//import android.net.NetworkInfo;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.Handler.Callback;
//import android.os.Message;
//import android.telephony.TelephonyManager;
//import android.text.TextUtils;
//import android.util.DisplayMetrics;
//import android.view.Gravity;
//import android.view.View;
//import com.tencent.smtt.sdk.WebView;
//import com.tencent.smtt.sdk.WebViewClient;
//import android.widget.LinearLayout;
//
//public class JingdongAd extends AdParent{
//	private final String url = "http://bdsp.x.jd.com/adx/xiangha";
////	private StringBuffer params = new StringBuffer(
////			//请求id
////			"{\"id\":\"dummy_request_id\","
////				//id:展示id，唯一标识一个展示，tagid：广告位id，唯一标识一个广告位
////				+ "\"imp\":[{\"id\":\"dummy_imp_id\",\"tagid\":\"dummy_tagid\","
////							//广告位 宽，高，在屏幕位置
////							+ 	"\"banner\":{\"w\":660,\"h\":165,\"pos\":1}}],"
////				//操作系统， md5后的IMEI
////				+ "\"device\":{\"os\":\"Android\",\"didmd5\":\"865854025013313\","
////								//设备网络连接类型，设备制造商，设备硬件型号，硬件型号版本
////								+ 	"\"connectiontype\": 2,\"make\": \"Apple\",\"model\": \"MI 3\",\"hwv\": \"4.4.4 KTU84P\"}}");
//	private BaseActivity mAct;
//	private WebView mWebView;
//	private JSONObject mJsonArray;
//	private JSONObject mDeviceJsb;
//	private JSONArray mImpJsa;
//	private JSONObject mImpJsb;
//	private JSONObject mBannerJsb;
//
//	private Handler mHandler;
//
//	public JingdongAd(BaseActivity act,String tagId,WebView webView){
//		mAct = act;
//		mWebView = webView;
//		mJsonArray = new JSONObject();
//		mDeviceJsb = new JSONObject();
//		mImpJsa = new JSONArray();
//		mImpJsb = new JSONObject();
//		mBannerJsb = new JSONObject();
//
//		mHandler = new Handler(new Callback() {
//
//			@Override
//			public boolean handleMessage(Message msg) {
//				String data = msg.obj.toString();
////				data = data.replaceFirst("</script>", "</script><script type=\"text/javascript\">var screen_width = document.getElementsByTagName('html')[0].scrollWidth;var screen_height = document.getElementsByTagName('html')[0].scrollHeight ; alert(\"screen_width:\" + screen_width );alert(\"screen_height:\" +screen_height); </script>");
////				UtilFile.saveFileToCompletePath(Environment.getExternalStorageDirectory().getAbsolutePath() + "/hh.xtml", data, false);
//				mWebView.loadDataWithBaseURL("jd:ad", data, "text/html", "utf-8", null);
//				mWebView.setVisibility(View.VISIBLE);
//				return false;
//			}
//		});
//		initWebView(mWebView);
//		try {
//			mDeviceJsb.put("os", "Android");
//			//md5后的IMEI
//			mDeviceJsb.put("didmd5", ToolsDevice.getIMEI(mAct));
//			mDeviceJsb.put("connectiontype", getNetWork(mAct));
//			//设备制造商
//			mDeviceJsb.put("make", android.os.Build.MANUFACTURER);
//			//设备硬件型号
//			mDeviceJsb.put("model", android.os.Build.MODEL);
//			//硬件型号版本
//			mDeviceJsb.put("hwv", android.os.Build.VERSION.SDK);
//			mJsonArray.put("device", mDeviceJsb);
//
//			mBannerJsb.put("w", "660");
//			mBannerJsb.put("h", "165");
//			mImpJsb.put("banner", mBannerJsb);
//			//唯一标识一个广告位
//			mImpJsb.put("tagid", tagId);
//
//		} catch (JSONException e) {
//			e.printStackTrace();
//		}
//	}
//
//	/**
//	 * 是否有京东广告
//	 */
//	@Override
//	public boolean isShowAd(String adPlayId,AdIsShowListener listener) {
//		boolean isShow = super.isShowAd(adPlayId,listener);
//		listener.onIsShowAdCallback(this,isShow);
//		return isShow;
//	}
//
//	@Override
//	public void onResumeAd(){
//		onAdShow(TONGJI_JD);
//		new Thread(new Runnable() {
//			@Override
//			public void run() {
//				try {
//					Date dt= new Date();
//					Long time= dt.getTime();
//					//广告位id
//					mImpJsb.put("id", "" + time);
//					mImpJsa.put(mImpJsb);
//					mJsonArray.put("imp", mImpJsa);
//					time= dt.getTime();
//					//请求id
//					mJsonArray.put("id", "" + time);
//
//					String data = requestByPost(url,mJsonArray.toString());
//					if(data != ""){
//						ArrayList<Map<String, String>> array = StringManager.getListMapByJson(data);
//						String seatbid = array.get(0).get("seatbid");
//						String bid = StringManager.getListMapByJson(seatbid).get(0).get("bid");
//						Map<String, String>map = StringManager.getListMapByJson(bid).get(0);
//						String adm = map.get("adm");
//						Message msg = new Message();
//						msg.obj = adm;
//						mHandler.sendMessage(msg);
//					}
//
//				} catch (Throwable e) {
//					e.printStackTrace();
//				}
//			}
//		}).start();
//	}
//
//	private void initWebView(WebView webView){
//		/**  布局为4：2  */
//		DisplayMetrics dm = ToolsDevice.getWindowPx(mAct);
//		int adTipLayoutBottomImgMaxWidth = dm.widthPixels - (ToolsDevice.dp2px(mAct, 15) * 2);
//		int adTipLayoutBottomImgMaxHeight = adTipLayoutBottomImgMaxWidth / 4;
//		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(adTipLayoutBottomImgMaxWidth, adTipLayoutBottomImgMaxHeight);
//		params.gravity = Gravity.CENTER_HORIZONTAL;
//		webView.setLayoutParams(params);
//
//		webView.setWebViewClient(new MyWebViewClient(mAct));
//		webView.getSettings().setJavaScriptEnabled(true);
//		webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
////		webView.getSettings().setUseWideViewPort(true);
////		webView.setWebChromeClient(new WebChromeClient() {
////
////			@Override
////			public boolean onJsAlert(WebView view, String url, String message,JsResult result) {
////				return super.onJsAlert(view, url, message, result);
////			}
////		});
//	}
//
//	// Post方式请求
//	private String requestByPost(String httpUrl,String params) throws Throwable {
//	    // 请求的参数转换为byte数组
////	    String params = URLEncoder.encode(param, "UTF-8");
//	    byte[] postData = params.getBytes();
//	    // 新建一个URL对象
//	    URL url = new URL(httpUrl);
//	    // 打开一个HttpURLConnection连接
//	    HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
//	    // 设置连接超时时间
//	    urlConn.setConnectTimeout(5 * 1000);
//	    // Post请求必须设置允许输出
//	    urlConn.setDoOutput(true);
//	    // Post请求不能使用缓存
//	    urlConn.setUseCaches(false);
//	    // 设置为Post请求
//	    urlConn.setRequestMethod("POST");
//	    urlConn.setInstanceFollowRedirects(true);
//	    // 配置请求Content-Type
//	    urlConn.setRequestProperty("Content-Type",
//	            "application/x-www-form-urlencode");
//	    // 开始连接
//	    urlConn.connect();
//	    // 发送请求参数
//	    DataOutputStream dos = new DataOutputStream(urlConn.getOutputStream());
//	    dos.write(postData);
//	    dos.flush();
//	    dos.close();
//	    // 判断请求是否成功
//	    if (urlConn.getResponseCode() == 200) {
//	        // 获取返回的数据
//	        byte[] data = UtilFile.inputStream2Byte(urlConn.getInputStream());
//	        String returnData = new String(data, "UTF-8");
////	        Log.i(TAG_POST, "Post请求方式成功，返回数据如下：");
//	        return returnData;
//	    } else {
//	        return "";
//	    }
//	}
//
//	@Override
//	public void onPsuseAd() {
//
//	}
//
//
//	@Override
//	public void onDestroyAd() {
//		mHandler.removeCallbacksAndMessages(null);
//	}
//
//	private String getNetWork(Context context){
//		if (context != null) {
//			ConnectivityManager connectMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
//			NetworkInfo netInfo = connectMgr.getActiveNetworkInfo();
//			if (netInfo != null && netInfo.isConnectedOrConnecting()) {
//				switch (netInfo.getType()) {
//				case ConnectivityManager.TYPE_WIFI:
//					return "2";
//				case ConnectivityManager.TYPE_MOBILE:
//					switch (netInfo.getSubtype()) {
//					case TelephonyManager.NETWORK_TYPE_GPRS: // 联通2g
//					case TelephonyManager.NETWORK_TYPE_CDMA: // 电信2g
//					case TelephonyManager.NETWORK_TYPE_EDGE: // 移动2g
//					case TelephonyManager.NETWORK_TYPE_1xRTT:
//					case TelephonyManager.NETWORK_TYPE_IDEN:
//						return "4";
//					case TelephonyManager.NETWORK_TYPE_EVDO_A: // 电信3g
//					case TelephonyManager.NETWORK_TYPE_UMTS:
//					case TelephonyManager.NETWORK_TYPE_EVDO_0:
//					case TelephonyManager.NETWORK_TYPE_HSDPA:
//					case TelephonyManager.NETWORK_TYPE_HSUPA:
//					case TelephonyManager.NETWORK_TYPE_HSPA:
//					case TelephonyManager.NETWORK_TYPE_EVDO_B:
//					case TelephonyManager.NETWORK_TYPE_EHRPD:
//					case TelephonyManager.NETWORK_TYPE_HSPAP:
//						return "5";
//					case TelephonyManager.NETWORK_TYPE_LTE:
//						return "6";
//					default:
//						return "0";
//					}
//				}
//			}
//		}
//		return "0";
//	}
//
//	public class MyWebViewClient extends WebViewClient{
//
//		private BaseActivity mAct;
//
//		public MyWebViewClient(BaseActivity act){
//			mAct = act;
//		}
//
//		@Override
//		public boolean shouldOverrideUrlLoading(WebView view, String url) {
//			if (!TextUtils.isEmpty(url)) {
//				onAdClick(TONGJI_JD);
//				Intent intent = new Intent(mAct, AdWebView.class);
//				Bundle bundle = new Bundle();
//				// 开启url，同时识别是否是原生的
//				bundle.putString("url", url);
//				intent.putExtras(bundle);
//				mAct.startActivity(intent);
//				return true;
//			}
//			return super.shouldOverrideUrlLoading(view, url);
//		}
//
//	}
//
//}
