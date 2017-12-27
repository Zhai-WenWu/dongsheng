package third.mall.aplug;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.LoginManager;
import acore.logic.XHApiMonitor;
import acore.logic.XHClick;
import acore.logic.load.LoadManager;
import acore.override.XHApplication;
import acore.tools.FileManager;
import acore.tools.LogManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import aplug.basic.XHConf;
import aplug.basic.XHInternetCallBack;
import xh.basic.internet.InterCallback;
import xh.basic.internet.UtilInternet;
import xh.basic.tool.UtilFile;

/**
 * 电商网络层回调
 * 
 * @author yu
 *
 */
public abstract class MallInternetCallback extends XHInternetCallBack {

	public MallInternetCallback() {
		super();
	}

	@Override
	public void loaded(int flag, String url, Object msg) {
		
	}
	public abstract void loadstat(int flag, String url, Object msg,Object... stat);
	@Override
	public void backResStr(String url, String str,String method,String params,String cookie) {
		String theUrl = statTime(url);
		String msg = "";
		LogManager.print(XHConf.log_tag_net, "d", "------------------返回字符串------------------\n" + url + "\n" + str);
		// 解析API中的res与data
		Map<String, String> result = new HashMap<String, String>();
		if (url.contains(MallStringManager.mall_apiUrl)) {
			// 解析过程
			try {
				result = StringManager.getListMapByJson(str).get(0);
				String resCode = result.get("code");
				String resData = result.get("data");
				if (resCode.equals("0")) {
					loadstat(UtilInternet.REQ_OK_STRING, url, resData,result.get("stat"));
				} else if (resCode.equals("100002")) {// 注册
					loadstat(UtilInternet.REQ_CODE_ERROR, url, result,result.get("stat"));
				} else {
					loadstat(UtilInternet.REQ_CODE_ERROR, url, result,result.get("stat"));
				}
				if(!TextUtils.isEmpty(result.get("stat"))){
					UtilFile.saveShared(XHApplication.in(), FileManager.MALL_STAT, FileManager.MALL_STAT, result.get("stat"));
				}
			
			} catch (Exception e) {
				msg = "解析错误，请重试或反馈给我们";
				result.put("msg", msg);
				loadstat(UtilInternet.REQ_STRING_ERROR, url, result,result.get("stat"));
				XHClick.mapStat(XHApplication.in(), "a_apiError", msg, theUrl, 1);
				XHApiMonitor.monitoringAPI(XHApplication.in(), "解析错误", url, cookie, method, params, "", str);
			}
		} else {
			ArrayList<Map<String, String>> array = StringManager.getListMapByJson(str);
			if (array.size() > 0) {
				result = array.get(0);
				if (url.contains(MallStringManager.mall_getDsToken)) {
					String resCode = result.get("res");
					String resData = result.get("data");
					msg = result.get("data");
					if (resCode.equals("2"))
						loadstat(UtilInternet.REQ_OK_STRING, url, resData,"");
					else if (resCode.equals("1"))
						loadstat(UtilInternet.REQ_CODE_ERROR, url, result,"");
				} else {
					// loadstat(UtilInternet.REQ_OK_STRING,url, result);
				}
			} else {
				msg = "解析错误，请重试或反馈给我们";
				result.put("msg", msg);
				loadstat(UtilInternet.REQ_STRING_ERROR, url, result,"");
				XHClick.mapStat(XHApplication.in(), "a_apiError", msg, theUrl, 1);
				XHApiMonitor.monitoringAPI(XHApplication.in(), "解析错误", url, cookie, method, params, "", str);
			}
		}
		finish();
	}

	@Override
	public void saveCookie(Map<String, String> cookies, String url, String method) {
		
	}

	@Override
	public Map<String, String> getReqHeader(Map<String, String> header, String url, Map<String, String> params) {
		// 配置cookie
		header.put("XH-Client-Data",getCookieStr());
		String isAccept= AppCommon.getConfigByLocal("imageAccept");//1不,sdk版本
		if(!TextUtils.isEmpty(isAccept)){
			Log.i("xianghaTag","isAccept:::"+isAccept);
			Map<String,String> map=StringManager.getFirstMap(isAccept);
			if(!TextUtils.isEmpty(map.get("sdk"))&&Integer.parseInt(map.get("sdk"))<= Build.VERSION.SDK_INT){
				Log.i("xianghaTag","22222:::"+isAccept);
				String accept = header.containsKey("Accept") ? header.get("Accept") : "";
				if(accept.length() > 0){
					if(!accept.contains("image/webp")){
						accept += ";image/webp";
					}
				}else{
					accept = "image/webp";
				}
				header.put("Accept",accept);
			}
		}

		if (!header.containsKey("Connection"))
			header.put("Connection", "keep-alive");
		if (!header.containsKey("Charset"))
			header.put("Charset", XHConf.net_encode);

		return header;
	}

	@Override
	public void backResError(int reqCode, String url, Object obj, String backMsg,String method,String params,String cookie) {
		String[] values = url.split("\\?", 2);
		String theUrl = values[0];
		String msg = "";
		switch (reqCode) {
		case UtilInternet.REQ_FAILD:
			if (obj == null){
				msg = "网络错误，请检查网络或重试";
			} 
			else
				msg = obj.toString();
			break;
		case UtilInternet.REQ_EXP:
			msg = "连接异常，请检查网络或重试";
			XHApiMonitor.monitoringAPI(XHApplication.in(), "连接异常", url, cookie, method, params, LogManager.reportError("网络异常"+url, (Exception) obj), "");
			break;
		case UtilInternet.REQ_STATE_ERROR:
			msg = "服务状态" + obj.toString() + "，请重试或反馈给我们";
			break;
		}
		Map<String, String> map = new HashMap<String, String>();
		map.put("msg", msg);
		XHClick.mapStat(XHApplication.in(), "a_apiError", msg, theUrl, 1);
		super.backResError(reqCode, url, map, backMsg,method,params,cookie);
	}

	/**
	 * toast请求失败的返回值
	 *
	 * @param returnObj
	 * @return
	 */
	private void toastFaildRes(Object returnObj) {
		String returnRes = returnObj.toString();
		if (returnRes.length() > 0) {
			if (Tools.isDebug(XHApplication.in()) || Tools.isOpenRequestTip(XHApplication.in()))
				Tools.showToast(XHApplication.in(), returnRes);
		}
	}

	public String statTime(String url) {
		String[] values = url.split("\\?", 2);
		String theUrl = values[0];
		String time = "0-1s";
		if (requestTime > 1000000)
			requestTime = 999999;
		if (requestTime > 180000)
			time = ">3m";
		else if (requestTime > 60000)
			time = "1m-3m";
		else if (requestTime > 30000)
			time = "30s-1m";
		else if (requestTime > 10000)
			time = "10-30s";
		else if (requestTime > 3000)
			time = "3-10s";
		else if (requestTime > 1000)
			time = "1-3s";
		XHClick.mapStat(XHApplication.in(), "a_apiTime", time, theUrl, (int) requestTime);
		return theUrl;
	}
	
	@Override
	public void finish() {
		super.finish();
	}
}
