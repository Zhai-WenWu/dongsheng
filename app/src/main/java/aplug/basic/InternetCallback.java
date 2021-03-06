package aplug.basic;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.xiangha.R;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;

import acore.logic.MessageTipController;
import acore.logic.XHApiMonitor;
import acore.logic.XHClick;
import acore.logic.ConfigMannager;
import acore.override.XHApplication;
import acore.override.helper.XHActivityManager;
import acore.tools.LogManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import acore.widget.ToastCustom;
import xh.basic.tool.UtilString;

import static acore.logic.ConfigMannager.KEY_IMAGEACCEPT;

/**
 * 网络请求回调类
 * 
 * @author Jerry
 */
public abstract class InternetCallback extends XHInternetCallBack {
	private String encryptparams;
	public InternetCallback() {
		super();
	}
	public InternetCallback(String encryptparams){
		super();
		this.encryptparams= encryptparams;
	}
	public void  setEncryptparams(String encryptparams){
		this.encryptparams= encryptparams;
	}
	@Override
	public void backResIS(String url, InputStream is) {
		statTime(url);
		super.backResIS(url, is);
	}

	@Override
	public void backResStr(String url, String str, String method, String params, String cookie) {
		LogManager.print(XHConf.log_tag_net, "d", "------------------返回字符串------------------\n" + url + "\n" + str);
		String theUrl = statTime(url);
		String msg = "";
		// 解析API中的res与data
		if (url.contains(StringManager.apiUrl)
				|| url.contains(StringManager.api_uploadUserLog)
				|| url.contains(StringManager.api_uploadImg)
				) {
			ArrayList<Map<String, String>> resultList = StringManager.getListMapByJson(str);
			// 解析过程
			if (resultList.size() > 0) {
				Map<String, String> result = resultList.get(0);
				try {
					String resCode = result.get("res");
					String resData = result.get("data");
					String resPower = result.get("power");
					msg = result.get("data");
					try {
						if (result.containsKey("append")) {
							Map<String, String> map = UtilString.getListMapByJson(result.get("append")).get(0);
							ArrayList<Map<String, String>> array = UtilString.getListMapByJson(map.get("popMsg"));
							// 显示提示用户的
							if (array != null && array.size() > 0) {
								// for(Map<String,String> mesMap : array){
								// playAddScoreAnim(context,mesMap.get(""));
								// }
								if(XHActivityManager.getInstance().getCurrentActivity()!=null)playAddScoreAnim(XHActivityManager.getInstance().getCurrentActivity().getApplicationContext(), array);
							} else if (map.containsKey("promptMsg") && !TextUtils.isEmpty(map.get("promptMsg"))) {
								Tools.showToast(XHApplication.in(), map.get("promptMsg"));
							}
							if (XHConf.log_isDebug && map.containsKey("debugMsg")
									&& !TextUtils.isEmpty(map.get("debugMsg"))) {
								if(XHActivityManager.getInstance().getCurrentActivity()!=null)showDialog(XHActivityManager.getInstance().getCurrentActivity(), map.get("debugMsg"));
							}
						}
					} catch (Exception e) {
					}
					if (resCode.equals("2")) {
						//权限处理
						if(!TextUtils.isEmpty(resPower))
							getPower(ReqInternet.REQ_OK_STRING, url, resPower);
						//数据处理 2
						loaded(ReqInternet.REQ_OK_STRING, url, resData);
					} else if (msg.equals("网络不稳定")) {
						msg = "网络不稳定，请重试";
						XHClick.mapStat(XHApplication.in(), "a_apiError", msg, theUrl);
						MessageTipController.newInstance().getCommonData(null);
						loaded(ReqInternet.REQ_CODE_ERROR, url, msg);
					} else {
						loaded(ReqInternet.REQ_CODE_ERROR, url, msg);
//                        toastFaildRes(msg);
                    }
				} catch (Exception e) {
					e.printStackTrace();
					msg = "数据展示异常，请反馈给我们";
					loaded(ReqInternet.REQ_STRING_ERROR, url, msg);
					XHClick.mapStat(XHApplication.in(), "a_apiError", msg, theUrl);
					XHApiMonitor.monitoringAPI(XHApplication.in(), "数据展示异常", url, cookie, method, params, "", str);
				}
			} else {
				msg = "解析错误，请重试或反馈给我们";
				loaded(ReqInternet.REQ_STRING_ERROR, url, msg);
				XHClick.mapStat(XHApplication.in(), "a_apiError", msg, theUrl);
				XHApiMonitor.monitoringAPI(XHApplication.in(), "解析错误", url, cookie, method, params, "", str);
			}
		} else
			loaded(ReqInternet.REQ_OK_STRING, url, str);
		finish();
	}

	public void getPower(int flag, String url, Object obj){}

	private void playAddScoreAnim(Context context, ArrayList<Map<String, String>> resultList) {
		ToastCustom toastCustom = new ToastCustom(context, R.layout.pop_window, resultList);
		toastCustom.show();
	}

	private void showDialog(Context context, String message) {
		LayoutInflater inflater = LayoutInflater.from(context);
		View view = inflater.inflate(R.layout.view_debug_test, null);
		final TextView tv = (TextView) view.findViewById(R.id.tv_hint);
		tv.setText(message);
		final AlertDialog dlg = new AlertDialog.Builder(context).create();
		view.findViewById(R.id.tv_close).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dlg.dismiss();
			}
		});
		dlg.show();
		dlg.setContentView(view);
	}

	@Override
	public void backResError(int reqCode, String url, Object obj, String backMsg, String method, String params,String cookie) {
		String[] values = url.split("\\?", 2);
		String theUrl = values[0];
		String statMsg = "", statContent = theUrl + "_";
		switch (reqCode) {
		case ReqInternet.REQ_FAILD:
			backMsg = "网络错误，请检查网络或重试";
			statMsg = "网络错误";
			statContent += backMsg;
			break;
		case ReqInternet.REQ_EXP:
			Exception e = (Exception) obj;
			backMsg = "连接异常，请检查网络或重试";
			statMsg = "连接异常";
			String expMsg = e.getMessage();
			statContent += expMsg == null ? e.toString() : expMsg;
			XHApiMonitor.monitoringAPI(XHApplication.in(), statMsg, url, cookie, method, params,LogManager.reportError("网络异常" + url, e), "");
			break;
		case ReqInternet.REQ_STATE_ERROR:
			backMsg = "状态错误" + obj.toString() + "，请重试";
			statMsg = "服务状态错误";
			statContent += obj.toString();
			break;
		}
		XHClick.mapStat(XHApplication.in(), "a_apiError", statMsg, statContent);

		// if(statMsg!=""){
		// statContent+="\n时长："+requestTime;
		//// Toast.makeText(context, statMsg+"\n"+statContent,
		// Toast.LENGTH_LONG).show();
		// LinkedHashMap<String, String> map =
		// LogManager.getReportLog("loadError",statMsg,Tools.getAssignTime("yyyy-MM-dd
		// HH:mm:ss", 0),statContent);
		// ReqInternet.in().doPost("http://crash.huher.com:9810/crash/report2",
		// map , new InternetCallback(context) {
		// @Override
		// public void loaded(int flag, String url, Object returnObj) {
		//
		// }
		// });
		// }
		super.backResError(reqCode, url, obj, backMsg, method, params, cookie);
	}

	@Override
	public void saveCookie(Map<String, String> cookies, String url, String method) {
		LogManager.print(XHConf.log_tag_net, "d",
				"------------------接收到cookie------------------\n" + url + "\n" + cookies.toString());
		for (String name : cookies.keySet()) {
			String value = cookies.get(name);
			// 保存cookie
			if (name.equals("USERID") && value.length() > 0) {
				LogManager.print(XHConf.log_tag_net, "d", "------------------保存的cookie------------------\n" + url
						+ "\nname = " + name + " , value = " + value);
				ReqInternet.cookieMap.put(name, value);
				break;
			}
		}
	}

	@Override
	public Map<String, String> getReqHeader(Map<String, String> header, String url, Map<String, String> params) {
		// 配置cookie
//		header.put("Cookie", cookie);
		header.put("XH-Client-Data",getCookieStr());
        if(!TextUtils.isEmpty(url)
				&&(url.contains("main7")||url.contains("Main7")||url.contains("main8")||url.contains("main9"))
				&&!TextUtils.isEmpty(encryptparams)){
			encryptparams=encryptparams.replaceAll("\\n","");
			header.put("xh-parameter", encryptparams);
        }
		String isAccept= ConfigMannager.getConfigByLocal(KEY_IMAGEACCEPT);//isWebp 2表示使用，1不
		if(!TextUtils.isEmpty(isAccept)){
			Map<String,String> map=StringManager.getFirstMap(isAccept);
			if(!TextUtils.isEmpty(map.get("sdk"))&&Integer.parseInt(map.get("sdk"))<= Build.VERSION.SDK_INT){
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

		return super.getReqHeader(header, url, params);
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
		XHClick.mapStat(XHApplication.in(), "a_apiTime", time, theUrl);
//		XHClick.mapStat(context, "a_apiTime", time, theUrl, (int) requestTime);
		return theUrl;
	}
}
