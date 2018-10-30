package acore.logic;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import acore.logic.load.LoadManager;
import acore.override.XHApplication;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;

/**
 * 监控API
 * @author tzy
 * 
 */
public class XHApiMonitor {
	/** 错误报告文件的扩展名 */
	public static final String APIERROR_REPORTER_EXTENSION = ".txt";
	/** 路径  apiError/ */
	public final static String PATH_API_ERROR =  "apiError/";
	/** 图片加载失败次数 */
	public static int ImageErrorCount = 0 ;
			
	/**
	 * 
	 * @param context 上下文
	 * @param controlType 监控类型，例如：网络错误，连接异常，服务状态错误，网络不稳定，数据展示异常，解析异常
	 * @param requestUrl 请求Url
	 * @param requsetCookie 所有cookie
	 * @param requestType 请求类型：post 或 get
	 * @param requestParam 请求参数（可以为空）
	 * @param requestHint 监控提示（可以为空）
	 * @param responseStr 返回字符串（可以为空）
	 */
	public static synchronized void monitoringAPI(Context context , String controlType , String requestUrl , String requsetCookie, String requestType ,
			String requestParam , String requestHint , String responseStr){
		//正常情况是不会有null存在的，所以有null就return
		if(context == null 
				|| controlType == null 
				|| requestUrl == null 
				|| requestType == null 
				|| requsetCookie == null 
				|| requestParam == null 
				|| requestHint == null
				|| responseStr == null){
			return;
		}
		//拼接请求参数的Json串
		Map<String,String> map = new HashMap<>();
		map.put("controlType", controlType);
		String requestDevice = ToolsDevice.getDevice(context) + ToolsDevice.getNetWorkType(context) + "#"
				+ ToolsDevice.getAvailMemory(context) + "#" + ToolsDevice.getPackageName(context) + 
				"#"+StringManager.appID+"#" + LoadManager.tok;
		map.put("requestDevice", requestDevice);
		map.put("requestXhcode", ToolsDevice.getXhCode(context));
		map.put("requestCookie", requsetCookie.replace(" ", "_"));
		map.put("requestUrl", requestUrl);
		map.put("requestType", requestType);
		map.put("requestParam", requestParam);
		map.put("requestHint", requestHint);
		map.put("responseStr", responseStr);
		//上传image错误日志
		uploadErrorInfo(map);
	}
	
	/**
	 * 储存image错误请求信息
	 * @param context
	 */
	public static void monitoringImageRequest(Context context){
		ImageErrorCount++;
		if(ImageErrorCount == 3
				|| ImageErrorCount == 9
				|| ImageErrorCount == 27){
			if(context == null){
				context = XHApplication.in();
			}
			//拼接请求参数的Json串
			Map<String,String> map = new HashMap<>();
			//app网络状态
			map.put("controlType", ToolsDevice.getNetWorkType(context));
			WifiManager wifiManager = (WifiManager) context.getSystemService("wifi");
			DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
			map.put("ipAddress", intToIp(dhcpInfo.ipAddress));
			map.put("dns1", intToIp(dhcpInfo.dns1));
			map.put("dns2", intToIp(dhcpInfo.dns2));
			WifiInfo wifiInfo = wifiManager.getConnectionInfo();
			map.put("wifiIp", wifiInfo.getIpAddress() + "");
			map.put("pingXHStatic", pingIpAddress().replace(" ", "_"));
			//上传image错误日志
			uploadErrorInfo(map);
		}
	}
	
	/**
	 * 上传错误信息
	 * @param json
	 */
	private static void uploadErrorInfo(Map<String,String> map){
		String json = Tools.map2Json(map);
		LinkedHashMap<String, String> paramsMap = new LinkedHashMap<>();
		paramsMap.put("monitoringData", json);
		//发送请求
		ReqInternet.in().doPost(StringManager.api_monitoring, paramsMap , new InternetCallback() {
			@Override public void loaded(int flag, String url, Object msg) {}
		});
	}
	
	/**
	 * 将获取到的int数据转换为ip
	 * @param paramInt
	 * @return
	 */
	private static String intToIp(int paramInt) {
		return (paramInt & 0xFF) 
				+ "." + (0xFF & paramInt >> 8) 
				+ "." + (0xFF & paramInt >> 16) 
				+ "." + (0xFF & paramInt >> 24);
	}
	
	/**
	 * 
	 * @return ping ip 的结果
	 */
	private static String pingIpAddress() {
        String mPingIpAddrResult = "";
	    try {
	        // This is hardcoded IP addr. This is for testing purposes.
	        // We would need to get rid of this before release.
	        String ipAddress = "static.xiangha";
	        Process p = Runtime.getRuntime().exec("ping " + ipAddress);
	        mPingIpAddrResult = Tools.InputStream2String(p.getInputStream()) + "";
	        if(TextUtils.isEmpty(mPingIpAddrResult)){
	        	mPingIpAddrResult = Tools.InputStream2String(p.getErrorStream()) + "";
	        }
	        p.waitFor();
	        p.destroy();
	    } catch (IOException e) {
	        mPingIpAddrResult = "Fail:IOException";
	    } catch (InterruptedException e) {
	    	mPingIpAddrResult = "Fail:InterruptedException";
		}
	    return mPingIpAddrResult;
	}
	
}
